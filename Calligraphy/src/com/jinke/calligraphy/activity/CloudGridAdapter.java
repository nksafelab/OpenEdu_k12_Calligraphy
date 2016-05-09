package com.jinke.calligraphy.activity;

import java.util.ArrayList;
import java.util.List;

import com.jinke.calligraphy.app.branch.R;
import com.jinke.single.BitmapCount;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CloudGridAdapter extends BaseAdapter{
	private Context context;
	private ArrayList<String> thumbList = null;
	private static List<cloudItem> cloudList = null;
	
	
	
	public CloudGridAdapter(Context context, ArrayList<String> thumbList){
		this.thumbList = thumbList;
		this.context = context;
		
		cloudList = new ArrayList<cloudItem>();
		cloudItem item = null;
		for(String s : thumbList){
			item = new cloudItem();
			item.name = s;
			item.thumbBitmap = null;
			cloudList.add(item);
		}
			
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return cloudList.size();
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
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		
		GridViewHolder holder;
		
		if(convertView == null){
			holder = new GridViewHolder();
			LayoutInflater flater = LayoutInflater.from(context);
			convertView = flater.inflate(R.layout.thumb_item, null);
			holder.textView = (TextView)convertView.findViewById(R.id.thumbItemText); 
			holder.imageView = (ImageView)convertView.findViewById(R.id.thumbItemImage); 
			
			convertView.setTag(holder);
		}else{
			holder = (GridViewHolder)convertView.getTag();
		}
		
		holder.textView.setText(cloudList.get(position).name);
		
		
		if(cloudList.get(position).thumbBitmap != null){
			
//			iv.setBackgroundColor(Color.WHITE);
			holder.imageView.setImageBitmap(cloudList.get(position).thumbBitmap);
			
			Log.e("Cloud55555555555555555555555555555", "getView name:" + cloudList.get(position).name + " bitmap:" + (cloudList.get(position).thumbBitmap == null));
		}
		else{
			holder.imageView.setBackgroundResource(R.drawable.thumbdefault);
			Log.e("Cloud", "getView name:" + cloudList.get(position).name + " bitmap:" + (cloudList.get(position).thumbBitmap == null));
		}
		
		
		return convertView;
	}

	public static class cloudItem{
		public String name;
		public Bitmap thumbBitmap;
	}
	
	public List<cloudItem> getCloudGridList(){
		return cloudList;
	}
	
	
	public void updateGridItem(cloudItem item){
		
		for(cloudItem it : cloudList){
			if(it.name.equals(item.name)){
				Log.e("Cloud", "name:" + it.name + " bitmap:" + (it.thumbBitmap == null));
				it.thumbBitmap = item.thumbBitmap;
				Log.e("Cloud", "name:" + it.name + " bitmap:" + (it.thumbBitmap == null));
			}
		}
		
		this.notifyDataSetChanged();
	}
	
	public void recycleBitmap(){
		for(cloudItem it : cloudList){
			if(it != null && it.thumbBitmap != null && !it.thumbBitmap.isRecycled()){
				it.thumbBitmap.recycle();
				BitmapCount.getInstance().recycleBitmap("CloudGridAdapter recycleBitmap");
			}
		}
		
		
	}
	
	public final class GridViewHolder{
		public ImageView imageView;
		public TextView textView;
	}
}
