package com.thunsaker.soup.data.events;

import com.thunsaker.android.common.bus.BaseEvent;
import com.thunsaker.soup.data.api.model.FoursquareList;

public class GetListEvent extends BaseEvent {
    public FoursquareList resultList;
    public GetListEvent(Boolean result, String resultMessage, FoursquareList resultList) {
        super(result, resultMessage);
        this.resultList = resultList;
    }
}
