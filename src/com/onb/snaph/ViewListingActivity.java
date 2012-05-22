package com.onb.snaph;

import com.facebook.android.DialogError;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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

public class ViewListingActivity extends Activity{

	protected static final String TAG = ViewListingActivity.class.getSimpleName();
	
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
			e.printStackTrace();
		}
    	
    	finish();
		
	}
	
	public void onFacebookPost(View view){
			//Log.d(TAG,"facebook: " + snaph.facebook.toString());
			Bundle params = new Bundle();
			params.putString("link", listingLink);
			params.putString("picture", imageLink);
			params.putString("name", title.getText().toString());
			params.putString("description", description.getText().toString());
			params.putString("caption", price.getText().toString());
		 	snaph.facebook.dialog(this, "feed", params, new DialogListener(){

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
		this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
		if (TwitterUtils.isAuthenticated(prefs)) {
			sendTweet();
 } 
		else {
			Intent i = new Intent(getApplicationContext(), PrepareRequestTokenActivity.class);
			i.putExtra("tweet_msg",getTweetMsg());
			startActivity(i);
		}
	}
	
	private String getTweetMsg() {
		return "Selling " + title.getText() + " for " + price.getText();
	}	
	
	public void sendTweet() {
		Thread t = new Thread() {
	        public void run() {
	        	
	        	try {
	        		TwitterUtils.sendTweet(prefs,getTweetMsg());
	        		twitterHandler.post(mUpdateTwitterNotification);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
	        }

	    };
	    t.start();
	}
	
	final Runnable mUpdateTwitterNotification = new Runnable() {
		public void run() {
        	Toast.makeText(getBaseContext(), "Tweet sent !", Toast.LENGTH_LONG).show();
        }
    };
	
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
