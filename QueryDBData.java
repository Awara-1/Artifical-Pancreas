package com.imperial.biap;

public class QueryDBData extends QueryDB{
	private static final String SQL_TABLE = "data";
	
	//Get all available data from Data table
	public static String[][] getData(){
		return queryData(SQL_TABLE, null, null, null, null, null, null);
	}
	
	//sort data in descending order of _id (and hence time) and return top result
	//returns the latest piece of data in the database
	public static String[] getLatestData(int i){
		String[] result;
		
		result = queryData(SQL_TABLE, null, null, null, null, null, "_id DESC")[i];
		
		return result;
	}
	
	//A function to insert a new row of information into the database
	public static void insertData(String[] newValues){
		String[] sqlColumns = new String[] {"patient_id", "Date", "Time", "Glucose", "Insulin", "SR", "Insulin_Feed", "K", "Mean_Glucose", 
				"dG", "Safety_Condition", "Basal_Insulin"};
		
		addData(SQL_TABLE, sqlColumns, newValues);
	}
}