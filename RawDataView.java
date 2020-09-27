package com.imperial.biap;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class RawDataView extends Activity{
	// Debugging
    private static final String TAG = "Data View";
    private static final boolean D = true;
    
    // Member fields
    private ArrayAdapter<String> mArrayAdapter;
    
//    private String[] latestData = {"","","","","","","","","","","","",}; 
    private String[] latestData;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.raw_data_view);
		if(D) Log.e(TAG, "+++ ON CREATE +++");
		
		DatabaseManager.createDatabase(this);
		
		// Initialise array adapters.
		mArrayAdapter = new ArrayAdapter<String>(this, R.layout.data_item_layout);
		
		// Find and set up the ListView for raw data
        ListView dataListView = (ListView) findViewById(R.id.listViewRawData);
        dataListView.setAdapter(mArrayAdapter);
        
        for(int i = 0; i < 12; i++){
        latestData = QueryDBData.getLatestData(i);
        mArrayAdapter.add(latestData[2] + " | " + latestData[3] + " | " + latestData[4] + " | " + latestData[10] + " | " + latestData[5]);
        }
        
	}
	
	@Override
    public void onStart() {
		super.onStart();
        if(D) Log.e(TAG, "++ ON START ++");
        
	}
	
	@Override
    public synchronized void onPause() {
        super.onPause();
        if(D) Log.e(TAG, "- ON PAUSE -");
    }

    @Override
    public void onStop() {
        super.onStop();
        if(D) Log.e(TAG, "-- ON STOP --");
    }
}
