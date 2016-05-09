package com.jinke.calligraphy.app.branch;

import hallelujah.cal.CalligraphyVectorUtil;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.Media;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListPopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.itextpdf.xmp.options.Options;
import com.jinke.calligraphy.activity.DownloadProgressActivity;
import com.jinke.calligraphy.activity.MainTab;
import com.jinke.calligraphy.activity.Properyt;
import com.jinke.calligraphy.backup.CalligraphyBackupUtil;
import com.jinke.calligraphy.command.BackupCommand;
import com.jinke.calligraphy.command.EditStatus;
import com.jinke.calligraphy.command.UploadCommand;
import com.jinke.calligraphy.database.CDBPersistent;
import com.jinke.calligraphy.database.CalligraphyDB;
import com.jinke.calligraphy.date.DateSlider;
import com.jinke.calligraphy.date.DateTimeSlider;
import com.jinke.calligraphy.date.GoogleCalendarUtil;
import com.jinke.calligraphy.date.TimeLabeler;
import com.jinke.calligraphy.template.WolfTemplateUtil;
import com.jinke.kanbox.DownloadAllFileThread;
import com.jinke.kanbox.DownloadEntity;
import com.jinke.mywidget.FileListDialog;
import com.jinke.newly.Config;
import com.jinke.rloginservice.IReadingsLoginService;
import com.jinke.rloginservice.UserInfo;
import com.jinke.single.BitmapCount;
import com.jinke.single.BitmapUtils;
import com.jinke.single.ScaleSave;

public class Start extends Activity implements OnGestureListener, OnTouchListener{
	// 01-05 23:36:26.701: ERROR/AndroidRuntime(9178): at
	// com.jinke.calligraphy.database.CDBPersistent.getCharListByPageAndID(CDBPersistent.java:638)

	public static final String CLIENT_ID = "45fd6312c0c847d62017e483f05f5f50";		//申请的api应用的client_id
	public static final String CLIENT_SECRET = "adf5b6555197ee52d8dbbd7ec1cb3fb9";	//申请的api应用的client_secret
	public static final String REDIRECT_URI = "https://www.kanbox.com";		//重定向url，可自行修改
	
	public static Context context;
	public static Activity instance;
	public static SharedPreferences LoginInfo;
	public static EditStatus status;
	public static Bitmap OOM_BITMAP;
	public static Bitmap EMPTY_BITMAP;
//	public static Bitmap BUTTOM_LINE_BITMAP;
	public static Bitmap RED_ARROW_BITMAP;
	public static Bitmap BLACK_ARROW_BITMAP;
	public static Bitmap backgroundBitmap;
	
//	public static Uri saveBitmapUri; 
//	public static OutputStream imageFileOS;
//	
	public static String storagePath = "";
	static ProgressBar bar;
	public static TextView barText;
	
	public static final String WIFI = "wifi";
	public static final String ADHOC = "adhoc";
	public static String netStatus = WIFI; 
	// 定义广播Action
	private static final String BC_ACTION = "com.jinke.calligraphy.action.BC_ACTION";

	public static List<Uri> picList = new ArrayList<Uri>();
	public static List<Cursor> picCursor = new ArrayList<Cursor>();
	public static int picListIndex;
	public static int picCursorIndex;
	
	public static List<String> picName = new ArrayList<String>();
	public static int picNameIndex;
	
	public static Calligraph c;
	public static int totlePageNum = 0;
	public static int PAGENUM = 1;
	public static String date = "";
	private static AlertDialog.Builder builder;

	static final int DATETIMESELECTOR_ID = 5;

	public static final int AddPictureRequest = 1;
	public static final int AddCameraRequest = 2;
	public static final int AddVideoRequest = 3;
	public static final int AddAudioRequest = 4;
	public static final String TempImgFilePath = "/sdcard/calligraphy/img.jpg";

	public static final String TempImgUpPath = "/sdcard/calligraphy/up.jpg";
	
	//gesture detect
	GestureDetector mGestureDetector;
	
	public static int SCREEN_WIDTH;
	public static int SCREEN_HEIGHT;
	private static float density;
	
	public static int limit_num = 1;
	public static int autoSaveTime = 2;
	private boolean paused = false;
	public static ParametersDialog parameterDialog;
	public static boolean netUploadLogin = false;
	public static boolean netDownloadLogin = false;
	
	public static double auto_upload_time = 10.3;

	public static final int PAGE_CHANGE = -1;
	public static final int PAGE_DELETE = -2;
	public static Handler pageChangeHandler = new Handler(){
		public void handleMessage(Message msg) {
			if(msg.what == PAGE_CHANGE){
				//caoheng 2015.11.24不让乱保存
			//	c.view.saveDatebase();
				PAGENUM = msg.arg1;
				if(c.view.drawStatus != MyView.STATUS_DRAW_CURSOR)
				{
					Log.e("CalligraphyIndex", "pagenum:" + PAGENUM);
					Calligraph.mDrawStatusChangeBtn
					.setBackgroundResource(R.drawable.status_tuyasel);
					c.view.changeDrawState(MyView.STATUS_DRAW_CURSOR);
				}
			}else if(msg.what == PAGE_DELETE){
				int deletePage = msg.arg1;
				Log.e("CalligraphyIndex", "delete:" + deletePage);
				CDBPersistent db = new CDBPersistent(context);
				db.open();
				db.deletePage(deletePage);
				db.close();
				
				if (deletePage <= getPageNum()) {
					Start.delPageNum();
				}
				resetTotalPagenum();
				
			}
			ImageLimit.instance().resetImageCount();
			WordLimit.getInstance().resetWordCount();
			bar.setVisibility(View.VISIBLE);
			barText.setText("正在载入");
			barText.setVisibility(View.VISIBLE);
			(new TT()).start();
			
		};
	};
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		Log.e("okkeydown", "onkeydown KEY_BACK" + (keyCode == KeyEvent.KEYCODE_BACK));
		if(keyCode == 126 || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE){
			//126  3.0新增的KEYCODE_MEDIA_PLAY按键。
			
//			Start.saveHandler.sendEmptyMessage(0);
			
			Start.status.modified("tuya");
			if(c.view.drawStatus == MyView.STATUS_DRAW_FREE) {
				c.view.changeStateAndSync(MyView.STATUS_DRAW_CURSOR);
				c.view.cursorBitmap.initDate(WolfTemplateUtil.getCurrentTemplate());
			} else {
				c.view.changeStateAndSync(MyView.STATUS_DRAW_FREE);
			}
			return true;
		}
		if(keyCode == KeyEvent.KEYCODE_1){
			Log.e("KeyCode", "1 black");
			c.view.colorChanged(Color.BLACK);
		}
		if(keyCode == KeyEvent.KEYCODE_2){
			Log.e("KeyCode", "2 red");
			c.view.colorChanged(Color.RED);
		}
		if(keyCode == KeyEvent.KEYCODE_0){
			Log.e("KeyCode", "3 blue");
			c.view.colorChanged(Color.BLUE);
		}
		if(keyCode == KeyEvent.KEYCODE_3){
			Log.e("KeyCode", "4 green");
			c.view.colorChanged(Color.GREEN);
		}
		if(keyCode == KeyEvent.KEYCODE_BACK){
			Log.e("KeyCode", "keycode back");
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	public static Handler fileListHandler = new Handler() {
        FileListDialog fdialog;
        public void handleMessage(android.os.Message msg) {
        	
        		DownloadProgressActivity.barTextHandler.sendEmptyMessage(DownloadProgressActivity.KANBOX_START_FILELIST);
        	
        		ArrayList<String> list = (ArrayList<String>) msg.obj;
                Log.v("listlocal",list+"");
//                fdialog = new FileListDialog(context , list);
                
                Intent i = new Intent();
                Bundle b = new Bundle();
                b.putStringArrayList("dirList", list);
                i.putExtras(b);
                i.setClass(Start.context, MainTab.class);
                Start.context.startActivity(i);
                
        };
};

	
	/*
	public static Handler saveHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
//			 Toast.makeText(context, "自动保存", Toast.LENGTH_LONG).show();
//			 c.view.saveDatebase();
//			new UploadCommand(context, null,false).execute();
			
			
//			EditableCalligraphy editable = null;
//			 boolean needSaved = false;
//			 for(int i=0;i<c.view.cursorBitmap.listEditableCalligraphy.size();i++){
//				 editable = c.view.cursorBitmap.listEditableCalligraphy.get(i);
//				 for(int j=0;j<editable.charList.size();j++){
//					 if(editable.charList.get(j).getSaved()){
//						 needSaved = true;
//					 }
//				 }
//			 }
//			Toast.makeText(context, "自动保存", Toast.LENGTH_LONG).show();
			 if (Start.status.isNeedSave()) {
				 	Toast.makeText(context, "自动保存", Toast.LENGTH_LONG).show();
//					Start.status.resetStatus();
					
					(new AutoSaveThread()).start();
			}
			
		};
	};
	*/
	public static Handler kanboxUploadHandler = new Handler() {
		//03-05 17:19:24.275: ERROR/AndroidRuntime(10544): 
//		Caused by: java.lang.RuntimeException: 
//		Can't create handler inside thread that has not called Looper.prepare()

		public void handleMessage(android.os.Message msg) {
			if(msg.what == 2){
				Log.e("autoupload", "auto upload !!!!!!!!!!");
				removeMessages(2);
				Log.e("autoupload", "auto upload delay " + (long)(Start.auto_upload_time * 60 * 4) +"!!!!!!!!!!");
				sendEmptyMessageDelayed(2, (long)(Start.auto_upload_time * 60 * 4));
				return;
			}
			
			if(msg.what == 1){
				//自动上传，检测有没有网络
				
				if(!checkNetworkInfo()){
					Toast.makeText(Start.context, "当前没有网络，不进行上传",
							Toast.LENGTH_LONG).show();
					return;	
				}
				Intent uploadintent = new Intent();
				uploadintent.putExtra("type", DownloadProgressActivity.AUTO_UPLOAD);
				uploadintent.setClass(Start.context, DownloadProgressActivity.class);
				Start.context.startActivity(uploadintent);
				Log.e(TAG, ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>startActivity");
				//等初始化控件完成之后，再开始上传，否则会报异常。
				return;
			}
			if(!checkNetworkInfo())
				dialogNet();
			
			redownload = 0;
			//caoheng 2015.11.24不让乱保存
		//	c.view.saveDatebase();
//			barText.setVisibility(View.VISIBLE);
//			barText.setText("保存当前页，开始上传");
//			if(msg.what == 0)
				new UploadCommand(Start.context, new Handler() {
					@Override
					public void handleMessage(Message msg) {
						// TODO Auto-generated method stub
						if (msg.what != -1)
							Toast.makeText(Start.context, "已经更新到服务器",
									Toast.LENGTH_LONG).show();
						else
							Toast.makeText(Start.context, "更新出现异常，请重试",
									Toast.LENGTH_LONG).show();
						
					}
				},true).execute();
//			else
//				new BackupCommand(Start.context, backupHandler).execute();
//			Toast.makeText(Start.context, "开始上传", Toast.LENGTH_SHORT).show();
		};
	};
	public static Handler kanboxDownloadHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			
			checkNetworkInfo();
			redownload = 0;
			
			if(msg.what == -1){
				//下载具体文件
				
//				barText.setVisibility(View.VISIBLE);
//				barText.setText("开始从酷盘服务器下载");
				String dirName = (String)msg.obj;
				Log.e("Start", "---------------------------------------startDownload" + dirName);
				new BackupCommand(Start.context, backupHandler,DownloadAllFileThread.OP_DOWNLOAD,dirName).execute();
			}else{
				//获取文件列表
//				barText.setVisibility(View.VISIBLE);
//				barText.setText("开始从酷盘服务器下载");
				new BackupCommand(Start.context, backupHandler,DownloadAllFileThread.OP_GETLIST,"").execute();
			}
			
//			barText.setVisibility(View.VISIBLE);
//			barText.setText("开始从酷盘服务器下载");
//			new BackupCommand(Start.context, backupHandler).execute();
//			Toast.makeText(Start.context, "开始下载", Toast.LENGTH_SHORT).show();
		};
	}; 

