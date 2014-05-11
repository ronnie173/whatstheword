package com.heyzap.sdk.ads;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.KeyguardManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import android.util.DisplayMetrics;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.heyzap.http.RequestParams;
import com.heyzap.internal.APIClient;
import com.heyzap.internal.APIResponseHandler;
import com.heyzap.internal.Connectivity;
import com.heyzap.internal.Logger;
import com.heyzap.internal.Utils;

class AdUnit {

    public static enum AdUnitType {
        INTERSTITIAL, BANNER
    };

    public static enum AdState {
        NONE, LOADING, LOADED
    };

    public static enum AdPrefetchStrategy {
        NONE, ENABLED
    }

    public static String DEFAULT_TAG_NAME = "default";

    // Type
    private Context context;
    private AdUnitType type;
    private AdState state;
    private boolean prefetch;
    private int orientation = Configuration.ORIENTATION_PORTRAIT;

    private String position;

    // Data fields
    private String htmlData;
    private String strategy;
    private String gamePackage;
    private String actionUrl;
    private String impressionId;
    private String tag = DEFAULT_TAG_NAME;
    protected String supportedCreativeTypes; // comma-separated list, like "foo,bar"
    protected String creativeType;
    private long refreshTime;
    private Boolean sentImpression = false;
    private Boolean sentClick = false;

    private boolean manualSize = false;
    private int height;
    private int width;
    private boolean disableGlobalTouch;
    private int backgroundOverlay = -1;
    private boolean hideOnOrientationChange;

    // Default Data
    public String defaultHtmlData = "";
    protected long defaultRefreshTime = 0;

    // Callbacks
    private OnAdStateChangeHandler onStateChangeHandler = null;
    private OnAdDisplayListener displayListener = null;

    // Timers
    private Boolean refreshPaused = false;
    private long lastRefreshTime = System.currentTimeMillis();
    private Timer timer;

    protected String requiredOrientation = null;
    private String creativeId;
    private String targetCreativeType;
    private static int tickRate = 2000;
    private AtomicBoolean marketIntentLaunched = new AtomicBoolean(false);
    
    final static private int NUM_RETRIES = 3;
    private int retries = NUM_RETRIES;
    private String rejectedImpression = null;

    public AdUnit(Context context, AdUnitType type, boolean prefetch, String supportedCreativeTypes, OnAdDisplayListener listener, String tag) {
        this.context = context;
        this.type = type;
        this.state = AdState.NONE;
        this.prefetch = prefetch;
        this.supportedCreativeTypes = supportedCreativeTypes;
        this.displayListener = listener;
        
        if (tag != null && tag.length() > 0) {
        	this.tag = tag;
        }

        if (this.shouldPrefetch()) {
            this.fetch();
        }
    }

    public void setOnStateChangeHandler(OnAdStateChangeHandler handler) {
        this.onStateChangeHandler = handler;
        this.onStateChangeHandler.onStateChange(this, this.state);
    }

    public AdState getState() {
        return this.state;
    }

