package com.onb.snaph;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

/**
 *
 *
 */
public class RetrieverThread extends Thread {

	private String address = "http://10.10.6.127:8080/Snaph/retrieve";
	
	private String fbUserId;
	private ArrayAdapter<CompressedListing> adapter;
	private Context context;
	private Handler handler;
	
	public RetrieverThread(Context context, String fbUserId, ArrayAdapter<CompressedListing> adapter) {
		super();
		
		this.fbUserId = fbUserId;
		this.adapter = adapter;
		this.context = context;
		handler = new Handler();
	}

	@Override
	public void run() {

		showToast("Retrieving your items");
		JSONArray jsonArray;
		try {
			jsonArray = getListFromWebApp();			
			parseListAndInsertToAdapter(jsonArray);
			if (adapter.isEmpty()) {
				showToast("No items found");
			} else {
				showToast(adapter.getCount()+" item(s) retrieved");	
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
			showToast("Bad web data");
		} catch (IOException e) {
			showToast("Failed to connect");
			e.printStackTrace();
		} catch (Exception e) {
			showToast("Error occured");
			Log.d("other exceptions", e.getMessage());
			e.printStackTrace();
		}
	}
	
	private JSONArray getListFromWebApp() throws IOException, JSONException, Exception {
		HttpPost httppost = new HttpPost(address);
		
		httppost.setEntity(makeEntityRequest(fbUserId));
		String jsonString = executeHttpRequest(httppost);
		if (jsonString.equals("error")) {
			Log.d("WebApp posted this error", jsonString);
			throw new Exception("Web posted an error");
		}
		Log.d("raw json string", jsonString);
		JSONArray array = extractJsonArrayFrom(jsonString);
				
		return array;
	}
	
	private String executeHttpRequest(HttpPost httppost) throws ClientProtocolException, IOException {
		
		HttpClient httpclient = new DefaultHttpClient();
		
		HttpResponse response = httpclient.execute(httppost);
		HttpEntity entity = response.getEntity();		
		String stringResponse = EntityUtils.toString(entity);
		entity.consumeContent();
		
		httpclient.getConnectionManager().shutdown();
		
		return stringResponse;
	}

	private MultipartEntity makeEntityRequest(String userId) throws UnsupportedEncodingException {

		MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		entity.addPart("facebookUserId", new StringBody(userId));
		
		return entity;
	}
	private void parseListAndInsertToAdapter(JSONArray jsonArray) throws JSONException {
		
		adapter.clear();
		
		//lets declare variables outside the loop so we can reuse them every iteration
		JSONObject item;
		CompressedListing listing;
		
		for (int i=0; i<jsonArray.length(); i++) {
			item = jsonArray.getJSONObject(i);
			listing = parseJSONObject(item);
			adapter.add(listing);
			Log.d("parsed Listing", listing.toString());
		}
	}
	
	private CompressedListing parseJSONObject(JSONObject obj) throws JSONException {
		String name = obj.getString("name");
		String description = obj.getString("description");
		String price = obj.getString("price");
		String imageUri = obj.getString("image");
		int itemId = obj.getInt("itemId");
		String itemUrl = obj.getString("itemUrl");
		
		return new CompressedListing(itemId, name, description, price, imageUri, itemUrl);
	}
	
	private JSONArray extractJsonArrayFrom(String jsonString) throws JSONException {
		JSONObject obj = new JSONObject(jsonString);
		JSONArray array = obj.getJSONArray("items");
		return array;
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
