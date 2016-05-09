package com.jinke.kanbox;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.jinke.calligraphy.activity.Cloud;
import com.jinke.calligraphy.activity.DownloadProgressActivity;
import com.jinke.calligraphy.app.branch.Start;
import com.jinke.calligraphy.template.ZipUtils;

public class DownloadAllFileThread extends Thread implements RequestListener{

	public static final int OP_GETLIST = 1;
	public static final int OP_DOWNLOAD = 2;
	public static final int OP_GETTHUMBLIST = 3;
	private int option = -1;
	private String dstDirName = "";
	
	String zipPath = "";
	
	public DownloadAllFileThread(int op,String dirName){
		zipPath = Start.getStoragePath() + "/" + Start.username + ".zip";
		this.option = op;
		this.dstDirName = dirName;
		Log.e("Start", "---------------------------------------DownloadAll" + dirName + " type:" + op);
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		Looper.prepare();
		if(Start.getDownloadList().size() != 0){
			RequestListener listener = new RequestListenerImplement(Start.getDownloadList().size());
			String path;
			String dstPath;
			for(DownloadEntity enty : Start.getDownloadList()){
				path = enty.getPath();
				dstPath = enty.getDestPath();
				Kanbox.getInstance().download(
						path, 
						dstPath, 
						Token.getInstance(), 
						listener);	
			}
			
			Start.clearDownloadList();
			
		}else{
//			getFileList("/" + Start.username + "/calligraphy");
			if(option == OP_GETLIST)
				getFileList("/" + Start.username);
			else if(option == OP_GETTHUMBLIST)
				getFileList("/" + Start.username + "/" + dstDirName);
			else if(option == OP_DOWNLOAD){
				
				Message msg = new Message();
				msg.what = Start.KANBOX_START_DOWNLOAD;
				msg.obj = "开始备份本地数据";
//				Start.barTextHandler.sendMessage(msg);
				
				DownloadProgressActivity.barTextHandler.sendMessage(msg);
				
				(new LocalCopyThread()).run();
				
				
				msg = new Message();
				msg.what = Start.KANBOX_START_DOWNLOAD;
				msg.obj = "备份本地数据完成，开始下载网络数据";
//				Start.barTextHandler.sendMessage(msg);
				DownloadProgressActivity.barTextHandler.sendMessage(msg);
				
				Log.e("dir", "path:" + "/" + Start.username + "/" + dstDirName);
				if(dstDirName.contains("_local")){
					//复制本地备份文件
					copyBackupLocalFile(dstDirName);
				}else
					//获取网络备份
					getFileList("/" + Start.username + "/" + dstDirName);
			}
				
		}
		//获得列表之后，挨个下载，在onComplete里执行
//		downloadFile();
		Looper.loop();
		
	}
	
	/**
	 * 获取文件列表
	 * 假设路径为“/照片”
	 */
	private void getFileList(String dstDir) {
//		Kanbox.getInstance().getFileList(Token.getInstance(), "/照片", this);
		Kanbox.getInstance().getFileList(Token.getInstance(), dstDir, this);
	}
	
