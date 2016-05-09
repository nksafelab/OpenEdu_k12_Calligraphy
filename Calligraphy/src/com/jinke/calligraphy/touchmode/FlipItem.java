package com.jinke.calligraphy.touchmode;

import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Rect;

public class FlipItem {

	int id;
	int type;
	boolean end;
	String name;
	Rect textRect;
	int textSize = 45;
	boolean touch = false;
	boolean inside = false;
	boolean deapper = false;
	Bitmap smallBitmap;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public Bitmap getSmallBitmap() {
		return smallBitmap;
	}
	public void setSmallBitmap(Bitmap smallBitmap) {
		this.smallBitmap = smallBitmap;
	}
	public boolean isDeapper() {
		return deapper;
	}
	public void setDeapper(boolean deapper) {
		this.deapper = deapper;
	}
	public boolean isInside() {
		return inside;
	}
	public void setInside(boolean inside) {
		this.inside = inside;
	}
	List<FlipItem> right_Second_ItemList;
	
	public List<FlipItem> getRight_Second_ItemList() {
		return right_Second_ItemList;
	}
	public void setRight_Second_ItemList(List<FlipItem> right_Second_ItemList) {
		this.right_Second_ItemList = right_Second_ItemList;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public boolean isEnd() {
		return end;
	}
	public void setEnd(boolean end) {
		this.end = end;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Rect getTextRect() {
		return textRect;
	}
	public void setTextRect(Rect textRect) {
		this.textRect = textRect;
	}
	public int getTextSize() {
		if(isTouch() || isInside())
			return textSize + 20;
		else
			return textSize;
	}
	public void setTextSize(int textSize) {
		this.textSize = textSize;
	}
	public boolean isTouch() {
		return touch;
	}
	public void setTouch(boolean touch) {
		this.touch = touch;
	}
	
	
	
}
