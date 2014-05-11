package com.heyzap.sdk.ads;

public interface OnAdDisplayListener {
	public void onShow(String tag);
	public void onClick(String tag);
	public void onHide(String tag);
	public void onFailedToShow(String tag);
	public void onAvailable(String tag);
	public void onFailedToFetch(String tag);
}