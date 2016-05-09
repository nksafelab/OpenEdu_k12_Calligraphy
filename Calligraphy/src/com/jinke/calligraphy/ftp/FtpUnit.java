package com.jinke.calligraphy.ftp;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPReply;

import android.os.Environment;

public class FtpUnit {
        private FTPClient ftpClient = null;
        private String SDPATH;
        public FtpUnit(){
                SDPATH =Environment.getExternalStorageDirectory()+"/";
        }
        
        /**
         * 连接Ftp服务器
         */
        public  void connectServer(){
                if(ftpClient == null){
                        int reply;
                        try{
                                ftpClient = new FTPClient();
                                ftpClient.setDefaultPort(21);
                                ftpClient.configure(getFtpConfig());
                                ftpClient.connect("172.16.18.175");
                                ftpClient.login("anonymous","");
                                ftpClient.setDefaultPort(21);                                
                                reply = ftpClient.getReplyCode();
                                System.out.println(reply+"----");
                if (!FTPReply.isPositiveCompletion(reply)) {
                     ftpClient.disconnect();
                     System.err.println("FTP server refused connection.");
                 }
                ftpClient.enterLocalPassiveMode();
                ftpClient.setControlEncoding("gbk");
                        }catch(Exception e){
                                e.printStackTrace();
                        }
                }
        }
        
        /**
     * 上传文件
     * @param localFilePath--本地文件路径
     * @param newFileName--新的文件名
    */
   public void uploadFile(String localFilePath,String newFileName){
        connectServer();
       //上传文件
        BufferedInputStream buffIn=null;
       try{
            buffIn=new BufferedInputStream(new FileInputStream(SDPATH+"/"+localFilePath));
            System.out.println(SDPATH+"/"+localFilePath);
            System.out.println("start="+System.currentTimeMillis());
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.storeFile("a1.mp3", buffIn);
            System.out.println("end="+System.currentTimeMillis());
        }catch(Exception e){
            e.printStackTrace();
        }finally{
           try{
               if(buffIn!=null)
                    buffIn.close();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }
   
   /**
    * 下载文件
    * @param remoteFileName --服务器上的文件名
    * @param localFileName--本地文件名
   */
  public  void loadFile(String remoteFileName,String localFileName){
       connectServer();
       System.out.println("==============="+localFileName);
      //下载文件
       BufferedOutputStream buffOut=null;
      try{
           buffOut=new BufferedOutputStream(new FileOutputStream(SDPATH+localFileName));
           long start = System.currentTimeMillis();
           ftpClient.retrieveFile(remoteFileName, buffOut);
           long end = System.currentTimeMillis();
           System.out.println(end-start);
       }catch(Exception e){
           e.printStackTrace();
       }finally{
          try{
              if(buffOut!=null)
                   buffOut.close();
           }catch(Exception e){
               e.printStackTrace();
           }
       }
   }
  
        /**
     * 设置FTP客服端的配置--一般可以不设置
     * @return
     */
   private static FTPClientConfig getFtpConfig(){
        FTPClientConfig ftpConfig=new FTPClientConfig(FTPClientConfig.SYST_UNIX);
        ftpConfig.setServerLanguageCode(FTP.DEFAULT_CONTROL_ENCODING);
       return ftpConfig;
    }
   
   /**
    * 关闭连接
   */
  public  void closeConnect(){
      try{
          if(ftpClient!=null){
               ftpClient.logout();
               ftpClient.disconnect();
           }
       }catch(Exception e){
           e.printStackTrace();
       }
   }
  
}