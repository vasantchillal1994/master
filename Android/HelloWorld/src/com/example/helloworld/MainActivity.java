package com.example.helloworld;

import java.util.ArrayList;
import java.util.Locale;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.net.Uri;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class MainActivity extends Activity {
	private static final int REQ_CODE_SPEECH_INPUT = 100;
	private TextView mVoiceInputTv;
    private ImageButton mSpeakBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mVoiceInputTv = (TextView) findViewById(R.id.voiceInput);
		mSpeakBtn = (ImageButton) findViewById(R.id.btnSpeak);
		mSpeakBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startVoiceInput();
			}
		});
	}
	
	private void startVoiceInput() {
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
		intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Hello");
		try {
			startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
		} catch (ActivityNotFoundException a) {
			
		}
	}
	
	/*protected void Search() {
		Uri contacts = android.provider.ContactsContract.Contacts.CONTENT_URI;
		Cursor c = getContentResolver().query(contacts, null, null, null, null);
		if(c != null) {
			if(c.moveToFirst()) {
				do {
					String display_ContactsName = getValue(c, android.provider.ContactsContract.Contacts.DISPLAY_NAME);
					if(Check(display_ContactsName, c) == true) {
						break;
					}
				}while(c.moveToNext());
			}
		}
		if(c != null) {
			c.close();
		}
	}
	
	private String getValue(Cursor cursor, String name) {
		return cursor.getString(cursor.getColumnIndex(name));
	}
	
	private boolean Check(String Name, Cursor c) {
		boolean Found = false;
		String txt = "Bhushan ESS";
		if(txt.equals(Name)) {
			AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
			alert.setTitle("Hey This Contact Is in the LIST!!!");
			alert.setMessage("The contact "+Name+" was found in this phone... oh ya");
			alert.setPositiveButton("ok", new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
			});
			alert.show();
		}
		return Found;
	}*/
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
			case REQ_CODE_SPEECH_INPUT: {
				if(resultCode == RESULT_OK && null != data) {
					ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
					mVoiceInputTv.setText(result.get(0));
					//Search();
				}
				break;
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
