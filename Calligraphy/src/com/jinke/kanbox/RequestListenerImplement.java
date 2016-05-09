package com.jinke.kanbox;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import com.jinke.calligraphy.activity.DownloadProgressActivity;
import com.jinke.calligraphy.app.branch.Start;
import com.jinke.calligraphy.database.CDBPersistent;
import com.jinke.calligraphy.template.ZipUtils;

import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class RequestListenerImplement implements RequestListener{

	private int totalPage = 0;
	private int finished = 0;
	private int errored = 0;
	private boolean flag;
	private Message msg;
	public RequestListenerImplement(int pagenum){
		totalPage = pagenum;
		flag = false;
	}
	
	@Override
	public void onComplete(String response, int operationType) {
		switch (operationType) {
		case OP_REFRESH_TOKEN:
			try {
				Token.getInstance().parseToken(response);
				Toast.makeText(Start.context, "刷新token成功\n\n" + response, Toast.LENGTH_LONG).show();
				saveToken(Token.getInstance());
			} catch (JSONException e) {
				e.printStackTrace();
				Toast.makeText(Start.context, "刷新token失败\n\n" + response, Toast.LENGTH_LONG).show();
			}
			break;
		case OP_UPLOAD:
			
				finished ++;
//			Toast.makeText(Start.context, "上传" + finished +"/" + totalPage, Toast.LENGTH_LONG).show();
			Log.e("uploadfinish", "response:" + response);
			//response:1.../jinke007/page1.zip
			String lPath = response.substring(response.lastIndexOf("/"),response.length());
			String uploadedPath =Start.getStoragePath() +  "/calldir"+response.substring(response.lastIndexOf("/"),response.length());
			File uploadedFile = new File(uploadedPath);
			if(uploadedFile.exists())
				uploadedFile.delete();
			Log.e("uploadfinish", "uploadedPath:" + uploadedPath);
			
			String pNumString = lPath.replace("/page", "第 ");
			pNumString = lPath.replace(".zip", " 页");
			double upprogress = ((double)finished/totalPage) * 100;
			
			NumberFormat   format   =   NumberFormat.getNumberInstance();
            format.setMaximumFractionDigits(2);
            
			
			msg = new Message();
			msg.what = DownloadProgressActivity.KANBOX_END_UPLOAD_PAGE;
			msg.obj = pNumString + "上传成功" + "\n" +
					"总体进度：" + finished +"/" + totalPage  + " = " + format.format(upprogress) + " %";
			msg.arg1 = (int)upprogress;
//			Start.barTextHandler.sendMessage(msg);
			DownloadProgressActivity.barTextHandler.sendMessage(msg);
			
			if(errored > 0){
				if(finished + errored == totalPage){
					msg = new Message();
					msg.what = DownloadProgressActivity.KANBOX_ERROR_DOWNLOAD;
					msg.obj = "传输过程中存在错误：";
//					Start.barTextHandler.sendMessage(msg);
					DownloadProgressActivity.barTextHandler.sendMessage(msg);
				}
			}
			
			
			if(finished == totalPage){
				CDBPersistent db = new CDBPersistent(Start.context);
				db.open();
				db.uploadedSuccess();
//				Toast.makeText(Start.context, "上传完成，共 "+ totalPage +"份", Toast.LENGTH_LONG).show();
				
				Start.clearDownloadList();
				
				msg = new Message();
				msg.what = DownloadProgressActivity.KANBOX_FINISH_UPLOAD;
				msg.obj = pNumString + "上传全部完成";
//				Start.barTextHandler.sendMessage(msg);
				DownloadProgressActivity.barTextHandler.sendMessage(msg);
				
				Log.e("uploadfinish", "uploaded success:");
				db.close();
			}
			
			
			
			break;
		case OP_DOWNLOAD:
//			Toast.makeText(Start.context, "下载成功\n\n" + response, Toast.LENGTH_LONG).show();
			Log.e("download", "response:" + response + " response.substring(2):" + response.substring(0,2));
			//response:ok/mnt/extsd/calldir/page1.zip
			
			if("ok".equals(response.substring(0,2))){
				String localPath = response.substring(2,response.length());
				Log.e("download", "localPath:" + localPath);
				if(localPath.contains(".db")){
					//数据库文件
					File dbFile = new File(localPath);
					if(dbFile.exists()){
						msg = new Message();
						msg.what = Start.KANBOX_END_DBDOWNLOAD;
						msg.obj = "数据库文件下载完成";
//						Start.barTextHandler.sendMessage(msg);
						DownloadProgressActivity.barTextHandler.sendMessage(msg);
						
						finished ++;
						try {
							InputStream in = new FileInputStream(dbFile);
							File outFile = new File("/data/data/com.jinke.calligraphy.app.branch/databases/calligraphy.db");
							if(outFile.exists())
								outFile.delete();
							OutputStream out = new FileOutputStream(new File("/data/data/com.jinke.calligraphy.app.branch/databases/calligraphy.db"));
							
							byte[] buf = new byte[128];
							int count = 0;
							while((count = in.read(buf)) != -1){
								out.write(buf, 0, count);
							}
							in.close();
							out.flush();
							out.close();
							dbFile.delete();
						} catch (FileNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}	
					}
				}else if(!response.contains(".zip")){
					
				}else{
					//一般文件
					//解压本地文件
					int start_ = response.lastIndexOf("/");
					int end_ = response.indexOf(".zip");
					String dirN = response.substring(start_, end_);
					dirN = dirN.replace("page", "free_");
					
					msg = new Message();
					msg.what = Start.KANBOX_END_DBDOWNLOAD;
					msg.obj = "第"+ dirN.replace("page", "") + "页文件下载完成";
//					Start.barTextHandler.sendMessage(msg);
					DownloadProgressActivity.barTextHandler.sendMessage(msg);
					finished ++;
					//dirN:/page1
//					Log.e("download", "localPath:" + localPath);
					try {
						Log.e("download", "localPath:" + localPath);
						ZipUtils.Ectract(localPath, Start.getStoragePath() + "/calldir" + dirN+"/");
					} catch (Exception e) {
						Log.e("download", "zip exception", e);
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				
				File f = new File(localPath);
				if(f.exists()){
					f.delete();
				}
				
				msg = new Message();
				msg.what = Start.KANBOX_END_DBDOWNLOAD;
				double progress = ((double)finished/totalPage) * 100;
				format   =   NumberFormat.getNumberInstance();
	            format.setMaximumFractionDigits(2);
				msg.obj = "总体下载进度:" + format.format(progress) + " %";
				msg.arg1 = (int)progress;
//				Start.barTextHandler.sendMessage(msg);
				DownloadProgressActivity.barTextHandler.sendMessage(msg);
				
				
				if(errored > 0){
					if(finished + errored == totalPage){
						msg = new Message();
						msg.what = DownloadProgressActivity.KANBOX_ERROR_DOWNLOAD;
						msg.obj = "下载过程中存在错误：";
//						Start.barTextHandler.sendMessage(msg);
						DownloadProgressActivity.barTextHandler.sendMessage(msg);
					}
				}
				
				if(progress == 100){
					Start.clearDownloadList();
					msg = new Message();
					msg.what = DownloadProgressActivity.KANBOX_FINISH_DOWNLOAD;
					msg.obj = "下载完成";
//					Start.barTextHandler.sendMessage(msg);
					DownloadProgressActivity.barTextHandler.sendMessage(msg);
				}
				
			}else{
				break;
			}
				
//			Start.backupHandler.sendEmptyMessage(0);//好像不用了 住掉试试
			
			break;
		case OP_MAKE_DIR:
//			Toast.makeText(Start.context, "创建文件夹\n\n" + response, Toast.LENGTH_LONG).show();
			break;
		default:
			handleResponse(response);
			break;
		}
	}

	@Override
	public void onError(KanboxException error, int operationType) {
//		Toast.makeText(Start.context, "操作失败" + error.getStatusCode() + "\n\n" + error.toString(), Toast.LENGTH_LONG).show();
	}

	private void handleResponse(String response) {
		try {
			JSONObject sData = new JSONObject(response);
			String status = sData.getString("status");
			if(status.equals("ok")) {
//				Toast.makeText(Start.context, "操作成功\n\n" + response, Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(Start.context, "操作失败\n\n" + response, Toast.LENGTH_LONG).show();
			}
		} catch (JSONException e) {
			Toast.makeText(Start.context, "操作失败\n\n" + e.toString(), Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void downloadProgress(long currSize) {
//		Toast.makeText(Start.context, "已传输：" + currSize + " %", Toast.LENGTH_SHORT).show();
	}

	public void saveToken(Token token) {
		PushSharePreference sPreference = new PushSharePreference(Start.context, "oauth");
		sPreference.saveStringValueToSharePreferences("accecc_token", token.getAcceccToken());
		sPreference.saveStringValueToSharePreferences("refresh_token", token.getRefreshToken());
		sPreference.saveLongValueToSharePreferences("expries", token.getExpires());
	}

	@Override
	public void onError(KanboxException error, int operationType, String path,
			String destPath) {
		// TODO Auto-generated method stub
		
		Start.addDownloadEnty(new DownloadEntity(path, destPath));
		errored ++;
		if(finished + errored == totalPage){
			msg = new Message();
			if(operationType == OP_DOWNLOAD)
				msg.what = DownloadProgressActivity.KANBOX_ERROR_DOWNLOAD;
			else if(operationType == OP_UPLOAD)
				msg.what = DownloadProgressActivity.KANBOX_ERROR_UPLOAD;
			msg.obj = "传输过程中存在错误：";
//			Start.barTextHandler.sendMessage(msg);
			DownloadProgressActivity.barTextHandler.sendMessage(msg);
		}
		
	}


}
