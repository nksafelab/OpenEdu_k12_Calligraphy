package com.jinke.calligraphy.app.control;

import android.graphics.Bitmap;
import android.util.Log;

import com.jinke.calligraphy.app.branch.EditableCalligraphyItem;
import com.jinke.single.BitmapUtils;

public class ResetImageWork implements Runnable{
		EditableCalligraphyItem e = null;
		public String identity = "";
		public ResetImageWork(EditableCalligraphyItem e, String iden){
			this.e = e;
			this.identity = iden;
		}
		
		@Override
		public boolean equals(Object o) {
			// TODO Auto-generated method stub
			
			return identity.equals(((ResetImageWork)o).identity);
		}
		
		@Override
		public void run() {
			Log.e("resetImage", identity + " begin execute------------------");
			execute();
			
			Log.e("resetImage", identity + " end execute");
		}
		
		public EditableCalligraphyItem execute(){
			
			// TODO Auto-generated method stub
			Log.e("resetImage", "resetBitmapWork:" + identity + " :" + e.getImageUri() + "--------------");
			Bitmap bitmap = BitmapUtils.getInstance().getBitmapFromUri(e.getImageUri());
			Log.e("resetImage", "resetBitmapWork:" + identity + " :" + e.getImageUri() + "after getBitmapFromUri--------------");
			
			e.resetCharBitmap(bitmap , null,null);
			if(bitmap != null){
				e.setNomalStatus("reset success " + identity);
				Log.e("resetImage", "bitmap != null " + identity + " nomral");
			}
			else{
				Log.e("resetImage", "bitmap == null not nomalStatus " + identity);
				e.setRecycleStatus("reset failed " + identity);
			}
	        return e;
		}
}