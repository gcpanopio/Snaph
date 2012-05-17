
package com.onb.snaph;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
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
import android.util.Pair;
import android.widget.Toast;

public class UploaderThread extends Thread {
	private Context context;
	private Listing listing;
	private String token;
	private String userId;
	
	final String address = "http://10.10.5.51:8080/Snaph/upload";
	
	private Handler handler;
	
	public UploaderThread(Context context, Listing listing, Pair<String, String> userInfo) {
		super();
		this.context = context;
		this.listing = listing;
		this.token = userInfo.second;
		this.userId = userInfo.first;
		
		handler = new Handler();
	}
	
	@Override
	public void run () {
	
		
		try {
			handler.post(makeToast("Sending data"));
			HttpResponse response = sendToNetwork();
			try {
				receiveResponse(response);
				handler.post(makeToast("Data sent"));
			} catch (IOException e) {
				e.printStackTrace();
				Log.d("DataSenderThread error", e.getMessage());
				handler.post(makeToast("Error in response"));
			}
			
		} catch (IOException e) {
			e.printStackTrace();
			Log.d("DataSenderThread error", e.getMessage());
			handler.post(makeToast("Failed to send data"));
		}		
	}
	
	private HttpResponse sendToNetwork() throws ClientProtocolException, IOException {
		HttpClient httpclient = new DefaultHttpClient();
		httpclient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
		HttpPost httppost = new HttpPost(address);
		
		httppost.setEntity(listingToMultipartEntity(listing));
		
		HttpResponse response = httpclient.execute(httppost);
		
		httpclient.getConnectionManager().shutdown();	
		
		return response;
	}
	
	private void receiveResponse(HttpResponse response) throws IOException {
		HttpEntity resEntity = response.getEntity();
		System.out.println(response.getStatusLine());
		if (resEntity != null) {
		     System.out.println(EntityUtils.toString(resEntity));
		     resEntity.consumeContent();
		}
	}
	
	private MultipartEntity listingToMultipartEntity(Listing listing) throws UnsupportedEncodingException {
		MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		
		entity.addPart("name", new StringBody(listing.getName()));
		entity.addPart("description", new StringBody(listing.getDescription()));
		entity.addPart("price", new StringBody(listing.getPrice().toString()));
		entity.addPart("image", bitmapToByteArrayBody(listing.getImage()));
		entity.addPart("userId", new StringBody(userId));
		entity.addPart("token", new StringBody(token));
		
		return entity;
	}
	
	private ByteArrayBody bitmapToByteArrayBody(Bitmap image) {
	     ByteArrayOutputStream bos = new ByteArrayOutputStream();
	     image.compress(CompressFormat.JPEG, 75, bos);
	     
	     byte[] data = bos.toByteArray();
	     
	     Log.d("image size sent after compression", ""+data.length);
	     
	     return new ByteArrayBody(data, "image.jpg");
	}
	
	private Runnable makeToast(final String message) {
		return new Runnable() {
			
			public void run() {
				Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
			}		
		};
	}
}
