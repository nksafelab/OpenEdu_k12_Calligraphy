package com.jinke.calligraphy.data;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

import com.jinke.calligraphy.template.Available;
import com.jinke.calligraphy.template.TemplateParser;
import com.jinke.calligraphy.template.WolfTemplate;
import com.jinke.calligraphy.template.WolfTemplateUtil;
import com.jinke.calligraphy.template.ZipUtils;


public class CreatTemplet {
	final static String COPY = "copy";
	final static String CONTACTS = "contacts";
	final static String MEETINGS = "meetings";
	final static String COPYBOOK = "copybook";
	final static String DIARY = "diary";
	final static String NOTEBOOK = "notebook";
	
	public static void write(){
		
		String template = NOTEBOOK;
		ZipUtils.Ectract(WolfTemplateUtil.TEMPLATE_PATH+template+".zip", WolfTemplateUtil.TEMPLATE_PATH+template+"/");
		
		File file = new File(WolfTemplateUtil.TEMPLATE_PATH+template+"/"+template+".xml");
		try {
			InputStream in = new FileInputStream(file);
			
			WolfTemplate wt = TemplateParser.parse(in);
			showTemplate(wt);
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}
	public static void showTemplate(WolfTemplate wt){
		
		Available available;
		System.out.println("===================================================");
		
		System.out.println("id:"+wt.getId());
		System.out.println("name:"+wt.getName());
		System.out.println("format:"+wt.getFormat());
		System.out.println("pentype:"+wt.getPentype());
		System.out.println("fontsize:"+wt.getFontsize());
		System.out.println("linespace:"+wt.getLinespace());
		System.out.println("wordspace:"+wt.getWordspace());
		
		
		if(wt.getAvailables() != null)
		for(int i=0;i<wt.getAvailables().size();i++){
			System.out.println("availables	"+i+"~~~~~~~~~~~~~~~~~~~~");
			available = wt.getAvailables().get(i);
			System.out.println("	aid:"+available.getAid());
			System.out.println("	startX:"+available.getStartX());
			System.out.println("	startY:"+available.getStartY());
			System.out.println("	endX:"+available.getEndX());
			System.out.println("	endY:"+available.getEndY());
		}
		
		
		
		System.out.println("background:"+wt.getBackground());
		
		
		System.out.println("===================================================");
	}
}
