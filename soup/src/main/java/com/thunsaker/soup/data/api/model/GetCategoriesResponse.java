package com.thunsaker.soup.data.api.model;

import java.util.List;

public class GetCategoriesResponse extends FoursquareResponse {
    public GetCategoriesResponseResponse response;

    public class GetCategoriesResponseResponse {
        public List<Category> categories;
    }
}
