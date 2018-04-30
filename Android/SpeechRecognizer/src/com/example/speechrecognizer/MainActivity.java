package com.example.speechrecognizer;

import com.example.speechrecognizer.MyService.MyBinder;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {
	Intent intent;
	Button start, stop;
	MyService mBoundService;
	boolean mServiceBound = false;
	private Intent playIntent;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		intent = new Intent(MainActivity.this, MyService.class);
		start = (Button) findViewById(R.id.Start);
		stop = (Button) findViewById(R.id.Stop);
		start.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, MyService.class);
		        startService(intent);
		        bindService(intent, musicConnection, Context.BIND_AUTO_CREATE);
			}
		});
		stop.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (mServiceBound) {
                    unbindService(musicConnection);
                    mServiceBound = false;
                }
                Intent intent = new Intent(MainActivity.this, MyService.class);
				stopService(intent);
			}
		});
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		if(playIntent==null){
			playIntent = new Intent(this, MyService.class);
			bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
			startService(playIntent);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private ServiceConnection musicConnection = new ServiceConnection(){
		 
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			MyBinder binder = (MyBinder)service;
			mBoundService = binder.getService();
			mServiceBound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mServiceBound = false;
		}
	};
}
