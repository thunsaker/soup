package com.thunsaker.soup.app;

import com.thunsaker.android.common.dagger.DaggerApplication;

import java.util.Collections;
import java.util.List;

public class SoupApp extends DaggerApplication {

    @Override
    protected List<Object> getAppModules() {
        return Collections.<Object>singletonList(new SoupAppModule());
    }
}