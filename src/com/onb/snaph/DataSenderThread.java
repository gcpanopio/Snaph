package com.onb.snaph;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import java.util.List;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;


public class DataSenderThread extends Thread {
	
	final String address = "http://192.168.0.114/upload.php";
	
	private List<BasicNameValuePair> list;
	private HttpClient httpclient;
	private HttpPost httppost; 
	private Handler handler;
	private Context context;
	
	public DataSenderThread(Context context, Listing listing) {		
		this.list = listingToListPairs(listing);
		this.context = context;

		httpclient = new DefaultHttpClient();
		httpclient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
		httppost = new HttpPost(address);
		
		handler = new Handler();
	}

	@Override
	public void run() {
		try {
			handler.post(makeToast("Sending data"));
			sendToNetwork();
			handler.post(makeToast("Data sent"));
		} catch (IOException e) {
			e.printStackTrace();
			//Log.d("DataSenderThread error", e.getMessage());
			handler.post(makeToast("Failed to send data"));
		}
	}
	
	private void sendToNetwork () throws IOException  {
		httppost.setEntity(new UrlEncodedFormEntity(list));
		System.out.println("executing request " + httppost.getRequestLine());
		HttpResponse response = httpclient.execute(httppost);
						
		HttpEntity resEntity = response.getEntity();
		System.out.println(response.getStatusLine());
		if (resEntity != null) {
		     System.out.println(EntityUtils.toString(resEntity));
		     resEntity.consumeContent();
		}
		httpclient.getConnectionManager().shutdown();		
	}
	
	private List<BasicNameValuePair> listingToListPairs (Listing listing) {
		List<BasicNameValuePair> list = new ArrayList<BasicNameValuePair>();

		list.add(new BasicNameValuePair("name", listing.getName()));
        list.add(new BasicNameValuePair("description", listing.getDescription()));
        list.add(new BasicNameValuePair("price", listing.getPrice().toString()));
        list.add(new BasicNameValuePair("image", bitmapToString(listing.getImage())));
        
        return list;
	}
	
	private String bitmapToString(Bitmap image) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
		
		return Base64.encodeToString(outputStream.toByteArray(), 0);
	}	
	
	private Runnable makeToast(final String message) {
		return new Runnable() {
			public void run () {
				Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
			}		
		};
	}
		
}
