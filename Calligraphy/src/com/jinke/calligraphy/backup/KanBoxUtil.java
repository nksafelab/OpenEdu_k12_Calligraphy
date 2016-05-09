package com.jinke.calligraphy.backup;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import android.util.Log;

public class KanBoxUtil {
	
	private String code = "";
	
	private String client_serc = "adf5b6555197ee52d8dbbd7ec1cb3fb9";
	private String client_id = "45fd6312c0c847d62017e483f05f5f50";
	
	public KanBoxUtil(String code){
		this.code = code;
	}
	
	public String getJsonFromHttps(String urlString) {
		String rc = "";
		try {
			HttpURLConnection http = null;
			URL url = new URL(urlString);
			if (url.getProtocol().toLowerCase().equals("https")) {
				Log.e("content", "https");
				trustAllHosts();
				Log.e("content", "https	trustAllHosts");
				HttpsURLConnection https = (HttpsURLConnection) url
						.openConnection();
				Log.e("content", "https	trustAllHosts openConnection");
				https.setHostnameVerifier(DO_NOT_VERIFY);
				Log.e("content",
						"ssshttps	trustAllHosts openConnection setHostnameVerifier");
				http = https;
			} else {
				http = (HttpURLConnection) url.openConnection();
			}

			String query = "grant_type=authorization_code" + "&client_id="
					+ client_id + "&client_secret=" + client_serc + "&code="
					+ code + "&redirect_uri=" + "Cloud Note"; 
			byte[] entitydata = query.getBytes();

			http.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");

			((HttpsURLConnection) http).setRequestMethod("POST");
			http.setReadTimeout(30000);
			http.setDoOutput(true);
			http.setDoInput(true);
			http.connect();

			Log.e("content",
					"https	trustAllHosts openConnection setHostnameVerifier  connect()");
			OutputStream outStream = http.getOutputStream();
			outStream.write(entitydata);
			Log.e("content",
					"https	trustAllHosts openConnection setHostnameVerifier  connect() outStream.write");
			outStream.flush();
			Log.e("content", "https getInputStream flush");
			outStream.close();

			BufferedReader in = new BufferedReader(new InputStreamReader(
					http.getInputStream()));

			Log.e("content", "https getInputStream");

			String line;
			StringBuffer sb = new StringBuffer();
			while ((line = in.readLine()) != null) {
				sb.append(line);
				Log.e("content", "https getInputStream readLine:" + line);
			}

			rc = sb.toString();

			Log.e("content", "result:" + rc);

		} catch (Exception e) {
			Log.e("content", "exception:", e);
		}
		
		int startToken = rc.indexOf("access_token") + 15;
		int endToken = startToken + 32;
		rc = rc.substring(startToken, endToken);

		
		return rc;
	}
	
	final static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
		public boolean verify(String hostname, SSLSession session) {
			return true;
		}
	};

	/**
	 * Trust every server - dont check for any certificate
	 */
	private static void trustAllHosts() {
		// Create a trust manager that does not validate certificate chains
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return new java.security.cert.X509Certificate[] {};
			}

			@Override
			public void checkClientTrusted(X509Certificate[] chain,
					String authType) throws CertificateException {
				// TODO Auto-generated method stub

			}

			@Override
			public void checkServerTrusted(X509Certificate[] chain,
					String authType) throws CertificateException {
				// TODO Auto-generated method stub

			}
		} };

		// Install the all-trusting trust manager
		try {
			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection
					.setDefaultSSLSocketFactory(sc.getSocketFactory());

			HttpsURLConnection.setDefaultHostnameVerifier(DO_NOT_VERIFY);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
