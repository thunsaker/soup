package com.thunsaker.soup.data.api.model;

public class VenueFriendVisits extends BaseCountClass {
    public String summary;

    private class VenueFriendVisitItem {
        public int visitedCount;
        public boolean liked;
        public boolean disliked;
        public CompactFoursquareUser user;
    }
}
