package com.iteye.weimingtom.hbksuger;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import br.com.belocodigo.rtmpdump.RTMPDownloadThread;

import com.flazr.rtmp.client.RtmpClient;
import com.iteye.weimingtom.hbksuger.WebDowner.DetailInfo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class HBKProgramDetailActivity extends Activity {
	private final static boolean D = false;
	private final static String TAG = "HBKProgramDetailActivity";
	
	public final static String EXTRA_TAB_URL = "com.iteye.weimingtom.hbksuger.HBKProgramDetailActivity.tabUrl";
	public final static String EXTRA_DETAIL_URL = "com.iteye.weimingtom.hbksuger.HBKProgramDetailActivity.detailUrl";
	public final static String EXTRA_INFO_TEXT = "com.iteye.weimingtom.hbksuger.HBKProgramDetailActivity.infoText";
	public final static String EXTRA_MP3_FILE = "com.iteye.weimingtom.hbksuger.HBKProgramDetailActivity.mp3File";
	public final static String EXTRA_DESC_URL = "com.iteye.weimingtom.hbksuger.HBKProgramDetailActivity.descUrl";
	
	public final static int MESSAGE_UPDATE_THREAD = 111;
	public final static int MESSAGE_UPDATE_THREAD2 = 112;
	
	private LinearLayout linearLayoutContent, linearLayoutProgress;
	private TextView textViewTitle;
	private TextView textViewLoading;
	private TextView textViewDetail;
	private TextView textViewUrl;
	private TextView textViewLog;
	private Button buttonDownload, buttonRefresh, buttonStop, buttonOpen, buttonDownloadService, buttonCache, buttonClear, buttonExtern;
	private ProgressBar progressBar1;
	private TextView textViewPercent;
	private ImageView imageViewThumbnail;
	private Button buttonWebPage, buttonOpenWMA;
	
	private UpdateHandler updateHandler;
	private UpdateThread updateThread;
	private WebDowner webDowner;

	private DownloadHandler downloadHandler;
	private String detailUrl, infoText, tabUrl, mp3File, descUrl;
	//private DownloadThread downloadThread;
	private RTMPDownloadThread downloadThread;
	private DetailInfo info;
	private String mmsUrl;
	
	private double duration;
	private int currentProgress;
	
	private String mp3filename;
    private Bitmap bitmap;
	private InputStream bitmapIs;
	
	private boolean isNoTimeout = false;
	
	private boolean isBitmapCached = false;
	
	private class UpdateHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			if (msg != null) {
				if (msg.what == MESSAGE_UPDATE_THREAD) {
					linearLayoutContent.setVisibility(LinearLayout.VISIBLE);
					textViewLoading.setVisibility(TextView.INVISIBLE);
					textViewDetail.setText(infoText);
					if (info != null) {
						StringBuffer sb = new StringBuffer();
						if (info.thumbnail != null) {
							sb.append(info.thumbnail);
							sb.append("\n\n");
						}
						if (info.getRtmpUrl() != null) {
							sb.append(info.getRtmpUrl());
							sb.append("\n\n");
						}
						if (descUrl != null) {
							sb.append(descUrl);
							sb.append("\n\n");
						}
						if (mmsUrl != null) {
							sb.append(mmsUrl);
							sb.append("\n\n");
						} else {
							if (msg.arg1 == 1) {
								sb.append("超时，获取mms链接失败");
								sb.append("\n\n");
//								Toast.makeText(HBKProgramDetailActivity.this, 
//										"超时，获取mms链接失败", Toast.LENGTH_SHORT).show();
							} else {
								sb.append("正在获取mms链接中,请稍候...");
								sb.append("\n\n");
							}
						}
						textViewUrl.setText(sb.toString());
						if (!isBitmapCached) {
							imageViewThumbnail.setImageBitmap(getBitmapFromUrl(info.thumbnail, info.getMP3Filename(), false));
						}
					}
					File sdcardDir = Environment.getExternalStorageDirectory();
			        String dirpath = sdcardDir.getPath() + "/hbksugar/";
					if (!createSDCardDir(dirpath)) {
						log("无法创建目录" + dirpath);
					}
				} else if (msg.what == MESSAGE_UPDATE_THREAD2) {
					if (info != null) {
						StringBuffer sb = new StringBuffer();
						if (info.thumbnail != null) {
							sb.append(info.thumbnail);
							sb.append("\n\n");
						}
						if (info.getRtmpUrl() != null) {
							sb.append(info.getRtmpUrl());
							sb.append("\n\n");
						}
						if (descUrl != null) {
							sb.append(descUrl);
							sb.append("\n\n");
						}
						if (mmsUrl != null) {
							sb.append(mmsUrl);
							sb.append("\n\n");
						} else {
							sb.append("超时，获取mms链接失败");
							sb.append("\n\n");
							Toast.makeText(HBKProgramDetailActivity.this, 
									"超时，获取mms链接失败", Toast.LENGTH_SHORT).show();
						}
						textViewUrl.setText(sb.toString());
					}
			}
			}
		}
	}
	
	private class UpdateThread extends Thread {
		private volatile boolean isStop = false;
		private Object isStopLock = new Object();
		
		public void setStop(boolean isStop) {
			synchronized (isStopLock) {
				this.isStop = isStop;
				if (bitmapIs != null) {
					try {
						bitmapIs.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (webDowner != null) {
					webDowner.abort();
				}
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
			try {
				if (D) {
					Log.e(TAG, "===1");
				}
				info = webDowner.getDetail(detailUrl);
				//http://www2.uliza.jp/IF/WMVDisplay.aspx?clientid=910&playerid=2869&episodeid=130218_tamako&videoid=130218_tamako-wm&vid=1883118&memberid=&membersiteid=516&nickname=&sex=&birthday=&local=&mode=1
				if (D) {
					Log.e(TAG, "===2");
				}
				updateHandler.sendMessage(updateHandler.obtainMessage(MESSAGE_UPDATE_THREAD, 0, 0));
				if (descUrl != null) {
					String asxUrl = webDowner.getASXURL(descUrl);
					//"http://www2.uliza.jp/IF/WMVDisplay.aspx?clientid=910&playerid=2869&episodeid=130218_tamako&videoid=130218_tamako-wm&vid=1883118&memberid=&membersiteid=516&nickname=&sex=&birthday=&local=&mode=1"
					mmsUrl = webDowner.getASX2(asxUrl);
				}
				updateHandler.sendMessage(updateHandler.obtainMessage(MESSAGE_UPDATE_THREAD2));
				if (D) {
					Log.e(TAG, "===3");
				}
			} catch (Throwable e) {
				updateHandler.sendMessage(updateHandler.obtainMessage(MESSAGE_UPDATE_THREAD, 1, 0));
				e.printStackTrace();
			} finally {
				
			}
			setStop(true);
		}
	}
	
	private class DownloadHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch(msg.what) {
			case DownloadThread.MESSAGE_START_THREAD:
			case RTMPDownloadThread.MESSAGE_START_THREAD:
				log("前台下载线程已启动。");
				if (linearLayoutProgress != null) {
					linearLayoutProgress.setVisibility(LinearLayout.GONE);
				}
				duration = 0;
				log ("保存路径:" + mp3filename);
				break;
				
			case DownloadThread.MESSAGE_CLOSE_THREAD:
			case RTMPDownloadThread.MESSAGE_CLOSE_THREAD:
				log("前台下载线程已关闭。");
				Toast.makeText(HBKProgramDetailActivity.this, 
						"前台下载线程已关闭", Toast.LENGTH_SHORT)
						.show();
				break;

			case DownloadThread.MESSAGE_DURATION_THREAD:
			case RTMPDownloadThread.MESSAGE_DURATION_THREAD:
				if (msg.obj instanceof Double) {
					currentProgress = 0;
					duration = (Double)msg.obj;
					log("总长度:" + duration + "秒。");
					if (downloadThread != null) {
						downloadThread.writeTxtFile((int)(duration * 1000));
					}
					if (linearLayoutProgress != null) {
						linearLayoutProgress.setVisibility(LinearLayout.VISIBLE);
						progressBar1.setMax((int)duration);
						updatePercent();
					}
				}
				break;
				
			case DownloadThread.MESSAGE_PROGRESS_THREAD:
			case RTMPDownloadThread.MESSAGE_PROGRESS_THREAD:
				//log("下载进度:" + msg.arg1 + "秒。");
				if (linearLayoutProgress != null) {
					linearLayoutProgress.setVisibility(LinearLayout.VISIBLE);
					currentProgress = msg.arg1;
					progressBar1.setProgress(currentProgress);
					updatePercent();
				}
				break;
			}
		}
	}
	
	private void updatePercent() {
		if (duration <= 0) {
			textViewPercent.setText("---");
		} else {
			float percent = (duration != 0) ? (float)(((double)currentProgress / duration) * 100) : 0;
			textViewPercent.setText(
					((int)(percent * 100) / 100.0) + "%" + 
					"(" + MSToString(((int)duration - currentProgress) * 1000) +"/" +
					MSToString(((int)duration) * 1000) +")"
			);
		}
	}
	
	public static String MSToString(int num) {
		int hour = 0;
		int min = 0;
		int sec = 0;
		int msec = 0;
		msec = num % 1000;
		min = sec = (num - msec) / 1000;
		sec %= 60;
		hour = min = (min - sec) / 60;
		min %= 60;
		hour = (hour - min) / 60;
		return String.format("%d:%02d:%02d", hour, min, sec);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.detail);
		linearLayoutContent = (LinearLayout) findViewById(R.id.linearLayoutContent);
		textViewTitle = (TextView) findViewById(R.id.textViewTitle);
		textViewLoading = (TextView) findViewById(R.id.textViewLoading);
		textViewDetail = (TextView) findViewById(R.id.textViewDetail);
		textViewUrl = (TextView) findViewById(R.id.textViewUrl);
		buttonDownload = (Button) findViewById(R.id.buttonDownload);
		buttonStop = (Button) findViewById(R.id.buttonStop);
		textViewLog = (TextView) findViewById(R.id.textViewLog);
		progressBar1 = (ProgressBar) findViewById(R.id.progressBar1);
		buttonClear = (Button) findViewById(R.id.buttonClear);
		linearLayoutProgress = (LinearLayout) findViewById(R.id.linearLayoutProgress);
		textViewPercent = (TextView) findViewById(R.id.textViewPercent);
		buttonOpen = (Button) findViewById(R.id.buttonOpen);
		imageViewThumbnail = (ImageView) findViewById(R.id.imageViewThumbnail);
		buttonRefresh = (Button) findViewById(R.id.buttonRefresh);
		buttonDownloadService = (Button) findViewById(R.id.buttonDownloadService);
		buttonCache = (Button) findViewById(R.id.buttonCache);
		buttonExtern = (Button) findViewById(R.id.buttonExtern);
		buttonWebPage = (Button) findViewById(R.id.buttonWebPage);
		buttonOpenWMA = (Button) findViewById(R.id.buttonOpenWMA);
		
		textViewTitle.setText("节目明细");
		buttonDownload.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if ((downloadThread != null && downloadThread.getStop()) || 
					downloadThread == null) {
					if (info != null) {
						String url = info.getRtmpUrl();
						if (url != null && url.length() > 0) {
							mp3filename = info.getMP3Filename();
							downloadThread = new RTMPDownloadThread(0, url, mp3filename, infoText, tabUrl, detailUrl, downloadHandler, 1);
							downloadThread.start();
							Toast.makeText(HBKProgramDetailActivity.this, 
								"启动前台下载线程，请稍候...", Toast.LENGTH_SHORT)
								.show();
						}
					}
				} else {
					Toast.makeText(HBKProgramDetailActivity.this, 
						"下载未完成", Toast.LENGTH_SHORT).show();
				}
			}
		});
		buttonDownloadService.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (info != null) {
					String url = info.getRtmpUrl();
					if (url != null && url.length() > 0) {
						mp3filename = info.getMP3Filename();
						log("启动后台下载线程");
						log("下载文件名:" + mp3filename);
						startActivity(
							new Intent(HBKProgramDetailActivity.this, HBKDownloadActivity.class)
							.putExtra(HBKDownloadActivity.EXTRA_URL, url)
							.putExtra(HBKDownloadActivity.EXTRA_MP3_FILENAME, mp3filename)
							.putExtra(HBKDownloadActivity.EXTRA_INFO_TEXT, infoText)
							.putExtra(HBKDownloadActivity.EXTRA_TAB_URL, tabUrl)
							.putExtra(HBKDownloadActivity.EXTRA_DETAIL_URL, detailUrl)
						);
					}
				}
			}
		});
		buttonStop.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (downloadThread != null && !downloadThread.getStop()) {
					downloadThread.setStop(true);
					try {
						downloadThread.join(50);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					downloadThread = null;
					Toast.makeText(HBKProgramDetailActivity.this, 
							"hbksugar取消下载", Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(HBKProgramDetailActivity.this, 
						"未启动下载线程", Toast.LENGTH_SHORT).show();
				}
			}
		});
		buttonOpen.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
