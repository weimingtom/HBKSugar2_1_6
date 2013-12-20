package com.iteye.weimingtom.hbksuger;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class HBKProgramPageActivity extends Activity {
	public final static String EXTRA_TABNUM = "com.iteye.weimingtom.hbksuger.HBKProgramPageActivity.tabNum";
	public final static String EXTRA_TABNAME = "com.iteye.weimingtom.hbksuger.HBKProgramPageActivity.tabName";
	public final static int MESSAGE_UPDATE_THREAD = 111;
	
	private ListView viewBookList;
	private TextView textViewTitle;
	private MenuItemAdapter adapter;
	private TextView textViewLoading;
	private List<MenuItemModel> models;
	private List<MenuItemModel> tempmodels;
	private UpdateHandler updateHandler;
	private UpdateThread updateThread;
	private WebDowner webDowner;
	private String tabName, tabNum;
	
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
				List<WebDowner.PageInfo> pages = webDowner.getPages(tabName);
				tempmodels.clear();
				for (WebDowner.PageInfo page : pages) {
					tempmodels.add(new MenuItemModel(page.info, page.getUrl(), null, null, page.getDescUrl()));
				}
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
		
		textViewLoading.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
		
		textViewTitle.setText("节目表");
		models = new ArrayList<MenuItemModel>();
		tempmodels = new ArrayList<MenuItemModel>();
		adapter = new MenuItemAdapter(this, models);
		viewBookList.setAdapter(adapter);
		viewBookList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (position >= 0 && position < models.size()) {
					startActivity(new Intent(HBKProgramPageActivity.this, HBKProgramDetailActivity.class)
						.putExtra(HBKProgramDetailActivity.EXTRA_INFO_TEXT, models.get(position).title)
						.putExtra(HBKProgramDetailActivity.EXTRA_DETAIL_URL, models.get(position).detail)
						.putExtra(HBKProgramDetailActivity.EXTRA_TAB_URL, tabName)
						.putExtra(HBKProgramDetailActivity.EXTRA_DESC_URL, models.get(position).desc)
					);
				}
			}
		});
		viewBookList.setVisibility(ListView.INVISIBLE);
		textViewLoading.setVisibility(ProgressBar.VISIBLE);
		webDowner = new WebDowner();
		updateHandler = new UpdateHandler();
		
		Intent intent = this.getIntent();
		if (intent != null) {
			tabName = intent.getStringExtra(EXTRA_TABNAME);
			tabNum = intent.getStringExtra(EXTRA_TABNUM);
			if (tabNum != null && tabNum.length() > 0) {
				textViewTitle.setText("节目表 - " + tabNum);
			}
			if (tabName != null) {
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
	
}
