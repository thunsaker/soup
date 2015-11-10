package com.thunsaker.soup.app;

import android.content.Context;

import com.thunsaker.android.common.dagger.DaggerApplication;

import java.util.Collections;
import java.util.List;

public class SoupApp extends DaggerApplication {

    public static SoupApp from(Context context) {
        return (SoupApp) context.getApplicationContext();
    }

    @Override
    protected List<Object> getAppModules() {
        return Collections.<Object>singletonList(new SoupAppModule());
    }
}