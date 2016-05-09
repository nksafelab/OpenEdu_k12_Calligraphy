package com.jinke.calligraphy.touchmode;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.Log;
import android.view.MotionEvent;

import com.jinke.calligraphy.app.branch.MyView;

public class CursorNullMode implements TouchMode{
	
	private static final String TAG = "CursorNullMode";
	MyView view;
	
	public CursorNullMode(MyView view) {
		this.view = view;
	}

	@Override
	public void touch_action_down(MotionEvent event) {
		// TODO Auto-generated method stub
		
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

	@Override
	public void draw(Canvas canvas) {
		// TODO Auto-generated method stub
		
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
