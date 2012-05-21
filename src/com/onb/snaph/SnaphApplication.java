package com.onb.snaph;


import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import twitter4j.Twitter;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.Facebook;
import android.app.Application;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.widget.ArrayAdapter;

public class SnaphApplication extends Application{

	private Bitmap image;
	private ArrayAdapter<CompressedListing> adapter;
	
	public static final String CONSUMER_KEY = "FMgRyHdIQZTEixINFFErw";
	public static final String CONSUMER_SECRET= "QVYXJvDZt5Hzcp8cl0WoR5LoxLPpVbdJcOF3rmSbI";
	
	public static final String REQUEST_URL = "http://api.twitter.com/oauth/request_token";
	public static final String ACCESS_URL = "http://api.twitter.com/oauth/access_token";
	public static final String AUTHORIZE_URL = "http://api.twitter.com/oauth/authorize";
	
	public static final String	OAUTH_CALLBACK_SCHEME	= "x-oauthflow-twitter";
	public static final String	OAUTH_CALLBACK_HOST		= "callback";
	public static final String	OAUTH_CALLBACK_URL		= OAUTH_CALLBACK_SCHEME + "://" + OAUTH_CALLBACK_HOST;
	
	String fbUserName = "";
	String fbUserId = "";
	String fbToken="";
	Bitmap fbUserImage;
	
	OAuthConsumer consumer; 
    OAuthProvider provider;
	
	SharedPreferences sharedPrefs;
	AsyncFacebookRunner asyncRunner;
	Twitter twitter;
	Facebook facebook;
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
