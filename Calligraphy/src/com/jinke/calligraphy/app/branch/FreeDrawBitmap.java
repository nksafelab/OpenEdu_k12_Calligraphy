package com.jinke.calligraphy.app.branch;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.jinke.calligraphy.database.CDBPersistent;
import com.jinke.single.BitmapCount;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

/*
 * 涂鸦态，只需要有一个bitmap即可
 */
/**
 * @author lh
 *
 */
public class FreeDrawBitmap extends BaseBitmap{
	
	private static final String TAG = "FreeDrawBitmap";
	
	public		Bitmap 	mBitmap;    //保持对父类全局变量的引用
    protected 	Bitmap 	bitmap;		//保持对父类中子类独立的变量bitmap的引用
    public 		Paint 	paint;		//保持对父类画笔对象的引用

    private List<FreeBitmapInfo> freeBitmapInfoList;
    private boolean firstSave = false;
	public FreeDrawBitmap(Bitmap b, MyView v){

		super(b, v);

		
		mBitmap = super.mBitmap;

		
		super.bitmap = null;
		bitmap = super.bitmap;
		//ly
		//给FreeDraw状态增加背景图
		//super.bitmap = Bitmap.createBitmap(Start.SCREEN_WIDTH, Start.SCREEN_HEIGHT, Bitmap.Config.ARGB_4444);
		//BitmapCount.getInstance().createBitmap("FreeDrawBitmap bitmap");
		
		
		
		paint = super.paint;	//此处画笔还未赋值，当具体点(硬笔/毛笔)构造时，会给此赋值
		
		bCurInfo = new CurInfo(mBitmap, 0);
		
		freeBitmapInfoList = new ArrayList<FreeBitmapInfo>();
		
		initFreeBitmapInfoList();
		
		//初始化光标态底图bitmap
		//Canvas canvas = new Canvas();
		//canvas.setBitmap(bitmap);
		//canvas.drawBitmap(bitmap, new Rect(0, 0, Start.SCREEN_WIDTH, Start.SCREEN_HEIGHT), new Rect(0, 0, Start.SCREEN_WIDTH, Start.SCREEN_HEIGHT), new Paint());
	}
	
	
	public List<FreeBitmapInfo> getFreeBitmapInfoList(){
		return freeBitmapInfoList;
	}

	public void updateToBitmapList() {
				
		Log.e("in updateFreeBmp","!!!:"+freeBitmapInfoList.size());
		if(TransparentChanged) {
			
			if(mEndX - mStartX > 5 && mEndY - mStartY > 5){
//				if(mEndX > 600)
//					mEndX = 600;
				
				//ly
				if(mEndX>1600)
					mEndX = 1600;
				
				

				
				float addSize = CalliPoint.FILTER_FACTOR + CalliPoint.SIZE_MAX;
				RectF rect = new RectF(mStartX-addSize>0 ? mStartX-addSize : 0, mStartY-addSize > 0 ? mStartY-addSize : 0, mEndX+2*addSize , mEndY+2*addSize);
				Log.i(TAG, rect.toString());
				rect.offset(0, EditableCalligraphy.flip_dst);
				int x = (int)(mStartX-addSize>0 ? mStartX-addSize : 0);
				int y = (int)(mStartY-addSize > 0 ? mStartY-addSize : 0);
				int width = (int)((mEndX - mStartX)+2*addSize);
				int height = (int)((mEndY - mStartY)+2*addSize);
				
				if(x + width > transparentBitmap.getWidth())
					width = transparentBitmap.getWidth() - x;
				if(y + height > transparentBitmap.getHeight())
					height = transparentBitmap.getHeight() - y;
				
				Bitmap bitmap = Bitmap.createBitmap(transparentBitmap, x, y, 
						width, height);
				BitmapCount.getInstance().createBitmap("FreeDrawBitmap updateToBitmapList bitmap");
				
				resetBound();
				freeBitmapInfoList.add(new FreeBitmapInfo(bitmap, rect));
				Log.i(TAG, freeBitmapInfoList.toString());
				
				transparentBitmap.eraseColor(Color.TRANSPARENT);
			}
	    	TransparentChanged = false;
		}
	}
	
	//ly
	//用于把背景图加入到List中
	public void addBgPic(Bitmap bitmap)
	{
		if(freeBitmapInfoList.size()!=0)
			return;
		float ratio = bitmap.getWidth() / (float)1600;
		RectF rect = new RectF(0, 0, 1600, (int)(bitmap.getHeight()/ratio));
		freeBitmapInfoList.add(new FreeBitmapInfo(bitmap, rect));
	}
	//end
	
