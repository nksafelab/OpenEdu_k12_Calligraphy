package com.jinke.mywidget;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.jinke.calligraphy.app.branch.Start;
import com.jinke.calligraphy.database.CDBPersistent;
import com.jinke.kanbox.DownloadAllFileThread;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class FileListDialog extends Dialog {

	boolean checked = false;
	List<Integer> pageList;
	private int selected = 0;
	private List<String> folderList;
	public FileListDialog(Context context,List<String> list) {
		super(context);
		this.folderList = list;
		// TODO Auto-generated constructor stub
		LayoutInflater inflater = LayoutInflater.from(context);
	    
	    final String[] folderName = new String[folderList.size()];
	    
	    DateFormat format2 = null;
	    
	    for (int i = 0; i < folderList.size();i++){
	    	
	    	if("calligraphy".equals(list.get(i))){
	    		folderName[i] = folderList.get(i).replace("calligraphy", "当前最新版本");
	    		
	    	}
	    	else{
	    		folderName[i] = folderList.get(i);
	    		
	    		String tmp = folderName[i];
	    		String time = tmp.substring("calligraphy".length()
	    				, "calligraphy".length() + "yyyyMMddHHmmss".length());
	    		Log.e("dir", "time String:" + time);
	    		try {
	    			format2 = new java.text.SimpleDateFormat(  
	    	        "yyyyMMddHHmmss");
					Date d = format2.parse(time);
					format2 = new java.text.SimpleDateFormat(  
			        "HH:mm yyyy-MM-dd");
					time = format2.format(d);
					
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    		
		    	if(list.get(i).contains("_local")){
		    	
//		    		folderName[i] = folderList.get(i).replace("_local", " (本地备份)");
		    		folderName[i] = "本地备份 " + time;
		    	}
		    	else if(list.get(i).contains("calligraphy")){
		    
//		    		folderName[i] = folderList.get(i).replace("calligraphy", "时间");
		    		folderName[i] = "网络备份 " + time;
		    		
		    	}
	    	}
	    }
         	
		AlertDialog.Builder builder = new AlertDialog.Builder(context);

		builder.setSingleChoiceItems(folderName, 0, new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
				Log.e("dir", "select clicked:" + which);
				selected = which;
				
			}
		});
		
		builder.setPositiveButton("确定",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						setTitle("点击确定");
						
						Message msg = new Message();
						msg.what = -1;
						if(selected != -1)
							msg.obj = folderList.get(selected);
						Log.e("dir", "selected:" + selected + " size:" + folderName.length + " msg:" + folderList.get(selected));
						Start.kanboxDownloadHandler.sendMessage(msg);
					}
				});
		
		
		
		builder.setNegativeButton("取消",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						setTitle("点击取消");
					}
				});
		builder.show();
	}

}
