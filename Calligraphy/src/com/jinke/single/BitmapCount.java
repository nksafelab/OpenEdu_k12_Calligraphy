package com.jinke.single;

import android.util.Log;

public class BitmapCount {
	
	private static final String TAG = "BitmapCount";
	private static BitmapCount bitmapCount = null;
	private int count = 0;
	private BitmapCount(){
		
	}
	public static BitmapCount getInstance(){
		if(bitmapCount == null)
			bitmapCount = new BitmapCount();
		return bitmapCount;
	}
	
	public void createBitmap(String msg){
		count++;
		Log.e(TAG, "create bitmap for :" + msg + " current count:" + count);
	}
	public void recycleBitmap(String msg){
		count --;
		Log.e(TAG, "------recycle bitmap for :" + msg + " current count:" + count);
	}
	
	public void pageChanged(){
		count = 0;
//		Log.e(TAG, "pageChanged reset count :" + count);
	}
	
	public void count()
	{
		Log.e(TAG, count+"!!!!!!!!!!!!!!!!!!!!!!!!!");
	}

}
