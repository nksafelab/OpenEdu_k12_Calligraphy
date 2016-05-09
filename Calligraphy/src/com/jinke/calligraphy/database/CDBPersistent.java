package com.jinke.calligraphy.database;

import hallelujah.cal.CalligraphyVectorUtil;
import hallelujah.cal.SingleWord;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;
import android.view.View;

import com.jinke.calligraphy.app.branch.Calligraph;
import com.jinke.calligraphy.app.branch.EditableCalligraphy;
import com.jinke.calligraphy.app.branch.EditableCalligraphyItem;
import com.jinke.calligraphy.app.branch.EditableCalligraphyItem.Types;
import com.jinke.calligraphy.app.branch.Start;
import com.jinke.calligraphy.backup.CalligraphyItem;
import com.jinke.calligraphy.backup.UploadToServer;
import com.jinke.calligraphy.template.WolfTemplateUtil;
import com.jinke.single.BitmapCount;

public class CDBPersistent {

	private String table;
	private Context context;
	private SQLiteDatabase db;
	private CDBHelper cDBhelper;
	private BitmapFactory.Options opt = new BitmapFactory.Options();
	private boolean flags = true;

	// ----以下两个成员变量是针对在SD卡中存储数据库文件使用----
	private File path = new File("/sdcard/readings/dbfile"); // 数据库文件目录
	private File f = new File("/sdcard/readings/dbfile/readingDB"); // 数据库文件

	public CDBPersistent(Context context, String table) {
		this.context = context;
		this.table = table;
		System.out
				.println("CDBPersistent_key()@CDBPersistent_key---------from table:-----------"
						+ table);
		opt.inPreferredConfig = Bitmap.Config.ARGB_8888;
		opt.inPurgeable = true;
		opt.inInputShareable = true;

		
		cDBhelper = new CDBHelper(context);
	}

	public CDBPersistent(Context context) {
		this.context = context;
		cDBhelper = new CDBHelper(context);
	}

