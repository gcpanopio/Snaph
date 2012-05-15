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

public class RetrieverThread extends Thread {

	private String address = "http://10.10.5.122:8080/Snaph/retrieve";
	
	private String token;
	private ArrayAdapter<CompressedListing> adapter;
	
	// these are for the toast
	private Context context;
	private Handler handler;
	public RetrieverThread(Context context, String token, ArrayAdapter<CompressedListing> adapter) {
		super();
		this.token = token;
		this.adapter = adapter;
		
		this.context = context;
		handler = new Handler();
	}

	@Override
	public void run() {

		showToast("fetching from web");
		JSONArray jsonArray;
		try {
			jsonArray = getListFromWebApp();			
			parseListAndInsertToAdapter(jsonArray);
			showToast("done fetching");
			
		} catch (JSONException e) {
			e.printStackTrace();
			showToast("Bad web data");
		} catch (IOException e) {
			showToast("Connection failed");
			e.printStackTrace();
		} catch (Exception e) {
			showToast("Error occured");
			Log.d("Retrieving error", e.getMessage());
			e.printStackTrace();
		}
	}
	
	private JSONArray getListFromWebApp() throws ClientProtocolException, IOException, JSONException {
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(address);
		
		httppost.setEntity(makeEntityRequest(token));
		
		String jsonString = executeHttpRequest(httpclient, httppost);
		Log.d("Response string from server", jsonString);

		httpclient.getConnectionManager().shutdown();
		
		JSONObject jsonObject = new JSONObject(jsonString);
		
		return jsonObject.getJSONArray("items");
	}
	
	private MultipartEntity makeEntityRequest(String token) throws UnsupportedEncodingException {
		MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		entity.addPart("token", new StringBody(token));
		return entity;
	}
	
	private String executeHttpRequest(HttpClient httpclient, HttpPost httppost) throws ClientProtocolException, IOException {
		HttpResponse response = httpclient.execute(httppost);
		HttpEntity entity = response.getEntity();
		
		String stringResponse = EntityUtils.toString(entity);
		entity.consumeContent();
		
		return stringResponse;
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
	
	// parses one instance of Listing from the json array
	private CompressedListing parseJSONObject(JSONObject obj) throws JSONException {
		String name = obj.getString("name");
		String description = obj.getString("description");
		String price = obj.getString("price");
		String uri = obj.getString("image");
				
		return new CompressedListing(name, description, price, uri);
	}
	
	private void showToast(final String message) {
		Runnable toast = new Runnable() {
			@Override
			public void run () {
				Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
			}		
		};
		handler.post(toast);
	}
	
	
	/**
	 * test methods. remove after test
	 */
	
	private JSONArray makeJSONArraySample() throws JSONException {
		String result = makeJSONString();
		
		JSONObject obj = new JSONObject(result);
		
		return obj.getJSONArray("items");
	}
	
	private String makeJSONString () {
    	StringBuilder str = new StringBuilder("{ \"items\": [");
    	str.append(makeJSONStringWithValues("house", "new", "good", "html.com")).append(", ");
    	str.append(makeJSONStringWithValues("car", "2-wheeled", "$10.00", "car.com")).append(", ");
    	str.append(makeJSONStringWithValues("lot", "100 sqr meters", "P1M", "lot_lot.com"));
    	str.append("]}");
    	
    	return str.toString();
    }
    
    private String makeJSONStringWithValues(String name, String description, String price, String uri) {
    	StringBuilder jsonListing = new StringBuilder("{");
    	jsonListing.append("\"name\": \"").append(name).append("\", ");
       	jsonListing.append("\"description\": \"").append(description).append("\", ");
       	jsonListing.append("\"price\": \"").append(price).append("\", ");
       	jsonListing.append("\"uri\": \"").append(uri).append("\" ");
    	jsonListing.append("}");
    	return jsonListing.toString();
    }
	
}
