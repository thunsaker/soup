package com.thunsaker.soup.data.api.model;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.List;

public class FoursquareTip {
    public String id;
    public long createdAt;
    public String text;
    public String type;
    public String canonicalUrl;
    public boolean like;
    public boolean logView;
    public CompactFoursquareUser user;

    public List<Object> flags;
    public FoursquareLikes likes;
    public FoursquareListed listed;
    public FoursquareTodo todo;
    public FoursquareListSaves saves;

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public static FoursquareTip parseFromJson(JsonObject jsonObject) {
        try {
            FoursquareTip myFoursquareTip = new FoursquareTip();

            myFoursquareTip.id = jsonObject.get("id") != null
                    ? jsonObject.get("id").getAsString() : "";

            myFoursquareTip.createdAt = jsonObject.get("createdAt") != null
                    ? jsonObject.get("createdAt").getAsLong() : 0;

            myFoursquareTip.text = jsonObject.get("text") != null
                    ? jsonObject.get("text").getAsString() : "";

            myFoursquareTip.type = jsonObject.get("type") != null
                    ? jsonObject.get("type").getAsString() : "";

            myFoursquareTip.canonicalUrl = jsonObject.get("canonicalUrl") != null
                    ? jsonObject.get("canonicalUrl").getAsString() : "";

            myFoursquareTip.like = jsonObject.get("like") != null && jsonObject.get("like").getAsBoolean();

            myFoursquareTip.logView = jsonObject.get("logView") != null && jsonObject.get("logView").getAsBoolean();

            myFoursquareTip.user = jsonObject.get("user") != null
                    ? CompactFoursquareUser.GetCompactFoursquareUserFromJson(jsonObject.get("user").getAsJsonObject()) : null;

            return myFoursquareTip;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}