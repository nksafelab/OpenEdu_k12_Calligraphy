package com.jinke.calligraphy.ftp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.jinke.calligraphy.app.branch.Start;
import com.jinke.calligraphy.backup.CalligraphyBackupUtil;
import com.jinke.calligraphy.template.WolfTemplateUtil;
import com.jinke.rloginservice.IReadingsLoginService;
import com.jinke.rloginservice.UserInfo;

public class FTPUtil {

//	public static String FILE_PATH_HEADER = "/extsd";
	public static String FILE_PATH_HEADER = Start.getStoragePath();
	
	public static void upload(String picPath){
		
		ContinueFTP myFtp = new ContinueFTP();
        try {
                        myFtp.connect("61.181.14.184", 21, "Administrator", "Ke!#%!@)%()&&Jin");
                        myFtp.ftpClient.makeDirectory(new String("Calligraphy_backup".getBytes("UTF-8"),
                                        "iso-8859-1"));
                        myFtp.ftpClient.changeWorkingDirectory(new String("Calligraphy_backup"
                                        .getBytes("UTF-8"), "iso-8859-1"));
                        
                        
                        myFtp.upload(picPath,picPath);
                        
                        

                        myFtp.disconnect();
                } catch (IOException e) {
                        System.out.println("连接FTP出错：" + e.getMessage());
                }

		
	}
	
	public static void download(String RemoteName,String localName){
		String date = getCurrentDate();
		ContinueFTP myFtp = new ContinueFTP();
        try {
                        myFtp.connect("61.181.14.184", 21, "Administrator", "Ke!#%!@)%()&&Jin");
//                        myFtp.ftpClient.makeDirectory(new String("Calligraphy_backup".getBytes("UTF-8"),
//                                        "iso-8859-1"));
                        myFtp.ftpClient.changeWorkingDirectory(new String("Calligraphy_backup"
                                        .getBytes("UTF-8"), "iso-8859-1"));
                        myFtp.ftpClient.changeWorkingDirectory(new String(FILE_PATH_HEADER
                                .getBytes("UTF-8"), "iso-8859-1"));
                        
                        myFtp.downloadFile(RemoteName, localName);
                        
//                        myFtp.list();

                        myFtp.disconnect();
                } catch (IOException e) {
                        System.out.println("连接FTP出错：" + e.getMessage());
                }
	}
	/**
	 * 上传文件夹/extsd/calldir下的所有文件
	 * @return
	 */
	public static boolean uploadLocalCalldir(){
		
		String simID = "";
		String userName = "";
		ContinueFTP myFtp = new ContinueFTP();
        try {
                        myFtp.connect("61.181.14.184", 21, "Administrator", "Ke!#%!@)%()&&Jin");
                        myFtp.ftpClient.makeDirectory(new String("Calligraphy_backup".getBytes("UTF-8"),
                                        "iso-8859-1"));
                        myFtp.ftpClient.changeWorkingDirectory(new String("Calligraphy_backup"
                                        .getBytes("UTF-8"), "iso-8859-1"));
                        
                        simID = CalligraphyBackupUtil.getSimID();
                        
                        userName = Start.username;
                        Log.e("FTP", "username:" + userName);

                        
                        myFtp.ftpClient.makeDirectory(new String(userName.getBytes("UTF-8"),
                        "iso-8859-1"));
                        myFtp.ftpClient.changeWorkingDirectory(new String(userName
                        .getBytes("UTF-8"), "iso-8859-1"));
                        
                        
                        File calldir = new File(FILE_PATH_HEADER + "/calldir");
                        if(!calldir.exists())
                        	return false;
                        
                        String[] names = null;
                        if(calldir.isDirectory())
                        	names = calldir.list();
                        for(String str:names){
                        	Log.e("ftp", "name:"+ str);
                        	
                        	myFtp.upload(FILE_PATH_HEADER + "/calldir/"+str, str);
                        }
                        
                        
                        
                        myFtp.disconnect();
                } catch (IOException e) {
                        System.out.println("连接FTP出错：" + e.getMessage());
                }

		return true;
	}
	
