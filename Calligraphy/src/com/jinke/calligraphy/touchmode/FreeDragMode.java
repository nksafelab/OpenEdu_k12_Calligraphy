package com.jinke.calligraphy.touchmode;

import android.graphics.Bitmap;
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
import android.view.View;

import com.jinke.calligraphy.app.branch.BaseBitmap;
import com.jinke.calligraphy.app.branch.Calligraph;
import com.jinke.calligraphy.app.branch.EditableCalligraphy;
import com.jinke.calligraphy.app.branch.MyView;

public class FreeDragMode implements TouchMode{
	
	public final String TAG = "FreeDragMode";
	
	private MyView 	view;
	private float 	sOffsetX1,sOffsetY1;
	private Bitmap 	mBitmap;
	private float 	curWidth, curHeight;
	private Matrix 	sMatrix;
	private PointF 	dPointF;
	private PointF 	midPointF;
	private float  	oldDist;
	private Canvas 	sCanvas;
	private float  	sOffsetX = 0.0f;
	private float  	sOffsetY = 0.0f;
	
	public FreeDragMode(MyView view) {
		this.view = view;
		sOffsetX1 = 0;
		sOffsetY1 = 0;
		
		//ly
//		curWidth = 600;
//		curHeight = 1024;
		curWidth = 1600;
		curHeight = 2560;

		
		mBitmap = this.view.mBitmap;
		
		
    	sMatrix = new Matrix();
    	dPointF = new PointF();
    	midPointF = new PointF();
    	sCanvas = new Canvas();
	}

