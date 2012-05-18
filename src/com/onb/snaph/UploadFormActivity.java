package com.onb.snaph;

import java.math.BigDecimal;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

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
        
        Button upload = (Button) findViewById(R.id.upload_photo);
        upload.getBackground().setAlpha(70);
        
        Log.d(TAG, "UPLOAD FORM!");
        Button cancelUpload = (Button) findViewById(R.id.cancel_upload);
        cancelUpload.getBackground().setAlpha(70);
        
        snaph = (SnaphApplication) getApplication();
        
        image = (ImageView) findViewById(R.id.image);
        image.setImageBitmap(snaph.getImage());
        title = (EditText) findViewById(R.id.title);
        description = (EditText) findViewById(R.id.description);
        price = (EditText) findViewById(R.id.price);
    }
    
    public void onCancel(View view){
    	finish();
    }
    
    public void onUpload(View view){
    	Listing list = new Listing(title.getText().toString(), description.getText().toString(), new BigDecimal(price.getText().toString()), snaph.getImage());
    	
    	
    	UserAccount fbUser = new UserAccount(snaph.fbToken, snaph.fbUserId, true);
    	SellerInfo seller = new SellerInfo(fbUser, null, AndroidUserCommand.INSERT);
    	Thread thread = new UploaderThread(this.getBaseContext(), list, seller);
    	thread.start();
    	try {
			thread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	snaph.getAdapter().clear();
    	Thread thread2 = new RetrieverThread(getApplicationContext(), snaph.fbUserId, snaph.getAdapter());
    	thread2.start();
    	snaph.notifyAdapterChange();
    	finish();
    }
}
