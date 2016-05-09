package com.jinke.mindmap;

import com.jinke.single.LogUtil;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

public class DrawOutLineHalfLeft implements DrawOutLine{

	float vertical_offset_percentage;
	float ovallength;
	public DrawOutLineHalfLeft(float olength,float vopercentage){
		 this.ovallength = olength;//设置弧度
		 this.vertical_offset_percentage=vopercentage;//框的高度
	}
	public void setOvalLength(float olength){
		 this.ovallength = olength;//设置弧度
	}
	public void operate(LocateDot in, LocateDot out, Canvas c, Paint mp) {
		// TODO Auto-generated method stub
		LogUtil.getInstance().e("drawLine", "DrawOutLineHalfLeft");
		LocateDot ovalf_l = new LocateDot(in.x,out.y-ovallength);
		LocateDot ovalf_r = new LocateDot(in.x+ovallength,out.y);
		c.drawLine(in.x,  in.y+Math.abs(in.y-out.y)*this.vertical_offset_percentage, ovalf_l.x,ovalf_l.y,mp);
		RectF ovalf = new RectF(ovalf_l.x,2*ovalf_l.y-ovalf_r.y,2*ovalf_r.x-ovalf_l.x,ovalf_r.y);
		c.drawArc(ovalf, 90, 90, false, mp);
		c.drawLine(ovalf_r.x, ovalf_r.y,out.x,out.y,mp);
	}
	
}