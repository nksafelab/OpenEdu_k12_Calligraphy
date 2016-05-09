package com.jinke.calligraphy.date;

import com.jinke.calligraphy.app.branch.Start;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.ViewDebug.FlagToString;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // 获得提示信息
    	//获得页面信息
    	int pagenum = intent.getIntExtra("pagenum", 1);
        String msg = intent.getStringExtra("msg");
        // 显示提示信息
        
        
        
        if(Start.instance == null){
        	//当前不在云记事中，启动
        	Intent mIntent = new Intent();	
            mIntent.setClass(context, Start.class);
            mIntent.putExtra("pagenum", pagenum);
            
            mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(mIntent);
            
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
            
//          Calling startActivity() from outside of an Activity  context requires the FLAG_ACTIVITY_NEW_TASK flag. Is this really what you want?

        }else{
//        	//当前在云记事中，提示即可
        	Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
        }
        
        
        
    }
}