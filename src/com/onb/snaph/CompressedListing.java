package com.onb.snaph;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;


/**
 * CompressedListing represents an item fetched from the web app.
 * 
 * It is expected that several items will be retrieved by the mobile app from the web app.
 * That is why it is important to have another domain to hold lightweight version of Listing domain.
 * 
 * Lightweight version means, unlike Listing, CompressedListing substitutes URI rather than Bitmap for images.
 * 
 * The fields of CompressedListing are also immutable. To modify, there is a method toListing() which
 * converts the fields into a mutable instance of Listing (yep, URI is transformed into Bitmap!)
 * 
 * @author ken
 *
 */
public class CompressedListing {
	private int itemId;
	private String name;
	private String price;
	private String description;
	private String imageUri;
	private String itemUrl;
	
	public CompressedListing(int itemId, String name, String description, String price, String imageUri, String itemUrl) {
		this.itemId = itemId;
		this.name = name;
		this.description = description;
		this.price = price;
		this.imageUri = imageUri;
		this.itemUrl = itemUrl;
	}
	
	public int getItemId() {
		return itemId;
	}
	public String getName() {
		return name;
	}
	public String getPrice() {
		return price;
	}
	public String getDescription() {
		return description;
	}
	public String getImageUri() {
		return imageUri;
	}
	public String getItemUrl() {
		return itemUrl;
	}
	/**
	 * This function takes time to complete because image will be fetched from web.
	 * A Toast or Loading dialog is encouraged when this function is called.
	 * @return
	 */
	public Listing toListing () {
		BigDecimal price = new BigDecimal(this.getPrice());
		Bitmap image = this.getImageBitmap();
		Listing result = new Listing(this.getName(), this.getDescription(), price, image);		
		result.setItemId(this.getItemId());
		
		return result;
	}
	
	public Bitmap getImageBitmap() {
		Bitmap result = null;
		ArrayList<Bitmap> list = new ArrayList<Bitmap>(1);
		Thread thread = bitmapListThread(list);
		thread.start();
		try {
			thread.join();
			result = list.get(0);
		} catch (Exception e) {
			Log.d("thread join error", e.getMessage());
		}
		return result;
	}
	
	private Thread bitmapListThread(final List<Bitmap> list){
		Thread thread = new Thread() {
			@Override
			public void run() {
				try {
					InputStream inputStream = openStream(imageUri);
				    BufferedInputStream bufferedStream = new BufferedInputStream(inputStream, 80000);
				    Bitmap bitmap = BitmapFactory.decodeStream(bufferedStream);
				    inputStream.close();
				    bufferedStream.close();
				    
					list.add(bitmap);
				} catch (MalformedURLException e) {
					Log.d("URL Error in openStream", e.getMessage());
					e.printStackTrace();
				} catch (IOException e) {
					Log.d("IO Exception in uriToBitmap", e.getMessage());
					e.printStackTrace();
				}
			}		
		};
		return thread;
	}

	private InputStream openStream(String imageUri) throws MalformedURLException, IOException {
		URLConnection connection = new URL(imageUri).openConnection();
		connection.connect();
		
		InputStream inputStream = connection.getInputStream();
		return inputStream;
	}
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder("(");
		result.append(getItemId()).append(",").append(getName()).append(",");
		result.append(getDescription()).append(",").append(getPrice()).append(",");
		result.append(getImageUri()).append(",").append(getItemUrl()).append(")");
		
		return result.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + itemId;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((price == null) ? 0 : price.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof CompressedListing))
			return false;
		CompressedListing other = (CompressedListing) obj;
		if (itemId != other.itemId)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (price == null) {
			if (other.price != null)
				return false;
		} else if (!price.equals(other.price))
			return false;
		return true;
	}
}
