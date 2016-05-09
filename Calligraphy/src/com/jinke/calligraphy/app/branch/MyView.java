package com.jinke.calligraphy.app.branch;

import hallelujah.cal.CalligraphyVectorUtil;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Random;


import android.app.AlertDialog;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.gesture.Gesture;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.PointF;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region.Op;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.storage.StorageManager;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.text.Html;
import android.text.Spanned;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Scroller;
import android.widget.TextView;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;

import com.jinke.calligraphy.app.branch.FreeDrawBitmap.FreeBitmapInfo;
import com.jinke.calligraphy.data.Storage;
import com.jinke.calligraphy.database.CDBPersistent;
import com.jinke.calligraphy.date.SliderContainer;
import com.jinke.calligraphy.template.Available;
import com.jinke.calligraphy.template.WolfTemplate;
import com.jinke.calligraphy.template.WolfTemplateUtil;
import com.jinke.calligraphy.touchmode.CursorChoiceMode;
import com.jinke.calligraphy.touchmode.CursorNullMode;
import com.jinke.calligraphy.touchmode.CursorPullMode;
import com.jinke.calligraphy.touchmode.CursorScaleMode;
import com.jinke.calligraphy.touchmode.FreeDragMode;
import com.jinke.calligraphy.touchmode.FreeNullMode;
import com.jinke.calligraphy.touchmode.FreeScaleMode;
import com.jinke.calligraphy.touchmode.HandWriteMode;
import com.jinke.calligraphy.touchmode.ImageSlideMode;
import com.jinke.calligraphy.touchmode.MindSlideMode;
import com.jinke.calligraphy.touchmode.SideDownMode;
import com.jinke.calligraphy.touchmode.SideUpMode;
import com.jinke.calligraphy.touchmode.SideUpMode;
import com.jinke.calligraphy.touchmode.StartMode;
import com.jinke.calligraphy.touchmode.TouchMode;
import com.jinke.newly.CNetTransfer;
import com.jinke.newly.Config;
import com.jinke.newly.HomeworkBean;
import com.jinke.newly.HomeworkInfoParser;
import com.jinke.single.BitmapCount;
import com.jinke.single.LogUtil;

public class MyView extends View implements ColorPickerDialog.OnColorChangedListener{
	private static final String TAG = "MyView";

	public boolean hasTouch = false;
	public static int statusBarHeight = 0;
	private int rowNumber;
	ImageAdapter ia;
	GridView g;
	private GestureLibrary gestureLib;//创建一个手势仓库
    public int doubleCount = 0;
	public boolean isLoad = false; //用来判断涂鸦态背景是否已经加载
	public long panzuoyeTimer1;
	public long panzuoyeTimer2;
	public Bitmap mBitmap;
	private Bitmap mScreenLayerBitmap;
	public WolfTemplate mTemplate;// 模板信息

	public BasePointsImpl baseImpl;
	public CalliPointsImpl calliImpl;
	public HardPointsImpl hardImpl;

	public BaseBitmap baseBitmap;
	public FreeDrawBitmap freeBitmap;
	public CursorDrawBitmap cursorBitmap;

	public static int STATUS_PEN_CALLI = 1; // 毛笔状态
	public static int STATUS_PEN_HARD = 0; // 硬笔状态
	public static int penStatus = STATUS_PEN_CALLI; // 默认为毛笔

	public static final int STATUS_DRAW_FREE = 0; // 涂鸦态
	public static final int STATUS_DRAW_CURSOR = 1; // 光标态
	public static int drawStatus = STATUS_DRAW_FREE; // 默认为涂鸦态

	public TouchMode startMode;
	public TouchMode sideDownMode;
	public TouchMode sideUpMode;
	public TouchMode handWriteMode;
	public TouchMode freeDragMode;
	public TouchMode freeScaleMode;
	public TouchMode cursorChoiceMode;
	public TouchMode freeNullMode;
	public TouchMode cursorNullMode;
	public TouchMode cursorScaleMode;
	public TouchMode cursorPullMode;
	public TouchMode imageSlideMode;
	public TouchMode mindSlideMode;

	public TouchMode touchMode;

	
	private Canvas mCanvas;

	private Matrix mmMatrix;// null
	public Matrix cursorMatrix;
	public Matrix freeSavedMatrix;

	public Storage mStorage;
	public static boolean splitFlag = false;

	// public static final String FILE_PATH_HEADER = "/extsd";
	public static final String FILE_PATH_HEADER = Start.getStoragePath();
	public static final int MAX_SHARE_PIC_WIDTH = Start.SCREEN_WIDTH;
	public static final int MAX_SHARE_PIC_HEIGHT = Start.SCREEN_HEIGHT * 2;
	
	public long time;
	
	
	public Bitmap foreImage;
	public Bitmap bgImage;
	public GradientDrawable shadowDrawableRL; //阴影
	public GradientDrawable shadowDrawableLR; //阴影，设置变换
	public ColorMatrixColorFilter mColorMatrixFilter;  //图片加灰、变性处理
	public Scroller mScroller;      //滚条实现触电放开后的翻页动画效果
	public int lastTouchX;
	public PointF touchPt;
	public int screenWidth;
	public int screenHeight;
	
	public static int SlideMode = 0;   //SlideMode=0 为画笔，=1为滑动翻页
	
	private Canvas slideCanvas;
	
	private static int isTurnToNextPic = 0;
	
	//抬头作业信息
	private LinearLayout personalInfoDisplayLayout;
	public TextView nameText;
	public static int keshiName = 1;
	public static int name = 1;
	
	
	public static  String pageXML ;
	

	
	//双击
	List<Long> clickTimes = new ArrayList<Long>();
	private Boolean doubleClickState = true; //打开图true，关闭图false
	
	public String bgName;
	//作业xml信息 2016.3.23 caoheng
	
	

	public void setBitmap(Bitmap bitmap) {
		this.mBitmap = bitmap;
	}

	public MyView(Context c, Bitmap bp, Bitmap layer, WolfTemplate wt) {
		super(c);
		pageXML = "0944-0001-0000-0023-0003-0009-0022";
	
		Log.e("storage", "MyView init storage1");
		
		Log.i("fanye", "myview 1");

		mBitmap = bp;
		mTemplate = wt;
		mScreenLayerBitmap = layer;

		
		startMode = new StartMode(this);
		Log.e("vectorr", " -------------->>>>>>>>>>>>>>>>>>>>>>>>>>>>>startMode finish" +
				"" );
		sideDownMode = new SideDownMode(this);
		Log.e("vectorr", " -------------->>>>>>>>>>>>>>>>>>>>>>>>>>>>>sideDownMode finish" );
		
		// 解析模板 初始化
		// 初始化 drawStatus 绘画风格
		initTemplate();
		
		mStorage = new Storage(this);
        
		
		
		
		HandWriteMode handMode = new HandWriteMode(this);
		handMode.setsMatrix(Start.m);
		
		handWriteMode = handMode;
		
		freeDragMode = new FreeDragMode(this);
		freeScaleMode = new FreeScaleMode(this);
		cursorChoiceMode = new CursorChoiceMode(this);
		freeNullMode = new FreeNullMode(this);
		cursorNullMode = new CursorNullMode(this);
		cursorScaleMode = new CursorScaleMode(this);
		cursorPullMode = new CursorPullMode(this);
		imageSlideMode = new ImageSlideMode(this);
		mindSlideMode = new MindSlideMode(this);
		touchMode = handWriteMode;
		
		//ly
		//mBitmap.eraseColor(Color.WHITE);
		//end
		
		mCanvas = new Canvas();
		cursorMatrix = new Matrix();
		freeSavedMatrix = new Matrix();
		
		
		//滑动初始工作
		touchPt = new PointF(-1, -1);
		
		//ARGB A(0-透明,255-不透明)
	    int[] color = { 0xb0333333 ,0x00333333};
	    shadowDrawableRL = new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, color);
	    shadowDrawableRL.setGradientType(GradientDrawable.LINEAR_GRADIENT);


	    shadowDrawableLR = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, color);
	    shadowDrawableLR.setGradientType(GradientDrawable.LINEAR_GRADIENT);
	    
         float array[] = { 0.55f,    0,    0,     0, 80.0f, 
        		   0    ,0.55f,    0,     0, 80.0f, 
          		   0    ,    0,0.55f,     0, 80.0f, 
        		   0    ,    0,    0,  0.2f, 0};
        ColorMatrix cm = new ColorMatrix();
        cm.set(array);
        
        mColorMatrixFilter = new ColorMatrixColorFilter(cm);

        //利用滚动条来实现接触点放开后的动画效果
        mScroller = new Scroller(c);
        
