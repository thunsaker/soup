package com.thunsaker.soup.data.api.model;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class FoursquareListItem {
    public String id;
    public long createdAt;
    public FoursquareTip tip;
    public FoursquareImage photo;
    public CompactVenue venue;
    public CompactFoursquareUser user;
    public long sharedAt;
    public String state;
    public String type;

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public static FoursquareListItem parseFromJson(JsonObject jsonObject) {
        try {
            FoursquareListItem myFoursquareListItem = new FoursquareListItem();

            myFoursquareListItem.id = jsonObject.get("id") != null
                    ? jsonObject.get("id").getAsString() : "";

            myFoursquareListItem.createdAt = jsonObject.get("createdAt") != null
                    ? jsonObject.get("createdAt").getAsLong() : 0;

            myFoursquareListItem.tip = jsonObject.get("tip") != null
                    ? FoursquareTip.parseFromJson(jsonObject.get("tip").getAsJsonObject()) : null;

            myFoursquareListItem.photo = jsonObject.get("photo") != null
                    ? FoursquareImage.GetFoursquareImageFromJson(jsonObject.get("photo").getAsJsonObject()) : null;

            myFoursquareListItem.venue = jsonObject.get("venue") != null
                    ? CompactVenue.ParseCompactVenueFromJson(jsonObject.get("venue").getAsJsonObject()) : null;

            return myFoursquareListItem;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}