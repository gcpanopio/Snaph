package com.onb.snaph;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

public class ViewListingActivity extends Activity{

	private ImageView image;
	private EditText title;
	private EditText description;
	private EditText price;
	private SnaphApplication snaph;
	
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
        
      /*  snaph = (SnaphApplication) getApplication();
        
        image = (ImageView) findViewById(R.id.image_view);
        image.setImageBitmap(snaph.getImage());
        title = (EditText) findViewById(R.id.title_view);
        description = (EditText) findViewById(R.id.description_view);
        price = (EditText) findViewById(R.id.price_view);*/
    }
	
	public void onDelete(){
		
	}
	
	public void onEdit(){
		
	}
	
	public void onBack(View view){
		finish();
	}
}
