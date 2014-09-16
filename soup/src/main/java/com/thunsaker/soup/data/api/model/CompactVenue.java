package com.thunsaker.soup.data.api.model;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.List;

public class CompactVenue {
	public String id;
	public String name;
	public Contact contact;
	public com.thunsaker.soup.data.api.model.Location location;
	public String canonicalUrl;
	public List<Category> categories;
	public Boolean verified;
	public VenueStats stats;
	public String url;
	public VenueBeenHere beenHere;
/*	public VenueLikes Likes;
	public Menu Menu;
	public BeenHere BeenHere;
	public Specials specials;
	public VenuePrice price;
	public HereNow HereNow;
	public Listed listed; */
    public double rating;
    public double ratingSignals;
    public String storeId;
	public String referralId;
    public FoursquareTastes tastes;

	@Override
	public String toString() {
		return toJson(this);
	}

	public static String toJson(CompactVenue myCompactVenue) {
		Gson gson = new Gson();
		return myCompactVenue != null ? gson.toJson(myCompactVenue, CompactVenue.class) : "";
	}

	public static CompactVenue GetCompactVenueFromJson(String jsonString) {
		Gson gson = new Gson();
		return jsonString != null ? gson.fromJson(jsonString, CompactVenue.class) : null;
	}

	public static CompactVenue ParseCompactVenueFromJson(JsonObject jsonObject) {
		try {
			CompactVenue myVenue = new CompactVenue();

			myVenue.id = jsonObject.get("id") != null
                    ? jsonObject.get("id").getAsString()
                    : "";
			myVenue.name =  jsonObject.get("name") != null
					? jsonObject.get("name").getAsString()
					: "";
			myVenue.contact = jsonObject.get("contact") != null
					? com.thunsaker.soup.data.api.model.Contact.GetContactFromJson(
                    jsonObject.getAsJsonObject("contact"))
					: null;
			myVenue.location = jsonObject.get("location") != null
					? com.thunsaker.soup.data.api.model.Location.GetLocationFromJson(
                        jsonObject.getAsJsonObject("location"))
					: null;
			myVenue.canonicalUrl = jsonObject.get("canonicalUrl") != null
					? jsonObject.get("canonicalUrl").getAsString()
					: "";
			myVenue.categories =
                    jsonObject.get("categories") != null
					? Category.GetCategoriesFromJson(
                            jsonObject.getAsJsonArray("categories"), false)
					: null;
			myVenue.verified =
                    jsonObject.get("verified") != null
                    && jsonObject.get("verified").getAsBoolean();
			myVenue.stats = null;
			myVenue.url = jsonObject.get("url") != null
					? jsonObject.get("url").getAsString()
					: "";
			myVenue.referralId = jsonObject.get("referralId") != null
					? jsonObject.get("referralId").getAsString()
					: "";
//			myVenue.beenHere =
//                    jsonObject.get("beenHere") != null
//                            ? jsonObject.get("beenHere").getAsString()
            myVenue.storeId = jsonObject.get("storeId") != null
                    ? jsonObject.get("storeId").getAsString()
                    : "";

			return myVenue;
		} catch (Exception e) {
			return null;
		}
	}

    public static CompactVenue GetCompactVenueFromFoursquareCompactVenueResponse(FoursquareCompactVenueResponse response) {
        try {
            CompactVenue myVenue = new CompactVenue();

            myVenue.id = response.id != null
                    ? response.id
                    : "";
            myVenue.name = response.name != null
                    ? response.name
                    : "";
            myVenue.contact = response.contact != null
                    ? response.contact
                    : null;
            myVenue.location = response.location != null
                    ? com.thunsaker.soup.data.api.model.Location
                    .GetLocationFromFoursquareLocationResponse(response.location)
                    : null;
            myVenue.canonicalUrl = response.canonicalUrl != null
                    ? response.canonicalUrl
                    : "";
            myVenue.categories = response.categories != null
                    ? response.categories
                    : null;
            myVenue.verified = response.verified;
            myVenue.stats = null;
            myVenue.url = response.url!= null
                    ? response.url
                    : "";
            myVenue.referralId = response.referralId!= null
                    ? response.referralId
                    : "";

            myVenue.storeId = response.storeId!= null
                    ? response.storeId
                    : "";

            // TODO: Convert this to use the object
//            myVenue.beenHere = response.beenHere != null && response.beenHere.count > 0;
            return myVenue;
        } catch (Exception e) {
            return null;
        }
    }
}