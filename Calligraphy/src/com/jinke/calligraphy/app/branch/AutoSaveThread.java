package com.jinke.calligraphy.app.branch;

public class AutoSaveThread extends Thread{

	@Override
	public void run() {
		// TODO Auto-generated method stub
		//caoheng 2015.11.24不让乱保存
	//Start.c.view.saveDatebase();
	//	Start.c.view.saveDrawLine();
		
		Start.backupHandler.sendEmptyMessage(0);
	}
}
