package com.jinke.kanbox;

public class DownloadEntity {

	String path;
	String destPath;
	
	public DownloadEntity(String path, String destPath) {
		super();
		this.path = path;
		this.destPath = destPath;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getDestPath() {
		return destPath;
	}
	public void setDestPath(String destPath) {
		this.destPath = destPath;
	}
	
	
}
