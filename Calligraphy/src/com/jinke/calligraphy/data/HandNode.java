package com.jinke.calligraphy.data;

import android.util.Log;

public class HandNode {

	private static final String TAG = "HandNode";
	
	public String HANDNODE_START = "<hwnode>";
	public String HANDNODE_END = "</hwnode>";
	public String INDEX_START = "<index>";
	public String INDEX_END = "</index>";
	public String ADDRESS_START = "<address>";
	public String ADDRESS_END = "</address>";
	public String NEXT_START = "<next>";
	public String NEXT_END = "</next>";
	public String AVAILABLE_START = "<available>";
	public String AVAILABLE_END = "</available>";
	
	
	public String HANDNODE = "hwnode";
	public String INDEX = "index";
	public String ADDRESS = "address";
	public String NEXT = "next";
	public String AVAILABLE = "available";
	
	
	String	index;				 //10bytes  0x00000000
	String	handitemAddress;	 //10bytes  0x00000000
	String	nextHandNodeAddress; //10bytes  0x00000000
	String	available;
	
	public int getLength(){
		int len =  HANDNODE_START.length() + HANDNODE_END.length() + INDEX_START.length() + INDEX_END.length() + 
				ADDRESS_START.length() + ADDRESS_END.length() + NEXT_START.length() + NEXT_END.length() + 
				AVAILABLE_START.length() + AVAILABLE_END.length() + 10 + 10 + 10 + 1;

		Log.i(TAG, "length:" + len);
		return len;
	}

	public String getIndex() {
		return index;
	}

	public void setIndex(String index) {
		this.index = index;
	}

	public String getHanditemAddress() {
		return handitemAddress;
	}

	public void setHanditemAddress(String handitemAddress) {
		this.handitemAddress = handitemAddress;
	}

	public String getNextHandNodeAddress() {
		return nextHandNodeAddress;
	}

	public void setNextHandNodeAddress(String nextHandNodeAddress) {
		this.nextHandNodeAddress = nextHandNodeAddress;
	}

	public String getAvailable() {
		return available;
	}

	public void setAvailable(String available) {
		this.available = available;
	}
	
	
}
