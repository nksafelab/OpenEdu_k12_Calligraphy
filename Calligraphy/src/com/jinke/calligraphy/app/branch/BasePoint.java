package com.jinke.calligraphy.app.branch;

import android.graphics.Paint;

public class BasePoint {
	
	public float x;
	public float y;

    public	float 	size;
    public	float 	color[];
    
    public BasePoint(){
    	 color = new float[4];
    }
    
    public BasePoint(float x1, float y1, float s, float c[]){
    	this();
    	x = x1;
    	y = y1;
    	size = s;
    	System.arraycopy(c, 0, color, 0, 4);
    }
    
    public BasePoint(float x1, float y1, float s, Paint p){
    	this();
    	x = x1;
    	y = y1;
    	size = s;
    	for(int i=0;i<4;i++){
    		color[i] = (p.getColor() >> (32 - (i+1) * 8) ) & 0xff;
    	}
    }
}


