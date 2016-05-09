package com.jinke.calligraphy.fliplayout;

import java.util.ArrayList;
import java.util.HashMap;

import com.jinke.calligraphy.app.branch.R;
import com.jinke.single.BitmapCount;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class PicActivity extends Activity{

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			Toast.makeText(PicActivity.this, "receive", Toast.LENGTH_SHORT).show();
		}

	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pic);
		
		
		new pic_thread().start();
		
		GridView gridview = (GridView) findViewById(R.id.gridview);  
        
	      //生成动态数组，并且转入数据  
	      ArrayList<HashMap<String, Object>> lstImageItem = new ArrayList<HashMap<String, Object>>();  
	      for(int i=0;i<10;i++)  
	      {  
	        HashMap<String, Object> map = new HashMap<String, Object>();  
//	        map.put("ItemImage", R.drawable.icon);//添加图像资源的ID  
	        map.put("ItemImage", BitmapFactory.decodeResource(getResources(), R.drawable.icon));//添加图像资源的ID
	        BitmapCount.getInstance().createBitmap("PicActivity BitmapFactory.decodeResource");
	        
	        map.put("ItemText", "NO."+String.valueOf(i));//按序号做ItemText  
	        lstImageItem.add(map);  
	      }  
	      //生成适配器的ImageItem <====> 动态数组的元素，两者一一对应  
	      SimpleAdapter saImageItems = new SimpleAdapter(this, //没什么解释  
	                                                lstImageItem,//数据来源   
	                                                R.layout.pic_item,//night_item的XML实现  
	                                                  
	                                                //动态数组与ImageItem对应的子项          
	                                                new String[] {"ItemImage","ItemText"},   
	                                                  
	                                                //ImageItem的XML文件里面的一个ImageView,两个TextView ID  
	                                                new int[] {R.id.ItemImage,R.id.ItemText});  
	      //添加并且显示  
	      gridview.setAdapter(saImageItems);  
	      //添加消息处理  
	      gridview.setOnItemClickListener(new ItemClickListener());  
	  }  
	 
	String picUrl = "http://61.181.14.184:8084/ReadingsSina/shouxie/pic_1.jpg";
	class pic_thread extends Thread{
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			Bitmap mBitmap = null;
			try {
				mBitmap = BitmapFactory.decodeStream(CNetTransfer.getBitStreamEx(picUrl));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			mHandler.sendEmptyMessage(0);
			}
	}
	
	  //当AdapterView被单击(触摸屏或者键盘)，则返回的Item单击事件  
	  class  ItemClickListener implements OnItemClickListener  
	  {  
	public void onItemClick(AdapterView<?> arg0,//The AdapterView where the click happened   
	                                  View arg1,//The view within the AdapterView that was clicked  
	                                  int arg2,//The position of the view in the adapter  
	                                  long arg3//The row id of the item that was clicked  
	                                  ) {  
	    //在本例中arg2=arg3  
	    HashMap<String, Object> item=(HashMap<String, Object>) arg0.getItemAtPosition(arg2);  
	    //显示所选Item的ItemText  
	    setTitle((String)item.get("ItemText"));  
	}  
}
}