package com.hightechsoftware.whatstheword;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.opengl.Visibility;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;
import com.hightechsoftware.whatstheword.car.R;
import com.hightechsoftware.whatstheword.db.DBHelper;
import com.hightechsoftware.whatstheword.inappbilling.util.IabHelper;
import com.hightechsoftware.whatstheword.inappbilling.util.IabHelper.QueryInventoryFinishedListener;
import com.hightechsoftware.whatstheword.inappbilling.util.IabResult;
import com.hightechsoftware.whatstheword.inappbilling.util.Inventory;
import com.hightechsoftware.whatstheword.inappbilling.util.Purchase;
import com.hightechsoftware.whatstheword.utility.AppPreferences;
import com.hightechsoftware.whatstheword.utility.Utility;

public class StartActivity extends Activity {
	// Screen resources
	private Button btnPlay;
//	private TextView btnGetCoins;
	private CheckBox checkboxAds;
	private Typeface face;
	private RelativeLayout rlTopBar;
	private RelativeLayout rlPopupGetCoins;
//	private RelativeLayout rlPopupShare;
	private RelativeLayout rlPopupSettings;
	private RelativeLayout rlQuoteMessage;

//	private ImageView ivShare;
	private ImageView ivSettings;

	private TextView tvPopupSettingsClose;
	private TextView tvPopupSettingsTitle;
	private TextView tvPopupSoundOff;
	private TextView tvPopupQuoteOff;
	private TextView tvPopupResetData;
	private TextView tvRateApp;
	private TextView tvLevel;

//	private TextView tvPopupShareClose;
//	private TextView tvPopupShareTitle;
//	private TextView tvShareFacebook;
//	private TextView tvShareTwitter;

	private TextView tvPopupCoinsClose;
	private TextView tvPopupCoinsTitle;
	private TextView tvPopup750Coins;
	private TextView tvPopupDownloadCoins;
	
	private TextView tvSuccessMessage;
	private TextView tvSuccessSubMessage;
	// Popup Buttons/Layouts
	private RelativeLayout rlResetData;
	private RelativeLayout rlSoundStatus;
	private RelativeLayout rlQuoteStatus;
	private RelativeLayout rlRateApp;
//	private RelativeLayout rlShareFacebook;
//	private RelativeLayout rlShareTwitter;
	private RelativeLayout rlBuy750Coins;

	//Quote Message
	private String[] mSuccessMsg;
	private String[] mSuccessSubMsg;
	
	// Members and Parameters
//	private int mFacebookCoins = 0;
//	private int mTwitterCoins = 0;
	private Animation mAnimGrow;
	private boolean mIsPopupVisible;
	private AppPreferences _appPrefs;
	private MediaPlayer mpButtonClick;
	
	RelativeLayout relativeLayout;
	LinearLayout adLinearLayout;

	// Need handler for callbacks to the UI thread
	final Handler mHandler = new Handler();

	// Google Play in app purchase
	private static String base64EncodedPublicKey = "";
	private IabHelper mHelper;
	private int mPurchaseCoins = 0;
	// Used for testing by google play
	// android.test.purchased, android.test.cancelled, 
	// android.test.refunded, android.test.item_unavailable
	static final String REMOVEADS = "4pics_removeads";
	
	private static String APP_PNAME = "com.hightechsoftware.whatstheword";
	
	private static int SPLASH_TIME_OUT = 7000;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		LoadResources();
		LoadListeners();
		