	public static Handler saveToDatebaseHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			 
//			 c.view.saveDatebase();
			 EditableCalligraphy editable = null;
			 boolean needSaved = false;
			 for(int i=0;i<c.view.cursorBitmap.listEditableCalligraphy.size();i++){
				 editable = c.view.cursorBitmap.listEditableCalligraphy.get(i);
				 for(int j=0;j<editable.charList.size();j++){
					 if(editable.charList.get(j).getSaved()){
						 needSaved = true;
					 }
				 }
			 }
			 if (Start.status.isNeedSave()) {
				 	Toast.makeText(context, "自动保存", Toast.LENGTH_LONG).show();
					Start.status.resetStatus();
					(new AutoSaveThread()).start();
			}
			sendEmptyMessageDelayed(1, autoSaveTime * 700000);
		};
	};
	
	public static final int KANBOX_CHECK_AUTHOR = 1;
	public static final int KANBOX_CHECK_TOKEN = 2;
	
	public static final int KANBOX_START_FILELIST = 19;
	public static final int KANBOX_START_DOWNLOAD = 3;
	public static final int KANBOX_START_UPLOAD = 4;
	public static final int KANBOX_START_UPLOAD_PAGE = 5;
	
	public static final int KANBOX_GET_FILELIST = 6;
	
	
	public static final int KANBOX_END_DBDOWNLOAD = 7;
	public static final int KANBOX_END_MKDIR = 8;
	public static final int KANBOX_END_UPLOAD_PAGE = 9;
	public static final int KANBOX_END_UPLOAD = 10;
	
	
	public static final int KANBOX_FINISH_UPLOAD = 11;
	public static final int KANBOX_FINISH_REFRESHTOKEN = 12;
	public static final int KANBOX_FINISH_DOWNLOAD = 13;
	public static final int START_FINISH = 14;
	
	public static final int KANBOX_ERROR = 15;
	public static final int KANBOX_ERROR_DOWNLOAD = 16;
	public static final int KANBOX_ERROR_UPLOAD = 17;
	public static final int KANBOX_ERROR_REFRESHTOKEN = 18;
	
	
	private static List<DownloadEntity> downloadList = new ArrayList<DownloadEntity>();
	private static int  MAX_REDOWNLOAD = 3;
	private static int  MAX_LINES = 30;
	private static int redownload = 0;
	private static int lines = 0;
	public static boolean addDownloadEnty(DownloadEntity newEnty){
//		for(DownloadEntity enty : downloadList){
//			if(enty.getPath().equals(newEnty.getPath()))
//				return false;
//		}
		downloadList.add(newEnty);
		return true;
	}
	public static void clearDownloadList(){
		downloadList.clear();
	}
	public static List<DownloadEntity> getDownloadList(){
		synchronized (downloadList) {
			return downloadList;
		}
		
	}
	
	public static Handler barTextHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			// Toast.makeText(context, "自动保存", Toast.LENGTH_LONG).show();
			// c.view.saveDatebase();
			switch (msg.what) {
			case KANBOX_GET_FILELIST:
				lines ++;
//				barText.setText(barText.getText() + "\n" + "获取文件列表成功，开始下载具体文件");
				break;
			case KANBOX_START_DOWNLOAD:
			case KANBOX_END_DBDOWNLOAD:
			case KANBOX_START_UPLOAD:
			case KANBOX_END_UPLOAD_PAGE:
			case KANBOX_END_UPLOAD:
			case KANBOX_END_MKDIR:
			case KANBOX_START_UPLOAD_PAGE:
			case KANBOX_ERROR:
				lines ++;
				if(lines > MAX_LINES){
//					barText.setText((String)msg.obj);
					lines = 0;
				}else
//					barText.setText(barText.getText() + "\n" + (String)msg.obj);
				break;
			case START_FINISH:
				
//				CDBPersistent db = new CDBPersistent(context);
//				db.open();
//				int template_byPage = db.getTemplateByPage(Start.getPageNum());
//				
//				c.view.doChangeBackground(WolfTemplateUtil
//						.getTypeByID(template_byPage));
//				db.close();
//				
//				for (int i = 0; i < CursorDrawBitmap.listEditableCalligraphy.size(); i++) {
//					CursorDrawBitmap.listEditableCalligraphy.get(i)
//							.initDatabaseCharList();
//				}
				for (int i = 0; i < CursorDrawBitmap.listEditableCalligraphy.size(); i++) {
					CursorDrawBitmap.listEditableCalligraphy.get(i)
							.initFlipBlock();
				}
				c.view.cursorBitmap.updateHandwriteState();
				Log.v("flipper", "                      START_FINISH updateHandwriteState!! ");
				c.view.freeBitmap.resetFreeBitmapList();
				bar.setVisibility(View.GONE);
				barText.setVisibility(View.GONE);
				break;
			case KANBOX_FINISH_DOWNLOAD:
				lines = 0;
				Toast.makeText(context, "恢复全部完成", Toast.LENGTH_LONG).show();
				barText.setVisibility(View.INVISIBLE);
				CalligraphyDB.getInstance(Start.context).resetDB();
				Log.e("download", "clear data after download -----------------------------------");
				c.view.cursorBitmap.clearDataBitmap();
				
				Log.e("create", "------initParsedWordList Start Handler");
				CalligraphyVectorUtil.initParsedWordList(Start.getPageNum());
				
				for (int i = 0; i < CursorDrawBitmap.listEditableCalligraphy
						.size(); i++) {
					CursorDrawBitmap.listEditableCalligraphy.get(i)
							.initDatabaseCharList();
				}
				c.view.cursorBitmap.updateHandwriteState();
				break;
			case KANBOX_FINISH_UPLOAD:
				lines = 0;
				Toast.makeText(context, "上传全部完成", Toast.LENGTH_LONG).show();
//				barText.setVisibility(View.INVISIBLE);
				break;
			case KANBOX_ERROR_DOWNLOAD:
				lines ++;
//				barText.setText(barText.getText() + "\n" + (String)msg.obj);
				
				for(DownloadEntity enty : downloadList){
//					barText.setText(barText.getText() + "\n" + enty.getPath() +"需要重新传输");	
				}
				
				if(redownload < MAX_REDOWNLOAD){
					redownload ++;
					lines ++;
//					barText.setText(barText.getText() + "\n" + "重新启动失败的任务");
					
					new BackupCommand(Start.context, backupHandler).execute();
				}else{
					lines ++;
//					barText.setText(barText.getText() + "\n" + "失败"+MAX_REDOWNLOAD+"次，请稍后重试恢复功能");
				}
				
				
				break;
			case KANBOX_ERROR_UPLOAD:
//				if(barText.getVisibility() == View.INVISIBLE)
//					barText.setVisibility(View.VISIBLE);
				
				lines ++;
//				barText.setText(barText.getText() + "\n" + (String)msg.obj);
				
				for(DownloadEntity enty : downloadList){
//					barText.setText(barText.getText() + "\n" + enty.getPath() +"需要重新上传");	
				}
				
				if(redownload < MAX_REDOWNLOAD){
					redownload ++;
					lines ++;
//					barText.setText(barText.getText() + "\n" + "重新启动失败的任务");
					
					new UploadCommand(Start.context, new Handler() {
						@Override
						public void handleMessage(Message msg) {
							// TODO Auto-generated method stub
							if (msg.what != -1)
								Toast.makeText(Start.context, "已经更新到服务器",
										Toast.LENGTH_LONG).show();
							else
								Toast.makeText(Start.context, "更新出现异常，请重试",
										Toast.LENGTH_LONG).show();
							
						}
					}, true).execute();
				}else{
					lines ++;
//					barText.setText(barText.getText() + "\n" + "失败"+MAX_REDOWNLOAD+"次，请稍后重试酷盘功能");
				}
				
				
				break;
			default:
				break;
			}
			
		};
	};
	public static Matrix m;
	
	public void dismissButtom(){
		if(android.os.Build.VERSION.SDK_INT >=11)
			getWindow().getDecorView( ).setSystemUiVisibility
			  (View.SYSTEM_UI_FLAG_LOW_PROFILE); 
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
//		        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()                   
//		        .detectDiskReads()                   
//		        .detectDiskWrites()                   
//		        .detectNetwork()   // or .detectAll() for all detectable problems                   
//		        .penaltyLog()                   
//		        .build());           
//		         StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()                   
//		        .detectLeakedSqlLiteObjects()                   
//		        .detectLeakedClosableObjects()                   
//		        .penaltyLog()                   
//		        .penaltyDeath()                  
//		        .build());       
		try{
				if(instance == null){
					Log.e("Start", "!!!!!!!!!!!!!onCreate");
					dismissButtom();
					parameterDialog = new ParametersDialog(this);
					Intent intent = new Intent(
							"com.jinke.rloginservice.IReadingsLoginService");
					if (bindService(intent, isLoginConn, Start.context.BIND_AUTO_CREATE)) {
						// Toast.makeText(CMain.this,
						// "bindService() Success",Toast.LENGTH_LONG).show();
					} else {
			
					}
					chackDevice();
					
					//恢复之前保存的Timer参数值(如果有的话)
			    	SharedPreferences settings = this.getSharedPreferences(ParametersDialog.FILENAME,  android.content.Context.MODE_PRIVATE);
					int progress = settings.getInt(ParametersDialog.PARAM_AUTO_UPLOAD_TIME, -1);
					if (progress != -1) {
						auto_upload_time = 	ParametersDialog.minAutoUploadTime + progress * ParametersDialog.autoUploadTimeFactor;
					}
			
					Log.e("state", "onCreate ----------" + CalligraphyBackupUtil.getSimID());
					DisplayMetrics dm = new DisplayMetrics();
					getWindowManager().getDefaultDisplay().getMetrics(dm);
					SCREEN_WIDTH = dm.widthPixels;
					SCREEN_HEIGHT = dm.heightPixels;
			
					float f = getResources().getDisplayMetrics().density;
					density = f;
			
					context = this;
					instance = this;
					status = new EditStatus();
					LoginInfo = getSharedPreferences("LoginInfo", MODE_WORLD_WRITEABLE);
					autoSaveTime = LoginInfo.getInt("autosavetime", 2);
					
			//		saveToDatebaseHandler.sendEmptyMessageDelayed(1,autoSaveTime * 700000);
					
					if(OOM_BITMAP == null)
						OOM_BITMAP = readBitMap(context, R.drawable.oom);
					if(EMPTY_BITMAP == null)
						EMPTY_BITMAP = readBitMap(context, R.drawable.empty_word);
			//		BUTTOM_LINE_BITMAP = readBitMap(context, R.drawable.buttomline);
					if(RED_ARROW_BITMAP == null)
						RED_ARROW_BITMAP = readBitMap(context, R.drawable.red_jiantou);
					if(BLACK_ARROW_BITMAP == null)
						BLACK_ARROW_BITMAP = readBitMap(context, R.drawable.black_jiantou);
					
					Intent alarmIntent = getIntent();
					int alarmPagenum = -1;
					if (alarmIntent != null) {
						alarmPagenum = alarmIntent.getIntExtra("pagenum", -1);
					}
					PAGENUM = 0;
					
					//ly：可优化，减少一次Matrix对象的创建
					m = CDBPersistent.getMatrix(LoginInfo.getString("matrix",
							new Matrix().toString()));
					//ly：这个地方有什么用？
					float[] v = new float[9];
					m.getValues(v);
					
					
					c = new Calligraph(this);
					setContentView(c);
					c.view.setMMMatirx(m);
					Log.e("changematrix", "change Matrix Start");
					c.view.setMMMatirx(m);
					LinearLayout statusLayout = new LinearLayout(Start.this);
					statusLayout.setOrientation(LinearLayout.VERTICAL);
					LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT,
							LayoutParams.WRAP_CONTENT);
					lp.addRule(RelativeLayout.CENTER_IN_PARENT);
					bar = new ProgressBar(Start.this);
					bar.setVisibility(View.VISIBLE);
					statusLayout.addView(bar, lp);
			
					barText = new TextView(Start.this);
					barText.setText("正在加载");
					barText.setVisibility(View.GONE);
					barText.setTextColor(Color.BLACK);
					barText.setTextSize(20);
					barText.setVerticalScrollBarEnabled(true);
					statusLayout.addView(barText, lp);
			
					c.addView(statusLayout, lp);
			
			//		c.view.setFreeDrawBitmap();
			
					c.view.setFocusable(true);
					c.view.setSelected(true);
					Log.e("state", "isFocused:" + c.view.isFocused());
					c.view.requestFocus();
					Log.e("state", "isFocused:" + c.view.isFocused());
					
					if (alarmPagenum == -1) {
						PAGENUM = LoginInfo.getInt("pagenum", 1);
						Log.e("Start", "not alarm start ! alarmPagenum:" + alarmPagenum);
					} else {
			
						PAGENUM = alarmPagenum;
						Log.e("Start", "alarm start ! alarmPagenum:" + alarmPagenum);
					}
					
					barText.setText("正在载入");
					barText.setVisibility(View.VISIBLE);
					c.view.cursorBitmap.updateHandwriteState();
					Log.v("startinit", "start TT thread:" + PAGENUM + "set visiable");
					(new TT()).start();
				}else{
					Log.e("Start", "!!!!!!!!!!!!! not onCreate");
					ViewGroup vg = (ViewGroup)c.getParent();
					vg.removeView(c);
					setContentView(c);
					
				}
				
		//		saveBitmapUri = getContentResolver().insert(Media.EXTERNAL_CONTENT_URI, new ContentValues());
		//		imageFileOS = getContentResolver().openOutputStream(saveBitmapUri);	
		//		
				c.view.setOnTouchListener(this);
				c.view.setFocusable(true);
				
				mGestureDetector = new GestureDetector(this, this);
				mGestureDetector.setIsLongpressEnabled(true);
				
		}catch (Exception e) {
			
		}
	}

	static class TT extends Thread{
		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			
				c.view.cursorBitmap.clearDataBitmap();
//				Log.e("create", "------TT Start");
//				CalligraphyVectorUtil.initParsedWordList(Start.getPageNum());
				
				//暂时不考虑换页切换模板的问题
//				CDBPersistent db = new CDBPersistent(context);
//				db.open();
//				int template_byPage = db.getTemplateByPage(Start.getPageNum());
//				c.view.doChangeBackground(WolfTemplateUtil
//						.getTypeByID(template_byPage));
//				db.close();
				ScaleSave.getInstance().newPage();
				//caoheng 1101不知道为嘛
//				for (int i = 0; i < CursorDrawBitmap.listEditableCalligraphy.size(); i++) {
//					CursorDrawBitmap.listEditableCalligraphy.get(i)
//							.initDatabaseCharList();v
//				}
				barTextHandler.sendEmptyMessage(START_FINISH);
		}
		
	}
	
	private static void dialogNet() {

		builder = new Builder(Start.context);
		builder.setMessage("当前wifi尚未打开，请点击 \"设置\" 选择网络。");
		// builder.setMessage(getString(R.string.Dialog_Login_Msg).toString());
		// builder.setTitle(getString(R.string.Dialog_Login_Title).toString());
		builder.setTitle("网络链接");
		// builder.setNegativeButton(getString(R.string.Button_exit).toString(),
		// new DialogInterface.OnClickListener() {
		builder.setNegativeButton("下次再说",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});

		// builder.setPositiveButton(getString(R.string.Button_ok).toString(),
		// new DialogInterface.OnClickListener() {
		builder.setPositiveButton("设置", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				c.closeADHoc();
				Start.context.startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));// 进入无线网络配置界面
				dialog.dismiss();
			}
		});

		builder.create().show();
	}

	private static boolean checkNetworkInfo() {
		ConnectivityManager conMan = (ConnectivityManager) Start.context.getSystemService(Context.CONNECTIVITY_SERVICE);

		// mobile 3G Data Network
		//ly
		//State mobile = conMan.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();

		// wifi
		State wifi = conMan.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
				.getState();
		// Toast.makeText(main.this,
		// "3G:"+mobile.toString()+"\nwifi:"+wifi.toString(),
		// Toast.LENGTH_SHORT).show(); //显示3G网络连接状态
		if (wifi != State.CONNECTED) {
//			dialogNet();
			return false;

		} else {
			// Toast.makeText(this, "网络连接成功", Toast.LENGTH_SHORT).show();
			return true;
		}
	}

	// 点击Menu时，系统调用当前Activity的onCreateOptionsMenu方法，并传一个实现了一个Menu接口的menu对象供你使用
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		/*
		 * CDBPersistent db = new CDBPersistent(this); db.open(); //
		 * totlePageNum =
		 * db.getTotalPageNumByTemplateID(c.view.mTemplate.getId());
		 * totlePageNum = db.getTotalPageNum(); db.close();
		 */
		// resetTotalPagenum();只在程序进入，保存，删除时重置应该就可以了。

		Log.e("databases", "pre totlePageNum:" + totlePageNum);
		/*
		 * add()方法的四个参数，依次是： 1、组别，如果不分组的话就写Menu.NONE,
		 * 2、Id，这个很重要，Android根据这个Id来确定不同的菜单 3、顺序，那个菜单现在在前面由这个参数的大小决定
		 * 4、文本，菜单的显示文本
		 */

		menu.add(Menu.NONE, Menu.FIRST + 1, 1, "前一页de").setIcon(
				android.R.drawable.ic_menu_set_as);
		// setIcon()方法为菜单设置图标，这里使用的是系统自带的图标，同学们留意一下,以
		// android.R开头的资源是系统提供的，我们自己提供的资源是以R开头的
		Log.e("databases", "create totlePageNum:" + totlePageNum);
		menu.add(Menu.NONE, Menu.FIRST + 2, 2, PAGENUM + "/" + totlePageNum)
				.setIcon(android.R.drawable.ic_menu_help);

		menu.add(Menu.NONE, Menu.FIRST + 3, 3, "后一页").setIcon(
				android.R.drawable.ic_menu_help);

		menu.add(Menu.NONE, Menu.FIRST + 4, 4, "更新到服务器").setIcon(
				android.R.drawable.ic_menu_add);
		menu.add(Menu.NONE, Menu.FIRST + 5, 5, "同步服务器数据到本地").setIcon(
				android.R.drawable.ic_menu_info_details);
		menu.add(Menu.NONE, Menu.FIRST + 6, 6, "添加日程").setIcon(
				android.R.drawable.ic_menu_send);
		
