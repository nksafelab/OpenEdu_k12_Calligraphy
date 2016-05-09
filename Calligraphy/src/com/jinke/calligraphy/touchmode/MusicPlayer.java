package com.jinke.calligraphy.touchmode;

import android.media.MediaPlayer;
import android.util.Log;

public class MusicPlayer {

	public static final String TAG = "MusicPlayer";
	private MediaPlayer player;
	private int length;
	
	public MusicPlayer(MediaPlayer player){
		this.player = player;
		length = player.getDuration();
	}
	
	public void play(float x){
		Log.e(TAG , "x:"+ x);
		if(x < 400){
			player.start();
			
			Log.e(TAG,player.getDuration()+"" );
		}
	}
}
