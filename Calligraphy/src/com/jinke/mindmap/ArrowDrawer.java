package com.jinke.mindmap;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

public abstract class ArrowDrawer {
	public Paint mPaint;
	public Matrix arrowMatrix;
	public ArrowDrawer(){
		mPaint = new Paint();
		arrowMatrix = new Matrix();
	}
	public abstract void doDraw(Canvas canvas,int mLastMotionX,int mLastMotionY);
}
