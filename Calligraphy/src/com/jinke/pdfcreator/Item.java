package com.jinke.pdfcreator;

public class Item {
	private String name;//文件名
	private String type;//字体、回车和无法识别的图片  char enter null
	private int index;//字体的索引
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
}
