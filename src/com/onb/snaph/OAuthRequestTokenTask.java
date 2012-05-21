package com.onb.snaph;


import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

/**
 * An asynchronous task that communicates with Twitter to 
 * retrieve a request token.
 * (OAuthGetRequestToken)
 * 
 * After receiving the request token from Twitter, 
 * pop a browser to the user to authorize the Request Token.
 * (OAuthAuthorizeToken)
 * 
 */
public class OAuthRequestTokenTask extends AsyncTask<Void, Void, Void> {

	final String TAG = getClass().getName();
	private Context	 context;
	private OAuthProvider provider;
	private OAuthConsumer consumer;
	private SnaphApplication snaph;

	/**
	 * 
	 * We pass the OAuth consumer and provider.
	 * 
	 * @param 	context
	 * 			Required to be able to start the intent to launch the browser.
	 * @param 	provider
	 * 			The OAuthProvider object
	 * @param 	consumer
	 * 			The OAuthConsumer object
	 */
	public OAuthRequestTokenTask(SnaphApplication snaph, Context context,OAuthConsumer consumer,OAuthProvider provider) {
		this.context = context;
		this.consumer = consumer;
		this.provider = provider;
		this.snaph = snaph;
	}

	/**
	 * 
	 * Retrieve the OAuth Request Token and present a browser to the user to authorize the token.
	 * 
	 */
	@Override
	protected Void doInBackground(Void... params) {
		
		try {
			Log.i(TAG, "Retrieving request token from Google servers");
			final String url = provider.retrieveRequestToken(consumer, snaph.OAUTH_CALLBACK_URL);
			Log.i(TAG, "Popping a browser with the authorize URL : " + Uri.parse(url));
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
			context.startActivity(intent);
		} catch (Exception e) {
			Log.e(TAG, "Error during OAUth retrieve request token", e);
		}

		return null;
	}

}