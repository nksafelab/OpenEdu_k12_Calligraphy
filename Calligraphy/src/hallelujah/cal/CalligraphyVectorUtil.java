package hallelujah.cal;

import hallelujah.cal.CalligraphyVectorParser.mParsedWord;
import hallelujah.cal.ctrl.ProducerFactory;
import hallelujah.cal.parser.ParserStroke;
import hallelujah.cal.parser.ParserWord;
import hallelujah.cal.producer.CalligraphyProducer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.jinke.calligraphy.app.branch.CursorDrawBitmap;
import com.jinke.calligraphy.app.branch.EditableCalligraphy;
import com.jinke.calligraphy.app.branch.EditableCalligraphyItem;
import com.jinke.calligraphy.app.branch.ImageLimit;
import com.jinke.calligraphy.app.branch.Start;
import com.jinke.calligraphy.app.branch.VEditableCalligraphyItem;
import com.jinke.calligraphy.app.branch.WordLimit;
import com.jinke.calligraphy.database.CDBPersistent;
import com.jinke.single.BitmapCount;
import com.jinke.single.ScaleSave;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.net.Uri;
import android.util.Log;


public class CalligraphyVectorUtil {
	private static final String TAG = "CalligraphyVectorUtil";
	private static List<mParsedWord> parseredWordList = null;//用于显示
	private static mWord mCurrentWord = null;
	private static SingleStroke currentStroke = null;
	private static Point currentPoint = null;
	private static CalligraphyProducer producer = null; 
	private static long lStkTS = 0;
	private static CalligraphyVectorUtil vectorUtil = null;
	private static CalligraphyVectorParser vectorParser = null;
	
	private static float mX = 0;
	private static float mY = 0;
	private static Path mPath = null;
	
//	private static float Left_X = 600;
	private static float Left_X = 1600;
	private static float Right_X = 0;
//	private static float Top_Y = 1024;
	private static float Top_Y = 2560;
	private static float Bottom_Y = 0;
	
