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

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class HBKGoogleTTSActivity extends Activity {
	private final static boolean D = false;
	private final static String TAG = "HBKGoogleTTSActivity";
	
	private TextView textViewTitle;
	private Button buttonDownload, buttonPlay;
	private LinearLayout linearLayoutTitle;
	private EditText editTextInput;
	
	private String urlFormat = "https://translate.google.co.jp/translate_tts?ie=UTF-8&q=%s&tl=ja";
	private String fileFormat = "/hbksugar_tts/%s.mp3";
	private MediaPlayer player;
	private DownloadThread thread;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.google_tts);
		
		textViewTitle = (TextView) this.findViewById(R.id.textViewTitle);
		buttonDownload = (Button)this.findViewById(R.id.buttonDownload);
		linearLayoutTitle = (LinearLayout) this.findViewById(R.id.linearLayoutTitle);
		editTextInput = (EditText) this.findViewById(R.id.editTextInput);
		buttonPlay = (Button) this.findViewById(R.id.buttonPlay);
		
		Intent intent = this.getIntent();
		if (intent != null && intent.getAction() != null && intent.getAction().equals(Intent.ACTION_SEND)) {
			String text = intent.getStringExtra(Intent.EXTRA_TEXT);
			if (text != null) {
				text = text.replace("\n", " ");
				editTextInput.setText(text);
			}
		}
		
		textViewTitle.setText("Google翻译TTS");
		//textViewTitle.setTextColor(0xff000000);
		//linearLayoutTitle.setBackgroundColor(0xfffcabbd);
		
		buttonDownload.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String inputStr = editTextInput.getText().toString();
				if (inputStr != null && inputStr.length() > 0) {
					download(inputStr);
				}
			}
		});
		buttonPlay.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String inputStr = editTextInput.getText().toString();
				if (inputStr != null && inputStr.length() > 0) {
					play(inputStr);
				}
			}
		});
		Button buttonBack = (Button)this.findViewById(R.id.buttonBack);
		buttonBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
	
	private void download(String str) {
		File sdcardDir = Environment.getExternalStorageDirectory();
        String dirpath = sdcardDir.getPath() + "/hbksugar_tts/";
		if (!createSDCardDir(dirpath)) {
			Toast.makeText(HBKGoogleTTSActivity.this, 
					"无法创建目录" + dirpath, 
					Toast.LENGTH_SHORT).show();
		} else {
			if (thread == null) {
				thread = new DownloadThread(str);
				Toast.makeText(HBKGoogleTTSActivity.this, 
						"开始下载", 
						Toast.LENGTH_SHORT).show();
				thread.start();
			} else {
				Toast.makeText(HBKGoogleTTSActivity.this, 
						"下载线程未结束", 
						Toast.LENGTH_SHORT).show();				
			}
		}
	}
	
	private void play(String inputStr) {
		File sdcardDir = Environment.getExternalStorageDirectory();
		String path = sdcardDir.getPath() + String.format(fileFormat, inputStr);

		try {
			if (player != null) {
				player.release();
				player = null;
			}
			player = new MediaPlayer();
			player.setDataSource(path);
			player.prepare();
			player.start();
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(HBKGoogleTTSActivity.this, 
					"播放失败:" + path, 
					Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (this.isFinishing() && player != null) {
			player.release();
			player = null;
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (player != null) {
			player.release();
			player = null;
		}
	}
	
	private class DownloadThread extends Thread {
		private String strInput;
		
		public DownloadThread(String input) {
			this.strInput = input;
		}
		
		@Override
		public void run() {
			File sdcardDir = Environment.getExternalStorageDirectory();
			final String strFileOut = sdcardDir.getPath() + String.format(fileFormat, this.strInput);
			final boolean result = getMp3FromUrl(String.format(urlFormat, Uri.encode(this.strInput)),
					strFileOut);
			thread = null;
			if (D) {
				Log.e(TAG, "DownloadThread finish");
			}
			buttonPlay.post(new Runnable() {
				@Override
				public void run() {
					if (result) {
						Toast.makeText(HBKGoogleTTSActivity.this, 
								"下载成功", 
								Toast.LENGTH_SHORT).show();
					} else {
						new File(strFileOut).delete();
						Toast.makeText(HBKGoogleTTSActivity.this, 
								"下载失败", 
								Toast.LENGTH_SHORT).show();						
					}
				}
			});
		}
	}
	
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
    
    private boolean getMp3FromUrl(String mp3Url, String mp3filename) {
    	boolean result = false;
    	URL url;
    	BufferedInputStream bis = null;
    	URLConnection connection = null;
    	FileOutputStream fos = null;
    	BufferedOutputStream bos = null;
    	InputStream mp3Is = null;
    	try {
           url = new URL(mp3Url);
           connection = url.openConnection();
           connection.setUseCaches(true);
    	   connection.setConnectTimeout(5000);
    	   connection.setReadTimeout(5000);
    	   mp3Is = connection.getInputStream();
           bis = new BufferedInputStream(mp3Is);
	       fos = new FileOutputStream(mp3filename);
	       bos = new BufferedOutputStream(fos);
	       while (true) {
	    	   int c = bis.read();
	    	   if (c == -1) {
	    		   break;
	    	   } else {
	    		   fos.write(c);
	    		   fos.flush();
	    	   }
	       }
	       result = true;
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
        	if (mp3Is != null) {
        		try {
        			mp3Is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
        	}
        }
    	return result;
    }
}
