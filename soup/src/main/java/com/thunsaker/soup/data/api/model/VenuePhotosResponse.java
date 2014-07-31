package com.thunsaker.soup.data.api.model;

import java.util.List;

public class VenuePhotosResponse extends BaseCountClass {
    public List<VenuePhotoGroupResponse> groups;

    private class VenuePhotoGroupResponse extends BaseCountClass<FoursquareImage> {
        public String type;
        public String name;
    }
}