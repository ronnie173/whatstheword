package com.heyzap.sdk;

import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.heyzap.http.RequestParams;
import com.heyzap.internal.Analytics;
import com.heyzap.internal.HeyzapProgressDialog;
import com.heyzap.internal.Logger;
import com.heyzap.internal.APIResponseHandler;
import com.heyzap.internal.APIClient;
import com.heyzap.internal.Utils;

public class HeyzapLib {
	public static final String HEYZAP_PACKAGE = "com.heyzap.android";
	private static final String HEYZAP_INTENT_CLASS = ".CheckinForm";
	public static final String IMAGE_FILE_NAME = "hzSdkImage.png";
	public static final String FIRST_RUN_KEY = "HeyzapFirstRun";
	public static final String OVERLAY_PREF = "HeyzapLeaderboardOverlay";
	public static final String LAST_PB_NOTIF = "HeyzapPBNotif";

	public static final Handler handler = new Handler(Looper.getMainLooper());

	private static String packageName;
	private static String newLevel;

	private static WeakReference<BroadcastReceiver> playWithFriendsReceiverRef;

	/* No flags */
	public static int FLAG_NONE = 0 << 0;
	
	/* Disable the Heyzap checkin prompt */
	public static int FLAG_NO_HEYZAP_INSTALL_SPLASH = 1 << 1;
	
	/* Disable the Heyzap install prompt notification */
	public static int FLAG_NO_NOTIFICATION = 1 << 23;
	
	/* Only show the Heyzap install notification to returning users */
	public static int FLAG_SUBTLE_NOTIFICATION = 1 << 24;
	
	/* Show a minimalist Achievement unlocked dialog */
	public static int FLAG_MINIMAL_ACHIEVEMENT_DIALOG = 1 << 25;
	
	private static int flags = 0;
	private static ActivityResultListener activityResultListener;

	static boolean ssoTestPassed = false;
	public static Context applicationContext;

	private static HeyzapProgressDialog progressDialog;
	private static Object progressDialogLock = new Object();
	
	/* Leaderboards */
	private static LevelRequestListener levelRequestListener;
	private static String pendingLevelId;
	private static boolean gameLaunchRegistered;
        private static boolean adsEnabled = false;
        static {
          Log.d("heyzap-sdk", "HeyzapLib loading (static)");
        }

	//
	// API methods exposed to developers
	//

	// Load the heyzap API, this call must be called first, this also:
	// - sends the android level "get heyzap" local notification
	// - shows the "checkin" popup over the game

	public static void load(final Context context, boolean showHeyzapInstallSplash) {
		start(context, FLAG_NO_HEYZAP_INSTALL_SPLASH);
	}
	
	public static void start(final Context context) {
		start(context, HeyzapLib.FLAG_NONE);
	}
	
