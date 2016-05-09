package com.jinke.calligraphy.app.branch;

import hallelujah.cal.SingleWord;

import java.io.Serializable;
import java.util.Calendar;

import com.jinke.calligraphy.database.CalligraphyDB;
import com.jinke.mindmap.MindMapItem;
import com.jinke.single.BitmapCount;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.util.Log;

public class EditableCalligraphyItem implements Serializable{
	public static enum Types {ImageItem, CharsWithoutStroke, Space,EnSpace, EndofLine, CharsWithStroke, INVAL, VEDIO,AUDIO,Unkown};
	public static enum ItemStatus {NORMAL,RECYCLED,QUEUED};
	public ItemStatus itemStatus = ItemStatus.NORMAL;;
	public int MinHeight = 10;//作为行距使用，但是不应该在此设置，使用EditableCalligraphy里的linespace替代。 byjinyang
	public final static int MinWidth = 40;
	public Types type;
	CalligraphyStrokePath stroke;
	transient Bitmap charBitmap;
	int width;
	int height;
	int strokeWidth;
	int currentX;
	int currentY;
	Uri imageUri;
	long time;
	private SingleWord word;
	private boolean wifiOrAdhoc = false;	
	private boolean saved = true;
	private int itemId = 0;
	private Matrix matrix;
	private float flip_dst_x;
	private float flip_dst_y;
	private Bitmap playingBitmap;
	private Bitmap stopBitmap;
	
	private boolean isSpecial = false;
	private MindMapItem belongsItem = null;
	
	private int op_pos;
	public void setOpPos(int pos){
		this.op_pos = pos;
	}
	public int getOpPos(){
		return this.op_pos;
	}
	
	
	public MindMapItem getMindMapItem(){
		return belongsItem;
	}
	public void setMindMapItem(MindMapItem item){
		this.belongsItem = item;
	}
	public boolean isSpecial(){
		return isSpecial;
	}
	public void setSpecial(){
		isSpecial = true;
		Log.e("mindmap", itemId + " set special");
	}
	
	
	public ItemStatus getStatus(){
		return itemStatus;
	}
	public void setNomalStatus(String iden){
		
		if(charBitmap!= null && !charBitmap.isRecycled()){
			itemStatus = ItemStatus.NORMAL;
			Log.e("itemstatus", iden + " set NORMAL status");	
		}else{
			Log.e("itemstatus", iden + " set NORMAL status error");
			setRecycleStatus(iden);
		}
	}
	public void setRecycleStatus(String iden){
		
		if(charBitmap!= null && !charBitmap.isRecycled()){
			Log.e("itemstatus", iden + " set RECYCLED status error");	
		}else{
			itemStatus = ItemStatus.RECYCLED;
			Log.e("itemstatus", iden + " set RECYCLED status");
		}
			
	}
	public void setQueueStatus(String iden){
		Log.e("itemstatus", iden + " set QUEUED status");
		itemStatus = ItemStatus.QUEUED;
	}
	
	public void setStopBitmap(){
		this.stopBitmap = charBitmap;
	}
	public Bitmap getStopBitmap(){
		return this.stopBitmap;
	}
	public Bitmap getAudioPlayingBitmap(){
		if(playingBitmap == null){
			Canvas canvas = new Canvas();
			Paint p = new Paint();
			p.setTextSize(20);
			//playingBitmap = BitmapFactory.decodeResource(Start.context.getResources(), R.drawable.audio).copy(Config.ARGB_8888, true);
			playingBitmap = BitmapFactory.decodeResource(Start.context.getResources(), R.drawable.audio).copy(Config.ARGB_4444, true);
			canvas.setBitmap(playingBitmap);
			double dur = MediaPlayerUtil.getInstance().getDuration(imageUri)/1000;//s
			String duration = Math.floor(dur/60) + "分" + Math.ceil((dur/60 - Math.floor(dur/60))* 60) + "秒";
			canvas.drawText(duration, 145f, 30f, p);
			Log.e("media", "duration:" + duration);
		}
		return playingBitmap;
	}
	public void setFlipDstX(float x){
		this.flip_dst_x = x;
		CalligraphyDB.getInstance(Start.context).updatePicdstx(Start.getPageNum(), 3, getItemID(), x);
	}
	public float getFlipDstX(){
		return this.flip_dst_x;
	}
	
	public void setItemId(int id){
		this.itemId = id;
	}
	public int getItemID(){
		return itemId;
	}
	
