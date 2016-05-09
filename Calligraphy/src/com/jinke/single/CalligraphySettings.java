package com.jinke.single;

import com.jinke.calligraphy.app.branch.Start;
import com.jinke.calligraphy.font.MFont;

import android.content.SharedPreferences;
import android.util.Log;

public class CalligraphySettings {
	public static final String FILENAME 				= "parameter";
	public static final String PARAM_INK_SPREAD 		= "ink_spread";
    public static final String PARAM_ONTO_SCREEN_TIME 	= "onto_screen_time";
    public static final String PARAM_AUTO_UPLOAD_TIME 	= "auto_upload_time";
    public static final String PARAM_WORD_STYLE			= "word_style";
    public static final String PARAM_CALI				= "cali_style";
    public static final String PARAM_WORD_BASE			= "word_base_pad";
    public static final String PARAM_WORD_PAD			= "word_pad";
    public static final String PARAM_SAVE_STYLE			= "save_style";
	
    
	private static 	CalligraphySettings settings 	= null;
	private SharedPreferences 			preference 	= null;
	
	private int 		ink_spread_time		= 0;
	private int 		on_screen_time		= 0;
	private int 		upload_time 		= 0;
	private int			word_base_line		= 0;
	private int 		word_pad			= 0;
	private boolean 	word_style			= false;
	private boolean 	cali_open			= false;
	private boolean		saveStyle			= false;
	
	private MFont 		enFont				= null;
	private MFont 		chFont				= null;
	
	private CalligraphySettings(){
		Log.e("settings", "start == null" + (Start.instance == null));
		preference = Start.instance.getSharedPreferences(FILENAME,  android.content.Context.MODE_PRIVATE);
		
		ink_spread_time =	getInt(PARAM_INK_SPREAD);
		on_screen_time	=	getInt(PARAM_ONTO_SCREEN_TIME);
		upload_time		=	getInt(PARAM_AUTO_UPLOAD_TIME);
		word_base_line	=	getInt(PARAM_WORD_BASE);
		word_pad		=	getInt(PARAM_WORD_PAD);
		word_style		=	getBoolean(PARAM_WORD_STYLE);
		cali_open		=	getBoolean(PARAM_CALI);
		saveStyle		=	getBoolean(PARAM_SAVE_STYLE);
		Log.e("clip", "get boolean from share :" + saveStyle);
	}
	public static CalligraphySettings getInstance(){
		if(settings == null){
			settings = new CalligraphySettings();
		}
		return settings;
	}
	
	
	//操作SharePreference
	private void putInt(final String TAG,int value){
		preference.edit().putInt(TAG, value).commit();
	}
	private void putBoolean(final String TAG,boolean value){
		preference.edit().putBoolean(TAG, value).commit();
	}
	private void putString(final String TAG,String value){
		preference.edit().putString(TAG, value).commit();
	}
	private void putFloat(final String TAG,float value){
		preference.edit().putFloat(TAG, value).commit();
	}
	/**
	 * 配置文件不存在，默认返回:-1
	 * @param TAG
	 * @return
	 */
	private int getInt(final String TAG){
		return preference.getInt(PARAM_AUTO_UPLOAD_TIME, -1);
	}
	/**
	 * 配置文件不存在，默认返回false
	 * @param TAG
	 * @return
	 */
	private boolean getBoolean(final String TAG){
		return preference.getBoolean(PARAM_SAVE_STYLE, false);
	}
	private String getString(final String TAG){
		return preference.getString(TAG, "");
	}
	private float getFloat(final String TAG){
		return preference.getFloat(TAG, -1f);
	}
	
	//get set
	public MFont getChFont() {
		return chFont;
	}
	public void setChFont(MFont chFont) {
		this.chFont = chFont;
	}
	public MFont getEnFont() {
		return enFont;
	}
	public void setEnFont(MFont enFont) {
		this.enFont = enFont;
	}
	public boolean getSaveStyle() {
		return saveStyle;
	}
	public void setSaveStyle(boolean saveStyle) {
		this.saveStyle = saveStyle;
		putBoolean(PARAM_SAVE_STYLE, saveStyle);
	}
	public int getInk_spread_time() {
		return ink_spread_time;
	}
	public void setInk_spread_time(int ink_spread_time) {
		this.ink_spread_time = ink_spread_time;
		putInt(PARAM_INK_SPREAD, this.ink_spread_time);
	}
	public int getOn_screen_time() {
		return on_screen_time;
	}
	public void setOn_screen_time(int on_screen_time) {
		this.on_screen_time = on_screen_time;
		putInt(PARAM_ONTO_SCREEN_TIME, on_screen_time);
	}
	public int getUpload_time() {
		return upload_time;
	}
	public void setUpload_time(int upload_time) {
		this.upload_time = upload_time;
		putInt(PARAM_AUTO_UPLOAD_TIME, upload_time);
	}
	public int getWord_base_line() {
		return word_base_line;
	}
	public void setWord_base_line(int word_base_line) {
		this.word_base_line = word_base_line;
		putInt(PARAM_WORD_BASE, word_base_line);
	}
	public int getWord_pad() {
		return word_pad;
	}
	public void setWord_pad(int word_pad) {
		this.word_pad = word_pad;
		putInt(PARAM_WORD_PAD, word_pad);
	}
	public boolean getWord_style() {
		return word_style;
	}
	public void setWord_style(boolean word_style) {
		this.word_style = word_style;
		putBoolean(PARAM_WORD_STYLE, word_style);
	}
	public boolean getCali_open() {
		return cali_open;
	}
	public void setCali_open(boolean cali_open) {
		this.cali_open = cali_open;
		putBoolean(PARAM_CALI, cali_open);
	}
	
}
