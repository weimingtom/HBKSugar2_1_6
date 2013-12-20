package com.iteye.weimingtom.hbksuger;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class HBKMainMenuActivity extends Activity {
	private ListView viewBookList;
	private TextView textViewTitle;
	private MenuItemAdapter adapter;
	
	private List<MenuItemModel> models;
	

 	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.main_menu);
		viewBookList = (ListView) findViewById(R.id.viewBookList);
		textViewTitle = (TextView) findViewById(R.id.textViewTitle);
		
		textViewTitle.setText("声音糖果");
		models = new ArrayList<MenuItemModel>();
		
		models.add(new MenuItemModel("hibiki", "网络电台数据流转换", null, null, null));
		models.add(new MenuItemModel("下载管理器", "浏览本地缓存文件", null, null, null));
		models.add(new MenuItemModel("音乐播放器", "播放转换后的mp3文件", null, null, null));
		models.add(new MenuItemModel("后台服务与配置", "后台服务控制台，文件清理，全局配置", null, null, null));
		models.add(new MenuItemModel("Google翻译TTS", "谷歌日文TTS", null, null, null));
		models.add(new MenuItemModel("Lantis网络电台", "http://lantis-net.com/index.html\n（播放需外部播放器VLC。如果用浏览器打开则可作为mp3格式播放或保存）", null, null, null));
		models.add(new MenuItemModel("hibiki-radio官网", "http://hibiki-radio.jp/mokuji", null, null, null));
		models.add(new MenuItemModel("Lantis官网", "http://lantis-net.com/index.html", null, null, null));
		models.add(new MenuItemModel("animate.tv官网", "http://www.animate.tv/radio", null, null, null));
		//models.add(new MenuItemModel("animate.tv网络电台", "http://www.animate.tv/radio\n（播放需外部播放器VLC）", null, null, null));
		models.add(new MenuItemModel("关于", "关于与帮助信息", null, null, null));
		
		adapter = new MenuItemAdapter(this, models);
		viewBookList.setAdapter(adapter);
		viewBookList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				switch(position) {
				case 0:
					startActivity(new Intent(HBKMainMenuActivity.this, 
							HBKProgramTabActivity.class));
					break;

				case 1:
					startActivity(new Intent(HBKMainMenuActivity.this, 
							HBKCachePageActivity.class));
					break;
					
				case 2:
					startActivity(new Intent(HBKMainMenuActivity.this, 
							HBKMediaPlayerActivity.class));
					break;
					
				case 3:
					startActivity(new Intent(HBKMainMenuActivity.this, 
							HBKDownloadActivity.class));
					break;

				case 4:
					startActivity(new Intent(HBKMainMenuActivity.this, 
							HBKGoogleTTSActivity.class));
					break;
					
				case 5:
					startActivity(new Intent(HBKMainMenuActivity.this, 
							HBKLantisTabActivity.class));
					break;

				case 6:
					openWebpage("http://hibiki-radio.jp/mokuji");
					break;
					
				case 7:
					openWebpage("http://lantis-net.com/index.html");
					break;
					
				case 8:
					openWebpage("http://www.animate.tv/radio");
					break;
					
				/*
				case 7:
					startActivity(new Intent(HBKMainMenuActivity.this, 
							HBKAnimatePageActivity.class));
					break;
				*/
					
				case 9:
					startActivity(new Intent(HBKMainMenuActivity.this, 
							HBKAboutActivity.class));
					break;
				}
			}
		});
		
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		for (MenuItemModel model : models) {
			if (model != null) {
				model.recycle();
			}
		}
	}
	
	private void openWebpage(String url) {
		if (url != null && url.length() > 0) {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.parse(url), "*/*");
			try {
				startActivity(intent);
			} catch (Throwable e) {
				e.printStackTrace();
				Toast.makeText(this, 
						"找不到可用的应用程序", Toast.LENGTH_SHORT)
						.show();
			}		
		} else {
			Toast.makeText(this, 
					"链接为空", Toast.LENGTH_SHORT)
					.show();					
		}
	}
}
