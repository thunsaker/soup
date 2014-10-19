package com.thunsaker.soup.app;

import android.app.Activity;
import android.content.Context;

import com.thunsaker.android.common.annotations.ForActivity;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        complete = true,
        library =  true,
        addsTo = SoupAppModule.class
)
public class SoupActivityModule {
    private final Activity mActivity;

    public SoupActivityModule(Activity activity) {
        mActivity = activity;
    }

    @Provides
    @Singleton
    @ForActivity
    Context providesActivityContext() {
        return mActivity;
    }

    @Provides
    @Singleton
    Activity providesActivity() {
        return mActivity;
    }
}