		//Show random quote
		if(_appPrefs.getQuote())			
			showRandomSuccessMsg();
		
//		checkboxAds.setVisibility(View.INVISIBLE);
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		FlurryAgent.onStartSession(this, getString(R.string.flurry));		
	}
	 
	@Override
	protected void onStop()
	{
		super.onStop();		
		FlurryAgent.onEndSession(this);
	}
	
	@Override
	public void onBackPressed() {
		if (!mIsPopupVisible) {
			super.onBackPressed();			
			finish();
		} else {			
			mIsPopupVisible = false;
			rlPopupGetCoins.setVisibility(View.INVISIBLE);
//			rlPopupShare.setVisibility(View.INVISIBLE);
			rlPopupSettings.setVisibility(View.INVISIBLE);
		}
		PlayButtonClick();
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
	
	/*
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    // TODO Auto-generated method stub
	    if (event.getAction() == KeyEvent.ACTION_DOWN) {
	        switch (keyCode) {
	        case KeyEvent.KEYCODE_HOME:
	        	LogFlurry("Button_Click","Start Activity","Home");
	        }
	    }

	    return super.onKeyDown(keyCode, event);
	}
*/
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		if (mHelper != null) mHelper.dispose();
		mHelper = null;
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mIsPopupVisible) {
			mIsPopupVisible = false;
			rlPopupGetCoins.setVisibility(View.INVISIBLE);
//			rlPopupShare.setVisibility(View.INVISIBLE);
			rlPopupSettings.setVisibility(View.INVISIBLE);
		}
		
		// TODO uncomment
		// Making that paper - Earn/buy 1000 coins
