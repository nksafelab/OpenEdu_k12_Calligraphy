package com.jinke.calligraphy.app.branch;

import android.graphics.Paint;
import android.util.Log;

/*
 * 毛笔点类
 */
public class CalliPoint extends BasePoint {
	
	public int 		life;
	public boolean 	startFlag;
	
	//public static final int SIZE_MAX = 50;
	public static int SIZE_MAX = (30);
	public static int SIZE_MIN_CONST = (5);
	public static int SIZE_MIN = SIZE_MIN_CONST;
	
	
	private static final String TAG = "CalliPoint";

	public static float SPREAD_FACTOR_MIN = 1.06f;
	public static float SPREAD_FACTOR_SMALL = 1.03f;
	public static float SPREAD_FACTOR_BIG = 1.02f;
	public static float SPREAD_FACTOR = 0.0f;
	public static float FILTER_FACTOR = 0.0f;

	
	public CalliPoint(){
		super();
		life = (10);
    	startFlag = false;
	}
	
	public CalliPoint(float x1, float y1, float s, float c[]){
		super(x1, y1, s, c);
		life = (10);
		startFlag = false;
	}
	
	public CalliPoint(float x1, float y1, float s, Paint p){
		super(x1, y1, s, p);
		life = (10);
		startFlag = false;
	}
	
	public CalliPoint(float x1, float y1, float s, float c[], int l){
		this(x1, y1, s, c);
		life = l;
	}
	
	 public void addSize(){
//			 size = ( (size >= SIZE_MAX) ? SIZE_MAX : (size*1.04f) );
		if(size < 3 * SIZE_MIN){
			 size = ( (size >= SIZE_MAX) ? SIZE_MAX : (size*(SPREAD_FACTOR_SMALL + SPREAD_FACTOR)) );
		}else{
			 size = ( (size >= SIZE_MAX) ? SIZE_MAX : (size*(SPREAD_FACTOR_BIG + SPREAD_FACTOR) ) );
		}

//		 size = ( (size >= SIZE_MAX) ? SIZE_MAX : (size + 0.015f) );
	 }
	
}
