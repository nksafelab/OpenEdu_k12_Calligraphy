package hallelujah.cal.parser;

import hallelujah.cal.Point;
import android.util.Log;

public class ParserStroke {
	private int mAlgType;
	private long mAbsTimeStamp;
	private short mColorSpace;
	private int mColor;
	private int mStrokeWidth;
	private int mPointCount;

	private int mNativeStroke;

	private boolean bRecycled = false;
	
	public int getColor(){
		return mColor;
	}

	private ParserStroke(int nativeStroke) {
		if (0 == nativeStroke) {
			throw new RuntimeException("internal error: native stroke is 0!");
		}
		nativeInit(nativeStroke);
		mNativeStroke = nativeStroke;
	}

	public Point getNextPoint() {
		return nativeGetNextPoint(mNativeStroke);
	}

	public int getPointCount() {
		return mPointCount;
	}

	public void recycle() {
		if (!bRecycled) {
			nativeRecycle(mNativeStroke);
			bRecycled = true;
		}
	}

	public  native Point nativeGetNextPoint(int nativeStroke);

	private native void nativeInit(int nativeStroke);

	private native void nativeRecycle(int nativeStroke);
}
