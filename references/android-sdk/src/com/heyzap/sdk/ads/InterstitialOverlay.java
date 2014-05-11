package com.heyzap.sdk.ads;

import java.util.HashMap;
import java.util.Set;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.heyzap.internal.ClickableToast;
import com.heyzap.internal.Connectivity;
import com.heyzap.internal.Logger;
import com.heyzap.internal.Utils;
import com.heyzap.sdk.ads.AdUnit.AdState;

@SuppressLint("SetJavaScriptEnabled")
public class InterstitialOverlay extends ClickableToast {
    private Context context;
    private long shownAt;
    private InterstitialOverlayView adWrapper;
    private static final float MAX_SIZE_PERCENT = 0.98f;
    private static final int MAX_SIZE_DP_WIDTH = 360;
    private static final int MAX_SIZE_DP_HEIGHT = 360;
    
    private HashMap<String, AdUnit>adUnits = new HashMap<String, AdUnit>();
    private AdUnit currentAdUnit = null;
    private OnAdDisplayListener listener;
    
    private static volatile InterstitialOverlay instance;

    private long lastAttemptedShow = 0;
    private long millisBetweenAttempts = 30000;
    private long millisBeforeShow = 2000;
    private class InterstitialWebView extends WebView {
        
        public InterstitialWebView(Context context) {
            super(context);
            // TODO Auto-generated constructor stub
            this.setBackgroundColor(0);
        }

