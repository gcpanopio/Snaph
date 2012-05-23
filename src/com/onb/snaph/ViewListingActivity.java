package com.onb.snaph;

import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.http.AccessToken;

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
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 
 * Shows the details (image, name, price and description) of the selected listing on the item list.
 * 
 * Has buttons for edit and delete.
 *
 */
public class ViewListingActivity extends Activity{

	protected static final String TAG = ViewListingActivity.class.getSimpleName().toString();
	
	private ImageView image;
	private TextView title;
	private TextView description;
	private TextView price;
	private SnaphApplication snaph;
	private int itemPosition;
	private final Handler twitterHandler = new Handler();
	private SharedPreferences prefs;
	private String listingLink;
	private String imageLink;
	private OAuthConsumer consumer; 
	private OAuthProvider provider;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
        setContentView(R.layout.view_listing);
        
        Button edit= (Button) findViewById(R.id.edit_button);
        edit.getBackground().setAlpha(70);
        
        Button delete = (Button) findViewById(R.id.delete_button);
        delete.getBackground().setAlpha(70);
        
        Button back = (Button) findViewById(R.id.back_button);
        back.getBackground().setAlpha(70);
        
        ImageButton facebook = (ImageButton) findViewById(R.id.facebook_button);
        facebook.getBackground().setAlpha(70);
        
        ImageButton twitter = (ImageButton) findViewById(R.id.twitter_button);
        twitter.getBackground().setAlpha(70);
        
        snaph = (SnaphApplication) getApplication();
        
        Intent viewForm = this.getIntent();
        itemPosition = viewForm.getIntExtra("item_position", -1);
        
        Log.d(TAG, "Item pos: "+itemPosition);
        Listing item = snaph.getAdapter().getItem(itemPosition).toListing();
      
