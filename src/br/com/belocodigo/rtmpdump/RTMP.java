package br.com.belocodigo.rtmpdump;

import android.os.Handler;

/**
 * @see HBKProgramDetailActivity
 * @see HBKDownloadRTMPService
 * @author Administrator
 *
 */
public class RTMP {
	private Handler handler;
	private long timestamp;
	private double mDuration;
	private String mp3FileName;
	
	public RTMP(Handler handler, String mp3FileName) {
		this.handler = handler;
		this.mp3FileName = mp3FileName;
	}
	
	public native void init(String url, String dest, boolean resume, boolean verbose);
	public native void stop();

	private void log(String str) {
		this.handler.sendMessage(handler.obtainMessage(RTMPDownloadThread.MESSAGE_LOG_THREAD, 0, 0, str));
	}
	
	private void progress(long size, long timestamp, double duration) {
		log("size " + size + ", " + (int)((timestamp / (duration * 1000) * 100) * 10.0) / 10.0 + "%\n");
		RTMPDownloadThread.writeMp3FilePos(this.mp3FileName, (int)timestamp);
		if (this.mDuration != duration) {
			this.mDuration = duration;
			if (this.mDuration > 0) {
				this.handler.sendMessage(handler.obtainMessage(RTMPDownloadThread.MESSAGE_DURATION_THREAD, 0, 0, Double.valueOf(this.mDuration)));
			}
		}
		this.handler.sendMessage(handler.obtainMessage(RTMPDownloadThread.MESSAGE_PROGRESS_THREAD, (int)(timestamp / 1000.0), 0, null));
	}
	
	static {
    	System.loadLibrary("rtmp");
        //System.loadLibrary("rtmpdump");
    }
}
