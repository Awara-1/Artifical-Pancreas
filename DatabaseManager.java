package com.imperial.biap;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

//Simple class to use SQL databases in the code
public abstract class DatabaseManager {
	private static MyDatabase db;
	
	//A function to create a database
	public static void createDatabase(Context context){
		db = new MyDatabase(context);
	}
	
	//A function to create/open a readable database
	public static SQLiteDatabase getReadableDatabase(){
		return db.getReadableDatabase();
	}
	
	//A function to create/open a readable and writable database
	public static SQLiteDatabase getWritableDatabase(){
		return db.getWritableDatabase();
	}
}
