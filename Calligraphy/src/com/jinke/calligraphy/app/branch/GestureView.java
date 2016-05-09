package com.jinke.calligraphy.app.branch;

import android.content.Context;
import android.gesture.GestureOverlayView;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class GestureView extends GestureOverlayView{
   
	public GestureView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public static float y;
	 
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(event.getAction()==MotionEvent.ACTION_DOWN)
		y=event.getY();
		

		return super.onTouchEvent(event);
		
	}
	@Override
	public boolean  dispatchTouchEvent(MotionEvent event) {
		return super.dispatchTouchEvent(event);
		
	}

	
	

}
