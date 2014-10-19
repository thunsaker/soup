package com.thunsaker.soup.app;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.thunsaker.android.common.dagger.DaggerApplication;

import java.util.Collections;
import java.util.List;

public class SoupApp extends DaggerApplication {

    public static SoupApp from(Context context) {
        return (SoupApp) context.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            PackageManager manager = getPackageManager();
            ApplicationInfo applicationInfo = manager.getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = applicationInfo.metaData;
            // NOTE: Not a great way to do crashlytics through an environmental variable right now
            bundle.putString("com.google.android.maps.v2.API_KEY", System.getenv("SOUP_MAPS_KEY"));
            applicationInfo.metaData = bundle;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected List<Object> getAppModules() {
        return Collections.<Object>singletonList(new SoupAppModule());
    }
}