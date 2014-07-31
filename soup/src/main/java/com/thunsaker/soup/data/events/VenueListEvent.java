package com.thunsaker.soup.data.events;

import com.thunsaker.android.common.bus.BaseEvent;
import com.thunsaker.soup.data.api.model.CompactVenue;

import java.util.List;

public class VenueListEvent extends BaseEvent {
    public List<CompactVenue> resultList;
    public String resultSearchQuery;
    public String resultListLocation;
    public String resultDuplicateVenueId;
    public int resultListType;

    public VenueListEvent(boolean result, String resultMessage, List<CompactVenue> resultList, String resultSearchQuery, String resultListLocation, String resultDuplicateVenueId, int resultListType) {
        super(result, resultMessage);
        this.resultList = resultList;
        this.resultListLocation = resultListLocation;
        this.resultSearchQuery = resultSearchQuery;
        this.resultDuplicateVenueId = resultDuplicateVenueId;
        this.resultListType = resultListType;
    }
}