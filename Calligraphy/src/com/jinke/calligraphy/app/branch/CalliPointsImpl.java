package com.jinke.calligraphy.app.branch;

import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;


/*
 * 毛笔点集操作
 */
public class CalliPointsImpl extends BasePointsImpl {
	
	public 	int 		mStartPoint;		//从此点开始画，当前点life为0时，即移到下一个点
	public 	int 		mDrawnPoint;		//从此点开始画，这一点已经不用再重绘
    public	float		mFontSize;			//保存当前的字体大小
	public	CalliTimer 	mTimer;
	public 	View		mView;
	
	public static boolean penStat = false;
	
	private float		zX; //zk20121027
	private float		zY; //zk20121027
	
	private float		mX;
	private float		mY;
	
//	private Canvas		mCanvas;
	public	Paint		mPaint;
	public 	Paint		mPathPaint;
	private CurInfo		mCurInfo;
	
	private static final String TAG = "CalliPointsImpl";
	private static boolean TIMER = false;
	private Canvas		strokeCanvas;
	private Canvas		addCanvas;
	
	private Path 	mPath;
	private Path 	mOffsetPath;
	private float mLastPointX=0;
	private float mLastPointY=0;
	
	
	//private Thread genDataThread;
	//private GenDataRunnable genDataRunnable;
	private final int TOUCH_TOLERANCE = 3;
	
	protected Rect dirtyRect;
	private float mStartX, mStartY, mEndX, mEndY;
	
	
	//zk20121027
	public void nextPoint() {
//		Log.i(TAG, "Time:" + Calendar.getInstance().getTimeInMillis());
		
		if(zX<1 && zY<1)
			return;
		
		float x=zX;
		float y=zY;
		mLastPointX=(x + mX)/2;
        mLastPointY=(y + mY)/2;
        
		float dx = mX - mLastPointX;
        float dy = mY - mLastPointY;
        
        float sqrt = ((float)Math.sqrt(dx*dx + dy*dy));
        
        float tmpFontSize = 0;
//        if(sqrt < 6){
//			tmpFontSize = mFontSize + (6) - sqrt;
//        }else{
//        	if(sqrt > 18){
//        		tmpFontSize = mFontSize - 1;//zk20121123
//        	}
//        	else
//        	{
//        		tmpFontSize = mFontSize - ((sqrt - (6)) / (12)) ;
//        	}
//        }
        tmpFontSize = mFontSize - ((sqrt - (7)) / (5)) ;
        	
        extendBound(mLastPointX, mLastPointY, mFontSize +10);
//        Log.i(TAG, "touch_move:size:" + tmpFontSize + " sqrt:" + sqrt);
        
        if(tmpFontSize > CalliPoint.SIZE_MAX)
        	tmpFontSize = CalliPoint.SIZE_MAX;
        if(tmpFontSize < CalliPoint.SIZE_MIN - 2 )
        	tmpFontSize = CalliPoint.SIZE_MIN - 2;

//      if( (Math.abs(dx) >= TOUCH_TOLERANCE || Math.abs(dy) >= TOUCH_TOLERANCE ) && cpi.mFontSize > 0) 
//      if( (Math.abs(dx) >= TOUCH_TOLERANCE || Math.abs(dy) >= TOUCH_TOLERANCE )) 
        {

        	for(int i=0;i<(int)(sqrt);i+=(sqrt/(4) > (5)) ? sqrt/(4):(5))
            {
                float px = mX - dx/sqrt * i;
                float py = mY - dy/sqrt * i;

//                
                CalliPoint p = new CalliPoint(px, py, mFontSize-((float)(mFontSize-tmpFontSize)/sqrt)*i, mPaint);
               	addPoint(p);
   //            	break;
            }
 	
        	if((int)sqrt == 0){
                CalliPoint p = new CalliPoint(mX, mY, tmpFontSize, mPaint);
               	addPoint(p);
        	}
        	
        	
        }
      	mFontSize = tmpFontSize;        
      	
      	mPath.quadTo(mX, mY, mLastPointX, mLastPointY);
//      mPath.lineTo(mLastPointX, mLastPointY);
//      mPath.quadTo(mX, mY, x, y);
      	
        mX = x;
        mY = y;
//        bView.invalidate();
		//genDataRunnable.isBitmapDirty = true;
		
		//zk20121027绘制轨迹操作移至Tick中
//		if(!mPath.isEmpty()) {
//			Log.i(TAG, "size min:" + CalliPoint.SIZE_MIN);
//			mPathPaint.setStrokeWidth(CalliPoint.SIZE_MIN + CalliPoint.FILTER_FACTOR);
//			bCanvas.drawPath(mPath, mPathPaint);
////			mPath.reset();
//		}
		
		//zk20121027刷屏移至Tick中
		/*
		if(count %3 == 0)
			bView.invalidate();
		if(++count > Integer.MAX_VALUE)
			count = 0;
			*/
	}
	
