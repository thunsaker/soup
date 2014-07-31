package com.thunsaker.soup.data.api.model;

import android.content.Context;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.thunsaker.soup.R;
import com.thunsaker.soup.util.Util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

/*
 * Created by thunsaker on 8/2/13.
 */

public class TimeFrame {
    public String daysString;
    public List<Integer> daysList;
    public boolean includesToday;
    public String openTimesString;
    public String openTime;
    public String closeTime;
    public String FoursquareApiString;
    public boolean is24Hours;

    public TimeFrame(){
        this.daysString = null;
        this.daysList = new ArrayList<Integer>();
        this.openTimesString = "";
        this.includesToday = false;
        this.openTime = "";
        this.closeTime = "";
    }

    public TimeFrame(String openTimes, String daysString) {
        this.openTimesString = openTimes;
        this.daysString = daysString;

        this.daysList = new ArrayList<Integer>();
        this.includesToday = false;
    }

    public TimeFrame(Context CurrentContext, List<Integer> days, String StartTime, String EndTime) {
        // Get Min day
        Collections.sort(days);
        Integer min = Collections.min(days);
        String minString = TimeFrame.ConvertIntegerDayToLocalizedDayString(CurrentContext, min);

        // Get Max day
        Integer max = Collections.max(days);
        String maxString = TimeFrame.ConvertIntegerDayToLocalizedDayString(CurrentContext, max);

        // Translate into String
        this.daysString = String.format("%s-%s", minString, maxString);
        this.daysList = days;

        setFoursquareApiString(createFoursquareApiString(CurrentContext, this));
    }

    public String getFoursquareApiString() {
        return FoursquareApiString;
    }

    public void setFoursquareApiString(String foursquareApiString) {
        FoursquareApiString = foursquareApiString;
    }

	public static List<TimeFrame> ParseVenueHourFromJson(JsonArray jsonArray,
                                                         List<TimeFrame> stringTimeFrames) {
        try {
            if(stringTimeFrames != null) {
                for(int t = 0; t < jsonArray.size(); t++) {
                    List<Integer> daysList = new ArrayList<Integer>();
                    JsonObject jObjectDaysList = jsonArray.get(t).getAsJsonObject();
                    JsonArray myDaysListArray =
                            jObjectDaysList.getAsJsonArray("days") != null
                            ? jObjectDaysList.getAsJsonArray("days").getAsJsonArray()
                            : null;
                    if(myDaysListArray != null) {
                        for (JsonElement dayElement : myDaysListArray) {
                            daysList.add(dayElement.getAsInt());
                        }
                    }
                    stringTimeFrames.get(t).daysList = daysList;
                    JsonArray myOpenTimeArray =
                            jObjectDaysList.getAsJsonArray("open") != null
                                    ? jObjectDaysList.getAsJsonArray("open").getAsJsonArray()
                                    : null;
                    if(myOpenTimeArray != null) {
                        JsonObject jObjectOpen = myOpenTimeArray.get(0).getAsJsonObject() != null
                                ? myOpenTimeArray.get(0).getAsJsonObject()
                                : null;
                        String openTime = null;
                        if (jObjectOpen != null) {
                            openTime = jObjectOpen.get("start").getAsString();
                        }
                        stringTimeFrames.get(t).openTime = openTime;
                        assert jObjectOpen != null;
                        String closeTime = jObjectOpen.get("end").getAsString();
                        stringTimeFrames.get(t).closeTime = closeTime;
                        stringTimeFrames.get(t).is24Hours = openTime.equals("0000") && closeTime.equals("+0000");
                    }
                }
            } else {
                stringTimeFrames = new ArrayList<TimeFrame>();

                for(JsonElement jsonElement : jsonArray) {
                    List<Integer> daysList = new ArrayList<Integer>();
                    JsonArray myDaysListArray =
                            jsonElement.getAsJsonObject().getAsJsonArray("days") != null
                                    ? jsonElement.getAsJsonObject()
                                        .getAsJsonArray("days").getAsJsonArray()
                                    : null;
                    if(myDaysListArray != null) {
                        for (JsonElement dayElement : myDaysListArray) {
                            daysList.add(dayElement.getAsInt());
                        }
                    }
                    TimeFrame myTimeFrame = new TimeFrame();
                    myTimeFrame.daysList = daysList;

                    JsonObject jObjectDaysList =
                            null;
                    if (myDaysListArray != null) {
                        jObjectDaysList = jsonElement.getAsJsonObject() != null
                                ? myDaysListArray.getAsJsonObject()
                                : null;
                    }
                    JsonArray myOpenTimeArray = null;
                    if (jObjectDaysList != null) {
                        myOpenTimeArray = jObjectDaysList.getAsJsonArray("open") != null
                                ? jObjectDaysList.getAsJsonArray("open").getAsJsonArray()
                                : null;
                    }
                    if(myOpenTimeArray != null) {
                        JsonObject jObjectOpen = myOpenTimeArray.getAsJsonObject() != null
                                ? myOpenTimeArray.getAsJsonObject()
                                : null;
                        if (jObjectOpen != null) {
                            myTimeFrame.openTime =
                                    jObjectOpen.get("start").getAsString();
                        }
                        if (jObjectOpen != null) {
                            myTimeFrame.closeTime =
                                    jObjectOpen.get("end").getAsString();
                        }

                        assert jObjectOpen != null;
                        String openTime = jObjectOpen.get("start").getAsString();
                        myTimeFrame.openTime = openTime;
                        String closeTime = jObjectOpen.get("end").getAsString();
                        myTimeFrame.closeTime = closeTime;
                        myTimeFrame.is24Hours = openTime.equals("0000") && closeTime.equals("+0000");
                    }
                }
            }

            return stringTimeFrames;
        } catch (Exception e) {
            return null;
        }
    }

