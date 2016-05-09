package com.jinke.calligraphy.app.branch;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;

import android.gesture.Prediction;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.CalendarContract.Colors;
import android.provider.MediaStore;
import android.util.Log;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jinke.adhocNDK.Jni;
import com.jinke.calligraphy.database.CDBPersistent;
import com.jinke.calligraphy.database.CalligraphyDB;
import com.jinke.calligraphy.fliplayout.FlipHorizontalLayout;
import com.jinke.calligraphy.template.Available;
import com.jinke.calligraphy.template.WolfTemplate;
import com.jinke.calligraphy.template.WolfTemplateUtil;
import com.jinke.calligraphy.touchmode.HandWriteMode;
import com.jinke.calligraphy.touchmode.SideDownMode;
import com.jinke.calligraphy.touchmode.TouchMode;
import com.jinke.mindmap.MindMapItem;
import com.jinke.mywidget.FlipImageView;
import com.jinke.mywidget.interpolator.EasingType.Type;
import com.jinke.mywidget.interpolator.ElasticInterpolator;
import com.jinke.mywidget.widget.Panel;
import com.jinke.mywidget.widget.Panel.OnPanelListener;
import com.jinke.single.BitmapCount;
import com.jinke.single.LogUtil;
import com.jinke.single.ScaleSave;

