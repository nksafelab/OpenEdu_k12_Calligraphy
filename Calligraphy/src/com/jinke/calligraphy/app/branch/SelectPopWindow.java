package com.jinke.calligraphy.app.branch;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

public class SelectPopWindow extends Activity implements OnClickListener{

	private Button btn_chapter1;
	private Button btn_chapter2;
	private LinearLayout layout;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_alert_dialog);
		btn_chapter1 = (Button)findViewById(R.id.btn_1);
		btn_chapter2 = (Button)findViewById(R.id.btn_2);
		
		layout = (LinearLayout)findViewById(R.id.pop_layout);
		
		layout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Toast.makeText(Start.context, "点击关闭窗口", Toast.LENGTH_SHORT).show();
			}
		});
		
		btn_chapter1.setOnClickListener(this);
		btn_chapter2.setOnClickListener(this);
		
	}
	
	public boolean onTouchEvent(MotionEvent event) {
		finish();
		return true;
	}
	
	
	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch(arg0.getId()){
		case R.id.btn_1:
			break;
		case R.id.btn_2:
			break;
		default:
			break;
		}
		finish();
	}
	

}
