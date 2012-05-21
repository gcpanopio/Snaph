
package com.onb.snaph;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

public class UploaderThread extends Thread {
	private Context context;
	private Listing listing;
	private SellerInfo sellerInfo;
	
	private String address = "http://124.104.156.136:8080/Snaph/upload";
	
	private Handler handler;
	
	public UploaderThread(Context context, Listing listing, SellerInfo sellerInfo) {
		this.context = context;
		this.listing = listing;
		this.sellerInfo = sellerInfo;
		
		handler = new Handler();
	}
	
	@Override
	public void run () {	
		try {
			showToast("Sending data");
			HttpResponse response = sendToNetwork();
			String message = decodeResponse(response);
			showToast(message);
		} catch (HttpHostConnectException e) {
			showToast("Failed to connect");
			Log.d("DataSenderThread Connection Exception", e.getMessage());
		} catch (IOException e) {
			showToast("Failed to send data");
			Log.d("DataSenderThread exception", e.getMessage());
			e.printStackTrace();
		}		
	}
	
	private HttpResponse sendToNetwork() throws ClientProtocolException, IOException {
		
		HttpClient httpclient = new DefaultHttpClient();
		httpclient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
		HttpPost httppost = new HttpPost(address);
		httppost.setEntity(createMultipartEntity());

		HttpResponse response = httpclient.execute(httppost);											
		httpclient.getConnectionManager().shutdown();			
		return response;
	}
	
	private String decodeResponse(HttpResponse response) throws ParseException, IOException {
		String  strResponse = EntityUtils.toString(response.getEntity());
		Log.d("Upload: Server says", strResponse);
		
		String message;
		
		if (strResponse.equals("saved item uploaded")) {
			message = "Item posted";
		} else if (strResponse.startsWith("edited item")) {
			message = "Item updated";
		} else if (strResponse.startsWith("edited item")) {
			message = "Item deleted";
		} else if (strResponse.equals("error")) {
			message = "error occurred";
		} else {
			message = "data sent";
		}
		return message;
	}
	
	private MultipartEntity createMultipartEntity() throws UnsupportedEncodingException {
	
		MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

		entity.addPart("itemId", new StringBody(listing.getItemIdAsString()));
		entity.addPart("command", new StringBody(sellerInfo.getCommandAsString()));

		// reduce the information to be sent by not sending anything else
		if (sellerInfo.getCommand() != AndroidUserCommand.DELETE){
			addListingToEntity(entity);
			addAccountToEntity(entity, sellerInfo.getFacebook(), "facebook");
			addAccountToEntity(entity, sellerInfo.getTwitter(), "twitter");
		} else {
			entity.addPart("facebookUserId", new StringBody(sellerInfo.getFacebook().getUserId()));
		}
		
		/*
		//to be deleted
		try {
			Log.d("entities before sending", entity.toString());
		} catch (ParseException e) {
			e.printStackTrace();
		}
		*/
		return entity;
	}
	
	private void addListingToEntity(MultipartEntity entity) throws UnsupportedEncodingException {
		entity.addPart("name", new StringBody(listing.getName()));
		entity.addPart("description", new StringBody(listing.getDescription()));
		entity.addPart("price", new StringBody(listing.getPrice().toString()));
		entity.addPart("image", bitmapToByteArrayBody(listing.getImage()));	
	}
	
	private void addAccountToEntity(MultipartEntity entity, UserAccount account, String prefix) throws UnsupportedEncodingException {
		if (account == null) {
			account = new UserAccount("none","none",false);
		}
		entity.addPart(prefix+"Token", new StringBody(account.getAccessToken()));
		entity.addPart(prefix + "UserId", new StringBody(account.getUserId()));
		entity.addPart("postTo" + prefix, new StringBody(account.toPost()));
	}
	
	private ByteArrayBody bitmapToByteArrayBody(Bitmap image) {
	
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
	    image.compress(CompressFormat.JPEG, 75, bos);
	     
	    byte[] data = bos.toByteArray();
	     
	    return new ByteArrayBody(data, "image.jpg");
	}
	
	private void showToast(final String message) {
		Runnable toast = new Runnable() {
			
			public void run() {
				Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
			}		
		};
		handler.post(toast);
	}
}
