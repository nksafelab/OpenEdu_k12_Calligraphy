package hallelujah.cal.parser;

public class ParserWord {

	private final int mNativeWord;
	private int mType;
	private long mAbsTimeStamp;
	private int mStrokeCount;
	
	private boolean bRecycled = false;

	private ParserWord(int nativeWord) {
		if (0 == nativeWord) {
			throw new RuntimeException("internal error: native singleword is 0");
		}
		nativeInit(nativeWord);
		mNativeWord = nativeWord;
	}

	public ParserStroke getNextStroke() {
		return nativeGetNextStroke(mNativeWord);
	}
	
	public int getStrokeCount()
	{
		return mStrokeCount;
	}

	public void recycle() {
		if (!bRecycled) {
			nativeRecycle(mNativeWord);
			bRecycled = true;
		}
	}
	public void initNative(){
		nativeInit(mNativeWord);
	}
	private static native ParserStroke nativeGetNextStroke(int nativeWord);

	private native void nativeInit(int nativeWord);
	
	private native void nativeRecycle(int nativeWord);
}
