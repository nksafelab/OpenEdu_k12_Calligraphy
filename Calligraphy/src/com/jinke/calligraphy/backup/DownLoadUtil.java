package com.jinke.calligraphy.backup;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.util.Log;

public class DownLoadUtil {
	public static final String TAG = "DownLoadUtil";

	public static InputStream getInputStream(String urlStr){
		Log.e("downLoadUtil", urlStr);
		 	InputStream in = null;
	        URL url;
			try {
				url = new URL(urlStr);
				HttpURLConnection urlConn = (HttpURLConnection)url.openConnection();
				in = urlConn.getInputStream();
				
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.e(TAG, "download exception", e);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.e(TAG, "download exception", e);
			}
			
			return in;
	}
}
