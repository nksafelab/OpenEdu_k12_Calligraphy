package com.jinke.calligraphy.touchmode;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import com.jinke.calligraphy.activity.Properyt;
import com.jinke.calligraphy.app.branch.EditableCalligraphy;
import com.jinke.calligraphy.app.branch.EditableCalligraphyItem;
import com.jinke.calligraphy.app.branch.MyView;
import com.jinke.calligraphy.app.branch.Start;
import com.jinke.calligraphy.app.branch.WorkQueue;
import com.jinke.mindmap.ArrowDrawer;
import com.jinke.mindmap.DownArrowDrawer;
import com.jinke.mindmap.LeftArrowDrawer;
import com.jinke.mindmap.MathUtil;
import com.jinke.mindmap.MathUtil.DIRECTION;
import com.jinke.mindmap.MindMapItem;
import com.jinke.mindmap.RightArrowDrawer;
import com.jinke.mindmap.UpArrowDrawer;
import com.jinke.single.LogUtil;

public class HandWriteMode implements TouchMode{

	private static final String TAG = "HandWriteMode";
	MyView view;
	private Matrix sMatrix;
	
	private int mLastMotionX,mLastMotionY;
	private boolean isMoved;
	private boolean isReleased;
	private int mCounter;
	private Runnable mLongPressRunnable;
	private static final int TOUCH_SLOP = 20;
	private EditableCalligraphyItem picItem = null;
	private boolean isPicPress;
	
	private EditableCalligraphyItem mindItem = null;
	private boolean isMindmapPress;
	private boolean drawArrow = false;
	private boolean mindmapEditableFlag = false;
	
	private MindMapItem flipMindItem = null;
	private boolean isMindmapFlip;
	
	//arrow draw
	private ArrowDrawer arrowDrawer = null;
	private UpArrowDrawer upArrowDrawer;
	private DownArrowDrawer downArrowDrawer;
	private LeftArrowDrawer leftArrowDrawer;
	private RightArrowDrawer rightArrowDrawer;
	
	public void setMindMapEditStatusTrue(){
		Start.c.setMindMapStatus();
		this.mindmapEditableFlag = true;
	}
	public void setMindMapEditStatusFalse(){
		Start.c.setNotMindMapStatus();
		this.mindmapEditableFlag = false;
	}
	public boolean isMindMapEditableStatus(){
		return this.mindmapEditableFlag;
	}
	
	public void initArrowDrawer(){
		upArrowDrawer = new UpArrowDrawer();
		downArrowDrawer = new DownArrowDrawer();
		leftArrowDrawer = new LeftArrowDrawer();
		rightArrowDrawer = new RightArrowDrawer();
		
		arrowDrawer = rightArrowDrawer;
	}
	public void setsMatrix(Matrix sMatrix) {
		this.sMatrix = sMatrix;
//		Log.e("vector", "handwriteMode:   matrix:" + (sMatrix == null));
	}

	@Override
	public Matrix getMatrix() {
		// TODO Auto-generated method stub
		return sMatrix;
	}

