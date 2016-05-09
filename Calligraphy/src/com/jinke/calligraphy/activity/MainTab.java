package com.jinke.calligraphy.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.jinke.calligraphy.app.branch.R;

import android.app.ActivityGroup;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TabHost;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class MainTab extends ActivityGroup implements OnCheckedChangeListener {
	private final static String TAGS = "MainTab";
	private TabHost mHost;
	private Intent localIntent;
	private Intent cloudIntent;

	private List<String> dataCloud;
	private List<String> dataLocal;

	private ArrayList<String> recvArrayList;
	List<Map<String,Integer>>backupNameAndNumber; //备份时间文件名和每次备份的份数的拇指图数量
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.maintabs);
		
		Intent recvIntent = getIntent();
		recvArrayList = (recvIntent.getExtras()).getStringArrayList("dirList");
		
		Log.e(TAGS, "recvArrayList:" + recvArrayList);

		mHost = (TabHost) findViewById(R.id.tabhost);
		mHost.setup(this.getLocalActivityManager());

		init(); 
	}

	private void init() {
		initList();
		initCloud();
		initLocal();
		initRadios();
		setupIntent();
		mHost.setCurrentTab(1);
	}

	private void initThumbCountOfPerPage() {
		if (Properyt.USETHIS == 0) {
			Intent intent = this.getIntent();
			Bundle bundle = intent.getExtras();
		}
	}


	private void initList() {
		dataCloud = new ArrayList<String>();
		dataLocal = new ArrayList<String>();

		String s = "";
		for(int i=recvArrayList.size() -1 ;i>=0;i--){
			s = recvArrayList.get(i);
			if(s.contains("_local"))
				dataLocal.add(s);
			else
				dataCloud.add(s);
		}
	}

	private void initCloud() {
		Log.e("locallist", dataCloud + "");
		cloudIntent = new Intent(this, Cloud.class);
		Bundle extra = new Bundle();
		extra.putStringArrayList(Properyt.CLOUD_ARRAYLIST,
				(ArrayList<String>) dataCloud);
		cloudIntent.putExtras(extra);
	}

	private void initLocal() {
		Log.e("locallist", dataLocal + "");
		localIntent = new Intent(this, Local.class);
		Bundle extra = new Bundle();
		extra.putStringArrayList(Properyt.LOCAL_ARRAYLIST,
				(ArrayList<String>) dataLocal);
		localIntent.putExtras(extra);
	}

	private void initRadios() {
		((RadioButton) findViewById(R.id.radio_button0))
				.setOnCheckedChangeListener(this);
		((RadioButton) findViewById(R.id.radio_button1))
				.setOnCheckedChangeListener(this);

	}

	private void setupIntent() {

		mHost
				.addTab(buildTabSpec("cloud", "网络存储", R.drawable.icon,
						localIntent));

		mHost
				.addTab(buildTabSpec("local", "本地存储", R.drawable.icon,
						cloudIntent));

	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (isChecked) {
			switch (buttonView.getId()) {
			case R.id.radio_button0:
				this.mHost.setCurrentTabByTag("local");
				break;
			case R.id.radio_button1:
				this.mHost.setCurrentTabByTag("cloud");
				break;
			}
		}
	}

	private TabHost.TabSpec buildTabSpec(String tag, String resLabel,
			int resIcon, final Intent content) {
		return this.mHost.newTabSpec(tag).setIndicator(resLabel,
				this.getResources().getDrawable(resIcon)).setContent(content);
	}
}