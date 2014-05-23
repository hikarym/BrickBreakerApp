package br.usp.ime.brickbreakerapp.sqlite;

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
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
		
		addUser("Master");
		/*
		addScore("Master", 0);//--------------------------------------------------------------------------------
		addScore("Master", 2);
		addScore("Master", 2);
		addScore("Master", 2);
		addScore("Master", 2);
		addScore("Master", 2);
		addScore("Master", 2);
		addScore("Master", 2);
		addScore("Master", 2);
		addScore("Master", 2);
		addScore("Master", 2);
		addScore("Master", 2);
		addScore("Master", 2);
		addScore("Master", 2);
		addScore("Master", 2);
		addScore("Master", 2);
		addScore("Master", 2);
		addScore("Master", 2);
		addScore("Master", 20);*/
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
	 * + update all score for a single user
	 * + get all score for a single user + get all score + get all score' users
	 * + get score with highest score
	 * + delete all score for a single user + delete all score
	 */

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
	
/*************************************************************************************************************/

	/**
	 * CRUD operations (create "add", read "get", update, delete) bbscore
	 * + update all bbscores for a single user
	 * + get all bbscores for a single user + get all bbscores + get all bbscores' users
	 * + get bbscore with highest score
	 * + delete all bbscores for a single user + delete all bbscores
	 */
	
	//---Add single BbScore.
	public void addBbScore(BbScore bbscore) {
		Log.d(TAG + ".addBbScore", bbscore.toString());
		
		ContentValues values = new ContentValues();
		values.put(KEY_USERNAME, bbscore.getUsername());
		values.put(KEY_SCORE, bbscore.getScore());
		
		mBbSqliteDB.beginTransaction();
		
		try {
			mBbSqliteDB.insert(TABLE_BBSCORES,
					null,
					values);
			
			mBbSqliteDB.setTransactionSuccessful();
		} finally {
			mBbSqliteDB.endTransaction();
		}
	}
	
	//---Get single BbScore
	public BbScore getBbScore(int id) {
		BbScore bbscore = null;
		
		Cursor cursor =
				mBbSqliteDB.query(TABLE_BBSCORES, // a. table
						BBSCORES_COLUMNS, // b. column names
						KEY_ID + " = ?", // c. selections
						new String[] { String.valueOf(id) }, // d. selections args
						null, // e. group by
						null, // f. having
						KEY_SCORE + " DESC", // g. order by
						null); // h. limit
		
		// If a result is found
		if (cursor.moveToFirst()) {
			bbscore = new BbScore();
			
			bbscore.setId(Integer.parseInt(cursor.getString(0)));
			bbscore.setUsername(cursor.getString(1));
			bbscore.setScore(Integer.parseInt(cursor.getString(2)));
			
			Log.d(TAG + ".getBbScore(" + id + ")", bbscore.toString());
		}
		
		else
			Log.d(TAG + ".getBbScore(" + id + ")", "BbScore not found!");
		
		cursor.close();
		
		return bbscore;
	}

	//---Get Highest BbScore
	public BbScore getHighBbScore() {
		BbScore bbscore = null;
		
		String query = "SELECT * FROM " + TABLE_BBSCORES + " WHERE " + KEY_SCORE + " = ("
							+ "SELECT MAX(" + KEY_SCORE + ") FROM " + TABLE_BBSCORES
							+ " WHERE " + KEY_SCORE + " > 0)";
		
		Cursor cursor = mBbSqliteDB.rawQuery(query, null);
		
		// If a result is found
		if (cursor.moveToFirst()) {
			bbscore = new BbScore();
			
			bbscore.setId(Integer.parseInt(cursor.getString(0)));
			bbscore.setUsername(cursor.getString(1));
			bbscore.setScore(Integer.parseInt(cursor.getString(2)));
	
			cursor.close();
			
			Log.d(TAG + ".getHighBbScore()", bbscore.toString());
		}

		else
			Log.d(TAG + ".getHighBbScore()", "BbHighScore not found!");
		
		return bbscore;
	}
	
	//---Get all BbScores for username == name
	public List<BbScore> getAllUserBbScores(String name) {
		List<BbScore> bbscores = null;
		
		Cursor cursor =
				mBbSqliteDB.query(TABLE_BBSCORES, // a. table
						BBSCORES_COLUMNS, // b. column names
						KEY_USERNAME + " = ? AND " + KEY_SCORE + " >= 0", // c. selections
						new String[] { name }, // d. selections args
						null, // e. group by
						null, // f. having
						KEY_SCORE + " DESC, " + KEY_USERNAME  + " ASC", // g. order by
						null); // h. limit
		
		// If a result is found
		if (cursor.moveToFirst()) {
			bbscores = new LinkedList<BbScore>();
			BbScore bbscore = null;
			
			// Add all to the list bbscores
			do {
				bbscore = new BbScore();
				bbscore.setId(Integer.parseInt(cursor.getString(0)));
				bbscore.setUsername(cursor.getString(1));
				bbscore.setScore(Integer.parseInt(cursor.getString(2)));
				
				// Add bbscore to bbscores
				bbscores.add(bbscore);
			} while (cursor.moveToNext());
			
			cursor.close();
			
			Log.d(TAG + ".getAllUserBbScores(" + name + ")", bbscores.toString());
		}
		
		else
			Log.d(TAG + ".getAllUserBbScores(" + name + ")", "User not found!");
		
		return bbscores;
	}
	
	//---Get all BbScores
	public List<BbScore> getAllBbScores() {
		List<BbScore> bbscores = null;
		
		Cursor cursor =
				mBbSqliteDB.query(TABLE_BBSCORES, // a. table
						BBSCORES_COLUMNS, // b. column names
						KEY_SCORE + " >= 0", // c. selections
						null, // d. selections args
						null, // e. group by
						null, // f. having
						KEY_SCORE + " DESC, " + KEY_USERNAME  + " ASC", // g. order by
						null); // h. limit
		
		// If a result is found
		if (cursor.moveToFirst()) {
			bbscores = new LinkedList<BbScore>();
			BbScore bbscore = null;
			
			// Add all to the list bbscores
			do {
				bbscore = new BbScore();
				bbscore.setId(Integer.parseInt(cursor.getString(0)));
				bbscore.setUsername(cursor.getString(1));
				bbscore.setScore(Integer.parseInt(cursor.getString(2)));
				
				// Add bbscore to bbscores
				bbscores.add(bbscore);
			} while (cursor.moveToNext());
			
			cursor.close();
			
			Log.d(TAG + ".getAllBbScores()", bbscores.toString());
		}
		
		else
			Log.d(TAG + ".getAllBbScores()", "There are no scores!");
		
		return bbscores;
	}
	
	//---Get all BbScores' Users
	public List<String> getAllBbScoresUsers() {
		List<String> users = new LinkedList<String>();
		
		String query = "SELECT DISTINCT " + KEY_USERNAME
				+ " FROM " + TABLE_BBSCORES
				+ " ORDER BY " + KEY_USERNAME;
		
		Cursor cursor = mBbSqliteDB.rawQuery(query, null);
		
		// If a result is found
		if (cursor.moveToFirst()) {
			users = new LinkedList<String>();
			
			// Add all to the list users
			do {
				// Add bbscore to users
				users.add(cursor.getString(1));
			} while (cursor.moveToNext());
			
			Log.d(TAG + ".getAllBbScoresUsers()", users.toString());
	
			cursor.close();
		}
		
		else
			Log.d(TAG + ".getAllBbScoresUsers()", "There are no users!");
		
		return users;
	}

	//---Updating single BbScore
	public int updateBbScore(BbScore bbscore) {
		Log.d(TAG + ".updateBbScore", bbscore.toString());
		
		int i = -1;
		
		ContentValues values = new ContentValues();
		values.put(KEY_USERNAME, bbscore.getUsername());
		values.put(KEY_SCORE, bbscore.getScore());
		
		mBbSqliteDB.beginTransaction();
		
		try {
			// Update row
			i = mBbSqliteDB.update(TABLE_BBSCORES,
					values,
					KEY_ID + " = ?",
					new String[] { String.valueOf(bbscore.getId()) });
			
			mBbSqliteDB.setTransactionSuccessful();
		} finally {
			mBbSqliteDB.endTransaction();
		}
		
		return i;
	}
	
	//---Updating all BbScores for username == oldUsername
	public int updateUserBbScores(String oldUsername, String newUsername) {
		Log.d(TAG + ".updateUserBbScores(" + oldUsername + "," + newUsername + ")", null);
		
		int i = -1;
		
		ContentValues values = new ContentValues();
		values.put(KEY_USERNAME, newUsername);
		
		mBbSqliteDB.beginTransaction();
		
		try {
			// Update row
			i = mBbSqliteDB.update(TABLE_BBSCORES,
					values,
					KEY_USERNAME + " = ?",
					new String[] { oldUsername });
			
			mBbSqliteDB.setTransactionSuccessful();
		} finally {
			mBbSqliteDB.endTransaction();
		}
		
		return i;
	}
	
	//---Deleting single BbScore
	public void deleteBbScore(BbScore bbscore) {
		Log.d(TAG + ".deleteBbScore", bbscore.toString());
		
		mBbSqliteDB.beginTransaction();
		
		try {
			mBbSqliteDB.delete(TABLE_BBSCORES,
					KEY_ID + " = ?",
					new String[] { String.valueOf(bbscore.getId()) });
			
			mBbSqliteDB.setTransactionSuccessful();
		} finally {
			mBbSqliteDB.endTransaction();
		}
	}
	
	//---Deleting all BbScores for username == name
	public void deleteAllUserBbScores(String name) {
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
	
	//---Deleting all BbScores
	public void deleteAllBbScores() {
		Log.d(TAG + ".deleteAllBbScores()", null);
		
		mBbSqliteDB.beginTransaction();
		
		try {
			mBbSqliteDB.delete(TABLE_BBSCORES,
					null,
					null);
			
			mBbSqliteDB.setTransactionSuccessful();
		} finally {
			mBbSqliteDB.endTransaction();
		}
	}
}