        @Override
        public boolean onKeyDown(int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                InterstitialOverlay.this.hide();
                return true;
            } else {
                return super.onKeyDown(keyCode, event);
            }
        }
    }
    
	private class InterstitialOverlayView extends RelativeLayout {
		
		public FrameLayout container;
		public InterstitialWebView webview;
		private String currentlyDisplayedImpressionId;
		
		private static final int OVERLAY_PADDING = 10;
		
    	public InterstitialOverlayView(Context context) {
    		super(context);
    		
    		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    		this.setLayoutParams(params);
    		this.setGravity(Gravity.CENTER);
    		
    		this.container = new FrameLayout(context);
    		
    		int scaledPadding = Utils.getScaledSize(context, OVERLAY_PADDING);
    		
    		RelativeLayout.LayoutParams containerParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    		containerParams.addRule(ALIGN_PARENT_LEFT);
    		containerParams.addRule(ALIGN_PARENT_TOP);
    		
    		this.addView(this.container, containerParams);
    		
    		this.webview = new InterstitialWebView(context);
            this.webview.setVisibility(View.VISIBLE);
            this.webview.setVerticalScrollBarEnabled(false);
            this.webview.setHorizontalScrollBarEnabled(false);
            this.webview.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
            this.webview.setBackgroundColor(0x00000000);
            
            RelativeLayout.LayoutParams webviewParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            this.container.addView(this.webview, webviewParams);
            
            this.webview.loadDataWithBaseURL(null, "<html></html>", "text/html", null, null);
    	}
    	
    	public void clear() {
    		this.webview.loadDataWithBaseURL(null, "<html></html>", "text/html", null, null);
    		this.currentlyDisplayedImpressionId = null;
    	}
    	
    	public void renderAdUnit(AdUnit adUnit) {
    		// Check if the currently rendered ad is the same. If so, don't do it again.
    		if (this.currentlyDisplayedImpressionId != null && this.currentlyDisplayedImpressionId == adUnit.getImpressionId()) {
    			return;
    		}
    		
    		this.webview.loadDataWithBaseURL(null, adUnit.getHtmlData(), "text/html", null, null);
    		this.currentlyDisplayedImpressionId = adUnit.getImpressionId();
    	}
    }

    private InterstitialOverlay(final Context context) {
        this(context, null);
    }

    private InterstitialOverlay(final Context context, OnAdDisplayListener listener) {
        super(context.getApplicationContext());
        this.initializeOverlay(context, listener);
    }

    private void setWidths(final Context context) {
        final Activity activity = (Activity) context;

        int width = (int) Math.round(activity.getWindowManager().getDefaultDisplay().getWidth() * MAX_SIZE_PERCENT);
        int height = (int) Math.round(activity.getWindowManager().getDefaultDisplay().getHeight() * MAX_SIZE_PERCENT);

        width = (int) Math.min(Utils.getScaledSize(context, MAX_SIZE_DP_WIDTH), width);
        height = (int) Math.min(Utils.getScaledSize(context, MAX_SIZE_DP_HEIGHT), height);

        width = (int) Math.min(width, height);
        height = (int) Math.min(width, height);

        if (this.currentAdUnit.hasManualSize()) {

            width = this.currentAdUnit.getWidth();
            height = this.currentAdUnit.getHeight();
        }
        
        int dp = Utils.dpToPx(context, 10);
        
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) this.adWrapper.getLayoutParams();
        layoutParams.gravity = Gravity.CENTER;

        layoutParams.width = width;
        layoutParams.height = height;

        InterstitialOverlay.this.setLayoutParams(layoutParams);
    }
    
    private void setupWebview() {
        this.adWrapper.webview.getSettings().setJavaScriptEnabled(true);
        this.adWrapper.webview.getSettings().setLoadsImagesAutomatically(true);
        this.adWrapper.webview.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);

        WebViewClient customWebViewClient = new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (InterstitialOverlay.this.currentAdUnit != null) {
                    if (url.contains("Heyzap.close")) {
                        InterstitialOverlay.this.hide();
                    } else if (url.contains("Heyzap.installHeyzap")) {
                        InterstitialOverlay.this.hide();
                        Manager.getInstance().installHeyzap(InterstitialOverlay.this.currentAdUnit);
                    } else if (url.contains("Heyzap.clickAd")) {
                        InterstitialOverlay.this.currentAdUnit.onClick(false);
                        InterstitialOverlay.this.hide();
                    } else if (url.contains("Heyzap.clickManualAdUrl=")) {
                        int urlStart = url.indexOf("Heyzap.clickManualAdUrl=") + 24;
                        int separator = url.indexOf(":::");
                        int packageStart = separator + 3;
                        String adUrl = url.substring(urlStart, separator);
                        String customGamePackage = url.substring(packageStart);
                        InterstitialOverlay.this.currentAdUnit.onClick(adUrl, customGamePackage, false);
                        InterstitialOverlay.this.hide();
                    }
                } else {
                    // currentAdUnit could be null in rare cases.
                    InterstitialOverlay.this.hide();
                }

                return true;
            }
        };

        WebChromeClient customWebChromeClient = new WebChromeClient() {
            @Override
            public void onConsoleMessage(String message, int lineNumber, String sourceID) {
                Logger.log("Console Message", message, lineNumber, sourceID);
            }
        };

        this.adWrapper.webview.setWebViewClient(customWebViewClient);
        this.adWrapper.webview.setWebChromeClient(customWebChromeClient);
    }

    private void setBackgroundOverlay() {
        setBackgroundColor(this.currentAdUnit.getBackgroundOverlayColor());
    }

    private void setTouchListener() {
        this.adWrapper.webview.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
            	if (InterstitialOverlay.this.currentAdUnit != null) {
	                if (InterstitialOverlay.this.currentAdUnit.isDisableGlobalTouch()) {
	                    return false;
	                } else {
	                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
	                        InterstitialOverlay.this.currentAdUnit.onClick(false);
	                        InterstitialOverlay.this.hide();
	                    }
	                }
            	}

                return true;
            }
        });
    }

    @Override
    protected void onConfigurationChanged(Configuration config) {
    	if (this.currentAdUnit != null && this.currentAdUnit.isHideOnOrientationChange()) {
    		this.hide();
    	}
    	
        super.onConfigurationChanged(config);
    }

    private void initializeOverlay(final Context context, OnAdDisplayListener listener) {
    	
    	this.adWrapper = new InterstitialOverlayView(context);
    	this.setContext(context);
    	this.listener = listener;
    	
    	// Webview bug workaround
    	// *** This quickly shows and then hides the interstitial, which seems to kickstart the webview into pre-rendering loaded HTML, which it won't do on first load. */
    	super.show();
    	super.hide();
    	// ----------------------
    	
    	AdUnit adUnit = this.getOrCreateAdUnitWithTag(AdUnit.DEFAULT_TAG_NAME);
        
        this.setContentView(this.adWrapper);
        this.setFocusable(true);
        this.setFocusableInTouchMode(true);
      
        this.setupWebview();
        setTouchListener();

        if (this.adUnits.get(AdUnit.DEFAULT_TAG_NAME).getState() == AdState.LOADED) {
        	this.adWrapper.renderAdUnit(adUnit);
        }
    }

    private void render() {
        final Activity activity = (Activity) this.context;
        
        if (this.currentAdUnit == null) {
        	return;
        }
        
        new Thread(new Runnable() {
        	public void run() {

                final AnimationSet set = new AnimationSet(true);

                // Fake in web view
                Animation animation = new AlphaAnimation(0.0f, 1.0f);
                animation.setDuration(200);
                set.addAnimation(animation);

                // Zoom in web view
                animation = new ScaleAnimation(0, 1, 0, 1, Animation.RELATIVE_TO_SELF, (float) 0.5, Animation.RELATIVE_TO_SELF, (float) 0.5);
                animation.setDuration(300);
                animation.setInterpolator(new AccelerateInterpolator());
                set.addAnimation(animation);

                //setWidths(context);
                setTouchListener();
                setBackgroundOverlay();
                
                activity.runOnUiThread(new Runnable() {
                	public void run() {
                		InterstitialOverlay.this.adWrapper.renderAdUnit(InterstitialOverlay.this.currentAdUnit);
                        InterstitialOverlay.this.adWrapper.startAnimation(set);
                	}
                });
        	}
        }).start();
    }

    @Override
    public WindowManager.LayoutParams getWmParams() {
        WindowManager.LayoutParams params = super.getWmParams();
        params.gravity = Gravity.CENTER;
        params.width = WindowManager.LayoutParams.FILL_PARENT;
        params.height = WindowManager.LayoutParams.FILL_PARENT;
        params.verticalMargin = 0.0f;
        params.horizontalMargin = 0.0f;
        params.flags &= ~WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        params.flags &= ~WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        params.flags |= WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
        params.flags &= ~WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        params.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;

        return params;
    }

    public void setContext(Context context) {
        this.context = context;
     	Set<String> adUnitTagSet = this.adUnits.keySet();
     	for (String tag : adUnitTagSet) {
     		this.adUnits.get(tag).setContext(this.context);
     	}
    }
    
    /* Visibility */

    @Override
    public void hide() {
        final Activity activity = (Activity)this.adWrapper.getContext();
        activity.runOnUiThread(new Runnable() {
        	public void run() {
        		
        		InterstitialOverlay.this.adWrapper.webview.loadUrl("javascript: try{adViewHidden();} catch(e) {}");
	
	            Animation animation = new ScaleAnimation(1, 0, 1, 0, Animation.RELATIVE_TO_SELF, (float) 0.5, Animation.RELATIVE_TO_SELF, (float) 0.5);
	            animation.setDuration(150);
	            animation.setInterpolator(new AccelerateInterpolator());
	            
	            animation.setAnimationListener(new AnimationListener() {
	
	                @Override
	                public void onAnimationEnd(Animation arg0) {
  	                	InterstitialOverlay.super.hide();
  	                	
  	                	InterstitialOverlay.this.adWrapper.clear();
	                    
	                	if (InterstitialOverlay.this.currentAdUnit != null) {
		                    InterstitialOverlay.this.currentAdUnit.onIgnore();
		                    InterstitialOverlay.this.currentAdUnit.onRefresh(false);
		                    
		                    // Clear the current ad unit.
		                    InterstitialOverlay.this.currentAdUnit = null;
	                	}
	                }
	
	                @Override
	                public void onAnimationRepeat(Animation arg0) {
	                }
	
	                @Override
	                public void onAnimationStart(Animation arg0) {
	
	                }
	
	            });
	
	            InterstitialOverlay.this.adWrapper.startAnimation(animation);
        	}
        });
    }
    
    private void show(String tag) {
    	this.currentAdUnit = this.getOrCreateAdUnitWithTag(tag);

        // Don't show if no connection
        if (!Connectivity.isConnected(context)) {
            this.currentAdUnit.onFailure();
            this.currentAdUnit = null;
        } else {
        	this.show();
        }
    }

    @Override
    public void show() {
    	
    	// This would be a weird case
    	if (this.currentAdUnit == null) {
    		return;
    	}
    	
        switch (this.currentAdUnit.getState()) {
        case NONE:
            // Don't attempt to refresh if last attempted time was less than minimum time between attempts
            if (this.lastAttemptedShow > 0 && (this.millisBetweenAttempts > (System.currentTimeMillis() - this.lastAttemptedShow))) {
                Logger.log("Attempted to show too early after failure!");
                // Send a failure to AdUnit.
                this.currentAdUnit.onFailure();
                return;
            }

            // Otherwise, attempt to refresh ad. Go to next step, and wait 2 seconds before attempting to show.
            this.currentAdUnit.onRefresh(true);
        case LOADING:
            // Wait to show ad to make sure it is loaded.
            Handler taskHandler = new Handler();
            taskHandler.postDelayed(new Runnable() {
                public void run() {
                    InterstitialOverlay.this.lastAttemptedShow = System.currentTimeMillis();
                    InterstitialOverlay.this.show();
                }
            }, this.millisBeforeShow);

            break;
        case LOADED:
            // Ad is loaded. Show immediately unless the current orientation is incorrect for the ad we fetched.
            DisplayMetrics dm = context.getResources().getDisplayMetrics();
            String orientationString = (dm.widthPixels > dm.heightPixels) ? "landscape" : "portrait";
            if (this.currentAdUnit.getRequiredOrientation() != null && !this.currentAdUnit.getRequiredOrientation().equals(orientationString)) {
                this.currentAdUnit.onRefresh(true);
                this.show();
                break;
            } else {
                this.lastAttemptedShow = 0; //reset this value
                shownAt = System.currentTimeMillis();
                if (!this.isShown()) {
                	
                	// Render the ad
                	this.render();
                	
                	// Send JS event
                	this.adWrapper.webview.loadUrl("javascript: try{adViewShown();}catch(e){}");

                	// Show it
                	super.show();
                	
                	// Register impression
                    this.currentAdUnit.onImpression();
                }
                break;
            }
        }
    }

    private boolean isOutOfBounds(MotionEvent event) {
        final int x = (int) event.getX();
        final int y = (int) event.getY();
        final int slop = ViewConfiguration.get(getContext()).getScaledWindowTouchSlop();
        final View decorView = this;
        return (x < -slop) || (y < -slop) || (x > (decorView.getWidth() + slop)) || (y > (decorView.getHeight() + slop));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            this.hide();
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }
    
    /* Ad Unit Tags */
    
    private AdUnit getOrCreateAdUnitWithTag(String tag) {
    	
    	Boolean prefetch = false; // We don't want to prefetch for non-default ad units.
    	if (tag == null || tag.length() == 0) {
    		tag = AdUnit.DEFAULT_TAG_NAME;
    	}
    	
    	if (tag.equals(AdUnit.DEFAULT_TAG_NAME)) {
    		prefetch = true;
    	}
    	
    	AdUnit adUnit = this.adUnits.get(tag);
    	if (adUnit == null) {
    		adUnit = new InterstitialAdUnit(this.context, tag, this.listener, prefetch);
            adUnit.setOnStateChangeHandler(adUnit.new OnAdStateChangeHandler() {
                @Override
                public void onStateChange(AdUnit adUnit, AdUnit.AdState state) {
                    if (state == AdState.LOADED) {
                    	InterstitialOverlay.this.adWrapper.renderAdUnit(adUnit);
                    }

                    if (state == AdState.NONE) {
                    	
                    }
                }
            });
            
    		this.adUnits.put(tag, adUnit);
    	}
    	
    	return adUnit;
    }    

    /* Singleton */

    protected static InterstitialOverlay getInstance() {
        return instance;
    }

    protected static void load(Context context) {
        load(context, null);
    }

    protected static void load(Context context, OnAdDisplayListener listener) {
        if (instance == null) {
            instance = new InterstitialOverlay(context, listener);
        }
    }
    
    /* Public Visible Methods */
    
    public static void fetch(boolean forced, String tag) {
    	if (getInstance() == null) {
    		return;
    	}
    	
    	AdUnit adUnit = getInstance().getOrCreateAdUnitWithTag(tag);
    	
    	if ((adUnit.getState() != AdState.LOADED && adUnit.getState() != AdState.LOADING) || forced) {
    		adUnit.onRefresh(true);
    		return;
    	}
    	
    	if (adUnit.getState() == AdState.LOADED) {
    		adUnit.onAvailable();
    		return;
    	}
    }
    
    public static void fetch() {
    	fetch(false, AdUnit.DEFAULT_TAG_NAME);
    }
    
    public static void fetch(String tag) {
    	fetch(false, tag);
    }

    public static Boolean isAvailable() {
    	return isAvailable(AdUnit.DEFAULT_TAG_NAME);
    }
    
    public static Boolean isAvailable(String tag) {
        if (instance != null && Connectivity.isConnected(instance.getContext())) {
        	if (tag == null || tag.length() == 0) {
        		tag = AdUnit.DEFAULT_TAG_NAME;
        	}
        	
        	AdUnit adUnit = instance.adUnits.get(tag);
        	return (adUnit != null && adUnit.getState() == AdState.LOADED);
        }
        
        return false;
    }
    
    public static void display(final Context context, final String tag) {
        Activity anActivity = (Activity) context;
        
        // Start HeyzapAds if it hasn't been started yet
        if (!HeyzapAds.hasStarted()) {
        	HeyzapAds.start(context);
        }
        
        anActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
            	
                if (instance == null) {
                    InterstitialOverlay.load(context);
                } else {
                	instance.setContext(context);
                }

                instance.show(tag);
            }
        });
    }

    public static void display(final Context context) {
        display(context, AdUnit.DEFAULT_TAG_NAME);
    }

    public static void dismiss() {
        if (getInstance() != null) {
        	getInstance().hide();
        }
    }

    public static void setDisplayListener(OnAdDisplayListener listener) {
        if (instance != null) {
     		instance.listener = listener;
         	Set<String> adUnitTagSet = instance.adUnits.keySet();
         	for (String tag : adUnitTagSet) {
         		instance.adUnits.get(tag).setDisplayListener(listener);
         	}
        }
    }
}
