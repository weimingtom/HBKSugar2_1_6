package com.iteye.weimingtom.hbksuger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import br.com.belocodigo.rtmpdump.RTMPDownloadThread;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class HBKCacheProgramActivity extends Activity {
	private static final boolean ENABLE_RETRY = false;
	
	private final static boolean D = false;
	private final static String TAG = "HBKCacheProgramActivity";
	
	public final static String EXTRA_PRENAME = "com.iteye.weimingtom.hbksuger.HBKCacheProgramActivity.preName";
	public final static int MESSAGE_UPDATE_THREAD = 111;
	
	private ListView viewBookList;
	private TextView textViewTitle;
	private MenuItemAdapter adapter;
	private TextView textViewLoading;
	private List<MenuItemModel> models;
	private List<MenuItemModel> tempmodels;
	private UpdateHandler updateHandler;
	private UpdateThread updateThread;
	private String preName;
	private LinearLayout linearLayoutTitle;
	
	private class UpdateHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == MESSAGE_UPDATE_THREAD) {
				models.clear();
				if (tempmodels != null) {
					for (MenuItemModel model : tempmodels) {
						models.add(model);
					}
				}
				adapter.notifyDataSetChanged();
				viewBookList.setVisibility(ListView.VISIBLE);
				textViewLoading.setVisibility(ProgressBar.INVISIBLE);
			}
		}
	}
	
	private class UpdateThread extends Thread {
		private volatile boolean isStop = false;
		private Object isStopLock = new Object();
		
		public void setStop(boolean isStop) {
			synchronized (isStopLock) {
				this.isStop = isStop;
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
				getCacheFileInfo();
			} catch (Throwable e) {
				e.printStackTrace();
				tempmodels.clear();
			} finally {
				updateHandler.sendMessage(updateHandler.obtainMessage(MESSAGE_UPDATE_THREAD));
			}
			setStop(true);
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.main_menu);
		viewBookList = (ListView) findViewById(R.id.viewBookList);
		textViewTitle = (TextView) findViewById(R.id.textViewTitle);
		textViewLoading = (TextView) findViewById(R.id.textViewLoading);
		linearLayoutTitle = (LinearLayout) findViewById(R.id.linearLayoutTitle);
		
		textViewLoading.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
		
		if (ENABLE_RETRY) {
			textViewTitle.setText("已下载文件（长按手动续传）");
		} else {
			textViewTitle.setText("已下载文件（不支持续传）");
		}
		textViewTitle.setTextColor(0xff000000);
		linearLayoutTitle.setBackgroundColor(0xfffcabbd);
		
		models = new ArrayList<MenuItemModel>();
		tempmodels = new ArrayList<MenuItemModel>();
		adapter = new MenuItemAdapter(this, models);
		viewBookList.setAdapter(adapter);
		viewBookList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (position >= 0 && position < models.size()) {
					startActivity(new Intent(HBKCacheProgramActivity.this, HBKMediaPlayerActivity.class)
						.putExtra(HBKMediaPlayerActivity.EXTRA_FILENAME, models.get(position).detail));
				}
			}
		});
		if (ENABLE_RETRY) {
			viewBookList.setOnItemLongClickListener(new OnItemLongClickListener() {
				@Override
				public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
					if (position >= 0 && position < models.size()) {
						openDownloadService(models.get(position).title, models.get(position).detail);
					}
					return true;
				}
			});
		}
		viewBookList.setVisibility(ListView.INVISIBLE);
		textViewLoading.setVisibility(ProgressBar.VISIBLE);
		updateHandler = new UpdateHandler();
		
		Intent intent = this.getIntent();
		if (intent != null) {
			preName = intent.getStringExtra(EXTRA_PRENAME);
			if (preName != null) {
				if (D) {
					Log.d(TAG, "preName == " + preName);
				}
				updateThread = new UpdateThread();
				updateThread.start();
			}
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		for (MenuItemModel model : models) {
			if (model != null) {
				model.recycle();
			}
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
	
    private String getStringFromFile(String filename, int[] lengths) {
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
				if (lengths != null && lengths.length > 0) {
					if (line != null && line.length() > "length = ".length() && line.startsWith("length = ")) {
						lengths[0] = Integer.parseInt(line.substring("length = ".length()));
					}
				}
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
	
	private void getCacheFileInfo() {
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
            tempmodels.clear();
            File[] files = filePath.listFiles();
            int[] lengths = new int[1];
            if (files != null) {
            	for (File file : files) {
            		String filepathname = file.getPath();
            		if (filepathname != null &&
            			preName != null &&
            			filepathname.endsWith(".mp3") &&
             			filepathname.contains("_download_") &&
            			(filepathname.startsWith(preName) || filepathname.startsWith("/mnt" + preName))) {
            			String infoText = getStringFromFile(filepathname.replace(".mp3", ".txt"), lengths);
            			int pos = DownloadThread.readMp3FilePos(filepathname);
            			if (pos == -1) {
            				pos = 0;
            			}
            			String percentString = "---";
            			if (lengths[0] != 0) {
            				float percent = (float)(pos / 1000) / (lengths[0] / 1000) * 100.0f;
            				percentString = ((int)(percent * 100) / 100.0) + "%";
            			}
            			String progress = "已下载进度:" + pos + "/" + lengths[0] + "(" + percentString + ")";
            			tempmodels.add(new MenuItemModel(infoText, filepathname, null, progress, null));
            		} else if (filepathname != null &&
            			preName != null &&
            			filepathname.endsWith(".flv") &&
             			filepathname.contains("_download_") &&
            			(filepathname.startsWith(preName) || filepathname.startsWith("/mnt" + preName))) {
            			String infoText = getStringFromFile(filepathname.replace(".flv", ".txt"), lengths);
            			int pos = RTMPDownloadThread.readMp3FilePos(filepathname);
            			if (pos == -1) {
            				pos = 0;
            			}
            			String percentString = "---";
            			if (lengths[0] != 0) {
            				float percent = (float)(pos / 1000) / (lengths[0] / 1000) * 100.0f;
            				percentString = ((int)(percent * 100) / 100.0) + "%";
            			}
            			String progress = "已下载进度:" + pos + "/" + lengths[0] + "(" + percentString + ")";
            			tempmodels.add(new MenuItemModel(infoText, filepathname, null, progress, null));
            		}
            	}
            }
        }
	}
	
	private void openDownloadService(String txtfileText, String mp3filename) {
		if (txtfileText != null && txtfileText.length() > 0) {
			String[] sentenses = txtfileText.split("\\n");
			String tabUrl = null;
			String detailUrl = null;
			String url = null;
			StringBuffer infoText = new StringBuffer();
			if (sentenses != null) {
				for (String sentense : sentenses) {
					if (sentense != null) {
						if (sentense.contains("get_program/")) {
							tabUrl = sentense;
						} else if (sentense.contains("uploads/data/")) {
							detailUrl = sentense;
						} else if (sentense.contains("rtmp")) {
							url = sentense;
						} else if (!sentense.startsWith("[") && 
								!sentense.contains("length = ")) {
							infoText.append(sentense + "\n");
						}
					}
				}
			}
			startActivity(
				new Intent(this, HBKDownloadActivity.class)
				.putExtra(HBKDownloadActivity.EXTRA_URL, url)
				.putExtra(HBKDownloadActivity.EXTRA_MP3_FILENAME, mp3filename)
				.putExtra(HBKDownloadActivity.EXTRA_INFO_TEXT, infoText.toString())
				.putExtra(HBKDownloadActivity.EXTRA_TAB_URL, tabUrl)
				.putExtra(HBKDownloadActivity.EXTRA_DETAIL_URL, detailUrl)
			);
		}
	}
}
