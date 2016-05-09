package com.jinke.calligraphy.command;

import android.graphics.Bitmap;

import com.jinke.calligraphy.app.branch.Command;
import com.jinke.calligraphy.app.branch.Start;
import com.jinke.calligraphy.ftp.FTPUtil;

public class FtpCommand implements Command{
	
//	private String callPath = "/extsd/calldir/";
	

	@Override
	public void execute() {
		// TODO Auto-generated method stub
		
//		new FtpThread().start();
//		FTPUtil.uploadLocalCalldir(); //上传所有文件
		
//		FTPUtil.uploadCalldirCurrentPage();  //上传当前页的文件
		
		FTPUtil f = new FTPUtil();
		f.uploadCalldirCurrentPageToUserDir();
	}

	@Override
	public void undo(Bitmap b) {
		// TODO Auto-generated method stub
		
	}
	
	class FtpThread extends Thread{
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			
			FTPUtil.uploadLocalCalldir();
			
			
			
		}
	}
	
	
}
