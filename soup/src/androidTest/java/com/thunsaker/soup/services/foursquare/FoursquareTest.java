package com.thunsaker.soup.services.foursquare;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class FoursquareTest {
    @Test
    public void testHasFoursquareId() throws Exception {
        String foursquare_id = System.getenv("SOUP_FOUR_ID");
        Assert.assertTrue(foursquare_id != null);
    }
}