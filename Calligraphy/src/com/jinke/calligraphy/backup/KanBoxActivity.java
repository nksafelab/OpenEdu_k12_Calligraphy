package com.jinke.calligraphy.backup;

import com.jinke.calligraphy.app.branch.R;
import com.jinke.calligraphy.app.branch.Start;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class KanBoxActivity extends Activity{

	private WebView kanBoxWv;
	private String url = "https://auth.kanbox.com/0/auth?response_type=code&client_id=45fd6312c0c847d62017e483f05f5f50&redirect_uri=Cloud Note&user_language=ZH&user_platform=android"; 
	private String code = "";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.kanbox);
		
		kanBoxWv = (WebView)findViewById(R.id.kanBoxWv);
		
		kanBoxWv.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) { // 重写此方法表明点击网页里面的链
				// 接还是在当前的webview里跳转，不跳到浏览器那边
				Log.e("content", "loadUrl:" + url);
				int start = url.indexOf("code=") + 5;
				code = url.substring(start, url.length());
				Log.e("content", "get code:" + code);
				Start.LoginInfo.edit().putString("code", code).commit();
				
				SharedPreferences s = KanBoxActivity.this.getSharedPreferences("LoginInfo", MODE_WORLD_WRITEABLE);
				Boolean b = s.edit()
				.putString("code", code)
				.putBoolean("update", true)
				.commit();
				Log.e("content", ""+ b);
				
				KanBoxUtil util = new KanBoxUtil(code);
				util.getJsonFromHttps("https://auth.kanbox.com/0/token");
				
				KanBoxActivity.this.finish();
				
//				access_token = getJsonFromHttps("https://auth.kanbox.com/0/token");
//				Log.e("content", "===============token============="
//						+ access_token);
//				int startToken = access_token.indexOf("access_token") + 15;
//				int endToken = startToken + 32;
//				access_token = access_token.substring(startToken, endToken);
//				Log.e("content", "===============token============="
//						+ access_token);
//				// download.loadUrl(downloadUrl);
//				Log.e("content", "===============downloadUrl=============");
//				
//				GetHttps();
//				//getHttpsR();
				

				return true;
			}

			@Override
			public void onPageFinished(WebView webview, String url) {
				// TODO Auto-generated method stub
				super.onPageFinished(webview, url);

			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				// TODO Auto-generated method stub
				super.onPageStarted(view, url, favicon);
			}

		});
		
		
		kanBoxWv.loadUrl(url);
		
	}
}
