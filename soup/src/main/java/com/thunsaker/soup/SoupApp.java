package com.thunsaker.soup;

import android.app.Application;
import android.content.Context;

import dagger.ObjectGraph;

public class SoupApp extends Application {
    private ObjectGraph mObjectGraph;

    public static SoupApp get(Context context) {
        return (SoupApp) context.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mObjectGraph = ObjectGraph.create(new SoupModule(this));
        SoupApp soupApplication = mObjectGraph.get(SoupApp.class);
    }

    public void inject(Object object) {
        mObjectGraph.inject(object);
    }
}
