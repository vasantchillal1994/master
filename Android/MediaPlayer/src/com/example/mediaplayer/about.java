package com.example.mediaplayer;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;

public class about extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    // Respond to the action bar's Up/Home button
	    case android.R.id.home:
	        this.finish();
	        //return true;
	    }
	    return super.onOptionsItemSelected(item);
	}
}
