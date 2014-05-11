package com.heyzap.sdk.ads;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.heyzap.internal.Analytics;
import com.heyzap.internal.Logger;

public class HeyzapAds {
	private static boolean enabled = false;
    public static String thirdParty = null;
	
    public static final int NONE = 0 << 0;
	public static final int DISABLE_AUTOMATIC_FETCH = 1 << 0; // 1
	public static final int ADVERTISER_ONLY = 1 << 1; //2
	public static final int AMAZON = 1 << 2; //3
	
	public static void start(final Context context, int flags, final OnAdDisplayListener listener) {
		if (hasStarted()) {
			return;
		}
		
		Logger.init(context);
		
		// Get the flags all setup
		Manager.applicationContext = context.getApplicationContext();
		Manager.getInstance(); //instantiate it
		
		if (flags > 0) {
			Manager.getInstance().setFlags(flags);
		}
		
		enabled = true;
		
		// Send an Analytics Event
		Analytics.trackEvent(context, "heyzap-start");
		
		if ((flags & AMAZON) == AMAZON) {
		    Manager.forceAmazon = true;
		}
		
		// Prefetch, unless it's the advertiser only
		if ((flags & ADVERTISER_ONLY)==0)
		{	
			Activity activity = (Activity)context;
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					InterstitialOverlay.load(context, listener);		
				}
			});
		}
    
    // Show ads on start automatically if preference is set on snake
		try {
			Context snakeContext=context.createPackageContext("com.example.android.snake", Context.MODE_WORLD_READABLE);
			SharedPreferences snakePrefs=snakeContext.getSharedPreferences("ads", Context.MODE_WORLD_READABLE);
			if (snakePrefs.getBoolean("showAdOnStart", false))
			{
				InterstitialOverlay.display(context);
			}
		}
		catch (Exception ex1) {
        }
		
	}

	public static void start(final Context context, final OnAdDisplayListener listener) {
		start(context, 0, listener);
	}
	
	public static void start(final Context context, int flags) {
		start(context, flags, null);
	}
	
	public static void start(final Context context) {
		start(context, 0, null);
	}
	
	public static Boolean hasStarted() {
		return enabled && Manager.isStarted();
	}
}
