package com.onb.snaph;

import java.math.BigDecimal;

import android.graphics.Bitmap;

/**
 * 
 * The primary purpose of this class is to serve as a wrapper of mutable data.
 * 
 * Creating several instances of Listing is impractical.
 * It shall only be used to hold the values of the active item (i.e, the one being created, modified, or deleted)
 * 
 * The fields of Listing are the ones being sent to the web appl.
 * 
 * @author ken
 *
 */

public class Listing {
	private String name;
	private String description;
	private BigDecimal price;
	private Bitmap image;
	private String itemUrl;
	private String imageUrl;
	//this is the database id from web app. A new Listing in android app has no itemId
	private int itemId;
	
	public Listing(String name, String description, BigDecimal price, Bitmap image, int itemId, String itemUrl, String imageUrl) {
		this.name = name;
		this.description = description;
		this.price = price;
		this.image = image;
		this.imageUrl = imageUrl;
		this.itemId = itemId;
		this.setItemUrl(itemUrl);
	}
	
	public Listing(String name, String description, BigDecimal price, Bitmap image) {
		this(name, description, price, image, -1, "", "");
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public BigDecimal getPrice() {
		return price;
	}
	
	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public Bitmap getImage() {
		return image;
	}
	
	public void setImage(Bitmap image) {
		this.image = image;
	}
	
	public int getItemId() {
		return itemId;
	}
	
	public String getItemIdAsString() {
		return Integer.toString(getItemId());
	}
	
	public void setItemId(int itemId){
		this.itemId = itemId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}
	
	public String toString() {
		StringBuilder result = new StringBuilder("(");
		result.append(getItemId()).append(",");
		result.append(getName()).append(",").append(getDescription()).append(",");
		result.append(getPrice()).append(")");
		
		return result.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Listing))
			return false;
		Listing other = (Listing) obj;
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

	public String getItemUrl() {
		return itemUrl;
	}

	public void setItemUrl(String itemUrl) {
		this.itemUrl = itemUrl;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
}
