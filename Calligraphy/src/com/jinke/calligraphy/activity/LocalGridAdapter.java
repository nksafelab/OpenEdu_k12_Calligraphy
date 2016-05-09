package com.jinke.calligraphy.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.jinke.calligraphy.activity.CloudGridAdapter.cloudItem;
import com.jinke.calligraphy.app.branch.R;
import com.jinke.calligraphy.app.branch.Start;
import com.jinke.single.BitmapCount;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class LocalGridAdapter extends BaseAdapter {
	private Context context;
	private ArrayList<String> thumbList = null;
	private static List<localItem> localList = null;
	Bitmap []localBitMap;

	public LocalGridAdapter(Context context, ArrayList<String> thumbList,String dir) {
		this.thumbList = thumbList;
		this.context = context;
		
		
		localBitMap = new Bitmap[thumbList.size()];			
		for (int i = 0; i < thumbList.size(); i++) {
			String s = thumbList.get(i);
			String path = Start.getStoragePath() + "/callbackup/" + dir + "/" + s;
			Log.e("locPATH",path);
			localBitMap[i] = BitmapFactory.decodeFile(path);
			BitmapCount.getInstance().createBitmap("decode localBitMap");
		}
		

		localList = new ArrayList<localItem>();
		localItem item = null;
		for (String s : thumbList) {
			item = new localItem();
			item.name = s;
			item.thumbBitmap = null;
			localList.add(item);
		}
		
		for(int i = 0; i<localList.size();i++){
			localList.get(i).thumbBitmap = localBitMap[i];
		}
		
	}

	@Override
	public int getCount() {
		return localList.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LocalGridViewHolder holder;
		if (convertView ==null){
			holder = new LocalGridViewHolder();
			LayoutInflater inflater = LayoutInflater.from(context);
			convertView = inflater.inflate(R.layout.thumb_item, null);
			holder.textView = (TextView) convertView.findViewById(R.id.thumbItemText);
			holder.imageView = (ImageView) convertView.findViewById(R.id.thumbItemImage);
			
			convertView.setTag(holder);
		}else{
			holder = (LocalGridViewHolder)convertView.getTag();
		}
		

		holder.textView.setText(localList.get(position).name);
 
		if (localList.get(position).thumbBitmap != null) {
			
			Log.e("localList", "bitmap is not null" + localList.get(position).name
					+ " bitmap:"
					+ (localList.get(position).thumbBitmap == null));
			holder.imageView.setImageBitmap(localList.get(position).thumbBitmap);
		} else {
			holder.imageView.setBackgroundResource(R.drawable.thumbdefault);
			Log.e("localList", "getView name:" + localList.get(position).name
					+ " bitmap:"
					+ (localList.get(position).thumbBitmap == null));
		}

		return convertView;
	}

	public static class localItem {
		public String name;
		public Bitmap thumbBitmap;
	}

	public List<localItem> getCloudGridList() {
		return localList;
	}
	
    public void updateGridItem(localItem item){
		
		for(localItem it : localList){
			if(it.name.equals(item.name)){
				Log.e("Cloud", "name:" + it.name + " bitmap:" + (it.thumbBitmap == null));
				it.thumbBitmap = item.thumbBitmap;
				Log.e("Cloud", "name:" + it.name + " bitmap:" + (it.thumbBitmap == null));
			}
		}
		
		this.notifyDataSetChanged();
	}

    public void recycleBitmap(){
	    for(Bitmap b : localBitMap){
		    if(b != null && !b.isRecycled()){
		    	b.recycle();
		    	BitmapCount.getInstance().recycleBitmap("LocalGridAdapter recycleBitmap");
		    }
	    }
	    
    }
    
    public final class LocalGridViewHolder{
    	public TextView textView;
    	public ImageView imageView;
    }
}
