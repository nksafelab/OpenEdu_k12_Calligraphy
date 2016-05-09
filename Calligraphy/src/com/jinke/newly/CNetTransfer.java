package com.jinke.newly;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.util.Log;

public class CNetTransfer {

	 public static String getUrl(String url) throws IOException {  
	        HttpGet request = new HttpGet(url);  
	        HttpClient httpClient = new DefaultHttpClient();  
	        HttpResponse response = httpClient.execute(request);  
	        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {  
	            return EntityUtils.toString(response.getEntity());  
	        } else {  
	            return "";  
	        }  
	      
	    }  
	
	public static String Get(String szUrl) throws Exception {
		URL oUrl = new URL(szUrl);
//		String szRes = "";
//		URLConnection con = oUrl.openConnection();
//
//		szRes = "http status code: "
//				+ ((HttpURLConnection) con).getResponseCode() + "\n";
//		// HttpURLConnection.HTTP_OK
//
//		InputStream is = con.getInputStream();
//		BufferedInputStream bis = new BufferedInputStream(is);
//		ByteArrayBuffer bab = new ByteArrayBuffer(32);
//		int current = 0;
//		while ((current = bis.read()) != -1) {
//			bab.append((byte) current);
//		}
//		szRes += EncodingUtils.getString(bab.toByteArray(), HTTP.UTF_8);
//
//		bis.close();
//		is.close();
		HttpClient client = new DefaultHttpClient();
        HttpGet get = new HttpGet(szUrl);
        String s = "";
        try {
        	
            HttpResponse response = client.execute(get);
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    response.getEntity().getContent()));
            for (s = reader.readLine(); s != null; s = reader.readLine()) {
                Log.v("response", s);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
		return s;
	}

	public static InputStream GetXmlInputStream(String szUrl) throws Exception {
		// URL oUrl = new URL(szUrl);
		// URLConnection con = oUrl.openConnection();
		// InputStream is = con.getInputStream();
		// return is;
		HttpClient oHttpClient = new DefaultHttpClient();
		HttpGet oHttpGet = new HttpGet();
		oHttpGet.setURI(new URI(szUrl));
		HttpResponse oHttpResponse = oHttpClient.execute(oHttpGet);
		HttpEntity oHttpEntity = oHttpResponse.getEntity();
		InputStream oIS = oHttpEntity.getContent();
		return oIS;
	}

	public static String GetXml(String szUrl) throws Exception {
		String szXml = "";

		/*
		 * HttpGet oHttpGet = new HttpGet(szUrl); ResponseHandler<String>
		 * oRpsHdl = new BasicResponseHandler();
		 */

		/*
		 * DefaultHttpClient oHttpClient = new DefaultHttpClient(); HttpPost
		 * oHttpPost = new HttpPost(szUrl); HttpResponse oHttpResponse =
		 * oHttpClient.execute(oHttpPost); HttpEntity oHttpEntity =
		 * oHttpResponse.getEntity(); InputStream oIn =
		 * oHttpEntity.getContent(); byte[] byteBuf = new byte[1024]; int nSize
		 * = 0; while ((nSize = oIn.read(byteBuf)) != -1) {
		 * 
		 * szXml += new String(byteBuf, "utf-8");
		 * 
		 * // sz = new String(sz.getBytes(""));
		 * 
		 * }
		 */

		HttpClient oHttpClient = new DefaultHttpClient();
		HttpGet oHttpGet = new HttpGet();
		oHttpGet.setURI(new URI(szUrl));
		HttpResponse oHttpResponse = oHttpClient.execute(oHttpGet);
		HttpEntity oHttpEntity = oHttpResponse.getEntity();
		InputStream oIn = oHttpEntity.getContent();
		// BufferedInputStream oBufRd = new BufferedInputStream(oIn);

		byte[] byteBuf = new byte[1024];
		int nSize = 0;
		while ((nSize = oIn.read(byteBuf)) != -1) {
			//szXml += new String(byteBuf,0 , nSize,"gbk");
			szXml += new String(byteBuf,0 , nSize,"utf-8");
			Log.e("size", ""+ nSize);
		}
		
		oIn.close();
		
		return szXml;
	}

