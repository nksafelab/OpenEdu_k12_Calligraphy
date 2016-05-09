package com.jinke.adhocNDK;

public class Jni {
    //public native String getSsidList();
    //public native int isAdhoc();
	public native int joinAdhoc(String ssid);
	public native int closeAdhoc();
	//public native String getGlobalHostList();
	public native String getLocalHost();
	//public native String getMac();
	//public native String getNetmask();
	//public native String getSsid();
    //public native int wgetDownloadFile(String IP, String filepath, String targetdir);
}
