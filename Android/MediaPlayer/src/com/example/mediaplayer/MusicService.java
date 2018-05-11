package com.example.mediaplayer;

import java.util.ArrayList;
import java.util.Random;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {
	private MediaPlayer player;
	private ArrayList<Song> songs;
	private int songPosn;
	
	private String songTitle="";
	private static final int NOTIFY_ID=1;
	
	private boolean shuffle=false;
	private Random rand;
	
	public Notification not;
	Intent notIntent;
	PendingIntent pendInt;
	Notification.Builder builder;
	
	private final IBinder musicBind = new MusicBinder();

	@Override
	public IBinder onBind(Intent intent) {
		return musicBind;
	}
	
	@Override
	public boolean onUnbind(Intent intent){
		player.stop();
		player.release();
		return false;
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		if(player.getCurrentPosition()>=0){
			mp.reset();
			playNext();
		}
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		mp.reset();
		return false;
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		mp.start();
		notIntent = new Intent(this, MainActivity.class);
		notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		pendInt = PendingIntent.getActivity(this, 0, notIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		builder = new Notification.Builder(this);
		builder.setContentIntent(pendInt).setSmallIcon(R.drawable.play).setTicker(songTitle).setOngoing(true).setContentTitle("Playing").setContentText(songTitle);
		not = builder.build();
		startForeground(NOTIFY_ID, not);
	}
	
	public void updateNotifiction() {
		builder.setContentIntent(pendInt).setSmallIcon(R.drawable.play).setTicker(songTitle).setOngoing(true).setContentTitle("Playing").setContentText(songTitle);
		builder.setContentText(songTitle);
		not = builder.build();
		startForeground(NOTIFY_ID, not);
	}
	
	@Override
	public void onDestroy() {
		stopForeground(true);
	}
	
	public void setList(ArrayList<Song> theSongs){
	  songs=theSongs;
	}
	
	public class MusicBinder extends Binder {
	  MusicService getService() {
	    return MusicService.this;
	  }
	}
	
	public void setSong(int songIndex){
		songPosn=songIndex;
	}
	
	public void onCreate() {
		super.onCreate();
		rand = new Random();
		songPosn = 0;
		player = new MediaPlayer();
		initMusicPlayer();
	}
	
	public void setShuffle() {
		if(shuffle) {
			shuffle=false;
			Toast.makeText(this, "shuffle is off", Toast.LENGTH_SHORT).show();
		} else {
			shuffle=true;
			Toast.makeText(this, "shuffle is on", Toast.LENGTH_SHORT).show();
		}
	}
	
	public void initMusicPlayer() {
		player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
		player.setAudioStreamType(AudioManager.STREAM_MUSIC);
		player.setOnPreparedListener(this);
		player.setOnCompletionListener(this);
		player.setOnErrorListener(this);
	}
	
	public void playSong() {
		player.reset();
		Song playSong = songs.get(songPosn);
		long currSong = playSong.getId();
		songTitle = playSong.getTitle();
		Uri trackUri = ContentUris.withAppendedId(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, currSong);
		try{
			player.setDataSource(getApplicationContext(), trackUri);
		}catch(Exception e){
			Log.e("MUSIC SERVICE", "Error setting data source", e);
		}
		player.prepareAsync();
	}
	
	public int getPosn(){
		return player.getCurrentPosition();
	}
	
	public int getDur(){
		return player.getDuration();
	}
	
	public boolean isPng(){
		return player.isPlaying();
	}
	
	public void pausePlayer(){
		player.pause();
	}
	
	public void seek(int posn){
		player.seekTo(posn);
	}
	
	public void go(){
		player.start();
	}
	
	public void playPrev(){
		songPosn--;
		if(songPosn < 0)
			songPosn=songs.size()-1;
		playSong();
	}
	
	public void playNext(){
		if(shuffle){
			int newSong = songPosn;
			while(newSong==songPosn) {
				newSong=rand.nextInt(songs.size());
			}
			songPosn=newSong;
		}
		else {
			songPosn++;
			if(songPosn >= songs.size())
				songPosn=0;
	  }
	  playSong();
	}

}
