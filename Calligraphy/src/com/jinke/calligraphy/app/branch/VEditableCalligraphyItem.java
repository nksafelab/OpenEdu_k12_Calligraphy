package com.jinke.calligraphy.app.branch;

import java.util.Calendar;

import com.jinke.mywidget.interpolator.EasingType.Type;

import hallelujah.cal.SingleWord;
import android.graphics.Bitmap;
import android.graphics.Path;

public class VEditableCalligraphyItem extends EditableCalligraphyItem{

	private Path mPath;
	private float Left_X;
	private float Right_X;
	private float Top_Y;
	private float Bottom_Y;
	private int mColor;
	
	public VEditableCalligraphyItem(){
		
		this.type = Types.CharsWithStroke;
	}
	
	
	public VEditableCalligraphyItem(Bitmap m){
		this();
		this.height = m.getHeight();
		this.width = m.getWidth();
		setCharBitmap(m);
	}
	public VEditableCalligraphyItem(Types t){
		super(t);
	}
	
	public Path getmPath() {
		return mPath;
	}

	public void setmPath(Path mPath) {
		this.mPath = mPath;
	}

	

	public float getLeft_X() {
		return Left_X;
	}

	public void setLeft_X(float left_X) {
		Left_X = left_X;
	}

	public float getRight_X() {
		return Right_X;
	}

	public void setRight_X(float right_X) {
		Right_X = right_X;
	}

	public float getTop_Y() {
		return Top_Y;
	}

	public void setTop_Y(float top_Y) {
		Top_Y = top_Y;
	}

	public float getBottom_Y() {
		return Bottom_Y;
	}

	public void setBottom_Y(float bottom_Y) {
		Bottom_Y = bottom_Y;
	}
	
	public int getmColor() {
		return mColor;
	}

	public void setmColor(int mColor) {
		this.mColor = mColor;
	}

	public void resetWidthHeight(){
		this.width = charBitmap.getWidth();
		this.height = charBitmap.getHeight();
		time = Calendar.getInstance().getTimeInMillis();
	}
	
	
	
}
