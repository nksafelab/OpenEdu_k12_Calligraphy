package com.jinke.calligraphy.app.branch;

import hallelujah.cal.CalligraphyVectorUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.jinke.calligraphy.app.branch.EditableCalligraphyItem.Types;
import com.jinke.calligraphy.database.CDBPersistent;
import com.jinke.calligraphy.template.Available;
import com.jinke.calligraphy.template.WolfTemplate;
import com.jinke.calligraphy.template.WolfTemplateUtil;
import com.jinke.single.BitmapCount;
import com.jinke.single.LogUtil;

/*
 * 光标态，还有一个透明层bitmap，以及转换为小图的功能
 */
public class CursorDrawBitmap extends BaseBitmap{
    
//	static {
//		System.loadLibrary("pdc_prs");
//	}
	
	private static final String TAG = "CursorDrawBitmap";
	
    public	Bitmap 	mBitmap;    //保持对父类全局变量的引用
    public 	Bitmap 	bitmap;		//保持对父类中子类独立的变量bitmap的引用
    public  Bitmap mSmallBitmap;
    public 	Paint 	paint;		//保持对父类画笔对象的引用
    public 	Timer  	timer;

    public  static  long 	millisInFuture = ParametersDialog.DEFAULT_ONSCREEN_TIME;
    public  static  long 	countDownInterval = millisInFuture;
    
    private int 	startX,startY,endX,endY;
    private int 	mWidth,mHeight;
    private Matrix  mMatrix;
    
    private Matrix  curMatrix;
    private static float mFixedHeight;//字体高度
    private static float mFixedWidth;//字体宽度，用于竖向书写
    
    
    public  final static float mMinHeight = 20.0f;
    public  final static float mMinWidth = 20.0f;
    public  final static float mMaxWidth = 100.0f;
    public  final static float mMaxHeight = 200.0f;
    
    public  static float	mIntervalHeight;
    private float	mHScale, mVScale;
    
    private ArrayList<RectF> rectsList;
    private float 	sTop, sBottom;
    
    public 	EditableCalligraphy 	cal_current;
    public 	EditableCalligraphy 	cal_title;
    public 	EditableCalligraphy 	cal_content;
    
    public static List<EditableCalligraphy> listEditableCalligraphy;
    private Runnable cursorRunnable;
    public Rect rec_title;
    public Rect rec_content;
    public WolfTemplate mTemplate;
    
    public int flag = -1;
    int count = 1000;
//    public 	DrawListener			drawListener;
    public Thread cursorThread;
    
    public boolean exit = false;
    private boolean jump = true;
 
    public boolean isPenDown = false;
    
    public boolean picFlag = false;//防止选择图片的时候，光标移动
    
    public boolean wordFinish = true;
    
    public boolean endofLine = true;
    
    public void setBbitmap(Bitmap b){
    	Canvas c = new Canvas();
    	c.setBitmap(bitmap);
    	c.drawBitmap(b, 0, 0, new Paint());
//    	bitmap = b;
    }
    public void setJump(){
    	jump = true;
    }
    public void setNotJump(){
    	jump = false;
    }
	
	public CursorDrawBitmap(Bitmap b, MyView v){
		super(b, v);		
		
		mBitmap = super.mBitmap;
		if(super.bitmap == null) {
			//super.bitmap = Bitmap.createBitmap(Start.SCREEN_WIDTH, Start.SCREEN_HEIGHT, Bitmap.Config.ARGB_8888);
			super.bitmap = Bitmap.createBitmap(Start.SCREEN_WIDTH, Start.SCREEN_HEIGHT, Bitmap.Config.ARGB_4444);
			BitmapCount.getInstance().createBitmap("CusorDrawBitmap bitmap");
		}
		bitmap = super.bitmap;
		paint = super.paint;   //此处画笔还未赋值，当具体点(硬笔/毛笔)构造时，会给此赋值
		
		initListEditableCalligraphy(v.getTemplate());
		
		cursorRunnable = new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				
				while(!exit){
					
					if(jump){
						try {
							cal_current.drawCurrentCursor(Color.BLACK);
							Thread.sleep(400);
							cal_current.drawCurrentCursor(Color.WHITE);
							Thread.sleep(400);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}else{
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						
					}
					
					
				}//endwhile
//				return;
			}
		};
		
		cursorThread = new Thread(cursorRunnable);
		cursorThread.start();
		
		
		bCurInfo = new CurInfo(mBitmap, -Start.SCREEN_WIDTH);
		
		//初始化光标态底图bitmap
		Canvas canvas = new Canvas();
		canvas.setBitmap(bitmap);
		canvas.drawBitmap(mBitmap, new Rect(Start.SCREEN_WIDTH, 0, Start.SCREEN_WIDTH * 2, Start.SCREEN_HEIGHT), new Rect(0, 0, Start.SCREEN_WIDTH, Start.SCREEN_HEIGHT), new Paint());


		//恢复之前保存的Timer参数值(如果有的话)
    	SharedPreferences settings = v.getContext().getSharedPreferences(ParametersDialog.FILENAME,  android.content.Context.MODE_PRIVATE);
		int progress = settings.getInt(ParametersDialog.PARAM_ONTO_SCREEN_TIME, -1);
		if (progress != -1) {
			millisInFuture = progress * ParametersDialog.ontoScreenTimeFactor;
			countDownInterval = millisInFuture;
		}
		timer = new Timer(millisInFuture, countDownInterval);
		
		mMatrix = new Matrix();
		mHScale = 0.01f;
		mVScale = 0.01f;

		
		//ly
		//据说是字体高度,我修改了一下里面的值
		mFixedWidth = mFixedHeight = 200.0f;
		//end
		
		mIntervalHeight = mTemplate.getLinespace();

		resetCursor();
		
		if(WolfTemplateUtil.getCurrentTemplate().getFormat() == 0){
			return;
		}
		cal_current = listEditableCalligraphy.get(0);
		Log.e("which", "==================000000000000000000");
		cal_current.setBitmapOffset(-Start.SCREEN_WIDTH, 0);
		