	@Override
	public void touch_action_down(MotionEvent event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void touch_action_pointer_down(MotionEvent event) {
		// TODO Auto-generated method stub
	    Log.i(TAG, "action pointer down");
	    //view.freeBitmap.resetFreeBitmapList();
	    Log.i("clear", "clear");
	   
	    
		//计算两点间距离
		oldDist = distanceMulti(event);
		Log.i(TAG, "old distance:" + oldDist);
		if (oldDist > 10f) {
//			savedMatrix.set(sMatrix);
			midPoint(midPointF, event);
			midPoint(dPointF, event);
			sOffsetX1 = sOffsetX;
			sOffsetY1 = sOffsetY;
			
//			sMatrix.reset();
//        	sMatrix.postTranslate(-sOffsetX1, -sOffsetY1);
			
			//当双指按下拖动时，将画在透明层上的内容再画在mScaleTransparentBitmap上
			syncChangeToTransparent();
			//将画在屏幕与透明层上的内容刷在底图上，否则拖动时画在屏幕上的内容还在残存着
			//此时，只要更新涂鸦态bitmap就可以，不需要更新光标态底图的bitmap，以及mBitmap的另一半
			//因为，在切换时会对transparentBitmap重新赋值，并进行统一更新
			//view.cursorBitmap.updateTransparent();
			view.freeBitmap.updateTransparentInFreeDragMode();
        	syncMainToScale();
		}
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
		Log.i("0801", "freedragmode touch_mode");
		// TODO Auto-generated method stub
		float newDist = distanceMulti(event);
    	if(Math.abs(newDist - oldDist ) < 40.0f) {
    		
    		sMatrix.reset();
            
            midPoint(dPointF, event);
//            sMatrix.postTranslate(x - dPointF.x, y - dPointF.y);
        	
            sMatrix.postTranslate(-sOffsetX1, -sOffsetY1);
            
            sMatrix.postTranslate(dPointF.x - midPointF.x, dPointF.y - midPointF.y);
            view.invalidate();
        	calcOffset();
//    		Log.i(TAG, "----offsetx:" + sOffsetX + " offsety:" + sOffsetY);	
    	} else {
    		
    		//ly
    		//先把此处都注释了
    		
    		//此处要切换为缩放模式
//    		tmpFlag = 1;
//    		syncScaleToMain();
    		view.setTouchMode(view.getFreeScaleMode());
    		/*
    		 * 缩放模式需要根据oldDist和midPointF来计算缩放的比例
    		 * 将这些变量传过去
    		*/
    		FreeScaleMode fsm = (FreeScaleMode)view.getFreeScaleMode();
    		fsm.setMidPointF(midPointF);
    		fsm.setOldDist(oldDist);
//    		setToScaleMode();
    		
    		//end
    		
    	}
	}

	@Override
	public void touch_up(MotionEvent event) {
		// TODO Auto-generated method stub
		Log.i(TAG, "touch up");
		syncScaleToMain();
		/*
		savedMatrix.set(sMatrix);
		 */
		view.setTouchMode(view.getHandWriteMode());
    	HandWriteMode hwm = (HandWriteMode) view.getHandWriteMode();
    	hwm.setsMatrix(sMatrix);
    	
    	
		/*
    	clearMode();
    	*/
	}

	@Override
	public void draw(Canvas canvas) {
		// TODO Auto-generated method stub
		view.baseImpl.draw(canvas, sMatrix);
	}
    
	public void setCurWidth(float curWidth) {
		this.curWidth = curWidth;
	}

	public void setCurHeight(float curHeight) {
		this.curHeight = curHeight;
	}

	public void setsOffsetX(float sOffsetX) {
		this.sOffsetX = sOffsetX;
	}

	public void setsOffsetY(float sOffsetY) {
		this.sOffsetY = sOffsetY;
	}

	private float distanceMulti(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return FloatMath.sqrt(x * x + y * y);
	}

	private void midPoint(PointF point, MotionEvent event) {
		float x = event.getX(0) + event.getX(1);
		float y = event.getY(0) + event.getY(1);
		point.set(x / 2, y / 2);
	}
	
	private void calcOffset() {
//		Log.i(TAG, "--------  calcOffset ------");
		
		//ly
		//RectF fscreen = new RectF(0,0, 600, 1024);
		RectF fscreen = new RectF(0,0, 1600, 2560);
		
		
		RectF ft = new RectF(0,0,0,0);
		sMatrix.mapRect(ft, fscreen);
		sOffsetX = -ft.left;
		sOffsetY = -ft.top;
		if(sOffsetX < 0 )
			sOffsetX =0;
		if(sOffsetY <0)
			sOffsetY =0;
		
		//ly
//		if(sOffsetX + 600 > curWidth) {
//			sOffsetX = curWidth - 600;
//		}
//		if(sOffsetY + 1024 > curHeight) {
//			sOffsetY = curHeight - 1024;
//		}
		if(sOffsetX + 1600 > curWidth) {
			sOffsetX = curWidth - 1600;
		}
		if(sOffsetY + 2560 > curHeight) {
			sOffsetY = curHeight - 2560;
		}
		
		
		Log.i(TAG, "calcOffset:" + sOffsetX + "  " + sOffsetY);
		Log.i(TAG, "curWidth:" + curWidth + " curHeight:" + curHeight);
//		Log.i(TAG, "=========  calcOffset =========");
	}
	
	public void syncChangeToTransparent() {
		
		//ly
		sCanvas.setBitmap(Calligraph.mScaleTransparentBitmap);
		sCanvas.drawBitmap(view.cursorBitmap.getTopBitmap(), new Rect(0, 0, 1600, 2460), 
    			new RectF(sOffsetX, sOffsetY, 1600 + sOffsetX, 2560 + sOffsetY), new Paint());	
		
//		sCanvas.drawBitmap(view.cursorBitmap.getTopBitmap(), new Rect(0, 0, 1600, 2460), 
//    			new RectF(sOffsetX, sOffsetY, 1600 + sOffsetX, 2460 + sOffsetY), new Paint());
		//这玩意用来把涂鸦态的图画在背景图上
		
		
		//涂鸦态双手按下开始绘图
		
//		sCanvas.setBitmap(Calligraph.mScaleTransparentBitmap);
//		sCanvas.drawBitmap(view.cursorBitmap.getTopBitmap(), new Rect(0, 0, 600, 1024), 
//    			new RectF(sOffsetX, sOffsetY, 600 + sOffsetX, 1024 + sOffsetY), new Paint());
//		view.cursorBitmap.getTopBitmap().eraseColor(Color.TRANSPARENT);
	}
	
	//抬手后将大图根据偏移截取屏幕部分大小到底图上，以便开始涂鸦
	//此处的拷贝策略，根据BaseBitmap中的绘图策略相应调整
	public void syncScaleToMain() {
		
		Log.i(TAG, "sync from scale to main");
    	Log.i(TAG, "sOffsetX:" + sOffsetX + " sOffsetY:" + sOffsetY);
    	
//		sCanvas.setBitmap(Calligraph.mScreenLayerBitmap);
//		sCanvas.drawBitmap(mBitmap, new Rect(0,0,600,1024), new Rect(0,0,600,1024), new Paint());
    	calcOffset();
    	
//		sCanvas.setBitmap(this.mBitmap);
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
    	
    	sCanvas.setBitmap(this.mBitmap);
    	
    	//ly
//		sCanvas.drawBitmap(Calligraph.mScaleBitmap, new Rect((int)sOffsetX, (int)sOffsetY, 
//				(int)(600 + sOffsetX), (int)(1024 + sOffsetY)), new Rect(0, posY, 600, 1024 + posY), new Paint());	
		sCanvas.drawBitmap(Calligraph.mScaleBitmap, new Rect((int)sOffsetX, (int)sOffsetY, 
				(int)(1600 + sOffsetX), (int)(2560 + sOffsetY)), new Rect(0, posY, 1600, 2560 + posY), new Paint());
		
	}
	
	//将当前屏幕拷贝到mScaliBitmap上
	public void syncMainToScale() {
		Log.i(TAG, "sync from main to scale");
    	sCanvas.setBitmap(Calligraph.mScaleBitmap);
    	
//    	sCanvas.save();
//    	sCanvas.setMatrix(sMatrix);
//    	this.draw(sCanvas);
//    	sCanvas.translate(sOffsetX, sOffsetY);
//    	this.draw(sCanvas);
//    	sCanvas.restore();
    	
//    	view.draw(sCanvas);
    	Log.i(TAG, "sOffsetX:" + sOffsetX + " sOffsetY:" + sOffsetY);
    	int posY = (EditableCalligraphy.flip_dst > BaseBitmap.TITLE_HEIGHT )? BaseBitmap.TITLE_HEIGHT :
    					EditableCalligraphy.flip_dst;
    	
    	//ly
//    	sCanvas.drawBitmap(mBitmap, new Rect(0, posY, 600, 1024), 
//    			new RectF(sOffsetX, sOffsetY, 600 + sOffsetX, 1024 + sOffsetY - posY), new Paint());
    	sCanvas.drawBitmap(mBitmap, new Rect(0, posY, 1600, 2560), 
    			new RectF(sOffsetX, sOffsetY, 1600 + sOffsetX, 2560 + sOffsetY - posY), new Paint());
    	
    	
//		canvas.drawBitmap(bView.cursorBitmap.getTopBitmap(), 0, 0, paint);

    	/*
    	sCanvas.translate(sOffsetX, sOffsetY);
    	//根据当前所在的页的位置，拷贝不用的部分到mScaleBitmap上。
		if(EditableCalligraphy.flip_dst > 0 && EditableCalligraphy.flip_dst <= 1024) {
			sCanvas.drawBitmap(mBitmap, new Rect(0, EditableCalligraphy.flip_dst, 600, 1024), 
	    			new RectF(0, 0, 600, 1024 - EditableCalligraphy.flip_dst), 
	    			new Paint());
			sCanvas.drawBitmap(BaseBitmap.addBitmapList.get(0), new Rect(0, 0, 600, EditableCalligraphy.flip_dst),
					new RectF(0, 1024 - EditableCalligraphy.flip_dst, 600, 1024), new Paint());
		} else if(EditableCalligraphy.flip_dst > 1024) {
			sCanvas.drawBitmap(BaseBitmap.addBitmapList.get(0), new Rect(0, EditableCalligraphy.flip_dst - 1024, 600, 1024),
					new RectF(0, 0, 600, 1024 - (EditableCalligraphy.flip_dst - 1024)), new Paint());
			sCanvas.drawBitmap(BaseBitmap.addBitmapList.get(1), new Rect(0, 0, 600, EditableCalligraphy.flip_dst - 1024),
					new RectF(0, 1024 - (EditableCalligraphy.flip_dst - 1024), 600, 1024), new Paint());
		} else {
	    	sCanvas.drawBitmap(mBitmap, new Rect(0, 0, 600, 1024), new RectF(0, 0, 600, 1024), new Paint());
		}
    	sCanvas.translate(-sOffsetX, -sOffsetY);
    	*/
	}
	
//	public void syncBitmapToScale(Bitmap bitmap) {
//	}

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
		syncChangeToTransparent();
		sMatrix.reset();
		sOffsetX1 = 0;
		sOffsetY1 = 0;
		sOffsetX = 0;
		sOffsetY = 0;
		
		//ly
//		curWidth = 600;
//		curHeight = 1024;
		curWidth = 1600;
		curHeight = 2560;
	}
	
//	public void viewToBitmap() {
//		canvas.drawBitmap(bitmapView.getDrawingCache(), new Rect(0, bView.statusBarHeight, 600,
//				1024), new Rect(0, 0, 600, 1024 - bView.statusBarHeight), new Paint());
//	}


}
