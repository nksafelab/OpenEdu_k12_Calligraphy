package com.jinke.calligraphy.touchmode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Handler;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

import com.jinke.calligraphy.app.branch.BaseBitmap;
import com.jinke.calligraphy.app.branch.Calligraph;
import com.jinke.calligraphy.app.branch.CursorDrawBitmap;
import com.jinke.calligraphy.app.branch.EditableCalligraphy;
import com.jinke.calligraphy.app.branch.EditableCalligraphyItem;
import com.jinke.calligraphy.app.branch.MyView;
import com.jinke.calligraphy.app.branch.Start;
import com.jinke.calligraphy.app.branch.WorkQueue;
import com.jinke.calligraphy.app.branch.EditableCalligraphyItem.Types;
import com.jinke.calligraphy.database.CalligraphyDB;
import com.jinke.single.BitmapCount;

public class CursorScaleMode implements TouchMode{
	
	private static final String TAG = "CursorScaleMode";
	MyView view;
	private float start_distance;
	public 	static Matrix mmMatrix;
	private Matrix savedMatrix;
	private float mScale = 1;
    private float[] mini = new float[9];
    private boolean isPicScale = false;
    private EditableCalligraphyItem picItem = null;
    int scaleCount = 0;
    
    private static final float PRESSURE_THRESHOLD = 0.67f;
    private float mCurrPressure = 0;
    private float mPrevPressure = 0;
    
    Handler mHandler = new Handler(){
    	public void handleMessage(android.os.Message msg) {
    		do_touch_up();
    	};
    };
//    private Timer timer = null;
    private boolean totallyUP = true;
//    private mTask task = null;
//	class mTask extends TimerTask{
//
//		@Override
//		public void run() {
//			// TODO Auto-generated method stub
//			totallyUP = true;
//			mHandler.sendEmptyMessage(0);
//			timer.cancel();
//			Log.e("touchupdelay", "timer running end");
//		}
//		
//	}
    
    
    private Matrix touchDownMatrix = null;
	
	public CursorScaleMode(MyView view) {
		this.view = view;
    	mmMatrix = new Matrix();
    	
    	//ly
    	//mmMatrix.postTranslate(-600, 0);
    	mmMatrix.postTranslate(-1600, 0);
    	
    	view.setMMMatirx(mmMatrix);
    	savedMatrix = new Matrix();
    	touchDownMatrix = new Matrix();
    	
	}

	@Override
	public void touch_action_down(MotionEvent event) {
		// TODO Auto-generated method stub
		Log.e("touchupdelay", " down");
	}

	@Override
	public void touch_action_pointer_down(MotionEvent event) {
		
//		if(!totallyUP){
//			if(timer != null){
//				timer.cancel();
//			}
//			if(task != null){
//				task.cancel();
//				Log.e("touchupdelay", "task cancel");
//			}
//		}
		if(totallyUP){
			
			mCurrPressure = event.getPressure(0) + event.getPressure(1);
			if (mCurrPressure / mPrevPressure > PRESSURE_THRESHOLD) {
				savedMatrix.set(mmMatrix);
				start_distance = distanceMulti(event);
				isPicture(event);
				if(isPicScale){
					mmMatrix.set(picItem.getMatrix());
					savedMatrix.set(mmMatrix);
					touchDownMatrix.set(picItem.getMatrix());
					Log.e("ispic", "set touchDown matrix:" + picItem.getMatrix().toShortString());
				}	 
			}
			mPrevPressure = mCurrPressure;
		}
		
	}

	@Override
	public void touch_action_pointer_up(MotionEvent event) {
		
		//ly
		//刷屏
		view.mBitmap.eraseColor(Color.WHITE);
		//end
		
		if(isPicScale){
//			timer = new Timer();
//			task = new mTask();
//			timer.schedule(task, 500);
//			totallyUP = false;
			Log.e("touchupdelay", "task start");
		}else
			cursorScaleUP();
	}

	

	@Override
	public void touch_move(MotionEvent event) {
		Log.i("0801", "cursorScaleMode touch_mode");
		// TODO Auto-generated method stub
		if(event.getPointerCount() == 2){
				addORScale(event);
		}
	}

	@Override
	public void touch_up(MotionEvent event) {
		// TODO Auto-generated method stub
		Log.e("touchupdelay", "touch up");
		do_touch_up();
	}
	private void do_touch_up(){
		Start.status.modified("scaleImage");
		float[] values = new float[9];
		mmMatrix.getValues(values);
		float scale = values[0];
		
		if(isPicScale){
			
			CalligraphyDB.getInstance(Start.context).
				updatePictrueItem(Start.getPageNum(), 3, picItem.getItemID(), picItem);
			
		}else{
			for(int i=0;i<view.cursorBitmap.listEditableCalligraphy.size();i++){
				EditableCalligraphy e = view.cursorBitmap.listEditableCalligraphy.get(i);
				e.scaleResetCharList(scale);
			}
		}
		
		view.setTouchMode(view.getHandWriteMode());
		view.cursorBitmap.setJump();
		HandWriteMode hwd = (HandWriteMode) view.getHandWriteMode();
		if(!isPicScale)
			hwd.setsMatrix(mmMatrix);
		view.cursorBitmap.updateHandwriteState();
		Start.c.flipHandler.sendEmptyMessage(0);//缩放抬手，释放屏幕外资源
		WorkQueue.getInstance().endFlipping();
	}


