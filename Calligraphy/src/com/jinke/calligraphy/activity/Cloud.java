package com.jinke.calligraphy.activity;

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
import com.jinke.calligraphy.app.branch.MyView;
import com.jinke.calligraphy.app.branch.R;
import com.jinke.calligraphy.app.branch.Start;
import com.jinke.kanbox.DownloadAllFileThread;
import com.jinke.kanbox.Kanbox;
import com.jinke.kanbox.KanboxAsyncTask;
import com.jinke.kanbox.KanboxException;
import com.jinke.kanbox.RequestListener;
import com.jinke.kanbox.Token;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnKeyListener;
import android.net.sip.SipRegistrationListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Adapter;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemSelectedListener;

public class Cloud extends Activity implements RequestListener {

	public static final int USE_THIS_CLOUD_BACKUP = 0;
	public static final int SHOW_THUMB_GRIDVIEW = 1;
	public static final int SHOW_PROGRESS_DIALOG = 2;

	private SimpleAdapter listAdapter;
	private LinkedList<Map<String, String>> data;
	private LinkedList<Map<String, String>> dataForDisplay;
	List<String> list;
	private int selectedItem = 0;
	DateFormat format = null;
	ListView list1;
	GridView thumbGrid;
	ArrayList<HashMap<String, Object>> imageItem;
	CloudGridAdapter adapter;

	public static Handler handler;
	public static Handler getIndexBitmapFinishhandler;

