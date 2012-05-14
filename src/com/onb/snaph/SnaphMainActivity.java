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
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

public class SnaphMainActivity extends Activity {
    
	protected static final String TAG = SnaphMainActivity.class.getSimpleName();
	protected static final int ACTIVITY_IMAGE_CAPTURE = 1000;
	private static final int ACTIVITY_FROM_GALLERY = 1001;
	TextView greetings;
	TextView userName;
	ImageView userImage;
	Handler userHandler;
	SnaphApplication application;
	AsyncFacebookRunner asyncRunner;
	SharedPreferences sharedPrefs;
	Facebook facebook; 
	 
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        application = (SnaphApplication) getApplication();
        facebook = new Facebook(application.APP_ID);
        setContentView(R.layout.main);
        userHandler = new Handler();
		facebook.authorize(this, new DialogListener() {
            
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
    	asyncRunner = new AsyncFacebookRunner(facebook);
    	Bundle params = new Bundle();
   		params.putString("fields", "id, name, picture");
   		userName = (TextView) findViewById(R.id.userName);
    	userImage = (ImageView) findViewById(R.id.userImage);
    	asyncRunner.request("me", params, new userRequestListener());
    	Log.d(TAG,"OUT");
    }
    
    class userRequestListener implements RequestListener {

    	final String RLTAG = userRequestListener.class.getSimpleName();
    	
    	public void onComplete(String response, Object state) {
    		JSONObject jsonObject;
    		try {
    			jsonObject = new JSONObject(response);
            	URL newurl = new URL(jsonObject.getString("picture")); 
            	Bitmap img = BitmapFactory.decodeStream(newurl.openConnection().getInputStream());
            	application.userImage = img;
            	application.userName = jsonObject.getString("name");
            	application.userId = jsonObject.getString("id");
            	Log.d(TAG,application.userId);
    		} catch (JSONException e) {
    			e.printStackTrace();
    		} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
    		
    		userHandler.post(new Runnable() {
                public void run() {
                	userName.setText(application.userName);
                	userImage.setImageBitmap(application.userImage);
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
    
    public void onSnapButtonActivity(View view){
    	 PopupMenu popup = new PopupMenu(this, view);
    	 MenuInflater inflater = popup.getMenuInflater();
    	 inflater.inflate(R.menu.photo_menu, popup.getMenu());
    	 popup.show();
    	 
    	 popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
			
    		 public boolean onMenuItemClick(MenuItem item) {
    		     switch (item.getItemId()) {
    		         case R.id.snap_image:
    		        	 Log.d(TAG, "SNAP IMAGE");
    		        	 snapImage();
    		             return true;
    		         case R.id.from_gallery:
    		        	 Log.d(TAG, "FROM GALLERY");
    		        	 fromGallery();
    		             return true;
    		         default:
    		             return false;
    		     }
    		 }
		});
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