package com.jinke.calligraphy.touchmode;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.jinke.calligraphy.app.branch.MyView;
import com.jinke.calligraphy.app.branch.R;
import com.jinke.calligraphy.app.branch.Start;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class SideUpMode implements TouchMode{
	
	MyView view;
	
	public SideUpMode(MyView v) {
		this.view = v;
		doCreate();
	}
	
	public void doCreate(){
		
	}

	@Override
	public void touch_action_down(MotionEvent event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void touch_action_pointer_down(MotionEvent event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void touch_move(MotionEvent event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void touch_action_pointer_up(MotionEvent event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void touch_up(MotionEvent event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void draw(Canvas canvas) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void printInfo() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Matrix getMatrix() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}
	
	public static void my_toast(String msg){
	    LayoutInflater inflater = (Start.instance).getLayoutInflater();
	    View layout = inflater.inflate(R.layout.toast,
	                                   (ViewGroup)Start.instance.findViewById(R.id.toast_layout_root));

	    ImageView image = (ImageView) layout.findViewById(R.id.image);
	    TextView text = (TextView) layout.findViewById(R.id.text);
	    text.setText(msg);

	    Toast toast = new Toast((Start.instance).getApplicationContext());
	    toast.setGravity(Gravity.BOTTOM, 0, 100);
	    toast.setDuration(Toast.LENGTH_LONG);
	    toast.setView(layout);
	    try
	    {
	        //  从Toast对象中获得mTN变量
	        Field field = toast.getClass().getDeclaredField("mTN");
	        field.setAccessible(true);
	                Object obj = field.get(toast);
	        //  TN对象中获得了show方法
	                Method method =  obj.getClass().getDeclaredMethod("show", null);
	        //  调用show方法来显示Toast信息提示框
	                method.invoke(obj, null);
	    }
	    catch (Exception e)
	    {
	    }
	   
	    toast.show();
	}

}
