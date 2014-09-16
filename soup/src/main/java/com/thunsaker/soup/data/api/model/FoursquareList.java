package com.thunsaker.soup.data.api.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class FoursquareList {
    public String id;
    public String name;
    public String description;
//    public List<Object> entities;
    public String type;
	public CompactFoursquareUser user;
    public boolean editable;

    @SerializedName("public")
    public boolean isPublic;
    public boolean collaborative;
    public String url;
    public String canonicalUrl;
    public String createdAt;
    public String updatedAt;
    public FoursquareImage photo;
    public int visitedCount;
    public int venueCount;
    public FoursquareListCategories categories;
    public boolean following;
//    public FoursquareListFollowers followers;
//    public FoursquareListSaves saves;
    public FoursquareListItemsCount listItems;

	public static FoursquareList GetListFromJson(JsonObject jsonObject) {
        try {
            FoursquareList myList = new FoursquareList();
            String myId = jsonObject.get("id") != null
                    ? jsonObject.get("id").getAsString() : "";
            myList.id = myId;
            if(myId.contains("/todos"))
                myList.type = "todos";
            else
                myList.type = jsonObject.get("type") != null
                    ? jsonObject.get("type").getAsString()
                    : "";
            myList.name = jsonObject.get("name") != null
                    ? jsonObject.get("name").getAsString() : "";
            myList.description = jsonObject.get("description") != null
                    ? jsonObject.get("description").getAsString() : "";
            myList.user = jsonObject.get("user") != null
                    ? CompactFoursquareUser.GetCompactFoursquareUserFromJson(
                    jsonObject.get("user").getAsJsonObject())
                    : null;

            myList.following = jsonObject.get("following") != null && jsonObject.get("following").getAsBoolean();
            myList.editable = jsonObject.get("editable") != null && jsonObject.get("editable").getAsBoolean();
            myList.isPublic = jsonObject.get("public") != null && jsonObject.get("public").getAsBoolean();
            myList.collaborative = jsonObject.get("collaborative") != null && jsonObject.get("collaborative").getAsBoolean();

            myList.url = jsonObject.get("url") != null ? jsonObject.get("url")
                    .getAsString() : "";
            myList.canonicalUrl = jsonObject.get("canonicalUrl") != null
                    ? jsonObject.get("canonicalUrl").getAsString() : "";

            myList.createdAt = jsonObject.get("createdAt") != null
                    ? jsonObject.get("createdAt").getAsString() : "";
            myList.updatedAt = jsonObject.get("updatedAt") != null
                    ? jsonObject.get("updatedAt").getAsString() : "";

            myList.photo = jsonObject.get("photo") != null
                    ? FoursquareImage.GetFoursquareImageFromJson(
                    jsonObject.get("photo").getAsJsonObject())
                    : null;

            JsonObject jListItems = jsonObject.get("listItems") != null
                    ? jsonObject.get("listItems").getAsJsonObject() : null;
            if (jListItems != null) {
                myList.venueCount = jListItems.get("count") != null
                        ? jListItems.get("count").getAsInt() : 0;
                myList.listItems = jListItems.get("items") != null
                        ? FoursquareList.GetListItemsFromJson(jListItems.get("items").getAsJsonArray())
                        : null;
            }

            return myList;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

	public static FoursquareListItemsCount GetListItemsFromJson(JsonArray jsonArray) {
		try {
            FoursquareListItemsCount myListItemsCount = new FoursquareListItemsCount();
            List<FoursquareListItem> myListItems = new ArrayList<FoursquareListItem>();
			for (JsonElement jListItem : jsonArray) {
				FoursquareListItem myListItem;
				myListItem = FoursquareListItem.parseFromJson(jListItem.getAsJsonObject());
				myListItems.add(myListItem);
			}
            myListItemsCount.items = myListItems;
			return myListItemsCount;
		} catch (Exception e) {
			return null;
		}
	}
}