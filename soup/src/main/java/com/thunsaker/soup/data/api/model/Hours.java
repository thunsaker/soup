package com.thunsaker.soup.data.api.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/*
 * Created by @thunsaker
 */
public class Hours {
    public String status;
    public boolean isOpen;
    public List<TimeFrame> timeFrames;

    public static Hours ParseVenueHoursFromJson(JsonObject jsonObject) {
        try {
            Hours myHours = new Hours();
            myHours.isOpen = jsonObject.get("isOpen") != null && jsonObject.get("isOpen").getAsBoolean();
            myHours.status = jsonObject.get("status") != null
                    ? jsonObject.get("status").getAsString() : "";
            JsonArray timesArray = jsonObject.get("timeframes") != null
                    ? jsonObject.get("timeframes").getAsJsonArray() : null;
            myHours.timeFrames = timesArray != null
                    ? TimeFrame.ParseVenueHourStringsFromJson(timesArray)
                    : new ArrayList<TimeFrame>();

            return myHours;
        } catch (Exception e) {
            return null;
        }
    }

    public static Hours ConvertVenueHoursResponseToHours(GetVenueHoursResponse.VenueHoursResponse response) {
        Hours hours = new Hours();
        hours.isOpen = response.isOpen;
        hours.status = response.status;
        hours.timeFrames = response.timeframes != null ? TimeFrame.ConvertVenueHoursTimeFrameResponseListToTimeFrameList(response.timeframes) : new ArrayList<TimeFrame>();
        return hours;
    }
}
