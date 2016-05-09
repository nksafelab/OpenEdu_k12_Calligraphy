package com.jinke.single;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import com.jinke.calligraphy.app.branch.Start;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

public class BitmapUtils {
	
	private volatile static BitmapUtils utils;
	public static synchronized BitmapUtils getInstance(){
		if(utils == null){
			synchronized (BitmapUtils.class) {
				if(utils == null)
					utils = new BitmapUtils();
			}
		}
		return utils;
	}
	
	private int computeSampleSize(InputStream stream, int maxResolutionX,
	        int maxResolutionY) {
	        BitmapFactory.Options options = new BitmapFactory.Options();
	        options.inJustDecodeBounds = true;
	        BitmapFactory.decodeStream(stream, null, options);
	        
	        int maxNumOfPixels = maxResolutionX * maxResolutionY;
	        int minSideLength = Math.min(maxResolutionX, maxResolutionY) / 2;
	        return Utils.computeSampleSize(options, minSideLength, maxNumOfPixels);
	    }
	
	public synchronized Bitmap getBitmapFromUri(Uri uri){
		Log.e("time", "getBitmapFromUri called" + uri);
		Bitmap bitmap = null;
		BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false; //如果需要一个非缩放位图的时候，应该关闭。打开时如果inDensity和inTargetDensity不为0，该位图将被缩放，
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inDither = true;
        //Gallery3D 调用方式
//        Uri uri = Uri.parse("file:///mnt/extsd/test1.jpg");
        BufferedInputStream bufferedInput = null;
        
        // Get the input stream for computing the sample size.
        try {
			bufferedInput = new BufferedInputStream(Start.context.getContentResolver().openInputStream(uri));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Compute the sample size, i.e., not decoding real pixels.
		if(bufferedInput != null){
			options.inSampleSize = computeSampleSize(bufferedInput, 1024, 1024);
		}
		
		// Get the input stream again for decoding it to a bitmap.
		bufferedInput = null;
		try {
			bufferedInput = new BufferedInputStream(Start.context.getContentResolver().openInputStream(uri),16384);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Decode bufferedInput to a bitmap.
		if(bufferedInput != null){
			options.inDither = false;
			options.inJustDecodeBounds = false;
			try {
				bitmap = BitmapFactory.decodeStream(bufferedInput, null, options);
				Log.e("getBitmap", "uri:" + uri.toString());
			} catch (OutOfMemoryError e) {
//				options.inSampleSize *= 4;
//				options.inDither = false;
//				options.inJustDecodeBounds = false;
//				try{
//					Log.e("time", "oom uri:" + uri.toString() + (bufferedInput == null));
//					bitmap = BitmapFactory.decodeStream(bufferedInput, null, options);
//					Log.e("time", "decode twice" + (bitmap == null));
//					return bitmap;
//					
//				}catch (OutOfMemoryError ee) {
//					Log.e("time", "error twice");
//					return null;
//				}
				return null;
			}
			Log.e("test", "bitmap width:" + bitmap.getWidth() + " height:" + bitmap.getHeight());
		}
		bufferedInput = null;
		Log.e("time", "getBitmapFromUri called  end" + uri);
		return bitmap;
	}
}
