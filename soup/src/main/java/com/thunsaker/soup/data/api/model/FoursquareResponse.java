package com.thunsaker.soup.data.api.model;

public class FoursquareResponse {
    public FoursquareResponseMeta meta;
    public Object[] notifications;

    public class FoursquareResponseMeta extends FoursquareResponseError {
        public int code;
    }

    public class FoursquareResponseError {
        public String errorType;
        public String errorMessage;
    }
}