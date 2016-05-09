package com.jinke.calligraphy.backup;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.jinke.calligraphy.app.branch.CursorDrawBitmap;
import com.jinke.calligraphy.app.branch.Start;
import com.jinke.calligraphy.command.BackupCommand;

public class BackupReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		
		Toast.makeText(Start.context, "收到消息，恢复!!!!!!!!!!!!", Toast.LENGTH_LONG).show();
		
		new BackupCommand(Start.context, new Handler(){
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				
				for(int i=0;i<CursorDrawBitmap.listEditableCalligraphy.size();i++){
					CursorDrawBitmap.listEditableCalligraphy.get(i).initDatabaseCharList();
				}
				Start.c.view.cursorBitmap.updateHandwriteState();
				Log.e("vectorr", " ---------------------------->>>>>>>>>>>>>>>>>>>>>>>>>>>>>start finish9" );
//				Start.c.view.setFreeDrawBitmap();
				Toast.makeText(Start.context, "同步服务器数据到本地，覆盖本地数据", Toast.LENGTH_LONG).show();
				
				
			}
		}).execute();
		
	}

	
}
