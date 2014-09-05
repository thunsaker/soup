package com.thunsaker.soup.data.events;

import com.thunsaker.android.common.bus.BaseEvent;
import com.thunsaker.soup.data.api.model.CompactFoursquareUser;

public class GetUserInfoEvent extends BaseEvent {
    public CompactFoursquareUser user;

    public GetUserInfoEvent(Boolean result, String resultMessage, CompactFoursquareUser resultUser) {
        super(result, resultMessage);
        user = resultUser;
    }
}
