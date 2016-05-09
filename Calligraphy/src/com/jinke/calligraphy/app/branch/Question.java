package com.jinke.calligraphy.app.branch;

public class Question {
	private int sPos;
	public  int right=0;//不能写static，否则一个对象里的值变了另一个会一起变
	public   int wrong=0;
	public   int weird=0;
	private String comment;

	public int getsPos() {
		return sPos;
	}
	public void setsPos(int sPos) {
		this.sPos = sPos;
	}

	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	

}