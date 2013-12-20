package com.iteye.weimingtom.hbksuger;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.iteye.weimingtom.hbksuger.WebDowner.AnimatePageInfo;
import com.iteye.weimingtom.hbksuger.WebDowner.LantisPageInfo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * FIXME:hbksugar_animate
 * @author Administrator
 *
 */
public class HBKAnimateDetailActivity extends Activity {
	private final static boolean D = false;
	private final static String TAG = "HBKAnimateDetailActivity";
	
	public final static String EXTRA_TITLE_HREF = "com.iteye.weimingtom.hbksuger.HBKAnimateDetailActivity.EXTRA_TITLE_HREF";
	public final static String EXTRA_TITLE = "com.iteye.weimingtom.hbksuger.HBKAnimateDetailActivityy.EXTRA_TITLE";

	public final static int MESSAGE_UPDATE_THREAD = 111;
	
	private LinearLayout linearLayoutContent;
	private TextView textViewTitle;
	private TextView textViewLoading;
	private TextView textViewDetail;
	private TextView textViewLog;
	private Button buttonRefresh, buttonOpen, buttonClear, buttonExtern, buttonExtern2;
	private ImageView imageViewThumbnail;
	
	private UpdateHandler updateHandler;
	private UpdateThread updateThread;
	private WebDowner webDowner;
	
	private AnimatePageInfo info;
    private Bitmap bitmap;
	private InputStream bitmapIs;
	
	private String asx;
	private String mms;
	private String text;
	
	private boolean isNoTimeout = false;
	private boolean isBitmapCached = false;
	