	ArrayList<String> thumbList;
	cloudItem item;
	private ProgressBar bar;
	private int lengthLimit = 5;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cloud);

		bar = (ProgressBar) findViewById(R.id.progressDownloadThumb);

		initHandler();
		createListViewData();
		getDataFromDialog();
		setListViewAdapter();
		setListViewListener();
	}

	private void initHandler() {
		if (handler == null) {
			handler = new Handler() {
				@Override
				public void handleMessage(Message msg) {

					switch (msg.what) {
					case SHOW_THUMB_GRIDVIEW:
						getThumbList(msg);
						break;
					}
				}
			};
		}

		if (getIndexBitmapFinishhandler == null) {
			getIndexBitmapFinishhandler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					item = (cloudItem) msg.obj;
					if (item != null) {
						Log.e("Cloud33333333333333333333333",
								"getBitmap-------------------bitmap name:"
										+ (item.name) + " bitmap null:"
										+ (item.thumbBitmap == null));
						adapter.updateGridItem(item);
						adapter.notifyDataSetChanged();
						bar.setVisibility(View.GONE);
						// dismissDialog(SHOW_PROGRESS_DIALOG);

					} else
						Log.e("Cloud", "getBitmap------------------null");
				}
			};
		}

	}

	private void getThumbList(Message msg) {
		thumbList = (ArrayList<String>) msg.obj;
		ArrayList<String> tmp = new ArrayList<String>();
		for (String s : thumbList) {
			if (s.contains("index_")) {
				tmp.add(s);
				Kanbox.getInstance().downloadIndexBitmap(s, "",
						Token.getInstance(), this);
			}
		}
		thumbList = tmp;
		
		Log.e("Sort", ""+thumbList);
		Collections.sort(thumbList, new StrComparator());
		Log.e("Sort", ""+thumbList);
		try {
			showDialog(SHOW_THUMB_GRIDVIEW);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	private void createListViewData() {
		if (data == null)
			data = new LinkedList<Map<String, String>>();
		if (dataForDisplay == null)
			dataForDisplay = new LinkedList<Map<String, String>>();
	}

	private void setListViewAdapter() {
		list1 = (ListView) findViewById(R.id.cloudListView);
		String[] from = { Properyt.CLOUD_ARRAYLIST };
		int[] to = { R.id.textcloud };
		listAdapter = new SimpleAdapter(this, dataForDisplay, R.layout.clouditem, from,
				to);
		list1.setAdapter(listAdapter);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case USE_THIS_CLOUD_BACKUP:
			return useThisCloud();
		case SHOW_THUMB_GRIDVIEW:
			return showThumbDialog();
		case SHOW_PROGRESS_DIALOG:
			return showProgressDialog();
		}
		return null;
	}

	private Dialog showProgressDialog() {
		ProgressDialog pd = new ProgressDialog(Cloud.this);
		pd.setTitle("获取数据备份列表");
		pd.setMessage("正在获取数据,请稍候...");
		pd.show();
		return pd;

	}

	private Dialog showThumbDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(Cloud.this);
		builder.setTitle("手写预览");

		LayoutInflater inflater = LayoutInflater.from(Cloud.this);
		View view = inflater.inflate(R.layout.gridthumb, null);

		thumbGrid = (GridView) view.findViewById(R.id.thumbGridView);
		adapter = new CloudGridAdapter(this, thumbList);

		thumbGrid.setAdapter(adapter);
		thumbGrid.setFocusable(true);

		builder.setView(view);
		builder.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(DialogInterface dialog, int keyCode,
					KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK) {
					removeDialog(SHOW_THUMB_GRIDVIEW);
					bar.setVisibility(View.GONE);
					adapter.recycleBitmap();		
				}
				return false;
			}
		});
		builder.setPositiveButton("点击下载", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				Message msg = new Message();
				msg.what = -1;
				if (selectedItem != -1) {
					msg.obj = list.get(list.size() - selectedItem - 1);
					Log.e("dddd", list.get(list.size() - selectedItem - 1) + "");
					Start.kanboxDownloadHandler.sendMessage(msg);
				}

				removeDialog(SHOW_THUMB_GRIDVIEW);
				adapter.recycleBitmap();
				
				Intent intent = new Intent();
				intent.setClass(Cloud.this,DownloadProgressActivity.class);
				startActivity(intent);
				
				finish();
			}
		});

		return builder.create();
	}

	private Dialog useThisCloud() {
		AlertDialog.Builder builder = new AlertDialog.Builder(Cloud.this);
		builder.setTitle("提示");
		builder.setMessage("是否恢复到该时间点?");
		builder.setPositiveButton("是", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				Message msg = new Message();
				msg.what = -1;
				if (selectedItem != -1) {
					msg.obj = list.get(list.size() - selectedItem - 1);
					Log.e("ssss", list.get(list.size() - selectedItem - 1) + "");
					// 启动下载线程
					
					Intent intent = new Intent();
					intent.setClass(Cloud.this,DownloadProgressActivity.class);
					startActivity(intent);
					
					Start.kanboxDownloadHandler.sendMessage(msg);
					finish();
				}
			}
		});
		builder.setNegativeButton("否", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

			}
		});
		return builder.create();

	}

	/**
	 * 加载listview监听 1.单击询问是否使用 2.长按显示拇指图列表
	 * */
	private void setListViewListener() {

		list1.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				selectedItem = arg2;
				System.out.println("--------------------show dialog before");
				showDialog(USE_THIS_CLOUD_BACKUP);
			}
		});

		list1.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				selectedItem = arg2;

				Log.e("Cloud",
						"long click "
								+ list.get(list.size() - selectedItem - 1));
				bar.setVisibility(View.VISIBLE);
				// showDialog(SHOW_PROGRESS_DIALOG);
				(new DownloadAllFileThread(
						DownloadAllFileThread.OP_GETTHUMBLIST, list.get(list
								.size() - selectedItem - 1))).start();
				return true;

			}
		});

	}

	/**
	 * 从主线程获得数据列表
	 * */
	private void getDataFromDialog() {
		Intent intent = this.getIntent();
		Bundle extra = intent.getExtras();
		list = extra.getStringArrayList(Properyt.CLOUD_ARRAYLIST);
		Log.e("listt", list + "");
		final String[] folderName = new String[list.size()];
		getFormat(list, folderName);

		Map<String, String> map;
		for (int i = 0; i < folderName.length; i++) {
			map = new HashMap<String, String>();
			map.put(Properyt.CLOUD_ARRAYLIST, folderName[i]);
			data.add(0, map);
			dataForDisplay.add(0,map);
		}
		
		cutDisplayData();
	}

	private void cutDisplayData() {
		if (dataForDisplay.size() > lengthLimit){
			int size = dataForDisplay.size();
			for(int i = lengthLimit; i < size; i++){
				dataForDisplay.remove(lengthLimit);
			}
		}
	}

	/**
	 * 规定时间格式 文字+时间
	 * */
	private void getFormat(List<String> list, final String[] folderName) {
		for (int i = 0; i < list.size(); i++) {
			folderName[i] = list.get(i);
			String tmp = folderName[i];
			Log.e("Cloud", "tmp:" + tmp);

			if ("calligraphy".equals(tmp)) {
				folderName[i] = "当前最新";
				continue;
			}
		
			String time = tmp.substring("calligraphy".length(),
					"calligraphy".length() + "yyyyMMddHHmmss".length());
			
			//java.lang.StringIndexOutOfBoundsException
			format = new java.text.SimpleDateFormat("yyyyMMddHHmmss");
			try {
				Date d = format.parse(time);
				format = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");
				time = format.format(d);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			folderName[i] = "网络备份" + time;
		}
	}

	@Override
	public void downloadProgress(long currSize) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onComplete(String response, int operationType) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onError(KanboxException error, int operationType) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onError(KanboxException error, int operationType, String path,
			String destPath) {
		// TODO Auto-generated method stub

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
		
//		if(o1.length() < o2.length())
//			return o1.compareTo(o2);
//		return -o1.compareTo(o2);

	}

}
