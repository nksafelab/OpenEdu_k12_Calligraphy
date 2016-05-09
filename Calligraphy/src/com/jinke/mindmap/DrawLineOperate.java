package com.jinke.mindmap;

import android.graphics.Canvas;

import android.graphics.Paint;

public class DrawLineOperate {
   private final float ARROW_WIDTH=8;//默认箭头大小
   private final float OVAL_LENGTH_LINE = 15;//默认弧度大小
   private final float FOLD_POINT_PERCENTAGE = 0.5f;//默认折线折点比例
   private final float BRACKET_LENGTH_LINE = 15;//默认括号大小
   
   private DrawLine drawline;//画线接口
   public LineStyle lineStyle ;//画线类型
   public float arrowWidth; //连接线使用的箭头的大小
   
   
   private DrawLineStraight drawStraightLine;//直线
   private DrawLineOval drawOvalLine;//弧线//效果不好，也可以不要了
   //有弧度的折线,带参数ovallength_line表示弧度的大小，当ovallength_line=0是，画折线，去掉原来的折线实现类
   public float ovallength_line ;
   public float bracketlength_line ;
   public float lrPercentage;         //折点位置（通过比例确定（取值范围0-1））
   private DrawLineOvalFold drawOvalFoldLine ;//有弧度的折线
   //初始化
   public float rowWidth;//列宽
   public float zoomPara;//缩放比例

   public DrawLineOperate(float rw,float zp){
	   this.arrowWidth = this.ARROW_WIDTH;//箭头大小
	   this.ovallength_line=this.OVAL_LENGTH_LINE;
	   this.bracketlength_line=this.BRACKET_LENGTH_LINE;
	   this.lrPercentage=this.FOLD_POINT_PERCENTAGE;
	   
	   this.rowWidth = rw;//列宽大小
	   this.zoomPara=zp;
	   
	   this.drawStraightLine = new  DrawLineStraight();
	   this.drawOvalLine = new DrawLineOval();
	   this.drawOvalFoldLine = new DrawLineOvalFold(this.ovallength_line,this.bracketlength_line,this.lrPercentage);

       this.lineStyle = LineStyle.OVAL_FOLD_LINE_BRACKET;//默认画线类型
   }
   public void setZoomPara(float zp){//缩放//传入缩放比例,改变弯曲的弧度和箭头大小
	   this.zoomPara=zp;
   }
   public void setLineStyle(LineStyle s){//设置画线类型
	   this.lineStyle = s;
   }
   public void operate(LocateDot from, LocateDot to, Canvas c, Paint mp){
	   switch(this.lineStyle){//根据类型画线
	   case STRAIGHT_LINE :
		   this.drawline = drawStraightLine;
		   break;
	   case STRAIGHT_FOLD_LINE :
		   this.ovallength_line = 0 ;
		   this.bracketlength_line = 0 ;
		   this.lrPercentage = 0.5f;  
		   drawOvalFoldLine.SetOvalPara(ovallength_line, bracketlength_line, lrPercentage);
		   this.drawline = drawOvalFoldLine;
		   break;
	   case OVAL_LINE :
		   this.drawline = drawOvalLine;
		   break;
	   case OVAL_FOLD_LINE_BRACKET :
		   this.ovallength_line = this.OVAL_LENGTH_LINE *this.zoomPara;
		   this.bracketlength_line = this.BRACKET_LENGTH_LINE *this.zoomPara;
		   this.lrPercentage = this.bracketlength_line/this.rowWidth;
		   drawOvalFoldLine.SetOvalPara(ovallength_line, bracketlength_line, lrPercentage);
		   this.drawline = drawOvalFoldLine;
		   break;
	   case OVAL_FOLD_LINE_HALF :
		   this.ovallength_line = this.OVAL_LENGTH_LINE*this.zoomPara ;
		   this.bracketlength_line = 0 ;
		   this.lrPercentage = 0.5f;  
		   drawOvalFoldLine.SetOvalPara(ovallength_line, bracketlength_line, lrPercentage);
		   this.drawline = drawOvalFoldLine;
		   break;
	   default:
		   break;
	   }
	   this.arrowWidth=this.ARROW_WIDTH*this.zoomPara;
       this.drawline.operate(from, to,  c, mp, this.arrowWidth);
   }
}
