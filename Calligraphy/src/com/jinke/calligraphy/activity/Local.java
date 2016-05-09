package com.jinke.calligraphy.activity;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.jinke.calligraphy.activity.CloudGridAdapter.cloudItem;
import com.jinke.calligraphy.activity.LocalGridAdapter.localItem;
import com.jinke.calligraphy.app.branch.R;
import com.jinke.calligraphy.app.branch.Start;
import com.jinke.kanbox.Kanbox;
import com.jinke.kanbox.Token;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnKeyListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

public class Local extends Activity {

	protected static final int USE_THIS_LOCAL_BACKUP = 0;
	protected static final int SHOW_THUMB_GRIDVIEW = 1;
	private SimpleAdapter listAdapter;
	private List<Map<String, String>> data;
	private List<Map<String, String>> dataForDisplay;
	private int selectedItem = 0;

	DateFormat format = null;
	private int selected = 0;
	GridView thumbGrid;
	List<String> list;
	ListView list2;

	LocalGridAdapter adapter;
	localItem item;
	private ArrayList<String> localFolderList;
	protected String dirName;
	private int lengthLimit = 5;
	public static Handler handler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.local);

		initHandler();
		createListViewData();
		getDataFromDialog();
		setListViewAdapter();
		setListViewListener();
		
		Log.i("hhh", "Local");
	}

	private void initHandler() {
		if (handler == null) {
			handler = new Handler() {
				@Override
				public void handleMessage(Message msg) {

					switch (msg.what) {
					case SHOW_THUMB_GRIDVIEW:
						setDirAndGridViewAdapter(msg);
						
						break;
					}
				}
			};
		}
	}

	private void setDirAndGridViewAdapter(Message msg) {
		dirName = (String) msg.obj;
		getLocalThumbBitmap(dirName);

		showDialog(SHOW_THUMB_GRIDVIEW);
	}
	
	private void createListViewData() {
		if (data == null)
			data = new LinkedList<Map<String, String>>();
		if (dataForDisplay == null)
			dataForDisplay = new LinkedList<Map<String, String>>();
	}

	private void setListViewAdapter() {
		list2 = (ListView) findViewById(R.id.localListView);
		String[] from = { Properyt.LOCAL_ARRAYLIST };
		int[] to = { R.id.textlocal };

		listAdapter = new SimpleAdapter(this, data, R.layout.localitem, from,
				to);
		list2.setAdapter(listAdapter);
	}

	private Dialog useThisLocal() {
		AlertDialog.Builder builder = new AlertDialog.Builder(Local.this);
		builder.setTitle("提示");
		builder.setMessage("是否恢复到该时间点?");
		builder.setPositiveButton("是", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				Message msg = new Message();
				msg.what = -1;
				if (selectedItem != -1) {
					msg.obj = list.get(list.size() - selectedItem - 1);
					Log.e("ssss", list.get(list.size() - selectedItem - 1)
									+ "");
					// 启动本地备份线程
					Start.kanboxDownloadHandler.sendMessage(msg);
				}
				finish();
				
				
			}
		});
		builder.setNegativeButton("否", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

			}
		});
		return builder.create();

	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case SHOW_THUMB_GRIDVIEW:
			return showThumbDialog();
			}
		return null;
		
	}

	private Dialog showThumbDialog() {
		
		
		AlertDialog.Builder builder = new AlertDialog.Builder(Local.this);
		builder.setTitle("手写预览");

		LayoutInflater inflater = LayoutInflater.from(Local.this);
		View view = inflater.inflate(R.layout.gridthumb, null);

		
		thumbGrid = (GridView) view.findViewById(R.id.thumbGridView);
		adapter = new LocalGridAdapter(this, localFolderList,dirName);
		thumbGrid.setAdapter(adapter);
		adapter.notifyDataSetChanged();
		thumbGrid.setFocusable(true);

		builder.setView(view);
		builder.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(DialogInterface dialog, int keyCode,
					KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK){
					adapter.recycleBitmap();
					removeDialog(SHOW_THUMB_GRIDVIEW);
				}
				return false;
			}
		});
		
		builder.setPositiveButton("点击恢复", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				Message msg = new Message();
				//恢复
				msg.what = -1;
				if (selectedItem != -1) {
					msg.obj = list.get(list.size() - selectedItem - 1);
					Log.e("dddd", list.get(list.size() - selectedItem - 1)
									+ "");
					Start.kanboxDownloadHandler.sendMessage(msg);
				}
				adapter.recycleBitmap();
				removeDialog(SHOW_THUMB_GRIDVIEW);
				finish();
			}
		});

		return builder.create();
	}

	private void getLocalThumbBitmap(String dir) {
		localFolderList = new ArrayList<String>();
		File localDir = new File(Start.getStoragePath() 
				+ "/callbackup/"+dir+"/");
		if(localDir.exists()){
			String[] s = localDir.list();
			for(String t:s){
				if(t.contains("index_"))
					localFolderList.add(t);
				
			}
		}
		
		ArrayList<String> tmp = new ArrayList<String>();
		for (String s : localFolderList) {
			if (s.contains("index_")) {
				tmp.add(s);
			}
		}
		localFolderList = tmp;
		Log.e("localFolderList",localFolderList+"");

		Collections.sort(localFolderList, new StrComparator());
		
	}

	private void setListViewListener() {
		list2.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				selectedItem = arg2;
				System.out.println("--------------------show dialog before");
				showDialog(USE_THIS_LOCAL_BACKUP);
			}
		});

		list2.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				selectedItem = arg2;