	/**
	 * 上传当前页的图片文件，包括目录，整图，涂鸦
	 * @return
	 */
	public static boolean uploadCalldirCurrentPage(){
		
		String simID = "";
		ContinueFTP myFtp = new ContinueFTP();
        try {
                        myFtp.connect("61.181.14.184", 21, "Administrator", "Ke!#%!@)%()&&Jin");
//                        myFtp.ftpClient.makeDirectory(new String("Calligraphy_backup".getBytes("UTF-8"),
//                                        "iso-8859-1"));
//                        myFtp.ftpClient.changeWorkingDirectory(new String("Calligraphy_backup"
//                                        .getBytes("UTF-8"), "iso-8859-1"));
                        myFtp.ftpClient.changeWorkingDirectory(new String("calli"
                              .getBytes("UTF-8"), "iso-8859-1"));
                        
                        simID = CalligraphyBackupUtil.getSimID();
                        
                        myFtp.ftpClient.makeDirectory(new String(simID.getBytes("UTF-8"),
                        "iso-8859-1"));
                        myFtp.ftpClient.changeWorkingDirectory(new String(simID
                        .getBytes("UTF-8"), "iso-8859-1"));
                        
                        String currentIndexPath = "index_"+Start.getPageNum()+".jpg";
                        String currentCallPath = "Calligraphy_"+Start.getPageNum()+"_"+
                        	WolfTemplateUtil.getCurrentTemplate().getTdirect() + ".jpg";
                        
//                        String currentBitPath = "bitmap_"+ Start.getPageNum()+".png";//涂鸦内容，改为文件夹内的png
                        
                        
                        File calldir = new File(FILE_PATH_HEADER + "/calldir");
                        if(!calldir.exists()){
                        	calldir.mkdir();
                        	return false;
                        }
                        
                        List<String> backupPicList = new ArrayList<String>();
                        backupPicList.add(currentIndexPath);
                        backupPicList.add(currentCallPath);
                        for(String str: backupPicList){
                        	Log.e("ftp", "name:"+ str);
                        	File file = new File(FILE_PATH_HEADER + "/calldir/"+str);
                        	if(file.exists()){
                        		myFtp.upload(FILE_PATH_HEADER + "/calldir/"+str, str);
                        		Log.e("ftp", str + " exit,upload");
                        	}else{
                        		Log.e("ftp", str + " donot exit, donot upload");
                        	}
                        }
                        
                        String dirName = "free_" + Start.getPageNum();
                        File dir = new File(FILE_PATH_HEADER + "/calldir/"+ dirName);
                        if(dir.exists()){
                        	if(dir.isDirectory()){
                        		
                        		myFtp.ftpClient.makeDirectory(new String(dirName.getBytes("UTF-8"),
                                "iso-8859-1"));
                                myFtp.ftpClient.changeWorkingDirectory(new String(dirName
                                .getBytes("UTF-8"), "iso-8859-1"));
                        		
                        		String [] names = dir.list();
                        		for(String str: names){
                        			File f = new File(FILE_PATH_HEADER + "/calldir/"+ dirName+"/"+str);
                        			if(f.exists()){
                        				myFtp.upload(FILE_PATH_HEADER + "/calldir/"+ dirName+"/"+str, str);
                        				Log.e("ftp", FILE_PATH_HEADER + "/calldir/"+ dirName+"/"+str + " exit , upload");
                        			}
                        			else
                        				Log.e("ftp", FILE_PATH_HEADER + "/calldir/"+ dirName+"/"+str + " not exit");
                        		}
                        	}
                        }
                        
//                        String[] names = {currentIndexPath,currentCallPath};
//                        if(calldir.isDirectory())
//                        	names = calldir.list();
                       
                        
                        
                        
                        myFtp.disconnect();
                } catch (IOException e) {
                        System.out.println("连接FTP出错：" + e.getMessage());
                }

		return true;
	}
	
