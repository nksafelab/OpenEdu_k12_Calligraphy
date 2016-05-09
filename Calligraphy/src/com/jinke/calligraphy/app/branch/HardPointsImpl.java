package com.jinke.calligraphy.app.branch;

import com.jinke.single.LogUtil;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.CountDownTimer;
import android.util.Log;

public class HardPointsImpl extends BasePointsImpl {
	
	
	private static final String TAG = "HardPointsImpl";
	
	private static final float MINP = 0.25f;
    private static final float MAXP = 0.75f;

    private Bitmap	mBitmap;
    private CurInfo mCurInfo;
    public Paint	paint ;
    private Paint   transparentPaint;
    
    private float 	mX, mY;
    private static final float TOUCH_TOLERANCE = 4;
    
    public Canvas  mCanvas;
    private Path    mPath;

	private float mLastPointX;
	private float mLastPointY;
	
//	private int tempCount = 0;

	public HardPointsImpl(BaseBitmap bb, MyView v) {
		super(bb, v);
		Log.i(TAG, "constructor" );
		
		mCurInfo = super.bCurInfo;
        paint = super.bPaint;
        
        transparentPaint = new Paint(super.bPaint);
        mCanvas = new Canvas();
        mCanvas.setBitmap(mCurInfo.mBitmap);
        
        mPath = new Path();
//        mBitmapPaint = new Paint(Paint.DITHER_FLAG);
	}

	@Override
	public void draw(Canvas canvas, Matrix matrix) {
		// TODO Auto-generated method stub
		super.draw(canvas, matrix);
		
		//涂鸦态时需要先往屏幕上画，再往透明层上画，每次从透明层截取小字
	    canvas.drawPath(mPath, paint);
	    //画在底图上(mBitmap或者透明层上)
	    //这个方式很2  by gongxl
	    
	    //ly
	    //我把这个地方删掉之后为啥还是正确的？？
	    
	    transparentPaint.set(paint);
	    reduceColor(transparentPaint);
	    transparentPaint.setStrokeWidth(paint.getStrokeWidth() * 1.3f);
	    //end
	    
//         mCanvas.save();
//         mCanvas.translate(0, EditableCalligraphy.flip_dst);
//             mCanvas.drawPath(mPath, transparentPaint);
//             mCanvas.restore();

               //add by luhao 11.14
               //如果是涂鸦态，则也往透明层上画一份，方便光标涂鸦切换时的拷贝
//             if(MyView.drawStatus == MyView.STATUS_DRAW_FREE) { 
//                     bCanvas.save();
//                     bCanvas.translate(0, EditableCalligraphy.flip_dst);
	    
	    			//ly
                       bCanvas.drawPath(mPath, transparentPaint);
	    			//end
	    
//                     bCanvas.restore();
//             }
//             mPath.reset();

	    
        
	}


	@Override
	public void start(float x, float y) {
		// TODO Auto-generated method stub
		super.start(x, y);
		tempCount = 0;
		if(MyView.drawStatus == MyView.STATUS_DRAW_FREE)
			mCanvas.setBitmap(mCurInfo.mBitmap);
		else {
			mCanvas.setBitmap(bDrawBitmap.getTopBitmap());
		}
		
//        mPath.reset();
		
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
        mLastPointX = x;
        mLastPointY = y;

		bView.invalidate();
		
//		tempCount ++;
//		Log.e("invalidate", "touch down start , invalidate! tempCount = " + tempCount);
		
//		bView.draw();
	}


	@Override
	public void clear() {
		// TODO Auto-generated method stub
		super.clear();
		mPath.reset();
	}

	
	@Override
	public void updatePaintSize() {
		// TODO Auto-generated method stub
		super.updatePaintSize();
		if(MyView.drawStatus == MyView.STATUS_DRAW_FREE) {
        //    paint.setStrokeWidth(4);
            paint.setStrokeWidth(3);
		} else {
            paint.setStrokeWidth(7);
		}
	}

	@Override
	public boolean makeNextPoint(float x, float y) {
		// TODO Auto-generated method stub
		if(!super.makeNextPoint(x, y))
			return false;
		
		mPath.moveTo(mLastPointX, mLastPointY);

        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
        	mLastPointX=(x + mX)/2;
            mLastPointY=(y + mY)/2;
        	mPath.quadTo(mX, mY, mLastPointX, mLastPointY);
            //mPath.lineTo(mLastPointX, mLastPointY);
            
            mX = x;
            mY = y;
        }

		
        //ly
        bView.invalidate();
        
        //四核优化
        //ly
        //不知道是干嘛的，直接注释掉
        if(tempCount % 2 == 0){
    		bView.invalidate();
        }
        tempCount ++;
        if(tempCount > Integer.MAX_VALUE)
			tempCount = 0;
        
        
		return true;
	}
	int tempCount = 0;
	@Override
	public boolean after() {
		// TODO Auto-generated method stub
		if(!super.after())
			return false;
		
		mPath.lineTo(mX, mY);
        // commit the path to our offscreen
//        mCanvas.drawPath(mPath, paint);
        // kill this so we don't double draw
//        mPath.reset();
        bView.invalidate();
//        tempCount = 0;
//		Log.e("invalidate", "touch up,reset tempCount = " + tempCount + " invalidate!");
        return true;
//        bView.draw();
	}
	
	@Override
	public void finish() {
		// TODO Auto-generated method stub
		super.finish();
		/*
		bDrawBitmap.finish();
		*/
	}

}
