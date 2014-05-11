package com.heyzap.internal;

import java.io.ByteArrayInputStream;
import java.io.InputStream;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.NinePatchDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Build;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;

public class Drawables {
    public static final int DIALOG_BUTTON_BACKGROUND = 1;
    public static final int PRIMARY_BUTTON_BACKGROUND = 2;
    public static final int SECONDARY_BUTTON_BACKGROUND = 3;
    public static final int DIALOG_SPLASH_BACKGROUND = 4;
	private static Bitmap buttonClose;
	private static Bitmap heyzapCorner;
    
    
    
    public static void setBackgroundDrawable(final Context context, final View view, final int id){
        final Handler h = new Handler();
        new Thread(new Runnable(){
            @Override
            public void run(){
                final Drawable d = Drawables.getDrawable(context, id);
                h.post(new Runnable(){
                    @Override public void run(){
                        view.setBackgroundDrawable(d);
                    }
                });
            }
        }).start();
    }
    
    public static void setBackgroundDrawable(final View view, final String path){
        setBackgroundDrawable(view.getContext(), view, path, null);
    }
    
    public static void setBackgroundDrawable(final Context context, final View view, final String path, final Runnable callback){
        final Handler h = new Handler();
        new Thread(new Runnable(){
            @Override
            public void run(){
                final Drawable d = Drawables.getDrawable(context, path);
                h.post(new Runnable(){
                    @Override public void run(){
                        view.setBackgroundDrawable(d);
                        if(callback != null) callback.run();
                    }
                });
            }
        }).start();
    }
    

    public static void setImageDrawable(final Context context, final ImageView view, final int id){
        final Handler h = new Handler();
        new Thread(new Runnable(){
            @Override
            public void run(){
                final Drawable d = Drawables.getDrawable(context, id);
                h.post(new Runnable(){
                    @Override public void run(){
                        view.setImageDrawable(d);
                    }
                });
            }
        }).start();
    }
    
    public static void setImageDrawable(final ImageView view, String path){ 
        setImageDrawable(view.getContext(), view, path);
    }
     
    public static void setImageDrawable(final Context context, final ImageView view, final String path){
        final Handler h = new Handler();
        new Thread(new Runnable(){
            @Override
            public void run(){
                final Drawable d = Drawables.getDrawable(context, path);
                h.post(new Runnable(){
                    @Override public void run(){
                        view.setImageDrawable(d);
                    }
                });
            }
        }).start();
    }
    
    private static Drawable getDrawable(Context context, int id){
        switch(id){
        case DIALOG_BUTTON_BACKGROUND:    return getDialogButtonBackground(context);
        case PRIMARY_BUTTON_BACKGROUND:   return getPrimaryButtonBackground(context);
        case SECONDARY_BUTTON_BACKGROUND: return getSecondaryButtonBackground(context);
        case DIALOG_SPLASH_BACKGROUND:    return getSplashDialogBackground(context);
        default:                          return null;
        }
    }
    
    public static Drawable getSplashDialogBackground(Context context){
        int r = Utils.dpToPx(context, 0);
        RoundRectShape backgroundRect = new RoundRectShape(new float[]{r,r,r,r,r,r,r,r},null, new float[]{r,r,r,r,r,r,r,r});
        ShapeDrawable backgroundDrawable = new ShapeDrawable(backgroundRect);
        backgroundDrawable.getPaint().setColor(Color.parseColor("#d1d1d1"));
        
        LayerDrawable layers = new LayerDrawable(new Drawable[]{backgroundDrawable});
        return layers;
    }
    
    public static Drawable getDialogButtonBackground(Context context){
        int r = Utils.dpToPx(context, 5);
        RoundRectShape backgroundRect = new RoundRectShape(new float[]{0,0,0,0,r,r,r,r},null, new float[]{r,r,r,r,r,r,r,r});
        ShapeDrawable backgroundDrawable = new ShapeDrawable(backgroundRect);
        backgroundDrawable.getPaint().setColor(Color.parseColor("#bdbebd"));
        return backgroundDrawable;
    }
    
    public static Drawable getPrimaryButtonBackground(Context context){
        StateListDrawable states = new StateListDrawable();
        Drawable pressed = getDrawable(null, "dialog_grn_btn_sel.png");
        states.addState(new int[]{android.R.attr.state_pressed}, pressed);
        states.addState(new int[]{android.R.attr.state_focused}, pressed);
        states.addState(new int[]{android.R.attr.state_enabled}, getDrawable(null, "dialog_grn_btn.png"));
        return states;
    }
    
    public static Drawable getSecondaryButtonBackground(Context context){
        StateListDrawable states = new StateListDrawable();
        Drawable pressed = getDrawable(null, "dialog_btn_sel.png");
        states.addState(new int[]{android.R.attr.state_pressed}, pressed);
        states.addState(new int[]{android.R.attr.state_focused}, pressed);
        states.addState(new int[]{android.R.attr.state_enabled}, getDrawable(null, "dialog_btn.png"));
        return states;
    }
    
