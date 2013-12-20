/*
 * Flazr <http://flazr.com> Copyright (C) 2009  Peter Thomas.
 *
 * This file is part of Flazr.
 *
 * Flazr is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Flazr is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Flazr.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.flazr.io.flv;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

import android.os.Handler;
import android.util.Log;

import com.flazr.rtmp.RtmpHeader;
import com.flazr.rtmp.RtmpMessage;
import com.flazr.rtmp.message.MessageType;
import com.flazr.rtmp.message.MetadataAmf0;
import com.iteye.weimingtom.hbksuger.DownloadThread;

import org.jboss.netty.buffer.ChannelBuffer;

public class FlvWriter {
	private static final boolean D = false; //FIXME:
    private static final String TAG = FlvWriter.class.getSimpleName();

    private final String mp3FileName;
    private final FileChannel out;
    private final int[] channelTimes = new int[RtmpHeader.MAX_CHANNEL_ID];
    private int primaryChannel = -1;
    private int lastLoggedSeconds;
    private final int seekTime;
    private final long startTime;
    private Handler handler;
    private int timerTimeout;
    
    public FlvWriter(final String fileName, Handler handler, int timerTimeout) {
        this(0, fileName, handler, timerTimeout);
    }

    public FlvWriter(final int seekTime, final String fileName, Handler handler, int timerTimeout) {
        this.seekTime = seekTime;
        this.startTime = System.currentTimeMillis();
        this.handler = handler;
        this.timerTimeout = timerTimeout;
        this.mp3FileName = fileName;
        if(fileName == null) {
        	if (D) {
        		Log.i(TAG, "save file notspecified, will only consume stream");
        	}
        	out = null;
            return;
        }
        try {
            File file = new File(fileName);
            FileOutputStream fos = new FileOutputStream(file, true); //FIXME: append
            out = fos.getChannel();
            //FIXME:
            if (false) {
            	out.write(FlvAtom.flvHeader().toByteBuffer());
            }
            if (D) {
            	Log.i(TAG, "opened file for writing: " + file.getAbsolutePath());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }        
    }

    public void close() {
        if(out != null) {
            try {
                out.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        if(primaryChannel == -1) {
        	if (D) {
        		Log.w(TAG, "no media was written, closed file");
        	}
        	return;
        }
        if (D) {
        	Log.i(TAG, "finished in " + ((System.currentTimeMillis() - startTime) / 1000) + " seconds, media duration: " + ((channelTimes[primaryChannel] - seekTime) / 1000) + " seconds (seek time: " + (seekTime / 1000) + ")");
        }
    }

    private void logWriteProgress() {
        final int seconds = (channelTimes[primaryChannel]/* - seekTime*/) / 1000;
        if (seconds >= lastLoggedSeconds + this.timerTimeout) { //FIXME: 10
        	if (D) {
        		Log.i(TAG, "write progress: " + seconds + " seconds");
        	}
        	if (handler != null) {
        		this.handler.sendMessage(
        				this.handler.obtainMessage(DownloadThread.MESSAGE_PROGRESS_THREAD, seconds, 0));
        	}
        	lastLoggedSeconds = seconds - (seconds % this.timerTimeout);
        }
    }

    public void write(final RtmpMessage message) {
        final RtmpHeader header = message.getHeader();
        if(header.isAggregate()) {
            final ChannelBuffer in = message.encode();
            while (in.readable()) {
                final FlvAtom flvAtom = new FlvAtom(in);
                final int absoluteTime = flvAtom.getHeader().getTime();
                channelTimes[primaryChannel] = absoluteTime;
                write(flvAtom);
                // logger.debug("aggregate atom: {}", flvAtom);
                logWriteProgress();
            }
        } else { // METADATA / AUDIO / VIDEO
            final int channelId = header.getChannelId();                        
            channelTimes[channelId] = seekTime + header.getTime();
            if(primaryChannel == -1 && (header.isAudio() || header.isVideo())) {
                if (D) {
                	Log.i(TAG, "first media packet for channel: " + header);
                }
                primaryChannel = channelId;
            }
            if(header.getSize() <= 2) {
                return;
            }
            write(new FlvAtom(header.getMessageType(), channelTimes[channelId], message.encode()));
            if (channelId == primaryChannel) {
                if (header.isAudio()) {
                	DownloadThread.writeMp3FilePos(this.mp3FileName, channelTimes[channelId]);
                }
                logWriteProgress();
            }
        }
    }

    private void write(final FlvAtom flvAtom) {
        if(out == null) {
            return;
        }
        //FIXME:
        if (flvAtom != null && flvAtom.getHeader().getMessageType() == MessageType.METADATA_AMF0) {
        	return;
        }
        if (flvAtom != null && flvAtom.getHeader().getMessageType() == MessageType.VIDEO) {
        	return;
        }
        try {
            out.write(flvAtom.write().toByteBuffer());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
}
