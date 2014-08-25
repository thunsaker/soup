package com.thunsaker.soup.data.events;

import com.thunsaker.android.common.bus.BaseEvent;
import com.thunsaker.soup.data.api.model.TimeFrame;

import java.util.List;

public class GetVenueHoursEvent extends BaseEvent {
    public List<TimeFrame> resultVenueHours;
    public int source;

    public GetVenueHoursEvent(Boolean result, String resultMessage, List<TimeFrame> resultVenueHours, int resultSource) {
        super(result, resultMessage);
        this.resultVenueHours = resultVenueHours;
        this.source = resultSource;
    }
}