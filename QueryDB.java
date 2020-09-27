package com.imperial.biap;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class QueryDB {
	
	private static SQLiteDatabase db;
	
	//A function to query the data in the database based on various SQL clauses
	public static String[][] queryData(String sqlTable, String[] sqlSelect, String sqlWhere, String[] sqlWhereArgs, String sqlGroupBy, String sqlHaving, String sqlSortOrder){
		db = DatabaseManager.getReadableDatabase();
		
		Cursor cursor = db.query(sqlTable, sqlSelect, sqlWhere, sqlWhereArgs, sqlGroupBy, sqlHaving, sqlSortOrder);
				
		cursor.moveToFirst();
		
		int rowCount = cursor.getCount();
		int columnCount = cursor.getColumnCount();
		
		String[][] queryArray = new String[rowCount][columnCount];
		
        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < columnCount; j++) {
                queryArray[i][j] = cursor.getString(j); //Enter the current value of the Cursor object into the 2D array.
            }
            cursor.moveToNext(); //Move the Cursor object to the next record in the query results because of row-major order.
        }
        
        cursor.close();
        db.close();
        
        return queryArray;
	}
	
	//A function to add data to the database 
    public static void addData(String sqlTable, String[] sqlColumns, String[] newValues){
        db = DatabaseManager.getWritableDatabase(); // Get a writable SQLiteDatabase object since we are writing.
        ContentValues contentValues = new ContentValues();

        //Put the values with its respective column
        for (int i = 0; i < sqlColumns.length; i++){
            contentValues.put(sqlColumns[i], newValues[i]);
        }

        db.insert(sqlTable, null, contentValues);
        db.close();
    }
    
    //A function which returns the number of rows in the database
    public static int queryDataCount(String sqlTable, String[] sqlSelect, String sqlWhere, String[] sqlWhereArgs) {
        db = DatabaseManager.getReadableDatabase(); 
        Cursor cursor = db.query(sqlTable, sqlSelect, sqlWhere, sqlWhereArgs, null, null, null);
        cursor.moveToFirst(); 
        int queryResult = cursor.getInt(0); 
        cursor.close(); 
        db.close();
        return queryResult;
    }
}
