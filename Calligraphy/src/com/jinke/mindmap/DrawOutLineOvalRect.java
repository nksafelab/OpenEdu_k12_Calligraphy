package com.jinke.mindmap;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

public class DrawOutLineOvalRect implements DrawOutLine{


	public float ovallength;
	public Paint contentPaint;	
	public DrawOutLineOvalRect(float olength,Paint cp){
		this.ovallength = olength;
		this.contentPaint=cp;
	}
	public void setOvalLength(float olength){
		this.ovallength=olength;
	}
	public void setContentPaint(Paint cp){
		this.contentPaint=cp;
	}
	public void operate(LocateDot in, LocateDot out, Canvas c, Paint mp) {
		// TODO Auto-generated method stub	
		RectF rect_s =new RectF(in.x, in.y ,out.x,out.y);
		c.drawRoundRect(rect_s, ovallength, ovallength, contentPaint);
		RectF rect_f =new RectF(in.x, in.y ,out.x,out.y);
		c.drawRoundRect(rect_f, ovallength, ovallength, mp);
	}
	
}