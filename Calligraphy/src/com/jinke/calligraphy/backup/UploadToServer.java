package com.jinke.calligraphy.backup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
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

import com.jinke.calligraphy.app.branch.Start;
import com.jinke.calligraphy.database.CDBPersistent;

import android.content.Context;
import android.database.Cursor;
import android.os.RemoteException;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;

public class UploadToServer {

	private static final String TAG = "UploadToServer";
	private Context context;
	public static final int UPLOAD_YES = 1;
	public static final int UPLOAD_NO = 0;
	private String uploadUrl = "http://61.181.14.184:8084/AndroidService/calligraphybackup.do";
	private UploadUtil uploadUtil;
	public UploadToServer(Context context){
		this.context = context;
		uploadUtil = new UploadUtil();
	}
	
	public void upload() throws IOException{
		
		//查询本地数据库，获得要上传的数据
		CDBPersistent db = new CDBPersistent(context);
		db.open();
		Cursor cursor = db.getUploadCursorByPage();
		
		
		int templateID = 0;
		int pageNum = 0;
		int availableID = 0;
		int itemID = 0;
		int flipBottom = 0;
		int flipDst = 0;
		String charType = "";
		String uri = "";
		
		String matrix = "";
		String created = "";
		
		String simID = "";
		if("123456".equals(CalligraphyBackupUtil.getSimID())){
			TelephonyManager telephonyManager=(TelephonyManager) Start.context.getSystemService(Context.TELEPHONY_SERVICE);
			simID=telephonyManager.getDeviceId();
		}else
			simID = CalligraphyBackupUtil.getSimID();
		
		
		
		
		
		
		byte[] byteBitmap = null;
		
		Log.e("upload", "!!!!!!!!!!"+cursor.getCount());
		//post方式挨个上传
		for(cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext()){
			templateID = cursor.getInt(cursor.getColumnIndex("template_id"));
			pageNum = cursor.getInt(cursor.getColumnIndex("pagenum"));
			availableID = cursor.getInt(cursor.getColumnIndex("available_id"));
			itemID = cursor.getInt(cursor.getColumnIndex("itemid"));
			flipBottom = cursor.getInt(cursor.getColumnIndex("flipbottom"));
			flipDst = cursor.getInt(cursor.getColumnIndex("flipdst"));
			
			charType = cursor.getString(cursor.getColumnIndex("charType"));
			byteBitmap = cursor.getBlob(cursor.getColumnIndex("charBitmap"));
			matrix = cursor.getString(cursor.getColumnIndex("matrix"));
			created = cursor.getString(cursor.getColumnIndex("created"));
			uri = cursor.getString(cursor.getColumnIndex("uri"));
			
			Log.e("upload", "pageNum"+pageNum+" available_id:"+availableID+" itemID:"+itemID + "uri:" + uri);
			
			if(pageNum == 27 && byteBitmap != null)
				Log.e("page27", "pagenum:" + pageNum + "charBitmap length? :" + byteBitmap.length);
			
			uploadUtil.uploadByHttpPost(templateID, availableID, pageNum,
					itemID, flipBottom, flipDst,
					charType, matrix,
					created, byteBitmap,simID , uri);
			
			
			//回写上传结果
//			db.updateUploadedStatus(pageNum, templateID, itemID);
			
		}
		
		
		
		db.close();
		
		
		
	}
	
	//use HttpPost
	public void uploadByHttpPost(byte[] b){
		
		
		
		/*
		 * 建立HTTP post 连接
		 */
		HttpPost httpRequest = new HttpPost(uploadUrl);
		//post 传送参数使用NameValuePair[]阵列
		List<NameValuePair> values = new ArrayList<NameValuePair>();
		
		byte[] encod = Base64.encode(b, Base64.DEFAULT);
		
		values.add(new BasicNameValuePair("name", new String(encod)));
		
		Log.e("upload", new String(encod));
		
		try {
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
			
			
			
			
			
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			Log.e(TAG, "exception", e);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			Log.e(TAG, "exception", e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.e(TAG, "exception", e);
		}
	}
	
	public void uploadByHttpURLConnection(byte[] b){
		
		
		try {
			//建立连接
			URL url = new URL(uploadUrl);
			HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
			
			//设置连接属性
			httpConn.setDoInput(true);//使用URL进行输入
			httpConn.setDoOutput(true);//使用URL进行输出
			httpConn.setUseCaches(false);//忽略缓存
			httpConn.setRequestMethod("POST");//设置URL请求方式
			
			String uploadStr = "upload value";
			
			//设置请求属性
			byte[] uploadStrBytes = uploadStr.getBytes();
			
			httpConn.setRequestProperty("Content-length", "" + b.length);
	        httpConn.setRequestProperty("Content-Type", "application/octet-stream");
	        httpConn.setRequestProperty("Connection", "Keep-Alive");// 维持长连接
	        httpConn.setRequestProperty("Charset", "UTF-8");
	        
	        String name=URLEncoder.encode("金洋","utf-8");
	        httpConn.setRequestProperty("NAME", "");
	        
	        //建立输出流，并写入数据
	        OutputStream outputStream = httpConn.getOutputStream();
	        outputStream.write(b);
	        outputStream.close();
	        
	        //获得响应状态
	        int responseCode = httpConn.getResponseCode();
	        if(responseCode == HttpURLConnection.HTTP_OK){//连接成功
	        	//正确响应时处理数据
	        	StringBuffer sb = new StringBuffer();
	        	String readLine;
	        	//处理响应流，编码必须与处理器响应流保持一致
	        	BufferedReader responseReader;
	        	responseReader = new BufferedReader(new InputStreamReader(httpConn.getInputStream(),"UTF-8"));
	        	
	        	while((readLine = responseReader.readLine()) != null){
	        		sb.append(readLine).append("\n");
	        	}
	        	responseReader.close();
	        	Log.i(TAG, "HttpURLConnection response :"+ sb.toString());
	        }
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			Log.e(TAG, "httpURLConnection exception", e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.e(TAG, "httpURLConnection exception", e);
		}
		
		
	}
	
}
