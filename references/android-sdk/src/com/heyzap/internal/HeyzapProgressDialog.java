package com.heyzap.internal;

import android.app.ProgressDialog;
import android.content.Context;

public class HeyzapProgressDialog extends ProgressDialog {

	public HeyzapProgressDialog(Context context) {
		super(context);
	}
	
	public HeyzapProgressDialog(Context context, int theme) {
		super(context, theme);
	}
	
	@Override
	public void show(){
		try{
			super.show();
		}catch(RuntimeException e){
			e.printStackTrace();
		}
	}
	
	@Override
	public void hide(){
		try{
			super.hide();
		}catch(RuntimeException e){
			e.printStackTrace();
		}
	}
	
	@Override
	public void dismiss(){
		try{
			super.dismiss();
		}catch(RuntimeException e){
			e.printStackTrace();
		}
	}
	
	
	

	public static ProgressDialog show(Context context, CharSequence title,
			CharSequence message) {
		return show(context, title, message, false);
	}

	public static HeyzapProgressDialog show(Context context, CharSequence title,
			CharSequence message, boolean indeterminate) {
		return show(context, title, message, indeterminate, false, null);
	}

	public static HeyzapProgressDialog show(Context context, CharSequence title,
			CharSequence message, boolean indeterminate, boolean cancelable) {
		return show(context, title, message, indeterminate, cancelable, null);
	}

	public static HeyzapProgressDialog show(
			Context context, 
			CharSequence title, 
			CharSequence message, 
			boolean indeterminate, 
			boolean cancelable, 
			OnCancelListener cancelListener
	) {
		HeyzapProgressDialog dialog = new HeyzapProgressDialog(context);
		dialog.setTitle(title);
		dialog.setMessage(message);
		dialog.setIndeterminate(indeterminate);
		dialog.setCancelable(cancelable);
		dialog.setOnCancelListener(cancelListener);
		dialog.show();
		return dialog;
	}

}
