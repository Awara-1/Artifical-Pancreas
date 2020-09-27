package com.imperial.biap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class SendJSON {
	
	private static final String TAG = "json";
	
	public static void postData(String[] queryArray) throws JSONException{   
		
		// Create a new HttpClient and Post Header
        HttpClient httpClient = new DefaultHttpClient();
//        HttpPost httpPost = new HttpPost("http://posttestserver.com/post.php");
        HttpPost httpPost = new HttpPost("http://155.198.108.137:9999/get_data/");
//        HttpPost httpPost = new HttpPost("http://129.31.207.21:8444/get_data/");
        JSONObject json = new JSONObject();
        String text;
 
        try {
            // JSON data:
            json.put("data_id", queryArray[0]);
            json.put("patient", queryArray[1]);
            json.put("date", queryArray[2]);
            json.put("time", queryArray[3]);
            json.put("cgm_value", queryArray[4]);
            json.put("insulin_infusion", queryArray[5]);
            json.put("sr", queryArray[6]);
            json.put("insulin_feed", queryArray[7]);
            json.put("controller_gain", queryArray[8]);
            json.put("mean_glucose", queryArray[9]);
            json.put("glucose_derivative", queryArray[10]);
            json.put("safety", queryArray[11]);
            json.put("basal_insulin", queryArray[12]);
        	   	
            JSONArray postJson = new JSONArray();
            postJson.put(json);
            
//            StringEntity se = new StringEntity(json.toString());
 
            // Post the data:
            httpPost.setHeader("json",json.toString());
//            httpPost.setEntity(se);
            httpPost.getParams().setParameter("jsonpost",postJson);
 
            // Execute HTTP Post Request
            HttpResponse response = httpClient.execute(httpPost);
 
            //Handle JSON response
            if(response != null)
            {
                InputStream is = response.getEntity().getContent();
 
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder sb = new StringBuilder();
 
                String line = null;
                try {
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                text = sb.toString();
                Log.d(TAG, text);
            }
 
        }catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
        } catch (IOException e) {
            // TODO Auto-generated catch block
        }
    }
}
