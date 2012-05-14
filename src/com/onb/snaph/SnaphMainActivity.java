package com.onb.snaph;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

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
		sharedPrefs = getPreferences(MODE_PRIVATE);
        String access_token = sharedPrefs.getString("access_token", null);
        long expires = sharedPrefs.getLong("access_expires", 0);
        if(access_token != null) {
            facebook.setAccessToken(access_token);
        }
        if(expires != 0) {
            facebook.setAccessExpires(expires);
        }
        if(!facebook.isSessionValid()) {
			facebook.authorize(this, new DialogListener() {
	            
	            public void onComplete(Bundle values) {
	            	Log.d(TAG,"Complete");
	            	init();
	            	SharedPreferences.Editor editor = sharedPrefs.edit();
                    editor.putString("access_token", facebook.getAccessToken());
                    editor.putLong("access_expires", facebook.getAccessExpires());
                    editor.commit();
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
        else{
        	Log.d(TAG, "INITIALIZE");
        	init();
        }
        
    }
    
    private void init(){
    	asyncRunner = new AsyncFacebookRunner(facebook);
    	Bundle params = new Bundle();
   		params.putString("fields", "name, picture");
    	asyncRunner.request("me", params, new UserRequestListener(application));
    	userName = (TextView) findViewById(R.id.userName);
    	userName.setText(application.userName);
    	userImage = (ImageView) findViewById(R.id.userImage);
    	userImage.setImageBitmap(getBitmap(application.userImage));
    }
    
    public static Bitmap getBitmap(String url) {
    	Bitmap bitmap = null;
		try { 
			
			URL userURL = new URL(url); 
	        URLConnection conn = userURL.openConnection(); 
	        conn.connect(); 
	        
	        InputStream inputStream = conn.getInputStream(); 
	        BufferedInputStream bufferedStream = new BufferedInputStream(inputStream); 
	        bitmap = BitmapFactory.decodeStream(new FlushedInputStream(inputStream));
	        bufferedStream.close(); 
	        inputStream.close();
	     } catch (Exception e) {
	    	Log.d(TAG,e.getMessage());
	     } 
	     return bitmap;
	}
    
    static class FlushedInputStream extends FilterInputStream {
        public FlushedInputStream(InputStream inputStream) {
            super(inputStream);
        }

        @Override
        public long skip(long n) throws IOException {
            long totalBytesSkipped = 0L;
            while (totalBytesSkipped < n) {
                long bytesSkipped = in.skip(n - totalBytesSkipped);
                if (bytesSkipped == 0L) {
                      int b = read();
                      if (b < 0) {
                          break;
                      } else {
                          bytesSkipped = 1;
                      }
               }
                totalBytesSkipped += bytesSkipped;
            }
            return totalBytesSkipped;
        }
    }
    
    public void onLogout(View view){
    	asyncRunner.logout(this, new RequestListener() {

    		  public void onComplete(String response, Object state) {
    			  Log.d(TAG,facebook.getAccessToken()+"");
    			  SharedPreferences.Editor editor = sharedPrefs.edit();
                  editor.putString("access_token", facebook.getAccessToken());
                  editor.putLong("access_expires", facebook.getAccessExpires());
                  editor.commit();
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