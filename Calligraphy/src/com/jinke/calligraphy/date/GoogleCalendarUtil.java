package com.jinke.calligraphy.date;

import java.util.Calendar;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

public class GoogleCalendarUtil {

	private Context mContext;
//	private SharedData share;
	private static String calanderURL = "";
	private static String calanderEventURL = "";
    private static String calanderRemiderURL = "";
    private String calId = "";

    //为了兼容不同版本的日历,2.2以后Uri发生改变
    static{
    	if(Integer.parseInt(Build.VERSION.SDK) >= 8){
    		calanderURL = "content://com.android.calendar/calendars";
    		calanderEventURL = "content://com.android.calendar/events";
    		calanderRemiderURL = "content://com.android.calendar/reminders";
    	}else{
    		calanderURL = "content://calendar/calendars";
    		calanderEventURL = "content://calendar/events";
    		calanderRemiderURL = "content://calendar/reminders";
    	}
    }

    public GoogleCalendarUtil(Context context) {
		super();
		this.mContext = context;
//		share = new SharedData(mContext);

		//获取要出入的gmail账户的id
		Cursor userCursor = mContext.getContentResolver().query(Uri.parse(calanderURL),
				null, null, null, null);
		if(userCursor.getCount() > 0){
			userCursor.moveToFirst();
			calId = userCursor.getString(userCursor.getColumnIndex("_id"));
		}
	}

	/**
	 * 添加生日信息到Google Calendar
	 *
	 */
	public boolean addToGoogleCalendar(String ScheduleTitle , String ScheduleDesc, Calendar birthday) {

//		Log.e("google", "calID:" + TextUtils.isEmpty(calId));
		if(TextUtils.isEmpty(calId))
			return false;
		
		ContentValues event = new ContentValues();
    	event.put("title", ScheduleTitle);
    	event.put("description", ScheduleDesc);

    	event.put("calendar_id",calId);

//    	birthday.set(Calendar.HOUR_OF_DAY, 8);
    	long start = birthday.getTime().getTime();
//    	birthday.set(Calendar.HOUR_OF_DAY, 12);
    	long end = birthday.getTime().getTime() + 30*60*1000;

    	event.put("dtstart", start);
    	event.put("dtend", end);
    	event.put("allDay", 0); // 0 for false, 1 for true
    	event.put("hasAlarm",1);// 0 for false, 1 for true 

    	Uri newEvent = mContext.getContentResolver().insert(Uri.parse(calanderEventURL), event);
    	//获取所添加的event的主键_id
    	long id = Long.parseLong( newEvent.getLastPathSegment() );
    	ContentValues values = new ContentValues();
        values.put( "event_id", id );

        //设置提前提醒时间
//        int advanceDays = share.getAdvanceTime();
        int advanceDays = 0;
        if (advanceDays == 0) {
        	// 默认提前十分钟
        	values.put( "minutes", 15);
		} else {
			values.put( "minutes", advanceDays * 24 * 60);
		}
        //设置提醒
        mContext.getContentResolver().insert(Uri.parse(calanderRemiderURL), values);

        return true;
        //给Friend添加事件id,同时更新friend信息
//        f.setSign("" + id);
	}

	/**
	 * 修改已保存的日程
	 * @param name  好友姓名
	 * @param id	好友id
	 * @param recentBirthday 最近生日
	 */
//	public void updateEvent(String ScheduleTitle , String ScheduleDesc, Calendar birthday) {
//		Uri eventsUri = Uri.parse(calanderEventURL);
//        Uri eventUri = ContentUris.withAppendedId(eventsUri, Long.parseLong(f.getSign()));
//
//		Cursor c = mContext.getContentResolver().query(eventsUri,
//				null, "_id=?", new String[]{f.getSign()}, null);
//		if (c == null || c.getCount() <= 0) {
//			//没有查询到日程时，则新增日程
//			addToGoogleCalendar(f, birthday);
//			return ;
//		}
//
//		ContentValues event = new ContentValues();
//    	event.put("title", f.getName() + "生日");
//    	event.put("description", f.getName() + "生日到了，记得问候！");
//    	event.put("calendar_id",calId);
//
//    	birthday.set(Calendar.HOUR_OF_DAY, 8);
//    	long start = birthday.getTime().getTime();
//    	birthday.set(Calendar.HOUR_OF_DAY, 12);
//    	long end = birthday.getTime().getTime();
//    	event.put("dtstart", start);
//    	event.put("dtend", end);
//
//    	mContext.getContentResolver().update(eventUri, event, null, null);
//	}

	/**
	 * 删除日程
	 * @param sign
	 */
	public void deleteEvent(String sign) {
		Uri eventsUri = Uri.parse(calanderEventURL);
        Uri eventUri = ContentUris.withAppendedId(eventsUri, Long.parseLong(sign));
		mContext.getContentResolver().delete(eventUri, null, null);
	}
}