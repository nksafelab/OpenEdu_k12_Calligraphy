package com.jinke.calligraphy.command;

import android.util.Log;

public class EditStatus {

	private final String TAG = "EditStatus";
	
	private boolean changed = false;
	
	public void resetStatus(){
		Log.e(TAG, "save! status reset");
		changed = false;
	}
	
	public void modified(String type){
		Log.e(TAG, "modified : " + type);
		changed = true;
	}
	
	public boolean isNeedSave(){
		if(changed)
			Log.e(TAG, "saving , for changed = " + changed);
		else
			Log.e(TAG, "not saving , for changed = " + changed);
		
		return changed;
	}
	
}
