package br.usp.ime.brickbreakerapp.sqlite;

import java.io.IOException;
import java.sql.SQLException;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class BbSQliteHelper extends SQLiteOpenHelper  {
	// Database Path
	private static String DB_PATH = null;

	// Database Name
	private static final String DB_NAME = "BrickBreakerApp.db";
	
	// Reference of database
	private SQLiteDatabase mBbSqliteDB;
	
	private final Context mContext;

	// Table name
	private static final String TABLE_BBSCORES = "bbscores";
	
	// BbScores table columns names
	private static final String KEY_ID = "_id";
	private static final String KEY_USERNAME = "username";
	private static final String KEY_SCORE = "score";
	
	private static final String[] BBSCORES_COLUMNS = {KEY_ID, KEY_USERNAME, KEY_SCORE};

	private static final int MAX_ID = 2147483647; // Maximum id allowed on the tables
	
	private static final String TAG = "BbSQliteHelper";
	
	public BbSQliteHelper(Context context) {
		super(context, DB_NAME, null, 1);
		
		this.mContext = context;

		//mContext.deleteDatabase(DB_NAME);//--------------------------------------------------------------------
		
		if (!checkDatabaseExists()) {
			try {
				createDatabase();
	    	} catch (IOException ioe) {
	    		Log.e(TAG, "Unable to create database");
	    		Log.e(TAG, "IOException: Error code = " + ioe.getMessage());
			}
		}
		
		else {
			DB_PATH = mContext.getDatabasePath(DB_NAME).getPath();
			
			try {
				openDatabase();
	    	}catch(SQLException sqle){
	    		Log.e(TAG, "Unable to open database");
	    		Log.e(TAG, "SQLException: Error code = " + sqle.getErrorCode());
			}
		}
	}
	
	//---Open a new private SQLiteDatabase associated with this Context's application package.
	//---Create the database file if it doesn't exist.
	public void createDatabase() throws IOException {
		mBbSqliteDB = mContext.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
		DB_PATH = mContext.getDatabasePath(DB_NAME).getPath();
		
		this.createDatabaseTables();
	}
	
	//---Check if the database already exist to avoid re-copying the file each time the app is opened
	private boolean checkDatabaseExists() {
		return mContext.getDatabasePath(DB_NAME).exists();
	}
	
	//---createDatabase() must be called before executing this method
	public void openDatabase() throws SQLException {
		//Open the database
		mBbSqliteDB = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
	}
	
	public void createDatabaseTables() {
		// SQL statement to create bbscores table
		String CREATE_BBSCORES_TABLE = "CREATE TABLE " + TABLE_BBSCORES + " ("
				+ KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ KEY_USERNAME + " TEXT, "
				+ KEY_SCORE + " INTEGER)";// CHECK score >= 0)";
		
		// Create bbscores table
		mBbSqliteDB.execSQL(CREATE_BBSCORES_TABLE);
		
		// Here we should add the default user, but since we aren't managing the user's names at the moment,
		// there's no need to.
		
		//addUser(OptionFragment.DEFAULT_USERNAME);
	}
	
	//---Return the BbScore table biggest id, and if it doesn't exist return -1;
	public int getDatabaseTableTopId() {
		int topID = -1;
		
		String query = "SELECT MAX(" + KEY_ID + ") FROM " + TABLE_BBSCORES;
		
		Cursor cursor = mBbSqliteDB.rawQuery(query, null);
		 
		// If no result is found, the table is empty
		if (cursor.moveToFirst() == false || cursor.getString(0) == null)
			return topID;
		
		topID = Integer.parseInt(cursor.getString(0));

		cursor.close();
		
		Log.d(TAG + ".getDatabaseTableTopId()", "topID = " + Integer.toString(topID));
		
		return topID;
	}
	
	public boolean checkDatabaseTableFull() {
		if (getDatabaseTableTopId() == MAX_ID) {
			Log.d(TAG + ".checkDatabaseTableFull()", "true");
			return true;
		}
		
		Log.d(TAG + ".checkDatabaseTableFull()", "false");
		
		return false;
	}
	
	public void dropDatabaseTables() {
		Log.w(TAG + ".dropDatabaseTables()", "Will destroy all old data!");
		
		mBbSqliteDB.beginTransaction();
		
		try {
			// Drop older bbscores table if existed
			mBbSqliteDB.execSQL("DROP TABLE IF EXISTS bbscores");
			
			mBbSqliteDB.setTransactionSuccessful();
		} finally {
			mBbSqliteDB.endTransaction();
		}
	}

	@Override
	public synchronized void close() {
		if (mBbSqliteDB != null)
			mBbSqliteDB.close();
		
		super.close();
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		//createDBTables();
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		/*
		if (oldVersion < newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			
			// Drop older bbscores table if existed
			db.execSQL("DROP TABLE IF EXISTS bbscores");
			
			// create new bbscores table
			this.onCreate(db);
		}
		*/
	}
	
/*************************************************************************************************************/

	/**
	 * CRUD operations (create "add", read "get", update, delete) score
	 * + verify if user is in the table + add user without scores
	 * + update all scores for a single user
	 * + get all scores for a single user + get all scores + get all users
	 * + get score with highest score
	 * + delete all score for a single user + delete all score
	 */

	//---Return true if the name is already in the table
	public boolean userExits(String name) {
		Cursor cursor =
				mBbSqliteDB.query(TABLE_BBSCORES, // a. table
						BBSCORES_COLUMNS, // b. column names
						KEY_USERNAME + " = ?", // c. selections
						new String[] { name }, // d. selections args
						null, // e. group by
						null, // f. having
						KEY_SCORE + " DESC, " + KEY_USERNAME  + " ASC", // g. order by
						null); // h. limit
		
		Log.d(TAG + ".getAllUserScoresInfo(" + name + ")", "Done");
		
		if (cursor.moveToNext()) {

			cursor.close();
			
			return true;			
		}
		
		cursor.close();
		
		return false;
	}
	
	//---Add single score to the table.
	public void addUser(String username) {
		String methodTAG = ".addUser (" + username + ")";
		
		// See if user is in the table
		if (userExits(username)) {
			Log.d(TAG + methodTAG, "The user is already in the table.");
			return;
		}
		
		ContentValues values = new ContentValues();
		values.put(KEY_USERNAME, username);
		values.put(KEY_SCORE, -1);
		
		mBbSqliteDB.beginTransaction();
		
		try {
			mBbSqliteDB.insert(TABLE_BBSCORES,
					null,
					values);
			
			mBbSqliteDB.setTransactionSuccessful();
		} finally {
			mBbSqliteDB.endTransaction();
		}
		
		Log.d(TAG + methodTAG, "Done");
	}

	//---Add single score to the table.
	public void addScore(String username, int score) {
		ContentValues values = new ContentValues();
		values.put(KEY_USERNAME, username);
		values.put(KEY_SCORE, score);
		
		String methodTAG = ".addScore (" + username + ", score: " + Integer.toString(score) + ")";
		
		mBbSqliteDB.beginTransaction();
		
		try {
			mBbSqliteDB.insert(TABLE_BBSCORES,
					null,
					values);
			
			mBbSqliteDB.setTransactionSuccessful();
		} finally {
			mBbSqliteDB.endTransaction();
		}
		
		Log.d(TAG + methodTAG, "Done");
	}

	//---Return the cursor of the information of a single score with KEY_ID = id.
	public Cursor getScore(int id) {
		Cursor cursor =
				mBbSqliteDB.query(TABLE_BBSCORES, // a. table
						BBSCORES_COLUMNS, // b. column names
						KEY_ID + " = ?", // c. selections
						new String[] { String.valueOf(id) }, // d. selections args
						null, // e. group by
						null, // f. having
						KEY_SCORE + " DESC", // g. order by
						null); // h. limit
		
		Log.d(TAG + ".getBbScore(" + id + ")", "Done");
		
		return cursor;
	}

	//---Returns the highest score on the table. Returns -1 if table is empty.
	public int getHighScore() {
		int highScore;
		
		String query = "SELECT MAX(" + KEY_SCORE + ") FROM " + TABLE_BBSCORES
				+ " WHERE " + KEY_SCORE + " > 0";
		
		Cursor cursor = mBbSqliteDB.rawQuery(query, null);
		
		// If no result is found
		if (cursor.moveToFirst() == false || cursor.getString(0) == null)
			return -1;
		
		highScore = Integer.parseInt(cursor.getString(0));

		cursor.close();
		
		Log.d(TAG + ".getHighScore()", "High score = " + highScore);
		
		return highScore;
	}
	
	//---Return the cursor of the information of all scores for username == name ordered by scores
	public Cursor getAllUserScoresInfo(String name) {
		Cursor cursor =
				mBbSqliteDB.query(TABLE_BBSCORES, // a. table
						BBSCORES_COLUMNS, // b. column names
						KEY_USERNAME + " = ? AND " + KEY_SCORE + " >= 0", // c. selections
						new String[] { name }, // d. selections args
						null, // e. group by
						null, // f. having
						KEY_SCORE + " DESC, " + KEY_USERNAME  + " ASC", // g. order by
						null); // h. limit
		
		Log.d(TAG + ".getAllUserScoresInfo(" + name + ")", "Done");
		
		return cursor;
	}
	
	//---Return the cursor of the information of all scores ordered by scores
	public Cursor getAllScoresInfo() {
		Cursor cursor =
				mBbSqliteDB.query(TABLE_BBSCORES, // a. table
						BBSCORES_COLUMNS, // b. column names
						KEY_SCORE + " >= 0", // c. selections
						null, // d. selections args
						null, // e. group by
						null, // f. having
						KEY_SCORE + " DESC, " + KEY_USERNAME  + " ASC", // g. order by
						null); // h. limit
		
		Log.d(TAG + ".getAllScoresInfo()", "Done");
		
		return cursor;
	}
	
	//---Return the cursor of all users ordered by name
	public Cursor getAllUsers() {
		String query = "SELECT DISTINCT " + KEY_USERNAME
				+ " FROM " + TABLE_BBSCORES
				+ " ORDER BY " + KEY_USERNAME;
		
		Cursor cursor = mBbSqliteDB.rawQuery(query, null);
		
		Log.d(TAG + ".getAllUsers()", "Done");
		
		return cursor;
	}

	//---Updating single score
	public void updateScore(int id, String username, int score) {
		ContentValues values = new ContentValues();
		values.put(KEY_USERNAME, username);
		values.put(KEY_SCORE, score);
		
		mBbSqliteDB.beginTransaction();
		
		try {
			// Update row
			mBbSqliteDB.update(TABLE_BBSCORES,
				values,
				KEY_ID + " = " + id,
				null);
			
			mBbSqliteDB.setTransactionSuccessful();
		} finally {
			mBbSqliteDB.endTransaction();
		}
		
		Log.d(TAG + ".updateBbScore(" + id + ", " + username + ", " + score + ")", "Done");
	}
	
	//---Updating all scores for username == oldUsername
	public void updateUserScores(String oldUsername, String newUsername) {
		ContentValues values = new ContentValues();
		values.put(KEY_USERNAME, newUsername);
		
		mBbSqliteDB.beginTransaction();
		
		try {
			// Update row
			mBbSqliteDB.update(TABLE_BBSCORES,
				values,
				KEY_USERNAME + " = ?",
				new String[] { oldUsername });
			
			mBbSqliteDB.setTransactionSuccessful();
		} finally {
			mBbSqliteDB.endTransaction();
		}
		
		Log.d(TAG + ".updateUserBbScores(" + oldUsername + "," + newUsername + ")", "Done");
	}
	
	//---Deleting single score
	public void deleteScore(int id) {
		mBbSqliteDB.beginTransaction();
		
		try {
			mBbSqliteDB.delete(TABLE_BBSCORES,
					KEY_ID + " = " + id,
					null);
			
			mBbSqliteDB.setTransactionSuccessful();
		} finally {
			mBbSqliteDB.endTransaction();
		}
		
		Log.d(TAG + ".deleteBbScore (" + id + ")", "Done");
	}
	
	//---Deleting all scores for username == name
	public void deleteAllUserScores(String name) {
		Log.d(TAG + ".deleteAllUserBbScores(" + name + ")", null);
		
		mBbSqliteDB.beginTransaction();
		
		try {
			mBbSqliteDB.delete(TABLE_BBSCORES,
					KEY_USERNAME + " = ?",
					new String[] { name });
			
			mBbSqliteDB.setTransactionSuccessful();
		} finally {
			mBbSqliteDB.endTransaction();
		}
	}
	
	//---Deleting all scores
	public void deleteAllScores() {
		mBbSqliteDB.beginTransaction();
		
		try {
			mBbSqliteDB.delete(TABLE_BBSCORES,
					null,
					null);
			
			mBbSqliteDB.setTransactionSuccessful();
		} finally {
			mBbSqliteDB.endTransaction();
		}

		Log.d(TAG + ".deleteAllBbScores()", "Done");
	}
}