//        Bitmap[] getBitmap = findPic();
//        foreImage = getBitmap[0];
//        if(getBitmap[1] != null) {
//        	bgImage = getBitmap[1];
//        } else {
//        	SlideMode = 0;
        
        
        
		Log.e("vectorr", " -------------->>>>>>>>>>>>>>>>>>>>>>>>>>>>>MyView finish" );
	}

	public MyView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mStorage = new Storage(this);
		// TODO Auto-generated constructor stub
	}

	public Bitmap getMBitmap() {
		return mBitmap;
	}

	public void setMMMatirx(Matrix m) {
//		this.mmMatrix = m;
		if(m == null)
			return;
		if(this.mmMatrix == null)
			this.mmMatrix = new Matrix();
		this.mmMatrix.set(m);
		Log.e("changematrix", "change Matrix MyView setMMMatirx");
	}
	public Matrix getMMMatrix(){
		return this.mmMatrix;
	}

	public WolfTemplate getTemplate() {
		return mTemplate;
	}

	public void initTemplate() {
		System.out.println("mTemplate" + (mTemplate == null));
		// 读取模板 初始化绘画风格
		if (mTemplate.getFormat() == STATUS_DRAW_FREE) {
			drawStatus = STATUS_DRAW_FREE;
		} else if (mTemplate.getFormat() == STATUS_DRAW_CURSOR) {
			drawStatus = STATUS_DRAW_CURSOR;

		} else {
			Toast.makeText(getContext(), "template format error",
					Toast.LENGTH_SHORT).show();
		}
		freeBitmap = new FreeDrawBitmap(mBitmap, this);
		cursorBitmap = new CursorDrawBitmap(mBitmap, this);
		Log.e("vectorr", "cursorBitmap create finish -------------->>>>>>>>>>>>>" );
         
		if (drawStatus == STATUS_DRAW_FREE) {
			baseBitmap = freeBitmap;
		} else {
			baseBitmap = cursorBitmap;
		}

		if (mTemplate.getPentype() == STATUS_PEN_CALLI) {
			penStatus = STATUS_PEN_CALLI;
		} else if (mTemplate.getPentype() == STATUS_PEN_HARD) {
			penStatus = STATUS_PEN_HARD;
		} else {
			Toast.makeText(getContext(), "template pentype error",
					Toast.LENGTH_SHORT).show();
		}

		// 初始化画笔状态
		calliImpl = new CalliPointsImpl(baseBitmap, this);
		hardImpl = new HardPointsImpl(baseBitmap, this);

		if (penStatus == STATUS_PEN_CALLI) {
			baseImpl = calliImpl;
		} else {
			baseImpl = hardImpl;
		} 
	
		Log.e("vectorr", " -------------->>>>>>>>>>>>>>>>>>>>>>>>>>>>>initTemplate finish" );
	}

	public void changeDrawState(int draw) {
		drawStatus = draw;
		if (drawStatus == STATUS_DRAW_FREE)
		{
			baseBitmap = freeBitmap;
		}
		else {
			baseBitmap = cursorBitmap;
			// 此处不需要更新了，放在了drawStateSync中
			// baseBitmap.updateState();
		}
		/*
		 * 底层绘制的bitmap状态改变后，每一个相应的实现都需要更新
		 */
		
		
		calliImpl.clear();
		hardImpl.clear();
		calliImpl.updateBitmap();
		hardImpl.updateBitmap();

		//calliImpl.updatePaintSize();
		//hardImpl.updatePaintSize();


		print();
//		invalidate();
	}

	public void changePenState(int pen) {
		Log.i("nmmp", "changepenstate");
		penStatus = pen;
		Log.i(TAG, "changePenState pen:"+pen);
		
		
		if(drawStatus == STATUS_DRAW_FREE)
		{
			freeBitmap.updateToBitmapList();

			freeBitmap.drawFreeBitmapSync();
			

			mCanvas.setBitmap(mBitmap);
			mCanvas.drawBitmap(mBitmap, new Rect(Start.SCREEN_WIDTH, 0, Start.SCREEN_WIDTH * 2, Start.SCREEN_HEIGHT), new Rect(
					0, 0, Start.SCREEN_WIDTH, Start.SCREEN_HEIGHT), new Paint());


			calliImpl.updateBitmap();
			hardImpl.updateBitmap();
		}
		if (penStatus == STATUS_PEN_CALLI) {

			baseImpl = calliImpl;
		} else {
			baseImpl = hardImpl;
			hardImpl.clear();
		}


		print();
		invalidate();
	}

	// 切换状态并且同步bitmap
	public void changeStateAndSync(int draw) {
		Log.e(TAG, "changeStateAndSync!!!!!!!!!!");
		switch (draw) {
		case STATUS_DRAW_FREE:
			Calligraph.mDrawStatusChangeBtn.setBackgroundResource(R.drawable.status_cursorsel);
//			 view.doChangeBackground(WolfTemplate.COPYBOOK2);

			
//			bgBitmap = BitmapFactory.decodeFile(freeBg).copy(Bitmap.Config.ARGB_4444, true);
			
			//ly
			//把背景当做第一个涂鸦态内容加入到列表中
			addFreeBg();
				
				
			scaleStateSync(draw);
			drawStateSync(draw);
			changeDrawState(STATUS_DRAW_FREE);
			
			//ly
			//新加的东西
			freeBitmap.drawFreeBitmapSync();
			//end
			
			//changePenState(STATUS_PEN_CALLI);
			break;
		case STATUS_DRAW_CURSOR:
			Calligraph.mDrawStatusChangeBtn
					.setBackgroundResource(R.drawable.status_tuyasel);
			// view.doChangeBackground(WolfTemplate.NOTEBOOK);

			
			scaleStateSync(draw);
			drawStateSync(draw);
			changeDrawState(STATUS_DRAW_CURSOR);
			cursorBitmap.updateHandwriteStateFlip();
			// changePenState(STATUS_PEN_CALLI);毛笔！！

			//changePenState(STATUS_PEN_HARD);
			

			
			
			break;
		}
	}

	public void scaleStateSync(int draw) {
		Log.i("nmmp", "scalestatesync");
		switch (draw) {
		case STATUS_DRAW_FREE:
			Log.e("changematrix", "change Matrix MyView scaleStateSync");
			cursorMatrix.set(mmMatrix);
			freeDragMode.clear();
			freeScaleMode.clear();
			freeSavedMatrix.reset();
			
			Calligraph.mScaleBitmap.eraseColor(Color.TRANSPARENT);
			Calligraph.mScaleTransparentBitmap.eraseColor(Color.TRANSPARENT);

						
						
			
			break;
		case STATUS_DRAW_CURSOR:
			Log.e("changematrix", "change Matrix MyView scaleStateSync");
			mmMatrix.set(cursorMatrix);
			Matrix m = new Matrix(freeSavedMatrix);
			Log.i(TAG, "MMMMMMMMMMMM" + m.toString());
			float[] values = new float[9];
			m.getValues(values);
			m.reset();
			Log.i(TAG, "MMMMMMMMMMMM2222" + values[0]);
			m.setScale(1 / values[0], 1 / values[0]);
			Log.i(TAG, "MMMMMMMMMMMM3333" + m.toString());
			Log.i("caoheng", "savefile1");
			saveFile(cursorBitmap.getTopBitmap(), "transparent1.jpg");
			// ((FreeDragMode)freeDragMode).syncBitmapToScale(cursorBitmap.getTopBitmap());
			freeDragMode.clear();
			freeScaleMode.clear();
			cursorBitmap.getTopBitmap().eraseColor(Color.TRANSPARENT);
			mCanvas.setBitmap(cursorBitmap.getTopBitmap());
			// mCanvas.drawBitmap(Calligraph.mScaleTransparentBitmap, m, new
			// Paint());

			mCanvas.drawBitmap(Calligraph.mScaleTransparentBitmap, new Rect(0, 0, (int)(Start.SCREEN_WIDTH*values[0]), (int)(Start.SCREEN_HEIGHT*values[0])),
					new Rect(0, 0, Start.SCREEN_WIDTH, Start.SCREEN_HEIGHT), new Paint());
//			saveFile(cursorBitmap.getTopBitmap(), "transparent2.jpg");
//			saveFile(Calligraph.mScaleTransparentBitmap, "mScale2.jpg");
			break;
		}
	}
	
	//ly
	//用于更新图片
	private Handler mHandler = new Handler()
	{

		public void handleMessage(android.os.Message msg) {
			Log.i("nmmp", "mhandler");
			switch(msg.what)
			{
			case 101:
				BitmapCount.getInstance().count();
				
				Log.e("in draw", "free");
				//String source ="/mnt/sdcard/homework/img1.jpg"; 
				HomeworkBean bean = (HomeworkBean) msg.obj;
				String source = bean.getPic();
				
				//mBitmap.eraseColor(Color.WHITE);
				
				
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inJustDecodeBounds = true;
				try {	
					File file = new File(source);
					  
			       // int heightRatio = (int)Math.ceil(options.outHeight/(float)screenHeight);  
			        //int widthRatio = (int)Math.ceil(options.outWidth/(float)screenWidth);  
			        int heightRatio = (int)Math.ceil(options.outHeight/(float)2560);  
			        int widthRatio = (int)Math.ceil(options.outWidth/(float)1600);  
			        if (heightRatio > 1 && widthRatio > 1)  
			        {  
			        	options.inSampleSize =  heightRatio > widthRatio ? heightRatio:widthRatio;  
			        }  
			        options.inJustDecodeBounds = false;  
			        
			        Bitmap b = BitmapFactory.decodeStream(new FileInputStream(file), null,  options);  
			        
//					if(mBitmap!=null)
//					{
//						mBitmap.recycle();
//					}
			        
			        //bitmap = bitmap.createScaledBitmap(bitmap, 1600, 2460, true);   
			        

					mCanvas.setBitmap(mScreenLayerBitmap);
					mCanvas.drawBitmap(b, new Rect(0, 0, b.getWidth(), b.getHeight()), new Rect(
							0, 0, Start.SCREEN_WIDTH, Start.SCREEN_HEIGHT), new Paint());
					
					
					freeBitmap.updateToBitmapList();
					freeBitmap.drawFreeBitmapSync();
					
//			        if(bitmap!=null)
//			        	bitmap.recycle();
			        
			        Log.e("ai", "hehe");
					//bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ddd,options);
					//init(bitmap.getWidth(),bitmap.getHeight());
									 
				} catch (OutOfMemoryError e) {
					// TODO: handle exception
					Log.e("AndroidRuntime", "MyView doChangeBackground() OOM !!!!");
				}catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//mBitmap.eraseColor(Color.RED);
				//end
				//mBitmap.eraseColor(Color.BLUE);
//				mCanvas.setBitmap(mBitmap);
//				mCanvas.drawBitmap(mBitmap, new Rect(Start.SCREEN_WIDTH, 0, Start.SCREEN_WIDTH * 2, Start.SCREEN_HEIGHT), new Rect(
//						0, 0, Start.SCREEN_WIDTH, Start.SCREEN_HEIGHT), new Paint());
				
				Start.bar.setVisibility(View.INVISIBLE);
				Start.barText.setVisibility(View.INVISIBLE);
				Calligraph.mNextBtn.setClickable(true);
				
				break;
			}
		};
	};
	//end
	
	//ly
	public List<HomeworkBean> homeworkList = new ArrayList<HomeworkBean>();
	public static int nextHomework = 0;
	
	/*
	 * ly
	 * 参数为当前待获取图片
	 */
	public void getNextHomework()
	{

		Start.bar.setVisibility(View.INVISIBLE);
		Start.barText.setVisibility(View.INVISIBLE);
		
		//mScreenLayerBitmap.eraseColor(Color.TRANSPARENT);
//		freeBitmap.bitmap.eraseColor(Color.TRANSPARENT);
//		cursorBitmap.bitmap.eraseColor(Color.TRANSPARENT);
		//baseBitmap.mBitmap.eraseColor(Color.WHITE);
		//mScreenLayerBitmap.eraseColor(Color.WHITE);
		//mCanvas.drawColor(Color.argb(255, 0, 0, 0));
		
		if(1==1)
			return;
		
		if(nextHomework==homeworkList.size())
		{
			Looper.prepare();
			Toast.makeText(getContext(), "作业已全部批改完成", Toast.LENGTH_SHORT).show();
			
			Start.bar.setVisibility(View.INVISIBLE);
			Start.barText.setVisibility(View.INVISIBLE);
			Calligraph.mNextBtn.setClickable(true);
			Looper.loop();
			return;
		}
		HomeworkBean bean = homeworkList.get(nextHomework++);
		if(bean==null)
		{
			Start.bar.setVisibility(View.INVISIBLE);
			Start.barText.setVisibility(View.INVISIBLE);
			Calligraph.mNextBtn.setClickable(true);
			return;
		}
		if(CNetTransfer.writeImage(bean.getPic(), "/mnt/sdcard/homework/"+bean.getPic().substring(bean.getPic().lastIndexOf("/")+1)))
		{
			bean.setPic("/mnt/sdcard/homework/"+bean.getPic().substring(bean.getPic().lastIndexOf("/")+1));
			Message msg = Message.obtain();
			msg.what = 101;
			msg.obj = bean;
			mHandler.sendMessage(msg);
		}	
	}
	//end
	
	public void drawStateSync(int draw) {
		switch (draw) {
		case STATUS_DRAW_FREE:
			Log.i("nmmp", "drawStateSync");

			//ly
			//打开原来的注释
			//freeBitmap.updateToBitmapList();
			
			mCanvas.setBitmap(mBitmap);
			
			//caoheng 2015.12.11保存涂鸦态尝试
			mCanvas.drawBitmap(mBitmap, new Rect(Start.SCREEN_WIDTH, 0, Start.SCREEN_WIDTH * 2, Start.SCREEN_HEIGHT), new Rect(
					0, 0, Start.SCREEN_WIDTH, Start.SCREEN_HEIGHT), new Paint());
//			mCanvas.drawBitmap(mBitmap, new Rect(Start.SCREEN_WIDTH, 0, Start.SCREEN_WIDTH , Start.SCREEN_HEIGHT), new Rect(
//					0, 0, Start.SCREEN_WIDTH, Start.SCREEN_HEIGHT), new Paint());
			
			break;
		case STATUS_DRAW_CURSOR:
			/*
			 * mCanvas.setBitmap(cursorBitmap.bitmap);
			 * mCanvas.drawBitmap(cursorBitmap.getTopBitmap(), new Rect(0, 0,
			 * 600, 1024), new Rect(0, 0, 600, 1024), new Paint());
			 * mCanvas.setBitmap(mBitmap);
			 * mCanvas.drawBitmap(cursorBitmap.getTopBitmap(), new Rect(0, 0,
			 * 600, 1024), new Rect(600, 0, 1200, 1024), new Paint());
			 */
			// saveFile(mBitmap, "bbb.jpg");
			
	//2015.12.11 caoheng 		下面这句话写在这才能保存画笔
			freeBitmap.updateToBitmapList();
			
			
			 cursorBitmap.updateTransparent();//回复了涂鸦副本mBitmap， 点击新建时，把其中的副本清空
			// 将更新了的mBitmap更新在右侧
			 mCanvas.setBitmap(mBitmap);
			//caoheng 2015.12.11保存涂鸦态尝试
			 mCanvas.drawBitmap(cursorBitmap.bitmap, new Rect(0, 0, 1600,2560  ), new
			 Rect(1600, 0, 1600*2 , 2560  ), new Paint());

			// saveFile(cursorBitmap.bitmap, "bitmap.jpg");
			// saveFile(mBitmap, "bb.jpg");
			break;
		}
	}

	public void destroy() {
		if (freeBitmap.bitmap != null){
			freeBitmap.bitmap.recycle();
			BitmapCount.getInstance().recycleBitmap("MyView destroy freeBitmap.bitmap");		
		}

		mBitmap.recycle();
		BitmapCount.getInstance().recycleBitmap("MyView destroy mBitmap");	
		
		mScreenLayerBitmap.recycle();
		BitmapCount.getInstance().recycleBitmap("MyView destroy mScreenLayerBitmap");	
		
		cursorBitmap.mBitmap.recycle();
		BitmapCount.getInstance().recycleBitmap("MyView destroy cursorBitmap.mBitmap");	

		if (baseBitmap.bitmap != null){
			baseBitmap.bitmap.recycle();
			BitmapCount.getInstance().recycleBitmap("MyView destroy baseBitmap.bitmap");	
		}
		
		cursorBitmap.bitmap.recycle();
		BitmapCount.getInstance().recycleBitmap("MyView destroy cursorBitmap.bitmap");
		
		if (cursorBitmap.mSmallBitmap != null){
			cursorBitmap.mSmallBitmap.recycle();
			BitmapCount.getInstance().recycleBitmap("MyView destroy cursorBitmap.mSmallBitmap");
		}
		cursorBitmap.transparentBitmap.recycle();
		BitmapCount.getInstance().recycleBitmap("MyView destroy cursorBitmap.transparentBitmap");
		
		cursorBitmap.exit = true;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		Log.d(TAG, "onsize" + h + "oldh" + oldh);
		if (0 == oldh) {
			Log.d(TAG, "onsiz 1");
			statusBarHeight = Start.SCREEN_HEIGHT - h;
			updateScreenLayer();
			invalidate();
			return;
		}

		if (h < Start.SCREEN_HEIGHT && Start.SCREEN_HEIGHT == oldh) {
			Log.d(TAG, "onsiz 2");
			statusBarHeight = Start.SCREEN_HEIGHT - h;
			updateScreenLayer();
			invalidate();
			return;
		}

		if (h == Start.SCREEN_HEIGHT && Start.SCREEN_HEIGHT > oldh) {
			Log.d(TAG, "onsiz 3");
			statusBarHeight = 0;
			updateScreenLayer();
			invalidate();
			return;
		}
	}

	public void updateScreenLayer() {
		Canvas canvas = new Canvas();
		canvas.setBitmap(mBitmap);
		canvas.drawBitmap(mScreenLayerBitmap, new Rect(0, statusBarHeight, Start.SCREEN_WIDTH,
				Start.SCREEN_HEIGHT), new Rect(0, 0, Start.SCREEN_WIDTH, Start.SCREEN_HEIGHT - statusBarHeight), new Paint());
	}
	


	@Override
	public void onDraw(Canvas canvas) {
		Log.i("fanye", "slidemode = " + SlideMode);
		if(SlideMode == 0)
		{
			//画笔模式
			touchMode.draw(canvas);
		} else if(SlideMode == 1) {
			
			
			//touchMode.draw(canvas); 别写这行，否则卡死
			
			
//			Canvas fanzao = new Canvas(foreImage);
			//slideCanvas.drawColor(Color.RED);
//			Paint p1 = new Paint();
//			slideCanvas.drawBitmap(bgImage, 0, 0, p1);
//			Paint p = new Paint();
//			p.setColor(Color.BLUE);
//			slideCanvas.drawLine(800,0,800,2560,p);
	
//			Paint forPaint = new Paint();
//			canvas.drawBitmap(foreImage, 0, 0, forPaint);
//			
//			Paint p = new Paint();
//			p.setColor(Color.BLUE);
//			slideCanvas.drawLine(800,0,800,2560,p);
//			slideCanvas.drawLine(touchPt.x, 0, touchPt.x, 2560, p);
			
			
			
//			drawPageEffect(slideCanvas);
			
			drawPageEffect(canvas);
	

			
//			touchMode = imageSlideMode;
//			drawPageEffect(canvas);
			super.onDraw(canvas);
			invalidate();
		}	
	}
	
	
	
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float x = event.getX();
		float y = event.getY();
		
		Log.i("fanye", "x = " + x + " y = " + y);
		
		if(event.getAction() == MotionEvent.ACTION_DOWN){
			if(x > 1450) {
				SlideMode = 1; //从最右侧点下，为滑动翻页模式
//				  Bitmap[] getBitmap = findPic();
//			        foreImage = getBitmap[0];
////					mCanvas1 = new Canvas(foreImage);
//			        if(getBitmap[1] != null) {
//			        	bgImage = getBitmap[1];
//			        } else {
//			        	SlideMode = 0;
//			        }
				if(isTurnToNextPic == 1) {
					  picAndName[] getPicAndName = findPicAndName();
//					  Bitmap[] getBitmap = findPic();
//				        foreImage = getBitmap[0];
					  foreImage = getPicAndName[0].getBitmap();
					  bgName = getPicAndName[0].getName();
//						mCanvas1 = new Canvas(foreImage);
//				        if(getBitmap[1] != null) {
					  if(getPicAndName[1].getBitmap() != null){
//				        	bgImage = getBitmap[1];
						  bgImage = getPicAndName[1].getBitmap();
						  bgName = getPicAndName[1].getName();
				        } else {
				        	SlideMode = 0;
				        }
				}
			        
			   slideCanvas = new Canvas(foreImage);
			}

		}
		
		if(SlideMode == 1) {   //翻页动画模式
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				Log.i("fanye", "action down");
			//	slideCanvas.drawColor(Color.RED);
				touchPt.x = event.getX();
				touchPt.y =	event.getY();
				Log.i("touchpoint",""+touchPt.x);
				Log.i("touchpoint",""+touchPt.y);
			}
			else if(event.getAction() == MotionEvent.ACTION_MOVE){
				Log.i("fanye", "action move");
	//			slideCanvas.drawColor(Color.RED);
				lastTouchX = (int)touchPt.x;
				touchPt.x = event.getX();
				touchPt.y =	event.getY();
				
	//			slideCanvas.drawColor(Color.RED);

				
				
				postInvalidate();
				invalidate();
			}
			else if(event.getAction() == MotionEvent.ACTION_UP){
				isTurnToNextPic = 0;
				Log.i("fanye", "action up");
				Log.i("addText", "xiugai Text");
				int dx,dy;
				
				dy = 0;
				
				//向右滑动
				if (lastTouchX<touchPt.x) {
					dx = foreImage.getWidth() - (int)touchPt.x + 100;
				}
				else{ 
				//向左滑动
					dx = -(int)touchPt.x - foreImage.getWidth();
				}
				
				mScroller.startScroll((int)touchPt.x,(int)touchPt.y,dx,dy,1000);
				postInvalidate();
				invalidate();
				SlideMode = 0;
				
				if(event.getX() < 900) {
					File file = new File("/sdcard/"+pageXML+".xml");
					saveXML(file);
					Calligraph.pingyuText.setVisibility(View.GONE);
					isTurnToNextPic = 1;
					freeBitmap.resetFreeBitmapList();
					freeBitmap.addBgPic(bgImage);
					int length = bgName.length();
					String strName = bgName.substring(0,4);
					String strChapter = bgName.substring(0,4);
					if(length > 9){
						strChapter = bgName.substring(length-8, length-4);
					}
					Log.i("bgname",""+bgName.substring(0,length-4));
					pageXML = bgName.substring(0,bgName.length()-4);
					
			//		Log.i("bgname",""+pageXML);
					Log.i("name", "name & chapter" + strName + " " + strChapter);	
					
					Calligraph.setNameText(strChapter, strName);
					Calligraph.setPageText();
					invalidate();
					changeStateAndSync(0);
				}

			}	
		} 
		
		else if(SlideMode == 0) {     //画笔模式
			Log.i("doubleClick", "doubleCount = " + doubleCount);
//			Log.i("slide", "Calligraph Gesture Mode = " + Calligraph.GESTURE_MODE);
//			if(Calligraph.GESTURE_MODE == 0){        //当处于画笔状态的时候监听屏幕的滑动操作，如果处于滑动翻页状态，则无画笔监听。
		//		switch (event.getAction() & MotionEvent.ACTION_MASK) {
				switch (event.getActionMasked()) {
					case MotionEvent.ACTION_DOWN:   //按下
						Log.i("click", "action down");
						clickTimes.add(SystemClock.uptimeMillis());
						Log.i("click", "size = " + clickTimes.size());
						if(clickTimes.size() == 2) {			
							Log.i("click", "time = " + clickTimes.get(clickTimes.size()-1) + " " + clickTimes.get(0));
							if(clickTimes.get(clickTimes.size()-1) - clickTimes.get(0) < 20) {
								clickTimes.clear();
//								my_toast("双击");
								
								if(doubleClickState){
									doubleClickState = false;
									//打开图
									switch(doubleCount){
										case 0:
										{
											Calligraph.staticText.setVisibility(VISIBLE);
											Spanned text = Html.fromHtml("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;√&nbsp;&nbsp;&nbsp;&nbsp;0&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
								        	 		"乄&nbsp;&nbsp;&nbsp;&nbsp;0&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
								        	 		"×&nbsp;&nbsp;&nbsp;&nbsp;0");
								        	 Calligraph.staticText.setText(text);
	//										Calligraph.staticText.setText("  对   0              半对   0          错   0");
												
											freeBitmap.resetFreeBitmapList();
											Resources res=getResources();
											Bitmap bmp = BitmapFactory.decodeResource(res, R.drawable.t1);
											freeBitmap.addBgPic(bmp);
											changeStateAndSync(0);
											doubleCount++;
											doubleClickState = true;
											
											
											 CountDownTimer timer = new CountDownTimer(3000, 1000) {  
												  
											 @Override  	       
											 public void onTick(long millisUntilFinished) {  
												 //    Calligraph.staticText.setText("  对   1              半对   0          错   0");  
											        }  
											  
											        @Override  
											        public void onFinish() {  
											        	 Calligraph.staticText.setEnabled(true);  
											        	 
											        	 Spanned text = Html.fromHtml("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;√&nbsp;&nbsp;&nbsp;&nbsp;<font color=red><b>1</b></font>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
											        	 		"乄&nbsp;&nbsp;&nbsp;&nbsp;0&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
											        	 		"×&nbsp;&nbsp;&nbsp;&nbsp;0");
											        	 Calligraph.staticText.setText(text);
	//										            Calligraph.staticText.setText("  对   1              半对   0          错   0"); 
	//										            doubleCount++;
											        }  
											    }.start();  
										}
										break;
									case 1:
									{
										Log.i("doubleClick", "step into doubleCount == 1");
										Calligraph.staticText.setVisibility(VISIBLE);
										Spanned text = Html.fromHtml("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;√&nbsp;&nbsp;&nbsp;&nbsp;1&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
							        	 		"乄&nbsp;&nbsp;&nbsp;&nbsp;0&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
							        	 		"×&nbsp;&nbsp;&nbsp;&nbsp;0");
							        	 Calligraph.staticText.setText(text);
//										Calligraph.staticText.setText("  对   1              半对   0          错   0");
											
										freeBitmap.resetFreeBitmapList();
										Resources res=getResources();
										Bitmap bmp = BitmapFactory.decodeResource(res, R.drawable.t2);
										freeBitmap.addBgPic(bmp);
										changeStateAndSync(0);
										doubleCount++;
										doubleClickState = true;
										
										
										 CountDownTimer timer = new CountDownTimer(3000, 1000) {  
											  
										 @Override  	       
										 public void onTick(long millisUntilFinished) {    
										        }  
										  
										        @Override  
										        public void onFinish() {  
										        	 Calligraph.staticText.setEnabled(true);  
										        	 
										        	 Spanned text = Html.fromHtml("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;√&nbsp;&nbsp;&nbsp;&nbsp;1&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
										        	 		"乄&nbsp;&nbsp;&nbsp;&nbsp;0&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
										        	 		"×&nbsp;&nbsp;&nbsp;&nbsp;<font color=red><b>1</b></font>");
										        	 Calligraph.staticText.setText(text);
//										            Calligraph.staticText.setText("  对   1              半对   0          错   0"); 
//										            doubleCount++;
										        }  
										    }.start();  
									}
										break;
									case 2:
									{
										Log.i("doubleClick", "step into doubleCount == 1");
										Calligraph.staticText.setVisibility(VISIBLE);
										Spanned text = Html.fromHtml("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;√&nbsp;&nbsp;&nbsp;&nbsp;1&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
							        	 		"乄&nbsp;&nbsp;&nbsp;&nbsp;0&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
							        	 		"×&nbsp;&nbsp;&nbsp;&nbsp;1");
							        	 Calligraph.staticText.setText(text);
//										Calligraph.staticText.setText("  对   1              半对   0          错   1");
											
										freeBitmap.resetFreeBitmapList();
										Resources res=getResources();
										Bitmap bmp = BitmapFactory.decodeResource(res, R.drawable.t3);
										freeBitmap.addBgPic(bmp);
										changeStateAndSync(0);
										doubleCount++;
										doubleClickState = true;
										
										
										 CountDownTimer timer = new CountDownTimer(3000, 1000) {  
											  
										 @Override  	       
										 public void onTick(long millisUntilFinished) {    
										        }  
										  
										        @Override  
										        public void onFinish() {  
										        	 Calligraph.staticText.setEnabled(true);  
										        	 
										        	 Spanned text = Html.fromHtml("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;√&nbsp;&nbsp;&nbsp;&nbsp;1&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
										        	 		"乄&nbsp;&nbsp;&nbsp;&nbsp;0&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
										        	 		"×&nbsp;&nbsp;&nbsp;&nbsp;<font color=red><b>2</b></font>");
										        	 Calligraph.staticText.setText(text);
//										            Calligraph.staticText.setText("  对   1              半对   0          错   2"); 
//										            doubleCount++;
										        }  
										    }.start();  
									
									}
										break;
										default:
										{
										
											Calligraph.pingyuText.setVisibility(INVISIBLE);
											Calligraph.staticText.setVisibility(GONE);
											freeBitmap.resetFreeBitmapList();
											Resources res=getResources();
											Bitmap bmp = BitmapFactory.decodeResource(res, R.drawable.tiping);
											freeBitmap.addBgPic(bmp);
											changeStateAndSync(0);
											doubleCount++;
											doubleCount=0;
											doubleClickState = true;
										}
											break;
									}
										
									
									
//									if(doubleCount==0){
//									Calligraph.staticText.setVisibility(VISIBLE);
//									Spanned text = Html.fromHtml("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;对&nbsp;&nbsp;&nbsp;&nbsp;0&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
//						        	 		"半对&nbsp;&nbsp;&nbsp;&nbsp;0&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
//						        	 		"错&nbsp;&nbsp;&nbsp;&nbsp;0");
//						        	 Calligraph.staticText.setText(text);
////									Calligraph.staticText.setText("  对   0              半对   0          错   0");
//										
//									freeBitmap.resetFreeBitmapList();
//									Resources res=getResources();
//									Bitmap bmp = BitmapFactory.decodeResource(res, R.drawable.t1);
//									freeBitmap.addBgPic(bmp);
//									changeStateAndSync(0);
//									doubleCount++;
//									doubleClickState = true;
//									
//									
//									 CountDownTimer timer = new CountDownTimer(3000, 1000) {  
//										  
//									 @Override  	       
//									 public void onTick(long millisUntilFinished) {  
//										 //    Calligraph.staticText.setText("  对   1              半对   0          错   0");  
//									        }  
//									  
//									        @Override  
//									        public void onFinish() {  
//									        	 Calligraph.staticText.setEnabled(true);  
//									        	 
//									        	 Spanned text = Html.fromHtml("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;对&nbsp;&nbsp;&nbsp;&nbsp;<font color=red><b>1</b></font>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
//									        	 		"半对&nbsp;&nbsp;&nbsp;&nbsp;0&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
//									        	 		"错&nbsp;&nbsp;&nbsp;&nbsp;0");
//									        	 Calligraph.staticText.setText(text);
////									            Calligraph.staticText.setText("  对   1              半对   0          错   0"); 
////									            doubleCount++;
//									        }  
//									    }.start();  
//									}
//									else if(doubleCount==1){
//										Log.i("doubleClick", "step into doubleCount == 1");
//										Calligraph.staticText.setVisibility(VISIBLE);
//										Spanned text = Html.fromHtml("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;对&nbsp;&nbsp;&nbsp;&nbsp;1&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
//							        	 		"半对&nbsp;&nbsp;&nbsp;&nbsp;0&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
//							        	 		"错&nbsp;&nbsp;&nbsp;&nbsp;0");
//							        	 Calligraph.staticText.setText(text);
////										Calligraph.staticText.setText("  对   1              半对   0          错   0");
//											
//										freeBitmap.resetFreeBitmapList();
//										Resources res=getResources();
//										Bitmap bmp = BitmapFactory.decodeResource(res, R.drawable.t2);
//										freeBitmap.addBgPic(bmp);
//										changeStateAndSync(0);
//										doubleCount++;
//										doubleClickState = true;
//										
//										
//										 CountDownTimer timer = new CountDownTimer(3000, 1000) {  
//											  
//										 @Override  	       
//										 public void onTick(long millisUntilFinished) {    
//										        }  
//										  
//										        @Override  
//										        public void onFinish() {  
//										        	 Calligraph.staticText.setEnabled(true);  
//										        	 
//										        	 Spanned text = Html.fromHtml("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;对&nbsp;&nbsp;&nbsp;&nbsp;<font color=red><b>1</b></font>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
//										        	 		"半对&nbsp;&nbsp;&nbsp;&nbsp;0&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
//										        	 		"错&nbsp;&nbsp;&nbsp;&nbsp;1");
//										        	 Calligraph.staticText.setText(text);
////										            Calligraph.staticText.setText("  对   1              半对   0          错   0"); 
////										            doubleCount++;
//										        }  
//										    }.start();  
//										
//									}
//									else if(doubleCount==2){
//										Calligraph.staticText.setVisibility(VISIBLE);
//										Spanned text = Html.fromHtml("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;对&nbsp;&nbsp;&nbsp;&nbsp;<font color=red><b>1</b></font>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
//							        	 		"半对&nbsp;&nbsp;&nbsp;&nbsp;0&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
//							        	 		"错&nbsp;&nbsp;&nbsp;&nbsp;0");
//							        	 Calligraph.staticText.setText(text);
////										Calligraph.staticText.setText("  对  1              半对   0          错   0");
//										freeBitmap.resetFreeBitmapList(); 
//										Resources res=getResources();
//										Bitmap bmp = BitmapFactory.decodeResource(res, R.drawable.t2);
//										freeBitmap.addBgPic(bmp);
//										changeStateAndSync(0);
//										doubleCount++;
//										doubleClickState = true;
//									}
//									else if(doubleCount==3){
//										Calligraph.staticText.setVisibility(VISIBLE);
//										Spanned text = Html.fromHtml("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;对&nbsp;&nbsp;&nbsp;&nbsp;1&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
//							        	 		"半对&nbsp;&nbsp;&nbsp;&nbsp;0&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
//							        	 		"错&nbsp;&nbsp;&nbsp;&nbsp;<font color=red><b>1</b></font>");
//							        	 Calligraph.staticText.setText(text);
////										Calligraph.staticText.setText("  对  1              半对   0          错   1");
////										freeBitmap.resetFreeBitmapList();
////										Resources res=getResources();
////										Bitmap bmp = BitmapFactory.decodeResource(res, R.drawable.t4);
////										freeBitmap.addBgPic(bmp);
////										changeStateAndSync(0);
//										doubleCount++;
//										doubleClickState = true;
//									}
//									else if(doubleCount==4){
//										Calligraph.staticText.setVisibility(VISIBLE);
//										Spanned text = Html.fromHtml("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;对&nbsp;&nbsp;&nbsp;&nbsp;1&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
//							        	 		"半对&nbsp;&nbsp;&nbsp;&nbsp;0&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
//							        	 		"错&nbsp;&nbsp;&nbsp;&nbsp;<font color=red><b>1</b></font>");
//							        	 Calligraph.staticText.setText(text);
////										Calligraph.staticText.setText("  对  1              半对   0          错   1");
//										freeBitmap.resetFreeBitmapList();
//										Resources res=getResources();
//										Bitmap bmp = BitmapFactory.decodeResource(res, R.drawable.t3);
//										freeBitmap.addBgPic(bmp);
//										changeStateAndSync(0);
//										doubleCount++;
//										doubleClickState = true;
//									}
//									else if(doubleCount==5){
//										Calligraph.staticText.setVisibility(VISIBLE);
//										Spanned text = Html.fromHtml("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;对&nbsp;&nbsp;&nbsp;&nbsp;1&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
//							        	 		"半对&nbsp;&nbsp;&nbsp;&nbsp;0&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
//							        	 		"错&nbsp;&nbsp;&nbsp;&nbsp;<font color=red><b>2</b></font>");
//							        	 Calligraph.staticText.setText(text);
//										Calligraph.staticText.setText("  对  1              半对   0          错   2");
////										freeBitmap.resetFreeBitmapList();
////										Resources res=getResources();
////										Bitmap bmp = BitmapFactory.decodeResource(res, R.drawable.t6);
////										freeBitmap.addBgPic(bmp);
////										changeStateAndSync(0);
//										doubleCount++;
//										doubleClickState = true;
//								
//									}
//									else{
//										Calligraph.staticText.setVisibility(GONE);
//										freeBitmap.resetFreeBitmapList();
//										Resources res=getResources();
//										Bitmap bmp = BitmapFactory.decodeResource(res, R.drawable.t3da);
//										freeBitmap.addBgPic(bmp);
//										changeStateAndSync(0);
//										doubleCount++;
//										doubleCount=0;
//										doubleClickState = true;
//									}
								} else {
									doubleClickState = true;
									//关闭图
							//		freeBitmap.resetFreeBitmapList();
							//		freeBitmap.addBgPic(bgImage);
									changeStateAndSync(0);					
								}

								
							} else {
								clickTimes.remove(0);
							}
						}
						Log.i("slide", "MyView onTouchEvent action down");
						touchMode.touch_action_down(event);	
						break;
					case MotionEvent.ACTION_POINTER_DOWN:   //非第一个触摸点按下
			//			Log.e("state", "MyView OnTouchEvent pointer down");
						Log.i("slide", "MyView onTouchEvent action pointer down");
						touchMode.touch_action_pointer_down(event);
						if (drawStatus == STATUS_DRAW_FREE) {
							touchMode = freeDragMode;
						} else if(touchMode != cursorScaleMode){
							touchMode.clear();
							touchMode = cursorScaleMode;
						}
						touchMode.touch_action_pointer_down(event);
						
						break;
					case MotionEvent.ACTION_MOVE:    //移动
						Log.i("slide", "MyView onTouchEvent action move");
						Log.i(TAG, "touch move");
						
						touchMode.touch_move(event); 
						
						invalidate();
						
						break;
					case MotionEvent.ACTION_POINTER_UP:    //非第一个触摸点抬起
						Log.i("slide", "MyView onTouchEvent action pointer up");
						touchMode.touch_action_pointer_up(event);
						break;
					case MotionEvent.ACTION_UP:            //抬起
						Log.i("slide", "MyView onTouchEvent action up");
			//			 Log.i(TAG, "action up" + System.currentTimeMillis());
						touchMode.touch_up(event);
						break;
				}		
		}
		

		//}
		if(SlideMode==0)return false;
		else return true;
	}
	
	

	public void setTouchMode(TouchMode mode) {
		touchMode = mode;
	}

	public TouchMode getTouchMode() {
		return touchMode;
	}

	public TouchMode getStartMode() {
		return startMode;
	}

	public TouchMode getSideDownMode() {
		return sideDownMode;
	}

	public TouchMode getHandWriteMode() {
		return handWriteMode;
	}

	public TouchMode getFreeDragMode() {
		return freeDragMode;
	}

	public TouchMode getFreeScaleMode() {
		return freeScaleMode;
	}

	public TouchMode getFreeNullMode() {
		return freeNullMode;
	}

	public TouchMode getCursorNullMode() {
		return cursorNullMode;
	}

	public TouchMode getCursorScaleMode() {
		return cursorScaleMode;
	}

	public TouchMode getCursorPullMode() {
		return cursorPullMode;
	}
	
	public TouchMode getImageSlideMode(){
		return imageSlideMode;
	}
	public TouchMode getMindSlideMode(){
		return mindSlideMode;
	}

	public void saveFile(Bitmap b, String filename) {
		try {
			Log.i("caoheng", "savefile2");
			Log.d(TAG, "saveBitmapFile >>>>>>>>>>>>>>>>>>>>>>>");
			BufferedOutputStream bos = new BufferedOutputStream(
					new FileOutputStream(new File(FILE_PATH_HEADER + "/"
							+ filename)));
			Log.i("caoheng", "" + FILE_PATH_HEADER + "/" + filename);
			// mBaseImpl.syncFromPartToMain();
			// view.pickBitmap();
			b.compress(Bitmap.CompressFormat.JPEG, 80, bos);
			bos.flush();

			bos.close();

		} catch (Exception e) {

		}
	}

	public void saveFile(Bitmap b, String picPath, String type) {
		Log.i("caoheng","savefile3");
		CompressFormat format = null;
		if ("PNG".equals(type)) {
			format = Bitmap.CompressFormat.PNG;
		} else if ("JPEG".equals(type)) {
			format = Bitmap.CompressFormat.JPEG;
		}

		try {

			Log.d(TAG, "saveBitmapFile >>>>>>>>>>>>>>>>>>>>>>>");
			BufferedOutputStream bos = new BufferedOutputStream(
					new FileOutputStream(new File(picPath)));
			// mBaseImpl.syncFromPartToMain();
			// view.pickBitmap();
			b.compress(format, 50, bos);
			bos.flush();

			bos.close();
			
			

		} catch (Exception e) {
			//空间不足
			Log.e("savefile", "", e);
		}
	}
	
 

	public void saveRectF(String path, RectF rectf) {
		try {
			File rFile = new File(path);
			// if(!rFile.exists())
			// rFile.createNewFile();

			FileOutputStream out = new FileOutputStream(rFile);
			out.write(rectf.toString().getBytes());
			out.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void changeBackground(int bg) {

		final String[] Scene = getContext().getResources().getStringArray(
				R.array.Scene_bg);
		new AlertDialog.Builder(getContext()).setTitle("请选择场景")
				.setItems(Scene, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						String selectedString = Scene[which];
						String type = "";
						if (selectedString.equals("日记")) {
							type = WolfTemplateUtil.DIARY;
						} else if (selectedString.equals("记事本")) {
							type = WolfTemplateUtil.NOTEBOOK;
						} else if (selectedString.equals("会议记录")) {
							type = WolfTemplateUtil.MEETINGS;
						} else if (selectedString.equals("通讯录")) {
							type = WolfTemplateUtil.CONTACTS;
						} else if (selectedString.equals("字帖")) {
							type = WolfTemplateUtil.COPYBOOK;
						} else if (selectedString.equals("临摹")) {
							type = WolfTemplateUtil.COPY;
						} else if (selectedString.equals("竖版临摹")) {

						} else if (selectedString.equals("临摹1")) {

						} else if (selectedString.equals("临摹2")) {
							// type = WolfTemplateUtil.COPYBOOK2;
						} else if (selectedString.equals("临摹3")) {
							type = WolfTemplateUtil.COPYBOOK3;
						} else if (selectedString.equals("临摹4")) {
							type = WolfTemplateUtil.COPYBOOK4;
						} else if (selectedString.equals("临摹5")) {
							type = WolfTemplateUtil.COPYBOOK5;
						} else if (selectedString.equals("临摹6")) {
							type = WolfTemplateUtil.COPYBOOK6;
						}

						doChangeBackground(type);

					}
				}).setNegativeButton("取消", null).show();

		invalidate();
	}

	AlertDialog.Builder builder;

	public void deleteConfirm(final int pagenum, final PopupWindow pop) {

		builder = new AlertDialog.Builder(getContext()).setTitle("删除记录");
		builder.setPositiveButton("确定", new AlertDialog.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				// 删除
				System.out.println("size:" + itemList.size() + " pagenum-1:"
						+ (pagenum - 1));

				Toast.makeText(getContext(), "删除！" + pagenum, Toast.LENGTH_LONG)
						.show();
				CDBPersistent db = new CDBPersistent(getContext());
				db.open();
				db.deletePage(pagenum);

				imagecursor = db.getBitmapPath();

				if (pagenum <= Start.getPageNum()) {
					Start.delPageNum();

					// 按page换模板

					int template_byPage = db.getTemplateByPage(Start
							.getPageNum());
					doChangeBackground(WolfTemplateUtil
							.getTypeByID(template_byPage));

					// 读取该页内容
					for (int i = 0; i < CursorDrawBitmap.listEditableCalligraphy
							.size(); i++) {
						CursorDrawBitmap.listEditableCalligraphy.get(i)
								.initDatabaseCharList();
					}

					cursorBitmap.updateHandwriteState();
//					setFreeDrawBitmap();

				}
				db.close();

				File file = new File(FILE_PATH_HEADER + "/calldir/bitmap_"
						+ pagenum + ".png");
				if (file.exists())
					file.delete();

				Start.resetTotalPagenum();

				dialog.dismiss();
				pop.dismiss();
				openDirectory();
			}
		});
		builder.setNegativeButton("取消", new AlertDialog.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				Toast.makeText(getContext(), "取消！" + pagenum, Toast.LENGTH_LONG)
						.show();
				dialog.dismiss();
			}
		});
		builder.show();

	}

	public void doChangeBackground(String type, boolean flag) {
		if (Start.status.isNeedSave() || Start.PAGENUM <= Start.totlePageNum)
			Start.c.addNewPage();
		doChangeBackground(type);
	}

	public void doChangeBackground(String type) {

		boolean flag = !type.equals(WolfTemplateUtil.getCurrentTemplate()
				.getName());
		// Start.c.addNewPage();
		
		//ly
		//注视点看效果
		if (flag) {
//			Log.e("oom", "not equals!!!!!!!!!!!!!!!!!!!!!!!");
//			final Canvas canvas = new Canvas();
//			canvas.setBitmap(mBitmap);
//
//			mTemplate = WolfTemplateUtil.getTemplateByType(type);
//			if (mTemplate == null)
//				return;
//			WolfTemplateUtil.changeCurrentTemplate(type);
//			Log.e(TAG,
//					"!!!!!!!!!!!!!la" + type + " "
//							+ (mTemplate.getAvailables() == null));
//
//			String path = WolfTemplateUtil.TEMPLATE_PATH + mTemplate.getName()
//					+ "/" + mTemplate.getBackground();
//
//			changeDrawState(mTemplate.getFormat());
//			// if(mTemplate.getFormat() == MyView.STATUS_DRAW_CURSOR)
//			// cursorBitmap.initListEditableCalligraphy(mTemplate);
//			changePenState(mTemplate.getPentype());
//
//			Bitmap b = null;
//			try {
//				b = BitmapFactory.decodeFile(path);
//				BitmapCount.getInstance().createBitmap("MyView decode mTemplate.getBackground()");
//				
//				Log.e("template", path);
//				canvas.drawBitmap(b, 0, 0, baseBitmap.paint);
//				canvas.drawBitmap(b, Start.SCREEN_WIDTH, 0, baseBitmap.paint);
//				canvas.setBitmap(cursorBitmap.bitmap);
//				canvas.drawBitmap(b, 0, 0, new Paint());
//
//			} catch (OutOfMemoryError e) {
//				// TODO: handle exception
//				Log.e("AndroidRuntime", "MyView doChangeBackground() OOM !!!!");
//			}
//			b.recycle();//竖版，此处崩溃null
//			BitmapCount.getInstance().recycleBitmap("MyView doChangeBackground template b");
//
//			// add by mouse
//			canvas.setBitmap(Calligraph.mScaleBitmap);
//			canvas.drawColor(Color.BLACK);

		} else {
			Log.e("oom", "equals!!!!!!!!!!!!!!!!!!!!!!!");
		}

		switch (drawStatus) {
		case STATUS_DRAW_CURSOR:
			// canvas.drawBitmap(b, Start.SCREEN_WIDTH, 0, baseBitmap.paint);
			// canvas.setBitmap(cursorBitmap.bitmap);
			// canvas.drawBitmap(b, 0, 0, new Paint());
//			for (int i = 0; i < cursorBitmap.listEditableCalligraphy.size(); i++) {
//				cursorBitmap.listEditableCalligraphy.get(i).clear();
//			}
			cursorBitmap.clearDataBitmap();
			// cursorBitmap.cal_current.clear();

			// 以下两句换页时引起内存不足
			if (flag) {
				cursorBitmap.initListEditableCalligraphy(mTemplate);
				baseBitmap.updateAvailables();
			} else {
				cursorBitmap.initDate(WolfTemplateUtil.getCurrentTemplate());
			}
			// Calligraph.mDragEnableBtn.setVisibility(View.GONE);
			Calligraph.mHandwriteDelBtn.setVisibility(View.VISIBLE);
			Calligraph.mHandwriteEndofLineBtn.setVisibility(View.VISIBLE);
			Calligraph.mHandwriteNewBtn.setVisibility(View.VISIBLE);
			Calligraph.mHandwriteInsertSpaceBtn.setVisibility(View.VISIBLE);
			Calligraph.mHandWriteUndoBtn.setVisibility(View.VISIBLE);

			break;
		case STATUS_DRAW_FREE:
			// canvas.drawBitmap(b, 0, 0, baseBitmap.paint);

			// Calligraph.mDragEnableBtn.setText("画笔模式");
			// MODE_DRAG = false;
			// MODE_SCALE = false;
			// Calligraph.mDragEnableBtn.setVisibility(View.VISIBLE);
			/*Calligraph.mHandwriteDelBtn.setVisibility(View.INVISIBLE);
			Calligraph.mHandwriteEndofLineBtn.setVisibility(View.INVISIBLE);
			Calligraph.mHandwriteNewBtn.setVisibility(View.INVISIBLE);
			Calligraph.mHandwriteInsertSpaceBtn.setVisibility(View.INVISIBLE);
			Calligraph.mHandWriteUndoBtn.setVisibility(View.INVISIBLE);*/
			Calligraph.mHandwriteDelBtn.setVisibility(View.VISIBLE);
			Calligraph.mHandwriteEndofLineBtn.setVisibility(View.VISIBLE);
			Calligraph.mHandwriteNewBtn.setVisibility(View.VISIBLE);
			Calligraph.mHandwriteInsertSpaceBtn.setVisibility(View.VISIBLE);
			Calligraph.mHandWriteUndoBtn.setVisibility(View.VISIBLE);
			break;
		default:
			break;
		}
//		cursorBitmap.updateHandwriteState();
		invalidate();
	}
	public void saveDrawLine() {

		if (drawStatus == STATUS_DRAW_CURSOR) {
			drawStateSync(STATUS_DRAW_FREE);
			Canvas line_canvas = new Canvas();
			line_canvas.setBitmap(mBitmap);

			Log.e("line", "====================save");
			touchMode.printInfo();
			Matrix m = new Matrix();
			if (touchMode.getMatrix() != null) {
				m.set(touchMode.getMatrix());
			}
			line_canvas.translate(Start.SCREEN_WIDTH, 0);
			
			
//			cursorBitmap.updateHandwriteState();

			baseBitmap.drawBgLine(line_canvas, m);
//			saveFile(mBitmap, FILE_PATH_HEADER + "/calldir/mBitmap.png", "PNG");

			Log.e("line", "====================" + m.toString());

		} else {
			drawStateSync(STATUS_DRAW_CURSOR);
			//try1
			cursorBitmap.updateHandwriteState();
			//freeBitmap.resetFreeBitmapList();
			
		}
	}
	
	//ly
	//这是原始的saveDatebase
	//被我给注释掉了，呵呵
	public boolean saveDatebase()
	{
		Log.i("caoheng", "saveDateBase");
		//saveFreeDrawBitmap();
//		Bitmap b1 = baseBitmap.bitmap;
		saveDrawLine();//保存画笔痕迹。出现上一张痕迹的代码在这个函数里这个函数必须有
	//	cursorBitmap.updateHandwriteState();
		freeBitmap.drawFreeBitmapSync();//删掉之后只能摁两次保存
	   
	
	    
		
		
		
		
		//changeDrawState(MyView.STATUS_DRAW_CURSOR);
	//	invalidate();
		
//		Bitmap background = freeBitmap.mBitmap;
//		freeBitmap.updateToBitmapList();
		Bitmap b2 = freeBitmap.mBitmap;
	//	mCanvas.setBitmap(mBitmap);
		
		b2.setWidth(1600);

		
//		String dir1 =  FILE_PATH_HEADER + "/calldir/baseBitmapbitmap.png";
		time = System.currentTimeMillis();
		Log.i("0801", "" + time);
		String dir2 =  FILE_PATH_HEADER + "/calldir/" + time + ".png";

//			
//		List<FreeBitmapInfo> list = freeBitmap.getFreeBitmapInfoList();
//		freeBitmap.clearFreeDrawHistory();
//		Bitmap background = list.get(0).bitmap;
//		Bitmap foreground;
//		Bitmap newbmp;  
//        Canvas cv;   
//		for(int i=1; i<list.size(); i++) {
//			  foreground = list.get(i).bitmap;
//			  newbmp = Bitmap.createBitmap(background.getWidth(), background.getHeight(), android.graphics.Bitmap.Config.ARGB_4444); 
//			  cv = new Canvas();
//			  cv.drawBitmap(background, 0, 0, null);
//			  cv.drawBitmap(foreground, list.get(i).rect, list.get(i-1).rect, null);
//			
//		}
//		 
//		
//		Bitmap background = list.get(0).bitmap;
//		Bitmap foreground = list.get(last).bitmap;
//		
//		//b2当作背景drawLine为画的图。合并。
//	      int bgWidth = background.getWidth();   
//         int bgHeight = background.getHeight();   
//
//         //create the new blank bitmap 创建一个新的和SRC长度宽度一样的位图    
//         Bitmap newbmp = Bitmap.createBitmap(bgWidth, bgHeight, android.graphics.Bitmap.Config.ARGB_4444);  
//         Canvas cv = new Canvas(newbmp);   
//         //draw bg into   
//         cv.drawBitmap(background, 0, 0, null);//在 0，0坐标开始画入bg   
//         //draw fg into   
////         cv.drawBitmap(foreground, list.get(last).rect, background.rect, null);//在 0，0坐标开始画入fg ，可以从任意位置画入
//         //save all clip   
//         cv.save(Canvas.ALL_SAVE_FLAG);//保存   
//         //store   
//         cv.restore();//存储   
		
	
	//	saveFreeDrawBitmap();
//		b2.compress(CompressFormat.PNG, 50, new OutputStream(dir2));
		
	    //saveFile(b1, dir1, "PNG");
		saveFile(b2, dir2, "PNG");
		
		
		
		
		
		
//		saveFile(newbmp, dir2,"PNG");
		//saveFile(b3, dir3, "PNG");
		//saveFile(b4, dir4, "PNG");
		//saveFile(b5, dir5, "PNG");
		//saveFile(b6, dir6, "PNG");
		//saveFile(b7, dir7, "PNG");
	//	Bitmap alteredBitmap = freeBitmap.mBitmap;
		//saveDrawLine();
       // alteredBitmap.compress(CompressFormat.PNG, 90, Start.imageFileOS);
//		Start.context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, data));   
		Uri data = Uri.parse("file://" + dir2);
	
		Start.context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, data));   
		
	
		return true;
	}

	//ly
	//这是原始的saveDatebase
	//被我给注释掉了，呵呵
	
	
	
	
	
	
	
	
	
	
	public boolean saveDatebaseBak() {
		
		
		Log.e("databases", "save to databases -------------:");
//		Start.status.resetStatus();
//		if (!Start.status.isNeedSave()) {
//			Start.status.resetStatus();
//			
//			return false;
//		}
		//保存粒度更小了，不再用这个判断；
		
		String dirName = "/calldir/free_" + Start.getPageNum();
		
//		PageData.getInstance().savePageData();
		
//		CalligraphyVectorUtil.instance().saveToFile();
		
		int tempstatus = drawStatus;
		if (tempstatus == STATUS_DRAW_FREE)
		{
			freeBitmap.updateToBitmapList();
			changeDrawState(MyView.STATUS_DRAW_CURSOR);
		}

		// freeBitmap.updateToBitmapList();

		cursorBitmap.initDate(mTemplate);

		File calldir = new File(FILE_PATH_HEADER + dirName);
		boolean created = false;
		if (!calldir.exists())
			created = calldir.mkdirs();
		Log.e("storage", "mkdirs------------------------" + created);

		String bitmapPath = FILE_PATH_HEADER + dirName + "/calligraphy_"
				+ Start.getPageNum() + "_"
				// 记录当前页数，打开的时候需要
				+ WolfTemplateUtil.getCurrentTemplate().getTdirect()
				// 记录当前图片是横版还是竖版，提供缩略图时需要取不同区域
				+ ".jpg";
		
		
		
//		saveDrawLine();// 没有画上

		Log.e("storage", "myview b null------------------------" + (mStorage.b==null));
		Log.i("caoheng", "savefile4");
		saveFile(mStorage.getCurBitmapRef(mStorage.CURSOR), bitmapPath, "JPEG");

		String indexBitmapPath = FILE_PATH_HEADER + dirName + "/index_"
				+ Start.getPageNum() + ".jpg";
		
		Bitmap b;
		try{

			cursorBitmap.drawBitmap(mStorage.indexB, 1, new Matrix(),true);
			Log.i("caoheng", "savefile5");
			saveFile(mStorage.indexB, indexBitmapPath,
					"JPEG");
		}catch (OutOfMemoryError e) {
			// TODO: handle exception
			
		}
//		saveFile(mStorage.getCurIndexBitmapRef(drawStatus), indexBitmapPath,
//				"JPEG");

		CDBPersistent cd = new CDBPersistent(super.getContext());
		cd.open();

		cd.insertBitmapPath(indexBitmapPath);

		EditableCalligraphy e;
		LinkedList<EditableCalligraphyItem> charList;

		int template_id = mTemplate.getId();// 模板id
		// boolean exit = cd.currentPageExit(template_id,
		// Start.getPageNum());//存在则为true
		boolean exit = cd.currentPageExit(Start.getPageNum());// 存在则为true
		if (exit) {
			// 数据库中存在该页数据 ， 询问是否覆盖
			// cd.deletecurrentPage(template_id, Start.getPageNum());
//			cd.deletecurrentPage(Start.getPageNum());
		}

//		cd.insert(template_id, Start.getPageNum(),
//				CursorDrawBitmap.listEditableCalligraphy);// 保存到数据库

		
		cd.close();

		saveFreeDrawBitmap();

//		cursorBitmap.updateHandwriteState();

		Start.status.resetStatus();
		if (tempstatus == STATUS_DRAW_FREE)
		{
			freeBitmap.drawFreeBitmapSync();
			scaleStateSync(MyView.STATUS_DRAW_FREE);
			drawStateSync(MyView.STATUS_DRAW_FREE);
			changeDrawState(MyView.STATUS_DRAW_FREE);
		}


		return true;
	}

	// public void saveFreeDrawBitmap(){
	// String picName = FILE_PATH_HEADER +
	// "/calldir/bitmap_"+Start.getPageNum()+".png";
	// saveFile(baseBitmap.bitmap, picName,"PNG");
	// }
	public void saveFreeDrawBitmap() {
		
		String dirName = "free_" + Start.getPageNum();
		File dir = new File(FILE_PATH_HEADER + "/calldir/" + dirName);
		Log.i( "caoheng", ""+FILE_PATH_HEADER + "/calldir/" + dirName);
		String freeName;
		String rectName;
		if (!dir.exists())
			dir.mkdir();
		else if (dir.isDirectory()) {
			List<FreeBitmapInfo> list = freeBitmap.getFreeBitmapInfoList();
			freeBitmap.clearFreeDrawHistory();
			for (int i = 0; i < list.size(); i++) {
				freeName = dirName + "_" + i + ".png";
				Log.i("0801", "" + freeName);
				rectName = dirName + "_" + i + "_r";
				String freepath = FILE_PATH_HEADER + "/calldir/" + dirName
						+ "/" + freeName;
				String rectpath = FILE_PATH_HEADER + "/calldir/" + dirName
						+ "/" + rectName;
				if (!(new File(freepath)).exists()) {
					Log.i("caoheng", "savefile6");
					saveFile(list.get(i).bitmap, freepath, "PNG");
					Log.i("caoheng", freepath);
					
					saveRectF(rectpath, list.get(i).rect);
				}
			}
		}
	}

	public Uri savePicBitmap(Uri uri,String picName) {
		Log.e("pic", "URI:"	 + uri.getPath());
		String dirName = "free_" + Start.getPageNum();
		File dir = new File(FILE_PATH_HEADER + "/calldir/" + dirName);
		int index = cursorBitmap.cal_current.currentpos;

		if (!dir.exists())
			dir.mkdir();
		else if (dir.isDirectory()) {
			File newFile  = null;
			if(picName == null){
				newFile = new File(dir + "/" + uri.getLastPathSegment());
				Log.e("pic", "name:" + uri.getLastPathSegment()); 
			}
			else
				newFile = new File(dir + "/" + picName);
			
			try {
				FileOutputStream out = new FileOutputStream(newFile);
				InputStream in = Start.context.getContentResolver()
						.openInputStream(uri);

				byte[] buff = new byte[1024];
				int len = 0;
				while ((len = in.read(buff)) > 0) {
					out.write(buff, 0, len);
				}

				in.close();
				out.close();
				
				return Uri.parse("file://" + newFile.getPath());
				
//				return Uri.fromFile(newFile);

			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public Uri savePicBitmap(String path,String picName) {
		String dirName = "free_" + Start.getPageNum();
		File dir = new File(FILE_PATH_HEADER + "/calldir/" + dirName);
		int index = cursorBitmap.cal_current.currentpos;

		Log.e("savePic", "start");
		if (!dir.exists())
			dir.mkdir();
		
		if (dir.isDirectory()) {
			File newFile  = null;
			Log.e("savePic", "dir:" + dir);
				newFile = new File(dir + "/" + picName);
			try {
				FileOutputStream out = new FileOutputStream(newFile);
//				InputStream in = Start.context.getContentResolver()
//						.openInputStream(uri);
				InputStream in = null;
				in = new FileInputStream(path);

				byte[] buff = new byte[256];
				int len = 0;
				while ((len = in.read(buff)) > 0) {
					out.write(buff, 0, len);
				}

				in.close();
				out.close();
				Log.e("savePic", "all right return uri:" + Uri.fromFile(newFile));
				return Uri.fromFile(newFile);

			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.e("savePic", "file not found");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.e("savePic", "IOException");
			}
		}
		
		Log.e("savePic", "return null");
		return null;
	}

//	public void setFreeDrawBitmap() {
//
//		String picpath = FILE_PATH_HEADER + "/calldir/bitmap_"
//				+ Start.getPageNum() + ".png";
//		File file = new File(picpath);
//
//		if (file.exists()) {
//			// cursorBitmap.setBbitmap(BitmapFactory.decodeFile(picpath).copy(Bitmap.Config.ARGB_4444,
//			// true));
//
//			BitmapFactory.Options opt = new BitmapFactory.Options();
//			opt.inPreferredConfig = Bitmap.Config.ARGB_4444;
//			opt.inPurgeable = true;
//			opt.inInputShareable = true;
//			// 获取资源图片
//			InputStream is = null;
//			try {
//				is = new FileInputStream(picpath);
//				Bitmap tmp = BitmapFactory.decodeStream(is, null, opt);// 第二次才崩
//
//				// Bitmap tmp = BitmapFactory.decodeFile(picpath);
//				cursorBitmap.setBbitmap(tmp);
//				tmp.recycle();
//				System.gc();// 没有用
//				cursorBitmap.updateHandwriteState();
//				Log.e("bitmap", "exit");
//			} catch (FileNotFoundException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (OutOfMemoryError e) {
//
//				// The VM does not always free-up memory as it should,
//
//				// so manually invoke the garbage collector
//
//				// and try loading the image again.
//
//				System.gc();
//
//				Log.e("AndroidRuntime", "MyView setFreeBitmap() OOM!!!");
//
//			}
//
//		} else {
//			Log.e("bitmap", "not exit" + picpath);
//		}
//
//	}

	public void print() {
		Log.i(TAG, "drawStatus:" + drawStatus);
		Log.i(TAG, "penStatus:" + penStatus);
	}

	//ly
	//用于加入涂鸦态默认的背景图
	public void addFreeBg(Bitmap mbackgroundBitmap){
		if(isLoad == false){
			//String freeBg = "/mnt/sdcard/calliPics/1.jpg";
			Bitmap bgBitmap = null;
			BitmapFactory.Options options = new BitmapFactory.Options();
			//options.inJustDecodeBounds = true;
			//BitmapFactory.decodeFile(freeBg,options); 
			
			//int heightRatio = (int)Math.ceil(options.outHeight/(float)2560);  
	       // int widthRatio = (int)Math.ceil(options.outWidth/(float)1600);  
//	        if (heightRatio > 1 && widthRatio > 1)  
//	        {  
//	        	options.inSampleSize =  heightRatio > widthRatio ? heightRatio:widthRatio;  
//	        }  
			options.outWidth = 1600;
			options.outHeight = 2560;
	
	        options.inJustDecodeBounds = false;  
	        
	        bgBitmap = mbackgroundBitmap;
			//bgBitmap = BitmapFactory.decodeStream(new FileInputStream(freeBg), null,  options);
			//bgBitmap = BitmapFactory.decodeStream(, null,  options);
			Bitmap bg = Bitmap.createScaledBitmap(bgBitmap, 1600, 2560, true);
			freeBitmap.addBgPic(bg);
			bgBitmap.recycle();  
	        isLoad = true;
			
		}
		//end
	}
	//end
	
	//ly
	//用于加入涂鸦态默认的背景图
//	public void addFreeBg(){
//		Log.i("caoheng", "addFreeBg()");
//		if(isLoad == false){
//			Log.i("caoheng", "loadFalse");
//			String freeBg = "/mnt/sdcard/calliPics/1.jpg";
//			Bitmap bgBitmap = null;
//			BitmapFactory.Options options = new BitmapFactory.Options();
//			//options.inJustDecodeBounds = true;
//			//BitmapFactory.decodeFile(freeBg,options); 
//			
//			//int heightRatio = (int)Math.ceil(options.outHeight/(float)2560);  
//	       // int widthRatio = (int)Math.ceil(options.outWidth/(float)1600);  
////	        if (heightRatio > 1 && widthRatio > 1)  
////	        {  
////	        	options.inSampleSize =  heightRatio > widthRatio ? heightRatio:widthRatio;  
////	        }  
//			options.outWidth = 1600;
//			options.outHeight = 2560;
//	
//	        options.inJustDecodeBounds = false;  
//	        
//			try {
//				Log.i("caoheng", "before bgBitmap");
//				bgBitmap = BitmapFactory.decodeStream(new FileInputStream(freeBg), null,  options);
//				Log.i("caoheng", "bgBitmap");
//				Bitmap bg = Bitmap.createScaledBitmap(bgBitmap, 1600, 2560, true);
//				freeBitmap.addBgPic(bg);		
//				bgBitmap.recycle();
//			} catch (FileNotFoundException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			//bgBitmap = BitmapFactory.decodeStream(, null,  options);
//  
//	        isLoad = true;
//			
//		}
//		//end
//	}
	
	
	
	//caoheng 2015.11.11

		//用于加入涂鸦态默认的背景图
		public void addFreeBg(){
			Log.i("caoheng", "addFreeBg()");
			if(isLoad == false){
				Log.i("caoheng", "loadFalse");
//				String[] proj = new String[]{
//						MediaStore.Images.ImageColumns.DATE_MODIFIED
//				};
				Start.picList.clear();
				Start.picCursor.clear();
				Start.picName.clear();
				Start.picListIndex = 0;
				Start.picCursorIndex = 0;
				Start.picNameIndex = 0;
				
				Cursor cursor = Start.context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,null, null, null,MediaStore.Images.Media.DATE_MODIFIED);
			
				String path;
				int index;
				String name;
				Uri picUri;
				
				for(cursor.moveToLast(); !cursor.isBeforeFirst(); cursor.moveToPrevious())
				{
					//Log.e("addmorepic", "inside for");
					path = cursor.getString(1);
					path = path.substring(0, 25);
					//Log.e("addmorepic", path);

					//Log.e("addmorepic", "found it, path = " + cursor.getString(1));
					//Log.e("addmorepic", "name = " + cursor.getString(0));
					index = cursor.getColumnIndex(Images.ImageColumns._ID);
					index = cursor.getInt(index);
					name = cursor.getString(3);
					Log.e("addmorepic", "index = " + index);
					picUri = Uri.parse("content://media/external/images/media/" + index);

					Start.picList.add(picUri);
					Start.picCursor.add(cursor);
					Start.picName.add(name);
				}
				cursor.close();	
		
				//String freeBg = "/mnt/sdcard/calliPics/1.jpg";
				Uri BgPic = Start.picList.get(0);
				bgName = Start.picName.get(0);
				
				Bitmap bgBitmap = null;
				BitmapFactory.Options options = new BitmapFactory.Options();
//				options.inJustDecodeBounds = true;
//				BitmapFactory.decodeFile(freeBg,options); 
//				
//				int heightRatio = (int)Math.ceil(options.outHeight/(float)2560);  
//		        int widthRatio = (int)Math.ceil(options.outWidth/(float)1600);  
//		        if (heightRatio > 1 && widthRatio > 1)  
//		        {  
//		        	options.inSampleSize =  heightRatio > widthRatio ? heightRatio:widthRatio;  
//		        }  
				options.outWidth = 1600;
				options.outHeight = 2560;
		
		        options.inJustDecodeBounds = false;  
		        
				try {
					Log.i("caoheng", "before bgBitmap1");
					//bgBitmap = BitmapFactory.decodeStream(new FileInputStream(freeBg), null,  options);
					bgBitmap = MediaStore.Images.Media.getBitmap(Start.context.getContentResolver(), BgPic);
					Log.i("caoheng", "bgBitmap");
					Bitmap bg = null;
					
					if(bgBitmap.getWidth() <= 1600) {
						bg = Bitmap.createScaledBitmap(bgBitmap, 1600, 2560, true);
					} else {
						float ratio = bgBitmap.getWidth() / (float) 1600;
						bg = Bitmap.createScaledBitmap(bgBitmap, 1600, (int) ((int)bgBitmap.getHeight() / ratio), true);
						
					}
					
					freeBitmap.addBgPic(bg);	
//					  Bitmap[] getBitmap = findPic();
//				        foreImage = getBitmap[0];
////						mCanvas1 = new Canvas(foreImage);
//				        if(getBitmap[1] != null) {
//				        	bgImage = getBitmap[1];
//				        } else {
//				        	SlideMode = 0;
//				        }
				        
						  picAndName[] getPicAndName = findPicAndName();
//						  Bitmap[] getBitmap = findPic();
//					        foreImage = getBitmap[0];
						  foreImage = getPicAndName[0].getBitmap();
						  bgName = getPicAndName[0].getName();
//							mCanvas1 = new Canvas(foreImage);
//					        if(getBitmap[1] != null) {
						  if(getPicAndName[1].getBitmap() != null){
//					        	bgImage = getBitmap[1];
							  bgImage = getPicAndName[1].getBitmap();
							  bgName = getPicAndName[1].getName();
					        } else {
					        	SlideMode = 0;
					        }
				        
				        
				        
				        
					bgBitmap.recycle();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//bgBitmap = BitmapFactory.decodeStream(, null,  options);
	  
		        isLoad = true;
				
			}
			//end
	        
		}

	
//	public void addNextPic(){
//		Log.i("addmorepic", "add next pic in myview");
//		int nextIndex = Start.picNameIndex + 1;
//		
//		if(nextIndex < Start.picName.size())
//		{
//			Start.picNameIndex = nextIndex;
//			Log.i("addmorepic", "nextIndex = "+ Start.picNameIndex);
//			String nextBg = "/mnt/sdcard/mypic/" + Start.picName.get(nextIndex);
//			Log.e("addmorepic", "nextBg: " + nextBg);
//			 
//			Bitmap bgBitmap = null;
//			BitmapFactory.Options options = new BitmapFactory.Options();
//
//			options.outWidth = 1600;
//			options.outHeight = 2560;
//	
//	        options.inJustDecodeBounds = false;  
//	        
//			try {
//				bgBitmap = BitmapFactory.decodeStream(new FileInputStream(nextBg), null,  options);
//				Bitmap bg = Bitmap.createScaledBitmap(bgBitmap, 1600, 2560, true);
//				freeBitmap.resetFreeBitmapList();
//				freeBitmap.addBgPic(bg);
//				changeStateAndSync(0);
//				//bgBitmap.recycle();
//			} catch (FileNotFoundException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			//bgBitmap = BitmapFactory.decodeStream(, null,  options);
//
//		} else {
//			my_toast("已经是最后一张");
//		}
//
//	}
		
		
		public Bitmap[] findPic() {
			Bitmap[] forAndNextImage = new Bitmap[2];
			Log.i("fanye", "add next pic in myview" + Start.picListIndex);
			//int nextIndex = Start.picNameIndex + 1;
			Uri forPicUri = Start.picList.get(Start.picListIndex);
			Bitmap forbgBitmap = null;
			BitmapFactory.Options foroptions = new BitmapFactory.Options();

			foroptions.outWidth = 1600;
			foroptions.outHeight = 2560;
	
	        foroptions.inJustDecodeBounds = false;  
	        
			try {
				//bgBitmap = BitmapFactory.decodeStream(new FileInputStream(nextBg), null,  options);
				forbgBitmap = MediaStore.Images.Media.getBitmap(Start.context.getContentResolver(), forPicUri);
				
				Bitmap bg = null;
				
				if(forbgBitmap.getWidth() <= 1600) {
					bg = Bitmap.createScaledBitmap(forbgBitmap, 1600, 2560, true);
				} else {
					float ratio = forbgBitmap.getWidth() / (float) 1600;
					bg = Bitmap.createScaledBitmap(forbgBitmap, 1600, (int) ((int)forbgBitmap.getHeight() / ratio), true);
					
				}
			
				forAndNextImage[0] = bg;
				//bgBitmap.recycle();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			
			int nextIndex = Start.picListIndex + 1;
			
			if(nextIndex < Start.picName.size())
			{
				//Start.picNameIndex = nextIndex;
				Start.picListIndex = nextIndex;
				Log.i("addmorepic", "nextIndex = "+ Start.picListIndex);
				Uri nextPicUri = Start.picList.get(nextIndex);
//				nextBg = Start.picName.get(nextIndex);
//				Log.e("addmorepic", "nextBg: " + nextBg);
				
				Bitmap bgBitmap = null;
				BitmapFactory.Options options = new BitmapFactory.Options();

				options.outWidth = 1600;
				options.outHeight = 2560;
		
		        options.inJustDecodeBounds = false;  
		        
				try {
					//bgBitmap = BitmapFactory.decodeStream(new FileInputStream(nextBg), null,  options);
					bgBitmap = MediaStore.Images.Media.getBitmap(Start.context.getContentResolver(), nextPicUri);
					
					Bitmap bg = null;
					
					if(bgBitmap.getWidth() <= 1600) {
						bg = Bitmap.createScaledBitmap(bgBitmap, 1600, 2560, true);
					} else {
						float ratio = forbgBitmap.getWidth() / (float) 1600;
						bg = Bitmap.createScaledBitmap(forbgBitmap, 1600, (int) ((int)forbgBitmap.getHeight() / ratio), true);
						
					}
					forAndNextImage[1] = bg;
					//bgBitmap.recycle();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//bgBitmap = BitmapFactory.decodeStream(, null,  options);

			} else {
				my_toast("已经是最后一张");
			}			
			return forAndNextImage;
		}
		
		public picAndName[] findPicAndName() {
			picAndName[] forAndNextImage = new picAndName[2];
			Log.i("fanye", "add next pic in myview" + Start.picListIndex);
			//int nextIndex = Start.picNameIndex + 1;
			Uri forPicUri = Start.picList.get(Start.picListIndex);
			Bitmap forbgBitmap = null;
			BitmapFactory.Options foroptions = new BitmapFactory.Options();

			foroptions.outWidth = 1600;
			foroptions.outHeight = 2560;
	
	        foroptions.inJustDecodeBounds = false;  
	        
			try {
				//bgBitmap = BitmapFactory.decodeStream(new FileInputStream(nextBg), null,  options);
				forbgBitmap = MediaStore.Images.Media.getBitmap(Start.context.getContentResolver(), forPicUri);
				
				Bitmap bg = null;
				
				if(forbgBitmap.getWidth() <= 1600) {
					bg = Bitmap.createScaledBitmap(forbgBitmap, 1600, 2560, true);
				} else {
					float ratio = forbgBitmap.getWidth() / (float) 1600;
					bg = Bitmap.createScaledBitmap(forbgBitmap, 1600, (int) ((int)forbgBitmap.getHeight() / ratio), true);
					
				}
			
				forAndNextImage[0] = new picAndName(bg, Start.picName.get(Start.picListIndex));
				//bgBitmap.recycle();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			
			int nextIndex = Start.picListIndex + 1;
			
			if(nextIndex < Start.picName.size())
			{
				//Start.picNameIndex = nextIndex;
				Start.picListIndex = nextIndex;
				Log.i("addmorepic", "nextIndex = "+ Start.picListIndex);
				Uri nextPicUri = Start.picList.get(nextIndex);
				String nextBg = Start.picName.get(nextIndex);
//				Log.e("addmorepic", "nextBg: " + nextBg);
				
				Bitmap bgBitmap = null;
				BitmapFactory.Options options = new BitmapFactory.Options();

				options.outWidth = 1600;
				options.outHeight = 2560;
		
		        options.inJustDecodeBounds = false;  
		        
				try {
					//bgBitmap = BitmapFactory.decodeStream(new FileInputStream(nextBg), null,  options);
					bgBitmap = MediaStore.Images.Media.getBitmap(Start.context.getContentResolver(), nextPicUri);
					
					Bitmap bg = null;
					
					if(bgBitmap.getWidth() <= 1600) {
						bg = Bitmap.createScaledBitmap(bgBitmap, 1600, 2560, true);
					} else {
						float ratio = forbgBitmap.getWidth() / (float) 1600;
						bg = Bitmap.createScaledBitmap(forbgBitmap, 1600, (int) ((int)forbgBitmap.getHeight() / ratio), true);
						
					}
					forAndNextImage[1] = new picAndName(bg, nextBg);
					//bgBitmap.recycle();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//bgBitmap = BitmapFactory.decodeStream(, null,  options);

			} else {
				my_toast("已经是最后一张");
			}			
			return forAndNextImage;
		}
		
		public void addNextPic(){
			Log.i("addmorepic", "add next pic in myview");
			//int nextIndex = Start.picNameIndex + 1;
			int nextIndex = Start.picListIndex + 1;
			
			if(nextIndex < Start.picName.size())
			{
				//Start.picNameIndex = nextIndex;
				Start.picListIndex = nextIndex;
				Log.i("addmorepic", "nextIndex = "+ Start.picListIndex);
				Uri nextPicUri = Start.picList.get(nextIndex);
				//String nextBg = "/mnt/sdcard/mypic/" + Start.picName.get(nextIndex);
				//Log.e("addmorepic", "nextBg: " + nextBg);
				
				Bitmap bgBitmap = null;
				BitmapFactory.Options options = new BitmapFactory.Options();

				options.outWidth = 1600;
				options.outHeight = 2560;
		
		        options.inJustDecodeBounds = false;  
		        
				try {
					//bgBitmap = BitmapFactory.decodeStream(new FileInputStream(nextBg), null,  options);
					bgBitmap = MediaStore.Images.Media.getBitmap(Start.context.getContentResolver(), nextPicUri);
					Bitmap bg = null;
					
					if(bgBitmap.getWidth() <= 1600) {
						bg = Bitmap.createScaledBitmap(bgBitmap, 1600, 2560, true);
					} else {
						float ratio = bgBitmap.getWidth() / (float) 1600;
						bg = Bitmap.createScaledBitmap(bgBitmap, 1600, (int) ((int)bgBitmap.getHeight() / ratio), true);
						
					}
					freeBitmap.resetFreeBitmapList();
					freeBitmap.addBgPic(bg);
					changeStateAndSync(0);
					//bgBitmap.recycle();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//bgBitmap = BitmapFactory.decodeStream(, null,  options);

			} else {
				my_toast("已经是最后一张");
			}
			invalidate();

		}
		//以前的图片 2015.11.11 caoheng
		public void addPreviousPic(){
			
			Log.i("addmorepic", "add next pic in myview");
			//int nextIndex = Start.picNameIndex + 1;
			if(Start.picListIndex == 0)
			{
			my_toast("已经是第一张");
			} else{
				int previousIndex = Start.picListIndex - 1;
				 Start.picListIndex = previousIndex;
		//	Start.picListIndex = previousIndex;
			Log.i("addmorepic121", "previousIndex = "+ previousIndex);
			Uri previousPicUri = Start.picList.get(previousIndex);
			
			Bitmap bgBitmap = null;
			BitmapFactory.Options options = new BitmapFactory.Options();

			options.outWidth = 1600;
			options.outHeight = 2560;

			        options.inJustDecodeBounds = false;  
			        
			try {
			//bgBitmap = BitmapFactory.decodeStream(new FileInputStream(nextBg), null,  options);
			bgBitmap = MediaStore.Images.Media.getBitmap(Start.context.getContentResolver(), previousPicUri);
			Bitmap bg = null;
			
			if(bgBitmap.getWidth() <= 1600) {
				bg = Bitmap.createScaledBitmap(bgBitmap, 1600, 2560, true);
			} else {
				float ratio = bgBitmap.getWidth() / (float) 1600;
				bg = Bitmap.createScaledBitmap(bgBitmap, 1600, (int) ((int)bgBitmap.getHeight() / ratio), true);
				
			}
			freeBitmap.resetFreeBitmapList();
			freeBitmap.addBgPic(bg);
			changeStateAndSync(0);
			//bgBitmap.recycle();
			} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			}
			}	
			invalidate();
			}
	
	public void share() {
		// if(drawStatus == STATUS_DRAW_CURSOR) {
		// changeStateAndSync(MyView.STATUS_DRAW_FREE);
		// changeStateAndSync(MyView.STATUS_DRAW_CURSOR);
		// } else {
		// changeStateAndSync(MyView.STATUS_DRAW_CURSOR);
		// changeStateAndSync(MyView.STATUS_DRAW_FREE);
		// }

		
		
		saveDrawLine();

//		Bitmap b = ShareLogo.addLogo(mStorage.getCurBitmapRef());
		Bitmap b = cursorBitmap.saveAllEditableToBitmap();
		if(b != null) {
			Log.i("caoheng", "savefile7");
			saveFile(b, "/extsd/curpic.jpg", "JPEG");
			
			b.recycle();
			BitmapCount.getInstance().recycleBitmap("MyView share b");
			// saveFile(mStorage.getCurBitmapRef(),"/sdcard/curpic.jpg","JPEG");

			Intent intent = new Intent(Intent.ACTION_SEND);
			intent.setType("image/*");
			
			Uri uri = Uri.fromFile(new File("/extsd/curpic.jpg"));
//			Uri uri = Uri.fromFile(new File("/sdcard/curpic.jpg"));
			
			intent.putExtra(Intent.EXTRA_STREAM, uri);
			intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
			getContext().startActivity(Intent.createChooser(intent, "分享"));
		}
		
	}

	public List<PageItem> itemList;
	Bitmap tempBitmap;// 读取大图
	Bitmap itemBitmap;// 生成需要的小图
	Cursor imagecursor = null;


	public void openDirectory() {

		
		itemList = new ArrayList<PageItem>();
		Canvas canvas = new Canvas();

		View popview = Start.instance.getLayoutInflater().inflate(R.layout.popup_window, null);
		
		setFocusable(true);
		setFocusableInTouchMode(true);
		popview.setFocusableInTouchMode(true);
		final PopupWindow pop = new PopupWindow(popview, 550, 870);// 870
		popview.setFocusable(true);
		popview.setFocusableInTouchMode(true);
		popview.setOnKeyListener(new OnKeyListener() {
			
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				Toast.makeText(Start.context, "down" + keyCode, Toast.LENGTH_SHORT);
			    if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {

			       Log.e("pop", "key down");
			       Start.instance.setVolumeControlStream(AudioManager.STREAM_MUSIC);
			       
			       AudioManager maudio;
			       maudio = (AudioManager)Start.context.getSystemService(Service.AUDIO_SERVICE);
			       
			       maudio.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND);
			      
			       	Start.c.view.onKeyDown(keyCode, event);
			       	Start.instance.onKeyDown(keyCode, event);
			    	return true;

			    }else if(keyCode == KeyEvent.KEYCODE_HEADSETHOOK){
			    	Log.e("pop", "key up");
			    	 AudioManager maudio;
				     	maudio = (AudioManager)Start.context.getSystemService(Service.AUDIO_SERVICE);
				       
			    	maudio.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND);
			    	Start.c.view.onKeyDown(keyCode, event);
			    	return true;
			    }
				return false;
			}
		});
		popview.setFocusableInTouchMode(true);
		
		CDBPersistent db = new CDBPersistent(getContext());
		db.open();
		imagecursor = db.getBitmapPath();
		

		File file = null;
		if (imagecursor != null && imagecursor.getCount() != 0)
			for (imagecursor.moveToLast(); !imagecursor.isBeforeFirst(); imagecursor
					.moveToPrevious()) {

				file = new File(imagecursor.getString(imagecursor
						.getColumnIndex("path")));
				if (!file.exists())
					try {
						itemBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.empty_cat);
						BitmapCount.getInstance().createBitmap("MyView openDirectory itemBitmap empty_cat");
					} catch (OutOfMemoryError e) {
						// TODO: handle exception
						// 改用默认错误提示图
	
						itemBitmap = Start.OOM_BITMAP;
						Log.e("AndroidRuntime", "MyView openDirectory() OOM!!!");
					}
				else{
					
					try {
	
						itemBitmap = BitmapFactory.decodeFile(
								imagecursor.getString(imagecursor
										.getColumnIndex("path")));
						BitmapCount.getInstance().createBitmap("MyView openDirectory itemBitmap");
					} catch (OutOfMemoryError e) {
						// TODO: handle exception
						// 改用默认错误提示图
	
						itemBitmap = Start.OOM_BITMAP;
						Log.e("AndroidRuntime", "MyView openDirectory() OOM!!!");
					}
				}
				
				
				itemList.add(new PageItem(imagecursor.getInt(imagecursor
						.getColumnIndex("pagenum")), imagecursor
						.getString(imagecursor.getColumnIndex("path")),
						itemBitmap));
			}
		
		
		imagecursor.close();
		db.close();

		g = (GridView) popview.findViewById(R.id.gridView);
		ia = new ImageAdapter(Start.context, imagecursor, itemList);
		g.setAdapter(ia);
		g.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub

				pop.dismiss();

				Log.e("image", "!!!!!!!!");
				// Toast.makeText(getContext(),"第"+ia.itemList.get(arg2).pagenum
				// + "页\n"+ ia.itemList.get(arg2).path,
				// Toast.LENGTH_LONG).show();

				Start.PAGENUM = ia.itemList.get(arg2).pagenum;

				// reset TestButton position
				//ly
//				Calligraph.TestButton.layout(600 - Calligraph.TestButton.getWidth(), 0, 600, Calligraph.TestButton.getHeight());
				//Calligraph.TestButton.layout(1600 - Calligraph.TestButton.getWidth(), 0, 1600, Calligraph.TestButton.getHeight());
				
				// 按page换模板
				CDBPersistent db = new CDBPersistent(Start.context);
				db.open();
				int template_byPage = db.getTemplateByPage(Start.getPageNum());
				doChangeBackground(WolfTemplateUtil
						.getTypeByID(template_byPage));
				db.close();

				CalligraphyVectorUtil.initParsedWordList(Start.getPageNum());
				// 读取该页内容
				for (int i = 0; i < CursorDrawBitmap.listEditableCalligraphy
						.size(); i++) {
					CursorDrawBitmap.listEditableCalligraphy.get(i)
							.initDatabaseCharList();
				}

				cursorBitmap.updateHandwriteState();
//				setFreeDrawBitmap();

			}
		});

		g.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub

				deleteConfirm(ia.itemList.get(arg2).pagenum, pop);

				if (ia.itemList.get(arg2).pagenum == Start.getPageNum()) {
					Toast.makeText(
							getContext(),
							"第" + ia.itemList.get(arg2).pagenum
									+ "份，长按，删除，并转到前一页", Toast.LENGTH_LONG)
							.show();
				} else {
					Toast.makeText(getContext(),
							"第" + ia.itemList.get(arg2).pagenum + "份，长按，删除，完事",
							Toast.LENGTH_LONG).show();
				}
				// return true，事件到此结束，不再处理click事件
				return true;
			}
		});

		pop.setOutsideTouchable(true);
		//必须设置背景                  
		pop.setBackgroundDrawable(new BitmapDrawable()); 
		pop.setFocusable(true);// 默认false，不会响应itemClickListener
		
		
		// pop.showAtLocation(Start.c.view, Gravity.CENTER_HORIZONTAL, 0, 0);
		
		//ly
