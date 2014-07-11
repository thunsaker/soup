package com.thunsaker.soup.data.api.model;

import java.util.List;

public class FoursquareVenueSearchResponse {
    public List<FoursquareCompactVenueResponse> venues;
    public String[] neighborhoods;
    public boolean confident;
    public FoursquareGeocode geocode;
}
