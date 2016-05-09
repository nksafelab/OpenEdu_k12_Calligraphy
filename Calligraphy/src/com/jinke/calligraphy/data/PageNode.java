package com.jinke.calligraphy.data;

import android.util.Log;

public class PageNode {

	private static final String TAG = "PageNode";
	
	public String PAGENODE_START = "<pagenode>";
	public String PAGENODE_END = "</pagenode>";
	public String ADDRESS_START = "<address>";
	public String ADDRESS_END = "</address>";
	public String SUBNODE_START = "<subnode>";
	public String SUBNODE_END = "</subnode>";
	public String NEXT_START = "<next>";
	public String NEXT_END = "</next>";
	public String AVAILABLE_START = "<available>";
	public String AVAILABLE_END = "</available>";

	public String PAGENODE = "pagenode";
	public String ADDRESS = "address";
	public String SUBNODE = "subnode";
	public String NEXT = "next";
	public String AVAILABLE = "available";
	
	
	String 	pageNum;				//10bytes  0x00000000
	String	firstHandNodeAddress;	//10bytes  0x00000000	
	String	nextPageNodeAddress;	//10bytes  0x00000000
	String	available;
	
	public int getLength(){
		int len =  PAGENODE_START.length() + PAGENODE_END.length() + ADDRESS_START.length() + ADDRESS_END.length() + 
				SUBNODE_START.length() + SUBNODE_END.length() + NEXT_START.length() + NEXT_END.length() + 
				AVAILABLE_START.length() + AVAILABLE_END.length() + 10 + 10 + 10 + 1; 
		Log.i(TAG, "length:" + len);
		return len;
	}

	public String getPageNum() {
		return pageNum;
	}

	public void setPageNum(String pageNum) {
		this.pageNum = pageNum;
	}

	public String getFirstHandNodeAddress() {
		return firstHandNodeAddress;
	}

	public void setFirstHandNodeAddress(String firstHandNodeAddress) {
		this.firstHandNodeAddress = firstHandNodeAddress;
	}

	public String getNextPageNodeAddress() {
		return nextPageNodeAddress;
	}

	public void setNextPageNodeAddress(String nextPageNodeAddress) {
		this.nextPageNodeAddress = nextPageNodeAddress;
	}

	public String getAvailable() {
		return available;
	}

	public void setAvailable(String available) {
		this.available = available;
	}
	
	
}
