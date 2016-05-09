package com.jinke.calligraphy.app.control;

import java.util.LinkedList;

import com.jinke.calligraphy.app.branch.BaseBitmap;
import com.jinke.calligraphy.app.branch.CursorDrawBitmap;
import com.jinke.calligraphy.app.branch.EditableCalligraphy;
import com.jinke.calligraphy.app.branch.EditableCalligraphyItem;
import com.jinke.calligraphy.app.branch.MyView;
import com.jinke.calligraphy.app.branch.Start;
import com.jinke.calligraphy.app.branch.VEditableCalligraphyItem;
import com.jinke.calligraphy.app.branch.WorkQueue;
import com.jinke.calligraphy.app.branch.EditableCalligraphyItem.ItemStatus;
import com.jinke.calligraphy.app.branch.EditableCalligraphyItem.Types;
import com.jinke.calligraphy.template.Available;
import com.jinke.calligraphy.touchmode.HandWriteMode;
import com.jinke.mindmap.MindMapItem;
import com.jinke.single.LogUtil;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.Log;

public class OrientationUpdate implements UpdateInterface{
	public static final String TAG = "OrientationUpdate";
	static int VMargin = 0;//竖版，字间距
	static int HMargin = 4;
	
	EditableCalligraphy currentEditor = null;
	Canvas c = null;
	Paint p = null;
	Available available = null; 
	MyView myView = null;
	
//	int currentpos = 0;
	int start_x = 0;
	int start_y = 0;
	int end_x = 0;
	int end_y = 0;
	int linespace = 0;
	
	
	public OrientationUpdate(EditableCalligraphy editor,Available ava,MyView myView){
		this.currentEditor = editor;
		this.available = ava;
		this.myView = myView;
		c = new Canvas();
		p = new Paint();
		
		start_x = this.available.getStartX()+Start.SCREEN_WIDTH;
		start_y = this.available.getStartY();
		end_x = this.available.getEndX()+Start.SCREEN_WIDTH;
		end_y = this.available.getEndY();
		
		linespace = this.available.getAlinespace();
	}
	
	int x = 0;
	int y = 0;
	
	int lastLineStartID = 0;
	int recycle_lastIndex = 0;
	int recycle_firstIndex = Integer.MAX_VALUE;
	
	int mindMapBeginY = 0;
	
	boolean bottomflag = true;
	boolean drawMapBegin = false;
	boolean recycleable = false;
	
	public float[] values = new float[9];
	float[] recycleValues = new float[9];
	float scale = 0f;
	
	MindMapItem mapItem = null;
	Bitmap tmp = null;
	
