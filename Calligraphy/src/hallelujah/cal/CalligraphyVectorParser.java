package hallelujah.cal;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.jinke.calligraphy.app.branch.CalligraphyStrokePath;
import com.jinke.calligraphy.app.branch.EditableCalligraphyItem;
import com.jinke.calligraphy.app.branch.Start;
import com.jinke.calligraphy.app.branch.VEditableCalligraphyItem;
import com.jinke.calligraphy.app.branch.EditableCalligraphyItem.Types;
import com.jinke.calligraphy.database.CDBPersistent;
import com.jinke.single.BitmapCount;

import hallelujah.cal.CalligraphyVectorUtil.mWord;
import hallelujah.cal.ctrl.ParserFactory;
import hallelujah.cal.parser.CalligraphyParser;
import hallelujah.cal.parser.ParserStroke;
import hallelujah.cal.parser.ParserWord;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Log;

public class CalligraphyVectorParser {
	private CalligraphyParser parser = null;
	private ParserWord word = null;
	private ParserStroke stroke = null;
	private int mColor;
	private Path mPath = null;
	private Point mPoint = null;
	private int strokeCount = 0;// 笔画数
	private int pointCount = 0;// 笔画的点数目
	private float mX = 0;
	private float mY = 0;
//	private float Left_X = 600;
//	private float Right_X = 0;
//	private float Top_Y = 1024;

	//ly
	private float Left_X = 1600;
	private float Right_X = 0;
	private float Top_Y = 2560;
	private float Bottom_Y = 0;
	
	private float TOUCH_TOLERANCE = 4.0f;
	private static final String TAG = "CalligraphyVectorParser";
	private Paint mPaint;
	
