package com.jinke.mindmap;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;

public class DrawOutLineOperate {

   private final float OVAL_LENGTH_BOX = 15;//默认弧度大小
   private final Paint TRANS_PAINT_LINE = new Paint();
   private final Paint TRANS_PAINT_CONTENT = new Paint();
   private final float VERTICAL_OFFSET_PERCENTAGE=0.7f;
   private DrawOutLine drawoutline;//画框接口
   
   public float ovallength_box;
   public float zoomPara;//缩放比例
   private Paint contentPaint;
   private Paint linePaint;
   
   public OutLineStyle outlineStyle;//框类型
   
   private DrawOutLineHalfLeft  drawOutLineHalfLeft =  new DrawOutLineHalfLeft(this.OVAL_LENGTH_BOX,this.VERTICAL_OFFSET_PERCENTAGE);
   private DrawOutLineHalfRight drawOutLineHalfRight = new DrawOutLineHalfRight(this.OVAL_LENGTH_BOX,this.VERTICAL_OFFSET_PERCENTAGE);
   private DrawOutLineHalfRound drawOutLineHalfRound = new DrawOutLineHalfRound(this.OVAL_LENGTH_BOX,this.VERTICAL_OFFSET_PERCENTAGE);
   private DrawOutLineOvalRect  drawOutLineRoundRect = new DrawOutLineOvalRect(this.OVAL_LENGTH_BOX,this.TRANS_PAINT_CONTENT);

   public DrawOutLineOperate(Paint cp, Paint lp, float zp){
	   this.ovallength_box=this.OVAL_LENGTH_BOX;//默认弧度大小15
	   
	   this.zoomPara=zp;//缩放比例
	   this.contentPaint=cp;//填充颜色
	   this.linePaint=lp;//画线颜色
	   
	   this.TRANS_PAINT_LINE.setColor(Color.TRANSPARENT);
	   this.TRANS_PAINT_LINE.setStyle(Style.FILL);
	   this.TRANS_PAINT_CONTENT.setColor(Color.TRANSPARENT);
	   this.TRANS_PAINT_CONTENT.setStyle(Style.STROKE);
	   
	   this.outlineStyle=OutLineStyle.OVAL_STROKE_BOX;//默认节点框类型是带线的弧形框
   }
   public void setOutLineStyle(OutLineStyle ols){
	   this.outlineStyle=ols;
   }
   public void setZoomPara(float zp){
	   this.zoomPara=zp;
   }
   public void setContentPaint(Paint sp){
	   this.contentPaint=sp;
   }
   public void operate(LocateDot from, LocateDot to, Canvas c, Paint mp){
	   switch(this.outlineStyle){
	   case STRAIGHT_STROKE_HALF_BOX_LEFT:
		   this.ovallength_box=0;
		   this.linePaint=mp;
		   drawOutLineHalfLeft.setOvalLength(this.ovallength_box);
		   drawoutline=drawOutLineHalfLeft;
		   break;
	   case STRAIGHT_STROKE_HALF_BOX_RIGHT:
		   this.ovallength_box=0;
		   this.linePaint=mp;
		   drawOutLineHalfRight.setOvalLength(this.ovallength_box);
		   drawoutline=drawOutLineHalfRight;
		   break;
	   case STRAIGHT_STROKE_HALF_BOX_LEFT_AND_RIGHT:
		   this.ovallength_box=0;
		   this.linePaint=mp;
		   drawOutLineHalfRound.setOvalLength(this.ovallength_box);
		   drawoutline=drawOutLineHalfRound;
		   break;
	   case STRAIGHT_STROKE_BOX:
		   this.ovallength_box=0;
		   this.linePaint=mp;
		   drawOutLineRoundRect.setOvalLength(this.ovallength_box);
		   drawOutLineRoundRect.setContentPaint(this.TRANS_PAINT_LINE);
		   drawoutline=drawOutLineRoundRect;
		   break;
	   case STRAIGHT_FILL_BOX:
		   this.ovallength_box=0;
		   this.linePaint=this.TRANS_PAINT_LINE;
		   drawOutLineRoundRect.setOvalLength(this.ovallength_box);
		   drawOutLineRoundRect.setContentPaint(this.contentPaint);
		   drawoutline=drawOutLineRoundRect;
		   break;
	   case STRAIGHT_STROKE_AND_FILL_BOX:
		   this.ovallength_box=0;
		   this.linePaint=mp;
		   drawOutLineRoundRect.setOvalLength(this.ovallength_box);
		   drawOutLineRoundRect.setContentPaint(this.contentPaint);
		   drawoutline=drawOutLineRoundRect;
		   break;
	   case OVAL_STROKE_HALF_BOX_LEFT:
		   this.linePaint=mp;
		   this.ovallength_box=this.OVAL_LENGTH_BOX*this.zoomPara;
		   drawOutLineHalfLeft.setOvalLength(this.ovallength_box);
		   drawoutline=drawOutLineHalfLeft;
		   break;
	   case OVAL_STROKE_HALF_BOX_RIGHT:
		   this.linePaint=mp;
		   this.ovallength_box=this.OVAL_LENGTH_BOX*this.zoomPara;
		   drawOutLineHalfRight.setOvalLength(this.ovallength_box);
		   drawoutline=drawOutLineHalfRight;
		   break;
	   case OVAL_STROKE_HALF_BOX_LEFT_AND_RIGHT:
		   this.linePaint=mp;
		   this.ovallength_box=this.OVAL_LENGTH_BOX*this.zoomPara;
		   drawOutLineHalfRound.setOvalLength(this.ovallength_box);
		   drawoutline=drawOutLineHalfRound;
		   break;
	   case OVAL_STROKE_BOX:
		   this.linePaint=mp;
		   this.ovallength_box=this.OVAL_LENGTH_BOX*this.zoomPara;
		   drawOutLineRoundRect.setOvalLength(this.ovallength_box);
		   drawOutLineRoundRect.setContentPaint(this.TRANS_PAINT_LINE);
		   drawoutline=drawOutLineRoundRect;
		   break;
	   case OVAL_FILL_BOX:
		   this.linePaint=this.TRANS_PAINT_LINE;
		   this.ovallength_box=this.OVAL_LENGTH_BOX*this.zoomPara;
		   drawOutLineRoundRect.setOvalLength(this.ovallength_box);
		   drawOutLineRoundRect.setContentPaint(this.contentPaint);
		   drawoutline=drawOutLineRoundRect;
		   break;
	   case OVAL_STROKE_AND_FILL_BOX:
		   this.ovallength_box=this.OVAL_LENGTH_BOX*this.zoomPara;
		   this.linePaint=mp;
		   drawOutLineRoundRect.setOvalLength(this.ovallength_box);
		   drawOutLineRoundRect.setContentPaint(this.contentPaint);
		   drawoutline=drawOutLineRoundRect;
		   break;
	   }
       this.drawoutline.operate(from, to,  c, this.linePaint);
   }
}
