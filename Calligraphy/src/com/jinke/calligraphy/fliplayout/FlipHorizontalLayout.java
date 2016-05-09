package com.jinke.calligraphy.fliplayout;

import com.jinke.calligraphy.app.branch.EditableCalligraphy;
import com.jinke.calligraphy.app.branch.Start;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.LinearLayout;

public class FlipHorizontalLayout extends LinearLayout{

	private static final String TAG = "HandWriteEditLayout";

	private float mX, mY;
	private int mdX, mdY;
	public static 	int ml;

	public int mr, mt, mb;
	private Handler handler;
	
	public FlipHorizontalLayout(Context context , Handler handler) {
		super(context);
		this.handler = handler;
	}

	public void setLayout(int t){
		Log.e("layout", "t:"+t);
		Log.e("layout", "ml:"+ml);
		Log.e("layout", "mr:"+mr);
		Log.e("layout", "getHeight:"+getHeight());
		layout(Start.SCREEN_WIDTH - t, getTop(), Start.SCREEN_WIDTH - t  + getWidth(), getBottom());
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
		
		mt = t;
		mb = b;
		
		if(r > Start.SCREEN_WIDTH){
			layout(Start.SCREEN_WIDTH - getWidth(), mt, Start.SCREEN_WIDTH, mb);
			return super.onTouchEvent(event);
		}
		
		if(l < 0) {
			layout(0, mt, getWidth(), mb);
			return super.onTouchEvent(event);
		}	
		
		System.out.println("$$$$$$$$$$$$$$$$ tt"+(Start.SCREEN_WIDTH-l));
		
		
		EditableCalligraphy.set_Horizonal_Flip_dst(Start.SCREEN_WIDTH-r);
//		EditableCalligraphy.flip_dst = t;  //改成按比例函数设置dst
		
		switch(event.getAction()){
		case MotionEvent.ACTION_MOVE:
			System.out.println("########move");
			
				mdX = (int)(x - mX);
				mdY = (int)(y - mY);
				Log.i(TAG, "onTouchEvent: x:" + x + " y:" + y + " l:" + l + " r:" + r + " t:" + t + " b:" + b);
	//			layout(x, y, x + r - l , y + b - t);
				ml = l + mdX;
				mr = r + mdX;
//				mt = t + mdY;
//				mb = b + mdY;
				
				layout(ml, mt, mr, mb);
				handler.sendEmptyMessage(t);
				invalidate();
				break;
		case MotionEvent.ACTION_UP:
			if(t <0)
				
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
		}
		return super.onInterceptTouchEvent(ev);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub
		super.onLayout(changed, l, t, r, b);
//		Log.i(TAG, "onLayout");
	}

	@Override
	public void invalidate() {
		// TODO Auto-generated method stub
		super.invalidate();
		Log.i(TAG, "invalidate");
	}
	
	
	
	

}