	public void extendBound(float x , float y, float w) {
		if(x - w  < mStartX)
			mStartX = x -w ;
		if(y -w  < mStartY)
			mStartY = y -w  ;
		if(x +w > mEndX)
			mEndX = x +w ;
		if(y +w > mEndY)
			mEndY = y +w ;
		if(mStartX < 0 )
			mStartX =0;
		if(mStartY < 0 )
			mStartY =0;
		if(mEndX >= Start.SCREEN_WIDTH)
			mEndX = Start.SCREEN_WIDTH;
		if(mEndY >= Start.SCREEN_HEIGHT)
			mEndY = Start.SCREEN_HEIGHT;
		//             dirtyRect.set(mStartX, mStartY, mEndX,mEndY);
		dirtyRect.set((int)mStartX, (int)mStartY, (int)mEndX,
				(int)mEndY);
	}
	
	public void resetBound() {
		mStartX = Float.MAX_VALUE;
		mStartY = Float.MAX_VALUE;
		mEndX = 0;
		mEndY = 0;
	}
    public CalliPointsImpl(BaseBitmap b, MyView v){
    	super(b, v);
    	mStartPoint	= 0;
    	mDrawnPoint	= 0;
    	mFontSize	= 0;
    	mPaint		= super.bPaint;
    	mCurInfo	= super.bCurInfo;
    	
    	//ly
    	mTimer = new CalliTimer(Long.MAX_VALUE, 80);
    	//end
    	
    	//mTimer		= new CalliTimer(Long.MAX_VALUE, 1); //zk20121027 mTimer		= new CalliTimer(Long.MAX_VALUE, 1); 
    	//mTimer.start();
    	SharedPreferences settings = Start.context.getSharedPreferences(ParametersDialog.FILENAME,  android.content.Context.MODE_PRIVATE);
		penStat = settings.getBoolean(ParametersDialog.PARAM_CALI, false);
//        mCanvas 	= new Canvas();
//        mCanvas.setBitmap(mCurInfo.mBitmap);
        addCanvas = new Canvas();
        addCanvas.setBitmap(BaseBitmap.addBitmap);

        strokeCanvas = new Canvas();
        
        mPath = new Path();
        mOffsetPath = new Path();
    	mPathPaint  = new Paint(mPaint);
    	mPathPaint.setStrokeWidth(CalliPoint.SIZE_MIN);
    	
    	//genDataRunnable = new GenDataRunnable();
    	//genDataThread = new Thread(genDataRunnable);
//		genDataThread.start();
    	dirtyRect = new Rect();
    	resetBound();
    }
    
