package com.heyzap.sdk.ads;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.os.Handler;
import android.os.Looper;

import com.heyzap.http.RequestParams;
import com.heyzap.internal.APIClient;
import com.heyzap.internal.APIResponseHandler;
import com.heyzap.internal.Logger;
import com.heyzap.internal.Utils;

public class Manager {
	
	public static Context applicationContext;
	
	private ArrayList<AdUnit> adUnits;
	public long lastClickedTime = 0;
	public static long maxClickDifference = 1000;
	
	public static final String AD_SERVER = "http://ads.heyzap.com/in_game_api/ads";
	public static final String ACTION_URL_PLACEHOLDER = "market://details?id=%s&referrer=%s";
	public static final String ACTION_URL_REFERRER = "utm_source%3Dheyzap%26utm_medium%3Dmobile%26utm_campaign%3Dheyzap_ad_network";

	public static final Handler handler = new Handler(Looper.getMainLooper());
	
	public static final String FIRST_RUN_KEY = "HeyzapAdsFirstRun";
    public static final String ADVERTISED_PACKAGES_KEY = "AdvertisedPackages";
	
    public static HashMap<String, String> gameImpressionMap = null;
    
	public static Boolean started = false;
	private int flags = 0;
	
	public static boolean forceAmazon = false;

    private static List<String> localPackages = null;
	
	// Standard Singleton Creation
	private Manager() {
		Logger.log("Heyzap Ad Manager started.");

		final SharedPreferences prefs = Manager.applicationContext.getSharedPreferences(Manager.FIRST_RUN_KEY, 0);
		SharedPreferences.Editor editor = prefs.edit();

		final boolean firstRun = !prefs.getBoolean("ran_once", false);
		
		if (firstRun) {
			Logger.log("Running first run tasks");
			editor.putBoolean("ran_once", true);
			editor.commit();
		}
		
		Manager.started = true;
	};
	
	static public Boolean isStarted() {
		return Manager.started;
	}
	
	public void registerInstall(final String gamePackage, final String impressionId) {
		if (Manager.applicationContext == null || impressionId == null) {
			return;
		}
		
		// Tell ad server we did an install
		new Thread(new Runnable() {
			
			public void run() {
				RequestParams reqParameters = new RequestParams();

				if (impressionId != null) {
				    reqParameters.put("impression_id", impressionId);
				}
				
				if (Utils.isAmazon()) {
				    reqParameters.put("platform", "amazon");
				} else {
				    reqParameters.put("platform", "android");
				}

				APIClient.post(Manager.applicationContext, AD_SERVER + "/track_impression_event", reqParameters,  new APIResponseHandler() {
                    @Override
                    public void onSuccess(final JSONObject response) {
                        try {
                            if (response.getInt("status") == 200) {
                                if (impressionId != null) {
                                    Logger.log("(INSTALL): " + impressionId);
                                }
                                
                            	deleteGameImpression(gamePackage);
                            } else {
                            	
                            }
                        } catch (Exception e) {
                        	
                        }
                    }
                });
				
			}
		}).start();
	}
    
    /**
     * Checks whether a package has been advertised before
     * and if so returns the impression for the ad.
     * 
     * @param gamePackage
     * @return
     */
    public String getGameImpression(String gamePackage) {
        if (Manager.applicationContext == null) {
            return null;
        }

        if (gameImpressionMap != null && gameImpressionMap.containsKey(gamePackage)) {
            return gameImpressionMap.get(gamePackage);
        }

        final SharedPreferences prefs = Manager.applicationContext.getSharedPreferences(Manager.ADVERTISED_PACKAGES_KEY, Context.MODE_PRIVATE);
        return prefs.getString(gamePackage, null);
    }
    
	/**
	 * Store the impression from a fetch for this game-package.
	 * This is cached in a static variable but also persisted to shared-preferences.
	 * 
	 * @param gamePackage
	 * @param impression
	 */
	public void setGameImpression(String gamePackage, String impression) {
        if (Manager.applicationContext == null) {
            return;
        }

        // save to shared preferences
        final SharedPreferences prefs = Manager.applicationContext.getSharedPreferences(Manager.ADVERTISED_PACKAGES_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(gamePackage, impression);
        editor.commit();
        
        if (gameImpressionMap == null) {
            gameImpressionMap = new HashMap<String, String>();
        }
        gameImpressionMap.put(gamePackage, impression);
	}
    
    /**
     * Deletes a game and it's impression ID from storage.
     * 
     * @param gamePackage
     * @return
     */
    public void deleteGameImpression(String gamePackage) {
        if (Manager.applicationContext == null) {
            return;
        }
        
        if (gameImpressionMap != null && gameImpressionMap.containsKey(gamePackage)) {
            gameImpressionMap.remove(gamePackage);
        }
        
        final SharedPreferences prefs = Manager.applicationContext.getSharedPreferences(Manager.ADVERTISED_PACKAGES_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(gamePackage);
        editor.commit();
    }
	
	public List<String> getLocalPackages() {
		if (Manager.applicationContext == null) {
			return null;
		}
		
		if (localPackages != null) {
		    return localPackages;
		}
		
		List<PackageInfo> packages = Manager.applicationContext.getPackageManager()
				.getInstalledPackages(0);
		
		final List<String> packageNames = new ArrayList<String>();
		
		for (PackageInfo packageInfo : packages) {
			if (packageInfo.packageName.startsWith("android.") || packageInfo.packageName.startsWith("com.google.android") || packageInfo.packageName.startsWith("com.android")) {
				continue;
			}
			
			packageNames.add(packageInfo.packageName);
		}
		
		localPackages = packageNames;
		
		return packageNames;
	}
	
	public void installHeyzap(AdUnit referringAdUnit) {
		// if a newish Heyzap is installed, launch game details.
		// if an older Heyzap is installed, just launch Heyzap.
		// if Heyzap isn't installed, open it in the market.
		
		String gamePackage = referringAdUnit.getGamePackage() == null ? "null" : referringAdUnit.getGamePackage();
		if (Utils.heyzapIsInstalled(Manager.applicationContext)) {
			Intent i = new Intent(Intent.ACTION_MAIN); 
			i.setAction(Utils.HEYZAP_PACKAGE);
			i.putExtra("from_ad_for_game_package", gamePackage);
			i.putExtra("packageName", Manager.applicationContext.getPackageName());
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			if (referringAdUnit.getGamePackage() != null) {
				i.setComponent(new ComponentName(Utils.HEYZAP_PACKAGE, Utils.HEYZAP_PACKAGE + ".activity.GameDetails"));
				i.putExtra("game_package", referringAdUnit.getGamePackage());
			} else {
				i.setComponent(new ComponentName(Utils.HEYZAP_PACKAGE, Utils.HEYZAP_PACKAGE + ".activity.CheckinHub"));
			}

			Manager.applicationContext.startActivity(i);
		} else {
			Utils.installHeyzap(Manager.applicationContext, String.format("action=ad_heyzap_logo&game_package=%s", gamePackage));
		}
	}
	
	// Flags
	
	public void setFlags(int flags) {
		this.flags = flags;
	}
	
	public Boolean isFlagEnabled(int flag) {
		return (this.flags & flag) > 0;
	}
	
	/* Singleton */

	public synchronized static Manager getInstance() {
	    if (ref == null) {
	        ref = new Manager();
	    }
	    
	    return ref;
	}
		
	public Object clone() {
		return null;
	}
	
	private static volatile Manager ref;
}