//				if (downloadThread == null || (downloadThread != null && downloadThread.getStop())) {
					if (mp3filename != null && mp3filename.length() > 0) {
						startActivity(new Intent(HBKProgramDetailActivity.this, HBKMediaPlayerActivity.class)
							.putExtra(HBKMediaPlayerActivity.EXTRA_FILENAME, mp3filename));
					} else {
						Toast.makeText(HBKProgramDetailActivity.this, 
								"路径为空，请先下载", Toast.LENGTH_SHORT).show();
					}
//				} else {
//					Toast.makeText(HBKProgramDetailActivity.this, 
//							"正在下载，请先停止", Toast.LENGTH_SHORT).show();					
//				}
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
						Toast.makeText(HBKProgramDetailActivity.this, 
							"找不到可用的应用程序", Toast.LENGTH_SHORT)
							.show();
					}
				} else {
					Toast.makeText(HBKProgramDetailActivity.this, 
							"路径为空，请先下载", Toast.LENGTH_SHORT).show();
				}
				return true;
			}
		});
		
		buttonClear.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				textViewLog.setText("");
				if (linearLayoutProgress != null) {
					linearLayoutProgress.setVisibility(LinearLayout.GONE);
				}
			}
		});
		
		buttonRefresh.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				isNoTimeout = true;
				refresh();
			}
		});
		buttonRefresh.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {				
				log("尝试删除缩略图");
				Toast.makeText(HBKProgramDetailActivity.this, 
						"尝试删除缩略图", Toast.LENGTH_SHORT).show();
				
				if (mp3File != null) {
			    	String preName = mp3File.substring(0, mp3File.lastIndexOf("_download_"));
			        String jpgName = preName + ".jpg";
			        File bmpFile = new File(jpgName);
			        if (bmpFile.isFile() && bmpFile.exists()) {
			        	bmpFile.delete();
			        }
				}
				
				isBitmapCached = false;
				
				imageViewThumbnail.setImageBitmap(null);
				
				return true;
			}
		});
		
		buttonCache.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (info != null) {
					String filepathname = info.getMP3Filename();
					if (filepathname != null) {
						String preName = filepathname.substring(0, filepathname.lastIndexOf("_download_"));
						if (D) {
							Log.d(TAG, "preName == " + preName);
						}
						startActivity(new Intent(HBKProgramDetailActivity.this, HBKCacheProgramActivity.class)
							.putExtra(HBKCacheProgramActivity.EXTRA_PRENAME, preName)
						);
					}
				}
			}
		});
		
		/**
		 * @see http://nightlies.videolan.org/build/
		 * 
		 * rtmp://fms.hibiki-radio.info/hibiki1004/8_0_5830
		 * mms://wms.hibiki-radio.info/hibiki1004/8_0_5830.wmv
		 */
		buttonExtern.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (info != null) {
					String url = info.getRtmpUrl();
					if (url != null && url.length() > 0) {
						Intent intent = new Intent(Intent.ACTION_VIEW);
						//url = url.replace("rtmp://fms", "mms://wms");
						//url = url + ".wmv";
						//String url = "mms://210.168.50.229/uliza/584/602_nqzX17uD_3TLrCwaOpfS_yBwGsyhUAI2QMvYdlFIR_1562567_640000_320_240_1805_20121127113411.wma";
						//url = "mms://210.168.50.229/uliza/584/602_nqzX17uD_3TLrCwaOpfS_yBwGsyhUAI2QMvYdlFIR_1562567_640000_320_240_1805_20121127113411.wma";
						intent.setDataAndType(Uri.parse(url), "audio/*");
						if (D) {
							Log.e(TAG, "url == " + url);
						}
						try {
							startActivity(intent);
						} catch (Throwable e) {
							e.printStackTrace();
							Toast.makeText(HBKProgramDetailActivity.this, 
									"找不到可用的应用程序", Toast.LENGTH_SHORT)
									.show();
						}		
					}
				}
			}
		});
		buttonOpenWMA.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mmsUrl != null) {
					Intent intent = new Intent(Intent.ACTION_VIEW);
					//"mms://wms.uliza.jp/uliza/910/130218_tamako_130218_tamako-wm.wma"
					//intent.setDataAndType(Uri.parse("http://www2.uliza.jp/IF/WMVDisplay.aspx?clientid=910&playerid=2869&episodeid=130218_tamako&videoid=130218_tamako-wm&vid=1883118&memberid=&membersiteid=516&nickname=&sex=&birthday=&local=&mode=1"), "*/*");
					intent.setDataAndType(Uri.parse(mmsUrl), "*/*");
					try {
						startActivity(intent);
					} catch (Throwable e) {
						e.printStackTrace();
						Toast.makeText(HBKProgramDetailActivity.this, 
								"找不到可用的应用程序", Toast.LENGTH_SHORT)
								.show();
					}
				} else {
					Toast.makeText(HBKProgramDetailActivity.this, 
							"链接为空", Toast.LENGTH_SHORT)
							.show();					
				}
			}
		});
		buttonWebPage.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (descUrl != null) {
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setDataAndType(Uri.parse(descUrl), "*/*");
					try {
						startActivity(intent);
					} catch (Throwable e) {
						e.printStackTrace();
						Toast.makeText(HBKProgramDetailActivity.this, 
								"找不到可用的应用程序", Toast.LENGTH_SHORT)
								.show();
					}
				} else {
					Toast.makeText(HBKProgramDetailActivity.this, 
						"链接为空", 
						Toast.LENGTH_SHORT).show();
				}
			}
		});
		textViewLoading.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});

		linearLayoutContent.setVisibility(LinearLayout.INVISIBLE);
		textViewLoading.setVisibility(TextView.VISIBLE);
		webDowner = new WebDowner();
		updateHandler = new UpdateHandler();
		downloadHandler = new DownloadHandler();
		
		refresh();
	}

	private void refresh() {
		Intent intent = this.getIntent();
		if (intent != null) {						
			this.infoText = intent.getStringExtra(EXTRA_INFO_TEXT);
			this.detailUrl = intent.getStringExtra(EXTRA_DETAIL_URL);
			this.tabUrl = intent.getStringExtra(EXTRA_TAB_URL);
			this.mp3File = intent.getStringExtra(EXTRA_MP3_FILE);
			this.descUrl = intent.getStringExtra(EXTRA_DESC_URL);
						
			if (this.detailUrl != null) {
				if (updateThread == null || 
					(updateThread != null && updateThread.getStop())) {
					
					this.imageViewThumbnail.setImageBitmap(null);
					this.textViewDetail.setText("");
					this.textViewUrl.setText("");
					//linearLayoutContent.setVisibility(LinearLayout.INVISIBLE);
					//textViewLoading.setVisibility(TextView.VISIBLE);	
					
					if (mp3File != null) {
						imageViewThumbnail.setImageBitmap(getBitmapFromUrl(null, mp3File, true));
					}
					
					updateThread = new UpdateThread();
					updateThread.start();
				} else {
					Toast.makeText(this, "更新线程未停止，现在尝试关闭", Toast.LENGTH_SHORT).show();
					if (updateThread != null) {
						updateThread.setStop(true);
					}
				}
			}
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
        if (bitmap != null) {
        	bitmap.recycle();
        	bitmap = null;
        }
		if (downloadThread != null && !downloadThread.getStop()) {
			downloadThread.setStop(true);
			try {
				downloadThread.join(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			Toast.makeText(HBKProgramDetailActivity.this, 
					"hbksugar取消下载", Toast.LENGTH_SHORT).show();
		}
		downloadThread = null;
		if (updateThread != null && !updateThread.getStop()) {
			updateThread.setStop(true);
			try {
				updateThread.join(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		updateThread = null;
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (this.isFinishing()) {
	        if (bitmap != null) {
	        	bitmap.recycle();
	        	bitmap = null;
	        }
			if (downloadThread != null && !downloadThread.getStop()) {
				downloadThread.setStop(true);
				try {
					downloadThread.join(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				Toast.makeText(HBKProgramDetailActivity.this, 
						"hbksugar取消下载", Toast.LENGTH_SHORT).show();
			}
			downloadThread = null;
			if (updateThread != null && !updateThread.getStop()) {
				updateThread.setStop(true);
				try {
					updateThread.join(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			updateThread = null;
		}
	}
	
    public static String getTimeString() {
		return new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss]", Locale
			.getDefault()).format(new Date(System.currentTimeMillis()));
    }
    
    private void log(String str) {
		if (textViewLog != null && str != null) {
			textViewLog.append(getTimeString() + str + "\n");
		}
    }
    
    /**
     * @see http://blog.sina.com.cn/s/blog_6ffbcfdb0100qn3a.html
     */
    public boolean createSDCardDir(String path){
    	if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
            File file = new File(path);
            if (!file.exists()) {
            	return file.mkdirs();
            } else {
            	return true;
            }
        }
    	return false;
    }
    
    /**
     * @see http://android.tgbus.com/Android/tutorial/201104/349170.shtml
     * @param imgUrl
     * @return
     */
    private Bitmap getBitmapFromUrl(String imgUrl, String mp3filename, boolean forceLocal) {
        if (bitmap != null) {
        	bitmap.recycle();
        	bitmap = null;
        }
        
    	String preName = mp3filename.substring(0, mp3filename.lastIndexOf("_download_"));
        String jpgName = preName + ".jpg";
        File bmpFile = new File(jpgName);
        if (bmpFile.isFile() && bmpFile.exists() && bmpFile.length() > 0) {
        	isBitmapCached = true;
        }
        
        if (forceLocal) {
        	bitmap = BitmapFactory.decodeFile(jpgName);
        } else if (isBitmapCached) {
        	bitmap = BitmapFactory.decodeFile(jpgName);
        } else {
	    	URL url;
	    	BufferedInputStream bis = null;
	    	URLConnection connection = null;
	    	FileOutputStream fos = null;
	    	BufferedOutputStream bos = null;
	    	try {
	           url = new URL(imgUrl);
	           connection = url.openConnection();
	           connection.setUseCaches(true);
	           if (!isNoTimeout) {
	        	   connection.setConnectTimeout(500);
	        	   connection.setReadTimeout(500);
	           } else {
	        	   connection.setConnectTimeout(5000);
	        	   connection.setReadTimeout(5000);        	   
	           }
	           bitmapIs = connection.getInputStream();
	           bis = new BufferedInputStream(bitmapIs);
	           bitmap = BitmapFactory.decodeStream(bis);
	           
	           if (bitmap != null) {
			       fos = new FileOutputStream(bmpFile);
			       bos = new BufferedOutputStream(fos);
			       bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
			       fos.flush();
	           }
	        } catch (MalformedURLException e) {
	        	e.printStackTrace();
	        } catch (IOException e) {
	            e.printStackTrace();
	        } finally {
	        	if (bos != null) {
	        		try {
						bos.close();
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
	        	if (bis != null) {
	        		try {
						bis.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
	        	}
	        	if (bitmapIs != null) {
	        		try {
	        			bitmapIs.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
	        	}
	        }
        }
        return bitmap;
    }
}
