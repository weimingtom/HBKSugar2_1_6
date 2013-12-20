package com.iteye.weimingtom.hbksuger;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.widget.Toast;

public class HBKDownloadService extends Service {
	public static final int DOWNLOAD_SERVICE_ID = 10000;
	
    public static final String ACTION_FOREGROUND = "com.iteye.weimingtom.hbksuger.HBKDownloadService.FOREGROUND";
    public static final String ACTION_STOP = "com.iteye.weimingtom.hbksuger.HBKDownloadService.STOP";
    public static final String ACTION_UPDATE = "com.iteye.weimingtom.hbksuger.HBKDownloadService.UPDATE";

    public static final String EXTRA_URL = "com.iteye.weimingtom.hbksuger.HBKDownloadService.url";
    public static final String EXTRA_MP3_FILENAME = "com.iteye.weimingtom.hbksuger.HBKDownloadService.mp3filename";
    public static final String EXTRA_INFO_TEXT = "com.iteye.weimingtom.hbksuger.HBKDownloadService.infoText";
    public static final String EXTRA_TAB_URL = "com.iteye.weimingtom.hbksuger.HBKDownloadService.tabUrl";
    public static final String EXTRA_DETAIL_URL = "com.iteye.weimingtom.hbksuger.HBKDownloadService.detailUrl";
    
	private DownloadHandler downloadHandler;
    private DownloadThread downloadThread;
    private String url, mp3filename, infoText, tabUrl, detailUrl;
    
    private double duration;
    private int currentProgress;
    private Notification notification;
    private PendingIntent contentIntent;
    
    private boolean isShowLog;
    private PowerManager.WakeLock wakeLock = null;
    
    private int retryNum = 0;
    
