package com.onb.snaph;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;

import com.facebook.android.Facebook;
import android.app.Application;
import android.graphics.Bitmap;
import android.widget.ArrayAdapter;

/**
 * 
 *
 */
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
	
	private String fbUserName = "";
	private String fbUserId = "";
	private String fbToken="";
	private Bitmap fbUserImage;
	
	OAuthConsumer consumer; 
    OAuthProvider provider;
	
	private Facebook facebook;
	final String APP_ID = "366360670078534";
	public boolean authenticated;
	
	@Override
	public void onCreate() {
        super.onCreate();
        authenticated = false;
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

	public String getFbToken() {
		return fbToken;
	}

	public void setFbToken(String fbToken) {
		this.fbToken = fbToken;
	}

	public String getFbUserId() {
		return fbUserId;
	}

	public void setFbUserId(String fbUserId) {
		this.fbUserId = fbUserId;
	}

	public Facebook getFacebook() {
		return facebook;
	}

	public void setFacebook(Facebook facebook) {
		this.facebook = facebook;
	}

	public String getFbUserName() {
		return fbUserName;
	}

	public void setFbUserName(String fbUserName) {
		this.fbUserName = fbUserName;
	}

	public Bitmap getFbUserImage() {
		return fbUserImage;
	}

	public void setFbUserImage(Bitmap fbUserImage) {
		this.fbUserImage = fbUserImage;
	}
	
	public int getItemIdAtPosition(int position){
		return adapter.getItem(position).getItemId();
	}
	
	public Listing getListingAtPosition(int position){
		return adapter.getItem(position).toListing();
	}
}
