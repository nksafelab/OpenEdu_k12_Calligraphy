package com.jinke.calligraphy.data;

import android.graphics.Bitmap;
import android.graphics.Rect;

public class Img {
	
	int 	type;
	int 	size;
	int 	id;
	int 	x;
	int 	y;
	int 	width;
	int 	height;
	int 	compact;
	int 	bitcount;
	int 	length;
	int 	kind;
	int 	format;
	int 	pagecontrol;
	Rect	imgpart;
	Bitmap	bitmap;
	
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getX() {
		return x;
	}
	public void setX(int x) {
		this.x = x;
	}
	public int getY() {
		return y;
	}
	public void setY(int y) {
		this.y = y;
	}
	public int getWidth() {
		return width;
	}
	public void setWidth(int width) {
		this.width = width;
	}
	public int getHeight() {
		return height;
	}
	public void setHeight(int height) {
		this.height = height;
	}
	public int getCompact() {
		return compact;
	}
	public void setCompact(int compact) {
		this.compact = compact;
	}
	public int getBitcount() {
		return bitcount;
	}
	public void setBitcount(int bitcount) {
		this.bitcount = bitcount;
	}
	public int getLength() {
		return length;
	}
	public void setLength(int length) {
		this.length = length;
	}
	public int getKind() {
		return kind;
	}
	public void setKind(int kind) {
		this.kind = kind;
	}
	public int getFormat() {
		return format;
	}
	public void setFormat(int format) {
		this.format = format;
	}
	public int getPagecontrol() {
		return pagecontrol;
	}
	public void setPagecontrol(int pagecontrol) {
		this.pagecontrol = pagecontrol;
	}
	public Rect getImgpart() {
		return imgpart;
	}
	public void setImgpart(Rect imgpart) {
		this.imgpart = imgpart;
	}
	public Bitmap getBitmap() {
		return bitmap;
	}
	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}
	

}
