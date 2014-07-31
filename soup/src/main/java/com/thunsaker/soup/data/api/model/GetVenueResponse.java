package com.thunsaker.soup.data.api.model;

public class GetVenueResponse extends FoursquareResponse {
    public GetFoursquareVenueResponse response;

    public class GetFoursquareVenueResponse {
        public FoursquareVenueResponse venue;
    }
}