    public void setState(AdState state) {
        this.state = state;
        if (this.onStateChangeHandler != null) {
            this.onStateChangeHandler.onStateChange(this, this.state);
        }
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public void setRequestOrientation(int orientation) {
        this.orientation = orientation;
    }

    public void setDisplayListener(OnAdDisplayListener listener) {
        this.displayListener = listener;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public String getHtmlData() {
        return this.htmlData;
    }

    public String getStrategy() {
        return this.strategy;
    }
    
    public String getImpressionId() {
    	return this.impressionId;
    }

    public String getGamePackage() {
        return this.gamePackage;
    }

    public long getRefreshTime() {
        return (this.refreshTime > 0) ? this.refreshTime : this.defaultRefreshTime;
    }

    private long getTimeSinceLastRefresh() {
        long currentTime = System.currentTimeMillis();
        return currentTime - this.lastRefreshTime;
    }

    public void onRequestImpression() {
        if (this.state == AdState.NONE) {
            this.fetch();
        }
    }

    private void fetch() {
        // bail if we've exhausted our retries, reset the retry count as well:
    	if (retries < 1) { retries = NUM_RETRIES; return; }
    	
        final Activity currentContext = (Activity) AdUnit.this.context;
        currentContext.runOnUiThread(new Runnable() {
        	public void run() {
                AdUnit.this.setState(AdState.LOADING);

                if (!Connectivity.isConnected(currentContext)) {
                    AdUnit.this.setState(AdState.NONE);
                    AdUnit.this.startRefreshTimer();
                    return;
                }

                final RequestParams requestParams = new RequestParams();

                DisplayMetrics dm = context.getResources().getDisplayMetrics();

                requestParams.put("creative_type", AdUnit.this.supportedCreativeTypes);

                // Device Specific
                requestParams.put("connection_type", Connectivity.connectionType(context));
                requestParams.put("device_dpi", Float.toString(dm.density));

                int statusBarHeight = 0;

                if (context instanceof Activity) {
                    Activity activity = (Activity) context;
                    Rect rect = new Rect();
                    Window window = activity.getWindow();
                    window.getDecorView().getWindowVisibleDisplayFrame(rect);
                    statusBarHeight = rect.top;
                }

                requestParams.put("device_width", Integer.toString(dm.widthPixels));
                requestParams.put("device_height", Integer.toString(dm.heightPixels));
                requestParams.put("status_bar_height", Integer.toString(statusBarHeight));
                requestParams.put("supported_features", "chromeless,js_visibility_callback");

                String orientationString = (dm.widthPixels > dm.heightPixels) ? "landscape" : "portrait";

                if (creativeId != null) {
                    requestParams.put("creative_id", creativeId);
                }
                if (targetCreativeType != null){
                	requestParams.put("random_creative_with_type", targetCreativeType);
                }

                // View specifics
                requestParams.put("ad_chrome", "true");
                requestParams.put("orientation", orientationString);

                if (position != null) {
                    requestParams.put("position", position);
                }

                try{
                  StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
                  long bytesAvailable = (long) stat.getBlockSize() * (long) stat.getAvailableBlocks();
                  requestParams.put("device_free_bytes", Long.toString(bytesAvailable));
                }
                catch (Exception ex1){
                  requestParams.put("device_free_bytes", "0");
                }

                if (AdUnit.this.tag != null) {
                    requestParams.put("tag", AdUnit.this.tag);
                }

                if (AdUnit.this.rejectedImpression != null) {
                    requestParams.put("rejected_impression_id", AdUnit.this.rejectedImpression);
                }
                
                final String url = Manager.AD_SERVER + "/fetch_ad";
                
                APIClient.post(context, url, requestParams, new APIResponseHandler() {

                    @Override
                    public void onSuccess(final JSONObject response) {

                        try {
                        	if (AdUnit.this.getState() != AdState.LOADING) {
                                Logger.log("Ads: dropped response because ad state was not loading");

                                return;
                            }

                            if (response.isNull("ad_html")) {
                                AdUnit.this.setState(AdState.NONE);
                                
                                return;
                            }
                            
                            String gamePackage  = (String) response.getString("promoted_game_package");
                            String impressionId = (String) response.getString("impression_id");

                            List<String> installedPackages = Manager.getInstance().getLocalPackages();
                            boolean alreadyInstalled = installedPackages.contains(gamePackage);
                            if (alreadyInstalled) {
                                AdUnit.this.rejectedImpression = impressionId;
                                AdUnit.this.retries -= 1;
                                
                                AdUnit.this.setState(AdState.NONE);
                                
                                currentContext.runOnUiThread(new Runnable() {
                                    public void run() {
                                        Logger.log("re-fetching");
                                        fetch();
                                    }
                                });
                                
                                return;
                            } else {
                                Manager.getInstance().setGameImpression(gamePackage, impressionId);
                                AdUnit.this.rejectedImpression = null;
                                AdUnit.this.retries = NUM_RETRIES;
                            }
                            
                            // Get the ad data
                            AdUnit.this.htmlData = (String) response.getString("ad_html");
                            AdUnit.this.strategy = (String) response.getString("ad_strategy");
                            AdUnit.this.gamePackage = gamePackage;
                            AdUnit.this.impressionId = impressionId;
                            AdUnit.this.actionUrl = (String) response.optString("click_url");

                            AdUnit.this.height = response.optInt("ad_height");
                            AdUnit.this.width = response.optInt("ad_width");

                            if (AdUnit.this.height == 0) {
                                String h = response.optString("ad_height");
                                if (h.equals("fill_parent")) {
                                    AdUnit.this.height = LayoutParams.FILL_PARENT;
                                }
                            }

                            if (AdUnit.this.width == 0) {
                                String h = response.optString("ad_width");
                                if (h.equals("fill_parent")) {
                                    AdUnit.this.width = LayoutParams.FILL_PARENT;
                                }
                            }

                            AdUnit.this.manualSize = AdUnit.this.height != 0 && AdUnit.this.width != 0;
                            AdUnit.this.requiredOrientation = response.optString("required_orientation", null);

                            disableGlobalTouch = response.optBoolean("disable_global_touch", false);
                            backgroundOverlay = response.optInt("background_overlay", -1);
                            hideOnOrientationChange = response.optBoolean("hide_on_orientation_change", false);

                            AdUnit.this.creativeType = response.optString("creative_type", "no_creative_type"); // I set the default in case anyone ever sees it in our db and wants to grep for it

                            Logger.log("(FETCH) [" + AdUnit.this.tag + "] Impression ID:" + AdUnit.this.impressionId);

                            // Reset defaults
                            AdUnit.this.sentClick = false;
                            AdUnit.this.sentImpression = false;

                            if (!response.isNull("refresh_time")) {
                                AdUnit.this.refreshTime = response.getInt("refresh_time") * 1000L;
                            }

                            AdUnit.this.setState(AdState.LOADED);

                            if (AdUnit.this.displayListener != null) {
                            	if (AdUnit.this.tag != AdUnit.DEFAULT_TAG_NAME) {
                            		AdUnit.this.displayListener.onAvailable(AdUnit.this.tag);
                            	} else {
                            		AdUnit.this.displayListener.onAvailable(null);
                            	}
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();

                            if (AdUnit.this.getState() == AdState.LOADING) {
                                AdUnit.this.setState(AdState.NONE);
                            }
                        } finally {
                            // Refresh ad
                            AdUnit.this.startRefreshTimer();

                            if (AdUnit.this.getState() == AdState.NONE) {
                                // Send out failure message
                                AdUnit.this.onFetchFailure();
                            }
                        }
                    }

                    @Override
                    public void onFailure(final Throwable e) {

                        e.printStackTrace();
                        AdUnit.this.setState(AdState.NONE);

                        // Refresh ad
                        AdUnit.this.startRefreshTimer();

                        // Send out failure message
                        AdUnit.this.onFetchFailure();
                    }

                });
        	}
        });
    }

    /* Features */
    public Boolean shouldPrefetch() {    	
        Boolean shouldPrefetch = this.prefetch && !Manager.getInstance().isFlagEnabled(HeyzapAds.DISABLE_AUTOMATIC_FETCH);
        return shouldPrefetch;
    }

    /* Timers */

    public void startRefreshTimer() {
        if (this.timer == null) {
            Logger.log("Starting timer...");
            this.timer = new Timer();
            if (this.getRefreshTime() > 0) {
                this.timer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        if (!AdUnit.this.isPaused()) {
                            Logger.log("Refreshing ad...");
                            AdUnit.this.onRefresh(true);
                        }
                    }
                }, 0, tickRate);
            }
        }
    }