//		menu.add(Menu.NONE, Menu.FIRST + 7, 7, "生成pdf到本地").setIcon(
//				android.R.drawable.ic_menu_send);
//		menu.add(Menu.NONE, Menu.FIRST + 8, 8, "制作添加翰林算子").setIcon(
//				android.R.drawable.ic_menu_send);
		

//		menu.add(Menu.NONE, Menu.FIRST + 7, 7, "改变刷新限制,当前:" + limit_num).setIcon(
//				android.R.drawable.ic_menu_send);
		// return true才会起作用
		return true;

	}

	// 菜单项被选择事件
	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case Menu.FIRST + 1:

			if (delPageNum()) {
//				bar.setVisibility(View.VISIBLE);
//				barText.setText("正在载入");
//				barText.setVisibility(View.VISIBLE);
//				(new TT()).start();
				pageChangeHandler.sendEmptyMessage(0);
				
//				Log.e("pre", PAGENUM + " " + totlePageNum);
//				// 按page换模板
//
//				CDBPersistent db = new CDBPersistent(this);
//				db.open();
//				int template_byPage = db.getTemplateByPage(Start.getPageNum());
//				c.view.doChangeBackground(WolfTemplateUtil
//						.getTypeByID(template_byPage));
//				db.close();
//				
//				CalligraphyVectorUtil.initParsedWordList(Start.getPageNum());
//
//				for (int i = 0; i < CursorDrawBitmap.listEditableCalligraphy
//						.size(); i++) {
//					CursorDrawBitmap.listEditableCalligraphy.get(i)
//							.initDatabaseCharList();
//				}
			} else {
				Toast.makeText(this, "最前一页", Toast.LENGTH_LONG).show();
			}