	public EditableCalligraphyItem(){
		type=Types.Unkown;
		width=0;
		height=0;
		stroke=null;
		charBitmap=null;
		strokeWidth=5;
		currentX=currentY=0;
		time = Calendar.getInstance().getTimeInMillis();
		matrix = new Matrix();
	}
	public EditableCalligraphyItem(EditableCalligraphyItem item){
		type= item.type;
		width= item.width;
		height= item.height;
		stroke= item.stroke;
		charBitmap= item.charBitmap;
		strokeWidth= item.strokeWidth;
		currentX=currentY= item.currentX;
		time = item.time;
		matrix = item.matrix;
		word = item.word;
		saved = false;
		imageUri = item.imageUri;
		
	}
	
	public EditableCalligraphyItem(CalligraphyStrokePath path)
	{
		this();
		stroke = path;
		width =  path.getWidth();
		if(width < MinWidth)
			width = MinWidth;
		height = path.getHeight();
		if(height < MinHeight)
			height = MinHeight;
		type=Types.CharsWithStroke;
	}
	public EditableCalligraphyItem(Bitmap m)
	{
		this();
		if(m != null)
			this.charBitmap = m;
		else
			this.charBitmap = null;
		if(m != null){
			this.width = m.getWidth();
			this.height = m.getHeight();
		}
		this.type = Types.CharsWithoutStroke;
	}
	public EditableCalligraphyItem(Bitmap m,SingleWord word)
	{
		this();
		this.charBitmap = m;
		if(m != null){
			this.width = m.getWidth();
			this.height = m.getHeight();
		}
		this.type = Types.CharsWithoutStroke;
		this.word = word;
	}
	public EditableCalligraphyItem(Bitmap m , int MinHeight)
	{
		this();
		this.charBitmap = m;
		this.width = m.getWidth();
		this.height = m.getHeight();
		this.MinHeight = MinHeight;
		this.type = Types.CharsWithoutStroke;
	}
	public EditableCalligraphyItem(Bitmap m , int MinHeight,SingleWord word)
	{
		this();
		this.charBitmap = m;
		this.width = m.getWidth();
		this.height = m.getHeight();
		this.MinHeight = MinHeight;
		this.type = Types.CharsWithoutStroke;
		this.word = word;
	}
	
	public EditableCalligraphyItem(Bitmap m , int MinHeight,Uri imageUri)
	{
		this();
		this.charBitmap = m;
		this.width = m.getWidth();
		this.height = m.getHeight();
		this.MinHeight = MinHeight;
		this.imageUri = imageUri;
		this.type = Types.ImageItem;
	}
	
	public EditableCalligraphyItem(Types t)
	{
		this();
		this.type = t;
		if(t == Types.Space ){
			this.width = this.MinWidth;
			this.height = this.MinHeight;
		}else if( t == Types.EndofLine ){
			this.width = 0;
			this.height = this.MinHeight;
//			Log.e("endofline", "mini hight:" + this.MinHeight);
		}else if( t == Types.EnSpace ){
			this.width = this.MinWidth / 4;
			this.height = this.MinHeight;
		}
	}
	protected void generateBitmap(int strokewidth)
	{
		//TODO: generate the bitmap for the calligraphy
		return;
	}
	public void setStrokeWidth(int w)
	{
		this.strokeWidth = w;
		if(this.type == Types.CharsWithStroke)
		{
			this.generateBitmap(w);
		}
	}
	public Bitmap getCharBitmap()
	{
		if(this.charBitmap == null){
			if(this.type == Types.CharsWithStroke ){
				this.generateBitmap(6);
			}
			return this.charBitmap;
		}else{
			return charBitmap;
		}
	}
	public int getWidth()
	{
		if(type == Types.ImageItem){
			if(charBitmap == null){
				return 500;
			}
			if(width < 100)
				return 500;
		}
		return this.width;
	}
	public int getHeight()
	{
		if(type == Types.ImageItem){
			if(charBitmap == null){
				return 580;
			}
			if(height < 100)
				return 580;
		}
		return this.height;
	}
	public void setCurPos(int x, int y)
	{
		this.currentX = x;
		this.currentY = y;
//		Log.e("CurrentPos", "setCurPos: x:" +x + " y:" + y);
	}
	public int getCurPosX()
	{
		return this.currentX;
	}
	public int getCurPosY()
	{
		return this.currentY;
	}
	public Types getType() {
		return type;
	}
	public int getCharType(){
		if(type == Types.CharsWithoutStroke)
			return 0;
		if(type == Types.CharsWithStroke)
			return 1;
		if(type == Types.Space)
			return 2;
		if(type == Types.EndofLine)
			return 3;
		if(type == Types.INVAL)
			return 4;
		if(type == Types.EnSpace)
			return 5;
		if(type == Types.ImageItem)
			return 7;
		if(type == Types.AUDIO)
			return 8;
		if(type == Types.VEDIO)
			return 9;
		return 6;
	}
	
