package com.thunsaker.soup.services.foursquare;

import com.thunsaker.soup.services.FoursquareService;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import javax.inject.Inject;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class FoursquareServiceTest {
    @Inject
    FoursquareService mFoursquareService;

    @Inject
    SwarmService mSwarmService;

    @Test
    public void testHasFoursquareId() throws Exception {
        Assert.assertTrue(System.getenv("SOUP_FOUR_ID") != null);
    }

    @Test
    public void testHasFoursquareSecret() throws Exception {
        Assert.assertTrue(System.getenv("SOUP_FOUR_ID") != null);
    }

//    @Test
//    public void testHasInjectedFoursquareService() throws Exception {
//        Assert.assertTrue(mFoursquareService != null);
//    }
//
//    @Test
//    public void testHasInjectedSwarmService() throws Exception {
//        Assert.assertTrue(mSwarmService != null);
//    }
}