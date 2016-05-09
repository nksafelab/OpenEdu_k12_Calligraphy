package com.jinke.calligraphy.database;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;

import com.jinke.calligraphy.app.branch.Start;
import com.jinke.single.BitmapCount;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.util.Log;

public class BitmapHelper {

	public static byte[] bitmapDecode(Bitmap bmp) throws IOException {
		if(bmp != null && bmp.isRecycled())
			return null;
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		if (bmp == null) {
			// Resources res=context.getResources();
			// bmp=BitmapFactory.decodeResource(res,R.drawable.icon);
			// System.out.println("bitmapDecode@CDBPersistent----exception bmp is null,use default book");
			return null;
		}

		bmp.compress(Bitmap.CompressFormat.PNG, 10, out);
		
		byte[] array = null;
		try {
			array = out.toByteArray();
		} catch (OutOfMemoryError e) {
			Log.e("arrayoom", "" , e);
		}
		return array;
	}
	
	public static Bitmap getBitmapFromUri(Uri uri , int pageNum){
		
//		uri = Uri.parse("content://media/external/images/media/25");
		String storagePath = Start.getStoragePath();// "/mnt/sdcard" 或者 "/mnt/extsd"
        Log.e("addpic", uri.toString()); 
        ContentResolver cr = Start.context.getContentResolver(); 
        Bitmap myBitmap = null;
        try { 
            Bitmap bitmap = null;
             
            try {
            	 
            	BitmapFactory.Options options = new BitmapFactory.Options();
                
            	 
            	options.inJustDecodeBounds = true;
            	Log.e("fromUrierror", "!!!!!!!!!!!!!!!!!!!!!!!!!" + storagePath + "/calldir/free_" + (pageNum + "/" + uri.getLastPathSegment()));
            	File file = new File(storagePath +"/calldir/free_" + (pageNum + "/" + uri.getLastPathSegment()));
            	if(!file.exists())
            		return null;
            		bitmap = BitmapFactory.decodeStream(new FileInputStream(
                		new File(storagePath + "/calldir/free_" + (pageNum + "/" + uri.getLastPathSegment()))), new Rect(-1,-1,-1,-1), options); //此时返回bm为空
            		BitmapCount.getInstance().createBitmap("CDBPersistent getBitmapFromUri");
//        				new File("/extsd/calldir/free_" + (pageNum + "/" + uri.getLastPathSegment()))), new Rect(-1,-1,-1,-1), options); //此时返回bm为空
                Log.e("error", "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!" + uri.getLastPathSegment());
                Log.e("fromUrierror", "!!!!!!!!!!!!!!!!!!!!!!!!!" + storagePath + "/calldir/free_" + (pageNum + "/" + uri.getLastPathSegment()));
               
                options.inJustDecodeBounds = false;
                
                 
                 //缩放比
                int be = 1;
                if(options.outHeight > 300 || options.outWidth > 300 ){
                    be = options.outHeight / 300;
                    int t = options.outWidth / 300;
                    if(be < t )
                    	be = t;
                }
                 
                options.inSampleSize = be;


                Log.e("FTP", "1bitmap:"+(bitmap == null) + "path:"+"/extsd/calldir/free_" + (Start.getPageNum() + "/" + uri.getLastPathSegment()));
//                Log.e("addpic", "pic path :"+"/extsd/calldir/free_" + (pageNum + "/" + uri.getLastPathSegment()));
                Log.e("addpic", "pic path :"+storagePath +"/calldir/free_" + (pageNum + "/" + uri.getLastPathSegment()));
                
                
                if(file.exists()){
                	Log.e("addpic", "file" + file.getAbsolutePath() + "exit");
	                bitmap=BitmapFactory.decodeStream(
	                		new FileInputStream(
	                				file), 
	                				new Rect(-1,-1,-1,-1),options);
	                BitmapCount.getInstance().createBitmap("CDBPersistent getBitmapFromUri");
	                //D/skia    ( 3558): --- decoder->decode returned false

                }else{
                	Log.e("addpic", "file" + file.getAbsolutePath() + "not exit");
                }
                
                Log.e("FTP", "2bitmap:"+(bitmap == null));
			} catch (OutOfMemoryError o) {
				Log.e("fromUrierror", "!!!!!!!!!!!!!!OOM!!!!!!!!!!!" + storagePath + "/calldir/free_" + (pageNum + "/" + uri.getLastPathSegment()));
				// TODO: handle exception
				Log.e("addpic", "---------decode file failed ");
				bitmap = Start.OOM_BITMAP;
			}
			if(bitmap == null){
				return null;
			}
            
            Log.e("FTP", "3bitmap:"+(bitmap == null));
            if(bitmap.getWidth() < 300 && bitmap.getHeight() < 300){
            	myBitmap = bitmap;
            }else{
            	try {
            		
					myBitmap = Start.createScaledBitmap(bitmap, 280, 280);
				} catch (OutOfMemoryError o) {
					// TODO: handle exception
					Log.e("addpic", "scale bitmap failed ");
					myBitmap = Start.OOM_BITMAP;
				}
            	bitmap.recycle();
            	BitmapCount.getInstance().recycleBitmap("CDBPersistent getBitmapFromUri");	
            }
        }catch (FileNotFoundException e) { 
            Log.e("Exception", e.getMessage(),e); 
        } 
        return myBitmap;
	}
	