		//ly
		//没看出cal_title这cal_content这些有什么用
		cal_title = new EditableCalligraphy();
		cal_title.setBitmapOffset(-Start.SCREEN_WIDTH, 0);
		rec_title = new Rect(200, 70, Start.SCREEN_WIDTH, 170);
		
		cal_content = new EditableCalligraphy();
		cal_content.setBitmapOffset(-Start.SCREEN_WIDTH, 0);
		rec_content = new Rect(0, 500, Start.SCREEN_WIDTH, Start.SCREEN_HEIGHT);

		rectsList = new ArrayList<RectF>();
	}
	
	public void setFixedHeight(float mFixedHeight){
		mFixedWidth = this.mFixedHeight = mFixedHeight;
	}
	
	public void initListEditableCalligraphy(WolfTemplate wt) {
		
		Log.e("create", "------initListEditableCalligraphy CursorDrawBitmap");
		CalligraphyVectorUtil.initParsedWordList(Start.getPageNum()); //当前不用初始化
		
		Log.e("date", "init Editable from database template" + wt.getName());
		// TODO Auto-generated method stub
		this.mTemplate = wt;
		listEditableCalligraphy = new ArrayList<EditableCalligraphy>();
		List<Available> la = wt.getAvailables();
		Available a;
		
		if(la != null){
			for(int i=0;i<la.size();i++){
				EditableCalligraphy ec;
				a = la.get(i);
				
				if("date".equals(a.getControltype())){
					Log.e("date", "draw date x:"+a.getStartX()+" y:"+a.getStartY());
					initDate(a);
					a.setEditable(false);
				}else{
					a.setEditable(true);
				}
				ec = new EditableCalligraphy(i,a,super.bView);
				ec.selected = false;
				listEditableCalligraphy.add(ec);
				for(int j=0;j<listEditableCalligraphy.size();j++){
					Log.e("matrix", "init:== "+ listEditableCalligraphy.get(j).getID());
					Log.e("matrix", "init: "+ listEditableCalligraphy.get(j).getAvailable().getAid());
					Log.e("matrix", "init::::: "+ la.get(j).getAid());
				}
				Log.e("vectorr", "init editable ->>>>>>>>>>>>>>>" + a.getAid());
			}
			if(listEditableCalligraphy.get(0).getAvailable().isEditable()){
				cal_current = listEditableCalligraphy.get(0);//首个是日期的时候，不可写，直接跳到下个编辑区域
				Log.e("which", "=========initEdit=========000000000000000000");
			}
			else
				cal_current = listEditableCalligraphy.get(1);
		}//end if 
		
//		for(int i=0;i<listEditableCalligraphy.size();i++){
//			ec = listEditableCalligraphy.get(i);
//			a = ec.getAvailable();
//			System.out.println("=======================EditableCalligraphy id:"+ec.getID());
//			System.out.println("	startX:"+a.getStartX());
//			System.out.println("	startY:"+a.getStartY());
//			System.out.println("	endX:"+a.getEndX());
//			System.out.println("	endY:"+a.getEndY());
//			
//			
//			
//		}
//		updateHandwriteState();
	}
public void initDate(WolfTemplate wt) {
		
		// TODO Auto-generated method stub
		Log.e("date", "init date from template: " + wt.getName());
		List<Available> la = wt.getAvailables();
		Available a;
		if(la != null){
			for(int i=0;i<la.size();i++){
				a = la.get(i);
				if("date".equals(a.getControltype())){
					initDate(a);
				}
				
			}
			if(cal_current == null)
				cal_current = listEditableCalligraphy.get(0); 
			//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!莫名的重绘，然后改变可写区，不知到为什么
			//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!莫名的重绘，然后改变可写区，不知到为什么
			//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!莫名的重绘，然后改变可写区，不知到为什么
			//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!莫名的重绘，然后改变可写区，不知到为什么
			//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!莫名的重绘，然后改变可写区，不知到为什么
			
			Log.e("which", "========init==========000000000000000000");
		}//end if 
		
//		updateHandwriteState();
	}
	
	
	private void initDate(Available a){
		Canvas date_canvas = new Canvas();
		
		
		date_canvas.setBitmap(mBitmap);
//		date_canvas.translate(Start.SCREEN_WIDTH, 0);
		Paint datePaint = new Paint();
		datePaint.setTextSize(a.getAfontsize());
		// 
//		Calendar c = Calendar.getInstance();
//		String date = c.get(Calendar.YEAR)%100 + "/" + (c.get(Calendar.MONTH)+1) + "/" + c.get(Calendar.DAY_OF_MONTH) + "  "+c.get(Calendar.HOUR)+":"+c.get(Calendar.MINUTE)+"";
		String date = "";
		CDBPersistent db = new CDBPersistent(Start.context);
		db.open();
		date = db.getDateByPage();
		db.close();
		Log.e("date", "date from database:"+date);
		
		if(TextUtils.isEmpty(date)){
			date = Start.getDate();
			Log.e("date", "date from database is null get:" + date);
		}
		
		float pad = (a.getAlinespace() - a.getAfontsize())/2; 
		Log.e("date", "startx:"+a.getStartX() + " starty:"+(a.getStartY()+a.getAlinespace()-pad));
		Log.e("date", "drawText:" + date);
		
		Rect r = new Rect(a.getStartX()+Start.SCREEN_WIDTH, a.getStartY(),
				Start.SCREEN_WIDTH * 2, (int) (a.getStartY()+a.getAlinespace()-pad));
		Paint p = new Paint();
		p.setColor(Color.WHITE);
		date_canvas.drawRect(r, p);
		
		date_canvas.drawText(date, a.getStartX()+Start.SCREEN_WIDTH, a.getStartY()+a.getAlinespace()-pad, datePaint);
		
		date_canvas.setBitmap(bitmap);

		
		r = new Rect(a.getStartX(), a.getStartY(),
				Start.SCREEN_WIDTH, (int) (a.getStartY()+a.getAlinespace()-pad));
		date_canvas.drawRect(r, p);
		date_canvas.drawText(date, a.getStartX(), a.getStartY()+a.getAlinespace()-pad, datePaint);
		
	}
	

	public void setListEditableCalligraphy(List<EditableCalligraphy> l){
		this.listEditableCalligraphy = l;
	}
	
