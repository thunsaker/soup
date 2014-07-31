package com.thunsaker.soup.data.api.model;

public class FoursquareVenueResponse extends FoursquareCompactVenueResponse {
    public VenueLikes likes;
    public boolean like;
    public boolean dislike;
    public double rating;
    public int ratingSignals;
    public VenueFriendVisits friendVisits;
    public VenuePhotosResponse photos;
//    public VenueReasons reasons;
    public String description;
//    public VenuePage page;
    public long createdAt;
    public VenueMayor mayor;
//    public VenueTips tips;
//    public String[] tags;
    public String shortUrl;
    public String timeZone;
    public VenuePageUpdates pageUpdates;
    public GetVenueHoursResponse.VenueHoursResponse hours;

    public static Venue ConvertFoursquareVenueResponseToVenue(FoursquareVenueResponse venue) {
        return Venue.ConvertVenueResponseToVenue(venue);
    }

    private class VenuePageUpdates extends BaseCountClass { }

//    public VenuesListDetails listed;
//    public VenuePhrases phrases;
//    public VenuePopularity popular;
//    public VenueAttributes attributes;
}