	/**
	 * 上传当前页的图片文件，包括目录，整图，涂鸦,上传到用户名文件夹
	 * @return
	 */
	public boolean uploadCalldirCurrentPageToUserDir(){
		
		String simID = "";
		String userName = "";
		ContinueFTP myFtp = new ContinueFTP();
        try {
                        myFtp.connect("61.181.14.184", 21, "Administrator", "Ke!#%!@)%()&&Jin");
//                        myFtp.ftpClient.makeDirectory(new String("Calligraphy_backup".getBytes("UTF-8"),
//                                        "iso-8859-1"));
//                        myFtp.ftpClient.changeWorkingDirectory(new String("Calligraphy_backup"
//                                        .getBytes("UTF-8"), "iso-8859-1"));
                        myFtp.ftpClient.changeWorkingDirectory(new String("calli"
                              .getBytes("UTF-8"), "iso-8859-1"));
                        
//                        simID = CalligraphyBackupUtil.getSimID();
                        
                        userName = Start.username;
                        Log.e("FTP", "username:" + userName);
                        myFtp.ftpClient.makeDirectory(new String(userName.getBytes("UTF-8"),
                        "iso-8859-1"));
                        myFtp.ftpClient.changeWorkingDirectory(new String(userName
                        .getBytes("UTF-8"), "iso-8859-1"));
                        
                        String currentIndexPath = "index_"+Start.getPageNum()+".jpg";
                        String currentCallPath = "Calligraphy_"+Start.getPageNum()+"_"+
                        	WolfTemplateUtil.getCurrentTemplate().getTdirect() + ".jpg";
                        
//                        String currentBitPath = "bitmap_"+ Start.getPageNum()+".png";//涂鸦内容，改为文件夹内的png
                        
                        
                        File calldir = new File(FILE_PATH_HEADER + "/calldir");
                        if(!calldir.exists()){
                        	calldir.mkdir();
                        	return false;
                        }
                        
                        List<String> backupPicList = new ArrayList<String>();
                        backupPicList.add(currentIndexPath);
                        backupPicList.add(currentCallPath);
                        for(String str: backupPicList){
                        	Log.e("ftp", "name:"+ str);
                        	File file = new File(FILE_PATH_HEADER + "/calldir/"+str);
                        	if(file.exists()){
                        		myFtp.upload(FILE_PATH_HEADER + "/calldir/"+str, str);
                        		Log.e("ftp", str + " exit,upload");
                        	}else{
                        		Log.e("ftp", str + " donot exit, donot upload");
                        	}
                        }
                        
                        String dirName = "free_" + Start.getPageNum();
                        File dir = new File(FILE_PATH_HEADER + "/calldir/"+ dirName);
                        if(dir.exists()){
                        	if(dir.isDirectory()){
                        		
                        		myFtp.ftpClient.makeDirectory(new String(dirName.getBytes("UTF-8"),
                                "iso-8859-1"));
                                myFtp.ftpClient.changeWorkingDirectory(new String(dirName
                                .getBytes("UTF-8"), "iso-8859-1"));
                        		
                        		String [] names = dir.list();
                        		for(String str: names){
                        			File f = new File(FILE_PATH_HEADER + "/calldir/"+ dirName+"/"+str);
                        			if(f.exists()){
                        				myFtp.upload(FILE_PATH_HEADER + "/calldir/"+ dirName+"/"+str, str);
                        				Log.e("ftp", FILE_PATH_HEADER + "/calldir/"+ dirName+"/"+str + " exit , upload");
                        			}
                        			else
                        				Log.e("ftp", FILE_PATH_HEADER + "/calldir/"+ dirName+"/"+str + " not exit");
                        		}
                        	}
                        }
                        
//                        String[] names = {currentIndexPath,currentCallPath};
//                        if(calldir.isDirectory())
//                        	names = calldir.list();
                       
                        
                        
                        
                        myFtp.disconnect();
                } catch (IOException e) {
                        System.out.println("连接FTP出错：" + e.getMessage());
                }

		return true;
	}
	

