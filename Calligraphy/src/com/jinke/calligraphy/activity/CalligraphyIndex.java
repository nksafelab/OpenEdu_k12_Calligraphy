package com.jinke.calligraphy.activity;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.jinke.calligraphy.app.branch.CursorDrawBitmap;
import com.jinke.calligraphy.app.branch.R;
import com.jinke.calligraphy.app.branch.Start;
import com.jinke.calligraphy.database.CDBPersistent;
import com.jinke.calligraphy.template.WolfTemplateUtil;
import com.jinke.single.BitmapCount;

public class CalligraphyIndex extends Activity{
	private static final String TAGS = "CalligraphyIndex";
	ListView indexListView = null;
	Button cancelButton = null;
	List<IndexItem> indexList = null;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.calligraphy_index);
		setWidthAndHeight();
		
		Log.i("hhh", "CalligraphyIndex!");
		
		indexListView = (ListView)findViewById(R.id.calligraphy_index_listView);
		cancelButton = (Button)findViewById(R.id.calligraphy_index_cancel);
		
		cancelButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		
		
		indexList = new ArrayList<CalligraphyIndex.IndexItem>();
		
		CDBPersistent db = new CDBPersistent(this);
		db.open();
		Cursor cursor = db.getBitmapPath();
		
		Log.e(TAGS, "cursor:" + (cursor == null));
		
		int pagenum = 0;
		String pagePath = "";
		IndexItem item = null;
		for(cursor.moveToLast();!cursor.isBeforeFirst();cursor.moveToPrevious()){
			item = new IndexItem();
			pagenum = cursor.getInt(cursor.getColumnIndex("pagenum"));
			pagePath = cursor.getString(cursor.getColumnIndex("path"));
			item.setPagenum(pagenum);
			item.setPagePath(pagePath);
			item.initBitmap();
			indexList.add(item);
		}
		cursor.close();
		db.close();
		
		CalligraphyIndexAdapter adapter = new CalligraphyIndexAdapter();
		indexListView.setAdapter(adapter);
		
		indexListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				Message msg = new Message();
				msg.what = Start.PAGE_CHANGE;
				msg.arg1 = indexList.get(arg2).pagenum;
				Start.pageChangeHandler.sendMessage(msg);
				Log.e(TAGS, "click item:" + indexList.get(arg2).pagenum);
				finish();
				
			}
		});
		indexListView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				
				Log.e(TAGS, "long click item:" + indexList.get(arg2).pagenum);
//				deleteConfirm(indexList.get(arg2).pagenum);
//				finish();
				return false;
			}
		});
		
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		
		for(IndexItem item : indexList){
			if(item.softBitmap != null){
				Bitmap b = item.softBitmap.get();
				if(b == null){
					Log.e(TAGS, "b = null" + item.getPagenum());
				}else{
					if(b.isRecycled()){
						Log.e(TAGS, "b allready recycle" + item.getPagenum());
					}
					if(b!= null && !b.isRecycled()){
						b.recycle();
						BitmapCount.getInstance().recycleBitmap("CalligraphyIndex onDestroy");
						Log.e(TAGS, "recycle item:" + item.getPagenum());
					}
				}
			}
		}
		
		super.onDestroy();
	}
	
	class CalligraphyIndexAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return indexList.size();
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
			ViewHolder holder = null;
			if(convertView == null){
				holder = new ViewHolder();
				LayoutInflater flater = LayoutInflater.from(CalligraphyIndex.this);
				convertView = flater.inflate(R.layout.calligraphy_index_item, null);
				
				holder.textView = (TextView)convertView.findViewById(R.id.calligraphy_index_item_textView);
				holder.imageView = (ImageView)convertView.findViewById(R.id.calligraphy_index_item_imageView);
				
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder)convertView.getTag();
				
			}
			
			holder.textView.setText("第 " + indexList.get(position).getPagenum() + " 份");
			holder.imageView.setImageBitmap(indexList.get(position).getBitmap());
			
			return convertView;
		}
		
		final class ViewHolder{
			public TextView textView;
			public ImageView imageView;
		}
		
	}
	
	class IndexItem{
		private int pagenum;
		private String pagePath;
		private SoftReference<Bitmap> softBitmap;
		public int getPagenum() {
			return pagenum;
		}
		public void setPagenum(int pagenum) {
			this.pagenum = pagenum;
		}
		public String getPagePath() {
			return pagePath;
		}
		public void setPagePath(String pagePath) {
			this.pagePath = pagePath;
		}
		public Bitmap getBitmap() {
			if(softBitmap == null){
				//OOM or File not exits
				return null;
			}else{
				if(softBitmap.get() == null){
					//bitmap recycled
					if(initBitmap())
						return softBitmap.get();
					else
						//redecode error OOM or File not exists
						return null;
				}else{
					return softBitmap.get();
				}
			}
		}
		public boolean initBitmap() {
			File indexFile = new File(pagePath);
			if(!indexFile.exists()){
				softBitmap = null;
				return false;
			}else{
				Bitmap b = null;
				try {
					b = BitmapFactory.decodeFile(pagePath);
					BitmapCount.getInstance().createBitmap("CalligraphyIndex decode localBitMap");
				} catch (OutOfMemoryError e) {
					// TODO: handle exception
					softBitmap = null;
					return false;
				}
				softBitmap = new SoftReference<Bitmap>(b);
				return true;
			}
		}
		
	}
	
	private void setWidthAndHeight(){
		WindowManager m = getWindowManager();
		Display d = m.getDefaultDisplay();
		LayoutParams p = getWindow().getAttributes();
		p.height = (int)(d.getHeight() * 0.9);
		p.width = (int)(d.getWidth() * 0.9);
		getWindow().setAttributes(p);
		getWindow().setGravity(Gravity.CENTER);
	}
	
	AlertDialog.Builder builder;

	public void deleteConfirm(final int pagenum) {

		builder = new AlertDialog.Builder(this).setTitle("删除记录");
		builder.setPositiveButton("确定", new AlertDialog.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				// 删除
				Log.e(TAGS, "dialog delete confirm:" + pagenum);
				Message msg = new Message();
				msg.what = Start.PAGE_DELETE;
				msg.arg1 = pagenum;
				Start.pageChangeHandler.sendMessage(msg);
				dialog.dismiss();
				finish();
			}
		});
		builder.setNegativeButton("取消", new AlertDialog.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
				dialog.dismiss();
			}
		});
		builder.show();

	}
}
