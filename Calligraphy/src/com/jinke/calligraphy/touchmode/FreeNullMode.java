package com.jinke.calligraphy.touchmode;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.Log;
import android.view.MotionEvent;

import com.jinke.calligraphy.app.branch.MyView;

public class FreeNullMode implements TouchMode{
	private static final String TAG = "FreeNullMode";
	MyView view;
	
	/* 只有FreeDragMode或者FreeScaleMode会转
	 * 为此状态，此变量保存是哪一个Mode
	 */
	private TouchMode lastTouchMode;
	private Matrix sMatrix;
	
	public void setLastTouchMode(TouchMode lastTouchMode) {
		this.lastTouchMode = lastTouchMode;
	}
	

	public void setsMatrix(Matrix sMatrix) {
		this.sMatrix = sMatrix;
	}


	public FreeNullMode(MyView view) {
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
		view.setTouchMode(lastTouchMode);
		view.getTouchMode().touch_up(event);
	}

	@Override
	public void draw(Canvas canvas) {
		// TODO Auto-generated method stub
		view.baseImpl.draw(canvas, sMatrix);
	}

	@Override
	public void printInfo() {
		// TODO Auto-generated method stub
		Log.i("touchmode", "This is " + TAG);
		
	}


	@Override
	public Matrix getMatrix() {
		// TODO Auto-generated method stub
		return sMatrix;
	}


	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}

}