	public String getDateByPage(){
		
		Cursor cursor = null;
		String date = "";
		cursor = this.db.query("page", new String[] {"created" },
				"pagenum = ?", new String[] { ""+Start.getPageNum() }, null, null, null);
		if(cursor != null && cursor.getCount() != 0){
			cursor.moveToFirst();
			date = cursor.getString(cursor.getColumnIndex("created"));
		}
		cursor.close();
		return date;
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

	public boolean insert(String key) {
		if (key == null) {
			System.out
					.println("insert(String key)@CDBPersistent: info is null");
			return false;
		}

		ContentValues initalValues = new ContentValues();
		initalValues.put("key", key);
		initalValues.put("created", getCurrent());

		return db.insert(table, null, initalValues) > 0;

	}

	public void open() {
		// ----如要在SD卡中创建数据库文件，先做如下的判断和创建相对应的目录和文件----
		// if(!path.exists()){ //判断目录是否存在
		// path.mkdirs(); //创建目录
		// System.out.println("open()@CDBPersistentcreat----------数据库文件夹不存在 创建目录----------------");
		// }
		// if(!f.exists()){ //判断文件是否存在
		// try{
		// f.createNewFile(); //创建文件
		//
		// this.db = SQLiteDatabase.openOrCreateDatabase(f, null);
		//
		// this.db.execSQL(DATEBASE_TABLE_KEY);
		// this.db.execSQL(DATEBASE_TABLE_HOT);
		// this.db.execSQL(DATEBASE_TABLE_REC);
		// this.db.execSQL(DATEBASE_TABLE_SEARCH_HIS);
		// System.out.println("open()@CDBPersistentcreat----------数据库表不存在 创建表----------------");
		//
		// }catch(IOException e){
		// e.printStackTrace();
		// }
		// }else{
		// this.db = SQLiteDatabase.openOrCreateDatabase(f, null);
		// }

		// [2]--如果是在SD卡中创建数据库，那么实例化sqlitedb的操作如下

		
		this.db = cDBhelper.getWritableDatabase();
		Log.e("databasess", "open");
		// this.db.beginTransaction();
	}

	public boolean insertKey(String key) {
		this.db.beginTransaction();
		boolean flag = true;
		if (exits(key)) {
			// System.out.println("not exit");
			flag = insert(key);
		} else {
			// System.out.println("exit");
			flag = updateInfo(key);
		}

		this.db.setTransactionSuccessful();
		this.db.endTransaction();

		return flag;
	}

	public void trans() {
		this.db.setTransactionSuccessful();
		this.db.endTransaction();
	}
//12-13 21:54:08.406: ERROR/AndroidRuntime(21880): android.database.sqlite.SQLiteException: unable to close due to unfinalised statements

	public void close() {
//		try {
//			super.finalize();
//		} catch (Throwable e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		this.cDBhelper.close();
		this.db.close();
		Log.e("databasess", "close");
	}

	public boolean updateInfo(String key) {

		ContentValues initalValues = new ContentValues();
		initalValues.put("key", key);
		initalValues.put("created", getCurrent());

		return this.db.update(table, initalValues, "key = ?",
				new String[] { key }) > 0;
	}

	public boolean deleteAll() {
		this.db.delete("template", null, null);
		// this.db.setTransactionSuccessful();
		// this.db.endTransaction();
		return true;
	}

	/**
	 * �����ڷ���true
	 * 
	 * @param info
	 * @return
	 */
	public boolean exits(String key) {
		Cursor cursor = null;

		cursor = this.db.query(table, new String[] { "_id", "key", "created" },
				"key = ?", new String[] { key }, null, null, null);

		if (cursor.getCount() == 0) {
			// System.out.println("not exit");
			return true;
		} else {
			// System.out.println("exits");
			return false;
		}

	}

	public Cursor getAllKey() {

		// this.db = this.cDBhelper.getReadableDatabase();

		Cursor cursor = null;

		cursor = this.db.query(table, new String[] { "_id", "key", "created" },
				null, null, null, null, "created desc");

		System.out.println("CDBPersistent@getAllInfo--------:" + table + " "
				+ cursor.getCount());

		// this.db.close();
		// String _id;
		// String key;
		// String created;
		//
		// System.out.println("_id	key	created");
		// for(int i = 0;i<cursor.getColumnCount();i++){
		// System.out.println("column"+i+" "+cursor.getColumnName(i));
		// }
		// System.out.println(cursor.getColumnName(0)
		// +""+cursor.getColumnName(1)
		// +""+cursor.getColumnName(2));
		//
		// for(cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext())
		// {
		// _id = cursor.getString(cursor.getColumnIndex("_id"));
		// key = cursor.getString(cursor.getColumnIndex("key"));
		// created = cursor.getString(cursor.getColumnIndex("created"));
		//
		//
		// System.out.println(_id+"	"+key+"		"+created);
		//
		// }
		//
		return cursor;
	}

	/**
	 * 保存编辑区数据到数据库
	 */
	public boolean insert(int template_id, int pagenum,
			List<EditableCalligraphy> listEditableCalligraphy) {
		EditableCalligraphy e;

		LinkedList<EditableCalligraphyItem> charList;
		EditableCalligraphyItem eItem;
		Bitmap charBitmap;

		int available_id = 0;
		int charType = 0;
		int currentx = 0;
		int currenty = 0;
		int height = 0;
		String matrix = "";
		String type = "";
		int width = 0;
		long time = 0;
		int itemid = 0;
		String uri = "";
		ContentValues initalValues;
		SingleWord word = null;
		
		for (int i = 0; i < listEditableCalligraphy.size(); i++) {
			Log.e("saveTest", "EditableCalligraphy:" + i);
			e = listEditableCalligraphy.get(i);
			
			available_id = e.getID();
			
			Cursor cursor = db.query("template", 
					null,
					"pagenum = ? and available_id = ?", 
					new String[]{"" + pagenum, "" + available_id}, 
					null, null, null);
			
			int count = cursor.getCount();
			cursor.close();
			Log.e("saveTest", "page:" + pagenum + " available:" + available_id + " count:" + count);

			charList = e.getCharsList();
			int charListSize = charList.size();
			boolean flag = true;
			for (int j = 0; j < charList.size(); j++) {
				
				itemid = j;
				Log.e("saveTest", "itemid:" + j);
				eItem = charList.get(j);
				//一旦有一个没有存，后面的全部存储
				word = eItem.getWord();
				
					
				if(flag){
					if(eItem.getSaved()){
						continue;
					}else{
						flag = false;
						if(word != null)
						{
							Log.v("saveword", "word save recycled:" + word.isRecycle());
							CalligraphyVectorUtil.saveWordToFile(word, pagenum, available_id, itemid);
						}else
							Log.e("vector", "null");
					}
				}else{
					if(word != null)
					{	Log.v("saveword", "word save recycled:" + word.isRecycle());
						CalligraphyVectorUtil.saveWordToFile(word, pagenum, available_id, itemid);
					}else
						Log.e("vector", "null");
				}
				
				Log.e("saveTest", "save charList item:" + j);

				charBitmap = eItem.getCharBitmap();
				charType = eItem.getCharType();
				if(charType == 7 || eItem.getType() == Types.AUDIO || eItem.getType() == Types.VEDIO){
					uri = eItem.getImageUri().toString();
				}else{
					uri = "";
				}
				currentx = eItem.getCurPosX();
				currenty = eItem.getCurPosY();
				height = eItem.getHeight();
				matrix = eItem.getMatrix().toString();
				time = eItem.getTime();
				type = eItem.getType().toString();
				width = eItem.getWidth();

				Log.e("saveTest", "template_id:" + template_id + "\n pagenum:"
						+ pagenum + "\n available_id:" + available_id
						+ "\n itemid:" + itemid + "\n chartype:" + charType
						+ "\n px:" + currentx + "\n py:" + currenty
						+ "\n height:" + height + "\n matrix:" + matrix
						+ "\n time:" + time + "\n type:" + type + "\n width:"
						+ width);

				
				initalValues = new ContentValues();
				initalValues.put("template_id", template_id);
				
				initalValues.put("pagenum", pagenum);
				initalValues.put("available_id", available_id);
				initalValues.put("itemid", itemid);
				initalValues.put("charType", charType);
				initalValues.put("currentx", currentx);
				initalValues.put("currenty", currenty);
				initalValues.put("matrix", matrix);
				initalValues.put("uri", uri);
				Log.e("uri", "uri: " + uri.toString());
				initalValues.put("height", height);
				initalValues.put("time", time);
				initalValues.put("width", width);
				
				if(e.getAvailable().getDirect() != 1){
					initalValues.put("flipbottom", EditableCalligraphy.flip_bottom);
					initalValues.put("flipdst", EditableCalligraphy.flip_dst);
				}else{
					initalValues.put("flipbottom", EditableCalligraphy.flip_Horizonal_bottom);
					initalValues.put("flipdst", EditableCalligraphy.flip_Horizonal_dst);
					Log.e("dst", "database save flip_horizonal_bottom"+EditableCalligraphy.flip_Horizonal_bottom);
				}
				
				
//				initalValues.put("uploaded", UploadToServer.UPLOAD_NO); //存0溢出 1M 内存
//				initalValues.put("uploaded", 1);
				
//				initalValues.put("created", getCurrent());
				
				
				try {
					if(EditableCalligraphyItem.getType(eItem.getCharType()) == Types.CharsWithStroke){
						
					}else{
						initalValues.put("charBitmap",
								this.bitmapDecode(charBitmap));
					}
					
				} catch (IOException ex) {
					// TODO Auto-generated catch block
					ex.printStackTrace();
				}
				
				
				// initalValues.put("thumb", "thumb");

				Cursor exitCursor = db.query("template", 
						null,
						"pagenum = ? and available_id = ? and itemid = ?", 
						new String[]{"" + pagenum, "" + available_id , "" + itemid}, 
						null, null, null);
				if(exitCursor.getCount() == 0){
					Log.e("saveTest", "insert page:" + pagenum + " available:" + available_id + " itemid:" + itemid + "not exit insert");
					db.insert("template", null, initalValues);
				}else{
					Log.e("saveTest", " update page:" + pagenum + " available:" + available_id + " itemid:" + itemid + "exit update");
					db.update("template", initalValues, 
							"pagenum = ? and available_id = ? and itemid = ?",
							new String[]{"" + pagenum, "" + available_id , "" + itemid});
				}
				exitCursor.close();
				eItem.setSaved();
			}//charlist end
			Log.e("update", "count:" + count + " charListSize:" + charListSize);
			if(count > charListSize){
				db.delete("template", 
						"pagenum = ? and available_id = ? and itemid > ?",
						new String[]{"" + pagenum, "" + available_id , ""+ (charListSize -1)});
				Log.e("saveTest", "delete pagenum:" + pagenum + 
						" available:" + available_id + " itemid >:" + (charListSize -1));
			}
		}

		return true;

	}
	
	/* 保存时
	 * 记录每一页缩略图的路径
	 */
	public void insertBitmapPath(String path){
		
//		+ "direct integer, "
//		+ "pagenum integer, "
//		+ "path text"
//		+ "created text);";
		int direct = WolfTemplateUtil.getCurrentTemplate().getTdirect();
		int pagenum = Start.getPageNum();
		ContentValues values = new ContentValues();
		values.put("direct", direct);
		values.put("pagenum", pagenum);
		values.put("path", path);
		
		values.put("dirty", true);
		
		Cursor cursor = db.query("page", null,"pagenum = ?", new String[]{"" + pagenum}, null, null, null);
		if(cursor == null || cursor.getCount() == 0){
			
			Log.e("path","insert"+ path);
			values.put("created", Start.getDate());
			db.insert("page", null, values);	
		}else{
			db.update("page", values, "pagenum = ?", new String[]{""+ pagenum});
			Log.e("path","update"+ path);
		}
		cursor.close();
	}
	
	public Cursor getBitmapPath(){
		Cursor cursor = db.query("page", null, null, null, null, null, "pagenum asc");
		
		for(cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext()){
			Log.e("path", "direct:"+cursor.getInt(cursor.getColumnIndex("direct")) 
					+ " pagenum:" + cursor.getInt(cursor.getColumnIndex("pagenum"))
					+ " pagePath:" + cursor.getString(cursor.getColumnIndex("path"))
					);
		}
		
		return cursor;
	}
	

	/**
	 * 保存服务器数据到数据库
	 * 当要保存的是插入的图片时，数据库图片数据为空，要从文件中读取；
	 */
	public boolean insert(List<CalligraphyItem> cList) {

		CalligraphyItem cItem = null;
		Log.e("databases", "insert List:"+cList.size());
		int tempID = 0;
		for (int j = 0; j < cList.size(); j++) {
			
			Log.e("databases", "insert List:"+j);
			
			cItem = cList.get(j);
			Log.e("databases", "template_id:" + cItem.getTemplateID()
					+ "\n pagenum:" + cItem.getPageNum() + "\n available_id:"
					+ cItem.getAvailableID() + "\n itemid:" + cItem.getItemID()
					+ "\n chartype:" + cItem.getCharType() + "\n matrix:"
					+ cItem.getMatrix() + "\n time:" + cItem.getCreated());

			ContentValues initalValues = new ContentValues();
			initalValues.put("template_id", cItem.getTemplateID());
			initalValues.put("pagenum", cItem.getPageNum());
			initalValues.put("available_id", cItem.getAvailableID());
			initalValues.put("itemid", cItem.getItemID());
			initalValues.put("charType", cItem.getCharType());
			initalValues.put("matrix", cItem.getMatrix());
			initalValues.put("flipbottom", cItem.getFlipBottom());
			initalValues.put("flipdst", cItem.getFlipDst());
			initalValues.put("uploaded", UploadToServer.UPLOAD_NO);
			initalValues.put("created", cItem.getCreated());
			initalValues.put("uri", cItem.getUri());
			
//			if(!"".equals(cItem.getUri()) && (cItem.getUri() != null) &&( "7".equals(cItem.getCharType()) )){
			
			if("7".equals(cItem.getCharType()) && cItem.getItemID() != tempID){
				
				tempID = cItem.getItemID();
				Log.e("FTP", "url:" + (cItem.getUri() == null));
				Log.e("FTP", "url:" + cItem.getUri());
				try {
					Log.e("fromUrierror", "!!!!!!!!!!!!!!!!!!!!!!!!! start page:" + cItem.getPageNum() + " itemID:" + cItem.getItemID());
					initalValues.put("charBitmap",
					this.bitmapDecode(
							this.getBitmapFromUri(
									Uri.parse(
											cItem.getUri()), cItem.getPageNum())
											));
					Log.e("fromUrierror", "!!!!!!!!!!!!!!!!!!!!!!!!! end");
					Log.e("FTP", "url OK !!");
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			
			if(cItem.getByteBitmap() == null || "".equals(cItem.getByteBitmap())){
				Log.e("FTP", "url do nothing !!");
			}else{
				Log.e("FTP", "url do normal !!");
				
				initalValues.put("charBitmap", Base64.decode(cItem.getByteBitmap()
						.getBytes(), Base64.DEFAULT));
				
				if(cItem.getPageNum() > 20)
					Log.e("charError", "normal char , input charBitmap into db pagenum = " + cItem.getPageNum());
			}
			
			Cursor cursor = db.query("template", null, "template_id = ? AND available_id = ? AND pagenum = ? AND itemid= ?",
					new String[]{""+cItem.getTemplateID(),""+cItem.getAvailableID(),
					""+cItem.getPageNum(),""+cItem.getItemID()}, null, null, null);
			if(cursor.getCount() == 0){
				Log.e("databases", ""+cItem.getTemplateID()+""+cItem.getAvailableID()+
					""+cItem.getPageNum()+""+cItem.getItemID()+" not exit!! insert");
				db.insert("template", null, initalValues);	
			}else{
				Log.e("databases", ""+cItem.getTemplateID()+""+cItem.getAvailableID()+
						""+cItem.getPageNum()+""+cItem.getItemID()+"  exit!! update");
				db.update("template", initalValues, "template_id = ? AND available_id = ? AND pagenum = ? AND itemid= ?",
						new String[]{""+cItem.getTemplateID(),""+cItem.getAvailableID(),
						""+cItem.getPageNum(),""+cItem.getItemID()});
			}
			cursor.close();
		}
		return true;
	}
	
	public void pageBackup(List<CalligraphyItem> cList) {
		int direct = 0;
		int tempage = 0;
		String path = "";
		for(CalligraphyItem cItem : cList){
			if(tempage != cItem.getPageNum()){
				//新页，插入page表
				tempage = cItem.getPageNum();
				
				String type = WolfTemplateUtil.getTypeByID(cItem.getTemplateID());
				direct = WolfTemplateUtil.getTemplateByType(type).getTdirect();
				path = "calligraphy_"+ tempage+"_"+ direct + ".png";
				ContentValues values = new ContentValues();
				
				values.put("direct", direct);
				values.put("pagenum", tempage);
				values.put("path", path);
				
				Cursor cursor = db.query("page", null,"pagenum = ?", new String[]{"" + tempage}, null, null, null);
				if(cursor == null || cursor.getCount() == 0){
					
					Log.e("path","insert"+ path);
					values.put("created", Start.getDate());
					db.insert("page", null, values);	
				}else{
//					db.update("page", values, "pagenum = ?", new String[]{""+ tempage});
//					Log.e("path","update"+ path);
				}
			}
			
		}
	
	}
	

	/*
	 * 不按模板查找，废弃
	 */
	public LinkedList<EditableCalligraphyItem> getCharListByPageAndIDANDTemplateID(int templateID,int id) {

		Cursor cursor = null;
		cursor = this.db.query("template", new String[] { "_id", "template_id",
				"pagenum", "available_id", "itemid", "charType", "charBitmap",
				"width", "height", "currentx", "currenty", "time", "matrix",
				"created", "flipbottom", "flipdst", "uploaded" },
				"pagenum = ? AND available_id = ? AND template_id = ?",
				new String[] { Start.getPageNum() + "", id + "" ,templateID+""}, null, null,
				"_id asc");// desc

		Bitmap charBitmap;
		int _id = 0;
		int template_id = 0;
		int pagenum = 0;
		int available_id = 0;
		int charType = 0;
		int currentx = 0;
		int currenty = 0;
		int height = 0;
		String matrix = "";
		String type = "";
		int width = 0;
		long time = 0;
		int itemid = 0;
		String created = "";
		byte[] arrbt = null;
		int uploaded = 0;

		LinkedList<EditableCalligraphyItem> charList = new LinkedList<EditableCalligraphyItem>();
		;
		EditableCalligraphyItem e = null;

		int bottom = 0;
		int dst = 0;
		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {

			_id = cursor.getInt(cursor.getColumnIndex("_id"));
			template_id = cursor.getInt(cursor.getColumnIndex("template_id"));
			pagenum = cursor.getInt(cursor.getColumnIndex("pagenum"));
			available_id = cursor.getInt(cursor.getColumnIndex("available_id"));
			itemid = cursor.getInt(cursor.getColumnIndex("itemid"));
			charType = cursor.getInt(cursor.getColumnIndex("charType"));
			width = cursor.getInt(cursor.getColumnIndex("width"));
			height = cursor.getInt(cursor.getColumnIndex("height"));
			currentx = cursor.getInt(cursor.getColumnIndex("currentx"));
			currenty = cursor.getInt(cursor.getColumnIndex("currenty"));
			time = cursor.getLong(cursor.getColumnIndex("time"));
			matrix = cursor.getString(cursor.getColumnIndex("matrix"));
			created = cursor.getString(cursor.getColumnIndex("created"));
			arrbt = cursor.getBlob(cursor.getColumnIndex("charBitmap"));
			uploaded = cursor.getInt(cursor.getColumnIndex("uploaded"));

			if (arrbt == null) {
				e = new EditableCalligraphyItem(
						EditableCalligraphyItem.getType(charType));
			} else {
				charBitmap = BitmapFactory.decodeByteArray(arrbt, 0,
						arrbt.length);
				e = new EditableCalligraphyItem(charBitmap);
			}
			e.setMatrix(getMatrix(matrix));
			charList.add(e);
			Log.e("databases", "available_id:" + available_id);
			Log.e("databases", "matrix:" + matrix);

		}
		cursor.close();
		return charList;
	}

	public int getTemplateByPage(int pagenum){
		
		
		Cursor cursor = null;
		cursor = db.query("template", new String[] { "template_id" },
				"pagenum = ?", new String[]{""+ pagenum}, null,
				null, null);
		int tmp = 0;
		if(cursor.getCount() == 0){
			return 6;//该页没有内容，返回默认模板
		}
		
		cursor.moveToFirst();
			
		tmp = cursor.getInt(cursor.getColumnIndex("template_id"));
		
		Log.e("databases", "return template_id:" + tmp);
		cursor.close();
		return tmp;
		
	}
	
	public LinkedList<EditableCalligraphyItem> getCharListByPageAndID(int id) {
		Cursor cursor = null;
		cursor = this.db.query("template", new String[] { "_id", "template_id",
				"pagenum", "available_id", "itemid", "charType", "charBitmap",
				"width", "height", "currentx", "currenty", "time", "matrix","uri",
				"created", "flipbottom", "flipdst", "uploaded" },
				"pagenum = ? AND available_id = ?",
				new String[] { Start.getPageNum() + "", id + ""}, null, null,
				"_id asc");// desc

		Bitmap charBitmap;
		int _id = 0;
		int template_id = 0;
		int pagenum = 0;
		int available_id = 0;
		int charType = 0;
		int currentx = 0;
		int currenty = 0;
		int height = 0;
		String matrix = "";
		String type = "";
		int width = 0;
		long time = 0;
		int itemid = 0;
		String created = "";
		byte[] arrbt = null;
		int uploaded = 0;
		String uri = "";
		
		String charPath = Start.getStoragePath() + "/calldir/free_" + Start.PAGENUM + "/chars";

		LinkedList<EditableCalligraphyItem> charList = new LinkedList<EditableCalligraphyItem>();
		;
		EditableCalligraphyItem e = null;

		int bottom = 0;
		int dst = 0;
//		long start = System.currentTimeMillis();
//		Log.e("time", "for encode bitmap: start" + start);
		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {

			_id = cursor.getInt(cursor.getColumnIndex("_id"));
			template_id = cursor.getInt(cursor.getColumnIndex("template_id"));
			pagenum = cursor.getInt(cursor.getColumnIndex("pagenum"));
			available_id = cursor.getInt(cursor.getColumnIndex("available_id"));
			itemid = cursor.getInt(cursor.getColumnIndex("itemid"));
			charType = cursor.getInt(cursor.getColumnIndex("charType"));
			width = cursor.getInt(cursor.getColumnIndex("width"));
			height = cursor.getInt(cursor.getColumnIndex("height"));
			currentx = cursor.getInt(cursor.getColumnIndex("currentx"));
			currenty = cursor.getInt(cursor.getColumnIndex("currenty"));
			time = cursor.getLong(cursor.getColumnIndex("time"));
			matrix = cursor.getString(cursor.getColumnIndex("matrix"));
			created = cursor.getString(cursor.getColumnIndex("created"));
			arrbt = cursor.getBlob(cursor.getColumnIndex("charBitmap"));
			uploaded = cursor.getInt(cursor.getColumnIndex("uploaded"));

			uri = cursor.getString(cursor.getColumnIndex("uri"));
			
			if (arrbt == null) {
				//数据库bitmap字段为空，认为是空格，回车等。 待改为使用charType判断
				e = new EditableCalligraphyItem(
						EditableCalligraphyItem.getType(charType));
				
			} else {
				//bitmap字段不为空，及普通字。从chars文件中读取.
				try {
//					charBitmap = BitmapFactory.decodeByteArray(arrbt, 0,
//							arrbt.length);
					File f = new File(charPath + "/char_a" + available_id + "i" + itemid + ".png");
					if(f.exists()){
						charBitmap = BitmapFactory.decodeStream(
								new FileInputStream(f),
								null, opt);
						BitmapCount.getInstance().createBitmap("CDBPersistent charBitmap");
					}
					else{
						charBitmap = BitmapFactory.decodeByteArray(arrbt, 0,
								arrbt.length);
						Log.e("char", "char file not exit!! from databases!");
					}
					
				} catch (OutOfMemoryError o) {
					// TODO: handle exception
					charBitmap = Start.OOM_BITMAP;
				}
				catch (FileNotFoundException er) {
					// TODO Auto-generated catch block
					charBitmap = Start.OOM_BITMAP;
					Log.e("getCharListByPageAndID", "filenotfound", er);
				}
				
				e = new EditableCalligraphyItem(charBitmap);
			}
			
			e.setType(EditableCalligraphyItem.getType(charType));
			
			Log.e("aa", "start && uriuri: --------------------------" + uri);
			
			if(uri != null)
			e.setImageUri(Uri.parse(uri));
			
			
//			Log.e("uri", "uri: " + uri.toString());
			
			e.setMatrix(getMatrix(matrix));
			charList.add(e);
			Log.e("databases", "available_id:" + available_id);
			Log.e("databases", "matrix:" + matrix);

		}
		long end = System.currentTimeMillis();
//		Log.e("time", "for encode bitmap: end" + end);
//		Log.e("time", "for encode bitmap:" + (end - start));
		cursor.close();
		return charList;
	
		/*
		Cursor cursor = null;
		cursor = this.db.query("template", new String[] { "_id", "template_id",
				"pagenum", "available_id", "itemid", "charType", "charBitmap",
				"width", "height", "currentx", "currenty", "time", "matrix","uri",
				"created", "flipbottom", "flipdst", "uploaded" },
				"pagenum = ? AND available_id = ?",
				new String[] { Start.getPageNum() + "", id + ""}, null, null,
				"_id asc");// desc

		Bitmap charBitmap;
		int _id = 0;
		int template_id = 0;
		int pagenum = 0;
		int available_id = 0;
		int charType = 0;
		int currentx = 0;
		int currenty = 0;
		int height = 0;
		String matrix = "";
		String type = "";
		int width = 0;
		long time = 0;
		int itemid = 0;
		String created = "";
		byte[] arrbt = null;
		int uploaded = 0;
		String uri = "";

		LinkedList<EditableCalligraphyItem> charList = new LinkedList<EditableCalligraphyItem>();
		;
		EditableCalligraphyItem e = null;

		int bottom = 0;
		int dst = 0;
		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {

			_id = cursor.getInt(cursor.getColumnIndex("_id"));
			template_id = cursor.getInt(cursor.getColumnIndex("template_id"));
			pagenum = cursor.getInt(cursor.getColumnIndex("pagenum"));
			available_id = cursor.getInt(cursor.getColumnIndex("available_id"));
			itemid = cursor.getInt(cursor.getColumnIndex("itemid"));
			charType = cursor.getInt(cursor.getColumnIndex("charType"));
			width = cursor.getInt(cursor.getColumnIndex("width"));
			height = cursor.getInt(cursor.getColumnIndex("height"));
			currentx = cursor.getInt(cursor.getColumnIndex("currentx"));
			currenty = cursor.getInt(cursor.getColumnIndex("currenty"));
			time = cursor.getLong(cursor.getColumnIndex("time"));
			matrix = cursor.getString(cursor.getColumnIndex("matrix"));
			created = cursor.getString(cursor.getColumnIndex("created"));
			arrbt = cursor.getBlob(cursor.getColumnIndex("charBitmap"));
			uploaded = cursor.getInt(cursor.getColumnIndex("uploaded"));

			uri = cursor.getString(cursor.getColumnIndex("uri"));
			
			if (arrbt == null) {
				
				e = new EditableCalligraphyItem(
						EditableCalligraphyItem.getType(charType));
				
			} else {
				try {
					charBitmap = BitmapFactory.decodeByteArray(arrbt, 0,
							arrbt.length);
				} catch (OutOfMemoryError o) {
					// TODO: handle exception
					charBitmap = Start.OOM_BITMAP;
				}
				
				e = new EditableCalligraphyItem(charBitmap);
			}
			
			e.setType(EditableCalligraphyItem.getType(charType));
			
			Log.e("aa", "start && uriuri: --------------------------" + uri);
			
			if(uri != null)
			e.setImageUri(Uri.parse(uri));
			
			
//			Log.e("uri", "uri: " + uri.toString());
			
			e.setMatrix(getMatrix(matrix));
			charList.add(e);
			Log.e("databases", "available_id:" + available_id);
			Log.e("databases", "matrix:" + matrix);

		}

		return charList;
		*/
	}

	/**
	 * 获得当前页需要上传的字
	 * @return
	 */
	public Cursor getUploadCursorByPage() {

//		Cursor cursor = this.db.query("template", new String[] { "_id",
//				"template_id", "pagenum", "available_id", "itemid", "charType",
//				"charBitmap", "matrix","uri" ,"created", "flipbottom", "flipdst" },
//				"pagenum = ?",
//				new String[] { Start.getPageNum() + ""}, null, null, "_id asc");// desc
		Cursor cursor = this.db.query("template", new String[] { "_id",
				"template_id", "pagenum", "available_id", "itemid", "charType",
				"charBitmap", "matrix","uri" ,"created", "flipbottom", "flipdst" },
				null,
				null, null, null, "_id asc");// desc

		return cursor;
	}

	public int updateUploadedStatus(int pagenum,int template_id,int item_id) {
		
		ContentValues initalValues = new ContentValues();
		initalValues.put("uploaded", UploadToServer.UPLOAD_YES);
		
		return this.db.update("template",
				initalValues,
				"pagenum = ? AND template_id = ? AND item_id = ?",
				new String[]{""+ pagenum , ""+template_id, ""+ item_id});

	}
	
	public LinkedList<EditableCalligraphyItem> getCharListByPageAndID_Obj(int id) {

		Cursor cursor = null;
		cursor = this.db.query("itemobj", new String[] { "_id", "template_id",
				"pagenum", "available_id", "itemid", "itemobj", "created" },

		"pagenum = ? AND available_id = ?", new String[] {
				Start.getPageNum() + "", id + "" }, null, null, "_id asc");// desc

		Bitmap charBitmap;
		int _id = 0;
		int template_id = 0;
		int pagenum = 0;
		int available_id = 0;
		byte[] itemobj;
		int itemid = 0;
		String created = "";

		LinkedList<EditableCalligraphyItem> charList = new LinkedList<EditableCalligraphyItem>();
		;
		EditableCalligraphyItem e = null;
		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {

			_id = cursor.getInt(cursor.getColumnIndex("_id"));
			template_id = cursor.getInt(cursor.getColumnIndex("template_id"));
			pagenum = cursor.getInt(cursor.getColumnIndex("pagenum"));
			available_id = cursor.getInt(cursor.getColumnIndex("available_id"));
			itemid = cursor.getInt(cursor.getColumnIndex("itemid"));
			created = cursor.getString(cursor.getColumnIndex("created"));
			itemobj = cursor.getBlob(cursor.getColumnIndex("itemobj"));
			ObjectInputStream ois = null;
			try {
				ois = new ObjectInputStream(new ByteArrayInputStream(itemobj));
			} catch (StreamCorruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			try {
				e = (EditableCalligraphyItem) ois.readObject();
			} catch (OptionalDataException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (ClassNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			Log.e("databases", "pagenum:" + pagenum);
			Log.e("databases", "available_id:" + available_id);
			Log.e("databases", "itemid:" + itemid);
			Log.e("databases", "e null ?:" + (e == null));
			Log.e("databases", "e.getMatrix:" + e.getMatrix());

			charList.add(e);

			Log.e("databases", "available_id:" + available_id);

		}

		return charList;
	}

	/*
	 * 废弃，不按模板分页
	 */
	public int getTotalPageNumByTemplateID(int template_id) {

		Cursor cursor = null;
		cursor = db.query("template", new String[] { "pagenum" },
				"template_id = ?", new String[] { template_id + "" }, null,
				null, "pagenum asc");
		int tmp = 0;
		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
			if (cursor.getInt(0) > tmp) {
				tmp = cursor.getInt(0);
			}
			Log.e("databases", "pagenum:" + tmp);
		}
		Log.e("databases", "return total page num:" + tmp);
		return tmp;
	}
	
	public int getTotalPageNum() {

		Cursor cursor = null;
//		cursor = db.query("template", new String[] { "pagenum" },
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

	public void getBottomByPage(int template_id, int current_page) {

		Log.e("dst", "bottom !!!!!!!!!!!!!!!!!!!!!!!!id:" + template_id
				+ " page:" + current_page);
		Cursor cursor = null;
		cursor = db
				.query("template", new String[] { "flipbottom" },
						"template_id = ? AND pagenum = ?", new String[] {
								template_id + "", current_page + "" }, null,
						null, null);
		int tmp = 0;
		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
			if (cursor.getInt(0) > tmp) {
				tmp = cursor.getInt(0);
			}
//			Log.e("databases", "bottom:" + tmp);
		}
		
		if(tmp <= 800){
			Calligraph.flipblockBtn.setVisibility(View.GONE);
			tmp = 800;
			EditableCalligraphy.flip_dst = 0;
		}
		EditableCalligraphy.flip_bottom = tmp;
		
		Log.e("dst", "bottom !!!!!!!!!!!!!!!!!!!!!!!!"
				+ EditableCalligraphy.flip_bottom);
	}

	public void getHorizonalBottomByPage(int template_id, int current_page) {

		Log.e("dst", "bottom !!!!!!!!!!!!!!!!!!!!!!!!id:" + template_id
				+ " page:" + current_page);
		Cursor cursor = null;
		cursor = db
				.query("template", new String[] { "flipbottom" },
						"template_id = ? AND pagenum = ?", new String[] {
								template_id + "", current_page + "" }, null,
						null, null);
		int tmp = 0;
		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
			if (cursor.getInt(0) > tmp) {
				tmp = cursor.getInt(0);
			}
//			Log.e("databases", "bottom:" + tmp);
		}
		
//		if(tmp >= 600){
//			Calligraph.flipblockBtn.setVisibility(View.GONE);
//			tmp = 600;
//			EditableCalligraphy.flip_Horizonal_dst = 0;
//		}
		
		if(tmp >= 1600){
			Calligraph.flipblockBtn.setVisibility(View.GONE);
			tmp = 1600;
			EditableCalligraphy.flip_Horizonal_dst = 0;
		}
		
		EditableCalligraphy.flip_Horizonal_bottom = tmp;
		
		Log.e("dst", "bottom return !!!!!!!!!!!!!!!!!!!!!!!!"
				+ EditableCalligraphy.flip_Horizonal_bottom);
	}
	
	/*
	 * 页与模板绑定
	 */
	public boolean currentPageExit(int template_id, int current_page) {

		Log.e("databases", "exit:" + current_page);
		Cursor cursor = null;
		cursor = db
				.query("template", new String[] { "_id" },
						"template_id = ? AND pagenum = ?", new String[] {
								template_id + "", current_page + "" }, null,
						null, null);

		return (cursor.getCount() != 0);
	}
	public boolean currentPageExit(int current_page) {

		Log.e("databases", "exit:" + current_page);
		Cursor cursor = null;
		cursor = db
				.query("template", new String[] { "_id" },
						"pagenum = ?", new String[] {
								current_page + "" }, null,
						null, null);

		return (cursor.getCount() != 0);
	}
 
	/*
	 * 页与模板绑定
	 */
	public void deletecurrentPage(int template_id, int current_page) {

		db.delete("template", "template_id = ? AND pagenum = ?", new String[] {
				template_id + "", current_page + "" });

	}
	public void deletecurrentPage(int current_page) {

		db.delete("template", "pagenum = ?", new String[] {
				current_page + "" });

	}
	
	public void deletePage(int pagenum) {

		db.delete("template", "pagenum = ?", new String[] {
				pagenum + "" });
		db.delete("page", "pagenum = ?", new String[] {
				pagenum + "" });

		db.execSQL("update page set pagenum = pagenum - 1 where pagenum > " + pagenum);
		db.execSQL("update template set pagenum = pagenum - 1 where pagenum > " + pagenum);
	}

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

		bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
		return out.toByteArray();
	}

	public static Matrix getMatrix(String stringMatrix) {
		Matrix m = new Matrix();
		String tmp = stringMatrix.substring(8, stringMatrix.length() - 2);
		tmp = tmp.replace("][", ",").replace(" ", "");

		String[] t = tmp.split(",");
		float[] mValues = new float[9];
		float f;
		for (int i = 0; i < t.length; i++) {

			f = Float.parseFloat(t[i]);
			mValues[i] = f;
		}
		m.setValues(mValues);
		return m;
	}
	
	public Bitmap getBitmapFromUri(Uri uri , int pageNum){
		
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
	
	public boolean updateCameraPicUri(int page, int aid, int itemid ,Uri newUri){
		
		ContentValues initalValues = new ContentValues();
		initalValues.put("uri", newUri.toString());
		Log.e("updateCamera", "newUri:" + newUri);
		initalValues.put("created", getCurrent());
		
		try {
			initalValues.put("charBitmap",
					this.bitmapDecode(
							this.getBitmapFromUri(
											newUri, page)
											));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return this.db.update("template", initalValues, "pagenum = ? and available_id = ? and itemid = ?",
				new String[] { ""+page,""+aid,""+itemid }) > 0;
		
	}
	
	public List<Integer> getUploadPage(){
		
		Cursor cursor = null;
		cursor = db
				.query("page", new String[] { "pagenum" },
						"dirty = ?", new String[] {"1"}, null,
						null, null);
		
		Log.e("dirty", "num:" + cursor.getCount());
		
		List<Integer> pageList;
		
		if(cursor.getCount() == 0)
			return null;
		else
			pageList = new ArrayList<Integer>();
		
		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
			
			pageList.add(cursor.getInt(cursor.getColumnIndex("pagenum")));
			
		}
		cursor.close();
		return pageList;
	}
	
	public void uploadedSuccess(){
		ContentValues initalValues = new ContentValues();
		initalValues.put("dirty", "0");
		db.update("page", initalValues, null, null);
	}
	
	static Cursor mCursor;
	public Cursor getCharByPage(int pagenum){
		/*
		 + "_id integer primary key autoincrement, "
		+ "template_id integer, "
		+ "pagenum integer, "
		+ "available_id integer, "
		+ "itemid integer, "
		+ "charType text, "
		+ "charBitmap blob, "
		+ "width integer, "
		+ "height integer, "
		+ "currentx integer, "
		+ "currenty integer, "
		+ "time text, "
		+ "matrix text, "
		+ "uri text, "
		+ "flipbottom integer, "
		+ "flipdst integer, "
		+ "uploaded integer, "
		+ "created text);";
		 */
		
		mCursor = null;
		mCursor = db
				.query("template", new String[] { "template_id","available_id" ,"itemid","charType",
						"matrix","uri","charBitmap"},
						"pagenum = ?", new String[] {pagenum + ""}, null,
						null, null);
//		Log.e("vector", "select from template cursor:" + (cursor == null));
		
		if(mCursor != null)
			Log.e("vector", "select from template cursor count:" + mCursor.getCount());	
		
		
		return mCursor;
	}
	
	public void backup(){
		ContentValues  initalValues = null;
		for (int j = 96 ; j < 250; j++) {
			
			Log.e("databases", "insert List:"+j);
			
			initalValues = new ContentValues();
			initalValues.put("template_id", 6);
			initalValues.put("pagenum", 15);
			initalValues.put("available_id", 3);
			initalValues.put("itemid", j);
			initalValues.put("charType", 1);
			Matrix m = new Matrix();
			initalValues.put("matrix", m.toString());
			
//			initalValues.put("uri", "file:///mnt/extsd/calldir/free_15/p15a3i94");
			
			db.insert("template", null, initalValues);	
//			db.update("template", initalValues, "pagenum = ? and available_id = ? and itemid = ?", 
//					new String[]{"" + 15,""+3,""+94});
			
		}
	}
	
	

	/* 查询指定页面是否存在
	 * 记录每一页缩略图的路径
	 */
	public boolean isPageExist(int pagenum){
		Cursor cursor = db.query("page", null,"pagenum = ?", new String[]{"" + pagenum}, null, null, null);
		if(cursor == null || cursor.getCount() == 0){
			cursor.close();
			return false;
		}else{
			cursor.close();
			return true;
		}
	}
}


