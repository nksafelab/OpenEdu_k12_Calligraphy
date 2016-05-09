package com.jinke.calligraphy.touchmode;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.Log;
import android.view.MotionEvent;

import com.jinke.calligraphy.app.branch.MyView;

public class StartMode implements TouchMode{

	private static final String TAG = "StartMode";
	MyView view;
	
	public StartMode(MyView view) {
		this.view = view;
	}

	@Override
	public void draw(Canvas canvas) {
		// TODO Auto-generated method stub
		Log.i(TAG, "draw");
//		view.baseImpl.dr
	}

	@Override
	public void touch_action_down(MotionEvent event) {
		// TODO Auto-generated method stub
		Log.i(TAG, "touch action down");
		if(checkRight(event.getX(), event.getY())){
			view.setTouchMode(view.getSideDownMode());
			/*
    		doTouch(event);
    		*/
    	} else{
    		view.setTouchMode(view.getHandWriteMode());
    		/*
    		touch_start(x, y);
    		*/
    	}
		view.getTouchMode().touch_action_down(event);
	}

	@Override
	public void touch_action_pointer_down(MotionEvent event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void touch_action_pointer_up(MotionEvent event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void touch_move(MotionEvent event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void touch_up(MotionEvent event) {
		// TODO Auto-generated method stub
		
	}
	
	public boolean checkRight(float x, float y){
		if (x>500 && y<100 || x<100 && y<100) 
			return true;
		else
			return false;
		
		/*
		if (x>500 && y<100 || x<100 && y<100) 
			splitFlag = true;
		else 
			splitFlag = false;
		if(over) {
			if((x < 100 && y > 533 && y < 600)){
				splitFlag = true;
				dispear = false;
			}
			if((x > 500 && y > 533 && y < 600)){
				splitFlag = true;
				dispear = false;
			}
		}
		return splitFlag;
		*/
	}

	@Override
	public void printInfo() {
		// TODO Auto-generated method stub
		Log.i("touchmode", "This is " + TAG);
		
	}

	@Override
	public Matrix getMatrix() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}

}