//				showDialog(SHOW_THUMB_GRIDVIEW);
				Message msg = new Message();
				msg.what = SHOW_THUMB_GRIDVIEW;
				msg.obj = list.get(list.size()- selectedItem - 1);
				Log.e("chose",list.get(list.size()- selectedItem - 1)+"");
				handler.sendMessage(msg);
				
				
				
//				AlertDialog.Builder builder= new AlertDialog.Builder(Local.this);
//				builder.setTitle("测试");
//				builder.setMessage("sssssss");
//				builder.create();
				
				
				
				return true;
			}
		});
	}

	private void getDataFromDialog() {
		Intent intent = this.getIntent();
		Bundle extra = intent.getExtras();
		list = extra.getStringArrayList(Properyt.LOCAL_ARRAYLIST);
		Log.e("listttttt", list + "");
		final String[] folderName = new String[list.size()];
		getFormat(list, folderName);

		Map<String, String> map;
		for (int i = 0; i < folderName.length; i++) {
			map = new HashMap<String, String>();
			map.put(Properyt.LOCAL_ARRAYLIST, folderName[i]);
			data.add(0, map);
			dataForDisplay.add(0,map);
		}

		cutDisplayData();
	}
	
	private void cutDisplayData() {
		if (dataForDisplay.size() > lengthLimit ){
			int size = dataForDisplay.size();
			for(int i = lengthLimit; i < size; i++){
				dataForDisplay.remove(lengthLimit);
			}
		}
	}

	private void getFormat(List<String> list, final String[] folderName) {
		for (int i = 0; i < list.size(); i++) {
			folderName[i] = list.get(i);
			String tmp = folderName[i];
			String time = tmp.substring("calligraphy".length(), "calligraphy"
					.length()
					+ "yyyyMMddHHmmss".length());
			format = new java.text.SimpleDateFormat("yyyyMMddHHmmss");
			try {
				Date d = format.parse(time);
				format = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");
				time = format.format(d);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			folderName[i] = "本地备份" + time;
		}
	}
	
	class StrComparator implements Comparator<String> {

		public int compare(String o1, String o2) {

			int start = o1.indexOf("index_") + "index_".length();
			int end = o1.indexOf(".jpg");
			int pagenum1 = Integer.parseInt(o1.substring(start, end));
			
			
			start = o2.indexOf("index_") + "index_".length();
			end = o2.indexOf(".jpg");
			int pagenum2 = Integer.parseInt(o2.substring(start, end));
			
			Log.e("Sort", "pagenum1:" + pagenum1 + " pagenum2:" + pagenum2);

			return -(pagenum1 - pagenum2);
			

//			if(o1.length() < o2.length())
//				return o1.compareTo(o2);
//			return -o1.compareTo(o2);

		}

	}

}
