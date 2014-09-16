package com.thunsaker.soup.data.events;

import com.thunsaker.android.common.bus.BaseEvent;
import com.thunsaker.soup.data.api.model.FoursquareList;

import java.util.List;

public class GetListsEvent extends BaseEvent {
    public List<FoursquareList> resultList;

    public GetListsEvent(Boolean result, String resultMessage, List<FoursquareList> resultList) {
        super(result, resultMessage);
        this.resultList = resultList;
    }
}
