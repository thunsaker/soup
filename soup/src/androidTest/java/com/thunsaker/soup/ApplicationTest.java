package com.thunsaker.soup;

import com.thunsaker.soup.ui.MainActivity;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static com.thunsaker.soup.app.TestSoupApp.injectMocks;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class ApplicationTest {
    private MainActivity activity;

    @Before
    public void setUp() {
        injectMocks(this);
//        activity = Robolectric.buildActivity(MainActivity.class).create().get();
    }

    @Test
    public void isThisThingOn() {
        Assert.assertTrue(true);
    }

//    @Test
//    public void checkActivityNotNull() throws Exception {
//        Assert.assertNotNull(activity);
//    }

//    @Test
//    public void testInjection() throws Exception {
//        Context actual = activity.getApplicationContext();
//        Assert.assertEquals(mContext, actual);
//    }
}