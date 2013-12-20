package br.com.belocodigo.rtmpdump;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import com.iteye.weimingtom.hbksuger.HBKProgramDetailActivity;

import android.os.Handler;
import android.util.Log;

public class RTMPDownloadThread extends Thread {
	private final static boolean D = false;
	private final static String TAG = "RTMPDownloadThread";
	
	private final static boolean D_VERBOSE = false;
	
	public final static int MESSAGE_START_THREAD = 1001;
	public final static int MESSAGE_CLOSE_THREAD = 1002;
	public final static int MESSAGE_DURATION_THREAD = 1003;
	public final static int MESSAGE_PROGRESS_THREAD = 1004;
	public final static int MESSAGE_LOG_THREAD = 1005;
	
	private volatile boolean isStop = false;
	private Object isStopLock = new Object();
	
	private RTMP rtmp;
	private String url, mp3filename;
	private Handler handler;
	private int startId;
	
	private String infoText;
	private String tabUrl;
	private String pageUrl;
	private int timerTimeout;
	
	private int startPos;
	
	private boolean isResume = false;
	
	public RTMPDownloadThread(int startId, String url, String mp3filename, String infoText, String tabUrl, String pageUrl, Handler handler, int timerTimeout) {
		this.handler = handler;
		this.rtmp = new RTMP(handler, mp3filename);
		this.url = url;
		this.mp3filename = mp3filename;
		this.startId = startId;
		
		this.infoText = infoText;
		this.tabUrl = tabUrl;
		this.pageUrl = pageUrl;
		this.timerTimeout = timerTimeout;
	}
	
	public void setResume(boolean isResume) {
		this.isResume = isResume;
	}
	
	public void setStop(boolean isStop) {
		synchronized (isStopLock) {
			this.isStop = isStop;
			stopRTMP();
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
					handler.obtainMessage(MESSAGE_START_THREAD));
		}
		this.startPos = readMp3FilePos(this.mp3filename);
		if (D) {
			Log.d(TAG, "startPos == " + startPos);
		}
		rtmp.init(url, mp3filename, isResume, D_VERBOSE);
		setStop(true);
		if (this.handler != null) {
			this.handler.sendMessage(
					handler.obtainMessage(MESSAGE_CLOSE_THREAD, this.startId, 0, null));
		}
	}
	
	public void stopRTMP() {
		if (rtmp != null) {
			rtmp.stop();
		}
	}
	
	public void writeTxtFile(int duration) {
		if (this.mp3filename != null && this.mp3filename.contains(".flv")) {
			String txtfilename = this.mp3filename.replace(".flv", ".txt");
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
		if (mp3file != null && mp3file.contains(".flv")) {
			String txtfilename = mp3file.replace(".flv", ".pos");
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
		if (mp3file != null && mp3file.contains(".flv")) {
			String txtfilename = mp3file.replace(".flv", ".pos");
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
