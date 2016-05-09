package com.jinke.calligraphy.template;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;

public class WolfTemplate {

	private int id;
	private int sid;
	private String name;
	private int format;
	private int pentype;
	private int fontsize;
	private int linespace;
	private int minlinespace;
	private int maxlinespace;
	private int tdirect;
	
	private int wordspace;
	private List<Available> availables;
	private String background;
	
	
	
	private List<Rect> list_Rect;
	
	public WolfTemplate(){
		super();
		list_Rect = new ArrayList<Rect>();
	}
	
	
	


	public int getTdirect() {
		return tdirect;
	}





	public void setTdirect(int tdirect) {
		this.tdirect = tdirect;
	}





	public int getSid() {
		return sid;
	}

	public void setSid(int sid) {
		this.sid = sid;
	}

	public int getMinlinespace() {
		return minlinespace;
	}

	public void setMinlinespace(int minlinespace) {
		this.minlinespace = minlinespace;
	}

	public int getMaxlinespace() {
		return maxlinespace;
	}

	public void setMaxlinespace(int maxlinespace) {
		this.maxlinespace = maxlinespace;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getFormat() {
		return format;
	}
	public void setFormat(int format) {
		this.format = format;
	}
	public int getPentype() {
		return pentype;
	}
	public void setPentype(int pentype) {
		this.pentype = pentype;
	}
	public int getFontsize() {
		return fontsize;
	}
	public void setFontsize(int fontsize) {
		this.fontsize = fontsize;
	}
	public int getLinespace() {
		return linespace;
	}
	public void setLinespace(int linespace) {
		this.linespace = linespace;
	}
	public int getWordspace() {
		return wordspace;
	}
	public void setWordspace(int wordspace) {
		this.wordspace = wordspace;
	}
	public List<Available> getAvailables() {
		return availables;
	}
	public void setAvailables(List<Available> availables) {
		this.availables = availables;
		Rect r;
		Available a;
		for(int i=0;i<availables.size();i++){
			a = availables.get(i);
			r = new Rect(a.getStartX(), a.getStartY()-getLinespace(), a.getEndX(), a.getEndY());
			list_Rect.add(r);
		}
		
	}
	public String getBackground() {
		return background;
	}
	public void setBackground(String background) {
		this.background = background;
	}
	
	
	public int whichEditable(int x,int y,Canvas canvas){
		Log.e("which", "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		for(int i=0;i<list_Rect.size();i++){
			if(list_Rect.get(i).contains(x,y))
				return i;
		}
		return -1;
//		if(rec_title.contains(x, y)){
////			cal_title.setCurrentPos(endX, endY);
////			cal_title.update(mBitmap, 760, 899-(int)(bCurInfo.mPosLeft+300), 70, Start.SCREEN_HEIGHT - 1);
////			flag = 1;
//			return 1;
//		}else if(rec_content.contains(x, y)){
//			
////			cal_content.setCurrentPos(endX, endY);
////			cal_content.update(mBitmap, 660, 899-(int)(bCurInfo.mPosLeft+300), 300, Start.SCREEN_HEIGHT - 1);
////			flag = 2;
//			return 2;
//		}else{
//			return 0;
//		}
	}

}