    /* Actions */

    public void onIgnore() {
        if (this.displayListener != null) {
        	if (this.tag != AdUnit.DEFAULT_TAG_NAME) {
        		this.displayListener.onHide(this.tag);
        	} else {
        		this.displayListener.onHide(null);
        	}
        }
    }

    public void onRefresh(Boolean forceRefresh) {
        this.setState(AdState.NONE); //reset the state		
        if (this.shouldPrefetch() || forceRefresh) {
            this.lastRefreshTime = System.currentTimeMillis();
            this.fetch();
        }
    }

    public void onRequest() {
        if (this.state != AdState.LOADED) {
            this.fetch();
        }
    }

    public void onFailure() {
        if (this.displayListener != null) {
        	if (this.tag != DEFAULT_TAG_NAME) {
        		this.displayListener.onFailedToShow(this.tag);
        	} else {
        		this.displayListener.onFailedToShow(this.tag);
        	}
        }
    }

    public void onFetchFailure() {
        if (this.displayListener != null && this.getState() == AdState.NONE) {
        	if (this.tag != DEFAULT_TAG_NAME) {
        		this.displayListener.onFailedToFetch(this.tag);
        	} else {
        		this.displayListener.onFailedToFetch(null);
        	}
        }
    }

    public void onAvailable() {
        if (this.displayListener != null) {
        	if (this.tag != DEFAULT_TAG_NAME) {
        		this.displayListener.onAvailable(this.tag);
        	} else {
        		this.displayListener.onAvailable(null);
        	}
        }
    }

