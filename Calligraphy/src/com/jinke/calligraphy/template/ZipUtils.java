package com.jinke.calligraphy.template;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.util.Log;

public class ZipUtils {
    /**
     * @param args
     */
    public static int iCompressLevel; // 压缩比 取值范围为0~9
    public static boolean bOverWrite; // 是否覆盖同名文件 取值范围为True和False
    private static ArrayList allFiles = new ArrayList();
    public static String sErrorMessage;

    public static ArrayList Ectract(String sZipPathFile, String sDestPath) {
    	Log.e("dir", "sZipPathFile:" + sZipPathFile);
        ArrayList allFileName = new ArrayList();
//        System.out.println("===================================");
        try {
            // 先指定压缩档的位置和档名，建立FileInputStream对象
            FileInputStream fins = new FileInputStream(sZipPathFile);
            // 将fins传入ZipInputStream中
            ZipInputStream zins = new ZipInputStream(fins);
            ZipEntry ze = null;
            byte ch[] = new byte[256];
            
//            System.out.println("===================================");
            
            while ((ze = zins.getNextEntry()) != null) {
            	Log.e("dir", "----->>>start unzip:" + ze.getName());
//            	System.out.println("==================================="+ ze.getName());
//            	if(ze.getName() != null && "calligraphy.db".equals(ze.getName())){
//            		//数据库文件，解压到程序目录
//            		File zfile = new File("/data/data/com.jinke.calligraphy.app.branch/databases/calligraphy.db");
//            		FileOutputStream fouts = new FileOutputStream(zfile);
//                    int i;
//                    allFileName.add(zfile.getAbsolutePath());
//                    while ((i = zins.read(ch)) != -1)
//                        fouts.write(ch, 0, i);
//                    zins.closeEntry();
//                    fouts.close();
//            		
//            	}else{
            		
                File zfile = new File(sDestPath + ze.getName());
                File fpath = new File(zfile.getParentFile().getPath());
                if (ze.isDirectory()) {
//                	Log.e("dir", "----->>>dir");
//                	System.out.println("===================================isDirectory");
                    if (!zfile.exists())
                        zfile.mkdirs();
                    zins.closeEntry();
                } else {
//                	Log.e("dir", "----->>>file " + fpath);
//                	System.out.println("===================================fpath"+ fpath);
                    if (!fpath.exists()){
                        fpath.mkdirs();
//                        System.out.println("===================================mkdir"+ fpath);
                    }
                    FileOutputStream fouts = new FileOutputStream(zfile);
                    int i;
                    allFileName.add(zfile.getAbsolutePath());
                    while ((i = zins.read(ch)) != -1)
                        fouts.write(ch, 0, i);
                    zins.closeEntry();
                    fouts.close();
                }
            }
            fins.close();
            
//            }
            zins.close();
            sErrorMessage = "OK";
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Extract error:" + e.getMessage());
            sErrorMessage = e.getMessage();
        }
        allFiles.clear();
        return allFileName;
    }

}