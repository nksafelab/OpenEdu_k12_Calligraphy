package com.jinke.calligraphy.app.branch;

public class ImageLimit {
	
	private static ImageLimit imageLimit = null;
	public static final int LIMIT_NUMBER = 20;
	private int imagenum = 0;
	
	public static ImageLimit instance(){
		if(imageLimit == null){
			imageLimit = new ImageLimit();
		}
		return imageLimit;
	}
	
	public void addImageCount(){
		imagenum ++;
	}
	public void deleteImageCount(){
		if(imagenum > 0)
			imagenum --;
	}
	
	public void resetImageCount(){
		imagenum = 0;
	}
	
	public boolean canInsertImage(){
		if(imagenum < LIMIT_NUMBER){
			imagenum ++;
			return true;
		}else{
			return false;
		}
	}

}
