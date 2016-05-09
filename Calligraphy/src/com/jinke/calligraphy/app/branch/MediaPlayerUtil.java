package com.jinke.calligraphy.app.branch;

import java.io.IOException;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;

public class MediaPlayerUtil {
	private static MediaPlayerUtil util = null;
	private static MediaPlayer player = null;
	private MediaPlayerUtil(){
		
	}
	public static MediaPlayerUtil getInstance(){
		if(util == null){
			util = new MediaPlayerUtil();
			player = new MediaPlayer();
		}
		return util;
	}
	
	public void stop(){
		player.stop();
	}
	public void setSource(String path){
		if(player.isPlaying()){
			player.stop();
		}
		try {
			player.reset();
			player.setDataSource(path);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void start(){
		if(player.isPlaying()){
			player.stop();
		}
		try {
			player.prepare();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		player.start();
	}
	public boolean isPlaying(){
		return player.isPlaying();
	}
	
	public int getDuration(Uri uri){
		MediaPlayer mp = MediaPlayer.create(Start.context, uri);
		return mp.getDuration();
	}
	public void setOnStopListener(OnCompletionListener listener){
		if(listener != null)
			player.setOnCompletionListener(listener);
	}
}