    public static List<TimeFrame> ParseVenueHourStringsFromJson(JsonArray jsonArray) {
        try {
            List<TimeFrame> myTimeFrameList = new ArrayList<TimeFrame>();
            for(JsonElement jsonElement : jsonArray) {
                String daysString = jsonElement.getAsJsonObject().get("days") != null
                        ? jsonElement.getAsJsonObject().get("days").getAsString() : "";
    //            if(daysString != "") {
    //                myTimeFrame.setDaysList(ConvertDaysStringToList(daysString));
    //            }
                StringBuilder openTimes = new StringBuilder();
                JsonArray openTimesArray = jsonElement.getAsJsonObject().get("open") != null
                        ? jsonElement.getAsJsonObject().get("open").getAsJsonArray() : null;
                if(openTimesArray != null) {
                    for (JsonElement jsonElementTime : openTimesArray) {
                        JsonObject renderedTimeObject = jsonElementTime.getAsJsonObject();
                        openTimes.append(renderedTimeObject.get("renderedTime").getAsString());
                    }
                }

                myTimeFrameList.add(new TimeFrame(openTimes.toString(), daysString));
            }

            return myTimeFrameList;
        } catch (Exception e) {
            return null;
        }
    }

    public static String ConvertIntegerDayToLocalizedDayString(Context context, Integer dayInt) {
        switch (dayInt) {
            case 2:
                return context.getString(R.string.day_of_week_tuesday_short_styled);
            case 3:
                return context.getString(R.string.day_of_week_wednesday_short_styled);
            case 4:
                return context.getString(R.string.day_of_week_thursday_short_styled);
            case 5:
                return context.getString(R.string.day_of_week_friday_short_styled);
            case 6:
                return context.getString(R.string.day_of_week_saturday_short_styled);
            case 7:
                return context.getString(R.string.day_of_week_sunday_short_styled);
            default:
                return context.getString(R.string.day_of_week_monday_short_styled);
        }
    }

