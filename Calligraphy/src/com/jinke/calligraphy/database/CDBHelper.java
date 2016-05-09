package com.jinke.calligraphy.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class CDBHelper extends SQLiteOpenHelper{

	private static final String DATEBASE_NAME = "calligraphy.db";
	private static final int DATEBASE_VERSION = 1;
	private static final String DATEBASE_CREATED = "created";
	
	private static final String DATEBASE_TABLE_TEMPLATE =  "create table template ("
		
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
	
	private static final String DATEBASE_TABLE_PAGENAME =  "create table page ("
		+ "_id integer primary key autoincrement, "
		
		+ "pageid text,"
		+ "version integer,"
		+ "dirty boolean,"
		
		+ "direct integer, "
		+ "pagenum integer, "
		+ "path text,"
		+ "created text);";
	
	private static final String DATEBASE_TABLE_REC =  "create table recent_reading ("
		+ "_id integer primary key autoincrement, "
		+ "title text, "
		+ "link text, "
		+ "abst1 text, "
		+ "sourceImg text, "
		+ "created text,"
		+ "thumb blob,"
		+ "source integer);";
	
	private static final String DATEBASE_TABLE_SEARCH_HIS =  "create table search_his ("
		+ "_id integer primary key autoincrement, "
		+ "key text, "
		+ "title text, "
		+ "link text, "
		+ "abst1 text, "
		+ "sourceImg text, "
		+ "created text,"
		+ "thumb blob,"
		+ "source integer,"
		+ "abst3 text);";
	
	
	public CDBHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}
	
	public CDBHelper(Context context, String name, CursorFactory factory) {
		super(context, name, factory, DATEBASE_VERSION);
		// TODO Auto-generated constructor stub
	}
	public CDBHelper(Context context, String name) {
		super(context, name, null, DATEBASE_VERSION);
		// TODO Auto-generated constructor stub
	}
	public CDBHelper(Context context) {
		super(context, DATEBASE_NAME, null, DATEBASE_VERSION);
		System.out.println("CDBHelper_Key(Context context)@CDBHelper_Key----------------------------- ");
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		System.out.println("CDBHelper@onCreater");
		db.execSQL(DATEBASE_TABLE_TEMPLATE);
		db.execSQL(DATEBASE_TABLE_PAGENAME);
//		db.execSQL(DATEBASE_TABLE_HOT);
//		db.execSQL(DATEBASE_TABLE_REC);
//		db.execSQL(DATEBASE_TABLE_SEARCH_HIS);
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
		db.execSQL("DROP TABLE IF EXISTS recent_reading");

		onCreate(db);
		
	}

}
