package com.jinke.calligraphy.app.branch;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;


public class ParametersDialog {
	private static final String TAG = "ParametersDialog";
	public static final String FILENAME = "parameter";
	
	private Activity mActivity;
    private AlertDialog parameterDialog;
    private SeekBar inkSpreadSeekbar;
    private SeekBar ontoScreenTimeSeekbar;
    private SeekBar autoSaveTimeSeekbar;
    private SeekBar autoUploadTimeSeekbar;
    private TextView ontoScreenTimeTextView;
    private TextView autoUploadTimeTextView;
    private TextView wordPadTextView;
    private TextView wordBasePadTextView;
    private RadioGroup wordStyleRadioGroup;
    private RadioGroup caliRadioGroup;
    private Button basePadAddButton;
    private Button wordPadAddButton;
    private Button basePadDelButton;
    private Button wordPadDelButton;
    

    public static final String PARAM_INK_SPREAD 		= "ink_spread";
    public static final String PARAM_ONTO_SCREEN_TIME 	= "onto_screen_time";
    public static final String PARAM_AUTO_SAVE_TIME 	= "auto_save_time";
    public static final String PARAM_AUTO_UPLOAD_TIME 	= "auto_upload_time";
    
    public static final String PARAM_WORD_STYLE		= "word_style";
    public static final String PARAM_CALI		= "cali_style";
    public static final String PARAM_WORD_BASE		= "word_base_pad";
    public static final String PARAM_WORD_PAD		= "word_pad";
    
    
    
    private PasswdBtnClickListener dialogListener;
    private SeekBar.OnSeekBarChangeListener seekbarsListener;
    private RadioGroup.OnCheckedChangeListener radioCheckedChangeListener;
	
    private View view;
    
    public static  boolean BY_WIDTH = true;
    public boolean CaliOpen = false;
    
    private static long minOntoScreenTime = 0;
    private static long maxOntoScreenTime = 2000;
    public 	static final int DEFAULT_ONSCREEN_TIME = 340;
    public  static long ontoScreenTimeFactor = (maxOntoScreenTime - minOntoScreenTime) / 100;
    
    public static long minAutoUploadTime = 10;
    public static long maxAutoUploadTime = 30;
    public static double autoUploadTimeFactor = (maxAutoUploadTime - minAutoUploadTime) * 1.0 / 100;

    private static int min_word_pad = 0;
    private static int max_word_pad = 20;
    private static int min_base_word_pad = 0;
    private static int max_base_word_pad = 20;
    private int wordPad = 0;
    private int baseWordPad = 0;
    
	
	public ParametersDialog(Activity activity) {
		mActivity = activity;
		AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
		parameterDialog = builder.create();
 	   	
 	   	initSeekbars();
 	   	
 	   	restoreInkSpread();
 	   	restoreOntoScreenTime();
 	   	restoreAutoUploadTime();
 	   	
 	   	restoreWordStyle();
 	   	restoreWordPad();
 	   	restoreWordBasePad();
        
 	   	dialogListener = new PasswdBtnClickListener();
 	   	parameterDialog.setInverseBackgroundForced(true);
 	   	parameterDialog.setView(view);
 	  	parameterDialog.setTitle("参数设置");
 	  	parameterDialog.setButton(DialogInterface.BUTTON_POSITIVE, "确定", dialogListener);

	}
	
    

	public void create() {
        Log.i(TAG, "create");
        parameterDialog.show();
    }
    
