package com.thunsaker.soup;

/*
 * Created by 20462660 on 3/13/14.
 */

import android.content.Context;
import android.location.LocationManager;

import com.thunsaker.android.common.annotations.ForApplication;
import com.thunsaker.soup.app.SoupApp;
import com.thunsaker.soup.util.foursquare.VenueEndpoint;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import de.greenrobot.event.EventBus;

import static android.content.Context.LOCATION_SERVICE;

@Module(
//        includes = {
//                UiModule.class,
//                DataModule.class
//        },
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

//    @Provides
//    @Singleton
//    Settings provideSettings(Context context) { return new Settings(context); }
//
//    @Provides
//    FoursquareService provideFoursquareService(RestAdapter restAdapter) {
//        return restAdapter.create(FoursquareService.class);
//    }
//
//    @Provides
//    RestAdapter provideRestAdapter(Settings settings) {
//        final String accessToken = settings.getFoursquareToken();
//
//        return new RestAdapter.Builder()
//                .setEndpoint("https://api.foursquare.com")
//                .setRequestInterceptor(new RequestInterceptor() {
//                    @Override
//                    public void intercept(RequestFacade requestFacade) {
//                        requestFacade.addEncodedQueryParam("oauth_token", accessToken);
//                    }
//                })
//                .build();
//    }

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

    @Provides
    @Singleton
    VenueEndpoint provideVenueEndpoint() { return new VenueEndpoint();}
}