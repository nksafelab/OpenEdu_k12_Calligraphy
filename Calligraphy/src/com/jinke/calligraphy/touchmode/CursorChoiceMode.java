package com.jinke.calligraphy.touchmode;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;

import com.jinke.calligraphy.app.branch.MyView;
import com.jinke.calligraphy.app.branch.Start;
import com.jinke.calligraphy.template.WolfTemplateUtil;
import com.jinke.single.BitmapCount;

public class CursorChoiceMode implements TouchMode{

	private static final String TAG = "CursorChoiceMode";
	
	MyView view;
	
	private Paint mPaint;
	private Canvas flip_canvas;
	private float start_y;
    private Bitmap addbgBitmap;//保存添加页的bitmap
    private Bitmap temp;//保存加页之前的bitmap，用于显示
    
	public CursorChoiceMode(MyView view) {
		this.view = view;
		
		mPaint = new Paint();
		mPaint.setStyle(Paint.Style.FILL);
		mPaint.setColor(Color.WHITE);
		
		mPaint.setColor(Color.RED);
		mPaint.setStrokeWidth(3);
		mPaint.setTextSize(25);
		
    	flip_canvas = new Canvas();
	}

	@Override
	public void draw(Canvas canvas) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void touch_action_down(MotionEvent event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void touch_action_pointer_down(MotionEvent event) {
		// TODO Auto-generated method stub
		touch_action_pointer_down_flip(event);
		start_y = event.getY(1);
		if(addbgBitmap == null)
			addbgBitmap = BitmapFactory.decodeFile(WolfTemplateUtil.TEMPLATE_PATH+WolfTemplateUtil.getCurrentTemplate().getName()+"/notebook_add_bg.png");
			BitmapCount.getInstance().createBitmap("CursorChoiceMode decode addbgBitmap");
//		if(temp == null)
//			temp = cursorBitmap.mBitmap;
		flip_canvas.setBitmap(view.mBitmap); 
		//ly
		//flip_canvas.drawBitmap(view.cursorBitmap.mBitmap, new Rect(600, 0, Start.SCREEN_WIDTH * 2, 1024), new Rect(0, 0, 600, 1024), mPaint);
		flip_canvas.drawBitmap(view.cursorBitmap.mBitmap, new Rect(1600, 0, Start.SCREEN_WIDTH * 2, 2560), new Rect(0, 0, 1600, 2560), mPaint);
		
		view.invalidate();
		Log.e(TAG, "copy!!!!!!!!!!!!!!!!!!!");
	}

	@Override
	public void touch_action_pointer_up(MotionEvent event) {
		// TODO Auto-generated method stub
		touch_action_pointer_up_flip(event);
	}

	@Override
	public void touch_move(MotionEvent event) {
		Log.i("0801", "CursorChoiceMode touch_mode");
		// TODO Auto-generated method stub
		if(event.getPointerCount() == 2){
    		Log.i(TAG, "muti_touch move");
    		Log.v(TAG, "point"+0+" move:"+event.getY(1));
    		for(int i=0;i<event.getPointerCount();i++){
//    			Log.v(TAG, "point"+i+" x:"+event.getX(i));
//    			Log.v(TAG, "point"+i+" y:"+event.getY(i));
    		}
    		touch_action_addPaper((int)event.getY(1));
//    		Log.i(TAG, "muti_touch moved:"+(event.getY(1) - start_y));
		}
	}

	@Override
	public void touch_up(MotionEvent event) {
		// TODO Auto-generated method stub
		
	}
	
	private void touch_action_addPaper(int f) {
		Log.i(TAG, "action pointer add paper"+ f);
		
		Log.e(TAG, "copy draw !!!!!!!!!!!!!!!!!!!");
		
		//ly
//		flip_canvas.drawBitmap(view.mBitmap, new Rect(0, 0, 600, Math.abs(f)), new Rect(600, 0, Start.SCREEN_WIDTH * 2, Math.abs(f)), mPaint);
//		flip_canvas.drawBitmap(addbgBitmap, new Rect(0, 0, 600, 1024 - Math.abs(f)), new Rect(600, f, Start.SCREEN_WIDTH * 2, 1024), mPaint);
		
		flip_canvas.drawBitmap(view.mBitmap, new Rect(0, 0, 1600, Math.abs(f)), new Rect(1600, 0, Start.SCREEN_WIDTH * 2, Math.abs(f)), mPaint);
		flip_canvas.drawBitmap(addbgBitmap, new Rect(0, 0, 1600, 2560 - Math.abs(f)), new Rect(1600, f, Start.SCREEN_WIDTH * 2, 2560), mPaint);
		
//		flip_canvas.drawBitmap(temp, new Rect(600, 0, Start.SCREEN_WIDTH * 2, f), new Rect(600, 0, Start.SCREEN_WIDTH * 2, f), mPaint);
		
		//ly
//		if(f >600)
		if(f > 1600)
			flip_canvas.drawText("取消添加", 700, 100, mPaint);
		else
			flip_canvas.drawText("添加新页", 700, 100, mPaint);
		view.invalidate();
//		syncScaleToMain();
    }
	
	//光标态，两点向上拖动 
    private void touch_action_pointer_up_flip(MotionEvent event) {
		Log.i(TAG, "action pointer up flip:"+event.getY()); 
		int temp_y = (int)event.getY();
		
		//ly
//		if(event.getY() > 600){
		if(event.getY() > 1600){
			
			flip_canvas.drawBitmap(view.mBitmap, new Rect(0, 0, 1600, 2560), new Rect(1600, 0, Start.SCREEN_WIDTH * 2, 2560), mPaint);
		
		}else{
			
			flip_canvas.drawColor(Color.WHITE);
			
			//ly
//			flip_canvas.drawBitmap(addbgBitmap, 600, 0, view.baseBitmap.paint);
			flip_canvas.drawBitmap(addbgBitmap, 1600, 0, view.baseBitmap.paint);
			
			
//			Canvas c = new Canvas();
//			c.setBitmap(cursorBitmap.bitmap); 
//			c.drawBitmap(addbgBitmap, 600, 0, mPaint);
			view.cursorBitmap.bitmap.recycle();
			BitmapCount.getInstance().recycleBitmap("CursorChoiceMode touch_action_pointer_up_flip");	
			
			view.cursorBitmap.bitmap = addbgBitmap.copy(Bitmap.Config.ARGB_8888, true);
			for(int i=0;i<view.cursorBitmap.listEditableCalligraphy.size();i++){
				view.cursorBitmap.listEditableCalligraphy.get(i).clear();
			}
			
		}
    }
    
    //光标态，两点向上拖动
    private void touch_action_pointer_down_flip(MotionEvent event) {
		Log.i(TAG, "action pointer down flip");
		//计算两点间距离
//		oldDist = distanceMulti(event);
//		if (oldDist > 10f) {
//			savedMatrix.set(sMatrix);
//			midPoint(midPointF, event);
//			MODE_SCALE = true;
//			Calligraph.mDragEnableBtn.setText("缩放模式");
//			syncMainToScale();
//		}
    }

	@Override
	public void printInfo() {
		// TODO Auto-generated method stub
		Log.i("touchmode", "This is " + TAG);
		
	}

	@Override
	public Matrix getMatrix() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}

}
