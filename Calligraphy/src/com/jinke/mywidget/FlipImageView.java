package com.jinke.mywidget;

import com.jinke.calligraphy.app.branch.Calligraph;
import com.jinke.calligraphy.app.branch.CursorDrawBitmap;
import com.jinke.calligraphy.app.branch.R;
import com.jinke.calligraphy.app.branch.Start;
import com.jinke.calligraphy.app.branch.WorkQueue;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

public class FlipImageView extends ImageView{
	
	public static final int FLIP_WHAT = -1111;
	public static final int FLIP_UP_WHAT = -2222;
	public int mPreviousx;
	public int mPreviousy;
	public Handler handler;
	public boolean up = false;
	public static boolean flipping = false;
	public static final int TOP_LIMIT = 40;
	public static final int BUTTOM_LIMIT = 850;
	
	public FlipImageView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	public FlipImageView(Context context, Handler handler){
		super(context);
		this.handler = handler;
		
	}
	
	@Override
	protected boolean setFrame(int l, int t, int r, int b) {
		// TODO Auto-generated method stub
//		Log.e("FlipImageView", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa=============  check jump ");
		if(Start.c != null){
			if(Start.c.panel.isTouched()){
				Start.c.panel.setTouchEnd();
				return false;
			}
//			Log.e("FlipImageView", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa=============" + Start.c.panel.touch);
		}
		return super.setFrame(l, t, r, b);
	}
	
	
	
	
	public void initLayout(){
		layout(600-getWidth(),
        		TOP_LIMIT,
                600,
                TOP_LIMIT + getHeight());
		invalidate();
	}
	
	@Override
	public void requestLayout() {
		// TODO Auto-generated method stub
//		Log.e("FlipImageView", "request layout");
		if(!up){
			super.requestLayout();
//			Log.e("FlipImageView", "request layout----------");
		}
	}
	
	@Override
	 public boolean onTouchEvent(MotionEvent event) {
        final int iAction = event.getAction();
        final int iCurrentx = (int)event.getX();
        final int iCurrenty = (int)event.getY();
        
        final int iLeft = getLeft();
        final int iTop = getTop();
        int top = 0;
        top = iTop;
//        if(iTop >= -10){
//	        for(int i=0;i<CursorDrawBitmap.listEditableCalligraphy.size();i++){
//				CursorDrawBitmap.listEditableCalligraphy.get(i).setFlip_dst(iTop);
//			}
//	        top = iTop;
//        }
        
        switch(iAction)
        {
        case MotionEvent.ACTION_DOWN:
            mPreviousx = iCurrentx;
            mPreviousy = iCurrenty;
            
            setBackgroundResource(R.drawable.flipblock_down);
            flipping = true;
            break;
        case MotionEvent.ACTION_MOVE:
            int iDeltx = iCurrentx - mPreviousx;
            int iDelty = iCurrenty - mPreviousy;
            
//            Log.e("flipButton", "move iTop" + iTop);
            if(top + iDelty >= TOP_LIMIT-5 && top + iDelty + getHeight() < BUTTOM_LIMIT){
//            	Log.e("flipButton", "iTop" + iTop);
	            if(iDeltx != 0 || iDelty != 0)
	                layout(600-getWidth(),
	                		top + iDelty,
	                        600,
	                        top + iDelty + getHeight());
//	            Log.e("flipButton", "top+iDelty:" + top + iDelty + " +height:" + top + iDelty + getHeight());
	            
	 //834
	            mPreviousx = iCurrentx - iDeltx;
	            mPreviousy = iCurrenty - iDelty;
	            
	            
	            	top -= TOP_LIMIT;
	    	        for(int i=0;i<CursorDrawBitmap.listEditableCalligraphy.size();i++){
	    	        	if(top <= TOP_LIMIT+2)
	    	        		CursorDrawBitmap.listEditableCalligraphy.get(i).setFlip_dst(0);
	    	        	else
	    	        		CursorDrawBitmap.listEditableCalligraphy.get(i).setFlip_dst(iTop);
	    			}
	            
//	    	    Log.v("flipper", "                    FlipImageView handler has message:"
//	    	    		+ handler.hasMessages(FLIP_WHAT));
	    	    if(!handler.hasMessages(FLIP_WHAT)){
	    	    	handler.sendEmptyMessage(FLIP_WHAT);
	    	    }
            }else{
            	WorkQueue.getInstance().endFlipping();
            }
            
            break;
        case MotionEvent.ACTION_UP:
        	
        	WorkQueue.getInstance().endFlipping();
        	flipping = false;
        	handler.sendEmptyMessage(iTop);
        	up = true;
        	setBackgroundResource(R.drawable.flipblock);
        	handler.sendEmptyMessage(FLIP_UP_WHAT);
            break;
        case MotionEvent.ACTION_CANCEL:
            break;
        }
        return true;
    }
	@Override
	public void setBackgroundResource(int resid) {
		// TODO Auto-generated method stub
		super.setBackgroundResource(resid);
	}
	
	
}
