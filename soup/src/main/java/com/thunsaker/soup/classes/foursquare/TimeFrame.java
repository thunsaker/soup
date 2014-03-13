package com.thunsaker.soup.classes.foursquare;

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
    private String DaysString;
    private List<Integer> DaysList;
    private boolean IncludesToday;
    private String OpenTimesString;
    private String OpenTime;
    private String CloseTime;
    private String FoursquareApiString;
    private boolean Is24Hours;

    public TimeFrame(){
        setDaysString(null);
        setDaysList(new ArrayList<Integer>());
        setOpenTimesString("");
        setIncludesToday(false);
        setOpenTime("");
        setCloseTime("");
    }

    public TimeFrame(String openTimes, String daysString) {
        setOpenTimesString(openTimes);
        setDaysString(daysString);

        setDaysList(new ArrayList<Integer>());
        setIncludesToday(false);
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
        String combinedString = String.format("%s-%s", minString, maxString);
        setDaysString(combinedString);
        setDaysList(days);

        setFoursquareApiString(createFoursquareApiString(CurrentContext, this));
    }

    public String getFoursquareApiString() {
        return FoursquareApiString;
    }

    public void setFoursquareApiString(String foursquareApiString) {
        FoursquareApiString = foursquareApiString;
    }

    public String getDaysString() {
        return DaysString;
    }
    public void setDaysString(String daysString) {
        DaysString = daysString;
    }

    public List<Integer> getDaysList() {
        return DaysList;
    }
    public void setDaysList(List<Integer> daysList) {
        DaysList = daysList;
    }

    public boolean isIncludesToday() {
        return IncludesToday;
    }
    public void setIncludesToday(boolean includesToday) {
        IncludesToday = includesToday;
    }

    public String getOpenTimesString() {
        return OpenTimesString;
    }
    public void setOpenTimesString(String openTimesString) {
        OpenTimesString = openTimesString;
    }

    public String getOpenTime() {
        return OpenTime;
    }
    public void setOpenTime(String openTime) {
        this.OpenTime = openTime;
    }

    public String getCloseTime() {
        return CloseTime;
    }
    public void setCloseTime(String closeTime) {
        this.CloseTime = closeTime;
    }

    public boolean isIs24Hours() {
		return Is24Hours;
	}

	public void setIs24Hours(boolean is24Hours) {
		Is24Hours = is24Hours;
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
                    stringTimeFrames.get(t).setDaysList(daysList);
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
                        stringTimeFrames.get(t).setOpenTime(openTime);
                        String closeTime = jObjectOpen.get("end").getAsString();
                        stringTimeFrames.get(t).setCloseTime(closeTime);

                        if(openTime.equals("0000") && closeTime.equals("+0000"))
                        	stringTimeFrames.get(t).setIs24Hours(true);
                        else
                        	stringTimeFrames.get(t).setIs24Hours(false);
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
                    myTimeFrame.setDaysList(daysList);

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
                            myTimeFrame.setOpenTime(
                                    jObjectOpen.get("start").getAsString());
                        }
                        if (jObjectOpen != null) {
                            myTimeFrame.setCloseTime(
                                    jObjectOpen.get("end").getAsString());
                        }

                        String openTime = jObjectOpen.get("start").getAsString();
                        myTimeFrame.setOpenTime(openTime);
                        String closeTime = jObjectOpen.get("end").getAsString();
                        myTimeFrame.setCloseTime(closeTime);

                        if(openTime == "0000" && closeTime == "+0000")
                        	myTimeFrame.setIs24Hours(true);
                        else
                        	myTimeFrame.setIs24Hours(false);
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
        String StartTime = t.getOpenTime();
        Calendar myStartDate = Calendar.getInstance();

        String EndTime = t.getCloseTime();
        Calendar myEndDate = Calendar.getInstance();

        if(t.isIs24Hours()) {
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
        days.addAll(t.getDaysList());
        String urlEncodedSemiColon = Util.Encode(";");
        String urlEncodedAddition = Util.Encode("+");
        for (Integer day : days) {
        	apiStringBuilder.append(String.format("%s,", day));

			if (t.isIs24Hours()) {
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
}