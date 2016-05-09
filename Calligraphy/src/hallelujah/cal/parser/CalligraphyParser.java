package hallelujah.cal.parser;

import java.io.IOException;

import android.R.bool;

public class CalligraphyParser {
	
	private final int mNativeParser;
	private boolean bRecycled = false;
	
	private CalligraphyParser(int nativeParser)
	{
		if (0 == nativeParser) {
			throw new RuntimeException("internal error: native parser is 0");
		}
		mNativeParser = nativeParser;
	}
	
	public ParserWord getParserWord() throws IOException
	{
		return nativeGetParserWord(mNativeParser);
	}
	
	public void recycle()
	{
		if (!bRecycled) {
			nativeRecycle(mNativeParser);
			bRecycled = true;
		}
	}
	
	public void finish(){
		nativeFinish(mNativeParser);
	}
	
	private static native ParserWord nativeGetParserWord(int nativeParser) throws IOException;
	
	private static native void nativeFinish(int nativeParser);
	private native void nativeRecycle(int nativeParser);

}
