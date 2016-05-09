package com.jinke.calligraphy.template;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;

public class TemplateParser {

	public static WolfTemplate parse(InputStream in){
		
		WolfTemplate template = null;
		List<Available> availables = null;
		Available avail = null;
		XmlPullParser parser = Xml.newPullParser();
		String temString = null;
		try {
			parser.setInput(in, "utf-8");
			int event = parser.getEventType();
			
			while(event != XmlPullParser.END_DOCUMENT){
				
				switch (event) {
				case XmlPullParser.START_DOCUMENT:
					template = new WolfTemplate();
					break;
				case XmlPullParser.START_TAG:
					
					if("id".equals(parser.getName())){
						temString = parser.nextText();
						System.out.println(temString);
						template.setId(Integer.parseInt(temString));
					}
					if("sid".equals(parser.getName())){
						temString = parser.nextText();
						System.out.println(temString);
						template.setSid(Integer.parseInt(temString));
					}
					
					if("tdirect".equals(parser.getName())){
						temString = parser.nextText();
						System.out.println(temString);
						template.setTdirect(Integer.parseInt(temString));
					}
					
					if("name".equals(parser.getName())){
						temString = parser.nextText();
						System.out.println(temString);
						template.setName(temString);
					}
					if("format".equals(parser.getName())){
						temString = parser.nextText();
						System.out.println(temString);
						template.setFormat(Integer.parseInt(temString));
					}
					if("pentype".equals(parser.getName())){
						temString = parser.nextText();
						System.out.println(temString);
						template.setPentype(Integer.parseInt(temString));
					}
					if("fontsize".equals(parser.getName())){
						temString = parser.nextText();
						System.out.println(temString);
						template.setFontsize(Integer.parseInt(temString));
					}
					if("linespace".equals(parser.getName())){
						template.setLinespace(Integer.parseInt(parser.nextText()));
					}
					if("minlinespace".equals(parser.getName())){
						template.setMinlinespace(Integer.parseInt(parser.nextText()));
					}
					if("maxlinespace".equals(parser.getName())){
						template.setMaxlinespace(Integer.parseInt(parser.nextText()));
					}
					if("wordspace".equals(parser.getName())){
						template.setWordspace(Integer.parseInt(parser.nextText()));
					}
					if("availables".equals(parser.getName())){
						availables = new ArrayList<Available>();
					}
					if("available".equals(parser.getName())){
						avail = new Available();
					}
					if(avail != null){
						if("aid".equals(parser.getName())){
							avail.setAid(Integer.parseInt(parser.nextText()));
						}
						if("zoomable".equals(parser.getName())){
							avail.setZoomable("1".equals(parser.nextText()));
						}
						if("startX".equals(parser.getName())){
							avail.setStartX(Integer.parseInt(parser.nextText()));
						}
						if("startY".equals(parser.getName())){
							avail.setStartY(Integer.parseInt(parser.nextText()));
						}
						if("endX".equals(parser.getName())){
							avail.setEndX(Integer.parseInt(parser.nextText()));
						}
						if("endY".equals(parser.getName())){
							avail.setEndY(Integer.parseInt(parser.nextText()));
						}
						if("controltype".equals(parser.getName())){
							avail.setControltype(parser.nextText());
						}
						if("linenumber".equals(parser.getName())){
							avail.setLinenumber(Integer.parseInt(parser.nextText()));
						}
						if("alinespace".equals(parser.getName())){
							avail.setAlinespace(Integer.parseInt(parser.nextText()));
						}
						if("afontsize".equals(parser.getName())){
							avail.setAfontsize(Integer.parseInt(parser.nextText()));
						}
						if("direct".equals(parser.getName())){
							avail.setDirect(Integer.parseInt(parser.nextText()));
						}
						
						
						
					}
					if("background".equals(parser.getName())){
						template.setBackground(parser.nextText());
					}
					break;
				case XmlPullParser.END_TAG:
					if("available".equals(parser.getName())){
						availables.add(avail);
						avail = null;
					}
					if("availables".equals(parser.getName())){
						template.setAvailables(availables);
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
		return template;
	}
}
