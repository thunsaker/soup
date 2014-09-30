package com.thunsaker.soup.data.api.model;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class TimeFrameTest {
    @Test
    public void testCreateFoursquareApiStringHoursWithLabel() throws Exception {
        String expected = "6,1700,2100,Happy+Hour%3B";
        TimeFrame timeFrame = new TimeFrame();
        timeFrame.daysList = new ArrayList<Integer>();
        timeFrame.daysList.add(6);
        timeFrame.openTime = new ArrayList<String>();
        timeFrame.closeTime = new ArrayList<String>();
        timeFrame.openTime.add("1700");
        timeFrame.closeTime.add("2100");
        timeFrame.label = "Happy Hour";

        String actual = TimeFrame.createFoursquareApiString(timeFrame);

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testCreateFoursquareApiStringHours24() throws Exception {
        String expected = "1,0000,0000%3B";
        TimeFrame timeFrame = new TimeFrame();
        timeFrame.daysList = new ArrayList<Integer>();
        timeFrame.daysList.add(1);
//        timeFrame.openTime = new ArrayList<String>();
//        timeFrame.closeTime = new ArrayList<String>();
        timeFrame.openTime.add("0000");
        timeFrame.closeTime.add("0000");
        timeFrame.label = "";
        timeFrame.is24Hours = true;

        String actual = TimeFrame.createFoursquareApiString(timeFrame);

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testCreateFoursquareApiStringHoursAfterMidnight() throws Exception {
        String expected = "4,2300,+0100,After+After+Party%3B";
        TimeFrame timeFrame = new TimeFrame();
        timeFrame.daysList = new ArrayList<Integer>();
        timeFrame.daysList.add(4);
        timeFrame.openTime = new ArrayList<String>();
        timeFrame.closeTime = new ArrayList<String>();
        timeFrame.openTime.add("2300");
        timeFrame.closeTime.add("100");
        timeFrame.label = "After After Party";

        String actual = TimeFrame.createFoursquareApiString(timeFrame);

        Assert.assertEquals(expected, actual);
    }
}
