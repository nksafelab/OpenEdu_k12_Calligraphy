package com.jinke.mindmap;

import com.jinke.single.LogUtil;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

public class DrawOutLineHalfRound implements DrawOutLine{
	float vertical_offset_percentage;
	float ovallength;
	public DrawOutLineHalfRound(float olength,float vopercentage){
		 this.ovallength = olength;//设置弧度
		 this.vertical_offset_percentage=vopercentage;//框的高度
	}
	public void setOvalLength(float olength){
		 this.ovallength = olength;//设置弧度
	}
	
	public void operate(LocateDot in, LocateDot out, Canvas c, Paint mp) {
		// TODO Auto-generated method stub
		LogUtil.getInstance().e("drawLine", "DrawOutLineHalfRound");
		LocateDot ovalf_l = new LocateDot(in.x-mp.getStrokeWidth(),out.y-ovallength);
		LocateDot ovalf_r = new LocateDot(in.x+ovallength,out.y);
		c.drawLine(in.x-mp.getStrokeWidth(),    in.y+Math.abs(in.y-out.y)*this.vertical_offset_percentage, ovalf_l.x,ovalf_l.y,mp);
		RectF rextf_l = new RectF(ovalf_l.x,2*ovalf_l.y-ovalf_r.y,2*ovalf_r.x-ovalf_l.x,ovalf_r.y);
		c.drawArc(rextf_l, 90, 90, false, mp);
		c.drawLine(ovalf_r.x, ovalf_r.y,out.x-ovallength,out.y,mp);
		
		RectF rectf_r = new RectF(out.x-2*ovallength,out.y-2*ovallength,out.x,out.y);
		c.drawArc(rectf_r, 0, 90, false, mp);
		c.drawLine(out.x,in.y+Math.abs(in.y-out.y)*this.vertical_offset_percentage, out.x,out.y-ovallength,mp);
		
	}
	
}