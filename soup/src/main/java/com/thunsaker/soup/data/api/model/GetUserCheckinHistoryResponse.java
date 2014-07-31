package com.thunsaker.soup.data.api.model;

public class GetUserCheckinHistoryResponse extends FoursquareResponse {
    public UserCheckinHistoryResponse response;

    public class UserCheckinHistoryResponse {
        public CheckinsResponse checkins;
    }

    public class CheckinsResponse extends BaseCountClass<Checkin> { }
}
