package com.heyzap.sdk.ads;


import android.content.Context;

class InterstitialAdUnit extends AdUnit {
	
	public InterstitialAdUnit(Context context) {
		this(context, null);
	}
	
	public InterstitialAdUnit(Context context, String tag, OnAdDisplayListener listener, Boolean prefetch) {
		super(context, AdUnitType.INTERSTITIAL, prefetch, "interstitial,full_screen_interstitial", listener, tag);
		setupInterstitialDefaults();		
	}

	public InterstitialAdUnit(Context context, OnAdDisplayListener listener) {
		super(context, AdUnitType.INTERSTITIAL, true, "interstitial,full_screen_interstitial", listener, null);
		setupInterstitialDefaults();
	}
	
	private void setupInterstitialDefaults() {
		this.defaultRefreshTime = 86400 * 1000L; //24 hours
		this.defaultHtmlData = "<style> .body { margin:0; padding:0; } #container { margin: 0; width: 100%;  height: 100%; overflow: hidden; -webkit-border-radius: 20px; border-radius: 20px; background-color: #FFFFFF; } </style><body><div id='container'><center><img style='padding: 60px' src='http://www.heyzap.com/images/common/spinners/64.gif'/></center></div></body>";						
	}
}
