package com.hightechsoftware.whatstheword.utility;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory.Options;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.util.TypedValue;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;

import com.hightechsoftware.whatstheword.common.LetterInfo;

public class Utility {

	public static Animation setLayoutAnim_slideUp() {

		AnimationSet set = new AnimationSet(true);

		Animation animation = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, -1.0f);
		animation.setDuration(800);
		animation.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
				// MapContacts.this.mapviewgroup.setVisibility(View.VISIBLE);

			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub				

			}
		});
		set.addAnimation(animation);

		return animation;
	}
	
	public static Animation setLayoutAnim_slidedown() {

		AnimationSet set = new AnimationSet(true);

		Animation animation = new TranslateAnimation(
				Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
				0.0f, Animation.RELATIVE_TO_SELF, -1.0f,
				Animation.RELATIVE_TO_SELF, 0.0f);
		animation.setDuration(800);
		animation.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
				// MapContacts.this.mapviewgroup.setVisibility(View.VISIBLE);

			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub				

			}
		});
		set.addAnimation(animation);

		return animation;
	}
	
	/**
	 * Get drawable from the asset location
	 * @param ctx
	 * @param location
	 * @param originalDensity
	 * @return
	 */
	public static Drawable DrawableFromAsset(Context ctx, String location,
			int originalDensity, int sampleSize)
	{
		Drawable drawable = null;

		// set options to resize the image
	    Options opts = new BitmapFactory.Options();
	    opts.inDensity = originalDensity;
	    opts.inSampleSize = sampleSize;
	    
	    try {
	      drawable = Drawable.createFromResourceStream(ctx.getResources(), null, ctx.getAssets().open(location), location, opts);
	      //drawable = Drawable.createFromStream(ctx.getAssets().open(location), null);
	    } catch (Throwable e) {
	      // handle
	    } 
	    return drawable;
	}
	/**
	 * Parse the string, and return an array of TargetWordInfo
	 * @param str Full, concatenated String
	 * @param visible default visibility status
	 * @return
	 */
	public static ArrayList<LetterInfo> ParseString(String str, boolean visible)
	{
		ArrayList<LetterInfo> list = new ArrayList<LetterInfo>();
		for(int i=0;i<str.length();i++)
		{
			LetterInfo word = new LetterInfo();
			word.Letter = str.substring(i, i+1);
			word.Visible = visible;
			list.add(word);
		}
		return list;
	}
		
	/**
	 * Craete an empty String, and return an array of LetterInfo
	 * @return
	 */
	public static ArrayList<LetterInfo> GenerateBlankArray(String val, String word, boolean visible)
	{
		int length = word.length();
		ArrayList<LetterInfo> list = new ArrayList<LetterInfo>();
		
		for(int i=0;i<length;i++)
		{	
			LetterInfo letter = new LetterInfo();				
			letter.Letter = val;
			letter.Visible = visible;				
			list.add(letter);
		}
		return list;
	}
	
	/**
	 * Generate an Array of Letters 
	 * @param fixed
	 * @param optional
	 * @param max
	 * @return
	 */
	public static ArrayList<LetterInfo> RandomLetters(String fixed, String optional, int max, boolean visible)
	{
		long seed = System.nanoTime();
		int location = 0;		
		ArrayList<LetterInfo> list = ParseString(fixed, visible);
		int size = max-list.size();
		
		for(int i=0;i<size;i++)
		{
			LetterInfo word = new LetterInfo();
			word.Letter = optional.substring(location,location+1);
			word.Visible = visible;
			list.add(word);
		}
		Collections.shuffle(list, new Random(seed));
		return list;
	}
	
	/**
	 * Rate the app on Google Play
	 * 
	 * @param ctx
	 */
	
	public static void RateApp(Context ctx) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		// Try Google play
		intent.setData(Uri.parse("market://details?id="+ctx.getPackageName()));
		//intent.setData(Uri.parse(ctx.getString(R.string.share_marketUri)));
		try {
			ctx.startActivity(intent);
		} catch (ActivityNotFoundException e) {
			// Market (Google play) app seems not installed, let's try to open a
			// webbrowser
			intent.setData(Uri.parse(ctx.getPackageName()));			
			try {
				ctx.startActivity(intent);
			} catch (ActivityNotFoundException e1) {
			}
		}
	}
	
	/**
	 * Share the application via Twiter.
	 * 
	 * @param ctx
	 */

	public static void ShareViaTwiter(Context ctx, String text) {
		Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
		shareIntent.setType("text/plain");
		shareIntent.putExtra(android.content.Intent.EXTRA_TEXT,text);
		
		List<ResolveInfo> activityList = ctx.getPackageManager().queryIntentActivities(shareIntent,0);
		for (final ResolveInfo app : activityList) {
			if ("com.twitter.android.PostActivity"
					.equals(app.activityInfo.name)) {
				final ActivityInfo activity = app.activityInfo;
				final ComponentName name = new ComponentName(
						activity.applicationInfo.packageName, activity.name);
				shareIntent.addCategory(Intent.CATEGORY_LAUNCHER);
				shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
						| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
				shareIntent.setComponent(name);
				ctx.startActivity(shareIntent);
				break;
			}
		}
	}

	public static void ShareViaFacebook(Context ctx, String subject, String text) {
		Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
		shareIntent.setType("text/plain");
		shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,subject);
		shareIntent.putExtra(
				android.content.Intent.EXTRA_TEXT,text);
		PackageManager pm = ctx.getPackageManager();
		List<ResolveInfo> activityList = pm.queryIntentActivities(shareIntent,
				0);
		for (final ResolveInfo app : activityList) {
			if ((app.activityInfo.name).contains("facebook")) {
				final ActivityInfo activity = app.activityInfo;
				final ComponentName name = new ComponentName(
						activity.applicationInfo.packageName, activity.name);
				shareIntent.addCategory(Intent.CATEGORY_LAUNCHER);
				shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
						| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
				shareIntent.setComponent(name);
				ctx.startActivity(shareIntent);
				break;
			}
		}
	}
	
	/**
	 * Get the statusBar heigh
	 * @return
	 */
	public static int getStatusBarHeight(Context ctx) {
		  int result = 0;
		  int resourceId = ctx.getResources().getIdentifier("status_bar_height", "dimen", "android");
		  if (resourceId > 0) {
		      result = ctx.getResources().getDimensionPixelSize(resourceId);
		  }
		  return result;
		}
	
	/**
	 * Save the bitmap image to Camera folder
	 * @param bitmap
	 * @return
	 */
	public static File saveBitmapToCameraFolder(Bitmap bitmap)
	{
		File root = Environment.getExternalStorageDirectory();
        File file = new File(root.getAbsolutePath()+"/DCIM/Camera/screenshot.png");
        try 
        {
            file.createNewFile();
            FileOutputStream ostream = new FileOutputStream(file);
            bitmap.compress(CompressFormat.PNG, 100, ostream);
            ostream.close();
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }
        return file;
	}
	
	/**
	 * 
	 * @param file
	 */
	public static void Share(Context ctx, File file, String title)
	{
		Intent share = new Intent(Intent.ACTION_SEND);		
		share.setType("image/png");// image/jpeg if it's a jpeg

		//share.putExtra(Intent.EXTRA_STREAM, Uri.parse(imagePath));
		share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));

		ctx.startActivity(Intent.createChooser(share, title));
	}
	
	public static int convertDpToPixels(float dp, Context context){
	    Resources resources = context.getResources();
	    return (int) TypedValue.applyDimension(
	            TypedValue.COMPLEX_UNIT_DIP,
	            dp, 
	            resources.getDisplayMetrics()
	    );
	}
}
