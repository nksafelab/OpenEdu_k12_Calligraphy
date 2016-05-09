package com.jinke.kanbox;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import org.apache.http.client.methods.HttpRequestBase;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class Token {
	private String acceccToken;
	private String refreshToken;
	private long expires;
	private String saveString;
	private static Token mToken;
	
	private Token() {
	}
	
	public static final Token getInstance() {
		if(mToken == null) {
			mToken = new Token();
		}
		return mToken;
	}
	
	/**
	 * 解析token内容
	 * @param response
	 * @throws JSONException
	 */
	public void parseToken(String response) throws JSONException {
		JSONObject sData = new JSONObject(response);
		acceccToken = sData.getString("access_token");
		expires = sData.getLong("expires_in");
		refreshToken = sData.getString("refresh_token");
	}

	public void setToken(String acceccToken, String refreshToken, long expires) {
		this.acceccToken = acceccToken;
		this.refreshToken = refreshToken;
		this.expires = expires;
	}
	
	/**
	 * 用code换取token
	 * @param clientId
	 * @param clientSecret
	 * @param code
	 * @param redirectUrl
	 * @param listener
	 * @throws UnsupportedEncodingException
	 */
	public void getToken(String clientId, String clientSecret, String code, String redirectUrl, RequestListener listener) throws UnsupportedEncodingException {
		String getTokenUrl = "https://auth.kanbox.com/0/token";
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("grant_type", "authorization_code");
		params.put("client_id", clientId);
		params.put("client_secret", clientSecret);
		params.put("code", code);
		params.put("redirect_uri", redirectUrl);
		
		HttpRequestBase httpMethod = KanboxHttp.doPost(getTokenUrl, params);
		new KanboxAsyncTask(null, httpMethod, listener, RequestListener.OP_GET_TOKEN).execute();
	}
	
	/**
	 * 刷新access_token
	 * @param clientId
	 * @param clientSecret
	 * @param listener
	 * @throws UnsupportedEncodingException
	 */
	public void refreshToken(String clientId, String clientSecret, RequestListener listener) throws UnsupportedEncodingException {
		String refreshTokenUrl = "https://auth.kanbox.com/0/token";
		
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("grant_type", "refresh_token");
		params.put("client_id", clientId);
		params.put("client_secret", clientSecret);
		params.put("refresh_token", refreshToken);
		
		HttpRequestBase httpMethod = KanboxHttp.doPost(refreshTokenUrl, params);
		new KanboxAsyncTask(null, httpMethod, listener, RequestListener.OP_REFRESH_TOKEN).execute();
	}
	

	public String getAcceccToken() {
		return acceccToken;
	}

	public void setAcceccToken(String acceccToken) {
		this.acceccToken = acceccToken;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	public long getExpires() {
		return expires;
	}

	public void setExpires(long expires) {
		this.expires = expires;
	}
	
	public String getSaveString() {
		return saveString;
	}

	public void setSaveString(String saveString) {
		this.saveString = saveString;
	}

	public boolean hasOauth(){
		
//		Log.e("check", "acceccToken:" + getAcceccToken() + " isnull:" + (getAcceccToken() == null));
//		Log.e("check", "refreshToken:" + getRefreshToken() + " isnull:" + (getRefreshToken() == null));
//		Log.e("check", "expires:" + getExpires() + " isZero:" + (getExpires() == 0));
		
		return !((getAcceccToken() == null) || (getRefreshToken() == null) || (getExpires() == 0));
	}
	
	public boolean needRefresh(){
		boolean flag = false;
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh-mm-ss");
		
		try {
			String now = getCurrentTime();
			Date saveDate = sdf.parse(getSaveString());
			Date nowDate = sdf.parse(now);
			
			Log.e("check", "needRefresh:\n" +
					"nowDate" + now + "\n" +
							"saveDate:" +getSaveString() + "\n" +
								"now time:" + nowDate.getTime() +"\n" +
									"save time:" + saveDate.getTime() + "\n" +
										"now - save:" + (nowDate.getTime() - saveDate.getTime()) + "\n" +
											"Expires:" + getExpires());
			
			flag = ((nowDate.getTime() - saveDate.getTime()<0) || ((nowDate.getTime() - saveDate.getTime()) > getExpires() * 1000));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return flag;
	}
	
	public static String getCurrentTime(){
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
}