    public void updateTransparentInFreeDragMode() {
    	if(TransparentChanged) {
        	Log.i(TAG, "TransparentChanged");
        	
        	Canvas c = new Canvas();
        	
        	int posY = (EditableCalligraphy.flip_dst > BaseBitmap.TITLE_HEIGHT )? BaseBitmap.TITLE_HEIGHT :
				EditableCalligraphy.flip_dst;
        	c.setBitmap(this.mBitmap);
    		c.drawBitmap(transparentBitmap, new Rect(0, 0, Start.SCREEN_WIDTH, Start.SCREEN_HEIGHT), new Rect(0, posY, Start.SCREEN_WIDTH, Start.SCREEN_HEIGHT + posY), new Paint());
    		
        	/*
        	if(EditableCalligraphy.flip_dst <= Start.SCREEN_HEIGHT) {
            	c.setBitmap(bView.mBitmap);
            	c.drawBitmap(transparentBitmap, new Rect(0, 0, Start.SCREEN_WIDTH, Start.SCREEN_HEIGHT - EditableCalligraphy.flip_dst),
                                    new Rect(0, EditableCalligraphy.flip_dst, Start.SCREEN_WIDTH, Start.SCREEN_HEIGHT), new Paint());
            	c.setBitmap(BaseBitmap.addBitmapList.get(0));
            	c.drawBitmap(transparentBitmap, new Rect(0, Start.SCREEN_HEIGHT - EditableCalligraphy.flip_dst, Start.SCREEN_WIDTH, Start.SCREEN_HEIGHT),
                                    new Rect(0, 0, Start.SCREEN_WIDTH, EditableCalligraphy.flip_dst), new Paint());
        	} else {
        		c.setBitmap(BaseBitmap.addBitmapList.get(0));
            	c.drawBitmap(transparentBitmap, new Rect(0, 0, Start.SCREEN_WIDTH, Start.SCREEN_HEIGHT - (EditableCalligraphy.flip_dst - Start.SCREEN_HEIGHT)),
                                    new Rect(0, EditableCalligraphy.flip_dst - Start.SCREEN_HEIGHT, Start.SCREEN_WIDTH, Start.SCREEN_HEIGHT), new Paint());
            	c.setBitmap(BaseBitmap.addBitmapList.get(1));
            	c.drawBitmap(transparentBitmap, new Rect(0, Start.SCREEN_HEIGHT - (EditableCalligraphy.flip_dst - Start.SCREEN_HEIGHT), Start.SCREEN_WIDTH, Start.SCREEN_HEIGHT),
                                    new Rect(0, 0, Start.SCREEN_WIDTH, EditableCalligraphy.flip_dst - Start.SCREEN_HEIGHT), new Paint());
        	}
        	*/
        	transparentBitmap.eraseColor(Color.TRANSPARENT);
        }
    }

	
	class FreeBitmapInfo {
		
		Bitmap bitmap;
		RectF rect;
		
		public FreeBitmapInfo(Bitmap bitmap, RectF rect) {
			this.bitmap = bitmap;
			this.rect = rect;
		}

		@Override
		public String toString() {
			return "FreeBitmapInfo [bitmap size =" + bitmap.getWidth() + "," + bitmap.getHeight() 
					+ ", rect=" + rect.toString() + "]";
		}
		
	}
	
