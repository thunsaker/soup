package com.thunsaker.soup.data.api.model;

import java.util.List;

public class FoursquareCompactVenueResponse {
    public String id;
    public String name;
    public Contact contact;
    public FoursquareLocationResponse location;
    public List<Category> categories;
    public boolean verified;
    public VenueStats stats;
    public String url;
    public String canonicalUrl;
    public VenueBeenHere beenHere;
    public VenueSpecials specials;
    public VenueHereNow hereNow;
    public String storeId;
    public String referralId;

    public static CompactVenue ConvertFoursquareCompactVenueResponseToCompactVenue(FoursquareCompactVenueResponse response) {
        return CompactVenue.GetCompactVenueFromFoursquareCompactVenueResponse(response);
    }
}