	public static Types getType(int t){
		switch (t) {
		case 0:
			return Types.CharsWithoutStroke;
		case 1:
			return Types.CharsWithStroke;
		case 2:
			return Types.Space;
		case 3:
			return Types.EndofLine;
		case 4:
			return Types.INVAL;
		case 5:
			return Types.EnSpace;
		case 7:
			return Types.ImageItem;
		case 8:
			return Types.AUDIO;
		case 9:
			return Types.VEDIO;
		default:
			return Types.Unkown;
		}
		
		
	}
	public void setType(Types type){
		this.type = type;
	}
	
	public void resetCharBitmap(Bitmap newBitmap,Matrix mmMateix,Uri newImageUri){
		
		if(newBitmap == null)
			return;
		if(charBitmap!= null && type != Types.AUDIO){
			charBitmap.recycle();
			BitmapCount.getInstance().recycleBitmap("EditableCalligraphyItem resetCharBitmap charBitmap");
		}
		charBitmap = newBitmap;
		
		if(type != Types.AUDIO)
			this.type = Types.ImageItem;
		width = charBitmap.getWidth();
		height = charBitmap.getHeight();
		
		if(newImageUri != null)
			this.imageUri = newImageUri;
		
		if(mmMateix != null)
			setMatrix(mmMateix);
		time = Calendar.getInstance().getTimeInMillis();
		setNotWifiOrAdhoc();
//		setNotSaved();
		Log.e("resetImage", "resetImage");
	}
	
	public void resetVideoUri(Bitmap newBitmap,Matrix mmMateix,Uri newImageUri){
		if(newBitmap == null)
			return;
		if(charBitmap!= null){
			charBitmap.recycle();
			BitmapCount.getInstance().recycleBitmap("EditableCalligraphyItem resetVideoUri charBitmap");
		}
		charBitmap = newBitmap;
		
		width = charBitmap.getWidth();
		height = charBitmap.getHeight();
		if(newImageUri != null)
			this.imageUri = newImageUri;
		setMatrix(mmMateix);
		time = Calendar.getInstance().getTimeInMillis();
		setNotSaved();
		Log.e("resetImage", "resetImage");
	}
	public void resetAudioUri(Bitmap newBitmap,Matrix mmMateix,Uri newImageUri){
		if(newBitmap == null)
			return;
		if(charBitmap!= null){
			charBitmap.recycle();
			BitmapCount.getInstance().recycleBitmap("EditableCalligraphyItem resetAudioUri charBitmap");	
		}
		charBitmap = newBitmap;
		
		width = charBitmap.getWidth();
		height = charBitmap.getHeight();
		if(newImageUri != null)
			this.imageUri = newImageUri;
		setMatrix(mmMateix);
		time = Calendar.getInstance().getTimeInMillis();
		setNotSaved();
		Log.e("resetImage", "resetImage");
	}
	
	public Uri getImageUri(){
		return imageUri;
	}
	public void setImageUri(Uri uri){
		this.imageUri = uri;
	}
	public long getTime() {
		return time;
	}
	public void setMatrix(Matrix m){
		Log.e("ispic", "itemid " + getItemID() + " setMatrix:" + m.toShortString());
		this.matrix.set(m);
	}
	public Matrix getMatrix(){
		return this.matrix;
	}

	public SingleWord getWord() {
		return word;
	}

	public void setWord(SingleWord word) {
		this.word = word;
	}

	public static int getMinwidth() {
		return MinWidth;
	}

	public int getMinHeight() {
		return MinHeight;
	}

	public void setMinHeight(int minHeight) {
		MinHeight = minHeight;
	}

	public void setCharBitmap(Bitmap charBitmap) {
		this.charBitmap = charBitmap;
	}
	public void setSaved(){
		saved = true;
	}
	public void setNotSaved(){
		saved = false;
	}
	public boolean getSaved(){
		return saved;
	}
	public boolean isWifiOrAdhoc(){
		return wifiOrAdhoc;
	}
	public void setWifiOrAdhoc(){
		wifiOrAdhoc = true;
	}
	public void setNotWifiOrAdhoc(){
		wifiOrAdhoc = false;
	}
	public void recycleBitmap(){
		if(charBitmap != null)
			charBitmap.recycle();
	}
}
