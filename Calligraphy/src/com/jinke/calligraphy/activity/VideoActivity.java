package com.jinke.calligraphy.activity;

import com.jinke.calligraphy.app.branch.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

public class VideoActivity extends Activity implements OnCompletionListener, OnPreparedListener{
	private VideoView mVideoView = null;
	private MediaController mController = null;
	private String videoPath = "";
	private AlertDialog.Builder builder;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.videomain);
		
		Intent recvIntent = getIntent();
		videoPath = recvIntent.getStringExtra("videoPath");
		Uri uri = Uri.parse(videoPath);
		
		mController = new MediaController(this);
		mVideoView = (VideoView)findViewById(R.id.videoView);
		mVideoView.setOnCompletionListener(this);
		mVideoView.setOnPreparedListener(this);
//		mVideoView.setVideoPath(videoPath);
		mVideoView.setVideoURI(uri);
		mVideoView.setMediaController(mController);
		mController.setKeepScreenOn(true);
		mVideoView.start();
		
		
	}
	@Override
	public void onPrepared(MediaPlayer mp) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onCompletion(MediaPlayer mp) {
		// TODO Auto-generated method stub
		
		builder = new AlertDialog.Builder(this);
		builder.setTitle("是否重新播放");
		builder.setMessage("已经播放完成，是否重新播放");
		builder.setNegativeButton("退出播放界面", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		builder.setPositiveButton("重新播放", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				mVideoView.setVideoPath(videoPath);
				mVideoView.start();
				
			}
		});
		builder.show();
	}
}
