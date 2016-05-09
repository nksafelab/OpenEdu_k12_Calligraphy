package com.jinke.calligraphy.template;

import android.graphics.Rect;

public class Available {
	
		public static final String AVAILABLE_SUBJECT = "subject";
		public static final String AVAILABLE_DATE = "date";
		public static final String AVAILABLE_NUMBER = "number";
		public static final String AVAILABLE_CONTENT = "content";
		private int aid;
		private boolean zoomable;
		private int startX;
		private int startY;
		private int endX;
		private int endY;
		private String controltype;
		private int linenumber;
		private int alinespace;
		private int afontsize;
		private int direct;
		private boolean editable;
		
		
		public int getDirect() {
			return direct;
		}
		public void setDirect(int direct) {
			this.direct = direct;
		}
		public boolean isEditable() {
			return editable;
		}
		public void setEditable(boolean editable) {
			this.editable = editable;
		}
		public boolean getZoomable() {
			return zoomable;
		}
		public void setZoomable(boolean b) {
			this.zoomable = b;
		}
		public int getAid() {
			return aid;
		}
		public void setAid(int aid) {
			this.aid = aid;
		}
		public int getStartX() {
			return startX;
		}
		public void setStartX(int startX) {
			this.startX = startX;
		}
		public int getStartY() {
			return startY;
		}
		public void setStartY(int startY) {
			this.startY = startY;
		}
		public int getEndX() {
			return endX;
		}
		public void setEndX(int endX) {
			this.endX = endX;
		}
		public int getEndY() {
			return endY;
		}
		public void setEndY(int endY) {
			this.endY = endY;
		}
		public int getLinenumber() {
			return linenumber;
		}
		public void setLinenumber(int linenumber) {
			this.linenumber = linenumber;
		}
		public int getAlinespace() {
			return alinespace;
		}
		public void setAlinespace(int alinespace) {
			this.alinespace = alinespace;
		}
		public int getAfontsize() {
			return afontsize;
		}
		public void setAfontsize(int afontsize) {
			this.afontsize = afontsize;
		}
		public String getControltype() {
			return controltype;
		}
		public void setControltype(String controltype) {
			this.controltype = controltype;
		}
		
}
