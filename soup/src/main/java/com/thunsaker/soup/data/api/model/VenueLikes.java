package com.thunsaker.soup.data.api.model;

import com.google.gson.JsonObject;

import java.util.List;


public class VenueLikes extends BaseCountClass {
    public List<VenueLikesGroups> groups;
    public String summary;

    private class VenueLikesGroups extends BaseCountClass {
        public String type;
    }

    public static VenueLikes ParseVenueLikesFromJson(JsonObject likes) {
        return null;
    }
}