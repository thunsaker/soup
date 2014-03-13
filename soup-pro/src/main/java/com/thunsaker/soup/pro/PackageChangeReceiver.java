package com.thunsaker.soup.pro;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

public class PackageChangeReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		ComponentName componentToEnable = new ComponentName(context, MainActivity.SOUP_PRO_LAUNCHER_ACTIVITY);
		PackageManager pm = context.getPackageManager();
		pm.setComponentEnabledSetting(componentToEnable, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
	}
}
