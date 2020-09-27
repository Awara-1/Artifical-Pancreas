package com.imperial.biap;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.UUID;

import org.json.JSONException;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread for connecting with
 *  a device and a thread for performing data transmissions when connected.
 */
public class BluetoothService {
	// Debugging
    private static final String TAG = "BluetoothService";
    private static final boolean D = true;
    
	// Unique UUID for this application
    //private static final UUID MY_UUID = UUID.fromString("d0c722b0-7e15-11e1-b0c4-0800200c9a66");
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    
    // Member fields
    private final BluetoothAdapter mAdapter;
    private final Handler mHandler;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState;
    
    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device
    
    //Incoming data flags
    private static final String checkIfPatientID = "p";
    private static final String checkIfGlucose = "g";
    private static final String checkIfInsulin = "i";
    private static final String checkIfSR = "s";
    private static final String checkIfInsulinFeed = "f";
    private static final String checkIfK = "K";
    private static final String checkIfMeanGlucose = "m";
    private static final String checkIfdG = "d";
    private static final String checkIfSafetyCondition = "c";
    private static final String checkIfBasalInsulin = "b";
    private static final String checkIfNext = "\n";
    private static final String checkIfEnd = "E";
    
    //Context passed from MainActivity to thread for handling SQLite Databases
    public Context mContext;
    
    public static int COUNTER = 0;
    
    /**
     * Constructor. Prepares a new Bluetooth connection.
     * @param context  The UI Activity Context
     * @param handler  A Handler to send messages back to the UI Activity
     */
    public BluetoothService(Context context, Handler handler) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        mHandler = handler;
        
