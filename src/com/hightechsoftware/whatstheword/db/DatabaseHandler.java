package com.hightechsoftware.whatstheword.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHandler extends SQLiteOpenHelper {

	// Context for Assets
	private Context mContext;

	// Database Version
	public static final int DATABASE_VERSION =1;

	// Database Name
	public static final String DATABASE_NAME = "GuessThatWord.db";

		public DatabaseHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		mContext = context;
	}

	@Override
	public void close() {
		try {
			super.close();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	// Creating Tables
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DBHelper.CREATE_TABLE_WORDS);
		DBHelper.initialize(db,mContext);
	}

	// Upgrading database
	/**
	 * Database upgrade is actually Data Upgrade.
	 * Using the "upgradeDB.txt" data content can be added/removed/deleted.
	 * onUpgrade is called after the DB Version is increased.
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		/*// Upgrade the Categories/Levels/Words. Never drop tables here.
		// Drop older table if existed
		db.execSQL("DROP TABLE IF EXISTS " + WordInfoDB.DATABASE_TABLE + ";");
		db.execSQL("DROP TABLE IF EXISTS " + LevelInfoDB.DATABASE_TABLE + ";");
		db.execSQL("DROP TABLE IF EXISTS " + CategoryInfoDB.DATABASE_TABLE + ";");
		db.execSQL("DROP TABLE IF EXISTS " + AchievementInfoDB.DATABASE_TABLE + ";");
		// Create tables again
		onCreate(db);
		*/
		DBHelper.upgrade(db,mContext);
	}

	
}
