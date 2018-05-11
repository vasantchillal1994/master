package com.example.mediaplayerservice;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {
	Button start, stop;
	
	private int mBindFlag;
	private Messenger mServiceMessenger;
	
	final ServiceConnection mServiceConnection = new ServiceConnection()
	{
		@Override
		public void onServiceConnected(ComponentName name, IBinder service)
		{
			mServiceMessenger = new Messenger(service);
			Message msg = new Message();
			msg.what = ServiceClass.MSG_RECOGNIZER_START_LISTENING;
			ServiceClass.setContext(getApplicationContext());
			try
			{
				mServiceMessenger.send(msg);
			} 
			catch (RemoteException e)
			{
				e.printStackTrace();
			}
		}
		@Override
		public void onServiceDisconnected(ComponentName name)
		{
			mServiceMessenger = null;
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent service = new Intent(MainActivity.this, ServiceClass.class);
		MainActivity.this.startService(service);
	    mBindFlag = Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH ? 0 : Context.BIND_ABOVE_CLIENT;
		setContentView(R.layout.activity_main);
		start = (Button) findViewById(R.id.start);
		start.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				bindService(new Intent(MainActivity.this, ServiceClass.class), mServiceConnection, mBindFlag);
				startService(new Intent(MainActivity.this, ServiceClass.class));
				start.setEnabled(false);
				stop.setEnabled(true);
			}
		});
		stop = (Button) findViewById(R.id.stop);
		stop.setEnabled(false);
		stop.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mServiceMessenger != null)
			    {
					unbindService(mServiceConnection);
					stopService(new Intent(MainActivity.this, ServiceClass.class));
			        mServiceMessenger = null;
			        stop.setEnabled(false);
			        start.setEnabled(true);
			    }
			}
		});
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
}
