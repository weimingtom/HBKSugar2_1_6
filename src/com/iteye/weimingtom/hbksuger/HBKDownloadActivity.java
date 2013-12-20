package com.iteye.weimingtom.hbksuger;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Hashtable;

import br.com.belocodigo.rtmpdump.HBKDownloadRTMPService;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class HBKDownloadActivity extends Activity {
	private Button buttonStartForeground;
	private Button buttonStop;
    private Button buttonOpen;
    private Button buttonOpenDownnloading;
    private Button buttonDelete;
    private Button buttonStopServiceMusic;
    private Button buttonStartGetMusicInfo;
    private Button buttonPlayPauseMusic;
    private Button buttonUpdateCacheFileInfo;
    private Button buttonUpdateDownloading;

    private CheckBox checkBoxEnableRetry;
    
    public static final String EXTRA_URL = "com.iteye.weimingtom.hbksuger.HBKDownloadActivity.url";
    public static final String EXTRA_MP3_FILENAME = "com.iteye.weimingtom.hbksuger.HBKDownloadActivity.mp3filename";
    public static final String EXTRA_INFO_TEXT = "com.iteye.weimingtom.hbksuger.HBKDownloadActivity.infoText";
    public static final String EXTRA_TAB_URL = "com.iteye.weimingtom.hbksuger.HBKDownloadActivity.tabUrl";
    public static final String EXTRA_DETAIL_URL = "com.iteye.weimingtom.hbksuger.HBKDownloadActivity.detailUrl";

    public static final String ACTION_UPDATE = "com.iteye.weimingtom.hbksuger.HBKDownloadActivity.UPDATE";
    public static final String EXTRA_UPDATE_PROGRESS = "com.iteye.weimingtom.hbksuger.HBKDownloadActivity.UPDATE_PROGRESS";
    public static final String EXTRA_UPDATE_DURATION = "com.iteye.weimingtom.hbksuger.HBKDownloadActivity.UPDATE_DURATION";
    public static final String EXTRA_UPDATE_PERCENT = "com.iteye.weimingtom.hbksuger.HBKDownloadActivity.UPDATE_PERCENT";
    public static final String EXTRA_UPDATE_INFO = "com.iteye.weimingtom.hbksuger.HBKDownloadActivity.UPDATE_INFO";
    public static final String EXTRA_UPDATE_MP3 = "com.iteye.weimingtom.hbksuger.HBKDownloadActivity.UPDATE_MP3";
    
    public static final String ACTION_MUSIC = "com.iteye.weimingtom.hbksuger.HBKDownloadActivity.MUSIC";
    public static final String EXTRA_MUSIC_NAME = "com.iteye.weimingtom.hbksuger.HBKDownloadActivity.MUSIC_NAME";
    public static final String EXTRA_MUSIC_INFO = "com.iteye.weimingtom.hbksuger.HBKDownloadActivity.MUSIC_INFO";
    public static final String EXTRA_MUSIC_PROGRESS = "com.iteye.weimingtom.hbksuger.HBKDownloadActivity.MUSIC_PROGRESS";
    public static final String EXTRA_MUSIC_DURATION = "com.iteye.weimingtom.hbksuger.HBKDownloadActivity.MUSIC_DURATION";
    
    private static final String SHARE_PREF_NAME = "pref";
	private static final String SHARE_PREF_ENABLE_RETRY = "enableRetry";
	
    private static final int MESSAGE_GETINFO = 7777;
    
	private String url, mp3filename, infoText, tabUrl, detailUrl;
    private TextView textViewTitle;
    private TextView textViewLog;
    private TextView textViewUpdateInfo;
    private ProgressBar progressBar1;
    private TextView textViewServiceMusicLog;
    private LinearLayout linearLayoutTitle;
    private TextView textViewCacheFileInfo;
    private ImageView imageViewDownloadingThumbnail;
    private ProgressBar progressBarMusic;
    private ImageView imageViewMusicThumbnail;
    
    private String downloadingMp3filename;
    
    private final static int DELAY = 500;
    private boolean isGettingInfo = false;
    private Handler getInfoHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (msg != null && msg.what == MESSAGE_GETINFO) {
				requestMusicInfo();
				if (isGettingInfo) {
					this.sendMessageDelayed(this.obtainMessage(MESSAGE_GETINFO), DELAY);
				}
			}
		}
    };
    
	private static final class FileInfo {
		public String filename;
		public long size;
		
		public FileInfo(String filename, long size) {
			this.filename = filename;
			this.size = size;
		}
	}
    
	public class MyReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent != null) {
				if (ACTION_UPDATE.equals(intent.getAction())) {
					int progress = intent.getIntExtra(EXTRA_UPDATE_PROGRESS, 0);
					int duration = intent.getIntExtra(EXTRA_UPDATE_DURATION, 0);
					String percent = intent.getStringExtra(EXTRA_UPDATE_PERCENT);
					String info = intent.getStringExtra(EXTRA_UPDATE_INFO);
					downloadingMp3filename = intent.getStringExtra(EXTRA_UPDATE_MP3);
					
					if (progressBar1 != null) {
						if (duration != 0) {
							progressBar1.setVisibility(ProgressBar.VISIBLE);
							progressBar1.setProgress(progress);
							progressBar1.setMax(duration);
						} else {
							progressBar1.setVisibility(ProgressBar.GONE);
						}
					}
					
					StringBuffer sb = new StringBuffer();
					if (downloadingMp3filename != null) {
						sb.append(downloadingMp3filename);
						sb.append("\n");				
					}
					if (info != null) {
						sb.append(info);
						sb.append("\n");
					}
					if (percent != null) {
						sb.append("下载进度：\n");
						sb.append(percent);
						sb.append("\n");
					}
					textViewUpdateInfo.setText(sb.toString());
					if (downloadingMp3filename != null) {
						String preName = downloadingMp3filename.substring(0, downloadingMp3filename.lastIndexOf("_download_"));
						String downloadingJPGfilename = preName + ".jpg";
						imageViewDownloadingThumbnail.setImageURI(Uri.fromFile(new File(downloadingJPGfilename)));
					}
					
				} else if (ACTION_MUSIC.equals(intent.getAction())) {
					String musicFileName = intent.getStringExtra(EXTRA_MUSIC_NAME);
					String musicInfo = intent.getStringExtra(EXTRA_MUSIC_INFO);
					long progress = intent.getLongExtra(EXTRA_MUSIC_PROGRESS, 0);
					long duration = intent.getLongExtra(EXTRA_MUSIC_DURATION, 0);
					if (textViewServiceMusicLog != null) {
						textViewServiceMusicLog.setText(musicInfo);
					}
					if (musicFileName != null) {
						String preName = musicFileName.substring(0, musicFileName.lastIndexOf("_download_"));
						String musicJPGfilename = preName + ".jpg";
						imageViewMusicThumbnail.setImageURI(Uri.fromFile(new File(musicJPGfilename)));
					}
					if (progressBarMusic != null) {
						if (duration != 0) {
							progressBarMusic.setVisibility(ProgressBar.VISIBLE);
							progressBarMusic.setProgress((int)(progress / 1000));
							progressBarMusic.setMax((int)(duration / 1000));
						} else {
							progressBarMusic.setVisibility(ProgressBar.GONE);						
						}
					}
				}
			}
		}
	}
	
    private MyReceiver receiver;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.download_service);
        
        textViewLog = (TextView) this.findViewById(R.id.textViewLog);
        progressBar1 = (ProgressBar) this.findViewById(R.id.progressBar1);
        textViewUpdateInfo = (TextView) this.findViewById(R.id.textViewUpdateInfo);
        buttonOpenDownnloading = (Button) this.findViewById(R.id.buttonOpenDownnloading);
        buttonDelete = (Button) this.findViewById(R.id.buttonDelete);
        imageViewDownloadingThumbnail = (ImageView) this.findViewById(R.id.imageViewDownloadingThumbnail);
        progressBarMusic = (ProgressBar) this.findViewById(R.id.progressBarMusic);
        imageViewMusicThumbnail = (ImageView) this.findViewById(R.id.imageViewMusicThumbnail);
        
        textViewServiceMusicLog = (TextView) this.findViewById(R.id.textViewServiceMusicLog);
        buttonStopServiceMusic = (Button) this.findViewById(R.id.buttonStopServiceMusic);
        buttonPlayPauseMusic = (Button) this.findViewById(R.id.buttonPlayPauseMusic);
        
        textViewCacheFileInfo = (TextView) this.findViewById(R.id.textViewCacheFileInfo);
        buttonUpdateCacheFileInfo = (Button) this.findViewById(R.id.buttonUpdateCacheFileInfo);
        
        buttonUpdateDownloading = (Button) this.findViewById(R.id.buttonUpdateDownloading);

        checkBoxEnableRetry = (CheckBox) this.findViewById(R.id.checkBoxEnableRetry);
        
        progressBar1.setVisibility(ProgressBar.GONE);
        progressBarMusic.setVisibility(ProgressBar.GONE);
        
        Intent intent = this.getIntent();
        if (intent != null) {
        	
	        this.url = intent.getStringExtra(EXTRA_URL);
			this.mp3filename = intent.getStringExtra(EXTRA_MP3_FILENAME);
			this.infoText = intent.getStringExtra(EXTRA_INFO_TEXT);
			this.tabUrl = intent.getStringExtra(EXTRA_TAB_URL);
			this.detailUrl = intent.getStringExtra(EXTRA_DETAIL_URL);
			
			StringBuffer sb = new StringBuffer();
			if (this.tabUrl != null) {
				sb.append(this.tabUrl);
				sb.append('\n');
			}
			if (this.detailUrl != null) {
				sb.append(this.detailUrl);
				sb.append('\n');
			}
			if (this.url != null) {
				sb.append(this.url);
				sb.append('\n');
			}
			if (this.mp3filename != null) {
				sb.append(this.mp3filename);
				sb.append('\n');
			}
			if (this.infoText != null) {
				sb.append(this.infoText);
				sb.append('\n');
			}
			textViewLog.setText(sb.toString());
			/*
			if (mp3filename != null) {
				String preName = mp3filename.substring(0, mp3filename.lastIndexOf("_download_"));
				String musicJPGfilename = preName + ".jpg";
				imageViewMusicThumbnail.setImageURI(Uri.fromFile(new File(musicJPGfilename)));
			}
			*/
        }
        
		buttonOpenDownnloading.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (downloadingMp3filename != null && downloadingMp3filename.length() > 0) {
					startActivity(new Intent(HBKDownloadActivity.this, HBKMediaPlayerActivity.class)
							.putExtra(HBKMediaPlayerActivity.EXTRA_FILENAME, downloadingMp3filename));
				} else {
					Toast.makeText(HBKDownloadActivity.this, 
							"路径为空，请先下载", Toast.LENGTH_SHORT).show();
				}
			}
		});
        buttonOpenDownnloading.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				if (downloadingMp3filename != null && downloadingMp3filename.length() > 0) {
					Intent intent = new Intent();
					intent.setAction(Intent.ACTION_VIEW);
					if (downloadingMp3filename.endsWith(".flv")) {
						intent.setDataAndType(Uri.fromFile(new File(downloadingMp3filename)), "video/*");
					} else {
						intent.setDataAndType(Uri.fromFile(new File(downloadingMp3filename)), "audio/*");
					}
					try {
						startActivity(intent);
					} catch (Throwable e) {
						e.printStackTrace();
						Toast.makeText(HBKDownloadActivity.this, 
							"找不到可用的应用程序", Toast.LENGTH_SHORT)
							.show();
					}
				} else {
					Toast.makeText(HBKDownloadActivity.this, 
							"路径为空，请先下载", Toast.LENGTH_SHORT).show();
				}
				return true;
			}
		});
        
        
        buttonStartForeground = (Button)findViewById(R.id.buttonStartForeground);
        buttonStop = (Button)findViewById(R.id.buttonStop);
        textViewTitle = (TextView)findViewById(R.id.textViewTitle);
        buttonOpen = (Button) findViewById(R.id.buttonOpen);
        linearLayoutTitle = (LinearLayout) this.findViewById(R.id.linearLayoutTitle);
        buttonStartGetMusicInfo = (Button) this.findViewById(R.id.buttonStartGetMusicInfo);
        
        textViewTitle.setText("服务控制台");
		textViewTitle.setTextColor(0xff000000);
		linearLayoutTitle.setBackgroundColor(0xfffcabbd);
		
        buttonStartForeground.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (url != null) {
                	if (false) {
	                    Intent intent = new Intent(HBKDownloadService.ACTION_FOREGROUND);
	                    intent.setClass(HBKDownloadActivity.this, HBKDownloadService.class);
	                    intent.putExtra(HBKDownloadService.EXTRA_URL, url);
	                    intent.putExtra(HBKDownloadService.EXTRA_MP3_FILENAME, mp3filename);
	                    intent.putExtra(HBKDownloadService.EXTRA_INFO_TEXT, infoText);
	                    intent.putExtra(HBKDownloadService.EXTRA_TAB_URL, tabUrl);
	                    intent.putExtra(HBKDownloadService.EXTRA_DETAIL_URL, detailUrl);
	                    startService(intent);
	                } else {
	                    Intent intent = new Intent(HBKDownloadRTMPService.ACTION_FOREGROUND);
	                    intent.setClass(HBKDownloadActivity.this, HBKDownloadRTMPService.class);
	                    intent.putExtra(HBKDownloadRTMPService.EXTRA_URL, url);
	                    intent.putExtra(HBKDownloadRTMPService.EXTRA_MP3_FILENAME, mp3filename);
	                    intent.putExtra(HBKDownloadRTMPService.EXTRA_INFO_TEXT, infoText);
	                    intent.putExtra(HBKDownloadRTMPService.EXTRA_TAB_URL, tabUrl);
	                    intent.putExtra(HBKDownloadRTMPService.EXTRA_DETAIL_URL, detailUrl);
	                    startService(intent);
	                }
                }
            }
        });
        
        buttonUpdateDownloading.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	if (false) {
	                Intent intent = new Intent(HBKDownloadService.ACTION_UPDATE);
	                intent.setClass(HBKDownloadActivity.this, HBKDownloadService.class);
	                startService(intent);
	                stopService(new Intent(HBKDownloadActivity.this,
	                        HBKDownloadService.class));
	            } else {
	                Intent intent = new Intent(HBKDownloadRTMPService.ACTION_UPDATE);
	                intent.setClass(HBKDownloadActivity.this, HBKDownloadRTMPService.class);
	                startService(intent);
	                stopService(new Intent(HBKDownloadActivity.this,
	                		HBKDownloadRTMPService.class));
	            }
            }
        });
        
        buttonStop.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	if (false) {
	                Intent intent = new Intent(HBKDownloadService.ACTION_STOP);
	                intent.setClass(HBKDownloadActivity.this, HBKDownloadService.class);
	                startService(intent);
	                stopService(new Intent(HBKDownloadActivity.this,
	                        HBKDownloadService.class));
	            } else {
	                Intent intent = new Intent(HBKDownloadRTMPService.ACTION_STOP);
	                intent.setClass(HBKDownloadActivity.this, HBKDownloadRTMPService.class);
	                startService(intent);
	                stopService(new Intent(HBKDownloadActivity.this,
	                		HBKDownloadRTMPService.class));	            	
            	}
            }
        });
        
		buttonOpen.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mp3filename != null && mp3filename.length() > 0) {
					startActivity(new Intent(HBKDownloadActivity.this, HBKMediaPlayerActivity.class)
						.putExtra(HBKMediaPlayerActivity.EXTRA_FILENAME, mp3filename));
				} else {
					Toast.makeText(HBKDownloadActivity.this, 
							"路径为空，请先下载", Toast.LENGTH_SHORT).show();
				}
			}
		});
		buttonOpen.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				if (mp3filename != null && mp3filename.length() > 0) {
					Intent intent = new Intent();
					intent.setAction(Intent.ACTION_VIEW);
					intent.setDataAndType(Uri.fromFile(new File(mp3filename)), "audio/*");
					try {
						startActivity(intent);
					} catch (Throwable e) {
						e.printStackTrace();
						Toast.makeText(HBKDownloadActivity.this, 
							"找不到可用的应用程序", Toast.LENGTH_SHORT)
							.show();
					}
				} else {
					Toast.makeText(HBKDownloadActivity.this, 
							"路径为空，请先下载", Toast.LENGTH_SHORT).show();
				}
				return true;
			}
		});
		buttonDelete.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				deleteSmallFiles();
			}
		});
		buttonStopServiceMusic.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
                Intent intent = new Intent(HBKMusicService.ACTION_STOP);
                intent.setClass(HBKDownloadActivity.this, HBKMusicService.class);
                startService(intent);
			}
		});
		buttonStartGetMusicInfo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				requestMusicInfo();
			}
		});
		buttonPlayPauseMusic.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
                Intent intent = new Intent(HBKMusicService.ACTION_PLAYPAUSE);
                intent.setClass(HBKDownloadActivity.this, HBKMusicService.class);
                startService(intent);
			}
		});
		buttonUpdateCacheFileInfo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
                updateCacheFileInfo();
			}
		});
		
		checkBoxEnableRetry.setChecked(getLastEnableRetry(HBKDownloadActivity.this));
		checkBoxEnableRetry.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				setLastEnableRetry(HBKDownloadActivity.this, isChecked);
			}
		});
		
		receiver = new MyReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_UPDATE);
		filter.addAction(ACTION_MUSIC);
		this.registerReceiver(receiver, filter);
    }
    
    private void requestMusicInfo() {
        Intent intent = new Intent(HBKMusicService.ACTION_GETINFO);
        intent.setClass(HBKDownloadActivity.this, HBKMusicService.class);
        startService(intent);
    }
    
	@Override
	protected void onResume() {
		super.onResume();
		updateCacheFileInfo();
		requestMusicInfo();
		isGettingInfo = true;
		getInfoHandler.sendMessageDelayed(
				getInfoHandler.obtainMessage(MESSAGE_GETINFO), DELAY);
	}

	
	@Override
	protected void onPause() {
		super.onPause();
		isGettingInfo = false;
	}

	@Override
	protected void onStop() {
		super.onStop();
		isGettingInfo = false;
		if (this.isFinishing()) {
			if (receiver != null) {
				this.unregisterReceiver(receiver);
				receiver = null;
			}
		}
	}
	
	private void deleteSmallFiles() {
		File sdcardDir = Environment.getExternalStorageDirectory();
        String path = sdcardDir.getPath() + "/hbksugar";
    	if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
            File filePath = new File(path);
            if (!filePath.exists()) {
        		Toast.makeText(this, 
        				"目录" + path + "不存在", 
        				Toast.LENGTH_SHORT).show();
        		return;
            }
            File[] files = filePath.listFiles();
            Hashtable<String, FileInfo> map = new Hashtable<String, FileInfo>(); 
            if (files != null) {
            	for (File file : files) {
            		String filepathname = file.getPath();
            		if (filepathname != null) {
	            		if (filepathname.endsWith(".mp3") &&
	            			filepathname.contains("_download_")) {
	            			String key = filepathname.substring(0, filepathname.lastIndexOf("_download_"));
	            			FileInfo info = map.get(key);
	            			long length = file.length();
	            			if (length == 0) {
	        					if (file != null) {
	        						file.delete();
	        						new File(file.getPath().replace(".mp3", ".txt")).delete();
	        						new File(file.getPath().replace(".mp3", ".pos")).delete();
	        					}
	            			} else if (info == null) {
	            				map.put(key, new FileInfo(filepathname, length));
	            			} else if (info.size < length) {
	        					if (info.filename != null) {
	        						new File(info.filename).delete();
	        						new File(info.filename.replace(".mp3", ".txt")).delete();
	        						new File(info.filename.replace(".mp3", ".pos")).delete();
	        					}
	        					map.put(key, new FileInfo(filepathname, length));
	            			} else {
	        					if (file != null) {
	        						file.delete();
	        						new File(file.getPath().replace(".mp3", ".txt")).delete();
	        						new File(file.getPath().replace(".mp3", ".pos")).delete();
	        					}
	            			}
	            		} else if (filepathname.endsWith(".flv") &&
	            			filepathname.contains("_download_")) {
	            			String key = filepathname.substring(0, filepathname.lastIndexOf("_download_"));
	            			FileInfo info = map.get(key);
	            			long length = file.length();
	            			if (length == 0) {
	        					if (file != null) {
	        						file.delete();
	        						new File(file.getPath().replace(".flv", ".txt")).delete();
	        						new File(file.getPath().replace(".flv", ".pos")).delete();
	        					}
	            			} else if (info == null) {
	            				map.put(key, new FileInfo(filepathname, length));
	            			} else if (info.size < length) {
	        					if (info.filename != null) {
	        						new File(info.filename).delete();
	        						new File(info.filename.replace(".flv", ".txt")).delete();
	        						new File(info.filename.replace(".flv", ".pos")).delete();
	        					}
	        					map.put(key, new FileInfo(filepathname, length));
	            			} else {
	        					if (file != null) {
	        						file.delete();
	        						new File(file.getPath().replace(".flv", ".txt")).delete();
	        						new File(file.getPath().replace(".flv", ".pos")).delete();
	        					}
	            			}
	            		} else if (filepathname.endsWith(".txt")) {
	            			File mp3file = new File(filepathname.replace(".txt", ".mp3"));
	            			File flvfile = new File(filepathname.replace(".txt", ".flv"));
	            			if (!mp3file.exists() && !flvfile.exists()) {
	            				file.delete();
	            			}
	            		} else if (filepathname.endsWith(".jpg")) {
	            			if (file.length() == 0) {
	            				file.delete();
	            			}
	            		}
            		}
            	}
            }
        } else {
    		Toast.makeText(this, 
    				"未挂接外部存储", 
    				Toast.LENGTH_SHORT).show();
    		return;
        }
    	updateCacheFileInfo();
		Toast.makeText(this, 
			"删除完成", 
			Toast.LENGTH_SHORT).show();
	}
	
    private void updateCacheFileInfo() {
		File sdcardDir = Environment.getExternalStorageDirectory();
        String path = sdcardDir.getPath() + "/hbksugar";
    	if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
            File filePath = new File(path);
            if (!filePath.exists()) {
            	this.textViewCacheFileInfo.setText("目录" + path + "不存在");
        		return;
            }
            File[] files = filePath.listFiles();
            long totalMP3Length = 0, totalFilesLength = 0, totalJPGLength = 0;
            int totalMP3Num = 0, totalFilesNum = 0, totalJPGNum = 0;
            if (files != null) {
            	for (File file : files) {
            		String filepathname = file.getPath();
            		if (filepathname != null &&
            			(filepathname.endsWith(".mp3") || filepathname.endsWith(".flv")) &&
            			filepathname.contains("_download_")) {
            			long length = file.length();
            			totalMP3Length += length;
            			totalFilesLength += length;
            			totalMP3Num++;
            			totalFilesNum++;
            		} else if (filepathname != null &&
            			filepathname.endsWith(".jpg")) {
            			long length = file.length();
            			totalJPGLength += length;
            			totalFilesLength += length;
            			totalJPGNum++;
            			totalFilesNum++;
            		} else if (filepathname != null) {
            			long length = file.length();
            			totalMP3Length += length;
            			totalFilesNum++;
            		}
            	}
            }
            this.textViewCacheFileInfo.setText(
        		"下载目录：" + path + "\n" + 
        		"MP3/FLV文件个数：" + totalMP3Num + "\n" + 
        		"MP3/FLV文件总大小:" + formatByteSize(totalMP3Length) + "\n" +
        		"JPG文件个数：" + totalJPGNum + "\n" + 
        		"JPG文件总大小:" + formatByteSize(totalJPGLength) + "\n" +
        		"全部下载文件(mp3, flv, jpg, txt等)总数：" + totalFilesNum + "\n" + 
        		"全部下载文件(mp3, flv, jpg, txt等)总大小：" + formatByteSize(totalFilesLength)
            );
        } else {
    		this.textViewCacheFileInfo.setText("未挂接外部存储");
    		return;
        }
    }
    
	private static String formatByteSize(long sz) {
		DecimalFormat df = new DecimalFormat("#,###.#");
		if (sz > 1000000) return String.format("%s MB", df.format(sz / 1000000.0));
		if (sz > 1000) return String.format("%s KB", df.format(sz / 1000.0));
		return String.format("%s B", df.format(sz));
	}
	
    public static void setLastEnableRetry(Context context, boolean isEnabled) {
		Editor e = context.getSharedPreferences(SHARE_PREF_NAME, MODE_PRIVATE).edit();
		e.putBoolean(SHARE_PREF_ENABLE_RETRY, isEnabled);
		e.commit();
    }
    
    public static boolean getLastEnableRetry(Context context) {
    	SharedPreferences sp = context.getSharedPreferences(SHARE_PREF_NAME, MODE_PRIVATE);
		return sp.getBoolean(SHARE_PREF_ENABLE_RETRY, true);
    }
}
