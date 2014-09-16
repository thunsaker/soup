package com.thunsaker.soup.data.events;

import com.thunsaker.android.common.bus.BaseEvent;

public class FlagVenueEvent extends BaseEvent {
    public FlagVenueEvent(Boolean result, String resultMessage) {
        super(result, resultMessage);
    }
}