package com.jinke.calligraphy.command;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.json.JSONException;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.jinke.calligraphy.activity.DownloadProgressActivity;
import com.jinke.calligraphy.app.branch.Command;
import com.jinke.calligraphy.app.branch.Start;
import com.jinke.calligraphy.backup.UploadToServer;
import com.jinke.kanbox.DownloadAllFileThread;
import com.jinke.kanbox.KanboxException;
import com.jinke.kanbox.PushSharePreference;
import com.jinke.kanbox.RequestListener;
import com.jinke.kanbox.Token;
import com.jinke.kanbox.UploadAllFileThread;
import com.jinke.kanbox.WeiboWeb;


public class UploadCommand implements Command , RequestListener{
	
	UploadThread upThread;
	Context context;
	Handler handler;
	boolean uploadMethod = false;
	public UploadCommand(Context context,Handler handler,boolean uploadMethod){
		this.context = context;
		this.handler = handler;
		this.uploadMethod = uploadMethod;
		upThread = new UploadThread();
	}
	
	
	@Override
	public void execute() {
		// TODO Auto-generated method stub
		 
		//使用库盘上传，暂且住掉
//		upThread.start();
		
		
		//loadToken(); 先载入Token  包括access-token  和 refresh-token
		//判断有没有load成功，没有成功就先运行author
		
		//检测有没有授权
		Log.e("Start", "start upload");
		if(checkIsOathur()){
			
			//已授权
			//检测当前授权码有没有过期
			if(checkNeedRefresh()){
				//需要refresh
				refreshAccessToken();
					//refresh成功后启动上传
			}else{
				//没过期直接上传
				Log.e("check", "no refresh upload");
				(new UploadAllFileThread(uploadMethod)).start();
			}
		}else
			//没有授权
			getOauth();//跳转到webview，正常状态：获得token，保存本地，开启线程下载，退出Webiew返回。	
	}
	
	/**
	 * 是否已经授权
	 */
	private boolean checkIsOathur(){
		loadToken();
		boolean flag = Token.getInstance().hasOauth();
//		Toast.makeText(Start.context, "oauth:" + flag, Toast.LENGTH_LONG).show();
		Start.barText.setText(Start.barText.getText() + "\n" + (flag ? "经检测，已经通过酷盘授权":"进入酷盘授权页面"));
		Message msg = new Message();
		msg.what = DownloadProgressActivity.KANBOX_CHECK_AUTHOR;
		msg.obj = flag ? "经检测，已经通过酷盘授权":"进入酷盘授权页面";
		Log.e("Start", "send upload msg");
		DownloadProgressActivity.barTextHandler.sendMessage(msg);
		
		return flag;
	}
	/**
	 * 是否需要重新获取token,(已授权的前提下)
	 */
	private boolean checkNeedRefresh(){
		loadToken();
		boolean flag = Token.getInstance().needRefresh();
//		Toast.makeText(Start.context, "refresh:" + flag, Toast.LENGTH_LONG).show();
		Start.barText.setText(Start.barText.getText() + "\n" + (flag ? "token已经过期，从服务器重新获取":"token可以使用"));
		
		Message msg = new Message();
		msg.what = DownloadProgressActivity.KANBOX_CHECK_TOKEN;
		msg.obj = flag ? "token已经过期，从服务器重新获取":"token可以使用";
		DownloadProgressActivity.barTextHandler.sendMessage(msg);
		
		return flag;
	}
	/**
	 * 刷新access_token
	 */
	private void refreshAccessToken() {
		try {
			Token.getInstance().refreshToken(Start.CLIENT_ID, Start.CLIENT_SECRET, this);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			Toast.makeText(Start.context, "操作失败\n\n" + e.toString(), Toast.LENGTH_LONG).show();
			
			Message msg = new Message();
			msg.what = DownloadProgressActivity.KANBOX_ERROR_REFRESHTOKEN;
			msg.obj = "刷新token出现异常，请退出重试";
			DownloadProgressActivity.barTextHandler.sendMessage(msg);
			
		}
	}
	public void loadToken() {
		PushSharePreference sPreference = new PushSharePreference(Start.context, "oauth");
		Token sToken = Token.getInstance();
		sToken.setAcceccToken(sPreference.getStringValueByKey("accecc_token"));
		sToken.setRefreshToken(sPreference.getStringValueByKey("refresh_token"));
		sToken.setExpires(sPreference.getLongValueByKey("expries"));
		Log.e("", "");
		sToken.setSaveString(sPreference.getStringValueByKey("save_time"));
	}
	public void saveToken(Token token) {
		PushSharePreference sPreference = new PushSharePreference(Start.context, "oauth");
		sPreference.saveStringValueToSharePreferences("accecc_token", token.getAcceccToken());
		sPreference.saveStringValueToSharePreferences("refresh_token", token.getRefreshToken());
		sPreference.saveLongValueToSharePreferences("expries", token.getExpires());
		sPreference.saveStringValueToSharePreferences("save_time", Token.getCurrentTime());
		
	}
	