    int count = 0;
    @Override
	public void start(float x, float y) {
		// TODO Auto-generated method stub
    	super.start(x, y);
    	count = 0;
    	Log.i(TAG, "start");
    	if(TIMER == false){
        	mTimer.start();
        	TIMER = true;
    	}
    	
		mFontSize = CalliPoint.SIZE_MIN + CalliPoint.FILTER_FACTOR;
		CalliPoint point = new CalliPoint(x, y, mFontSize, mPaint);
    	point.startFlag = true;
        addPoint(point);
        mX = x;
        mY = y;
        mPath.moveTo(x, y);
        mLastPointX = x;
        mLastPointY = y;

		//当切换到涂鸦态时，此函数返回null
//		if(MyView.drawStatus == MyView.STATUS_DRAW_FREE)
//			mCanvas.setBitmap(mCurInfo.mBitmap);
//		else
//			mCanvas.setBitmap(bDrawBitmap.getTopBitmap());
		
		//genDataRunnable.isRun = true;
		
		//genDataRunnable.isBitmapDirty = true;
	}


	@Override
	public boolean after() {
		// TODO Auto-generated method stub
		if(!super.after())
			return false;
		
		//mPath.lineTo(mX, mY);
		//zk20121027
		//makeNextPoint(mX, mY);
		makeLastPoint();
		
		zX=0;
		zY=0; 
		
		
		if(mFontSize < 0)
			mFontSize = 0;

//		bView.invalidate();
		mPath.reset();
//		genDataThread.stop();

		//genDataRunnable.isRun = false;
		return true;
	}


	@Override
	public void finish() {
		// TODO Auto-generated method stub
		super.finish();
		mTimer.cancel();
		
	}

	//当光标态一个字写完时会调用此方法
	@Override
	public void clear() {
		// TODO Auto-generated method stub
		super.clear();
		mStartPoint = 0;
		mDrawnPoint = 0;
		
		mLastPointX=0;
		mLastPointY=0;
		zX=0;
		zY=0;
		mTimer.cancel();
		TIMER = false;
		bPointsList.clear();
		mPath.reset();
		mX = 0;
		mY = 0;
		resetBound();
		Log.e("clear", "CalliPoint clear !!!!");
	}

	

	@Override
	public void updatePaintSize() {
		// TODO Auto-generated method stub
		super.updatePaintSize();
		if(MyView.drawStatus == MyView.STATUS_DRAW_FREE) {
			CalliPoint.SIZE_MIN = (2);
		//	CalliPoint.SIZE_MIN = 1;
            CalliPoint.SIZE_MAX = (10);
//            mFontSize = CalliPoint.SIZE_MIN;
            mFontSize = 0;
		} else {
			CalliPoint.SIZE_MIN = (5);
            CalliPoint.SIZE_MAX = (30);
            mFontSize = CalliPoint.SIZE_MIN + CalliPoint.FILTER_FACTOR;
            mFontSize = 0;
		}
	}

