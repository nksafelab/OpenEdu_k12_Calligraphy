package hallelujah.cal.ctrl;

import hallelujah.cal.parser.CalligraphyParser;
import hallelujah.cal.producer.CalligraphyProducer;

import java.io.IOException;

import android.util.Log;

 class ParserController {
	 private static final String TAG = "ParserController";
//	static
//	{
//		System.loadLibrary("pdc_prs");
//	}
//	
	public static CalligraphyProducer newProducer(String szFilePath) throws IOException
	{
		return nativeNewProducer(szFilePath, 0);
	}
	
	public static CalligraphyProducer newProducer(String szFilePath, int nPos) throws IOException
	{
		return nativeNewProducer(szFilePath, nPos);
	}
	
	public static CalligraphyParser newParser(String szFilePath) throws IOException
	{
		CalligraphyParser p = nativeNewParser(szFilePath, 0);
		Log.i(TAG, "create prs");
		return p;
	}
	
	public static CalligraphyParser newParser(String szFilePath, int nPos) throws IOException
	{
		CalligraphyParser p = nativeNewParser(szFilePath, nPos);
		Log.i(TAG, "create prs");
		return p;
	}
	
	private static native CalligraphyProducer nativeNewProducer(String szFilePath, int nPos) throws IOException;
	
	private static native CalligraphyParser nativeNewParser(String szFilePath, int nPos) throws IOException;

}
