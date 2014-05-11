package com.hightechsoftware.whatstheword.utility;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class AppPreferences {
	public static final String KEY_PREFS_SHOW_ADD = "key_prefs_show_add";
	public static final String KEY_PREFS_SHOW_ADS = "key_prefs_show_ads";
	public static final String KEY_PREFS_COINS = "key_prefs_coins";
	public static final String KEY_PREFS_COINS_DIFF = "key_prefs_coins_diff";
	public static final String KEY_PREFS_SOUND = "key_prefs_sound";
	public static final String KEY_PREFS_QUOTE = "key_prefs_quote";
	

	public static final String KEY_PREFS_LEVELS_COMPLETED_COUNT = "key_prefs_levels_completed_count";
	public static final String KEY_PREFS_SOLVED_CATEGORY_COUNT = "key_prefs_solved_category_count";
	public static final String KEY_PREFS_ON_ROLL_COUNT = "key_prefs_on_roll_count";
	public static final String KEY_PREFS_CORRECT_ANSWER_COUNT = "key_prefs_correct_answer_count";
	public static final String KEY_PREFS_WORD_COMPLETED_COUNT = "key_prefs_word_completed_count";
	public static final String KEY_PREFS_ACHIEVEMENTS_COUNT = "key_prefs_achievements_count";

	private static final String APP_SHARED_PREFS = "com.hightechsoftware.whatstheword";
	private SharedPreferences _sharedPrefs;
	private Editor _prefsEditor;

	public AppPreferences(Context context) {
		this._sharedPrefs = context.getSharedPreferences(APP_SHARED_PREFS,
				Context.MODE_PRIVATE);
		this._prefsEditor = _sharedPrefs.edit();
	}

	/*
	public int getCoinsDiff() {
		return _sharedPrefs.getInt(KEY_PREFS_COINS_DIFF, 0);
	}

	public void saveCoinsDiff(int coins) {
		_prefsEditor.putInt(KEY_PREFS_COINS_DIFF, coins);
		_prefsEditor.commit();
	}
	*/

	public int getCoins() {
		return _sharedPrefs.getInt(KEY_PREFS_COINS, 500);
	}

	public void saveCoins(int coins) {
		_prefsEditor.putInt(KEY_PREFS_COINS, coins);
		_prefsEditor.commit();
	}
	
	public int getShowAdd() {
		return _sharedPrefs.getInt(KEY_PREFS_SHOW_ADD, 1);
	}

	public void setShowAdd(int show) {
		_prefsEditor.putInt(KEY_PREFS_SHOW_ADD, show);
		_prefsEditor.commit();
	}
	
	public boolean getShowAds() {
		return _sharedPrefs.getBoolean(KEY_PREFS_SHOW_ADS, true);
	}

	public void setShowAds(boolean isShow) {
		_prefsEditor.putBoolean(KEY_PREFS_SHOW_ADS, isShow);
		_prefsEditor.commit();
	}
	
	public boolean getSound() {
		return _sharedPrefs.getBoolean(KEY_PREFS_SOUND, true);
	}

	public void soundOff() {
		_prefsEditor.putBoolean(KEY_PREFS_SOUND, false);
		_prefsEditor.commit();
	}

	public void soundOn() {
		_prefsEditor.putBoolean(KEY_PREFS_SOUND, true);
		_prefsEditor.commit();
	}

	public boolean getQuote() {
		return _sharedPrefs.getBoolean(KEY_PREFS_QUOTE, false);
	}

	public void quoteOff() {
		_prefsEditor.putBoolean(KEY_PREFS_QUOTE, false);
		_prefsEditor.commit();
	}

	public void quoteOn() {
		_prefsEditor.putBoolean(KEY_PREFS_QUOTE, true);
		_prefsEditor.commit();
	}
	/******************* ACHIEVEMENTS ***********************/
	public boolean getAchievement(String heyZapID) {
		return _sharedPrefs.getBoolean(heyZapID, false);
	}

	public void setAchievement(String heyZapID, boolean val) {
		_prefsEditor.putBoolean(heyZapID, val);
		_prefsEditor.commit();
	}

	/******************* ACHIEVEMENTS ***********************/
	// Counter - Dedication - finish 1000 levels (probably won�t be possible on
	// release)
	public int getLevelsCompletedCount() {
		return _sharedPrefs.getInt(KEY_PREFS_LEVELS_COMPLETED_COUNT, 0);
	}

	public void setLevelsCompletedCount(int val) {
		_prefsEditor.putInt(KEY_PREFS_LEVELS_COMPLETED_COUNT, val);
		_prefsEditor.commit();
	}

	// Counter - On a roll - get three correct answers in a row without any
	// mistakes
	public int getOnRollCount() {
		return _sharedPrefs.getInt(KEY_PREFS_ON_ROLL_COUNT, 0);
	}

	public void setOnRollCount(int val) {
		_prefsEditor.putInt(KEY_PREFS_ON_ROLL_COUNT, val);
		_prefsEditor.commit();
	}

	// Counter - On a roll - get three correct answers in a row without any
	// mistakes
	public int getCorrectAnswerCount() {
		return _sharedPrefs.getInt(KEY_PREFS_CORRECT_ANSWER_COUNT, 0);
	}

	public void setCorrectAnswerCount(int val) {
		_prefsEditor.putInt(KEY_PREFS_CORRECT_ANSWER_COUNT, val);
		_prefsEditor.commit();
	}

	// Counter - On a roll - get three correct answers in a row without any
	// mistakes
	public int getNumberAchievementsCount() {
		return _sharedPrefs.getInt(KEY_PREFS_ACHIEVEMENTS_COUNT, 0);
	}

	public void setNumberAchievementsCount(int val) {
		_prefsEditor.putInt(KEY_PREFS_ACHIEVEMENTS_COUNT, val);
		_prefsEditor.commit();
	}
	
	public int getWordCompleted() {
		return _sharedPrefs.getInt(KEY_PREFS_WORD_COMPLETED_COUNT, 1);
	}

	public void setWordCompleted(int val) {
		_prefsEditor.putInt(KEY_PREFS_WORD_COMPLETED_COUNT, val);
		_prefsEditor.commit();
	}
}