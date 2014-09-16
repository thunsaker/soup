package com.thunsaker.soup.data.api.model;

public class GetUserListsResponse extends FoursquareResponse {
    public GetUserListsResponseResponse response;

    public class GetUserListsResponseResponse {
        public FoursquareListsResponse lists;

        public class FoursquareListsResponse extends BaseCountClass<FoursquareList> {
        }
    }
}