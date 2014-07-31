package com.thunsaker.soup.data.events;

public class VenueSearchEvent {
    public String searchQuery;
    public String searchLocation;
    public String duplicateVenueId;
    public int listType;

    public VenueSearchEvent() { }

    public VenueSearchEvent(String searchQuery, String searchLocation, String duplicateVenueId, int listType) {
        this.searchQuery = searchQuery;
        this.searchLocation = searchLocation;
        this.duplicateVenueId = duplicateVenueId;
        this.listType = listType;
    }
}
