package com.heyzap.sdk;

import java.io.InputStream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.heyzap.internal.Analytics;
import com.heyzap.internal.ClickableToast;
import com.heyzap.internal.Logger;
import com.heyzap.sdk.Rzap;

public class AchievementEarnedDialogTop extends ClickableToast {
    private Context context;
    private boolean fromSdk;
    private View.OnClickListener showInGameOverlayOrLaunchLeaderboardActivity;
    private JSONObject response;
    private Runnable autoHide;
    private View wrapper;
    private long shownAt;
    private String achievementImageUrl;
    private String achievementName;
    private String achievementDescription;
    private boolean hasAchievements = false;
    private boolean downloadingImage = false;
    private Bitmap downloadedImage;

    public AchievementEarnedDialogTop(Context context, JSONObject response) {
        super(context);
        this.response = response;
        this.context = context;
        
        Logger.log(response.toString());
        
        try {
	        JSONArray achievements = response.getJSONArray("achievements");
	        JSONObject topAchievement;
	        
	        if (achievements.length() > 0) {
	        	topAchievement = achievements.getJSONObject(0);
	        	this.achievementImageUrl = topAchievement.getString("image_url");
	        	this.achievementName = topAchievement.getString("name");
	        	this.achievementDescription = topAchievement.getString("description");
	        	hasAchievements = true;
	        }
        } catch (JSONException e) {
        	
        }
        
        this.setContentView(Rzap.layout("achievement_earned_dialog_top"));

        showInGameOverlayOrLaunchLeaderboardActivity = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // This is what this class does in the Heyzap SDK:
            	HeyzapLib.showAchievements(AchievementEarnedDialogTop.this.context);
                AchievementEarnedDialogTop.this.hide();
            }
        };

        wrapper = findViewById(Rzap.id("wrapper"));
        wrapper.setOnClickListener(showInGameOverlayOrLaunchLeaderboardActivity);

        // Use portrait width, regardless of orientation
        RelativeLayout container = (RelativeLayout) findViewById(Rzap.id("container"));
        FrameLayout.LayoutParams p = (FrameLayout.LayoutParams) container.getLayoutParams();
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        p.width = Math.min(metrics.widthPixels, metrics.heightPixels);
        container.setLayoutParams(p);

        final Animation slide = AnimationUtils.loadAnimation(getContext(), Rzap.anim("slide_from_top"));
        Logger.log("starting slide in");
        wrapper.startAnimation(slide);

        autoHide = new Runnable() {

            @Override
            public void run() {
                AchievementEarnedDialogTop.this.hide();
            }
        };

        wrapper.postDelayed(autoHide, 7000);
    }

    public void setFromSdk(boolean fromSdk) {
        this.fromSdk = fromSdk;
    }

    @Override
    public WindowManager.LayoutParams getWmParams() {
        WindowManager.LayoutParams params = super.getWmParams();
        params.gravity = Gravity.TOP;
        params.width = WindowManager.LayoutParams.FILL_PARENT;
        params.verticalMargin = 0.0f;
        params.horizontalMargin = 0.0f;
        params.flags &= ~WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        params.flags |= WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;

        return params;
    }

    public void hide() {
        wrapper.removeCallbacks(autoHide);

        slideUp(new Runnable() {

            @Override
            public void run() {
                AchievementEarnedDialogTop.super.hide();
            }
        });
    }

    public void slideUp(Runnable after) {
        slide(Rzap.anim("slide_up"), after);
    }

    public void slide(int anim, Runnable after) {
        Animation animation = AnimationUtils.loadAnimation(getContext(), anim);
        wrapper.startAnimation(animation);
        if (after != null) {
            wrapper.postDelayed(after, (int) animation.getDuration());
        }
    }
    
    private void setupViews() {
        TextView titleView = (TextView) this.findViewById(Rzap.id("title"));
        TextView ctaView = (TextView) this.findViewById(Rzap.id("cta"));
        final ImageView pictureView = (ImageView) this.findViewById(Rzap.id("picture"));
        
        if (achievementImageUrl != null && !achievementImageUrl.equals("") && !achievementImageUrl.equals("null")) {
            if (!downloadingImage) {
            	downloadingImage = true;
            	
                new DownloadImageTask(new DownloadImageListener() {

                    @Override
                    public void onImageDownloaded(Bitmap bitmap) {
                    	downloadedImage = bitmap;
                        pictureView.setImageBitmap(downloadedImage);
                        downloadingImage = false;
                    }
                }).execute(achievementImageUrl);
            }
            
            if (downloadedImage != null) {
            	pictureView.setImageBitmap(downloadedImage);
            } else {
            	pictureView.setImageDrawable(null);
            }
        } else {
            pictureView.setImageDrawable(null);
        }
        
        titleView.setText(Html.fromHtml(String.format("<font color='#52a600'>%s</font>", achievementName)));
        ctaView.setText(Html.fromHtml(String.format("%s", achievementDescription)));
        
        ctaView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
    }

    @Override
    public void show() {
    	if (!hasAchievements) {
    		Logger.log("No achievements to show.");
    	}
    	
        shownAt = System.currentTimeMillis();
        setupViews();
        super.show();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_OUTSIDE && System.currentTimeMillis() > shownAt + 1000) {
            this.hide();
            return false;
        } else {
            return super.onTouchEvent(event);
        }
    }
    
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        DownloadImageListener listener;

        public DownloadImageTask(DownloadImageListener listener) {
            this.listener = listener;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Logger.log("Error: ", urldisplay, e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            if (listener != null) {
                listener.onImageDownloaded(result);
            }
        }
    }

    private interface DownloadImageListener {
        public void onImageDownloaded(Bitmap bitmap);
    }
}
