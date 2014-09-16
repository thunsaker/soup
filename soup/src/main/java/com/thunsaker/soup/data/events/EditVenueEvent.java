package com.thunsaker.soup.data.events;

import com.thunsaker.android.common.bus.BaseEvent;

public class EditVenueEvent extends BaseEvent {
    public int source;

    public EditVenueEvent(Boolean result, String resultMessage, int resultSource) {
        super(result, resultMessage);
        source = resultSource;
    }
}