package com.thunsaker.soup.app;

import android.content.Context;

import com.thunsaker.android.common.annotations.ForActivity;
import com.thunsaker.soup.ApplicationTest;
import com.thunsaker.soup.services.foursquare.FoursquareServiceTest;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

import static org.mockito.Mockito.mock;

@Module(
        complete = true,
        library = true,
        overrides = true,
        addsTo = SoupActivityModule.class,
        injects = {
                TestSoupApp.class,
                FoursquareServiceTest.class,
                ApplicationTest.class
        }
)

public class SoupTestModule {
    @Provides
    @Singleton
    @ForActivity
    Context providesActivityContext() {
        return mock(BaseSoupActivity.class);
    }
}
