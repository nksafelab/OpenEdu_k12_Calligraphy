package com.jinke.kanbox;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import com.jinke.calligraphy.activity.DownloadProgressActivity;
import com.jinke.calligraphy.app.branch.Start;
import com.jinke.calligraphy.database.CDBPersistent;

import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class UploadAllFileThread extends Thread implements RequestListener{

	String zipPath = "";
	List<Integer> pageList;
	String dstName;
	boolean iscopy ;
	
	public UploadAllFileThread(boolean iscoyp){
		zipPath = Start.getStoragePath() + "/" + Start.username + ".zip";
		this.iscopy = iscoyp;
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		
		Looper.prepare();
		
		CDBPersistent db = new CDBPersistent(Start.context);
		db.open();
		pageList = db.getUploadPage();
//		Log.e("update", "----------------->>>>>>>>>>>>>>>>>>>>>>>> getPageList:" + pageList.size());
		
		
		if(Start.getDownloadList().size() != 0){
			Log.e("update", "----------------->>>>>>>>>>>>>>>>>>>>>>>> getDownloadList:" + Start.getDownloadList().size());
			RequestListener listener = new RequestListenerImplement(Start.getDownloadList().size());
			String path;
			String dstPath;
			
//			for(DownloadEntity enty : Start.getDownloadList()){
			DownloadEntity enty;
			for(int i=0;i<Start.getDownloadList().size();i++){
				enty = Start.getDownloadList().get(i);
				path = enty.getPath();
				Log.e("update", "----------------->>>>>>>>>>>>>>>>>>>>>>>> getDownloadList:" + path);
				dstPath = enty.getDestPath();
				try {
					Kanbox.getInstance().upload(path, dstPath , Token.getInstance(),listener);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			Start.clearDownloadList();
			
		}else{
			//查看需要上传的页码
			
			if(pageList != null){
				for(int i : pageList)
					Log.e("uploadpage", "page:" + i);
				
	//			db.uploadedSuccess();
				
	
				Message msg = new Message();
				msg.what = Start.KANBOX_START_UPLOAD;
				msg.obj = "共" + pageList.size()+ "页需要上传";
//				Start.barTextHandler.sendMessage(msg);
				DownloadProgressActivity.barTextHandler.sendMessage(msg);
				
				dstName = "/"+ Start.username + "/calligraphy";
				makeDir(dstName);
				
				/*整体上传时使用
				zipAllPage();
				uploadAllFile();
				*/
				
			}
			else{
				Log.e("uploadpage", "no page need upload");
				Toast.makeText(Start.context, "没有 修改或新建 过的文件需要备份", Toast.LENGTH_LONG).show();
				Start.barText.setVisibility(View.INVISIBLE);
				
				Message msg = new Message();
				msg.what = DownloadProgressActivity.KANBOX_UPLOAD_NONEED;
				msg.obj = "没有 修改或新建 过的文件需要备份";
				DownloadProgressActivity.barTextHandler.sendMessage(msg);
				
			}
		}
		
		db.close();
		Looper.loop();
	}
	/**
	 * 打包所有页文件到一个压缩包,整体上传时使用
	 */
	private void zipAllPage(){
		//打包本地文件
		try {
			File zipFile = new File(zipPath);
			if(zipFile.exists())
				zipFile.delete();
			Compressor.zip(
					zipPath,
					Start.getStoragePath() + "/calldir");
			
//			ZipUtils.Ectract("/extsd/" + Start.username + ".zip", "/extsd/testzip/");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 上传文件，整体上传时使用
	 */
	private void uploadAllFile() {
		try {

			Kanbox.getInstance().upload(zipPath, "/" + Start.username + ".zip", Token.getInstance(), new RequestListenerImplement(0));
		} catch (IOException e) {
			e.printStackTrace();
			Toast.makeText(Start.context, "操作失败\n\n" + e.toString(), Toast.LENGTH_LONG).show();
		}
	}
	/**
	 * 创建文件夹
	 */
	private void makeDir(String dirName) {
//		Kanbox.getInstance().makeDir(Token.getInstance(), "/kanbox", null);
		Kanbox.getInstance().makeDir(Token.getInstance(), dirName, this);
		
	}
		
	/**
	 * 复制文件
	 */
	private void copyFile() {
		DateFormat format2 = new java.text.SimpleDateFormat(  
        "yyyyMMddHHmmss");  
		String s = format2.format(new Date());  
		Kanbox.getInstance().copyFile(Token.getInstance(), dstName, dstName+s, this);
	}
	
	/**
	 * 删除文件
	 */
	private void deleteFile() {
		Kanbox.getInstance().deleteFile(Token.getInstance(), "/kanbox.png", this);
	}
	
	
	/**
	 * 压缩需要上传的文件
	 */
	public void zipUploadPage(List<Integer> pageList,String dstName){
		String tempPath = "";
		String dirPath = "";
		File zipFile;
		int indexCount = 0;
		for(int i : pageList){
			tempPath =  Start.getStoragePath() + "/calldir/free_" + i + "/index_"+i + ".jpg";
			
			Log.e("uploadindex", "tempPath:" + tempPath + " exit:" + (new File(tempPath)).exists());
			
			if((new File(tempPath)).exists())
				indexCount++;
		}
		
		RequestListener listener =  new RequestListenerImplement(pageList.size() + 1 + indexCount);
		
		
		Message msg = new Message();
		for(int i : pageList){
			dirPath = "/calldir/free_" + i;
			tempPath =  Start.getStoragePath() + "/calldir/page" + i + ".zip";
			zipFile = new File(tempPath);
			if(zipFile.exists())
				zipFile.delete();
			try {
				Compressor.zipPage(
						tempPath,
						Start.getStoragePath() + dirPath);
				Log.e("uploadpage", "upload page" +  i);
				
				msg = new Message();
				msg.what = DownloadProgressActivity.KANBOX_START_UPLOAD_PAGE;
				msg.obj = dirPath + "开始上传";
//				Start.barTextHandler.sendMessage(msg);
				DownloadProgressActivity.barTextHandler.sendMessage(msg);
				
				Kanbox.getInstance().upload(tempPath, dstName + "/page" + i + ".zip" , Token.getInstance(),listener);
				
				tempPath =  Start.getStoragePath() + "/calldir/free_" + i + "/index_"+i + ".jpg";
				
				Log.e("uploadindex", "tempPath:" + tempPath + " exit:" + (new File(tempPath)).exists());
				
				if((new File(tempPath)).exists()){
					
					Kanbox.getInstance().upload(tempPath, dstName + "/index_"+i + ".jpg" , Token.getInstance(),listener);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		try {
			msg = new Message();
			msg.what = DownloadProgressActivity.KANBOX_START_UPLOAD;
			msg.obj = "数据库文件开始上传";
//			Start.barTextHandler.sendMessage(msg);
			DownloadProgressActivity.barTextHandler.sendMessage(msg);
			
			Kanbox.getInstance().upload(
					"/data/data/com.jinke.calligraphy.app.branch/databases/calligraphy.db",
					dstName + "/calligraphy.db" , Token.getInstance(), listener);
			Log.e("uploadpage", "upload databases" + dstName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void uploadAll(){
		Message msg = new Message();
		msg.what = DownloadProgressActivity.KANBOX_START_UPLOAD;
		msg.obj = "酷盘文件夹建立成功";
//		Start.barTextHandler.sendMessage(msg);
		DownloadProgressActivity.barTextHandler.sendMessage(msg);
		
		
		
		
		zipUploadPage(pageList,dstName);//压缩并上传所有需要上传的页
	}
	
	@Override
	public void onComplete(String response, int operationType) {
		// TODO Auto-generated method stub
		Log.e("dir", "Upload AllFileThread complete response\n:" + response);
		switch (operationType) {
		case OP_MAKE_DIR:
			Log.e("dir", "mkdir response:" + response);
			if(response.contains("ERROR_PATH_EXIST") && iscopy)
//				Log.e("dir", "should copy");
				copyFile();
			else{
				Log.e("dir", "do not copy");
			
				uploadAll();	
			}
			break;

		case OP_UPLOAD:
			if(response.contains(".db")){
				Toast.makeText(Start.context, "数据库文件上传成功", Toast.LENGTH_LONG).show();
				
				Message msg = new Message();
				msg.what = DownloadProgressActivity.KANBOX_START_UPLOAD;
				msg.obj = "数据库文件上传完成";
//				Start.barTextHandler.sendMessage(msg);
				DownloadProgressActivity.barTextHandler.sendMessage(msg);
				
				break;
			}
			
			break;
		case OP_COPY:
			Log.e("dir", "dir copy success");
			uploadAll();
			break;
		default:
			break;
		}
		
	}
	@Override
	public void onError(KanboxException error, int operationType) {
		// TODO Auto-generated method stub
		Log.e("dir", "Upload AllFileThread complete error\n:" + error);
		Message msg;
		switch (operationType) {
		case OP_MAKE_DIR:
			Toast.makeText(Start.context, "创建文件夹失败，请重试", Toast.LENGTH_LONG).show();
			
			msg = new Message();
			msg.what = DownloadProgressActivity.KANBOX_ERROR;
			msg.obj = "创建文件夹失败，请退出重试";
			DownloadProgressActivity.barTextHandler.sendMessage(msg);
			
			break;

		case OP_UPLOAD:
			
			
			break;
		case OP_COPY:
			Log.e("dir", "dir copy failed");
			
			
			msg = new Message();
			msg.what = DownloadProgressActivity.KANBOX_ERROR;
			msg.obj = "酷盘网络备份失败，请退出重试";
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
		Log.e("dir", "Upload AllFileThread complete error\n:" + error);
		Message msg;
		switch (operationType) {
		
		case OP_MAKE_DIR:
			Log.e("upload", "make dir error");
			msg = new Message();
			msg.what = DownloadProgressActivity.KANBOX_ERROR;
			msg.obj = "网络异常，建立远程文件夹失败，请稍后重新尝试：";
//			Start.barTextHandler.sendMessage(msg);
			DownloadProgressActivity.barTextHandler.sendMessage(msg);
			break;

		case OP_UPLOAD:
			Log.e("upload", "upload dir error");
			
		// TODO Auto-generated method stub
			Start.addDownloadEnty(new DownloadEntity(path, destPath));
			msg = new Message();
			msg.what = DownloadProgressActivity.KANBOX_ERROR_UPLOAD;
			msg.obj = "传输过程中存在错误：";
//			Start.barTextHandler.sendMessage(msg);
			DownloadProgressActivity.barTextHandler.sendMessage(msg);
			break;
		}
	}
	
	
}