	public static void start(final Context context, final int flags) {
		Boolean showSplash = true;
		if (flags > 0 && (flags & FLAG_NO_HEYZAP_INSTALL_SPLASH) == FLAG_NO_HEYZAP_INSTALL_SPLASH) {
			showSplash = false;
		}
		
		HeyzapLib.setFlags(flags);
		
		applicationContext = context.getApplicationContext();
		packageName = context.getPackageName();

		Analytics.trackEvent(context, "heyzap-start");

		APIClient.init(context);
		Logger.init(context);
		Utils.load(context);
		
		final String appName = Utils.getAppLabel(context);
		if (appName == null) return;
		
		final SharedPreferences overlayPrefs = context.getSharedPreferences(OVERLAY_PREF, 0);

		// Show overlay if they don't have heyzap and haven't seen the overlay
		// yet
		if (!Utils.heyzapIsInstalled(context) && !overlayPrefs.getBoolean(packageName, false) && showSplash) {
			((Activity) context).runOnUiThread(new Runnable() {
				@Override
				public void run() {
					final SharedPreferences.Editor overlayEditor = overlayPrefs.edit();
					overlayEditor.putBoolean(packageName, true);
					overlayEditor.commit();
					showFullOverlay(applicationContext);
				}
			});
		}

		if (context instanceof Activity) {
			Intent i = ((Activity) context).getIntent();
			if (i.hasExtra("level")) {
				pendingLevelId = ((Activity) context).getIntent().getStringExtra("level");
			}
		}

		if (!gameLaunchRegistered) {

			IntentFilter filter = new IntentFilter("com.heyzap.android.GAME_LAUNCHED");
			applicationContext.registerReceiver(new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {

					String packageName = intent.getStringExtra("package");
					if (packageName == null || !packageName.equals(context.getPackageName()))
						return;


					String levelId = intent.getStringExtra("level");
					if (levelId == null)
						return;

					if (levelRequestListener != null) {
						levelRequestListener.onLevelRequested(levelId);
						pendingLevelId = null;
					} else {
						pendingLevelId = levelId;
					}
				}
			}, filter);
			gameLaunchRegistered = true;
		}
	}

	public static Context getApplicationContext() {
		return applicationContext;
	}

	// Show the checkin form for this game, or show the "get heyzap" dialog
	public static void checkin(final Context context) {
		checkin(context, null);
	}
	
	public static void checkin(final Context context, final String prefillMessage) {
		applicationContext = context.getApplicationContext();
		((Activity) context).runOnUiThread(new Runnable() {
			@Override
			public void run() {
				rawCheckin(context, prefillMessage);
			}
		});
	}

	public static void showFullOverlay(final Context context) {
	    applicationContext = context.getApplicationContext();
	    new LeaderboardFullOverlay(context).show();
	}

	public static void showInGameOverlay(final Context context, final String displayName, final String source) {
		((Activity) context).runOnUiThread(new Runnable(){
			@Override
			public void run(){
				applicationContext = context.getApplicationContext();
				LeaderboardInGameOverlay overlay = new LeaderboardInGameOverlay(context, source);
				overlay.setDisplayName(displayName);
				overlay.show();				
			}
		});
	}

	public static void showLeaderboards(final Context context) {
		showLeaderboards(context, null);
	}

	public static void showLeaderboards(final Context context, final String levelId) {
		((Activity) context).runOnUiThread(new Runnable(){
			@Override
			public void run(){
				applicationContext = context.getApplicationContext();
				if (Utils.hasHeyzapLeaderboards(context)) {
					Intent i = new Intent(Intent.ACTION_MAIN);
					i.setAction(HEYZAP_PACKAGE);
					i.putExtra("game_context_package", context.getPackageName());
					if (levelId != null) {
						i.putExtra("level", levelId);
					}
					i.addCategory(Intent.CATEGORY_LAUNCHER);
					i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
					i.setComponent(new ComponentName(HEYZAP_PACKAGE, HEYZAP_PACKAGE + ".activity.Leaderboards"));

					context.startActivity(i);
				} else {
					new LeaderboardDialog(context, context.getPackageName(), levelId).show();
				}
		
			}
		});
	}

	public static String getLevel(Activity context) {
		if (context == null)
			return null;

		Intent intent = context.getIntent();
		if (intent == null)
			return null;

		return intent.getStringExtra("levelId");
	}

	public static void setLevelRequestListener(LevelRequestListener listener) {
		levelRequestListener = listener;
		if (pendingLevelId != null) {
			levelRequestListener.onLevelRequested(pendingLevelId);
			pendingLevelId = null;
		}
	}

	public static void submitScore(final Context context, final String score, final String displayScore, final String levelId) {
		submitScore(context, score, displayScore, levelId, false);
	}

	public static void submitScore(final Context context, final String score, final String displayScore, final String levelId, final boolean skipModalDialog) {
		applicationContext = context.getApplicationContext();

		Analytics.trackEvent(context, "score-received");

		if (Utils.hasHeyzapLeaderboards(context)) {
			Intent i = new Intent();
			i.setAction("com.heyzap.android.LeaderboardsReceiver");
			i.putExtra("leaderboard_action", "show_score_overlay");
			i.putExtra("score", score);
			i.putExtra("display_score", displayScore);
			i.putExtra("level", levelId);
			i.putExtra("game_context_package", context.getPackageName());
			i.setFlags(32); // Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
			if (skipModalDialog) {
				i.putExtra("skip_modal_dialog", true);
			}
			context.sendBroadcast(i);
		} else {
			((Activity) context).runOnUiThread(new Runnable() {

				@Override
				public void run() {
					Drawable gameIcon = null;
					try {
						gameIcon = applicationContext.getPackageManager().getApplicationIcon(packageName);
					} catch (NameNotFoundException e) {
						e.printStackTrace();
					}
					LeaderboardScoreLauncher.launchScoreDialog(context, score, displayScore, levelId, gameIcon, context.getPackageName(), true, skipModalDialog);
				}
			});
		}
	}

	public static void launchLeaderboardActivityOrShowInGameOverlay(Context context, String levelId, String gamePackage, String displayName, String source) {
		if (Utils.hasHeyzapLeaderboards(context)) {
			// i.setAction("com.heyzap.android.activity.Leaderboards");
			// i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

			// context.startActivity(i);

			Intent i = new Intent(Intent.ACTION_MAIN);
			i.setAction(HEYZAP_PACKAGE);
			i.putExtra("level", levelId);
			i.putExtra("game_context_package", gamePackage);
			i.addCategory(Intent.CATEGORY_LAUNCHER);
			i.putExtra("packageName", context.getPackageName());
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
			i.setComponent(new ComponentName(HEYZAP_PACKAGE, HEYZAP_PACKAGE + ".activity.Leaderboards"));

			context.startActivity(i);

		} else {
			showInGameOverlay(context, displayName, source);
		}
	}
	
    /**
     * Post achievement. Show Achievement dialog on success.
     * 
     * @param context
     * @param achievementIds
     */
	public static void unlockAchievement(final Context context, final List<String> achievementIds){
		unlockAchievement(context, TextUtils.join(",", achievementIds));
	}

    /**
     * Post achievement. Show Achievement dialog on success.
     * 
     * @param context
     * @param achievementIds
     *     A comma-separated list of achievement ids
     */
    public static void unlockAchievement(final Context context, final String achievementIds) {
        applicationContext = context.getApplicationContext();
        final String gameContextPackage = context.getPackageName();
        
        final boolean showBanner = (FLAG_MINIMAL_ACHIEVEMENT_DIALOG & flags) > 0;

        if (Utils.hasHeyzapAchievements(context) && !showBanner) {
            Intent i = new Intent();
            i.setAction("com.heyzap.android.LeaderboardsReceiver");
            i.putExtra("leaderboard_action", "unlock_achievements");
            i.putExtra("unlock_achievements", achievementIds);
            i.putExtra("game_context_package", gameContextPackage);
            i.setFlags(32); //Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            context.sendBroadcast(i);
        } else {
	        ((Activity) context).runOnUiThread(new Runnable() {
	            @Override
	            public void run() {
	                applicationContext = context.getApplicationContext();
	        
	                // param "game_package" is automatically added:
	                RequestParams requestParams = new RequestParams();
	                requestParams.put("achievement_ids", achievementIds);
	                requestParams.put("game_context_package", gameContextPackage);
	                requestParams.put("key", Utils.md5Hex(achievementIds + gameContextPackage));
	                
	                APIClient.post(context, "/in_game_api/achievements/unlock", requestParams, new APIResponseHandler() {
	                    @Override
	                    public void onSuccess(JSONObject response) {
	                        
	                        if (response.has("achievements")) {
	                            JSONArray stream;
	                            try {
	                                stream = response.getJSONArray("achievements");
	                                int unlockedAchievements = stream.length();
	                            	
	                                if (unlockedAchievements > 0) {
	                                	if (showBanner) {
		                                	AchievementEarnedDialogTop dialog = new AchievementEarnedDialogTop(context, response);
		                                	dialog.show();
	                                	} else {
		                                    AchievementDialogFull dialog = new AchievementDialogFull(context, false, response);
		                                    dialog.setTitle("New Achievement Unlocked!");
		                                    dialog.show();
	                                	}
	                                	
	                                	Analytics.trackEvent(context, "achievement-dialog-unlocked-shown");
	                                }
	                            } catch (org.json.JSONException e) {
	                                e.printStackTrace();
	                            }
	                        }
	                    }
	        
	                    @Override
	                    public void onFailure(Throwable e) {
	                    }
	                });
	            }
	        });
        }
    }
    
    /**
     * View all achievements.
     * 
     * @param context
     * @param includeLocked
     */
    public static void showAchievements(final Context context) {
        applicationContext = context.getApplicationContext();
        String gameContextPackage = context.getPackageName();

        // param "game_package" is automatically added:
        final RequestParams requestParams = new RequestParams();
        
        // only show unlocked achievements?
//        if (!viewAllAchievements) {
//        	requestParams.put("unlocked", "true");
//        }
        requestParams.put("game_context_package", gameContextPackage);

        // if they have the Heyzap app with a version that includes Achievements
        if (Utils.hasHeyzapAchievements(context)) {
            Intent i = new Intent();
            i.setAction("com.heyzap.android.LeaderboardsReceiver");
            i.putExtra("leaderboard_action", "show_achievements");
            i.putExtra("game_context_package", gameContextPackage);
            i.setFlags(32); //Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            context.sendBroadcast(i);
        } else {
        	if(context instanceof Activity){
        		((Activity) context).runOnUiThread(new Runnable(){
        			@Override
        			public void run(){
        				AchievementDialogFull dialog = new AchievementDialogFull(context, true, null);
        				Analytics.trackEvent(context, "achievement-dialog-all-shown");
        				dialog.show();        				
        			}
        		});
        	}
        }
    }

	public static void clearScorePrefs(Context context) {
		LeaderboardScoreLauncher.removeLeaderboardInfoFromPhone(context);
		final SharedPreferences overlayPrefs = context.getSharedPreferences(OVERLAY_PREF, 0);
		final SharedPreferences.Editor overlayEditor = overlayPrefs.edit();
		overlayEditor.clear();
		overlayEditor.commit();
	}

	// Check if Heyzap is supported on this device
	public static boolean isSupported(Context context) {
		return Utils.marketInstalled(context) && Utils.androidVersionSupported();
	}

	// Set sdk flags to do stuff like disable the phone notification
	public static void setFlags(int newFlags) {
		flags = newFlags;
	}
	
	public static int getFlags(){
		return flags;
	}

	//
	// PRIVATE: Not exposed to game developers
	//
	static void rawCheckin(final Context context, final String prefillMessage) {
		packageName = context.getPackageName();
		applicationContext = context.getApplicationContext();
		Log.v(Analytics.LOG_TAG, "checkin-called");

		if (Utils.packageIsInstalled(HEYZAP_PACKAGE, context)) {
			launchCheckinForm(context, prefillMessage);
		} else {
			// Show the "pre-market" dialog
			Analytics.trackEvent(context, "checkin-button-clicked");
			new PreMarketCheckinDialog(context.getApplicationContext(), packageName, prefillMessage).show();
		}
	}

	static boolean subtleNotifications() {
		return (FLAG_SUBTLE_NOTIFICATION & flags) > 0;
	}

	static void broadcastEnableSDK(Context context) {
		// Tell the heyzap app this is an SDK game, so the popup does not show
		// up
		Intent broadcast = new Intent("com.heyzap.android.enableSDK");
		broadcast.putExtra("packageName", context.getPackageName());
		context.sendBroadcast(broadcast);
	}

	static void sendPBNotification(final Context context, String displayScore) {
		if((flags & FLAG_NO_NOTIFICATION) > 0) return;
		final String appName = Utils.getAppLabel(context);
		if (appName == null) {
			return;
		}

		if (Utils.packageIsInstalled(HEYZAP_PACKAGE, context) || !Utils.marketInstalled(context) || !Utils.androidVersionSupported()) {
			return;
		}

		long now = System.nanoTime() / 1000000000;
		long last = context.getSharedPreferences(LAST_PB_NOTIF, 0).getLong("seconds", 0l);

		// only once every 24 hours
		if (last == 0 || (now - last) < 60 * 60 * 24) {
			// save the time
			Editor editor = context.getSharedPreferences(LAST_PB_NOTIF, 0).edit();
			editor.putLong("seconds", now);
			editor.commit();

			// actually create the notification
			HeyzapNotification.sendPB(context, appName, displayScore);
		}

	}

	static void sendNotification(final Context context) {
		if ((FLAG_NO_NOTIFICATION & flags) > 0)
			return;
		final String appName = Utils.getAppLabel(context);
		if (appName == null)
			return;

		// Send the "get heyzap" android local notification, unless they
		// already have heyzap or their phone doesn't support Heyzap
		if (!Utils.packageIsInstalled(HEYZAP_PACKAGE, context) && Utils.marketInstalled(context) && Utils.androidVersionSupported()) {
			try {
				Date today = new Date();
				if ((FLAG_SUBTLE_NOTIFICATION & flags) > 0) {
					SharedPreferences prefs = context.getSharedPreferences(FIRST_RUN_KEY, 0);
					long firstRun = prefs.getLong("firstRunAt", 0);
					if (firstRun == 0) {
						Editor editor = prefs.edit();
						editor.putLong("firstRunAt", today.getTime());
						editor.commit();
						return;
					} else if (Utils.daysBetween(today, new Date(firstRun)) < 1) {
						return;
					}
				}

				Date lastNotification = new Date(context.getSharedPreferences(FIRST_RUN_KEY, 0).getLong("notificationLastShown", 0l));
				int numberOfNotifications = context.getSharedPreferences(FIRST_RUN_KEY, 0).getInt("numberNotificationsShown", 0);
				switch (numberOfNotifications) {
				case 0:
					HeyzapNotification.send(context, appName);
					break;
				case 1:
					if (Utils.daysBetween(lastNotification, today) >= 5) {
						HeyzapNotification.send(context, appName);
					}
					break;
				case 2:
					if (Utils.daysBetween(lastNotification, today) >= 14) {
						HeyzapNotification.send(context, appName);
					}
					break;
				default:
					return;
				}

				// Store when we last showed the notification to avoid over
				// notification
				Editor editor = context.getSharedPreferences(FIRST_RUN_KEY, 0).edit();
				editor.putInt("numberNotificationsShown", numberOfNotifications + 1);
				editor.putLong("notificationLastShown", today.getTime());
				editor.commit();
			} catch (Exception e) {
				Log.d(Analytics.LOG_TAG, "Exception while sending notification");
				e.printStackTrace();
			}
		}
	}

	static void launchCheckinForm(final Context context, String prefillMessage) {
		String packageName = HEYZAP_PACKAGE;

		Intent popup = new Intent(Intent.ACTION_MAIN);
		popup.putExtra("message", prefillMessage);
		popup.setAction(packageName);
		popup.addCategory(Intent.CATEGORY_LAUNCHER);
		popup.putExtra("packageName", context.getPackageName());
		popup.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
		popup.setComponent(new ComponentName(packageName, HEYZAP_PACKAGE + HEYZAP_INTENT_CLASS));

		context.startActivity(popup);
	}

	static interface ActivityResultListener {
		public void onActivityResult(int requestCode, int resultCode, Intent data);
	}

	public static interface LevelRequestListener {
		public void onLevelRequested(String levelId);
	}
}
