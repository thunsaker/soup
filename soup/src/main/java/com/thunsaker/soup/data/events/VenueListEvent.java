package com.thunsaker.soup.data.events;

import com.thunsaker.android.common.bus.BaseEvent;
import com.thunsaker.soup.data.api.model.CompactVenue;

import java.util.List;

public class VenueListEvent extends BaseEvent {
    public List<CompactVenue> resultList;
    public String searchQuery;
    public String listLocation;
    public String duplicateVenueId;

    public VenueListEvent(Boolean result, String resultMessage, List<CompactVenue> resultList, String searchQuery, String listLocation, String duplicateVenueId) {
        super(result, resultMessage);
        this.resultList = resultList;
        this.listLocation = listLocation;
        this.searchQuery = searchQuery;
        this.duplicateVenueId = duplicateVenueId;
    }
}