//		if (_appPrefs.getCoins() >= 1000 && !_appPrefs.getAchievement(getString(R.string.achievements_Make_Money1)))
//		{
//			_appPrefs.setAchievement(getString(R.string.achievements_Make_Money1),true);
//			Achievements.unlockAchievement(this, getString(R.string.achievements_Make_Money1));
//		}
//		// Making that paper2 - Earn/buy 1500 coins
//		if (_appPrefs.getCoins() >= 1500 && !_appPrefs.getAchievement(getString(R.string.achievements_Make_Money2)))
//		{
//			_appPrefs.setAchievement(getString(R.string.achievements_Make_Money2),true);
//			Achievements.unlockAchievement(this, getString(R.string.achievements_Make_Money2));
//		}
//		// You so rich - Earn/buy 2500 coins
//		if (_appPrefs.getCoins() >= 2500 && !_appPrefs.getAchievement(getString(R.string.achievements_Make_Money3)))
//		{
//			_appPrefs.setAchievement(getString(R.string.achievements_Make_Money3),true);
//			Achievements.unlockAchievement(this, getString(R.string.achievements_Make_Money3));
//		}
	}

	@Override
	protected void onPause() {
		super.onPause();		
	}

	/**
	 * Load all the resources the activity needs
	 */
	private void LoadResources() {
		_appPrefs = new AppPreferences(getApplicationContext());
		face = Typeface.createFromAsset(getAssets(), "Chuck.ttf");
				
		//Quote Message
		rlQuoteMessage = (RelativeLayout) findViewById(R.id.layout_quote);
		/*
		if(_appPrefs.getQuote())
			rlQuoteMessage.setVisibility(View.VISIBLE);
		else
			rlQuoteMessage.setVisibility(View.GONE);
		*/
		mSuccessMsg = this.getResources().getStringArray(R.array.quote_message);
		mSuccessSubMsg = this.getResources().getStringArray(R.array.quote_person);
		tvSuccessMessage = (TextView) findViewById(R.id.tvSuccessQuoteMessage);
		//tvSuccessMessage.setTypeface(face);
		tvSuccessSubMessage = (TextView) findViewById(R.id.tvSuccessQuotePerson);
				
		// Google Play in app purchase
		mHelper = new IabHelper(this, base64EncodedPublicKey);
		
		// Buttons/Layouts/TextViews/etc.
		mpButtonClick = MediaPlayer.create(this, R.raw.button);
		
		mAnimGrow = AnimationUtils.loadAnimation(this, R.anim.grow_from_middle);
//		mFacebookCoins = this.getResources().getInteger(
//				R.integer.share_facebook);
//		mTwitterCoins = this.getResources().getInteger(R.integer.share_twitter);

		rlTopBar = (RelativeLayout) findViewById(R.id.layout_top_bar);
		Animation slideDown = Utility.setLayoutAnim_slidedown();
		rlTopBar.startAnimation(slideDown);
		btnPlay = (Button) findViewById(R.id.button_play);
		btnPlay.setTypeface(face);
		checkboxAds = (CheckBox) findViewById(R.id.checkbox_ads);

//		ivShare = (ImageView) findViewById(R.id.ivShare);
		ivSettings = (ImageView) findViewById(R.id.ivSettings);

		// Helper layout
		mIsPopupVisible = false;
		rlPopupGetCoins = (RelativeLayout) findViewById(R.id.layout_getcoins);
		rlPopupGetCoins.setVisibility(View.INVISIBLE);
//		rlPopupShare = (RelativeLayout) findViewById(R.id.layout_share);
//		rlPopupShare.setVisibility(View.INVISIBLE);
		rlPopupSettings = (RelativeLayout) findViewById(R.id.layout_settings);
		rlPopupSettings.setVisibility(View.INVISIBLE);

		// Level 		
		tvLevel = (TextView) findViewById(R.id.tvLevel);
		tvLevel.setTypeface(face);
		tvLevel.setText(String.valueOf(_appPrefs.getWordCompleted()));
				
		// Get coins popup
		rlBuy750Coins = (RelativeLayout) findViewById(R.id.layout_750_coins);

		
		tvPopupCoinsClose = (TextView) findViewById(R.id.tvPopupCoinsClose);
		tvPopupCoinsClose.setTypeface(face);
		tvPopupCoinsTitle = (TextView) findViewById(R.id.tvPopupCoinsTitle);
		tvPopupCoinsTitle.setTypeface(face);

		tvPopup750Coins = (TextView) findViewById(R.id.tv750Coins);
		tvPopup750Coins.setTypeface(face);
		tvPopupDownloadCoins = (TextView) findViewById(R.id.tvDownloadCoinsTitle);
		tvPopupDownloadCoins.setTypeface(face);

		// SHARE popup
//		tvPopupShareClose = (TextView) findViewById(R.id.tvPopupShareClose);
//		tvPopupShareClose.setTypeface(face);
//		tvPopupShareTitle = (TextView) findViewById(R.id.tvPopupShareTitle);
//		tvPopupShareTitle.setTypeface(face);
//		tvShareFacebook = (TextView) findViewById(R.id.tvShareFacebook);
//		tvShareFacebook.setTypeface(face);
//		tvShareTwitter = (TextView) findViewById(R.id.tvShareTwitter);
//		tvShareTwitter.setTypeface(face);

		// Settings popup
		tvPopupSettingsClose = (TextView) findViewById(R.id.tvPopupSettingsClose);
		tvPopupSettingsClose.setTypeface(face);
		tvPopupSettingsTitle = (TextView) findViewById(R.id.tvPopupSettingsTitle);
		tvPopupSettingsTitle.setTypeface(face);
		tvPopupSoundOff = (TextView) findViewById(R.id.tvSoundOff);
		tvPopupSoundOff.setTypeface(face);
		tvPopupQuoteOff = (TextView) findViewById(R.id.tvQuoteOff);
		tvPopupQuoteOff.setTypeface(face);
		tvPopupResetData = (TextView) findViewById(R.id.tvResetData);
		tvPopupResetData.setTypeface(face);
		tvRateApp = (TextView) findViewById(R.id.tvRateApp);
		tvRateApp.setTypeface(face);
		
		// Popup Button/Layout
		rlResetData = (RelativeLayout) findViewById(R.id.layout_reset_data);
		rlSoundStatus = (RelativeLayout) findViewById(R.id.layout_sound_off);
		rlQuoteStatus = (RelativeLayout) findViewById(R.id.layout_quote_off);
		rlRateApp = (RelativeLayout) findViewById(R.id.layout_rate_me);
		
		if(_appPrefs.getSound())
			tvPopupSoundOff.setText(R.string.settings_sound_off_title);
		else
			tvPopupSoundOff.setText(R.string.settings_sound_on_title);
		
		if(_appPrefs.getQuote())
			tvPopupQuoteOff.setText(R.string.settings_quote_off_title);
		else
			tvPopupQuoteOff.setText(R.string.settings_quote_on_title);
		
//		rlShareFacebook = (RelativeLayout) findViewById(R.id.layout_share_facebook);
//		rlShareTwitter = (RelativeLayout) findViewById(R.id.layout_share_twitter);
	}

	/**
	 * Log the flurry actions
	 * @param event
	 * @param Activity
	 * @param action
	 */
	private void LogFlurry(String event, String Activity, String action)
	{
		Map<String, String> params = new HashMap<String, String>();
		params.put(Activity, action);
		FlurryAgent.logEvent(event,params);
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
		                moreSkus.add(REMOVEADS); 
		 
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
		
		// Add the listener to open the Intro Activity
		btnPlay.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {				
				PlayButtonClick();
				Intent intent = new Intent(StartActivity.this,
						PlayActivity.class);
				startActivity(intent);
			}
		});

		// Add the listener to open the Buy Coins POPUP