    public static Drawable getFacebookButtonBackground(Context context){
        StateListDrawable states = new StateListDrawable();
        Drawable down = getDrawable(context, "button_fb_down.9.png");
        Drawable up = getDrawable(context, "button_fb_up.9.png");
        states.addState(new int[]{android.R.attr.state_pressed}, down);
        states.addState(new int[]{android.R.attr.state_focused}, down);
        states.addState(new int[]{android.R.attr.state_enabled}, up);
        return states;
    }
    
    public static Drawable getHeyzapButtonBackground(Context context){
        StateListDrawable states = new StateListDrawable();
        Drawable down = getDrawable(context, "getheyzap_down.9.png");
        Drawable up = getDrawable(context, "getheyzap_up.9.png");
        states.addState(new int[]{android.R.attr.state_pressed}, down);
        states.addState(new int[]{android.R.attr.state_focused}, down);
        states.addState(new int[]{android.R.attr.state_enabled}, up);
        return states;
    }
    
    public static Drawable getSettingsBackground(Context context){
        StateListDrawable states = new StateListDrawable();
        Drawable down = getDrawable(context, "cog_down.png");
        Drawable up = getDrawable(context, "cog_up.png");
        states.addState(new int[]{android.R.attr.state_pressed}, down);
        states.addState(new int[]{android.R.attr.state_focused}, down);
        states.addState(new int[]{android.R.attr.state_enabled}, up);
        return states;
    }
    
    public static Drawable getTogleSlider(Context context){
        StateListDrawable states = new StateListDrawable();
        Drawable down = getDrawable(context, "toggle_slider_down.png");
        Drawable up = getDrawable(context, "toggle_slider_up.png");
        states.addState(new int[]{android.R.attr.state_pressed}, down);
        states.addState(new int[]{android.R.attr.state_focused}, down);
        states.addState(new int[]{android.R.attr.state_enabled}, up);
        return states;
    }
    
    public static Drawable getSmallFacebookButtonBackground(Context context){
        StateListDrawable states = new StateListDrawable();
        Drawable down = getDrawable(context, "fb_login_down.png");
        Drawable up = getDrawable(context, "fb_login_up.png");
        states.addState(new int[]{android.R.attr.state_pressed}, down);
        states.addState(new int[]{android.R.attr.state_focused}, down);
        states.addState(new int[]{android.R.attr.state_enabled}, up);
        return states;
    }
    

    public static Bitmap getButtonClose(Context context){
    	if(buttonClose == null){
    		byte[] data = Base64.decode("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", 0);
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inScaled = false;
            opts.inDensity = 240;
            opts.inTargetDensity = context.getResources().getDisplayMetrics().densityDpi;
            opts.inScreenDensity = context.getResources().getDisplayMetrics().densityDpi;

            buttonClose = BitmapFactory.decodeByteArray(data, 0, data.length, opts);
    	}
    	return buttonClose;
    }
    
    public static Bitmap getHeyzapCorner(Context context){
    	if(heyzapCorner == null){
    		byte[] data = Base64.decode("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", 0);
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inScaled = false;
            opts.inDensity = 240;
            opts.inTargetDensity = context.getResources().getDisplayMetrics().densityDpi;
            opts.inScreenDensity = context.getResources().getDisplayMetrics().densityDpi;

            heyzapCorner = BitmapFactory.decodeByteArray(data, 0, data.length, opts);
    	}
    	return heyzapCorner;
    }

    public static Bitmap getBitmap(Context context, String path){
    	InputStream stream;
    	if(path.equals("button_close.png")){
    		return getButtonClose(context);
    	}else if(path.equals("heyzap_corner.png")){
    		return getHeyzapCorner(context);
    	}else{
    		stream = Utils.class.getClassLoader().getResourceAsStream("res/drawable/hz_" + path);
    	}
    	
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inScaled = false;
        opts.inDensity = 240;
        opts.inTargetDensity = context.getResources().getDisplayMetrics().densityDpi;
        opts.inScreenDensity = context.getResources().getDisplayMetrics().densityDpi;

        Bitmap bmp = BitmapFactory.decodeStream(stream, null, opts);
        return bmp;
    }
    
    public static Drawable getDrawable(Context context, String path){
        Bitmap bmp = getBitmap(context, path);
        if(bmp == null){
            return null;
        }
        
        Drawable drawable = null;
        byte[] chunk = bmp.getNinePatchChunk();
        if(chunk != null){
            drawable = new NinePatchDrawable(bmp, chunk, NinePatchChunk.deserialize(chunk).mPaddings, path);
        }else{
            drawable = new BitmapDrawable(bmp);
        }
        try{
            if(Integer.parseInt(Build.VERSION.SDK) >= 4){
                new DrawableDensitySetter().setDensity(drawable, context.getResources().getDisplayMetrics().densityDpi, context.getResources());
            }
        }catch(Exception e){}//unnecessary

        return drawable;
    }
    
    public static class DrawableDensitySetter{
        // to make sure setDensity isn't loaded into 1.5's jvm
        public void setDensity(Drawable drawable, int density, Resources resources){
            if(drawable instanceof BitmapDrawable){
                ((BitmapDrawable) drawable).setTargetDensity(density);
            }
            if(drawable instanceof NinePatchDrawable){
                ((NinePatchDrawable) drawable).setTargetDensity(density);
            }
        }
    }
}
