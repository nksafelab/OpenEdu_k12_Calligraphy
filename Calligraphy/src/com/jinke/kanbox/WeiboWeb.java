package com.jinke.kanbox;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;

import org.json.JSONException;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.jinke.calligraphy.app.branch.R;
import com.jinke.calligraphy.app.branch.Start;
import com.jinke.kanbox.KanboxException;
import com.jinke.kanbox.RequestListener;
import com.jinke.kanbox.Token;

public class WeiboWeb extends Activity implements RequestListener {
	protected WebView web;
	protected ProgressBar mProgressBar;
	protected String todo = "";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.web);
		todo = getIntent().getStringExtra("todo");

		web = (WebView) findViewById(R.id.web);
		mProgressBar = (ProgressBar) this.findViewById(R.id.web_process);

		web.getSettings().setJavaScriptEnabled(true);
		web.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
		web.clearCache(true);
		web.clearCache(true);
		web.clearHistory();
		web.clearFormData();
		web.setWebViewClient(new EmbeddedWebViewClient());
		web.setWebChromeClient(new WebChromeClient() {
			@Override
			public void onProgressChanged(WebView view, int progress) {
				mProgressBar.setProgress(progress);
			}
		});
		Log.e("url:", "getUrl:"+getIntent().getStringExtra("url"));
		web.loadUrl(getIntent().getStringExtra("url"));
	}

	private class EmbeddedWebViewClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			Log.e("kanbox", "loading:" + url);
			view.loadUrl(url);
			return true;
		}

		private void onProgressFinished() {
			mProgressBar.setVisibility(View.GONE);
		}

		@Override
		public void onPageFinished(WebView wv, String url) {
			onProgressFinished();
		}

		@Override
		public void onPageStarted(WebView wv, String url, Bitmap favicon) {
			mProgressBar.setVisibility(View.VISIBLE);
			handleUrl(url);
		}

		@Override
		public void onReceivedError(WebView wv, int errorCode, String description, String failingUrl) {
			onProgressFinished();
		}

		@Override
		public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
			handler.proceed();
		}
	}

	@Override
	public void onResume() {
		//WebView.enablePlatformNotifications();
		super.onResume();
	}

	@Override
	public void onPause() {
		//WebView.disablePlatformNotifications();
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (web.canGoBack()) {
				web.goBack();
			} else {
				web.stopLoading();
				// destoryThread();
				WeiboWeb.this.finish();
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private boolean haveGetToken = false;

	private void handleUrl(String url) {
		if (haveGetToken) return;
		if (url != null && url.startsWith("https://www.kanbox.com")) {
			haveGetToken = true;
			String code = url.substring(url.indexOf("code=") + 5);

			try {
				Token.getInstance().getToken(Start.CLIENT_ID, Start.CLIENT_SECRET, code, Start.REDIRECT_URI, WeiboWeb.this);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void onComplete(String response, int operationType) {
		try {
			Token.getInstance().parseToken(response);
			saveToken(Token.getInstance());
			if("update".equals(todo)){
				Toast.makeText(this, "操作成功" + response + "\n 开启上传线程", Toast.LENGTH_LONG).show();
				Log.e("check", "after oauth upload");
				(new UploadAllFileThread(true)).start();
			}else{
				Toast.makeText(this, "操作成功" + response + "\n 开启下载线程", Toast.LENGTH_LONG).show();
				(new DownloadAllFileThread(DownloadAllFileThread.OP_GETLIST,"")).start();
			}
			
			WeiboWeb.this.finish();
		} catch (JSONException e) {
			e.printStackTrace();
			Toast.makeText(this, "操作失败" + response, Toast.LENGTH_LONG).show();
		}
		
	}

	@Override
	public void onError(KanboxException error, int operationType) {
		Toast.makeText(this, "操作失败" + error.toString(), Toast.LENGTH_LONG).show();
	}

	@Override
	public void downloadProgress(long currSize) {
		// TODO Auto-generated method stub
		
	}
	
	public void saveToken(Token token) {
		PushSharePreference sPreference = new PushSharePreference(this, "oauth");
		sPreference.saveStringValueToSharePreferences("accecc_token", token.getAcceccToken());
		sPreference.saveStringValueToSharePreferences("refresh_token", token.getRefreshToken());
		sPreference.saveLongValueToSharePreferences("expries", token.getExpires());
		sPreference.saveStringValueToSharePreferences("save_time", getCurrentTime());
	}
	
	public String getCurrentTime(){
		Calendar c = Calendar.getInstance();
				
				String saveTime = 
					c.get(Calendar.YEAR) + "-" + 
						c.get(Calendar.MONTH) + "-" + 
							c.get(Calendar.DAY_OF_MONTH) + " " + 
									c.get(Calendar.HOUR_OF_DAY) + "-" + 
											c.get(Calendar.MINUTE) + "-" + 
													c.get(Calendar.SECOND);
		return saveTime;
		
	}

	@Override
	public void onError(KanboxException error, int operationType, String path,
			String destPath) {
		// TODO Auto-generated method stub
		
	}
	
}