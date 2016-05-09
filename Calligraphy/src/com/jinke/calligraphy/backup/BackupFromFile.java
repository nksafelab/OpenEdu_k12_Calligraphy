package com.jinke.calligraphy.backup;

import java.io.File;

import com.jinke.calligraphy.app.branch.Start;
import com.jinke.calligraphy.database.CDBPersistent;

import android.util.Log;

public class BackupFromFile {
	
	public void backupDB(){
		
		CDBPersistent db = new CDBPersistent(Start.context);
		db.open();
		db.backup();
		db.close();
		
		
		
		
		
	}

}
