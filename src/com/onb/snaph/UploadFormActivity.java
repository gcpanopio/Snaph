package com.onb.snaph;

import java.math.BigDecimal;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

/**
 * Represents the upload form and create function of the application.
 * 
 * Asks for the item's information (name, price and description) and sends the data to the web application
 *
 */
public class UploadFormActivity extends Activity{
	protected static final String TAG = UploadFormActivity.class.getSimpleName();
	
	private ImageView image;
	private EditText title;
	private EditText description;
	private EditText price;
	private SnaphApplication snaph;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.upload_form);
        
        Log.d(TAG, "UPLOAD FORM!");
        
        snaph = (SnaphApplication) getApplication();
        
        image = (ImageView) findViewById(R.id.image);
        image.setImageBitmap(snaph.getImage());
        title = (EditText) findViewById(R.id.title);
        description = (EditText) findViewById(R.id.description);
        price = (EditText) findViewById(R.id.price);
    }
    
    /**
     * Finishes the UploadFormActivity
     * @param view
     */
    public void onCancel(View view){
    	finish();
    }
    
    /**
     * Creates a new instance of listing with a default itemId -1. 
     * 
     * Calls the UploaderTHread to upload the data.
     * @param view
     */
    public void onUpload(View view){
    	Listing list = new Listing(title.getText().toString(), description.getText().toString(), new BigDecimal(price.getText().toString()), snaph.getImage());
    	UserAccount fbUser = new UserAccount(snaph.getFbToken(), snaph.getFbUserId(), true);
    	SellerInfo seller = new SellerInfo(fbUser, null, AndroidUserCommand.INSERT);
    	
    	Thread thread = new UploaderThread(this.getBaseContext(), list, seller);
    	thread.start();
    	try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	
    	finish();
    }
}
