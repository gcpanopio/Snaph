package com.onb.snaph;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

/**
 * The application's main activity. 
 * 
 * Contains the Facebook log-in. Shows the list of items of the current logged in user.
 * 
 */
public class SnaphMainActivity extends Activity {
    
	protected static final String TAG = SnaphMainActivity.class.getSimpleName();
	protected static final int ACTIVITY_IMAGE_CAPTURE = 1000;
	private static final int ACTIVITY_FROM_GALLERY = 1001;
	private ArrayAdapter<CompressedListing> adapter;

	private TextView userName;
	private ImageView userImage;
	private Handler userHandler;
	private SnaphApplication snaph;
	private AsyncFacebookRunner asyncRunner;
	private Facebook facebook; 
	private ListView itemList;
	 
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        snaph = (SnaphApplication) getApplication();
        facebook = new Facebook(snaph.APP_ID);
        snaph.setFacebook(facebook);
        setContentView(R.layout.main);
		facebook.authorize(this, new String[] {"publish_stream"}, new DialogListener() {
            
            public void onComplete(Bundle values) {
            	Log.d(TAG,"Complete");
            	init();
            }
            
            public void onFacebookError(FacebookError error) {
            	Log.d(TAG,"FBError");
            	finish();
            }

            public void onError(DialogError e) {
            	Log.d(TAG,"Error");
            	finish();
            }
            
            public void onCancel() {
            	Log.d(TAG,"Cancel");
            	finish();
            } 
		});
    }
    
    private void init(){
        userHandler = new Handler();
    	asyncRunner = new AsyncFacebookRunner(facebook);
    	snaph.setFbToken(facebook.getAccessToken());
    	Bundle params = new Bundle();
   		params.putString("fields", "id, name, picture");
   		userName = (TextView) findViewById(R.id.userName);
    	userImage = (ImageView) findViewById(R.id.userImage);
    	asyncRunner.request("me", params, new userRequestListener());
    	
    	Log.d(TAG, "[2] Initialize");
    
    	setListView();
    }
    
    private void setListView(){
    	adapter = createListingAdapter(getApplicationContext(), 0);
    	snaph.setAdapter(adapter);
    	
    	itemList = (ListView) findViewById(R.id.item_list);

    	Log.d(TAG, "Username: "+snaph.getFbUserName());
    	Log.d(TAG, "Id: "+snaph.getFbUserId());
        Thread thread = new RetrieverThread(getApplicationContext(), snaph.getFbUserId(), snaph.getAdapter());
        thread.start();
        try {
			thread.join();
			itemList.setAdapter(snaph.getAdapter());
			snaph.notifyAdapterChange();
		} catch (InterruptedException e) {
			Log.d("Thread join error", e.getMessage());
			e.printStackTrace();
		}
        
        itemList.setOnItemClickListener(new ListView.OnItemClickListener() {
              public void onItemClick(AdapterView<?> a, View v, int i, long l) {
                  try {
                	  Log.d(TAG, "Item clicked on position "+i);
                	  viewListing(i);
                  }
                  catch(Exception e) {
                      
                  }
              }
          });

    	Log.d(TAG,"OUT");
    }
    
    /**
     * 
     */
    public void onResume(){
    	super.onResume();
    	setListView();
    }
    
    private void viewListing(int position){
    	Intent viewForm = new Intent(this, ViewListingActivity.class);
    	viewForm.putExtra("item_position", position);
  	  	startActivity(viewForm);
    }

    /**
     * 
     * 
     *
     */
    class userRequestListener implements RequestListener {

    	final String RLTAG = userRequestListener.class.getSimpleName();
    	
    	/**
    	 * 
    	 */
    	public void onComplete(String response, Object state) {
    		Log.d(TAG, "[1] Facebook User Request Listener");
    		JSONObject jsonObject;
    		try {
    			jsonObject = new JSONObject(response);
            	URL newurl = new URL(jsonObject.getString("picture")); 
            	Bitmap img = BitmapFactory.decodeStream(newurl.openConnection().getInputStream());
            	snaph.setFbUserImage(img);
            	snaph.setFbUserName(jsonObject.getString("name"));
            	snaph.setFbUserId(jsonObject.getString("id"));
            	
            	
    		} catch (JSONException e) {
    			e.printStackTrace();
    		} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
    		
    		userHandler.post(new Runnable() {
                public void run() {
                	userName.setText(snaph.getFbUserName());
                	userImage.setImageBitmap(snaph.getFbUserImage());
                }
            });
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
    
    private ArrayAdapter<CompressedListing> createListingAdapter(Context context, int id) {
    	ArrayAdapter<CompressedListing> adapter = new ArrayAdapter<CompressedListing>(context, id) {
    		
    		@Override
    		public View getView(int position, View convertView, ViewGroup parent) {
    			
    			LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    			LinearLayout customView = new LinearLayout(getContext());
    			
    			inflater.inflate(R.layout.list_item, customView, true);
    			
    			TextView name = (TextView) customView.findViewById(R.id.list_item_name);
    			TextView price = (TextView) customView.findViewById(R.id.list_item_price);
    			
    			CompressedListing currentItem = (CompressedListing) getItem(position);
    			
    			name.setText(currentItem.getName());
    			price.setText(currentItem.getPrice());
    			
    			return customView;
    		}

    	};
		return adapter;
    }
    
    /**
     * Logs out the current user
     * 
     * @param view
     */
    public void onLogout(View view){
    	asyncRunner.logout(this, new RequestListener() {

    		  public void onComplete(String response, Object state) {
    			  Log.d(TAG,facebook.getAccessToken()+"");
                  finish();
    		  }
    		  
    		  public void onIOException(IOException e, Object state) {
    			  Log.d(TAG,e.getMessage());
    		  }
    		  
    		  public void onFileNotFoundException(FileNotFoundException e,
    		        Object state) {
    			  Log.d(TAG,e.getMessage());
    		  }
    		  
    		  public void onMalformedURLException(MalformedURLException e,
    		        Object state) {
    			  Log.d(TAG,e.getMessage());
    		  }
    		  
    		  public void onFacebookError(FacebookError e, Object state) {
    			  Log.d(TAG,e.getMessage());
    		  }
    		});
    }
    
    /**
     * Shows a menu where the user can choose how he wants to upload the picture of the item.
     * @param view
     */
    public void onSnapButtonActivity(View view){    	
    	final CharSequence[] items = {"Snap Image", "Upload from Gallery"};

    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle("Snap a Photo!");
    	builder.setItems(items, new DialogInterface.OnClickListener() {
    	    public void onClick(DialogInterface dialog, int item) {
    	       Log.d(TAG, "Item clicked: "+item);
    	       if (item == 0){
    	    	   snapImage();
    	       }
    	       else if (item == 1){
    	    	   fromGallery();
    	       }
    	    }
    	});
    	AlertDialog alert = builder.create();
    	alert.show();
    }
    
    private void snapImage(){
    	Intent launchCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		startActivityForResult(launchCamera, ACTIVITY_IMAGE_CAPTURE);
    }
    
    private void fromGallery(){
    	Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
    	photoPickerIntent.setType("image/*");
    	startActivityForResult(photoPickerIntent, ACTIVITY_FROM_GALLERY);
    }
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	Log.d(TAG,"ActivityResult");
        
		if(requestCode == ACTIVITY_IMAGE_CAPTURE && resultCode == RESULT_OK) {
			Bitmap capturedImage = (Bitmap) data.getExtras().get("data");
			setSelectedImage(capturedImage);
		} else if(requestCode == ACTIVITY_FROM_GALLERY && resultCode == RESULT_OK) {
			Uri selectedImageUri = data.getData();
			Bitmap chosenImage = null;
	        try {
				chosenImage = Media.getBitmap(this.getContentResolver(), selectedImageUri);
				setSelectedImage(chosenImage);
	        } catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else{
			facebook.authorizeCallback(requestCode, resultCode, data);
		}
	}   
    
    private void setSelectedImage(Bitmap image){
    	Log.d(TAG, "SELECTED IMAGE 1");
    	SnaphApplication snaph = (SnaphApplication) getApplication();
    	snaph.setImage(image);
    	
    	Intent launchUploadFormActivity = new Intent(this, UploadFormActivity.class);
    	startActivity(launchUploadFormActivity);
    }
}