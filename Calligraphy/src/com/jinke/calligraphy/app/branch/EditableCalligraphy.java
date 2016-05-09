package com.jinke.calligraphy.app.branch;

import hallelujah.cal.CalligraphyVectorUtil;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.jinke.calligraphy.activity.VideoActivity;
import com.jinke.calligraphy.app.branch.EditableCalligraphyItem.ItemStatus;
import com.jinke.calligraphy.app.branch.EditableCalligraphyItem.Types;
import com.jinke.calligraphy.database.CDBPersistent;
import com.jinke.calligraphy.database.CalligraphyDB;
import com.jinke.calligraphy.template.Available;
import com.jinke.calligraphy.template.WolfTemplateUtil;
import com.jinke.calligraphy.touchmode.HandWriteMode;
import com.jinke.mindmap.DrawLineOperate;
import com.jinke.mindmap.DrawOutLineHalfLeft;
import com.jinke.mindmap.DrawOutLineHalfRight;
import com.jinke.mindmap.DrawOutLineHalfRound;
import com.jinke.mindmap.DrawOutLineOperate;
import com.jinke.mindmap.LineStyle;
import com.jinke.mindmap.MindMapItem;
import com.jinke.mindmap.OutLineStyle;
import com.jinke.mywidget.FlipImageView;
import com.jinke.single.BitmapCount;
import com.jinke.single.BitmapUtils;
import com.jinke.single.LogUtil;

public class EditableCalligraphy implements Command{
	LinkedList<EditableCalligraphyItem> charList;
	List mindMapList;
	
	private static final String TAG = "EditableCalligraphy";
	private static  int THREAD_NUM = 3;
	private static  int RECYCLE_LIMIT = 150;
	
	private int id;
	public int currentpos;
	private int count = 0;//要在构造函数初始化
	
	public static int HMargin = 4;
	public static int basePad = 0;
	public static boolean BY_WIDTH = false;
	static int VMargin = 0;//竖版，字间距
	static int bitmapOffsetX=0;
	static int bitmapOffsetY=0;
	
	public int start_x = 0;
	public int start_y = 0;
	public int end_x = 0;
	public int end_y = 0;
	
	public static int flip_dst = 0;//init flip distance 0
	public static int flip_Horizonal_dst = 0;//init flip distance 0
	public static int flip_Horizonal_bottom = Start.SCREEN_WIDTH;//init flip distance 0
	
	//ly
	public static int flip_bottom = 1900;//init flip distance 0

	
	public int linenumber = 0;
	public Canvas add_c = new Canvas();
	public boolean selected;
	int linespace = 0;
	
	private static int bottomY = 0;	//针对于每个编辑区的，用于记录底部的变量
	//private static final int FLIPSTART_LIMIT_BOTTOM = 650;//记录超过多少可以滑动
	private static final int FLIPSTART_LIMIT_BOTTOM = 2000;//记录超过多少可以滑动
	private static final int FLIPSTART_LIMIT_HBOTTOM = 590;//记录超过多少可以滑动
	private static int bottomHX = 0;
	
	//for undo 
	
	public static int TYPE_DEL = 1;
	public static int TYPE_SPACE = 2;
	public static int TYPE_NEWLINE = 3;
	public static int TYPE_INSERT = 4;
	public static int TYPE_ENSPACE = 6;
	public static int TYPE_UNKNOWN = 5;
	public int type = TYPE_UNKNOWN;
	public EditableCalligraphyItem undoItem;
	
	public float[] values = new float[9];
	
	public final static int cursorMargin = 25;
	
	
	
	/**
	 * 根据滑动距离，计算出偏移量 flip distance
	 * 只在滑动的时候调用
	 * @param distance
	 */
	public  void setFlip_dst(int distance){
		if(available.getDirect() == 1){
			Calligraph.flipblockBtn.setVisibility(View.GONE);
			return;
		}
		if(flip_bottom == 0){
			flip_bottom = FLIPSTART_LIMIT_BOTTOM;
			Calligraph.flipblockBtn.setVisibility(View.GONE);
		}
		//滑块儿滑动的距离
		float scale = distance/(float)FLIPSTART_LIMIT_BOTTOM;
		flip_dst = (int) ((bottomY - FLIPSTART_LIMIT_BOTTOM) * scale);
		if(flip_dst <0)
			flip_dst = 0;
//		Log.e("flipset", "flip_dst:" + flip_dst);
	}
	
	
	public static void set_Horizonal_Flip_dst(int distance){
//		Log.e("layout", "flip distance ="+distance);
//		Log.e("layout", "flip bottom ="+flip_bottom);
//		if(flip_bottom == 0){
//			flip_bottom = 800;
//			Calligraph.flipblockBtn.setVisibility(View.GONE);
//		}
//		//滑块儿滑动的距离
//		float scale = distance/800f;
//		Log.e("layout", "flip scale ="+scale);	
//		flip_dst = (int) ((flip_bottom - 800) * scale);
		Log.e("dst", "flip_Horizonal_bottom:"+flip_Horizonal_bottom);
		if(flip_Horizonal_bottom == 0){
			flip_Horizonal_bottom = Start.SCREEN_WIDTH;
			Calligraph.flipblockHBtn.setVisibility(View.GONE);
		}
		float scale = distance/(Start.SCREEN_WIDTH * 1.0f);
		
		flip_Horizonal_dst = (int) ((FLIPSTART_LIMIT_HBOTTOM - bottomHX) * scale);
//		flip_Horizonal_dst = distance;
		
		Log.e("layout", "flip_dst:"+flip_dst);
	}
	
	/**
	 * 只在删除的时候调用，通知线程刷新
	 * @param flag
	 */
	public void setFlipDstAndFresh(boolean flag){
		if(flag ){
			WorkQueue.getInstance().endFlipping();
		}
		setFlipDst(flag,"setFlipDstAndFresh");
	}
	
	/**
	 * 根据底部距离，设置滑块是否消失，出现。
	 * 回车，删除
	 * @param flag
	 */
	public void setFlipDst(boolean flag,String where){
		if(available.getDirect() == 1){
			setFlipDst_vertical();
			return;
		}
			Log.e("cursorButtom", "setFlipDst bottomy called from " + where + " bottomY:" + bottomY);
			
			if(bottomY > FLIPSTART_LIMIT_BOTTOM  && 
					(cursor_y-flip_dst > FLIPSTART_LIMIT_BOTTOM || cursor_y-flip_dst < 0)){
				int t = (int)(((float)flip_dst/bottomY) * (Start.SCREEN_HEIGHT -250));
				
				if(flag ){
					if(cursor_y > FLIPSTART_LIMIT_BOTTOM){
						flip_dst = cursor_y - FLIPSTART_LIMIT_BOTTOM;
//						flip_dst = bottomY - FLIPSTART_LIMIT_BOTTOM;
						Log.e("cursorButtom", "---------------where:" + where + " FlipButtom:" + FLIPSTART_LIMIT_BOTTOM
								+ " flip_dst:" + flip_dst);
					}
					else
						flip_dst = 0;
					
					t = (int)(((float)cursor_y/bottomY) * (Start.SCREEN_HEIGHT -250));
//					Calligraph.flipblockLayout.setLayout(t);
					
					Log.e("fliperror", "cursor_y:" + cursor_y + " bottomY:" + bottomY);
					
					//ly
//					Calligraph.TestButton.layout(600 - Calligraph.TestButton.getWidth(),
//							t, 
//							600, 
//							t + Calligraph.TestButton.getHeight());
//					Calligraph.TestButton.layout(1600 - Calligraph.TestButton.getWidth(),
//							t, 
//							1600, 
//							t + Calligraph.TestButton.getHeight());
					
				}
//				Calligraph.TestButton.setVisibility(View.VISIBLE);
			}
			
			//以后光标都改为以此做判断!!!!!!!!!!!!!!!  留待改进
			if(bottomY < FLIPSTART_LIMIT_BOTTOM){
				flip_dst = 0;
				int t = (int)(((float)cursor_y/bottomY) * 800);
//				Calligraph.flipblockLayout.setLayout(t);
				Calligraph.flipblockBtn.setVisibility(View.INVISIBLE);
//				Start.c.TestButton.setVisibility(View.INVISIBLE);
//				Log.e("bottomy", "setFlip INVISIBLE bottomy:" + bottomY);
//				if(flag ){
//					WorkQueue.getInstance().endFlipping();
//				}
				
			}
			if(bottomY >= FLIPSTART_LIMIT_BOTTOM){
				//Start.c.TestButton.setVisibility(View.VISIBLE);
			}
			
			
	}
	public void resetFlip(){
		if(available.getDirect() == 1 && flip_Horizonal_dst > 0){
			Calligraph.flipblockHBtn.setVisibility(View.GONE);
			Calligraph.flipblockHBtn.setVisibility(View.VISIBLE);
		}else if(available.getDirect() != 1 && flip_dst > 0){
			Calligraph.flipblockBtn.setVisibility(View.GONE);
			Calligraph.flipblockBtn.setVisibility(View.VISIBLE);
		}
	}
	
	private void setFlipDst_vertical(){
		Log.e("dst", cursor_x + "!!!!!");
		
		
//		if(flip_Horizonal_bottom < Start.SCREEN_WIDTH){
		if(bottomHX < Start.SCREEN_WIDTH){
		
			Log.e("fliph", "bottomHX:"+ bottomHX);
			
			int t = (int)(((float)(Start.SCREEN_WIDTH * 2 - cursor_x)/(Start.SCREEN_WIDTH * 2 - flip_Horizonal_bottom)) * Start.SCREEN_WIDTH);
			
			
			Log.e("fliph", "t:"+ t);
			if(t>Start.SCREEN_WIDTH){
				t = Start.SCREEN_WIDTH;
				set_Horizonal_Flip_dst(t);//竖，写到最后，不重设t，滑块超出屏幕，只重设t，不setdst，字不跟着滚动
			}
			Calligraph.flipblockHLayout.setLayout(t);
			Calligraph.flipblockHBtn.setVisibility(View.VISIBLE);
			
			Log.e(TAG, "insert EndofLine: flip_Horizonal_bottom="+ flip_Horizonal_bottom);
		}
		
		
		if(bottomHX > Start.SCREEN_WIDTH){
			Calligraph.flipblockHBtn.setVisibility(View.GONE);
			flip_Horizonal_dst = 0;
			flip_Horizonal_bottom = Start.SCREEN_WIDTH;
			Log.e("bottomy", "!!!!!!!!!!!!!!");
		}
	}
	
	
	public boolean getSelected(){
		System.out.println("get:"+selected);
		return selected;
	}
	public void setSelected(boolean selected){
		this.selected = selected;
	}
	
	private boolean cursor_change = false;
	
	public int cursor_x = 0;
	public int cursor_y = 0;
	
	public int temp_cursor_x = 0;
	public int temp_cursor_y = 0;
	
	public int storage_endx = 0;
	
	private Canvas c = new Canvas();
	public Bitmap mb;
	private Available available;
	private MyView mv;
	public int getID(){
		return id;
	}
	
	public Available getAvailable(){
		return available;
	}
	
	
	public EditableCalligraphy()
	{
		currentpos = 0;
		charList = new LinkedList<EditableCalligraphyItem>();
		mindMapList = new ArrayList<MindMapItem>();
	}
	
	
	public EditableCalligraphy(EditableCalligraphy e)
	{
		this.currentpos = e.currentpos;
		this.id = e.getID();
		this.mb = e.mb;
		this.start_x = e.start_x;
		this.start_y = e.start_y;
		this.end_x = e.end_x;
		storage_endx = end_x;
		this.end_y = e.end_y;
		this.cursor_x = e.cursor_x;
		this.cursor_y = e.cursor_y;
		
		this.linenumber = e.linenumber;
		charList = (LinkedList<EditableCalligraphyItem>) e.getCharsList().clone();
		
	}
	public EditableCalligraphy(EditableCalligraphy e,EditableCalligraphyItem undoItem)
	{
		this.currentpos = e.currentpos;
		this.id = e.getID();
		this.mb = e.mb;
		this.undoItem = new EditableCalligraphyItem(undoItem);
	}
	
	
	
	public EditableCalligraphy(int id,Available available,MyView mv)
	{
		
		if(available.getAlinespace() != 0){
			linespace = available.getAlinespace();
		}else{
			linespace = WolfTemplateUtil.getCurrentTemplate().getLinespace();
		}
		
		this.id = id;
		currentpos = 0;
		charList = new LinkedList<EditableCalligraphyItem>();
		mindMapList  = new ArrayList<MindMapItem>();
		this.available = available;
		this.count = 0;
		this.start_x = available.getStartX()+Start.SCREEN_WIDTH;
		
//		this.start_y = available.getStartY()-100;
		this.start_y = available.getStartY();	
		
		this.end_x = available.getEndX()+Start.SCREEN_WIDTH;
		storage_endx = end_x;
		this.end_y = available.getEndY();
		this.linenumber = available.getLinenumber();
		
		setCursorXY(start_x, start_y);
		
		this.mv = mv;
		selected = true;
		
		SharedPreferences settings = Start.context.getSharedPreferences(ParametersDialog.FILENAME,  android.content.Context.MODE_PRIVATE);
		HMargin = settings.getInt(ParametersDialog.PARAM_WORD_PAD, EditableCalligraphy.HMargin);
		basePad = settings.getInt(ParametersDialog.PARAM_WORD_BASE, 0);
		BY_WIDTH = settings.getBoolean(ParametersDialog.PARAM_WORD_STYLE, false);
		initDatabaseCharList();
		Log.e("init", "EditableCalligraphy()");
		Log.e("matrix", "init:---" + this.available.getAid());
	}
	
