package com.onb.snaph;


/**
 * Compressed listing represents a lightweight Listing which is fetched from the web app.
 * A list of Listings will be retrieved by android from the web, that is why it is important
 * that android will not use a lot of memory spaces after the list is retrieved.
 * 
 *  An object of the class CompressedListing shall consists of strings. The image part will be
 *  replaced by a uri, for which the image can be retrieved on demand from the web.
 * 
 * @author ken
 *
 */
public class CompressedListing {
	private String name;
	private String price;
	private String description;
	private String uri;
	
	
	public CompressedListing(String name, String description, String price, String uri) {
		super();
		this.name = name;
		this.description = description;
		this.price = price;
		this.uri = uri;
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
	public String getUri() {
		return uri;
	}
	
	public String toString() {
		StringBuilder result = new StringBuilder("<");
		result.append(getName()).append(",").append(getDescription()).append(",");
		result.append(getPrice()).append(",").append(getUri()).append("<");
		
		return result.toString();
	}


}
