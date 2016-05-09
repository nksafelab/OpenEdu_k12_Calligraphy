package com.jinke.calligraphy.database;

import hallelujah.cal.CalligraphyVectorUtil;
import hallelujah.cal.SingleWord;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.util.Log;

import com.jinke.calligraphy.app.branch.EditableCalligraphyItem;
import com.jinke.calligraphy.app.branch.EditableCalligraphyItem.Types;
import com.jinke.calligraphy.app.branch.ImageLimit;
import com.jinke.calligraphy.app.branch.Start;
import com.jinke.calligraphy.app.branch.VEditableCalligraphyItem;
import com.jinke.mindmap.MindMapItem;
import com.jinke.single.BitmapCount;
import com.jinke.single.LogUtil;

public class CalligraphyDB {
	
	public static int INIT_WORD_COUNT = 270;
	
	public static final int OP_ADD_WORD = 1;
	public static final int OP_ADD_MIND_WORD = 2;
	public static final int OP_DELETE_WORD = -1;

	private static final int DATEBASE_VERSION = 2;
	private static final String DATABASE_NAME = "calligraphy.db";
	
	private static final String WORD_TABLE = "word";
	private static final String PAGE_TABLE = "page";
	
	private static final String CREATE_TABLE_WORD = "create table if not exists word " +
			"(_id integer primary key autoincrement, "
			+ "template_id integer, "
			+ "pagenum integer, "
			+ "available_id integer, "
			+ "itemid integer, "
			+ "op_type integer, "
			+ "op_pos integer, "
			+ "charType text, "
			+ "charBitmap blob, "
			+ "matrix text, "
			+ "uri text, "
			+ "uploaded integer, "
			+ "created text);";
	
	private static final String CREATE_TABLE_PAGE = "create table if not exists page " +
			"(id integer primary key autoincrement, "
			+ "pageid text,"
			+ "version integer,"
			+ "dirty boolean,"
			+ "direct integer, "
			+ "pagenum integer, "
			+ "path text,"
			+ "created text);";
	
	//add option type 这种方式处理数据库更新时的操作：添加字段；
	private static final String ADD_FLIPDST = "alter table word add flipdstx integer;";
	
	//添加思维导图，增加导图id字段和父节点id字段
	private static final String ADD_MINDID = "alter table word add mindid integer;";
	private static final String ADD_MINDPARENTID = "alter table word add mindparentid integer;";
	
	
	private SQLiteDatabase db;
	
