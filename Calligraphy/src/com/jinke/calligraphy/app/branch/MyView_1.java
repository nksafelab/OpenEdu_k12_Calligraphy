package com.jinke.calligraphy.app.branch;
//package hallelujah.app;
//
//import android.content.Context;
//import android.graphics.Bitmap;
//import android.graphics.Canvas;
//import android.graphics.Paint;
//import android.graphics.Rect;
//import android.util.AttributeSet;
//import android.util.Log;
//import android.view.MotionEvent;
//import android.view.SurfaceHolder;
//import android.view.SurfaceView;
//
//
//public class MyView extends SurfaceView implements SurfaceHolder.Callback{
//    
//	private static final String TAG = "MyView";
//	
//	private static final float MINP = 0.25f;
//    private static final float MAXP = 0.75f;
//    
////    private Bitmap  mBitmap, mLeftBitmap, mRightBitmap, mTmpBitmap;
//
//    public boolean 			hasTouch = false;
//    public static 	int		statusBarHeight = 0;
//
//    private Bitmap	mBitmap;
//    private Bitmap 	mScreenLayerBitmap;
//
//    public BasePointsImpl	baseImpl;
//    public CalliPointsImpl	calliImpl;
//    public HardPointsImpl	hardImpl;
//    
//    public BaseBitmap		baseBitmap;
//    public FreeDrawBitmap	freeBitmap;
//    public CursorDrawBitmap	cursorBitmap;
//    
//    public static int STATUS_PEN_CALLI 	= 0;   //毛笔状态
//    public static int STATUS_PEN_HARD  	= 1;   //硬笔状态
//	public static int penStatus = STATUS_PEN_CALLI;  //默认为毛笔
////	public static int penStatus = STATUS_PEN_HARD;    //默认为硬笔
//    
//    public static int STATUS_DRAW_FREE		= 0;  //涂鸦态 
//    public static int STATUS_DRAW_CURSOR	= 1;  //光标态
//	public static int drawStatus = STATUS_DRAW_FREE;  	//默认为涂鸦态
////    public static int drawStatus = STATUS_DRAW_CURSOR;  //默认为光标态
//    
////    public static boolean 	drawStatusChanged = false;
////    public static boolean 	penStatusChanged = false;
//    
//    public SurfaceHolder	mHolder;
//    private boolean 		mHasSurface;
//
//    public void setBitmap(Bitmap bitmap){
//    	this.mBitmap = bitmap;
//    }
//
//    
//    public MyView(Context c, Bitmap bp, Bitmap layer){
//
//    	super(c);
//    	
//    	mHolder = getHolder();
//    	Log.i(TAG, "getHolder");
//    	
//    	mHolder.addCallback(this);
//    	mHasSurface = false;
//    	
//    	
//    	mBitmap = bp;
//    	mScreenLayerBitmap = layer;
//
//    	//初始化绘画风格
//    	freeBitmap = new FreeDrawBitmap(bp, this);
//    	cursorBitmap = new CursorDrawBitmap(bp, this);
//    	cursorBitmap.setDrawListener(new DrawStartEndListener());
//    	
//    	if(drawStatus == STATUS_DRAW_FREE) {
//    		baseBitmap = freeBitmap;
//    	} else {
//    		baseBitmap = cursorBitmap;
//    	}
//    	
//		//初始化画笔状态
//		calliImpl = new CalliPointsImpl(baseBitmap, this);
//		hardImpl = new HardPointsImpl(baseBitmap, this);
//
//    	if(penStatus == STATUS_PEN_CALLI)
//    		baseImpl = calliImpl;
//    	else
//    		baseImpl = hardImpl;
//    	
//    	this.draw();
////    	baseImpl.setView(this);
////    	baseImpl.setBitmap(baseBitmap);
//    }
//    
////	public MyView(Context context, AttributeSet attrs) {
////		super(context, attrs);
////		// TODO Auto-generated constructor stub
////	}
//	
//	public void changeDrawState(int draw){
//		drawStatus = draw;
//		if(drawStatus == STATUS_DRAW_FREE)
//    		baseBitmap = freeBitmap;
//    	else
//    		baseBitmap = cursorBitmap;
//		
//		/*
//		 * 底层绘制的bitmap状态改变后，每一个相应的实现都需要更新
//		 */
//		calliImpl.updateBitmap();
//		hardImpl.updateBitmap();
//		
//		print();
////		invalidate();
//		draw();
//	}
//	
//	public void changePenState(int pen){
//		penStatus = pen;
//		if(penStatus == STATUS_PEN_CALLI)
//    		baseImpl = calliImpl;
//    	else
//    		baseImpl = hardImpl;
//    	print();
////    	invalidate();
//    	draw();
//	}
//	
//	public void changeState(int pen, int draw) {
//		penStatus = pen;
//		drawStatus = draw;
//		if(drawStatus == STATUS_DRAW_FREE)
//    		baseBitmap = freeBitmap;
//    	else
//    		baseBitmap = cursorBitmap;
//    	if(penStatus == STATUS_PEN_CALLI)
//    		baseImpl = calliImpl;
//    	else
//    		baseImpl = hardImpl;
////    	invalidate();
//    	draw();
//	}
//	
//	public void destroy(){
//		freeBitmap.bitmap.recycle();
//		cursorBitmap.bitmap.recycle();
//	}
//	
//	/*
//    @Override
//    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
//	    super.onSizeChanged(w, h, oldw, oldh);
//        Log.i(TAG, "onsize" + h + "oldh" + oldh);
//        
//	    if (0 == oldh) {
//	    	Log.d(TAG, "onsiz 1");
//		    statusBarHeight  = Start.SCREEN_HEIGHT - h;
//		    updateScreenLayer();
//		    invalidate();
////		    draw();
//		    return;
//	    }
//
//	    if (h < Start.SCREEN_HEIGHT && Start.SCREEN_HEIGHT == oldh) {
//	    	Log.d(TAG, "onsiz 2");
//		    statusBarHeight  = Start.SCREEN_HEIGHT - h;
//		    updateScreenLayer();
//		    invalidate();
////		    draw();
//		    return;
//	    }
//
//	    if (h == Start.SCREEN_HEIGHT && Start.SCREEN_HEIGHT > oldh) {
//	    	Log.d(TAG, "onsiz 3");
//		    statusBarHeight  = 0;
//		    updateScreenLayer();
//		    invalidate();
////		    draw();
//		    return;
//	    }
//
//    }
//    */
//    
//    public void updateScreenLayer(){
//    	Canvas canvas = new Canvas();
//    	canvas.setBitmap(mBitmap);
//    	canvas.drawBitmap(mScreenLayerBitmap, new Rect(0, statusBarHeight, Start.SCREEN_WIDTH, Start.SCREEN_HEIGHT), 
//    			new Rect(0, 0, Start.SCREEN_WIDTH, Start.SCREEN_HEIGHT-statusBarHeight),new Paint());
//    }
//    
////    @Override
////    protected void onDraw(Canvas canvas) {
////    	Log.i(TAG, "onDraw");
////        canvas.drawColor(0xFFAAAAAA);
////        baseImpl.draw(canvas);
////    }
//    
////	@Override
////	public void invalidate() {
////		// TODO Auto-generated method stub
////    	Log.i(TAG, "invalidate");
////		super.invalidate();
////		this.draw();
////	}
//
//
//	public void draw(){
//		
////    	Log.i(TAG, (mHolder == null) ? "NULL" : "NOT NULL");
//    	
//    	if(mHolder != null) {
//    		Canvas canvas = mHolder.lockCanvas(new Rect(0,0,Start.SCREEN_WIDTH,Start.SCREEN_HEIGHT));
////        	Log.i(TAG, (canvas == null) ? "NULL" : "NOT NULL");
//    		if(canvas != null) {
//            	//canvas.drawColor(0xFFAAAAAA);
//            	baseImpl.draw(canvas);
//            	mHolder.unlockCanvasAndPost(canvas);
//    		}
//    	}
//    	
//    }
//
//
//	private void touch_start(float x, float y) {
//    	Log.i(TAG, "start");
//    	baseImpl.start(x, y);
//    }
//    
//    private void touch_move(float x, float y) {
//    	baseImpl.makeNextPoint(x, y);
//    }
//    
//    private void touch_up() {
//    	baseImpl.after();
//    }
//
//	@Override
//    public boolean onTouchEvent(MotionEvent event) {
//        float x = event.getX();
//        float y = event.getY();
//        
//        switch (event.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//                touch_start(x, y);
//                break;
//            case MotionEvent.ACTION_MOVE:
//                touch_move(x, y);
//                break;
//            case MotionEvent.ACTION_UP:
//                touch_up();
//                break;
//        }
//        return true;
//    }
//	
//	public void print(){
//		Log.i(TAG, "drawStatus:" + drawStatus);
//		Log.i(TAG, "penStatus:" + penStatus);
//	}
//
//
//	@Override
//	public void surfaceChanged(SurfaceHolder holder, int format, int width,
//			int height) {
//		// TODO Auto-generated method stub
//		
//	}
//
//
//	@Override
//	public void surfaceCreated(SurfaceHolder holder) {
//		// TODO Auto-generated method stub
//		Log.i(TAG, "surface created");
//		Canvas canvas = mHolder.lockCanvas(new Rect(0,0,Start.SCREEN_WIDTH,Start.SCREEN_HEIGHT));
//        canvas.drawColor(0xFFAAAAAA);
//        baseImpl.draw(canvas);
//        mHolder.unlockCanvasAndPost(canvas);
//	}
//
//
//	@Override
//	public void surfaceDestroyed(SurfaceHolder holder) {
//		// TODO Auto-generated method stub
//		Log.i(TAG, "surface destroyed");
//	}
//	
//}
