package com.jinke.pdfcreator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.os.Environment;
import android.util.Log;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;
import com.jinke.calligraphy.app.branch.EditableCalligraphy;
import com.jinke.calligraphy.app.branch.EditableCalligraphyItem;
import com.jinke.calligraphy.app.branch.EditableCalligraphyItem.Types;
import com.jinke.calligraphy.database.BitmapHelper;

public class CloudNote {
	public static final float titleH=80;//标题的高度
    public static final float lineSpace=40;//行间距
    public static final float columSpace=20;//距离PDF两边的宽度
    public static final float topMargin=50;//距离页面顶部的高度
    public static final float bottomMargin=30;//距离页面底部的高度
    public static final float tab=40;
    public  float width=0;//PDF的宽度
    public  float height=0;//PDF的高度
    private Document document;
    Image lineImage;//

    public void createPDF(String resultPDF, List<EditableCalligraphy> editList) throws DocumentException, MalformedURLException, IOException{
		 	
    		document=new Document(PageSize.A4);
	        PdfWriter writer=PdfWriter.getInstance(document, new FileOutputStream(resultPDF));
	        document.open();
//	        Paragraph titleP=new Paragraph("Cloud note\n\n",new Font(FontFamily.HELVETICA, 22));  
//	        titleP.setAlignment(titleP.ALIGN_CENTER);  
//	        document.add(titleP);  
	        lineImage= Image.getInstance(Environment.getExternalStorageDirectory()
	        		.getAbsolutePath() + "/line.png");
	        width=PageSize.A4.getWidth();
	        height=PageSize.A4.getHeight();
	        
	        float x=columSpace;
	        float y=height-titleH;
	        EditableCalligraphy editable = null;
	        LinkedList<EditableCalligraphyItem> charList = null;
//	        EditableCalligraphyItem item = null;
//	        Image img;
        	editable = editList.get(0);//标题
        	charList=editable.getCharsList();
        	setTitle(x,y, charList);
        	
        	
    		editable=null;
    		charList=null;
    		
    		x=width-200;
    		editable = editList.get(1);//日期
    		charList=editable.getCharsList();
    		setDate(x, y, charList);
//    		editable = editList.get(2);//行数
    		
    		
    		editable=null;
    		charList=null;
    		x=columSpace+tab;
    		y=y-lineSpace; 
    		Log.v("pdf", "THis y:"+y);
        	editable = editList.get(3);//正文
        	charList = editable.getCharsList();
         	setText(x,y, charList);
//	        	for(int j=0;j<charList.size();j++){
//	        		item = charList.get(j);
//	        		Log.v("pdf", "availableid:" + editable.getAvailable().getAid() 
//	        				+ " itemid:" + item.getItemID() + " bitmap isRecycle:" + item.getCharBitmap().isRecycled());
//	        		array = BitmapHelper.bitmapDecode(item.getCharBitmap());
////		        	if(type.endsWith("char"))
//	        		if(item.getType() == Types.CharsWithStroke)
//		        	{
//			        	img = Image.getInstance(array);
//			        	array = null;
//			        	float dy=0;//调整字体上下位置
//			        	if((x+img.getScaledWidth())>width)
//			        	{
//			        		lineImage.setAbsolutePosition(0,(y - lineImage.getHeight()));
//					        document.add(lineImage);
//					        
//			        		x=columSpace;
//			        		y=y-lineSpace;
//			        	}
//			        	if(y<bottomMargin)	
//			        	{
//			        		document.newPage();
//			        		y=PageSize.A4.getHeight()-topMargin;
//			        		x=columSpace;
//			        		
//			        	}
//			        	dy=(lineSpace-img.getAbsoluteY())/2;
//			        	img.setAbsolutePosition(x,y+dy);
//			        	//System.out.println(img.getScaledWidth());
//				        document.add(img);
//			        	x+=img.getScaledWidth()+5;
//			        	
//		        	}
////		        	else if(type.endsWith("enter"))
//	        		else if(item.getType() == Types.EndofLine)
//		        	{
//		        		
//		        		lineImage.setAbsolutePosition(0,(y - lineImage.getHeight()));
//				        document.add(lineImage);
//		        		
//		        		x=columSpace+tab;
//		        		y -= (lineSpace);
//		        	}
//	        	}
	        document.close();
	    }
	 
	public void setTitle(float x,float y,LinkedList<EditableCalligraphyItem> charList)throws DocumentException, MalformedURLException, IOException{
															
		EditableCalligraphyItem item = null;
		byte[] array=null;
		Image img=null;
		for(int j=0;j<charList.size();j++){
			item=charList.get(j);
			array=BitmapHelper.bitmapDecode(item.getCharBitmap());
			img=Image.getInstance(array);
			array=null;
			img.setAbsolutePosition(x,y);
	        document.add(img);
        	x+=img.getScaledWidth()+5;
        	Log.e("TAG", "y" + y);
		}
			
	}
	
	public void setDate(float x,float y,LinkedList<EditableCalligraphyItem> charList)throws DocumentException, MalformedURLException, IOException{

		EditableCalligraphyItem item=null;
		byte[] array=null;
		Image img=null;
		for(int j=0;j<charList.size();j++)
		{
			item=charList.get(j);
			array=BitmapHelper.bitmapDecode(item.getCharBitmap());
			img=Image.getInstance(array);
			array=null;
			img.setAbsolutePosition(x, y);
			document.add(img);
			x+=img.getScaledWidth()+5;
			Log.e("TAG", "date:");
		}
		
		
		
	}
	
	public void setText(float x,float y,LinkedList<EditableCalligraphyItem> charList)throws DocumentException, MalformedURLException, IOException{
	   
		EditableCalligraphyItem item = null;
	    byte[] array = null;
	    Image img;
	    //制作下划线
	    lineImage.scaleAbsoluteWidth(width-2*columSpace);
	    float ly=y;
	    while(true)
	    {
	    	lineImage.setAbsolutePosition(columSpace, ly-lineImage.getHeight());
	    	document.add(lineImage);
	    	ly=ly-(lineImage.getHeight()+lineSpace);
	    	if(ly<bottomMargin)
	    	break;
	    }
	    //插入字体
    	for(int j=0;j<charList.size();j++){
    		item = charList.get(j);
    		array = BitmapHelper.bitmapDecode(item.getCharBitmap());
    		if(item.getType() == Types.CharsWithStroke)
        	{
	        	img = Image.getInstance(array);
	        	array = null;
	        	float dy=0;//调整字体上下位置
	        	if((x+img.getScaledWidth())>width)
	        	{
	        		
	        		lineImage.setAbsolutePosition(columSpace,(y - lineImage.getHeight()));
			        document.add(lineImage);
	        		x=columSpace;
	        		y=y-lineSpace-lineImage.getHeight();
	        	}
	        	if(y<bottomMargin)	
	        	{
	        		document.newPage();
	        		y=PageSize.A4.getHeight()-topMargin;
	        		x=columSpace;
	        		
	        	}
//	        	dy=(lineSpace-img.getAbsoluteY())/2;
	        	img.setAbsolutePosition(x,y+dy);
		        document.add(img);
	        	x+=img.getScaledWidth()+5;
	        	Log.e("TAG", "content  y" + y);
        	}
    		else if(item.getType() == Types.EndofLine)
        	{
        		
        		lineImage.setAbsolutePosition(columSpace,(y - lineImage.getHeight()));
		        document.add(lineImage);
        		
        		x=columSpace+tab;
        		y -= (lineSpace+lineImage.getHeight());
        	}
    	}
	}
}
