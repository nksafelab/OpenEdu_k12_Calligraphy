package com.jinke.mywidget;

import com.jinke.calligraphy.app.branch.CursorDrawBitmap;
import com.jinke.calligraphy.app.branch.R;
import com.jinke.calligraphy.app.branch.WorkQueue;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Button;

public class FlipButton extends Button{

	public int mPreviousx;
	public int mPreviousy;
	public Handler handler;
	public boolean up = false;
	
	public FlipButton(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	public FlipButton(Context context, Handler handler){
		super(context);
		this.handler = handler;
		
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
            break;
        case MotionEvent.ACTION_MOVE:
            int iDeltx = iCurrentx - mPreviousx;
            int iDelty = iCurrenty - mPreviousy;
            
            Log.e("flipButton", "move iTop" + iTop);
            if(top + iDelty >= -5 && top + iDelty + getHeight() < 850){
            	Log.e("flipButton", "iTop" + iTop);
	            if(iDeltx != 0 || iDelty != 0)
	                layout(600-getWidth(),
	                		top + iDelty,
	                        600,
	                        top + iDelty + getHeight());
	            Log.e("flipButton", "top+iDelty:" + top + iDelty + " +height:" + top + iDelty + getHeight());
	            
	 //834
	            mPreviousx = iCurrentx - iDeltx;
	            mPreviousy = iCurrenty - iDelty;
	            
	            
	            
	    	        for(int i=0;i<CursorDrawBitmap.listEditableCalligraphy.size();i++){
	    	        	if(top <= 40)
	    	        		CursorDrawBitmap.listEditableCalligraphy.get(i).setFlip_dst(0);
	    	        	else
	    	        		CursorDrawBitmap.listEditableCalligraphy.get(i).setFlip_dst(iTop);
	    			}
	            
	            
	            handler.sendEmptyMessage(iTop);
            }else{
            	WorkQueue.getInstance().endFlipping();
            }
            
            break;
        case MotionEvent.ACTION_UP:
        	WorkQueue.getInstance().endFlipping();
        	up = true;
        	setBackgroundResource(R.drawable.flipblock);
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
	
	
		/*
		 * 
		 * 返回true则这个手势事件就结束了，并不会向下传递到子控件
		 */
	

}