        mContext = context;
        DatabaseManager.createDatabase(mContext);
    }
    
    /**
     * Set the current state of the chat connection
     * @param state  An integer defining the current connection state
     */
    private synchronized void setState(int state) {
        if (D) Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;

        // Give the new state to the Handler so the UI Activity can update
        mHandler.obtainMessage(MainActivity.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }
    
    /**
     * Return the current connection state. */
    public synchronized int getState() {
        return mState;
    }
    
    /**
     * Stop all threads
     */
    public synchronized void stop() {
        if (D) Log.d(TAG, "stop");
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}
        setState(STATE_NONE);
    }
    
    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed() {
        setState(STATE_NONE);

        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(MainActivity.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(MainActivity.TOAST, "Unable to connect device");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }
    
    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {
        setState(STATE_NONE);

        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(MainActivity.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(MainActivity.TOAST, "Device connection was lost");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }
    
    public boolean isNetworkConnected(Context mContext){
        ConnectivityManager connMgr = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()){ 
        	Log.d(TAG, "There is a Network Connection");
        	return true;
        } else {
        	Log.d(TAG, "There is NO Network Connection");
            return false;
        }    
    }
    
    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     * @param device  The BluetoothDevice to connect
     */
    public synchronized void connect(BluetoothDevice device) {
        if (D) Log.d(TAG, "connect to: " + device);

        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }
	
	/**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "create() failed", e);
            }
            mmSocket = tmp;
        }

        @Override
		public void run() {
            Log.i(TAG, "BEGIN mConnectThread");
            setName("ConnectThread");

            // Always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
            } catch (IOException e) {
                connectionFailed();
            	Log.e(TAG, "CONNECTION_FAILED", e);
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() socket during connection failure", e2);
                }
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (BluetoothService.this) {
                mConnectThread = null;
            }

            // Start the connected thread
            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
    
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        if (D) Log.d(TAG, "connected");

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();
        
        // Send the name of the connected device back to the UI Activity
        Message msg = mHandler.obtainMessage(MainActivity.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(MainActivity.DEVICE_NAME, device.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        setState(STATE_CONNECTED);
    }
    
    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        
        private String[] totalMessage = {"","","","","","","","","","","",""};
        private String temp = "";
        private String[] returnMessage;
        private String[] queryArray;
        
        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "create ConnectedThread");
            mmSocket = socket;
            InputStream tmpIn = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
        }

        @Override
		public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
//            byte[] buffer = new byte[1024];
//            int bytes;
            byte[] buffer1 = new byte[1];
            int oneByte;
            

            // Keep listening to the InputStream while connected
            while (true) {
                try {                	
                	// Read from the InputStream a byte at a time
//                	bytes = mmInStream.read(buffer);
                	oneByte = mmInStream.read(buffer1);
                    
                    //Convert the byte into a string
                    String readMessage = new String(buffer1, 0, oneByte);
                    
                    //Check if end of message
                    if(!readMessage.equalsIgnoreCase(checkIfEnd)){
                    	if(readMessage.equalsIgnoreCase(checkIfPatientID)){
                    		while(!readMessage.equalsIgnoreCase(checkIfNext)){
                    			oneByte = mmInStream.read(buffer1);
                    			readMessage = new String(buffer1, 0, oneByte);
                    			temp = temp + readMessage;
                    		}
                    		totalMessage[0] = temp;
                    		temp = "";
//                    		Log.d(TAG, totalMessage[0]);
                    	}else if(readMessage.equalsIgnoreCase(checkIfGlucose)){
                    		while(!readMessage.equalsIgnoreCase(checkIfNext)){
                    			oneByte = mmInStream.read(buffer1);
                    			readMessage = new String(buffer1, 0, oneByte);
                    			temp = temp + readMessage;
                    		}
                    		totalMessage[3] = temp;
                    		temp = "";
//                    		Log.d(TAG, totalMessage[3]);
                    	}else if(readMessage.equalsIgnoreCase(checkIfInsulin)){
                    		while(!readMessage.equalsIgnoreCase(checkIfNext)){
                    			oneByte = mmInStream.read(buffer1);
                    			readMessage = new String(buffer1, 0, oneByte);
                    			temp = temp + readMessage;
                    		}
                    		totalMessage[4] = temp;
                    		temp = "";
//                    		Log.d(TAG, totalMessage[4]);
                    	}else if(readMessage.equalsIgnoreCase(checkIfSR)){
                    		while(!readMessage.equalsIgnoreCase(checkIfNext)){
                    			oneByte = mmInStream.read(buffer1);
                    			readMessage = new String(buffer1, 0, oneByte);
                    			temp = temp + readMessage;
                    		}
                    		totalMessage[5] = temp;
                    		temp = "";
//                    		Log.d(TAG, totalMessage[5]);
                    	}else if(readMessage.equalsIgnoreCase(checkIfInsulinFeed)){
                    		while(!readMessage.equalsIgnoreCase(checkIfNext)){
                    			oneByte = mmInStream.read(buffer1);
                    			readMessage = new String(buffer1, 0, oneByte);
                    			temp = temp + readMessage;
                    		}
                    		totalMessage[6] = temp;
                    		temp = "";
//                    		Log.d(TAG, totalMessage[6]);
                    	}else if(readMessage.equalsIgnoreCase(checkIfK)){
                    		while(!readMessage.equalsIgnoreCase(checkIfNext)){
                    			oneByte = mmInStream.read(buffer1);
                    			readMessage = new String(buffer1, 0, oneByte);
                    			temp = temp + readMessage;
                    		}
                    		totalMessage[7] = temp;
                    		temp = "";
//                    		Log.d(TAG, totalMessage[7]);
                    	}else if(readMessage.equalsIgnoreCase(checkIfMeanGlucose)){
                    		while(!readMessage.equalsIgnoreCase(checkIfNext)){
                    			oneByte = mmInStream.read(buffer1);
                    			readMessage = new String(buffer1, 0, oneByte);
                    			temp = temp + readMessage;
                    		}
                    		totalMessage[8] = temp;
                    		temp = "";
//                    		Log.d(TAG, totalMessage[8]);
                    	}else if(readMessage.equalsIgnoreCase(checkIfdG)){
                    		while(!readMessage.equalsIgnoreCase(checkIfNext)){
                    			oneByte = mmInStream.read(buffer1);
                    			readMessage = new String(buffer1, 0, oneByte);
                    			temp = temp + readMessage;
                    		}
                    		totalMessage[9] = temp;
                    		temp = "";
//                    		Log.d(TAG, totalMessage[9]);
                    	}else if(readMessage.equalsIgnoreCase(checkIfSafetyCondition)){
                    		while(!readMessage.equalsIgnoreCase(checkIfNext)){
                    			oneByte = mmInStream.read(buffer1);
                    			readMessage = new String(buffer1, 0, oneByte);
                    			temp = temp + readMessage;
                    		}
                    		totalMessage[10] = temp;
                    		temp = "";
//                    		Log.d(TAG, totalMessage[10]);
                    	}else if(readMessage.equalsIgnoreCase(checkIfBasalInsulin)){
                    		while(!readMessage.equalsIgnoreCase(checkIfNext)){
                    			oneByte = mmInStream.read(buffer1);
                    			readMessage = new String(buffer1, 0, oneByte);
                    			temp = temp + readMessage;
                    		}
                    		totalMessage[11] = temp;
                    		temp = "";
//                    		Log.d(TAG, totalMessage[11]);
                    	}
                    	
                    }else{
                    	//Calender to retrieve the date and current time
                    	Calendar calendar = Calendar.getInstance();
                    	
                    	SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
                    	SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss");
                    	String currentDate = sdfDate.format(calendar.getTime());
                    	String currentTime = sdfTime.format(calendar.getTime());
                    	
                    	totalMessage[1] = currentDate;
//                    	Log.d(TAG, totalMessage[1]); 

                    	totalMessage[2] = currentTime;
//                    	Log.d(TAG, totalMessage[2]); 
                    	
                    	//Insert the obtained data into the database
                    	QueryDBData.insertData(totalMessage);
                    	Log.d(TAG, "INSERTED_DATA");
                    	
                    	//Check for network connection and send data to server via JSON
                    	if(isNetworkConnected(mContext)){
                        	for(; COUNTER > 0; COUNTER--){
                        		queryArray = QueryDBData.getLatestData(COUNTER);
                        		try {
                                    SendJSON.postData(queryArray);
                                } catch (JSONException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                        	}
                        	queryArray = QueryDBData.getLatestData(COUNTER);
                        	try {
                        		SendJSON.postData(queryArray);
                            } catch (JSONException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }else{
                        	COUNTER++;
                        }
                    	
                    	returnMessage = QueryDBData.getLatestData(0);
                    	// Send the obtained bytes to the UI Activity
                    	mHandler.obtainMessage(MainActivity.MESSAGE_READ, returnMessage).sendToTarget();
                    	
                    	//Reset totalMessage
                    	totalMessage = new String[totalMessage.length];  
                    }  
                    
                    // Send the obtained bytes to the UI Activity
//                    mHandler.obtainMessage(MainActivity.MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                    
                    
                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    connectionLost();
                    break;
                }
            }

        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
}
