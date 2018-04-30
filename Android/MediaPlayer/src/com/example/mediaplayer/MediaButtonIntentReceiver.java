package com.example.mediaplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

public class MediaButtonIntentReceiver extends BroadcastReceiver {
	private final static String TAG = "gauntface";
	static boolean myFlag=true;

	public MediaButtonIntentReceiver() {
        super();
    }
	
	MainActivity main = null;
	void setMainActivityHandler(MainActivity main){
	    this.main=main;
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "HardButtonReceiver: Button press received");
		abortBroadcast();

		// Pull out the KeyEvent from the intent
		KeyEvent key = (KeyEvent) intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);

		// This is just some example logic, you may want to change this for different behaviour
		if(key.getAction() == KeyEvent.KEYCODE_UNKNOWN)
		{
			int keycode = key.getKeyCode();

			// These are examples for detecting key presses on a Nexus One headset
			if(keycode == KeyEvent.KEYCODE_MEDIA_NEXT)
			{
				if(myFlag) {
					Toast.makeText(context, "NEXT START", Toast.LENGTH_SHORT).show();
					main.startVoiceInput();
					myFlag=false;
					//startVoiceInput();
				}
				else
					myFlag=true;
				Toast.makeText(context, "NEXT End", Toast.LENGTH_SHORT).show();
			}
			else if(keycode == KeyEvent.KEYCODE_MEDIA_PREVIOUS)
			{
				Toast.makeText(context, "PREVIOUS", Toast.LENGTH_SHORT).show();
			}
			else if(keycode == KeyEvent.KEYCODE_HEADSETHOOK)
			{
				Toast.makeText(context, "HEADSETHOOK", Toast.LENGTH_SHORT).show();
			}
			else {
				Toast.makeText(context, "UNKNOWN", Toast.LENGTH_SHORT).show();
			}
		}
	}
	
}