//			c.view.cursorBitmap.updateHandwriteState();
//			// c.view.setFreeDrawBitmap();
//			c.view.freeBitmap.resetFreeBitmapList();
			break;
		case Menu.FIRST + 2:

//			CalligraphyDB.getInstance(Start.context).getCurrentWordCount(5, 3);
//			BitmapToFile.savaBitmap();
			
			
			break;
		case Menu.FIRST + 3:

			if (addPageNum()) {

				// 按page换模板

//				bar.setVisibility(View.VISIBLE);
//				barText.setText("正在载入");
//				barText.setVisibility(View.VISIBLE);
//				(new TT()).start();
				pageChangeHandler.sendEmptyMessage(0);
			} else 
				Toast.makeText(this, "最后一页", Toast.LENGTH_LONG).show();

			
			break;
		case Menu.FIRST + 4:
			
			try {
				if (loginService.isLogin()) {
					// login
					uploadToKanbox();
				} else {
					// error
					netUploadLogin = true;
					dialog("离线状态无法使用，请先登录您的Readings帐号");
					// RequestBookActivity.this.finish();
				}
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
//			Intent uploadintent = new Intent();
//			uploadintent.putExtra("type", DownloadProgressActivity.UPLOAD);
//			uploadintent.setClass(Start.context, DownloadProgressActivity.class);
//			Start.context.startActivity(uploadintent);
//			
//			kanboxUploadHandler.sendEmptyMessage(0);
			

			break;
		case Menu.FIRST + 5:
			
			try {
				if (loginService.isLogin()) {
					// login
					downloadFromKanbox();
				} else {
					// error
					netDownloadLogin = true;
					dialog("离线状态无法使用，请先登录您的Readings帐号");
					// RequestBookActivity.this.finish();
				}
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
//			Intent intent = new Intent();
//			intent.setClass(Start.context, DownloadProgressActivity.class);
//			intent.putExtra("type", DownloadProgressActivity.DOWNLOAD);
//			Start.context.startActivity(intent);
//			
//			//点击事件不属于UI主线程，没有事件循环，在其中创建AsyncTask会出错
//			kanboxDownloadHandler.sendEmptyMessage(0);
			
			

			break;
		case Menu.FIRST + 6:

			// String picName = "/extsd/calldir/bigmap_"+getPageNum()+".png";
			// c.view.saveFile(c.view.baseBitmap.bitmap, picName,"PNG");
			//
			// FTPUtil.upload(picName);
			// Toast.makeText(this, "上传", Toast.LENGTH_LONG).show();

			// FTPUtil.downloadLocalCalldir();

			// new FtpCommand().execute();

			showDialog(DATETIMESELECTOR_ID);

			break;
			
//		case Menu.FIRST + 7:
//
//            Intent pdfIntent = new Intent();
//            pdfIntent.setClass(this, CloudActivity.class);
//            startActivity(pdfIntent);
//            break;
//		case Menu.FIRST + 8:
//			Intent mIntent = new Intent();
//			ComponentName comp = new ComponentName(
//		                         "com.studio.mindo",
//		                         "com.studio.mindo.MindoTestActivity");
//			mIntent.setComponent(comp);
//			mIntent.setAction("android.intent.action.VIEW");
//			startActivityForResult(mIntent, Properyt.MINDMAP_REQUEST_CODE);
//			break;
		}

		return false;
	}

	// 选项菜单被关闭事件，菜单被关闭有三种情形，menu按钮被再次点击、back按钮被点击或者用户选择了某一个菜单项
	@Override
	public void onOptionsMenuClosed(Menu menu) {
		// Toast.makeText(this, "选项菜单关闭了", Toast.LENGTH_LONG).show();
//		(new LocalCopyThread()).start();
//		getWindow().getDecorView().setSystemUiVisibility
//		  (View.SYSTEM_UI_FLAG_LOW_PROFILE);
	}

	// 菜单被显示之前的事件
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		/*
		 * CDBPersistent db = new CDBPersistent(this); db.open(); //
		 * totlePageNum =
		 * db.getTotalPageNumByTemplateID(c.view.mTemplate.getId());
		 * totlePageNum = db.getTotalPageNum(); db.close(); 改为先获取总页数，然后直接调用
		 */
		dismissButtom();
		resetTotalPagenum();

		Log.e("pre", "prepare " + PAGENUM);
		menu.getItem(Menu.FIRST).setTitle(PAGENUM + "/" + totlePageNum);
		return true;
	}
	private void uploadToKanbox(){
		Intent uploadintent = new Intent();
		uploadintent.putExtra("type", DownloadProgressActivity.UPLOAD);
		uploadintent.setClass(Start.context, DownloadProgressActivity.class);
		Start.context.startActivity(uploadintent);
		
		kanboxUploadHandler.sendEmptyMessage(0);
	}
	private void downloadFromKanbox(){
		Intent intent = new Intent();
		intent.setClass(Start.context, DownloadProgressActivity.class);
		intent.putExtra("type", DownloadProgressActivity.DOWNLOAD);
		Start.context.startActivity(intent);
		
		//点击事件不属于UI主线程，没有事件循环，在其中创建AsyncTask会出错
		kanboxDownloadHandler.sendEmptyMessage(0);
	}

	public static int getPageNum() {
		return PAGENUM;
	}

	public static boolean addPageNum() {
		if (PAGENUM + 1 <= totlePageNum) {
			//caoheng 2015.11.24不让乱保存
	//		c.view.saveDatebase();
			PAGENUM++;
			return true;
		} else {
			return false;
		}

	}

	public static boolean delPageNum() {
		Log.e("pre", "deletepagenum : " + PAGENUM);
		if (PAGENUM - 1 >= 1) {
			Log.e("pre", "deletepagenum : PAGENUM--" + PAGENUM);
			//caoheng 2015.11.24不让乱保存
	//		c.view.saveDatebase();
			PAGENUM--;
			Log.e("pre", "见过" + PAGENUM);
			return true;
		} else {
			return false;
		}

	}

	public static String getDate() {
		if (TextUtils.isEmpty(date))
			date = CDBPersistent.getCurrent();

		return date;
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.e("adhoc", "start call onDestroy");
//		if(!paused)
//			c.view.saveDatebase();
		
		Log.e("databases", "before destory -------------:");
		int page = totlePageNum;
		if (PAGENUM > totlePageNum)
			page = totlePageNum;
		else
			page = PAGENUM;

		LoginInfo
				.edit()
				.putInt("templateid",
						WolfTemplateUtil.getCurrentTemplate().getId())
				.putInt("pagenum", PAGENUM)
				.putString("templatetype",
						WolfTemplateUtil.getCurrentTemplate().getName())
				.putString("matrix", c.view.getMMMatrix().toString())
				.putInt("autosavetime", autoSaveTime)
				.commit();
		//caoheng 2015.11.24不让乱保存
	//	c.view.saveDatebase();
		
		ScaleSave.getInstance().close();
		Log.e("adhoc", "start call closeAdhoc");
		c.closeADHoc();
		c.destroy();

		c.view.cursorBitmap.clearDataBitmap();
//		unbindService(isLoginConn);
		
		Log.e("Start", "sleep");
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.e("Start", "killed");
		android.os.Process.killProcess(android.os.Process.myPid());
		

	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub
		super.finish();
		
		Log.e("finish", "finish");
	}

	
	
	public static void resetTotalPagenum() {
		CDBPersistent db = new CDBPersistent(context);
		db.open();
		totlePageNum = db.getTotalPageNum();
		db.close();
		Log.e("db", "resetTotalPagenum close");
	}

	public static void resetDate() {
		date = "";
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		Log.e("Start", "!!!!!!!!!!!!!onstart");
		super.onStart();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		Log.e("Start", "!!!!!!!!!!!!!stop");
		super.onStop();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub


		Log.e("Start", "!!!!!!!!!!!!!pause");
//		if(!Calligraph.wifiandadhocPause){
//			c.closeADHoc();
//			c.view.saveDatebase();
//		}
		Calligraph.wifiandadhocPause = false;
		paused = true;
		super.onPause();

	}

	
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
		Log.e("changed", "configuration changed !!!!!!!!!!!  do nothing");
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		Log.e("Start", "!!!!!!!!!!!!!resume");
		super.onResume();
		dismissButtom();
		
		
