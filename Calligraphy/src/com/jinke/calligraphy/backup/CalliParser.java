package com.jinke.calligraphy.backup;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;


import android.util.Xml;

public class CalliParser {
	List<CalligraphyItem> listItem;
	CalligraphyItem item;
	
	public List<CalligraphyItem> parse(InputStream in){
		
		XmlPullParser parser = Xml.newPullParser();
		String temString = "";
		
		try {
			parser.setInput(in, "utf-8");
			int event = parser.getEventType();
			
			while(event != XmlPullParser.END_DOCUMENT){
				
				switch (event) {
				case XmlPullParser.START_DOCUMENT:
					listItem = new ArrayList<CalligraphyItem>();
					break;
				case XmlPullParser.START_TAG:
					if("Item".equals(parser.getName())){
						item = new CalligraphyItem();
					}
					
					if("templateID".equals(parser.getName())){
						temString = parser.nextText();
						System.out.println(StringToInt(temString));
						
					}
					
					if("pageNum".equals(parser.getName())){
						temString = parser.nextText();
						System.out.println(StringToInt(temString));
						
					}
					if("availableID".equals(parser.getName())){
						temString = parser.nextText();
						System.out.println(StringToInt(temString));
						
					}
					if("itemID".equals(parser.getName())){
						temString = parser.nextText();
						System.out.println(StringToInt(temString));
						
					}
					if("flipBottom".equals(parser.getName())){
						temString = parser.nextText();
						System.out.println(StringToInt(temString));
						
					}
					if("flipDst".equals(parser.getName())){
						temString = parser.nextText();
						System.out.println(StringToInt(temString));
						
					}
					if("charType".equals(parser.getName())){
						temString = parser.nextText();
						System.out.println(temString);
						
					}
					if("matrix".equals(parser.getName())){
						temString = parser.nextText();
						System.out.println(temString);
						
					}
					if("created".equals(parser.getName())){
						temString = parser.nextText();
						System.out.println(temString);
						
					}
					if("byteBitmap".equals(parser.getName())){
						temString = parser.nextText();
						System.out.println(temString);
						
					}
					
					break;
				case XmlPullParser.END_TAG:
					if("Item".equals(parser.getName())){
						
					}
					break;
				
				default:
					break;
				}
				
				event = parser.next();
			}
			
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return listItem;
	}
	
	private int StringToInt(String str){
		return Integer.parseInt(str);
	}
}
