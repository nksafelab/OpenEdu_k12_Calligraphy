package com.jinke.calligraphy.touchmode;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;

import com.jinke.calligraphy.app.branch.BaseBitmap;
import com.jinke.calligraphy.app.branch.Calligraph;
import com.jinke.calligraphy.app.branch.EditableCalligraphy;
import com.jinke.calligraphy.app.branch.MyView;
import com.jinke.calligraphy.app.branch.Start;

public class FreeScaleMode implements TouchMode{

	private static final String TAG = "FreeScaleMode";
	
	private MyView view;
		
	private static int  SMALLER = 1;
	private static int  BIGGER = 2;
	private static int  NORMAL = 3;
	
	private int scaleMode = NORMAL;
	
	private Matrix 	sMatrix;
	private PointF 	midPointF;
	private float  	oldDist;
	private Canvas 	sCanvas;
	private float  	sOffsetX = 0.0f;
	private float  	sOffsetY = 0.0f;
	private RectF	target ;
	private float  	oldWidth, oldHeight;
	private float  	newWidth, newHeight;
	
	public FreeScaleMode(MyView view) {
		this.view = view;
		
    	sMatrix = new Matrix();
    	sCanvas = new Canvas();
    	target = new RectF();
    	//oldWidth = 600;
    	//oldHeight = 1024;
    	oldWidth = 1600;
    	oldHeight = 2460;
    	newWidth = oldWidth;
    	newHeight = oldHeight;
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
    	Log.i(TAG, "action pointer up");
    	view.setTouchMode(view.getFreeNullMode());
    	FreeNullMode fnm = (FreeNullMode) view.getFreeNullMode();
    	fnm.setLastTouchMode(this);
    	fnm.setsMatrix(sMatrix);
	}

	@Override
	public void touch_move(MotionEvent event) {
		Log.i("0801", "freescaleMode touch_mode");
		// TODO Auto-generated method stub
		//此处的oldDist需要从拖动模式传入
		
		float newDist = distanceMulti(event);
		float scale = newDist / oldDist;
		sMatrix.reset();
		newWidth = scale * oldWidth;
		newHeight = scale * oldHeight;
//			Log.i(TAG, "new width:" + newWidth + " height:" + newHeight + " old width:" + oldWidth + " height:" + oldHeight);
//			if(newWidth > Start.SCREEN_WIDTH * 2) {
//				newWidth = Start.SCREEN_WIDTH * 2;
//				newHeight = Start.SCREEN_HEIGHT * 2;
//				scale = 2.0f;
//			}
//			if(newWidth < 600) {
//				scaleMode = SMALLER;
//				newWidth = 600;
//				newHeight = 1024;
//				scale = 1.0f;
//			}
//			sMatrix.postTranslate(-sOffsetX1, -sOffsetY1);
		sMatrix.postScale(scale, scale, midPointF.x, midPointF.y);
		Log.i(TAG, "scale:" + scale + " sMatrix:" + sMatrix.toString());
		calcOffset();
		view.invalidate();
	}