//		LoginInfo = getSharedPreferences("LoginInfo", MODE_WORLD_WRITEABLE);
//		Boolean isUpdate = LoginInfo.getBoolean("update", false);
//		LoginInfo.edit().putBoolean("update", false);
//		String code = "";
//		if(isUpdate){
//			//是由Kanbox授权页面返回的，读取code，进一步获取授权码，并上传
//			code = LoginInfo.getString("code", "");
//			if("".equals(code)){
//				//授权过程出错，code为空
//			}else{
//				//上传
//				Log.e("content", "code:" + code 
//						+"\n 继续");
//			}
//			
//		}
		
	}

	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		Log.e("Start", "!!!!!!!!!!!!!restart");
		super.onRestart();
		
		if(netDownloadLogin || netUploadLogin){
			try {
				if (loginService.isLogin()) {
					// login
					UserInfo userInfo = loginService.getUserInfo();
					if (userInfo != null)
						username = userInfo.getUsername();
					
					if(netUploadLogin){
						uploadToKanbox();
						netUploadLogin = false;
					}
					if(netDownloadLogin){
						downloadFromKanbox();
						netDownloadLogin = false;
					}
				} else {
					Toast.makeText(context, "没有登录，离线状态", Toast.LENGTH_LONG).show();
				}
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
//		Intent intent = new Intent(
//		"com.jinke.rloginservice.IReadingsLoginService");
//		if (bindService(intent, isLoginConn, Start.context.BIND_AUTO_CREATE)) {
//			// Toast.makeText(CMain.this,
//			// "bindService() Success",Toast.LENGTH_LONG).show();
//		} else {
//		
//		}

	}

	private DateSlider.OnDateSetListener mDateTimeSetListener = new DateSlider.OnDateSetListener() {
		public void onDateSet(DateSlider view, Calendar selectedDate) {
			// update the dateText view with the corresponding date
			int minute = selectedDate.get(Calendar.MINUTE)
					/ TimeLabeler.MINUTEINTERVAL * TimeLabeler.MINUTEINTERVAL;
			// dateText.setText(String.format("The chosen date and time:%n%te. %tB %tY%n%tH:%02d",
			// selectedDate, selectedDate, selectedDate, selectedDate, minute));
			// 28. 12月 2011 20：30

			selectedDate.set(Calendar.MINUTE, minute);

			String date = String.format("%tY/%tm/%te", selectedDate,
					selectedDate, selectedDate);
			String datetime = String.format("%tH:%02d", selectedDate, minute);

			// Toast.makeText(Start.this, "选择的日期：	"+
			// String.format("%tY年%tm月%te日	 %tH:%02d",
			// selectedDate, selectedDate, selectedDate,selectedDate, minute)
			// , Toast.LENGTH_LONG).show();

			c.view.cursorBitmap.insertAlarmItem(date, datetime);

			GoogleCalendarUtil util = new GoogleCalendarUtil(Start.this);
			boolean flag = util.addToGoogleCalendar("翰林-云记事", "翰林-云记事-第 "
					+ PAGENUM + " 份需要您的关注！", selectedDate);

			if (!flag) {
				Toast.makeText(Start.this, "该设备尚未绑定google帐号，请您先登录google账户",
						Toast.LENGTH_LONG).show();
				Log.e("alarm", "do not add alarm!");
				// return;
			}

			Log.e("alarm", "add alarm!");
			// 获得AlarmManager实例
			AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);

			// 实例化Intent
			Intent intent = new Intent();
			// 设置Intent action属性
			intent.setAction(BC_ACTION);

			intent.putExtra("pagenum", PAGENUM);
			intent.putExtra("msg", "翰林-云记事，第 " + PAGENUM + " 份内容需要您关注");
			// 实例化PendingIntent
			PendingIntent pi = PendingIntent.getBroadcast(Start.this, 0,
					intent, 0);
			// 获得系统时间
			long time = System.currentTimeMillis() + 10 * 1000;

			time = selectedDate.getTimeInMillis();
			

			// am.setRepeating(AlarmManager.RTC_WAKEUP, time,8
			// * 1000, pi);

			
			am.set(AlarmManager.RTC_WAKEUP, time, pi);

		}
	};

	@Override
	protected Dialog onCreateDialog(int id) {
		// this method is called after invoking 'showDialog' for the first time
		// here we initiate the corresponding DateSlideSelector and return the
		// dialog to its caller

		final Calendar c = Calendar.getInstance();
		switch (id) {
		case DATETIMESELECTOR_ID:
			return new DateTimeSlider(this, mDateTimeSetListener, c, c);
		}
		return null;
	}

	public static Bitmap createScaledBitmap(Bitmap src, int w, int h) {
		//RuntimeException: Canvas: trying to use a recycled bitmap android.graphics.Bitmap@2ac8bd20

		// float bitmapW = src.getWidth();
		// float bitmapH = src.getHeight();
		float wc = src.getWidth() / (float) w;
		float hc = src.getHeight() / (float) h;
		if (wc > hc)
			wc = hc;
		Log.i("BitmapScale", "Width " + (int) (src.getWidth() / wc)
				+ " height  " + (int) (src.getHeight() / wc));
		Bitmap tmp = null;
		try {
			tmp = Bitmap.createScaledBitmap(src, (int) (src.getWidth() / wc),
					(int) (src.getHeight() / wc), true);
		} catch (OutOfMemoryError e) {
			// TODO: handle exception
			Log.e("ispic", "Start createScaledBitmap OOM");
			// ERROR/dalvikvm-heap(20557): external allocation too large f
			// 捕获不到异常
		}
		// return Bitmap.createScaledBitmap(src, (int)(src.getWidth()/wc),
		// (int)(src.getHeight()/wc), true);
		return tmp;
	}

	
	
	//ly
	
		public static void rotaingImageView(int angle ) {  
	        //旋转图片 动作 
			Bitmap bitmap = BitmapFactory.decodeFile(TempImgFilePath);
	        Matrix matrix = new Matrix();;  
	        matrix.postRotate(angle);  
	        //System.out.println("angle2=" + angle);  
	        // 创建新的图片  
	        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,  
	                bitmap.getWidth(), bitmap.getHeight(), matrix, true);  
	        try
	        {
		        File myFile = new File(TempImgUpPath);
		        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(myFile));
		        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos);
		        bos.flush();
		        bos.close();
	        }catch(Exception e)
	        {
	        	e.printStackTrace();
	        }
	        //return resizedBitmap;  
	    }
	
	 /*
	  * 查看图片旋转角度 
	  */
	 public static int readPictureDegree(String path) {  
	       int degree  = 0;  
	       try {  
	               ExifInterface exifInterface = new ExifInterface(path);  
	               int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);  
	               switch (orientation) {  
	               case ExifInterface.ORIENTATION_ROTATE_90:  
	                       degree = 90;  
	                       break;  
	               case ExifInterface.ORIENTATION_ROTATE_180:  
	                       degree = 180;  
	                       break;  
	               case ExifInterface.ORIENTATION_ROTATE_270:  
	                       degree = 270;  
	                       break;  
	               }  
	       } catch (Exception e) {  
	               e.printStackTrace();  
	       }  
	       return degree;  
	   }  
	
	
	//作业上传服务器
	//private String server = "http://192.168.1.109/upload.aspx";
	private String server = Config.UPLOAD_HOMEWORK;
	/**
	 * 2013-12-23
	 * ly
	 * 用于作业批改系统，当拍照成功后上传到服务器
	 * @param file:文件名
	 */
	private void uploadImage(String file)
	{
		String end="\r\n";
		String twoHyphens ="--";
		String boundary = "******";
		
		Looper.prepare();
		
		try
		{
			URL url = new URL(server);
			HttpURLConnection httpURLConnection= (HttpURLConnection) url.openConnection();
			
			//设置传输流大小，此方法用于在预先不知道内容长度时启用没有进行内部缓冲的HTTP请求正文的流
			httpURLConnection.setChunkedStreamingMode(128*1024);
			//允许输入输出流
			httpURLConnection.setDoInput(true);
			httpURLConnection.setDoOutput(true);
			httpURLConnection.setUseCaches(false);
			
			//使用POST
			httpURLConnection.setRequestMethod("POST");
			httpURLConnection.setRequestProperty("Connection","Keep-Alive");
			httpURLConnection.setRequestProperty("Charset", "UTF-8");
			httpURLConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
			
			Date date = new Date();
	        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss"); 
			
			DataOutputStream dos = new DataOutputStream(httpURLConnection.getOutputStream());
			dos.writeBytes(twoHyphens+boundary+end);
			String str = (dateFormat.format(date))+file.substring(file.lastIndexOf(".")) ;
			dos.writeBytes("Content-Disposition: form-data; name=\"file1\"; filename=\""  
			          //+ file.substring(file.lastIndexOf("/") + 1)  
			          + (dateFormat.format(date))+file.substring(file.lastIndexOf(".")) 
					  + "\""  
			          + end);  
			dos.writeBytes(end);
			
			FileInputStream fis = new FileInputStream(file);
			byte[] buffer = new byte[8192];//8K
			int count =0;
			//读取文件
			while((count=fis.read(buffer))!=-1)
			{
				dos.write(buffer,0,count);
			}
			fis.close();
			dos.writeBytes(end);
			dos.writeBytes(twoHyphens+boundary+twoHyphens+end);
			dos.flush();
			
			InputStream is = httpURLConnection.getInputStream();
			InputStreamReader isr =new InputStreamReader(is,"utf-8");
			BufferedReader br=new BufferedReader(isr);
			String result = br.readLine();
			
			Toast.makeText(getApplicationContext(), "上传成功", Toast.LENGTH_SHORT).show();
			
			
			Log.e("!!!!",result);
			
			dos.close();
			is.close();
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
			Toast.makeText(getApplicationContext(), "上传失败", Toast.LENGTH_SHORT).show();
		}
		Looper.loop();
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		/*Toast.makeText(context, "back:" + resultCode, Toast.LENGTH_SHORT)
				.show();*/
		c.view.cursorBitmap.picFlag = false;
		Log.i("caoheng", "resultCode: " + resultCode + " requestCode: " + requestCode);
		
		
//		//添加涂鸦背景，caoheng，10.25
//		if(resultCode == -1 && requestCode == 0) {
//			Log.i("caoheng", "onActivityResult1");
//			//Uri imageFileUri = getContentResolver().insert(Media.EXTERNAL_CONTENT_URI, new ContentValues());
//			Uri imageFileUri = data.getData();
//			Log.i("uri_imagefileuri",imageFileUri.toString());
//			Log.e("addmorepic", "uri = " + imageFileUri);
//			picList.add(imageFileUri);
//			picListIndex = 0;
//
//			Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
//			//cursor.moveToFirst();
//			
//			String targetPath = "/storage/emulated/0/mypic";
//			String path;
//			Uri picUri;
//			int index;
//			String name;
//			picCursor.clear();
//			picName.clear();
//     		picList.clear();
//			
//			//caoheng 11.07， 获取在targetPath目录下的所有图片Uri
//			for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext())
//			{
//				//Log.e("addmorepic", "inside for");
//				path = cursor.getString(1);
//				path = path.substring(0, 25);
//				Log.i("uri_path",path);
//				Log.e("addmorepic", path);
//				Log.e("addmorepic","path"+cursor.getString(1));
//				Log.e("addmorepic", "1" + cursor.getString(0));
//				Log.e("addmorepic", "2"+ cursor.getString(1));
//				Log.e("addmorepic", "3" +cursor.getString(2));
//				Log.e("addmorepic", "4" +cursor.getString(3));    //////file name, e.g. 1.jpg
//				Log.e("addmorepic", "5" +cursor.getString(4));
//				Log.e("addmorepic", "6" +cursor.getString(5));
//
//				if(imageFileUri.toString().contains(path));
//				{
//					//Log.e("addmorepic", "found it, path = " + cursor.getString(1));
//					//Log.e("addmorepic", "name = " + cursor.getString(0));
//					index = cursor.getColumnIndex(Images.ImageColumns._ID);
//					index = cursor.getInt(index);
//					name = cursor.getString(3);
//					Log.e("addmorepic", "index = " + index);
//					picUri = Uri.parse("content://media/external/images/media/" + index);
//					if(picUri.equals(imageFileUri))
//					{
//						picName.add(name);
//						picCursor.add(cursor);
//					}
//				}
//			}
//			
//			for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext())
//			{
//				//Log.e("addmorepic", "inside for");
//				path = cursor.getString(1);
//				path = path.substring(0, 25);
//				//Log.e("addmorepic", path);
//                 Log.i("uri_path",path);
//				if(imageFileUri.toString().contains(path));
//				{
//					//Log.e("addmorepic", "found it, path = " + cursor.getString(1));
//					//Log.e("addmorepic", "name = " + cursor.getString(0));
//					index = cursor.getColumnIndex(Images.ImageColumns._ID);
//					index = cursor.getInt(index);
//					name = cursor.getString(3);
//					Log.e("addmorepic", "index = " + index);
//					picUri = Uri.parse("content://media/external/images/media/" + index);
//					if(!picUri.equals(imageFileUri))
//					{
//						picList.add(picUri);
//						picCursor.add(cursor);
//						picName.add(name);
//					}
//				}
//			}
//			
//			cursor.close();
		//添加涂鸦背景，caoheng，11.11
		//添加涂鸦背景，caoheng，10.25
				if(resultCode == -1 && requestCode == 0) {
					Log.i("caoheng", "onActivityResult1");
					//Uri imageFileUri = getContentResolver().insert(Media.EXTERNAL_CONTENT_URI, new ContentValues());
					Uri imageFileUri = data.getData();
					Log.e("addmorepic", "uri = " + imageFileUri);
					Cursor chooseCursor = managedQuery(imageFileUri, null, null, null, null);
					chooseCursor.moveToLast();
					String choosePath = chooseCursor.getString(1);
					String chooseName = chooseCursor.getString(3);
					int choosePathLength = choosePath.length();
					int chooseNameLength = chooseName.length();
					
					String targetPath = choosePath.substring(0, choosePathLength - chooseNameLength);
					Log.e("addmorepic", "targetPath = " + targetPath);


				    Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Images.Media.DATE_MODIFIED);
					//cursor.moveToFirst();
					
					String path;
					Uri picUri;
					int index;
					String name;
					picList.clear();
					picCursor.clear();
					picName.clear();
				
					picListIndex = 0;
					picCursorIndex = 0;
					picNameIndex = 0;
					
					
					
					//caoheng 11.11， 获取在targetPath目录下的所有图片Uri
					for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext())
					{
						//Log.e("addmorepic", "inside for");
						path = cursor.getString(1);
						path = path.substring(0, choosePathLength - chooseNameLength);
						Log.e("addmorepic", path);
						Log.e("addhomework", "1 " + cursor.getString(0));
						Log.e("addhomework", "2 "+ cursor.getString(1));
						Log.e("addhomework", "3 " +cursor.getString(2));
						Log.e("addhomework", "4 " +cursor.getString(3));    //////file name, e.g. 1.jpg
						Log.e("addhomework", "5 " +cursor.getString(4));
						Log.e("addhomework", "6 " +cursor.getString(5));

						if(path.equalsIgnoreCase(targetPath))
						{
							//Log.e("addmorepic", "found it, path = " + cursor.getString(1));
							//Log.e("addmorepic", "name = " + cursor.getString(0));
							index = cursor.getColumnIndex(Images.ImageColumns._ID);
							index = cursor.getInt(index);
							name = cursor.getString(3);
							Log.e("addmorepic", "index = " + index);
							picUri = Uri.parse("content://media/external/images/media/" + index);
							if(picUri.equals(imageFileUri))
							{
								picList.add(imageFileUri);
								picName.add(name);
								picCursor.add(cursor);
							}
						}
					}
					
					for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext())
					{
						//Log.e("addmorepic", "inside for");
						path = cursor.getString(1);
						path = path.substring(0, choosePathLength - chooseNameLength);
						//Log.e("addmorepic", path);

						if(path.equalsIgnoreCase(targetPath))
						{
							//Log.e("addmorepic", "found it, path = " + cursor.getString(1));
							//Log.e("addmorepic", "name = " + cursor.getString(0));
							index = cursor.getColumnIndex(Images.ImageColumns._ID);
							index = cursor.getInt(index);
							name = cursor.getString(3);
							Log.e("addmorepic", "index = " + index);
							picUri = Uri.parse("content://media/external/images/media/" + index);
							if(!picUri.equals(imageFileUri))
							{
								picList.add(picUri);
								picCursor.add(cursor);
								picName.add(name);
							}
						}
					}
					
					cursor.close();

