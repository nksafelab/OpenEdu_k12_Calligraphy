
package com.jinke.mindmap;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

public class DrawLineOvalFold implements DrawLine{

	public float ovallength ;          //弧度大小（0-15）可以通过比例放缩
	public float bracketlength ;          //弧度大小（0-15）可以通过比例放缩
	public float lrPercentage;         //折点位置（通过比例确定（取值范围0-1））
	public DrawLineOvalFold(float olength,float blength, float percentage){
		this.ovallength=olength;
		this.bracketlength=blength;
		this.lrPercentage = percentage;
	}
	public void SetOvalPara(float olength,float blength, float percentage){
		this.ovallength=olength;
		this.bracketlength=blength;
		this.lrPercentage = percentage;
	}
	public void operate(LocateDot from, LocateDot to, Canvas c, Paint mp,float arrawWidth) {
		// TODO Auto-generated method stub
		LocateDot ovalf_l = new LocateDot();
		LocateDot ovalf_r = new LocateDot();
		LocateDot bracketf_l = new LocateDot();
		LocateDot bracketf_r = new LocateDot();
		//连接线
		if( from.y == to.y ){
			c.drawLine(from.x, from.y,to.x,to.y,mp);
		}
		else{
			if(from.y>to.y){
				ovalf_l.setLocateDot(from.x+Math.abs((from.x-to.x)*lrPercentage), to.y+ovallength);
				ovalf_r.setLocateDot(from.x+Math.abs((from.x-to.x)*lrPercentage)+ovallength, to.y);
				
				bracketf_l.setLocateDot(from.x+Math.abs((from.x-to.x)*lrPercentage)-bracketlength, from.y);
				bracketf_r.setLocateDot(from.x+Math.abs((from.x-to.x)*lrPercentage), from.y-bracketlength);
				
				c.drawLine(from.x,    from.y,    bracketf_l.x, bracketf_l.y,   mp);
				c.drawLine(bracketf_r.x, bracketf_r.y,    ovalf_l.x, ovalf_l.y,mp);
				c.drawLine(ovalf_r.x, ovalf_r.y, to.x,      to.y,     mp);
				
				RectF bracketf = new RectF(bracketf_r.x-2*bracketlength,bracketf_l.y-2*bracketlength,bracketf_r.x,bracketf_l.y);
				c.drawArc(bracketf, 0, 90, false, mp);
				
			    RectF ovalf = new RectF(ovalf_l.x,ovalf_r.y,2*ovalf_r.x-ovalf_l.x,2*ovalf_l.y-ovalf_r.y);
				c.drawArc(ovalf, 180, 90, false, mp);
				
			}
			else{
				ovalf_l.setLocateDot(from.x+Math.abs((from.x-to.x)*lrPercentage), to.y-ovallength);
				ovalf_r.setLocateDot(from.x+Math.abs((from.x-to.x)*lrPercentage)+ovallength, to.y);
				
				bracketf_l.setLocateDot(from.x+Math.abs((from.x-to.x)*lrPercentage)-bracketlength, from.y);
				bracketf_r.setLocateDot(from.x+Math.abs((from.x-to.x)*lrPercentage), from.y+bracketlength);
				
				c.drawLine(from.x,    from.y,    bracketf_l.x, bracketf_l.y,   mp);
				c.drawLine(bracketf_r.x, bracketf_r.y,    ovalf_l.x, ovalf_l.y,mp);
				c.drawLine(ovalf_r.x, ovalf_r.y, to.x,      to.y,     mp);
				
				RectF bracketf = new RectF(bracketf_l.x-bracketlength,bracketf_l.y,bracketf_r.x,bracketf_r.y+bracketlength);
				c.drawArc(bracketf, 270, 90, false, mp);
				
			    RectF ovalf = new RectF(ovalf_l.x,2*ovalf_l.y-ovalf_r.y,2*ovalf_r.x-ovalf_l.x,ovalf_r.y);
				c.drawArc(ovalf, 90, 90, false, mp);
			}
		}
		//箭头
		c.drawLine(to.x-arrawWidth, to.y-arrawWidth,to.x,to.y,mp);
		c.drawLine(to.x-arrawWidth, to.y+arrawWidth,to.x,to.y,mp);
		
	}
	
}