	public HandWriteMode(MyView view) {
		this.view = view;
		initArrowDrawer();
		mLongPressRunnable = new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				mCounter--;
				if(mCounter > 0 || isMoved || isReleased)
					return;
				longPressed();
				clear();
			}
		};
	}

	protected void longPressed() {
		// TODO Auto-generated method stub
		Log.i("0801", "long press !@!!!!!!!!!!!!!!!");
		Log.e("long", "long press !@!!!!!!!!!!!!!!!");
		if(isPicPress){
			view.setTouchMode(view.getImageSlideMode());
			((ImageSlideMode)view.getImageSlideMode()).setStartPoint(mLastMotionX, mLastMotionY);
			((ImageSlideMode)view.getImageSlideMode()).setPicItem(picItem);
		}
		else if(isMindmapPress){
			drawArrow = true;
			setMindMapEditStatusTrue();
			view.invalidate();
		}else if(isMindmapFlip){
			view.setTouchMode(view.getMindSlideMode());
			((MindSlideMode)view.getMindSlideMode()).setStartPoint(mLastMotionX, mLastMotionY);
			((MindSlideMode)view.getMindSlideMode()).setMindItem(flipMindItem);
		}
	}

	@Override
	public void touch_action_down(MotionEvent event) {
		// TODO Auto-generated method stub
		int x = (int)event.getX();
		int y = (int)event.getY();
		if(checkRight(event.getX(), event.getY())){
			view.setTouchMode(view.getSideDownMode());
			view.getTouchMode().touch_action_down(event);
    	} else{   		
    		/*
    		 * 此处每次书写都会检测是否是图片长按，是否是翰林算子长按；可优化；
    		 */
    		isPicture(event);
    		if(isPicPress){
	    		mLastMotionX = x;
	    		mLastMotionY = y;
	    		mCounter ++;
	    		isReleased = false;
	    		isMoved = false;
	    		view.postDelayed(mLongPressRunnable, ViewConfiguration.getLongPressTimeout());
    		}else{
    			if(isMindmap(event)){
    				mLastMotionX = x;
    	    		mLastMotionY = y;
    	    		mCounter ++;
    	    		isReleased = false;
    	    		isMoved = false;
    	    		view.postDelayed(mLongPressRunnable, (int)(ViewConfiguration.getLongPressTimeout()*1.5));
    			}else if(isMindmapFlip(event)){
    				mLastMotionX = x;
    	    		mLastMotionY = y;
    	    		mCounter ++;
    	    		isReleased = false;
    	    		isMoved = false;
    	    		view.postDelayed(mLongPressRunnable, (int)(ViewConfiguration.getLongPressTimeout()*1.5));
    			}
    		}
    		view.baseImpl.start(event.getX(), event.getY());
    	}
	}

	private boolean isMindmapFlip(MotionEvent event){
		flipMindItem = view.cursorBitmap.listEditableCalligraphy.get(3).
			isMindmapFlip(event.getX(), event.getY());
		if(flipMindItem == null)
			isMindmapFlip = false;
		else
			isMindmapFlip = true;
		return isMindmapFlip;
	}
	private boolean isMindmap(MotionEvent event) {
		// TODO Auto-generated method stub
		EditableCalligraphy currentCalligraphy = view.cursorBitmap.listEditableCalligraphy.get(3);
		mindItem = currentCalligraphy.isMindmap(event.getX(), event.getY());
		if(mindItem == null){
			isMindmapPress = false;
		}else{
			isMindmapPress = true;
			LogUtil.getInstance().e("mindmap","mind map long press");
		}
		return isMindmapPress;
	}

	@Override
	public void touch_action_pointer_down(MotionEvent event) {
		// TODO Auto-generated method stub
		isMoved = true;
	}

	@Override
	public void touch_action_pointer_up(MotionEvent event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void touch_move(MotionEvent event) {
		Log.i("0801", "handwriteMode touch_mode");
		// TODO Auto-generated method stub
		
		if(isPicPress||isMindmapFlip){
			if(isMoved){
			}else{
				int x = (int)event.getX();
				int y = (int)event.getY();
				if(Math.abs(mLastMotionX - x) > TOUCH_SLOP ||
						Math.abs(mLastMotionY - y) > TOUCH_SLOP){
					isMoved = true;
				}
			}
		}
		if(isMindmapPress){
			if(isMoved){
			}else{
				if(!drawArrow){
					//还没有出箭头.判断是长按，还是正常书写
					int x = (int)event.getX();
					int y = (int)event.getY();
					if(Math.abs(mLastMotionX - x) > TOUCH_SLOP ||
							Math.abs(mLastMotionY - y) > TOUCH_SLOP){
						isMoved = true;
					}
				}else{
					DIRECTION direction = 
						MathUtil.getAngle((double)mLastMotionX, (double)mLastMotionY, (double)event.getX(), (double)event.getY());
					if(direction == DIRECTION.UP){
						arrowDrawer = upArrowDrawer;
					}else if(direction == DIRECTION.RIGHT){
						arrowDrawer = rightArrowDrawer;
					}else if(direction == DIRECTION.DOWN){
						arrowDrawer = downArrowDrawer;
					}else{
						arrowDrawer = leftArrowDrawer;
					}
					arrowDrawer = rightArrowDrawer;//目前只支持向右扩展，只出现向右箭头
				}
			}
		}
		if(!drawArrow){
			//确定是长按，画出箭头后，移动不再记录
			view.baseImpl.makeNextPoint(event.getX(), event.getY());
		}
		
	}

	@Override
	public void touch_up(MotionEvent event) {
		// TODO Auto-generated method stub
		if(isPicPress || isMindmapPress)//抬手，设置取消长按定时器
			isReleased = true;
		if(!drawArrow)
			view.baseImpl.after();
		else
			clear();
		if(isMindmapPress && drawArrow){
			isMindmapPress = false;
			drawArrow = false;
			
			//ly
			//插入节点之前先清屏，再重绘
			view.mBitmap.eraseColor(Color.WHITE);
			//end
			
			createNewMindMapItem();
			view.cursorBitmap.updateHandwriteState();
			
		}
		

	}
	/**
	 * 在按下的节点上，创建新的节点
	 */
	private void createNewMindMapItem() {
		Log.e("minderror", "create new item");
//		try{  //zk20121109
//		if(mindmapEditableFlag && view.cursorBitmap.cal_current.getCurrentMindMapItem().getCharList().size() == 0){
//			Log.e("minderror", "error1");
//		
//			return;
//		}
//		} catch (Exception e) {
//			Log.e("minderror", "error1",e);
//			return;
//		}
//		Log.e("minderror", "not error");
		MindMapItem item = mindItem.getMindMapItem().createNewChild();
		//改变当前编辑节点
		view.cursorBitmap.cal_current = 
			view.cursorBitmap.listEditableCalligraphy.get(3);
		if(view.cursorBitmap.cal_current.getID() == 3){
			view.cursorBitmap.cal_current.setCurrentMindMapItem(item);
		}

	}
	@Override
	public void draw(Canvas canvas) {
		// TODO Auto-generated method stub
		
		view.baseImpl.draw(canvas, sMatrix);
		
		if(drawArrow){
			arrowDrawer.doDraw(canvas,mLastMotionX,mLastMotionY);
		}
		
	}
	
	public boolean checkRight(float x, float y) {
		//if (x>500 && y<100 || x<100 && y<100)
		if (x>1500 && y<100 || x<100 && y<100)
			return true;
		else
			return false;
	}

	@Override
	public void printInfo() {
		// TODO Auto-generated method stub
		Log.i("touchmode", "This is " + TAG);
		
	}


	@Override
	public void clear() {
		// TODO Auto-generated method stub
		//取消之前的点击事件
		view.cursorBitmap.cancelWord();
	}
	private void isPicture(MotionEvent event) {
		// TODO Auto-generated method stub
		
		EditableCalligraphy currentCalligraphy = view.cursorBitmap.listEditableCalligraphy.get(3);
		
		picItem = currentCalligraphy.isPic(event.getX(), event.getY());
		if(picItem == null){
			isPicPress = false;
		}else{
			isPicPress = true;
		}
	}


}
