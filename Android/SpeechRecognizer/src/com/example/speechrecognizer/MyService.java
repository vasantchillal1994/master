package com.example.speechrecognizer;

import java.util.ArrayList;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.Toast;

public class MyService extends Service implements RecognitionListener {
	private SpeechRecognizer speechRecognizer;
	
    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
    
    @Override
    public void onCreate() {
        Toast.makeText(this, "My Service Created", Toast.LENGTH_LONG).show();
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());
        speechRecognizer.setRecognitionListener(this);
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        speechRecognizer.startListening(intent); 
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "My Service Stopped", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onStart(Intent intent, int startid) {
        Toast.makeText(this, "My Service Started", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBeginningOfSpeech() {
    	Toast.makeText(this, "onBeginningOfSpeech", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
    	Toast.makeText(this, "onBufferReceived", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onEndOfSpeech() {
    	Toast.makeText(this, "onEndOfSpeech", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onError(int error) {
    	Toast.makeText(this, "onError", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onEvent(int eventType, Bundle params) {
    	Toast.makeText(this, "onEvent", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPartialResults(Bundle partialResults) {
    	Toast.makeText(this, "onPartialResults", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onReadyForSpeech(Bundle params) {
    	Toast.makeText(this, "onReadyForSpeech", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onResults(Bundle results) {
    	Toast.makeText(this, "onResults", Toast.LENGTH_LONG).show();
		ArrayList<?> strlist = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
		for (int i = 0; i < strlist.size();i++ ) {
			Log.d("Speech", "result=" + strlist.get(i));
		}
    }

    @Override
    public void onRmsChanged(float rmsdB) {
    	Toast.makeText(this, "onRmsChanged", Toast.LENGTH_LONG).show();
    }
    
    public class MyBinder extends Binder {
    	MyService getService() {
			return MyService.this;
		}
	}
}
