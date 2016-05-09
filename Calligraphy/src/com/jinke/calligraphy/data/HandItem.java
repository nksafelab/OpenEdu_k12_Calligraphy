package com.jinke.calligraphy.data;

import java.util.List;


public class HandItem {
	String 			url;
	int	 			offset;
	int 			page;
	int				userid;
	long 			time;
	List<HandImg> 	handImgList;
	
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public int getOffset() {
		return offset;
	}
	public void setOffset(int offset) {
		this.offset = offset;
	}
	public int getPage() {
		return page;
	}
	public void setPage(int page) {
		this.page = page;
	}
	public int getUserid() {
		return userid;
	}
	public void setUserid(int userid) {
		this.userid = userid;
	}
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}
	public List<HandImg> getHandImgList() {
		return handImgList;
	}
	public void setHandImgList(List<HandImg> handImgList) {
		this.handImgList = handImgList;
	}


	
}


