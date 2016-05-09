package com.jinke.calligraphy.touchmode;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Log;
import android.view.MotionEvent;
import com.jinke.calligraphy.app.branch.MyView;
import com.jinke.calligraphy.app.branch.Start;
import com.jinke.calligraphy.database.CalligraphyDB;
import com.jinke.mindmap.MindMapItem;

public class MindSlideMode implements TouchMode{
	private static final String TAG = "MindSlideMode";
	MyView view;
	Paint mPaint;
	private Matrix sMatrix;
	private Path mPath;
	float move_x;
	float move_y;
	float start_x;
	float start_y;
	float pre_dst;
	float flip_dst;
	private static final int mHeight = 200;//箭头高度
	private static final int mmWidth = 50;//箭头宽度
	int MoveCount;
	float move_old_x=0;
	float move_old_y=0;
	private MindMapItem mindItem = null;
	
	public MindSlideMode(MyView v){
		this.view = v;
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setColor(Color.RED); 
		// 设置paint的风格为“空心”  
        mPaint.setStyle(Paint.Style.STROKE);
        mPath = new Path();
	}
	public void setStartPoint(float start_x,float start_y){
		this.start_x = start_x;
		this.start_y = start_y;
		MoveCount=0;//zk20121109
	}
	public void setMindItem(MindMapItem mindItem){
		this.mindItem = mindItem;
		this.pre_dst = mindItem.getFlipDstX();
	}
	@Override
	public void touch_action_down(MotionEvent event) {
		
	}

	@Override
	public void touch_action_pointer_down(MotionEvent event) {
		
	}

	@Override
	public void touch_move(MotionEvent event) {
		Log.i("0801", "MindSlideMode touch_mode");
		move_x = event.getX();
		move_y = event.getY();
		mPath.reset();
		if(move_x < start_x){
			//向左滑动
			//ly
			//设置箭头
//			float x1 = mmWidth;
			float x1 = mmWidth + 0;
			float y1 = start_y - 0.5f * mHeight;
			mPath.moveTo(x1, y1);
			//ly
//			x1 = 0;
			x1 = 0;
			y1 = start_y;
			mPath.lineTo(x1, y1);
			
			//ly
			//x1 = mmWidth;
			x1 = 0 + mmWidth;
			y1 = start_y + 0.5f * mHeight;
			mPath.lineTo(x1, y1);
		}else{
			//向右滑动
			//ly
			//设置箭头位置
//			float x1 = 600 - mmWidth;
			float x1 = 1600 - mmWidth;
			float y1 = start_y - 0.5f * mHeight;
			mPath.moveTo(x1, y1);
//			x1 = 600;
			x1 = 1600;
			y1 = start_y;
			mPath.lineTo(x1, y1);
			x1 = 1600 - mmWidth;
//			x1 = 600 - mmWidth;
			y1 = start_y + 0.5f * mHeight;
			mPath.lineTo(x1, y1);
		}
		
		
		//ly
		//刷屏
		view.mBitmap.eraseColor(Color.WHITE);
		//end
		
		
		flip_dst = pre_dst + move_x - start_x;
		Log.v(TAG, "flip_dst:" + flip_dst);
		mindItem.setFlipDstX((int)flip_dst);
		view.cursorBitmap.updateHandwriteState();
	}

	@Override
	public void touch_action_pointer_up(MotionEvent event) {
		// TODO Auto-generated method stub
		Log.v(TAG, "Pointer_up" );
	}

	@Override
	public void touch_up(MotionEvent event) {
		// TODO Auto-generated method stub
		CalligraphyDB.getInstance(Start.context)
			.updateMindDstx(Start.getPageNum(), 3, mindItem.getMindID(), flip_dst);
		view.setTouchMode(view.getHandWriteMode());
		Log.v(TAG, "touch_up" );
	}

	@Override
	public void draw(Canvas canvas) {
		
		Log.e("mindflip", "move_x:" + move_x + " move_y:" + move_y);
		view.baseImpl.draw(canvas, sMatrix);
		mPaint.setStrokeWidth(5);
		canvas.drawCircle(move_x, move_y, 100, mPaint);
		canvas.drawPath(mPath, mPaint);
		if(move_old_x==move_x && move_old_y==move_y)//zk20121109
		{
			if(MoveCount>5)
			{
				CalligraphyDB.getInstance(Start.context)
				.updateMindDstx(Start.getPageNum(), 3, mindItem.getMindID(), flip_dst);
				view.setTouchMode(view.getHandWriteMode());
			}
			MoveCount++;
		}
		else
		{
			move_old_x=move_x;
			move_old_y=move_y;
			MoveCount=0;
		}
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