//	public void setDrawListener(DrawListener l){
//		this.drawListener = l;
//	}
	
	@Override
	public void doDraw(Canvas canvas, Paint paint, Matrix matrix) {
		// TODO Auto-generated method stub
		super.doDraw(canvas, paint, matrix);
		this.curMatrix = matrix;
//		Log.e("vector", ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>doDraw curMatrix" + (curMatrix == null)); null
	}
	
	 

	@Override
	public void start(float x, float y) {
		// TODO Auto-generated method stub
		
		super.start(x, y);
		
		if(wordFinish){
			//上一个word已经结束。产生新word
			Log.e("vector", "wordFinish, new word");
//			CalligraphyVectorUtil.instance();
			
			CalligraphyVectorUtil.instance().createWord(
					Start.getPageNum(),
					Start.c.view.cursorBitmap.cal_current.getID(),
					Start.c.view.cursorBitmap.cal_current.currentpos
					);
			Log.e("vector", "create word finish");
			
			CalligraphyVectorUtil.instance().start(x,y);
			
		}else{
			//word尚未结束，产生新的stroke
			Log.e("vector", "wordFinish false, new word");
			CalligraphyVectorUtil.instance().start(x,y);
		}
		wordFinish = false;
		
//		drawListener.start();
		timer.cancel();
		
		
		setNotJump();
		mMatrix.reset();
	}
	
	@Override
	public void move(float x, float y) {
		// TODO Auto-generated method stub
		
		super.move(x, y);
		
		CalligraphyVectorUtil.instance().move(x,y);
		
	}

	/*
	 * touch up(non-Javadoc)
	 * @see hallelujah.app.BaseBitmap#after(float, float)
	 */
	@Override
	public void after() {
		// TODO Auto-generated method stub
		
		super.after();
		timer.start();
		
		CalligraphyVectorUtil.instance().end();
		
		rectsList.add(new RectF(sStartX, sStartY, sEndX, sEndY));
		System.out.println("timer start");
		isPenDown = false;
	}


	@Override
	public void finish() {
		
		// TODO Auto-generated method stub
		super.finish();
		System.out.println("@@@@@@@@@@@@@@@@cursorDrawBitmap finish");
		transparentBitmap.recycle();
		BitmapCount.getInstance().recycleBitmap("CursorDrawBitmap finish transparentBitmap");
	}
	
	
	
	@Override
	public boolean forceUpdateTopBitmap() {
		// TODO Auto-generated method stub
//		if(MyView.penStatus == MyView.STATUS_PEN_CALLI )
//			return true;
		return super.forceUpdateTopBitmap();
	}

	@Override
	public Bitmap getBaseBitmap() {
		// TODO Auto-generated method stub
		return super.getBaseBitmap();
	}

	@Override
	public Bitmap getTopBitmap() {
		// TODO Auto-generated method stub
		return transparentBitmap;
	}


    @Override
	public void clearDataBitmap() {
		// TODO Auto-generated method stub
		super.clearDataBitmap();
		Log.i(TAG, ">>>>>>>>>>>>>>>>>>>>>>>>>clearDataBitmap");
		
		for(int i=0;i<listEditableCalligraphy.size();i++)
			listEditableCalligraphy.get(i).clear();
		
//		cal_current.clear();
		
		//ly
		//清理思维导图
		if(!mBitmap.isRecycled())
			mBitmap.eraseColor(Color.WHITE);
		//end
		
	}
   
    
    public LinkedList<EditableCalligraphyItem> getCharList(){
    	return cal_current.getCharsList();
    }
    
    public void delCharacter(){
    	endofLine = false;
    	Log.i(TAG, "del");
    	cal_current.delete();
//    	Log.i(TAG, "SIZE:" + cal_current.getSize() + " CUR:" + cal_current.currentpos);
    	updateHandwriteState();
    }
    
    public void backward(){
    	cal_current.backward();
    	Log.i(TAG, "SIZE:" + cal_current.getSize() + " CUR:" + cal_current.currentpos);
    	updateHandwriteState();
    }
    
    public void forward(){
    	cal_current.forward();
    	updateHandwriteState();
    }
    
    public void insertSpace(){
    	endofLine = false;
    	cal_current.insertSpace();
    	Log.i(TAG, "SIZE:" + cal_current.getSize() + " CUR:" + cal_current.currentpos);
    	updateHandwriteStateFlip();
    }
    public void insertEnSpace(){
    	endofLine = false;
    	cal_current.insertEnSpace();
    	Log.i(TAG, "SIZE:" + cal_current.getSize() + " CUR:" + cal_current.currentpos);
    	updateHandwriteStateFlip();
    }
    
    public void addNewMindMap(){
    	if(cal_current.getID() == 3){
    		cal_current.addNewMindMapItem();
    		updateHandwriteStateFlip();
    	}else{
    		Toast.makeText(Start.context, "正文区才可以插入翰林算子", Toast.LENGTH_LONG).show();
    	}
    	
    }
    
    public void insertEndOfLine(){
    	endofLine = true;
    	if(cal_current.linenumber == 1){
    		//只有一行  进入下一编辑区域
    		System.out.println("current id:"+cal_current.getID());
    		System.out.println("list size:"+listEditableCalligraphy.size());
    		if(cal_current.getID() <=  listEditableCalligraphy.size()-1){
    			cal_current = listEditableCalligraphy.get(cal_current.getID()+1);
    		}
    	}else{
    		//添加换行符
    		cal_current.insertEndofLine();
    	}
    	Log.i(TAG, "SIZE:" + cal_current.getSize() + " CUR:" + cal_current.currentpos);
    	
    	PreUpdateHandwriteStateFlip();
    	cal_current.setFlipDst(true,"insertEndOfLine");
    	updateHandwriteStateFlip();
    	
    	
    	Log.e("mindend", "insert end of line");
    }
    public void endInsertOfLine(){
    	if(cal_current.currentpos == cal_current.charList.size())
    		insertEndOfLine();
//    	else if(cal_current.currentpos < cal_current.charList.size()){
//    		if(cal_current.charList.get(cal_current.currentpos + 1).type != Types.EndofLine)
//    			insertEndOfLine();
//    	}
    	else{
	    	cal_current.end();
	    	Log.e("mindend", "forward");
	    	PreUpdateHandwriteStateFlip();
	    	cal_current.setFlipDst(true,"insertEndOfLine");
	    	updateHandwriteStateFlip();
    	}
    }
    public void endMindmapEdit(){
    	Log.e("mindend", "cal_current.currentpos:" + cal_current.currentpos + " size:" + cal_current.charList.size());
    	cal_current.destroyCurrentMindmapItem();
    }

	@Override
	public void setBgBitmap() {
		// TODO Auto-generated method stub
		super.setBgBitmap();
	}

	
	
	private void resetCursor(){
		mWidth 	= Start.SCREEN_WIDTH;
		mHeight	= 70;
	}
	

	/**
	 * 不动态释放的更新显示方法
	 */
	public void updateHandwriteState() {
		// TODO Auto-generated method stub
		super.updateState();
		//更新手写区域
		Canvas canvas = new Canvas();
		canvas.setBitmap(mBitmap);
		

		//bitmap.eraseColor(Color.WHITE);
		
		
		canvas.drawBitmap(bitmap, Start.SCREEN_WIDTH, 0, paint);//重绘底图

//		for(int i=0;i<listEditableCalligraphy.size();i++){
		Log.v("flipper", " updateHandwriteState!! ");
		
		//ly
		//注释掉，不让涂鸦态内容显示到编辑态
		//super.drawFreeBitmap();
		
		
		bView.resetRowNumber();
		for(int i=listEditableCalligraphy.size()-1;i>=0;i--){
//			listEditableCalligraphy.get(i).dispearPreCursor();
			listEditableCalligraphy.get(i).update(mBitmap,false);
		}
		bView.invalidate();
	}
	
	/**
	 * 先排版
	 */
	public void PreUpdateHandwriteStateFlip() {
		// TODO Auto-generated method stub
		super.updateState();
		//更新手写区域
		for(int i=listEditableCalligraphy.size()-1;i>=0;i--){
			listEditableCalligraphy.get(i).preUpdate(mBitmap, true);
		}
	}
	
	/**
	 * 动态释放的更新显示方法
	 */
	public void updateHandwriteStateFlip() {
		// TODO Auto-generated method stub
		super.updateState();
		//更新手写区域
		Canvas canvas = new Canvas();
		canvas.setBitmap(mBitmap);
		
		
		canvas.drawBitmap(bitmap, Start.SCREEN_WIDTH, 0, paint);//重绘底图

//		for(int i=0;i<listEditableCalligraphy.size();i++){
		Log.v("flipper", " updateHandwriteState!! ");
		
		//ly
		//注释掉，不让涂鸦态内容显示到编辑态
		//super.drawFreeBitmap();
		
		
		bView.resetRowNumber();
		for(int i=listEditableCalligraphy.size()-1;i>=0;i--){
//			listEditableCalligraphy.get(i).dispearPreCursor();
			listEditableCalligraphy.get(i).update(mBitmap,true);
			
		}
		bView.invalidate();
	}

	/**
	 * 动态释放的更新显示方法
	 */
	public void recycleOutScreenBitmap() {
		// TODO Auto-generated method stub
		for(int i=listEditableCalligraphy.size()-1;i>=0;i--){
			listEditableCalligraphy.get(i).startRecycleInVisiableBitmap();
		}
	}
	public void updateTransparent() {
        //除了要将透明层更新在不同的bitmap上之外，
        //还需要更新bitmap，因为每次光标态重排
        //小字时都用bitmap来刷底图
        if(TransparentChanged) {
        	Log.i(TAG, "TransparentChanged");
        	
        	Canvas c = new Canvas();
        	
        	if(EditableCalligraphy.flip_dst <= Start.SCREEN_HEIGHT) {
            	c.setBitmap(bView.mBitmap);
            	c.drawBitmap(transparentBitmap, new Rect(0, 0, Start.SCREEN_WIDTH, Start.SCREEN_HEIGHT - EditableCalligraphy.flip_dst),
                                    new Rect(0, EditableCalligraphy.flip_dst, Start.SCREEN_WIDTH, Start.SCREEN_HEIGHT), new Paint());
            	c.drawBitmap(transparentBitmap, new Rect(0, 0, Start.SCREEN_WIDTH, Start.SCREEN_HEIGHT - EditableCalligraphy.flip_dst),
                        new Rect(Start.SCREEN_WIDTH, EditableCalligraphy.flip_dst, Start.SCREEN_WIDTH * 2, Start.SCREEN_HEIGHT), new Paint());
            	c.setBitmap(bitmap);
            	c.drawBitmap(transparentBitmap, new Rect(0, 0, Start.SCREEN_WIDTH, Start.SCREEN_HEIGHT - EditableCalligraphy.flip_dst),
                                    new Rect(0, EditableCalligraphy.flip_dst, Start.SCREEN_WIDTH, Start.SCREEN_HEIGHT), new Paint());
            	
            	c.setBitmap(cleanBitmap1);
            	c.drawBitmap(transparentBitmap, new Rect(0, Start.SCREEN_HEIGHT - EditableCalligraphy.flip_dst, Start.SCREEN_WIDTH, Start.SCREEN_HEIGHT),
            	      new Rect(0, 0, Start.SCREEN_WIDTH, EditableCalligraphy.flip_dst), new Paint());
            	
        	} else {
            	c.setBitmap(cleanBitmap1);
            	c.drawBitmap(transparentBitmap, new Rect(0, 0, Start.SCREEN_WIDTH, Start.SCREEN_HEIGHT - (EditableCalligraphy.flip_dst - Start.SCREEN_HEIGHT)),
            	     new Rect(0, EditableCalligraphy.flip_dst - Start.SCREEN_HEIGHT, Start.SCREEN_WIDTH, Start.SCREEN_HEIGHT), new Paint());
            	c.setBitmap(cleanBitmap2);
            	c.drawBitmap(transparentBitmap, new Rect(0, Start.SCREEN_HEIGHT - (EditableCalligraphy.flip_dst - Start.SCREEN_HEIGHT), Start.SCREEN_WIDTH, Start.SCREEN_HEIGHT),
            			new Rect(0, 0, Start.SCREEN_WIDTH, EditableCalligraphy.flip_dst - Start.SCREEN_HEIGHT), new Paint());
        	
        	}
        	transparentBitmap.eraseColor(Color.TRANSPARENT);
        	TransparentChanged = false;
        }
    }



       
	class Timer extends CountDownTimer {

		float values[] = new float[9];
		public Timer(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
			// TODO Auto-generated constructor stub
		}
		public void getScaled(float distX, float distY){
			if(cal_current.getAvailable().getDirect() != 1){
				
				if(cal_current.getAvailable().getAfontsize() != 0){
					mFixedHeight = cal_current.getAvailable().getAfontsize();
				}
				
				if(distY <= mFixedHeight) {
					if(distX <= mMaxWidth)
						mVScale = mHScale = 1.0f;
					else {
						mVScale = mHScale = mMaxWidth / distX;
					}
				}//处理横线状字体 
				else {
					mVScale = mHScale = mFixedHeight / (mEndY - mStartY);
					
					if(distX * mHScale > mMaxWidth)
						mVScale = mHScale = mMaxWidth / distX;
				}
				//----------------------------------------------------------------
				
				if(distY < mMaxHeight)
					mVScale = mHScale = mFixedHeight / mMaxHeight;
				else 
					mVScale = mHScale = mFixedHeight / distY;
				
//				Log.e("pathscale", "cursor scale------------->>>>>>>>>>>distX:" + distX + " " +
//						"distY:" + distY + " " +
//								"mFixedHeight:" + mFixedHeight + "" +
//										"mMaxHeight:" + mMaxHeight + " " +
//												"Scale:" + mVScale);
				
			}else{
				//竖屏
				
				if(cal_current.getAvailable().getAfontsize() != 0){
					mFixedWidth = cal_current.getAvailable().getAfontsize();//等宽
				}
				
				//distX = distX = endX - startX;
				if(distX <= mFixedWidth) {
					Log.e("scale", "distx < mFixedWidth"+ "distx:"+ distX + " mFixedWidth:"+ mFixedWidth);
					if(distY <= mMaxHeight){
						Log.e("scale", " distY <= mMaxHeight"+ "distY:"+ distY + " mMaxHeight:"+ mMaxHeight);
						mVScale = mHScale = 1.0f;
					}else {
						mVScale = mHScale = mMaxHeight / distY;
						Log.e("scale", " else distY <= mMaxHeight"+ "distY:"+ distY + " mMaxHeight:"+ mMaxHeight);
					}
				} else {
					//字变大
					
					//mFixedWidth 字宽
					
					mVScale = mHScale = mFixedWidth / (mEndX - mStartX);
					
					Log.e("scale", "else distX <= mFixedWidth"
							+ "distY:"+ distY + " mMaxHeight:"+ mMaxHeight);
					
					
					if(distY * mHScale > mMaxHeight){
						mVScale = mHScale = mMaxHeight / distY;
						Log.e("scale", "distY * mHScale > mMaxHeight"+ "distY:"+ distY + " distY:"+ distY + "mMaxHeight"+mMaxHeight);
					}
					
				}
				
				
				
				if(distX < mMaxWidth)
					mVScale = mHScale = mFixedWidth / mMaxWidth;
				else 
					mVScale = mHScale = mFixedWidth / distX;
				
			}
       }
		
		@Override
		public void onFinish() {
			

//			drawListener.end();
			//让点集实现类实现相关的操作
			bView.baseImpl.clear();
			/*
			Log.i(TAG, "--------------original---------------");
            printRectFs();

            Collections.sort(rectsList, new topComparator());
            Log.i(TAG, "--------------top---------------");
            printRectFs();
            sTop = getTop2();

            Collections.sort(rectsList, new bottomComparator());
            Log.i(TAG, "--------------bottom---------------");
            printRectFs();
            sBottom = getBottom2();
			 */
            rectsList.clear();

			if((mStartY> mEndY) ||(mStartX > mEndX))
				return;
			
			/*
			startX = (int)(mStartX);
			startY = (int)(mStartY);
			endX = (int)(mEndX);
			endY = (int)(mEndY);
			*/
			
			startX = (int)(mStartX - 1);
			startY = (int)(mStartY - 1);
			endX = (int)(mEndX + 1);
			endY = (int)(mEndY + 1);
			
			startX = startX < 0 ? 0 : startX;
			startY = startY < 0 ? 0 : startY;
			
			if(endX <= startX || endY <= startY)
				return;
			
			//mVScale = mHScale = mFixedHeight / (mEndY - mStartY);
			float distX = endX - startX;
//			/*
//             * 暂时修改
            float distY = endY - startY;
//             */
//            float distY = sBottom - sTop;
//			Log.e("pathscale", "distx:" + distX + " distY:" + distY);
			getScaled(distX, distY); //判断当前是否是竖屏
			
			
			if(curMatrix != null) {
//				Log.i("vector", ">>>>>>>>>mHScale3:" + mHScale + " mVScale3:" + mVScale);
				curMatrix.getValues(values);
				mHScale *= values[0];
				mVScale *= values[4];
			}
//			Log.i(TAG, "mHScale3:" + mHScale + " mVScale3:" + mVScale);
			
			Canvas canvas = new Canvas();
			canvas.setBitmap(mBitmap);

			//startX = startX<0 ? 0 : startX;
			//startY = startY<0 ? 0 : startY; 
			Log.e("createword", "endX - startX:" + (endX - startX) + " endy-starty:" + (endY - startY));
			if(endX-startX < mMinWidth || endY - startY < mMinHeight){
				
				if(picFlag){
					
				}else{
					
					CalligraphyVectorUtil.instance().finish(false,cal_current.currentpos);//不是字
					wordFinish = true;
					
					//canvas.drawBitmap(bitmap, new Rect(300, 0, Start.SCREEN_WIDTH, Start.SCREEN_HEIGHT), new Rect(Start.SCREEN_WIDTH, 0, 900, Start.SCREEN_HEIGHT), paint);
					
					
					
	//				cal_current.update(mBitmap);
					
//					for(int i=0;i<listEditableCalligraphy.size();i++){
////						listEditableCalligraphy.get(i).update(mBitmap,bView.mmMatrix); vectorScale
//						listEditableCalligraphy.get(i).update(mBitmap);
//					}
					
					if(mTemplate.whichEditable(endX,endY,null) != -1)
						cal_current = listEditableCalligraphy.get(mTemplate.whichEditable(endX,endY,null));
					
					cal_current.setCurrentPos(endX, endY);
					resetBound();
					updateHandwriteState();
//					cal_current.setFlipDst(true);
					
					transparentBitmap.eraseColor(Color.TRANSPARENT);
				}
				
			} else {
				
				if(!WordLimit.getInstance().canInsertWord()){
					Toast.makeText(Start.context, "最多能书写" + WordLimit.WORDLIMIT + "个字", Toast.LENGTH_SHORT).show();
					CalligraphyVectorUtil.instance().finish(false,cal_current.currentpos);//不是字
					wordFinish = true;
					setJump();
					resetBound();
					transparentBitmap.eraseColor(Color.TRANSPARENT);
					canvas.drawBitmap(bitmap, Start.SCREEN_WIDTH,0, paint);
					updateHandwriteState();
					return;
				}
				mMatrix.postScale(mHScale, mVScale);
				
//				Bitmap vectorBitmap = null;
//				Log.v("create", "penStatus hard:" + (bView.penStatus == bView.STATUS_PEN_HARD));
				if(bView.penStatus == bView.STATUS_PEN_HARD){
					mSmallBitmap = CalligraphyVectorUtil.instance().finish(true,cal_current.currentpos);//是字
					if(mSmallBitmap == null){
						return;
					}
					endofLine = false;
					wordFinish = true;
				}else{
					try{
						if(endX <=  transparentBitmap.getWidth()){
							int x = startX;
							if(x > 10)
								x -= 10;
							int y = startY;
							if(y > 10)
								y -= 10;
//							if(endX + 10 > 600)
//								endX = 590;
							
							//ly
							if(endX + 10 > 1610)
								endX = 1600;
							
							if(endY + 10 > transparentBitmap.getHeight())
								endY = transparentBitmap.getHeight() - 10;
	            			Log.i("tttttt", "height:" + transparentBitmap.getHeight() + " y:" + y + " endY:" + endY);
	            			mSmallBitmap = Bitmap.createBitmap(transparentBitmap, x, y, endX - x + 10,
	            			endY - y + 10, mMatrix, true);
	            			Log.e("bianjie", "bitmap :" + (mSmallBitmap == null));
							BitmapCount.getInstance().createBitmap("CusorDrawBitmap create mSmallBitmap");
						}
					}catch(OutOfMemoryError e){
						System.gc();
						Log.e("bianjie", "Create small bitmap OOM!!!");
					}
				}
				
            		//	transparentBitmap.recycle();
				resetBound();
				transparentBitmap.eraseColor(Color.TRANSPARENT);
				//可能不再需要了
				
				//--------------获取vector的path，生成bitmap-------
				//--------------
	            //by jinyang
            	
	            canvas.drawBitmap(bitmap, Start.SCREEN_WIDTH,0, paint);
	           
//	            if(curMatrix != null)
//	            	Log.e("onFinish", curMatrix.toString());
//	            else
//	            	Log.e("onFinish", "curMatrix null");
	
	            if(bView.penStatus == bView.STATUS_PEN_CALLI){
	            	cal_current.insert(mSmallBitmap, curMatrix);
	            }
	            else{
//	            	mSmallBitmap = vectorBitmap;
	            	cal_current.insertVEditable(mSmallBitmap, curMatrix);
	            	
	            }
	            
	            Start.status.modified("insert new word");
//	            Log.i(TAG, "modify");
//	            Log.i(TAG, "mBitmap:width:" + mBitmap.getWidth() + " height:" + mBitmap.getHeight());
//	            cal_current.update(mBitmap);
	            
	            
	            
//	            for(int i=0;i<listEditableCalligraphy.size();i++){
////					listEditableCalligraphy.get(i).update(mBitmap,bView.mmMatrix);
//	            	listEditableCalligraphy.get(i).update(mBitmap);
//				}
//	            updateHandwriteState();
	            PreUpdateHandwriteStateFlip();
	            cal_current.setFlipDst(true,"onFinish");
	            updateHandwriteStateFlip();
	            
			}
			
//			cursorThread.notify();
			
			setJump();
			
            bView.invalidate();
		}

		@Override
		public void onTick(long millisUntilFinished) {
			// TODO Auto-generated method stub
			Log.i(TAG, "Tick---" + millisUntilFinished);
		}
		
	}
	
	public void cancelWord(){
		LogUtil.getInstance().e("cancel", "cancel word");
		bView.baseImpl.clear();
        rectsList.clear();
        wordFinish = true;
        resetBound();
		transparentBitmap.eraseColor(Color.TRANSPARENT);
		setJump();
	}
	public void insertAlarmItem(String date,String time){
//		Toast.makeText(Start.context, "date:"+ date, Toast.LENGTH_SHORT).show();
		
		//Bitmap alarmBitmap = Bitmap.createBitmap(150, 50, Bitmap.Config.ARGB_8888);
		Bitmap alarmBitmap = Bitmap.createBitmap(150, 50, Bitmap.Config.ARGB_4444);
		BitmapCount.getInstance().createBitmap("CursorDrawBitmap Create alarmBitmap");
		
		alarmBitmap.eraseColor(Color.TRANSPARENT);
		
		Bitmap alarm = BitmapFactory.decodeResource(Start.context.getResources(), R.drawable.img_calendar);
		BitmapCount.getInstance().createBitmap("CursorDrawBitmap decode img_calendar");
		
		Canvas alarmCanvas = new Canvas();
		alarmCanvas.setBitmap(alarmBitmap);
		alarmCanvas.drawBitmap(alarm, 0, 0, new Paint());
		alarm.recycle();
		BitmapCount.getInstance().recycleBitmap("CursorDrawBitmap insertAlarmItem alarm");
		
		Paint p = new Paint();
		p.setTextSize(20);
		
		alarmCanvas.drawText(time, 60, 48, p);
		
		alarmCanvas.drawText(date, 40, 22, p);
		
		
		alarmCanvas.setBitmap(mBitmap);
		alarmCanvas.drawBitmap(bitmap, Start.SCREEN_WIDTH,0, paint);
       
        if(curMatrix != null)
        	Log.e("onFinish", curMatrix.toString());
        else
        	Log.e("onFinish", "curMatrix null");
        
        
        cal_current.insert(alarmBitmap, curMatrix);
        Start.status.modified("insert new word");
        Log.i(TAG, "modify");
        
        Log.i(TAG, "mBitmap:width:" + mBitmap.getWidth() + " height:" + mBitmap.getHeight());
//        cal_current.update(mBitmap);
        
        
        
        for(int i=0;i<listEditableCalligraphy.size();i++){
//			listEditableCalligraphy.get(i).update(mBitmap,bView.mmMatrix);  vectorScale
        	listEditableCalligraphy.get(i).update(mBitmap,false);
		}
        
        cal_current.setFlipDst(true,"insertAlarmItem");
        
        updateHandwriteStateFlip();
	}
	
	public EditableCalligraphyItem insertImageBitmap(Bitmap src ,Uri imageUri){
//		cal_current.insert(src, curMatrix);
		EditableCalligraphyItem item = null;
		item = cal_current.insertImage(src, curMatrix, imageUri);
        Start.status.modified("insert new word");
        Log.i(TAG, "modify");
        
//        Log.i(TAG, "mBitmap:width:" + mBitmap.getWidth() + " height:" + mBitmap.getHeight());
////        cal_current.update(mBitmap);
//        
//        for(int i=0;i<listEditableCalligraphy.size();i++){
////			listEditableCalligraphy.get(i).update(mBitmap,bView.mmMatrix); vectorScale
//        	listEditableCalligraphy.get(i).update(mBitmap,false);
//		}
        
        cal_current.setFlipDst(true,"insertImageBitmap");
        
//        Start.c.view.cursorBitmap.updateHandwriteState();
        updateHandwriteStateFlip();
        return item;
	}
	public void insertVideoBitmap(Bitmap src ,Uri imageUri){
//		cal_current.insertVideo(src, curMatrix, imageUri);
		cal_current.insertVideo(src, new Matrix(), imageUri);
        Start.status.modified("insert new word");
        cal_current.setFlipDst(true,"insertVideoBitmap");
        updateHandwriteStateFlip();
	}
	public void insertAudioBitmap(Bitmap src ,Uri imageUri){
//		cal_current.insertAudio(src, curMatrix, imageUri);
		cal_current.insertAudio(src, new Matrix(), imageUri);
        Start.status.modified("insert new word");
        cal_current.setFlipDst(true,"insertAudioBitmap");
        updateHandwriteStateFlip();
	}
	
	public void insertImageItem(int imageRes){
		//Bitmap imageBitmap = Bitmap.createBitmap(50, 50, Bitmap.Config.ARGB_8888);
		Bitmap imageBitmap = Bitmap.createBitmap(50, 50, Bitmap.Config.ARGB_4444);
		BitmapCount.getInstance().createBitmap("CursorDrawBitmap Create imageBitmap");
		
		imageBitmap.eraseColor(Color.TRANSPARENT);
		Canvas imageCanvas = new Canvas();
		imageCanvas.setBitmap(imageBitmap);
		
		Bitmap img = BitmapFactory.decodeResource(Start.context.getResources(), imageRes);
		BitmapCount.getInstance().createBitmap("CursorDrawBitmap insertImageItem imageRes");
		
		imageCanvas.drawBitmap(img, 0, 0, new Paint());
		img.recycle();
		BitmapCount.getInstance().recycleBitmap("CursorDrawBitmap insertImageItem img");
		
		imageCanvas.setBitmap(mBitmap);
		imageCanvas.drawBitmap(bitmap, Start.SCREEN_WIDTH,0, paint);
       
        if(curMatrix != null)
        	Log.e("onFinish", curMatrix.toString());
        else
        	Log.e("onFinish", "curMatrix null");
        
        cal_current.insert(imageBitmap, curMatrix);
        Start.status.modified("insert new word");
        Log.i(TAG, "modify");
        
//        for(int i=0;i<listEditableCalligraphy.size();i++){
//        	listEditableCalligraphy.get(i).update(mBitmap,false);
//		}
        
        cal_current.setFlipDst(true,"insertImageItem");
        
        updateHandwriteStateFlip();
	}
	
	public float getTop2() {
        if(rectsList.size() == 0)
                return 0;
        else if(rectsList.size() == 1)
                return rectsList.get(0).top;
        else {
                float f1 = rectsList.get(0).top;
                float f2 = rectsList.get(1).top;
//              if(Math.abs(f2 - f1) > 100) 
//                      return f2;
//              else
//                      return f1;
                return f2;
        }
	}

	public float getBottom2() {
	        if(rectsList.size() == 0)
	                return 0;
	        else if(rectsList.size() == 1)
	                return rectsList.get(0).bottom;
	        else  {
	                float f1 = rectsList.get(0).bottom;
	                float f2 = rectsList.get(1).bottom;
	                if(Math.abs(f2 - f1) > 100)
	                        return f2 + Math.abs(f2 -f1) / 1.5f;
	                else
	                        return f1;
	        }
	}

	/*
	 * add by mouse 2012.02.16
	 * 返回当前所有手写区域中的最大高度值
	 */
	public int getMaxHeight() {
		int height;
		int max = 0;
		for(int i=0;i<listEditableCalligraphy.size();i++){
			height = listEditableCalligraphy.get(i).getBottomY();
			if(height > max) 
				max = height;
		}
		return max;
	}
	
	public void printRectFs() {
	        Iterator iter = rectsList.iterator();
	        Log.i(TAG, "=============START==============");
	        while(iter.hasNext()){
	                Log.i(TAG, ((RectF)iter.next()).toString());
	        }
	        Log.i(TAG, "=============END==============");
	}
	
	class topComparator implements Comparator<RectF> {

        @Override
        public int compare(RectF object1, RectF object2) {
                // TODO Auto-generated method stub
                return (int)(object1.top - object2.top);
        }

	}
	
	class bottomComparator implements Comparator<RectF> {

        @Override
        public int compare(RectF object1, RectF object2) {
                // TODO Auto-generated method stub
                return (int)(object2.bottom - object1.bottom);
        }
	}
	
	public Bitmap saveAllEditableToBitmap() {
		
		Bitmap b = null;
		Matrix m = new Matrix(); //用于计算最终生成图片的缩放比例(设置最大值，防止超出)
		
		int destWidth = Start.SCREEN_WIDTH; //生成的bitmap的最终宽度
		int destHeight = getMaxHeight(); //生成的bitmap的最终高度
		
		float scale = 1.0f;
		if(destHeight > MyView.MAX_SHARE_PIC_HEIGHT) {
			scale = MyView.MAX_SHARE_PIC_HEIGHT * 1.0f / destHeight;
			m.postScale(scale, scale);
		}

		Log.i("shareScale", "before-----destW:" + destWidth + " destH:" + destHeight + " scale:" + scale);
		
		destWidth *= scale;
		destHeight *= scale;
		
		Log.i("shareScale", "after-----destW:" + destWidth + " destH:" + destHeight);
		
		try {
//			System.gc();
			//b = Bitmap.createBitmap(destWidth, destHeight + 100, Bitmap.Config.ARGB_8888);
			b = Bitmap.createBitmap(destWidth, destHeight + 100, Bitmap.Config.ARGB_4444);
			BitmapCount.getInstance().createBitmap("CursorDrawBitmap saveAllEditableToBitmap create b");
		} catch (OutOfMemoryError e) {
			try {
				Log.i("shareScale", "OOM SHARE");
				scale *= 0.5f;
				destWidth *= 0.5f;
				destHeight *= 0.5f;
				
				m = new Matrix();
				m.postScale(scale, scale);
				
				//b = Bitmap.createBitmap(destWidth, destHeight, Bitmap.Config.ARGB_8888);
				b = Bitmap.createBitmap(destWidth, destHeight, Bitmap.Config.ARGB_4444);
				BitmapCount.getInstance().createBitmap("CursorDrawBitmap saveAllEditableToBitmap oom create small b");
			} catch (OutOfMemoryError oom) {
				Toast.makeText(bView.getContext(), "Out Of Memory, save failed!", Toast.LENGTH_SHORT);
				return null;
			}
		}
		
		drawBitmap(b,scale,m , false);
		
		return b;
	}
	public void drawBitmap(Bitmap b,float scale,Matrix m,boolean isIndex){
		Canvas c = new Canvas();
		c.setBitmap(b);
		c.drawColor(Color.WHITE);
		
		
		
		int lineNum = 0;
		int miniNum = 0;
		Paint line_paint = new Paint();
		line_paint.setColor(Color.GRAY);
		
		Matrix matrix = new Matrix();
		float[] matrixValue = new float[9];
		matrix.getValues(matrixValue);
		
//		Log.i(TAG, "-------------" + matrix.toString());
		WolfTemplate template = WolfTemplateUtil.getCurrentTemplate();
		List<Available>  listAvailables = template.getAvailables();
		Available available;
		int line_top = 0;
		int line_bottom = 0;
		int line_space = 0;
		Canvas canvas = new Canvas();
		canvas.setBitmap(b);
		canvas.scale(scale, scale);
		
		c.scale(scale, scale);
		
		

		c.drawBitmap(bitmap, 0, 0, new Paint());
		
		for(int j=0;j<listAvailables.size();j++){
			available = listAvailables.get(j);
			if(available.getDirect() != 1){
				//横向
				line_top = available.getStartY();
				line_bottom = available.getEndY();	
//				line_space = template.getLinespace(); 
				if(available.getAlinespace() != 0)
					line_space = available.getAlinespace();
				else
					line_space = template.getLinespace();
				
//				if(available.getLinenumber() == 0)
//					return;
				
//				line_space = (int) (line_space * matrixValue[4]);
				line_space = (int) (line_space);
				
				lineNum = (getMaxHeight() - line_top)/ line_space;
				
				if(available.getLinenumber() == 1){
					Log.e("drawShareLine", "linenum:" + available.getLinenumber());
				}else{
					Log.e("drawShareLine", "------linenum:" + available.getLinenumber());
					Log.e("drawShareLine", "------draw linenum:" + lineNum);
//					Log.e("BaseBitmap","linespace:"+ line_space);
					
					
					for(int i=0;i<lineNum;i++){
//						Log.e("line", "direct:"+available.getDirect()+" lineNumber:"+available.getLinenumber());
						canvas.drawLine( 20 , line_top+line_space*(i+1), 560, line_top+line_space*(i+1), line_paint);
					}
				}
			}
		}//for
		
		
		for(int i=0;i<listEditableCalligraphy.size();i++){
			listEditableCalligraphy.get(i).saveAllCharToBitmap(b,m,isIndex);
		}
	}
	
	public void rebuildTimer() {
		timer = new Timer(millisInFuture, countDownInterval);
	}
	
}
