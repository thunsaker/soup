package com.thunsaker.soup.data.events;

import com.thunsaker.android.common.bus.BaseEvent;
import com.thunsaker.soup.data.api.model.Category;

import java.util.List;

public class GetCategoriesEvent extends BaseEvent {
    public List<Category> resultCategories;
    public int resultSource;

    public GetCategoriesEvent(Boolean result, String resultMessage, List<Category> resultCategories, int resultSource) {
        super(result, resultMessage);
        this.resultCategories = resultCategories;
        this.resultSource = resultSource;
    }
}
