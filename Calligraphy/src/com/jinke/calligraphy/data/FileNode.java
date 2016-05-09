package com.jinke.calligraphy.data;

import android.util.Log;

public class FileNode {

	private static final String TAG = "FileNode";
	
	public String FILENODE_START = "<filenode>";
	public String FILENODE_END = "</filenode>";
	public String ADDRESS_START = "<address>";
	public String ADDRESS_END = "</address>";
	public String SUBNODE_START = "<subnode>";
	public String SUBNODE_END = "</subnode>";
	public String NEXT_START = "<next>";
	public String NEXT_END = "</next>";
	public String LAST_START = "<last>";
	public String LAST_END = "</last>";
	

	public String FILENODE = "filenode";
	public String ADDRESS = "address";
	public String SUBNODE = "subnode";
	public String NEXT = "next";
	public String LAST = "last";
	
	public String 	filePath;
	public String 	firstPageNodeAddress;	//10bytes	"0x00000000"
	public String	nextFileNodeAddress; 	//10bytes	"0x00000000"
	public String  	lastPageNodeAddress; 	//10bytes	"0x00000000"
	
	public FileNode(String filePath, String firstPageNodeAddress,
			String nextFileNodeAddress, String lastPageNodeAddress) {

		this.filePath = filePath;
		this.firstPageNodeAddress = firstPageNodeAddress;
		this.nextFileNodeAddress = nextFileNodeAddress;
		this.lastPageNodeAddress = lastPageNodeAddress;
	}
	
	
	public String getFilePath() {
		return filePath;
	}


	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}


	public String getFirstPageNodeAddress() {
		return firstPageNodeAddress;
	}


	public void setFirstPageNodeAddress(String firstPageNodeAddress) {
		this.firstPageNodeAddress = firstPageNodeAddress;
	}


	public String getNextFileNodeAddress() {
		return nextFileNodeAddress;
	}


	public void setNextFileNodeAddress(String nextFileNodeAddress) {
		this.nextFileNodeAddress = nextFileNodeAddress;
	}


	public String getLastPageNodeAddress() {
		return lastPageNodeAddress;
	}


	public void setLastPageNodeAddress(String lastPageNodeAddress) {
		this.lastPageNodeAddress = lastPageNodeAddress;
	}


	public int getLength(){
		int len = FILENODE_START.length() + FILENODE_END.length() + ADDRESS_START.length() +
				ADDRESS_END.length() + SUBNODE_START.length() + SUBNODE_END.length() +
				NEXT_START.length() + NEXT_END.length() + LAST_START.length() + LAST_END.length() +
				filePath.length() + 10 + 10 + 10;
		Log.i(TAG, "length:" + len);
		return len;
		
	}
	
	public int getNextPos(){
//		return FILENODE_START.length() /*+ ADDRESS_START.length() + filePath.length() + ADDRESS_END.length() +
//			SUBNODE_START.length() + 10 + SUBNODE_END.length() */ + NEXT_START.length();

		return FILENODE_START.length() + ADDRESS_START.length() + filePath.length() + ADDRESS_END.length() +
			SUBNODE_START.length() + 10 + SUBNODE_END.length() + NEXT_START.length();
	}

}
