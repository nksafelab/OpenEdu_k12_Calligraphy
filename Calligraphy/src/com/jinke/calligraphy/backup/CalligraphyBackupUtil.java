package com.jinke.calligraphy.backup;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.jinke.calligraphy.app.branch.Start;
import com.jinke.calligraphy.database.CDBPersistent;

public class CalligraphyBackupUtil {
	private static final String TAG = "CalligraphyBackupUtil";
	private String getStoreUrl = "http://61.181.14.184:8084/AndroidService/getCalligraphyList.do?";
	
	//private String getAllStore = "http://61.181.14.184:8084/AndroidService/calligraphyrestorationall.do?";
	
	private String getAllStore = "http://61.181.14.184:8084/AndroidService/calligraphysync.do?";
	 
	private final static String SIMCARD_PATH = "/sys/devices/platform/simcard/info";

	private Context context;
	
	public CalligraphyBackupUtil(Context context){
		this.context = context;
	}
	public boolean getCalligraphyList(int templateId,int pageNum){
        
		getStoreUrl = getStoreUrl + "templateID="+templateId+"&pageNum="+pageNum+"&simID="+getSimID();
		
		List<CalligraphyItem> cList = null;
		CalliParse calliParser = new CalliParse();
		try {
			cList = calliParser.doParse(DownLoadUtil.getInputStream(getStoreUrl));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.e(TAG, "exception in Util",e);
			e.printStackTrace();
		}
//		Log.e(TAG, "cListSize:"+cList.size());
		if(cList.size() == 0 || cList == null){
			Toast.makeText(context, "获取列表异常，请稍后再试", Toast.LENGTH_SHORT).show();
			return false;
		}else{
			updateDB(cList);
			return true;
		}
		
		
	}
	
	public boolean getAllCalligraphyList(){
        
		if("123456".equals(getSimID())){
			//没有simcard，获得imei号
			TelephonyManager telephonyManager=(TelephonyManager) Start.context.getSystemService(Context.TELEPHONY_SERVICE);
			String imei=telephonyManager.getDeviceId();
			getAllStore = getAllStore + "simID="+imei;

		}else{
			getAllStore = getAllStore + "simID="+getSimID();
		}
		Log.e("getAllStore",getAllStore);
		List<CalligraphyItem> cList = null;
		CalliParse calliParser = new CalliParse();
		try {
			clearDB();
			cList = calliParser.doParse(DownLoadUtil.getInputStream(getAllStore));
//			cList = calliParser.doParse(CNetTransfer.GetXmlInputStream(getAllStore));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.e(TAG, "exception in Util",e);
			e.printStackTrace();
		}
		if(cList.size() == 0 || cList == null){
//			Toast.makeText(context, "获取列表异常，请稍后再试", Toast.LENGTH_SHORT).show();
			Log.e("CalligraphyBackup", "获取列表异常，请稍后再试");
			return false;
		}else{
			
			return updateDB(cList);
		}
	}
	public void clearDB(){
		CDBPersistent db = new CDBPersistent(context);
		db.open();
		db.deleteAll();
		db.close();
	}
	public boolean updateDB(List<CalligraphyItem> cList){
		CDBPersistent db = new CDBPersistent(context);
		db.open();
		boolean flag = db.insert(cList);
		db.pageBackup(cList);
		db.close();
		return flag;
	}
	
	public static String getSimID(){
        // TODO Auto-generated method stub
        Log.v(TAG, "getSimID() called");
        String simID = "";
         try {
                    FileInputStream is = new FileInputStream(SIMCARD_PATH);
                    DataInputStream dis = new DataInputStream(is);
                    simID = dis.readLine();
                    simID = simID.trim();
                    is.close();
                    dis.close();
                        return simID;
            } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    Log.v(TAG, "getSimID exception: do not have a SIM Card!",e);
                    return "123456";
            } catch (IOException e) {
                        // TODO Auto-generated catch block
                Log.v(TAG, "getSimID exception: read IOException",e);
                        return "";
                }



}

}