	@Override
	public void touch_up(MotionEvent event) {
		// TODO Auto-generated method stub
		Log.i(TAG, "touch up");
		//抬手时需要把此时的图像宽度传给freeDragMode
		
		oldWidth = newWidth;
		oldHeight = newHeight;
		
		float scale = 0;
		Log.i(TAG, "new width111:" + newWidth + " new height:" + newHeight + " offsetx:" + sOffsetX + " offsety:" + sOffsetY);
		
		if(oldWidth < 1600 || oldHeight < 2460) {
			scale = 1600/oldWidth;
			Log.i(TAG, "small than initial bitmap: scale:" + scale);
			newWidth = oldWidth = 1600;
			newHeight = oldHeight = 2460;
			sMatrix.postScale(scale, scale, midPointF.x, midPointF.y);
			sOffsetX = 0;
			sOffsetY = 0;
			scaleMode = SMALLER;
		}
//		if(oldWidth < 600 || oldHeight < 1024) {
//			scale = 600/oldWidth;
//			Log.i(TAG, "small than initial bitmap: scale:" + scale);
//			newWidth = oldWidth = 600;
//			newHeight = oldHeight = 1024;
//			sMatrix.postScale(scale, scale, midPointF.x, midPointF.y);
//			sOffsetX = 0;
//			sOffsetY = 0;
//			scaleMode = SMALLER;
//		}
		if(oldWidth > Calligraph.mScaleBitmap.getWidth() || oldHeight > Calligraph.mScaleBitmap.getHeight()) {
			scale = Calligraph.mScaleBitmap.getWidth() / oldWidth;
			Log.i(TAG, "bigger than twice bitmap: scale:" + scale);
			newWidth = oldWidth = Start.SCREEN_WIDTH * 2;
			newHeight = oldHeight = Start.SCREEN_HEIGHT * 2;
			sMatrix.postScale(scale, scale, midPointF.x, midPointF.y);
			calcOffset();
			scaleMode = BIGGER;
		}
		
//		if(scaleMode != NORMAL) {
		//将缩放后抬手之后的当前偏移和尺寸传给FreeDragMode
		//以便再次双手按下时需要知道当前偏移
			FreeDragMode fdm = (FreeDragMode) view.getFreeDragMode();
			fdm.setsOffsetX(sOffsetX);
			fdm.setsOffsetY(sOffsetY);
			fdm.setCurHeight(newHeight);
			fdm.setCurWidth(newWidth);
//		}
			
		Log.i(TAG, "new width222:" + newWidth + " new height:" + newHeight + " offsetx:" + sOffsetX + " offsety:" + sOffsetY);
	
		syncScaleToMain();
		/*
//		savedMatrix.set(sMatrix);
    	clearMode();
    	*/
    	view.setTouchMode(view.getHandWriteMode());
    	HandWriteMode hwm = (HandWriteMode) view.getHandWriteMode();
    	hwm.setsMatrix(sMatrix);
    	//用来保存当切换到光标态时，此时的缩放矩阵大小
    	view.freeSavedMatrix.set(sMatrix);
    	Log.i(TAG, "sMatrix:" + sMatrix.toString());
	}

	@Override
	public void draw(Canvas canvas) {
		// TODO Auto-generated method stub
		view.baseImpl.draw(canvas, sMatrix);
	}
	
	public void setMidPointF(PointF midPointF) {
		this.midPointF = midPointF;
	}

	public void setOldDist(float oldDist) {
		this.oldDist = oldDist;
	}

	private void calcOffset() {
		//RectF fscreen = new RectF(0,0, 600, 1024);
		RectF fscreen = new RectF(0,0, 1600, 2460);
		RectF ft = new RectF(0,0,0,0);
		sMatrix.mapRect(ft, fscreen);
		sOffsetX = -ft.left;
		sOffsetY = -ft.top;
//		Log.e(TAG, ft.toString());
		Log.i(TAG, "calcOffset:" + sOffsetX + "-" + sOffsetY);
		if(sOffsetX < 0 )
			sOffsetX =0;
		if(sOffsetY <0)
			sOffsetY =0;
//		if(sOffsetX + 600 > newWidth){
//			sOffsetX = newWidth - 600;
//		}
		if(sOffsetX + 1600 > newWidth){
			sOffsetX = newWidth - 1600;
		}
//		if(sOffsetY + 1024 > newHeight){
//			sOffsetY = newHeight - 1024;
//		}
		if(sOffsetY + 2460 > newHeight){
			sOffsetY = newHeight - 2460;
		}
	}
	