	/**
	 * 下载文件,never used
	 */
	private void downloadFile() {
//		Kanbox.getInstance().download("/kanbox.png", "/sdcard/download.png", Token.getInstance(), new RequestListenerImplement());
		Log.e("download", "name:" + Start.getStoragePath() + "/" + Start.username + ".zip");
		Kanbox.getInstance().download(
				"/" + Start.username + ".zip", 
				Start.getStoragePath() + "/" + Start.username + ".zip", 
				Token.getInstance(), 
				new RequestListenerImplement(0));
	}
	@Override
	public void onComplete(String response, int operationType) {
		// TODO Auto-generated method stub
		switch (operationType) {
		case OP_GET_FILELIST:
			
			try {
				JSONObject listObj = new JSONObject(response);
				String status = listObj.getString("status");
				
				
				
				
				if("ok".equals(status)){
//					Toast.makeText(Start.context, "获得文件列表成功\n" + response, Toast.LENGTH_LONG).show();
					
					Start.barTextHandler.sendEmptyMessage(Start.KANBOX_GET_FILELIST);
					DownloadProgressActivity.barTextHandler.sendEmptyMessage(DownloadProgressActivity.KANBOX_GET_FILELIST);
					
					JSONArray ja = listObj.getJSONArray("contents");
//					RequestListener listener = new RequestListenerImplement(ja.length());
					RequestListener listener = null;
					int count = 0;
					for(int i=0;i<ja.length();i++){
						JSONObject jo = ja.getJSONObject(i);
						String dstPath = jo.getString("fullPath");
						if(!dstPath.contains("index_"))
							count++;
					}
					listener = new RequestListenerImplement(count);
					
					List folderList = new ArrayList<String>();
					ArrayList<String> thumbList = new ArrayList<String>();
					for(int i=0;i<ja.length();i++){
						JSONObject jo = ja.getJSONObject(i);
						String dstPath = jo.getString("fullPath");
						String fileName = dstPath.substring(dstPath.lastIndexOf("/")+1,dstPath.length());
						
						boolean isFolder = jo.getBoolean("isFolder");
						if(isFolder && fileName.contains("calligraphy")){
							folderList.add(fileName);
						}
						
						thumbList.add(dstPath);
						
						System.out.println("fullPath:" + dstPath + " fileName:" + fileName + " isFolder:" + isFolder);
						
//						if(option == OP_DOWNLOAD){
						if(option == OP_DOWNLOAD && !dstPath.contains("index_")){
							
							Message msg = new Message();
							msg.what = Start.KANBOX_START_DOWNLOAD;
							msg.obj = fileName + "开始下载";
//							Start.barTextHandler.sendMessage(msg);
							DownloadProgressActivity.barTextHandler.sendMessage(msg);
							
							
								Kanbox.getInstance().download(
									dstPath, 
									Start.getStoragePath() + "/calldir/" + fileName, 
									Token.getInstance(), 
									listener);
						}
					}
					if(option == OP_GETTHUMBLIST){
						Message msg = new Message();
						msg.obj = thumbList;
						msg.what = Cloud.SHOW_THUMB_GRIDVIEW;
						Cloud.handler.sendMessage(msg);
					}
						
					if(option == OP_GETLIST){
						//添加本地列表
						
						File localDir = new File(Start.getStoragePath() 
								+ "/callbackup/");
						if(localDir.exists()){
							String[] s = localDir.list();
							for(String t:s)
								folderList.add(t);
						}
						Message msg = new Message();
						msg.obj = folderList;
						Start.fileListHandler.sendMessage(msg);
					}
				}
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			/*
			 {"status":"ok",
			 "hash":1331111420,
			 "contents":
			 [{
			 "fullPath":"\/jinke007\/page4.zip",
			 "modificationDate":"2012-03-07T09:10:20+00:00",
			 "fileSize":23790,"isFolder":false,
			 "shareId":0,"isShared":false},
			 {"fullPath":"\/jinke007\/calligraphy.db",
			 "modificationDate":"2012-03-07T09:10:17+00:00",
			 "fileSize":20480,
			 "isFolder":false,
			 "shareId":0,
			 "isShared":false},
			 {
			 "fullPath":"\/jinke007\/page1.zip",
			 "modificationDate":"2012-03-07T08:32:58+00:00",
			 "fileSize":22391,
			 "isFolder":false,
			 "shareId":0,
			 "isShared":false},
			 {"fullPath":"\/jinke007\/page3.zip",
			 "modificationDate":"2012-03-07T08:31:43+00:00",
			 "fileSize":18409,
			 "isFolder":false,
			 "shareId":0,
			 "isShared":false},
			 {
			 "fullPath":"\/jinke007\/page2.zip",
			 "modificationDate":"2012-03-07T08:31:40+00:00",
			 "fileSize":18409,
			 "isFolder":false,
			 "shareId":0,
			 "isShared":false}]}..!.null

			 */
			
			break;

		default:
			break;
		}
	}
	@Override
	public void onError(KanboxException error, int operationType) {
		// TODO Auto-generated method stub
		switch (operationType) {
		case OP_GET_FILELIST:
//			Toast.makeText(Start.context, "获取文件列表失败，请重试\n\n" + error.getStatusCode(), Toast.LENGTH_LONG).show();
			
			Message msg = new Message();
			msg.what = Start.KANBOX_START_DOWNLOAD;
			msg.obj = "获取文件列表失败，请重新尝试恢复";
//			Start.barTextHandler.sendMessage(msg);
			
			DownloadProgressActivity.barTextHandler.sendMessage(msg);
			
			break;

		default:
			break;
		}
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
	
	public void copyBackupLocalFile(String localPath){
		localPath = Start.getStoragePath() + "/callbackup/" + localPath;
		File localDir = new File(localPath);
		if(localDir.exists()){
			String[] files = localDir.list();
			for(String f : files){
				Log.e("dir", "----->>>unzip:" + f);
				
				if(f.contains(".zip")){
					String index = f.substring(f.indexOf("page") + "page".length()
							, f.indexOf(".zip"));
					int pagenum = Integer.parseInt(index);
					
					Log.e("dir", "----->>>start unzip:" + f);
					Log.e("dir", "----->>>>>>>>>>>>>>from:" + localPath + "/" + f);
					Log.e("dir", "----->>>>>>>>>>>>>>to:" + Start.getStoragePath() + "/calldir/free_" + pagenum + "/");
					try {
						ZipUtils.Ectract(localPath + "/" + f, Start.getStoragePath() + "/calldir/free_" + pagenum + "/");
						Log.e("dir", "----->>>end unzip:" + f);
					} catch (Exception e) {
						Log.e("dir", "zip exception in download local", e);
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}//end if
				else if(f.contains(".db")){
					Log.e("dir", "start copy db");
					LocalCopyThread.copy(localPath + "/" + f
							, "/data/data/com.jinke.calligraphy.app.branch/databases/calligraphy.db");
					Log.e("dir", "end copy db");
				}
			}//end for
			Message msg = new Message();
			msg.what = Start.KANBOX_FINISH_DOWNLOAD;
			Start.barTextHandler.sendMessage(msg);
		}
	}
	
}
