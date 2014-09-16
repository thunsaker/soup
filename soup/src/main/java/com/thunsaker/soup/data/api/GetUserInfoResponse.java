package com.thunsaker.soup.data.api;

import com.thunsaker.soup.data.api.model.CompactFoursquareUser;
import com.thunsaker.soup.data.api.model.FoursquareResponse;

public class GetUserInfoResponse extends FoursquareResponse {
    public UserResponse response;

    public class UserResponse {
        public CompactFoursquareUser user;
    }
}