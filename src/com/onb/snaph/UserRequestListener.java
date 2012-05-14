package com.onb.snaph;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.FacebookError;

public class UserRequestListener implements RequestListener {

	protected static final String TAG = SnaphMainActivity.class.getSimpleName();
	SnaphApplication app;
	public UserRequestListener(SnaphApplication app){
		this.app = app;
	}
	public void onComplete(String response, Object state) {
		JSONObject jsonObject;
		try {
			jsonObject = new JSONObject(response);
		
        	app.userImage = jsonObject.getString("picture");
        	app.userName = jsonObject.getString("name");
		} catch (JSONException e) {
			Log.d(TAG,e.getMessage());
		}
	}

	public void onIOException(IOException e, Object state) {
		Log.d(TAG,e.getMessage());
	}

	public void onFileNotFoundException(FileNotFoundException e, Object state) {
		Log.d(TAG,e.getMessage());
	}

	public void onMalformedURLException(MalformedURLException e, Object state) {
		Log.d(TAG,e.getMessage());
	}

	public void onFacebookError(FacebookError e, Object state) {
		Log.d(TAG,e.getMessage());
	}
	
}
