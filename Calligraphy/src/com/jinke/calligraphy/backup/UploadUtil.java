package com.jinke.calligraphy.backup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;


import android.util.Base64;
import android.util.Log;

public class UploadUtil {
	
	private static final String TAG = "UploadUtil";
	private String uploadUrl = "http://61.181.14.184:8084/AndroidService/calligraphybackup.do";
	
	private HttpPost httpRequest;
	private List<NameValuePair> values;
	
	public UploadUtil(){
		/*
		 * 建立HTTP post 连接
		 */
		httpRequest = new HttpPost(uploadUrl);
	}
	//use HttpPost
	public void uploadByHttpPost(int templateID,int availableID,int pageNum,
			int itemID,int flipBottom,int flipDst,
			String charType,String matrix,
			String created,byte[] charBitmap,String simID ,String uri) throws IOException{
		
		
		
		//post 传送参数使用NameValuePair[]阵列
		values = new ArrayList<NameValuePair>();
		
		values.add(new BasicNameValuePair("templateID", templateID+""));
		values.add(new BasicNameValuePair("availableID", availableID+""));
		values.add(new BasicNameValuePair("pageNum", pageNum+""));
		values.add(new BasicNameValuePair("itemID", itemID+""));
		values.add(new BasicNameValuePair("flipBottom", flipBottom+""));
		values.add(new BasicNameValuePair("flipDst", flipDst+""));
		values.add(new BasicNameValuePair("charType", charType+""));
		values.add(new BasicNameValuePair("matrix", matrix+""));
		values.add(new BasicNameValuePair("created", created+""));
		values.add(new BasicNameValuePair("simID", simID+""));
		values.add(new BasicNameValuePair("uri", uri+""));
		
		Log.e("upload", "flipBottom"+flipBottom);
		Log.e("upload", "-----------------------simID"+simID);
		byte[] encod = null;
//		if(charBitmap != null && "".equals(uri)){
		if(charBitmap != null && !"7".equals(charType)){
		//编码charBitmap to String
			Log.e("delete", "to delete pageNum:" + pageNum + " itemID:" + itemID + " length:" + charBitmap.length);
			encod = Base64.encode(charBitmap, Base64.DEFAULT);
			values.add(new BasicNameValuePair("charBitmap", new String(encod)));
			Log.e("upload", new String(encod));
		}else{
			values.add(new BasicNameValuePair("charBitmap", ""));
			Log.e("upload", "空格或者换行");
		}
		
		
		
		
//		try {
			//发出HttpRequest
			httpRequest.setEntity(new UrlEncodedFormEntity(values,HTTP.UTF_8));
			//获得httpResponse
			HttpResponse httpResponse = new DefaultHttpClient().execute(httpRequest);
			
			//获得状态码 200ok
			if(httpResponse.getStatusLine().getStatusCode() == 200){
				//取出回应字符串
				String strResult = EntityUtils.toString(httpResponse.getEntity());
				Log.i(TAG, "response :"+ strResult);
			}else{
				Log.i(TAG, "Error response :"+ httpResponse.getStatusLine().toString());
			}
			
//		} catch (UnsupportedEncodingException e) {
//			// TODO Auto-generated catch block
//			Log.e(TAG, "exception", e);
//		} catch (ClientProtocolException e) {
//			// TODO Auto-generated catch block
//			Log.e(TAG, "exception", e);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			Log.e(TAG, "exception", e);
//		}
		
	}
}
