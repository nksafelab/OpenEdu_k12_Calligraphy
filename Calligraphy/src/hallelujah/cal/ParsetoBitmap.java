package hallelujah.cal;

import hallelujah.cal.Point;
import hallelujah.cal.ctrl.ParserFactory;
import hallelujah.cal.parser.CalligraphyParser;
import hallelujah.cal.parser.ParserStroke;
import hallelujah.cal.parser.ParserWord;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.util.Log;

public class ParsetoBitmap extends Activity {

	static {
		System.loadLibrary("pdc_prs");
	}
	
	private CalligraphyParser parser = null;
	private ParserWord word = null;
	private ParserStroke stroke = null;
	private Path mPath = null;
	private Point mPoint = null;
	private int strokeCount = 0;// 笔画数
	private int pointCount = 0;// 笔画的点数目
	private float mX = 0;
	private float mY = 0;
//	private float Left_X = 600;
	
	private float Left_X = 1600;
	
	private float Right_X = 0;
//	private float Top_Y = 1024;
	
	private float Top_Y = 2560;
	
	private float Bottom_Y = 0;
	
	private float TOUCH_TOLERANCE = 4.0f;
	private static final String TAG = "ParsetoBitmap";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		
		Paint mPaint = new Paint();
		mPaint.setColor(0xFFFF0000);
		mPaint.setStyle(Paint.Style.STROKE);
		
		//02-23 10:32:57.460: D/JNI_CalligraphyParser(17226): 
		//frameworks/base/calligraphyEx/parse/jni/hallelujah/cal/parser/CalligraphyParser.cpp, 
		//_jobject* Parser_GetParserWord(JNIEnv*, _jobject*, hprs::CalligraphyParser*), 56

		
		mPath = new Path();

		long start = System.currentTimeMillis();
		Log.e(TAG, "Start from get parser:>>>>>>>>>>>>>>" + start);
		try {
			parser = ParserFactory.instance().newParser("single",
					"/mnt/extsd/calldir/free_2/a0i1", 0);
			word = parser.getParserWord();
			strokeCount = word.getStrokeCount();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (strokeCount != 0)
			for (int strokeIndx = 0; strokeIndx < strokeCount; strokeIndx++) {
				
				stroke = word.getNextStroke();
				pointCount = stroke.getPointCount();
				for (int pointIndx = 0; pointIndx < pointCount; pointIndx++) {
					mPoint = stroke.getNextPoint();
					if(pointIndx == 0){
						strokeBegin(mPoint.getXCoordinate(), mPoint.getYCoordinate());
					}else if(pointIndx +1 == pointCount){
						strokeEnd(mPoint.getXCoordinate(), mPoint.getYCoordinate());
						stroke.recycle();
					}else{
						strokeDrag(mPoint.getXCoordinate(), mPoint.getYCoordinate());
					}
				}// stroke的每个点

			}// 每个stroke

		//mPath 恢复完成
		word.recycle();
		
		float width = (Right_X - Left_X);
		float height = (Bottom_Y - Top_Y);
		float smal_width = 50;
		float smal_height = 50;
		float widthScale = smal_width/width;
		float heightScale = smal_height/height;
		
		Bitmap b = Bitmap.createBitmap((int)smal_width, (int)smal_height, Bitmap.Config.ARGB_8888);
		b.eraseColor(Color.WHITE);
		Canvas mc = new Canvas();
		mc.setBitmap(b);
		
		Matrix scaleMatrix = new Matrix();
		scaleMatrix.setScale(widthScale, heightScale);
		mPath.offset(-Left_X, -Top_Y);
		mPath.transform(scaleMatrix);
		
		mc.drawPath(mPath, mPaint);
		
		mPath.reset();
		
		long end = System.currentTimeMillis();
		Log.e(TAG, "End create Bitmap:>>>>>>>>>>>>>>" + end);
		Log.e(TAG, "a word create used:>>>>>>>>>>>>>>" + (end - start) + " ms");
		
		saveFile(b, "/extsd/vectorBitmap.jpg");
		
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
		}else if(x > Right_X){
			Right_X = x;
		}
		if(y < Top_Y){
			Top_Y = y;
			
		}else if(y > Bottom_Y){
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
	
}
