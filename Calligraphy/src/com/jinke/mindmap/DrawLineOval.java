package com.jinke.mindmap;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

public class DrawLineOval implements DrawLine{

	public void operate(LocateDot from, LocateDot to, Canvas c, Paint mp,float arrawWidth) {
		// TODO Auto-generated method stub
		//连接线
		if(from.y == to.y){
			c.drawLine(from.x, from.y,to.x,to.y,mp);
		}
		else{
			LocateDot middle = new LocateDot();
			middle.setLocateDot((from.x+to.x)/2,(from.y+to.y)/2);
			if(from.y>to.y){
				 RectF ovalf1 = new RectF(2*from.x-middle.x,2*middle.y-from.y,middle.x,from.y);
				 c.drawArc(ovalf1, 0, 90, false, mp);
				 RectF ovalf2 = new RectF(middle.x,to.y, 2*to.x-middle.x,2*middle.y-to.y);
				 c.drawArc(ovalf2, 180, 90, false, mp);
			}
			else{
				 RectF ovalf1 = new RectF(2*from.x-middle.x,from.y,middle.x,2*middle.y-from.y);
				 c.drawArc(ovalf1, 270, 90, false, mp);
				 RectF ovalf2 = new RectF(middle.x,2*middle.y-to.y, 2*to.x-middle.x,to.y);
				 c.drawArc(ovalf2, 90, 90, false, mp);
			}
		}
		//箭头
		c.drawLine(to.x-arrawWidth, to.y-arrawWidth,to.x,to.y,mp);
		c.drawLine(to.x-arrawWidth, to.y+arrawWidth,to.x,to.y,mp);
		
	}
	
}