	private class UpdateHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == MESSAGE_UPDATE_THREAD) {
				linearLayoutContent.setVisibility(LinearLayout.VISIBLE);
				textViewLoading.setVisibility(TextView.INVISIBLE);
				if (info != null) {
					StringBuffer sb = new StringBuffer();
					if (info.title != null && info.title.length() > 0) {
						sb.append(info.title);
						sb.append("\n");
					}
					if (text != null && text.length() > 0) {
						sb.append(text);
						sb.append("\n");						
					}
					if (asx != null && asx.length() > 0) {
						sb.append(asx);
						sb.append("\n");						
					}
					if (mms != null && mms.length() > 0) {
						sb.append(mms);
						sb.append("\n");
					}
					textViewDetail.setText(sb.toString());
					if (!isBitmapCached) {
						imageViewThumbnail.setImageBitmap(getBitmapFromUrl(info.thumbImage, false));
					}
				}
				File sdcardDir = Environment.getExternalStorageDirectory();
		        String dirpath = sdcardDir.getPath() + "/hbksugar_animate/";
				if (!createSDCardDir(dirpath)) {
					log("无法创建目录" + dirpath);
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
				if (info != null) {
					if (D) {
						Log.d(TAG, "loading asx... " + info.titleHref);
					}
					text/*asx*/ = webDowner.getAnimateDetail(info.titleHref);
					/*
					if (D) {
						Log.d(TAG, "loading mms... " + asx);
					}
					mms = webDowner.getASX(asx);
					*/
				}
			} catch (Throwable e) {
				e.printStackTrace();
			} finally {
				updateHandler.sendMessage(updateHandler.obtainMessage(MESSAGE_UPDATE_THREAD));
			}
			setStop(true);
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
		this.setContentView(R.layout.animate_detail);
		linearLayoutContent = (LinearLayout) findViewById(R.id.linearLayoutContent);
		textViewTitle = (TextView) findViewById(R.id.textViewTitle);
		textViewLoading = (TextView) findViewById(R.id.textViewLoading);
		textViewDetail = (TextView) findViewById(R.id.textViewDetail);
		textViewLog = (TextView) findViewById(R.id.textViewLog);
		buttonClear = (Button) findViewById(R.id.buttonClear);
		buttonOpen = (Button) findViewById(R.id.buttonOpen);
		imageViewThumbnail = (ImageView) findViewById(R.id.imageViewThumbnail);
		buttonRefresh = (Button) findViewById(R.id.buttonRefresh);
		buttonExtern = (Button) findViewById(R.id.buttonExtern);
		buttonExtern2 = (Button) findViewById(R.id.buttonExtern2);
		
		textViewLoading.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
		
		textViewTitle.setText("节目明细");
		buttonOpen.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (info != null) {
					String url = info.titleHref;
					if (url != null && url.length() > 0) {
						Intent intent = new Intent(Intent.ACTION_VIEW);
						intent.setDataAndType(Uri.parse(url), "*/*");
						if (D) {
							Log.e(TAG, "url == " + url);
						}
						try {
							startActivity(intent);
						} catch (Throwable e) {
							e.printStackTrace();
							Toast.makeText(HBKAnimateDetailActivity.this, 
									"找不到可用的应用程序", Toast.LENGTH_SHORT)
									.show();
						}		
					} else {
						Toast.makeText(HBKAnimateDetailActivity.this, 
								"链接为空", Toast.LENGTH_SHORT)
								.show();					
					}
				} else {
					Toast.makeText(HBKAnimateDetailActivity.this, 
							"链接为空", Toast.LENGTH_SHORT)
							.show();					
				}
			}
		});
		
		buttonClear.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				textViewLog.setText("");
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
				Toast.makeText(HBKAnimateDetailActivity.this, 
						"尝试删除缩略图", Toast.LENGTH_SHORT).show();
				if (info != null && info.thumbImage != null) {
					File sdcardDir = Environment.getExternalStorageDirectory();
			        String jpgName = sdcardDir.getPath() + "/hbksugar_animate/" + info + ".jpg";
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
					//String url = info.asx32k;
					String url = mms;
					if (url != null && url.length() > 0) {
						Intent intent = new Intent(Intent.ACTION_VIEW);
						intent.setDataAndType(Uri.parse(url), "audio/*");
						if (D) {
							Log.e(TAG, "url == " + url);
						}
						try {
							startActivity(intent);
						} catch (Throwable e) {
							e.printStackTrace();
							Toast.makeText(HBKAnimateDetailActivity.this, 
									"找不到可用的应用程序", Toast.LENGTH_SHORT)
									.show();
						}		
					} else {
						Toast.makeText(HBKAnimateDetailActivity.this, 
								"链接为空", Toast.LENGTH_SHORT)
								.show();					
					}
				} else {
					Toast.makeText(HBKAnimateDetailActivity.this, 
							"链接为空", Toast.LENGTH_SHORT)
							.show();					
				}
			}
		});
		buttonExtern2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (info != null) {
					//String url = info.asx64k;
					String url = text;
					//String url = "mms://210.168.50.229/uliza/584/633_9h7fjTxg_xMPzVGWVv3l_s4bXPdgHap7bQEBs0HDK_1859658_640000_320_240_1805_20130125130723.wma?di=584&si=198&pi=628&gi=635&gc=qUIOoG&bi=28484&bc=9h7fjTxg&ei=336247&ec=xMPzVGWVv3l&vi=1795624&vc=s4bXPdgHap7bQEBs0HDK&msi=163&mc=&ni=285";
					if (url != null && url.length() > 0) {
						Intent intent = new Intent(Intent.ACTION_SEND);
						intent.setType("text/plain");
				        intent.putExtra(Intent.EXTRA_SUBJECT, "text");
				        intent.putExtra(Intent.EXTRA_TEXT, text);
						/*
				        if (D) {
							Log.e(TAG, "url == " + url);
						}
						*/
						try {
							startActivity(intent);
						} catch (Throwable e) {
							e.printStackTrace();
							Toast.makeText(HBKAnimateDetailActivity.this, 
									"找不到可用的应用程序", Toast.LENGTH_SHORT)
									.show();
						}		
					} else {
						Toast.makeText(HBKAnimateDetailActivity.this, 
								"链接为空", Toast.LENGTH_SHORT)
								.show();					
					}
				} else {
					Toast.makeText(HBKAnimateDetailActivity.this, 
							"链接为空", Toast.LENGTH_SHORT)
							.show();					
				}
			}
		});

		linearLayoutContent.setVisibility(LinearLayout.INVISIBLE);
		textViewLoading.setVisibility(TextView.VISIBLE);
		webDowner = new WebDowner();
		updateHandler = new UpdateHandler();

		refresh();
	}

	private void refresh() {
		Intent intent = this.getIntent();
		if (intent != null) {
			String titleHref = intent.getStringExtra(EXTRA_TITLE_HREF);
			String title = intent.getStringExtra(EXTRA_TITLE);
			info = new AnimatePageInfo(titleHref, title);
			if (D) {
				Log.d(TAG, "info.titleHref = " + info.titleHref);
				Log.d(TAG, "info.title = " + info.title);
				Log.d(TAG, "info.id = " + info.id);
				Log.d(TAG, "info.thumbImage = " + info.thumbImage);
			}
			this.imageViewThumbnail.setImageBitmap(null);
			this.textViewDetail.setText("");
			
			imageViewThumbnail.setImageBitmap(getBitmapFromUrl(null, true));
			if (this.info.thumbImage != null && this.info.thumbImage.length() > 0) {
				if (updateThread == null || 
					(updateThread != null && updateThread.getStop())) {
					updateThread = new UpdateThread();
					updateThread.start();
				} else {
					Toast.makeText(this, "更新线程未停止", Toast.LENGTH_SHORT).show();
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
    private Bitmap getBitmapFromUrl(String imgUrl, boolean forceLocal) {
        if (bitmap != null) {
        	bitmap.recycle();
        	bitmap = null;
        }
        
        if (info == null || info.thumbImage == null || info.thumbImage.length() == 0) {
        	bitmap = null;
        	return null;
        }
        
        File sdcardDir = Environment.getExternalStorageDirectory();
        String jpgName = sdcardDir.getPath() + "/hbksugar_animate/" + info.id + ".jpg";
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
