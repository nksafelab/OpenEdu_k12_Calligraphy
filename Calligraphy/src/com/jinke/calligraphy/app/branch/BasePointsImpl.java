package com.jinke.calligraphy.app.branch;
import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.Log;


public class BasePointsImpl {
	
	public	ArrayList<BasePoint> 	bPointsList;
	public  BaseBitmap				bDrawBitmap;
	public	int						bMaxPointNum;     	//保存点阵数组中最大可以保存的点数。
	public 	MyView					bView;
	public 	CurInfo					bCurInfo;			//在构造时由具体传入的BaseBitmap赋值
	public	Paint					bPaint;				//每种点(毛笔/硬笔)都有不同的paint
	public  Canvas 					bCanvas;
	
	public  boolean 		isPenDown = false;
	private static final String TAG = "BasePointsImpl";
	

	public BasePointsImpl(BaseBitmap bb, MyView view){
		bPointsList = new ArrayList<BasePoint>();
		bMaxPointNum = Integer.MAX_VALUE;
		bDrawBitmap = bb;
		bView = view;
		
		bCurInfo = bDrawBitmap.bCurInfo;
		
		bPaint = new Paint();
		bPaint.setAntiAlias(true);
		bPaint.setDither(true);
		bPaint.setStyle(Paint.Style.STROKE);
		bPaint.setStrokeJoin(Paint.Join.ROUND);
		bPaint.setStrokeCap(Paint.Cap.ROUND);
		bPaint.setStrokeWidth(7);
		bPaint.setColor(Color.RED);
		bb.paint = bPaint;
		//11.29 caoheng 初始画笔颜色
		bCanvas = new Canvas();
		bCanvas.setBitmap(bView.cursorBitmap.getTopBitmap());

	}

	public void addPoint(BasePoint p){
//		Log.e("addPoint", "before add:" + bPointsList.size());
		bPointsList.add(p);
	}
	
	public void draw(Canvas canvas, Matrix matrix){
//		Log.i(TAG, "draw");
		bDrawBitmap.doDraw(canvas, bPaint, matrix);
	}
	
	/*
	 * touch start
	 */
	public void start(float x, float y){
		isPenDown = true;
		Log.i(TAG, "start");
		bView.hasTouch = true;
		bDrawBitmap.start(x, y);
	}
	
	/*
	 * touch move
	 * by gongxl & jinyang
	 * 加上返回值，如果为从未start，则不执行之后的绘图操作,
	 * 返回false，在子类中进行判断，阻止子类代码继续执行
	 * ：修改侧滑后，点击>Start.SCREEN_WIDTH时产生的不正常绘图，导致崩溃
	 */
	public boolean makeNextPoint(float x, float y) {
		if(!isPenDown)
			return false;
		bDrawBitmap.move(x, y);
		
		return true;
	}

	/*
	 * touch up
	 */
	public boolean after(){
		if(!isPenDown)
			return false;
		isPenDown = false;
		bDrawBitmap.after();
		return true;
	}
	
	
	public void finish(){
		bDrawBitmap.finish();
	}

	
	/*
	 * 设置当前使用的画布风格(涂鸦/光标)
	 */
	public void setBitmap(BaseBitmap bb){
		bDrawBitmap = bb;
	}
	
	/*
	 * 保持对MyView的引用
	 */
	public void setView(MyView v){
		bView = v;
	}

	public void clear(){
		bPointsList.clear();
	}
	
	public void updateBitmap() {
		Log.i(TAG, "update bitmap");
		bDrawBitmap = bView.baseBitmap;
		bCurInfo = bDrawBitmap.bCurInfo;
	}
	public void reduceColor(Paint mPaint) {
//        int color = mPaint.getColor();
//        if(color <= 0xFFFFFFFF - 5)
//                return;
//        mPaint.setARGB(Color.alpha(color), Color.red(color)+5,
//        		Color.green(color)+5, Color.blue(color)+5);
    }
	public void updatePaintSize() {
    }
}
