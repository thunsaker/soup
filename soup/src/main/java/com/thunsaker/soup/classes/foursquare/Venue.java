package com.thunsaker.soup.classes.foursquare;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class Venue extends CompactVenue {
	private String Description;
    private Hours VenueHours;

    public Venue() {
		
	}
	
	public Venue(Venue existingVenue) {
		this.setId(existingVenue.getId());
		this.setName(existingVenue.getName());
		this.setContact(existingVenue.getContact());
		this.setLocation(existingVenue.getLocation());
		this.setCanonicalUrl(existingVenue.getCanonicalUrl());
		this.setCategories(existingVenue.getCategories());
		this.setVerified(existingVenue.getVerified());
		this.setStats(existingVenue.getStats());
		this.setUrl(existingVenue.getUrl());
		this.setReferralId(existingVenue.getReferralId());
		this.setDescription(existingVenue.getDescription());
        this.setVenueHours(existingVenue.getVenueHours());
	}
	
	public String getDescription() {
		return Description;
	}
	public void setDescription(String description) {
		this.Description = description;
	}

    public Hours getVenueHours() {
        return VenueHours;
    }
    public void setVenueHours(Hours venueHours) {
        VenueHours = venueHours;
    }
	
	public static Venue ConvertCompactVenueToVenue(CompactVenue CompactVenueToConvert) {
		Venue myVenue = new Venue();
		myVenue.setId(CompactVenueToConvert.getId());
		myVenue.setName(CompactVenueToConvert.getName());
		myVenue.setContact(CompactVenueToConvert.getContact());
		myVenue.setLocation(CompactVenueToConvert.getLocation());
		myVenue.setCanonicalUrl(CompactVenueToConvert.getCanonicalUrl());
		myVenue.setCategories(CompactVenueToConvert.getCategories());
		myVenue.setVerified(CompactVenueToConvert.getVerified());
		myVenue.setStats(CompactVenueToConvert.getStats());
		myVenue.setUrl(CompactVenueToConvert.getUrl());
		myVenue.setReferralId(CompactVenueToConvert.getReferralId());
		return myVenue;
	}
	
	public static Venue ParseVenueFromJson(JsonObject jObjectVenue) {
		CompactVenue myCompactVenue = CompactVenue.ParseCompactVenueFromJson(jObjectVenue);
		Venue myVenue = ConvertCompactVenueToVenue(myCompactVenue);
		myVenue.setDescription(jObjectVenue.get("description") != null
                ? jObjectVenue.get("description").getAsString()
                : "");
        Hours myVenueHours = Hours.ParseVenueHoursFromJson(
                jObjectVenue.get("hours") != null
                        ? jObjectVenue.get("hours").getAsJsonObject()
                        : null);
        myVenue.setVenueHours(myVenueHours);
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
