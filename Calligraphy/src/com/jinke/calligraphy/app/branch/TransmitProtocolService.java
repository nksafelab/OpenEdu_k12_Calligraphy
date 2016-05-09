package com.jinke.calligraphy.app.branch;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class TransmitProtocolService extends Service {
	
	public static long progress = 0;
	public static ServerSocket ss = null;
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		Log.v("renkai","onBind");
		return sBind;
	}
	
	
	@Override
	public boolean onUnbind(Intent intent) {
		// TODO Auto-generated method stub
		return super.onUnbind(intent);
	}

	public class ShareBind extends Binder        //句柄类
	{
		public TransmitProtocolService getService()
		{
			return TransmitProtocolService.this;
		}
	}
	
	private ShareBind sBind = new ShareBind();   //返回本类的句柄
	
	//interfaces
	public void wputfile(String uri, int port)
	{
		progress = 0;
		if(ss != null){
			try {
				Log.v("renkai","ss  close");
				ss.close();
				ss = null;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		new Server(uri, port).start();
	}
	
	public void wgetfile(String saveuri, String destip, int port)
	{
		progress = 0;
		new Client(saveuri, destip, port).start();
	}
	
	public void wputfiles(String[] uri, int port)
	{
		progress = 0;
		if(ss != null){
			try {
				Log.v("renkai","ss  close");
				ss.close();
				ss = null;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		new Servers(uri, port).start();
	}
	
	public void wgetfiles(String[] saveuri, String destip, int port)
	{
		progress = 0;
		new Clients(saveuri, destip, port).start();
	}
	
	public int getProgress()
	{
		return (int)this.progress;
	}
	
	class Server extends Thread
	{
		public String uri;
		public int port;
		
		public Server(String uri, int port){
			this.uri = uri;
			this.port = port;
		}
		
		public void run(){
			Socket s = null;
	        try {
	        	if(ss == null)
	        		ss = new ServerSocket(port);
                // 选择进行传输的文件
            	
                String filePath = uri;
                System.out.println("文件路径:" + filePath);
                File fi = new File(filePath);
                
                long sumlength = fi.length();

                System.out.println("文件长度:" + sumlength);

               // IOException侦听并接受到此套接字的连接。此方法在进行连接之前一直阻塞。
                s = ss.accept();
                System.out.println("建立socket链接");

                DataInputStream fis = new DataInputStream(new BufferedInputStream(new FileInputStream(filePath)));
                DataOutputStream ps = new DataOutputStream(s.getOutputStream());
                //将文件名及长度传给客户端。这里要真正适用所有平台，例如中文名的处理，还需要加工，具体可以参见Think In Java 4th里有现成的代码。
                ps.writeLong(sumlength);
                ps.flush();

                int bufferSize = 8192;
                byte[] buf = new byte[bufferSize];
                
                long hasread = 0;

                long begin = System.currentTimeMillis();
                
                while (true) {
                    int read = 0;
                    if (fis != null) {
                        read = fis.read(buf);
                        hasread += read;
                        Log.v("renkai", "hasread="+String.valueOf(hasread));
                        progress = hasread * 100/sumlength;
                        Log.v("renkai", "progress="+String.valueOf(progress));
                    }

                    if (read == -1) {
                        break;
                    }
                    ps.write(buf, 0, read);
                }
                ps.flush();
                // 注意关闭socket链接哦，不然客户端会等待server的数据过来，
                // 直到socket超时，导致数据不完整。
                progress = 100;
                long end = System.currentTimeMillis();
                Thread.sleep(1000);
                fis.close();
                s.close();
                ss.close();
                ss = null;
                System.out.println("传输时间："+String.valueOf((long)(end - begin)));
                System.out.println("文件传输完成");
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
		}
	}
	
	class Servers extends Thread
	{
		public String[] uri;
		public int port;
		
		public Servers(String[] uri, int port){
			this.uri = uri;
			this.port = port;
		}
		
		public void run(){
			Socket s = null;
	        try {
	        	if(ss == null)
	        		ss = new ServerSocket(port);
                // 选择进行传输的文件
	        	long sumlength = 0;
            	for(int i = 0; i != uri.length;i++){
            		String filePath = uri[i];
                    System.out.println("文件路径:" + filePath);
                    File fi = new File(filePath);
                    sumlength += fi.length();
            	}
                System.out.println("文件长度:" + sumlength);
                // IOException侦听并接受到此套接字的连接。此方法在进行连接之前一直阻塞。
                long end = System.currentTimeMillis();
				Log.v("time", String.valueOf((long)end));
                s = ss.accept();
                System.out.println("建立socket链接");

                DataOutputStream ps = new DataOutputStream(s.getOutputStream());
                //将文件名及长度传给客户端。这里要真正适用所有平台，例如中文名的处理，还需要加工，具体可以参见Think In Java 4th里有现成的代码。
                ps.writeLong(sumlength);
                ps.flush();

                long hasread = 0;
                for(int i = 0; i != uri.length;i++){
                	DataInputStream fis = new DataInputStream(new BufferedInputStream(new FileInputStream(uri[i])));

                	int bufferSize = 8192;
                    byte[] buf = new byte[bufferSize];
                    long begin = System.currentTimeMillis();
                    
                    while (true) {
//                    	Log.v("renkai","write2");
                        int read = 0;
                        if (fis != null) {
                            read = fis.read(buf);
                            Log.v("renkai","read="+String.valueOf(read));
                            if (read == -1) {
//                            	Log.v("renkai","break;");
                                break;
                            }
                            hasread += read;
                            progress = hasread * 100/sumlength;
//                            Log.v("renkai","progress="+String.valueOf(progress));
                        }
//                        Log.v("renkai","write");
                        try{
                        ps.write(buf, 0, read);
                        }catch(IOException e)
                        {
                        	e.printStackTrace();
                        }
//                        Log.v("renkai","write1");
                    }
                    ps.flush();
                    Thread.sleep(1000);
                    Log.v("renkai","pswrite");
                    ps.write(buf, 0, 1);
                    ps.flush();               
                    fis.close();
                }
                progress = 100;
                s.close();
                ss.close();
                ss = null;
                Log.v("renkai","s close?");
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
		}
	}
	
	public class ClientSocket {
	    private String ip;
	    private int port;
	    private Socket socket = null;
	    DataOutputStream out = null;
	    DataInputStream getMessageStream = null;

	    public ClientSocket(String ip, int port) {
	        this.ip = ip;
	        this.port = port;
	    }

	    /** *//**
	     * 创建socket连接
	     * 
	     * @throws Exception
	     *             exception
	     */
	    public void CreateConnection() throws Exception {
	        try {
	            socket = new Socket(ip, port);
	        } catch (Exception e) {
	            e.printStackTrace();
	            if (socket != null)
	                socket.close();
	            throw e;
	        } finally {
	        }
	    }

	    public DataInputStream getMessageStream() throws Exception {
	        try {
	            getMessageStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
	            return getMessageStream;
	        } catch (Exception e) {
	            e.printStackTrace();
	            if (getMessageStream != null)
	                getMessageStream.close();
	            throw e;
	        } finally {
	        }
	    }
	    
	    public void sendMessage(String sendMessage) throws Exception {
	        try {
	            out = new DataOutputStream(socket.getOutputStream());
	            if (sendMessage.equals("Windows")) {
	                out.writeByte(0x1);
	                out.flush();
	                return;
	            }
	            if (sendMessage.equals("Unix")) {
	                out.writeByte(0x2);
	                out.flush();
	                return;
	            }
	            if (sendMessage.equals("Linux")) {
	                out.writeByte(0x3);
	                out.flush();
	            } else {
	                out.writeUTF(sendMessage);
	                out.flush();
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	            if (out != null)
	                out.close();
	            throw e;
	        } finally {
	        }
	    }

	    public void shutDownConnection() {
	        try {
	            if (out != null)
	                out.close();
	            if (getMessageStream != null)
	                getMessageStream.close();
	            if (socket != null)
	                socket.close();
	        } catch (Exception e) {

	        }
	    }
	}

	public class Client extends Thread{
	    
		private String uri;
		private String destip;
		private ClientSocket cs = null;
	    private int port;

	    public Client(String saveuri, String ip, int port) {      
	    	this.uri = saveuri;
	    	this.port = port;
	    	this.destip = ip;
	    }
	    
	    public void run(){
	    	try {
	            if (createConnection()) {
	            	sendMessage();
	                getMessage();
	            }

	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }
	    }

	    private boolean createConnection() {
	        cs = new ClientSocket(destip, port);
	        int time = 0;
	        while(time < 10){
		        try {
		            cs.CreateConnection();
		            System.out.print("连接服务器成功!" + "\n");
		            return true;
		        } catch (Exception e) {
		            System.out.print("连接服务器失败!" + "\n");
		            time++;
		            try {
						Thread.sleep(500);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
		        }
	        }
            return false;
	    }
	    
	    private void sendMessage() {
	        if (cs == null)
	            return;
	        try {
	            cs.sendMessage("Linux");
	        } catch (Exception e) {
	            System.out.print("发送消息失败!" + "\n");
	        }
	    }

	    private void getMessage() {
	        if (cs == null)
	            return;
	        DataInputStream inputStream = null;
	        try {
	            inputStream = cs.getMessageStream();
	        } catch (Exception e) {
	            System.out.print("接收消息缓存错误\n");
	            return;
	        }

	        DataOutputStream fileOut = null;
	        try {
	            //本地保存路径，文件名会自动从服务器端继承而来。
	            String savePath = uri;
	            int bufferSize = 8192;
	            byte[] buf = new byte[bufferSize];
	            long passedlen = 0;
	            long len=0;
	                      
	            fileOut = new DataOutputStream(new BufferedOutputStream(new BufferedOutputStream(new FileOutputStream(savePath))));
	            
	            len = inputStream.readLong();
	            
	            System.out.println("文件的长度为:" + len + "\n");
	            System.out.println("开始接收文件!" + "\n");
	                    
	            while (true) {
	                int read = 0;
	                if (inputStream != null) {
	                    read = inputStream.read(buf);
	                    System.out.println(read+"");
	                }
	                
	                if (read == -1) {
	                	System.out.println("readfinish");
	                    break;
	                }
	                passedlen += read;
	                Log.v("renkai", "passedlen="+passedlen);
	                progress = passedlen * 100/ len;
	                Log.v("renkai", "progress="+progress);
	                //下面进度条本为图形界面的prograssBar做的，这里如果是打文件，可能会重复打印出一些相同的百分比
	                System.out.println("文件接收了" +  (passedlen * 100/ len) + "%\n");
	                fileOut.write(buf, 0, read);
	                if(progress == 100){
	                	fileOut.flush();
//	                	fileOut.close();
	                	cs.shutDownConnection();
	                }
	            }
	            System.out.println("接收完成，文件存为" + savePath + "\n");
                progress = 100;
                fileOut.flush();
//	            fileOut.close();
	            
	        } catch (Exception e) {
	        	e.printStackTrace();
	        	try {
	        		fileOut.flush();
//					fileOut.close();
					cs.shutDownConnection();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
	            System.out.println("接收消息错误" + "\n");
	            return;
	        }
	    }
	}
	
	public class Clients extends Thread{
	    
		private String[] uri;
		private String destip;
		private ClientSocket cs = null;
	    private int port;

	    public Clients(String[] saveuri, String ip, int port) {      
	    	this.uri = saveuri;
	    	this.port = port;
	    	this.destip = ip;
	    }
	    
	    public void run(){
	    	try {
	            if (createConnection()) {
	            	sendMessage();
	                getMessage();
	            }

	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }
	    }

	    private boolean createConnection() {
	        cs = new ClientSocket(destip, port);
	        int time = 0;
	        while(time < 10){
	        	long end = System.currentTimeMillis();
				Log.v("time", String.valueOf((long)end));
		        try {
		        	cs.CreateConnection();
		            System.out.print("连接服务器成功!" + "\n");
		            return true;
		        } catch (Exception e) {
		            System.out.print("连接服务器失败!" + "\n");
		            time++;
		            try {
						Thread.sleep(500);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
		        }
	        }
            return false;
	    }
	    
	    private void sendMessage() {
	        if (cs == null)
	            return;
	        try {
	            cs.sendMessage("Linux");
	        } catch (Exception e) {
	            System.out.print("发送消息失败!" + "\n");
	        }
	    }

	    private void getMessage() {
	        if (cs == null)
	            return;
	        DataInputStream inputStream = null;
	        try {
	            inputStream = cs.getMessageStream();
	        } catch (Exception e) {
	            System.out.print("接收消息缓存错误\n");
	            return;
	        }

	        DataOutputStream fileOut = null;
	        long len = 0;
	        try {
				len = inputStream.readLong();
	            System.out.println("文件的长度为:" + len + "\n");
	            System.out.println("开始接收文件!" + "\n");
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			

        	Log.v("renkai", "uri.length="+String.valueOf(uri.length));
        	long passedlen = 0;
            for(int i = 0; i != uri.length; i++){
            	String savePath = uri[i];
            	Log.v("renkai","savePath="+savePath);
	            int bufferSize = 8192;
	            byte[] buf = new byte[bufferSize];
	            int read = 0;       
	            try{
	            	fileOut = new DataOutputStream(new BufferedOutputStream(new BufferedOutputStream(new FileOutputStream(savePath))));
	            	
		            while (true) {
		                if (inputStream != null) {
		                    read = inputStream.read(buf);
		                    System.out.println(String.valueOf(read)+"");
		                }
		                
		                if(read == 1)
		                {
		                	fileOut.flush();
		                	break;
		                }else if(read == -1){
		                	fileOut.flush();
		                	break;
		                }
		                
		                passedlen += read;
		                Log.v("renkai", "passedlen="+passedlen);
		                progress = passedlen * 100/ len;
		                Log.v("renkai", "progress="+progress);
		                //下面进度条本为图形界面的prograssBar做的，这里如果是打文件，可能会重复打印出一些相同的百分比
		                System.out.println("文件接收了" +  (passedlen * 100/ len) + "%\n");
		                fileOut.write(buf, 0, read);
		                if(progress == 100){
		                	fileOut.flush();
		                	cs.shutDownConnection();
		                	break;
		                }
		            }
	            } catch (Exception e) {
	            	try {
	            		e.printStackTrace();
	            		Log.v("renkai","EXCE");
						fileOut.write(buf, 0, read);
						fileOut.flush();
						if(progress == 100){
		                	cs.shutDownConnection();
		                }
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
		        }
	            
            }
            progress = 100;
	    }
	}
}