    public static Integer ConvertLocalizedDayToInteger(Context context, String daysString) {
        if(daysString.equals(context.getString(R.string.day_of_week_monday_short))){
            return 1;
        } else if(daysString.equals(context.getString(R.string.day_of_week_tuesday_short))){
            return 2;
        } else if(daysString.equals(context.getString(R.string.day_of_week_wednesday_short))){
            return 3;
        } else if(daysString.equals(context.getString(R.string.day_of_week_thursday_short))){
            return 4;
        } else if(daysString.equals(context.getString(R.string.day_of_week_friday_short))){
            return 5;
        } else if(daysString.equals(context.getString(R.string.day_of_week_saturday_short))){
            return 6;
        } else {
            return 7;
        }
    }

    public static String createFoursquareApiString(Context CurrentContext, TimeFrame t) {
        String StartTime = t.openTime;
        Calendar myStartDate = Calendar.getInstance();

        String EndTime = t.closeTime;
        Calendar myEndDate = Calendar.getInstance();

        if(t.is24Hours) {
        	StartTime = "0000";
        	EndTime = "0000";
        } else {
            if(StartTime.contains(":")) {
                String[] startSplit = StartTime.split(":");
                myStartDate.set(Calendar.HOUR, Integer.parseInt(startSplit[0]));
                myStartDate.set(Calendar.MINUTE, Integer.parseInt(startSplit[1]));
                StartTime = String.format("%s%s", startSplit[0], startSplit[1]);
            }

            if(EndTime.contains(":")) {
                String[] endSplit = EndTime.split(":");
                myEndDate.set(Calendar.HOUR, Integer.parseInt(endSplit[0]));
                myEndDate.set(Calendar.MINUTE, Integer.parseInt(endSplit[1]));
                EndTime = String.format("%s%s", endSplit[0], endSplit[1]);
            }
        }

        StartTime = StartTime.replace("+", "");
        EndTime = EndTime.replace("+", "");

        Integer apiFormattedStart =
                Integer.parseInt(StartTime);
        Integer apiFormattedEnd =
                Integer.parseInt(EndTime);

        StringBuilder apiStringBuilder = new StringBuilder();
        List<Integer> days = new ArrayList<Integer>();
        days.addAll(t.daysList);
        String urlEncodedSemiColon = Util.Encode(";");
        String urlEncodedAddition = Util.Encode("+");
        for (Integer day : days) {
        	apiStringBuilder.append(String.format("%s,", day));

			if (t.is24Hours) {
				apiStringBuilder.append(String.format("0000,%s0000", urlEncodedAddition));
			} else {
				apiStringBuilder
						.append(String
								.format("%s,%s%s",
										apiFormattedStart < 100 ? "00" + apiFormattedStart.toString() : apiFormattedStart < 1000 ? "0" + apiFormattedStart.toString() : apiFormattedStart.toString(),
										apiFormattedEnd < apiFormattedStart ? urlEncodedAddition : "",
										apiFormattedEnd < 100 ? "00" + apiFormattedEnd.toString() : apiFormattedEnd < 1000 ? "0" + apiFormattedEnd.toString() : apiFormattedEnd.toString()));
			}

			apiStringBuilder.append(urlEncodedSemiColon);
        }

        return apiStringBuilder.toString();
    }

    public static List<TimeFrame> ConvertVenueHoursTimeFrameResponseListToTimeFrameList(List<GetVenueHoursResponse.VenueHoursTimeFrameResponse> timeFramesResponse) {
        List<TimeFrame> list = new ArrayList<TimeFrame>();
        for(GetVenueHoursResponse.VenueHoursTimeFrameResponse h : timeFramesResponse) {
            TimeFrame t = new TimeFrame();
            t.daysString = h.days;
            t.includesToday = h.includesToday;
            t.openTimesString = "";
            for(GetVenueHoursResponse.VenueHoursTimeFrameOpen openTimes : h.open) {
                t.openTimesString += t.openTimesString.length() > 0 ? ", " + openTimes.renderedTime : openTimes.renderedTime;
            }
            list.add(t);
        }
        return list;
    }
}