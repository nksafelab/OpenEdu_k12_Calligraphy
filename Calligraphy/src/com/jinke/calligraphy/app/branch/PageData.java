package com.jinke.calligraphy.app.branch;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.util.Log;

public class PageData {

	private static final String TAG = "PageData";
	private static PageData currentPageData;
	//others 
	
	private PageData(){
		
	}
	
	public static PageData getInstance(){
		
		if(currentPageData == null){
			currentPageData = new PageData();
		}
		return currentPageData;
	}
	
	public boolean savePageData(){
		
		String charPath = Start.getStoragePath() + "/calldir/free_" + Start.PAGENUM + "/chars";
		File charDir = new File(charPath);
		if(!charDir.exists()){
			if(charDir.mkdirs()){
				Log.e(TAG, "mkdir:" + charPath + " success");
			}else{
				Log.e(TAG, "mkdir:" + charPath + " failed");
				return false;
			}
		}else{
			//删除所有文件
			File[] chars = charDir.listFiles();
			for(File f : chars){
				f.delete();
			}
		}
		byte[] buf = new byte[1024];
		Bitmap charBitmap;
		CompressFormat format = Bitmap.CompressFormat.PNG;
		OutputStream out = null;
		int availableid = 0;
		int itemid = 0;
		File charFile = null;
		//保存当前页数据到本地，以文件的形式。
		List<EditableCalligraphy> editList = CursorDrawBitmap.listEditableCalligraphy;
		EditableCalligraphy edit;
		for(int i=0;i<editList.size(); i++){
			edit = editList.get(i);
			availableid = edit.getID();
			LinkedList<EditableCalligraphyItem> charList = edit.charList;
			EditableCalligraphyItem item;
			for(int j=0;j<charList.size();j++){
				itemid = j;
				item = charList.get(j);
				charBitmap = item.charBitmap;
				charFile = new File(charPath + "/char_a" + availableid + "i" + itemid + ".png");
				try {
					out = new BufferedOutputStream(new FileOutputStream(charFile));
					if(charBitmap == null)
						continue;
					charBitmap.compress(format, 80, out);
					
					// 0-100. 0 meaning compress for small 
//					 size, 100 meaning compress for max quality. Some formats, like PNG which is lossless, will ignore the quality setting 
//					 stream The outputstream to write the compressed data
					out.flush();
					out.close();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					Log.e(TAG, "filenotfound", e);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					Log.e(TAG, "filenotfound", e);
				}
				
			}
			
		}//end for
		return true;
	}
	
}
