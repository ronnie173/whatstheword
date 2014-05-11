package com.hightechsoftware.whatstheword;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;
import com.google.ads.Ad;
import com.google.ads.AdListener;
import com.google.ads.AdRequest;
import com.google.ads.InterstitialAd;
import com.swarmconnect.Swarm;
import com.swarmconnect.SwarmLeaderboard;
import com.hightechsoftware.whatstheword.car.R;
import com.hightechsoftware.whatstheword.common.LetterInfo;
import com.hightechsoftware.whatstheword.common.WordInfo;
import com.hightechsoftware.whatstheword.db.WordInfoDB;
import com.hightechsoftware.whatstheword.inappbilling.util.IabHelper;
import com.hightechsoftware.whatstheword.inappbilling.util.IabHelper.QueryInventoryFinishedListener;
import com.hightechsoftware.whatstheword.inappbilling.util.IabResult;
import com.hightechsoftware.whatstheword.inappbilling.util.Inventory;
import com.hightechsoftware.whatstheword.inappbilling.util.Purchase;
import com.hightechsoftware.whatstheword.utility.AppPreferences;
import com.hightechsoftware.whatstheword.utility.MyCountDownTimer;
import com.hightechsoftware.whatstheword.utility.Utility;

public class PlayActivity extends Activity implements AdListener {

	private static int HINT_POSITION = 6; // Button position. Actual 7 at the
											// end
	private static int BUY_COINS_POSITION = 13; // Button position. Actual 14 at
												// the end
	private LetterInfo DUMMY_LETTER;
	private AppPreferences _appPrefs;

	// Screen resources
	private ImageView ivBack;
	private ImageView ivLogo;
	private TextView tvTitle;
	private TextView tvCoins;
	private ImageView ivLeaderBoard;
	private GridView gvLetters;
	private GridView gvTargetWord;
	private ImageView ivWordImage1;
	private ImageView ivWordImage2;
	private ImageView ivWordImage3;
	private ImageView ivWordImage4;
	private ImageView ivWordImage;
	private ImageView ivSuccess;
	private LinearLayout llSuccessImage;
	private RelativeLayout rlSuccessSummary;
	private LinearLayout llLetters;
	private RelativeLayout rlTargetWord;
	private LinearLayout llTargetImg;
	private TextView tvNext;
	private TextView tvBack;
	private TextView tvSummaryScore;
	private TextView tvSummaryCoins;
	private TextView tvLevelNums;
	private LinearLayout llSummaryScore;
	private LinearLayout llSummaryCoins;
	private TextView tvSuccessMessage;
	private TextView tvSuccessSubMessage;	
	private static boolean mDisplayQuote = false;	/*Switch between success message and Quote*/

	// Helper Layout
	private RelativeLayout rlPopup;
	private RelativeLayout rlRevealLetter;
	private RelativeLayout rlRemoveLetters;
	private RelativeLayout rlShare;
	private RelativeLayout rlAskFacebook;
	private RelativeLayout rlAskTwitter;
	private TextView tvPopupClose;
	private TextView tvPopupTitle;
	private TextView tvRevealLetter;
	private TextView tvRemoveLetter;
	private TextView tvAskFacebook;
	private TextView tvAskTwitter;
	private TextView tvAskShare;
	
	//Get Coins
	private RelativeLayout rlPopupGetCoins;
	private TextView tvPopupCoinsClose;
	private TextView tvPopupCoinsTitle;
	private TextView tvPopup750Coins;
	private TextView tvPopup2000Coins;
	private TextView tvPopup5000Coins;
	private TextView tvPopup15000Coins;
	private TextView tvPopupDownloadCoins;
	// Popup Buttons/Layouts
	private RelativeLayout rlBuy750Coins;
	private RelativeLayout rlBuy2000Coins;
	private RelativeLayout rlBuy5000Coins;
	private RelativeLayout rlBuy15000Coins;		
	// Google Play in app purchase
	private static String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAi+rRG6vn7nMbYv+VjH0L9CrtejJYzMi4e8i853q53sZ85yfUSYmL4nU+PkliS1Rmh4nSmxrXX+uFxeshHPsWmXORwoKfREkBLaL4ZcNbasKN7KECtw9lNw1+vht774JcrFaNbXDG4AOzoM+q2JPM24wecMrs6iYODiEFyJNUqPKKT3AAKP9Q4WQ2vufe6KNcIjNY1TnkMovNvnyUAHyqRGkAdYo772iLwa2JKCnu5v8y8KIizPRKsHwi6Gb4MqoF8h+VNbFioRtJLjPpb8CpvcYMp31x5/12E/873mKendAnVkbljr4tRDVcD9cM7JH8umb6YhpOx85K/Rq2joptnQIDAQAB";
	private IabHelper mHelper;
	private int mPurchaseCoins = 0;
	private int mTotalWords = 0;
	// Used for testing by google play
	// android.test.purchased, android.test.cancelled, 
	// android.test.refunded, android.test.item_unavailable
	static final String SKU_750 = "4pics_750";
	static final String SKU_2000 = "4pics_2000";
	static final String SKU_5000 = "4pics_5000";
	static final String SKU_15000 = "4pics_15000";

	// Members and Parameters
	private WordInfo mWordInfo; // Target Word information
	private LettersAdapter mLettersAdapter;
	private TargetWordAdapter mTargetWordAdapter;
	private ArrayList<LetterInfo> mLetters; // Full list of Letters
	private ArrayList<LetterInfo> mTargetWord; // Target list of letters
	private long mElapsedTime;
	private long mPauseElapsedTime;
	private int mBaseScore;
	private int mBasePenalty;
	private int mPenalties; // Penalty is for incorrect try (10,9,8...(
							// consecutive tries.
	private boolean mCorrectWord; // Flag used for handling penalties
	private boolean mSolvedWord; // Flag used for handling compelte word
	private Typeface face;
	private MyCountDownTimer mTimerRemind;
	private int mCountdownTime;
	private int mCostRevealLetter;
	private int mCostRemoveLetters;
	private Animation mAnimShake;
	private View mHelperView;
	private Animation mAnimGrow;
	private Animation mScaleUp;
	private Animation mScaleDown;
	private boolean mIsPopupVisible;
	private int mScreenWidth;
	private boolean isScaledUp;
	private String[] mSuccessMsg;
	private String[] mSuccessSubMsg;
	private MediaPlayer mpButtonClick;
	private MediaPlayer mpGameWin;
	private boolean mNoMistakes = true;
	private int mScore = 0;
	private int mCoins = 0;

	/** The interstitial ad. */
	private InterstitialAd interstitialAd;
	
	// TAPJOY
	int point_total;
	String currency_name;

	String displayText = "";
	boolean update_text = false;
	boolean earnedPoints = false;

	// Display Ads.
	boolean update_display_ad = false;
	View adView;
	RelativeLayout relativeLayout;
	LinearLayout adLinearLayout;