	private static SingleStroke currentStroke = null;
	private static SingleWord currentWord = null;
	
	
	public List<mParsedWord> parseVectorWordFromVectorFile(int pagenum){
		
		Log.e("vectorr", "parserVectorWord->>>>>>>>>>>>>>>>>>>>>>>>>");
		List<mParsedWord> parsedWordList = new ArrayList<mParsedWord>();
		mParsedWord mWord = null;
		
		CDBPersistent db = new CDBPersistent(Start.context);
		db.open();
			Cursor cursor = db.getCharByPage(pagenum);
		
		
		mPaint = new Paint();
		mPaint.setColor(Color.BLACK);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeWidth(2);
		
		int cursorCount = 0;
		Log.e("create", "------initListEditableCalligraphy cursor:" + (cursor == null) + " size:" + cursor.getCount());
		if(cursor != null)
		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
			
//			Left_X = 600;
//			Right_X = 0;
//			Top_Y = 1024;
			
			Left_X = 1600;
			Right_X = 0;
			Top_Y = 2560;
			
			Bottom_Y = 0;
			
			//"template_id","available_id" ,"itemid","charType","matrix"
			int template_id = cursor.getInt(cursor.getColumnIndex("template_id"));
			int available_id = cursor.getInt(cursor.getColumnIndex("available_id"));
			int itemid = cursor.getInt(cursor.getColumnIndex("itemid"));
			String charType = cursor.getString(cursor.getColumnIndex("charType"));
			String matrix = cursor.getString(cursor.getColumnIndex("matrix"));
			String uri = cursor.getString(cursor.getColumnIndex("uri"));
			byte[] attr = null;
			
			
			
			if(EditableCalligraphyItem.getType(Integer.parseInt(charType)) !=
				EditableCalligraphyItem.Types.CharsWithStroke){
				Log.e(TAG, ">>>>> available:" + available_id + " itemid:" + itemid + 
						" charType:" + EditableCalligraphyItem.getType(Integer.parseInt(charType)));
			
				mWord = new mParsedWord();
				mWord.pagenum = pagenum;
				mWord.aid = available_id;
				mWord.itemid = itemid;
				mWord.charType = EditableCalligraphyItem.getType(Integer.parseInt(charType));
				
				
				if(EditableCalligraphyItem.getType(Integer.parseInt(charType)) ==
				EditableCalligraphyItem.Types.ImageItem || 
					EditableCalligraphyItem.getType(Integer.parseInt(charType)) ==
						EditableCalligraphyItem.Types.AUDIO ||
							EditableCalligraphyItem.getType(Integer.parseInt(charType)) ==
								EditableCalligraphyItem.Types.VEDIO){
					Log.e("audio", "types:" + EditableCalligraphyItem.getType(Integer.parseInt(charType)));
					//图片
					mWord.uri = uri;
					attr = cursor.getBlob(cursor.getColumnIndex("charBitmap"));
					if(attr != null){
						try {
							mWord.picBitmap =  BitmapFactory.decodeByteArray(attr, 0,
									attr.length);
						} catch (OutOfMemoryError e) {
							// TODO: handle exception
							mWord.picBitmap = Start.OOM_BITMAP;
						}
						
					}
					
				}else if(EditableCalligraphyItem.getType(Integer.parseInt(charType)) ==
				EditableCalligraphyItem.Types.CharsWithoutStroke){
					//毛笔
					attr = cursor.getBlob(cursor.getColumnIndex("charBitmap"));
					if(attr != null){
						try{
							mWord.picBitmap =  BitmapFactory.decodeByteArray(attr, 0,
									attr.length);
						} catch (OutOfMemoryError e) {
							// TODO: handle exception
							mWord.picBitmap = Start.OOM_BITMAP;
						}
					}
					mWord.matrix = matrix;
					
				}
				parsedWordList.add(mWord);
			}
			else{
				//CharsWithStroke
				
				String wordpath = Start.getStoragePath() + "/calldir/free_" + pagenum + "/a" + available_id + "i" + itemid;
				File wordfile = new File(wordpath);
				long size = wordfile.length();
//				if(wordfile.exists()){
//					
//					try {
//						InputStream in = new FileInputStream(wordfile);
//						size = in.available();
//					} catch (FileNotFoundException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//				}
				if(size == 0){
					
					mWord = new mParsedWord();
					mWord.pagenum = pagenum;
					mWord.aid = available_id;
					mWord.itemid = itemid;
					mWord.charType = EditableCalligraphyItem.Types.CharsWithoutStroke;
					attr = cursor.getBlob(cursor.getColumnIndex("charBitmap"));
					if(attr != null)
						mWord.picBitmap =  BitmapFactory.decodeByteArray(attr, 0,
								attr.length);
					mWord.matrix = matrix;
					parsedWordList.add(mWord);
					
					
					continue;
				}
				
			
				mPath = new Path();
				long start = System.currentTimeMillis();
//				Log.e(TAG, "Start from get parser:>>>>>>>>>>>>>>" + start);
				try {
					
					parser = ParserFactory.instance().newParser("single",
							Start.getStoragePath() + "/calldir/free_" + pagenum + "/a" + available_id + "i" + itemid, 0);
					if(parser == null){
						Log.e(TAG, "parser == null continue");
						continue;
					}
					word = parser.getParserWord();
					parser.finish();
					parser.recycle();
					
					//解析开始，开始生成字
					currentWord = SingleWord.createSingleWord(1, System.currentTimeMillis(),
							1024 * 1024);
					currentWord.begin();
					
					strokeCount = word.getStrokeCount();
					
					if (strokeCount != 0)
						for (int strokeIndx = 0; strokeIndx < strokeCount; strokeIndx++) {
							
							stroke = word.getNextStroke();
							mColor = stroke.getColor();
							
							//边解边生成
							currentStroke = SingleStroke.createSingleStroke(1, System.currentTimeMillis(), mColor, 100,
									1024);
							currentStroke.begin();
							
							pointCount = stroke.getPointCount();
							for (int pointIndx = 0; pointIndx < pointCount; pointIndx++) {
								mPoint = stroke.getNextPoint();
//								int mColor = stroke.getColor();
//								Log.e(TAG, "stroke color : " + mColor);
								if(pointIndx == 0){
									currentStroke.putPoint(mPoint);
									strokeBegin(mPoint.getXCoordinate(), mPoint.getYCoordinate());
								}else if(pointIndx +1 == pointCount){
									currentStroke.putPoint(mPoint);
									currentStroke.end();
									currentWord.putStroke(currentStroke);
									currentStroke.recycle();
									
									
									strokeEnd(mPoint.getXCoordinate(), mPoint.getYCoordinate());
									stroke.recycle();
								}else{
									currentStroke.putPoint(mPoint);
									strokeDrag(mPoint.getXCoordinate(), mPoint.getYCoordinate());
								}
							}// stroke的每个点

						}// 每个stroke

					//mPath 恢复完成
//					word.recycle();
					currentWord.end();
			
					mWord = new mParsedWord();
					mWord.pagenum = pagenum;
					mWord.aid = available_id;
					mWord.itemid = itemid;
					mWord.charType = EditableCalligraphyItem.getType(Integer.parseInt(charType));
					mWord.mPath = mPath;
					mWord.mColor = mColor;
//					Log.e("vector", "parse path>>>>>>>>>>>>:" + (mPath == null));
					mWord.Left_X = Left_X+2;
					mWord.Right_X = Right_X +2;
					mWord.Bottom_Y = Bottom_Y +2;
					mWord.Top_Y = Top_Y +2;
					
//					mWord.word = currentWord;
					
					parsedWordList.add(mWord);
	
				} catch (IOException e) {
					// TODO Auto-generated catch block
					Log.e(TAG, "parser", e);
					e.printStackTrace();
				}
			}
			
		}
		