	int abc = 0;
	int tmpFlag = 0;
	public void syncScaleToMain() {
		
		Log.i(TAG, "sync from scale to main");
    	Log.i(TAG, "sOffsetX:" + sOffsetX + " sOffsetY:" + sOffsetY);
    	Log.i(TAG, "new Width:" + newWidth + " Height:" + newHeight);
    	
    	//抬手后，要把放大的图重新画在mScaleBitmap上，经试验，自身不能通过matrix放大
    	//画在自身上，所以借用mScreenLayerBitmap来保存一下，然后再画在mScaleBitmap上
    	//新增  所有对mScaleBitmap的操作也要对mScaleTransparentBitmap操作一次
		sCanvas.setBitmap(Calligraph.mScreenLayerBitmap);
		//sCanvas.drawBitmap(view.mBitmap, new Rect(0,0,600,1024), new Rect(0,0,600,1024), new Paint());

		sCanvas.drawBitmap(view.mBitmap, new Rect(0,0,1600,2460), new Rect(0,0,1600,2460), new Paint());
		
		sMatrix.mapRect(target, new RectF(0,0,1,1));
		Log.i(TAG, " translated rect  "+target +"    "+ target.width()+"    " + target.height());
		
//		sCanvas.setBitmap(mBitmap);
//    	this.draw(sCanvas);
		
		sCanvas.setBitmap(Calligraph.mScaleBitmap);
		Matrix tempMatrix = new Matrix();
		tempMatrix.postScale(target.width(), target.height());

		Paint paint = new Paint();
		
		//小于屏幕大小时，放为屏幕大小
		if(scaleMode == SMALLER) {
			Log.i(TAG, "copy --  small");
			Log.i(TAG, "offset:" + sOffsetX + " " + sOffsetY);
			sCanvas.setBitmap(Calligraph.mScreenLayerBitmap);
			sCanvas.drawBitmap(Calligraph.mScaleBitmap, tempMatrix, paint);
			sCanvas.setBitmap(Calligraph.mScaleBitmap);
			sCanvas.drawColor(Color.BLACK);
			sCanvas.drawBitmap(Calligraph.mScreenLayerBitmap, 0, 0, paint);
			
			//新增  也要更新mScaleTransparentBitmap
			Calligraph.mScreenLayerBitmap.eraseColor(Color.TRANSPARENT);
			sCanvas.setBitmap(Calligraph.mScreenLayerBitmap);
			sCanvas.drawBitmap(Calligraph.mScaleTransparentBitmap, tempMatrix, paint);
			sCanvas.setBitmap(Calligraph.mScaleTransparentBitmap);
			Calligraph.mScaleTransparentBitmap.eraseColor(Color.TRANSPARENT);
			sCanvas.drawBitmap(Calligraph.mScreenLayerBitmap, 0, 0, paint);
			
			sMatrix.reset();
		} else {
			Log.i(TAG, "copy --  normal");
			Log.i(TAG, "offset:" + sOffsetX + " " + sOffsetY + " " + tempMatrix.toString());
			
			sCanvas.setBitmap(Calligraph.mScreenLayerBitmap);
			sCanvas.drawColor(Color.BLACK);
			tempMatrix.reset();
			
			//ly
//			tempMatrix.postScale(target.width() * 600 / newWidth, target.height() * 1024 / newHeight);
			tempMatrix.postScale(target.width() * 1600 / newWidth, target.height() * 2560 / newHeight);
			
			
			sCanvas.drawBitmap(Calligraph.mScaleBitmap, tempMatrix, new Paint());
						
			sCanvas.setBitmap(Calligraph.mScaleBitmap);
			tempMatrix.reset();
			
			//ly
//			tempMatrix.postScale(newWidth / 600, newHeight / 1024);
			tempMatrix.postScale(newWidth / 1600, newHeight / 2560);
			
			sCanvas.drawBitmap(Calligraph.mScreenLayerBitmap, tempMatrix, new Paint());
			
			//新增  也要更新mScaleTransparentBitmap
			sCanvas.setBitmap(Calligraph.mScreenLayerBitmap);
			Calligraph.mScreenLayerBitmap.eraseColor(Color.TRANSPARENT);
			tempMatrix.reset();
			
			//ly
//			tempMatrix.postScale(target.width() * 600 / newWidth, target.height() * 1024 / newHeight);
			tempMatrix.postScale(target.width() * 1600 / newWidth, target.height() * 2560 / newHeight);
			
			
			sCanvas.drawBitmap(Calligraph.mScaleTransparentBitmap, tempMatrix, new Paint());
						
			sCanvas.setBitmap(Calligraph.mScaleTransparentBitmap);
			Calligraph.mScaleTransparentBitmap.eraseColor(Color.TRANSPARENT);
			tempMatrix.reset();
			
			//ly
//			tempMatrix.postScale(newWidth / 600, newHeight / 1024);
			tempMatrix.postScale(newWidth / 1600, newHeight / 2560);
			
			sCanvas.drawBitmap(Calligraph.mScreenLayerBitmap, tempMatrix, new Paint());
		}

//		view.saveFile(Calligraph.mScaleBitmap, "mScaleBitmap_before.jpg");
		//Calligraph.mScaleBitmap.recycle();
		//Calligraph.mScaleBitmap = Bitmap.createBitmap(Calligraph.mScreenLayerBitmap, 0,0, 600, 1024, sMatrix, true);
				
//		sCanvas.setBitmap(view.mBitmap);
//		sCanvas.drawBitmap(Calligraph.mScaleBitmap, new Rect((int)sOffsetX, (int)sOffsetY, 
//				(int)(600 + sOffsetX), (int)(1024 + sOffsetY)), new Rect(0, 0, 600, 1024), new Paint());
		/*
		Rect fromRect = new Rect();
		if(EditableCalligraphy.flip_dst > 0 && EditableCalligraphy.flip_dst <= 1024) {
			sCanvas.setBitmap(view.mBitmap);
			fromRect.set(0, 0, 600, 1024 - EditableCalligraphy.flip_dst);
			fromRect.offset((int)sOffsetX, (int)sOffsetY);
			sCanvas.drawBitmap(Calligraph.mScaleBitmap, fromRect
					, new RectF(0, EditableCalligraphy.flip_dst, 600, 1024), new Paint());
			sCanvas.setBitmap(BaseBitmap.addBitmapList.get(0));
			fromRect.set(0, 1024 - EditableCalligraphy.flip_dst, 600, 1024);
			fromRect.offset((int)sOffsetX, (int)sOffsetY);
			sCanvas.drawBitmap(Calligraph.mScaleBitmap, fromRect,
					new RectF(0, 0, 600, EditableCalligraphy.flip_dst), new Paint());
		} else if(EditableCalligraphy.flip_dst > 1024) {
			sCanvas.setBitmap(BaseBitmap.addBitmapList.get(0));
			fromRect.set(0, 0, 600, 1024 - (EditableCalligraphy.flip_dst - 1024));
			fromRect.offset((int)sOffsetX, (int)sOffsetY);
			sCanvas.drawBitmap(Calligraph.mScaleBitmap, fromRect,
					new RectF(0, EditableCalligraphy.flip_dst - 1024, 600, 1024), new Paint());
			sCanvas.setBitmap(BaseBitmap.addBitmapList.get(1));
			fromRect.set(0, 1024 - (EditableCalligraphy.flip_dst - 1024), 600, 1024);
			fromRect.offset((int)sOffsetX, (int)sOffsetY);
			sCanvas.drawBitmap(Calligraph.mScaleBitmap, fromRect,
					new RectF(0, 0, 600, EditableCalligraphy.flip_dst - 1024), new Paint());
		} else {
			sCanvas.setBitmap(view.mBitmap);
			fromRect.set(0, 0, 600, 1024);
			fromRect.offset((int)sOffsetX, (int)sOffsetY);
	    	sCanvas.drawBitmap(Calligraph.mScaleBitmap, fromRect, new RectF(0, 0, 600, 1024), new Paint());
	    	Log.i(TAG, fromRect.toString());
		}
		*/
		
		int posY = (EditableCalligraphy.flip_dst > BaseBitmap.TITLE_HEIGHT )? BaseBitmap.TITLE_HEIGHT :
			EditableCalligraphy.flip_dst;

		sCanvas.setBitmap(view.mBitmap);
		
		//ly
//		sCanvas.drawBitmap(Calligraph.mScaleBitmap, new Rect((int)sOffsetX, (int)sOffsetY, 
//				(int)(600 + sOffsetX), (int)(1024 + sOffsetY)), new Rect(0, posY, 600, 1024 + posY), new Paint());
		sCanvas.drawBitmap(Calligraph.mScaleBitmap, new Rect((int)sOffsetX, (int)sOffsetY, 
				(int)(1600 + sOffsetX), (int)(2560 + sOffsetY)), new Rect(0, posY, 1600, 2560 + posY), new Paint());
		

//		view.saveFile(Calligraph.mScaleBitmap, "mScaleBitmap_after.jpg");
//		view.saveFile(view.mBitmap, "mBitmap.jpg");
//		view.saveFile(BaseBitmap.addBitmapList.get(0), "add_1.jpg");
		scaleMode = NORMAL;
	}
	
	private float distanceMulti(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return FloatMath.sqrt(x * x + y * y);
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
		sMatrix.reset();
		sOffsetX = 0;
		sOffsetY = 0;
		
		//ly
    	//oldWidth = 600;
    	//oldHeight = 1024;
    	oldWidth = 1600;
    	oldHeight = 2560;
    	
    	newWidth = oldWidth;
    	newHeight = oldHeight;
	}
}
