package com.iteye.weimingtom.hbksuger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import android.os.Handler;
import android.util.Log;

import com.flazr.rtmp.client.RtmpClient;

public class DownloadThread extends Thread {
	private final static boolean D = false;
	private final static String TAG = "DownloadThread";
	
	public final static int MESSAGE_START_THREAD = 5551;
	public final static int MESSAGE_CLOSE_THREAD = 5552;
	public final static int MESSAGE_DURATION_THREAD = 5553;
	public final static int MESSAGE_PROGRESS_THREAD = 5554;
	
	private int startId;
	private String url, mp3filename;
	private volatile boolean isStop = false;
	private Object isStopLock = new Object();
	private RtmpClient rtmpClient;
	private Handler handler;
	private String infoText;
	private String tabUrl;
	private String pageUrl;
	private int timerTimeout;
	
	private int startPos;
	
	public DownloadThread(int startId, String url, String mp3filename, String infoText, String tabUrl, String pageUrl, Handler handler, int timerTimeout) {
		this.startId = startId;
		this.url = url;
		this.mp3filename = mp3filename;
		this.rtmpClient = new RtmpClient();
		this.handler = handler;
		this.infoText = infoText;
		this.tabUrl = tabUrl;
		this.pageUrl = pageUrl;
		this.timerTimeout = timerTimeout;
	}
	
	public void setStop(boolean isStop) {
		synchronized (isStopLock) {
			this.isStop = isStop;
			this.rtmpClient.tryClose();
		}
	}

	public boolean getStop() {
		synchronized (isStopLock) {
			return this.isStop;
		}
	}
	
	@Override
	public void run() {
		setStop(false);
		if (this.handler != null) {
			this.handler.sendMessage(
					this.handler.obtainMessage(MESSAGE_START_THREAD));
		}
		this.startPos = readMp3FilePos(this.mp3filename);
		if (D) {
			Log.d(TAG, "startPos == " + startPos);
		}
		rtmpClient.tryConnect(this.handler, this.timerTimeout, startPos, new String[]{this.url, this.mp3filename});
		setStop(true);
		if (this.handler != null) {
			this.handler.sendMessage(
					this.handler.obtainMessage(MESSAGE_CLOSE_THREAD, startId, 0));
		}
	}
	
	public void writeTxtFile(int duration) {
		if (this.mp3filename != null && this.mp3filename.contains(".mp3")) {
			String txtfilename = this.mp3filename.replace(".mp3", ".txt");
			FileOutputStream fos = null;
			OutputStreamWriter writer = null;
			BufferedWriter obuf = null;
			try {
				fos = new FileOutputStream(txtfilename);
				writer = new OutputStreamWriter(fos, "UTF-8");
				obuf = new BufferedWriter(writer);
				obuf.write(HBKProgramDetailActivity.getTimeString() + "\n"); // [... ...]
				obuf.write("length = " + duration + "\n");
				obuf.write(this.tabUrl + "\n");
				obuf.write(this.pageUrl + "\n");
				obuf.write(this.url + "\n");
				obuf.write(this.infoText + "\n");
				obuf.flush();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (obuf != null) {
					try {
						obuf.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (writer != null) {
					try {
						writer.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (fos != null) {
					try {
						fos.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	public static int readMp3FilePos(String mp3file) {
		int pos = -1;
		if (mp3file != null && mp3file.contains(".mp3")) {
			String txtfilename = mp3file.replace(".mp3", ".pos");
			FileInputStream fis = null;
			InputStreamReader reader = null;
			BufferedReader ibuf = null;
			try {
				fis = new FileInputStream(txtfilename);
				reader = new InputStreamReader(fis, "UTF-8");
				ibuf = new BufferedReader(reader);
				String line = null;
				do {
					line = ibuf.readLine();
					if (line != null && line.length() > 0) {
						if (D) {
							Log.d(TAG, "readMp3FilePos : " + line);
						}
						pos = Integer.parseInt(line);
					}
				} while (line != null);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (ibuf != null) {
					try {
						ibuf.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (reader != null) {
					try {
						reader.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (fis != null) {
					try {
						fis.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return pos;
	}
	
	public static void writeMp3FilePos(String mp3file, int pos) {
//		if (D) {
//			Log.d(TAG, "startPos >> " + pos);
//		}
		if (mp3file != null && mp3file.contains(".mp3")) {
			String txtfilename = mp3file.replace(".mp3", ".pos");
			FileOutputStream fos = null;
			OutputStreamWriter writer = null;
			BufferedWriter obuf = null;
			try {
				fos = new FileOutputStream(txtfilename);
				writer = new OutputStreamWriter(fos, "UTF-8");
				obuf = new BufferedWriter(writer);
				obuf.write("" + pos + "\n");
				obuf.flush();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (obuf != null) {
					try {
						obuf.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (writer != null) {
					try {
						writer.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (fos != null) {
					try {
						fos.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}
