package com.jinke.calligraphy.template;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.jinke.calligraphy.app.branch.Start;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class WolfTemplateUtil {
	
//	public static final String TEMPLATE_PATH = "/extsd/template/";
//	public static final String TEMPLATE_PATH = Start.getStoragePath() + "/template/";
	public static final String TEMPLATE_PATH = "/mnt/sdcard/template/";
	
	public final static String COPY = "copy";
	public final static String CONTACTS = "contacts";
	public final static String MEETINGS = "meetings";
	public final static String COPYBOOK = "copybook";
	public final static String DIARY = "diary";
	public final static String NOTEBOOK = "notebook";
	public final static String BRIEFNOTE = "briefnote";
	public final static String BUSINESS = "business";
	public final static String CHECKER = "checker";
	
	public final static String WINI = "wini";
	public final static String COPYBOOK1 = "copybook1";
	public final static String COPYBOOK3 = "copybook3";
	public final static String COPYBOOK4 = "copybook4";
	public final static String COPYBOOK5 = "copybook5";
	public final static String COPYBOOK6 = "copybook6";
	public final static String COPYBOOK7 = "copybook7";
	
	
	private static WolfTemplate template = null;
	//保存全局唯一的模板信息
	
	public static String getTypeByID(int template_id){
		
		
		Log.e("util", template_id + "!!!!!!!!!!!!");
		
		if(template_id == 6)
			return NOTEBOOK;
		else if(template_id == 7)
			return BRIEFNOTE;
		else if(template_id == 8)
			return BUSINESS;
		else if(template_id == 9)
			return CHECKER;
		else if(template_id == 10)
			return WINI;
		else
			return NOTEBOOK;
	}
	
	public synchronized static WolfTemplate getCurrentTemplate(){
		
		if(template == null){
				template = getTemplateByType(Start.LoginInfo.getString("templatetype", NOTEBOOK));
			//默认返回记事本模板
		}
		return template;
	}
	
	public synchronized static void changeCurrentTemplate(String type){
		
		//改变当前模板
		template = getTemplateByType(type);
	}
	
	
	
	/**
	 * 
	 *@Title: getTemplateByType
	 *@Description: TODO 
	 *@return WolfTemplate
	 *@param type: copy,copy book,meetings,diary,notebook,contacts
	 *@throws
	 */
	public static WolfTemplate getTemplateByType(String type){
		WolfTemplate wf = null;
		
//		ZipUtils.Ectract("/sdcard/template/"+type+".zip", "/sdcard/template/"+type+"/");
		
		
		ZipUtils.Ectract(TEMPLATE_PATH+type+".zip", TEMPLATE_PATH+type+"/");
		File file = new File(TEMPLATE_PATH + type + "/"+type+".xml");
		InputStream in = null;
		
		try {
			in = new FileInputStream(file);
			wf = TemplateParser.parse(in);
			in.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return wf;		
	}
	
	public static WolfTemplate getTemplateByName(String templateName){
		WolfTemplate wf = null;
		String type = templateName.substring(0,templateName.length() - 4);
		
		File file = new File(TEMPLATE_PATH + type + "/"+type+".xml");
		if(file.exists()){
			
		}else{
			ZipUtils.Ectract(TEMPLATE_PATH+type+".zip", TEMPLATE_PATH+type+"/");
			file = new File(TEMPLATE_PATH + type + "/"+type+".xml");
		}
		
		InputStream in = null;
		
		try {
			in = new FileInputStream(file);
			wf = TemplateParser.parse(in);
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return wf;		
	}
	
	public static void getTemplates(Handler handler){
		TemplatePrepare templateThread = new TemplatePrepare(handler);
		templateThread.start();
	}
	
	public static String getTemplateBgPath(){
		return  TEMPLATE_PATH + template.getName()+"/"+template.getBackground();
	}
	
	static class TemplatePrepare extends Thread{
		Handler handler;
		public TemplatePrepare(Handler handler){
			this.handler = handler;
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			Message msg = new Message();
			String templates[] = getTemplates();
			List<WolfTemplate> template_list = new ArrayList<WolfTemplate>();
			WolfTemplate wt;
			for(int i=0;i<templates.length;i++){
				wt = getTemplateByName(templates[i]);
				templates[i] = wt.getName();
				template_list.add(wt);
			}
			
			HashMap<String, Object> templatemap = new HashMap<String, Object>();
			templatemap.put("templateName", templates);
			templatemap.put("templateList", template_list);
			
			msg.obj = templatemap;
			
			handler.sendMessage(msg);
			
		}
		
		public String[] getTemplates(){
			
			File template_dir = new File(TEMPLATE_PATH);
			String templates[] = null;
			try {
				
				if(!template_dir.exists()){
					if(template_dir.mkdir()){
						System.out.println("创建模板文件夹成功");
					}else{
						System.out.println("创建模板文件夹失败");
						return null;
					}
				}
				
				if(!template_dir.isDirectory()){
					System.out.println(TEMPLATE_PATH+"不是文件夹");
					return null;
				}
				
				File[] template_files = template_dir.listFiles();
				
				if(template_files.length == 0)
					return null;
				
				
				
				File template;
				String templatePath;
				String templateName;
				List<String> template_list = new ArrayList<String>();
				for(int i=0;i<template_files.length;i++){
					
					template = template_files[i];
					
					if(template.isDirectory())
						continue;
					
					templatePath = template.getAbsolutePath();
					templateName = templatePath.substring(templatePath.lastIndexOf("/")+1);
					
					if(!templateName.contains(".zip"))
						continue;
					
					template_list.add(templateName);
					
					System.out.println("Template "+i+" Name:"+templateName);
					
				}
				
				templates = new String[template_list.size()];
				for(int i=0;i<template_list.size();i++){
					templates[i] = template_list.get(i);
				}
				
				
			} catch (Exception e) {
				// TODO: handle exception
			}
			return templates;
		}
	}
}
