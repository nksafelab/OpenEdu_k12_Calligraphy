package com.jinke.single;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import com.jinke.calligraphy.app.branch.Start;

public class ScaleSave {
	private String txtPath = "/extsd/calldir/scale.txt";
	private FileOutputStream os = null;
	PrintWriter write = null;
	
	private ScaleSave() throws IOException{
//		write = new PrintWriter(new FileWriter(txtPath,true));
		
	}
	private static ScaleSave instance = null;
	public static ScaleSave getInstance(){
		if(instance == null)
			try {
				instance = new ScaleSave();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return instance;
	}
	
	public void insertScale(int currentPos,float distX,float distY,float Scale){
//		String str = "";
//		if(distY < 250.0f)
//			str = "currentPos:" + currentPos + "-------- distX: " + distX + " distY:" + distY + " Scale:" + Scale;
//		else
//			str = "currentPos:" + currentPos + " distX: " + distX + " distY:" + distY + " Scale:" + Scale;
//		write.append(str + "\n");
//		write.flush();
	}
	
	public void close(){
//		write.close();
	}
	
	public void newPage(){
//		write.append("---------------pagenum:" + Start.getPageNum() + "------------------------------------\n");
//		write.flush();
	}
	
}