	public static String getCurrent() {

		Calendar calender = Calendar.getInstance();

		String minute = "";
		if( calender.get(Calendar.MINUTE) < 10){
			minute = 0 + "" + calender.get(Calendar.MINUTE);
		}else{
			minute = "" + calender.get(Calendar.MINUTE);
		}
			
		String created = calender.get(Calendar.YEAR)%100 + "/"
				+ (calender.get(Calendar.MONTH) + 1) + "/"
				+ calender.get(Calendar.DAY_OF_MONTH) + " "
				+ calender.get(Calendar.HOUR_OF_DAY) + ":"
				+ minute + "";
		return created;
	}
	
	
	public static Bitmap getBitmapFromURI(Uri uri,int width ,int height){
		ContentResolver cr = Start.context.getContentResolver(); 
		if (uri!= null) {
	        BitmapFactory.Options opts = null;
	        if (width > 0 && height > 0) {
	            opts = new BitmapFactory.Options();
	            opts.inJustDecodeBounds = true;
//	            BitmapFactory.decodeFile(dst.getPath(), opts);
	            try {
					BitmapFactory.decodeStream(cr.openInputStream(uri), new Rect(-1,-1,-1,-1), opts);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	            // 计算图片缩放比例
	            final int minSideLength = Math.min(width, height);
	            opts.inSampleSize = computeSampleSize(opts, minSideLength,
	                    width * height);
	            opts.inJustDecodeBounds = false;
	            opts.inInputShareable = true;
	            opts.inPurgeable = true;
	        }
	        try {
//	            return BitmapFactory.decodeFile(dst.getPath(), opts);
	        	return BitmapFactory.decodeStream(cr.openInputStream(uri), new Rect(-1,-1,-1,-1), opts);
	        } catch (OutOfMemoryError e) {
	            e.printStackTrace();
	        } catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	    return null;
	}
	
	public static int computeSampleSize(BitmapFactory.Options options,
	        int minSideLength, int maxNumOfPixels) {
	    int initialSize = computeInitialSampleSize(options, minSideLength,
	            maxNumOfPixels);

	    int roundedSize;
	    if (initialSize <= 8) {
	        roundedSize = 1;
	        while (roundedSize < initialSize) {
	            roundedSize <<= 1;
	        }
	    } else {
	        roundedSize = (initialSize + 7) / 8 * 8;
	    }

	    return roundedSize;
	}

	private static int computeInitialSampleSize(BitmapFactory.Options options,
	        int minSideLength, int maxNumOfPixels) {
	    double w = options.outWidth;
	    double h = options.outHeight;

	    int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math
	            .sqrt(w * h / maxNumOfPixels));
	    int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(Math
	            .floor(w / minSideLength), Math.floor(h / minSideLength));

	    if (upperBound < lowerBound) {
	        // return the larger one when there is no overlapping zone.
	        return lowerBound;
	    }

	    if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
	        return 1;
	    } else if (minSideLength == -1) {
	        return lowerBound;
	    } else {
	        return upperBound;
	    }
	}
}
