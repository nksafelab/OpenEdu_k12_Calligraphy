package com.jinke.calligraphy.activity;

import java.util.ArrayList;
import java.util.List;

import hallelujah.cal.CalligraphyVectorUtil;

import com.jinke.calligraphy.app.branch.Calligraph;
import com.jinke.calligraphy.app.branch.CursorDrawBitmap;
import com.jinke.calligraphy.app.branch.R;
import com.jinke.calligraphy.app.branch.Start;
import com.jinke.calligraphy.command.BackupCommand;
import com.jinke.calligraphy.command.UploadCommand;
import com.jinke.calligraphy.database.CDBPersistent;
import com.jinke.calligraphy.template.WolfTemplateUtil;
import com.jinke.kanbox.DownloadEntity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class DownloadProgressActivity extends Activity {

	public static final int UPLOAD = 1;
	public static final int DOWNLOAD = 2;
	public static final int AUTO_UPLOAD = 3;
	
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
	
	public static final int KANBOX_UPLOAD_NONEED = 20;
	
	
	private static List<DownloadEntity> downloadList = Start.getDownloadList();
	private static int MAX_REDOWNLOAD = 3;
	private static int MAX_LINES = 6;
	private static int redownload = 0;
	private static int lines = 0;
	public static TextView barText;
	public static TextView progressText;
	public TextView textdown;
	private static ProgressBar progressBar;
	private Button btn;
	private int visible = 0;
	private static Activity activity;
	protected int finished;
	
	public static Handler barTextHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {

			switch (msg.what) {
			case KANBOX_GET_FILELIST:
				lines++;
				barText.setText(barText.getText() + "\n"
						+ "获取文件列表成功，即将展示");
				break;
			case KANBOX_CHECK_TOKEN:
			case KANBOX_CHECK_AUTHOR:
			case KANBOX_START_DOWNLOAD:
			case KANBOX_UPLOAD_NONEED:
			case KANBOX_START_UPLOAD:
			
			case KANBOX_END_UPLOAD:
			case KANBOX_END_MKDIR:
			case KANBOX_START_UPLOAD_PAGE:
			case KANBOX_ERROR:
				lines++;
				if (lines > MAX_LINES) {
					barText.setText((String) msg.obj);
					lines = 0;
				} else{
					Log.e("Start", ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>find by id " + (barText == null) + "msg:" + msg.what);
					barText.setText(barText.getText() + "\n"
							+ (String) msg.obj);
					
				}
				break;
			
			case START_FINISH:

				barText.setVisibility(View.GONE);

				break;
			case KANBOX_FINISH_DOWNLOAD:
				lines = 0;
				barText.setVisibility(View.INVISIBLE);
				
				lines++;
				
				msg = new Message();
				msg.what = KANBOX_FINISH_DOWNLOAD;
				barText.setText(barText.getText() + "\n 下载已经全部完成");
				Start.barTextHandler.sendMessage(msg);
				
				break;
			case KANBOX_FINISH_UPLOAD:
				lines = 0;
//				barText.setVisibility(View.INVISIBLE);
				barText.setText(barText.getText() + "\n 上传已经全部完成");
				break;
			case KANBOX_ERROR_DOWNLOAD:
				lines++;
				barText.setText(barText.getText() + "\n"
						+ (String) msg.obj);

				for (DownloadEntity enty : downloadList) {
					barText.setText(barText.getText() + "\n"
							+ enty.getPath() + "需要重新传输");
				}

				if (redownload < MAX_REDOWNLOAD) {
					redownload++;
					lines++;
					barText.setText(barText.getText() + "\n"
							+ "重新启动失败的任务");
					new BackupCommand(Start.context, Start.backupHandler).execute();

				} else {
					lines++;
					barText.setText(barText.getText() + "\n" + "失败"
							+ MAX_REDOWNLOAD + "次，请稍后重试恢复功能");
				}

				break;
			case KANBOX_ERROR_UPLOAD:
				if (barText.getVisibility() == View.INVISIBLE)
					barText.setVisibility(View.VISIBLE);

				lines++;
				barText.setText(barText.getText() + "\n"
						+ (String) msg.obj);

				for (DownloadEntity enty : downloadList) {
					barText.setText(barText.getText() + "\n"
							+ enty.getPath() + "需要重新传输");
				}

				if (redownload < MAX_REDOWNLOAD) {
					redownload++;
					lines++;
					barText.setText(barText.getText() + "\n"
							+ "重新启动失败的任务");
					
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

				} else {
					lines++;
					barText.setText(barText.getText() + "\n" + "失败"
							+ MAX_REDOWNLOAD + "次，请稍后重试酷盘功能");
				}

				break;
			case KANBOX_START_FILELIST:
				activity.finish();
				break;
			case KANBOX_END_DBDOWNLOAD:
			case KANBOX_END_UPLOAD_PAGE:
				
				int progress = msg.arg1;
				lines++;
				if (lines > MAX_LINES) {
					barText.setText((String) msg.obj);
					lines = 0;
				} else
					barText.setText(barText.getText() + "\n"
							+ (String) msg.obj);
				progressBar.setProgress(progress);
				progressText.setText("" + progress + " %");
				
				break;
			
			default:
				break;
			}

		};
	};

	public static Handler progressDownload;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		activity = this;
		setContentView(R.layout.download_progress);
		
		Intent intent = getIntent();
		int type = intent.getIntExtra("type", 0);
		
		barText = (TextView) findViewById(R.id.barText);
		Log.e("Start", ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>find by id " + (barText == null));
		
		textdown = (TextView)findViewById(R.id.textdown);
		if(type == DOWNLOAD)
			textdown.setText("下载进度");
		else if(type == UPLOAD && type == AUTO_UPLOAD)
			textdown.setText("上传进度");
		
		progressText = (TextView)findViewById(R.id.progressText);
		
		progressBar = (ProgressBar) findViewById(R.id.downloadProgressBar);
		progressBar.setProgress(0);
		progressBar.setMax(100);
		btn = (Button) findViewById(R.id.buttonScript);
		
		
		btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				switch (visible) {
				case 0:
					btn.setText("隐藏详细信息");
					barText.setVisibility(View.VISIBLE);
					visible = 1;
					break;
				case 1:
					btn.setText("显示详细信息");
					barText.setVisibility(View.GONE);
					visible = 0;
					break;
				}
			}
		});

		
		if(type == AUTO_UPLOAD)
			Start.kanboxUploadHandler.sendEmptyMessage(0);

	}

}
