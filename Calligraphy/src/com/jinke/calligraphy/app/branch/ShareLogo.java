package com.jinke.calligraphy.app.branch;

import com.jinke.single.BitmapCount;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;

public class ShareLogo {
	private volatile static Bitmap logoBitmap;
	
	private ShareLogo() {}
	
	public static Bitmap getInstance() {
		if(logoBitmap == null) {
			synchronized(ShareLogo.class) {
				if(logoBitmap == null) {
//					logoBitmap = BitmapFactory.decodeFile(MyView.FILE_PATH_HEADER + "/logo.png").
//									copy(Bitmap.Config.ARGB_8888, true);
					logoBitmap = BitmapFactory.decodeFile(MyView.FILE_PATH_HEADER + "/logo.png").
									copy(Bitmap.Config.ARGB_4444, true);
					BitmapCount.getInstance().createBitmap("ShareLogo decode logo.png");
				}
			}
		}
		return logoBitmap;
	}
	
	public static Bitmap addLogo(Bitmap dst) {
		Canvas c = new Canvas(dst);
		int xPos = dst.getWidth() - getInstance().getWidth() - 10;
		int yPos = dst.getHeight() - getInstance().getHeight() - 10;
		c.drawBitmap(getInstance(), xPos, yPos, new Paint());
		return dst;
	}
}
