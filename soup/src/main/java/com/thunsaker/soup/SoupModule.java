package com.thunsaker.soup;

import android.content.Context;
import android.location.LocationManager;

import com.thunsaker.android.common.annotations.ForApplication;
import com.thunsaker.soup.app.SoupApp;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import de.greenrobot.event.EventBus;

import static android.content.Context.LOCATION_SERVICE;

@Module(
        injects = {
                SoupApp.class
        },
        library = true
)

public class SoupModule {
    private SoupApp app;

    public SoupModule(SoupApp app) {
        this.app = app;
    }

    @Provides
    @Singleton
    @ForApplication
    Context provideApplication() {
        return app;
    }

    @Provides
    @Singleton
    LocationManager provideLocationManager() {
        return (LocationManager) app.getSystemService(LOCATION_SERVICE);
    }

    @Provides
    @Singleton
    EventBus provideBus() {
        return new EventBus();
    }
}