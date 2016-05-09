package com.jinke.calligraphy.app.control;

import hallelujah.cal.CalligraphyVectorUtil;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Log;

import com.jinke.calligraphy.app.branch.Start;
import com.jinke.calligraphy.app.branch.VEditableCalligraphyItem;
import com.jinke.calligraphy.app.branch.EditableCalligraphyItem.Types;
import com.jinke.single.BitmapCount;

public class ResetBitmapWork implements Runnable{
		VEditableCalligraphyItem e = null;
		String identity = "";
		public ResetBitmapWork(VEditableCalligraphyItem e, String iden){
			this.e = e;
			this.identity = iden;
//			workList.add(iden);
//			WorkQueue.getInstance().addIdentify(iden);
		}
		
		@Override
		public boolean equals(Object o) {
			// TODO Auto-generated method stub
			return identity.equals(((ResetBitmapWork)o).identity);
		}
		
		@Override
		public void run() {
			
			execute();
		}
		
		public VEditableCalligraphyItem execute(){

			// TODO Auto-generated method stub
			
			Log.e("workqueue", "resetBitmapWork:" + identity);
			
			Path mPath = null;
			Paint mPaint = new Paint();
			
			mPaint.setColor(e.getmColor());
			mPaint.setStyle(Paint.Style.STROKE);
			mPaint.setStrokeWidth(2);
			Path tmpPath;
			Bitmap tempB;
			float Right_X,Left_X,Bottom_Y,Top_Y;
			
				if(e.type == Types.CharsWithStroke){
					Log.e("vector", "vitem mpath>>>>>>>>>>>>>>>>>>>\n:" + e.getmPath() + " Bottom_Y:" + e.getBottom_Y());
					
					tempB = e.getCharBitmap();
					if(tempB != null && !tempB.isRecycled() && tempB != Start.OOM_BITMAP){
						e.recycleBitmap();
						BitmapCount.getInstance().recycleBitmap("EditableCalligraphy VEditableCalligraphyItem execute tempB");
					}
					
					mPath = e.getmPath();
					Right_X = e.getRight_X();
					Left_X = e.getLeft_X();
					Top_Y = e.getTop_Y();
					Bottom_Y = e.getBottom_Y();
					
					Matrix currentMatrix;
					if(Start.c == null)
						currentMatrix = Start.m;
					else
						currentMatrix = Start.c.view.getMMMatrix();
					
					float[] values = new float[9];
					currentMatrix.getValues(values);
					
					int startx = (int)(Left_X + 1);
					int endx = (int)(Right_X + 1);
					int starty = (int)(Top_Y + 1);
					int endy = (int)(Bottom_Y + 1);
					
					startx = startx < 0 ? 0 : startx;
					starty = starty < 0 ? 0 : starty;
					int distX = endx - startx;
					int distY = endy - starty;
					if(distX <=0 || distY <= 0)
						return e;
					Log.e("pathscale", "-->distx:" + distX + " distY:" + distY);
					
					float scale = CalligraphyVectorUtil.getScaled(Right_X - Left_X ,Bottom_Y - Top_Y);
					scale *= values[0];
					Log.e("cursorScale", "---------scale:" + scale);
					float height = Bottom_Y - Top_Y;
					
//					Log.e("vectorScal", "scale:" + values[0]);
//					Log.e("", "End create Bitmap:>>>>>>>>>>>>>>" + (mPath == null));
					tmpPath = new Path(mPath);
//					Log.e("", "End create Bitmap:>>>>>>>>>>>>>>" );
					
					Bitmap b;
					try {
						int sizeX=(int)((endx - startx)*scale);
						int sizeY=(int)((endy - starty)*scale);
						if(sizeX <0)
							sizeX *= -1;
						else if(sizeX == 0)
							sizeX = 1;
						if(sizeY <0)
							sizeY *= -1;
						else if(sizeY == 0)
							sizeY = 1;
						b = Bitmap.createBitmap(sizeX, sizeY, Bitmap.Config.ARGB_8888);
						BitmapCount.getInstance().createBitmap("EditableCalligraphy VEditableCalligraphyItem excute");
						
						b.eraseColor(Color.TRANSPARENT);
						Canvas mc = new Canvas();
						mc.setBitmap(b);
						
						Matrix scaleMatrix = new Matrix();
						scaleMatrix.setScale(scale, scale);
						tmpPath.offset(-Left_X, -Top_Y);
						tmpPath.transform(scaleMatrix);
						
						mc.drawPath(tmpPath, mPaint);
						
						tmpPath.reset();
						
					} catch (OutOfMemoryError e) {
						// TODO: handle exception
						b = Start.OOM_BITMAP;
					}
					
					
//					e.charBitmap = b;
					e.setCharBitmap(b);
					e.resetWidthHeight();
					e.setMatrix(currentMatrix);
					
				}
				Log.e("sharerecycle", "execute recycled" + e.getCharBitmap().isRecycled());
				return e;
		}
}
