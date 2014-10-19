package com.thunsaker.soup.app;

import org.robolectric.Robolectric;

import java.util.ArrayList;
import java.util.List;

public class TestSoupApp extends SoupApp {

    public static <T> void injectMocks(T object) {
        TestSoupApp app = (TestSoupApp) Robolectric.application;
        app.inject(object);
    }

    protected List<Object> getAppModules() {
//        List<Object> modules = super.getAppModules();
//        modules.add(new SoupTestModule());
//        return modules;

        List<Object> modules = new ArrayList<Object>();
        modules.add(new SoupTestModule());
        return modules;
    }
}