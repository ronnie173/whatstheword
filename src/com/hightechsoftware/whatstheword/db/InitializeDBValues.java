package com.hightechsoftware.whatstheword.db;

import android.content.Context;

public class InitializeDBValues {

	/**
	 * Create all words in the DB from XML
	 * 
	 * @param ctx
	 */
	public static void CreateWords(Context ctx)
	{
		WordInfoDB db = new WordInfoDB(ctx);
		db.open();
		
		db.close();
	}
}
