package com.iteye.weimingtom.hbksuger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
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

public class HBKCachePageActivity extends Activity {
	public final static int MESSAGE_UPDATE_THREAD = 111;
	
	public final static int REQUEST_REMOVE = 1;
	
	private ListView viewBookList;
	private TextView textViewTitle;
	private MenuItemAdapter adapter;
	private TextView textViewLoading;
	private List<MenuItemModel> models;
	private List<MenuItemModel> tempmodels;
	private UpdateHandler updateHandler;
	private UpdateThread updateThread;
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
		
		textViewTitle.setText("已下载节目（长按删除节目）");
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
					startActivity(new Intent(HBKCachePageActivity.this, HBKCacheProgramActivity.class)
						.putExtra(HBKCacheProgramActivity.EXTRA_PRENAME, models.get(position).detail)
					);
				}
			}
		});
		viewBookList.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				if (position >= 0 && position < models.size()) {
					startActivityForResult(new Intent(HBKCachePageActivity.this, HBKRemoveProgramActivity.class)
						.putExtra(HBKRemoveProgramActivity.EXTRA_THUMBNAIL, models.get(position).imageSrc)
						.putExtra(HBKRemoveProgramActivity.EXTRA_INFO, models.get(position).title)
						.putExtra(HBKRemoveProgramActivity.EXTRA_PRENAME, models.get(position).detail),
						REQUEST_REMOVE
					);
				}
				return true;
			}
			
		});
		refresh();
	}
	
	private void refresh() {
		viewBookList.setVisibility(ListView.INVISIBLE);
		textViewLoading.setVisibility(ProgressBar.VISIBLE);
		updateHandler = new UpdateHandler();
		updateThread = new UpdateThread();
		updateThread.start();
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
	
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_REMOVE && resultCode == RESULT_OK) {
			refresh();
		}
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
				if (!line.startsWith("[") && 
					!line.startsWith("http") &&
					!line.startsWith("rtmp") &&
					!line.startsWith("length = ")) {
					sb.append(line);
					sb.append('\n');
				}
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
		tempmodels.clear();
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
            Hashtable<String, String> map = new Hashtable<String, String>(); 
            if (files != null) {
            	for (File file : files) {
            		String filepathname = file.getPath();
            		if (filepathname != null &&
            			filepathname.endsWith(".mp3") &&
            			filepathname.contains("_download_")) {
            			String key = filepathname.substring(0, filepathname.lastIndexOf("_download_"));
            			String info = map.get(key);
            			if (info == null) {
            				String infoText = getStringFromFile(filepathname.replace(".mp3", ".txt"));
            				map.put(key, infoText);
            			}
            		} else if (filepathname != null &&
            			filepathname.endsWith(".flv") &&
            			filepathname.contains("_download_")) {
            			String key = filepathname.substring(0, filepathname.lastIndexOf("_download_"));
            			String info = map.get(key);
            			if (info == null) {
            				String infoText = getStringFromFile(filepathname.replace(".flv", ".txt"));
            				map.put(key, infoText);
            			}
            		}
            	}
            }
            for (Entry<String, String> entry : map.entrySet()) {
            	String key = entry.getKey();
            	String info = entry.getValue();
            	if (key != null) {
            		tempmodels.add(new MenuItemModel(info, key, key + ".jpg", null, null));
            	}
            }
        }
	}
}
