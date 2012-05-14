package com.onb.snaph;

import java.math.BigDecimal;

import android.graphics.Bitmap;

/**
 * 
 * The primary purpose of this class is to serve as a wrapper to the input data to be sent to the web application.
 * This is the reason why there are no setter methods
 * 
 * @author ken
 *
 */

public class Listing {
	private String name;
	private String description;
	private BigDecimal price;
	private Bitmap image;
	
	public Listing(String name, String description, BigDecimal price, Bitmap image) {
		this.name = name;
		this.description = description;
		this.price = price;
		this.image = image;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public Bitmap getImage() {
		return image;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((image == null) ? 0 : image.hashCode());
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
		if (!(obj instanceof Listing))
			return false;
		Listing other = (Listing) obj;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (image == null) {
			if (other.image != null)
				return false;
		} else if (!image.equals(other.image))
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
