package com.jinke.calligraphy.app.branch;

import java.util.ArrayList;
import java.util.List;

import com.jinke.calligraphy.database.CDBPersistent;
import com.jinke.single.BitmapCount;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class ImageAdapter extends BaseAdapter{

	private Context mContext;
	private Cursor cursor;
	public List<PageItem> itemList;
	Bitmap tempBitmap;//读取大图
	Bitmap itemBitmap;//生成需要的小图
	Canvas canvas;
	
	public ImageAdapter(Context context,Cursor cursor,List<PageItem> itemList) {
		this.mContext=context;
		this.cursor = cursor;
		this.itemList = itemList;
		
		
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		
//		return cursor.getCount();
		return itemList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		
		//定义一个ImageView,显示在GridView里
//		View itemView;
//		ImageView imageView = null;
//		TextView textView;
		
		viewHolder holder = null;

		if(convertView==null){

			holder = new viewHolder();
			
			convertView = Start.instance.getLayoutInflater().inflate(R.layout.popup_window_item, null);
			
			holder.imageView = (ImageView)convertView.findViewById(R.id.itemImg);
			holder.textView = (TextView)convertView.findViewById(R.id.itemTxt);
			
			convertView.setTag(holder);

		}else{

			holder = (viewHolder)convertView.getTag();
			
			
		}

		holder.imageView.setImageBitmap(itemList.get(position).bgBitmap);
		

		holder.textView.setText("第"+ itemList.get(position).pagenum+ "份");
		holder.textView.setTextColor(Color.BLACK);
		return convertView;
	}
	
	
	public final class viewHolder{
		public ImageView imageView;
		public TextView textView;
	}
	

	public void releaseBitmap(){
		for(int i=0;i<itemList.size();i++){
			if(itemList.get(i)!= null && !itemList.get(i).bgBitmap.isRecycled()){
				itemList.get(i).bgBitmap.recycle();
				BitmapCount.getInstance().recycleBitmap("ImageAdapter releaseBitmap bgBitmap");
			}
		}
	}
	
	
}