    public void onImpression() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (AdUnit.this.sentImpression == true) {
                    Logger.log("Already sent impression successfully.");
                    return;
                }

                if (AdUnit.this.displayListener != null) {
                	if (AdUnit.this.tag != DEFAULT_TAG_NAME) {
                		AdUnit.this.displayListener.onShow(AdUnit.this.tag);
                	} else {
                		AdUnit.this.displayListener.onShow(null);
                	}
                }

                Logger.log("Sending impression");

                RequestParams params = new RequestParams();
                params.put("impression_id", impressionId);
                params.put("promoted_android_package", AdUnit.this.gamePackage);

                if (AdUnit.this.tag != null) {
                    params.put("tag", AdUnit.this.tag);
                }

                APIClient.post(context, Manager.AD_SERVER + "/register_impression", params, new APIResponseHandler() {
                    @Override
                    public void onSuccess(final JSONObject response) {
                        try {
                            if (response.getInt("status") == 200) {
                                Logger.log("(IMPRESSION) [" + AdUnit.this.tag + "] Impression ID:" + AdUnit.this.impressionId);
                                AdUnit.this.sentImpression = true;
                            }
                        } catch (JSONException e) {
                        	
                        }
                    }
                });
            }
        }).start();
    }

    public void onClick(Boolean refresh) {
        onClick(this.actionUrl, null, refresh);
    }

    public void registerClick(final String customGamePackage) {
        (new Thread(new Runnable() {
            @Override
            public void run() {
                RequestParams params = new RequestParams();
                params.put("ad_strategy", AdUnit.this.strategy);
                params.put("promoted_game_package", AdUnit.this.gamePackage);
                if (customGamePackage != null) {
                    params.put("custom_game_package", customGamePackage);
                }
                params.put("impression_id", AdUnit.this.impressionId);

                if (AdUnit.this.tag != null) {
                    params.put("tag", AdUnit.this.tag);
                }

                APIClient.post(context, Manager.AD_SERVER + "/register_click", params, new APIResponseHandler() {
                    @Override
                    public void onSuccess(final JSONObject response) {
                        try {
                            if (response.getInt("status") == 200) {
                                Logger.log("(CLICK) [" + AdUnit.this.tag + "] Impression ID:" + AdUnit.this.impressionId);
                                AdUnit.this.sentClick = true;
                            }
                        } catch (JSONException e) {

                        }
                    }
                });
            }
        })).start();
    }

    public void onClick(String adUrl, final String customGamePackage, Boolean refresh) {
        if (this.sentClick) {
            Logger.log("Already sent click successfully.");
            return;
        }

        // Check we haven't received this event twice in close proximity	
        if (System.currentTimeMillis() - Manager.getInstance().lastClickedTime < Manager.maxClickDifference) {
            return;
        }

        // Show a spinner
        try {
            final ProgressDialog marketSpinner = ProgressDialog.show(this.context, "", "Loading...", true);
            Manager.handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        marketSpinner.dismiss();
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    }
                }
            }, 3000);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Fire callbacks
        if (this.displayListener != null) {
        	if (this.tag != DEFAULT_TAG_NAME) {
        		this.displayListener.onClick(this.tag);
        	} else {
                this.displayListener.onClick(null);	
        	}
        }

        registerClick(customGamePackage);

        marketIntentLaunched.set(false);
        gotoMarket(adUrl);
        
        // Update
        Manager.getInstance().lastClickedTime = System.currentTimeMillis();

        // Change the ad
        if (refresh) {
            this.onRefresh(true);
        }
    }
    
    private void launchMarketIntent(Context context, String intentUrl) {
        if (marketIntentLaunched.compareAndSet(false, true)) {
            // Fire the intent
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(intentUrl));
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            context.startActivity(intent);
        } else {
        }
    }
    
    private boolean launchIfMarket(Context context, String adUrl) {
        if (Utils.isAmazon()) {
            if (adUrl.startsWith("amzn")) {
                launchMarketIntent(context, adUrl);
                return true;
            }
            
            if (adUrl.contains("amazon.com/gp/mas/dl/android?")) {
                // Turn it into an amzn:// link so the user won't get the "open with browser or market" prompt
                int i = adUrl.indexOf("android?");
                String marketUrl = "amzn://apps/" + adUrl.substring(i);
                launchMarketIntent(context, marketUrl);
                return true;
            }
        } else {
            if (adUrl.startsWith("market")) {
                launchMarketIntent(context, adUrl);
                return true;
            }
            
            if (adUrl.contains("play.google")) {
                // Turn it into a market:// link so the user won't get the "open with browser or market" prompt
                int i = adUrl.indexOf("details?");
                if (i == -1) {
                    // Doesn't look to be the kind of play.google URL we expect, but let's still launch it
                    launchMarketIntent(context, adUrl);
                } else {
                    String marketUrl = "market://" + adUrl.substring(i);
                    launchMarketIntent(context, marketUrl);
                }
                return true;
            }
        }
        
        return false;
    }

    private void gotoMarket(String url) {
        final String adUrl = url;
        
        if (launchIfMarket(context, adUrl)) {
            return;
        }
        
        final WebView webView = new WebView(context);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView wView, String url) {
                return super.shouldOverrideUrlLoading(wView, url);
            }
            
            @Override
            public void onLoadResource(WebView view, String url) {
                super.onLoadResource(view, url);
                if (launchIfMarket(context, url)) {
                    view.stopLoading();
                    return;
                }
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                
                if (launchIfMarket(context, url)) {
                    view.stopLoading();
                    return;
                }
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
            }
            
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }
        });

        webView.setWebChromeClient(new WebChromeClient());
        
        webView.getSettings().setJavaScriptEnabled(true);
        
        Timer timer = new Timer();
        
        webView.postDelayed(new Runnable(){
            @Override
            public void run() {
            	((Activity)context).runOnUiThread(new Runnable() {
	            	@Override
	            	public void run() {
	            		webView.loadUrl(adUrl);
	            	}
            	});
            }
        }, 250);
        
        webView.postDelayed(new Runnable(){
            
            @Override
            public void run() {
                if (!marketIntentLaunched.get()) {
                	((Activity)context).runOnUiThread(new Runnable() {
    	            	@Override
    	            	public void run() {
    	            		webView.loadUrl(adUrl);
    	            	}
                	});
                } else {
                }
            }
        }, 750);
        
        webView.postDelayed(new Runnable(){
            
            @Override
            public void run() {
                if (!marketIntentLaunched.get()) {
                    launchMarketIntent(context, adUrl);
                } else {
                }
            }
        }, 1250);
        
        
    }

    public void setRefreshPaused(Boolean paused) {
        this.refreshPaused = paused;
    }

    public Boolean isPaused() {

        // Pause enabled
        Boolean paused = !Utils.isApplicationOnTop(AdUnit.this.context) || AdUnit.this.refreshPaused;

        // Expire time is up
        Boolean shouldRefresh = AdUnit.this.getRefreshTime() > 0 && AdUnit.this.getTimeSinceLastRefresh() > AdUnit.this.getRefreshTime();

        KeyguardManager myKM = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        Boolean isScreenLocked = myKM.inKeyguardRestrictedInputMode(); // This is true when screen is locked

        // Internet connectivity is bad
        Boolean isConnected = Connectivity.isConnected(AdUnit.this.context);

        return paused || !shouldRefresh || isScreenLocked || !isConnected;
    }

    abstract public class OnAdStateChangeHandler {
        public void onStateChange(AdUnit adUnit, AdState state) {
        };
    }

    public boolean hasManualSize() {
        return manualSize;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public boolean isDisableGlobalTouch() {
        return disableGlobalTouch;
    }

    public boolean isHideOnOrientationChange() {
        return hideOnOrientationChange;
    }

    public void setCreativeId(String creativeId) {
        this.creativeId = creativeId;
    }

    public void setTargetCreativeType(String creativeType) {
        this.targetCreativeType = creativeType;
    }

    public Integer getBackgroundOverlayColor() {
        if (backgroundOverlay == -1) {
            return Color.TRANSPARENT;
        } else {
            return backgroundOverlay;
        }
    }

    public AdUnitType getType() {
        return this.type;
    }

    public String getCreativeType() {
        return this.creativeType;
    }

    public String getRequiredOrientation() {
        return requiredOrientation;
    }
}