	@Override
	public void draw(Canvas canvas) {
		// TODO Auto-generated method stub
		view.baseImpl.draw(canvas, mmMatrix);
		if(isPicScale){
			Matrix nm = new Matrix(picItem.getMatrix());
			float touchdownf[] = new float[9];
			picItem.getMatrix().getValues(touchdownf);
			float s = touchdownf[0];
			nm.setScale(s, s);
			int t = 0;
				nm.postTranslate(view.cursorBitmap.listEditableCalligraphy.get(3).getAvailable().getStartX(),
						picItem.getCurPosY() - EditableCalligraphy.flip_dst);
			
			canvas.drawBitmap(picItem.getCharBitmap(),
					nm, new Paint());
		}
	}

	@Override
	public void printInfo() {
		// TODO Auto-generated method stub
		Log.i("touchmode", "This is " + TAG);
	}
	
	private float distanceMulti(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return FloatMath.sqrt(x * x + y * y);
	}
	private void addORScale(MotionEvent event) {
		scaleCount++;
		if(scaleCount % 2 == 0){
	    	float distance = distanceMulti(event);
	    	if(distance > start_distance)
	    		distance = start_distance + (distance - start_distance)/3;
	    	else if(distance < start_distance)
	    		distance = start_distance - (start_distance - distance)/3;
	    	
	    	mScale = (distance/start_distance);
	    	float[] f = new float[9];
	    	mmMatrix.set(savedMatrix);
	    	//f[0]缩放之前的比例
	    	savedMatrix.getValues(f);
	    	
	    	if(!isPicScale && f[0] * mScale > 2){
	    		Log.e("matrix", "to large mmMatrix:" + mmMatrix.toShortString());
	    		return;
	    	}
	    	
	    	if(isPicScale){
	    		float pref[] = new float[9];
	    		picItem.getMatrix().getValues(pref);
	    		if(pref[0] * mScale > 3){
	    			return;
	    		}
	    		if(pref[0] * mScale < 0.5f){
	    			Log.e("ispic", "缩放过小 mScale:" + mScale);
	    			return;
	    		}
	        	mmMatrix.postScale(mScale,mScale);
	    		picItem.setMatrix(mmMatrix);
	    		
	    		Log.e("ispic", "item id:" + picItem.getItemID() + " scaleing matrix:" + picItem.getMatrix().toShortString());
	    		
	    	}else{
	    		mmMatrix.postScale(mScale,mScale);
	    	}
	    	
	    	if(!isPicScale){
		    	Canvas c = new Canvas();
		    	
		    	//ly
		    	//用于清屏的代码
		    	view.mBitmap.eraseColor(Color.WHITE);
		    	//end
		    	
		    	c.setBitmap(view.mBitmap);
//		    	c.drawBitmap(view.cursorBitmap.bitmap, new Rect(0, 0, 600, 1024), new Rect(600,0,Start.SCREEN_WIDTH * 2,1024), new Paint());
		    	c.drawBitmap(view.cursorBitmap.bitmap, new Rect(0, 0, 1600, 2460), new Rect(1600,0,Start.SCREEN_WIDTH * 2,2400), new Paint());
		    	
		    	
		    	for(int i=0;i<view.cursorBitmap.listEditableCalligraphy.size();i++){
		    			view.cursorBitmap.listEditableCalligraphy.get(i).scaleUpdate(view.mBitmap, mmMatrix);
		    	}
		    	
	    		Log.e("ispic", "----view setMatrix" + mmMatrix.toShortString() + " isPicScale:"  + isPicScale);
		    	view.setMMMatirx(mmMatrix);
	    	}
	    	view.invalidate();
		}
		if(scaleCount == Integer.MAX_VALUE)
			scaleCount = 0;
    	
    }


    public void cursorScaleUP(){
    	mmMatrix.getValues(mini);
    	if(mini[0] < 0.75f){
    		mmMatrix.setScale(0.75f, 0.75f);
    		//ly
//    		mmMatrix.postTranslate(-600, 0);
    		mmMatrix.postTranslate(-1600, 0);
    		
    		view.setMMMatirx(mmMatrix);
    		view.invalidate();
    	}
    	
    }

	@Override
	public Matrix getMatrix() {
		// TODO Auto-generated method stub
		return mmMatrix;
	}
	
	private void isPicture(MotionEvent event) {
		// TODO Auto-generated method stub
		
		Log.e("ispic", "event.getX(0):" + event.getX(0) + " event.getY(0):" + event.getY(0));
		Log.e("ispic", "event.getX(1):" + event.getX(1) + " event.getY(1):" + event.getY(1));
		EditableCalligraphy currentCalligraphy = view.cursorBitmap.listEditableCalligraphy.get(3);
		
		picItem = currentCalligraphy.isPic(event.getX(0), event.getY(0));
		if(picItem == null){
			isPicScale = false;
			Log.e("touchupdelay", "not picture scale");
			return;
		}
		picItem = currentCalligraphy.isPic(event.getX(1), event.getY(1));
		if(picItem == null){
			isPicScale = false;
			Log.e("touchupdelay", "not picture scale");
			return;
		}
		
		Log.e("ispic", "isPicItem:" + picItem.getItemID() + " matrix:" + picItem.getMatrix().toShortString());
		isPicScale = true;
	}
	
	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}
	
	
	
}
