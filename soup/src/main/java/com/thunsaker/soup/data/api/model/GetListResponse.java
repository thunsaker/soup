package com.thunsaker.soup.data.api.model;

public class GetListResponse extends FoursquareResponse {
    public GetListResponseResponse response;

    public class GetListResponseResponse {
        public FoursquareList list;
    }
}