package com.jinke.mindmap;

import com.jinke.single.LogUtil;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

public class DrawOutLineHalfRight implements DrawOutLine{
	float ovallength;
	float vertical_offset_percentage;
	public DrawOutLineHalfRight(float olength,float vopercentage){
		 this.ovallength = olength;//设置弧度
		 this.vertical_offset_percentage=vopercentage;//框的高度
	}
	public void setOvalLength(float olength){
		 this.ovallength = olength;//设置弧度
	}
	public void operate(LocateDot in, LocateDot out, Canvas c, Paint mp) {
		// TODO Auto-generated method stub
		LogUtil.getInstance().e("drawLine", "DrawOutLineHalfRight");
		c.drawLine(in.x-mp.getStrokeWidth(), out.y, out.x-ovallength,out.y,mp);
		
		RectF rectf_r = new RectF(out.x-2*ovallength,out.y-2*ovallength,out.x,out.y);
		c.drawArc(rectf_r, 0, 90, false, mp);
		c.drawLine(out.x,in.y+Math.abs(in.y-out.y)*this.vertical_offset_percentage, out.x,out.y-ovallength,mp);
	}
	
}