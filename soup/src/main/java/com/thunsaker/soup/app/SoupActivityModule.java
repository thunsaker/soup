package com.thunsaker.soup.app;

import android.app.Activity;
import android.content.Context;

import com.thunsaker.android.common.annotations.ForActivity;
import com.thunsaker.soup.ui.MainActivity;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        complete = true,
        library =  true,
        addsTo = SoupAppModule.class,
        injects = {
                MainActivity.class
        }
)
public class SoupActivityModule {
    private final BaseSoupActivity mActivity;

    public SoupActivityModule(BaseSoupActivity activity) {
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