//			Uri testUri;
//			for(int i=0; i<picList.size(); i++)
//			{
//				testUri = picList.get(i);
//				Log.e("addmorepic", "uri = " + testUri);
//			}
//			
			
			BitmapFactory.Options bmFactoryOptions = new BitmapFactory.Options();
			try {
				Log.i("caoheng", "change freeBitmap");
				backgroundBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageFileUri), null, bmFactoryOptions);
				Bitmap bg = Bitmap.createScaledBitmap(backgroundBitmap, 1600, 2560, true);
				c.view.freeBitmap.resetFreeBitmapList();
				c.view.freeBitmap.addBgPic(bg);
				c.view.changeStateAndSync(0);
				//c.view.freeBitmap.drawFreeBitmapSync();
				//c.view.addFreeBg(backgroundBitmap);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// 相机返回图片
		if (resultCode == RESULT_OK && requestCode == AddCameraRequest) {
			Log.i("caoheng", "1");

			//Toast.makeText(getApplicationContext(), TempImgFilePath, Toast.LENGTH_SHORT).show();
			
			
			//直接上传
			new Thread(new Runnable()
			{

				@Override
				public void run() {
					// TODO Auto-generated method stub
					//为了使用Toast，这个地方加上事件循环				

					int degree = readPictureDegree(TempImgFilePath);
					rotaingImageView(degree);
					uploadImage(TempImgUpPath);
				}
				
			}).start();
			
			
//			Uri uri = Uri.fromFile(new File(TempImgFilePath));
//			// 拷到资源文件夹
//			uri = c.view.savePicBitmapRandom(uri);
//
//			ContentResolver cr = this.getContentResolver();
//			try {
//				Bitmap bitmap;
//
//				try {
//					BitmapFactory.Options options = new BitmapFactory.Options();
//					options.inJustDecodeBounds = true;
//					bitmap = BitmapFactory.decodeStream(
//							cr.openInputStream(uri), new Rect(-1, -1, -1, -1),
//							options); // 此时返回bm为空
//					BitmapCount.getInstance().createBitmap("Start AddCameraRequest decodeStream");
//					options.inJustDecodeBounds = false;
//					// 缩放比
//					int be = 1;
//					if (options.outHeight > 300 || options.outWidth > 300) {
//						be = options.outHeight / 300;
//						int t = options.outWidth / 300;
//						if (be < t)
//							be = t;
//					}
//					options.inSampleSize = be;
//
//					bitmap = BitmapFactory.decodeStream(
//							cr.openInputStream(uri), new Rect(-1, -1, -1, -1),
//							options);
//					BitmapCount.getInstance().createBitmap("Start AddCameraRequest decodeStream");
//
//				} catch (OutOfMemoryError o) {
//
//					// TODO: handle exception
//					Log.e("addpic", "decode file failed ");
//					bitmap = Start.OOM_BITMAP;
//				}
//				Bitmap myBitmap;
//
//				if (bitmap.getWidth() < 300 && bitmap.getHeight() < 300) {
//					myBitmap = bitmap;
//				} else {
//					try {
//
//						myBitmap = createScaledBitmap(bitmap, 280, 280);
//					} catch (OutOfMemoryError o) {
//						// TODO: handle exception
//						Log.e("addpic", "scale bitmap failed ");
//						myBitmap = Start.OOM_BITMAP;
//					}
//					if(myBitmap != Start.OOM_BITMAP){
//						bitmap.recycle();
//						BitmapCount.getInstance().recycleBitmap("Start onActivityResult bitmap");
//					}
//				}
//
//				c.view.cursorBitmap.insertImageBitmap(myBitmap, uri);
//
//			} catch (FileNotFoundException e) {
//
//				Log.e("Exception", e.getMessage(), e);
//			}

			// 添加本地图片
		}else if(resultCode == 0 && requestCode == AddCameraRequest){
			Log.i("caoheng", "2");
			Log.v("renkai", "wifi");
			netStatus = WIFI;
			
			startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS)); 
			c.startTransCameraPic();
			
		}else if(resultCode == 1 && requestCode == AddCameraRequest){
			Log.i("caoheng", "3");
			Log.v("renkai", "adhoc");
			netStatus = ADHOC;
			c.startTransCameraPic();
			
		}
		
		
		else if (resultCode == RESULT_OK && requestCode == AddPictureRequest) {
			Log.i("caoheng", "4");
			Uri uri = data.getData();
			if(uri == null){
				Toast.makeText(this, "图片不存在或格式不识别", Toast.LENGTH_LONG).show();
			}else{
				Log.e("addpic", "before savePicBitmap uri::" + uri.toString() + " path:" + uri.getPath());
				// 拷到资源文件夹
				uri = c.view.savePicBitmap(uri,null);
				if(uri == null){
					Toast.makeText(this, "图片不存在或格式不识别", Toast.LENGTH_LONG).show();
				}else{
					Log.e("addpic","after save:"+ uri.toString());
					Log.e("addpic", "path" + uri.getPath());
					createAndSavePic(uri);
				}
			}
		}else if (requestCode == AddVideoRequest || requestCode == AddAudioRequest) {
			Log.i("caoheng", "5");
			if(resultCode == 0){
				netStatus = WIFI;
				Log.v("renkai", "wifi");
			}else if(resultCode == 1){
				netStatus = ADHOC;
				Log.v("renkai", "adhoc");
			}
			c.startTransCameraPic();
		}
		else if(requestCode == 10){
			c.restartGetIP();
		}else if(requestCode == Properyt.MINDMAP_REQUEST_CODE){
			Log.i("caoheng", "6");
			if(resultCode == RESULT_OK){
				Log.e("mindmap", "uri:" + data.getData().toString());
				// 拷到资源文件夹
				Uri uri = data.getData();
				uri = c.view.savePicBitmap(uri,null);
				createAndSavePic(uri);
			}else{
				Log.e("mindmap", "mind map result error");
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	

	public void chackDevice() {
		Log.e("environment", "data dir:"
				+ Environment.getDataDirectory().getAbsolutePath());

//		storagePath = Environment.getExternalStorageDirectory()
//				.getAbsolutePath();
		storagePath = "/mnt/sdcard";
		

		Log.e("environment", "root dir:"
				+ Environment.getRootDirectory().getAbsolutePath());
	}

	public static String getStoragePath() {
		return storagePath;
	}

	/**
	 * 以最省内存的方式读取本地资源的图片
	 * 
	 * @param context
	 * @param resId
	 * @return
	 */
	public static Bitmap readBitMap(Context context, int resId) {
		BitmapFactory.Options opt = new BitmapFactory.Options();
		//opt.inPreferredConfig = Bitmap.Config.ARGB_4444;
		opt.inPreferredConfig = Bitmap.Config.ARGB_4444;
		opt.inPurgeable = true;
		opt.inInputShareable = true;
		// 获取资源图片
		InputStream is = context.getResources().openRawResource(resId);
		BitmapCount.getInstance().createBitmap("Start readBitMap decodeStream");
		return BitmapFactory.decodeStream(is, null, opt);
	}

	private static String TAG = "Start";
	private IReadingsLoginService loginService;
	public static String username = "003399";// 设置默认用户名，测试没有登录readings的手机时使用
	private ServiceConnection isLoginConn = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
			Log.v(TAG, "onServiceDisconnected() called");
			loginService = null;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// TODO Auto-generated method stub
			Log.v(TAG, "onServiceConnected() called");
			loginService = IReadingsLoginService.Stub.asInterface(service);
			try {
				if (loginService.isLogin()) {
					// login
					UserInfo userInfo = loginService.getUserInfo();
					if (userInfo != null)
						username = userInfo.getUsername();
					
//					username = "jinketest";
					Log.e(TAG, "username:" + username);
					
					// usernameTextView.setText(username_sp);
				} else {
					// error
					dialog("请先登录您的Readings帐号");
					// RequestBookActivity.this.finish();
				}
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	};
	private void dialog(String title) {

        builder = new Builder(Start.this);
        builder.setMessage("您尚未登录");
        builder.setTitle(title);
        builder.setNegativeButton("离线使用", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                }
        });

        builder.setPositiveButton("登录", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                        try {
                                loginService.loginActivity();
                        } catch (RemoteException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                        }
                        
                        dialog.dismiss();
                }
        });
