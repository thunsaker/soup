package com.thunsaker.soup.data.events;

import com.thunsaker.android.common.bus.BaseEvent;
import com.thunsaker.soup.data.api.model.Checkin;

import java.util.List;

public class CheckinHistoryEvent extends BaseEvent {
    public List<Checkin> resultHistoryList;

    public CheckinHistoryEvent(Boolean result, String resultMessage, List<Checkin> resultHistoryList) {
        super(result, resultMessage);
        this.resultHistoryList = resultHistoryList;
    }
}