		db.close();
		return parsedWordList;
	}
	
	public Bitmap getBitmapFromParsedWord(mParsedWord mpd,boolean zoomable) {
		// TODO Auto-generated method stub
		
		long start = System.currentTimeMillis();
		Paint paint = new Paint(mPaint);
		int color = mpd.mColor;
		paint.setColor(color);
//		Matrix currentMatrix = Start.m;
//		Log.e("vector", "Start.c:" + (Start.c == null));
		Matrix currentMatrix;
		if(Start.c == null)
			currentMatrix = Start.m;
		else
			currentMatrix = Start.c.view.getMMMatrix();
		
		if(!zoomable)
			currentMatrix = new Matrix();
		
		float[] values = new float[9];
		currentMatrix.getValues(values);
		
		Path tmpPath = new Path(mpd.mPath);
		
		float width = (mpd.Right_X - mpd.Left_X);
		float height = (mpd.Bottom_Y - mpd.Top_Y);
		
		float scale = CalligraphyVectorUtil.getScaled(width, height);
//		Log.e("pathscale", "width:" + width + " height:" + height + " scale:" + scale);
		if(width < 0)
			width =  -width;
		else if(width == 0)
			width = height;
		if(height < 0)
			height =  -height;
		else if(width == 0)
			width = height;
		
		scale *= values[0];
//		Log.e("pathscale", "width:" + width + " height:" + height + " scale:" + scale);
		
		float smal_width = 36 * values[0];
		float smal_height = 36 * values[0];
		float widthScale = smal_width/width;
		float heightScale = smal_height/height;
		
		//add OOM exception
		Bitmap b = null;
		try {
			b = Bitmap.createBitmap((int)(width * scale)+1, (int)(height * scale)+1, Bitmap.Config.ARGB_8888);
			BitmapCount.getInstance().createBitmap("getBitmapFromParsedWord");
			
			b.eraseColor(Color.TRANSPARENT);
			Canvas mc = new Canvas();
			mc.setBitmap(b);
			
			Matrix scaleMatrix = new Matrix();
//			scaleMatrix.setScale(widthScale, heightScale);
			scaleMatrix.setScale(scale, scale);
			tmpPath.offset(-mpd.Left_X, -mpd.Top_Y);
			tmpPath.transform(scaleMatrix);
			
			mc.drawPath(tmpPath, paint);
			
			tmpPath.reset();
		} catch (OutOfMemoryError e) {
			// TODO: handle exception
			b = Start.OOM_BITMAP;
		}
		
		long end = System.currentTimeMillis();
		
		return b;
	}
	
	
	
	
	private void strokeBegin(float x, float y) {
		setBound(x,y);
		mPath.moveTo(x, y);
		mX = x;
		mY = y;
	}

	private void strokeDrag(float x, float y) {
		setBound(x,y);
		float dx = Math.abs(x - mX);
		float dy = Math.abs(y - mY);
		if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
			mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
			mX = x;
			mY = y;
		}
		
	}

	private void strokeEnd(float x, float y) {
		setBound(x,y);
		mPath.lineTo(mX, mY);
	}
	
	public void setBound(float x,float y){
		
		if(x < Left_X){
			Left_X = x;
		}
if(x > Right_X){
			Right_X = x;
		}
		if(y < Top_Y){
			Top_Y = y;
			
		}
if(y > Bottom_Y){
			Bottom_Y = y;
		}
	}
	
	public void saveFile(Bitmap b, String filePath) {
		try {

			BufferedOutputStream bos = new BufferedOutputStream(
					new FileOutputStream(new File(filePath)));
			
			b.compress(Bitmap.CompressFormat.JPEG, 80, bos);
			bos.flush();

			bos.close();

		} catch (Exception e) {
			Log.e(TAG, "saveFile", e);
		}
	}
	
final static class mParsedWord{
		
		public int pagenum;
		public int aid;
		public int itemid;
		public EditableCalligraphyItem.Types charType;
		public Path mPath;
//		public SingleWord word;
		public int mColor;
		
		public float Right_X;
		public float Left_X;
		public float Bottom_Y;
		public float Top_Y;
		
		public String uri;
		public Bitmap picBitmap;
		public String matrix;
	}

