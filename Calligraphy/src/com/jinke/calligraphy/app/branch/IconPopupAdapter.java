package com.jinke.calligraphy.app.branch;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class IconPopupAdapter extends BaseAdapter{

	private Context context;
	private int[] ImageRes;
	public IconPopupAdapter(Context context , int[] ImageRes){
		this.context = context;
		this.ImageRes = ImageRes;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return ImageRes.length;
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
		ImageView imageView = null;
		if(convertView == null){
			imageView = new ImageView(context);
		}else{
			imageView = (ImageView)convertView;
		}
		
		imageView.setImageResource(ImageRes[position]);
		
		return imageView;
	}

}
