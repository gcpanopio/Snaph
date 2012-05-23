package com.onb.snaph;

import java.math.BigDecimal;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

/**
 * Represents the edit form and edit function of the application.
 * 
 * Given a selected listing, the user can edit the listing's basic information (name, price and description).
 * The user can then send the edited version to the web application for update.
 * 
 */
public class EditFormActivity extends Activity{
	protected static final String TAG = EditFormActivity.class.getSimpleName();
		
	private ImageView image;
	private EditText title;
	private EditText description;
	private EditText price;
	private SnaphApplication snaph;
	private Listing  item;
	private int itemPosition;
	
	/**
	 * 
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.edit_form);
	    	    
	    snaph = (SnaphApplication) getApplication();
        
        Intent viewForm = this.getIntent();
        itemPosition = viewForm.getIntExtra("item_position", -1);
        Log.d(TAG, "Item pos: "+itemPosition);
        item = snaph.getListingAtPosition(itemPosition);

	    image = (ImageView) findViewById(R.id.image_edit);
        image.setImageBitmap(item.getImage());
        title = (EditText) findViewById(R.id.title_edit);
        title.setText(item.getName());
        description = (EditText) findViewById(R.id.description_edit);
        description.setText(item.getDescription());
        price = (EditText) findViewById(R.id.price_edit);
        price.setText(item.getPrice().toString());
	}
	
	/**
	 * Finishes the EditFormActivity 
	 * @param view
	 */
	public void onCancel(View view){
		finish();
	}
	
	/**
	 * Sends the updated version of the selected listing to the web application.
	 * 
	 * @param view
	 */
	public void onUpdate(View view){
		Listing list = new Listing(title.getText().toString(), description.getText().toString(), new BigDecimal(price.getText().toString()), item.getImage());
		list.setItemId(snaph.getItemIdAtPosition(itemPosition));
		Log.d(TAG, list.toString());
		
		UserAccount fbUser = new UserAccount(snaph.getFbToken(), snaph.getFbUserId(), true);
		SellerInfo seller = new SellerInfo(fbUser, null, AndroidUserCommand.EDIT);
		
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
