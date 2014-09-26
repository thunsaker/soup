package com.thunsaker.soup.data.api.model;

import android.content.Context;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.thunsaker.soup.R;
import com.thunsaker.soup.util.Util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TimeFrame {
    public String daysString;
    public List<Integer> daysList;
    public boolean includesToday;
    public String openTimesString;
    public List<String> openTime;
    public List<String> closeTime;
    public boolean is24Hours;
    public String label;

    public TimeFrame(){
        this.daysString = "";
        this.daysList = new ArrayList<Integer>();
        this.openTimesString = "";
        this.openTime = new ArrayList<String>();
        this.closeTime = new ArrayList<String>();
        this.label = "";
    }

    public TimeFrame(String openTimes, String daysString) {
        this.openTimesString = openTimes;
        this.daysString = daysString;
        this.daysList = new ArrayList<Integer>();
        this.includesToday = false;
    }

//    public TimeFrame(Context CurrentContext, List<Integer> days, String StartTime, String EndTime) {
//        // Get Min day
//        Collections.sort(days);
//        Integer min = Collections.min(days);
//        String minString = TimeFrame.ConvertIntegerDayToLocalizedDayString(CurrentContext, min);
//
//        // Get Max day
//        Integer max = Collections.max(days);
//        String maxString = TimeFrame.ConvertIntegerDayToLocalizedDayString(CurrentContext, max);
//
//        // Translate into String
//        this.daysString = String.format("%s-%s", minString, maxString);
//        this.daysList = days;
//
//        setFoursquareApiString(createFoursquareApiString(this));
//    }

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
                        List<String> openTimes = new ArrayList<String>();
                        List<String> closeTimes = new ArrayList<String>();
                        if (jObjectOpen != null) {
                            JsonArray myOpenTimesArray = jObjectOpen.get("start").getAsJsonArray();
                            if(myOpenTimesArray != null) {
                                for (JsonElement openTimeElement : myOpenTimesArray)
                                    openTimes.add(openTimeElement.getAsString());
                            }
                            stringTimeFrames.get(t).openTime = openTimes;

                            JsonArray myCloseTimesArray = jObjectOpen.get("end").getAsJsonArray();
                            if(myCloseTimesArray != null) {
                                for (JsonElement closeTimeElement : myCloseTimesArray)
                                    closeTimes.add(closeTimeElement.getAsString());
                            }
                            stringTimeFrames.get(t).closeTime = closeTimes;

                            if(openTimes.get(0) != null && closeTimes.get(0) != null)
                                stringTimeFrames.get(t).is24Hours = openTimes.get(0).equals("0000") && closeTimes.get(0).equals("+0000");
                        }
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
                            myTimeFrame.openTime.add(
                                    jObjectOpen.get("start").getAsJsonArray().get(0).getAsString());
                        }
                        if (jObjectOpen != null) {
                            myTimeFrame.closeTime.add(
                                    jObjectOpen.get("end").getAsJsonArray().get(0).getAsString());
                        }

                        assert jObjectOpen != null;
                        String openTime = jObjectOpen.get("start").getAsString();
                        myTimeFrame.openTime.add(openTime);
                        String closeTime = jObjectOpen.get("end").getAsString();
                        myTimeFrame.closeTime.add(closeTime);
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

    /**
     * Create a Foursquare api compatible semi-colon separated list of time frames
     * <b>Note:</b> Midnight is represented as "0000"; not "+0000" or "+000", semicolon can be url-encoded or not
     *
     * @param t     {@link com.thunsaker.soup.data.api.model.TimeFrame}
     * @return      Url encoded Foursquare time frame formatted string.
     */
    public static String createFoursquareApiString(TimeFrame t) {
        try {
            String StartTime = t.openTime.get(0);
            Calendar myStartDate = Calendar.getInstance();

            String EndTime = t.closeTime.get(0);
            Calendar myEndDate = Calendar.getInstance();

            if (t.is24Hours) {
                StartTime = "0000";
                EndTime = "0000";
            } else {
                if (StartTime.contains(":")) {
                    String[] startSplit = StartTime.split(":");
                    myStartDate.set(Calendar.HOUR, Integer.parseInt(startSplit[0]));
                    myStartDate.set(Calendar.MINUTE, Integer.parseInt(startSplit[1]));
                    StartTime = String.format("%s%s", startSplit[0], startSplit[1]);
                }

                if (EndTime.contains(":")) {
                    String[] endSplit = EndTime.split(":");
                    myEndDate.set(Calendar.HOUR, Integer.parseInt(endSplit[0]));
                    myEndDate.set(Calendar.MINUTE, Integer.parseInt(endSplit[1]));
                    EndTime = String.format("%s%s", endSplit[0], endSplit[1]);
                }
            }

            StartTime = StartTime.replace("+", "");
            EndTime = EndTime.replace("+", "");

            Integer apiFormattedStart = !t.is24Hours ? StartTime.length() > 0 ? Integer.parseInt(StartTime) : 0 : 0;
            Integer apiFormattedEnd = !t.is24Hours ? EndTime.length() > 0 ? Integer.parseInt(EndTime) : 0 : 0;

            StringBuilder apiStringBuilder = new StringBuilder();
            List<Integer> days = new ArrayList<Integer>();
            days.addAll(t.daysList);
            String urlEncodedSemiColon = Util.Encode(";");

            for (Integer day : days) {
                apiStringBuilder.append(String.format("%s,", day));

                if (t.is24Hours) {
                    apiStringBuilder.append("0000,0000");
                } else {
                    apiStringBuilder
                            .append(String
                                    .format("%s,%s%s",
                                            apiFormattedStart < 100 ? "00" + apiFormattedStart.toString() : apiFormattedStart < 1000 ? "0" + apiFormattedStart.toString() : apiFormattedStart.toString(),
                                            apiFormattedEnd < apiFormattedStart && apiFormattedEnd != 0 ? "+" : "",
                                            apiFormattedEnd == 0 ? "0000" : apiFormattedEnd < 100 ? "00" + apiFormattedEnd.toString() : apiFormattedEnd < 1000 ? "0" + apiFormattedEnd.toString() : apiFormattedEnd.toString()));
                }

                if (t.label != null && t.label.length() > 0)
                    apiStringBuilder.append(String.format(",%s", Util.Encode(t.label)));

                apiStringBuilder.append(urlEncodedSemiColon);
            }

            return apiStringBuilder.toString();
        } catch (Exception ex) {
            return "";
        }
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

    public static List<TimeFrame> ConvertVenueHoursTimeFrameResponseDayListToTimeFrameList(List<GetVenueHoursResponse.VenueHoursTimeFrameResponseDayList> timeFramesResponse) {
        List<TimeFrame> list = new ArrayList<TimeFrame>();
        if(timeFramesResponse != null && timeFramesResponse.size() > 0) {
            for (GetVenueHoursResponse.VenueHoursTimeFrameResponseDayList h : timeFramesResponse) {
                TimeFrame t = new TimeFrame();
                t.daysList = h.days;
                t.includesToday = h.includesToday;
                if (t.openTime == null)
                    t.openTime = new ArrayList<String>();
                if (t.closeTime == null)
                    t.closeTime = new ArrayList<String>();
                for (GetVenueHoursResponse.VenueHoursTimeFrameOpen openTimes : h.open) {
                    t.openTime.add(openTimes.start);
                    t.closeTime.add(openTimes.end);
                }
                list.add(t);
            }
        }

        return list;
    }

    public static List<TimeFrame> MergeVenueHoursTimeFrames(List<TimeFrame> original, List<TimeFrame> updated, Context context) {
        List<TimeFrame> timeFrames = new ArrayList<TimeFrame>();

        if(original.size() == updated.size()) {
            for (int i = 0; i < original.size(); i++) {
                TimeFrame t = new TimeFrame();
                TimeFrame tu = updated.get(0);
                t.openTime = new ArrayList<String>();
                t.openTime = tu.openTime;
                t.closeTime = new ArrayList<String>();
                t.closeTime = tu.closeTime;
                t.daysList = tu.daysList;

                TimeFrame to = original.get(0);
                t.daysString = to.daysString;
                t.openTimesString = to.openTimesString;
                t.includesToday = to.includesToday;
                timeFrames.add(t);
            }
        } else {
            for (int i = 0; i < updated.size(); i++) {
                TimeFrame t = new TimeFrame();
                TimeFrame tu = updated.get(i);
                t.openTime = new ArrayList<String>();
                t.openTime = tu.openTime;
                t.closeTime = new ArrayList<String>();
                t.closeTime = tu.closeTime;
                t.daysList = tu.daysList;

                t.daysString = TimeFrame.GetStringSegmentFormat(ConvertIntegerDayToLocalizedDayString(context, tu.daysList.get(0)), ConvertIntegerDayToLocalizedDayString(context, tu.daysList.get(tu.daysList.size() - 1)));

                for (int j = 0; j < t.openTime.size(); j++) {
                    t.openTimesString += t.openTimesString.length() > 0 ? ", " + TimeFrame.GetStringSegmentFormat(t.openTime.get(i), t.closeTime.get(i)) : TimeFrame.GetStringSegmentFormat(t.openTime.get(i), t.closeTime.get(i));
                }
                timeFrames.add(t);
            }
        }

        return timeFrames;
    }

    public static String GetStringSegmentFormat(String open, String close) {
        return String.format("%s—%s", open, close);
    }
}