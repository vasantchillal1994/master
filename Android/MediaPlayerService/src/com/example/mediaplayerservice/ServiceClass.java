package com.example.mediaplayerservice;

import com.example.mediaplayerservice.ServiceClass;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

public class ServiceClass extends Service {
	
	protected static AudioManager mAudioManager; 
    protected SpeechRecognizer mSpeechRecognizer;
    protected static Context context;
    protected Intent mSpeechRecognizerIntent;
    protected final Messenger mServerMessenger = new Messenger(new IncomingHandler(this));
    
    protected static SpeechRecognitionListener instance = null;

    protected boolean mIsListening;
    protected volatile boolean mIsCountDownOn;
    private static boolean mIsStreamSolo;

    static final int MSG_RECOGNIZER_START_LISTENING = 1;
    static final int MSG_RECOGNIZER_CANCEL = 2;

	@Override
	public IBinder onBind(Intent intent) {
		return mServerMessenger.getBinder();
	}
	
	public static void setContext(Context con) {
		context = con;
	}
	
	public void onCreate(){
		super.onCreate();
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		startListening();
	}
	
	protected static class IncomingHandler extends Handler
    {
		private WeakReference<ServiceClass> mtarget;
		
		IncomingHandler(ServiceClass target)
        {
            mtarget = new WeakReference<ServiceClass>(target);
        }
		
		@Override
        public void handleMessage(Message msg)
        {
            final ServiceClass target = mtarget.get();
            switch (msg.what)
            {
                case MSG_RECOGNIZER_START_LISTENING:
                	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                    {
                        // turn off beep sound  
                        if (!mIsStreamSolo)
                        {
                            mAudioManager.setStreamSolo(AudioManager.STREAM_VOICE_CALL, true);
                            mIsStreamSolo = true;
                        }
                    }
                     if (!target.mIsListening)
                     {
                         target.mSpeechRecognizer.startListening(target.mSpeechRecognizerIntent);
                         target.mIsListening = true;
                         Log.d("message start listening", "message start listening"); //$NON-NLS-1$
                     }
                     break;
                 case MSG_RECOGNIZER_CANCEL:
                    if (mIsStreamSolo)
                    {
                        mAudioManager.setStreamSolo(AudioManager.STREAM_VOICE_CALL, false);
                        mIsStreamSolo = false;
                    }
                    target.mSpeechRecognizer.cancel();
                    target.mIsListening = false;
                    Log.d("message canceled recognizer", "message canceled recognizer"); //$NON-NLS-1$
                    break;
             }
        }
    }
	
	// Count down timer for Jelly Bean work around
    protected CountDownTimer mNoSpeechCountDown = new CountDownTimer(5000, 5000)
    {
        @Override
        public void onTick(long millisUntilFinished)
        {
        }

        @Override
        public void onFinish()
        {
            mIsCountDownOn = false;
            Message message = Message.obtain(null, MSG_RECOGNIZER_CANCEL);
            try
            {
                mServerMessenger.send(message);
                message = Message.obtain(null, MSG_RECOGNIZER_START_LISTENING);
                mServerMessenger.send(message);
            }
            catch (RemoteException e)
            {
            }
        }
    };
    
    @Override
    public void onDestroy()
    {
        super.onDestroy();
        stopListening();
    }
    
    protected void stopListening() {
    	if (mIsCountDownOn)
        {
            mNoSpeechCountDown.cancel();
        }
        if (mSpeechRecognizer != null)
        {
            mSpeechRecognizer.destroy();
        }
    }
    
    protected void startListening() {
    	initSpeech();
    	try {
    		mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
    		mSpeechRecognizer.setRecognitionListener(new SpeechRecognitionListener());
    		mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
    		mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
    		mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    
    protected void initSpeech() {
    	if (mSpeechRecognizer == null) {
    		mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
    		if (!SpeechRecognizer.isRecognitionAvailable(context)) {
    			Toast.makeText(context, "Speech Recognition is not available",Toast.LENGTH_LONG).show();
    			stopListening();
    		}
    		mSpeechRecognizer.setRecognitionListener(new SpeechRecognitionListener().getInstance());
    	}
	}
    
    protected void restartListeningService() {
    	stopListening();
    	startListening();
    }
    
    public class SpeechRecognitionListener implements RecognitionListener, TextToSpeech.OnInitListener
    {
    	//public TextToSpeech tts;
    	@Override
        public void onBeginningOfSpeech()
        {
    		//tts = new TextToSpeech(getApplicationContext(), this);
            // speech input will be processed, so there is no need for count down anymore
            if (mIsCountDownOn)
            {
                mIsCountDownOn = false;
                mNoSpeechCountDown.cancel();
            }               
            Log.d("onBeginingOfSpeech", "onBeginingOfSpeech"); //$NON-NLS-1$
        }

    	public RecognitionListener getInstance() {
    		if (instance == null) {
    			instance = this;
    		}
    		return instance;
		}

		@Override
        public void onBufferReceived(byte[] buffer)
        {
        }
    	
    	@Override
        public void onEndOfSpeech()
        {
            Log.d("onEndOfSpeech", "onEndOfSpeech"); //$NON-NLS-1$
        }
    	
    	@Override
        public void onError(int error)
        {
            if (mIsCountDownOn)
            {
                mIsCountDownOn = false;
                mNoSpeechCountDown.cancel();
            }
             mIsListening = false;
             Message message = Message.obtain(null, MSG_RECOGNIZER_START_LISTENING);
             try
             {
                    mServerMessenger.send(message);
                    Log.d("msg = ", message.toString());
             }
             catch (RemoteException e)
             {
            	 Log.d("hi = ",e.getMessage());
             }
            Log.d("error = ", "error = " + error); //$NON-NLS-1$
        }
    	
    	@Override
        public void onEvent(int eventType, Bundle params)
        {
        }

        @Override
        public void onPartialResults(Bundle partialResults)
        {
        }

        @Override
        public void onReadyForSpeech(Bundle params)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            {
                mIsCountDownOn = true;
                mNoSpeechCountDown.start();
            }
            Log.d("onReadyForSpeech", "onReadyForSpeech"); //$NON-NLS-1$
        }
        
        @Override
        public void onResults(Bundle results)
        {
            Log.d("onResults", "onResults"); //$NON-NLS-1$
            ArrayList<String> data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            String word = (String) data.get(0);
            //speakOut(word);
            Toast.makeText(getApplicationContext(), word, Toast.LENGTH_SHORT).show();
            restartListeningService();
        }

        @Override
        public void onRmsChanged(float rmsdB)
        {
        }
        
        //private void speakOut(String speechText) {
           // tts.speak(speechText, TextToSpeech.QUEUE_FLUSH, null, null);
       // }

		@Override
		public void onInit(int status) {
			if (status == TextToSpeech.SUCCESS) {
	            //int result = tts.setLanguage(Locale.US);
	            //if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
	             //   Log.d("TTS", "This Language is not supported");
	            //} else {
	                //speakOut("Welcome");
	           // }
	 
	        } else {
	            Log.d("TTS", "Initilization Failed!");
	        }
		}
    }
    
	public int onStartCommand(Intent intent, int flags, int startId){
		return 1;
	}

}
