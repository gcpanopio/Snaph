package com.onb.snaph;

import com.facebook.android.AsyncFacebookRunner;

import android.app.Application;
import android.content.SharedPreferences;
import android.graphics.Bitmap;

public class SnaphApplication extends Application{

	private Bitmap image;
	String userName = "";
	String userId = "";
	String token="";
	Bitmap userImage;
	SharedPreferences sharedPrefs;
	AsyncFacebookRunner asyncRunner;
	final String APP_ID = "366360670078534";
	
	@Override
	public void onCreate() {
        super.onCreate();
	}
	
	public void setImage(Bitmap image){
		this.image = image;
	}
	
	public Bitmap getImage(){
		return this.image;
	}
}
