package com.jinke.calligraphy.app.branch;

import java.util.ArrayList;
import java.util.List;

import android.os.Handler;
import android.util.Log;

import com.jinke.calligraphy.database.CalligraphyDB;
import com.jinke.calligraphy.template.WolfTemplateUtil;

public class BackgroundSaveThread extends Thread{
	class SaveUnit{
		int op_type;
		int op_pos;
		int template_id;
		int pagenum;
		int available_id;
		EditableCalligraphyItem eItem;
	}
	
	private List<SaveUnit> unitList;
	private Handler saveFinishHandler;
	
	public BackgroundSaveThread(Handler saveFinishHandler) {
		this.saveFinishHandler = saveFinishHandler;
		unitList = new ArrayList<BackgroundSaveThread.SaveUnit>();
	}
	
	public void addSaveUnit(int OP_type,int OP_Pos,int template_id,int pagenum,int available_id,EditableCalligraphyItem eItem){
		SaveUnit unit = new SaveUnit();
		unit.op_type		=	OP_type;
		unit.op_pos			=	OP_Pos;
		unit.template_id	=	template_id;
		unit.pagenum		=	pagenum;
		unit.available_id	=	available_id;
		unit.eItem			=	eItem;
		unitList.add(unit);
	}
	
	@Override
	public void run() {
		Log.e("mes", "thread run");
		SaveUnit unit = null;
		for(int i=0;i<unitList.size();i++){
			unit	=	unitList.get(i);
			CalligraphyDB.getInstance(Start.context).saveOperating(
					unit.op_type,
					unit.op_pos,
					unit.template_id,
					unit.pagenum, 
					unit.available_id, 
					unit.eItem
			);
			
			Log.e("clip", "unit i:" + i);
		}
		saveFinishHandler.sendEmptyMessage(0);
		Log.e("mes", "send mesg saveFinish true!!!!!!!!!!!");
	}
}