//        builder.setOnKeyListener(new OnKeyListener() {
//			
//			@Override
//			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
//				// TODO Auto-generated method stub
//				if(keyCode == KeyEvent.KEYCODE_BACK){
//					dialog.dismiss();
//					Start.this.finish();
//				}
//				return false;
//			}
//		});

        builder.create().show();
}
	
	
public static int createActualPixels(float f) {
	return (int) (f * density + 0.5f);
}

public static Handler backupHandler = new Handler() {
	@Override
	public void handleMessage(Message msg) {
		// TODO Auto-generated method stub

		/*
		CalligraphyVectorUtil.initParsedWordList(Start.getPageNum());
		
		for (int i = 0; i < CursorDrawBitmap.listEditableCalligraphy
				.size(); i++) {
			CursorDrawBitmap.listEditableCalligraphy.get(i)
					.initDatabaseCharList();
		}
		c.view.cursorBitmap.updateHandwriteState();
		*/
		
		
		
//		c.view.setFreeDrawBitmap();
//		Toast.makeText(Start.this, "同步服务器数据到本地，覆盖本地数据",
//				Toast.LENGTH_LONG).show();

//		bar.setVisibility(View.GONE);
//		barText.setVisibility(View.GONE);
		Log.v("flipper", "                      backupHandler updateHandwriteState!! ");
		c.view.cursorBitmap.updateHandwriteState();

	}
};

