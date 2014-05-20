package br.usp.ime.brickbreakerapp.sqlite;

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

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

	private static int TOP_ID = 1; // id of the next score to be added
	
	private static final String TAG = "BbSQliteHelper";
	
	public BbSQliteHelper(Context context) {
		super(context, DB_NAME, null, 1);
		
		this.mContext = context;

		//mContext.deleteDatabase(DB_NAME);//--------------------------------------------------------------------
		
		if (!checkDBExists()) {
			try {
				createDB();
	    	} catch (IOException ioe) {
	    		Log.e(TAG, "Unable to create database");
	    		Log.e(TAG, "IOException: Error code = " + ioe.getMessage());
			}
		}
		
		else {
			DB_PATH = mContext.getDatabasePath(DB_NAME).getPath();
			
			try {
				openDB();
	    	}catch(SQLException sqle){
	    		Log.e(TAG, "Unable to open database");
	    		Log.e(TAG, "SQLException: Error code = " + sqle.getErrorCode());
			}
		}
	}
	
	//---Open a new private SQLiteDatabase associated with this Context's application package.
	//---Create the database file if it doesn't exist.
	public void createDB() throws IOException {
		mBbSqliteDB = mContext.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
		DB_PATH = mContext.getDatabasePath(DB_NAME).getPath();
		
		this.createDBTable();
	}
	
	//---Check if the database already exist to avoid re-copying the file each time the app is opened
	private boolean checkDBExists() {
		return mContext.getDatabasePath(DB_NAME).exists();
	}
	
	//---createDB() must be called before executing this method
	public void openDB() throws SQLException {
		//Open the database
		mBbSqliteDB = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
	}
	
	public void createDBTable() {
		// SQL statement to create bbscores table
		String CREATE_BBSCORES_TABLE = "CREATE TABLE " + TABLE_BBSCORES + " ("
				+ KEY_ID + " INTEGER PRIMARY KEY, "
				+ KEY_USERNAME + " TEXT, "
				+ KEY_SCORE + " INTEGER)";// CHECK score >= 0)";
		
		// Create bbscores table
		mBbSqliteDB.execSQL(CREATE_BBSCORES_TABLE);
		
		addScore("MASTER", -1);
		/*
		addScore("MASTER", 0);//--------------------------------------------------------------------------------
		addScore("MASTER", 2);
		addScore("MASTER", 2);
		addScore("MASTER", 2);
		addScore("MASTER", 2);
		addScore("MASTER", 2);
		addScore("MASTER", 2);
		addScore("MASTER", 2);
		addScore("MASTER", 2);
		addScore("MASTER", 2);
		addScore("MASTER", 2);
		addScore("MASTER", 2);
		addScore("MASTER", 2);
		addScore("MASTER", 2);
		addScore("MASTER", 2);
		addScore("MASTER", 2);
		addScore("MASTER", 2);
		addScore("MASTER", 2);
		addScore("MASTER", 20);*/
	}
	
	public boolean checkDBTableFull() {
		return TOP_ID == 2147483647 ? true : false;
	}
	
	public void dropDBTable() {
		Log.w(TAG + ".dropDBTable()", "Will destroy all old data!");
		
		mBbSqliteDB.beginTransaction();
		
		try {
			// Drop older bbscores table if existed
			mBbSqliteDB.execSQL("DROP TABLE IF EXISTS bbscores");
			
			mBbSqliteDB.setTransactionSuccessful();
		} finally {
			mBbSqliteDB.endTransaction();
		}
		
		// TOP_ID back to 1
		TOP_ID = 1;
	}

	@Override
	public synchronized void close() {
		if (mBbSqliteDB != null)
			mBbSqliteDB.close();
		
		super.close();
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		//createDBTable();
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
		values.put(KEY_ID, TOP_ID);
		values.put(KEY_USERNAME, username);
		values.put(KEY_SCORE, score);
		
		mBbSqliteDB.beginTransaction();
		
		try {
			mBbSqliteDB.insert(TABLE_BBSCORES,
					null,
					values);
			
			mBbSqliteDB.setTransactionSuccessful();
		} finally {
			mBbSqliteDB.endTransaction();
			TOP_ID++;
		}
		
		Log.d(TAG + ".addScore (" + username + ", score: " + score + ")", "Done");
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
		int highscore;
		
		String query = "SELECT MAX(" + KEY_SCORE + ") FROM " + TABLE_BBSCORES
				+ " WHERE " + KEY_SCORE + " > 0";
		
		Cursor cursor = mBbSqliteDB.rawQuery(query, null);
		
		// If no result is found
		if (cursor.moveToFirst() == false || cursor.getString(0) == null)
			return -1;
		
		highscore = Integer.parseInt(cursor.getString(0));

		cursor.close();
		
		Log.d(TAG + ".getHighScore()", "High score = " + highscore);
		
		return highscore;
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
		
		Log.d(TAG + ".getAllUserBbScores(" + name + ")", "Done");
		
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
		
		Log.d(TAG + ".getAllScores()", "Done");
		
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
		values.put(KEY_ID, TOP_ID);
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
			TOP_ID++;
		}
	}
	
	//---Get single BbScore
	public BbScore getBbScore(int id) {
		BbScore bbscore = new BbScore();
		
		Cursor cursor =
				mBbSqliteDB.query(TABLE_BBSCORES, // a. table
						BBSCORES_COLUMNS, // b. column names
						KEY_ID + " = ?", // c. selections
						new String[] { String.valueOf(id) }, // d. selections args
						null, // e. group by
						null, // f. having
						KEY_SCORE + " DESC", // g. order by
						null); // h. limit
		
		// If no result is found
		if (cursor.moveToFirst() == false)
			return null;
		
		// Get first result
		cursor.moveToFirst();
		
		bbscore.setId(Integer.parseInt(cursor.getString(0)));
		bbscore.setUsername(cursor.getString(1));
		bbscore.setScore(Integer.parseInt(cursor.getString(2)));

		cursor.close();
		
		Log.d(TAG + ".getBbScore(" + id + ")", bbscore.toString());
		
		return bbscore;
	}

	//---Get Highest BbScore
	public BbScore getHighBbScore() {
		BbScore bbscore = new BbScore();
		
		String query = "SELECT * FROM " + TABLE_BBSCORES + " WHERE " + KEY_SCORE + " = ("
							+ "SELECT MAX(" + KEY_SCORE + ") FROM " + TABLE_BBSCORES
							+ " WHERE " + KEY_SCORE + " > 0)";
		
		Cursor cursor = mBbSqliteDB.rawQuery(query, null);
		
		// If no result is found
		if (cursor.moveToFirst() == false)
			return bbscore;

		bbscore.setId(Integer.parseInt(cursor.getString(0)));
		bbscore.setUsername(cursor.getString(1));
		bbscore.setScore(Integer.parseInt(cursor.getString(2)));

		cursor.close();
		
		Log.d(TAG + ".getHighBbScore()", bbscore.toString());
		
		return bbscore;
	}
	
	//---Get all BbScores for username == name
	public List<BbScore> getAllUserBbScores(String name) {
		List<BbScore> bbscores = new LinkedList<BbScore>();
		
		Cursor cursor =
				mBbSqliteDB.query(TABLE_BBSCORES, // a. table
						BBSCORES_COLUMNS, // b. column names
						KEY_USERNAME + " = ? AND " + KEY_SCORE + " >= 0", // c. selections
						new String[] { name }, // d. selections args
						null, // e. group by
						null, // f. having
						KEY_SCORE + " DESC, " + KEY_USERNAME  + " ASC", // g. order by
						null); // h. limit
		
		// If no result is found
		if (cursor.moveToFirst() == false)
			return null;
		
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
		
		return bbscores;
	}
	
	//---Get all BbScores
	public List<BbScore> getAllBbScores() {
		List<BbScore> bbscores = new LinkedList<BbScore>();
		
		Cursor cursor =
				mBbSqliteDB.query(TABLE_BBSCORES, // a. table
						BBSCORES_COLUMNS, // b. column names
						KEY_SCORE + " >= 0", // c. selections
						null, // d. selections args
						null, // e. group by
						null, // f. having
						KEY_SCORE + " DESC, " + KEY_USERNAME  + " ASC", // g. order by
						null); // h. limit
		
		// If no result is found
		if (cursor.moveToFirst() == false)
			return null;
		
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
		
		return bbscores;
	}
	
	//---Get all BbScores' Users
	public List<String> getAllBbScoresUsers() {
		List<String> users = new LinkedList<String>();
		
		String query = "SELECT DISTINCT " + KEY_USERNAME
				+ " FROM " + TABLE_BBSCORES
				+ " ORDER BY " + KEY_USERNAME;
		
		Cursor cursor = mBbSqliteDB.rawQuery(query, null);

		// If no result is found
		if (cursor.moveToFirst() == false)
			return null;
		
		// Add all to the list users
		do {
			// Add bbscore to users
			users.add(cursor.getString(1));
		} while (cursor.moveToNext());
		
		Log.d(TAG + ".getAllBbScoresUsers()", users.toString());

		cursor.close();
		
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
