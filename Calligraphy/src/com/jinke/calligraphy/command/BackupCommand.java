package com.jinke.calligraphy.command;

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
import com.jinke.calligraphy.backup.CalligraphyBackupUtil;
import com.jinke.calligraphy.ftp.FTPUtil;
import com.jinke.kanbox.DownloadAllFileThread;
import com.jinke.kanbox.KanboxException;
import com.jinke.kanbox.PushSharePreference;
import com.jinke.kanbox.RequestListener;
import com.jinke.kanbox.Token;
import com.jinke.kanbox.UploadAllFileThread;
import com.jinke.kanbox.WeiboWeb;

public class BackupCommand implements Command,RequestListener{

	Handler handler;
	Context context;
	BackupThread backupThread;
	int option = -1;
	String dirName = "";
	
	public BackupCommand(Context context,Handler handler){
		this.handler = handler;
		this.context = context;
		backupThread = new BackupThread();
	}
	public BackupCommand(Context context,Handler handler , int option , String dirName){
		this(context , handler);
		this.option = option;
		this.dirName = dirName;
	}
	
	@Override
	public void execute() {
		// TODO Auto-generated method stub
		//测试kanbox下载恢复，这里先住掉
//		backupThread.start();
		
		//-----------------kanbox-----------------
		//检测有没有授权
		if(checkIsOathur()){
			//已授权
			//检测当前授权码有没有过期
			if(checkNeedRefresh()){
				//需要refresh
				refreshAccessToken();
					//refresh成功后启动上传
			}else{
				//没过期直接下载
				(new DownloadAllFileThread(option,dirName)).start();
				
			}
		}else
			//没有授权
			getOauth();//跳转到webview，正常状态：获得token，保存本地，开启线程下载，退出Webiew返回。
	}

	/**
	 * 请求用户授权
	 */
	private void getOauth() {
		String getCodeUrl = "https://auth.kanbox.com/0/auth?response_type=code&client_id=" + Start.CLIENT_ID + "&platform=android" + "&redirect_uri=" + Start.REDIRECT_URI + "&user_language=ZH";
		Intent sIntent = new Intent(Start.context, WeiboWeb.class);
		sIntent.putExtra("url", getCodeUrl);
		sIntent.putExtra("todo", "download");
		Start.barText.setText(Start.barText.getText() + "\n" + "进入授权页面");
		Start.context.startActivity(sIntent);
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
		DownloadProgressActivity.barTextHandler.sendMessage(msg);
		Log.e("Start", "send msg is oathur");
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
		sToken.setSaveString(sPreference.getStringValueByKey("save_time"));
	}
	public void saveToken(Token token) {
		PushSharePreference sPreference = new PushSharePreference(Start.context, "oauth");
		sPreference.saveStringValueToSharePreferences("accecc_token", token.getAcceccToken());
		sPreference.saveStringValueToSharePreferences("refresh_token", token.getRefreshToken());
		sPreference.saveLongValueToSharePreferences("expries", token.getExpires());
		sPreference.saveStringValueToSharePreferences("save_time", Token.getCurrentTime());
		
	}
	@Override
	public void undo(Bitmap b) {
		// TODO Auto-generated method stub
		
	}
	
	class BackupThread extends Thread{
		@Override
		public void run() {
			// TODO Auto-generated method stub
			
			CalligraphyBackupUtil backupUtil = new CalligraphyBackupUtil(context);
			
			Log.e("ftp", "download start");
			
			
//			FTPUtil.downloadLocalCalldir();
			
			FTPUtil f = new FTPUtil();
			f.downloadUserLocalCalldir();
			
			
			Log.e("ftp", "download over");
			if(backupUtil.getAllCalligraphyList()){
				//获得列表,更新数据库成功
				handler.sendEmptyMessage(0);
			}else{
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
				
//				Start.barText.setText(Start.barText.getText() + "\n\n" + "刷新token成功，开始下载");
				
				Message msg = new Message();
				msg.what = DownloadProgressActivity.KANBOX_FINISH_REFRESHTOKEN;
				msg.obj = "刷新token成功，开始获取备份列表";
				DownloadProgressActivity.barTextHandler.sendMessage(msg);
				
				//刷新成功，开始下载
				(new DownloadAllFileThread(option,dirName)).start();
				
			} catch (JSONException e) {
				e.printStackTrace();
				Toast.makeText(Start.context, "刷新token失败\n\n" + response, Toast.LENGTH_LONG).show();
			}
			return;
		
		}
		
	}

	@Override
	public void onError(KanboxException error, int operationType) {
		// TODO Auto-generated method stub
		
		Start.barText.setText(Start.barText.getText() + "\n\n" + "刷新token失败，开始下载");
		
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