	public LinkedList<EditableCalligraphyItem> getCharsList(){
			return charList;
	}
	public int backward()
	{
		if(currentpos <= 0 )
			return 0;
		currentpos--;
		
//		charList.remove(currentpos);
		return this.currentpos;
		
	}
	public int forward()
	{
		if(currentpos < charList.size())
			currentpos++;
		return this.currentpos;
	}
	public int end()
	{
		currentpos = charList.size();
		return this.currentpos;
	}
	public void insert(Bitmap m, Matrix matrix)
	{
		if(available.isEditable()){
			addToUndoList(TYPE_INSERT);
			int l = linespace;
			if(linespace < m.getHeight())
				l = m.getHeight();
			EditableCalligraphyItem e = new EditableCalligraphyItem(m , l);
			e.setMatrix(matrix);
			e.setNotSaved();
			e.setItemId(getCurrentCount());
			addCurrentCount();
			
			int pos = 0;
			boolean result = false;
			int operate = CalligraphyDB.OP_ADD_WORD;
			if(mv.getTouchMode() instanceof HandWriteMode){
				boolean isMindmapEdit = ((HandWriteMode)mv.getTouchMode()).isMindMapEditableStatus();
				if(isMindmapEdit){
					currentMindMapItem.addNewWord(e);
					operate = CalligraphyDB.OP_ADD_MIND_WORD;
					pos = charList.size();
				}else{
					operate = CalligraphyDB.OP_ADD_WORD;
					pos = currentpos;
				}
			}
			e.setOpPos(pos);
			result = CalligraphyDB.getInstance(Start.context).saveOperating(
					operate,
					currentpos,
					WolfTemplateUtil.getCurrentTemplate().getId(),
					Start.getPageNum(), 
					available.getAid() -1, e);
			
			try {
				synchronized (charList) {
					charList.add(currentpos, e);
				}
			} catch (IndexOutOfBoundsException iob) {
				currentpos = 0;
				charList.add(currentpos, e);
			}
			 
			currentpos++;
			setFlipDst(true,"insert");
		}
	}
	public void insertVEditable(Bitmap m, Matrix matrix)
	{
		if(available.isEditable()){
			addToUndoList(TYPE_INSERT);
			int l = linespace;
			if(linespace < m.getHeight())
				l = m.getHeight();
//			EditableCalligraphyItem e = new EditableCalligraphyItem(m , l,word);
			
			VEditableCalligraphyItem e = new VEditableCalligraphyItem(m);
			Log.v("vectorword", "word null:" + (CalligraphyVectorUtil.getFinishedWord() == null));
			e.setWord(CalligraphyVectorUtil.getFinishedWord());
			Log.v("saveword", "getFinishedWord" + (e.getWord()== null));
			e.setmPath(CalligraphyVectorUtil.getmPath());
			e.setBottom_Y(CalligraphyVectorUtil.getBottom_Y());
			e.setTop_Y(CalligraphyVectorUtil.getTop_Y());
			e.setLeft_X(CalligraphyVectorUtil.getLeft_X());
			e.setRight_X(CalligraphyVectorUtil.getRight_X());
			e.setmColor(Start.c.view.baseImpl.bPaint.getColor());
			e.setNotSaved();
			if(available.getZoomable())
				e.setMatrix(matrix);
			else
				e.setMatrix(new Matrix());
			e.setMinHeight(l);
			
			e.setItemId(getCurrentCount());
			addCurrentCount();
			
			int pos = 0;
			boolean result = false;
			int operate = CalligraphyDB.OP_ADD_WORD;
			if(mv.getTouchMode() instanceof HandWriteMode){
				boolean isMindmapEdit = ((HandWriteMode)mv.getTouchMode()).isMindMapEditableStatus();
				if(isMindmapEdit){
					currentMindMapItem.addNewWord(e);
					operate = CalligraphyDB.OP_ADD_MIND_WORD;
					pos = charList.size();
				}else{
					operate = CalligraphyDB.OP_ADD_WORD;
					pos = currentpos;
				}
			}
			e.setOpPos(pos);
			result = CalligraphyDB.getInstance(Start.context).saveOperating(
					operate,
					pos,
					WolfTemplateUtil.getCurrentTemplate().getId(),
					Start.getPageNum(), 
					available.getAid() -1, e);
			
			try {
				synchronized (charList) {
					charList.add(pos, e);
					LogUtil.getInstance().e("insertV", "add at " + currentpos);
				}
			} catch (IndexOutOfBoundsException iob) {
				pos = 0;
				charList.add(pos, e);
			}
			
			
			if(!result)
				Log.e(TAG, "save error !!!!!!!!!!!!!!!!!!!!!!!!!!!");
			
			currentpos++;
			setFlipDst(true,"insertVEditable");
			
			
		}
	}
	public EditableCalligraphyItem insertImage(Bitmap m, Matrix matrix,Uri imageUri)
	{
		if(available.isEditable() && m != null){
			addToUndoList(TYPE_INSERT);
			int l = linespace;
			if(linespace < m.getHeight())
				l = m.getHeight();
			EditableCalligraphyItem e = new EditableCalligraphyItem(m , l ,imageUri);
			e.setMatrix(matrix);
			
			e.setItemId(getCurrentCount());
			addCurrentCount();
//			charList.add(currentpos, e);
			
			try {
				synchronized (charList) {
					charList.add(currentpos, e);
				}
			} catch (IndexOutOfBoundsException iob) {
				currentpos = 0;
				synchronized (charList) {
					charList.add(currentpos, e);
				}
			}
			e.setNotSaved();
			
			boolean result = CalligraphyDB.getInstance(Start.context).saveOperating(
					CalligraphyDB.OP_ADD_WORD,
					currentpos,
					WolfTemplateUtil.getCurrentTemplate().getId(),
					Start.getPageNum(), 
					available.getAid() -1, e);
			if(!result)
				Log.e(TAG, "save error !!!!!!!!!!!!!!!!!!!!!!!!!!!");
			
			currentpos++;
			
			return e;
		}
		return null;
	}
	public void insertVideo(Bitmap m, Matrix matrix,Uri videoUri)
	{
		if(available.isEditable() && m != null){
			addToUndoList(TYPE_INSERT);
			int l = linespace;
			if(linespace < m.getHeight())
				l = m.getHeight();
			EditableCalligraphyItem e = new EditableCalligraphyItem(m , l ,videoUri);
			e.setMatrix(matrix);
			e.setType(Types.VEDIO);
			e.setItemId(getCurrentCount());
			addCurrentCount();
			
			try {
				synchronized (charList) {
					charList.add(currentpos, e);
				}
			} catch (IndexOutOfBoundsException iob) {
				currentpos = 0;
				synchronized (charList) {
					charList.add(currentpos, e);
				}
			}
			e.setNotSaved();
			
			boolean result = CalligraphyDB.getInstance(Start.context).saveOperating(
					CalligraphyDB.OP_ADD_WORD,
					currentpos,
					WolfTemplateUtil.getCurrentTemplate().getId(),
					Start.getPageNum(), 
					available.getAid() -1, e);
			if(!result)
				Log.e(TAG, "save error !!!!!!!!!!!!!!!!!!!!!!!!!!!");
			
			currentpos++;
		}
	}
	public void insertAudio(Bitmap m, Matrix matrix,Uri videoUri)
	{
		if(available.isEditable() && m != null){
			addToUndoList(TYPE_INSERT);
			int l = linespace;
			if(linespace < m.getHeight())
				l = m.getHeight();
			EditableCalligraphyItem e = new EditableCalligraphyItem(m , l ,videoUri);
			e.setMatrix(matrix);
			e.setType(Types.AUDIO);
			e.setItemId(getCurrentCount());
			addCurrentCount();
			
			try {
				synchronized (charList) {
					charList.add(currentpos, e);
				}
			} catch (IndexOutOfBoundsException iob) {
				currentpos = 0;
				synchronized (charList) {
					charList.add(currentpos, e);
				}
			}
			e.setNotSaved();
			
			boolean result = CalligraphyDB.getInstance(Start.context).saveOperating(
					CalligraphyDB.OP_ADD_WORD,
					currentpos,
					WolfTemplateUtil.getCurrentTemplate().getId(),
					Start.getPageNum(), 
					available.getAid() -1, e);
			if(!result)
				Log.e(TAG, "save error !!!!!!!!!!!!!!!!!!!!!!!!!!!");
			
			currentpos++;
		}
	}
	public void insert(Bitmap m, Matrix matrix,int i)
	{
//		EditableCalligraphyItem e = new EditableCalligraphyItem(m);
		EditableCalligraphyItem e = new EditableCalligraphyItem(m, available.getAlinespace());
		e.setMatrix(matrix);
		e.setNotSaved();
//		charList.add(currentpos, e);
		
		try {
			synchronized (charList) {
				charList.add(currentpos, e);
			}
		} catch (IndexOutOfBoundsException iob) {
			currentpos = 0;
			synchronized (charList) {
				charList.add(currentpos, e);
			}
		}
		
		boolean result = CalligraphyDB.getInstance(Start.context).saveOperating(
				CalligraphyDB.OP_ADD_WORD,
				currentpos,
				WolfTemplateUtil.getCurrentTemplate().getId(),
				Start.getPageNum(), 
				available.getAid() -1, e);
		if(!result)
			Log.e(TAG, "save error !!!!!!!!!!!!!!!!!!!!!!!!!!!");
		
		currentpos++;
	}
	//1
	public int delete()
	{
		
		if(mv.getTouchMode() instanceof HandWriteMode  &&
				((HandWriteMode)mv.getTouchMode()).isMindMapEditableStatus()){
			if(currentMindMapItem.getCharList() == null || currentMindMapItem.getCharList().size() == 0){
				return -1;
			}
		}
		
    	Log.i("delete", "del");
		addToUndoList(TYPE_DEL);
		
		if(this.currentpos > this.charList.size()){
			this.currentpos = this.charList.size();
			Log.i("delete", "out of bound");
			return this.currentpos;
		}
		currentpos--;
		if(currentpos < 0) {
			currentpos = 0;
			Log.i("delete", "out of bound !");
			return currentpos;
		}
		
		if(currentpos + 1 > charList.size() - 1){
			
		}else if(currentpos + 1 < charList.size())
			charList.get(currentpos + 1).setNotSaved();
		
//		EditableCalligraphyItem e = charList.remove(currentpos);
		EditableCalligraphyItem e = null;
		int deleteIndex = 0;
		Log.e("delete", "is mindMap:" + ((HandWriteMode)mv.getTouchMode()).isMindMapEditableStatus());
		if(mv.getTouchMode() instanceof HandWriteMode  &&
				((HandWriteMode)mv.getTouchMode()).isMindMapEditableStatus()){
			int lastIndex = currentMindMapItem.getCharList().size() -1;
			try {
				e = currentMindMapItem.getCharList().remove(lastIndex);
				deleteIndex = charList.indexOf(e);
				charList.remove(e);
				LogUtil.getInstance().e("delete", "op_pos index in charList:" + deleteIndex  + " currentPos:" + currentpos);
			} catch (ArrayIndexOutOfBoundsException e2) {
				// TODO: handle exception
			}
			
			
		}else{
			e = charList.remove(currentpos);
			deleteIndex = currentpos;
		}
		
		if(e.isSpecial()){
			e.getMindMapItem().deleteWord(e);
		}
		
		if(e.type == Types.ImageItem)
			ImageLimit.instance().deleteImageCount();
		
		boolean result = CalligraphyDB.getInstance(Start.context).saveOperating(
				CalligraphyDB.OP_DELETE_WORD,
//				currentpos,
				deleteIndex,
				WolfTemplateUtil.getCurrentTemplate().getId(),
				Start.getPageNum(), 
				available.getAid() -1, e);
		if(!result)
			Log.e(TAG, "save error !!!!!!!!!!!!!!!!!!!!!!!!!!!");
		
		e = null;
//		setFlipDst(true);
		setFlipDstAndFresh(true);
		return this.currentpos;
	}
	//2
	public void insertSpace()
	{

		addToUndoList(TYPE_SPACE);
		EditableCalligraphyItem e = new EditableCalligraphyItem(EditableCalligraphyItem.Types.Space);
		e.setNotSaved();
//		charList.add(currentpos, e);
		
		try {
			charList.add(currentpos, e);
		} catch (IndexOutOfBoundsException iob) {
			currentpos = 0;
			charList.add(currentpos, e);
		}
		
		boolean result = CalligraphyDB.getInstance(Start.context).saveOperating(
				CalligraphyDB.OP_ADD_WORD,
				currentpos,
				WolfTemplateUtil.getCurrentTemplate().getId(),
				Start.getPageNum(), 
				available.getAid() -1, e);
		if(!result)
			Log.e(TAG, "save error !!!!!!!!!!!!!!!!!!!!!!!!!!!");
		
		currentpos++;
	}
	public void insertEnSpace()
	{

		addToUndoList(TYPE_ENSPACE);
		EditableCalligraphyItem e = new EditableCalligraphyItem(EditableCalligraphyItem.Types.EnSpace);
		e.setNotSaved();
//		charList.add(currentpos, e);
		
		try {
			charList.add(currentpos, e);
		} catch (IndexOutOfBoundsException iob) {
			currentpos = 0;
			charList.add(currentpos, e);
		}
		
		boolean result = CalligraphyDB.getInstance(Start.context).saveOperating(
				CalligraphyDB.OP_ADD_WORD,
				currentpos,
				WolfTemplateUtil.getCurrentTemplate().getId(),
				Start.getPageNum(), 
				available.getAid() -1, e);
		if(!result)
			Log.e(TAG, "save error !!!!!!!!!!!!!!!!!!!!!!!!!!!");
		
		currentpos++;
	}
	//3
	public void insertEndofLine()
	{

		addToUndoList(TYPE_NEWLINE);
		EditableCalligraphyItem e = new EditableCalligraphyItem(EditableCalligraphyItem.Types.EndofLine);
		e.setNotSaved();
//		charList.add(currentpos, e);
		
		try {
			charList.add(currentpos, e);
		} catch (IndexOutOfBoundsException iob) {
			currentpos = 0;
			charList.add(currentpos, e);
		}
		
		boolean result = CalligraphyDB.getInstance(Start.context).saveOperating(
				CalligraphyDB.OP_ADD_WORD,
				currentpos,
				WolfTemplateUtil.getCurrentTemplate().getId(),
				Start.getPageNum(), 
				available.getAid() -1, e);
		if(!result)
			Log.e(TAG, "save error !!!!!!!!!!!!!!!!!!!!!!!!!!!");
		
		currentpos++;
		
		
		dispearPreCursor();
		System.out.println("available !!!!!!!!:"+available.getLinenumber());
	}
	
	
    public void addToUndoList(int t){
    	
		this.type = t;
		if(Calligraph.undoList.size() >3){
//			EditableCalligraphy e = (EditableCalligraphy)Calligraph.undoList.get(0);
//			for(int i=0;i<e.charList.size();i++){
//				if(e.charList.get(i).charBitmap != null && !e.charList.get(i).charBitmap.isRecycled())
//					e.charList.get(i).charBitmap.recycle();
//			}
			Calligraph.undoList.remove(0);
		}
		Calligraph.undoList.add(new EditableCalligraphy(this));
		
		Log.i("undo", "list size:" + Calligraph.undoList.size());
	}
    public void addToUndoList(int t,EditableCalligraphyItem item){
    	
		this.type = t;
		
		Calligraph.undoList.add(new EditableCalligraphy(this,item));
		
		Log.i("undo", "list size:" + Calligraph.undoList.size());
	}
	
	public void changeEndX(Matrix matrix){
		matrix.getValues(values);
		float scale = values[0];
		end_x = start_x + (int)((storage_endx - start_x)/scale);
		
	}
	
