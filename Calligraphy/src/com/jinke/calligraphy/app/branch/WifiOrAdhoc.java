package com.jinke.calligraphy.app.branch;

import com.jinke.adhocNDK.Jni;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

public class WifiOrAdhoc {
	private Jni jni;
	public WifiOrAdhoc(){
		jni = new Jni();
	}
	private WifiManager m_wifimanager = (WifiManager)Start.context.getSystemService(Context.WIFI_SERVICE);
	
	public void setAdhocMode(){
			if(m_wifimanager.isWifiEnabled()){
				Log.v("renkai","close");
				m_wifimanager.setWifiEnabled(false);
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			Log.v("renkai","joinjinke");
			jni.joinAdhoc("HanlinF7");
	}
	
	public void setWifiMode(){
		
			if(!m_wifimanager.isWifiEnabled()){
				Log.v("renkai","closejinke");
				jni.closeAdhoc();
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				m_wifimanager.setWifiEnabled(true);
//				Start.c.restartGetIP();
			}
			dialogNet();
	}
	Builder builder = null;
	private void dialogNet() {

		builder = new Builder(Start.context);
		builder.setMessage("请点击 \"设置\"连接wifi热点");
		builder.setTitle("网络链接");
		builder.setNegativeButton("取消传输",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});

		builder.setPositiveButton("设置", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				Start.instance.startActivityForResult(new Intent(Settings.ACTION_WIRELESS_SETTINGS),10);// 进入无线网络配置界面
				dialog.dismiss();
			}
		});

		builder.create().show();
	}
	
	
	
}
