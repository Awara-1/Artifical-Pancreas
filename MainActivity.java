package com.imperial.biap;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData; 
import com.jjoe64.graphview.GraphViewSeries;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.ViewPager;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends Activity implements SettingsFragment.OnHeadlineSelectedListener, ChangeColor.OnHeadlineSelectedListener, ActionBar.TabListener {
	//GRAPHS
	private static final int GRAPH_VIEW = 1;
	private static final int GRAPH_VIEW_2 = 2;
	
	//Example Series
	private static final int EXAMPLE_SERIES = 0;
	private static final int EXAMPLE_SERIES_LOW = 1;
	private static final int EXAMPLE_SERIES_HIGH = 2;
	private static final int EXAMPLE_SERIES_2 = 3;
	
	// Debugging
    private static final String TAG = "Biap";
    private static final boolean D = true;
    
    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    
    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
    
    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int REQUEST_VIEW_DATA = 3;
    
    // Name of the connected device
    private String mConnectedDeviceName = null;
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the chat services
    private BluetoothService mBluetoothService = null;
    
    //Notification
    private NotificationManager mNotificationManager;
    private int notificationIDConnection = 100;
    private int notificationIDGlucoseWarning = 101;
//    private int notificationIDdGWarning = 102;
    
  //Fragments
//TODO: Add other fragment #'s
  	private static final int GRAPH_FRAGMENT = 1;
  	private static final int ALARM_FRAGMENT = 2;
  	private static final int SETTINGS_FRAGMENT = 3;
  	
    private static GraphFragment graphFragment = GraphFragment.newInstance(GRAPH_FRAGMENT);
    private static AlarmFragment alarmFragment = AlarmFragment.newInstance(ALARM_FRAGMENT);
    private static SettingsFragment settingsFragment = SettingsFragment.newInstance(SETTINGS_FRAGMENT);
    
  //TODO: Add other fragments here
    
    private int numMessages = 0;
    
    private AudioManager Audio;
    
	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a {@link FragmentPagerAdapter}
	 * derivative, which will keep every loaded fragment in memory. If this
	 * becomes too memory intensive, it may be best to switch to a
	 * {@link android.support.v13.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;
    
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
  		
		// Create the adapter that will return a fragment for each of the three
		// primary sections of the activity.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager
		.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				actionBar.setSelectedNavigationItem(position);
			}
		});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.
			actionBar.addTab(actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}
    			
		if(D) Log.e(TAG, "+++ ON CREATE +++");
		
		// Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        
        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available on this device", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
        //settingsFragment.CreateListView(graphFragment, alarmFragment);
        
        
	}
	
	@Override
    public void onStart() {
		super.onStart();
        if(D) Log.e(TAG, "++ ON START ++");
        
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        // Otherwise, setup the chat session
        } else {
//        	if(!(mBluetoothService.getState() == BluetoothService.STATE_CONNECTED)){
//        		Toast.makeText(this, "Bluetooth is enabled, please conenct to a device", Toast.LENGTH_LONG).show();
//        	}
        }
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop the running threads
//        if (mBluetoothService != null) mBluetoothService.stop();
        if(D) Log.e(TAG, "--- ON DESTROY ---");
    }
    
    //Fragment Instances getter
  	private Fragment getFragment(int fragment) { 		
  		switch(fragment) {
  			case GRAPH_FRAGMENT: return graphFragment;
  			case ALARM_FRAGMENT: return alarmFragment;
  			case SETTINGS_FRAGMENT: return settingsFragment;
  			
  		}
		
  		//NOTE: Code should never reach this.
  		return graphFragment;
  	}
  	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(D) Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
        case REQUEST_CONNECT_DEVICE:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                // Get the device MAC address
                String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                // Get the BLuetoothDevice object
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                // Initialize the BluetoothChatService to perform bluetooth connections
                mBluetoothService = new BluetoothService(this, mHandler);
                // Attempt to connect to the device
                mBluetoothService.connect(device);
            }
            break;
        case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
                // Bluetooth is now enabled, so set up a chat session
            	Toast.makeText(this, "Bluetooth is enabled, please conenct to a device", Toast.LENGTH_SHORT).show();
            } else {
                // User did not enable Bluetooth or an error occurred
                Log.d(TAG, "BT not enabled");
                Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
//                finish();
            }
        }
        
    }
	
	protected void displayNotification(String title, String text, int notificationID, int alertlevel) {
	      Log.i("Start", "notification");

	       //Invoking the default notification service 
	      NotificationCompat.Builder  mBuilder = 
	      new NotificationCompat.Builder(this);	

	      mBuilder.setContentTitle(title);
	      mBuilder.setContentText(text);
	      mBuilder.setTicker("BiAP Alert!");
	      mBuilder.setSmallIcon(R.drawable.ic_launcher);
	      
	      if((notificationID == notificationIDConnection) && (alertlevel == 1)){
	    	  //Blue LED notification 
	    	  mBuilder.setLights(0xff0000ff, 1000, 1000);
	    	  mBuilder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);
	    	  mBuilder.setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000, 1000 });
	    	  numMessages++;
	    	  
	      }else if(notificationID == notificationIDGlucoseWarning){
	    	  if(alertlevel == 1){
	    		  //Red LED notification 
	    		  mBuilder.setLights(0xffff0000, 1000, 1000);
	    		  mBuilder.setVibrate(new long[] { 1000, 1000, 1000, 1000, });
	    	  }
	    	  if(alertlevel == 2){
	    		  //Red LED notification 
	    		  mBuilder.setLights(0xffff0000, 1000, 1000);
		    	  mBuilder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);
		    	  mBuilder.setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000, 1000 });
	    	  }
	    	  
	      }
	      
	      // Creates an explicit intent for an Activity in your app 
	      Intent resultIntent = new Intent(this, MainActivity.class);

	      TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
	      stackBuilder.addParentStack(MainActivity.class);

	      // Adds the Intent that starts the Activity to the top of the stack 
	      stackBuilder.addNextIntent(resultIntent);
	      PendingIntent resultPendingIntent =
	         stackBuilder.getPendingIntent(
	            0,
	            PendingIntent.FLAG_UPDATE_CURRENT
	         );

	      mBuilder.setContentIntent(resultPendingIntent);

	      mNotificationManager =
	      (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

	      // notificationID allows you to update the notification later on. 
	      mNotificationManager.notify(notificationID, mBuilder.build());
	   }
	
	protected void cancelNotification(int notificationID) {
	      Log.i("Cancel", "notification");
	      mNotificationManager.cancel(notificationID);
    }
	
	// The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_STATE_CHANGE:
                if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                switch (msg.arg1) {
                case BluetoothService.STATE_CONNECTED:
                	if(numMessages > 0){
                		cancelNotification(notificationIDConnection); 
                	}
                	break;
                case BluetoothService.STATE_CONNECTING:
                	Toast.makeText(getApplicationContext(), "Connecting ", Toast.LENGTH_SHORT).show();
                    break;
                case BluetoothService.STATE_NONE:
                    break;
                }
                break;
            case MESSAGE_READ:	
                String[] readBuf = (String[]) msg.obj;
           
                //Get the current time
                long now = new Date().getTime();
                
                graphFragment.setViewPort(GRAPH_VIEW, now, 120000);
                graphFragment.setViewPort(GRAPH_VIEW_2, now, 120000);
                
                //Insert new data into the graph
                GraphViewData newData = new GraphViewData(now, Float.parseFloat(readBuf[4]));
                GraphViewData newDataLow = new GraphViewData(now, 3.5);
                GraphViewData newDataHigh = new GraphViewData(now, 12);
                GraphViewData newData2 = new GraphViewData(now , Float.parseFloat(readBuf[5]));              
                
                graphFragment.appendData(EXAMPLE_SERIES, newData, true, 12);
                graphFragment.appendData(EXAMPLE_SERIES_LOW, newDataLow, true, 12);
                graphFragment.appendData(EXAMPLE_SERIES_HIGH, newDataHigh, true, 12);
                graphFragment.appendData(EXAMPLE_SERIES_2, newData2, true, 12);
                
                //Control when the traffic lights and notifications are changed/sent
        		if (Float.parseFloat(readBuf[4]) < 3.5) {
        			alarmFragment.setLights(R.drawable.red_l);
        			alarmFragment.setYoda(R.raw.low_gl);
        		} else if (Float.parseFloat(readBuf[4]) < 4.5) {
        			alarmFragment.setLights(R.drawable.yellow_l);
        			alarmFragment.setYoda(R.raw.decreasing_gl);
        		} else if (Float.parseFloat(readBuf[4]) > 12) {
        			alarmFragment.setLights(R.drawable.red_r);
        			alarmFragment.setYoda(R.raw.high_gl);
        		} else if (Float.parseFloat(readBuf[4]) > 11) {
        			alarmFragment.setLights(R.drawable.yellow_r);
        			alarmFragment.setYoda(R.raw.increasing_gl);
        		} else {
        			alarmFragment.setLights(R.drawable.green);
        			alarmFragment.setYoda(R.raw.normal_gl);
        		}
                
                
                if(Float.parseFloat(readBuf[4]) < 3.5){
                	displayNotification("Hypoglycemia", "Warning Hypoglycaemia", notificationIDGlucoseWarning, 2);               	
                }else if((Float.parseFloat(readBuf[4])) < 4.5){
                	displayNotification("Glucose Levels Low", "Nearing Hypoglycaemic range", notificationIDGlucoseWarning, 1);
                }else if(Float.parseFloat(readBuf[4]) > 12){
                	displayNotification("Hyperglycemia", "Warning Hyperglycaemia", notificationIDGlucoseWarning, 2);
                }else if(Float.parseFloat(readBuf[4]) > 11){
                	displayNotification("Glucose Levels High", "Nearing Hyperglycaemic range", notificationIDGlucoseWarning, 1);
                }else{
                	displayNotification("Glucose OK", "Glucose levels are normal", notificationIDGlucoseWarning, 0);
                }
                
                //Control when the arrows are changed
                if(Float.parseFloat(readBuf[10]) < -0.11){
                	alarmFragment.setArrow(R.drawable.south);
                }else if((Float.parseFloat(readBuf[10])) < -0.06){
                	alarmFragment.setArrow(R.drawable.southeast);
                }else if(Float.parseFloat(readBuf[10]) > 0.11){
                	alarmFragment.setArrow(R.drawable.north);
                }else if(Float.parseFloat(readBuf[10]) > 0.06){
                	alarmFragment.setArrow(R.drawable.northeast);
                }else{
                	alarmFragment.setArrow(R.drawable.east);
                }
                
                break;
            case MESSAGE_DEVICE_NAME:
                // save the connected device's name
                mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                Toast.makeText(getApplicationContext(), "Connected to "
                               + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                break;
            case MESSAGE_TOAST:
                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                               Toast.LENGTH_SHORT).show();
                if(msg.getData().getString(TOAST).equalsIgnoreCase("Device connection was lost")){
                	displayNotification("No Bluetooth Connection", "Please connect to a Bluetooth Device", notificationIDConnection, 1);
                }
                break;
            }
            
        }
    };
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		
		if(id == R.id.option_connect){
			// Launch the DeviceListActivity to see devices and do scan
			Intent serverIntent = new Intent(this, DeviceListActivity.class);
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
            return true;
		}
		
		if(id == R.id.option_data){
			// Launch the RawDataView activity to see database
			Intent serverIntent = new Intent(this, RawDataView.class);
			startActivityForResult(serverIntent, REQUEST_VIEW_DATA);
            return true;
		}
		
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		mViewPager.setCurrentItem(tab.getPosition());	
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {

	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
	
	}
	
	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter  {

		public SectionsPagerAdapter(FragmentManager fragmentManager) {
			super(fragmentManager);
		}
		
        @Override
        public Fragment getItem(int position) {
        	switch(position){
        	case 0:
        		Log.d("POSITION", "Case 0: " + position);
        		return graphFragment;
            case 1:
            	Log.d("POSITION", "Case 1: " + position);
            	return alarmFragment;
        	case 2:
        		Log.d("POSITION", "Case 2: " + position);
        		return settingsFragment;
        	}
        	return null;
        }

		@Override
		public int getCount() {
			// Show 3 total pages.
			return 3;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.graphs_title).toUpperCase(l);
			case 1:
				return getString(R.string.alerts_title).toUpperCase(l);
			case 2:
				return getString(R.string.settings_title).toUpperCase(); //TODO: getString(R.string.title_section3).toUpperCase(l);
			}
			return null;
		}
	}
	
	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		private static final String ARG_SECTION_NUMBER = "section_number";

		/**
		 * Returns a new instance of this fragment for the given section number.
		 */
		public static PlaceholderFragment newInstance(int sectionNumber) {
			PlaceholderFragment fragment = new PlaceholderFragment();
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			fragment.setArguments(args);
			return fragment;
		}

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}
	}

	public void onArticleSelectedMain(int position) {
		// TODO Auto-generated method stub
		// The user selected the headline of an article from the HeadlinesFragment
        // Do something here to display that article
		if(position==0){
			//Intent intent = new Intent(MainActivity.this, ActivityChangeColor.class);
			//startActivity(intent);
		    // Create new fragment and transaction
		    Fragment newFragment = new ChangeColor(); 
		    // consider using Java coding conventions (upper first char class names!!!)
		    FragmentTransaction transaction = getFragmentManager().beginTransaction();
		    // Replace whatever is in the fragment_container view (id of SettingsFragment layout container) with this fragment,
		    // and add the transaction to the back stack
		    transaction.replace(R.id.fragment_container, newFragment);
		    transaction.addToBackStack(null);

		    // Commit the transaction
		    transaction.commit(); 
		}
		if(position==1){
			Audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
			Audio.setMode(AudioManager.MODE_IN_CALL);
	        Audio.setStreamSolo(AudioManager.STREAM_VOICE_CALL, true);
	        Toast.makeText(this, "Media player muted", Toast.LENGTH_LONG).show();
		}
		if(position==2){
			Audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
			Audio.setMode(AudioManager.MODE_NORMAL );
			Audio.setStreamSolo(AudioManager.STREAM_VOICE_CALL, false);
			Toast.makeText(this, "Media player unmuted", Toast.LENGTH_LONG).show();
		}
	}
	
  public void onArticleSelectedColor(int position) {
/*	  if(position==1){
		  GraphViewSeries testseries = graphFragment.getTestSeries();
		  graphFragment.getGraphView().addSeries(testseries);
		  
	  }*/
	 
	 graphFragment.setGridColor(position);
	
  }
	
	
}
