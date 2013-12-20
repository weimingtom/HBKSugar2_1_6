package com.iteye.weimingtom.hbksuger;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.IBinder;
import android.os.PowerManager;
import android.widget.Toast;

public class HBKMusicService extends Service {
	public static final int MUSIC_SERVICE_ID = 10001; //FIXME:
	
    public static final String ACTION_FOREGROUND = "com.iteye.weimingtom.hbksuger.HBKMusicService.FOREGROUND";
    public static final String ACTION_STOP = "com.iteye.weimingtom.hbksuger.HBKMusicService.STOP";
    public static final String ACTION_GETINFO = "com.iteye.weimingtom.hbksuger.HBKMusicService.GETINFO";
    public static final String ACTION_PLAYPAUSE =  "com.iteye.weimingtom.hbksuger.HBKMusicService.PLAYPAUSE";
    
    public static final String EXTRA_MUSIC_NAME = "com.iteye.weimingtom.hbksuger.HBKMusicService.musicName";

    private String musicName;
    private Notification notification;
    private PendingIntent contentIntent;
    private MediaPlayer player;
    
	public void updateUI(String infoText) {
		long duration = 0;
		long progress = 0;
		try {
			if (player != null) {
				progress = player.getCurrentPosition();
				duration = player.getDuration();
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		Intent intent = new Intent(HBKDownloadActivity.ACTION_MUSIC)
			.putExtra(HBKDownloadActivity.EXTRA_MUSIC_NAME, musicName)
			.putExtra(HBKDownloadActivity.EXTRA_MUSIC_INFO, infoText)
			.putExtra(HBKDownloadActivity.EXTRA_MUSIC_PROGRESS, progress)
			.putExtra(HBKDownloadActivity.EXTRA_MUSIC_DURATION, duration);
		sendBroadcast(intent);
	}
	
    @Override
    public void onCreate() {
    	
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        handleCommand(intent, startId);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handleCommand(intent, startId);
        return START_STICKY;
    }
    
    private void log(String str) {
    	if (notification != null) {
	        notification.setLatestEventInfo(this, 
	        		getText(R.string.app_name), 
	        		str, 
	        		contentIntent);
	        startForeground(MUSIC_SERVICE_ID, notification);
    	}
    }
    
    private void handleCommand(Intent intent, int startId) {
    	if (intent != null) {
	        if (ACTION_FOREGROUND.equals(intent.getAction())) {
	        	if (player != null && player.isPlaying()) {
	        		Toast.makeText(this, 
							"音乐未播放完", Toast.LENGTH_SHORT).show();
	        	} else {
	        		if (player != null) {
	        			player.release();
	        			player = null;
	        		}
					this.musicName = intent.getStringExtra(EXTRA_MUSIC_NAME);

					Intent newIntent = new Intent(this, HBKDownloadActivity.class);
					notification = new Notification(R.drawable.ic_launcher, 
			        		"后台音乐", 
			        		System.currentTimeMillis());
					contentIntent = PendingIntent.getActivity(this, 0, newIntent, 0);				
	        		
					boolean isOK = false;
		            try {
		            	isOK = playMusic(startId);
					} catch (Throwable e) {
						e.printStackTrace();
						if (player != null) {
							player.release();
							player = null;
						}
					}
		            
		            if (isOK) {
		        		log("正在播放，点击打开控制台可停止播放");
		            } else {
		        		log("播放失败");
		            }
	        		updateInfo();
	        	}
	        } else if (ACTION_STOP.equals(intent.getAction())) {
				if (player != null && player.isPlaying()) {
					player.release();
					player = null;
					Toast.makeText(this,
						"取消后台播放",
						Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(this,
						"未启动后台播放",
						Toast.LENGTH_SHORT).show();
				}
				stopForeground(true);
				updateInfo();
				stopSelf(startId);
	        } else if (ACTION_GETINFO.equals(intent.getAction())) {
	        	updateInfo();
	        	if (player == null) {
	        		stopSelf(startId);
	        	}
	        } else if (ACTION_PLAYPAUSE.equals(intent.getAction())) {
	        	try {
	        		if (player != null) {
	        			if (player.isPlaying()) {
	        				log("暂停播放，点击打开控制台可停止播放");
	        				player.pause();
	        			} else {
	        				log("正在播放，点击打开控制台可停止播放");
	        				player.start();
	        			}
	        		}
	        	} catch (Throwable e) {
	        		e.printStackTrace();
	        	}
    			updateInfo();
	        }
    	}
    }
    
    private void updateInfo() {
		StringBuffer sb = new StringBuffer();
    	if (player != null && musicName != null) {
			if (player.isPlaying()) {
				sb.append("状态：播放\n");
			} else {
				sb.append("状态：暂停\n");
			}
			sb.append("文件名：" + musicName + "\n");
			sb.append("总长度：" + HBKProgramDetailActivity.MSToString(player.getDuration()) + "\n");
			sb.append("进度：" + HBKProgramDetailActivity.MSToString(player.getCurrentPosition()) + "\n");
			String filename = musicName.replace(".mp3", ".txt");
			String str = getStringFromFile(filename);
			if (str != null) {
				sb.append("下载信息：\n" + str);
			}
    	} else {
    		sb.append("无信息");
    	}
		updateUI(sb.toString());
    }
    
    private boolean playMusic(final int startId) throws IllegalArgumentException, IllegalStateException, IOException {
		player = new MediaPlayer();
		player.setDataSource(musicName);
		player.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				stopForeground(true);
				updateInfo();
				stopSelf(startId);
			}
		});
		player.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);
		player.prepare();
		player.start();
		return true;
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    private String getStringFromFile(String filename) {
    	StringBuffer sb = new StringBuffer();
		InputStream istr = null;
		InputStreamReader reader = null;
		BufferedReader rbuf = null;
		try {
			istr = new FileInputStream(filename);
			reader = new InputStreamReader(istr, "utf8");
			rbuf = new BufferedReader(reader);
			String line;
			while (null != (line = rbuf.readLine())) {
				sb.append(line);
				sb.append('\n');
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (rbuf != null) {
				try {
					rbuf.close();
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
			if (istr != null) {
				try {
					istr.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return sb.toString();
    }
}
