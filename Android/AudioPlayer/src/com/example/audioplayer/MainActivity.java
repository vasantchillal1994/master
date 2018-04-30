package com.example.audioplayer;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends Activity implements RecognitionListener {
	SpeechRecognizer speech;
	Intent recognizerIntent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		speech = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());
		speech.setRecognitionListener(this);
		recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en");
		recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getApplicationContext().getPackageName());
		recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
		recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
		speech.startListening(recognizerIntent);
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

	@Override
	public void onReadyForSpeech(Bundle params) {
		Toast.makeText(this, "onReadyForSpeech", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onBeginningOfSpeech() {
		Toast.makeText(this, "onBeginningOfSpeech", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onRmsChanged(float rmsdB) {
		Toast.makeText(this, "onRmsChanged", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onBufferReceived(byte[] buffer) {
		Toast.makeText(this, "onBufferReceived", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onEndOfSpeech() {
		Toast.makeText(this, "onEndOfSpeech", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onError(int error) {
		String message = "";
		switch (error) {
	      case SpeechRecognizer.ERROR_AUDIO:
	          message = "R.string.error_audio_error";
	          break;
	      case SpeechRecognizer.ERROR_CLIENT:
	          message = "R.string.error_client";
	          break;
	      case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
	          message = "R.string.error_permission";
	          break;
	      case SpeechRecognizer.ERROR_NETWORK:
	          message = "R.string.error_network";
	          break;
	      case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
	          message = "R.string.error_timeout";
	          break;
	      case SpeechRecognizer.ERROR_NO_MATCH:
	          message = "R.string.error_no_match";
	          break;
	      case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
	          message = "R.string.error_busy";
	          break;
	      case SpeechRecognizer.ERROR_SERVER:
	          message = "R.string.error_server";
	          break;
	      case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
	          message = "R.string.error_timeout";
	          break;
	      default:
	          message = "R.string.error_understand";
	          break;
	}
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onResults(Bundle results) {
		ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
		Toast.makeText(this, matches.get(0), Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onPartialResults(Bundle partialResults) {
		Toast.makeText(this, "onPartialResults", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onEvent(int eventType, Bundle params) {
		Toast.makeText(this, "onEvent", Toast.LENGTH_SHORT).show();
	}
}