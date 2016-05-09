package com.jinke.mindmap;

import com.jinke.calligraphy.activity.Properyt;
import com.jinke.calligraphy.app.branch.Start;

import android.graphics.Canvas;

public class DownArrowDrawer extends ArrowDrawer{
	
	public DownArrowDrawer(){
		super();
	}
	@Override
	public void doDraw(Canvas canvas, int mLastMotionX, int mLastMotionY) {
		// TODO Auto-generated method stub
		//向上
		arrowMatrix.reset();
		arrowMatrix.postTranslate(mLastMotionX, (mLastMotionY-Start.BLACK_ARROW_BITMAP.getHeight()-Properyt.ARROW_FLIP));
		canvas.drawBitmap(Start.BLACK_ARROW_BITMAP, arrowMatrix, mPaint);
		
		//向右
		arrowMatrix.reset();
		arrowMatrix.postTranslate(mLastMotionX+Start.BLACK_ARROW_BITMAP.getHeight()*2+Properyt.ARROW_FLIP, (mLastMotionY));
		arrowMatrix.preRotate(90);
		canvas.drawBitmap(Start.BLACK_ARROW_BITMAP, arrowMatrix, mPaint);
		
		//向下,红色
		arrowMatrix.reset();
		arrowMatrix.postTranslate(mLastMotionX+Start.BLACK_ARROW_BITMAP.getWidth(),(mLastMotionY+Start.BLACK_ARROW_BITMAP.getHeight()+Properyt.ARROW_FLIP));
		arrowMatrix.preRotate(180);
		canvas.drawBitmap(Start.RED_ARROW_BITMAP, arrowMatrix, mPaint);
		
		//向左
		arrowMatrix.reset();
		arrowMatrix.postTranslate(mLastMotionX-Start.BLACK_ARROW_BITMAP.getHeight()-Properyt.ARROW_FLIP,(mLastMotionY+Start.BLACK_ARROW_BITMAP.getWidth()));
		arrowMatrix.preRotate(-90);
		canvas.drawBitmap(Start.BLACK_ARROW_BITMAP, arrowMatrix, mPaint);
	}

}
