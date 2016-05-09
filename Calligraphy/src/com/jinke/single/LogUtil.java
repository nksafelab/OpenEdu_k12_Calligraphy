package com.jinke.single;

import android.util.Log;

public class LogUtil {

	private boolean logSwitch = false;
	private static LogUtil logUtil = null;
	public static LogUtil getInstance(){
		if(logUtil == null){
			synchronized (LogUtil.class) {
				if(logUtil == null){
					logUtil = new LogUtil();
				}
			}
		}
		return logUtil;
	}
	
	public void v(String tag,String msg){
		if(logSwitch)
			Log.v(tag, msg);
	}
	public void e(String tag,String msg){
		if(logSwitch)
			Log.e(tag, msg);
	}
	public void v(String tag,String msg,Throwable tr){
		if(logSwitch)
			Log.v(tag, msg, tr);
	}
	public void e(String tag,String msg,Throwable tr){
		if(logSwitch)
			Log.e(tag, msg, tr);
	}
}