	public InputStream GetBitStream(String szUrl) throws Exception {
		URL url = new URL(szUrl);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setConnectTimeout(30 * 1000);
		conn.setReadTimeout(60 * 1000);
		conn.setDoInput(true);// 允许输入
		conn.setDoOutput(true);// 允许输出
		conn.setUseCaches(false);// 不使用Cache
		conn.setRequestMethod("GET");
		// conn.setRequestProperty("Connection", "Keep-Alive");
		conn.setRequestProperty("Charset", "UTF-8");
		conn.connect();

		if (conn.getResponseCode() != 200) {
			throw new RuntimeException("request exception");
		}
		InputStream inputStream = conn.getInputStream();
		return inputStream;

	}
	
	public static InputStream getBitStreamEx(String szUrl) throws Exception{   
        URL url = new URL(szUrl);   
        URLConnection connection = url.openConnection();
        return connection.getInputStream();  
    }   
	
	public static InputStream postInformation(String szPostUrl, String szComment) throws Exception
	{
		URL url = new URL(szPostUrl);
		HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
		httpConn.setDoOutput(true);
		httpConn.setDoInput(true);
		httpConn.setRequestMethod("POST");
		
//        httpConn.setRequestProperty("Content-length", "" + szComment.length);
//        httpConn.setRequestProperty("Content-Type", "application/octet-stream");
//        httpConn.setRequestProperty("Connection", "Keep-Alive");
//        httpConn.setRequestProperty("Charset", "UTF-8");
//        //
//        httpConn.setRequestProperty("NAME", "");
//		httpConn.setUseCaches(false);
		OutputStream outputStream = httpConn.getOutputStream();
		InputStream inputStream = httpConn.getInputStream();
		outputStream.write(szComment.getBytes());
		outputStream.close();
		
		if (httpConn.getResponseCode() != HttpURLConnection.HTTP_OK)
		{
			System.out.println("postInformation@CNetTransfer failed");
			return null;
		}
		
		return inputStream;
	}
	
	public static boolean postFormInfo(String szPostUrl, String szComment) throws Exception
	{
		HttpClient oHttpClient = new DefaultHttpClient();
		HttpPost oHttpPost = new HttpPost();
		oHttpPost.setURI(new URI(szPostUrl));
		List <NameValuePair> params=new ArrayList<NameValuePair>();
	    params.add(new BasicNameValuePair("comment",szComment));
	     oHttpPost.setEntity(new UrlEncodedFormEntity(params,HTTP.UTF_8));
		HttpResponse oHttpResponse = oHttpClient.execute(oHttpPost);
		//HttpEntity oHttpEntity = oHttpResponse.getEntity();
		if (oHttpResponse.getStatusLine().getStatusCode() != HttpURLConnection.HTTP_OK)
		{
			System.out.println("postInformation@CNetTransfer failed");
		}
		return false;
	}
	
	public static InputStream postRegiste(String szUrl) throws Exception
	{
		URL url = new URL(szUrl);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		return conn.getInputStream();
	}
	
	//以图片URL和存放路径为参数
	public static Boolean writeImage(String strUrl,String filepath)
	{
		URL url = null;
		try
		{
			url = new URL(strUrl);
		}catch(MalformedURLException e2)
		{
			e2.printStackTrace();
		}
		InputStream is  = null;
		try
		{
			is = url.openStream();
		}catch(IOException e1)
		{
			e1.printStackTrace();
			return false;
		}
		OutputStream os = null;
//		File f = new File("/mnt/sdcard/homework/");
//		f.mkdirs();

		try
		{
			File f = new File(filepath);
			if(!f.exists())
				f.createNewFile();
			os = new FileOutputStream(f,false);
			int bytesRead = 0;
			byte[] buffer = new byte[8192];
			while((bytesRead = is.read(buffer, 0, 8192))!=-1)
				os.write(buffer,0,bytesRead);
		}
		catch(FileNotFoundException e)
		{
			return false;
		}catch(IOException e)
		{
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
}
