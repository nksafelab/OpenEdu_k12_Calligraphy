package hallelujah.cal.producer;

import hallelujah.cal.SingleWord;

import java.io.IOException;

public final class CalligraphyProducer {
	private final int mNativeID;
	private boolean bRecycled = false;
	private boolean bFinished = false;

	private CalligraphyProducer(int nNativeID) {
		mNativeID = nNativeID;
	}

	public void putSingleWord(SingleWord word) throws IOException {
		nativePutSingleWord(mNativeID, word.nativeID());
	}

	public void recycle()
	{
		if (!bRecycled)
		{
			nativeRecycle(mNativeID);
			bRecycled = true;
		}
		
	}
	
	public void finish()
	{
		if (!bFinished)
		{
			try {
				nativeFinish(mNativeID);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			bFinished = true;
		}
	}
	
	public void flush()
	{
		try {
			nativeFlush(mNativeID);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private native void nativeRecycle(int nativeID);
	private native void nativeFlush(int nativeID)throws IOException;
	private native void nativeFinish(int nativeID) throws IOException;
	
	private native void nativePutSingleWord(int nativeProducer, int nativeSingleWord) throws IOException;

}