	// Need handler for callbacks to the UI thread
	final Handler mHandler = new Handler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_play);

		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		mScreenWidth = metrics.widthPixels;

		_appPrefs = new AppPreferences(getApplicationContext());		
		
		LoadTotalWords();		
		LoadResources();
		
		
		if (_appPrefs.getWordCompleted() - 1 >= mTotalWords) {
			ProcessCompletedAllLevels();
		} else {
			LoadWord(_appPrefs.getWordCompleted());
								
			// initialize like the title and score
			InitializeValues();
					
			LoadListeners();
		}
			
	}

	@Override
	protected void onStart() {
		super.onStart();
		FlurryAgent.onStartSession(this, getString(R.string.flurry));
		// Start the timer
//		LogFlurryTimed("Play_Word", "Play Activity", "Start", true);
	}

	@Override
	protected void onStop() {
		super.onStop();
		FlurryAgent.onEndSession(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		if (mHelper != null) mHelper.dispose();
		mHelper = null;
	}

	@Override
	protected void onPause() {
		super.onPause();		
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mIsPopupVisible) {
			// mElapsedTime = System.currentTimeMillis() - mElapsedTime;
			mIsPopupVisible = false;
			// Start the timer
			mTimerRemind.start();
			mElapsedTime = mElapsedTime + (mPauseElapsedTime - mElapsedTime);
		}
	}

	/***
	 * On Activity Result, to handle the In App Purchase result
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) 
	{
		if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {     
			super.onActivityResult(requestCode, resultCode, data);
			}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			switch (keyCode) {
			case KeyEvent.KEYCODE_HOME:
//				LogFlurryTimed("Play_Word", "Play Activity", "Start", false);
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onBackPressed() {
		if (!mIsPopupVisible) {
			super.onBackPressed();
//			LogFlurryTimed("Play_Word", "Play Activity", "Start", false);
			mTimerRemind.cancel();
			mPauseElapsedTime = System.currentTimeMillis();
			finish();
		} else {
			mIsPopupVisible = false;
			rlPopup.setVisibility(View.INVISIBLE);
			mTimerRemind.start();
			mElapsedTime = mElapsedTime + (mPauseElapsedTime - mElapsedTime);
		}
		PlayButtonClick();
	}

	/**
	 * Log the flurry actions
	 * 
	 * @param event
	 * @param Activity
	 * @param action
	 */
	private void LogFlurry(String event, String Activity, String action) {
		Map<String, String> params = new HashMap<String, String>();
		/*params.put("Category", mLevelInfo.CatName);
		params.put("Level", mLevelInfo.LvlName);
		params.put("Word", mWordInfo.Word);*/
		params.put(Activity, action);
		FlurryAgent.logEvent(event, params);
	}

	/**
	 * Log the flurry actions
	 * 
	 * @param event
	 * @param Activity
	 * @param action
	 */