    void initSeekbars() {
    	
 	   	view = mActivity.getLayoutInflater().inflate(R.layout.parameters_setter, null);
 	   	seekbarsListener = new SeekBarsListener();
 	   	
 	   
 	   	
 	   	ontoScreenTimeTextView = (TextView) view.findViewById(R.id.onto_screen_textview);
 	   	autoUploadTimeTextView = (TextView) view.findViewById(R.id.auto_upload_textview); 
    	inkSpreadSeekbar = (SeekBar) view.findViewById(R.id.ink_spread_seekbar);
 	    ontoScreenTimeSeekbar = (SeekBar) view.findViewById(R.id.onto_screen_seekbar);
 	    autoSaveTimeSeekbar = (SeekBar) view.findViewById(R.id.auto_save_seekbar);
 	    autoUploadTimeSeekbar = (SeekBar) view.findViewById(R.id.auto_upload_seekbar);
 	    wordBasePadTextView = (TextView) view.findViewById(R.id.display_parameters_base_pad_textView);
 	    wordPadTextView = (TextView) view.findViewById(R.id.display_parameters_word_pad_textView);
 	    wordStyleRadioGroup = (RadioGroup) view.findViewById(R.id.display_radio_group);
 	    caliRadioGroup = (RadioGroup) view.findViewById(R.id.cali_radio_group);
 	    
 	    Log.e("NULL", "radioGroup null:	" + (wordStyleRadioGroup == null));
 	    basePadAddButton = (Button) view.findViewById(R.id.display_parameters_base_pad_add_button);
 	    wordPadAddButton = (Button) view.findViewById(R.id.display_parameters_word_pad_add_button);
 	    basePadDelButton = (Button) view.findViewById(R.id.display_parameters_base_pad_delete_button);
	    wordPadDelButton = (Button) view.findViewById(R.id.display_parameters_word_pad_delete_button);
 	    
 	    basePadAddButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				baseWordPad = Integer.parseInt(wordBasePadTextView.getText().toString());
				if(baseWordPad <max_base_word_pad){
					baseWordPad++;
					wordBasePadTextView.setText(baseWordPad+"");
				}
			}
		});
 	    
 	   wordPadAddButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				wordPad = Integer.parseInt(wordPadTextView.getText().toString());
				if(wordPad < max_word_pad){
					wordPad++;
					wordPadTextView.setText(wordPad+"");
				}
			}
		});
 	  basePadDelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				baseWordPad = Integer.parseInt(wordBasePadTextView.getText().toString());
				if(baseWordPad > min_base_word_pad){
					baseWordPad--;
					wordBasePadTextView.setText(baseWordPad+"");
				}
			}
		});
	    
	   wordPadDelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				wordPad = Integer.parseInt(wordPadTextView.getText().toString());
				if(wordPad > min_word_pad){
					wordPad--;
					wordPadTextView.setText(wordPad+"");
				}
			}
		});
	   	radioCheckedChangeListener = new MyRadioCheckChangeListener();
 	   	inkSpreadSeekbar.setOnSeekBarChangeListener(seekbarsListener);
 	   	ontoScreenTimeSeekbar.setOnSeekBarChangeListener(seekbarsListener);
 	   	autoSaveTimeSeekbar.setOnSeekBarChangeListener(seekbarsListener);
 	   	autoUploadTimeSeekbar.setOnSeekBarChangeListener(seekbarsListener);
 	   	wordStyleRadioGroup.setOnCheckedChangeListener(radioCheckedChangeListener);
 	   	caliRadioGroup.setOnCheckedChangeListener(radioCheckedChangeListener);
    }
    
    void restoreInkSpread() {
    	SharedPreferences settings = mActivity.getSharedPreferences(FILENAME,  android.content.Context.MODE_PRIVATE);
		int progress = settings.getInt(PARAM_INK_SPREAD, -1);
		if (progress != -1) {
           inkSpreadSeekbar.setProgress(progress);
			CalliPoint.SPREAD_FACTOR = (progress - 50) * 0.001f;
			CalliPoint.FILTER_FACTOR = (progress) * 0.03f;
		}
    }

    void restoreOntoScreenTime() {
    	SharedPreferences settings = mActivity.getSharedPreferences(FILENAME,  android.content.Context.MODE_PRIVATE);
		int progress = settings.getInt(PARAM_ONTO_SCREEN_TIME, -1);
		if (progress != -1)
			ontoScreenTimeSeekbar.setProgress(progress);
		else
			ontoScreenTimeSeekbar.setProgress((int)(DEFAULT_ONSCREEN_TIME / ontoScreenTimeFactor));
    }
    
    void restoreAutoUploadTime() {
    	SharedPreferences settings = mActivity.getSharedPreferences(FILENAME,  android.content.Context.MODE_PRIVATE);
		int progress = settings.getInt(PARAM_AUTO_UPLOAD_TIME, -1);
		if (progress != -1)
			autoUploadTimeSeekbar.setProgress(progress);
		else
			autoUploadTimeSeekbar.setProgress((int)(Start.auto_upload_time / autoUploadTimeFactor));
    }
    
    private void restoreWordBasePad() {
    	SharedPreferences settings = mActivity.getSharedPreferences(FILENAME,  android.content.Context.MODE_PRIVATE);
    	baseWordPad = settings.getInt(PARAM_WORD_PAD, EditableCalligraphy.basePad);
    	wordBasePadTextView.setText(baseWordPad+ "");
	}

	private void restoreWordPad() {
		SharedPreferences settings = mActivity.getSharedPreferences(FILENAME,  android.content.Context.MODE_PRIVATE);
		wordPad = settings.getInt(PARAM_WORD_PAD, EditableCalligraphy.HMargin);
		wordPadTextView.setText(wordPad+"");
	}

	private void restoreWordStyle() {
		SharedPreferences settings = mActivity.getSharedPreferences(FILENAME,  android.content.Context.MODE_PRIVATE);
		BY_WIDTH = settings.getBoolean(PARAM_WORD_STYLE, true);
		CaliOpen = settings.getBoolean(PARAM_CALI, false);
		if(BY_WIDTH)
			wordStyleRadioGroup.check(R.id.display_radio_byWidth);
		else
			wordStyleRadioGroup.check(R.id.display_radio_byHeight);
		
		if(CaliOpen)
			caliRadioGroup.check(R.id.cali_a);
		else
			caliRadioGroup.check(R.id.cali_b);
	}
    
	class MyRadioCheckChangeListener implements RadioGroup.OnCheckedChangeListener{

		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			switch (checkedId) {
			case R.id.display_radio_byHeight:
				BY_WIDTH = false;
				break;
			case R.id.display_radio_byWidth:
				BY_WIDTH = true;
				break;
			case R.id.cali_a:
				CaliOpen = true;
				Log.e("uuuu", "set true");
				break;
			case R.id.cali_b:
				CaliOpen = false;
				Log.e("uuuu", "set true");
				break;
			default:
				BY_WIDTH = true;
				CaliOpen = false;
				break;
			}
		}
	}
	
	class SeekBarsListener implements SeekBar.OnSeekBarChangeListener {

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			// TODO Auto-generated method stub
			if(seekBar == inkSpreadSeekbar) {
			}
			if(seekBar == ontoScreenTimeSeekbar) {
				ontoScreenTimeTextView.setText(parameterDialog.getContext().getResources().getString(R.string.onto_screen_time) + ":" + 
						progress * ontoScreenTimeFactor * 1.0 / 1000 + "秒");
			}
			if(seekBar == autoSaveTimeSeekbar) {
				
			}
			if(seekBar == autoUploadTimeSeekbar) {
				autoUploadTimeTextView.setText(parameterDialog.getContext().getResources().getString(R.string.auto_upload_time) + ":" + 
						(Math.round(minAutoUploadTime + progress * autoUploadTimeFactor)) + "分");
			}
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
			Log.i(TAG, "onStartTrackingTouch");
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
			Log.i(TAG, "onStopTrackingTouch");
			if(seekBar == inkSpreadSeekbar) {
				int progress = seekBar.getProgress();
				CalliPoint.SPREAD_FACTOR = (progress - 50) * 0.001f;
				CalliPoint.FILTER_FACTOR = (progress) * 0.03f;
				SharedPreferences settings = mActivity.getSharedPreferences(FILENAME,  android.content.Context.MODE_PRIVATE);
				settings.edit().putInt(PARAM_INK_SPREAD, progress).commit();
			}
			if(seekBar == ontoScreenTimeSeekbar) {
				int progress = seekBar.getProgress();
				if(progress == 0) progress = 1;
				CursorDrawBitmap.millisInFuture = progress * ontoScreenTimeFactor;
				CursorDrawBitmap.countDownInterval = CursorDrawBitmap.millisInFuture;
				Start.c.view.cursorBitmap.rebuildTimer();
				SharedPreferences settings = mActivity.getSharedPreferences(FILENAME,  android.content.Context.MODE_PRIVATE);
				settings.edit().putInt(PARAM_ONTO_SCREEN_TIME, progress).commit();
				Log.i(TAG, "ontoScreenTimeSeekbar:" + seekBar.getProgress());
			}
			if(seekBar == autoSaveTimeSeekbar) {

				Log.i(TAG, "autoSaveTimeSeekbar:" + seekBar.getProgress());
			}
			if(seekBar == autoUploadTimeSeekbar) {
				
				int progress = seekBar.getProgress();
				SharedPreferences settings = mActivity.getSharedPreferences(FILENAME,  android.content.Context.MODE_PRIVATE);
				settings.edit().putInt(PARAM_AUTO_UPLOAD_TIME, progress).commit();
				Start.auto_upload_time = minAutoUploadTime + progress * autoUploadTimeFactor;
				Log.e("autoupload", "set auto_upload_time:" + Start.auto_upload_time);
				Start.kanboxUploadHandler.removeMessages(1);
				Start.kanboxUploadHandler.sendEmptyMessageDelayed(1, (long)(Start.auto_upload_time * 60 * 1000));
				Log.i(TAG, "autoUploadTimeSeekbar:" + seekBar.getProgress());
			}
		} 
		
	}
	
    class PasswdBtnClickListener implements DialogInterface.OnClickListener {

 		@Override
 		public void onClick(DialogInterface arg0, int which) {
 			// TODO Auto-generated method stub

 			switch(which) {
 			case DialogInterface.BUTTON_POSITIVE:
 				EditableCalligraphy.HMargin = wordPad;
 				EditableCalligraphy.basePad = baseWordPad;
 				EditableCalligraphy.BY_WIDTH = BY_WIDTH;
 				
 				CalliPointsImpl.penStat = CaliOpen;
 				
 				Start.c.view.cursorBitmap.updateHandwriteState();
 				SharedPreferences settings = mActivity.getSharedPreferences(FILENAME,  android.content.Context.MODE_PRIVATE);
				settings.edit()
				.putInt(PARAM_WORD_BASE, baseWordPad)
				.putInt(PARAM_WORD_PAD, wordPad)
				.putBoolean(PARAM_CALI, CaliOpen)
				.putBoolean(PARAM_WORD_STYLE, BY_WIDTH)
				.commit();
				
				Log.e("dddd", "style:" + BY_WIDTH + " word_base:" + baseWordPad + " pad:" + wordPad + " caliOpen:" + CaliOpen);
 				break;
 			case DialogInterface.BUTTON_NEUTRAL:
 				break;
 			}
 		}
     	
     }
}