//		pop.showAsDropDown(Start.c.view,27, -1024); // 正常 on f7
		pop.showAsDropDown(Start.c.view,27, -2560); // 正常 on f7
		
//		pop.showAsDropDown(Start.c.view, Start.SCREEN_WIDTH * (27/600), -(Start.SCREEN_HEIGHT *(955/1024)));
		

		pop.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss() {
				// TODO Auto-generated method stub
				// ia.releaseBitmap();
				for (int i = 0; i < itemList.size(); i++) {
					itemList.get(i).bgBitmap.recycle();
					BitmapCount.getInstance().recycleBitmap("MyView openDirectory setOnDismissListener");	
				}
				Log.e("null", "dismiss listener after release");
			}
		});

		Button cancelBtn = (Button) popview.findViewById(R.id.cancelBtn);
		cancelBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				pop.dismiss();
			}
		});

		popview.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				pop.dismiss();
			}
		});

	}

	@Override
	public void colorChanged(int color) {
		if(drawStatus == STATUS_DRAW_FREE)
		{
			freeBitmap.updateToBitmapList();
			freeBitmap.drawFreeBitmapSync();
			
			//ly
			//把切换颜色时候的背景切换去掉
//			mCanvas.setBitmap(mBitmap);
//			mCanvas.drawBitmap(mBitmap, new Rect(Start.SCREEN_WIDTH, 0, Start.SCREEN_WIDTH * 2, Start.SCREEN_HEIGHT), new Rect(
//					0, 0, Start.SCREEN_WIDTH, Start.SCREEN_HEIGHT), new Paint());
			//end
			
			calliImpl.updateBitmap();
			hardImpl.updateBitmap();
		}

		if (penStatus == STATUS_PEN_CALLI) {
		} else {
			hardImpl.clear();
		}
		baseImpl.bPaint.setColor(color);
		calliImpl.mPaint.setColor(color);
		calliImpl.mPathPaint.setColor(color);
	}

	public Uri savePicBitmapRandom(Uri uri) {
		String dirName = "free_" + Start.getPageNum();
		File dir = new File(FILE_PATH_HEADER + "/calldir/" + dirName);
		int index = cursorBitmap.cal_current.currentpos;
		Uri newUri = null;
		if (!dir.exists())
			dir.mkdir();
		else if (dir.isDirectory()) {
			Random r = new Random();
			File newFile = new File(dir + "/" + r.nextInt(100));
			try {
				FileOutputStream out = new FileOutputStream(newFile);
				InputStream in = Start.context.getContentResolver()
						.openInputStream(uri);

				byte[] buff = new byte[1024];
				int len = 0;
				while ((len = in.read(buff)) > 0) {
					out.write(buff, 0, len);
				}

				in.close();
				out.close();
				newUri = Uri.fromFile(newFile);

				Log.e("camera", "uri null:" + (newUri == null));

			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return newUri;
	}

	public int getMaxHeight() {
		return cursorBitmap.getMaxHeight();
	}
	public void addRowNumber(String TYPE){
//		Log.e("row", "add" + TYPE);
		if(Available.AVAILABLE_CONTENT.equals(TYPE)){
			rowNumber ++;
			
		}
	}
	public void resetRowNumber(){
		Log.e("row", "reset");
		
			rowNumber = 0;
	}
	public int getRowNumber(){
		return rowNumber;
	}
	
	
	public static void my_toast(String msg){
	    LayoutInflater inflater = (Start.instance).getLayoutInflater();
	    View layout = inflater.inflate(R.layout.toast,
	                                   (ViewGroup)Start.instance.findViewById(R.id.toast_layout_root));

	    ImageView image = (ImageView) layout.findViewById(R.id.image);
	    TextView text = (TextView) layout.findViewById(R.id.text);
	    text.setText(msg);

	    Toast toast = new Toast((Start.instance).getApplicationContext());
	    toast.setGravity(Gravity.BOTTOM, 0, 100);
	    toast.setDuration(Toast.LENGTH_LONG);
	    toast.setView(layout);
	    toast.show();
	}
	
	@Override
	public void computeScroll() {
		// TODO Auto-generated method stub
		if (mScroller.computeScrollOffset()) {
			touchPt.x = mScroller.getCurrX();
			touchPt.y = mScroller.getCurrY();
			
			postInvalidate();
		}
		else{
//			touchPt.x = -1;
//			touchPt.y = -1;			
		}

		super.computeScroll();
	}
	
	public void SetScreen(int screenWidth,int screenHeight){
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;
	}
	
	/**
	 * 画前景图片
	 * @param canvas
	 */
	private void drawForceImage(Canvas canvas) {
		// TODO Auto-generated method stub
		Paint mPaint = new Paint();
		
		if (foreImage!=null) {
			Log.i("fanye", "draw forImage");
			canvas.drawBitmap(foreImage, 0, 0, mPaint);
		}		
	}


	/**
	 * 画背景图片
	 * @param canvas
	 */
	private void drawBgImage(Canvas canvas,Path path) {
		// TODO Auto-generated method stub
		Log.i("fanye", "draw BgImage");
		Paint mPaint = new Paint();
		
		if (bgImage!=null) {
			canvas.save();
			
			//只在与路径相交处画图
			canvas.clipPath(path, Op.INTERSECT);
			canvas.drawBitmap(bgImage, 0, 0, mPaint);
			
			canvas.restore();
		}
	}
	
	private void drawPageEffect(Canvas canvas) {
		// TODO Auto-generated method stub
		Log.i("fanye", "draw page effect");
		drawForceImage(canvas);
		Paint mPaint = new Paint();
		if (touchPt.x!=-1 && touchPt.y!=-1) {
			Log.i("fanye", "touchPt.x  =" +  touchPt.x + " touchPT.y = " + touchPt.y);
			//翻页左侧书边
			canvas.drawLine(touchPt.x, 0, touchPt.x,2560, mPaint);
			Log.i("drawline", "" + touchPt.x);
			//左侧书边画阴影
			shadowDrawableRL.setBounds((int)touchPt.x - 20, 0 ,(int)touchPt.x, 2560);
			shadowDrawableRL.draw(canvas);
			
			//翻页对折处
			float halfCut = touchPt.x + (1600 - touchPt.x)/2;
			canvas.drawLine(halfCut, 0, halfCut, 2560, mPaint);
			
			//对折处左侧画翻页页图片背面
			Rect backArea = new Rect((int)touchPt.x,0,(int)halfCut,2560);
			Paint backPaint = new Paint();
			backPaint.setColor(0xffdacab0);
			canvas.drawRect(backArea, backPaint);
			
			//将翻页图片正面进行处理水平翻转并平移到touchPt.x点
			Paint fbPaint = new Paint();
			fbPaint.setColorFilter(mColorMatrixFilter);
			Matrix matrix = new Matrix();
			
			matrix.preScale(-1,1);
			matrix.postTranslate(foreImage.getWidth() + touchPt.x,0);
			
			
			canvas.save();
			canvas.clipRect(backArea);
			canvas.drawBitmap(foreImage, matrix, fbPaint);
			canvas.restore();
			 
			Log.i("effect", "1    " + canvas.toString());
			//对折处画左侧阴影
			shadowDrawableRL.setBounds((int)halfCut - 50, 0 ,(int)halfCut, 2560);
			shadowDrawableRL.draw(canvas);
			Log.i("effect", "2");
			
			Path bgPath = new Path();
			
			//可以显示背景图的区域
			bgPath.addRect(new RectF(halfCut,0,1600,2560), Direction.CW);
			
			//对折出右侧画背景
			drawBgImage(canvas,bgPath);
			
			//对折处画右侧阴影
			shadowDrawableLR.setBounds((int)halfCut, 0 ,(int)halfCut + 50, 2560);
			shadowDrawableLR.draw(canvas);
			Calligraph.staticText.setVisibility(GONE);
			invalidate();
			
		}
	}
	
//	private void drawPageEffect() {
//		// TODO Auto-generated method stub
//		Log.i("fanye", "draw page effect");
//		drawForceImage(mCanvas);
//		Paint mPaint = new Paint();
//		if (touchPt.x!=-1 && touchPt.y!=-1) {
//			Log.i("fanye", "touchPt.x  =" +  touchPt.x + " touchPT.y = " + touchPt.y);
//			//翻页左侧书边
//			mCanvas.drawLine(touchPt.x, 0, touchPt.x,screenHeight, mPaint);
//			
//			//左侧书边画阴影
//			shadowDrawableRL.setBounds((int)touchPt.x - 20, 0 ,(int)touchPt.x, screenHeight);
//			shadowDrawableRL.draw(mCanvas);
//			
//			//翻页对折处
//			float halfCut = touchPt.x + (screenWidth - touchPt.x)/2;
//			mCanvas.drawLine(halfCut, 0, halfCut, screenHeight, mPaint);
//			
//			//对折处左侧画翻页页图片背面
//			Rect backArea = new Rect((int)touchPt.x,0,(int)halfCut,screenHeight);
//			Paint backPaint = new Paint();
//			backPaint.setColor(0xffdacab0);
//			mCanvas.drawRect(backArea, backPaint);
//			
//			//将翻页图片正面进行处理水平翻转并平移到touchPt.x点
//			Paint fbPaint = new Paint();
//			fbPaint.setColorFilter(mColorMatrixFilter);
//			Matrix matrix = new Matrix();
//			
//			matrix.preScale(-1,1);
//			matrix.postTranslate(foreImage.getWidth() + touchPt.x,0);
//			
//			
//			mCanvas.save();
//			mCanvas.clipRect(backArea);
//			mCanvas.drawBitmap(foreImage, matrix, fbPaint);
//			mCanvas.restore();
//			 
//			Log.i("effect", "1    " + mCanvas.toString());
//			//对折处画左侧阴影
//			shadowDrawableRL.setBounds((int)halfCut - 50, 0 ,(int)halfCut, screenHeight);
//			shadowDrawableRL.draw(mCanvas);
//			Log.i("effect", "2");
//			
//			Path bgPath = new Path();
//			
//			//可以显示背景图的区域
//			bgPath.addRect(new RectF(halfCut,0,screenWidth,screenHeight), Direction.CW);
//			
//			//对折出右侧画背景
//			drawBgImage(mCanvas,bgPath);
//			
//			//对折处画右侧阴影
//			shadowDrawableLR.setBounds((int)halfCut, 0 ,(int)halfCut + 50, screenHeight);
//			shadowDrawableLR.draw(mCanvas);
//		}
//	}
	class picAndName {
		Bitmap bitmap = null;
		String name = null;
		public picAndName(Bitmap b, String n){
			bitmap = b;
			name = n;
		}
		
		public Bitmap getBitmap(){
			return bitmap;
		}
		
		public String getName(){
			return name;
		}
	}
	
	public void DemoChangeBg(){
		
		
		doubleClickState = false;
		//打开图
		freeBitmap.resetFreeBitmapList();
		Resources res=getResources();
		Bitmap bmp = BitmapFactory.decodeResource(res, R.drawable.paper);
		freeBitmap.addBgPic(bmp);
		
		changeStateAndSync(0);
	}
public void DemoChangeBg1(){
		
		
		doubleClickState = false;
		//打开图
		freeBitmap.resetFreeBitmapList();
		Resources res=getResources();
		Bitmap bmp = BitmapFactory.decodeResource(res, R.drawable.tiping);
		freeBitmap.addBgPic(bmp);
		Calligraph.nameText.setVisibility(GONE);
		changeStateAndSync(0);
	}



public void quesComment(){
	
	
	doubleClickState = false;
	//打开图
	freeBitmap.resetFreeBitmapList();
	Resources res=getResources();
	Bitmap bmp = BitmapFactory.decodeResource(res, R.drawable.tiping);
	freeBitmap.addBgPic(bmp);
//	Calligraph.nameText.setVisibility(GONE);
	changeStateAndSync(0);
}
public void pageComment(){
	
	Calligraph.pingyuText.setVisibility(View.GONE);
	doubleClickState = false;
	//打开图
	freeBitmap.resetFreeBitmapList();
	Resources res=getResources();
	Bitmap bmp = BitmapFactory.decodeResource(res, R.drawable.yeping);
	freeBitmap.addBgPic(bmp);
//	Calligraph.nameText.setVisibility(GONE);
	changeStateAndSync(0);
}
public void findGesture(Gesture gesture) {
	try {
		// 关于两种方式创建模拟器的SDcard在【Android2D游戏开发之十】有详解
		if (Environment.getExternalStorageState() != null) {// 这个方法在试探终端是否有sdcard!
		//当存在此文件的时候我们需要先删除此手势然后把新的手势放上
				//读取已经存在的文件,得到文件中的所有手势
				if (!gestureLib.load()) {//如果读取失败
				
				} else {//读取成功 
					List<Prediction> predictions = gestureLib.recognize(gesture);
					//recognize()的返回结果是一个prediction集合，
					//包含了所有与gesture相匹配的结果。
					//从手势库中查询匹配的内容，匹配的结果可能包括多个相似的结果， 
					if (!predictions.isEmpty()) {
						Prediction prediction = predictions.get(0);
						//prediction的score属性代表了与手势的相似程度
						//prediction的name代表手势对应的名称 
						//prediction的score属性代表了与gesture得相似程度（通常情况下不考虑score小于1的结果）。 
						if (prediction.score >= 1) {
							Toast.makeText(getContext(), "dui", Toast.LENGTH_SHORT).show();	;
						}
					}
				}
			
		} else {

		}
	} catch (Exception e) {
		e.printStackTrace();
	
	}
}
//public void setPageXML(){
//	pageXML = bgName.substring(0, bgName.length()-4)+".xml";
////	Log.i("pageXML",""+pageXML);
//}
public static void saveXML(File file) {
	FileOutputStream fos = null;
	try {
		if (!file.exists()) {// 文件不存在则创建
			file.createNewFile();
		}
		fos = new FileOutputStream(file, false);

		fos.write(Calligraph.doc.html().getBytes());// 写入文件内容
		fos.flush();
	} catch (IOException e) {
		System.err.println("文件创建失败");
	} finally {
		if (fos != null) {
			try {
				fos.close();
			} catch (IOException e) {
				System.err.println("文件流关闭失败");
			}
		}
	}
}

}

