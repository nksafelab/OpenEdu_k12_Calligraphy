package com.jinke.calligraphy.app.control;

import java.util.LinkedList;

import com.jinke.calligraphy.app.branch.EditableCalligraphyItem;

import android.graphics.Bitmap;

public interface UpdateInterface {
	
	float recycle_line = 0;
	float recycle_bottom_line = 900;
	
	public void update(Bitmap m,boolean flip,LinkedList<EditableCalligraphyItem> charList);
	public void setCurrentPos(int pos);
}