	@Override
	public void update(Bitmap m, boolean flip,LinkedList<EditableCalligraphyItem> charList) {
//		Start.kanboxUploadHandler.removeMessages(1);
//		Start.kanboxUploadHandler.sendEmptyMessageDelayed(1, (long)(Start.auto_upload_time * 60 * 1000));
		
		Log.e(TAG, "update");
		lastLineStartID = 0; 
		bottomflag = true;
		drawMapBegin = false;
		recycleable = false;
		
		recycle_lastIndex = 0;
		recycle_firstIndex = 10000;
		
		Matrix matrix;
		if(Start.c == null)
			matrix = Start.m;
		else
			matrix = Start.c.view.getMMMatrix();
		
		
//		Paint p = new Paint();  //mv to conductor
		p = new Paint();
		matrix.getValues(values);
		scale = values[0];
//		currentEditor.resetRecycleLimit(scale);
		
		float dScale ;//应该显示的缩放比例
		float width = 0.0f;
		
//		this.mb = m;
//		c = new Canvas(); //mv to conductor
		c.setBitmap(m);
		
		x=start_x;
		y=start_y;
		
		if(Available.AVAILABLE_NUMBER.equals(available.getControltype())){
			int tap = (linespace - available.getAfontsize())/2;
			y = end_y - tap;
			p.setTextSize(available.getAfontsize());
			c.drawText("共 " + myView.getRowNumber() + " 行", x, y, p);
			return;
		}
		
		float tt = 0;
		float maxHeight=0;
		
		if(charList.size() != 0){
			myView.addRowNumber(available.getControltype());
		}
		
		for(int i =0; i < charList.size() ; i++){
			
			Log.e(TAG, "i:" + i);
			
			String iden = "a" + available.getAid() + "i" + i;
			matrix.getValues(values);
			scale = values[0];
			dScale = scale;
			
			EditableCalligraphyItem e = null;
			
			try {
				e = charList.get(i);
			} catch (ClassCastException e2) {
				// TODO: handle exception
				Log.e("audio", "audioException",e2);
				continue;
			}
			e.getMatrix().getValues(values);
			
			if(EditableCalligraphyItem.getType(e.getCharType()) == Types.CharsWithoutStroke || 
					EditableCalligraphyItem.getType(e.getCharType()) == Types.AUDIO ||
					EditableCalligraphyItem.getType(e.getCharType()) == Types.VEDIO){
				dScale /= values[0];    //当前缩放比例，除以字体生成时的缩放比例，得到应该显示的缩放比例
			}else if(e.type == Types.ImageItem){
				dScale = values[0];
			}
			else{
				dScale = 1;//矢量字体，不需要缩放
			}
			
			tt = values[0];//字体生成时的缩放比例
//			if(maxHeight < (linespace + VMargin)*scale){
//				maxHeight = (linespace + VMargin)*scale;
//			}
			if(maxHeight < linespace*scale){
				maxHeight = linespace*scale;
			}

			if(e.getWidth()  > end_x - x && x != start_x){
				if(available.getLinenumber() != 1){
					x = start_x;
					y += maxHeight;
					Log.e(TAG, "next line: y:" + y + " maxHeight:" + maxHeight);
					maxHeight=0;
					myView.addRowNumber(available.getControltype());
					//记录到光标为止，行首字的id
					if(i < currentEditor.currentpos){
						lastLineStartID = i + 1;
						Log.e("mindmap", "newline lastLineStartID set:" + lastLineStartID);
					}
				}else{
					Log.e(TAG, "only one line");
					continue;
				}
			}
			if(e.getWidth() > end_x - start_x && x != start_x)
			{
				continue;
			}
			if(EditableCalligraphyItem.getType(e.getCharType()) == Types.ImageItem){
				if(maxHeight < e.getHeight() * dScale){
					maxHeight = e.getHeight() * dScale;
					int tem = 0;
					if(maxHeight % (int)(linespace * scale) != 0){
						tem = (int) (maxHeight / (int)(linespace * scale));
						tem ++;
						maxHeight = tem * linespace * scale;
					}
				}
			}
			if(i == currentEditor.currentpos && !drawMapBegin){
				//光标被改变到此位置。 绘制光标
				if(myView.getTouchMode() instanceof HandWriteMode  &&
						((HandWriteMode)myView.getTouchMode()).isMindMapEditableStatus()){
					//如果是正在编辑导图状态，光标不置于末尾
				}else{
//					currentEditor.setCursorXY(x, y);
					currentEditor.dispearPreCursor();
				}
			} 
			if(e.type == EditableCalligraphyItem.Types.EndofLine){
				e.setCurPos(x, y);
				x = start_x;
				y += maxHeight;
				maxHeight=0;
				myView.addRowNumber(available.getControltype());
				
				//记录到光标为止，行首字的id
				if(i < currentEditor.currentpos){
					lastLineStartID = i + 1;
					Log.e("mindmap", "endofline lastLineStartID set:" + lastLineStartID + " currentPos:" + currentEditor.currentpos);
				}
				drawMapBegin = false;
				LogUtil.getInstance().e("drawmindmap", "end of line -------- y:" + y);
				continue;
			}
			
			//画导图
			if(e.isSpecial()){
				mapItem = e.getMindMapItem();
				
				if(mapItem.hasParent()){
					LogUtil.getInstance().e("drawmindmap", "mindmapitem mindid:" + mapItem.getMindID() + " hasParent:" 
							+ mapItem.hasParent() + " parentid:" + mapItem.getParentID());
					continue;
				}
				
				if(!mapItem.isFirst(e)){
					LogUtil.getInstance().e("drawmindmap", "mindmapitem mindid:" + mapItem.getMindID() + " continue");
					continue;
				}
				
				if(drawMapBegin){
					LogUtil.getInstance().e("drawmindmap", "mindmapitem continue");
					//已经进入该副导图排版过程,跳过导图内的所有字
					continue;
				}
				//遇到该导图的第一个字,取出对应的导图引用，开始排版绘制每一个字
				drawMapBegin = true;
				mindMapBeginY = y;
//				currentEditor.setMindmapBeginY(mindMapBeginY);
				LogUtil.getInstance().e("drawmindmap", "mindmapitem:" + e.getMindMapItem().getMindID() + " start update y:" + y);
				currentEditor.updateMindMap(mapItem, p,x+mapItem.getFlipDstX(),true);
//				i+= 13;
			}else{
			
				 if(e.type != EditableCalligraphyItem.Types.Space && e.type != EditableCalligraphyItem.Types.EnSpace){
					//画所有的字
					
					 tmp = e.getCharBitmap();
						float pad = 0.0f;
						if(available.getAlinespace() != 0){
							CursorDrawBitmap.mIntervalHeight = available.getAlinespace();
						}
						if(Available.AVAILABLE_SUBJECT.equals(available.getControltype())){
							tt = 1;
						}
						if(e.getHeight()/tt < CursorDrawBitmap.mIntervalHeight) {
							pad = (CursorDrawBitmap.mIntervalHeight  - 
									e.getHeight()/tt)  /1.2f;
	//						Log.e("pad", "i:" + i + "e.getHeight():" + e.getHeight() + 
	//								" mIntervalHeight:" + CursorDrawBitmap.mIntervalHeight
	//								+ " tmp.getHeight():" + tmp.getHeight()
	//								+ " pad:" + pad
	//								+ " tt:" + tt
	//								+ " available.getControltype():" + available.getControltype());
						}
						if(e.type == Types.ImageItem){
							if(e.getWidth()* dScale > Start.SCREEN_WIDTH)
								x += e.getFlipDstX();
						}
						Matrix mm = new Matrix();
						LogUtil.getInstance().e("updateflip", "flipdst:" + currentEditor.flip_dst);
						if(currentEditor.flip_dst < BaseBitmap.TITLE_HEIGHT){
							mm.postTranslate(x,y+pad);//test -70
						}else{
							int t = y+ (int)pad - currentEditor.flip_dst + BaseBitmap.TITLE_HEIGHT;
							mm.postTranslate(x,t);//test -70
						}
						if(e.type == Types.CharsWithoutStroke || 
								e.type == Types.AUDIO ||
								e.type == Types.VEDIO ||
								e.type == Types.ImageItem){
							mm.preScale(dScale, dScale);
						}
						
						mm.getValues(recycleValues);
						if(recycleValues[5] + e.getHeight()*dScale < recycle_line){
							//因为有BaseBitmap.TITLE_HEIGHT; 在超出屏幕BaseBitmap.TITLE_HEIGHT时被释放；
							recycleable = true;
							recycle_lastIndex = i;
							//如果是图片，立即释放
							if(e.type == Types.ImageItem){
	//							Log.e("test", "image recycle from top" + iden);
								if(e.itemStatus == ItemStatus.NORMAL){
									e.recycleBitmap();
									e.setRecycleStatus("recycle top " + iden);
								}
							}
						}else if(recycleValues[5] > recycle_bottom_line ){
							if(bottomflag){
									recycleable = true;
									recycle_firstIndex = i;
									bottomflag = false;
							}
							//如果是图片，立即释放
							if(e.type == Types.ImageItem){
								if(e.itemStatus == ItemStatus.NORMAL){
									e.recycleBitmap();
									e.setRecycleStatus("recycle bottom " + iden);
								}
							}
						}
						else{
							if(tmp == null || ((tmp != null) && tmp.isRecycled())){
								boolean isInsert = false;
									try {
										if(e.type == EditableCalligraphyItem.Types.CharsWithStroke){
											WorkQueue.getInstance().
											execute(new ResetBitmapWork((VEditableCalligraphyItem)e 
													,iden));
										}else if(e.type == Types.ImageItem){
											if(e.itemStatus == ItemStatus.RECYCLED){
												isInsert = WorkQueue.getInstance().executeImage(new ResetImageWork(e,iden));
											}
										}
									} catch (ClassCastException e2) {
										Log.e(TAG, "addtoworkqueue", e2);
									}
								//java.lang.ClassCastException: com.jinke.calligraphy.app.branch.EditableCalligraphyItem
								LogUtil.getInstance().e("empty", "empty draw");
								c.drawBitmap(Start.EMPTY_BITMAP, mm, p);
							}else{
								c.drawBitmap(tmp, mm, p);
								Log.e(TAG, "draw bitmap");
							}
						}
				}//end  if
				drawMapBegin = false;
				if(e != null){
				if(e.type == Types.ImageItem)
					Log.e("ispic", "setX:" + x + " setY:" + y);
				e.setCurPos(x, y);
				x += e.getWidth()*dScale  + HMargin;
				}
	//			x += e.getWidth()  + HMargin;
			}
			
		}//end for
		if(flip){
			currentEditor.startRecycleInVisiableBitmap();
		}
		if( currentEditor.currentpos >= charList.size()){
			if(myView.getTouchMode() instanceof HandWriteMode  &&
					((HandWriteMode)myView.getTouchMode()).isMindMapEditableStatus()){
				//如果是正在编辑导图状态，光标不置于末尾
			}else{
//				currentEditor.setCursorXY(x, y);
				currentEditor.dispearCurrentCursor();
			}
		}
		
		if(available.getAid() == 4){
//			currentEditor.bottomY = y + (int)maxHeight;
			currentEditor.setFlipDst(false,"update");//绕圈儿鸟
//			LogUtil.getInstance().e("buttomy", "buttomY:" + currentEditor.bottomY);
		}
		
//		currentEditor.setLastStartID(lastLineStartID);
	
	}


	@Override
	public void setCurrentPos(int pos) {
		// TODO Auto-generated method stub
	}

}