//	private void LogFlurryTimed(String event, String Activity, String action,
//			boolean start) {
//		if (start) {
//			Map<String, String> params = new HashMap<String, String>();
//			params.put("Word", mWordInfo.Word);
//			params.put(Activity, action);
//			FlurryAgent.logEvent(event, params, true);
//		} else
//			FlurryAgent.endTimedEvent(event);
//	}

	/**
	 * Play button click if sound is enabled
	 */
	private void PlayButtonClick() {
		if (_appPrefs.getSound())
		{
			if (mpButtonClick!=null) {
				mpButtonClick.stop();
				mpButtonClick.release();
	          }
			mpButtonClick= MediaPlayer.create(PlayActivity.this,R.raw.button);
			mpButtonClick.start();
		}
	}

	/**
	 * Play button click if sound is enabled
	 */
	private void PlaySuccessSound() {
		if (_appPrefs.getSound())
			mpGameWin.start();
	}

	/**
	 * Count total words
	 * 
	 * @param catId
	 */
	private void LoadTotalWords() {
		WordInfoDB db = new WordInfoDB(this);
		db.open();
		Cursor cur = db.getRecords();
		mTotalWords = cur.getCount();
		cur.close();
		db.close();
	}
	
	/**
	 * Load all the levels for the specified Category
	 * 
	 * @param catId
	 */
	private void LoadWord(int id) {
		mWordInfo = new WordInfo();
		mWordInfo.ID = id;

		WordInfoDB db = new WordInfoDB(this);
		db.open();
		Cursor cur = db.getRecord(id);
		if (cur != null) {
			cur.moveToFirst();
			mWordInfo.ID = cur.getInt(cur.getColumnIndex(WordInfoDB.ROW_ID));
			mWordInfo.Unlocked = cur.getInt(cur
					.getColumnIndex(WordInfoDB.UNLOCKED)) > 0;
			mWordInfo.Solved = cur
					.getInt(cur.getColumnIndex(WordInfoDB.SOLVED)) > 0;
			mWordInfo.Score = cur.getInt(cur.getColumnIndex(WordInfoDB.SCORE));
			mWordInfo.Word = cur.getString(cur.getColumnIndex(WordInfoDB.WORD));
			mWordInfo.Letters = cur.getString(cur
					.getColumnIndex(WordInfoDB.LETTERS));
			mWordInfo.Image1 = cur.getString(cur
					.getColumnIndex(WordInfoDB.IMAGE1));
			mWordInfo.Image2 = cur.getString(cur
					.getColumnIndex(WordInfoDB.IMAGE2));
			mWordInfo.Image3 = cur.getString(cur
					.getColumnIndex(WordInfoDB.IMAGE3));
			mWordInfo.Image4 = cur.getString(cur
					.getColumnIndex(WordInfoDB.IMAGE4));
			mWordInfo.HTTP = cur.getString(cur.getColumnIndex(WordInfoDB.HTTP));
			mWordInfo.LINK = cur.getString(cur.getColumnIndex(WordInfoDB.LINK));
			cur.close();
		}
		db.close();
	}

	

	/**
	 * Initialize all the default or inherited values
	 */
	private void InitializeValues() {
		
		
		interstitialAd = new InterstitialAd(this, getResources().getString(R.string.admob_id));
        
        // Set the AdListener.
        interstitialAd.setAdListener(this);
        
		// Load Default values
		isScaledUp = false;		
		mBaseScore = this.getResources().getInteger(R.integer.base_score);
		mBasePenalty = this.getResources().getInteger(R.integer.base_penalty);
		mCountdownTime = this.getResources().getInteger(
				R.integer.help_icon_shake);
		mCostRevealLetter = this.getResources().getInteger(
				R.integer.help_reveal_letter);
		mCostRemoveLetters = this.getResources().getInteger(
				R.integer.help_remove_letters);
		mCorrectWord = true;
		
		// mLetters = Utility.RandomLetters(mWordInfo.Letters, mAlphabet,
		// mMaxNumberOfLetters);
		mLetters = Utility.ParseString(mWordInfo.Letters, true);
		mLetters.add(HINT_POSITION, DUMMY_LETTER);// Get ready for Hint
		mLetters.add(DUMMY_LETTER);// Get ready for GetCoins
		mTargetWord = Utility.GenerateBlankArray("", mWordInfo.Word, true);
		// Mark letters as target word letters
		String letter = "";
		boolean isFound = false;
		for (int i = 0; i < mWordInfo.Word.length(); i++) {
			letter = mWordInfo.Word.substring(i, i + 1);
			isFound = false;
			for (int j = 0; j < mLetters.size(); j++) {
				if (!mLetters.get(j).isButton
						&& !mLetters.get(j).isTargetLetter) {
					if (mLetters.get(j).Letter.equals(letter)) {
						mLetters.get(j).isTargetLetter = true;
						isFound = true;
					}
				}
				if (isFound)
					break;
			}
		}

		// Load Grid Views - Target Word
		mTargetWordAdapter = new TargetWordAdapter(this, mTargetWord);
		gvTargetWord.setNumColumns(mTargetWord.size());		
		gvTargetWord.setAdapter(mTargetWordAdapter);
		gvTargetWord.setClickable(true);
		// Set target Word width
		ViewGroup.LayoutParams layoutParams = gvTargetWord.getLayoutParams();
		layoutParams.width = layoutParams.width * mTargetWord.size(); // this is
																		// in
																		// pixels
		gvTargetWord.setLayoutParams(layoutParams);

		// Load Grid Views - Proposed Letters
		mLettersAdapter = new LettersAdapter(this, mLetters);
		gvLetters.setAdapter(mLettersAdapter);
		gvLetters.setClickable(true);

		// Load Images
		ivWordImage1.setImageDrawable(Utility.DrawableFromAsset(this,
				mWordInfo.Image1, DisplayMetrics.DENSITY_DEFAULT, 1));
		ivWordImage2.setImageDrawable(Utility.DrawableFromAsset(this,
				mWordInfo.Image2, DisplayMetrics.DENSITY_DEFAULT, 1));
		ivWordImage3.setImageDrawable(Utility.DrawableFromAsset(this,
				mWordInfo.Image3, DisplayMetrics.DENSITY_DEFAULT, 1));
		ivWordImage4.setImageDrawable(Utility.DrawableFromAsset(this,
				mWordInfo.Image4, DisplayMetrics.DENSITY_DEFAULT, 1));
		
		// Set the coins
		tvCoins.setText(String.valueOf(_appPrefs.getCoins()));

		// ***********************************************************

		// Start the countdown timer
		// mTimerRemind.start();
	}

	/**
	 * Load all the resources the activity needs
	 */
	private void LoadResources() {
						
		mpGameWin = MediaPlayer.create(this, R.raw.win);
		face = Typeface.createFromAsset(getAssets(), "Chuck.ttf");

		//Get Coins
		rlPopupGetCoins = (RelativeLayout) findViewById(R.id.layout_getcoins);
		rlPopupGetCoins.setVisibility(View.INVISIBLE);
		rlBuy750Coins = (RelativeLayout) findViewById(R.id.layout_750_coins);
		rlBuy2000Coins = (RelativeLayout) findViewById(R.id.layout_2000_coins);
		rlBuy5000Coins = (RelativeLayout) findViewById(R.id.layout_5000_coins);
		rlBuy15000Coins = (RelativeLayout) findViewById(R.id.layout_15000_coins);
		
		tvPopupCoinsClose = (TextView) findViewById(R.id.tvPopupCoinsClose);
		tvPopupCoinsTitle = (TextView) findViewById(R.id.tvPopupCoinsTitle);
		tvPopupCoinsTitle.setTypeface(face);

		tvPopup750Coins = (TextView) findViewById(R.id.tv750Coins);
		tvPopup750Coins.setTypeface(face);
		tvPopup2000Coins = (TextView) findViewById(R.id.tv2000Coins);
		tvPopup2000Coins.setTypeface(face);
		tvPopup5000Coins = (TextView) findViewById(R.id.tv5000Coins);
		tvPopup5000Coins.setTypeface(face);
		tvPopup15000Coins = (TextView) findViewById(R.id.tv15000Coins);
		tvPopup15000Coins.setTypeface(face);
		tvPopupDownloadCoins = (TextView) findViewById(R.id.tvDownloadCoinsTitle);
		tvPopupDownloadCoins.setTypeface(face);
		
		ivBack = (ImageView) findViewById(R.id.button_back);
		ivLogo = (ImageView) findViewById(R.id.button_logo);
		tvTitle = (TextView) findViewById(R.id.tvTopBarTitle);		
		tvTitle.setTypeface(face);
		
		tvLevelNums = (TextView) findViewById(R.id.tvLevelNums);
		tvLevelNums.setText(String.valueOf(_appPrefs.getWordCompleted()));
		tvLevelNums.setTypeface(face);
		
		tvCoins = (TextView) findViewById(R.id.tvTopBarCoins);
		tvCoins.setTypeface(face);
		ivLeaderBoard = (ImageView) findViewById(R.id.icon_leaderboard);
		
		gvLetters = (GridView) findViewById(R.id.gvLetters);
		gvTargetWord = (GridView) findViewById(R.id.gvTargetWord);
		// gvTargetWord.setEnabled(false);
		ivWordImage1 = (ImageView) findViewById(R.id.ivWordImage1);
		ivWordImage2 = (ImageView) findViewById(R.id.ivWordImage2);
		ivWordImage3 = (ImageView) findViewById(R.id.ivWordImage3);
		ivWordImage4 = (ImageView) findViewById(R.id.ivWordImage4);
		ivWordImage = (ImageView) findViewById(R.id.ivWordImage);

		// Summary layout
		llSummaryScore = (LinearLayout) findViewById(R.id.layout_success_summary_score);
		llSummaryCoins = (LinearLayout) findViewById(R.id.layout_success_summary_coins);

		llSuccessImage = (LinearLayout) findViewById(R.id.layout_images_success);
		llSuccessImage.setVisibility(View.INVISIBLE);
		ivSuccess = (ImageView) findViewById(R.id.ivWordImageSuccess);

		rlSuccessSummary = (RelativeLayout) findViewById(R.id.layout_success_summary);
		rlSuccessSummary.setVisibility(View.INVISIBLE);
		/*Switch between success message and Quote*/
		if(mDisplayQuote)
		{
			tvSuccessMessage = (TextView) findViewById(R.id.tvSuccessQuoteMessage);
			tvSuccessMessage.setTypeface(face);
			tvSuccessSubMessage = (TextView) findViewById(R.id.tvSuccessQuotePerson);
		}
		else
		{
			tvSuccessMessage = (TextView) findViewById(R.id.tvSuccessMessage);
			tvSuccessMessage.setTypeface(face);
			tvSuccessSubMessage = (TextView) findViewById(R.id.tvSuccessSubMessage);
		}

		llLetters = (LinearLayout) findViewById(R.id.layout_bottom_letters);
		rlTargetWord = (RelativeLayout) findViewById(R.id.layout_target_word);
		llTargetImg = (LinearLayout) findViewById(R.id.layout_target_images);
		Animation anim = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.push_left_in);
		llLetters.startAnimation(anim);
		rlTargetWord.startAnimation(anim);
		llTargetImg.startAnimation(anim);

		tvNext = (TextView) findViewById(R.id.tvNext);
		tvNext.setTypeface(face);
		tvBack = (TextView) findViewById(R.id.tvBack);
		tvBack.setTypeface(face);
		tvSummaryCoins = (TextView) findViewById(R.id.tvSummaryCoinsVal);
		tvSummaryScore = (TextView) findViewById(R.id.tvSummaryScoreVal);

		DUMMY_LETTER = new LetterInfo();
		DUMMY_LETTER.Letter = "";
		DUMMY_LETTER.Visible = true;
		DUMMY_LETTER.isButton = true;

		// Helper layout
		mIsPopupVisible = false;
		rlPopup = (RelativeLayout) findViewById(R.id.layout_helpers);
		rlPopup.setVisibility(View.INVISIBLE);
		rlRevealLetter = (RelativeLayout) findViewById(R.id.layout_reveal_letter);
		rlRemoveLetters = (RelativeLayout) findViewById(R.id.layout_remove_letters);
		rlShare = (RelativeLayout) findViewById(R.id.layout_share);
		rlAskFacebook = (RelativeLayout) findViewById(R.id.layout_ask_facebook);
		rlAskTwitter = (RelativeLayout) findViewById(R.id.layout_ask_twitter);

		//Hide this, since we are using general share intent
		rlAskFacebook.setVisibility(View.GONE);
		rlAskTwitter.setVisibility(View.GONE);
		
		tvPopupClose = (TextView) findViewById(R.id.tvPopupClose);
		tvPopupTitle = (TextView) findViewById(R.id.tvPopupTitle);
		tvPopupTitle.setTypeface(face);
		tvRevealLetter = (TextView) findViewById(R.id.tvRevealLetter);
		tvRevealLetter.setTypeface(face);
		tvRemoveLetter = (TextView) findViewById(R.id.tvRemoveLetter);
		tvRemoveLetter.setTypeface(face);
		tvAskFacebook = (TextView) findViewById(R.id.tvAskFacebook);
		tvAskFacebook.setTypeface(face);
		tvAskTwitter = (TextView) findViewById(R.id.tvAskTwitter);
		tvAskTwitter.setTypeface(face);
		tvAskShare = (TextView) findViewById(R.id.tvShare);
		tvAskShare.setTypeface(face);
		
		// Google Play in app purchase
		mHelper = new IabHelper(this, base64EncodedPublicKey);
		
		if(mDisplayQuote)
		{
			mSuccessMsg = this.getResources().getStringArray(R.array.quote_message);
			mSuccessSubMsg = this.getResources().getStringArray(R.array.quote_person);
		}
		else
		{
			mSuccessMsg = this.getResources().getStringArray(R.array.success_message);
			mSuccessSubMsg = this.getResources().getStringArray(R.array.success_submessage);
		}
		
		mAnimShake = AnimationUtils.loadAnimation(this, R.anim.shake_icon);
		mAnimGrow = AnimationUtils.loadAnimation(this, R.anim.grow_from_middle);
		mScaleUp = AnimationUtils.loadAnimation(this, R.anim.scale_up);
		mScaleDown = AnimationUtils.loadAnimation(this, R.anim.scale_down);

		// Initialize the Time
		mElapsedTime = System.currentTimeMillis();

		mTimerRemind = new MyCountDownTimer(mCountdownTime * 1000, 1000) {
			@Override
			public void onTick(long millisUntilFinished) {
			}

			@Override
			public void onFinish() {
				if (!mSolvedWord) {
					mHelperView = gvLetters.getChildAt(HINT_POSITION);
					mHelperView.startAnimation(mAnimShake);
					mCountdownTime = PlayActivity.this.getResources()
							.getInteger(R.integer.help_icon_shake_repeat);
					this.setMillisInFuture(mCountdownTime * 1000); // here we
																	// change
																	// the
																	// millisInFuture
																	// of our
																	// timer
					this.start();

				}
			}
		};
	}

	/**
	 * Load all the Listeners. Resources need to be loaded previously.
	 */
	private void LoadListeners() {
		mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
	        @Override
	        public void onIabSetupFinished(IabResult result) {
	            if(result.isSuccess()) {
	                // Fill a list of SKUs that we want the price infos for
	                // (SKU = "stockable unit" = buyable things)
	                ArrayList<String> moreSkus = new ArrayList<String>();
	                moreSkus.add(SKU_750); 
	                moreSkus.add(SKU_2000);
	                moreSkus.add(SKU_5000);
	                moreSkus.add(SKU_15000);
	 
	                // We initialize the price field with a "retrieving price" message while we wait 
	                // for the price
	                // final TextView tvPrice = (TextView)BuyPremiumActivity.this.findViewById(R.id.price);
	                // tvPrice.setText(R.string.waiting_for_price);
	                 
	                // Start the query for the details for the SKUs. This runs asynchronously, so
	                // it may be that the price appears a bit later after the rest of the Activity is shown.
	                mHelper.queryInventoryAsync(true, moreSkus, new QueryInventoryFinishedListener() {
	                    @Override
	                    public void onQueryInventoryFinished(IabResult result, Inventory inv) {
	                        if(result.isSuccess()) {
	                            // If we successfully got the price, show it in the text field		                            
	                            // On successful init and price getting, enable the "buy me" button		                           
	                        } else {
	                            // Error getting the price... show a sorry text in the price field now
	                            //tvPrice.setText(R.string.cant_get_prices);
	                        	complain("Problem setting up in-app billing: " + result);
	                        }
	                    }
	                });
	            } else {
	                // If the billing API could not be initialized at all, show a sorry dialog. This
	                // will surely prevent the user from being able to buy anything.
	                //Billing.showSorry(BuyPremiumActivity.this, R.string.cant_init_billing_api);
	            	complain("Problem setting up in-app billing: " + result);
	            }
	        }
	    });	
		
		// Go back to previous screen
		ivBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				PlayButtonClick();
				finish();
			}
		});
		ivLogo.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				PlayButtonClick();
				finish();
			}
		});

		// Show the leaderboard
		ivLeaderBoard.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				LogFlurry("High_Scores", "Show Leaderboard", "Play Activity");
				PlayButtonClick();
				showLeaderBoard();
			}
		});

		
		//myItem = myAdapter.getItem(myGridView.pointToPosition((int)e.getX(), (int)e.getY()));
		gvLetters.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction()==MotionEvent.ACTION_DOWN)
				{					
					float currentXPosition = event.getX();
		            float currentYPosition = event.getY();
		            int position = gvLetters.pointToPosition((int) currentXPosition, (int) currentYPosition);
		            if (position == HINT_POSITION) // Show Hint
					{
		            	PlayButtonClick();						
						mIsPopupVisible = true;
						mTimerRemind.cancel();
						mPauseElapsedTime = System.currentTimeMillis();
						rlPopup.setVisibility(View.VISIBLE);
						rlPopup.startAnimation(mAnimGrow);
					} else if (position == BUY_COINS_POSITION) // Show coins
					{
						PlayButtonClick();	
						LogFlurry("Get_Coins","Play Activity","Initiated");
						mIsPopupVisible = true;
						mTimerRemind.cancel();
						mPauseElapsedTime = System.currentTimeMillis();						
						rlPopupGetCoins.setVisibility(View.VISIBLE);
						rlPopupGetCoins.startAnimation(mAnimGrow);
						
					} else if (position > -1){
						PlayButtonClick();
						if (!mSolvedWord) {
							PlayGame(position, false);
						}
					}
				}
				return false;
			}
		});
		
		
		gvTargetWord.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				
				if(event.getAction()==MotionEvent.ACTION_DOWN)
				{
					PlayButtonClick();
					 
					float currentXPosition = event.getX();
		            float currentYPosition = event.getY();
		            int position = gvTargetWord.pointToPosition((int) currentXPosition, (int) currentYPosition);

		            if (position > -1){
			            if (!mTargetWord.get(position).isHint) {
							ReleaseLetter(position);
						}
		            }
				}
				return false;
			}
		});		

		// GoBack After game completed
		tvBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				PlayButtonClick();
				finish();
			}
		});

		// Go to The Next game, after game completed
		tvNext.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				PlayButtonClick();
				finish();
				// Get the next (first game)
				if (_appPrefs.getWordCompleted() - 1 < mTotalWords) {
					WordInfoDB db = new WordInfoDB(getApplicationContext());
					db.open();
					int id = db.getNextID(mWordInfo.ID);
					db.close();

					Intent intent = getIntent();
					_appPrefs.setWordCompleted(id);
					startActivity(intent);
					
					if ( _appPrefs.getShowAds() && (_appPrefs.getWordCompleted() - 1) % getResources().getInteger(R.integer.interstitial_level) == 0) {
				        // Create ad request
				        AdRequest adRequest = new AdRequest();

				        // Begin loading your interstitial
				        interstitialAd.loadAd(adRequest);
					}
				}
			}
		});

		// HELPER WINDOW
		tvPopupClose.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				LogFlurry("Hint_Popup", "Play Activity", "Cancelled");
				PlayButtonClick();
				mIsPopupVisible = false;
				rlPopup.setVisibility(View.INVISIBLE);
				mTimerRemind.start();
				mElapsedTime = mElapsedTime
						+ (mPauseElapsedTime - mElapsedTime);
			}
		});
		rlRevealLetter.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				PlayButtonClick();
				if (mCostRevealLetter > _appPrefs.getCoins()) {
					ShowDialog(getString(R.string.dialog_message_no_coins));					
					LogFlurry("Hint_Popup", "Reveal Letter", "No Coins");
				} else {
					LogFlurry("Hint_Popup", "Reveal Letter", "Purchased");
					LogFlurry("Hint_Popup", "Play Activity", "Reveal Letter");
					ShowHintLetter();
					mIsPopupVisible = false;
					rlPopup.setVisibility(View.INVISIBLE);
					mTimerRemind.start();
					mElapsedTime = mElapsedTime
							+ (mPauseElapsedTime - mElapsedTime);
					updateAppCoins(-1 * mCostRevealLetter);
				}
			}
		});
		rlRemoveLetters.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				PlayButtonClick();
				if (mCostRemoveLetters > _appPrefs.getCoins()) {
					ShowDialog(getString(R.string.dialog_message_no_coins));
					LogFlurry("Hint_Popup", "Remove Letter", "No Coins");
				} else {
					if (RemoveLetters()) {
						LogFlurry("Hint_Popup", "Remove Letter", "Purchased");
						LogFlurry("Hint_Popup", "Play Activity", "Remove Letter");
						updateAppCoins(-1 * mCostRemoveLetters);
						/*
						 * _appPrefs.saveCoins(_appPrefs.getCoins() -
						 * mCostRemoveLetters);
						 * tvCoins.setText(String.valueOf(_appPrefs
						 * .getCoins()));
						 * TapjoyConnect.getTapjoyConnectInstance(
						 * ).spendTapPoints( mCostRemoveLetters,
						 * PlayActivity.this);
						 */
					}
					mIsPopupVisible = false;
					rlPopup.setVisibility(View.INVISIBLE);
					mTimerRemind.start();
					mElapsedTime = mElapsedTime
							+ (mPauseElapsedTime - mElapsedTime);

				}
			}
		});
		
		rlAskFacebook.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				PlayButtonClick();
				String text = getString(R.string.share_facebook_text_sub) + " "
						+ mWordInfo.LINK;
				Utility.ShareViaFacebook(PlayActivity.this,
						getString(R.string.share_facebook_subject), text);

				rlPopup.setVisibility(View.INVISIBLE);
			}
		});
		
		// Take screenhshot and share
		rlShare.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {				
				LogFlurry("Help_Share","Word",mWordInfo.Word);
				rlPopup.setVisibility(View.INVISIBLE);
				File file = Utility.saveBitmapToCameraFolder(takeScreenshot());
				rlPopup.setVisibility(View.VISIBLE);				
				Utility.Share(PlayActivity.this, file,getString(R.string.share_all_title));				
			}
		});
		rlAskTwitter.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				PlayButtonClick();
				String text = getString(R.string.share_twitter_text) + " "
						+ mWordInfo.HTTP;
				Intent tweet = new Intent(Intent.ACTION_VIEW);
				tweet.setData(Uri.parse("http://twitter.com/?status="
						+ Uri.encode(text)));
				startActivity(tweet);

				rlPopup.setVisibility(View.INVISIBLE);

			}
		});

		// Show the big image picture
		ivWordImage.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				PlayButtonClick();
				if (isScaledUp) {
					ivWordImage.setVisibility(View.INVISIBLE);
					ivWordImage.startAnimation(mScaleDown);
					isScaledUp = false;
				}
			}
		});

		// Show the big image picture
		ivWordImage1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				PlayButtonClick();
				if (!isScaledUp) {
					isScaledUp = true;
					ivWordImage.setImageDrawable(Utility.DrawableFromAsset(
							getApplicationContext(), mWordInfo.Image1,
							DisplayMetrics.DENSITY_MEDIUM, 1));
					ivWordImage.setVisibility(View.VISIBLE);
					ivWordImage.startAnimation(mScaleUp);
				}

			}
		});

		ivWordImage2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				PlayButtonClick();
				if (!isScaledUp) {
					isScaledUp = true;
					ivWordImage.setImageDrawable(Utility.DrawableFromAsset(
							getApplicationContext(), mWordInfo.Image2,
							DisplayMetrics.DENSITY_MEDIUM, 1));
					ivWordImage.setVisibility(View.VISIBLE);
					ivWordImage.startAnimation(mScaleUp);
				}
			}
		});
		ivWordImage3.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				PlayButtonClick();
				if (!isScaledUp) {
					isScaledUp = true;
					ivWordImage.setImageDrawable(Utility.DrawableFromAsset(
							getApplicationContext(), mWordInfo.Image3,
							DisplayMetrics.DENSITY_MEDIUM, 1));
					ivWordImage.setVisibility(View.VISIBLE);
					ivWordImage.startAnimation(mScaleUp);
				}
			}
		});

		ivWordImage4.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				PlayButtonClick();
				if (!isScaledUp) {
					isScaledUp = true;
					ivWordImage.setImageDrawable(Utility.DrawableFromAsset(
							getApplicationContext(), mWordInfo.Image4,
							DisplayMetrics.DENSITY_MEDIUM, 1));
					ivWordImage.setVisibility(View.VISIBLE);
					ivWordImage.startAnimation(mScaleUp);
				}
			}
		});

		// HELPER WINDOW
				tvPopupCoinsClose.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {				
						LogFlurry("Get_Coins","Start Activity","Cancelled");
						PlayButtonClick();
						mIsPopupVisible = false;
						rlPopupGetCoins.setVisibility(View.INVISIBLE);
					}
				});
				
				// Buy 750 Coins					
				rlBuy750Coins.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						PlayButtonClick();
						LogFlurry("Get_Coins","Buy Coins","750");
						mPurchaseCoins =750;				
						//setWaitScreen(true);
				        
				        /* TODO: for security, generate your payload here for verification. See the comments on 
				         *        verifyDeveloperPayload() for more info. Since this is a SAMPLE, we just use 
				         *        an empty string, but on a production app you should carefully generate this. */
				        String payload = ""; 		    				
						mHelper.launchPurchaseFlow(PlayActivity.this, SKU_750, 
								mPurchaseCoins, mPurchaseFinishedListener);				
					}
				});
				// Buy 2000 Coins					
				rlBuy2000Coins.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						PlayButtonClick();
						LogFlurry("Get_Coins","Buy Coins","2000");
						mPurchaseCoins =2000;
						mHelper.launchPurchaseFlow(PlayActivity.this, SKU_2000, 
								mPurchaseCoins, mPurchaseFinishedListener);	
					}
				});
				// Buy 5000 Coins					
				rlBuy5000Coins.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						PlayButtonClick();
						LogFlurry("Get_Coins","Buy Coins","5000");
						mPurchaseCoins =5000;			
						mHelper.launchPurchaseFlow(PlayActivity.this, SKU_5000, 
								mPurchaseCoins, mPurchaseFinishedListener);
					}
				});
				// Buy 15000 Coins					
				rlBuy15000Coins.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						PlayButtonClick();
						LogFlurry("Get_Coins","Buy Coins","15000");
						mPurchaseCoins =15000;				
						mHelper.launchPurchaseFlow(PlayActivity.this, SKU_15000, 
								mPurchaseCoins, mPurchaseFinishedListener);	
					}
				});
	}

	/**
	 * Update the points status.
	 * 
	 * @param points
	 */
	private void updateAppCoins(int points) {
		_appPrefs.saveCoins(_appPrefs.getCoins() + points);
		tvCoins.setText(String.valueOf(_appPrefs.getCoins()));
	}

	@Override
    public void onReceiveAd(Ad ad) {
      if (ad == interstitialAd) {
    	  interstitialAd.show();
      }
    }
    
    /** Called when an ad is clicked and about to return to the application. */
    @Override
    public void onDismissScreen(Ad ad) {
      
    }

    /** Called when an ad was not received. */
    @Override
    public void onFailedToReceiveAd(Ad ad, AdRequest.ErrorCode error) {
      
    }

    /**
     * Called when an ad is clicked and going to start a new Activity that will
     * leave the application (e.g. breaking out to the Browser or Maps
     * application).
     */
    @Override
    public void onLeaveApplication(Ad ad) {
      
    }

    /**
     * Called when an Activity is created in front of the app (e.g. an
     * interstitial is shown, or an ad is clicked and launches a new Activity).
     */
    @Override
    public void onPresentScreen(Ad ad) {    
    }
    
	/**
	 * Show the Leaderboard
	 */
	private void showLeaderBoard() {
		mTimerRemind.cancel();
		mPauseElapsedTime = System.currentTimeMillis();
		mIsPopupVisible = true;
			
		Swarm.setAllowGuests(true);
		Swarm.init(this, this.getResources().getInteger(R.integer.swarm_app_id), this.getResources().getString(R.string.swarm_app_key));

		//Submit Totals
		// TODO dcm
		ArrayList<WordInfo> mWordList = LoadWords();		
		int totalScore = 0;
		for(int i=0;i<mWordList.size();i++)
			totalScore = totalScore + mWordList.get(i).Score;

		SwarmLeaderboard.submitScoreAndShowLeaderboard(
		this.getResources().getInteger(R.integer.swarm_leaderboard_id), totalScore);
	}
		

	/**
	 * Load all the levels for the specified Category
	 * 
	 * @param catId
	 */
	private ArrayList<WordInfo> LoadWords() {
		ArrayList<WordInfo> mWordList = new ArrayList<WordInfo>();
		WordInfoDB db = new WordInfoDB(this);
		db.open();
		Cursor cur = db.getRecords();
		if (cur != null) {
			cur.moveToFirst();
			do {
								
				WordInfo mWordInfo = new WordInfo();			
				mWordInfo.ID = cur.getInt(cur.getColumnIndex(WordInfoDB.ROW_ID));
				mWordInfo.Unlocked = cur.getInt(cur
						.getColumnIndex(WordInfoDB.UNLOCKED)) > 0;
				mWordInfo.Solved = cur
						.getInt(cur.getColumnIndex(WordInfoDB.SOLVED)) > 0;
				mWordInfo.Score = cur.getInt(cur.getColumnIndex(WordInfoDB.SCORE));
				mWordInfo.Word = cur.getString(cur.getColumnIndex(WordInfoDB.WORD));
				mWordInfo.Letters = cur.getString(cur
						.getColumnIndex(WordInfoDB.LETTERS));
				mWordInfo.Image1 = cur.getString(cur
						.getColumnIndex(WordInfoDB.IMAGE1));
				mWordInfo.Image2 = cur.getString(cur
						.getColumnIndex(WordInfoDB.IMAGE2));
				mWordInfo.Image3 = cur.getString(cur
						.getColumnIndex(WordInfoDB.IMAGE3));
				mWordInfo.Image4 = cur.getString(cur
						.getColumnIndex(WordInfoDB.IMAGE4));
				mWordInfo.HTTP = cur.getString(cur.getColumnIndex(WordInfoDB.HTTP));
				mWordInfo.LINK = cur.getString(cur.getColumnIndex(WordInfoDB.LINK));
				
				mWordList.add(mWordInfo);
			} while (cur.moveToNext());
			cur.close();
		}
		db.close();
		return mWordList;
	}
	/**
	 * Remove a single letter from the list of invalid letters. This is a
	 * version 2.
	 * 
	 */
	private boolean RemoveLetters() {
		boolean isRemoved = false;
		boolean hasLetters = false;
		Random rnd = new Random();

		// Check if there are letters to be hidden.
		for (int i = 0; i < mLetters.size(); i++) {
			if (mLetters.get(i).Visible && !mLetters.get(i).isTargetLetter
					&& !mLetters.get(i).isButton)
				hasLetters = true;
		}

		// Select randomly a letter and hide it.
		// Check if letters are offered or already selected as mistake in target
		// word
		if (hasLetters) {
			do {
				int pos = rnd.nextInt(mLetters.size());
				if (!mLetters.get(pos).isButton && !mLetters.get(pos).isHint
						&& !mLetters.get(pos).isTargetLetter) {
					mLetters.get(pos).Visible = false;
					mLetters.get(pos).isHint = true;
					gvLetters.getChildAt(pos).setVisibility(View.INVISIBLE);
					isRemoved = true;
				}
			} while (!isRemoved);
		}
		/*
		 * else // { for (int i = 0; i < mTargetWord.size(); i++) {
		 * if(!mTargetWord.get(i).Letter.equals("") &&
		 * !mTargetWord.get(i).isTargetLetter) hasLetters = true; }
		 * Log.d("WORD","hasLetters2: " + hasLetters); if(hasLetters) { do { int
		 * pos = rnd.nextInt(mTargetWord.size()); if
		 * (!mTargetWord.get(pos).isButton && !mTargetWord.get(pos).isHint &&
		 * !mTargetWord.get(pos).isTargetLetter) { mLetters.get(pos).Visible =
		 * false; Log.d("WORD","Removed.Letter: " + mLetters.get(pos).Letter);
		 * mLetters.get(pos).Letter = ""; isRemoved = true; } } while
		 * (!isRemoved);
		 * 
		 * if(isRemoved) {
		 * Log.d("WORD","mTargetWordAdapter.notifyDataSetChanged()"); // Refresh
		 * the target word Adapter mTargetWordAdapter.notifyDataSetChanged();
		 * gvTargetWord.setAdapter(mTargetWordAdapter);
		 * gvTargetWord.refreshDrawableState(); } } }
		 */
		if (isRemoved) {
			// Mark the level has used a hint.
			// TODO dcm
//			mLevelInfo.HintUsed = true;
//			LevelInfoDB level = new LevelInfoDB(this);
//			level.open();
//			level.updateHintUsed(mLevelInfo.ID, 1);
//			level.close();
		}
		return isRemoved;
	}

	/**
	 * Show the next letter as hint. Make it not clickable.
	 */
	private void ShowHintLetter() {
		String letter = "";
		int position = 0;
		Random rnd = new Random();
		boolean isNext = false;

		do {
			int i = rnd.nextInt(mTargetWord.size());
			if (mTargetWord.get(i).Letter.equals("")) {
				letter = mWordInfo.Word.substring(i, i + 1);
				isNext = true;
			}
		} while (!isNext);

		// Find the position in the source
		for (int i = 0; i < mLetters.size(); i++) {
			if (mLetters.get(i).Visible
					&& mLetters.get(i).Letter.equals(letter)) {
				mLetters.get(i).isHint = true;
				position = i;
				break;
			}
		}
		// Mark the level has used a hint.
		// TODO dcm
//		mLevelInfo.HintUsed = true;
//		LevelInfoDB level = new LevelInfoDB(this);
//		level.open();
//		level.updateHintUsed(mLevelInfo.ID, 1);
//		level.close();

		// Play the game
		PlayGame(position, true);
	}

	/**
	 * Show the OK dialog activity
	 * 
	 * @param message
	 */
	private void ShowDialog(String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message)
				.setCancelable(false)
				.setPositiveButton(getString(R.string.dialog_button_OK),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								// do things
							}
						});
		AlertDialog alert = builder.create();
		alert.show();
	}

	/**
	 * Rollback the letter, pressed on the Target word. Leave the space as
	 * empty, and show the hidden letter.
	 * 
	 * @param position
	 */
	private void ReleaseLetter(int position) {
		mTimerRemind.cancel();
		String letter = mTargetWord.get(position).Letter;

		mTargetWord.get(position).Letter = "";
		mTargetWord.get(position).Visible = false;

		// Check if full word is entered and solved
		mSolvedWord = false;
		mCorrectWord = false;

		// Refresh the target word Adapter
		mTargetWordAdapter.notifyDataSetChanged();
		gvTargetWord.setAdapter(mTargetWordAdapter);
		gvTargetWord.refreshDrawableState();

		for (int i = 0; i < mLetters.size(); i++) {
			if (mLetters.get(i).Letter.equals(letter)
					&& !mLetters.get(i).Visible) {
				mLetters.get(i).Visible = true;
				gvLetters.getChildAt(i).setVisibility(View.VISIBLE);
				break;
			}
		}
		mTimerRemind.start();
	}

	/**
	 * After every letter guess, play the game, check for the end, and calulate
	 * penalties. Also, check and show the Success window
	 * 
	 * @param position
	 */
	private void PlayGame(int position, boolean isHint) {
		mTimerRemind.cancel();
		String letter = mLetters.get(position).Letter;
		mLetters.get(position).Visible = false;

		// If its hint, than it can be random, otherwise not.
		if (isHint) {
			// Place the letter in the target Word
			for (int i = 0; i < mTargetWord.size(); i++) {
				if (mTargetWord.get(i).Letter.equals("")
						&& mWordInfo.Word.substring(i, i + 1).equals(letter)) {
					mTargetWord.get(i).Letter = letter;
					mTargetWord.get(i).Visible = true;
					mTargetWord.get(i).isHint = mLetters.get(position).isHint;
					mTargetWord.get(i).isTargetLetter = mLetters.get(position).isTargetLetter;
					break;
				}
			}
		} else {
			// Place the letter in the target Word
			for (int i = 0; i < mTargetWord.size(); i++) {
				if (mTargetWord.get(i).Letter.equals("")) {
					mTargetWord.get(i).Letter = letter;
					mTargetWord.get(i).Visible = true;
					mTargetWord.get(i).isHint = mLetters.get(position).isHint;
					mTargetWord.get(i).isTargetLetter = mLetters.get(position).isTargetLetter;
					break;
				}
			}
		}
		// Hide the button from the list of letters
		gvLetters.getChildAt(position).setVisibility(View.INVISIBLE);

		// Refresh the target word Adapter, to show the letter
		mTargetWordAdapter.notifyDataSetChanged();
		gvTargetWord.setAdapter(mTargetWordAdapter);
		gvTargetWord.refreshDrawableState();

		// Check if full word is entered and solved
		mSolvedWord = WordSolved();
		if (mSolvedWord)
			mCorrectWord = WordCorrect();

		if (mSolvedWord && !mCorrectWord) {
			// Mark mistake fro Achievements counter
			mNoMistakes = false;
			_appPrefs.setCorrectAnswerCount(0);
			AddPenalty();
		}

		// Refresh the target word Adapter, to show the letter
		/*
		 * mTargetWordAdapter.notifyDataSetChanged();
		 * gvTargetWord.setAdapter(mTargetWordAdapter);
		 * gvTargetWord.refreshDrawableState();
		 */

		if (mSolvedWord && mCorrectWord)
			ProcessSuccess();
		else
			mTimerRemind.start();
	}

	/**
	 * Check if the Target word is Correct
	 * 
	 * @return
	 */
	private boolean WordCorrect() {
		for (int i = 0; i < mTargetWord.size(); i++) {
			if (!mTargetWord.get(i).Visible
					|| !mTargetWord.get(i).Letter.equals(mWordInfo.Word
							.substring(i, i + 1)))
				return false;
		}
		return true;
	}

	/**
	 * Check if all the letters have been entered
	 * 
	 * @return
	 */
	private boolean WordSolved() {
		for (int i = 0; i < mTargetWord.size(); i++) {
			if (!mTargetWord.get(i).Visible
					|| mTargetWord.get(i).Letter.equals(""))
				return false;
		}
		return true;
	}

	/**
	 * Calculate the penalties if word is incorrect
	 * 
	 * @param position
	 *            Position of letter clicked
	 */
	private void AddPenalty() {
		// Add the points to penalty
		mPenalties = mPenalties + mBasePenalty;
		// Decrease the base penalty
		if (mBasePenalty > 0)
			mBasePenalty = mBasePenalty - 1;
	}

	/**
	 * At the end of the game, show the success screen, calcucate and save
	 * scores.
	 * 
	 */
	private void ProcessCompletedAllLevels() {
				
		PlaySuccessSound();
		// Show the success message
		showRandomSuccessMsg();
		
		// Show the success layout
		llSuccessImage.setVisibility(View.VISIBLE);
		llSuccessImage.setBackgroundResource(R.color.play_success_shade);
		ivSuccess.startAnimation(mAnimGrow);

		Animation animL = AnimationUtils.loadAnimation(getApplicationContext(),
				R.anim.push_left_in);
		Animation animR = AnimationUtils.loadAnimation(getApplicationContext(),
				R.anim.push_right_in);

		llSummaryScore.startAnimation(animL);
		llSummaryCoins.startAnimation(animR);

	}
	
	/**
	 * At the end of the game, show the success screen, calcucate and save
	 * scores.
	 * 
	 */
	private void ProcessSuccess() {
		tvNext.setEnabled(false);
		
//		LogFlurryTimed("Play_Word", "Play Activity", "Start", false);
		PlaySuccessSound();
		// Show the success message
		showRandomSuccessMsg();
		// Prevent scale up when finished
		isScaledUp = true;
		// Stop the timer and calculate Score+Coins
		mElapsedTime = System.currentTimeMillis() - mElapsedTime;

		// Calculate the score
		mScore = CalculateScore();
		mCoins = CalculateCoins(mScore);

		// Save the score on category level
		
		_appPrefs.setWordCompleted(_appPrefs.getWordCompleted()+1);
		
		// Save the Coins, and handle synch for offline mode
		updateAppCoins(mCoins);					
				
		// Set the values
		tvSummaryCoins.setText(String.valueOf(mCoins));
		tvSummaryScore.setText(String.valueOf(mScore));

		// Show the summary
		llLetters.setVisibility(View.INVISIBLE);
		rlSuccessSummary.setVisibility(View.VISIBLE);

		// Disable Letters
		gvLetters.setEnabled(false);
		
		// Show the success layout
		llSuccessImage.setVisibility(View.VISIBLE);
		llSuccessImage.setBackgroundResource(R.color.play_success_shade);
		ivSuccess.startAnimation(mAnimGrow);

		Animation animL = AnimationUtils.loadAnimation(getApplicationContext(),
				R.anim.push_left_in);
		Animation animR = AnimationUtils.loadAnimation(getApplicationContext(),
				R.anim.push_right_in);

		llSummaryScore.startAnimation(animL);
		llSummaryCoins.startAnimation(animR);

		/*
		 * _appPrefs.saveCoins(_appPrefs.getCoins() + coins);
		 * _appPrefs.saveCoinsDiff(_appPrefs.getCoinsDiff()+ coins); //award
		 * points if plus, remove otherwise if(_appPrefs.getCoinsDiff() > 0)
		 * TapjoyConnect
		 * .getTapjoyConnectInstance().awardTapPoints(_appPrefs.getCoinsDiff(),
		 * this); else
		 * TapjoyConnect.getTapjoyConnectInstance().spendTapPoints(_appPrefs
		 * .getCoinsDiff(), this);
		 */
		
		ProcessPostSuccess();
	}

	private void ProcessPostSuccess() {
		new AsyncTask<Void, Void, Boolean>() {
			
			@Override
			protected void onPreExecute() {
				/*
				 * This is executed on UI thread before doInBackground(). It is
				 * the perfect place to show the progress dialog.
				 */				
			}

			@Override
			protected Boolean doInBackground(Void... params) {
				if (params == null) {
					return false;
				}
				try {
					/*
					 * This is run on a background thread, so we can sleep here
					 * or do whatever we want without blocking UI thread. A more
					 * advanced use would download chunks of fixed size and call
					 * publishProgress();
					 */
					// Thread.sleep(params[0]);
					// HERE I'VE PUT ALL THE FUNCTIONS THAT WORK FOR ME			
										
					// Save score word to DB
					WordInfoDB db = new WordInfoDB(PlayActivity.this);
					db.open();
					db.update(mWordInfo.ID, 1, 1, mScore);
					db.close();

				} catch (Exception e) {
					/*
					 * The task failed
					 */
					return false;
				}

				/*
				 * The task succeeded
				 */
				return true;
			}

			@Override
			protected void onPostExecute(Boolean result) {				
				/*
				 * Update here your view objects with content from download. It
				 * is save to dismiss dialogs, update views, etc., since we are
				 * working on UI thread.
				 */
				tvNext.setEnabled(true);
			}

		}.execute();
	}

	/**
	 * Show the success message from random success messages
	 * 
	 * @return
	 */
	private void showRandomSuccessMsg() {
		Random rnd = new Random();
		if(mDisplayQuote)
		{
			int position = rnd.nextInt(mSuccessMsg.length);
			tvSuccessMessage.setText(mSuccessMsg[position]);
			tvSuccessSubMessage.setText(mSuccessSubMsg[position]);
			tvSuccessMessage.setVisibility(View.VISIBLE);
			tvSuccessSubMessage.setVisibility(View.VISIBLE);
		}
		else
		{
			tvSuccessMessage.setText(mSuccessMsg[rnd.nextInt(mSuccessMsg.length)]);
			tvSuccessSubMessage.setText(mSuccessSubMsg[rnd
					.nextInt(mSuccessSubMsg.length)]);
			tvSuccessMessage.setVisibility(View.VISIBLE);
			tvSuccessSubMessage.setVisibility(View.VISIBLE);
			// tvSuccessMessage.setAnimation(m)
		}
	}

	/**
	 * Calculate the score after game completed
	 * 
	 * @return
	 */
	private int CalculateScore() {
		int score = (int) (mBaseScore + (250 / Math
				.sqrt((mElapsedTime / 1000) + 4)));
		score = score - mPenalties;
		return score;
	}

	/**
	 * Calculate the coins based on the number of coins
	 * 
	 * @param score
	 * @return
	 */
	private int CalculateCoins(int score) {
		if (score >= 100)
			return 5;
		else if (score >= 90)
			return 4;
		else if (score >= 80)
			return 3;
		else if (score >= 70)
			return 2;
		else
			return 1;
	}

	/**
	 * Used for Displaying Possible Letters
	 */
	private class TargetWordAdapter extends BaseAdapter {
		ArrayList<LetterInfo> list;
		LayoutInflater inflater = null;
		TextView mLetter;
		ImageView mImage;

		public TargetWordAdapter(Context context, ArrayList<LetterInfo> list) {
			this.list = list;
			inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		// @Override
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			View v = convertView;

			if (v == null)
				v = inflater.inflate(R.layout.target_letter_list_item, parent,
						false);

			v.setMinimumWidth(mScreenWidth / list.size());

			mLetter = (TextView) v.findViewById(R.id.tvTargetLetter);
			mImage = (ImageView) v.findViewById(R.id.ivTargetLetter);
			mLetter.setText(list.get(position).Letter);			
			mLetter.setTypeface(face);

			if (mSolvedWord) {
				if (mCorrectWord)
					mImage.setImageResource(R.drawable.button_letters_correct);					
				else
				{
					mImage.setImageResource(R.drawable.button_letters_error);
					Animation animation = new AlphaAnimation(1, 0); // Change alpha from fully visible to invisible
				    animation.setDuration(80); // duration - half a second
				    animation.setInterpolator(new LinearInterpolator()); // do not alter animation rate
				    animation.setRepeatCount(4); // Repeat animation 
				    animation.setRepeatMode(Animation.REVERSE); // Reverse animation at the end so the button will fade back in				  
				    v.startAnimation(animation);
				}
			} 
			else if (list.get(position).Letter.equals(""))
				mImage.setImageResource(R.drawable.button_letters_normal);
			else if (list.get(position).isHint)
				mImage.setImageResource(R.drawable.button_letters_hint);
			else
				mImage.setImageResource(R.drawable.button_letters_normal);
			
			return v;
		}

		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public LetterInfo getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}
	}

	/**
	 * Used for Displaying Possible Letters
	 * 
	 */
	private class LettersAdapter extends BaseAdapter {
		ArrayList<LetterInfo> list;
		LayoutInflater inflater = null;
		TextView mLetter;
		ImageView mImage;

		public LettersAdapter(Context context, ArrayList<LetterInfo> list) {
			this.list = list;
			inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		// @Override
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			View v = convertView;

			if (v == null)
				v = inflater.inflate(R.layout.letter_list_item, parent, false);
			v.setId(position);
			mLetter = (TextView) v.findViewById(R.id.tvLetter);
			mLetter.setTypeface(face);
			mImage = (ImageView) v.findViewById(R.id.ivButtonLetter);

			// Check if Hint button
			if (position == HINT_POSITION) {
				mLetter.setVisibility(View.INVISIBLE);
				mImage.setImageResource(R.drawable.button_hint);
			} else
			// Check if But Coins button
			if (position == BUY_COINS_POSITION) {
				mLetter.setVisibility(View.INVISIBLE);
				mImage.setImageResource(R.drawable.button_buy_coins);
			} else {
				mLetter.setText(list.get(position).Letter);
			}

			return v;
		}

		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public LetterInfo getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}
	}	
	
	/**
	 * Take current screen screenshot
	 * @return the image
	 */
	private Bitmap takeScreenshot() {
	   View rootView = findViewById(R.id.content).getRootView();
	   rootView.setDrawingCacheEnabled(true);
	   int statusBarHeight = Utility.getStatusBarHeight(this);
	   Bitmap bmp = Bitmap.createBitmap(rootView.getDrawingCache(), 0, statusBarHeight,
			   rootView.getDrawingCache().getWidth(),
			   rootView.getDrawingCache().getHeight()- statusBarHeight);			   
	   return bmp;
	}	
	
	// ----------------------------------------------------------------
		//   IN APP PURCHASE HANDLING
		// ----------------------------------------------------------------
		
		// Listener that's called when we finish querying the items and subscriptions we own
	    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
	        @Override
			public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
	            if (result.isFailure()) {
	                complain("Failed to query inventory: " + result);
	                return;
	            }

	            // Check for coins delivery -- if we own coins, we should the coin status immediately
	            Purchase coinsPurchase = inventory.getPurchase(SKU_750);
	            if (coinsPurchase != null && verifyDeveloperPayload(coinsPurchase)) {
	                mHelper.consumeAsync(inventory.getPurchase(SKU_750), mConsumeFinishedListener);
	                return;
	            }
	         // Check for coins delivery -- if we own coins, we should the coin status immediately
	            coinsPurchase = inventory.getPurchase(SKU_2000);
	            if (coinsPurchase != null && verifyDeveloperPayload(coinsPurchase)) {
	                mHelper.consumeAsync(inventory.getPurchase(SKU_2000), mConsumeFinishedListener);
	                return;
	            }
	         // Check for coins delivery -- if we own coins, we should the coin status immediately
	            coinsPurchase = inventory.getPurchase(SKU_5000);
	            if (coinsPurchase != null && verifyDeveloperPayload(coinsPurchase)) {
	                mHelper.consumeAsync(inventory.getPurchase(SKU_5000), mConsumeFinishedListener);
	                return;
	            }
	         // Check for coins delivery -- if we own coins, we should the coin status immediately
	            coinsPurchase = inventory.getPurchase(SKU_15000);
	            if (coinsPurchase != null && verifyDeveloperPayload(coinsPurchase)) {
	                mHelper.consumeAsync(inventory.getPurchase(SKU_15000), mConsumeFinishedListener);
	                return;
	            }
	            updateUi();
	            //setWaitScreen(false);          
	        }
	    };
		
	    
	    /** Verifies the developer payload of a purchase. */
	    boolean verifyDeveloperPayload(Purchase p) {
	        String payload = p.getDeveloperPayload();
	        
	        /*
	         * TODO: verify that the developer payload of the purchase is correct. It will be
	         * the same one that you sent when initiating the purchase.
	         * 
	         * WARNING: Locally generating a random string when starting a purchase and 
	         * verifying it here might seem like a good approach, but this will fail in the 
	         * case where the user purchases an item on one device and then uses your app on 
	         * a different device, because on the other device you will not have access to the
	         * random string you originally generated.
	         *
	         * So a good developer payload has these characteristics:
	         * 
	         * 1. If two different users purchase an item, the payload is different between them,
	         *    so that one user's purchase can't be replayed to another user.
	         * 
	         * 2. The payload must be such that you can verify it even when the app wasn't the
	         *    one who initiated the purchase flow (so that items purchased by the user on 
	         *    one device work on other devices owned by the user).
	         * 
	         * Using your own server to store and verify developer payloads across app
	         * installations is recommended.
	         */
	        
	        return true;
	    }
	    
	    // Callback for when a purchase is finished
	    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
	        @Override
			public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
	            if (result.isFailure()) {
	                complain("Error purchasing: " + result);
	                //setWaitScreen(false);
	                return;
	            }
	            if (!verifyDeveloperPayload(purchase)) {
	                complain("Error purchasing. Authenticity verification failed.");
	                //setWaitScreen(false);
	                return;
	            }

	            if (purchase.getSku().equals(SKU_750) || purchase.getSku().equals(SKU_2000)
	            		|| purchase.getSku().equals(SKU_5000) || purchase.getSku().equals(SKU_15000)) {
	                // bought Coins, So consume it.
	                mHelper.consumeAsync(purchase, mConsumeFinishedListener);
	            }            
	        }
	    };

	    // Called when consumption is complete
	    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
	        @Override
			public void onConsumeFinished(Purchase purchase, IabResult result) {
	            int coins = 0;            
	            // We know this is the "gas" sku because it's the only one we consume,
	            // so we don't check which sku was consumed. If you have more than one
	            // sku, you probably should check...
	            if (result.isSuccess()) {
	            	try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// ignored
					}
	            	if(purchase.getSku().equals(SKU_750))
	                	coins = 750;
	                else if(purchase.getSku().equals(SKU_2000))
	                	coins = 2000;
	                else if(purchase.getSku().equals(SKU_5000))
	                	coins = 5000;
	                else if(purchase.getSku().equals(SKU_15000))
	                	coins = 15000;
	            	LogFlurry("Get_Coins","Purchased",String.valueOf(coins));
	            	
	                // successfully consumed, so we apply the effects of the item in our
	                // game world's logic, which in our case means filling the gas tank a bit
	                _appPrefs.saveCoins(_appPrefs.getCoins()+coins);
	                alert("You added "+ coins+ " Coins. Your total is now " + String.valueOf(_appPrefs.getCoins()));
	            }
	            else {
	                complain("Error while consuming: " + result);
	            }
	            updateUi();
	            //setWaitScreen(false);
	        }
	    };
	    
	    /***
	     * updates UI to reflect model 
	     */
	    public void updateUi() {
	        // Nothing to update, since the coins are not displayed     
	    }

		/**
		 * Show the alert message for Error
		 * @param message
		 */
	    void complain(String message) {
	        //alert("Error: " + message);
	    }

	    /**
	     * Show the alert message for information
	     * @param message
	     */
	    void alert(String message) {
	        AlertDialog.Builder bld = new AlertDialog.Builder(this);
	        bld.setMessage(message);
	        bld.setNeutralButton("OK", null);
	        bld.create().show();
	    }	
	
}
