package com.jinke.calligraphy.app.branch;

import android.graphics.Bitmap;

class PageItem{
	int pagenum;
	String path;
	Bitmap bgBitmap;
	public PageItem(int pagenum,String path,Bitmap bitmap){
		this.pagenum = pagenum;
		this.path = path;
		this.bgBitmap = bitmap;
	}
}