	private static CalligraphyDB calligraphyDB;
	private CalligraphyDB(Context ctx){
		db = ctx.openOrCreateDatabase(DATABASE_NAME, 0, null);
		db.execSQL(CREATE_TABLE_WORD);
		db.execSQL(CREATE_TABLE_PAGE);
		
		//用以下方法处理数据库版本变更时的操作
		Log.e("databases", "version:" + db.getVersion());
		try {
			if(db.getVersion() < 1){
				db.execSQL(ADD_FLIPDST);
				db.execSQL(ADD_MINDID);
				db.execSQL(ADD_MINDPARENTID);
			}else if (db.getVersion() == 1){
				db.execSQL(ADD_FLIPDST);
				db.execSQL(ADD_MINDID);
				db.execSQL(ADD_MINDPARENTID);
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		
	}
	public void resetDB(){
		db.close();
		calligraphyDB = null;
	}
	public static CalligraphyDB getInstance(Context ctx){
		if(calligraphyDB == null)
			calligraphyDB = new CalligraphyDB(ctx);
		return calligraphyDB;
	}
	private void initInitWordCount(){
		Matrix matrix;
		if(Start.c == null)
			matrix = Start.m;
		else
			matrix = Start.c.view.getMMMatrix();
		
		float[] values = new float[9];
		matrix.getValues(values);
		float scale = values[0];
		if(scale > 1 && scale < 1.3)
			INIT_WORD_COUNT = 200;
		else if(scale >= 1.3 && scale <= 1.6)
			INIT_WORD_COUNT = 120;
		else if(scale > 1.6 && scale < 2.0)
			INIT_WORD_COUNT = 60;
		else if(scale > 2.5 && scale >= 2.0)
			INIT_WORD_COUNT = 40;
		else if(scale > 2.5)
			INIT_WORD_COUNT = 25;
		else if(scale <= 1)
			INIT_WORD_COUNT = 275;
		
		
	}
	public boolean saveOperating(int OP_type,int OP_Pos,int template_id,int pagenum,int available_id,EditableCalligraphyItem eItem){
		
			int itemid = eItem.getItemID();
			//一旦有一个没有存，后面的全部存储
			SingleWord word = eItem.getWord();
			Log.v("vectorword", "saveOperating word:" + (word == null));
			switch (OP_type) {
			case OP_ADD_MIND_WORD:
			case OP_ADD_WORD:
				if(word != null){	
					CalligraphyVectorUtil.saveWordToFile(word, pagenum, available_id, itemid);
					word.recycle();
				}else
					Log.e("vector", "null");
				break;
			case OP_DELETE_WORD:
				
				break;

			default:
				break;
			}
			
			byte[] bitmapChars = null;
			
			Bitmap charBitmap = eItem.getCharBitmap();
			float flipdstx = 0;
			String uri = "";
			int charType = eItem.getCharType();
			if(charType == 7){
				//视频音频未传输完成之前不使用uri
				uri = eItem.getImageUri().toString();
				flipdstx = eItem.getFlipDstX();
			}else{
				uri = "";
			}
			String matrix = eItem.getMatrix().toString();
			
			ContentValues initalValues = new ContentValues();
			initalValues.put("itemid", itemid);
			initalValues.put("op_type", OP_type);
			initalValues.put("op_pos", OP_Pos);
			
			initalValues.put("template_id", template_id);
			initalValues.put("pagenum", pagenum);
			initalValues.put("available_id", available_id);
			initalValues.put("charType", charType);
			initalValues.put("matrix", matrix);
			initalValues.put("uri", uri);
			initalValues.put("flipdstx", flipdstx);
			
			
			if(OP_type == OP_ADD_MIND_WORD){
				initalValues.put("mindid", eItem.getMindMapItem().getMindID());
				LogUtil.getInstance().e("mindmap", "save mindID:" + eItem.getMindMapItem().getMindID());
				initalValues.put("mindparentid", eItem.getMindMapItem().getParentID());
			}
			
			try {
				if(EditableCalligraphyItem.getType(eItem.getCharType()) == Types.CharsWithStroke 
//						|| eItem.type == Types.AUDIO 
//						|| eItem.type == Types.VEDIO
						){
					
				}else{
					bitmapChars = BitmapHelper.bitmapDecode(charBitmap);
					initalValues.put("charBitmap",
							bitmapChars);
				}
			} catch (OutOfMemoryError ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			} catch (IOException e) {
				
			}
			bitmapChars = null;
			long result = db.insert(WORD_TABLE, null, initalValues);
		return !(result == -1);
	}
	
	public LinkedList<EditableCalligraphyItem> getCharList(int available_id,boolean zoomable,List mindList){
		
		initInitWordCount();
		
		LinkedList<EditableCalligraphyItem> charList = new LinkedList<EditableCalligraphyItem>();
		EditableCalligraphyItem item = null;
		Cursor cursor = db.query(
				WORD_TABLE
				,new String[] { "template_id","itemid","op_type","op_pos","charType"
				,"matrix","uri","charBitmap","flipdstx","mindid","mindparentid"}
				, "pagenum = ? and available_id = ?"
				, new String[]{Start.getPageNum()+"",available_id + ""}, null, null, null);
		int template_id = 0;
		int itemid = 0;
		int op_type = 0;
		int op_pos = 0;
		int charType = 0;
		float flipdstx = 0;
		String matrix = "";
		String uri = "";
		byte[] attr = null;
		int pagenum = Start.getPageNum();
		Log.v("startinit", "------available_id:" + available_id + " size:" + cursor.getCount() 
				+ " pagenum:" + pagenum);
		int count = 0;
		int imageCount = 0;
		
		MindMapItem mapItem = null;
		int preMindItemid = -1;
		int mindid = 0;
		int mindparentid = 0;
		HashMap<Integer, MindMapItem> mindMapHash = new HashMap<Integer, MindMapItem>();
		
		for(cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext()){
			template_id = cursor.getInt(cursor.getColumnIndex("template_id"));
			itemid = cursor.getInt(cursor.getColumnIndex("itemid"));
			op_type = cursor.getInt(cursor.getColumnIndex("op_type"));
			op_pos = cursor.getInt(cursor.getColumnIndex("op_pos"));
			charType = cursor.getInt(cursor.getColumnIndex("charType"));
			
			matrix = cursor.getString(cursor.getColumnIndex("matrix"));
			uri = cursor.getString(cursor.getColumnIndex("uri"));
			flipdstx = cursor.getFloat(cursor.getColumnIndex("flipdstx"));
			attr = cursor.getBlob(cursor.getColumnIndex("charBitmap"));
			
			Log.v("getCharList", "template_id:" + template_id
					+"itemid:" + itemid
					+"op_type:" + op_type
					+"op_pos:" + op_pos
					+"charType:" + charType
					+"matrix:" + matrix
					+"uri:" + uri
					+"attr:" + (attr == null));
			Types type = EditableCalligraphyItem.getType(charType);
			
			
			//生成不同的	EditableableItem
			if(type == Types.CharsWithStroke){
				
				item = CalligraphyVectorUtil.instance().getEditableCalligraphyItem(
						pagenum, available_id, itemid, matrix, zoomable);
				
			}else if(type == Types.CharsWithoutStroke){
				attr = cursor.getBlob(cursor.getColumnIndex("charBitmap"));
				Bitmap picBitmap = null;
				if(attr != null){
					try{
						picBitmap =  BitmapFactory.decodeByteArray(attr, 0,
								attr.length);
					} catch (OutOfMemoryError e) {
						// TODO: handle exception
						picBitmap = Start.OOM_BITMAP;
					}
				}
				
				item = new EditableCalligraphyItem(picBitmap);
				item.setMatrix(MatrixHelper.getMatrix(matrix)); 
				item.setItemId(itemid);
				
			}else if(type == Types.EndofLine  || type == Types.EnSpace || type == Types.Space){
				item = new VEditableCalligraphyItem(type);
				item.setItemId(itemid);
			}else if(type == Types.ImageItem){
				Bitmap picBitmap = null;
				if(imageCount < 2){
					if(attr != null){
						try{
							picBitmap =  BitmapFactory.decodeByteArray(attr, 0,
									attr.length);
							
						} catch (OutOfMemoryError e) {
							// TODO: handle exception
							picBitmap = Start.OOM_BITMAP;
							Log.e("time", "calligraphyDB image oom a" + available_id + " i" + itemid );
						}
					}
				}else
					picBitmap = null;
				
				if(type == Types.ImageItem){
					if(op_type == OP_ADD_WORD)
						ImageLimit.instance().addImageCount();
					else
						ImageLimit.instance().deleteImageCount();
				}
//				item.setRecycleStatus("from databases failed");
				item = new EditableCalligraphyItem(picBitmap);
				item.setType(type);
				item.setFlipDstX(flipdstx);
				item.setItemId(itemid);
				item.setImageUri(Uri.parse(uri));
				item.setMatrix(CDBPersistent.getMatrix(matrix));
			}else if(type == Types.VEDIO || type == Types.AUDIO){
				Bitmap picBitmap = null;
				attr = cursor.getBlob(cursor.getColumnIndex("charBitmap"));
				if(attr != null){
					try{
						picBitmap =  BitmapFactory.decodeByteArray(attr, 0,
								attr.length);
					} catch (OutOfMemoryError e) {
						// TODO: handle exception
//						picBitmap = Start.OOM_BITMAP;
						picBitmap = null;
						item.setRecycleStatus("from databases failed");
					}
				}
				
				
				item = new EditableCalligraphyItem(picBitmap);
				item.setType(type);
				item.setItemId(itemid);
				item.setImageUri(Uri.parse(uri));
				item.setMatrix(CDBPersistent.getMatrix(matrix));
				
				if(type == Types.AUDIO)
					item.setStopBitmap();
			}
			item.setOpPos(op_pos);
			
			attr = null;
			if(count > INIT_WORD_COUNT){
				Log.v("recyclebitmap", "init count:" + count + "bitmap:" + (item.getCharBitmap() == null));
				if(item.getCharBitmap() != null && type != Types.CharsWithoutStroke){
					item.recycleBitmap();
					BitmapCount.getInstance().recycleBitmap("EditableCalligraphy recycleCharListBitmap");
					Log.v("recyclebitmap", "init count:" + count + " recycle!!!!");
				}
			}
			
			
			
			if(op_type == OP_ADD_MIND_WORD){
				mindid = cursor.getInt(cursor.getColumnIndex("mindid"));
				LogUtil.getInstance().e("addNewWord", "item mindid:" + mindid);
				mindparentid = cursor.getInt(cursor.getColumnIndex("mindparentid"));
				if(!mindMapHash.containsKey(mindid)){
					LogUtil.getInstance().e("addNewWord", "item mindid:" + mindid + " not exit");
					//该id节点尚未出现，初始化,判断是否有父节点
					if(mindparentid == -1){
						mapItem = new MindMapItem();
						mindList.add(mapItem);
						LogUtil.getInstance().e("mindmap", "init root item mindid:" + mapItem.getMindID());
					}else{
						if(mindMapHash.containsKey(mindparentid)){
							
							LogUtil.getInstance().e("mindmap", "init item mindid:" + mapItem.getMindID() + " contains mindparentid:" + mindparentid);
							
							mapItem = mindMapHash.get(mindparentid).createNewChild();
							
						}else{
							LogUtil.getInstance().e("mindmap", "init error !!!!!!!!!!!!!mindid:" + mindid + " parentid:" + mindparentid);
						}
					}
					mindMapHash.put(mindid, mapItem);
					LogUtil.getInstance().e("addNewWord", "hash map put" + mindid);
				}else{
					LogUtil.getInstance().e("addNewWord", "item mindid:" + mindid + " exit");
					mapItem = mindMapHash.get(mindid);
				}
				mapItem.setFlipDstX((int)flipdstx);
				mapItem.addNewWord(item);
				LogUtil.getInstance().e("addNewWord", "mapItem " + mapItem.getMindID() + " insert");
			}else{
//				mindMapHash.clear();
			}
			
			
			
			
			
			//按照操作类型，操作位置，初始化charList
			switch (op_type) {
			case OP_ADD_MIND_WORD:
			case 1:
				//add char word
//				Log.v("getCharList", "file: a" +available_id + "i" + itemid
//						+ " add to " + op_pos + " size:" + charList.size());
				
				try {
					charList.add(op_pos,item);
				} catch (IndexOutOfBoundsException e) {
					// TODO: handle exception
					Log.v("wordCount", "add Index OutOfBounds Exception file: a" +available_id + "i" + itemid
							+ " add to " + op_pos + " size:" + charList.size());
					
					charList.add(item);
				}
				
				
				break;
			case -1:
				//delete word
//				Log.v("getCharList", "file: a" +available_id + "i" + itemid
//						+ " delete " + op_pos + " size:" + charList.size());
				
				EditableCalligraphyItem removeItem = null;
				try {
					removeItem = charList.remove(op_pos);
				} catch (IndexOutOfBoundsException e) {
					// TODO: handle exception
					removeItem = charList.removeLast();
					Log.v("wordCount", "delete Index OutOfBounds Exception file: a" +available_id + "i" + itemid
							+ " add to " + op_pos + " size:" + charList.size());
				}
				
				LogUtil.getInstance().e("delete", "delete special word");
				if(removeItem.isSpecial()){
					LogUtil.getInstance().e("delete", "delete special word");
					removeItem.getMindMapItem().deleteWord(removeItem);
				}
				
				if(removeItem.getCharBitmap() != null){
					removeItem.recycleBitmap();
					removeItem = null;
				}
			default:
				break;
			}
			count ++;
		}
		
		
		cursor.close();
		
		return charList;
	}
	
	public boolean updateCameraPicUri(int page,int aid,int itemid,Uri newUri){
		ContentValues initalValues = new ContentValues();
		initalValues.put("uri", newUri.toString());
		
		Log.e("update", "page:" + page + " aid:" + aid + " itemid:" + itemid + "newUri:" + newUri);
		
		initalValues.put("created", BitmapHelper.getCurrent());
		
		try {
			initalValues.put("charBitmap",
					BitmapHelper.bitmapDecode(
							BitmapHelper.getBitmapFromUri(
											newUri, page)
											));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		boolean flag = this.db.update(WORD_TABLE, initalValues, "pagenum = ? and available_id = ? and itemid = ?",
				new String[] { ""+page,""+aid,""+itemid }) > 0;
				Log.e("update", "result:" + flag);
				return flag;
	}
	
	public boolean updateAudioUri(int page,int aid,int itemid,Uri newUri,Bitmap bitmap){
		ContentValues initalValues = new ContentValues();
		initalValues.put("uri", newUri.toString());
		initalValues.put("created", BitmapHelper.getCurrent());
		try {
			initalValues.put("charBitmap",
					BitmapHelper.bitmapDecode(bitmap));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		boolean flag = this.db.update(WORD_TABLE, initalValues, "pagenum = ? and available_id = ? and itemid = ?",
				new String[] { ""+page,""+aid,""+itemid }) > 0;
				Log.e("update", "result:" + flag);
				return flag;
	}
	
public boolean updateMindmapItem(int page,int aid,int itemid,EditableCalligraphyItem item){
		
		ContentValues initalValues = new ContentValues();
		initalValues.put("op_type", OP_ADD_MIND_WORD);
		initalValues.put("mindid", item.getMindMapItem().getMindID());
		initalValues.put("mindparentid", item.getMindMapItem().getParentID());
		
		boolean flag = this.db.update(WORD_TABLE, initalValues, "pagenum = ? and available_id = ? and itemid = ? and charType = ?",
				new String[] { ""+page,""+aid,""+itemid ,""+item.getCharType()}) > 0;
				Log.e("update", "pagenum = " + page 
						+ "available_id"  + aid + " itemid" + itemid + "result:" + flag);
				return flag;
	}
	
	
	public boolean updatePictrueItem(int page,int aid,int itemid,EditableCalligraphyItem item){
		
		ContentValues initalValues = new ContentValues();
		initalValues.put("matrix", item.getMatrix().toString());
		
		boolean flag = this.db.update(WORD_TABLE, initalValues, "pagenum = ? and available_id = ? and itemid = ?",
				new String[] { ""+page,""+aid,""+itemid }) > 0;
				Log.e("update", "pagenum = " + page 
						+ "available_id"  + aid + " itemid" + itemid + "result:" + flag);
				return flag;
	}
	
	
	public int getTotalPageNum() {

		Cursor cursor = null;
		cursor = db.query("page", new String[] { "pagenum" },  //应该从这里获得，但是会引起恢复时，page表没有被回复，数据不见的bug，暂时改为上面方案，会有只有涂鸦态不能保存的bug。暂时如此
				null, null, null,
				null, "pagenum asc");
		int tmp = 0;
		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
			if (cursor.getInt(0) > tmp) {
				tmp = cursor.getInt(0);
			}
			Log.e("databases", "pagenum:" + tmp);
		}
		cursor.close();
		Log.e("databases", "return total page num:" + tmp);
		return tmp;
	}
	
	public int getCurrentWordCount(int pagenum,int available_id){
		Cursor cursor = db.query(WORD_TABLE, new String[]{"max(itemid)"}, 
				"pagenum = ? and available_id = ?", new String[]{"" + pagenum ,"" + available_id}, null, null, null);
		
		cursor.moveToFirst();
		int count = cursor.getInt(cursor.getColumnIndex("max(itemid)"));
		cursor.close();
		return count;
	}
	
	public boolean updatePicdstx(int page,int aid,int itemid,float newDst){
		ContentValues initalValues = new ContentValues();
		initalValues.put("flipdstx", newDst);
		initalValues.put("created", BitmapHelper.getCurrent());
		boolean flag = this.db.update(WORD_TABLE, initalValues, "pagenum = ? and available_id = ? and itemid = ?",
				new String[] { ""+page,""+aid,""+itemid }) > 0;
				Log.e("update", "result:" + flag);
				return flag;
	}
	public boolean updateMindDstx(int page,int aid,int mindid,float newDst){
		ContentValues initalValues = new ContentValues();
		initalValues.put("flipdstx", newDst);
		initalValues.put("created", BitmapHelper.getCurrent());
		boolean flag = this.db.update(WORD_TABLE, initalValues, "pagenum = ? and available_id = ? and mindid = ?",
				new String[] { ""+page,""+aid,""+mindid }) > 0;
				Log.e("update", "result:" + flag);
				return flag;
	}
	
	public void backupData(){
		String matrix = (new Matrix()).toString();
		
		ContentValues initalValues = null;
		for(int i=0;i<315;i++){
			initalValues = new ContentValues();
			initalValues.put("itemid", i);
			initalValues.put("op_type", 1);
			initalValues.put("op_pos", i);
			
			initalValues.put("pagenum", 17);
			initalValues.put("available_id", 3);
			initalValues.put("charType", 6);
			initalValues.put("matrix", matrix);
			
			long result = db.insert(WORD_TABLE, null, initalValues);
		}
	}
}
