package com.jinke.calligraphy.data;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import android.graphics.Bitmap;

public class XMLCreater {
	DataOutputStream raf;
	String encode;
	
	public XMLCreater(String code, DataOutputStream file){
		encode = code;
		raf = file;
	}
	
	public void startDocument(){
		try {
			raf.write(("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").getBytes());
			raf.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void endDocument(){
		try {
			raf.flush();
			raf.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void attribute(String name, String value){
		try {
			String n = " " + name + "=";
			String v = "\"" + value + "\"";
			raf.write(n.getBytes());
			raf.write(v.getBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void startTag(String tag){
		try {
			raf.write(("<" + tag).getBytes());
			raf.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*
	 * use when two or more startTag occurs together
	 */
	public void withoutAttribute(){
		try {
			raf.write((">").getBytes());
			raf.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void endTag(String tag){
		try {
			raf.write(("</" + tag + ">").getBytes());
			raf.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void text(String s){
		try {
			raf.write(">".getBytes());
			raf.write(s.getBytes());
			raf.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void text(byte b){
		try {
			raf.write(">".getBytes());
			raf.writeByte(b);
			raf.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void text(int i){
		try {
			raf.write(">".getBytes());
			raf.writeInt(i);
			raf.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void text(byte[] bs){
		try {
			raf.write(">".getBytes());
			raf.write(bs);
			raf.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void text(short s){
		try {
			raf.write(">".getBytes());
			raf.writeShort(s);
			raf.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void text(long s){
		try {
			raf.write(">".getBytes());
			raf.writeLong(s);
			raf.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void text(Bitmap b){
		try {
			raf.write(">".getBytes());
			//b.compress(Bitmap.CompressFormat.JPEG, 80, raf);
			// by gongxl. Temp changed to PNG
			b.compress(Bitmap.CompressFormat.PNG, 80, raf);
			raf.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void text(File f){
		int len;
		byte[] buffer = new byte[512];
		
		try {
			raf.write(">".getBytes());
			FileInputStream fis = new FileInputStream(f);
			while((len = fis.read(buffer)) != -1) {
				raf.write(buffer, 0, len);
			}
			fis.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	
}
