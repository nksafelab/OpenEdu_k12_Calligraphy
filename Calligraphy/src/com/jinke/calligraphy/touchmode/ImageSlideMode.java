package com.jinke.calligraphy.touchmode;

import com.jinke.calligraphy.app.branch.EditableCalligraphyItem;
import com.jinke.calligraphy.app.branch.MyView;
import com.jinke.calligraphy.app.branch.Start;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Log;
import android.view.MotionEvent;

public class ImageSlideMode implements TouchMode{
	private static final String TAG = "ImageSlideMode";
	MyView view;
	private Matrix sMatrix;
	Paint mPaint;
	float move_x;
	float move_y;
	float start_x;
	float start_y;
	float flip_dst;
	float flipMax;
	private static final int mHeight = 200;//箭头高度
	private static final int mmWidth = 50;//箭头宽度
	private Path mPath;
	private EditableCalligraphyItem picItem = null;
	
	public ImageSlideMode(MyView v){
		this.view = v;
		mPath = new Path();
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setColor(Color.RED); 
		// 设置paint的风格为“空心”  
        mPaint.setStyle(Paint.Style.STROKE);  
        
	}
	public void setsMatrix(Matrix sMatrix) {
		this.sMatrix = sMatrix;
	}
	public void setStartPoint(float start_x,float start_y){
		this.start_x = start_x;
		this.start_y = start_y;
	}
	public void setPicItem(EditableCalligraphyItem picItem){
		float[] values = new float[9];
		picItem.getMatrix().getValues(values);
		this.picItem = picItem;
		flipMax = Start.SCREEN_WIDTH - picItem.getWidth() * values[0];
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
	public void touch_move(MotionEvent event) {
		Log.i("0801", "ImageSlide touch_mode");
		// TODO Auto-generated method stub
		
		move_x = event.getX();
		move_y = event.getY();
		mPath.reset();
		if(flipMax >= 0){
			view.invalidate();
			Log.v(TAG, "不需滑动");
			return;
		}
		if(move_x < start_x){
			//向左滑动
			float x1 = mmWidth;
			float y1 = start_y - 0.5f * mHeight;
			mPath.moveTo(x1, y1);
			x1 = 0;
			y1 = start_y;
			mPath.lineTo(x1, y1);
			x1 = mmWidth;
			y1 = start_y + 0.5f * mHeight;
			mPath.lineTo(x1, y1);
		}else{
			//向右滑动
			float x1 = 600 - mmWidth;
			float y1 = start_y - 0.5f * mHeight;
			mPath.moveTo(x1, y1);
			x1 = 600;
			y1 = start_y;
			mPath.lineTo(x1, y1);
			x1 = 600 - mmWidth;
			y1 = start_y + 0.5f * mHeight;
			mPath.lineTo(x1, y1);
		}
		
		flip_dst += move_x - start_x;
		Log.v(TAG, "flip_dst:" + flip_dst);
		if(flip_dst > 0)
			flip_dst = 0;//不许滑动到原位置的右侧
		if(flip_dst < flipMax)
			flip_dst = flipMax;
		picItem.setFlipDstX(flip_dst);
		
		view.cursorBitmap.updateHandwriteState();
		
	}

	@Override
	public void touch_action_pointer_up(MotionEvent event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void touch_up(MotionEvent event) {
		// TODO Auto-generated method stub
		view.setTouchMode(view.getHandWriteMode());
//		view.getTouchMode().touch_up(event);
	}

	@Override
	public void draw(Canvas canvas) {
		Log.v(TAG, "imageSlideMode draw");
		// TODO Auto-generated method stub
		view.baseImpl.draw(canvas, sMatrix);
		if(flipMax >= 0){
			mPaint.setStrokeWidth(1);
			mPaint.setTextSize(50);
			canvas.drawText("屏幕内，不需拖动", 0.5f*Start.SCREEN_WIDTH - 4*50f, move_y - 50, mPaint);
			return;
		}
		// 设置“空心”的外框的宽度  
        mPaint.setStrokeWidth(5);
		canvas.drawCircle(move_x, move_y, 100, mPaint);
		canvas.drawPath(mPath, mPaint);
	}

	@Override
	public void printInfo() {
		// TODO Auto-generated method stub
		Log.v("touchmode", TAG);
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
