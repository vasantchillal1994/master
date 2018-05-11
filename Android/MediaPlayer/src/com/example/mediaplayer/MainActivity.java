package com.example.mediaplayer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

import com.example.mediaplayer.MusicService.MusicBinder;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.speech.RecognizerIntent;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.MediaController.MediaPlayerControl;
import android.speech.tts.TextToSpeech;
import android.util.Log;

public class MainActivity extends Activity implements MediaPlayerControl, OnKeyListener, TextToSpeech.OnInitListener {
	private ArrayList<Song> songList;
	private ListView songView;
	
	private MusicService musicSrv;
	private Intent playIntent;
	private boolean musicBound=false;
	
	private MusicController controller;
	
	private boolean paused=false, playbackPaused=false;
	MediaButtonIntentReceiver mbir = new MediaButtonIntentReceiver();
	
	public ImageButton playButton;
	public ImageButton stopButton;
	public ImageButton skipButton;
	
	public TextToSpeech tts;
	public String speechText;
	public String number;
	
	private static final int REQ_CODE_SPEECH_INPUT = 100;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		tts = new TextToSpeech(this, this);
		IntentFilter mediaFilter = new IntentFilter(Intent.ACTION_MEDIA_BUTTON);
		mediaFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
		mbir.setMainActivityHandler(this);
		registerReceiver(mbir, mediaFilter);
		setContentView(R.layout.activity_main);
		songView = (ListView) findViewById(R.id.song_list);
		songList = new ArrayList<Song>();
		getSongList();
		Collections.sort(songList, new Comparator<Song>() {
			public int compare(Song a, Song b) {
				return a.getTitle().compareTo(b.getTitle());
			}
		});
		SongAdapter songAdt = new SongAdapter(this, songList);
		songView.setAdapter(songAdt);
		//setController();
	}
	
	@Override
	protected void onPause(){
		super.onPause();
		paused=true;
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		if(paused){
			setController();
			paused=false;
		}
	}
	
	@Override
	protected void onStop() {
		//controller.hide();
		super.onStop();
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		if(playIntent==null){
			playIntent = new Intent(this, MusicService.class);
			bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
			startService(playIntent);
		}
	}
	
	private ServiceConnection musicConnection = new ServiceConnection(){
	 
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			MusicBinder binder = (MusicBinder)service;
			musicSrv = binder.getService();
			musicSrv.setList(songList);
			musicBound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			musicBound = false;
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_shuffle:
			musicSrv.setShuffle();
			break;
		case R.id.action_end:
			stopService(playIntent);
			musicSrv=null;
			System.exit(0);
			break;
		case R.id.about:
			Intent i = new Intent(MainActivity.this, about.class);
			startActivity(i);
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onDestroy() {
		stopService(playIntent);
		musicSrv=null;
		unregisterReceiver(mbir);
		super.onDestroy();
	}
	
	public void getSongList() {
		ContentResolver musicResolver = getContentResolver();
		Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
		Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);
		if(musicCursor!=null && musicCursor.moveToFirst()) {
			int titleColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE);
			int idColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media._ID);
			int artistColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ARTIST);
			do {
				long thisId = musicCursor.getLong(idColumn);
				String thisTitle = musicCursor.getString(titleColumn);
				String thisArtist = musicCursor.getString(artistColumn);
				songList.add(new Song(thisId, thisTitle, thisArtist));
			} while(musicCursor.moveToNext());
		}
	}
	
	public void songPicked(View view){
		musicSrv.setSong(Integer.parseInt(view.getTag().toString()));
		musicSrv.playSong();
		if(playbackPaused){
			setController();
			playbackPaused=false;
		}
		//controller.show(0);
	}
	
	public void playNext() {
		musicSrv.playNext();
		if(playbackPaused) {
			setController();
		    playbackPaused=false;
		}
		//controller.show(0);
	}
	
	public void playPrev() {
		musicSrv.playPrev();
		if(playbackPaused){
			setController();
		    playbackPaused=false;
		}
		//controller.show(0);
	}
	
	private void setController(){
		controller = new MusicController(this);
		controller.setPrevNextListeners(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				playNext();
			}
		}, new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				playPrev();
			}
		});
		controller.setMediaPlayer(this);
		controller.setAnchorView(findViewById(R.id.song_list));
	}

	@Override
	public void start() {
		musicSrv.go();
	}

	@Override
	public void pause() {
		playbackPaused=true;
		musicSrv.pausePlayer();
	}

	@Override
	public int getDuration() {
		if(musicSrv!=null && musicBound && musicSrv.isPng())
			return musicSrv.getDur();
		else
			return 0;
	}

	@Override
	public int getCurrentPosition() {
		if(musicSrv!=null && musicBound && musicSrv.isPng())
			return musicSrv.getPosn();
		else
			return 0;
	}

	@Override
	public void seekTo(int pos) {
		musicSrv.seek(pos);
	}

	@Override
	public boolean isPlaying() {
		if(musicSrv!=null && musicBound)
			return musicSrv.isPng();
		return false;
	}

	@Override
	public int getBufferPercentage() {
		return 0;
	}

	@Override
	public boolean canPause() {
		return true;
	}

	@Override
	public boolean canSeekBackward() {
		return true;
	}

	@Override
	public boolean canSeekForward() {
		return true;
	}

	@Override
	public int getAudioSessionId() {
		return 0;
	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		return false;
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		super.onKeyDown(keyCode, event);
		switch (keyCode) {
			case KeyEvent.KEYCODE_MEDIA_NEXT:
				Toast.makeText(this, "NEXT Main", Toast.LENGTH_SHORT).show();
				return true;
			case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
				Toast.makeText(this, "PREVIOUS Main", Toast.LENGTH_SHORT).show();
				return true;
			case KeyEvent.KEYCODE_HEADSETHOOK:
				startVoiceInput();
				Toast.makeText(this, "HEADSETHOOK Main", Toast.LENGTH_SHORT).show();
				return true;
		}
		return false;
	}
	
	public void startVoiceInput() {
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
		intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Hello");
		try {
			startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
		} catch (ActivityNotFoundException a) {
			
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
			case REQ_CODE_SPEECH_INPUT: {
				if(resultCode == RESULT_OK && null != data) {
					ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
					String voice = result.get(0);
					Toast.makeText(this, voice, Toast.LENGTH_SHORT).show();
					if(voice.toLowerCase(Locale.ENGLISH).contains("next")) {
						playNext();
					}
					else if(voice.toLowerCase(Locale.ENGLISH).contains("previous")) {
						playPrev();
					}
					else if(voice.toLowerCase(Locale.ENGLISH).contains("stop")||voice.toLowerCase(Locale.ENGLISH).contains("pause")) {
						pause();
					}
					else if(voice.toLowerCase(Locale.ENGLISH).contains("start")||voice.toLowerCase(Locale.ENGLISH).contains("play")) {
						playNext();
						playPrev();
					}
					else if(voice.toLowerCase(Locale.ENGLISH).contains("call")) {
						String[] split = voice.split(" ", 2);
						Search(split[1]);
					}
					else if(voice.toLowerCase(Locale.ENGLISH).contains("lock") && voice.toLowerCase(Locale.ENGLISH).contains("screen")) {
						speechText="Under Development";
						speakOut(speechText);
					}
					else if(voice.toLowerCase(Locale.ENGLISH).contains("shuffle")) {
						musicSrv.setShuffle();
					}
					else if(voice.toLowerCase(Locale.ENGLISH).contains("time") && voice.toLowerCase(Locale.ENGLISH).contains("now")) {
						Calendar calander = Calendar.getInstance();
						SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm", Locale.US);
						String time = simpleDateFormat.format(calander.getTime());
						speechText=time;
						speakOut(speechText);
					}
					else if(voice.toLowerCase(Locale.ENGLISH).contains("close")) {
						finish();
						System.exit(0);
					}
					else {
						speechText="Sorry! Unable to serve your command";
						speakOut(speechText);
					}
				}
				break;
			}
		}
	}
	
	protected void Search(String searchName) {
		Uri contacts = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
		boolean flag = true;
		Cursor c = getContentResolver().query(contacts, null, null, null, null);
		if(c != null) {
			if(c.moveToFirst()) {
				do {
					String diplayName = getValue(c, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
					number = getValue(c, ContactsContract.CommonDataKinds.Phone.NUMBER);
					if(Check(diplayName, c, searchName) == true) {
						Toast.makeText(this, searchName, Toast.LENGTH_SHORT).show();
						Intent callIntent = new Intent(Intent.ACTION_CALL);  
		                callIntent.setData(Uri.parse("tel:"+number));  
		                startActivity(callIntent);
		                flag = false;
						break;
					}
				}while(c.moveToNext());
			}
			if(flag) {
				speakOut(searchName+" was not fond");
			}
		}
		if(c != null) {
			c.close();
		}
	}
	
	private String getValue(Cursor cursor, String name) {
		return cursor.getString(cursor.getColumnIndex(name));
	}
	
	private boolean Check(String Name, Cursor c, String searchName) {
		boolean Found = false;
		String txt = searchName.toLowerCase(Locale.ENGLISH);
		Name = Name.toLowerCase(Locale.ENGLISH);
		if(txt.equals(Name)) {
			Found = true;
		}
		return Found;
	}
	
	private void speakOut(String speechText) {
		pause();
        tts.speak(speechText, TextToSpeech.QUEUE_FLUSH, null, null);
        start();
    }

	@Override
	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.US);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.d("TTS", "This Language is not supported");
            } else {
                speakOut("Welcome");
            }
 
        } else {
            Log.d("TTS", "Initilization Failed!");
        }
	}

}
