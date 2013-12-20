package com.iteye.weimingtom.hbksuger;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class HBKRemoveProgramActivity extends Activity {
	public final static String EXTRA_PRENAME = "com.iteye.weimingtom.hbksuger.HBKRemoveProgramActivity.preName";
	public final static String EXTRA_INFO = "com.iteye.weimingtom.hbksuger.HBKRemoveProgramActivity.info";
	public final static String EXTRA_THUMBNAIL = "com.iteye.weimingtom.hbksuger.HBKRemoveProgramActivity.thumbnail";
	
	private LinearLayout linearLayoutTitle;
	private TextView textViewTitle;
	private ImageView imageViewThumbnail;
	private TextView textViewInfo;
	private Button buttonOK;
	private Button buttonCancel;
	
	private String preName, info, thumbnail;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.remove_program);
		
		textViewTitle = (TextView) findViewById(R.id.textViewTitle);
		linearLayoutTitle = (LinearLayout) findViewById(R.id.linearLayoutTitle);
		imageViewThumbnail = (ImageView) findViewById(R.id.imageViewThumbnail);
		textViewInfo = (TextView) findViewById(R.id.textViewInfo);
		buttonOK = (Button) findViewById(R.id.buttonOK);
		buttonCancel = (Button) findViewById(R.id.buttonCancel);
		
		textViewTitle.setText("删除下载的节目");
		textViewTitle.setTextColor(0xff000000);
		linearLayoutTitle.setBackgroundColor(0xfffcabbd);

		buttonOK.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				deleteFile();
			}
		});
		buttonCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				finish();
			}
		});
		
		Intent intent = this.getIntent();
		if (intent != null) {
			thumbnail = intent.getStringExtra(EXTRA_THUMBNAIL);
			info = intent.getStringExtra(EXTRA_INFO);
			preName = intent.getStringExtra(EXTRA_PRENAME);
			if (thumbnail != null) {
				this.imageViewThumbnail.setImageURI(Uri.fromFile(new File(thumbnail)));
			}
			if (info != null) {
				this.textViewInfo.setText(info);
			}
		}
	}
	
	private void deleteFile() {
		int num = 0;
		if (preName != null) {
			File sdcardDir = Environment.getExternalStorageDirectory();
	        String path = sdcardDir.getPath() + "/hbksugar";
	    	if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
	            File filePath = new File(path);
	            if (!filePath.exists()) {
	        		Toast.makeText(this, 
	        				"目录" + path + "不存在", 
	        				Toast.LENGTH_SHORT).show();
	        		setResult(RESULT_CANCELED);
	    			finish();
	    			return;
	            }
	            File[] files = filePath.listFiles();
	            if (files != null) {
	            	for (File file : files) {
	            		String filepathname = file.getPath();
	            		if (filepathname != null &&
	            			filepathname.endsWith(".mp3") &&
	            			filepathname.contains("_download_")) {
	            			String key = filepathname.substring(0, filepathname.lastIndexOf("_download_"));
	            			if (key != null && preName.equals(key)) {
	            				file.delete();
	            				new File(filepathname.replace(".mp3", ".txt")).delete();
	            				new File(filepathname.replace(".mp3", ".pos")).delete();
	            				num++;
	            			}
	            		} else if (filepathname != null &&
	            			filepathname.endsWith(".flv") &&
	            			filepathname.contains("_download_")) {
	            			String key = filepathname.substring(0, filepathname.lastIndexOf("_download_"));
	            			if (key != null && preName.equals(key)) {
	            				file.delete();
	            				new File(filepathname.replace(".flv", ".txt")).delete();
	            				new File(filepathname.replace(".flv", ".pos")).delete();
	            				num++;
	            			}
	            		}
	            	}
	            }
	        } else {
				Toast.makeText(this, 
						"未挂接外部存储", 
						Toast.LENGTH_SHORT).show();
				setResult(RESULT_CANCELED);
				finish();
				return;
	        }
			Toast.makeText(this, 
				"删除完成"/* + num*/, 
				Toast.LENGTH_SHORT).show();
			setResult(RESULT_OK);
			finish();
			return;
		} else {
			Toast.makeText(this, 
					"前缀名为空", 
					Toast.LENGTH_SHORT).show();
				setResult(RESULT_OK);
			setResult(RESULT_CANCELED);
			finish();
			return;
		}
	}
}
