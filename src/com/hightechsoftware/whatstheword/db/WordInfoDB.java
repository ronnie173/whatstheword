package com.hightechsoftware.whatstheword.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class WordInfoDB {
	//Database Table
	public static final String DATABASE_TABLE = "WORDS";
	//Columns
	public static final String ROW_ID = "_id";
	public static final String UNLOCKED = "unlocked";
	public static final String SOLVED = "solved";
	public static final String SCORE = "score";
	public static final String WORD = "word";
	public static final String LETTERS = "letters";
	public static final String IMAGE1 = "image1";
	public static final String IMAGE2 = "image2";
	public static final String IMAGE3 = "image3";
	public static final String IMAGE4 = "image4";
	public static final String HTTP = "http";
	public static final String LINK = "link";

	private DatabaseHandler mDbHandler;
	private SQLiteDatabase mDb;

	private final Context mCtx;
	
	/**
	 * Constructor - takes the context to allow the database to be
	 * opened/created
	 * 
	 * @param ctx
	 *            the Context within which to work
	 */
	public WordInfoDB(Context ctx) {
		this.mCtx = ctx;
	}
	
	/**
	 * Open the Apps database. If it cannot be opened, try to create a new
	 * instance of the database. If it cannot be created, throw an exception to
	 * signal the failure
	 * 
	 * @return this (self reference, allowing this to be chained in an
	 *         initialization call)
	 * @throws SQLException
	 *             if the database could be neither opened or created
	 */
	public WordInfoDB open() throws SQLException {
		this.mDbHandler = new DatabaseHandler(this.mCtx);
		this.mDb = this.mDbHandler.getWritableDatabase();
		return this;
	}
	
	/**
	 * close return type: void
	 */
	public void close() {
		this.mDbHandler.close();
	}

		
	/**
	 * Insert a new record
	 * 
	 * @return rowId or -1 if failed
	 */
	public long insert(int catID, String catName, int levelID, int unlocked, int solved,
			int score, String word, String letters, String image1, String image2,
			String image3, String image4, String http, String link) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(UNLOCKED, unlocked);
		initialValues.put(SOLVED, solved);
		initialValues.put(SCORE, score);
		initialValues.put(WORD, word);
		initialValues.put(LETTERS, letters);
		initialValues.put(IMAGE1, image1);
		initialValues.put(IMAGE2, image2);
		initialValues.put(IMAGE3, image3);
		initialValues.put(IMAGE4, image4);
		initialValues.put(HTTP, http);
		initialValues.put(LINK, link);
		return this.mDb.insert(DATABASE_TABLE, null, initialValues);
	}

	/**
	 * Update unlocked, score, solved
	 * 
	 * @param rowId
	 * @param unlocked
	 * @param score
	 * @param solved
	 * @return
	 */
	public boolean update(long rowId, int unlocked, int solved, int score) {
		ContentValues args = new ContentValues();
		args.put(UNLOCKED, unlocked);
		args.put(SOLVED, solved);
		args.put(SCORE, score);
		return this.mDb
				.update(DATABASE_TABLE, args, ROW_ID + "=" + rowId, null) > 0;
	}

	/**
	 * Update unlocked value
	 * 
	 * @param rowId
	 * @param score
	 * @return
	 */
	public boolean updateUnlocked(long rowId, int unlocked) {
		ContentValues args = new ContentValues();
		args.put(UNLOCKED, unlocked);
		return this.mDb
				.update(DATABASE_TABLE, args, ROW_ID + "=" + rowId, null) > 0;
	}
	/**
	 * Update Solved value
	 * 
	 * @param rowId
	 * @param score
	 * @return
	 */
	public boolean updateSolved(long rowId, int solved) {
		ContentValues args = new ContentValues();
		args.put(SOLVED, solved);
		return this.mDb
				.update(DATABASE_TABLE, args, ROW_ID + "=" + rowId, null) > 0;
	}
	
//	public boolean resetData() {
//		ContentValues args = new ContentValues();
//		args.put(SOLVED, 0);
//		return this.mDb
//				.update(DATABASE_TABLE, args, "1=1", null) > 0;
//	}
	
	
	/**
	 * Return a Cursor with all Records in the table
	 * 
	 * @return Cursor over all Apps
	 */
	public Cursor getRecords() {
		return this.mDb.query(DATABASE_TABLE, new String[] { ROW_ID, UNLOCKED, SOLVED,SCORE, WORD, LETTERS,IMAGE1,IMAGE2,
				IMAGE3,IMAGE4,HTTP,LINK}, null, null, null,
				null, null);
	}
	
	/**
	 * 
	 * @param level
	 * @param curWord
	 * @return
	 */
	public int getNextID(int curWord) {
		int ret = -1;
		String sql = "SELECT "+ROW_ID+" FROM " + DATABASE_TABLE 
				+ " WHERE " +UNLOCKED+ "=1 AND "+SOLVED+"=0 " + " ORDER BY "+ ROW_ID + " ASC;";
				//+ " AND "+ROW_ID+">" + curWord +" ORDER BY "+ ROW_ID + " ASC;";
		Cursor cur = this.mDb.rawQuery(sql, null);
		
		if (cur != null) {
			//cur.moveToFirst();
			if (cur.moveToFirst()) {
				ret = cur.getInt(0);
			}
			cur.close();
		}
		return ret;
	}

	/**
	 * Return a cursor with the list of specified records
	 * 
	 * @return Cursor with the specified record if exists
	 * @throws SQLException
	 *             if App could not be found/retrieved
	 */
	public Cursor getRecords(long catID, long levelID) throws SQLException {

		return this.mDb.query(DATABASE_TABLE, new String[] {ROW_ID, UNLOCKED, SOLVED,SCORE, WORD, LETTERS,IMAGE1,IMAGE2,
				IMAGE3,IMAGE4,HTTP,LINK},null,
				null, null, null, null, null);
	}
	/**
	 * Return a Cursor positioned at the App that matches the given rowId
	 * 
	 * @param rowId
	 * @return Cursor positioned to matching App, if found
	 * @throws SQLException
	 *             if App could not be found/retrieved
	 */
	public Cursor getRecord(long rowId) throws SQLException {

		return this.mDb.query(DATABASE_TABLE, new String[] {ROW_ID, UNLOCKED, SOLVED,SCORE, WORD, LETTERS,IMAGE1,IMAGE2,
				IMAGE3,IMAGE4,HTTP,LINK}, ROW_ID + "=" + rowId,
				null, null, null, null, null);
	}

	/**
	 * Return the count of all records
	 * 
	 * @return
	 */
	public int getCount() {
		int count = 0;
		Cursor mCursor = this.mDb.query(true, DATABASE_TABLE,
				new String[] { ROW_ID }, null, null, null, null, null, null);
		if (mCursor != null) {
			count = mCursor.getCount();
			mCursor.close();
		}
		return count;
	}	
}
