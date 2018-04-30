package com.example.mediaplayer;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SongAdapter extends BaseAdapter {
	private ArrayList<Song> songs;
	private LayoutInflater songInf;
	
	public SongAdapter(Context c, ArrayList<Song> theSongs){
	  songs=theSongs;
	  songInf=LayoutInflater.from(c);
	}

	@Override
	public int getCount() {
		return songs.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LinearLayout songLay = (LinearLayout) songInf.inflate(R.layout.song, parent, false);
		TextView songView = (TextView) songLay.findViewById(R.id.song_title);
		TextView artistView = (TextView) songLay.findViewById(R.id.song_artist);
		Song currong = songs.get(position);
		songView.setText(currong.getTitle());
		songView.setTextColor(Color.parseColor("#ffffff"));
		artistView.setText(currong.getArtist());
		artistView.setTextColor(Color.parseColor("#000000"));
		songLay.setTag(position);
		return songLay;
	}

}