public EditableCalligraphyItem getEditableCalligraphyItem(int pagenum, int available_id,int itemid,String matrix,boolean zoomable){
	//CharsWithStroke
	
	mPaint = new Paint();
	mPaint.setColor(Color.BLACK);
	mPaint.setStyle(Paint.Style.STROKE);
	mPaint.setStrokeWidth(2);
	
//	Left_X = 600;
//	Right_X = 0;
//	Top_Y = 1024;
	
	Left_X = 1600;
	Right_X = 0;
	Top_Y = 2560;
	
	Bottom_Y = 0;
	
	mParsedWord mWord = null;
	String wordpath = Start.getStoragePath() + "/calldir/free_" + pagenum + "/a" + available_id + "i" + itemid;
	File wordfile = new File(wordpath);
	long size = wordfile.length();
	if(size == 0){
		
		mWord = new mParsedWord();
		mWord.pagenum = pagenum;
		mWord.aid = available_id;
		mWord.itemid = itemid;
		mWord.charType = EditableCalligraphyItem.Types.CharsWithoutStroke;
		mWord.matrix = matrix;
		Log.e(TAG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!file size == 0");
		return null;
	}
	

	mPath = new Path();
	long start = System.currentTimeMillis();
	try {
		
		parser = ParserFactory.instance().newParser("single",
				Start.getStoragePath() + "/calldir/free_" + pagenum + "/a" + available_id + "i" + itemid, 0);
		if(parser == null){
			Log.e(TAG, "!!!!!!!!!!!!!!!!!!!!!!!!!!parser == null continue");
			return null;
		}
		word = parser.getParserWord();
		parser.finish();
		parser.recycle();
		
		//解析开始，开始生成字
		currentWord = SingleWord.createSingleWord(1, System.currentTimeMillis(),
				1024 * 1024);
		currentWord.begin();
		
		strokeCount = word.getStrokeCount();
		
		if (strokeCount != 0)
			for (int strokeIndx = 0; strokeIndx < strokeCount; strokeIndx++) {
				
				stroke = word.getNextStroke();
				mColor = stroke.getColor();
				
				//边解边生成
				currentStroke = SingleStroke.createSingleStroke(1, System.currentTimeMillis(), mColor, 100,
						1024);
				currentStroke.begin();
				
				pointCount = stroke.getPointCount();
				for (int pointIndx = 0; pointIndx < pointCount; pointIndx++) {
					mPoint = stroke.getNextPoint();
//					int mColor = stroke.getColor();
//					Log.e(TAG, "stroke color : " + mColor);
					if(pointIndx == 0){
						currentStroke.putPoint(mPoint);
						strokeBegin(mPoint.getXCoordinate(), mPoint.getYCoordinate());
					}else if(pointIndx +1 == pointCount){
						currentStroke.putPoint(mPoint);
						currentStroke.end();
						currentWord.putStroke(currentStroke);
						currentStroke.recycle();
						
						
						strokeEnd(mPoint.getXCoordinate(), mPoint.getYCoordinate());
						stroke.recycle();
					}else{
						currentStroke.putPoint(mPoint);
						strokeDrag(mPoint.getXCoordinate(), mPoint.getYCoordinate());
					}
				}// stroke的每个点

			}// 每个stroke

		//mPath 恢复完成
//		word.recycle();
		currentWord.end();
		currentWord.recycle();
		
		mWord = new mParsedWord();
		mWord.pagenum = pagenum;
		mWord.aid = available_id;
		mWord.itemid = itemid;
		mWord.charType = Types.CharsWithStroke;
		mWord.mPath = mPath;
		mWord.mColor = mColor;
		mWord.Left_X = Left_X+2;
		mWord.Right_X = Right_X +2;
		mWord.Bottom_Y = Bottom_Y +2;
		mWord.Top_Y = Top_Y +2;
		
//		mWord.word = currentWord;
	} catch (IOException e) {
		// TODO Auto-generated catch block
		Log.e(TAG, "parser", e);
		e.printStackTrace();
	}
	

	Log.e("create", "              add  charWithStroke");
	Bitmap b = null;
	VEditableCalligraphyItem editItem = null;
	editItem = new VEditableCalligraphyItem(getBitmapFromParsedWord(mWord,zoomable));
	
//	editItem.setWord(mWord.word);
	editItem.setmPath(mWord.mPath);
	editItem.setBottom_Y(mWord.Bottom_Y);
	editItem.setTop_Y(mWord.Top_Y);
	editItem.setLeft_X(mWord.Left_X);
	editItem.setRight_X(mWord.Right_X);
	editItem.setmColor(mWord.mColor);
	editItem.setItemId(itemid);
	if(Start.c == null)
		editItem.setMatrix(Start.m);
	else
		editItem.setMatrix(Start.c.view.getMMMatrix());

	return editItem;
}
	
}