public static void createAndSavePic(Uri uri){
//		Bitmap myBitmap = BitmapUtils.getBitmapFromUri(uri);
		Bitmap myBitmap = BitmapUtils.getInstance().getBitmapFromUri(uri);
		/*
		 * 已经存到本地了，不用再存uri，只需要保存文件名
		 */
		c.view.cursorBitmap.insertImageBitmap(myBitmap, uri);

	
}


@Override
public void onLowMemory() {
	// TODO Auto-generated method stub
	super.onLowMemory();
}
//11.29caoheng gesture开启
@Override
public boolean onTouch(View arg0, MotionEvent arg1) {
	// TODO Auto-generated method stub
//	if(c.GESTURE_MODE == c.GESTURE_MODE_OFF){
//		Log.i("slide", "gesture mode off");
//		return false;
//	} else {
//		Log.i("slide", "gesture mode on");
//		//return true;
//		return mGestureDetector.onTouchEvent(arg1);	
//	}
	return false;

}
@Override
public boolean onDown(MotionEvent arg0) {
	// TODO Auto-generated method stub
	return false;
}
@Override
public boolean onFling(MotionEvent arg0, MotionEvent arg1, float arg2,
		float arg3) {
	// TODO Auto-generated method stub
	if(arg2 < 0)
	{
		Log.i("slide", "next page");
		c.view.addNextPic();
	}
	if(arg2 > 0)
	{
		Log.i("slide", "previous page");
		c.view.addPreviousPic();
	}
	return true;
	//return false;
}
@Override
public void onLongPress(MotionEvent arg0) {
	// TODO Auto-generated method stub
	
}
@Override
public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2,
		float arg3) {
	// TODO Auto-generated method stub
	return false;
}
@Override
public void onShowPress(MotionEvent arg0) {
	// TODO Auto-generated method stub
	
}
@Override
public boolean onSingleTapUp(MotionEvent arg0) {
	// TODO Auto-generated method stub
	return false;
}



}