        imageLink = item.getImageUrl();
        listingLink = item.getItemUrl();
        image = (ImageView) findViewById(R.id.image_view);
        image.setImageBitmap(item.getImage());
        title = (TextView) findViewById(R.id.title_view);
        title.setText(item.getName());
        description = (TextView) findViewById(R.id.description_view);
        description.setText(item.getDescription());
        price = (TextView) findViewById(R.id.price_view);
        price.setText(item.getPrice().toString());
    }
	
	/**
	 * Deletes the selected listing.
	 * 
	 * Sends DELETE command to the web application with the selected listing's item ID.
	 * @param view
	 */
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
		
		UserAccount fbUser = new UserAccount(snaph.getFbToken(), snaph.getFbUserId(), true);
    	SellerInfo seller = new SellerInfo(fbUser, null, AndroidUserCommand.DELETE);
    	Thread thread = new UploaderThread(this.getBaseContext(), item, seller);
    	thread.start();
    	try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	
    	finish();
		
	}
	
	/**
     * Calls the EditFormActivity
     * 
     * @param view
     */
	public void onEdit(View view){

        Log.d(TAG, "Item pos>> "+itemPosition);
        finish();
		Intent editForm = new Intent(this, EditFormActivity.class);
    	editForm.putExtra("item_position", itemPosition);
  	  	startActivity(editForm);
	}
	
	/**
	 * Finishes the ViewListingAcitvity
	 * @param view
	 */
	public void onBack(View view){
		finish();
	}
	/**
	 * 
	 * @param view
	 */
	public void onFacebookPost(View view){
		Bundle params = new Bundle();
		params.putString("link", listingLink);
		params.putString("picture", imageLink);
		Log.d(TAG,imageLink);
		params.putString("name", title.getText().toString());
		params.putString("description", description.getText().toString());
		params.putString("caption", price.getText().toString());
	 	snaph.getFacebook().dialog(this, "feed", params, new DialogListener(){

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
	
	/**
	 * 
	 * @param view
	 */
	public void onTwitterPost(View view){
		Log.d(TAG,"Tweeting");
		String token = prefs.getString(OAuth.OAUTH_TOKEN, "");
		String secret = prefs.getString(OAuth.OAUTH_TOKEN_SECRET, "");
		Log.d(TAG,"AccessToken");
		AccessToken a = new AccessToken(token,secret);
		Log.d(TAG,"TweeterFactory");
		Twitter twitter = new TwitterFactory().getInstance();
		Log.d(TAG,"SettingUp");
		twitter.setOAuthConsumer(SnaphApplication.CONSUMER_KEY, SnaphApplication.CONSUMER_SECRET);
		twitter.setOAuthAccessToken(a);
		Log.d(TAG,"Authenticated");
		boolean authenticated = false;
		/*
		try {
			twitter.getAccountSettings();
			authenticated = true;
		} catch (TwitterException e) {}
		Log.d(TAG,authenticated+"");
		*/
		if (authenticated) {
			sendTweet();
    	} 
		else {
			try {
	    		consumer = new CommonsHttpOAuthConsumer(SnaphApplication.CONSUMER_KEY, SnaphApplication.CONSUMER_SECRET);
	    	    provider = new CommonsHttpOAuthProvider(SnaphApplication.REQUEST_URL,SnaphApplication.ACCESS_URL,SnaphApplication.AUTHORIZE_URL);
	    	} catch (Exception e) {
	    		Log.e(TAG, "Error creating consumer / provider",e);
			}
	        Log.i(TAG, "Starting task to retrieve request token.");
	        new OAuthRequestTokenTask(this,consumer,provider).execute();
    	}
	}
	
	private void sendTweet(){
		Thread t = new Thread() {
	        public void run() {
	        	try {
	        		String token = prefs.getString(OAuth.OAUTH_TOKEN, "");
	        		String secret = prefs.getString(OAuth.OAUTH_TOKEN_SECRET, "");
	        		
	        		AccessToken a = new AccessToken(token,secret);
	        		Twitter twitter = new TwitterFactory().getInstance();
	        		twitter.setOAuthConsumer(SnaphApplication.CONSUMER_KEY, 
	        				SnaphApplication.CONSUMER_SECRET);
	        		twitter.setOAuthAccessToken(a);
	        		String msg = "Check out this link to see the item I'm selling \n" + listingLink;
	                twitter.updateStatus(msg);
	        		twitterHandler.post(mUpdateTwitterNotification);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
	        }

	    };
	    t.start();
	    try {
			t.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	final Runnable mUpdateTwitterNotification = new Runnable() {
        public void run() {
        	Toast.makeText(getBaseContext(), "Tweet sent!", Toast.LENGTH_LONG).show();
        }
    };
    
    @Override
	public void onNewIntent(Intent intent) {
		super.onNewIntent(intent); 
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		final Uri uri = intent.getData();
		if (uri != null && uri.getScheme().equals(SnaphApplication.OAUTH_CALLBACK_SCHEME)) {
			Log.i(TAG, "Callback received : " + uri);
			Log.i(TAG, "Retrieving Access Token");
			new RetrieveAccessTokenTask(this).execute(uri);
			
			setContentView(R.layout.view_listing);
	        
	        Button edit= (Button) findViewById(R.id.edit_button);
	        edit.getBackground().setAlpha(70);
	        
	        Button delete = (Button) findViewById(R.id.delete_button);
	        delete.getBackground().setAlpha(70);
	        
	        Button back = (Button) findViewById(R.id.back_button);
	        back.getBackground().setAlpha(70);
	        
	        ImageButton facebook = (ImageButton) findViewById(R.id.facebook_button);
	        facebook.getBackground().setAlpha(70);
	        
	        ImageButton twitter = (ImageButton) findViewById(R.id.twitter_button);
	        twitter.getBackground().setAlpha(70);
	        
	        snaph = (SnaphApplication) getApplication();
	        
	        Intent viewForm = this.getIntent();
	        itemPosition = viewForm.getIntExtra("item_position", -1);
	        
	        Log.d(TAG, "Item pos: "+itemPosition);
	        Listing item = snaph.getAdapter().getItem(itemPosition).toListing();
	      
	        imageLink = item.getImageUrl();
	        listingLink = item.getItemUrl();
	        image = (ImageView) findViewById(R.id.image_view);
	        image.setImageBitmap(item.getImage());
	        title = (TextView) findViewById(R.id.title_view);
	        title.setText(item.getName());
	        description = (TextView) findViewById(R.id.description_view);
	        description.setText(item.getDescription());
	        price = (TextView) findViewById(R.id.price_view);
	        price.setText(item.getPrice().toString());
		}
	}
    
    public class RetrieveAccessTokenTask extends AsyncTask<Uri, Void, Void> {
		
    	private Context context;
    	
    	public RetrieveAccessTokenTask(Context context){
    		this.context = context;
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
				startActivity(new Intent(context,ViewListingActivity.class));

				executeAfterAccessTokenRetrieval();
				
				Log.i(TAG, "OAuth - Access Token Retrieved");
				
			} catch (Exception e) {
				Log.e(TAG, "OAuth - Access Token Retrieval Error", e);
			}

			return null;
		}


		private void executeAfterAccessTokenRetrieval() {
			try {
				sendTweet();
			} catch (Exception e) {
				Log.e(TAG, "OAuth - Error sending to Twitter", e);
			}
		}
	}	

}