	public static boolean downloadLocalCalldir(){
		
		String simID = "";
		ContinueFTP myFtp = new ContinueFTP();
        try {
                        myFtp.connect("61.181.14.184", 21, "Administrator", "Ke!#%!@)%()&&Jin");
//                        myFtp.ftpClient.makeDirectory(new String("Calligraphy_backup".getBytes("UTF-8"),
//                                        "iso-8859-1"));
//                        myFtp.ftpClient.changeWorkingDirectory(new String("Calligraphy_backup"
//                                        .getBytes("UTF-8"), "iso-8859-1"));
                        myFtp.ftpClient.changeWorkingDirectory(new String("calli"
                                .getBytes("UTF-8"), "iso-8859-1"));
                        
                        simID = CalligraphyBackupUtil.getSimID();
                        
                        myFtp.ftpClient.makeDirectory(new String(simID.getBytes("UTF-8"),
                        "iso-8859-1"));
                        myFtp.ftpClient.changeWorkingDirectory(new String(simID
                        .getBytes("UTF-8"), "iso-8859-1"));
                        
                        String[] lists = myFtp.list();
                        for(String s : lists){
                        	System.out.println("name:"+ s);
                        	if(s.contains("free_")){
                        		System.out.println("-----------------free_" + s);
                        		File localdir = new File(FILE_PATH_HEADER + "/calldir/" + s);
                        		if(!localdir.exists())
                        			localdir.mkdir();
                        		
                        		myFtp.ftpClient.changeWorkingDirectory(new String(s
                                        .getBytes("UTF-8"), "iso-8859-1"));
                        		//下载
                        		String[] frees = myFtp.list();
                        		for(String free: frees){
                        			System.out.println("-----------------frees:" + free);
                        			myFtp.downloadFile(free, FILE_PATH_HEADER + "/calldir/"+s + "/" + free);
                        		}
                        		
                        		myFtp.ftpClient.changeToParentDirectory();
                        	}else
                        		myFtp.downloadFile(s, FILE_PATH_HEADER + "/calldir/"+s);
                        }
//                        Start.c.view.setFreeDrawBitmap();
                        
                        myFtp.disconnect();
                } catch (IOException e) {
                        System.out.println("连接FTP出错：" + e.getMessage());
                }

		return true;
	}
	
	/**
	 * 下载当前用户的文件夹
	 * @return
	 */
	public boolean downloadUserLocalCalldir(){
		
		String simID = "";
		String userName = "";
		ContinueFTP myFtp = new ContinueFTP();
        try {
                        myFtp.connect("61.181.14.184", 21, "Administrator", "Ke!#%!@)%()&&Jin");
//                        myFtp.ftpClient.makeDirectory(new String("Calligraphy_backup".getBytes("UTF-8"),
//                                        "iso-8859-1"));
//                        myFtp.ftpClient.changeWorkingDirectory(new String("Calligraphy_backup"
//                                        .getBytes("UTF-8"), "iso-8859-1"));
                        myFtp.ftpClient.changeWorkingDirectory(new String("calli"
                                .getBytes("UTF-8"), "iso-8859-1"));
                        
                        simID = CalligraphyBackupUtil.getSimID();
                        userName = Start.username;
                        Log.e("FTP", "username:" + userName);
                        myFtp.ftpClient.makeDirectory(new String(userName.getBytes("UTF-8"),
                        "iso-8859-1"));
                        myFtp.ftpClient.changeWorkingDirectory(new String(userName
                        .getBytes("UTF-8"), "iso-8859-1"));
                        
                        String[] lists = myFtp.list();
                        for(String s : lists){
                        	System.out.println("name:"+ s);
                        	if(s.contains("free_")){
                        		System.out.println("-----------------free_" + s);
                        		File localdir = new File(FILE_PATH_HEADER + "/calldir/" + s);
                        		if(!localdir.exists())
                        			localdir.mkdir();
                        		
                        		myFtp.ftpClient.changeWorkingDirectory(new String(s
                                        .getBytes("UTF-8"), "iso-8859-1"));
                        		//下载
                        		String[] frees = myFtp.list();
                        		for(String free: frees){
                        			System.out.println("-----------------frees:" + free);
                        			myFtp.downloadFile(free, FILE_PATH_HEADER + "/calldir/"+s + "/" + free);
                        		}
                        		
                        		myFtp.ftpClient.changeToParentDirectory();
                        	}else
                        		myFtp.downloadFile(s, FILE_PATH_HEADER + "/calldir/"+s);
                        }
//                        Start.c.view.setFreeDrawBitmap();
                        
                        myFtp.disconnect();
                } catch (IOException e) {
                        System.out.println("连接FTP出错：" + e.getMessage());
                }

		return true;
	}
	
	
	public static String getCurrentDate(){
		final Calendar c = Calendar.getInstance();

        int mYear = c.get(Calendar.YEAR); //获取当前年份 

        int mMonth = c.get(Calendar.MONTH);//获取当前月份 

        int mDay = c.get(Calendar.DAY_OF_MONTH);//获取当前月份的日期号码 

        int mHour = c.get(Calendar.HOUR_OF_DAY);//获取当前的小时数 

        int mMinute = c.get(Calendar.MINUTE);//获取当前的分钟数  

        return mYear+"y"+mMonth+"m"+mDay+"d"+mHour+"h"+mMinute;
	}
	
	
	
	
	
}
