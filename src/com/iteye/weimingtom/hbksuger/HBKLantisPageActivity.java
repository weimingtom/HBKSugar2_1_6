package com.iteye.weimingtom.hbksuger;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class HBKLantisPageActivity extends Activity {
	public final static String EXTRA_TABNAME = "com.iteye.weimingtom.hbksuger.HBKLantisPageActivity.tabName";
	public final static String EXTRA_TITLENAME = "com.iteye.weimingtom.hbksuger.HBKLantisPageActivity.titleName";
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
	private String tabName;
	private String titleName;
	private List<WebDowner.LantisPageInfo> pages;
	
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
				pages = webDowner.getLantisPages(tabName);
				tempmodels.clear();
				for (WebDowner.LantisPageInfo page : pages) {
					tempmodels.add(new MenuItemModel(page.title, 
						page.titleHref + "\n" +
						page.banner + "\n" +
						Html.fromHtml(page.comment).toString() + "\n" +
						page.asx32k + "\n" + 
						page.asx64k + "\n" +
						Html.fromHtml(page.time).toString(), 
						null, null, null));
				}
			} catch (Throwable e) {
				tempmodels.clear();
				e.printStackTrace();
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
		
		textViewTitle.setText("Lantis节目表");
		models = new ArrayList<MenuItemModel>();
		tempmodels = new ArrayList<MenuItemModel>();
		adapter = new MenuItemAdapter(this, models);
		viewBookList.setAdapter(adapter);
		viewBookList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (position >= 0 && position < pages.size()) {
					startActivity(new Intent(HBKLantisPageActivity.this, HBKLantisDetailActivity.class)
						.putExtra(HBKLantisDetailActivity.EXTRA_TITLE_HREF, pages.get(position).titleHref)
						.putExtra(HBKLantisDetailActivity.EXTRA_TITLE, pages.get(position).title)
						.putExtra(HBKLantisDetailActivity.EXTRA_BANNER, pages.get(position).banner)
						.putExtra(HBKLantisDetailActivity.EXTRA_COMMENT, pages.get(position).comment)
						.putExtra(HBKLantisDetailActivity.EXTRA_ASX32K, pages.get(position).asx32k)
						.putExtra(HBKLantisDetailActivity.EXTRA_ASX64K, pages.get(position).asx64k)
						.putExtra(HBKLantisDetailActivity.EXTRA_TIME, pages.get(position).time)
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
			titleName = intent.getStringExtra(EXTRA_TITLENAME);
			if (titleName != null && titleName.length() > 0) {
				textViewTitle.setText("Lantis - " + titleName);
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
