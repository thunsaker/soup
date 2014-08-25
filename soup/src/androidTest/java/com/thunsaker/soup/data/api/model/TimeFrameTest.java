package com.thunsaker.soup.data.api.model;

import android.test.InstrumentationTestCase;

import java.util.ArrayList;

public class TimeFrameTest extends InstrumentationTestCase {
    public void testCreateFoursquareApiStringHoursWithLabel() {
        String expected = "6,1700,2100,Happy+Hour%3B";
        TimeFrame timeFrame = new TimeFrame();
        timeFrame.daysList = new ArrayList<Integer>();
        timeFrame.daysList.add(6);
        timeFrame.openTime = "1700";
        timeFrame.closeTime = "2100";
        timeFrame.label = "Happy Hour";

        String actual = TimeFrame.createFoursquareApiString(timeFrame);

        assertEquals(expected, actual);
    }

    public void testCreateFoursquareApiStringHours24() {
        String expected = "1,0000,+0000%3B";
        TimeFrame timeFrame = new TimeFrame();
        timeFrame.daysList = new ArrayList<Integer>();
        timeFrame.daysList.add(1);
        timeFrame.openTime = "0000";
        timeFrame.closeTime = "0000";
        timeFrame.label = "";
        timeFrame.is24Hours = true;

        String actual = TimeFrame.createFoursquareApiString(timeFrame);

        assertEquals(expected, actual);
    }

    public void testCreateFoursquareApiStringHoursAfterMidnight() {
        String expected = "4,2300,+0100,After+After+Party%3B";
        TimeFrame timeFrame = new TimeFrame();
        timeFrame.daysList = new ArrayList<Integer>();
        timeFrame.daysList.add(4);
        timeFrame.openTime = "2300";
        timeFrame.closeTime = "100";
        timeFrame.label = "After After Party";

        String actual = TimeFrame.createFoursquareApiString(timeFrame);

        assertEquals(expected, actual);
    }
}