	private class DownloadHandler extends Handler {
		private long lastTime = 0;
		
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch(msg.what) {
			case DownloadThread.MESSAGE_START_THREAD:
				log("线程启动");
				break;
				
			case DownloadThread.MESSAGE_CLOSE_THREAD:
				if (getIsRetry() && 
					currentProgress > 0 && 
					duration > 0 && 
					currentProgress < duration) { //FIXME:检查下载是否已完成
					retryNum++;
					//log("重试开始：" + retryNum);
					//isShowLog = false;
					updateUI(true);
					downloadThread = new DownloadThread(msg.arg1, url, mp3filename, infoText, tabUrl, detailUrl, downloadHandler, 1);
					downloadThread.start();
				} else {
					log("线程关闭");
		    		if (wakeLock != null) {
		    			wakeLock.release();
		    			wakeLock = null;
		    		}
					stopForeground(true);
		        	isShowLog = false;
		        	updateUI(true);
					stopSelf(msg.arg1);
				}
				break;

			case DownloadThread.MESSAGE_DURATION_THREAD:
				if (msg.obj instanceof Double) {
					currentProgress = 0;
					duration = (Double)msg.obj;
					log("总长度:" + duration + "秒");
					if (downloadThread != null) {
						downloadThread.writeTxtFile((int)(duration * 1000));
					}
					updateUI(false);
				}
				break;
				
			case DownloadThread.MESSAGE_PROGRESS_THREAD:
				currentProgress = msg.arg1;
				float percent = (duration != 0) ? (float)(((double)currentProgress / duration) * 100) : 0;
				long currentTime = System.currentTimeMillis();
				if (currentTime - lastTime > 1000) {
					lastTime = currentTime;
					log(((int)(percent * 100) / 100.0) + "%" + 
							"(" + HBKProgramDetailActivity.MSToString(((int)duration - currentProgress) * 1000) +"/" +
							HBKProgramDetailActivity.MSToString(((int)duration) * 1000) + ")" + "(" + retryNum + ")"
							);
				}
				updateUI(false);
				break;
			}
		}
	}
    
	public void updateUI(boolean isOver) {
		float percent = (duration != 0) ? (float)(((double)currentProgress / duration) * 100) : 0;
		String percentInfo = (((int)(percent * 100) / 100.0) + "%" + 
				"(" + HBKProgramDetailActivity.MSToString(((int)duration - currentProgress) * 1000) +"/" +
				HBKProgramDetailActivity.MSToString(((int)duration) * 1000) +")"
				);
		percentInfo += "\n重试次数：" + retryNum + " ";
		if (isOver) {
			percentInfo += "线程已结束";
		}
		Intent intent = new Intent(HBKDownloadActivity.ACTION_UPDATE)
			.putExtra(HBKDownloadActivity.EXTRA_UPDATE_PROGRESS, currentProgress)
			.putExtra(HBKDownloadActivity.EXTRA_UPDATE_DURATION, (int)duration)
			.putExtra(HBKDownloadActivity.EXTRA_UPDATE_PERCENT, percentInfo)
			.putExtra(HBKDownloadActivity.EXTRA_UPDATE_INFO, infoText)
			.putExtra(HBKDownloadActivity.EXTRA_UPDATE_MP3, mp3filename);
		sendBroadcast(intent);
	}
	
    @Override
    public void onCreate() {
    	super.onCreate();
    	downloadHandler = new DownloadHandler();
        isShowLog = false;
    }

    @Override
    public void onDestroy() {
    	super.onDestroy();
    	if (wakeLock != null) {
    		wakeLock.release();
    		wakeLock = null;
    	}
    	stopForeground(true);
        isShowLog = false;
    }

    @Override
    public void onStart(Intent intent, int startId) {
    	super.onStart(intent, startId);
        handleCommand(intent, startId);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handleCommand(intent, startId);
        return START_STICKY;
    }
    
    private void log(String str) {
    	if (isShowLog && contentIntent != null && notification != null) {
	        notification.setLatestEventInfo(this, 
	        		getText(R.string.app_name), 
	        		str, 
	        		contentIntent);
	        startForeground(DOWNLOAD_SERVICE_ID, notification);
    	}
    }
    
    private void handleCommand(Intent intent, int startId) {
    	if (intent != null) {
	        if (ACTION_FOREGROUND.equals(intent.getAction())) {
	        	if (downloadThread == null || 
	        		(downloadThread != null && downloadThread.getStop())) {
		            
		            this.url = intent.getStringExtra(EXTRA_URL);
					this.mp3filename = intent.getStringExtra(EXTRA_MP3_FILENAME);
					this.infoText = intent.getStringExtra(EXTRA_INFO_TEXT);
					this.tabUrl = intent.getStringExtra(EXTRA_TAB_URL);
					this.detailUrl = intent.getStringExtra(EXTRA_DETAIL_URL);
					
					startDownload(startId);
	        	} else {
					Toast.makeText(this, 
						"下载服务未停止", Toast.LENGTH_SHORT).show();
	        	}
	        } else if (ACTION_STOP.equals(intent.getAction())) {
	    		if (wakeLock != null) {
	    			wakeLock.release();
	    			wakeLock = null;
	    		}
				stopForeground(true);
	        	isShowLog = false;
				if (downloadThread != null && !downloadThread.getStop()) {
					downloadThread.setStop(true);
					try {
						downloadThread.join(50);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					downloadThread = null;
					log("hbksugar取消下载");
				} else {
					log("未启动下载线程");
				}
				stopSelf(startId);
	        } else if (ACTION_UPDATE.equals(intent.getAction())) {
	        	updateUI(false);
	        	if (downloadThread == null) {
	        		stopSelf(startId);
	        	}
	        }
    	}
    }
    
    private void startDownload(int startId) {
		Intent newIntent = new Intent(this, HBKDownloadActivity.class)
			.putExtra(HBKDownloadActivity.EXTRA_URL, url)
			.putExtra(HBKDownloadActivity.EXTRA_MP3_FILENAME, mp3filename)
			.putExtra(HBKDownloadActivity.EXTRA_INFO_TEXT, infoText)
			.putExtra(HBKDownloadActivity.EXTRA_TAB_URL, tabUrl)
			.putExtra(HBKDownloadActivity.EXTRA_DETAIL_URL, detailUrl);
	    if (infoText != null) {
			notification = new Notification(R.drawable.ic_launcher, 
	        		"开始下载:" + infoText, 
	        		System.currentTimeMillis());
	    } else {
			notification = new Notification(R.drawable.ic_launcher, 
	        		"开始下载", 
	        		System.currentTimeMillis());			        	
	    }
		contentIntent = PendingIntent.getActivity(this, 0, newIntent, 0);				
		
		isShowLog = true;
		log("连接中...");
		
		if (wakeLock != null) {
			wakeLock.release();
		}
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "HBKDownloadService");
		wakeLock.acquire();
		
		retryNum = 0;
	    downloadThread = new DownloadThread(startId, url, mp3filename, infoText, tabUrl, detailUrl, downloadHandler, 1);
		downloadThread.start();
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    private boolean getIsRetry() {
    	return HBKDownloadActivity.getLastEnableRetry(this);
    }
}
