package com.jinke.calligraphy.app.branch;

import android.graphics.Paint;

/*
 * 硬笔点类
 */
public class HardPoint extends BasePoint{
	
	public HardPoint(){
		super();
	}
	
	public HardPoint(float x1, float y1, float s, float c[]){
		super(x1, y1, s, c);
	}
	
	public HardPoint(float x1, float y1, float s, Paint p){ 
		super(x1, y1, s, p);
	}
}