//		btnGetCoins.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				LogFlurry("Get_Coins","Start Activity","Initiated");
//				PlayButtonClick();
//				mIsPopupVisible = true;
//				rlPopupGetCoins.setVisibility(View.VISIBLE);
//				rlPopupGetCoins.startAnimation(mAnimGrow);				
//			}
//		});
		
		checkboxAds.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{
		    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
		    {
		        if ( isChecked )
		        {
					PlayButtonClick();
					mIsPopupVisible = true;
					rlPopupGetCoins.setVisibility(View.VISIBLE);
					rlPopupGetCoins.startAnimation(mAnimGrow);		
		        }

		    }
		});
		// Add the listener to open the SHARE POPUP
//		ivShare.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				LogFlurry("Share_Game","Start Activity","Initiated");
//				PlayButtonClick();
//				mIsPopupVisible = true;
//				rlPopupShare.setVisibility(View.VISIBLE);
//				rlPopupShare.startAnimation(mAnimGrow);
//			}
//		});

		// Add the listener to open the SETTINGS POPUP
		ivSettings.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {				
				PlayButtonClick();
				mIsPopupVisible = true;
				rlPopupSettings.setVisibility(View.VISIBLE);
				rlPopupSettings.startAnimation(mAnimGrow);
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
				mHelper.launchPurchaseFlow(StartActivity.this, REMOVEADS, 
						mPurchaseCoins, mPurchaseFinishedListener);				
			}
		});

//		tvPopupShareClose.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				LogFlurry("Share_Game","Start Activity","Cancelled");
//				PlayButtonClick();
//				mIsPopupVisible = false;
//				rlPopupShare.setVisibility(View.INVISIBLE);
//			}
//		});
		
		
		// Twitter share					
//		rlShareTwitter.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				LogFlurry("Share_Game","Start Activity","Confirmed");
//				LogFlurry("Share_Game","Application","Twitter");
//				PlayButtonClick();
//				String text = getString(R.string.share_twitter_text_sub) + " "
//						+ getString(R.string.app_store_location);
//				Intent tweet = new Intent(Intent.ACTION_VIEW);
//				tweet.setData(Uri.parse("http://twitter.com/?status="
//						+ Uri.encode(text)));
//				startActivity(tweet);
//				mIsPopupVisible = false;
//				rlPopupShare.setVisibility(View.INVISIBLE);
//				//TapjoyConnect.getTapjoyConnectInstance().awardTapPoints(
//				//		mTwitterCoins, StartActivity.this);
//				//_appPrefs.saveCoinsDiff(mTwitterCoins);
//				_appPrefs.saveCoins(_appPrefs.getCoins() + mTwitterCoins);
//								
//				// Socializer - share the app via twitter or facebook
//				if (!_appPrefs.getAchievement(getString(R.string.achievements_Socializer)))
//				{
//					_appPrefs.setAchievement(getString(R.string.achievements_Socializer),true);
//				}
//			}
//		});

		// Facebook Share
