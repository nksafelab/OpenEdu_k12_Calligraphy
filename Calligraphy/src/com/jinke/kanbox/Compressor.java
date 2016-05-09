package com.jinke.kanbox;

/*
* Created on 2007-10-11 *
*   java编程 日记---   压缩文件和文件夹类Compressor.java
*/
import java.util.zip.*;
import java.io.*;

import android.util.Log;

public class Compressor {
/**
   * 压缩文件
   * @param zipFileName 保存的压缩包文件路径
   * @param inputFile 需要压缩的文件夹或者文件路径
   * @throws Exception
   */
public static void zip(String zipFileName, String inputFile) throws Exception {
//	long start = System.currentTimeMillis();
//	Log.e("zip","start:" + start);
   zip(zipFileName, new File(inputFile));
//   long end = System.currentTimeMillis();
//   Log.e("zip", "end:" + end);
//   Log.e("zip", "use:" + (end - start));
   /*
    *346: sysout:
    * 	E/zip     ( 3152): start:1334304490109
		E/zip     ( 3152): zip f:free_22 start at:1334304490119 end at:1334304520639 use:30520
		E/zip     ( 3152): end:1334304521207
		E/zip     ( 3152): use:31098

	Log.e
		start:1334304692261
		zip f:free_22 start at:1334304692274 end at:1334304722863 use:30589
		end:1334304723460
		E/zip     ( 3792): use:31199
		
		E/zip     ( 4224): start:1334304822757
		E/zip     ( 4224): end:1334304853048
		E/zip     ( 4224): use:30291
		
		
		E/zip     ( 6873): start:1334305507035
		E/zip     ( 6873): end:1334305507843
		E/zip     ( 6873): use:808
		E/zip     ( 6902): start:1334305511931
		E/zip     ( 6902): end:1334305512735
		E/zip     ( 6902): use:804
		E/zip     ( 6930): start:1334305516660
		E/zip     ( 6930): end:1334305517516
		E/zip     ( 6930): use:856
		E/zip     ( 6955): start:1334305521539
		E/zip     ( 6955): end:1334305522335
		E/zip     ( 6955): use:796

		
    */
}
	public static void zipPage(String zipFileName, String inputFile) throws Exception {
		zipPage(zipFileName, new File(inputFile));
		}
	
	private static void zip(String zipFileName, File inputFile) throws Exception {
	   ZipOutputStream out = new ZipOutputStream(new FileOutputStream(
	     zipFileName));
	   zip(out, inputFile, "");//递归压缩方法
	   zip(out,new File("/data/data/com.jinke.calligraphy.app.branch/databases/calligraphy.db"),"calligraphy.db");
	   System.out.println("zip done");
	   out.flush();
	   out.close();
	}
	private static void zipPage(String zipFileName, File inputFile) throws Exception {
		   ZipOutputStream out = new ZipOutputStream(new FileOutputStream(
		     zipFileName));
		   zip(out, inputFile, "");//递归压缩方法
		   System.out.println("zip done");
		   out.close();
		}


     /**
      * 递归压缩方法
      * @param out   压缩包输出流
      * @param f     需要压缩的文件
      * @param base 压缩的路径
      * @throws Exception
      */
private static void zip(ZipOutputStream out, File f, String base) throws Exception {
//   System.out.println("Zipping   " + f.getName()); //记录日志，开始压缩
//	Log.e("zip", "zip:" + f.getName());
   if (f.isDirectory()) {   // 如果是文件夹，则获取下面的所有文件
	long start = System.currentTimeMillis();
//	Log.e("zip", "zip f:" + f.getName() + " start at:" + start);
    File[] fl = f.listFiles();
    
    out.putNextEntry(new ZipEntry(base + "/"));
    base = base.length() == 0 ? "" : base + "/";
    for (int i = 0; i < fl.length; i++) {
     zip(out, fl[i], base + fl[i].getName());
    }
    long end = System.currentTimeMillis();
//    Log.e("zip", "zip f:" + f.getName() + " start at:" + start + " end at:" + end + " use:" + (end - start));
   } else {   // 如果是文件，则压缩
    out.putNextEntry(new ZipEntry(base)); // 生成下一个压缩节点
    FileInputStream in = new FileInputStream(f);   // 读取文件内容
    
    byte[] temp = new byte[1024];
    int count = 0;
    while((count = in.read(temp)) != -1){
    	out.write(temp, 0, count);
    }
//    int b;
//    while ((b = in.read()) != -1)
//     out.write(b);   // 写入到压缩包
    in.close();
   }
}
public static void main(String [] args){
   Compressor cpr = new Compressor();
   try {
   cpr.zip("F:\\client\\update.zip", "F:\\server");
} catch (Exception e) {
   // TODO Auto-generated catch block
   e.printStackTrace();
}

}
}
