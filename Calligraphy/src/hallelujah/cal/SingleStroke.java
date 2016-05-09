package hallelujah.cal;

import java.io.IOException;

import android.util.Log;

import hallelujah.cal.Point;

public class SingleStroke {
	private final int mNativeStroke;
	private boolean bRecycled = false;

	private SingleStroke(int nativeStroke) {
		Log.d("SingleStroke", "native stroke is " + nativeStroke);
		if (0 == nativeStroke) {
			throw new RuntimeException("internal error: native stroke is 0!");
		}
		mNativeStroke = nativeStroke;
	}

	public int nativeID() {
		return mNativeStroke;
	}

	public static SingleStroke createSingleStroke(int nAlgType, long lTimeStamp,
			int nColor, int nStrokeWidtch, int nBufferLen) {
		return nativeCreateSingleStroke(nAlgType, lTimeStamp, nColor,
				nStrokeWidtch, nBufferLen);
	}
	
	public void recycle() {
		if (!bRecycled) {
			nativeRecycle(mNativeStroke);
			bRecycled = true;
		}
	}

	public void resetStroke()
	{
		nativeResetStroke(mNativeStroke);
	}
	
	public void putPoint(Point pnt) throws IOException {
		nativePutPoint(mNativeStroke, pnt);
	}
	
	public void begin()
	{
		nativeBegin(mNativeStroke);
	}
	
	public void end()
	{
		nativeEnd(mNativeStroke);
	}

	private static native SingleStroke nativeCreateSingleStroke(int nAlgType,
			long lTimeStamp, int nColor, int nStrokeWidtch, int nBufferLen);

	private static native void nativeRecycle(int nativeSingleStroke);

	private static native void nativePutPoint(int nativeSingleStroke, Point point) throws IOException;
	
	private static native void nativeResetStroke(int nativeSingleStroke);
	
	private static native void nativeBegin(int nativeSingleStroke);
	
	private static native void nativeEnd(int nativeSingleStroke);


	
	

}