	static final int maxDrawPointsNum = 15;
	static final int maxSpreadPointsNum = 1000;
	/* (non-Javadoc)
	 * @see com.jinke.calligraphy.app.branch.BasePointsImpl#draw(android.graphics.Canvas, android.graphics.Matrix)
	 */
	@Override
	public void draw(Canvas canvas, Matrix matrix) {
//		Log.e("CALLIDRAW", "BEGIN DRAW");
		// TODO Auto-generated method stub
		super.draw(canvas, matrix);
//		canvas.drawBitmap(bView.cursorBitmap.getTopBitmap(), 0, 0, new Paint());
		if(mEndX > mStartX && mEndY > mStartY)
			canvas.drawBitmap(bView.cursorBitmap.getTopBitmap(), dirtyRect,	dirtyRect, new Paint());
	}

    
    public boolean spread() {
    	int size = bPointsList.size();
    	boolean spreaded = false;
		int maxnum = size-1;
    	if(mStartPoint + maxSpreadPointsNum < size)
    		maxnum = mStartPoint + maxSpreadPointsNum;
    	CalliPoint point = null;
		int curcolor = mPaint.getColor();
    	for (int i = mStartPoint; i < maxnum; i++) {
    		point = (CalliPoint) bPointsList.get(i);
//    		for(int j =0;j < point.life;j++)
//    			point.addSize();
//    		point.life = 0;
    		point.addSize();
    		point.life-=1;
    		if(point.life <= 0) {
    			spreaded = true;
    			mStartPoint = i+1;
    			//continue;
    		}
    		CalliPoint point2 = (CalliPoint) bPointsList.get(i+1);
//    		mPaint.setARGB((int)point.color[0], (int)point.color[1], (int)point.color[2], (int)point.color[3]);
//    		mPathPaint.setARGB((int)point.color[0], (int)point.color[1], (int)point.color[2], (int)point.color[3]);
    		if(point.size <= point.SIZE_MIN + 2 * CalliPoint.FILTER_FACTOR)
    			continue;
//    		mPaint.setStrokeWidth(point.size);
    		if(point2.startFlag == true) {
    		} else {       
//                       reduceColor(mPaint);
                       mPaint.setStrokeWidth(point.size );
           	           bCanvas.drawLine(point.x, point.y, point2.x, point2.y, mPaint);

    		}
    	
//    		point.addSize();
//    		point.life-=1;
//			if(point.life <= 0) {
//    			spreaded = true;
//    			mStartPoint = i+1;
//    			continue;
//    		}
//    		point.addSize();
//    		point.life-=1;
//    		if(point.life <= 0){
//    			spreaded = true;
//    			mStartPoint = i+1;
//    			continue;
//    		}
//    		point.addSize();
//    		point.life-=1;
//    		if(point.life <= 0) {
//    			spreaded = true;
//    			mStartPoint = i+1;
//    			continue;
//    		}
//    		for(int j=1;j<4;j++)
//    			point.color[j] -= 1;
    		spreaded = true;
        }
    	if(point != null){
    		CalliPoint point2 = (CalliPoint) bPointsList.get(maxnum);
//    		mPaint.setARGB((int)point.color[0], (int)point.color[1], (int)point.color[2], (int)point.color[3]);
//    		mPathPaint.setARGB((int)point.color[0], (int)point.color[1], (int)point.color[2], (int)point.color[3]);
    		if(point.size > point.SIZE_MIN) {
    			
//    			mPaint.setStrokeWidth(point.size);
    			if(point2.startFlag == true) {
    			} else {       
//    				reduceColor(mPaint);
    				mPaint.setStrokeWidth(point.size );
    				bCanvas.drawLine(point.x, point.y, point2.x, point2.y, mPaint);
    				
    			}
    		}
    	}
		mPaint.setColor(curcolor);
		mPathPaint.setColor(curcolor);
    	return spreaded;
    }
    
    @Override
	public boolean makeNextPoint(float x, float y) {
		// TODO Auto-generated method stub
		if(!super.makeNextPoint(x, y))
			return false;
		
//		Log.i(TAG, "Time:" + Calendar.getInstance().getTimeInMillis());
		mPath.moveTo(mLastPointX, mLastPointY);
		
		//zk20121027 添加点的动作放在了Tick中
		zX = x;
		zY = y;
		
        return true;
	}


