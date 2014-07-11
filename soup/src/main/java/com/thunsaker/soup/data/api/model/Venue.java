package com.thunsaker.soup.data.api.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class Venue extends CompactVenue {
	public String description;
    public Hours venueHours;

    public Venue() { }

	public Venue(Venue existingVenue) {
		this.id = existingVenue.id;
		this.name = existingVenue.name;
        this.contact = existingVenue.contact;
        this.location = existingVenue.location;
		this.canonicalUrl = existingVenue.canonicalUrl;
		this.categories = existingVenue.categories;
		this.verified = existingVenue.verified;
		this.stats = existingVenue.stats;
		this.url = existingVenue.url;
		this.referralId = existingVenue.referralId;
		this.description = existingVenue.description;
        this.venueHours = existingVenue.venueHours;
	}

//	public String getDescription() {
//		return Description;
//	}
//	public void setDescription(String description) {
//		this.Description = description;
//	}
//
//    public Hours getVenueHours() {
//        return VenueHours;
//    }
//    public void setVenueHours(Hours venueHours) {
//        VenueHours = venueHours;
//    }

	public static Venue ConvertCompactVenueToVenue(CompactVenue CompactVenueToConvert) {
		Venue myVenue = new Venue();
        myVenue.id = CompactVenueToConvert.id;
        myVenue.name = CompactVenueToConvert.name;
        myVenue.contact = CompactVenueToConvert.contact;
        myVenue.location = CompactVenueToConvert.location;
        myVenue.canonicalUrl = CompactVenueToConvert.canonicalUrl;
        myVenue.categories = CompactVenueToConvert.categories;
        myVenue.verified = CompactVenueToConvert.verified;
        myVenue.stats = CompactVenueToConvert.stats;
        myVenue.url = CompactVenueToConvert.url;
        myVenue.referralId = CompactVenueToConvert.referralId;
		return myVenue;
	}

	public static Venue ParseVenueFromJson(JsonObject jObjectVenue) {
		CompactVenue myCompactVenue = ParseCompactVenueFromJson(jObjectVenue);
		Venue myVenue = ConvertCompactVenueToVenue(myCompactVenue);
		myVenue.description = jObjectVenue.get("description") != null
                ? jObjectVenue.get("description").getAsString()
                : "";
        Hours myVenueHours = Hours.ParseVenueHoursFromJson(
                jObjectVenue.get("hours") != null
                        ? jObjectVenue.get("hours").getAsJsonObject()
                        : null);
        myVenue.venueHours = myVenueHours;
		return myVenue;
	}

    public static Hours ParseVenueHoursFromJson(JsonObject jObjectHours, Hours venueHours) {
        JsonArray timesArray = jObjectHours.get("timeframes") != null
                ? jObjectHours.get("timeframes").getAsJsonArray() : null;
        venueHours.setTimeFrames(
                TimeFrame.ParseVenueHourFromJson(timesArray, venueHours.getTimeFrames()));
        return venueHours;
    }
}