	private CalligraphyVectorUtil(){
		
	}
	public static CalligraphyVectorUtil instance(){
//		Log.e("vector", "instance");
		if(vectorUtil == null){
//			Log.e("vector", "instance null");
			vectorUtil = new CalligraphyVectorUtil();
			return vectorUtil;
		}else
			return vectorUtil;
	}
	
	
	public static void initParsedWordList(int pagenum){
		if(vectorParser == null)
			vectorParser = new CalligraphyVectorParser();
		if(parseredWordList == null){
			
		}else
			clearParseredWordList();
			
		parseredWordList = vectorParser.parseVectorWordFromVectorFile(pagenum);
		
		
	}
	public static void clearParseredWordList(){
		Log.e("vector", ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>clear Current parseredWordList");
		for(mParsedWord wd : parseredWordList){
			if(wd.picBitmap != null){
				wd.picBitmap.recycle();
				BitmapCount.getInstance().recycleBitmap("CalligraphyVectorUtil clearParseredWordList");
			}
//			if(wd.word != null)
//				wd.word.recycle();
		}
	}
	
	public static LinkedList<EditableCalligraphyItem> getEditableListByParsedWordList(int available_id,boolean zoomable){
		Log.e("vectorr", "getEditableListByParsedWordList->>>>>>>>>>>>>>>>>>>>>>>>> ");
		LinkedList<EditableCalligraphyItem> editItemList = new LinkedList<EditableCalligraphyItem>();
		VEditableCalligraphyItem editItem = null;
		Log.e("create", "getEditableListByParsedWordList word available:" + available_id + " parseredWordList.size:" + parseredWordList.size());
		if(parseredWordList != null){
			int i=0;
			for(mParsedWord mpd : parseredWordList){
				if(mpd.aid != available_id)
					continue;
				//不是本可写区的文字忽略
				
				Log.e("create", "------getEditableListByParsedWordList word available item:" + available_id
						+ " item:" + i + " type:" + mpd.charType);
				
				if(mpd.charType == EditableCalligraphyItem.Types.ImageItem)
					ImageLimit.instance().addImageCount();
				else
					WordLimit.getInstance().addWordCount();
				
				if(mpd.charType != EditableCalligraphyItem.Types.CharsWithStroke){
					//不是普通书写字体
					
					//插入的图片
					if(mpd.charType == EditableCalligraphyItem.Types.ImageItem || 
							mpd.charType == EditableCalligraphyItem.Types.AUDIO ||
								mpd.charType == EditableCalligraphyItem.Types.VEDIO){
						Log.e("create", "              add  ImageItem");
						EditableCalligraphyItem editItem_ = new EditableCalligraphyItem(mpd.picBitmap);
						editItem_.setType(mpd.charType);
						editItem_.setImageUri(Uri.parse(mpd.uri));
						
						editItemList.add(i,editItem_);
					}else if(mpd.charType == EditableCalligraphyItem.Types.CharsWithoutStroke){
						Log.e("create", "              add  free word");
						EditableCalligraphyItem editItem_ = new EditableCalligraphyItem(mpd.picBitmap);
						editItem_.setMatrix(CDBPersistent.getMatrix(mpd.matrix)); 
						editItemList.add(i,editItem_);
						
					}else{
						Log.e("create", "              add  end of line");
						editItem = new VEditableCalligraphyItem(mpd.charType);
						
						editItemList.add(i,editItem);
					}
					
				}else{
					Log.e("create", "              add  charWithStroke");
					Bitmap b = null;
					editItem = new VEditableCalligraphyItem(vectorParser.getBitmapFromParsedWord(mpd,zoomable));
					
//					editItem.setWord(mpd.word);
					editItem.setmPath(mpd.mPath);
					editItem.setBottom_Y(mpd.Bottom_Y);
					editItem.setTop_Y(mpd.Top_Y);
					editItem.setLeft_X(mpd.Left_X);
					editItem.setRight_X(mpd.Right_X);
					editItem.setmColor(mpd.mColor);
					
					if(Start.c == null)
						editItem.setMatrix(Start.m);
					else
						editItem.setMatrix(Start.c.view.getMMMatrix());

					if(i > 250){
						editItem.recycleBitmap();
						BitmapCount.getInstance().recycleBitmap("CalligraphyVectorUtil getEditableListByParsedWordList i>250");	
					}
					editItemList.add(i,editItem);
				}
				
				i++;
			}
		}
		Log.e("vectorr", "getEditableListByParsedWordList->>>>>>>>>>>>>>>>>>>>>>>>> return" + available_id);
		return editItemList;
	}
	
	//timer开始
	public static void createWord(int pagenum, int aid,int itemid){
		//createSingleWord
//		Log.e("vector", "new word; >>>>>>>>>>>>>>pagenum=" + pagenum + " aid=" + aid + " itemid=" + itemid);
		
		mCurrentWord = new mWord();
		
		mCurrentWord.word = SingleWord.createSingleWord(1, System.currentTimeMillis(),
				1024 * 1024);
		resetBound();
		
		mCurrentWord.word.begin();
		
		mPath = new Path();
		//word.pagenum  aid  itemid
		//word.begin
	}
	
	public static void start(float x,float y){
//		Log.e("vector", "new stroke; putPoint: x=" + x + " y=" + y);
		
		if(Start.c.view.penStatus == Start.c.view.STATUS_PEN_CALLI){
//			Log.e(TAG, "free draw return");
			return;
		}
		
		setBound(x, y);
		mPath.moveTo(x, y);
		mX = x;
		mY = y;
		
		currentPoint = new Point();
		currentPoint.setXCoordinate(x);
		currentPoint.setYCoordinate(y);
		
		lStkTS = System.currentTimeMillis();
		int color = Start.c.view.baseImpl.bPaint.getColor();
//		color = Color.BLACK;
		
		currentStroke = SingleStroke.createSingleStroke(1, lStkTS, color, 100,
				1024);
		
		/*
		 * black -16777216 
		 * blue  -16776961
		 * cyan    -16711681
		 * red  -65536
		 */
		currentStroke.begin();
		
		try {
			currentStroke.putPoint(currentPoint);
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		//init stroke 
		//stroke.begin 
		//stroke.putPoint
	}
	
	private static float TOUCH_TOLERANCE = 4.0f;
	public static void move(float x, float y){
		
		if(Start.c.view.penStatus == Start.c.view.STATUS_PEN_CALLI){
//			Log.e(TAG, "free draw return");
			return;
		}
		
		setBound(x, y);
		float dx = Math.abs(x - mX);
		float dy = Math.abs(y - mY);
		if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
			mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
			mX = x;
			mY = y;
		}
		
		currentPoint = new Point();
		currentPoint.setXCoordinate(x);
		currentPoint.setYCoordinate(y);
		
		try {
			currentStroke.putPoint(currentPoint);
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		
	}
	
	public static void end(){
		//stroke.putPoint
		//stroke.end
		//少处理一个点
		
		if(Start.c.view.penStatus == Start.c.view.STATUS_PEN_CALLI){
//			Log.e(TAG, "free draw return");
			return;
		}
		
		currentStroke.end();
		try {
			mCurrentWord.word.putStroke(currentStroke);
		} catch (IOException e) {
			e.printStackTrace();
		}
		currentStroke.recycle();
		
		//word.putStroke
		//stroke.recyle
	}
	
	//timer结束
	public static Bitmap finish(boolean isword,int currentPos){
		Bitmap b = null;
		Matrix currentMatrix;
		if(Start.c == null)
			currentMatrix = Start.m;
		else
			currentMatrix = Start.c.view.getMMMatrix();
		
		if(!Start.c.view.cursorBitmap.cal_current.getAvailable().getZoomable())
			currentMatrix = new Matrix();
		
		if(isword){
		//word.end
		//put word to list
//		Log.e("vector", "a word finish");
			
			
			
			int startx = (int)(Left_X - 0.5);
			int endx = (int)(Right_X + 0.5);
			int starty = (int)(Top_Y - 0.5);
			int endy = (int)(Bottom_Y + 0.5);
			
			startx = startx < 0 ? 0 : startx;
			starty = starty < 0 ? 0 : starty;
			int distX = endx - startx;
			int distY = endy - starty;
			
//			distX = (int)(Right_X - Left_X) + 1;
//			distY = (int)(Bottom_Y - Top_Y) + 1;
			
			
//			Log.e("vectorTest", "finish-----------distX:" + distX + " distY:" + distY);
			
			if(distX <=0 || distY <= 0)
				return null;
			
//			Log.e("pathscale", "-->distx:" + distX + " distY:" + distY);
			
			float scale = getScaled(Right_X - Left_X ,Bottom_Y - Top_Y);
			ScaleSave.getInstance().insertScale(currentPos,Right_X - Left_X, Bottom_Y - Top_Y, scale);
			float[] values = new float[9];
			
			currentMatrix.getValues(values);
			scale *= values[0];
			
			float height = Bottom_Y - Top_Y;
			
			int sizeX=(int)((endx - startx)*scale);
			int sizeY=(int)((endy - starty)*scale);
			if(sizeX <0)
				sizeX *= -1;
			else if(sizeX == 0)
				sizeX = 1;
			if(sizeY <0)
				sizeY *= -1;
			else if(sizeY == 0)
				sizeY = 1;
			
//			Log.e("vectorTest", "finish-----------sizeX:" + sizeX + "sizeY:" + sizeY);
			
			try {
				b = Bitmap.createBitmap(sizeX+3, sizeY+3, Bitmap.Config.ARGB_4444);	
				BitmapCount.getInstance().createBitmap("CalligraphyVectorUtil word finish");
				
			} catch (OutOfMemoryError e) {
				// TODO: handle exception
				b = Start.OOM_BITMAP;
				Log.e("OOM", "finish OOM");
			}
			
			
			int color = Start.c.view.baseImpl.bPaint.getColor();
			
			Canvas mc = new Canvas();
			mc.setBitmap(b);
			Paint pp = new Paint();
			pp.setStrokeWidth(2);
			pp.setColor(color);
			pp.setStyle(Paint.Style.STROKE);
			Matrix m = new Matrix();
			m.setScale(scale, scale);
			Path tempPath = new Path();
			mPath.offset(-Left_X, -Top_Y,tempPath);
			
			tempPath.transform(m);
			mc.drawPath(tempPath, pp);
			
			mCurrentWord.word.end();

		}
		else{
//			Log.e("vector", "just a pot , not a word");
			mCurrentWord.word.end();
			mCurrentWord.word.recycle();
		}
		return b;
			//word.recyle()
	}
	
	public static void saveWordToFile(SingleWord word, int pagenum,int aid,int itemid){
		String wordPath = "";
		//for each
//			Log.e("vector", "aid:" + aid + " itemid:" + itemid);
			String freeDirPath = wordPath = Start.getStoragePath() + "/calldir/free_" + pagenum;
			File freedir = new File(freeDirPath);
			if(!freedir.exists() || !freedir.isDirectory()){
				freedir.mkdirs();
			}
			
			wordPath = Start.getStoragePath() + "/calldir/free_" + pagenum + "/a" + aid + "i" + itemid;
			File f = new File(wordPath);
			
			if(f.exists()){
				System.out.println("exit" + wordPath);
				f.delete();
			}
			
			Log.e("vector", "path:" + wordPath);
			// producer.create
			try {
				producer = ProducerFactory.instance().newProducer("single",
						wordPath, 0);
				
				// producer.putSingleWord
				Log.e("vectorword", "producer:" + (producer == null) + " word:" + (word == null));
				producer.putSingleWord(word);
//				word.recycle();
				producer.flush();
				producer.finish();
				// word.recyle
				// producer.flush
				// producer.finish	
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		
	}
	
	
	public static SingleWord getFinishedWord(){
		
		return mCurrentWord.word;
	}
	
	
	final static class mWord{
		
		SingleWord word;
		
	}
	
	private static void resetBound(){
		//Left_X = 600;
		Left_X = 1600;
		Right_X = 0;
//		Top_Y = 1024;
		Top_Y = 2560;
		Bottom_Y = 0;
	}
	public static void setBound(float x,float y){
		
		if(x < Left_X){
			Left_X = x;
//			Log.e("vectorTest", "x:" + x + "y:" + y + " Left_x:" + Left_X);
		}
		if(x > Right_X){
			Right_X = x;
//			Log.e("vectorTest", "x:" + x + "y:" + y + " Right_X:" + Right_X);
		}
		if(y < Top_Y){
			Top_Y = y;
//			Log.e("vectorTest", "x:" + x + "y:" + y + " Top_Y:" + Top_Y);
			
		}
		if(y > Bottom_Y){
			Bottom_Y = y;
//			Log.e("vectorTest", "x:" + x + "y:" + y + " Bottom_Y:" + Bottom_Y);
		}
		
//		Log.e("vectorTest", "x:" + x + "y:" + y + " Left_x:" + Left_X  +
//				" Right_x:" + Right_X + " Top_Y:" + Top_Y + " Bottom_Y:" + Bottom_Y);
		
	}
	
	
	
	
	private static void showParsedWord(){
		if(parseredWordList != null){
			int i=0;
			for(mParsedWord mpd : parseredWordList){
//				Log.e("vector", "word i" + i + ">>>>> available:" 
//						+ mpd.aid + " itemid:" + mpd.itemid + " charType:" + mpd.charType + " path:" + (mpd.mPath == null));
				
				i++;
			}
		}
	}
	public static float getLeft_X() {
		return Left_X;
	}
	public static void setLeft_X(float left_X) {
		Left_X = left_X;
	}
	public static float getRight_X() {
		return Right_X;
	}
	public static void setRight_X(float right_X) {
		Right_X = right_X;
	}
	public static float getTop_Y() {
		return Top_Y;
	}
	public static void setTop_Y(float top_Y) {
		Top_Y = top_Y;
	}
	public static float getBottom_Y() {
		return Bottom_Y;
	}
	public static void setBottom_Y(float bottom_Y) {
		Bottom_Y = bottom_Y;
	}
	public static Path getmPath() {
		return mPath;
	}
	public static void setmPath(Path mPath) {
		CalligraphyVectorUtil.mPath = mPath;
	}
	
	static float mVScale,mHScale,mFixedHeight,mMaxHeight,mMaxWidth;
	
	public static float getScaled(float distX, float distY){
			mFixedHeight = 36.0f;//字高
			mMaxHeight = 250.0f;
			mMaxWidth = 250.0f;
//			float mMaxWidth = 400.0f;
//			if(distX < distY){
//				if(distX < mMaxWidth)
//					mVScale = mHScale = mFixedHeight / mMaxWidth;
//				else 
//					mVScale = mHScale = mFixedHeight / distX;
//			}else{
			
			if(!EditableCalligraphy.BY_WIDTH){
				if(distY < mMaxHeight)
					mVScale = mHScale = mFixedHeight / mMaxHeight;
				else 
					mVScale = mHScale = mFixedHeight / distY;
			}else{
				// 宽度一致
				if(distX < CursorDrawBitmap.mMaxWidth)
					mVScale = mHScale = mFixedHeight / mMaxWidth;
				else 
					mVScale = mHScale = mFixedHeight / distX;
			}
				
				ScaleSave.getInstance().insertScale(0,distX, distY, mVScale);
//			}
//			Log.e("pathscale", "util scale------------->>>>>>>>>>>distX:" + distX + " " +
//					"distY:" + distY + " " +
//							"mFixedHeight:" + mFixedHeight + "" +
//									"mMaxHeight:" + mMaxHeight + " " +
//											"Scale:" + mVScale);
			
			return mVScale;
	}
	
	public EditableCalligraphyItem getEditableCalligraphyItem(int pagenum, int available_id,int itemid,String matrix,boolean zoomable){
		return vectorParser.getEditableCalligraphyItem(pagenum, available_id, itemid, matrix, zoomable);
	}
}
