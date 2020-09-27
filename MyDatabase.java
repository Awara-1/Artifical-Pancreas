package com.imperial.biap;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MyDatabase extends SQLiteOpenHelper{
	//Debug
	private static final String TAG = "MyDatabase";
	
	//Command to be sent to SQL
	private static final String CREATE_DB = "CREATE TABLE data("
			+ "_id integer primary key autoincrement,"
			+ "patient_id integer,"
			+ "Date text,"
			+ "Time text,"
			+ "Glucose real,"
			+ "Insulin real,"
			+ "SR real,"
			+ "Insulin_Feed real,"
			+ "K real,"
			+ "Mean_Glucose real," 
			+ "dG real," 
			+ "Safety_Condition real," 
			+ "Basal_Insulin real);";
	
	private static final String DATABASE_NAME = "BiAP.db";
	private static final int DATABASE_VERSION = 1;
	
	public MyDatabase(Context context){
		super(context,DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		//Create the Database
		db.execSQL(CREATE_DB);
		Log.d(TAG, "DATABASE CREATED");
	}
	

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}
}
