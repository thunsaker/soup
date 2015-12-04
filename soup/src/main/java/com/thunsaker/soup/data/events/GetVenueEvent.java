package com.thunsaker.soup.data.events;

import com.thunsaker.android.common.bus.BaseEvent;
import com.thunsaker.soup.data.api.model.Venue;

public class GetVenueEvent extends BaseEvent {
    public Venue resultVenue;
    public int source;
    public boolean withHours;

    public GetVenueEvent(Boolean result, String resultMessage, Venue resultVenue, int resultSource, boolean withHours) {
        super(result, resultMessage);
        this.resultVenue = resultVenue;
        this.source = resultSource;
        this.withHours = withHours;
    }
}