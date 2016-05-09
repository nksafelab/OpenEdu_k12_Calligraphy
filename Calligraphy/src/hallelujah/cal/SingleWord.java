package hallelujah.cal;

import java.io.IOException;


public final class SingleWord {
	private final int mNativeSingleWord;
	private boolean bRecycled = false;
	public static int pagenum;
	public static int aid;
	public static int itemid;

	private SingleWord(int nativeSingleWord) {
		if (0 == nativeSingleWord) {
			throw new RuntimeException("internal error: native singleword is 0");

		}
		mNativeSingleWord = nativeSingleWord;
	}

	public final int nativeID() {
		return mNativeSingleWord;
	}

	public static SingleWord createSingleWord(int nType, long lAbsTS,
			int nBufferLen) {
		
		return nativeCreateSingleWord(nType, lAbsTS, nBufferLen);
	}
	

	public void recycle() {
		if (!bRecycled) {
			nativeRecycle(mNativeSingleWord);
			bRecycled = true;
		}
	}
	public boolean isRecycle(){
		return bRecycled;
	}

	public void putStroke(SingleStroke stroke) throws IOException {
		nativePutStroke(mNativeSingleWord, stroke.nativeID());
	}

	public void begin() {
		nativeBegin(mNativeSingleWord);
	}

	public void end() {
		nativeEnd(mNativeSingleWord);
	}

	private static native SingleWord nativeCreateSingleWord(int nType,
			long lAbsTS, int nBufferLen);

	private static native void nativeRecycle(int nativeSingleWord);

	private static native void nativePutStroke(int nativeSingleWord,
			int nativeSingleStroke);

	private static native void nativeBegin(int nativeSingleWord);

	private static native void nativeEnd(int nativeSingleWord);

}