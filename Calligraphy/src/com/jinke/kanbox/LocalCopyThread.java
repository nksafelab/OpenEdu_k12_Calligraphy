package com.jinke.kanbox;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import android.os.Message;
import android.util.Log;

import com.jinke.calligraphy.app.branch.Start;
import com.jinke.calligraphy.database.CDBPersistent;

public class LocalCopyThread{

	String backupPath = Start.getStoragePath() + "/callbackup";
	
	public void run() {
		// TODO Auto-generated method stub
		
		File backupDir = new File(backupPath);
		if(!backupDir.exists())
			backupDir.mkdir();
		
		DateFormat format2 = new java.text.SimpleDateFormat(  
        "yyyyMMddHHmmss");  
		String s = format2.format(new Date()); 
		
		String current = backupPath + "/calligraphy" + s + "_local/";
		File currentDir = new File(current);
		if(!currentDir.exists())
			currentDir.mkdir();
		
		Start.resetTotalPagenum();
		zipUploadPage(current);
		
	}
	
	public static void copy(String from, String to){
		InputStream in = null;
		OutputStream out = null;
		try {
			in = new FileInputStream(from);
			out = new FileOutputStream(to);
			int count = 0;
			byte[] tmp = new byte[1024];
			while((count = in.read(tmp)) != -1){
				out.write(tmp, 0, count);
			}
			in.close();
			out.flush();
			out.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}
	
	
	/**
	 * 压缩需要上传的文件
	 */
	public void zipUploadPage(String backupPath){
		String tempPath = "";
		String dirPath = "";
		File zipFile;
		copy("/data/data/com.jinke.calligraphy.app.branch/databases/calligraphy.db",backupPath + "/calligraphy.db");
		for(int i=0;i<Start.totlePageNum;i++){
			dirPath = "/calldir/free_" + (i+1);
			tempPath =  backupPath + "page" + (i+1) + ".zip";
			
			copy(Start.getStoragePath() + dirPath + "/index_" + (i+1) + ".jpg",backupPath + "/index_" + (i+1) + ".jpg");
			
			zipFile = new File(tempPath);
			if(zipFile.exists())
				zipFile.delete();
			try {
				Compressor.zipPage(
						tempPath,
						Start.getStoragePath() + dirPath);
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
	}
	
	
}