	private void initFreeBitmapInfoList(){
		File dir = new File(MyView.FILE_PATH_HEADER + "/calldir/free_" + Start.getPageNum() + "/");
		Log.e("rect", "free path:" + MyView.FILE_PATH_HEADER + "/calldir/free_" + Start.getPageNum() + "/");
		Log.i(TAG, "initFreeBitmapInfoList dir path:"+dir.getAbsolutePath());
		CDBPersistent db = new CDBPersistent(bView.getContext());
		db.open();
		int pagenum = Start.getPageNum();
		boolean exist = db.isPageExist(pagenum);
		db.close();
		Log.i(TAG, "initFreeBitmapInfoList db exist:"+exist);
		if(!exist)
			firstSave = true;
		else
			firstSave = false;
		if(dir.isDirectory() && exist){
			Log.i(TAG, "initFreeBitmapInfoList dir path exist.");
			File[] rects = dir.listFiles(new FilenameFilter() {
				
				@Override
				public boolean accept(File dir, String filename) {
					// TODO Auto-generated method stub
					return filename.contains("_r");
				}
			});
//			File[] frees = dir.listFiles(new FilenameFilter() {
//				
//				@Override
//				public boolean accept(File dir, String filename) {
//					// TODO Auto-generated method stub
//					return filename.contains("png") && filename.contains("free_");
//				}
//			});
			Log.e("rect", "rects.length:" + rects.length);
//			Log.e("rect", "frees.length:" + frees.length);
//			if(rects.length != frees.length)
//				return;
			FreeBitmapInfo info;
			for(int i=rects.length - 1;i >= 0 ;i--){
				Log.e("rect", "i " + i + " rectName:" + rects[i].getName());
//				Log.e("rect", "i " + i + " freeName:" + frees[i].getName());
				Log.e("rect", "absult" + rects[i].getAbsolutePath());
				try {
					
					String temp = rects[i].getAbsolutePath();
					temp = temp.substring(0, temp.length() - 2);
					Bitmap b = BitmapFactory.decodeStream(new FileInputStream(temp + ".png"));
					BitmapCount.getInstance().createBitmap("FreeDrawBitmap initFreeBitmapInfoList");
					
					RectF f = getRectf(rects[i]);
					info = new FreeBitmapInfo(b, f);
					freeBitmapInfoList.add(info);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (OutOfMemoryError e){
					
				}
				
			}
			
			
		}
	}
	/**
	 * 换页时候使用，更换涂鸦内容
	 * 释放之前资源，回收内存
	 */
	public void resetFreeBitmapList(){
		
		for(int i=0;i<freeBitmapInfoList.size();i++){
			if(!freeBitmapInfoList.get(i).bitmap.isRecycled()){
				freeBitmapInfoList.get(i).bitmap.recycle();
				BitmapCount.getInstance().recycleBitmap("FreeDrawBitmap resetFreeBitmapList bgBitmap");
			}
		}
		freeBitmapInfoList.clear();
		System.gc();
		
		initFreeBitmapInfoList();
		
		
		
	}
	
	private RectF getRectf(File f){
		FileInputStream in;
		float[] floats = new float[4];
		try {
			in = new FileInputStream(f);
			byte[] tmp = new byte[256];
			int length = in.read(tmp);
			String rect = new String(tmp, 0, length);
			Log.e("rect", "read:" + rect);
			rect = rect.substring( 6 , rect.length()-1);
			String[] fs = rect.split(",");
			
			for(int j=0;j<fs.length;j++){
				floats[j] = Float.parseFloat(fs[j]);
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return (new RectF(floats[0], floats[1], floats[2], floats[3]) );
	}
	
	public void clearFreeDrawHistory()
	{

		if(firstSave)
		{
			File dir = new File(MyView.FILE_PATH_HEADER + "/calldir/free_" + Start.getPageNum() + "/");
			Log.i(TAG, "clearFreeDrawHistory dir path:"+dir.getAbsolutePath());

			/* remove history free draw bitmap */
			File[] rects = dir.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String filename) {
					// TODO Auto-generated method stub
					return filename.contains("_r");
				}
			});
			for(int i=rects.length - 1;i >= 0 ;i--){
				Log.e(TAG, "i " + i + " rectName:" + rects[i].getName());
//					Log.e("rect", "i " + i + " freeName:" + frees[i].getName());
				Log.e(TAG, "absult path:" + rects[i].getAbsolutePath());
				try {
					
					String temp = rects[i].getAbsolutePath();
					temp = temp.substring(0, temp.length() - 2) + ".png";
					Log.e(TAG, "temp:" + temp);
					rects[i].delete();
					File bmpFile = new File(temp);
					bmpFile.delete();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (OutOfMemoryError e){
				}
			}
			firstSave = false;
		}
	}
	public void drawFreeBitmapSync(){
		Canvas canvas = new Canvas();
		canvas.setBitmap(mBitmap);
		List<FreeBitmapInfo> list = getFreeBitmapInfoList();
		float x,y;
		Bitmap infoBitmap;
		Log.i(TAG, "drawFreeBitmapSync list size:"+list.size());
		for(FreeBitmapInfo info : list){
			infoBitmap = info.bitmap;
			

			//x = info.rect.left + Start.SCREEN_WIDTH;
			//ly
			//涂鸦态的文字为什么要加一个屏幕宽度呢？
			x = info.rect.left;
			//end
			
			Log.e("!!!!!!before!!!!!!","xxx:"+x+"yyyy:"+info.rect.top+"flip_dst"+EditableCalligraphy.flip_dst);
			
			if(EditableCalligraphy.flip_dst > TITLE_HEIGHT)
				y = info.rect.top - EditableCalligraphy.flip_dst + TITLE_HEIGHT;
			else
				y = info.rect.top - EditableCalligraphy.flip_dst;
			
			Log.e("!!!!!!!!!!!!","xxx:"+x+"yyyy:"+y+"flip_dst"+EditableCalligraphy.flip_dst);
			
			canvas.drawBitmap(infoBitmap, x, y, new Paint());
		}
	}
}
