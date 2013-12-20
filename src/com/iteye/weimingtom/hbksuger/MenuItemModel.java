package com.iteye.weimingtom.hbksuger;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class MenuItemModel {
	public String title;
	public String detail;
	public String imageSrc;
	public String progress;
	public String desc;
	public Bitmap bitmap;
	
	public MenuItemModel(String title, String detail, String imageSrc, String progress, String desc) {
		this.title = title;
		this.detail = detail;
		this.imageSrc = imageSrc;
		this.progress = progress;
		if (this.imageSrc != null) {
			this.bitmap = BitmapFactory.decodeFile(this.imageSrc);
		}
		this.desc = desc;
	}
	
	public void recycle() {
		if (this.bitmap != null) {
			this.bitmap.recycle();
			this.bitmap = null;
		}
	}
}
