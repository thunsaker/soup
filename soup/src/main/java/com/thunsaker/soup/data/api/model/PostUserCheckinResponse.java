package com.thunsaker.soup.data.api.model;

public class PostUserCheckinResponse extends FoursquareResponse {
    public CheckinResponse response;

    public class CheckinResponse {
        public Checkin checkin;
    }
}