	public void changeSmallBitmap(Matrix matrix){
		matrix.getValues(values);
		EditableCalligraphyItem e = null;
		Bitmap temp_dst = null;
		Bitmap temp_src = null;
		for(int i=0;i<charList.size();i++){
			e = charList.remove(i);
			temp_src = e.getCharBitmap();
			temp_dst = Bitmap.createBitmap(temp_src, 0, 0, temp_src.getWidth(), temp_src.getHeight(),matrix,true);
			BitmapCount.getInstance().createBitmap("EditableCalligraphy changeSmallBitmap");
			e.recycleBitmap();
			BitmapCount.getInstance().recycleBitmap("EditableCalligraphy changeSmallBitmap temp_src");
			
			e = new EditableCalligraphyItem(temp_dst);
			charList.add(i, e);
		}
		
	}
//	boolean scaleFlag = false;
	public void scaleUpdate(Bitmap m,Matrix matrix){
		
		if(available.getDirect() == 1){
			update_vertical_fromRight(m, matrix);
			return;
		}
		if(available.getZoomable()){
			
		}else{
			matrix = new Matrix();
		}
		Paint p = new Paint();
		
		
		matrix.getValues(values);
		scale = values[0];
		float dScale ;
		float wordScale = 0;
		float width = 0.0f;
		float picScale = 0;
		
		this.mb = m;
		c = new Canvas();
		c.setBitmap(m);
		
		int x=start_x;
		int y=start_y;
		
		
		float tt = 0;
		float maxHeight=0;;
		int end = 0;
		if(recycle_firstIndex <= charList.size()){
			end = recycle_firstIndex;
		}else{
			end = charList.size();
		}
		
		for(int i =recycle_lastIndex; i < end ; i++){
			
			matrix.getValues(values);
			scale = values[0];
			dScale = scale;
			
			EditableCalligraphyItem e = charList.get(i);
			e.getMatrix().getValues(values);
				dScale /= values[0];
			tt = values[0];
			
			if(maxHeight < (linespace + VMargin)*scale){
				maxHeight = (linespace + VMargin)*scale;
			}
			
			if(e.getWidth()  > end_x - x && x != start_x){
				if(available.getLinenumber() != 1){
					x = start_x;
					y += maxHeight;
					maxHeight=0;
				}else{
					continue;
				}
			}
			if(e.getWidth() > end_x - start_x && x != start_x)
			{
				continue;
			}
			if(EditableCalligraphyItem.getType(e.getCharType()) == Types.ImageItem){
					if(maxHeight < e.getHeight() * tt){
						maxHeight = e.getHeight() * tt;
						int tem = 0;
						if(maxHeight % (int)(linespace * dScale) != 0){
							tem = (int) (maxHeight / (int)(linespace * dScale));
							tem ++;
							maxHeight = tem * linespace * dScale;
						}
					}
			}
			
			if(i == this.currentpos){
				//光标被改变到此位置。 绘制光标
				setCursorXY(x, y);
				dispearPreCursor();
			} 
			if(e.type == EditableCalligraphyItem.Types.EndofLine){
				Log.i(TAG, "end of line");
				e.setCurPos(x, y);
				x = start_x;
				y += maxHeight;
				
				maxHeight=0;
				continue;
			}
			if(e.type != EditableCalligraphyItem.Types.Space && e.type != EditableCalligraphyItem.Types.EnSpace){
				
				//画所有的字
				Bitmap tmp = e.getCharBitmap();
				if(tmp == null){
				}else{
				float pad = 0.0f;
				
				if(available.getAlinespace() != 0){
					CursorDrawBitmap.mIntervalHeight = available.getAlinespace();
				}
				
				if(tmp.getHeight()/tt < CursorDrawBitmap.mIntervalHeight) {
					pad = (CursorDrawBitmap.mIntervalHeight  - 
							tmp.getHeight()/tt)  /1.2f;
				}
				
				Matrix mm = new Matrix();
				if(flip_dst < BaseBitmap.TITLE_HEIGHT)
					mm.postTranslate(x,y+pad);//test -70
				else{
					int t = y+ (int)pad - flip_dst + BaseBitmap.TITLE_HEIGHT;
					mm.postTranslate(x,t);//test -70
					
				}
				//是图片不缩放
				
				if(mv.getTouchMode() != mv.getCursorScaleMode()){
					mm.preScale(1, 1);
				}else if(e.type == Types.ImageItem){
					
						mm.preScale(tt,tt);
				}
				else{
						mm.preScale(dScale,dScale);
				}
				
				
				c.drawBitmap(tmp, mm, p);
				
			}//end null if
			}//end  if
			
			e.setCurPos(x, y);
			x += e.getWidth()*dScale  + HMargin;
			
		}//end for
		if( this.currentpos >= charList.size()){
			setCursorXY(x, y);
			dispearCurrentCursor();
		}
		if(available.getAid() == 4){
			bottomY = y;
			setFlipDst(false,"scaleUpdate");//绕圈儿鸟
			Log.e("fliperror", "scale update set buttomY:" + bottomY);
		}
	}
	
	float scale;
	float recycle_line = 0;
	float recycle_bottom_line = 900;
	int lastLineStartID = 0;
	boolean recycleable;
	int recycle_lastIndex = 0;
	int recycle_firstIndex = 10000;
	float[] recycleValues = new float[9];
	boolean bottomflag = true;
	