    public boolean makeLastPoint() {
//    	Log.i("kk", "************----------------------------------- ");
    	if(zX<1 && zY<1)
			return false;
		float x=zX;
		float y=zY;
		mPath.reset();
		mPath.moveTo(mLastPointX, mLastPointY);
		float mmLX=mLastPointX;
		float mmLY=mLastPointY;
		mLastPointX=(x + mX)/2;
        mLastPointY=(y + mY)/2;
        
		float dx = mX - mLastPointX;
        float dy = mY - mLastPointY;
        
        float sqrt = ((float)Math.sqrt(dx*dx + dy*dy));
//        Log.i("kk", "*****************************************makeLastPoint: " +"sqrt: "+sqrt);
        float tmpFontSize = 0;
        
        if(sqrt>8){
        	float tmp=(1.8f+CalliPoint.SPREAD_FACTOR)*(sqrt - (6)/(12))  ;
        	tmp=mFontSize;
        	float tmpX=mmLX;
        	float tmpY=mmLY;
        	float tmpSqrt=((float)Math.sqrt((mX-mmLX)*(mX-mmLX) + (mY-mmLY)*(mY-mmLY)));
        	float sinFirst=(mX-mmLX)/tmpSqrt;
        	float cosFirst=(mY-mmLY)/tmpSqrt;
        	tmpSqrt=((float)Math.sqrt((mX-mLastPointX)*(mX-mLastPointX) + (mY-mLastPointY)*(mY-mLastPointY)));
        	float sinLast=(mLastPointX-mX)/tmpSqrt;
        	float cosLast=(mLastPointY-mY)/tmpSqrt;
        	tmpFontSize=mFontSize;

        	for(;tmp>0;tmp=tmp-0.2f)
        	{
        		tmpFontSize=tmpFontSize-0.2f;
                if(tmpFontSize < CalliPoint.SIZE_MIN ){
                	tmpFontSize = CalliPoint.SIZE_MIN;
                	break;
                }
                
            	if( ( ((mmLX<mX)&&(tmpX>=mmLX)&&(tmpX<=mX))  || ((mmLX>mX)&&(tmpX>=mX)&&(tmpX<=mmLX)) ) &&   ( ((mmLY<mY)&&(tmpY>=mmLY)&&(tmpY<=mY))  || ((mmLY>mY)&&(tmpY>=mY)&&(tmpY<=mmLY)) ) )
        		{
	        		tmpX=tmpX+1*sinFirst;
	        		tmpY=tmpY+1*cosFirst;
	        		Log.i("kk", "Last Point First Line: " + tmpFontSize+" "+mFontSize+" "+tmp+" "+tmpX+" "+tmpY);
        		}
        		else
        		{
        			tmpX=tmpX+1*sinLast;
	        		tmpY=tmpY+1*cosLast;
	        		Log.i("kk", "Last Point Last Line: " + tmpFontSize+" "+mFontSize+" "+tmp+" "+tmpX+" "+tmpY);
//        			if( ( ((mLastPointX<mX)&&(tmpX>=mLastPointX)&&(tmpX<=mX))  || ((mLastPointX>mX)&&(tmpX>=mX)&&(tmpX<=mLastPointX)) ) &&   ( ((mLastPointY<mY)&&(tmpY>=mLastPointY)&&(tmpY<=mY))  || ((mLastPointY>mY)&&(tmpY>=mY)&&(tmpY<=mLastPointY)) ) )
//            		{
//        				tmpX=tmpX+1*sinLast;
//		        		tmpY=tmpY+1*cosLast;
//		        		Log.i("kk", "Last Point Last Line: " + tmpFontSize+" "+mFontSize+" "+sqrt+" ");
//            		}
//        			else
//        			{
//        				Log.i("kk", "Last Point End Line: " + tmpFontSize+" "+mFontSize+" "+sqrt+" ");
//        				break;
//        			}
        		}
            	
            	extendBound(tmpX, tmpY, tmpFontSize);
            	
        		mPathPaint.setStrokeWidth(tmpFontSize);
              	mPath.lineTo(tmpX, tmpY);
              	bCanvas.drawPath(mPath, mPathPaint);
              	mPath.reset();
        		mPath.moveTo(tmpX, tmpY);
        		//bView.invalidate();
        	}
        	
//        	tmpFontSize = mFontSize - tmp + i-1  ;
        	
        }
        bView.invalidate();
        return true;
    }
    public boolean directNextpoint() {
    	if(zX<1 && zY<1)
			return false;
		float x=zX;
		float y=zY;
		mPath.reset();
		mPath.moveTo(mLastPointX, mLastPointY);
		float mmLX=mLastPointX;
		float mmLY=mLastPointY;
		mLastPointX=(x + mX)/2;
        mLastPointY=(y + mY)/2;
        
//		float dx = mX - mLastPointX;
//        float dy = mY - mLastPointY;
//        float sqrt = ((float)Math.sqrt(dx*dx + dy*dy));
        
        float dx1 = mX-mmLX ;
        float dy1 = mY-mmLY;
        
        float dx2 = mLastPointX - mX;
        float dy2 = mLastPointY - mY;
        float sqrt1 = ((float)Math.sqrt(dx1*dx1 + dy1*dy1));
        float sqrt2 = ((float)Math.sqrt(dx2*dx2 + dy2*dy2));
        float sqrt=sqrt1+sqrt2;
        
        
    	
    	
        
        
        
        float tmpFontSize = 0;
        
        
        if(sqrt>25){ //13
//        	float tmp=(1.3f+CalliPoint.SPREAD_FACTOR)*(sqrt - (6)/(12))  ;
////        	if(tmp>1.8f){
////        		tmpFontSize= mFontSize -1.8f;
////        		Log.i(TAG, "33333333333: " + tmpFontSize+" "+mFontSize+" "+sqrt+" ");
////        	}
////        	else{
////        		tmpFontSize = mFontSize - tmp;
////        	}
//        	
//        	
//        	
//        	float tmpX=mmLX;
//        	float tmpY=mmLY;
//        	float tmpSqrt=((float)Math.sqrt((mX-mmLX)*(mX-mmLX) + (mY-mmLY)*(mY-mmLY)));
//        	float sinFirst=(mX-mmLX)/tmpSqrt;
//        	float cosFirst=(mY-mmLY)/tmpSqrt;
//        	tmpSqrt=((float)Math.sqrt((mX-mLastPointX)*(mX-mLastPointX) + (mY-mLastPointY)*(mY-mLastPointY)));
//        	float sinLast=(mLastPointX-mX)/tmpSqrt;
//        	float cosLast=(mLastPointY-mY)/tmpSqrt;
//        	tmpFontSize=mFontSize;
//
//        	for(;tmp>0;tmp=tmp-0.1f)
//        	{
//        		tmpFontSize=tmpFontSize-0.1f;
//                if(tmpFontSize < CalliPoint.SIZE_MIN ){
//                	tmpFontSize = CalliPoint.SIZE_MIN;
//                	break;
//                }
//                
//            	if( ( ((mmLX<mX)&&(tmpX>=mmLX)&&(tmpX<=mX))  || ((mmLX>mX)&&(tmpX>=mX)&&(tmpX<=mmLX)) ) &&   ( ((mmLY<mY)&&(tmpY>=mmLY)&&(tmpY<=mY))  || ((mmLY>mY)&&(tmpY>=mY)&&(tmpY<=mmLY)) ) )
//        		{
//	        		tmpX=tmpX+1*sinFirst;
//	        		tmpY=tmpY+1*cosFirst;
////	        		Log.i(TAG, "First Line: " + tmpFontSize+" "+mFontSize+" "+sqrt+" ");
//        		}
//        		else
//        		{
//        			if( ( ((mLastPointX<mX)&&(tmpX>=mLastPointX)&&(tmpX<=mX))  || ((mLastPointX>mX)&&(tmpX>=mX)&&(tmpX<=mLastPointX)) ) &&   ( ((mLastPointY<mY)&&(tmpY>=mLastPointY)&&(tmpY<=mY))  || ((mLastPointY>mY)&&(tmpY>=mY)&&(tmpY<=mLastPointY)) ) )
//            		{
//	        			tmpX=tmpX+1*sinLast;
//		        		tmpY=tmpY+1*cosLast;
////		        		Log.i(TAG, "Last Line: " + tmpFontSize+" "+mFontSize+" "+sqrt+" ");
//            		}
//        			else
//        			{
////        				Log.i(TAG, "End Line: " + tmpFontSize+" "+mFontSize+" "+sqrt+" ");
//        				break;
//        			}
//        		}
//
//        		mPathPaint.setStrokeWidth(tmpFontSize);
//              	mPath.lineTo(tmpX, tmpY);
//              	bCanvas.drawPath(mPath, mPathPaint);
//              	mPath.reset();
//        		mPath.moveTo(tmpX, tmpY);
//        	}
//        	float fontSizeChange=(1.3f+CalliPoint.SPREAD_FACTOR)*((sqrt - 15)/(20))  ;   	
        	
        	float fontSizeChange=(2.3f+CalliPoint.SPREAD_FACTOR)*((sqrt - 15)/(20))  ;   	
        	
//        	float fontSizeChange=(1.3f+CalliPoint.SPREAD_FACTOR)*((sqrt - 6)/(12))  ;
        	bezier(mmLX,mmLY,mX, mY, mLastPointX, mLastPointY,fontSizeChange);
//        	tmpFontSize = mFontSize - tmp + i-1  ;
        	
        }
        else{
        	

        	
        	if(sqrt<15)
        	{
//        		tmpFontSize = mFontSize + (0.1f+CalliPoint.SPREAD_FACTOR)*((15)- sqrt)  ;
        		tmpFontSize = mFontSize + (2.1f+CalliPoint.SPREAD_FACTOR)*((15)- sqrt)  ;
        		
        	}
        	else
        	{
        		tmpFontSize = mFontSize;
        	}
        
        
	        	
	        extendBound(x, y, tmpFontSize);
	//        Log.i(TAG, "touch_move:size:" + tmpFontSize + " sqrt:" + sqrt);
	        
	        if(tmpFontSize > CalliPoint.SIZE_MAX)
	        	tmpFontSize = CalliPoint.SIZE_MAX;

	
	      	mFontSize = tmpFontSize;
	      	mPathPaint.setStrokeWidth(tmpFontSize);
	  //    	mPath.quadTo(mX, mY, mLastPointX, mLastPointY);
	        mPath.lineTo(mLastPointX, mLastPointY);
	//      mPath.quadTo(mX, mY, x, y);
	      	bCanvas.drawPath(mPath, mPathPaint);
      	}
        mX = x;
        mY = y;
        return true;
    }
    
    
    public boolean drawPen() {
    	boolean result=false;
    	//Log.e("uuuu", "penStat" + penStat);
		if(penStat)
		{
			nextPoint();
			//}
			
			result = spread(); 
			
			//zk20121027绘制轨迹操作移至Tick中
			if(!mPath.isEmpty()) {
				//if(tempcounter %8 ==0){    //修改N为6 比8次在单核上效果 
//					Log.i(TAG, "size min:" + CalliPoint.SIZE_MIN);
					mPathPaint.setStrokeWidth(CalliPoint.SIZE_MIN + CalliPoint.FILTER_FACTOR);
					bCanvas.drawPath(mPath, mPathPaint);
	//				mPath.reset();
					result=true;
				//}
			}
		}
		else
		{
			result=directNextpoint();
		}
		
		
		
		 //zk 20121027 将刷屏操作移到判断之外，Tick N次后就刷屏。
		if(result){
			//由于采样周期设置的长了，所以就不等待了 测试CalliTimer 10 这里设置为2效果也还行，(10*2ms一刷屏)，4就有点跳了。
			//if(tempcounter %8 ==0){    //修改N为6 比8次在单核上效果 
				bView.invalidate();

			//}
			
		}
		return result;
		
//		counter ++;
//		if(counter >= 1 && dirty ) {
////			bView.invalidate(dirtyRect);
////			if(genDataRunnable.isUpdated){
////				bView.invalidate();
////				genDataRunnable.isUpdated = false;
////			}
//				bView.invalidate();
//			counter = 0;
//			dirty = false;
//		}
//		if(spread())
//			bView.invalidate();
		//spread();
//			bView.draw();
    }
    
