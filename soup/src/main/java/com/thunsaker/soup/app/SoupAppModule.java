package com.thunsaker.soup.app;

import android.app.NotificationManager;
import android.content.Context;
import android.location.LocationManager;

import com.squareup.picasso.Picasso;
import com.thunsaker.android.common.annotations.ForApplication;
import com.thunsaker.android.common.dagger.AndroidApplicationModule;
import com.thunsaker.soup.BuildConfig;
import com.thunsaker.soup.services.FoursquareService;
import com.thunsaker.soup.services.foursquare.FoursquarePrefs;
import com.thunsaker.soup.services.foursquare.FoursquareTasks;
import com.thunsaker.soup.services.foursquare.endpoints.VenueEndpoint;
import com.thunsaker.soup.ui.FoursquareAuthorizationActivity;
import com.thunsaker.soup.ui.FoursquareListFragment;
import com.thunsaker.soup.ui.HistoryActivity;
import com.thunsaker.soup.ui.ListActivity;
import com.thunsaker.soup.ui.ListsFragment;
import com.thunsaker.soup.ui.MainActivity;
import com.thunsaker.soup.ui.VenueAddCategoryActivity;
import com.thunsaker.soup.ui.VenueDetailActivity;
import com.thunsaker.soup.ui.VenueDetailActivityReceiver;
import com.thunsaker.soup.ui.VenueDetailFragment;
import com.thunsaker.soup.ui.VenueEditCategoriesActivity;
import com.thunsaker.soup.ui.VenueEditHoursAddActivity;
import com.thunsaker.soup.ui.VenueListFragment;
import com.thunsaker.soup.ui.VenueSearchActivity;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;

import static android.content.Context.LOCATION_SERVICE;

@Module(
        complete = true,
        library = true,
        addsTo = AndroidApplicationModule.class,
        injects = {
                SoupApp.class,
                FoursquareAuthorizationActivity.class,
                MainActivity.class,
                FoursquareTasks.class,
                VenueEndpoint.class,
                VenueListFragment.class,
                VenueSearchActivity.class,
                VenueDetailFragment.class,
                ListsFragment.class,
                FoursquareListFragment.class,
                ListActivity.class,
                HistoryActivity.class,
                VenueDetailActivity.class,
                VenueDetailActivityReceiver.class,
                VenueAddCategoryActivity.class,
                VenueEditHoursAddActivity.class,
                VenueEditCategoriesActivity.class
        }
)
public class SoupAppModule {
    public SoupAppModule() { }

    @Provides
    Picasso providesPicasso(@ForApplication Context context) {
        Picasso picasso = Picasso.with(context);

        picasso.setDebugging(BuildConfig.DEBUG);
        return picasso;
    }

    @Provides
    @Singleton
    NotificationManager providesNotificationManager(@ForApplication Context myContext) {
        return (NotificationManager) myContext.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Provides
    @Singleton
    LocationManager provideLocationManager(@ForApplication Context context) {
        return (LocationManager) context.getSystemService(LOCATION_SERVICE);
    }

    @Provides
    @Singleton
    FoursquareService providesFoursquareService() {
        RequestInterceptor requestInterceptor = new RequestInterceptor() {
            @Override
            public void intercept(RequestFacade request) {
                request.addQueryParam("v", FoursquarePrefs.CURRENT_API_DATE);
            }
        };

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(FoursquarePrefs.FOURSQUARE_BASE_URL)
                .setRequestInterceptor(requestInterceptor)
                .build();
        return restAdapter.create(FoursquareService.class);
    }
}