	private MindMapItem currentMindMapItem;
	public MindMapItem getCurrentMindMapItem(){
		return this.currentMindMapItem;
	}
	public void setCurrentMindMapItem(MindMapItem item){
		Log.e("minderror", "mind setCurrentMindItem " + (item == null));
		this.currentMindMapItem = item;
	}
	private boolean drawMapBegin = false;
	private int mindMapBeginY;
	private Bitmap tmp;//排版里暂存bitmap
	private MindMapItem mapItem;
	int x;
	int y;
	public void update(Bitmap m,boolean flip)
	{
		Start.kanboxUploadHandler.removeMessages(1);
		Start.kanboxUploadHandler.sendEmptyMessageDelayed(1, (long)(Start.auto_upload_time * 60 * 1000));
		
		lastLineStartID = 0; 
		bottomflag = true;
		drawMapBegin = false;
		recycleable = false;
		
//		workList.clear();
		
		recycle_lastIndex = 0;
		recycle_firstIndex = 10000;
		
		Matrix matrix;
		if(Start.c == null)
			matrix = Start.m;
		else
			matrix = Start.c.view.getMMMatrix();
		
		if(available.getDirect() == 1){
//			update_vertical_fromRight(m, matrix);
			return;
		}
		
		Paint p = new Paint();
		
		matrix.getValues(values);
		scale = values[0];
		resetRecycleLimit(scale);
		linespace = (int)(available.getAlinespace() * values[4]);
		float dScale ;//应该显示的缩放比例
		float width = 0.0f;
		
		this.mb = m;
		c = new Canvas();
		c.setBitmap(m);
		
		x=start_x;
		y=start_y;
		
		if(Available.AVAILABLE_NUMBER.equals(available.getControltype())){
			int tap = (linespace - available.getAfontsize())/2;
			y = end_y - tap;
			p.setTextSize(available.getAfontsize());
			c.drawText("共 " + mv.getRowNumber() + " 行", x, y, p);
			return;
		}
		
		float tt = 0;
		float maxHeight=0;
		
		if(charList.size() != 0){
			mv.addRowNumber(available.getControltype());
		}
		
		for(int i =0; i < charList.size() ; i++){
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
			
			if(maxHeight < linespace){ 
				maxHeight = linespace;
			}
			if(e.getWidth()  > end_x - x && x != start_x){
				if(available.getLinenumber() != 1){
					x = start_x;
					y += maxHeight;
					maxHeight=0;
					mv.addRowNumber(available.getControltype());
					//记录到光标为止，行首字的id
					if(i < this.currentpos){
						lastLineStartID = i ;
						Log.e("mindmap", "newline lastLineStartID set:" + lastLineStartID);
					}
				}else{
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
					if(maxHeight % (linespace ) != 0){
						tem = (int) (maxHeight /(linespace));
						tem ++;
						maxHeight = tem * linespace;
						Log.e("imageHeight", "linespace:" +linespace + " maxHeight:"+
								maxHeight + " tem:" + tem);
					}
				}
			}
			if(i == this.currentpos && !drawMapBegin){
				//光标被改变到此位置。 绘制光标
				if(mv.getTouchMode() instanceof HandWriteMode  &&
						((HandWriteMode)mv.getTouchMode()).isMindMapEditableStatus()){
					//如果是正在编辑导图状态，光标不置于末尾
				}else{
					setCursorXY(x, y);
					dispearPreCursor();
				}
			} 
			if(e.type == EditableCalligraphyItem.Types.EndofLine){
				e.setCurPos(x, y);
				x = start_x;
//				Log.e("drawmindmap", "end of line before-------- y:" + y + " maxHeight:" + maxHeight);
				y += maxHeight;
				maxHeight=0;
				mv.addRowNumber(available.getControltype());
				
				//记录到光标为止，行首字的id
				if(i < this.currentpos){
					lastLineStartID = i + 1;
//					Log.e("mindmap", "endofline lastLineStartID set:" + lastLineStartID + " currentPos:" + currentpos);
				}
				drawMapBegin = false;
//				Log.e("drawmindmap", "end of line -------- y:" + y);
				continue;
			}
			
			//画导图
			if(e.isSpecial()){
				lastLineStartID = i+1;
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
//				Log.e("drawmindmap", "mindmapitem:" + e.getMindMapItem().getMindID() + " start update y:" + y);
				updateMindMap(mapItem, p,x+mapItem.getFlipDstX(),true);
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
									e.getHeight()/tt)  /1.2f + basePad;
	//						Log.e("pad", "i:" + i + "e.getHeight():" + e.getHeight() + 
	//								" mIntervalHeight:" + CursorDrawBitmap.mIntervalHeight
	//								+ " tmp.getHeight():" + tmp.getHeight()
	//								+ " pad:" + pad
	//								+ " tt:" + tt
	//								+ " available.getControltype():" + available.getControltype());
						}else{
							pad = basePad;
						}
						
						if(e.type == Types.ImageItem){
							if(e.getWidth()* dScale > Start.SCREEN_WIDTH)
								x += e.getFlipDstX();
						}
						Matrix mm = new Matrix();
						LogUtil.getInstance().e("updateflip", "flipdst:" + flip_dst);
						if(flip_dst < BaseBitmap.TITLE_HEIGHT){
							mm.postTranslate(x,y+pad);//test -70
						}else{
							int t = y+ (int)pad - flip_dst + BaseBitmap.TITLE_HEIGHT;
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
											execute(new resetBitmapWork((VEditableCalligraphyItem)e 
													,iden));
										}else if(e.type == Types.ImageItem){
											if(e.itemStatus == ItemStatus.RECYCLED){
												isInsert = WorkQueue.getInstance().executeImage(new resetImageWork(e,iden));
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
			startRecycleInVisiableBitmap();
		}
		if( this.currentpos >= charList.size()){
			if(mv.getTouchMode() instanceof HandWriteMode  &&
					((HandWriteMode)mv.getTouchMode()).isMindMapEditableStatus()){
				//如果是正在编辑导图状态，光标不置于末尾
			}else{
				setCursorXY(x, y);
				dispearCurrentCursor();
			}
		}
		
		if(available.getAid() == 4){
			bottomY = y + (int)maxHeight;
			setFlipDst(false,"update");//绕圈儿鸟
			Log.e("fliperror", "update set buttomY:" + bottomY);
		}
	}
	int maxX = 0;
	

	//ly
	int ERROR_DST_Y = 10;//行距
	//end
	
	public void updateMindMap(MindMapItem mapItem,Paint p,int preX,boolean drawflag){
		
		
//		y = mindMapBeginY +  mapItem.getMarginTop()*available.getAlinespace();
//		LogUtil.getInstance().e("mindmap", "brotherButtom:" + mapItem.getbrotherButtom());
		if(mapItem.getbrotherButtom() == -1){
			y = mindMapBeginY +  (int)(mapItem.getMarginTop()*available.getAlinespace()*scale);
		}else{
			y = mapItem.getbrotherButtom() + (int)( mapItem.getMarginTop() * available.getAlinespace() * scale) ;
		}
		Log.e("mindmapY", "y:" + y + " scale:" + scale + " linespace:" + (int)(available.getAlinespace() * scale));
//		LogUtil.getInstance().v("mindmap", "mindmap getMarginTop:" + mapItem.getMarginTop());
		x = preX;
		maxX = x;
		int l;
		EditableCalligraphyItem e = null;
		//minditem类中添加LocatDot类记录每个节点的画线的出发点和接>    入点
		//行距available.getAlinespace()
		//在画该节点的第一个字之前记录这个节点画线的接入点//
		if(flip_dst < BaseBitmap.TITLE_HEIGHT){
			mapItem.indot.setLocateDot(x, y+ ERROR_DST_Y);
		}else{
			mapItem.indot.setLocateDot(x, y + ERROR_DST_Y - flip_dst + BaseBitmap.TITLE_HEIGHT);
		}
		
		for(l=0;l<mapItem.getCharList().size();l++){
			e = mapItem.getCharList().get(l);
			
			if(drawflag){
//				float mappad = (CursorDrawBitmap.mIntervalHeight  - e.getHeight())  /1.2f;
				float mappad = ((int)(available.getAlinespace() * scale) - e.getHeight())  /1.2f;
				Matrix mm = new Matrix();
				if(flip_dst < BaseBitmap.TITLE_HEIGHT){
					mm.postTranslate(x,y+mappad);//test -70
				}else{
					int t = y+ (int)mappad - flip_dst + BaseBitmap.TITLE_HEIGHT;
					mm.postTranslate(x,t);//test -70
				}
				tmp = e.getCharBitmap();
				if(tmp != null)
					c.drawBitmap(tmp, mm, p);
				e.setCurPos(x, y);
			}
			
			x += e.getWidth() + HMargin;	
			if(x>maxX)
				maxX = x;
		}
		//画完了这个节点的时候记录这个节点画线的出发点
		if(flip_dst < BaseBitmap.TITLE_HEIGHT){
			mapItem.outdot.setLocateDot(x, y+(int)(available.getAlinespace() * scale));
		}else{
			mapItem.outdot.setLocateDot(x, y+(int)(available.getAlinespace() * scale)- flip_dst + BaseBitmap.TITLE_HEIGHT);
		}
		
		mapItem.setButtom(y+ (int)(available.getAlinespace()* scale));
		if(mapItem == getCurrentMindMapItem()){
//			LogUtil.getInstance().e("SetCursorXY", "updateMindmap x:" + x + " y:" + y + " available:" + available.getAid()); 
			setCursorXY(x, y);
			dispearCurrentCursor();
		}
		
		if(mapItem.getChildList() != null && mapItem.getChildList().size()!= 0){
			MindMapItem tempItem = null;
			preX = maxX + 50;
			for(int j=0;j<mapItem.getChildList().size();j++){
				tempItem = mapItem.getChildList().get(j);
				y += available.getAlinespace();
				updateMindMap(tempItem, p,preX,drawflag);
			}
		}
		
		if(!drawflag)
			return;
		
		//在计算了这个节点的所有子节点的出发点和接入点后画线（仅对本节点的一级节点）//连接线
		//连接线使用的画笔
		Paint mp = new Paint();
		mp.setColor(0x804169e1);
		//87CEFA
		mp.setStrokeWidth(2);
		mp.setStyle(Style.STROKE);
		
		//连接线操作函数
		DrawLineOperate drawOperate = null;
		drawOperate = new DrawLineOperate(50,1);//50是列宽，1是缩放比例
		//连接线类型
		LineStyle my_line_style ;
		my_line_style =LineStyle.STRAIGHT_LINE;
		my_line_style =LineStyle.STRAIGHT_FOLD_LINE;
		my_line_style =LineStyle.OVAL_LINE;
		my_line_style =LineStyle.OVAL_FOLD_LINE_HALF;
		my_line_style =LineStyle.OVAL_FOLD_LINE_BRACKET;
		//设置线类型
		drawOperate.setLineStyle(my_line_style);
		
		float vertical_offset = 0.5f;
		//节点框使用的填充画笔
		Paint rp = new Paint();
		rp.setColor(0x2087CEFA);
		rp.setStyle(Style.FILL);
		//节点框使用的操作函数
		DrawOutLineOperate drawOutOperate = null;
		drawOutOperate =  new DrawOutLineOperate(rp,mp,1);//填充画笔，画线画笔，1是缩放比例
		drawOutOperate.setOutLineStyle(OutLineStyle.OVAL_STROKE_AND_FILL_BOX);

		OutLineStyle my_out_line_style ;//框类型
		my_out_line_style =OutLineStyle.STRAIGHT_STROKE_HALF_BOX;//直线半框无填充
		my_out_line_style =OutLineStyle.STRAIGHT_STROKE_BOX;//直线框无填充
		my_out_line_style =OutLineStyle.STRAIGHT_FILL_BOX;//只有填充
		my_out_line_style =OutLineStyle.STRAIGHT_STROKE_AND_FILL_BOX;//直线框有填充
		my_out_line_style =OutLineStyle.OVAL_STROKE_BOX;//弧线框无填充
		my_out_line_style =OutLineStyle.OVAL_FILL_BOX;//只有填充
		my_out_line_style =OutLineStyle.OVAL_STROKE_AND_FILL_BOX;//弧线框有填充
		my_out_line_style =OutLineStyle.OVAL_STROKE_HALF_BOX;//弧线半框无填充
		//设置框类型
		switch(my_out_line_style){
		case STRAIGHT_STROKE_HALF_BOX://画半框的直线线
			vertical_offset=0.7f;
	        if( mapItem.getMindID() == 0 ){
	        	drawOutOperate.setOutLineStyle(OutLineStyle.STRAIGHT_STROKE_HALF_BOX_RIGHT);
	        }
	        else{
	        	if(mapItem.hasChild()){
	       		 drawOutOperate.setOutLineStyle(OutLineStyle.STRAIGHT_STROKE_HALF_BOX_LEFT_AND_RIGHT);
	            }
	            else{
	           	 drawOutOperate.setOutLineStyle(OutLineStyle.STRAIGHT_STROKE_HALF_BOX_LEFT);
	            }
	        }
			break;
		case OVAL_STROKE_HALF_BOX://画半框的弧线
			vertical_offset=0.7f;
	        if( mapItem.getMindID() == 0 ){
	        	drawOutOperate.setOutLineStyle(OutLineStyle.OVAL_STROKE_HALF_BOX_RIGHT);
	        }
	        else{
	        	if(mapItem.hasChild()){
	       		 drawOutOperate.setOutLineStyle(OutLineStyle.OVAL_STROKE_HALF_BOX_LEFT_AND_RIGHT);
	            }
	            else{
	           	 drawOutOperate.setOutLineStyle(OutLineStyle.OVAL_STROKE_HALF_BOX_LEFT);
	            }
	        }
			break;
		default:
			vertical_offset=0.5f;
			drawOutOperate.setOutLineStyle(my_out_line_style);
			break;
		}

        //画框
        drawOutOperate.operate(mapItem.indot, mapItem.outdot, c, mp);
        
        if(mapItem.getChildList() != null && mapItem.getChildList().size()!=0){
        	 LogUtil.getInstance().e("child", "childList null:" + (mapItem.getChildList() == null) + " size:"
        			 + mapItem.getChildList().size());
        	 //画线
        	 for(int j=0;j<mapItem.getChildList().size();j++){
                	 drawOperate.operate(mapItem.getFromDot(vertical_offset),mapItem.getChildList().get(j).getToDot(vertical_offset), c, mp);
             }
        }
        
		return;
		
	}
	
	public void update_vertical(Bitmap m,Matrix matrix)
	{
		Log.e("matrix", "zoom"+available.getZoomable());
		if(available.getZoomable()){
			
		}else{
			matrix = new Matrix();
		}
		Paint p = new Paint();
		
		matrix.getValues(values);
		scale = values[0];
		float dScale ;
		float width = 0.0f;
		
		this.mb = m;
		c = new Canvas();
		c.setBitmap(m);
		
		int x=start_x;
		int y=start_y;
		
		
		float tt = 1;
		for(int i =0; i < charList.size() ; i++){
			
			matrix.getValues(values);
			scale = values[0];
			dScale = scale;
			Log.i(TAG, "scale:" + scale);
			EditableCalligraphyItem e = charList.get(i);
			e.getMatrix().getValues(values);
//			dScale /= values[0];
			
//			tt = values[0];
				
			if(e.getWidth() > end_x - start_x)
			{
				continue;
			}
			if(i == this.currentpos){
				//光标被改变到此位置。 绘制光标
				setCursorXY(x, y);
				dispearPreCursor();
			} 
			
			if(e.type == EditableCalligraphyItem.Types.EndofLine){
//				Log.i(TAG, "end of line");
				e.setCurPos(x, y);
//				x = start_x;
//				y += (EditableCalligraphyItem.MinHeight + VMargin)*scale;
				y = start_y;
				x += (linespace + VMargin)*scale;
				continue;
			}
			
//			if(e.getWidth()  > end_x - x){
//				Log.v("test", "update!!!!!!!!!!!!!e.getWidth() > endX - x:");
//				if(available.getLinenumber() != 1){
//					x = start_x;
//					y += (EditableCalligraphyItem.MinHeight + VMargin) * scale;
//				}else{
//					continue;
//				}
//			}
			if(e.getHeight()  > end_y - y){
				if(available.getLinenumber() != 1){
					y = start_y;
					x += (linespace + VMargin) * scale;
				}else{
					continue;
				}
			}
			if(e.type != EditableCalligraphyItem.Types.Space){
				//画所有的字
				Bitmap tmp = e.getCharBitmap();
				float pad = 0.0f;
				
				if(available.getAlinespace() != 0){
					CursorDrawBitmap.mIntervalHeight = available.getAlinespace();//行间距
				}
				
				if(tmp.getWidth()/tt < linespace) {
					pad = (linespace  - 
							tmp.getWidth()/tt)  /2;
					//error
//					Log.e("pad", "i:"+i);
				}
				Log.i(TAG, "tmp height:" + tmp.getHeight() + " pad:" + pad);
				Matrix mm = new Matrix();

//				mm.postTranslate(x,y+pad);//test -70
				mm.postTranslate(x + pad,y);//test -70
				mm.preScale(dScale, dScale);
				
				c.drawBitmap(tmp, mm, p);
				
				Log.e("line", "y:"+y);  
//				if(y> Start.SCREEN_HEIGHT && y< Start.SCREEN_HEIGHT * 2){
//					
//					System.out.println(BaseBitmap.addBitmap == null);
////					add_c.setBitmap(BaseBitmap.addBitmap );
//					add_c.setBitmap(BaseBitmap.getAddedBitmap(0));
//					
////					add_c.drawBitmap(tmp, 20,30+y-Start.SCREEN_HEIGHT, p);ok
//					Matrix mmm = new Matrix();
//					Log.e("line", "x:"+x);  
//
//					mmm.postTranslate(x-Start.SCREEN_WIDTH,y+pad - Start.SCREEN_HEIGHT);//test -70
////					mm.preScale(scale, scale);
//					mmm.preScale(dScale, dScale);
//					add_c.drawBitmap(tmp, mmm, p);
//				}else if(y > Start.SCREEN_HEIGHT * 2){
//					System.out.println(BaseBitmap.addBitmap == null);
////					add_c.setBitmap(BaseBitmap.addBitmapList.get(1));
//					add_c.setBitmap(BaseBitmap.getAddedBitmap(1));
//					
////					add_c.drawBitmap(tmp, 20,30+y-Start.SCREEN_HEIGHT, p);ok
//					Matrix mmm = new Matrix();
//					Log.e("line", "x:"+x);  
//
//					mmm.postTranslate(x-Start.SCREEN_WIDTH,y+pad - Start.SCREEN_HEIGHT * 2);//test -70
////					mm.preScale(scale, scale);
//					mmm.preScale(dScale, dScale);
//					add_c.drawBitmap(tmp, mmm, p);
//				}
				
			}
			
			e.setCurPos(x, y);
//			x += e.getWidth()*dScale  + HMargin;
			y += e.getHeight()*dScale  + HMargin;
		}//end for
		if( this.currentpos >= charList.size()){
			Log.v("test", "update!! this.currentpos:"+this.currentpos+">= charList.size()"+charList.size());
//			dispearCursor(c, p, pre_x, pre_y, EditableCalligraphyItem.MinHeight);
//			this.drawCursor(c, p, x, y, EditableCalligraphyItem.MinHeight);
			setCursorXY(x, y);
			dispearCurrentCursor();
//			this.drawCursor(c, p, cursor_x, cursor_y, EditableCalligraphyItem.MinHeight);
		}
	}
	
	public void update_vertical_fromRight(Bitmap m,Matrix matrix)
	{
		Log.e("matrix", "zoom"+available.getZoomable());
		if(available.getZoomable()){
			
		}else{
			matrix = new Matrix();
		}
		Paint p = new Paint();
		
		matrix.getValues(values);
		scale = values[0];
		float dScale ;
		float width = 0.0f;
		
		this.mb = m;
		c = new Canvas();
		c.setBitmap(m);
		
		int x=start_x;
		int y=start_y;
		
		scale = values[0];
		Log.e("scale", "scale:"+scale+ " x:"+ x + " scaledX:" +(int) (Start.SCREEN_WIDTH - (Start.SCREEN_WIDTH - x)*scale));
		x = (int) (Start.SCREEN_WIDTH * 2 - (Start.SCREEN_WIDTH * 2 - x)*scale);
		
		float tt = 0;
		for(int i =0; i < charList.size() ; i++){
			
			matrix.getValues(values);
//			scale = values[0];
//			
//			x = (int) (Start.SCREEN_WIDTH - (Start.SCREEN_WIDTH - x)*scale);
			
			dScale = scale;
			Log.i(TAG, "scale:" + scale);
			EditableCalligraphyItem e = charList.get(i);
			e.getMatrix().getValues(values);
			dScale /= values[0];
			
			tt = values[0]; 
				
//			if(e.getWidth() > end_x - start_x)
			if(e.getWidth() > start_x- end_x)
			{
				continue;
			}
			if(i == this.currentpos){
				//光标被改变到此位置。 绘制光标
				setCursorXY(x, y);
				dispearPreCursor();
			} 
			
			if(e.type == EditableCalligraphyItem.Types.EndofLine){
				Log.i(TAG, "end of line");
				e.setCurPos(x, y);
//				x = start_x;
//				y += (EditableCalligraphyItem.MinHeight + VMargin)*scale;
				y = start_y;
				x -= (linespace + VMargin)*scale;
				continue;
			}
			
			if(e.getHeight() * scale  > end_y - y){
				if(available.getLinenumber() != 1){
					y = start_y;
					x -= (linespace + VMargin) * scale;
				}else{
					continue;
				}
			}
			if(e.type != EditableCalligraphyItem.Types.Space ){
				//画所有的字
				
				Bitmap tmp = e.getCharBitmap();
				if(tmp == null)
					continue;
				
				float pad = 0.0f;
				
				if(available.getAlinespace() != 0){
					CursorDrawBitmap.mIntervalHeight = available.getAlinespace();//行间距
				}
				Log.e("ERROR", "e.type:" + e.type);
				if(tmp.getWidth()/tt < linespace) {
					pad = (linespace  - 
							tmp.getWidth()/tt)  /2;
					//error
//					Log.e("pad", "i:"+i);
				}
				Log.i(TAG, "tmp height:" + tmp.getHeight() + " pad:" + pad);
				Matrix mm = new Matrix();

//				mm.postTranslate(x,y+pad);//test -70
				mm.postTranslate(x + pad + flip_Horizonal_dst,y);//test -70
				mm.preScale(dScale, dScale);
				
				c.drawBitmap(tmp, mm, p);
				
				Log.e("line", "y:"+y);				
			}
			
			e.setCurPos(x, y);
			y += e.getHeight()*dScale  + HMargin;
		}//end for
		if( this.currentpos >= charList.size()){
			Log.v("test", "update!! this.currentpos:"+this.currentpos+">= charList.size()"+charList.size());

			setCursorXY(x, y);
			dispearCurrentCursor();
		}
		
		bottomHX = x - available.getAlinespace();
//		setFlipDst(false);非常卡，不知为何
	}
	
	
	private void setCursorXY(int x,int y){
		dispearCurrentCursor();
		temp_cursor_x = cursor_x;
		temp_cursor_y = cursor_y;
		this.cursor_x = x;
		this.cursor_y = y;
		
		LogUtil.getInstance().e("SetCursorXY", "updateMindmap x:" + x + " y:" + y + " available:" + available.getAid()); 
		cursor_change = true;
	}
	
	public int getCursor_x(){
		return this.cursor_x;
	}
	public int getCursor_y(){
		return this.cursor_y;
	}
	protected void drawCursor(Canvas c, Paint p, int x, int y, int height )
	{
		c.drawLine(x, y, x, y+height, p);
//		Log.v("test", "test!!!!!!!!!!!!!!!!drawCursor x:"+ x +" y:"+y + "endx:"+(y+height));
	}
	protected void drawCursor_vertical(Canvas c, Paint p, int x, int y, int height )
	{
		c.drawLine(x, y, x+height, y, p);
//		Log.v("test", "test!!!!!!!!!!!!!!!!drawCursor x:"+ x +" y:"+y + "endx:"+(y+height));
	}
	protected void dispearCursor(Canvas c, Paint p, int x, int y, int height )
	{
		p.setColor(Color.WHITE);
		c.drawLine(x, y, x, y+height, p);
//		Log.v("test", "test!!!!!!!!!!!!!!!!drawCursor x:"+ x +" y:"+y + "endx:"+(y+height));
	}
	
	public void setCurrentPos(int x, int y)
	{
		x -= bitmapOffsetX;
		y -= bitmapOffsetY;
		
//		Log.v("CurrentPos", "setCurrentPos x:"+ x +" y:"+y);
		int i =0;
		float initScale = 0;
		float[] eValues = new float[9];
		
		int preWidth = 0;
		Rect r;
		EditableCalligraphyItem lastItem = null;
		for( i =0 ; i< charList.size(); i++){
			final EditableCalligraphyItem e = charList.get(i);
			lastItem = e;
			e.getMatrix().getValues(eValues);
			initScale = eValues[0];
			Log.e("CurrentPos", "i:" + i + " e.type:" + e.type);
			r = new Rect(
					e.getCurPosX() - preWidth/2,
					e.getCurPosY()- flip_dst - cursorMargin,
//					(int)(e.getCurPosX()+ e.getWidth()*scale + HMargin) - preWidth/2,
					(int)(e.getCurPosX()+ (e.getWidth()*scale)/2 + HMargin),
					(int)(e.getCurPosY()+ e.getHeight()*scale + VMargin*scale - flip_dst) + cursorMargin);
			preWidth = (int)(e.getWidth()*scale + HMargin);
			Log.v("CurrentPos", "e.getCurPosX:" + e.getCurPosX() + " e.getCurPosY:" + e.getCurPosY()
					+ " flipdst:" + flip_dst + " scale:" + scale);
			
			
			if(e.type == EditableCalligraphyItem.Types.VEDIO){
				
				r = new Rect(
//						(int)(e.getCurPosX()+ (e.getWidth()*scale)/4),
						(int)e.getCurPosX(),
						e.getCurPosY()- flip_dst - cursorMargin,
						(int)(e.getCurPosX()+ (e.getWidth()*scale*3)/4 + HMargin),
						(int)(e.getCurPosY()+ e.getHeight() + VMargin*scale - flip_dst) + cursorMargin);
				if(r.contains(x,y)){
					Uri vUri = e.getImageUri();
					
					if(vUri == null)
						Toast.makeText(Start.context, "视频尚未传输完成，不能播放", Toast.LENGTH_LONG).show();
					else{
						Toast.makeText(Start.context, "视频点击，开始播放", Toast.LENGTH_LONG).show();
						String videoPath = e.getImageUri().toString();
						Log.e(TAG, "videoPath :" + videoPath);
						Log.e("video", videoPath);
						Intent mIntent = new Intent();
						mIntent.putExtra("videoPath", 
								videoPath);
						mIntent.setClass(Start.context, VideoActivity.class);
						Start.context.startActivity(mIntent);
					}
					return;
				}
				r = new Rect(
							e.getCurPosX()- preWidth/2,
							e.getCurPosY()- flip_dst - cursorMargin,
							(int)(e.getCurPosX()+ (e.getWidth()*scale)/4 + HMargin),
							(int)(e.getCurPosY()+ e.getHeight() + VMargin*scale - flip_dst) + cursorMargin);
				 preWidth = (int)((e.getWidth()*scale + HMargin)/3);
			}
			if(e.type == EditableCalligraphyItem.Types.AUDIO){
				Log.v("CurrentPos", "audio x:"+ x +" y:"+y);
				r = new Rect(
//						(int)(e.getCurPosX()+ (e.getWidth()*scale)/4),
						(int)e.getCurPosX(),
						e.getCurPosY()- flip_dst - cursorMargin,
						(int)(e.getCurPosX()+ (e.getWidth()*scale*3)/4 + HMargin),
						(int)(e.getCurPosY()+ e.getHeight() + VMargin*scale - flip_dst) + cursorMargin);
				if(r.contains(x,y)){
					
//					String videoPath = Start.getStoragePath() + "/calldir/free_" + Start.getPageNum() + 
//					"/p" + Start.getPageNum() + "a" + (available.getAid()-1) + "i" + i + ".amr";
					Uri uri = e.getImageUri();
					if(uri == null)
					{
//						Toast.makeText(Start.context, "传输尚未完成，不能播放", Toast.LENGTH_LONG).show();
						Start.c.positiveDialogOnClick();
					}
					else{
						Toast.makeText(Start.context, "音频点击，开始播放", Toast.LENGTH_LONG).show();
						if(MediaPlayerUtil.getInstance().isPlaying()){
							MediaPlayerUtil.getInstance().stop();
							e.resetCharBitmap(e.getStopBitmap(), null, null);
						}else{
							MediaPlayerUtil.getInstance().setSource(uri.getPath());
							MediaPlayerUtil.getInstance().setOnStopListener(new OnCompletionListener() {
								
								@Override
								public void onCompletion(MediaPlayer mp) {
									// TODO Auto-generated method stub
									Log.e("complete", "play complete");
									e.resetCharBitmap(e.getStopBitmap(), null, null);
									Start.c.view.cursorBitmap.updateHandwriteState();
								}
							});
							e.resetCharBitmap(e.getAudioPlayingBitmap(), null, null);
							MediaPlayerUtil.getInstance().start();
							
						}
					}
					
					return;
				}
				r = new Rect(
							e.getCurPosX()- preWidth/2,
							e.getCurPosY()- flip_dst - cursorMargin,
							(int)(e.getCurPosX()+ (e.getWidth()*scale)/4 + HMargin),
							(int)(e.getCurPosY()+ e.getHeight() + VMargin*scale - flip_dst) + cursorMargin);
				 preWidth = (int)((e.getWidth()*scale + HMargin)/3);
			}
			if(e.type == EditableCalligraphyItem.Types.EndofLine){
				r = new Rect(e.getCurPosX(),
						e.getCurPosY() - flip_dst - cursorMargin, 
						e.getCurPosX() + Start.SCREEN_WIDTH, 
						e.getCurPosY()+ (int)((e.getHeight()/initScale)*scale) + (int)(VMargin * scale) - flip_dst + cursorMargin);
				preWidth = 0;
				Log.v("CurrentPos", "回车rect:"+ r.toShortString());
			}
			if( e.type == EditableCalligraphyItem.Types.ImageItem){
				 Log.e("wifioradhoc", "图片");
				 r = new Rect(
						e.getCurPosX(),
						e.getCurPosY()- flip_dst - cursorMargin,
						(int)(e.getCurPosX()+ e.getWidth()*initScale + HMargin),
						(int)(e.getCurPosY()+ e.getHeight()*initScale + VMargin*scale - flip_dst) + cursorMargin);
				 if(e.isWifiOrAdhoc()){
					 int temp = ((int)(e.getCurPosY()+ e.getHeight() + VMargin*scale - flip_dst) + cursorMargin - y);
					 if(x <= e.getCurPosX() + temp){
						 Log.e("wifioradhoc", "左边");
						 Bitmap b = BitmapFactory.decodeResource(Start.context.getResources(), R.drawable.photo_wifi);
						 e.resetCharBitmap(b, e.getMatrix(), e.getImageUri());
						 e.setWifiOrAdhoc();
						 Start.c.setWifiMode();
					 }else{
						 Bitmap b = BitmapFactory.decodeResource(Start.context.getResources(), R.drawable.photo_adhoc);
						 e.resetCharBitmap(b, e.getMatrix(), e.getImageUri());
						 e.setWifiOrAdhoc();
						 Start.c.setAdhocMode();
						 Log.e("wifioradhoc", "右边");
					 }
				 }
			}
			
			if(r.contains(x, y)){
				
				LogUtil.getInstance().e("CurrentPos", "contain!!!!!!!!!!! currentPos:"+i + " isSpecial:" + e.isSpecial());
				
				destroyCurrentMindmapItem();
				
				if(e.isSpecial()){
					if(mv.getTouchMode() instanceof HandWriteMode){
						((HandWriteMode)mv.getTouchMode()).setMindMapEditStatusTrue();
					}
					Log.e("CurrentPos", "mindid: " + e.getMindMapItem().getMindID());
					this.currentpos=i;
					setCurrentMindMapItem(e.getMindMapItem());
				}else{
					this.currentpos=i;
					if(mv.getTouchMode() instanceof HandWriteMode){
						((HandWriteMode)mv.getTouchMode()).setMindMapEditStatusFalse();
					}
				}
				break;
			}else{
				destroyCurrentMindmapItem();
				if(mv.getTouchMode() instanceof HandWriteMode){
					((HandWriteMode)mv.getTouchMode()).setMindMapEditStatusFalse();
				}
			}
		}//end for
		
		if( i == charList.size()){
			destroyCurrentMindmapItem();
			if(mv.getTouchMode() instanceof HandWriteMode){
				if(lastItem != null && !lastItem.isSpecial())
					((HandWriteMode)mv.getTouchMode()).setMindMapEditStatusFalse();
			}
			this.currentpos = i;
			LogUtil.getInstance().e("CurrentPos", "not contains to the last  currentpos:" + this.currentpos);
		}
	}
	public void destroyCurrentMindmapItem(){
		LogUtil.getInstance().e("isDestory", "destroyCurrentMindmapItem");
		if(mv.getTouchMode() instanceof HandWriteMode){
			LogUtil.getInstance().e("isDestory", "isMindmapEditableStatus:" + (
					(HandWriteMode)mv.getTouchMode()).isMindMapEditableStatus());
			if(((HandWriteMode)mv.getTouchMode()).isMindMapEditableStatus()){
				if(currentMindMapItem != null)
					currentMindMapItem.isDestory();
			}
		}
	}
	public void clear()
	{
		for(int i =0 ; i< charList.size(); i++){
			EditableCalligraphyItem e = charList.get(i);
			if(e.charBitmap != null){
				e.recycleBitmap();
				BitmapCount.getInstance().recycleBitmap("EditableCalligraphy clear e.getCharBitmap()");
			}
			if(e.getWord() != null)
				e.getWord().recycle();
		}
//		System.gc();
		
		charList.clear();
		mindMapList.clear();
		currentpos = 0;
		
		
		
	}
	
	public void drawCurrentCursor(){
		Paint mp = new Paint();
//		drawCursor(c, mp, cursor_x, cursor_y , EditableCalligraphyItem.MinHeight);
	}
	public void drawCurrentCursor(int color){
		if(available.getDirect() == 1){
			drawCurrentCursor_vertical(color);
			return;
		}
		
		
		Paint mp = new Paint();
		mp.setColor(color);
		
		
//		if(cursor_y > Start.SCREEN_HEIGHT && cursor_y < Start.SCREEN_HEIGHT * 2){
//			
//			add_c.setBitmap(BaseBitmap.addBitmap );
//			Log.e("CurrentPos", "x:"+cursor_x+" y:"+(cursor_y-Start.SCREEN_HEIGHT));
//			
////			drawCursor(add_c, mp, cursor_x - Start.SCREEN_WIDTH, cursor_y - Start.SCREEN_HEIGHT , (int)(EditableCalligraphyItem.MinHeight * scale));
//			drawCursor(add_c, mp, cursor_x - Start.SCREEN_WIDTH, cursor_y - Start.SCREEN_HEIGHT , (int)(linespace * scale));
//		}else if (cursor_y > Start.SCREEN_HEIGHT * 2){
//			add_c.setBitmap(BaseBitmap.addBitmapList.get(1));
//			Log.e("CurrentPos", "x:"+cursor_x+" y:"+(cursor_y-Start.SCREEN_HEIGHT * 2));
//			
////			drawCursor(add_c, mp, cursor_x - Start.SCREEN_WIDTH, cursor_y - Start.SCREEN_HEIGHT * 2 , (int)(EditableCalligraphyItem.MinHeight * scale));
//			drawCursor(add_c, mp, cursor_x - Start.SCREEN_WIDTH, cursor_y - Start.SCREEN_HEIGHT * 2 , (int)(linespace * scale));
//		}
		int y;
		if(EditableCalligraphy.flip_dst > BaseBitmap.TITLE_HEIGHT){
			y = cursor_y - EditableCalligraphy.flip_dst + BaseBitmap.TITLE_HEIGHT;
		}
		else{
			y = cursor_y;
//			Log.e("drawcursor", "cursory:" + y);
		}
		
		
//		drawCursor(c, mp, cursor_x, cursor_y , (int)(linespace * scale));
		if(!available.getZoomable())
			drawCursor(c, mp, cursor_x, y ,available.getAlinespace());
		else
			drawCursor(c, mp, cursor_x, y ,linespace);
		
		mv.postInvalidate();
	}
	public void drawCurrentCursor_vertical(int color){
		
		Paint mp = new Paint();
		mp.setColor(color);

		drawCursor_vertical(c, mp, cursor_x + EditableCalligraphy.flip_Horizonal_dst, cursor_y , (int)(available.getAlinespace() * scale));//更改图标适应
		
		mv.postInvalidate();
	}
	public void dispearPreCursor(){
		Paint mp = new Paint();
		mp.setColor(Color.WHITE);
		
		if(available.getDirect() == 1){
			dispearPreCursor_vertical();
			return;
		}
		
		
		drawCursor(c, mp, temp_cursor_x, temp_cursor_y , linespace);
//		if(temp_cursor_y > Start.SCREEN_HEIGHT && cursor_y < Start.SCREEN_HEIGHT * 2){
//			
//			add_c.setBitmap(BaseBitmap.addBitmap );
//			drawCursor(add_c, mp, cursor_x - Start.SCREEN_WIDTH, temp_cursor_y - Start.SCREEN_HEIGHT , linespace);
//		}else if( cursor_y > Start.SCREEN_HEIGHT * 2){
//			add_c.setBitmap(BaseBitmap.addBitmapList.get(1));
//			Log.e("CurrentPos", "x:"+cursor_x+" y:"+(cursor_y-Start.SCREEN_HEIGHT * 2));
//			
//			drawCursor(add_c, mp, cursor_x - Start.SCREEN_WIDTH, cursor_y - Start.SCREEN_HEIGHT * 2 , (int)(linespace * scale));
//		}
	}
	public void dispearPreCursor_vertical(){
		Paint mp = new Paint();
		mp.setColor(Color.WHITE);

		drawCursor_vertical(c, mp, temp_cursor_x + EditableCalligraphy.flip_Horizonal_dst, temp_cursor_y , (int)(available.getAlinespace() * scale));//更改图标适应
		
	}
	public void dispearCurrentCursor(){
		Paint mp = new Paint();
		mp.setColor(Color.WHITE);
		drawCursor(c, mp, cursor_x, cursor_y , linespace);
	}
	
	public void setBitmapOffset(int x , int y)
	{
		bitmapOffsetX=x;
		bitmapOffsetY=y;
	}
	public int getSize(){
		return charList.size();
	}

	public int getBottomY() {
		return bottomY;
	}

	@Override
	public void undo(Bitmap b) {
		// TODO Auto-generated method 
		List<EditableCalligraphy> list = CursorDrawBitmap.listEditableCalligraphy;
		list.get(this.id).charList = this.charList;
		list.get(this.id).currentpos = this.currentpos;
		
		
		
//		setCursorXY(this.cursor_x, this.cursor_y);
		for(int i=0;i<list.size();i++){
			list.get(i).update(b,false);
		}
		
		
		
	}
	
	public void initDatabaseCharList(){
		Log.e("init", "initDatabaseCharList by available_id");
		
		CDBPersistent db = new CDBPersistent(mv.getContext());
		
		db.open();
		resetCurrentCount();//重置矢量化文字数量
		mindMapList.clear();
//		charList = CalligraphyVectorUtil.instance().getEditableListByParsedWordList(id,available.getZoomable());
		charList = CalligraphyDB.getInstance(Start.context).getCharList(id,available.getZoomable(),mindMapList);
		Log.v("charscount", "available_id:" + available.getAid() + " initDataChar charList.size :" + charList.size());
		initCurrentCount();
		
		db.close();
		
	}
	public void initFlipBlock(){
		if(available.getDirect() != 1){
			//横版初始化滑块
			Log.e("Start", "null"+flip_bottom);

			if(flip_bottom > FLIPSTART_LIMIT_BOTTOM){
				int t = (int)(((float)flip_dst/flip_bottom) * Start.SCREEN_HEIGHT);
				
				Log.e("Start", (Calligraph.flipblockBtn == null) +" null");
				Calligraph.flipblockBtn.setVisibility(View.VISIBLE);
				//Calligraph.TestButton.setVisibility(View.VISIBLE);
				
				
				Calligraph.flipblockLayout.setLayout(t);
				Calligraph.flipblockHBtn.setVisibility(View.GONE);
				Log.e("Start", "bottom: flip_bottom="+ flip_bottom);
			}
		}else{
			//竖版初始化滑块
			Log.e("dst", "Horizonal init flip_Horizonal_bottom"+flip_Horizonal_bottom); 
			if(flip_Horizonal_bottom == 0){
				flip_Horizonal_bottom = Start.SCREEN_WIDTH;
			}
			if(flip_Horizonal_bottom < Start.SCREEN_WIDTH){
				int t = (int)(((float)Start.SCREEN_WIDTH/(Start.SCREEN_WIDTH * 2 - flip_Horizonal_bottom)) * Start.SCREEN_WIDTH);
				
				Log.e("Start", (Calligraph.flipblockBtn == null) +" null");
				Calligraph.flipblockHBtn.setVisibility(View.VISIBLE);
				Calligraph.flipblockHLayout.setLayout(t);
				
				Log.e("Start", "bottom: flip_bottom="+ flip_Horizonal_bottom);
			}
		}
	}

	@Override
	public void execute() {
		// TODO Auto-generated method stub
		
	}
	
	
	
	public void saveAllCharToBitmap(Bitmap b, Matrix scaleMatrix, boolean isIndex) {
/*
		if(available.getZoomable()){
			
		}else{
			matrix = new Matrix();
		}
		*/
		Paint p = new Paint();
		int end_x_ = available.getEndX();
//		matrix.getValues(values);
//		scale = values[0];
		Log.i("BitmapScale", "Scale in update bitmap  " + scale);
		float dScale ;
		float width = 0.0f;
		
		c = new Canvas();
		c.setBitmap(b);
		
		int start_x_ =available.getStartX(); 
		int x=start_x_;
		int y=start_y;
		
		float[] val =  new float[9];
		scaleMatrix.getValues(val);
		float shareScale = val[0];
		c.scale(shareScale, shareScale);
		
		float tt = 0;
		float maxHeight=0;;
		float scale;
		
		Log.e("shareScale", "start x:" + x);

		
		Log.e("shareScale", "shareScale:" + shareScale);
		Bitmap tmp = null;
		boolean recycleFlag = false;
		for(int i =0; i < charList.size() ; i++){
			if(isIndex && i > 20){
					return;
			}
//			matrix.getValues(values);
//			scale = values[0];
//			dScale = scale;
			scale = 1;
			dScale = 1;
			Log.i(TAG, "shareScale:" + scale);
			
			//获得单个字
			EditableCalligraphyItem e = charList.get(i);
			e.getMatrix().getValues(values);
			//获得字生成时的缩放比例
			dScale /= values[0];
			
			tt = values[0];
			
			if(maxHeight < (linespace + VMargin)*scale)
				maxHeight = (linespace + VMargin)*scale;
			
			Log.e("shareScale", "end_x_:" + end_x_ + " x:" + x);
			
			if(e.getWidth()  > end_x_ - x && x != start_x_){
//				Log.v("test", "update!!!!!!!!!!!!!e.getWidth() > endX - x:");
				if(available.getLinenumber() != 1){
					x = start_x_;
//					Log.e("update", "-MiniHeight:"+ linespace);
					y += maxHeight;
					maxHeight=0;
				}else{
					continue;
				}
			}
			if(e.getWidth() > end_x_ - start_x_ && x != start_x_)
			{
				Log.v("test", "update!! continue!!!!!!!!!!!!!!i =:"+i);
				continue;
			}
			
			
			if(maxHeight < e.getHeight()){
				
				maxHeight = e.getHeight();
				int tem = 0;
				if(maxHeight % (int)(linespace * scale) != 0){
					tem = (int) (maxHeight / (int)(linespace * scale));
					tem ++;
					maxHeight = tem * linespace * scale;
				}
			
			}
			
			
			
			if(i == this.currentpos){
				//光标被改变到此位置。 绘制光标
//				Log.v("test", "update!!!!!!!!!!!!!!!!i = this.currentpos:"+i);
//				drawCursor(c, p, x, y , EditableCalligraphyItem.MinHeight);
				setCursorXY(x, y);
				dispearPreCursor();
			} 
			if(e.type == EditableCalligraphyItem.Types.EndofLine){
				Log.i(TAG, "end of line");
				e.setCurPos(x, y);
				x = start_x_;
//				Log.e("endofline", "MiniHeight:"+ linespace);
				y += maxHeight;
				maxHeight=0;
				continue;
			}
			

			
			if(e.type != EditableCalligraphyItem.Types.Space && e.type != EditableCalligraphyItem.Types.EnSpace){
				
				//画所有的字
				tmp = e.getCharBitmap();
				if(tmp == null)
					continue;//切换目录可能产生空指针
				
				if(tmp.isRecycled()){
					recycleFlag = true;
					Log.e("sharerecycle", "before recycled" + e.getCharBitmap().isRecycled());
					if(e.type == Types.CharsWithStroke)
						e = (new resetBitmapWork((VEditableCalligraphyItem)e, "")).execute();
					else if(e.type == Types.ImageItem)
						e = (new resetImageWork(e, "")).execute();
					Log.e("sharerecycle", "after recycled" + e.getCharBitmap().isRecycled());
					
					tmp = e.getCharBitmap();
				}
				
				if(tmp == null){
					System.out.println(" !!!null:" + ( tmp == null)); 
				}else{
					
//				System.out.println("type:"+ e.type + "! null:" + ( tmp == null)); 
				float pad = 0.0f;
				
				if(available.getAlinespace() != 0){
					CursorDrawBitmap.mIntervalHeight = available.getAlinespace();
				}
				
				if(tmp.getHeight()/tt < CursorDrawBitmap.mIntervalHeight) {
					pad = (CursorDrawBitmap.mIntervalHeight  - 
							tmp.getHeight()/tt)  /2;
//					Log.e("pad", "i:"+i);
					if(i == 1){
//					Log.e("pad", "CursorDrawBitmap.mIntervalHeight:"+CursorDrawBitmap.mIntervalHeight);
//					Log.e("pad", "tt:"+tt);
//					Log.e("pad", "tmp.getHeight():"+tmp.getHeight());
					}
				}
				Log.i(TAG, "tmp height:" + tmp.getHeight() + " pad:" + pad);
//				c.drawBitmap(tmp, x, y + pad, p);
				
				int temp_flip_dst = 0;
				Matrix mm = new Matrix();
				if(temp_flip_dst < BaseBitmap.TITLE_HEIGHT)
					mm.postTranslate(x,y+pad);//test -70
				else{
					int t = y+ (int)pad - temp_flip_dst + BaseBitmap.TITLE_HEIGHT;
//					mm.postTranslate(x,y+pad - flip_dst + BaseBitmap.TITLE_HEIGHT);//test -70
					mm.postTranslate(x,t);//test -70
					
				}
//				mm.preScale(scale, scale);
				//是图片不缩放
				if(e.getCharType() == 7 && mv.getTouchMode() != mv.getCursorScaleMode()){
					mm.preScale(1, 1);
				}else
					mm.preScale(dScale, dScale);
//				mm.preTranslate(0, - flip_dst);
				

//				System.out.println(" scale:"+scale);
				
				
				
					c.drawBitmap(tmp, mm, p);
				
				
//				Log.v("test", "update!!!!!!!!!!!!!e.type != EditableCalligraphyItem.Types.Space:");
			}//end null if
			}//end  if
			
			e.setCurPos(x, y);
//			x += e.getWidth()*scale  + HMargin;
			x += e.getWidth()*dScale  + HMargin * scale;
			if(tmp != null && !tmp.isRecycled() && recycleFlag){
				e.recycleBitmap();
				BitmapCount.getInstance().recycleBitmap("EditableCalligraphy saveAllCharToBitmap tmp");	
			}
		}//end for
		if( this.currentpos >= charList.size()){
			Log.v("test", "update!! this.currentpos:"+this.currentpos+">= charList.size()"+charList.size());
//			dispearCursor(c, p, pre_x, pre_y, EditableCalligraphyItem.MinHeight);
//			this.drawCursor(c, p, x, y, EditableCalligraphyItem.MinHeight);
			setCursorXY(x, y);
			dispearCurrentCursor();
//			this.drawCursor(c, p, cursor_x, cursor_y, EditableCalligraphyItem.MinHeight);
		}
		
//		Log.e("bottomy", "!!!!!!!: y"+ y + "> bottomY:"+bottomY +" "+(y>bottomY));
		
//		if(available.getAid() == 4){
//			
//			bottomY = y;
//			setFlipDst(false);
//			Log.e("bottomy", "!!!!!!!!!! bottomy ="+ bottomY );
//		}
		
	
	}
	
	
	
	
	
	public void saveAllCharToBitmap2(Bitmap b, Matrix scaleMatrix) {
		
		int end_x_ = b.getWidth() + Start.SCREEN_WIDTH;
//		if(available.getDirect() == 1){
//			update_vertical_fromRight(m, matrix);
//			return;
//		}
//		Log.e("matrix", matrix.toString());
		Log.e("matrix", "zoom"+available.getZoomable() + " id:" + available.getAid());
		
		if(available.getZoomable()){
			
		}else{
//			posMatrix = new Matrix();
		}
		Paint p = new Paint();
		
		scaleMatrix.getValues(values);
		scale = values[0];
		Log.i("BitmapScale", "Scale in update bitmap  " + scale);
		float dScale ;
		float width = 0.0f;
		
//		this.mb = m;
		c = new Canvas();
		c.setBitmap(b);
		
		int start_x_ =available.getStartX(); 
		int x=start_x_;
		int y=start_y;
		
		
		float tt = 0;
		float maxHeight=0;;
		for(int i =0; i < charList.size() ; i++) {
			
			scaleMatrix.getValues(values);
			scale = values[0];
			Log.e("sharescale", "scale :" + scale);
			dScale = scale;
			Log.e("sharescale", "dscale :" + dScale);
			
			Log.i(TAG, "scale:" + scale);
			EditableCalligraphyItem e = charList.get(i);
			e.getMatrix().getValues(values);
			dScale /= values[0];
			Log.e("sharescale", "dscale :" + dScale  + " value0:" + values[0]);
			
			
			tt = values[0];
			
			if(maxHeight < (linespace + VMargin)*scale)
				maxHeight = (linespace + VMargin)*scale;
			
			//判断本行是否已满，是否要换行
			if(e.getWidth()  > (end_x_  - x)   && x != start_x_){
//				Log.v("test", "update!!!!!!!!!!!!!e.getWidth() > endX - x:");
				if(available.getLinenumber() != 1){
					x = start_x_;
//					Log.e("update", "-MiniHeight:"+ linespace);
					y += maxHeight;
					maxHeight=0;
				}else{
					continue;
				}
			}
			if(e.getWidth() > end_x_ - start_x_ && x != start_x_)
			{
				Log.v("test", "update!! continue!!!!!!!!!!!!!!i =:"+i);
				continue;
			}
			if(maxHeight < e.getHeight()){
				
				maxHeight = e.getHeight();
				int tem = 0;
				if(maxHeight % (int)(linespace * scale) != 0){
					tem = (int) (maxHeight / (int)(linespace * scale));
					tem ++;
					maxHeight = tem * linespace * scale;
				}
			
			}
			
			
			
			if(i == this.currentpos){
				//光标被改变到此位置。 绘制光标
//				Log.v("test", "update!!!!!!!!!!!!!!!!i = this.currentpos:"+i);
//				drawCursor(c, p, x, y , EditableCalligraphyItem.MinHeight);
				setCursorXY(x, y);
				dispearPreCursor();
			} 
			
			//回车换行
			if(e.type == EditableCalligraphyItem.Types.EndofLine){
				Log.i(TAG, "end of line");
				e.setCurPos(x, y);
				x = start_x_;
//				Log.e("endofline", "MiniHeight:"+ linespace);
				y += maxHeight;
				maxHeight=0;
				continue;
			}
			

			
			if(e.type != EditableCalligraphyItem.Types.Space && e.type != EditableCalligraphyItem.Types.EnSpace){
				
				//画所有的字
				Bitmap tmp = e.getCharBitmap();
				if(tmp == null){
					System.out.println(" !!!null:" + ( tmp == null)); 
				}else{
					
				System.out.println("type:"+ e.type + "! null:" + ( tmp == null)); 
				float pad = 0.0f;
				
				if(available.getAlinespace() != 0){
					CursorDrawBitmap.mIntervalHeight = available.getAlinespace();
				}
				
				if(tmp.getHeight()/tt < CursorDrawBitmap.mIntervalHeight) {
					pad = (CursorDrawBitmap.mIntervalHeight  - 
							tmp.getHeight()/tt)  /2;
					Log.e("pad", "i:"+i);
					if(i == 1){
					Log.e("pad", "CursorDrawBitmap.mIntervalHeight:"+CursorDrawBitmap.mIntervalHeight);
					Log.e("pad", "tt:"+tt);
					Log.e("pad", "tmp.getHeight():"+tmp.getHeight());
					}
				}
				Log.i(TAG, "tmp height:" + tmp.getHeight() + " pad:" + pad);
//				c.drawBitmap(tmp, x, y + pad, p);
				
				
				Matrix mm = new Matrix();
//				if(flip_dst < BaseBitmap.TITLE_HEIGHT)
//					mm.postTranslate(x,y+pad);//test -70
//				else{
					int t = y+ (int)pad;
//					mm.postTranslate(x,y+pad - flip_dst + BaseBitmap.TITLE_HEIGHT);//test -70
					mm.postTranslate(x,t);//test -70
					
//				}
//				mm.preScale(scale, scale);
				//是图片不缩放
				if(e.getCharType() == 7 && mv.getTouchMode() != mv.getCursorScaleMode()){
					mm.preScale(1, 1);
				} else
					mm.postScale(dScale, dScale);
				
				Log.e("sharescale", "dscale :" + dScale);
				
//				mm.preTranslate(0, - flip_dst);
				

				System.out.println(" scale:"+scale);
				
				
				
					c.drawBitmap(tmp, mm, p);
//				Log.v("test", "update!!!!!!!!!!!!!e.type != EditableCalligraphyItem.Types.Space:");
			}//end null if
			}//end  if
			
			e.setCurPos(x, y);
			x += e.getWidth()  + HMargin * dScale;
			
		}//end for
		if( this.currentpos >= charList.size()){
			Log.v("test", "update!! this.currentpos:"+this.currentpos+">= charList.size()"+charList.size());
//			dispearCursor(c, p, pre_x, pre_y, EditableCalligraphyItem.MinHeight);
//			this.drawCursor(c, p, x, y, EditableCalligraphyItem.MinHeight);
			setCursorXY(x, y);
			dispearCurrentCursor();
//			this.drawCursor(c, p, cursor_x, cursor_y, EditableCalligraphyItem.MinHeight);
		}
		
//		Log.e("bottomy", "!!!!!!!: y"+ y + "> bottomY:"+bottomY +" "+(y>bottomY));
		
//		if(available.getAid() == 4){
//			
//			bottomY = y;
//			setFlipDst(false);
//			Log.e("bottomy", "!!!!!!!!!! bottomy ="+ bottomY );
//		}
		
	
	}

	/*
	 * 重新生成bitmap
	 */
	public void scaleResetCharList(float scale) {
		Log.e("resetscale", "last:" + recycle_lastIndex + " first" + recycle_firstIndex);
		int start = recycle_lastIndex;
		int end = 0;
		if(recycle_firstIndex <= charList.size())
			end = recycle_firstIndex;
		else
			end = charList.size();
		
		scaleResetCharList(start,end);
	}
	
	/*
	 * 重新生成bitmap
	 */
	public void scaleResetCharList(int item_start,int item_end) {
		// TODO Auto-generated method stub
		
		int color = Color.BLACK;
		if(!available.getZoomable())
			return;
		VEditableCalligraphyItem vitem = null;
		Path mPath = null;
		Paint mPaint = new Paint();
//		mPaint.setColor(Color.BLACK);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeWidth(2);
		Path tmpPath;
		Bitmap tempB;
		float Right_X,Left_X,Bottom_Y,Top_Y;
		for(int i=item_start;i<item_end;i++){
//			Log.e("vector", "vitem mpath>>>>>>>>>>>>>>>>>>>\n:"+ charList.get(i).type);
			if(charList.get(i).type == Types.CharsWithStroke){
				vitem = (VEditableCalligraphyItem)charList.get(i);
//				Log.e("vector", "vitem mpath>>>>>>>>>>>>>>>>>>>\n:" + vitem.getmPath() + " Bottom_Y:" + vitem.getBottom_Y());
				
				color = vitem.getmColor();
				mPaint.setColor(color);
				
				tempB = vitem.charBitmap;
				if(!tempB.isRecycled()){
					vitem.recycleBitmap();
					BitmapCount.getInstance().recycleBitmap("EditableCalligraphy scaleResetCharList tempB");
//					Log.v("scalereset", "not recycled");
				}else{
//					Log.v("scalereset", "recycled");
					continue;
				}
				
				mPath = vitem.getmPath();
				Right_X = vitem.getRight_X();
				Left_X = vitem.getLeft_X();
				Top_Y = vitem.getTop_Y();
				Bottom_Y = vitem.getBottom_Y();
				
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
//				Log.e("pathscale", "-->distx:" + distX + " distY:" + distY);
				
				float scale = CalligraphyVectorUtil.getScaled(Right_X - Left_X ,Bottom_Y - Top_Y);
				scale *= values[0];
//				Log.e("cursorScale", "scale:" + scale);
				float height = Bottom_Y - Top_Y;
				
//				Log.e("vectorScal", "scale:" + values[0]);
//				Log.e(TAG, "End create Bitmap:>>>>>>>>>>>>>>" + (mPath == null));
				tmpPath = new Path(mPath);
//				Log.e(TAG, "End create Bitmap:>>>>>>>>>>>>>>" );
				
				Bitmap b;
				try {
					int sizeY=(int)((endy - starty)*scale);
					int sizeX=(int)((endx - startx)*scale);
					if(sizeX <0)
						sizeX *= -1;
					else if(sizeX == 0)
						sizeX = 1;
					if(sizeY <0)
						sizeY *= -1;
					else if(sizeY == 0)
						sizeY = 1;
					//b = Bitmap.createBitmap(sizeX, sizeY, Bitmap.Config.ARGB_8888);
					b = Bitmap.createBitmap(sizeX, sizeY, Bitmap.Config.ARGB_4444);
					BitmapCount.getInstance().createBitmap("EditableCalligraphy scaleResetCharList");
					
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
							
				vitem.charBitmap = b;
				vitem.resetWidthHeight();
				if(i>=recycle_firstIndex){
					vitem.recycleBitmap();
					BitmapCount.getInstance().recycleBitmap("EditableCalligraphy scaleResetCharList i>=recycle_firstIndex");
					
//					Log.e("resetscale", "recycle" + i);
				}
				
				vitem.setMatrix(currentMatrix);
				
			}
			
		}
//		System.gc();
	}
	
	
	
	
	
	class resetImageWork implements Runnable{
		EditableCalligraphyItem e = null;
		public String identity = "";
		public resetImageWork(EditableCalligraphyItem e, String iden){
			this.e = e;
			this.identity = iden;
		}
		
		@Override
		public boolean equals(Object o) {
			// TODO Auto-generated method stub
			
			return identity.equals(((resetImageWork)o).identity);
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
		
	};
	
	
	/*
	 * 重新生成bitmap
	 */
	public EditableCalligraphyItem ResetChar(int index) {
		// TODO Auto-generated method stub
		
		VEditableCalligraphyItem vitem = null;
		Path mPath = null;
		Paint mPaint = new Paint();
		mPaint.setColor(Color.BLACK);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeWidth(2);
		Path tmpPath;
		Bitmap tempB;
		float Right_X,Left_X,Bottom_Y,Top_Y;
		
			if(charList.get(index).type == Types.CharsWithStroke){
				vitem = (VEditableCalligraphyItem)charList.get(index);
//				Log.e("vector", "vitem mpath>>>>>>>>>>>>>>>>>>>\n:" + vitem.getmPath() + " Bottom_Y:" + vitem.getBottom_Y());
				
				tempB = vitem.charBitmap;
				if(!tempB.isRecycled()){
					vitem.recycleBitmap();
					BitmapCount.getInstance().recycleBitmap("EditableCalligraphy ResetChar tempB");
				}
				
				mPath = vitem.getmPath();
				Right_X = vitem.getRight_X();
				Left_X = vitem.getLeft_X();
				Top_Y = vitem.getTop_Y();
				Bottom_Y = vitem.getBottom_Y();
				
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
					return null;
//				Log.e("pathscale", "-->distx:" + distX + " distY:" + distY);
				
				float scale = CalligraphyVectorUtil.getScaled(Right_X - Left_X ,Bottom_Y - Top_Y);
				scale *= values[0];
				
				float height = Bottom_Y - Top_Y;
				
				Log.e("vectorScal", "scale:" + values[0]);
//				Log.e(TAG, "End create Bitmap:>>>>>>>>>>>>>>" + (mPath == null));
				tmpPath = new Path(mPath);
//				Log.e(TAG, "End create Bitmap:>>>>>>>>>>>>>>" );
				
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
					//b = Bitmap.createBitmap(sizeX, sizeY, Bitmap.Config.ARGB_8888);
					b = Bitmap.createBitmap(sizeX, sizeY, Bitmap.Config.ARGB_4444);
					BitmapCount.getInstance().createBitmap("EditableCalligraphy EditableCalligraphyItem ResetChar");
				} catch (OutOfMemoryError e) {
					// TODO: handle exception
					b = Start.OOM_BITMAP;
				}
				
				b.eraseColor(Color.TRANSPARENT);
				Canvas mc = new Canvas();
				mc.setBitmap(b);
				
				Matrix scaleMatrix = new Matrix();
				scaleMatrix.setScale(scale, scale);
				tmpPath.offset(-Left_X, -Top_Y);
				tmpPath.transform(scaleMatrix);
				
				mc.drawPath(tmpPath, mPaint);
				
				tmpPath.reset();
				
				
				vitem.charBitmap = b;
				vitem.resetWidthHeight();
				vitem.setMatrix(currentMatrix);
				
			}
			
		
		return vitem;
	}
	
	
	public void recycleCharListBitmap(int lastIndex) {
		long t = Calendar.getInstance().getTimeInMillis();
//		Log.v("recyclebitmap", "                   recycle available:" + available.getAid() + 
//				"|| recycle charlist from 0 to" + lastIndex + " !!");
		
		if(lastIndex > RECYCLE_LIMIT){
//			Log.v("recyclebitmap", "need recycle word from 0 to " + (lastIndex - RECYCLE_LIMIT));
			lastIndex = lastIndex - RECYCLE_LIMIT;
		}
		else{
//			Log.v("recyclebitmap", lastIndex + " < " + RECYCLE_LIMIT + " no need to recycle return");
			return;
		}
			
		
		EditableCalligraphyItem e;
		String iden = "";
		if(lastIndex != 1)
		for(int i = 0;i<lastIndex;i++){
			iden = "a" + available.getAid() + "i" + i;
			if(i<charList.size()){
				e = charList.get(i);
				
				
				
				if(t - e.time > 2000){
//					Log.v("flipper", " recycleCharListBitmap " + i);
					
					if(EditableCalligraphyItem.getType(e.getCharType()) == Types.CharsWithStroke
							&& (e.getCharBitmap() != null) && !e.getCharBitmap().isRecycled()){
//						e.getWord().recycle();
//						Log.v("saveword", "word null:" + (e.getWord() == null));
//						Log.v("saveword", " isRecycle:" + e.getWord().isRecycle());
						e.recycleBitmap();
						BitmapCount.getInstance().recycleBitmap("EditableCalligraphy recycleCharListBitmap" + i);
						
//						workList.remove(iden);
//						WorkQueue.getInstance().removeIdentify(iden);
//						Log.v("flipper", "  !!recycleCharListBitmap " + i);
					}
					if(EditableCalligraphyItem.getType(e.getCharType()) == Types.ImageItem
							&&(e.getCharBitmap() != null) && !e.getCharBitmap().isRecycled()){
						e.recycleBitmap();
						BitmapCount.getInstance().recycleBitmap("EditableCalligraphy recycleCharListBitmap" + i);
//						Log.v("flipper", "  !!recycleCharListBitmap " + i);
//						workList.remove(iden);
//						WorkQueue.getInstance().removeIdentify(iden);
//						Log.e("pool", ">>>>>>>>>>>>>>>>>recycle imageItem:" + iden);
					}
					
				}
			}
		}
	}

	public void recycleCharListBitmapBottom(int firstIndex) {
		long t = Calendar.getInstance().getTimeInMillis();
		
		int size = charList.size();
		if(firstIndex > size){
//			Log.v("recyclebitmap", "		firstIndex" + firstIndex + " > size " + size + " return");
			return;
		}
//		Log.v("recyclebitmap", "                   recycle available:" + available.getAid() + 
//				"|| recycle charlist from " + firstIndex + " to" + charList.size() + " !!");
		
		if((size - firstIndex) > RECYCLE_LIMIT){
//			Log.v("recyclebitmap", "need recycle from " + (firstIndex + RECYCLE_LIMIT) + " to " + size);
			firstIndex += RECYCLE_LIMIT;
		}
		else{
//			Log.v("recyclebitmap", firstIndex + " to " + size + " < " + RECYCLE_LIMIT + " no need to recycle");
			return;
		}
		
		EditableCalligraphyItem e;
		String iden = "";
		if(firstIndex != charList.size())
			for(int i = firstIndex;i<charList.size();i++){
				iden = "a" + available.getAid() + "i" + i;
				e = charList.get(i);
				if(e == null){
//					Log.e("error", "recycleCharListBitmapBottom editable item null");
					continue;
				}
				if(EditableCalligraphyItem.getType(e.getCharType()) == Types.EndofLine
						|| EditableCalligraphyItem.getType(e.getCharType()) == Types.Space
						|| EditableCalligraphyItem.getType(e.getCharType()) == Types.EnSpace
						|| EditableCalligraphyItem.getType(e.getCharType()) == Types.AUDIO
						|| EditableCalligraphyItem.getType(e.getCharType()) == Types.VEDIO
						|| EditableCalligraphyItem.getType(e.getCharType()) == Types.ImageItem		
				){
//					Log.v("flipper", "              !!recycleCharListBitmapBottom " + i + " " + 
//							EditableCalligraphyItem.getType(e.getCharType()));
					continue;
				}
				
				
				
				
				
				if(t - e.time > 2000){
//					Log.v("flipper", "              !!!!!!!recycleCharListBitmapBottom " + i + " " + 
//							EditableCalligraphyItem.getType(e.getCharType()));
					if(EditableCalligraphyItem.getType(e.getCharType()) == Types.CharsWithStroke
							&& (e.getCharBitmap() != null) && !e.getCharBitmap().isRecycled()){
						e.recycleBitmap();
						BitmapCount.getInstance().recycleBitmap("EditableCalligraphy recycleCharListBitmapBottom Types.CharsWithStroke");
//						Log.v("flipper", "  !!recycleCharListBitmapBottom " + i);
//						workList.remove(iden);
//						WorkQueue.getInstance().removeIdentify(iden);
//						Log.e("pool", ">>>>>>>>>>>>>>>>>recycle from firstIndex " + firstIndex + " to " + charList.size());
					}
					if(EditableCalligraphyItem.getType(e.getCharType()) == Types.ImageItem
							&&(e.getCharBitmap() != null) && !e.getCharBitmap().isRecycled()){
						e.recycleBitmap();
//						Log.e("time", "start recycle --------------");
						BitmapCount.getInstance().recycleBitmap("EditableCalligraphy recycleCharListBitmapBottom Types.ImageItem");
//						Log.v("flipper", "  !!recycleCharListBitmapBottom " + i);
//						workList.remove(iden);
//						WorkQueue.getInstance().removeIdentify(iden);
//						Log.e("pool", ">>>>>>>>>>>>>>>>>recycle imageItem:" + iden);
					}
				}
			}
	}
	public void startRecycleInVisiableBitmap(){
//		Log.v("recyclebitmap", "recycleable " + recycleable + " !FlipImageView.flipping:" +  !FlipImageView.flipping);
		if(recycleable && !FlipImageView.flipping){
			recycleCharListBitmap(recycle_lastIndex + 1);
			recycleCharListBitmapBottom(recycle_firstIndex + 1);
//			Log.e("flip", "last:" + recycle_lastIndex + " first:" + recycle_firstIndex);
			
		}
	}
	
	private void addCurrentCount(){
		count ++;
	}
	private int getCurrentCount(){
		return count;
	}
	private void initCurrentCount(){
		EditableCalligraphyItem item = null;
		for(int i=0;i<charList.size();i++){
			item = charList.get(i);
//			if(EditableCalligraphyItem.getType(item.getCharType()) == Types.CharsWithStroke){
			if(item.type == Types.CharsWithStroke || item.type == Types.CharsWithoutStroke || item.type == Types.ImageItem 
					|| item.type == Types.AUDIO ||item.type == Types.VEDIO){
				count++;
				Log.v("charscount", "available_id:" + available.getAid() 
						+ "count:" + count);
			}
		}
		count = CalligraphyDB.getInstance(Start.context).getCurrentWordCount(Start.getPageNum(), available.getAid() -1);
		count ++;
	}
	public void resetCurrentCount(){
		count = 0;
	}
	private void resetRecycleLimit(float scale){
		
		if(scale > 1 && scale <= 1.3){
			RECYCLE_LIMIT = 70;
			WorkQueue.getInstance().resetQueueSize(250);
		}else if(scale > 1.3 && scale <= 1.6){
			RECYCLE_LIMIT = 50;
			WorkQueue.getInstance().resetQueueSize(150);
		}else if(scale > 1.6 && scale <= 2){
			RECYCLE_LIMIT = 20;
			WorkQueue.getInstance().resetQueueSize(130);
		}else if(scale > 2 && scale <= 2.5){
			RECYCLE_LIMIT = 15;
			WorkQueue.getInstance().resetQueueSize(80);
		}else if(scale > 2.5){
			RECYCLE_LIMIT = 10;
			WorkQueue.getInstance().resetQueueSize(30);
		}
		else if( scale <= 1){
			RECYCLE_LIMIT = 100;
			WorkQueue.getInstance().resetQueueSize(300);
		}
			
//		Log.e("limit", "scale:" + scale + " recycle_limit:" + RECYCLE_LIMIT);
		
	}
	
	public void preUpdate(Bitmap m,boolean flip)
	{
		bottomflag = true;
		recycleable = false;
		drawMapBegin = false;
		
		
		Matrix matrix;
		if(Start.c == null)
			matrix = Start.m;
		else
			matrix = Start.c.view.getMMMatrix();
		
		
		
		
		if(available.getDirect() == 1){
			return;
		}
		
		Paint p = new Paint();
		
		matrix.getValues(values);
		scale = values[0];
		linespace = (int)(available.getAlinespace() * values[4]);
		float dScale ;//应该显示的缩放比例
		float width = 0.0f;
		
		this.mb = m;
		c = new Canvas();
		c.setBitmap(m);
		
		x=start_x;
		y=start_y;
		
		float tt = 0;
		float maxHeight=0;
		
		if(charList.size() != 0){
			mv.addRowNumber(available.getControltype());
		}
		
		for(int i =0; i < charList.size() ; i++){
			String iden = "a" + available.getAid() + "i" + i;
			matrix.getValues(values);
			scale = values[0];
			dScale = scale;
			
			EditableCalligraphyItem e = null;
			
			try {
				e = charList.get(i);
			} catch (ClassCastException e2) {
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
			
			if(maxHeight < linespace){ 
				maxHeight = linespace;
			}
			
			if(e.getWidth()  > end_x - x && x != start_x){
				if(available.getLinenumber() != 1){
					x = start_x;
					y += maxHeight;
					maxHeight=0;
					mv.addRowNumber(available.getControltype());
				}else{
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
					if(maxHeight % (linespace ) != 0){
						tem = (int) (maxHeight /(linespace));
						tem ++;
						maxHeight = tem * linespace;
					}
				
				}
			}
			
			if(i == this.currentpos){
				//光标被改变到此位置。 绘制光标
				setCursorXY(x, y);
				dispearPreCursor();
			} 
			if(e.type == EditableCalligraphyItem.Types.EndofLine){
				e.setCurPos(x, y);
				x = start_x;
				y += maxHeight;
				maxHeight=0;
				mv.addRowNumber(available.getControltype());
				drawMapBegin = false;
				continue;
			}
			

			//画导图
			if(e.isSpecial()){
				mapItem = e.getMindMapItem();
				
				if(mapItem.hasParent()){
					LogUtil.getInstance().e("drawmindmap", "mindmapitem mindid:" + mapItem.getMindID() + " hasParent:" 
							+ mapItem.hasParent() + " parentid:" + mapItem.getParentID() + " continue");
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
				LogUtil.getInstance().e("drawmindmap", "mindmapitem:" + e.getMindMapItem().getMindID() + " start update y:" + y + 
						" itemid:" + e.getItemID());
				updateMindMap(mapItem, p,x+mapItem.getFlipDstX(),false);
				LogUtil.getInstance().e("prebottom", "after y:" + y);
//				i+= 13;
			}else{
				if(e.type != EditableCalligraphyItem.Types.Space && e.type != EditableCalligraphyItem.Types.EnSpace){
					
					//画所有的字
					Bitmap tmp = e.getCharBitmap();
						
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
						}
						if(e.type == Types.ImageItem){
							if(e.getWidth()* dScale > Start.SCREEN_WIDTH)
								x += e.getFlipDstX();
						}
						Matrix mm = new Matrix();
						if(flip_dst < BaseBitmap.TITLE_HEIGHT){
							mm.postTranslate(x,y+pad);//test -70
						}else{
							int t = y+ (int)pad - flip_dst + BaseBitmap.TITLE_HEIGHT;
							mm.postTranslate(x,t);//test -70
							
						}
						if(e.type == Types.CharsWithoutStroke || 
								e.type == Types.AUDIO ||
								e.type == Types.VEDIO ||
								e.type == Types.ImageItem){
							mm.preScale(dScale, dScale);
						}
						
						mm.getValues(recycleValues);
						
					
				}//end  if
				drawMapBegin = false;
				if(e != null){
				if(e.type == Types.ImageItem)
					e.setCurPos(x, y);
					x += e.getWidth()*dScale  + HMargin;
				}
			}
		}//end for
		if( this.currentpos >= charList.size()){
			setCursorXY(x, y);
			dispearCurrentCursor();
		}
		
		if(available.getAid() == 4){
			bottomY = y + (int)maxHeight;
			Log.e("fliperror", "pre update set buttomY:" + bottomY);
			setFlipDst(false,"preUpdate");//绕圈儿鸟
		}
		
	}
	
	public EditableCalligraphyItem isPic(float x,float y){

		x -= bitmapOffsetX;
		y -= bitmapOffsetY;
		
		Log.v("CurrentPos", "setCurrentPos x:"+ x +" y:"+y);
		int i =0;
		float initScale = 0;
		float[] eValues = new float[9];
		
		Rect r;
		for( i =0 ; i< charList.size(); i++){
			EditableCalligraphyItem e = charList.get(i);
			if(e.type != Types.ImageItem){
				continue;
			}
			
			//if(e.getCurPosX() > 900){
			if(e.getCurPosX() > 2000){
				Log.e("ispic", "pic out of screen");
				break;
			}
			
			e.getMatrix().getValues(eValues);
			initScale = eValues[0];
			
			r = new Rect(
					e.getCurPosX() ,
					e.getCurPosY()- flip_dst - cursorMargin,
					(int)(e.getCurPosX()+ (e.getWidth() * initScale) + HMargin),
					(int)(e.getCurPosY()+ e.getHeight() * initScale + VMargin*scale - flip_dst) + cursorMargin);
			if(r.contains((int)x, (int)y)){
				return e;
			}
		}
		return null;
	}
	
	
	public MindMapItem isMindmapFlip(float x,float y){
		if(mindMapList.size() == 0)
			return null;
		x -= bitmapOffsetX;
		y -= bitmapOffsetY;
		LogUtil.getInstance().e("mindmap", "x:" + x  +" y:" + y);
		int i =0;
		Rect r;
		MindMapItem item = null;
		int top = 0;
		int buttom = 0;
		for( i =0 ; i< mindMapList.size(); i++){
			item = (MindMapItem)mindMapList.get(i);
			if(item.getCharList() == null && item.getCharList().size() == 0)
				return null;
			EditableCalligraphyItem e;
			if(item.getCharList().size() > 0) {
				e = item.getCharList().get(0);
				top = e.getCurPosY()- flip_dst - cursorMargin;
				
				buttom = item.getButtom() - flip_dst;
				top -= (buttom-top);
			}
			Log.e("mapflip", "dst:" + flip_dst + "top:" + top + "buttom:" + buttom);
			if(y>=top && y<=buttom){
				return item;
			}
		}
		return null;
	}
	public EditableCalligraphyItem isMindmap(float x,float y){

		x -= bitmapOffsetX;
		y -= bitmapOffsetY;
		
		LogUtil.getInstance().e("mindmap", "x:" + x  +" y:" + y);
		int i =0;
		float initScale = 0;
		float[] eValues = new float[9];
		
		Rect r;
		int width;
		int height;
		for( i =0 ; i< charList.size(); i++){
			EditableCalligraphyItem e = charList.get(i);
			if(!e.isSpecial()){
				continue;
			}
			
			e.getMatrix().getValues(eValues);
			initScale = eValues[0];
			width = e.getWidth();
			height = e.getHeight();
			if(width < 50){
				width = 50;
			}
			if(height < 50){
				height = 50;
			}
			
			r = new Rect(
					e.getCurPosX() ,
					e.getCurPosY()- flip_dst - cursorMargin,
					(int)(e.getCurPosX()+ width + HMargin + 5),
					(int)(e.getCurPosY()+ height  + VMargin*scale - flip_dst) + cursorMargin + 10);
//			LogUtil.getInstance().e("mindmap", "id:" + e.getItemID() + " rect:" + r.toShortString());
			if(r.contains((int)x, (int)y)){
				return e;
			}
		}
		return null;
	}
	
	
	public boolean setSpecial(){
		if(lastLineStartID != 0  && lastLineStartID < charList.size()){
			for(int i=lastLineStartID;i<this.currentpos;i++){
				charList.get(i).setSpecial();
			}
		}
		
		return true;
	}
	
	public MindMapItem addNewMindMapItem(){
		Log.e("mindmap", "lastLineStartID:" + lastLineStartID + " currentPos:" + currentpos);
		if(lastLineStartID < charList.size() && 
				currentpos <= charList.size()//光标在最后
				&& lastLineStartID < currentpos){
			MindMapItem map = new MindMapItem();
			EditableCalligraphyItem item = null;
			
			if(lastLineStartID != 0 && charList.get(lastLineStartID -1).type != Types.EndofLine){
				int temp = currentpos;
				currentpos = lastLineStartID;
				insertEndofLine();
				currentpos = temp;
				currentpos ++;
			}
			
			Log.e("updatemind", "update mind from " + lastLineStartID + " to " + currentpos);
			for(int i=lastLineStartID;i<currentpos;i++){
				item = charList.get(i);
				if(item.type == Types.CharsWithStroke || item.type == Types.CharsWithoutStroke){
					Log.e("mindmap", "add new word " + i);
					map.addNewWord(item);
					//update databases
					CalligraphyDB.getInstance(Start.context).updateMindmapItem(Start.getPageNum(), 3, item.getItemID(), item);
				}
			}//end for
			setCurrentMindMapItem(map);
			mindMapList.add(map);
			((HandWriteMode)mv.getTouchMode()).setMindMapEditStatusTrue();
			return map;
		}
		Toast.makeText(Start.context, "不能生成导图",Toast.LENGTH_LONG).show();
		Log.e("mindmap", "return null");
		return null;
	}
	
	
	
	
}
class resetBitmapWork implements Runnable{
	VEditableCalligraphyItem e = null;
	String identity = "";
	public resetBitmapWork(VEditableCalligraphyItem e, String iden){
		this.e = e;
		this.identity = iden;
//		workList.add(iden);
//		WorkQueue.getInstance().addIdentify(iden);
	}
	
	@Override
	public boolean equals(Object o) {
		// TODO Auto-generated method stub
		return identity.equals(((resetBitmapWork)o).identity);
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
				
				tempB = e.charBitmap;
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
				
//				Log.e("vectorScal", "scale:" + values[0]);
//				Log.e("", "End create Bitmap:>>>>>>>>>>>>>>" + (mPath == null));
				tmpPath = new Path(mPath);
//				Log.e("", "End create Bitmap:>>>>>>>>>>>>>>" );
				
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
					//b = Bitmap.createBitmap(sizeX, sizeY, Bitmap.Config.ARGB_8888);
					b = Bitmap.createBitmap(sizeX, sizeY, Bitmap.Config.ARGB_4444);
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
				
				
				e.charBitmap = b;
				e.resetWidthHeight();
				e.setMatrix(currentMatrix);
				
			}
			Log.e("sharerecycle", "execute recycled" + e.charBitmap.isRecycled());
			return e;
	}
	
}