    /*
     * 定时器，每一个tick都执行一次spread
     */
    
    class CalliTimer extends CountDownTimer {

		public CalliTimer(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void onFinish() {
			// TODO Auto-generated method stub
		}

		public int counter = 0;
		public boolean dirty = false;
		int tempcounter=0;
//		public Rect dirtyRect = new Rect(100, 100, 200, 300);
		@Override
		public void onTick(long millisUntilFinished) {
			// TODO Auto-generated method stub
			
			//Log.e("!!!!", "onTick");
			
			if(drawPen()) {
				dirty = true;
			}
		}
	}
    
    
    
    public boolean bezier(float p1x,float p1y,float cpx,float cpy,float p2x,float p2y,float fontSizeChange) {


//    Point p1 = ...; //起点
//    Point cp = ...; //初始的控制点
//    Point p2 = ...; //初始的终点
    

    float t = 0;

    float c1x; //将要求出的控制点的x
    float c1y; //将要求出的控制点的y
    float c2x; //将要求出的终点的x
    float c2y; //将要求出的终点的y

    float px; //二次贝赛尔曲线上的点的x
    float py; //二次贝赛尔曲线上的点的y
    
    float tInterval=0.01f;
  
    float fontSizeInterval=fontSizeChange*tInterval;
    


    
    mPath.reset();
	mPath.moveTo(p1x, p1y);
	
	float tmpFontSize=mFontSize;

	    while ( t < 1 ) {
	        /*
	         控制点是由起点和初始的控制点组成的一次／线性贝赛尔曲线上的点,
	         所以由一次／线性贝赛尔曲线函数表达式求出c1x,c1y
	         */
	        c1x = p1x + ( cpx - p1x ) * t;
	        c1y = p1y + ( cpy - p1y ) * t;
	        
	        /*
	         终点是由初始的控制点和初始的终点组成的一次／线性贝赛尔曲线上的点,
	         所以由一次／线性贝赛尔曲线函数表达式求出c2x,c2y
	         */
	        c2x = cpx + ( p2x - cpx ) * t;
	        c2y = cpy + ( p2y - cpy ) * t;
	        
	        /*
	         二次贝赛尔曲线上的点是由控制点和终点组成的一次／线性贝赛尔曲线上的点,
	         所以由一次／线性贝赛尔曲线函数表达式求出px,py
	         */
	        px = c1x + ( c2x - c1x ) * t;
	        py = c1y + ( c2y - c1y ) * t;

	        tmpFontSize-=fontSizeInterval;
	        t += tInterval;
	        //Log.i("kk", "Bezir "+px+"  "+py);
	    	
          if(tmpFontSize < CalliPoint.SIZE_MIN ){
        	tmpFontSize = CalliPoint.SIZE_MIN;
        }
	        
	        mPathPaint.setStrokeWidth(tmpFontSize);
	      	mPath.lineTo(px, py);
	      	extendBound(px, py, tmpFontSize);
	      	bCanvas.drawPath(mPath, mPathPaint);
	      	mPath.reset();
	    	mPath.moveTo(px, py);
	    	
//	        bCanvas.drawPoint(px, py, mPathPaint);
	    }
	    mPath.lineTo(p2x, p2y);
      	bCanvas.drawPath(mPath, mPathPaint);
	    mFontSize=tmpFontSize;
	    return true;
    }
    /*
    class GenDataRunnable implements Runnable {
    	
    	public boolean isRun = false; 
    	public boolean isBitmapDirty = false;
    	public boolean isUpdated = false;
		@Override
		public void run() {
			//  Auto-generated method stub
		while(true) {
			
			try {
				Thread.sleep(3);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		}
    	
    }
    */
}
