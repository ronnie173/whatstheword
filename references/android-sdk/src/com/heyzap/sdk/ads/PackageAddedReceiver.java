package com.heyzap.sdk.ads;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PackageAddedReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent){
		// make sure it's not an update
		if (intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)) return;
		
		final String packageName = intent.getDataString().replaceFirst(intent.getScheme()+":", "");
		
		if(packageName == null) return;
		
		Manager.applicationContext = context.getApplicationContext();

        // only register the install if we've recently advertised the game
        String impression = Manager.getInstance().getGameImpression(packageName);
        if (impression != null) {
            Manager.getInstance().registerInstall(packageName, impression);
        }
	}

}