//		rlShareFacebook.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				LogFlurry("Share_Game","Start Activity","Confirmed");
//				LogFlurry("Share_Game","Application","Facebook");
//				PlayButtonClick();
//				String text = getString(R.string.share_facebook_text_sub) + " "
//						+ getString(R.string.app_store_location);
//				Utility.ShareViaFacebook(StartActivity.this,
//						getString(R.string.share_facebook_subject), text);
//				mIsPopupVisible = false;
//				rlPopupShare.setVisibility(View.INVISIBLE);	
//				//Save the coins. They are synch when checking leadership
//				//_appPrefs.saveCoinsDiff(mFacebookCoins);
//				_appPrefs.saveCoins(_appPrefs.getCoins()+mFacebookCoins);
//				// Socializer - share the app via twitter or facebook
//				if (!_appPrefs.getAchievement(getString(R.string.achievements_Socializer)))
//				{
//					_appPrefs.setAchievement(getString(R.string.achievements_Socializer),true);					
//				}
//			}
//		});

		// Popup Settings close
		tvPopupSettingsClose.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {				
				PlayButtonClick();
				mIsPopupVisible = false;
				rlPopupSettings.setVisibility(View.INVISIBLE);
			}
		});

		// Reset App Data
		rlResetData.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				LogFlurry("Reset","Start Activity","Reset game data");
				PlayButtonClick();
				ResetData();
				
				/*
				DBHelper helper = new DBHelper(getApplicationContext());
				helper.open();
				helper.resetDB();
				helper.close();*/

				//mIsPopupVisible = false;
				//rlPopupSettings.setVisibility(View.INVISIBLE);
			}
		});
		
		// Manage Sound 
		rlSoundStatus.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {				
				PlayButtonClick();				
				if(_appPrefs.getSound())
				{
					_appPrefs.soundOff();
					tvPopupSoundOff.setText(R.string.settings_sound_on_title);
				}
				else
				{
					_appPrefs.soundOn();
					tvPopupSoundOff.setText(R.string.settings_sound_off_title);					
				}
				//mIsPopupVisible = false;
				//rlPopupSettings.setVisibility(View.INVISIBLE);
			}
		});
		/*
		if(_appPrefs.getSound())
			tvPopupSoundOff.setText(R.string.settings_sound_off_title);
		else
			tvPopupSoundOff.setText(R.string.settings_sound_on_title);
		*/
		// Manage Sound 
		rlQuoteStatus.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {				
				PlayButtonClick();				
				if(_appPrefs.getQuote())
				{
					_appPrefs.quoteOff();
					tvPopupQuoteOff.setText(R.string.settings_quote_on_title);
				}
				else
				{
					_appPrefs.quoteOn();
					tvPopupQuoteOff.setText(R.string.settings_quote_off_title);					
				}
				//mIsPopupVisible = false;
				//rlPopupSettings.setVisibility(View.INVISIBLE);
			}
		});
		/*		
		if(_appPrefs.getSound())
			tvPopupQuoteOff.setText(R.string.settings_quote_off_title);
		else
			tvPopupQuoteOff.setText(R.string.settings_quote_on_title);
			*/
		
		// Rate This App
		rlRateApp.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {				
				PlayButtonClick();				
				startActivity(new Intent(Intent.ACTION_VIEW, 
						Uri.parse("market://details?id=" + getPackageName())));
			}
		});
	}
	
	/**
	 * Play button click if sound is enabled
	 */	
	private void PlayButtonClick()
	{
		if(_appPrefs.getSound())
			mpButtonClick.start();
	}
	
	/**
	 * Reset the Data in an asynch task
	 */	
	private void ResetData()
	{
		new AsyncTask<Integer, Integer, Boolean>()
        {
            ProgressDialog progressDialog;

            @Override
            protected void onPreExecute()
            {
                /*
                 * This is executed on UI thread before doInBackground(). It is
                 * the perfect place to show the progress dialog.
                 */
                progressDialog = ProgressDialog.show(StartActivity.this, 
                		getString(R.string.dialog_reset_title),
                        getString(R.string.dialog_reset_message));
            }

            @Override
            protected Boolean doInBackground(Integer... params)
            {
                if (params == null)
                {
                    return false;
                }
                try
                {
                    /*
                     * This is run on a background thread, so we can sleep here
                     * or do whatever we want without blocking UI thread. A more
                     * advanced use would download chunks of fixed size and call
                     * publishProgress();
                     */
                    //Thread.sleep(params[0]);
                    // HERE I'VE PUT ALL THE FUNCTIONS THAT WORK FOR ME
                    DBHelper helper = new DBHelper(getApplicationContext());
    				helper.open();
    				helper.resetDB();
    				helper.close();
    				
    				// set word completed to 1
    				_appPrefs.setWordCompleted(1);
    				
    				
    				StartActivity.this.runOnUiThread(new Runnable(){
    				    public void run(){
    				    	tvLevel.setText(String.valueOf(_appPrefs.getWordCompleted()));
    				    }
    				});
                }
                catch (Exception e)
                {
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
            protected void onPostExecute(Boolean result)
            {
                progressDialog.dismiss();
                /*
                 * Update here your view objects with content from download. It
                 * is save to dismiss dialogs, update views, etc., since we are
                 * working on UI thread.
                 */
                AlertDialog.Builder b = new AlertDialog.Builder(StartActivity.this);
                b.setTitle(android.R.string.dialog_alert_title);
                if (result)
                {
                    b.setMessage(getString(R.string.dialog_reset_success));
                }
                else
                {
                    b.setMessage(getString(R.string.dialog_reset_nosuccess));
                }
                b.setPositiveButton(getString(android.R.string.ok),
                        new DialogInterface.OnClickListener()
                        {

                            @Override
                            public void onClick(DialogInterface dlg, int arg1)
                            {
                                dlg.dismiss();
                            }
                        });
                b.create().show();
            }
        }.execute(2000);
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
            Purchase coinsPurchase = inventory.getPurchase(REMOVEADS);
            if (coinsPurchase != null && verifyDeveloperPayload(coinsPurchase)) {
                mHelper.consumeAsync(inventory.getPurchase(REMOVEADS), mConsumeFinishedListener);
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

            if (purchase.getSku().equals(REMOVEADS)) {
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
            	if(purchase.getSku().equals(REMOVEADS)) {
                	coins = 750;
                	_appPrefs.setShowAds(false);
            	}
            	LogFlurry("Remove Ads","Purchased", "Remove ads");
            	
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
    
    /**
	 * Show the success message from random success messages
	 * 
	 * @return
	 */
	private void showRandomSuccessMsg() {
		Random rnd = new Random();
		
		
		int position = rnd.nextInt(mSuccessMsg.length);
		tvSuccessMessage.setText(mSuccessMsg[position]);
		tvSuccessSubMessage.setText(mSuccessSubMsg[position]);
		tvSuccessMessage.setVisibility(View.VISIBLE);
		tvSuccessSubMessage.setVisibility(View.VISIBLE);
		
		//Animation slideDown = Utility.setLayoutAnim_slidedown();		
		//Animation slideDown = Utility.setLayoutAnim_slideUp();
		Animation slideDown = AnimationUtils.loadAnimation(this, R.anim.scale_up);
		rlQuoteMessage.startAnimation(slideDown);
		rlQuoteMessage.setVisibility(View.VISIBLE);	
			
	
		//Hide message after some time
		new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
            	Animation slideDown = AnimationUtils.loadAnimation(StartActivity.this, R.anim.scale_down);
        		rlQuoteMessage.startAnimation(slideDown);
                // This method will be executed once the timer is over            	
            	rlQuoteMessage.setVisibility(View.GONE);
            }
        }, SPLASH_TIME_OUT);
	}
}