	/**
	 * 请求用户授权
	 */
	private void getOauth() {
		String getCodeUrl = "https://auth.kanbox.com/0/auth?response_type=code&client_id=" + Start.CLIENT_ID + "&platform=android" + "&redirect_uri=" + Start.REDIRECT_URI + "&user_language=ZH";
		Intent sIntent = new Intent(Start.context, WeiboWeb.class);
		sIntent.putExtra("url", getCodeUrl);
		sIntent.putExtra("todo", "update");
		
		Start.barText.setText(Start.barText.getText() + "\n" + "进入授权页面");
		
		Start.context.startActivity(sIntent);
	}
	

	@Override
	public void undo(Bitmap b) {
		// TODO Auto-generated method stub
		
	}
	
	class UploadThread extends Thread{
		@Override
		public void run() {
			// TODO Auto-generated method stub
			
			new FtpCommand().execute();
			
			Log.e("upload", "ftp finish!!!!!!!!!!!!!!!!!!!!!!");
			
			UploadToServer upload = new UploadToServer(context);
			try {
				upload.upload();
				if(handler != null)
					handler.sendEmptyMessage(0);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				if(handler != null)
					handler.sendEmptyMessage(-1);
			}
			
			
		}
	}

	@Override
	public void onComplete(String response, int operationType) {
		// TODO Auto-generated method stub
		switch (operationType) {
		case OP_REFRESH_TOKEN:
			try {
				Token.getInstance().parseToken(response);
//				Toast.makeText(Start.context, "刷新token成功\n\n" + response, Toast.LENGTH_LONG).show();
				saveToken(Token.getInstance());
				
				Start.barText.setText(Start.barText.getText() + "\n" + "刷新token成功，开始上传");
				
				Message msg = new Message();
				msg.what = DownloadProgressActivity.KANBOX_FINISH_REFRESHTOKEN;
				msg.obj = "刷新token成功，开始上传";
				DownloadProgressActivity.barTextHandler.sendMessage(msg);
				
				Log.e("check", "after refresh upload");
				//刷新成功，开始上传
				(new UploadAllFileThread(uploadMethod)).start();
				
			} catch (JSONException e) {
				e.printStackTrace();
				Toast.makeText(Start.context, "刷新token失败\n\n" + response, Toast.LENGTH_LONG).show();
			}
			return;
		case OP_UPLOAD:
			Toast.makeText(Start.context, "上传成功\n\n" + response, Toast.LENGTH_LONG).show();
			return;
		case OP_DOWNLOAD:
			Toast.makeText(Start.context, "下载成功\n\n" + response, Toast.LENGTH_LONG).show();
			return;
		default:
//			handleResponse(response);
			return;
		}
	}

	@Override
	public void onError(KanboxException error, int operationType) {
		// TODO Auto-generated method stub
		Start.barText.setText(Start.barText.getText() + "\n" + "刷新token失败，重试网络操作");
		Message msg = new Message();
		msg.what = DownloadProgressActivity.KANBOX_ERROR_REFRESHTOKEN;
		msg.obj = "刷新token出现异常，请退出重试";
		DownloadProgressActivity.barTextHandler.sendMessage(msg);
	}

	@Override
	public void downloadProgress(long currSize) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onError(KanboxException error, int operationType, String path,
			String destPath) {
		// TODO Auto-generated method stub
		
	}

}
