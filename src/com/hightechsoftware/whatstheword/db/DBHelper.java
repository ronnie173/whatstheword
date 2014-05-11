package com.hightechsoftware.whatstheword.db;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class DBHelper {
	
	private DatabaseHandler mDbHandler;
	private SQLiteDatabase mDb;

	private static Context mCtx;
	
	public static final String CREATE_TABLE_WORDS = "create table "
			+ WordInfoDB.DATABASE_TABLE + " (" + WordInfoDB.ROW_ID
			+ " integer primary key autoincrement, " + WordInfoDB.UNLOCKED
			+ " INTEGER," + WordInfoDB.SOLVED + " INTEGER," + WordInfoDB.SCORE
			+ " INTEGER," + WordInfoDB.WORD + " TEXT," + WordInfoDB.LETTERS
			+ " TEXT," + WordInfoDB.IMAGE1 + " TEXT," + WordInfoDB.IMAGE2
			+ " TEXT," + WordInfoDB.IMAGE3 + " TEXT," + WordInfoDB.IMAGE4
			+ " TEXT," + WordInfoDB.HTTP + " TEXT," 
			+ WordInfoDB.LINK + " TEXT);";

	/**
	 * Constructor - takes the context to allow the database to be
	 * opened/created
	 * 
	 * @param ctx
	 *            the Context within which to work
	 */
	public DBHelper(Context ctx) {
		DBHelper.mCtx = ctx;
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
	public DBHelper open() throws SQLException {
		this.mDbHandler = new DatabaseHandler(DBHelper.mCtx);
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
	 * Reset the database. Drop and recreated tables
	 * 
	 * @param rowId
	 * @return true if deleted, false otherwise
	 */
	public boolean resetDB() {

		// Drop older table if existed
		this.mDb.execSQL("DROP TABLE IF EXISTS " + WordInfoDB.DATABASE_TABLE + ";");
		
		//Create tables
		mDb.execSQL(DBHelper.CREATE_TABLE_WORDS);
		
		//Initialize values
		DBHelper.initialize(mDb,mCtx);
		
		return true;
	}
	
	
	public static void initialize(SQLiteDatabase db, Context ctx) {
		// Initialize DB
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					ctx.getAssets().open("InitializeDB.txt"), "UTF-8"));

			// do reading, usually loop until end of file reading
			String mLine = reader.readLine();
			while (mLine != null) {
				// process line
				db.execSQL(mLine);
				mLine = reader.readLine();
			}

			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void upgrade(SQLiteDatabase db, Context ctx) {
		// Initialize DB
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					ctx.getAssets().open("upgradeDB.txt"), "UTF-8"));

			// do reading, usually loop until end of file reading
			String mLine = reader.readLine();
			while (mLine != null) {
				// process line
				try {
					db.execSQL(mLine);	
				} catch (Exception e) {
					// already exists, so ignore
				}				
				mLine = reader.readLine();
			}

			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
