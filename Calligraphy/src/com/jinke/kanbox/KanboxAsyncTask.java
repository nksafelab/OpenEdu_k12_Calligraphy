package com.jinke.kanbox;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerPNames;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;

import com.jinke.calligraphy.activity.Cloud;
import com.jinke.calligraphy.activity.CloudGridAdapter.cloudItem;
import com.jinke.calligraphy.app.branch.Start;
import com.jinke.single.BitmapCount;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Message;
import android.util.Log;
import android.util.EventLogTags.Description;

public class KanboxAsyncTask extends AsyncTask<String, Long, String> {
	private HttpRequestBase mHttpRequest;
	private RequestListener mRequestListener;
	private KanboxException mException;
	
	private int mOpType;
	private String mDestPath;
	private String path;
	private Bitmap b;
	/**
	 * @param destPath:长传、下载时的目标路径
	 * @param httpRequest
	 * @param listener
	 * @param opType
	 */
	public KanboxAsyncTask(String destPath, HttpRequestBase httpRequest, RequestListener listener, int opType) {
		mDestPath = destPath;
		mHttpRequest = httpRequest;
		mRequestListener = listener;
		
		mOpType = opType;
	}
	
	public KanboxAsyncTask(String path, String destPath, HttpRequestBase httpRequest, RequestListener listener, int opType) {
		this.path = path;
		mDestPath = destPath;
		mHttpRequest = httpRequest;
		mRequestListener = listener;
		mOpType = opType;
	}

	@Override
	protected String doInBackground(String... params) {
		HttpClient sHttpClient = createHttpClient();
		
		try {
			Log.e("kanbox", "httpResponse>>>>>>>>>>>>>>>>>>>>>:");
			HttpResponse sHttpResponse = sHttpClient.execute(mHttpRequest);
			
			Log.e("kanbox", "after httpResponse>>>>>>>>>>>>>>>>>>>>>:");
			int statusCode = sHttpResponse.getStatusLine().getStatusCode();
			Log.e("kanbox", "statusCode:" + statusCode);
			if (statusCode == 200) {
				switch (mOpType) {
				case RequestListener.OP_DOWNLOAD:
					return downloading(sHttpResponse.getEntity());
				case RequestListener.OP_DOWNLOAD_BITMAP:
					return downloadingBitmap(sHttpResponse.getEntity());
				default:
					String strResult = EntityUtils.toString(sHttpResponse.getEntity());
					return strResult;
				}
			} else {
				Log.e("kanbox", "execute  getstatuscode error");
				mException = new KanboxException(statusCode);
				return "error";
			}
		} catch (ClientProtocolException e) {
			Log.e("kanbox", "ClientProtocolException  getstatuscode error");
			mException = new KanboxException(e);
			return "error";
		} catch (IOException e) {
			Log.e("kanbox", "IOException  getstatuscode error");
			mException = new KanboxException(e);
			return "error";
		} catch (OutOfMemoryError error){
			Log.e("kanbox", "IOException  getstatuscode error");
			mException = new KanboxException();
			return "error";
		}
	}

	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
		Log.e("test", result + "..!." + mDestPath);
		if(result == null || result.equals("error")) {
			Log.e("kanbox", "error:", mException);
			mRequestListener.onError(mException, mOpType);
			mRequestListener.onError(mException, mOpType,path,mDestPath);
		} else {
			if(mOpType == RequestListener.OP_DOWNLOAD_BITMAP) {
				Message msg = new Message();
				cloudItem item = new cloudItem();
				item.thumbBitmap = b;
				item.name = path;
				msg.obj = item;
				Cloud.getIndexBitmapFinishhandler.sendMessage(msg);
			}else
				mRequestListener.onComplete(result + mDestPath, mOpType);
		}
	}
	
	@Override
	protected void onProgressUpdate(Long... values) {
		super.onProgressUpdate(values);
		mRequestListener.downloadProgress(values[0]);
	}
	
	/**
	 * 读取数据流，并写到本地
	 * @param entity
	 * @return
	 */
	private String downloading(HttpEntity entity) {
		InputStream is = null;
		FileOutputStream fos = null;
		try {
			int size = 1024;	//每10K通知一次（用于更新进度条）
			
			is = entity.getContent();
			long totalLength = entity.getContentLength();
			fos = new FileOutputStream(mDestPath);
			byte[] buf = new byte[size];
			int num = -1;
			long count = 0, sendMessageNextPos = 0;
			
			if (is != null) {
				while ((num = is.read(buf)) != -1) {
					fos.write(buf, 0, num);
					count += num;
					
					if(count > sendMessageNextPos) {
						sendMessageNextPos += size;
						float f = (float)count/totalLength;
						long cc = (long) (f*100);
						Log.e("download", "float:" + f + " long:" + cc);
//						publishProgress(new Long[]{count} );
						publishProgress(new Long[]{cc} );
					}
				}
			}
			return "ok";
		} catch (IllegalStateException e) {
			Log.e("kanbox", "IllegalStateException:", mException);
			mException = new KanboxException(e);
			return "error";
		} catch (IOException e) {
			Log.e("kanbox", "downloading IOException:", mException);
			mException = new KanboxException(e);
			return "error";
		}finally{
			
				try {
					if(is != null)
						is.close();
					if(fos != null){
						fos.flush();
						fos.close();
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
		}
	}
	
	/**
	 * 读取数据流，生成bitmap
	 * @param entity
	 * @return
	 */
	private String downloadingBitmap(HttpEntity entity) {
		try {
			int size = 10 * 1024;	//每10K通知一次（用于更新进度条）
			
			InputStream is = entity.getContent();
			 try {
				 b = BitmapFactory.decodeStream(is);
				 BitmapCount.getInstance().createBitmap("KanboxAsyncTask downloadingBitmap");
			 }catch(OutOfMemoryError error){
				 b = Start.EMPTY_BITMAP;
			 }
			Log.e("Cloud", "decodeStream finish " + mDestPath + "--------");
			return "ok";
		} catch (IllegalStateException e) {
			Log.e("kanbox", "IllegalStateException:", mException);
			mException = new KanboxException(e);
			return "error";
		} catch (IOException e) {
			Log.e("kanbox", "downloading IOException:", mException);
			mException = new KanboxException(e);
			return "error";
		}
	}
	

	public static DefaultHttpClient createHttpClient() {

		final HttpParams httpParams = createHttpParams();

		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		schemeRegistry.register(new Scheme("https", new EasySSLSocketFactory(), 443));

		ClientConnectionManager cm = new ThreadSafeClientConnManager(httpParams, schemeRegistry);
		return new DefaultHttpClient(cm, httpParams);
	}

	private static HttpParams createHttpParams() {

		final HttpParams params = new BasicHttpParams();

		HttpConnectionParams.setStaleCheckingEnabled(params, false);

		HttpConnectionParams.setConnectionTimeout(params, 10 * 1000);
		HttpConnectionParams.setSoTimeout(params, 10 * 1000);
		HttpConnectionParams.setSocketBufferSize(params, 8192);

		params.setParameter(ConnManagerPNames.MAX_TOTAL_CONNECTIONS, 30);
		params.setParameter(ConnManagerPNames.MAX_CONNECTIONS_PER_ROUTE, new ConnPerRouteBean(30));
		params.setParameter(HttpProtocolParams.USE_EXPECT_CONTINUE, false);
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);

		return params;
	}
}
