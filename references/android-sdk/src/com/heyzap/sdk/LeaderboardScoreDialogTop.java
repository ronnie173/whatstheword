package com.heyzap.sdk;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.drawable.Drawable;
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

import com.heyzap.http.RequestParams;
import com.heyzap.internal.ClickableToast;
import com.heyzap.internal.Analytics;
import com.heyzap.internal.Logger;
import com.heyzap.sdk.Rzap;
import com.heyzap.internal.APIResponseHandler;
import com.heyzap.internal.APIClient;

public class LeaderboardScoreDialogTop extends ClickableToast {
    private Context context;
    private String gamePackage;
    private String score;
    private String displayScore;
    private String levelId;
    private boolean fromSdk;
    private View.OnClickListener showInGameOverlayOrLaunchLeaderboardActivity;
    private JSONObject response;
    private Runnable autoHide;
    private View wrapper;
    private String bestDisplayScore = null;
    private long shownAt;
    private boolean personalBest;
    private Drawable gameIcon;

    public LeaderboardScoreDialogTop(Context context, JSONObject response, String gamePackage, String score, String displayScore, String levelId) {
        this(context, response, gamePackage, score, displayScore, levelId, null, null);
    }

    public LeaderboardScoreDialogTop(Context context, JSONObject response, String gamePackage, String score, String displayScore, String levelId,
            String personalBestFromPhone, Drawable gameIcon) {
        super(context);
        this.response = response;
        this.context = context;
        this.displayScore = displayScore;
        this.gameIcon = gameIcon;
        this.bestDisplayScore = personalBestFromPhone;
        
        this.setContentView(Rzap.layout("leaderboard_score_dialog_top"));

        showInGameOverlayOrLaunchLeaderboardActivity = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // This is what this class does in the Heyzap SDK:
                Analytics.trackEvent(getContext(), "score-overlay-clicked-top");
                HeyzapLib.showInGameOverlay(LeaderboardScoreDialogTop.this.context, null, "top");
                LeaderboardScoreDialogTop.this.hide();
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
                LeaderboardScoreDialogTop.this.hide();
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
                LeaderboardScoreDialogTop.super.hide();
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
        ImageView pictureView = (ImageView) this.findViewById(Rzap.id("picture"));

        titleView.setText(Html.fromHtml(String.format("You scored <font color='#52a600'>%s</font>!", displayScore)));

        if (personalBest) {
            ctaView.setText(Html.fromHtml(String.format("<font color='#52a600'>New personal best!</font> Sign in to save.", displayScore)));
        } else {
            ctaView.setText(Html.fromHtml(String.format("Personal best: <font color='#52a600'>%s</font>. Sign in to save.", bestDisplayScore)));
        }
        ctaView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);

        pictureView.setBackgroundResource(Rzap.drawable("icon_default_people"));
        if (gameIcon != null) {
            pictureView.setImageDrawable(gameIcon);
        }
    }

    @Override
    public void show() {
        Analytics.trackEvent(getContext(), "score-overlay-shown-top");
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

    public void setPersonalBest(boolean personalBest) {
        this.personalBest = personalBest;
    }

}
