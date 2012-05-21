package com.onb.snaph;

import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.http.AccessToken;
import twitter4j.http.RequestToken;

import com.facebook.android.DialogError;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class ViewListingActivity extends Activity{

	protected static final String TAG = ViewListingActivity.class.getSimpleName();
	
	private ImageView image;
	private TextView title;
	private TextView description;
	private TextView price;
	private SnaphApplication snaph;
	private int itemPosition;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_listing);
        
        Button edit= (Button) findViewById(R.id.edit_button);
        edit.getBackground().setAlpha(70);
        
        Button delete = (Button) findViewById(R.id.delete_button);
        delete.getBackground().setAlpha(70);
        
        Button back = (Button) findViewById(R.id.back_button);
        back.getBackground().setAlpha(70);
        
        ImageButton facebook = (ImageButton) findViewById(R.id.facebook_button);
        back.getBackground().setAlpha(70);
        
        ImageButton twitter = (ImageButton) findViewById(R.id.twitter_button);
        back.getBackground().setAlpha(70);
        
        snaph = (SnaphApplication) getApplication();
        
        Intent viewForm = this.getIntent();
        itemPosition = viewForm.getIntExtra("item_position", -1);
        
        Log.d(TAG, "Item pos: "+itemPosition);
        Listing item = snaph.getAdapter().getItem(itemPosition).toListing();
      
        
        image = (ImageView) findViewById(R.id.image_view);
        image.setImageBitmap(item.getImage());
        title = (TextView) findViewById(R.id.title_view);
        title.setText(item.getName());
        description = (TextView) findViewById(R.id.description_view);
        description.setText(item.getDescription());
        price = (TextView) findViewById(R.id.price_view);
        price.setText(item.getPrice().toString());
    }
	
	public void onDelete(View view){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Are you sure you want to delete this item?")
		       .setCancelable(false)
		       .setNegativeButton("No", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                dialog.cancel();
		           }
		       })
				.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
	                deleteItem();
	           }
	       });
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	private void deleteItem() {
		Listing item = new Listing(null, null, null, null);
		item.setItemId(snaph.getAdapter().getItem(itemPosition).getItemId());
		
		UserAccount fbUser = new UserAccount(snaph.fbToken, snaph.fbUserId, true);
    	SellerInfo seller = new SellerInfo(fbUser, null, AndroidUserCommand.DELETE);
    	Thread thread = new UploaderThread(this.getBaseContext(), item, seller);
    	thread.start();
    	try {
			thread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	finish();
		
	}
	
	public void onFacebookPost(View view){
			//Log.d(TAG,"facebook: " + snaph.facebook.toString());
		 	snaph.facebook.dialog(this, "feed", new DialogListener(){

			public void onComplete(Bundle values) {
				final String postId = values.getString("post_id");

		        if (postId != null) {
		        	Log.d(TAG,"Wall Post Success");
		        } else {
		        	Log.d(TAG,"Wall Post Failed");
		        }
			}

			public void onFacebookError(FacebookError e) {
				Log.d(TAG,e.getMessage());
			}

			public void onError(DialogError e) {
				Log.d(TAG,e.getMessage());
			}

			public void onCancel() {}
		 });
	}
	
	public void onTwitterPost(View view){
		try {
    		snaph.consumer = new CommonsHttpOAuthConsumer(snaph.CONSUMER_KEY, snaph.CONSUMER_SECRET);
    	    snaph.provider = new CommonsHttpOAuthProvider(snaph.REQUEST_URL,snaph.ACCESS_URL,snaph.AUTHORIZE_URL);
    	} catch (Exception e) {
    		Log.e(TAG, "Error creating consumer / provider",e);
		}

        Log.i(TAG, "Starting task to retrieve request token.");
		new OAuthRequestTokenTask(snaph,this,snaph.consumer,snaph.provider).execute();
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String msg = title.getText() +" "+ price.getText() +"\n"+description.getText();
		try {
			new RetrieveAccessTokenTask(this,snaph.consumer,snaph.provider,prefs).sendTweet(prefs, msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		final Uri uri = intent.getData();
		if (uri != null && uri.getScheme().equals(snaph.OAUTH_CALLBACK_SCHEME)) {
			Log.i(TAG, "Callback received : " + uri);
			Log.i(TAG, "Retrieving Access Token");
			new RetrieveAccessTokenTask(this,snaph.consumer,snaph.provider,prefs).execute(uri);
			finish();
		}
	}
	
	public class RetrieveAccessTokenTask extends AsyncTask<Uri, Void, Void> {

		private Context	context;
		private OAuthProvider provider;
		private OAuthConsumer consumer;
		private SharedPreferences prefs;
		private final String TAG = RetrieveAccessTokenTask.class.getSimpleName();
		
		public RetrieveAccessTokenTask(Context context, OAuthConsumer consumer,OAuthProvider provider, SharedPreferences prefs) {
			this.context = context;
			this.consumer = consumer;
			this.provider = provider;
			this.prefs=prefs;
		}


		/**
		 * Retrieve the oauth_verifier, and store the oauth and oauth_token_secret 
		 * for future API calls.
		 */
		@Override
		protected Void doInBackground(Uri...params) {
			final Uri uri = params[0];
			final String oauth_verifier = uri.getQueryParameter(OAuth.OAUTH_VERIFIER);

			try {
				provider.retrieveAccessToken(consumer, oauth_verifier);

				final Editor edit = prefs.edit();
				edit.putString(OAuth.OAUTH_TOKEN, consumer.getToken());
				edit.putString(OAuth.OAUTH_TOKEN_SECRET, consumer.getTokenSecret());
				edit.commit();
				
				String token = prefs.getString(OAuth.OAUTH_TOKEN, "");
				String secret = prefs.getString(OAuth.OAUTH_TOKEN_SECRET, "");
				
				consumer.setTokenWithSecret(token, secret);
				context.startActivity(new Intent(context,SnaphApplication.class));

				executeAfterAccessTokenRetrieval();
				
				Log.i(TAG, "OAuth - Access Token Retrieved");
				
			} catch (Exception e) {
				Log.e(TAG, "OAuth - Access Token Retrieval Error", e);
			}

			return null;
		}


		private void executeAfterAccessTokenRetrieval() {
			String msg = getIntent().getExtras().getString("tweet_msg");
			try {
				sendTweet(prefs, msg);
			} catch (Exception e) {
				Log.e(TAG, "OAuth - Error sending to Twitter", e);
			}
		}
		
		public void sendTweet(SharedPreferences prefs,String msg) throws Exception {
			String token = prefs.getString(OAuth.OAUTH_TOKEN, "");
			String secret = prefs.getString(OAuth.OAUTH_TOKEN_SECRET, "");
			
			AccessToken a = new AccessToken(token,secret);
			Twitter twitter = new TwitterFactory().getInstance();
			twitter.setOAuthConsumer(snaph.CONSUMER_KEY, snaph.CONSUMER_SECRET);
			twitter.setOAuthAccessToken(a);
	        twitter.updateStatus(msg);
		}	
	}
	
	public void onEdit(View view){

        Log.d(TAG, "Item pos>> "+itemPosition);
        finish();
		Intent editForm = new Intent(this, EditFormActivity.class);
    	editForm.putExtra("item_position", itemPosition);
  	  	startActivity(editForm);
	}
	
	public void onBack(View view){
		finish();
	}
}
