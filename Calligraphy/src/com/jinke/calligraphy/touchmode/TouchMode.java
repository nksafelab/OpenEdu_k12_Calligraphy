package com.jinke.calligraphy.touchmode;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.view.MotionEvent;

public interface TouchMode {
	public void touch_action_down(MotionEvent event);
	public void touch_action_pointer_down(MotionEvent event);
	public void touch_move(MotionEvent event);
	public void touch_action_pointer_up(MotionEvent event);
	public void touch_up(MotionEvent event);
	public void draw(Canvas canvas);
	public void printInfo();
	public Matrix getMatrix();
	public void clear();
}
