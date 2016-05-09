package com.jinke.calligraphy.app.branch;

import com.jinke.mywidget.widget.Panel;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.LinearLayout;

public class HandWriteEditLayout extends LinearLayout{

	private static final String TAG = "HandWriteEditLayout";

	private float mX, mY;
	private int mdX, mdY;
	public static 	int ml;

	public int mr, mt, mb;
	private Handler handler;
	public int temp;
	
	
	public HandWriteEditLayout(Context context , Handler handler) {
		super(context);
		this.handler = new Handler();
		// TODO Auto-generated constructor stub
//		setBackgroundColor(Color.GREEN);
	}

	public void setLayout(int t){
		Log.e("layout", "setLayout called:");
		Log.e("layout", "t:"+t); 
		Log.e("layout", "ml:"+ml);
		Log.e("layout", "mr:"+mr);
		Log.e("layout", "getHeight:"+getHeight());
//		layout(getLeft(), 800-getHeight(), getRight(), 800);
//		layout(getLeft(), t-getHeight(), getRight(), t);
		
		
			layout(getLeft(), t, getRight(), t+getHeight());	
			temp = t;
			handler.sendEmptyMessage(0); 
			invalidate();
		
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		int x = (int) event.getX();
		int y = (int) event.getY();
		
		int l = getLeft();
		int r = getRight();
		int t = getTop();
		int b = getBottom();
		
		ml = l;
		mr = r;
		
		if(t<0){
			t=0;
			layout(ml, 0, mr, getHeight());
			handler.sendEmptyMessage(0);
			invalidate();
			Log.e("queue", "touch out of bound");
			WorkQueue.getInstance().endFlipping();
			return super.onTouchEvent(event);
		}
		
		

		for(int i=0;i<CursorDrawBitmap.listEditableCalligraphy.size();i++){
			CursorDrawBitmap.listEditableCalligraphy.get(i).setFlip_dst(t);
		}
		
//		EditableCalligraphy.flip_dst = t;  //改成按比例函数设置dst
		
		
		
		System.out.println("$$$$$$$$$$$$$$$$ tt"+t);
		
		switch(event.getAction()){
		case MotionEvent.ACTION_MOVE:
			
			System.out.println("########move");
			
				mdX = (int)(x - mX);
				mdY = (int)(y - mY);
				Log.i(TAG, "onTouchEvent: x:" + x + " y:" + y + " l:" + l + " r:" + r + " t:" + t + " b:" + b);
	//			layout(x, y, x + r - l , y + b - t);
	//			ml = l + mdX;
	//			mr = r + mdX;
				mt = t + mdY;
				mb = b + mdY;
				
				layout(ml, mt, mr, mb);
				temp = mt;
				handler.sendEmptyMessage(t);
				invalidate();
				break;
		case MotionEvent.ACTION_UP:
			
//			if(t <0)
			Log.e("queue", "touch up");
			WorkQueue.getInstance().endFlipping();
				break;
			
		}
		
		return super.onTouchEvent(event);
	}

	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		
		float x = ev.getX() , y = ev.getY();
		
		switch(ev.getAction()){
		case MotionEvent.ACTION_DOWN:
			Log.i(TAG, "onInterceptTouchEvent: ACTION_DOWN");

			
			mX = x;
			mY = y;
			return false;
		case MotionEvent.ACTION_MOVE:
			float dx = Math.abs(mX - x);
			float dy = Math.abs(mY - y);
			
			if(dx >= 4 || dy >= 4){
				Log.i(TAG, "onInterceptTouchEvent: left:" + getLeft() + " right:" + getRight() + " top:" + 
						getTop() + " bottom:" + getBottom());
				mX = x;
				mY = y;
				return true;
			}
			mX = x;
			mY = y;
			break;
		case MotionEvent.ACTION_UP:
			return false;
		}
		return super.onInterceptTouchEvent(ev);
		
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub
		
			super.onLayout(changed, l, t, r, b);
			Log.i(TAG, "onLayout l:" + getLeft() + " t:"+ getTop() + " r:" + getRight() + " b:" + getBottom());
		
	}
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
			
		
	}

	@Override
	public void invalidate() {
		// TODO Auto-generated method stub
		super.invalidate();
		Log.i(TAG, "invalidate");
		
	}
	
	
	
	

}
