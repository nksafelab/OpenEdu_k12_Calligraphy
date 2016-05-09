package com.jinke.mindmap;

import android.graphics.Canvas;
import android.graphics.Paint;

public class DrawLineStraight implements DrawLine{

	public void operate(LocateDot from, LocateDot to, Canvas c, Paint mp,float arrawWidth) {
		// TODO Auto-generated method stub
		//连接线
		float l=(float) ((Math.sqrt(2)/2)*arrawWidth);
		LocateDot arrow=new LocateDot(to.x-l,to.y);
		c.drawLine(from.x, from.y,arrow.x,arrow.y,mp);
		c.drawLine(arrow.x,arrow.y,to.x,to.y,mp);
		//箭头
		c.drawLine(to.x-arrawWidth, to.y-arrawWidth,to.x,to.y,mp);
		c.drawLine(to.x-arrawWidth, to.y+arrawWidth,to.x,to.y,mp);
	}
	
}