public class Calligraph extends RelativeLayout implements OnPanelListener,
		OnClickListener {

	final static int POPUP_VIEW_FACE = 1;
	final static int POPUP_VIEW_WEATHER = 2;
	private final File mStoreFile = new File(
			Environment.getExternalStorageDirectory(), "gestures");
	private static final String PHOTOSHARE = "PhotoShare";
	private static final String VIDEOSHARE = "VideoShare";
	private static final String AUDIOSHARE = "AudioShare";

	// public static final int GESTURE_MODE_ON = 1;
	// public static final int GESTURE_MODE_OFF = 0;
	// public static int GESTURE_MODE = GESTURE_MODE_OFF;

	final private String str = "com.jinke.wifiadhoc.select.wifioradhoc";

	private String shareType = PHOTOSHARE;

	private String[] savepath = null;

	private Context mContext = null;
	private WolfTemplate mTemplate;// 模板信息
	private static final String TAG = "Calligraph";

	// -------------------------------------------
	private Button expend_AlarmBtn;
	private Button expend_FaceBtn;
	private Button expend_WeatherBtn;
	private Button expend_AudioBtn;
	private Button expend_AddpicBtn;
	private Button expend_AddCameraBtn;
	private Button expend_AddVideoBtn;
	// -------------------------------------------
	static final int ALERT_MUSIC_DLG = 0;
	static final int ALERT_RECORD_DLG = 1;
	private DialogRecordListener dbrListener;
	private Dialog mRecordAlertDlg;
	private static MediaRecorder mAudioRecorder;
	private static String recorderPath;
	private static int recordAid;
	private static int recordIid;
	private static boolean isRecording;

	private void initRecord() {
		dbrListener = new DialogRecordListener();
		// 获得sdcard路径
		int recordCount = 0;
		// 设置录音文件路径
		mAudioRecorder = new MediaRecorder();
		isRecording = false;

	}

	public static GestureView gestures;
	public static float yy;// 设置toast显示位置

	// 抬头作业信息
	private LinearLayout personalInfoDisplayLayout;
	private LinearLayout pageInfoDisplayLayout;
	public LinearLayout staticInfoDisplayLayout;
	public RelativeLayout pingyuDisplayLayout;
	public LinearLayout tounaoDisplayLayout;
	public FrameLayout gesLayout;
	public RelativeLayout transParentStatisticLayout;

	public static TextView nameText;
	public static TextView pageText;
	public static TextView staticText;
	public static ImageView pingyuText;
	public static ImageView tounaoText;
	public static BorderTextView[] statisticTextView;

	public static String keshiName = "第二十二章 二次函数      ";
	public static String name = "0944 姓名：王洪亮";
	public static int pageNum = 1;
	public static int pageTotal = 22;
	public static int currentItem = 0;
	private GestureLibrary gestureLib;// 创建一个手势仓库

	// 录音到指定路径
	private static boolean startRecord(String r_path) {

		// 设置音频源
		mAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		// 设置输出格式
		mAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		// 设置编码格式
		mAudioRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		// 设置文件输出路径
		mAudioRecorder.setOutputFile(r_path);
		try {
			// 录音准备

			mAudioRecorder.prepare();
			mAudioRecorder.start();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			Log.e("record", "recordException", e);
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.e("record", "recordException", e);
			return false;
		}

		isRecording = true;
		Log.e("record", "set isRecording:" + isRecording);
		return true;
	}

	// 停止录音
	private static void stopRecording() {
		if (isRecording) {
			mAudioRecorder.stop();
			mAudioRecorder.reset();
			isRecording = false;
			Log.e("record", "set isRecording:" + isRecording);
		}

		// mAudioRecorder.release();
	}

	// 生成对话框//根据对话框类型
	protected Dialog onCreatDialog(int dlg_id) {
		return initRecordAlertDlg();
	}

	// 录音对话框事件处理
	public static void positiveDialogOnClick() {

		// TODO Auto-generated method stub
		if (isRecording) {
			my_toast("录音完成");
			stopRecording();
			Uri cameraUri = Uri.parse(recorderPath);

			Bitmap newBitmap = null;
			double dur = MediaPlayerUtil.getInstance().getDuration(cameraUri) / 1000;// s
			String duration = Math.floor(dur / 60) + "分"
					+ Math.ceil((dur / 60 - Math.floor(dur / 60)) * 60) + "秒";
			Log.e("media", "duration:" + duration);
			try {
				newBitmap = BitmapFactory.decodeResource(
						Start.context.getResources(), R.drawable.audio_stop)
						.copy(Config.ARGB_4444, true);

				BitmapCount.getInstance().createBitmap(
						"BaseBitmap decode R.drawable.audio_playing");
				BitmapCount.getInstance().createBitmap(
						"BaseBitmap decode R.drawable.audio_stop");

				Canvas canvas = new Canvas();
				canvas.setBitmap(newBitmap);
				Paint p = new Paint();
				p.setTextSize(20);
				canvas.drawText(duration, 145f, 30f, p);

			} catch (OutOfMemoryError e) {
				// TODO: handle exception
				Toast.makeText(Start.context, "内存不足，不能插入", Toast.LENGTH_SHORT)
						.show();
				return;
			}

			EditableCalligraphyItem item = Start.c.view.cursorBitmap.listEditableCalligraphy
					.get(recordAid).getCharsList().get(recordIid);
			// view.cursorBitmap.listEditableCalligraphy.get(aid).getCharsList().get(iid).resetAudioUri(newBitmap,new
			// Matrix(),cameraUri);
			item.resetAudioUri(newBitmap, new Matrix(), cameraUri);
			item.setStopBitmap();
			CalligraphyDB.getInstance(Start.context).updateAudioUri(
					Start.getPageNum(), recordAid, item.getItemID(), cameraUri,
					newBitmap);

			Start.c.view.cursorBitmap.updateHandwriteState();
			WorkQueue.getInstance().endFlipping();
		} else {

			try {
				Start.c.view.cursorBitmap.insertAudioBitmap(BitmapFactory
						.decodeResource(Start.context.getResources(),
								R.drawable.audio_unfinish), null);
				BitmapCount.getInstance().createBitmap(
						"Calligraph decode insertAudioBitmap audio_unfinish");

			} catch (OutOfMemoryError e) {
				// TODO: handle exception
				Toast.makeText(Start.context, "内存不足，不能插入", Toast.LENGTH_SHORT)
						.show();
				return;
			}

			Uri uri = Uri.parse("file:///android_asset/audio.png");// 默认等待图片
			Log.e("camera", uri.toString());

			recordAid = Start.c.view.cursorBitmap.cal_current.getID();
			recordIid = Start.c.view.cursorBitmap.cal_current.currentpos - 1;

			recorderPath = Start.getStoragePath() + "/calldir/free_"
					+ Start.getPageNum() + "/a" + recordAid + "i" + recordIid
					+ ".3gp";
			LogUtil.getInstance().e("record", "path:" + recorderPath);
			my_toast("开始录音");

			startRecord(recorderPath);
		}
	}

	// 初始化录音对话框
	private Dialog initRecordAlertDlg() {
		AlertDialog.Builder builder = new AlertDialog.Builder(Start.context);
		Log.e("record", "initRecordAlertDlg:" + isRecording);
		if (isRecording) {
			builder.setMessage("完成录音?");
		} else {
			builder.setMessage("开始录音?");
		}

		builder.setCancelable(false);
		builder.setPositiveButton("是", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				positiveDialogOnClick();
				dialog.cancel();

			}
		});
		builder.setNegativeButton("否", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});

		return builder.create();
	}

	// 按钮点击事件-----点击显示对话框
	class DialogRecordListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			// 点击显示对话框
			mRecordAlertDlg.show();
		}
	}

	// ------------------------------------------

	// private HandWriteEditLayout handwriteMenuLayout;
	private LinearLayout handwriteMenuLayout;
	private LinearLayout handwriteControlLayout;
	public static Button mPenStatusChangeBtn;
	public static Button mHandwriteBackwardBtn;
	public static Button mHandwriteForwardBtn;

	public static Button mHandwriteEndofLineBtn;

	public static Button mHandwriteInsertSpaceBtn;
	public static Button mHandwriteInsertEnSpaceBtn;

	public static Button rightBtn;
	public static Button leftBtn;
	public static Button rightDownBtn;

	public static Button mHandWriteUndoBtn;
	public static Button mHandwriteDelBtn;
	public static Button mDrawStatusChangeBtn;
	public static Button mHandwriteNewBtn;
	public static Button mMicrophoneBtn;
	public static Button mCameraBtn;
	// add download button, caoheng, 10.24
	public static Button mDownloadBtn;

	// ly
	public static Button mNextBtn;
	// end

	public static HandWriteEditLayout flipblockLayout;
	public static FlipHorizontalLayout flipblockHLayout;

	public static Button mDragEnableBtn;

	public MyView view;
	public Bitmap mBitmap; // 主图层，900*Start.SCREEN_HEIGHT
	public static Bitmap mScreenLayerBitmap;
	// 用来放大缩小的bitmap
	public static Bitmap mScaleBitmap;
	public static Bitmap mScaleTransparentBitmap;

	private String drawText;
	private String penText;

	private static final int DRAW_STATUS_BTN_ID = 1;
	private static final int PEN_STATUS_BTN_ID = 2;

	public static Button flipblockBtn;
	public static Button flipblockHBtn;

	// public static Button TestButton;
	// public static FlipImageView TestButton;

	// private String path;//使用bgPath替换，bgPath 从WolfTemplateUtil中静态获得
	private String bgPath;
	private static Paint mPaint;
	public BasePointsImpl mBaseImpl;
	private Canvas mCanvas;

	// public static List<Command> undoList;
	public static List<Command> undoList;

	public PopupWindow popupWindow;
	public View popupView;

	public Panel panel;
	public static boolean wifiandadhocPause = false;
	public boolean firstTransformPicFromMobile = true;
	public String judge = "weird";
	private long start = 0;
	private long end = 0;
	public int totalQuestion = 0;
	public static Document doc;
	public static String pagecXML = "demo.xml";
	public static File file = new File("/sdcard/" + pagecXML);
	
	
	//2016.4.11评语句子
	public static String[] commentString={"再深入了解下函数二次项系数性质，你会做得更好",
		"作业整体非常好，对一般式要再深入了解下","知识把握很好，要特别注意等号两边都是整式","计算中移项要变号哦，相信你下次能做的更好",
		"对概念理解不深，二次项系数可不能等0啊","移项的过程中注意变号，相信你可以做的更好"};
	public static String[] commentStringReplacement1 = {"对角线概念要清晰，再碰到类似题目可以多画图","计算要认真点，题目准确率会更高","要热爱学习，再复习下函数的概念",
		"审题过程要细致，下次肯定能做的更好","可以试试带入法，思维要开阔","计算结果要化简，学习要认真起来"};
	public static String[] commentStringReplacement2 = {"试试先化简怎么样，求判别式之前先化成最简式","方程两根之积与c/a符号有关，掌握根与系数的关系",
			"可以假设两根为未知数列方程，对知识的把握要再灵活一些","整体思路很对，注意用球根公式的前提是判别式大于零",
			"解题结束后要检验根的有效性，掌握相关概念非常有用","作业做的非常好，运算中平方根有正负两个"};
    public static String[] str1 = {"审题要认真","掌握清楚知识点","学习态度要端正","做题要仔细","继续努力","有待改进"};//页评

	static {
		try {
			System.loadLibrary("adhocNDK");
			System.loadLibrary("cal_parser");// 装载jni库
			System.loadLibrary("pdc_prs"); // 装载jni库
		} catch (UnsatisfiedLinkError e) {
			e.printStackTrace();
		}
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public Calligraph(Context context) {
		super(context);
		mContext = context;

		onCreate(context);

		// 2016.3.17 gestures caoheng

		//
		// LayoutInflater inflater1 = (LayoutInflater)
		// Start.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		// View gview = inflater1.inflate(R.layout.gestures, null);
		gesLayout = new FrameLayout(context);
		LayoutParams gLp = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		LayoutParams geLp = new LayoutParams(1200, 2200);

		// 加上就不行 gestures.setFadeEnabled(false);
		gestures = new GestureView(context);
		gestures.setFadeOffset(2000); // 多笔画每两次的间隔时间
		gestures.setGestureColor(Color.RED);// 画笔颜色
		gestures.setUncertainGestureColor(Color.RED);//未完成颜色
		// gestures.setBackgroundColor(Color.CYAN);
		gestures.setGestureStrokeWidth(1);// 画笔粗细值
		gestures.setGestureStrokeType(GestureView.GESTURE_STROKE_TYPE_MULTIPLE);

		this.addView(gestures, geLp);
		// this.addView(gesLayout,gLp);
		// gesLayout.bringToFront();
		// gestures.bringToFront();
		// gestures.setVisibility(View.GONE);
		gestures.invalidate();
		view.postInvalidate();

	
		// FileOutputStream fos=null;

		Log.i("pageXML", "pageC:" + pagecXML);
		try {
			doc = Jsoup.parse(file, "UTF-8");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.i("pageXML", "" + view.pageXML);
		Elements element = doc.getElementsByTag("ystart");
		final Elements elementResult = doc.getElementsByTag("result");
		final Elements elementComment =doc.getElementsByTag("comment");
		totalQuestion = element.size();
		// Log.i("totalquestion",""+element);
		final int[] pos = new int[totalQuestion];
		final Question question[] = new Question[totalQuestion];
		for (int i = 0; i < totalQuestion; i++) {
			question[i] = new Question();
			// list.add(question[i]);

		}
		for (int i = 0; i < totalQuestion; i++) {

			pos[i] = Integer.valueOf(element.get(i).text().toString());
			Log.i("totalquestion", "" + totalQuestion + "\n");
		}

		// 2016.3.30统计层 caoheng
		transParentStatisticLayout = new RelativeLayout(context);
		LayoutParams tpsLp = new LayoutParams(1600, 2500);

		// transParentStatisticLayout.setBackgroundColor(Color.BLUE);
		statisticTextView = new BorderTextView[totalQuestion];
		for (int i = 0; i < totalQuestion; i++) {
			statisticTextView[i] = new BorderTextView(context);
			LayoutParams stvLp = new LayoutParams(400, 100);
			// nameLp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			stvLp.setMargins(1200,
					Integer.valueOf(element.get(i).text().toString()), 1500,
					2700 - Integer.valueOf(element.get(i).text().toString()));
			// stvLp.topMargin =
			// Integer.valueOf(element.get(i).text().toString());
			statisticTextView[i].setGravity(Gravity.CENTER_VERTICAL);
			statisticTextView[i].setVisibility(View.VISIBLE);
			// 2016.4.1 统计条效果
			// if(i%2==0)statisticTextView[i].setBackgroundColor(Color.RED);
			// if(i==5)statisticTextView[i].setTextColor(Color.BLUE);
			// statisticTextView[i].setText(String.valueOf(i)+"");
			// statisticTextView[i].setTextSize(i+20);
			transParentStatisticLayout.addView(statisticTextView[i], stvLp);
		}
		this.addView(transParentStatisticLayout, tpsLp);

		// gestures.setGestureStrokeType(GestureView.GESTURE_STROKE_TYPE_SINGLE);
		gestures.setUncertainGestureColor(Color.RED);

		gestures.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				yy = gestures.y;
				return view.onTouchEvent(event);

			}
		});

		// 手势识别的监听器
		gestures.addOnGestureListener(new GestureView.OnGestureListener() {

			// 2016.4.12解决不同笔画数手势问题
			int lastStrokeCount;
			public GestureOverlayView overlay;
			public MotionEvent event;
			Handler handler = new Handler();

			Runnable runnable = new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					if (event.getAction() == MotionEvent.ACTION_UP&&(overlay.getGesture().getLength()>10)) {
						handler.postDelayed(this, 1000);
						lastStrokeCount = overlay.getGesture()
								.getStrokesCount();

						// Log.i("strokeevent",
						// String.valueOf(lastStrokeCount));
						switch (lastStrokeCount) {
						case 1:

							ArrayList<Prediction> predictionswr = gestureLib
									.recognize(overlay.getGesture());
							if (predictionswr.size() > 0) {
								Prediction prediction = (Prediction) predictionswr
										.get(0);
								if (prediction.score > 5) {
									judge = prediction.name;
									Toast.makeText(mContext, prediction.name,
											Toast.LENGTH_SHORT).show();
								}

								else
								{
									judge ="ignore";
									Toast.makeText(mContext, "写字",
											Toast.LENGTH_SHORT).show();
								}
								
							}
							break;
						case 2:

							ArrayList<Prediction> predictions = gestureLib
									.recognize(overlay.getGesture());
							if (predictions.size() > 0) {
								Prediction prediction1 = (Prediction) predictions
										.get(0);
								if (prediction1.score > 4) {
									judge = prediction1.name;
									Toast.makeText(mContext, prediction1.name,
											Toast.LENGTH_SHORT).show();
								}

								else
									judge ="ignore";
								Toast.makeText(mContext, "写字",
										Toast.LENGTH_SHORT).show();
							}
//							Toast.makeText(mContext, "错", Toast.LENGTH_SHORT)
//									.show();
							break;
						case 3:
							judge = "错3";
							Toast.makeText(mContext, "啊", Toast.LENGTH_SHORT)
									.show();
							break;
						case 4:
							judge = "错4";
							Toast.makeText(mContext, "4笔", Toast.LENGTH_SHORT)
									.show();
							break;
						default:
							lastStrokeCount = 0;
						}
						int situation = 4;

						if (judge.equals("Right"))
							situation = 0;
						else if (judge.equals("wrong"))
							situation = 1;
						else if (judge.equals("Almost"))
							situation = 2;

						for (currentItem = 1; currentItem < totalQuestion; currentItem++) {
                            if(currentItem==4){
                            	System.arraycopy(commentStringReplacement1, 0,commentString , 0, 6);
                            	Log.i("whichcomment",""+SideDownMode.whichComment);
        
                            		
                            
                            	}
                            	
                            
                            
                            else System.arraycopy(commentStringReplacement2, 0,commentString , 0, 6);
                            
							if ((yy > pos[currentItem - 1]) && (yy < pos[currentItem])) {
								Log.i("prediction", "i" + currentItem + "+" + pos[currentItem]);
								switch (situation) {
								case 0:
									question[currentItem-1].right++;
									elementResult.get(currentItem).text("对");

									break;
								case 1:
									question[currentItem-1].wrong++;
									elementResult.get(currentItem-1).text("错");
									break;
								case 2:
									question[currentItem-1].weird++;
									elementResult.get(currentItem-1).text("有问题");
									break;
								default:
								//	question[i].weird++;
									break;

								}

								Log.i("prediction", "" + situation + " "
										+ question[7].right + "  "
										+ question[7].wrong);
								Log.i("prediction", "i" + currentItem + "+" + pos[currentItem]);
								// my_toast("对"
								// + String.valueOf(question[i].right)
								// + "错"
								// + String.valueOf(question[i].wrong)
								// + "疑问"
								// + String.valueOf(question[i].weird));
								statisticTextView[currentItem-1].setVisibility(View.VISIBLE);
								statisticTextView[currentItem-1].setText(" 本题统计:" + " 对:"
										+ String.valueOf(question[currentItem-1].right)
										+ " 错:"
										+ String.valueOf(question[currentItem-1].wrong)
										+ " 疑问:"
										+ String.valueOf(question[currentItem-1].weird));
								statisticTextView[currentItem - 1]
										.setBackgroundColor(Color.argb(75, 99,
												99, 99));
								
								statisticTextView[currentItem - 1].setTextSize(16);
								statisticTextView[currentItem - 1]
										.setTextColor(Color.BLACK);
								statisticTextView[currentItem - 1].setBackgroundResource(R.drawable.corner_textview);
								break;
							} else {
								// my_toast("不在判定区域");
								continue;

							}

							// my_toast("对"+String.valueOf(question[i].right));
						}

						handler.removeCallbacks(runnable);
					}

				}

			};

			@Override
			public void onGesture(GestureOverlayView arg0, MotionEvent arg1) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onGestureCancelled(GestureOverlayView arg0,
					MotionEvent arg1) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onGestureEnded(GestureOverlayView overlay,
					MotionEvent event) {
				// TODO Auto-generated method stub
				yy = gestures.y;

				this.overlay = overlay;
				this.event = event;
				Log.i("prediction", "y=" + yy);
				handler.postDelayed(runnable, 1500);
				// ArrayList<Prediction> predictions = gestureLib
				// .recognize(overlay.getGesture());
				// if (predictions.size() > 0) {
				// Prediction prediction = (Prediction) predictions.get(0);
				// Log.i("prediction", "" + prediction.score);
				// // 匹配的手势
				// if (prediction.score > 1.0) { // 越匹配score的值越大，最大为10
				// Toast.makeText(mContext, prediction.name,
				// Toast.LENGTH_SHORT).show();
				// // my_toast(String.valueOf(yy));
				// judge = prediction.name;
				// int situation = 0;
				// // Log.i("prediction",""+judge);
				// if (judge.equals("Right"))
				// situation = 0;
				// else if (judge.equals("wrong"))
				// situation = 1;
				// else
				// situation = 2;
				// // Log.i("prediction","situation+"+situation);
				// // Log.i("prediction","yy+"+yy);
				// // Log.i("prediction","right="+question[0].right);
				//
				// for (int i = 1; i < totalQuestion; i++) {
				//
				// if ((yy > pos[i - 1]) && (yy < pos[i])) {
				// Log.i("prediction", "i" + i + "+" + pos[i]);
				// switch (situation) {
				// case 0:
				// question[i].right++;
				// elementResult.get(i).text("对");
				//
				// break;
				// case 1:
				// question[i].wrong++;
				// elementResult.get(i).text("错");
				// break;
				// case 2:
				// question[i].weird++;
				// elementResult.get(i).text("有问题");
				// break;
				// default:
				// question[i].weird++;
				// break;
				//
				// }
				//
				// Log.i("prediction", "" + situation + " "
				// + question[7].right + "  "
				// + question[7].wrong);
				// Log.i("prediction", "i" + i + "+" + pos[i]);
				// // my_toast("对"
				// // + String.valueOf(question[i].right)
				// // + "错"
				// // + String.valueOf(question[i].wrong)
				// // + "疑问"
				// // + String.valueOf(question[i].weird));
				// statisticTextView[i - 1].setText("本题统计" + "对"
				// + String.valueOf(question[i].right)
				// + "错"
				// + String.valueOf(question[i].wrong)
				// + "疑问"
				// + String.valueOf(question[i].weird));
				// statisticTextView[i - 1]
				// .setBackgroundColor(Color.argb(55, 0,
				// 255, 0));
				// statisticTextView[i - 1].setTextSize(20);
				// statisticTextView[i - 1]
				// .setTextColor(Color.BLACK);
				// break;
				// } else {
				// // my_toast("不在判定区域");
				// continue;
				//
				// }
				//
				// // my_toast("对"+String.valueOf(question[i].right));
				// }
				//
				// }
				// }

			}

			@Override
			public void onGestureStarted(GestureOverlayView overlay,
					MotionEvent event) {
				// TODO Auto-generated method stub

			}

		});

		if (gestureLib == null) {
			gestureLib = GestureLibraries.fromFile(mStoreFile);
			gestureLib.load();
		}

		// 2016.3.20caoheng 统计

		// Log.i("pos",""+pos[3]);
		// List list = new ArrayList();

		// Log.i("prediction",""+judge);

		// try {
		// if (!file.exists()) {//文件不存在则创建
		// file.createNewFile();
		// }
		// fos=new FileOutputStream(file,false);
		//
		// fos.write(doc.html().getBytes());//写入文件内容
		// fos.flush();
		// } catch (IOException e) {
		// System.err.println("文件创建失败");
		// }finally{
		// if (fos!=null) {
		// try {
		// fos.close();
		// } catch (IOException e) {
		// System.err.println("文件流关闭失败");
		// }
		// }
		// }

		personalInfoDisplayLayout = new LinearLayout(context);
		nameText = new TextView(context);
		LayoutParams nameLp = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		// nameLp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		nameLp.leftMargin = 300;
		nameLp.rightMargin = 200;
		nameLp.topMargin = 50;

		nameText.setText("学科：数学  " + " 章节：" + keshiName + "\n学号：" + name);
		nameText.setTextSize(23);
		Log.i("addText", "add text name");
		nameText.setGravity(Gravity.LEFT);
		nameText.setTextColor(Color.BLUE);
		nameText.setBackgroundColor(Color.alpha(Color.GRAY));
		nameText.bringToFront();
		nameText.setVisibility(View.VISIBLE);
		personalInfoDisplayLayout.addView(nameText, nameLp);
		this.addView(personalInfoDisplayLayout, nameLp);

		nameText.invalidate();
		view.postInvalidate();

		pageInfoDisplayLayout = new LinearLayout(context);
		pageText = new TextView(context);
		LayoutParams pageLp = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		pageLp.setMargins(1200, 2320, 0, 100);

		pageText.setText("" + pageNum + " / " + pageTotal);
		pageNum++;
		pageText.setTextSize(20);
		pageText.setGravity(Gravity.CENTER_VERTICAL);
		pageText.setTextColor(Color.BLACK);
		pageText.setBackgroundColor(Color.TRANSPARENT);
		pageText.bringToFront();
		pageText.setVisibility(View.VISIBLE);
		pageInfoDisplayLayout.addView(pageText, pageLp);
		this.addView(pageInfoDisplayLayout, pageLp);

		pageText.invalidate();
		view.postInvalidate();

		staticInfoDisplayLayout = new LinearLayout(context);
		staticText = new TextView(context);
		LayoutParams staticLp = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		staticLp.setMargins(400, 1506, 400, 1500);

		staticText.setTextSize(20);
		staticText.setGravity(Gravity.CENTER_VERTICAL);

		staticText.setTextColor(Color.BLACK);
		staticText.setBackgroundColor(Color.TRANSPARENT);
		staticText.bringToFront();
		staticText.setVisibility(View.GONE);
		staticInfoDisplayLayout.addView(staticText, staticLp);
		this.addView(staticInfoDisplayLayout, staticLp);

		staticText.invalidate();
		view.postInvalidate();
		
		
		
		
		
		
		
		
		
		

		pingyuDisplayLayout = new RelativeLayout(context);
		pingyuText = new DragImageView(context, null);
		LayoutParams pDLp = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		

		Resources res = getResources();
		
		Bitmap bmp = BitmapFactory.decodeResource(res, R.drawable.ziti);
      
		pingyuText.setImageBitmap(bmp);
   //     pingyuText.setOnTouchListener()
      
		pingyuText.setVisibility(View.GONE);
		LayoutParams pingyuLp = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
		pingyuLp.setMargins(10, 900, 200, 1200);
	//	pingyuDisplayLayout.addView(pingyuText);
	//	pingyuDisplayLayout.setBackgroundColor(Color.RED);
	//	pingyuText.setBackgroundColor(Color.BLUE);
		pingyuDisplayLayout.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View view, MotionEvent event) {
				// TODO Auto-generated method stub
				return view.onTouchEvent(event);
			}
		});
		
		   pingyuDisplayLayout.addView(pingyuText,pingyuLp);
		this.addView(pingyuDisplayLayout, pDLp);
	
     
		pingyuText.invalidate();
		view.postInvalidate();

		
		
		
		
		
		
		
		
		
		
		
		
		
		
		tounaoDisplayLayout = new LinearLayout(context);
		tounaoText = new DragImageView(context, null);
		LayoutParams tounaoLp = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
		// tounaoLp.topMargin = 100;
		// tounaoLp.leftMargin = 1000;
		Resources res1 = getResources();
		Bitmap bmp1 = BitmapFactory.decodeResource(res1, R.drawable.tounao);

		tounaoText.setImageBitmap(bmp1);

		tounaoLp.setMargins(1000, 1000, 800, 200);
		// pingyuText.bringToFront();
		tounaoText.setVisibility(View.INVISIBLE);
		tounaoDisplayLayout.addView(tounaoText, tounaoLp);
		this.addView(tounaoDisplayLayout, tounaoLp);

		tounaoText.invalidate();
		view.postInvalidate();

		view.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				// TODO Auto-generated method stub
				Log.v("long", "long click!!!!!!!!!");
				return false;
			}
		});
		view.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Log.v("long", "click!!!!!!!!!");
			}
		});
	}

	public static void setNameText(String chapter, String name) {
		if (chapter.equals("0021")) {

			keshiName = "第二十一章 一元一次方程";
		} else if (chapter.equals("0022")) {
			keshiName = "第二十二章 二次函数";
		} else if (chapter.equals("0023")) {
			keshiName = "第二十三章 旋转";
		}

		if (name.equals("0944")) {
			name = "0944 姓名： 王洪亮";
		} else if (name.equals("0945")) {
			name = "0945 姓名： 陈程";
		} else if (name.equals("0946")) {
			name = "0946 姓名： 李亚芳";
		}
		nameText.setText("学科：数学  " + " 章节：" + keshiName + "\n" + "学号：" + name);
	}

	public static void setPageXML(String str) {
		pagecXML = str;
		Log.i("pageXML", "pageC:" + pagecXML);

	}

	public static void setPageText() {
		pageText.setText("" + pageNum + " / " + pageTotal);
		pageNum++;
	}

	public void destroy() {
		// mindmapEditFinish();
		mScreenLayerBitmap.recycle();
		BitmapCount.getInstance().recycleBitmap(
				"Calligraph destroy mScreenLayerBitmap");

		mBitmap.recycle();
		BitmapCount.getInstance().recycleBitmap("Calligraph destroy mBitmap");

		mScaleBitmap.recycle();
		BitmapCount.getInstance().recycleBitmap(
				"Calligraph destroy mScaleBitmap");

		view.destroy();
	}

	public Handler flipHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			Log.v("flipper",
					"                      flipHandler updateHandwriteState!! ");
			if (msg.what == FlipImageView.FLIP_WHAT)
				view.cursorBitmap.updateHandwriteState();
			else
				view.cursorBitmap.updateHandwriteState();

			if (msg.what == 0) {
				// 任务队列,缩放动作，执行结束，释放屏幕外资源
				view.cursorBitmap.recycleOutScreenBitmap();
			}

			if (msg.what == FlipImageView.FLIP_UP_WHAT)
				view.cursorBitmap.updateHandwriteStateFlip();
			/*
			 * add by mouse
			 * 每次移动时，同时清空(硬笔时为mPath.reset(),毛笔时为bPointsList.clear())
			 */
			view.baseImpl.clear();
			/*
			 * add by mouse 每次滑动时，将透明层上的内容分别更新在不同的bitmap中
			 */
			// view.cursorBitmap.updateTransparent();
		}
	};

	Handler flipHandler2 = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			Canvas canvas = new Canvas();
			canvas.setBitmap(mBitmap);

			canvas.drawBitmap(view.cursorBitmap.bitmap, Start.SCREEN_WIDTH, 0,
					new Paint());// 不能住掉 有点用 删除时重绘底图
		}
	};

	Handler flip_Horizonal_Handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			Log.v("flipper",
					"                      flip_Horizonal_Handler updateHandwriteState!! ");
			view.cursorBitmap.updateHandwriteState();

			view.invalidate();
			System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%% flipHandler t:"
					+ msg.what);

		}
	};

	Handler upHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);

			flipblockLayout.setVisibility(View.VISIBLE);
			flipblockLayout.setLayout(flipblockLayout.temp);
		}
	};

	protected void onCreate(Context context) {
		initRecord();

		// ly
		// TestButton = new FlipImageView(Start.context,flipHandler);
		// TestButton.setBackgroundResource(R.drawable.flipblock_vertical);

		flipblockBtn = new Button(context);
		flipblockBtn.setVisibility(View.GONE);
		flipblockLayout = new HandWriteEditLayout(context, flipHandler);

		flipblockHBtn = new Button(context);
		flipblockHBtn.setVisibility(View.GONE);
		flipblockHLayout = new FlipHorizontalLayout(context,
				flip_Horizonal_Handler);

		mTemplate = WolfTemplateUtil.getCurrentTemplate();

		// System.out.println("!!!!!!!!!!!name:"+ mTemplate.getName());
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setDither(true);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeJoin(Paint.Join.ROUND);
		mPaint.setStrokeCap(Paint.Cap.ROUND);
		mPaint.setStrokeWidth(7);
		mPaint.setColor(Color.RED);

		mCanvas = new Canvas();

		if (mBitmap == null) {
			// mBitmap = Bitmap.createBitmap(Start.SCREEN_WIDTH * 2,
			// Start.SCREEN_HEIGHT, Bitmap.Config.ARGB_8888);
			// ly
			// 这里为什么要乘以2呢？
			// mBitmap = Bitmap.createBitmap(Start.SCREEN_WIDTH * 2,
			// Start.SCREEN_HEIGHT, Bitmap.Config.ARGB_4444);
			mBitmap = Bitmap.createBitmap(Start.SCREEN_WIDTH * 2,
					Start.SCREEN_HEIGHT, Bitmap.Config.ARGB_4444);
			BitmapCount.getInstance().createBitmap("Calligraph Create mBitmap");
		}

		if (mScaleBitmap == null) {
			mScaleBitmap = Bitmap.createBitmap(Start.SCREEN_WIDTH * 2,
					Start.SCREEN_HEIGHT * 2, Bitmap.Config.ARGB_4444);
			BitmapCount.getInstance().createBitmap(
					"Calligraph Create mScaleBitmap");
		}

		if (mScaleTransparentBitmap == null) {
			mScaleTransparentBitmap = Bitmap.createBitmap(
					Start.SCREEN_WIDTH * 2, Start.SCREEN_HEIGHT * 2,
					Bitmap.Config.ARGB_4444);
			BitmapCount.getInstance().createBitmap(
					"Calligraph Create mScaleTransparentBitmap");
		}

		final Canvas canvas = new Canvas();
		canvas.setBitmap(mBitmap);

		if (mScreenLayerBitmap == null) {
			mScreenLayerBitmap = Bitmap.createBitmap(Start.SCREEN_WIDTH,
					Start.SCREEN_HEIGHT, Bitmap.Config.ARGB_4444);
			BitmapCount.getInstance().createBitmap(
					"Calligraph Create mScreenLayerBitmap");
		}
		// path = WolfTemplateUtil.TEMPLATE_PATH +
		// mTemplate.getName()+"/"+mTemplate.getBackground();
		bgPath = WolfTemplateUtil.getTemplateBgPath();

		// ly
		// test
		// 注释掉，涂鸦态不加模板
		// if(mTemplate.getFormat() == MyView.STATUS_DRAW_FREE){
		//
		// System.out.println("picPath```````````````" + bgPath);
		//
		// Bitmap bgBitmap = null;
		// try {
		// bgBitmap =
		// BitmapFactory.decodeFile(bgPath).copy(Bitmap.Config.ARGB_4444, true);
		// BitmapCount.getInstance().createBitmap("Calligraph decode bgBitmap");
		//
		// canvas.drawBitmap(bgBitmap, 0, 0, mPaint);
		// bgBitmap.recycle();
		// BitmapCount.getInstance().recycleBitmap("Calligraph onCreate bgBitmap");
		// } catch (OutOfMemoryError e) {
		// // TODO: handle exception
		// Log.e("AndroidRuntime", "Calligraph OnCreate OOM!!!");
		// }
		//
		//
		// }

		// ly
		if (mTemplate.getFormat() == MyView.STATUS_DRAW_CURSOR) {
			Bitmap mHandBgBitmap = null;

			try {
				mHandBgBitmap = BitmapFactory.decodeFile(bgPath).copy(
						Bitmap.Config.ARGB_4444, true);
				BitmapCount.getInstance().createBitmap(
						"Calligraph decode mHandBgBitmap");

				canvas.drawBitmap(mHandBgBitmap, Start.SCREEN_WIDTH, 0, mPaint);

				mHandBgBitmap.recycle();
				BitmapCount.getInstance().recycleBitmap(
						"Calligraph onCreate mHandBgBitmap");
			} catch (OutOfMemoryError e) {
				// TODO: handle exception
				Log.e("AndroidRuntime", "Calligraph OnCreate() 2 OOM!!!");
			}
		}

		// mScalBitmap:w*2,h*2,底图改成mScaleBitmap了
		// 之后又把原始的mBitmap画到了mScaleBitmap上
		// canvas.setBitmap(mScaleBitmap);
		// canvas.drawBitmap(mBitmap, new Rect(0, 0, Start.SCREEN_WIDTH,
		// Start.SCREEN_HEIGHT),
		// new Rect(0, 0, Start.SCREEN_WIDTH, Start.SCREEN_HEIGHT), new
		// Paint());

		view = new MyView(context, mBitmap, mScreenLayerBitmap, mTemplate);
		this.addView(view);

		undoList = new LinkedList<Command>();

		mBaseImpl = view.baseImpl;
		LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
		mPenStatusChangeBtn = new Button(context);
		mPenStatusChangeBtn.setId(PEN_STATUS_BTN_ID);
		mPenStatusChangeBtn.setOnClickListener(new PenStatusBtnListener());

		// ---------------------------------------------------------

		LayoutParams jump_lp = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		jump_lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		jump_lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);

		LayoutInflater inflater = (LayoutInflater) Start.context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View mview = inflater.inflate(R.layout.panel_main, null);
		panel = (Panel) mview.findViewById(R.id.bottomPanel);
		panel.setId(111);
		panel.setOnPanelListener(this);
		panel.setInterpolator(new ElasticInterpolator(Type.OUT, 1.0f, 0.3f));

		expend_AlarmBtn = (Button) mview.findViewById(R.id.expend_alarmBtn);
		expend_AudioBtn = (Button) mview.findViewById(R.id.expend_audioBtn);
		expend_FaceBtn = (Button) mview.findViewById(R.id.expend_faceBtn);
		expend_WeatherBtn = (Button) mview.findViewById(R.id.expend_weatherBtn);
		expend_AddpicBtn = (Button) mview.findViewById(R.id.expend_addPicBtn);
		expend_AddCameraBtn = (Button) mview
				.findViewById(R.id.expend_cameraBtn);
		expend_AddVideoBtn = (Button) mview.findViewById(R.id.expend_videoBtn);

		expend_AlarmBtn.setOnClickListener(this);
		expend_AudioBtn.setOnClickListener(this);
		expend_FaceBtn.setOnClickListener(this);
		expend_WeatherBtn.setOnClickListener(this);
		expend_AddpicBtn.setOnClickListener(this);
		expend_AddCameraBtn.setOnClickListener(this);
		expend_AddVideoBtn.setOnClickListener(this);
		this.addView(panel, jump_lp);

		// ---------------------------------------------------------

		// handwriteMenuLayout = new HandWriteEditLayout(context);
		handwriteMenuLayout = new LinearLayout(context);
		LayoutParams handwriteMenuLp = new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

		handwriteMenuLp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		// handwriteMenuLp.addRule(RelativeLayout.ALIGN_PARENT_LEFT,
		// RelativeLayout.TRUE);
		handwriteMenuLp.addRule(RelativeLayout.RIGHT_OF, 111);

		HandwriteBtnListener handwriteListener = new HandwriteBtnListener();

		LayoutParams lp_edit = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);

		// mHandwriteBackwardBtn = new Button(context);
		// mHandwriteBackwardBtn.setText("后退");
		// mHandwriteBackwardBtn.setOnClickListener(handwriteListener);
		// mHandwriteBackwardBtn.setVisibility(View.GONE);
		// handwriteMenuLayout.addView(mHandwriteBackwardBtn,lp_edit);
		//
		// mHandwriteForwardBtn = new Button(context);
		// mHandwriteForwardBtn.setText("前进");
		// mHandwriteForwardBtn.setVisibility(View.GONE);
		// mHandwriteForwardBtn.setOnClickListener(handwriteListener);
		// handwriteMenuLayout.addView(mHandwriteForwardBtn,lp_edit);

		mHandwriteEndofLineBtn = new Button(context);
		// mHandwriteEndofLineBtn.setText("换行");
		mHandwriteEndofLineBtn.setOnClickListener(handwriteListener);
		mHandwriteEndofLineBtn.setBackgroundResource(R.drawable.enter);
		handwriteMenuLayout.addView(mHandwriteEndofLineBtn, lp_edit);

		mHandwriteInsertSpaceBtn = new Button(context);
		// mHandwriteInsertSpaceBtn.setText("空格");
		mHandwriteInsertSpaceBtn.setOnClickListener(handwriteListener);
		mHandwriteInsertSpaceBtn.setBackgroundResource(R.drawable.space);
		handwriteMenuLayout.addView(mHandwriteInsertSpaceBtn, lp_edit);

		mHandwriteInsertEnSpaceBtn = new Button(context);
		// mHandwriteInsertSpaceBtn.setText("空格");
		mHandwriteInsertEnSpaceBtn.setOnClickListener(handwriteListener);
		mHandwriteInsertEnSpaceBtn.setBackgroundResource(R.drawable.enspace);
		handwriteMenuLayout.addView(mHandwriteInsertEnSpaceBtn, lp_edit);

		mHandWriteUndoBtn = new Button(context);
		// mUndoBtn.setText("撤销");
		mHandWriteUndoBtn.setOnClickListener(handwriteListener);
		mHandWriteUndoBtn.setBackgroundResource(R.drawable.undo);
		handwriteMenuLayout.addView(mHandWriteUndoBtn, lp_edit);

		mHandwriteDelBtn = new Button(context);
		// mHandwriteDelBtn.setText("删除");
		mHandwriteDelBtn.setOnClickListener(handwriteListener);
		mHandwriteDelBtn.setBackgroundResource(R.drawable.backspace);
		handwriteMenuLayout.addView(mHandwriteDelBtn, lp_edit);

		// ly
		// 下一张作业的图片
		mNextBtn = new Button(context);
		mNextBtn.setClickable(false);
		mNextBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// ly
				// 此处为点击获取下一页图片
				Start.bar.setVisibility(View.VISIBLE);
				Start.barText.setVisibility(View.VISIBLE);
				mNextBtn.setClickable(false);

				// mBitmap.eraseColor(Color.WHITE);

				new Thread(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						view.getNextHomework();
					}

				}).start();

				// end
			}
		});
		mNextBtn.setBackgroundResource(R.drawable.next);
		mNextBtn.setVisibility(View.INVISIBLE);
		handwriteMenuLayout.addView(mNextBtn, lp_edit);
		// end

		// mDragEnableBtn = new Button(context);
		// mDragEnableBtn.setText("画笔模式");
		// mDragEnableBtn.setVisibility(View.GONE);
		// mDragEnableBtn.setOnClickListener(handwriteListener);
		// handwriteMenuLayout.addView(mDragEnableBtn);

		this.addView(handwriteMenuLayout, handwriteMenuLp);

		LayoutParams rightLp = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		rightLp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		rightLp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		rightBtn = new Button(Start.context);
		rightBtn.setBackgroundResource(R.drawable.rightpagedown2);
		rightBtn.setId(1);
		rightBtn.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				view.touchMode = view.sideDownMode;
				// Log.i("bbb","x:"+ event.getX() +" y:"+ event.getY());
				// Log.i("bbb","rawx:"+ event.getRawX() +" rawy:"+
				// event.getRawY());
				// event.setLocation(event.getX()+ 560, event.getY());
				// ly
				// 这个是右上角下滑按钮
				event.setLocation(event.getRawX() - 300, event.getRawY() - 150);
				return view.onTouchEvent(event);
			}
		});
		this.addView(rightBtn, rightLp);

		LayoutParams buttonlp = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		buttonlp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		buttonlp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		buttonlp.topMargin = FlipImageView.TOP_LIMIT;

		// this.addView(TestButton, buttonlp);

		LayoutParams leftLp = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		leftLp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		leftLp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		leftBtn = new Button(Start.context);
		leftBtn.setBackgroundResource(R.drawable.leftpagedown2);
		leftBtn.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				view.touchMode = view.sideDownMode;
				// Log.i("bbb","x:"+ event.getX() +" y:"+ event.getY());
				// Log.i("bbb","rawx:"+ event.getRawX() +" rawy:"+
				// event.getRawY());
				event.setLocation(event.getX() - 40, event.getY());
				return view.onTouchEvent(event);
			}
		});
		this.addView(leftBtn, leftLp);

		// -------------------------添加竖向滑块
		LayoutParams flipblockMenuLp = new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		flipblockBtn.setBackgroundResource(R.drawable.flipblock_vertical);

		flipblockMenuLp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		flipblockLayout.addView(flipblockBtn, flipblockMenuLp);

		LayoutParams fliplp = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		fliplp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		fliplp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);

		// this.addView(flipblockLayout, fliplp);
		view.invalidate();

		// ------------------------------------------

		// -------------------------添加横向滑块
		LayoutParams flipblockHMenuLp = new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		flipblockHBtn.setBackgroundResource(R.drawable.flipblock_horizontal);
		// flipblockHMenuLp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		flipblockHLayout.addView(flipblockHBtn, flipblockHMenuLp);

		LayoutParams flipHlp = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		flipHlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		flipHlp.bottomMargin = 70;
		flipHlp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);

		this.addView(flipblockHLayout, flipHlp);
		view.invalidate();

		// ---------------------------------------

		// if(MyView.drawStatus == MyView.STATUS_DRAW_FREE) {
		// personalInfoDisplayLayout = new LinearLayout(context);
		// nameText = new TextView(context);
		// LayoutParams nameLp = new LayoutParams(LayoutParams.WRAP_CONTENT,
		// LayoutParams.WRAP_CONTENT);
		// nameLp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		// nameLp.leftMargin = 400;
		//
		//
		// nameText.setText("章节课时名称： " + keshiName + "        姓名： " + name);
		// nameText.setTextSize(40);
		// Log.i("addText", "add text name");
		// nameText.setGravity(Gravity.CENTER_VERTICAL);
		// nameText.bringToFront();
		// nameText.setVisibility(View.VISIBLE);
		// personalInfoDisplayLayout.addView(nameText, nameLp);
		// this.addView(personalInfoDisplayLayout, nameLp);
		//
		// nameText.invalidate();
		// view.postInvalidate();
		// }

		handwriteControlLayout = new LinearLayout(context);
		LayoutParams handwriteControlLp = new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

		handwriteControlLp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		handwriteControlLp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,
				RelativeLayout.TRUE);

		mHandwriteNewBtn = new Button(context);
		// mHandwriteNewBtn.setText("新建");
		mHandwriteNewBtn.setOnClickListener(handwriteListener);
		mHandwriteNewBtn.setBackgroundResource(R.drawable.newpaper);
		mHandwriteNewBtn.setId(2);
		handwriteControlLayout.addView(mHandwriteNewBtn, lp_edit);

		mMicrophoneBtn = new Button(context);
		mMicrophoneBtn.setOnClickListener(handwriteListener);
		mMicrophoneBtn.setBackgroundResource(R.drawable.microphone);
		// handwriteControlLayout.addView(mMicrophoneBtn, lp_edit);

		mDrawStatusChangeBtn = new Button(context);
		mDrawStatusChangeBtn.setOnClickListener(handwriteListener);
		mDrawStatusChangeBtn.setId(8);
		if (MyView.drawStatus == MyView.STATUS_DRAW_FREE)
			mDrawStatusChangeBtn
					.setBackgroundResource(R.drawable.status_cursorsel);
		else
			mDrawStatusChangeBtn
					.setBackgroundResource(R.drawable.status_tuyasel);
		// handwriteControlLayout.addView(mDrawStatusChangeBtn,new
		// LayoutParams(110,LayoutParams.WRAP_CONTENT));
		handwriteControlLayout.addView(mDrawStatusChangeBtn, new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

		LayoutParams rightDownLp = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		;
		rightDownLp.setMargins(1470, 2200, 0, 100);
		rightDownBtn = new Button(Start.context);
		rightDownBtn.setBackgroundResource(R.drawable.downpageup);
		this.addView(rightDownBtn, rightDownLp);
		
		
		// rightDownBtn.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View arg0) {
		// // TODO Auto-generated method stub
		// // Start.instance.startActivity(new Intent(Start.context,
		// SelectPopWindow.class));
		// }
		// });
		rightDownBtn.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				view.touchMode = view.sideDownMode;
				Log.e("bbb", "x:" + event.getX() + " y:" + event.getY());
				Log.e("bbb",
						"rawx:" + event.getRawX() + " rawy:" + event.getRawY());
				// event.setLocation(event.getX()+ 560, event.getY());
				// ly
				// 这个是右上角下滑按钮
				// my_toast("touch");
				event.setLocation(event.getRawX() - 300, event.getRawY());
				return view.onTouchEvent(event);
			}
		});

		mCameraBtn = new Button(context);
		mCameraBtn.setOnClickListener(handwriteListener);
		mCameraBtn.setBackgroundResource(R.drawable.camera);
		// handwriteControlLayout.addView(mCameraBtn, lp_edit);
		this.addView(handwriteControlLayout, handwriteControlLp);

		if (MyView.penStatus == MyView.STATUS_PEN_CALLI)
			penText = "Hard Pen"; // 硬笔
		else
			penText = "Brush Pen"; // 毛笔

		if (MyView.drawStatus == MyView.STATUS_DRAW_FREE) {
			drawText = "Hand Write"; // 光标
		} else {
			drawText = "Free Draw"; // 涂鸦
		}

		// mDrawStatusChangeBtn.setText(drawText);
		mPenStatusChangeBtn.setText(penText);

		initPopupWindow();
	}

	void addNewPage() {
		// view.saveFile(mBitmap, MyView.FILE_PATH_HEADER + "/add1.png","PNG");
		// Log.i(TAG, "handwrite new line");
		// if(Start.PAGENUM <= Start.totlePageNum){
		// 不保存，页数就不增加
		// 当前页小于等于总页数，当前没有修改就不回保存，也应改新建一页
		// Start.PAGENUM++;不应改是当前页加1，造成混乱，应该变到总页数加1
		// if(view.saveDatebase())
		// Start.PAGENUM = Start.totlePageNum + 2;
		// else
		// Start.PAGENUM = Start.totlePageNum + 1;

		// ly
		// 刷新涂鸦态底图前的准备
		// bad
		if (view.drawStatus == MyView.STATUS_DRAW_FREE) {
			view.changeStateAndSync(MyView.STATUS_DRAW_CURSOR);
			view.changeStateAndSync(MyView.STATUS_DRAW_FREE);
		}
		// end

		// caoheng 2015.11.24不让乱保存
		// view.saveDatebase();
		// Start.c.view.saveDrawLine();
		Start.resetTotalPagenum();
		Start.PAGENUM = Start.totlePageNum + 1;
		File newDir = new File(Start.getStoragePath() + "/calldir/free_"
				+ Start.getPageNum());
		if (!newDir.exists()) {
			newDir.mkdir();
		}
		// }

		ScaleSave.getInstance().newPage();
		view.freeBitmap.resetFreeBitmapList();

		mScaleTransparentBitmap.eraseColor(Color.TRANSPARENT);// 清理涂鸦态底图
		for (int i = 0; i < view.cursorBitmap.listEditableCalligraphy.size(); i++) {
			view.cursorBitmap.listEditableCalligraphy.get(i)
					.resetCurrentCount();
		}
		view.cursorBitmap.clearDataBitmap();

		view.doChangeBackground(WolfTemplateUtil.getCurrentTemplate().getName());

		Canvas canvas = new Canvas();
		bgPath = WolfTemplateUtil.getTemplateBgPath();

		// Bitmap mHandBgBitmap =
		// BitmapFactory.decodeFile(bgPath).copy(Bitmap.Config.ARGB_4444, true);
		Bitmap mHandBgBitmap = null;
		try {
			mHandBgBitmap = BitmapFactory.decodeFile(bgPath).copy(
					Bitmap.Config.ARGB_4444, true);
			BitmapCount.getInstance().createBitmap(
					"Calligraph decode mHandBgBitmap");

			canvas.setBitmap(view.cursorBitmap.bitmap);
			canvas.drawBitmap(mHandBgBitmap, 0, 0, mPaint);
			//
			canvas.setBitmap(mBitmap);
			canvas.drawBitmap(mHandBgBitmap, 0, 0, mPaint);
			canvas.drawBitmap(mHandBgBitmap, Start.SCREEN_WIDTH, 0, mPaint);

			// canvas.setBitmap(view.cursorBitmap.transparentBitmap);
			// canvas.drawBitmap(mHandBgBitmap, 0, 0, mPaint);
			// //切换背景，第一个字保留上一张背景，所以住掉。

			// view.saveFile(view.cursorBitmap.transparentBitmap, "scale.jpg");
			// view.cursorBitmap.getTopBitmap().eraseColor(Color.TRANSPARENT);
			mHandBgBitmap.recycle();
			BitmapCount.getInstance().recycleBitmap(
					"Calligraph onCreate mHandBgBitmap");
		} catch (OutOfMemoryError e) {
			// TODO: handle exception
			Log.e("AndroidRuntime", "Calligraph addNewPage() OOM!!!");
		}

		// view.cursorBitmap.cal_current.clear();
		// Canvas canvas = new Canvas();
		// canvas.setBitmap(mBitmap);
		// path = WolfTemplateUtil.TEMPLATE_PATH +
		// view.mTemplate.getName()+"/"+view.mTemplate.getBackground();
		// System.out.println("================bgPath:"+path);
		// path = "/sdcard/template/diary/diary_bg.png";
		// Bitmap bgBitmap =
		// BitmapFactory.decodeFile("/sdcard/template/notebook_add_bg.png");
		// // if(view.mTemplate.getFormat() == 0){
		// // //0:涂鸦 1:光标
		// //// canvas.drawBitmap(bgBitmap, 0, 0, mPaint);
		// // }else{
		// canvas.drawBitmap(bgBitmap, Start.SCREEN_WIDTH, 0,
		// view.baseBitmap.paint);
		//
		// // }
		// bgBitmap.recycle();

		EditableCalligraphy.flip_bottom = 800;
		EditableCalligraphy.flip_dst = 0;

		EditableCalligraphy.flip_Horizonal_bottom = Start.SCREEN_WIDTH;
		EditableCalligraphy.flip_Horizonal_dst = 0;
		Start.resetDate();
		view.cursorBitmap.initDate(WolfTemplateUtil.getCurrentTemplate());

		flipblockBtn.setVisibility(View.GONE);
		view.cursorBitmap.updateHandwriteState();
		view.invalidate();

		ImageLimit.instance().resetImageCount();
		WordLimit.getInstance().resetWordCount();

		// ly
		// 新建涂鸦态之后要加入一个背景图
		view.isLoad = false;
		view.addFreeBg();
		// end

		// ly
		// 新建完成之后把涂鸦态背景图刷出来
		if (view.drawStatus == MyView.STATUS_DRAW_FREE)
			view.freeBitmap.drawFreeBitmapSync();
		// end

	}

	public void mindmapEditFinish() {
		if (((HandWriteMode) view.getTouchMode()).isMindMapEditableStatus()) {
			view.cursorBitmap.endMindmapEdit();
			((HandWriteMode) view.getTouchMode()).setMindMapEditStatusFalse();

			// view.cursorBitmap.insertEndOfLine();
			view.cursorBitmap.endInsertOfLine();
		}
	}

	public void changeMindmapStatus() {
		if (view.getTouchMode() instanceof HandWriteMode) {
			if (((HandWriteMode) view.getTouchMode()).isMindMapEditableStatus()) {
				LogUtil.getInstance().e("mindmap", "mindmap edit finish");
				mindmapEditFinish();
				setNotMindMapStatus();
			} else {
				setMindMapStatus();

				// ly
				// 刷屏
				view.mBitmap.eraseColor(Color.WHITE);
				// end

				view.cursorBitmap.addNewMindMap();
			}
		}
	}

	public void setMindMapStatus() {
		mHandwriteInsertEnSpaceBtn.setBackgroundResource(R.drawable.enspace_in);
	}

	public void setNotMindMapStatus() {
		mHandwriteInsertEnSpaceBtn.setBackgroundResource(R.drawable.enspace);
	}

	class HandwriteBtnListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub

			if (MyView.drawStatus == MyView.STATUS_DRAW_CURSOR) {
				try {// zk20121109
					if (v == mHandwriteDelBtn) {
						// Log.i(TAG, "handwrite del character");

						// ly
						// 清一下屏吧
						view.mBitmap.eraseColor(Color.WHITE);
						// view.mBitmap.eraseColor(Color.RED);
						// end

						view.cursorBitmap.delCharacter();
						view.cursorBitmap.cal_current.setFlipDst(true,
								"HandwriteBtnListener");
						Start.status.modified("delete");
						WorkQueue.getInstance().endFlipping();

					}
					if (v == mHandwriteBackwardBtn) {
						Log.i(TAG, "handwrite back");
						view.cursorBitmap.backward();
					}
					if (v == mHandwriteForwardBtn) {
						Log.i(TAG, "handwrite front");
						view.cursorBitmap.forward();
					}
					if (v == mHandwriteInsertSpaceBtn) {
						Log.i(TAG, "handwrite insert space");
						Start.status.modified("insert space");
						view.cursorBitmap.insertSpace();
					}
					if (v == mHandwriteInsertEnSpaceBtn) {
						/*
						 * 导图按钮 // Log.i(TAG, "handwrite insert space"); //
						 * view.cursorBitmap.insertSpace();
						 * view.cursorBitmap.insertEnSpace();
						 * Start.status.modified("insert space");
						 */
						changeMindmapStatus();

					}
					if (v == mHandwriteEndofLineBtn) {
						// Log.i(TAG, "handwrite new line");
						// if(!view.cursorBitmap.endofLine)
						// Start.saveHandler.sendEmptyMessage(0);
						view.cursorBitmap.insertEndOfLine();
						Start.status.modified("insert EndofLine");
					}

					if (v == mHandWriteUndoBtn) {
						// * 暂时改做翰林算子button
						Log.i(TAG, "handwrite new undo");
						int len = undoList.size();
						if (len > 0) {
							undoList.get(len - 1).undo(mBitmap);
							undoList.remove(len - 1);
							view.cursorBitmap.updateHandwriteState();// 将刷新图片的工作交给图片的持有者来做
							Start.status.modified("undo");
						} else
							Toast.makeText(mContext, "No Need To Undo",
									Toast.LENGTH_SHORT).show();
					}

				} catch (Exception e) {

				}
			}
			// 2015.11.11 caoheng
			if (MyView.drawStatus == MyView.STATUS_DRAW_FREE) {
				if (v == mHandwriteInsertSpaceBtn) {
					Log.e("addmorepic", "add next pic.");
					// view.saveDatebase();
					view.addNextPic();
				}
				if (v == mHandwriteEndofLineBtn) {
					Log.e("addmorepic", "add previous pic.");
					view.addPreviousPic();
					Toast.makeText(mContext, "上一张", Toast.LENGTH_SHORT).show();
				}

				if (v == mHandwriteDelBtn) {
					Log.i("addIndex", "show pic index.");
					// view.addPreviousPic();
					// Toast.makeText(mContext, "show index",
					// Toast.LENGTH_SHORT).show();
					AlertDialog.Builder builder = new AlertDialog.Builder(
							view.getContext());
					builder.setTitle("二十章正确率统计");
					builder.setMessage("1：对 3  错  0  半对  0\n"
							+ "2：对 2  错  1  半对  0\n" + "3：对 1  错  1  半对  1\n"
							+ "4：对 1  错  1  半对  1\n" + "5：对 1  错  2  半对  0\n");

					builder.setPositiveButton("提交批改",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface arg0,
										int arg1) {
									// TODO Auto-generated method stub
									Toast.makeText(view.getContext(), "批改已提交.",
											Toast.LENGTH_SHORT).show();
									view.DemoChangeBg1();
									arg0.cancel();

								}
							});

					builder.create().show();
				}

				if (v == mHandWriteUndoBtn) {
					// * 暂时改做翰林算子button

					Log.i("slide", "undo to change mode");
					Toast.makeText(mContext, "保存", Toast.LENGTH_SHORT).show();
					v.invalidate();
					saveScreen();
					v.invalidate();

					// v.postInvalidate();
					// caoheng, 11.28,undo按钮切换操作模式：滑屏<->画笔

					// if(GESTURE_MODE == GESTURE_MODE_ON) {
					// GESTURE_MODE = GESTURE_MODE_OFF;
					// // view.hardImpl.paint.setColor(Color.RED);
					// } else {
					// GESTURE_MODE = GESTURE_MODE_ON;
					// //取消画笔````
					// view.changePenState(MyView.STATUS_PEN_HARD);
					// view.hardImpl.paint.setAlpha(100);
					//
					// }

					// Log.i("slide", "gesture mode = " + GESTURE_MODE);

				}

			}

			// caoheng 1.13 新建按钮弹出listView
			// if(v == mHandwriteNewBtn) {
			//
			// //ly
			// //新建的时候要刷屏
			// //
			//
			// my_toast("xinjian");
			//
			//
			// // view.mBitmap.eraseColor(Color.WHITE);
			// // //end
			// //
			// // mindmapEditFinish();
			// // MindMapItem.resetMindMapCount();
			// // addNewPage();
			//
			//
			//
			//
			//
			// }
			/*
			 * if(v == mDragEnableBtn){ Log.i(TAG, "Drag Button Click");
			 * if(MyView.MODE_DRAG == false) { view.syncMainToScale();
			 * view.sMatrix.reset(); mDragEnableBtn.setText("拖拽模式");
			 * MyView.MODE_DRAG = true; } else { view.syncScaleToMain();
			 * mDragEnableBtn.setText("画笔模式"); MyView.MODE_DRAG = false; }
			 * view.invalidate(); }
			 */
			if (v == mDrawStatusChangeBtn) {

				// Start.saveHandler.sendEmptyMessage(0);

				// ly
				// view.mBitmap.eraseColor(Color.WHITE);
				// end

				// Start.status.modified("tuya");
				if (MyView.drawStatus == MyView.STATUS_DRAW_FREE) {

					// ly
					// 切换至光表态，隐藏下一页按钮
					// mNextBtn.setVisibility(View.INVISIBLE);
					// end

					view.changeStateAndSync(MyView.STATUS_DRAW_CURSOR);
					view.cursorBitmap.initDate(WolfTemplateUtil
							.getCurrentTemplate());
				} else {

					// ly
					// 切换至光表态，隐藏下一页按钮
					// mNextBtn.setVisibility(View.VISIBLE);
					// end

					// ly
					// 刷一下屏看看
					// mBitmap.eraseColor(Color.WHITE);
					// end
					Log.i("caoheng", "btnClickListener");

					view.changeStateAndSync(MyView.STATUS_DRAW_FREE);
				}
			}

			if (v == mMicrophoneBtn) {
				// Toast toast = new Toast(mContext);
				// ImageView tv = new ImageView(mContext);
				// tv.setBackgroundResource(R.drawable.icon);
				// toast.setView(tv);
				// toast.setDuration(Toast.LENGTH_SHORT);
				// toast.show();
				my_toast("录音功能正在建设中");
				// Toast.makeText(mContext, "正在建设中", Toast.LENGTH_LONG).show();
			}
			if (v == mCameraBtn) {
				Toast.makeText(mContext, "您的设备目前不支持", Toast.LENGTH_LONG).show();
			}
		}

	}

	class DrawStatusBtnListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Log.i(TAG, "drawStatus:" + MyView.drawStatus);
			if (MyView.drawStatus == MyView.STATUS_DRAW_FREE) {
				// view.paperFlag = 1;
				mDrawStatusChangeBtn.setText("Free Draw"); // 涂鸦
				view.changeDrawState(MyView.STATUS_DRAW_CURSOR);
			} else {
				// view.paperFlag = 0;
				mDrawStatusChangeBtn.setText("Hand Write"); // 光标
				view.changeDrawState(MyView.STATUS_DRAW_FREE);
				view.cursorBitmap.updateHandwriteState();
			}
		}
	}

	class PenStatusBtnListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Log.i(TAG, "penStatus:" + MyView.penStatus);
			if (MyView.penStatus == MyView.STATUS_PEN_CALLI) {
				// view.paperFlag = 1;
				mPenStatusChangeBtn.setText("Brush Pen"); // 毛笔
				view.changePenState(MyView.STATUS_PEN_HARD);
			} else {
				// view.paperFlag = 0;
				mPenStatusChangeBtn.setText("Hard Pen"); // 硬笔
				view.changePenState(MyView.STATUS_PEN_CALLI);
			}
		}
	}

	class SlideBtnListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if (MyView.drawStatus == MyView.STATUS_DRAW_CURSOR) {
				CurInfo cur = view.baseBitmap.bCurInfo;
				if (cur.mPosLeft > -Start.SCREEN_WIDTH)
					cur.mPosLeft--;
				view.invalidate();
				// view.draw();
			}

		}

	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub
		super.onLayout(changed, l, t, r, b);
		Log.i(TAG, "onLayout");
		// int ll = handwriteMenuLayout.ml;
		// int tt = handwriteMenuLayout.mt;
		// int rr = handwriteMenuLayout.mr;
		// int bb = handwriteMenuLayout.mb;
		// Log.i(TAG, ll + " " + tt + " " + rr + " " + bb);
		// if(ll != 0 && tt != 0 && rr != 0 && bb != 0)
		// handwriteMenuLayout.layout(ll, tt, rr, bb);
	}

	public static void my_toast(String msg) {
		LayoutInflater inflater = (Start.instance).getLayoutInflater();
		View layout = inflater
				.inflate(R.layout.toast, (ViewGroup) Start.instance
						.findViewById(R.id.toast_layout_root));

		ImageView image = (ImageView) layout.findViewById(R.id.image);
		TextView text = (TextView) layout.findViewById(R.id.text);
		text.setText(msg);

		Toast toast = new Toast((Start.instance).getApplicationContext());
		toast.setGravity(Gravity.TOP | Gravity.LEFT, 50, (int) (yy + 200));
		toast.setDuration(Toast.LENGTH_LONG);
		toast.setView(layout);
		// try
		// {
		// // 从Toast对象中获得mTN变量
		// Field field = toast.getClass().getDeclaredField("mTN");
		// field.setAccessible(true);
		// Object obj = field.get(toast);
		// // TN对象中获得了show方法
		// Method method = obj.getClass().getDeclaredMethod("show", null);
		// // 调用show方法来显示Toast信息提示框
		// method.invoke(obj, null);
		// }
		// catch (Exception e)
		// {
		// }
		toast.show();
	}

	public static boolean opened = false;

	@Override
	public void onPanelClosed(Panel panel) {
		// TODO Auto-generated method stub
		opened = false;

		Runnable r = new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				upHandler.sendEmptyMessage(0);
			}
		};
		(new Thread(r)).start();
	}

	@Override
	public void onPanelOpened(Panel panel) {
		// TODO Auto-generated method stub
		opened = true;
		Log.e("panel", "open listen top:" + flipblockLayout.getTop());
	}

	public void startTransCameraPic() {

		jni = new Jni();
		// 建立adhoc
		// 绑定service
		Intent intent = new Intent().setClass(Start.context,
				TransmitProtocolService.class);
		Start.context.bindService(intent, conn, Context.BIND_AUTO_CREATE);

		recvThreadFlag = true;

		(new RecvRequest()).start();// 与手机端握手链接
		(new GetIP()).start();// 获取本地ip
		(new HandleTramsmit()).start();// 开始传输，等待接收完毕

	}

	public void restartGetIP() {
		(new GetIP()).start();// 获取本地ip
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (panel.isFlipping()) {
			return;
		}
		if (v == expend_AlarmBtn) {
			Start.instance.showDialog(Start.DATETIMESELECTOR_ID);
			panel.openOrClose();
			// Toast.makeText(Start.context, "alarm",
			// Toast.LENGTH_SHORT).show();
		} else if (v == expend_AudioBtn) {
			// my_toast("录音功能正在建设中");
			// mRecordAlertDlg = onCreatDialog(ALERT_RECORD_DLG);
			//
			// dbrListener.onClick(v);
			positiveDialogOnClick();
			panel.openOrClose();

			/*
			 * shareType = AUDIOSHARE;
			 * 
			 * if(MyView.drawStatus == MyView.STATUS_DRAW_FREE) {
			 * Toast.makeText(Start.context, "涂鸦态下不可用",
			 * Toast.LENGTH_SHORT).show(); return; } if(MyView.drawStatus ==
			 * MyView.STATUS_DRAW_CURSOR) { // disable insert picture String
			 * name =
			 * view.cursorBitmap.cal_current.getAvailable().getControltype();
			 * if(Available.AVAILABLE_SUBJECT.equals(name) ||
			 * Available.AVAILABLE_NUMBER.equals(name) ||
			 * Available.AVAILABLE_DATE.equals(name)){ Toast.makeText(mContext,
			 * "不能插入音频", Toast.LENGTH_SHORT).show(); return; } }
			 * 
			 * try { view.cursorBitmap.insertAudioBitmap(
			 * BitmapFactory.decodeResource(getResources(),
			 * R.drawable.audio_unfinish) ,null);
			 * BitmapCount.getInstance().createBitmap
			 * ("Calligraph decode insertAudioBitmap audio_unfinish");
			 * 
			 * } catch (OutOfMemoryError e) { // TODO: handle exception
			 * Toast.makeText(Start.context, "内存不足，不能插入",
			 * Toast.LENGTH_SHORT).show(); return; }
			 * 
			 * Uri uri = Uri.parse("file:///android_asset/audio.png");//默认等待图片
			 * Log.e("camera", uri.toString());
			 * 
			 * cameraPicPage = Start.getPageNum(); cameraPicAvailableID =
			 * view.cursorBitmap.cal_current.getID(); cameraPicItemID =
			 * view.cursorBitmap.cal_current.currentpos -1;
			 * 
			 * Log.e("camera", "page:" + cameraPicPage + " availableID:" +
			 * cameraPicAvailableID + " itemID:" + cameraPicItemID);
			 * 
			 * wifiandadhocPause = true; Log.e("adhoc",
			 * "before start WIFI and ADHOC activity");
			 * 
			 * send3PhotoShare();
			 * 
			 * // Intent eintent = new Intent(); // eintent.setComponent(new
			 * ComponentName("com.jinke.wifiadhoc.select", //
			 * "com.jinke.wifiadhoc.select.wifioradhoc")); //
			 * Start.instance.startActivityForResult
			 * (eintent,Start.AddAudioRequest); setAdhocMode();
			 * startTransCameraPic();
			 * 
			 * // Toast.makeText(Start.context, "audio",
			 * Toast.LENGTH_SHORT).show();
			 */
		} else if (v == expend_AddCameraBtn) {
			/*
			 * my_toast("照片功能正在建设中");
			 */
			panel.openOrClose();

			// my_toast("拍照");
			File imgFile = new File(Start.TempImgFilePath);
			if (!imgFile.exists()) {
				File dir = imgFile.getParentFile();
				dir.mkdirs();
			}

			Uri uri = Uri.fromFile(imgFile);
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
			Start.instance.startActivityForResult(intent,
					Start.AddCameraRequest);

			// shareType = PHOTOSHARE;
			//
			// if(!ImageLimit.instance().canInsertImage()){
			// Toast.makeText(Start.context, "最多插入" + ImageLimit.LIMIT_NUMBER +
			// "张图片", Toast.LENGTH_SHORT).show();
			// return;
			// }
			//
			// if(MyView.drawStatus == MyView.STATUS_DRAW_FREE)
			// {
			// Toast.makeText(Start.context, "涂鸦态下不可用",
			// Toast.LENGTH_SHORT).show();
			// return;
			// }
			// if(MyView.drawStatus == MyView.STATUS_DRAW_CURSOR) {
			// // disable insert picture
			// String name =
			// view.cursorBitmap.cal_current.getAvailable().getControltype();
			// if("subject".equals(name) || "number".equals(name) ||
			// "date".equals(name)){
			// Toast.makeText(mContext, "不能插入图片", Toast.LENGTH_SHORT).show();
			// return;
			// }
			// }
			// File imgFile = new File(Start.TempImgFilePath);
			// if(!imgFile.exists()){
			// File dir = imgFile.getParentFile();
			// dir.mkdirs();
			// }
			//
			// if(!"123456".equals(CalligraphyBackupUtil.getSimID())){
			// //有sim卡,认为没有摄像头,插入默认图片，开启adhoc，等待传输成功后刷新
			//
			// try {
			// EditableCalligraphyItem item =
			// view.cursorBitmap.insertImageBitmap(
			// BitmapFactory.decodeResource(getResources(),
			// R.drawable.photo_adhoc)
			// ,Uri.parse("android.resource://" +
			// Start.context.getApplicationContext().getPackageName() + "/" +
			// R.drawable.photo_adhoc));
			// item.setWifiOrAdhoc();
			// BitmapCount.getInstance().createBitmap("Calligraph decode photo.png");
			// firstTransformPicFromMobile = true;
			// } catch (OutOfMemoryError e) {
			// // TODO: handle exception
			// Toast.makeText(Start.context, "内存不足，不能插入",
			// Toast.LENGTH_SHORT).show();
			// return;
			// }
			//
			// Uri uri = Uri.parse("file:///android_asset/photo.png");//默认等待图片
			// Log.e("camera", uri.toString());
			//
			// cameraPicPage = Start.getPageNum();
			// cameraPicAvailableID = view.cursorBitmap.cal_current.getID();
			// cameraPicItemID = view.cursorBitmap.cal_current.currentpos -1;
			//
			// Log.e("camera", "page:" + cameraPicPage + " availableID:" +
			// cameraPicAvailableID + " itemID:" + cameraPicItemID);
			//
			// wifiandadhocPause = true;
			// Log.e("adhoc", "before start WIFI and ADHOC activity");
			//
			// (new send3PhotoShareThread()).start();
			// try {
			// Thread.sleep(100);
			// } catch (InterruptedException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
			//
			// // Intent eintent = new Intent();
			// // eintent.setComponent(new
			// ComponentName("com.jinke.wifiadhoc.select",
			// // "com.jinke.wifiadhoc.select.wifioradhoc"));
			// //
			// Start.instance.startActivityForResult(eintent,Start.AddCameraRequest);
			//
			// setAdhocMode();
			// startTransCameraPic();
			//
			// }else{
			// //没有sim卡，认为有摄像头，直接打开摄像头拍照
			// Uri uri = Uri.fromFile(imgFile);
			// Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			// intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
			// Start.instance.startActivityForResult(intent,
			// Start.AddCameraRequest);
			// }

		} else if (v == expend_FaceBtn) {
			if (MyView.drawStatus == MyView.STATUS_DRAW_FREE) {
				Toast.makeText(Start.context, "涂鸦态下不可用", Toast.LENGTH_SHORT)
						.show();
				return;
			}
			int[] faceImageRes = new int[] { R.drawable.icon_mood1,
					R.drawable.icon_mood2, R.drawable.icon_mood3,
					R.drawable.icon_mood4, R.drawable.icon_mood5,
					R.drawable.icon_mood6, R.drawable.icon_mood7,
					R.drawable.icon_mood8, R.drawable.icon_mood9,
					R.drawable.icon_mood10, R.drawable.icon_mood11,
					R.drawable.icon_mood12 };
			showPopupView(faceImageRes);
			panel.openOrClose();
			// my_toast("表情功能正在建设中");
			// Toast.makeText(Start.context, "face", Toast.LENGTH_SHORT).show();
		} else if (v == expend_WeatherBtn) {
			if (MyView.drawStatus == MyView.STATUS_DRAW_FREE) {
				Toast.makeText(Start.context, "涂鸦态下不可用", Toast.LENGTH_SHORT)
						.show();
				return;
			}
			int[] weatherImageRes = new int[] { R.drawable.icon_weather1,
					R.drawable.icon_weather2, R.drawable.icon_weather3,
					R.drawable.icon_weather4, R.drawable.icon_weather5,
					R.drawable.icon_weather6, R.drawable.icon_weather7,
					R.drawable.icon_weather8 };
			showPopupView(weatherImageRes);
			panel.openOrClose();
			// my_toast("天气功能正在建设中");
			// Toast.makeText(Start.context, "weather",
			// Toast.LENGTH_SHORT).show();
		} else if (v == expend_AddpicBtn) {

			panel.openOrClose();

			if (!ImageLimit.instance().canInsertImage()) {
				Toast.makeText(Start.context,
						"最多插入" + ImageLimit.LIMIT_NUMBER + "张图片",
						Toast.LENGTH_SHORT).show();
				return;
			}

			if (MyView.drawStatus == MyView.STATUS_DRAW_FREE) {
				// 插入打开相册功能，caoheng, 10.25
				int CHOOSEPICTURE_REQUESTCODE = 0;
				Intent choosePictureIntent = new Intent(
						Intent.ACTION_PICK,
						android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				// 啟動該activity，并在該activity結束返回數據,所以調用startActivityForResult()方法
				Start.instance.startActivityForResult(choosePictureIntent,
						CHOOSEPICTURE_REQUESTCODE);
				// Toast.makeText(Start.context, "打开相册插入图片",
				// Toast.LENGTH_SHORT).show();
				return;
			}
			if (MyView.drawStatus == MyView.STATUS_DRAW_CURSOR) {
				// disable insert picture
				String name = view.cursorBitmap.cal_current.getAvailable()
						.getControltype();
				if (Available.AVAILABLE_SUBJECT.equals(name)
						|| Available.AVAILABLE_NUMBER.equals(name)
						|| Available.AVAILABLE_DATE.equals(name)) {
					Toast.makeText(mContext, "不能插入图片", Toast.LENGTH_SHORT)
							.show();
					return;
				}
			}
			view.cursorBitmap.picFlag = true;
			systemScan();
			Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setType("image/*");
			Start.instance.startActivityForResult(intent,
					Start.AddPictureRequest);

		} else if (v == expend_AddVideoBtn) {
			my_toast("视频功能正在建设中");
			panel.openOrClose();

			/*
			 * shareType = VIDEOSHARE; if(MyView.drawStatus ==
			 * MyView.STATUS_DRAW_FREE) { Toast.makeText(Start.context,
			 * "涂鸦态下不可用", Toast.LENGTH_SHORT).show(); return; }
			 * if(MyView.drawStatus == MyView.STATUS_DRAW_CURSOR) { // disable
			 * insert picture String name =
			 * view.cursorBitmap.cal_current.getAvailable().getControltype();
			 * if(Available.AVAILABLE_SUBJECT.equals(name) ||
			 * Available.AVAILABLE_NUMBER.equals(name) ||
			 * Available.AVAILABLE_DATE.equals(name)){ Toast.makeText(mContext,
			 * "不能插入视频", Toast.LENGTH_SHORT).show(); return; } }
			 * 
			 * try { view.cursorBitmap.insertVideoBitmap(
			 * BitmapFactory.decodeResource(getResources(),
			 * R.drawable.video_unfinish) ,null);
			 * BitmapCount.getInstance().createBitmap
			 * ("Calligraph decode video_unfinish.png"); } catch
			 * (OutOfMemoryError e) { // TODO: handle exception
			 * Toast.makeText(Start.context, "内存不足，不能插入",
			 * Toast.LENGTH_SHORT).show(); return; }
			 * 
			 * // Uri uri =
			 * Uri.parse("file:///android_asset/video.png");//默认等待图片 //
			 * Log.e("camera", uri.toString());
			 * 
			 * cameraPicPage = Start.getPageNum(); cameraPicAvailableID =
			 * view.cursorBitmap.cal_current.getID(); cameraPicItemID =
			 * view.cursorBitmap.cal_current.currentpos -1;
			 * 
			 * Log.e("camera", "page:" + cameraPicPage + " availableID:" +
			 * cameraPicAvailableID + " itemID:" + cameraPicItemID);
			 * 
			 * wifiandadhocPause = true; Log.e("adhoc",
			 * "before start WIFI and ADHOC activity");
			 * 
			 * send3PhotoShare();
			 * 
			 * // Intent eintent = new Intent(); // eintent.setComponent(new
			 * ComponentName("com.jinke.wifiadhoc.select", //
			 * "com.jinke.wifiadhoc.select.wifioradhoc")); //
			 * Start.instance.startActivityForResult
			 * (eintent,Start.AddVideoRequest); setAdhocMode();
			 * startTransCameraPic();
			 */
		}

	}

	private Handler updateCameraHandler = new Handler() {
		public void handleMessage(Message msg) {
			int page = msg.arg1;
			int aid = msg.arg2;
			int iid = msg.what;
			EditableCalligraphyItem item = null;

			if (savepath != null && PHOTOSHARE.equals(shareType)) {
				for (int i = 0; i < savepath.length; i++) {
					Log.e("uri", "savepath [" + i + "] :" + savepath[i]);
					if (i == 0 && firstTransformPicFromMobile) {
						firstTransformPicFromMobile = false;
						Uri cameraUri = Uri.parse(savepath[i]);
						Log.e("uri:", "cameraUri:" + (cameraUri == null));
						CDBPersistent db = new CDBPersistent(Start.context);
						Bitmap newBitmap = null;
						newBitmap = db.getBitmapFromUri(cameraUri, page);
						Log.e("photo", "reset:a" + aid + " item:" + iid);
						item = view.cursorBitmap.listEditableCalligraphy
								.get(aid).getCharsList().get(iid);
						view.cursorBitmap.listEditableCalligraphy
								.get(aid)
								.getCharsList()
								.get(iid)
								.resetCharBitmap(newBitmap, new Matrix(),
										cameraUri);
						CalligraphyDB.getInstance(Start.context)
								.updateCameraPicUri(page, aid,
										item.getItemID(), cameraUri);
					} else {
						try {
							Uri cameraUri = Uri.parse(savepath[i]);

							CDBPersistent db = new CDBPersistent(Start.context);
							Bitmap newBitmap = null;
							newBitmap = db.getBitmapFromUri(cameraUri, page);

							EditableCalligraphyItem e = view.cursorBitmap
									.insertImageBitmap(newBitmap, cameraUri);

						} catch (OutOfMemoryError e) {
							// TODO: handle exception
							Toast.makeText(Start.context, "内存不足，不能插入",
									Toast.LENGTH_SHORT).show();
							return;
						}
					}
				}
			}
			Bitmap newBitmap = null;
			if (VIDEOSHARE.equals(shareType)) {
				Uri cameraUri = Uri.parse(savepath[0]);

				double dur = MediaPlayerUtil.getInstance().getDuration(
						cameraUri) / 1000;// s
				String duration = Math.floor(dur / 60) + "分"
						+ Math.ceil((dur / 60 - Math.floor(dur / 60)) * 60)
						+ "秒";
				Log.e("media", "duration:" + duration);

				try {
					newBitmap = BitmapFactory.decodeResource(getResources(),
							R.drawable.video).copy(Config.ARGB_4444, true);
					Canvas canvas = new Canvas();
					canvas.setBitmap(newBitmap);
					Paint p = new Paint();
					p.setTextSize(20);
					canvas.drawText(duration, 145f, 30f, p);

					BitmapCount.getInstance().createBitmap(
							"BaseBitmap decode R.drawable.video");
				} catch (OutOfMemoryError e) {
					// TODO: handle exception
					Toast.makeText(Start.context, "内存不足，不能插入",
							Toast.LENGTH_SHORT).show();
					return;
				}
				item = view.cursorBitmap.listEditableCalligraphy.get(aid)
						.getCharsList().get(iid);
				view.cursorBitmap.listEditableCalligraphy.get(aid)
						.getCharsList().get(iid)
						.resetVideoUri(newBitmap, new Matrix(), cameraUri);
				CalligraphyDB.getInstance(Start.context).updateCameraPicUri(
						page, aid, item.getItemID(), cameraUri);
				CalligraphyDB.getInstance(Start.context).updatePictrueItem(
						Start.getPageNum(), 3, item.getItemID(), item);
			}

			if (AUDIOSHARE.equals(shareType)) {
				Uri cameraUri = Uri.parse(savepath[0]);

				double dur = MediaPlayerUtil.getInstance().getDuration(
						cameraUri) / 1000;// s
				String duration = Math.floor(dur / 60) + "分"
						+ Math.ceil((dur / 60 - Math.floor(dur / 60)) * 60)
						+ "秒";
				Log.e("media", "duration:" + duration);
				try {
					newBitmap = BitmapFactory.decodeResource(getResources(),
							R.drawable.audio_stop).copy(Config.ARGB_4444, true);

					BitmapCount.getInstance().createBitmap(
							"BaseBitmap decode R.drawable.audio_playing");
					BitmapCount.getInstance().createBitmap(
							"BaseBitmap decode R.drawable.audio_stop");

					Canvas canvas = new Canvas();
					canvas.setBitmap(newBitmap);
					Paint p = new Paint();
					p.setTextSize(20);
					canvas.drawText(duration, 145f, 30f, p);

				} catch (OutOfMemoryError e) {
					// TODO: handle exception
					Toast.makeText(Start.context, "内存不足，不能插入",
							Toast.LENGTH_SHORT).show();
					return;
				}
				item = view.cursorBitmap.listEditableCalligraphy.get(aid)
						.getCharsList().get(iid);
				// view.cursorBitmap.listEditableCalligraphy.get(aid).getCharsList().get(iid).resetAudioUri(newBitmap,new
				// Matrix(),cameraUri);
				item.resetAudioUri(newBitmap, new Matrix(), cameraUri);
				item.setStopBitmap();
				CalligraphyDB.getInstance(Start.context).updateCameraPicUri(
						page, aid, item.getItemID(), cameraUri);
				CalligraphyDB.getInstance(Start.context).updatePictrueItem(
						Start.getPageNum(), 3, item.getItemID(), item);
			}

			ImageLimit.instance().addImageCount();

			// String picName = "p"+page + "a" + aid + "i" + iid;
			// Uri cameraUri = view.savePicBitmap(saveuri, picName);
			// Log.e("uri:", "cameraUri:" + (cameraUri == null));
			// CDBPersistent db = new CDBPersistent(Start.context);
			// db.open();
			// Bitmap newBitmap = null;
			// if(VIDEOSHARE.equals(shareType)){
			//
			// try {
			// newBitmap = BitmapFactory.decodeResource(getResources(),
			// R.drawable.video);
			// BitmapCount.getInstance().createBitmap("BaseBitmap decode R.drawable.video");
			// } catch (OutOfMemoryError e) {
			// // TODO: handle exception
			// Toast.makeText(Start.context, "内存不足，不能插入",
			// Toast.LENGTH_SHORT).show();
			// return;
			// }
			// item =
			// view.cursorBitmap.listEditableCalligraphy.get(aid).getCharsList().get(iid);
			// view.cursorBitmap.listEditableCalligraphy.get(aid).getCharsList().get(iid).resetVideoUri(newBitmap,new
			// Matrix(),cameraUri);
			// CalligraphyDB.getInstance(Start.context).updateCameraPicUri(page,
			// aid, item.getItemID(), cameraUri);
			// }
			// if(AUDIOSHARE.equals(shareType)){
			// try {
			// newBitmap = BitmapFactory.decodeResource(getResources(),
			// R.drawable.audio);
			// BitmapCount.getInstance().createBitmap("BaseBitmap decode R.drawable.audio");
			// } catch (OutOfMemoryError e) {
			// // TODO: handle exception
			// Toast.makeText(Start.context, "内存不足，不能插入",
			// Toast.LENGTH_SHORT).show();
			// return;
			// }
			// item =
			// view.cursorBitmap.listEditableCalligraphy.get(aid).getCharsList().get(iid);
			// view.cursorBitmap.listEditableCalligraphy.get(aid).getCharsList().get(iid).resetAudioUri(newBitmap,new
			// Matrix(),cameraUri);
			// CalligraphyDB.getInstance(Start.context).updateCameraPicUri(page,
			// aid, item.getItemID(), cameraUri);
			// }

			view.cursorBitmap.updateHandwriteState();
			WorkQueue.getInstance().endFlipping();
			end = System.currentTimeMillis();
			Log.e("wifioradhoc", "using time:" + (end - start) + " ms");
			Start.status.modified("complete transform pic from telephone");
			// Start.saveHandler.sendEmptyMessage(0);
			// db.close();

			// jni.closeAdhoc();//可以改放到用wifi网络时再关闭
			// recvThreadFlag = false;
			// m_RecvHost.close();
			// m_handletransmit.close();
			// m_RecvHost = null;
			// m_handletransmit = null;

		}
	};

	private void initPopupWindow() {
		popupView = Start.instance.getLayoutInflater().inflate(
				R.layout.icon_popup_window, null);
		// popupWindow = new PopupWindow(popupView, 500, 270);
		popupWindow = new PopupWindow(popupView, Start.SCREEN_WIDTH * 5 / 6,
				Start.SCREEN_HEIGHT * 1 / 4);

		popupWindow.setOutsideTouchable(false);
		popupWindow.setFocusable(true);// 默认false，不会响应itemClickListener

	}

	public void showPopupView(final int[] res) {

		GridView icon_gridview = (GridView) popupView
				.findViewById(R.id.icon_popup_gridView);

		icon_gridview.setAdapter(new IconPopupAdapter(Start.context, res));

		icon_gridview
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						// TODO Auto-generated method stub

						view.cursorBitmap.insertImageItem(res[arg2]);
						popupWindow.dismiss();

					}
				});

		Button cancleButton = (Button) popupView
				.findViewById(R.id.icon_popup_cancleBtn);
		// cancleButton.setTextSize(Start.SCREEN_WIDTH / 30);
		cancleButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				popupWindow.dismiss();
			}
		});

		popupWindow.showAtLocation(Start.c.view, Gravity.NO_GRAVITY, 85, 640);

		// ly
		// popupWindow.showAtLocation(Start.c.view, Gravity.NO_GRAVITY,
		// Start.SCREEN_WIDTH * (85 / 600), Start.SCREEN_HEIGHT *(700/1024));
		popupWindow.showAtLocation(Start.c.view, Gravity.NO_GRAVITY,
				Start.SCREEN_WIDTH * (85 / 1600), Start.SCREEN_HEIGHT
						* (700 / 2560));
	}

	private TransmitProtocolService sservice = null;
	private ServiceConnection conn = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
			sservice = null;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// TODO Auto-generated method stub
			sservice = ((TransmitProtocolService.ShareBind) service)
					.getService();
		}
	};

	private boolean recvThreadFlag = true;
	DatagramSocket m_RecvHost = null;
	DatagramSocket m_handletransmit = null;
	public int SearchPort = 9000;
	public int waitport = 9002;
	private static int transmitport = 9004;
	public String localip = null;
	public boolean transmitting = false;

	private String saveuri = Environment.getExternalStorageDirectory()
			+ "/temp";

	Jni jni = null;

	int cameraPicPage;
	int cameraPicAvailableID;
	int cameraPicItemID;

	class RecvRequest extends Thread {
		public void run() {
			while (recvThreadFlag) {
				byte[] ba = new byte[1024];
				DatagramPacket packet = new DatagramPacket(ba, ba.length);
				try {
					if (m_RecvHost == null)
						m_RecvHost = new DatagramSocket(SearchPort);
					m_RecvHost.receive(packet);
					String message = new String(packet.getData());
					message = message.trim();
					String[] mm = message.split(";");
					if (mm[0].equals("1") && localip != null
							&& mm[1].equals(shareType)) {
						Log.e("localip", localip);
						DatagramSocket sck = null;
						try {
							sck = new DatagramSocket();
							InetAddress destadd = packet.getAddress();

							String content = "2;" + shareType + ";" + localip;
							byte[] baa = content.getBytes();
							DatagramPacket pack = new DatagramPacket(baa,
									baa.length, destadd, SearchPort);
							sck.send(pack);
							Log.e("getip", "send content localip" + content);
						} catch (SocketException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} // UDP，通过ServerPort端口发送消息
						catch (UnknownHostException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	class GetIP extends Thread {
		public void run() {
			while (recvThreadFlag) {
				Log.e("getip", "status:" + Start.netStatus);
				if (Start.ADHOC.equals(Start.netStatus))
					localip = jni.getLocalHost();
				else if (Start.WIFI.equals(Start.netStatus))
					localip = getLocalIpAddress();

				Log.e("getip", "localip:" + localip);

				if (localip != null)
					break;
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		public String getLocalIpAddress() {
			try {
				for (Enumeration<NetworkInterface> en = NetworkInterface
						.getNetworkInterfaces(); en.hasMoreElements();) {
					NetworkInterface intf = en.nextElement();
					for (Enumeration<InetAddress> enumIpAddr = intf
							.getInetAddresses(); enumIpAddr.hasMoreElements();) {
						InetAddress inetAddress = enumIpAddr.nextElement();
						if (!inetAddress.isLoopbackAddress()) {
							return inetAddress.getHostAddress().toString();
						}
					}
				}
			} catch (SocketException ex) {
				Log.e("WifiPreference IpAddress", ex.toString());
			}
			return null;
		}

	}

	DatagramSocket sck = null;

	private class HandleTramsmit extends Thread {
		public void run() {
			int port = 9002;
			String[] m = new String[] { "" };

			while (recvThreadFlag) {
				Log.e("wifioradhoc", "recvThreadFlag:" + recvThreadFlag);
				byte[] ba = new byte[1024];
				DatagramPacket packet = new DatagramPacket(ba, ba.length);
				try {
					if (m_handletransmit == null)
						m_handletransmit = new DatagramSocket(port);
					m_handletransmit.receive(packet);

					start = System.currentTimeMillis();
					Log.e("wifioradhoc", "start:" + start);
					String message = new String(packet.getData());
					message = message.trim();
					Log.v("renkai", "message=" + message);
					m = message.split(";");
					Log.v("renkai",
							"savepathnum=" + String.valueOf(m.length - 2));
					if (m[0].equals("1")) {
						Log.e("wifioradhoc", "recv message:" + message);
						savepath = new String[m.length - 2];
						for (int i = 0; i != savepath.length; i++) {
							savepath[i] = "/sdcard/calldir/free_"
									+ Start.getPageNum() + "/";
							savepath[i] += m[i + 2];
							Log.v("renkai", "savepath=" + savepath[i]);
						}
						DatagramSocket sck = null;
						try {
							sck = new DatagramSocket();
							InetAddress destadd = packet.getAddress();
							String content = "2";
							byte[] baa = content.getBytes();
							DatagramPacket packet1 = new DatagramPacket(baa,
									baa.length, destadd, port);
							sck.send(packet1);
							Log.e("wifioradhoc", "send 2!!!!");
						} catch (SocketException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} // UDP，通过ServerPort端口发送消息
						catch (UnknownHostException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else {
						if (m[0].equals("4")) {
							long end = System.currentTimeMillis();
							System.out.println("时间："
									+ String.valueOf((long) end));
							Log.v("renkai", "recv 4");

							String ip = packet.getAddress().toString();
							ip = ip.substring(1, ip.length());
							Log.v("renkai", "duifangip=" + ip);
							sservice.wgetfiles(savepath, ip, transmitport);

							new UpdateBar(cameraPicPage, cameraPicAvailableID,
									cameraPicItemID).start();
						}
					}

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	/*
	 * 收取单个文件 class HandleTramsmit extends Thread { public void run() {
	 * 
	 * while(recvThreadFlag){ byte[] ba = new byte[1024]; DatagramPacket packet
	 * = new DatagramPacket(ba,ba.length); try{ if(m_handletransmit == null)
	 * 
	 * m_handletransmit = new DatagramSocket(waitport);
	 * 
	 * m_handletransmit.receive(packet); String message = new
	 * String(packet.getData()); message = message.trim();
	 * if(message.equals("1") && localip != null){ Log.e("adhoc",
	 * "recv message:" + message); //DatagramSocket sck = null;
	 * 
	 * sck = new DatagramSocket(); InetAddress destadd = packet.getAddress();
	 * String content; if(transmitting){ content = "3"; byte[] baa =
	 * content.getBytes(); DatagramPacket pack = new
	 * DatagramPacket(baa,baa.length,destadd,waitport); sck.send(pack); } else {
	 * Message msg = new Message(); Bundle bun = new Bundle();
	 * bun.putString("ip", destadd.toString()); String string =
	 * (destadd.toString()).substring(1); Log.v("renkai",string);
	 * msg.setData(bun); //
	 * getContext().BeginTransmitHandler.sendMessage(msg);//询问是否开始传输
	 * //不用询问，直接开始传输
	 * 
	 * 
	 * // sckk = new DatagramSocket(); // InetAddress destadd1 =
	 * InetAddress.getByName(string); String content1 = "2";
	 * 
	 * byte[] baa = content1.getBytes(); DatagramPacket pack = new
	 * DatagramPacket(baa,baa.length,destadd,waitport); Log.e("adhoc",
	 * "before send------------------------------"); // sckk.send(pack);
	 * sck.send(pack);
	 * 
	 * Log.e("adhoc", "after send------------------------------");
	 * 
	 * transmitting = true;
	 * 
	 * 
	 * try { Thread.sleep(500); } catch (InterruptedException e) { // TODO
	 * Auto-generated catch block e.printStackTrace(); } // String content2 =
	 * "250"; // // byte[] baaa = content1.getBytes(); // DatagramPacket pack2 =
	 * new DatagramPacket(baaa,baaa.length,destadd,waitport); sck.send(pack);
	 * 
	 * sservice.wgetfile(saveuri, string, transmitport);
	 * 
	 * // tv.setText(getResources().getString(R.string.transmitting)); new
	 * UpdateBar(cameraPicPage,cameraPicAvailableID,cameraPicItemID).start();
	 * 
	 * 
	 * }
	 * 
	 * 
	 * } } catch (SocketException e) { // TODO Auto-generated catch block
	 * e.printStackTrace(); } catch (IOException e) { // TODO Auto-generated
	 * catch block e.printStackTrace(); } }//while }//run }//class
	 */

	public class UpdateBar extends Thread {

		int cameraPicPage;
		int cameraPicAvailableID;
		int cameraPicItemID;

		public UpdateBar(int page, int aid, int itemid) {
			this.cameraPicAvailableID = aid;
			this.cameraPicItemID = itemid;
			this.cameraPicPage = page;
		}

		public void run() {
			while (recvThreadFlag) {

				if (sservice.getProgress() == 100) {
					// 传输完毕,文件已经到/temp.jpg
					transmitting = false;
					Message msg = new Message();
					msg.arg1 = this.cameraPicPage;
					msg.arg2 = this.cameraPicAvailableID;
					msg.what = this.cameraPicItemID;
					updateCameraHandler.sendMessage(msg);
					break;
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	class send3PhotoShareThread extends Thread {
		public void send3PhotoShare() {
			DatagramSocket sck = null;
			try {
				sck = new DatagramSocket();
				InetAddress destadd = InetAddress.getByName("10.0.0.255");
				String content = "3;" + shareType + ";" + localip;
				byte[] ba = content.getBytes();
				DatagramPacket packet = new DatagramPacket(ba, ba.length,
						destadd, SearchPort);
				sck.send(packet);
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} // UDP，通过ServerPort端口发送消息
			catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		public void run() {
			send3PhotoShare();
		}
	};

	public void closeADHoc() {
		Log.e("adhoc", " call closeAdhoc");

		if (Start.ADHOC.equals(Start.netStatus)) {

			(new send3PhotoShareThread()).start();
			Log.e("adhoc", "closeAdhoc netStatus " + (Start.netStatus));

			Log.e("adhoc", "closeAdhoc jni " + (jni == null));
			if (jni != null) {
				jni.closeAdhoc();// 可以改放到用wifi网络时再关闭
				Log.e("adhoc", "closeAdhoc");
			}
			recvThreadFlag = false;
			firstTransformPicFromMobile = true;
			if (m_RecvHost != null)
				m_RecvHost.close();
			if (m_handletransmit != null)
				m_handletransmit.close();
			if (m_RecvHost != null)
				m_RecvHost = null;
			if (m_handletransmit != null)
				m_handletransmit = null;
		}

	}

	public void setWifiMode() {
		WifiOrAdhoc wifiOrAdhoc = new WifiOrAdhoc();
		wifiOrAdhoc.setWifiMode();
		Start.netStatus = Start.WIFI;
	}

	public void setAdhocMode() {
		WifiOrAdhoc wifiOrAdhoc = new WifiOrAdhoc();
		wifiOrAdhoc.setAdhocMode();
		Start.netStatus = Start.ADHOC;
	}

	public void systemScan() {
		Start.context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri
				.parse("file://" + Environment.getExternalStorageDirectory())));
	}

	// 截屏代码 caoheng 2015.12.15
	public boolean saveScreen() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss",
				Locale.US);
		String fname = "/sdcard/" + view.pageXML + ".png";
		view.invalidate();
		view.setDrawingCacheEnabled(true);
		view.buildDrawingCache();
		
		pingyuDisplayLayout.invalidate();
		pingyuDisplayLayout.setDrawingCacheEnabled(true);
		pingyuDisplayLayout.buildDrawingCache();
		
		
		Bitmap bitmap = view.getDrawingCache();
		Bitmap bitmap1 = pingyuDisplayLayout.getDrawingCache();
		
		Canvas canvas = new Canvas(bitmap);
		if(bitmap1!=null)canvas.drawBitmap(bitmap1, new Matrix(), null);
		if (bitmap != null) {
			System.out.println("bitmap got!");
			try {
				FileOutputStream out = new FileOutputStream(fname);
				bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
				System.out.println("file " + fname + "outputdone.");
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("bitmap is NULL!");
		}
		Uri data = Uri.parse("/storage/emulated/0");
		Start.context.sendBroadcast(new Intent(
				Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, data));
		return true;
	}

	// try {
	// if (!file.exists()) {//文件不存在则创建
	// file.createNewFile();
	// }
	// fos=new FileOutputStream(file,false);
	//
	// fos.write(doc.html().getBytes());//写入文件内容
	// fos.flush();
	// } catch (IOException e) {
	// System.err.println("文件创建失败");
	// }finally{
	// if (fos!=null) {
	// try {
	// fos.close();
	// } catch (IOException e) {
	// System.err.println("文件流关闭失败");
	// }
	// }
	// }
	// 2016.3.24保存批改结果xml
	public static void saveXML(File file) {
		FileOutputStream fos = null;
		try {
			if (!file.exists()) {// 文件不存在则创建
				file.createNewFile();
			}
			fos = new FileOutputStream(file, false);

			fos.write(doc.html().getBytes());// 写入文件内容
			fos.flush();
		} catch (IOException e) {
			System.err.println("文件创建失败");
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					System.err.println("文件流关闭失败");
				}
			}
		}
	}

}
