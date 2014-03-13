package com.thunsaker.soup.classes.foursquare;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/*
 * Created by @thunsaker
 */
public class Hours {
    private String Status;
    private boolean IsOpen;
    private List<TimeFrame> TimeFrames;

    public String getStatus() {
        return Status;
    }
    public void setStatus(String status) {
        Status = status;
    }

    public boolean isIsOpen() {
        return IsOpen;
    }
    public void setIsOpen(boolean isOpen) {
        IsOpen = isOpen;
    }

    public List<TimeFrame> getTimeFrames() {
        return TimeFrames;
    }
    public void setTimeFrames(List<TimeFrame> timeFrames) {
        TimeFrames = timeFrames;
    }

    public static Hours ParseVenueHoursFromJson(JsonObject jsonObject) {
        try {
            Hours myHours = new Hours();
            myHours.setIsOpen(jsonObject.get("isOpen") != null
                    ? jsonObject.get("isOpen").getAsBoolean() : false);
            myHours.setStatus(jsonObject.get("status") != null
                    ? jsonObject.get("status").getAsString() : "");
            JsonArray timesArray = jsonObject.get("timeframes") != null
                    ? jsonObject.get("timeframes").getAsJsonArray() : null;
            myHours.setTimeFrames(timesArray != null
                    ? TimeFrame.ParseVenueHourStringsFromJson(timesArray)
                    : new ArrayList<TimeFrame>());

            return myHours;
        } catch (Exception e) {
            return null;
        }
    }
}
