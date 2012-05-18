package com.onb.snaph;

import com.facebook.android.AsyncFacebookRunner;

import android.app.Application;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.widget.ArrayAdapter;

public class SnaphApplication extends Application{

	private Bitmap image;
	private ArrayAdapter<CompressedListing> adapter;
	
	String fbUserName = "";
	String fbUserId = "";
	String fbToken="";
	Bitmap fbUserImage;
	
	
	String twitterUserName = "";
	String twitterUserId = "";
	String twitterToken="";
	Bitmap twitterUserImage;
	
	SharedPreferences sharedPrefs;
	AsyncFacebookRunner asyncRunner;
	final String APP_ID = "366360670078534";
	
	@Override
	public void onCreate() {
        super.onCreate();
	}
	
	public void setAdapter(ArrayAdapter<CompressedListing> adapter){
		this.adapter = adapter;
	}
	
	public ArrayAdapter<CompressedListing> getAdapter(){
		return adapter;
	}
	
	public void notifyAdapterChange(){
		adapter.notifyDataSetChanged();
	}
	public void setImage(Bitmap image){
		this.image = image;
	}
	
	public Bitmap getImage(){
		return this.image;
	}
}
