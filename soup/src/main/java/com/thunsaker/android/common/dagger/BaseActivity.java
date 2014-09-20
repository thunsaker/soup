package com.thunsaker.android.common.dagger;

import android.app.Activity;
import android.os.Bundle;

import dagger.ObjectGraph;

public abstract class BaseActivity extends Activity implements Injector {
    private ObjectGraph mActivityGraph;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DaggerApplication daggerApplication = (DaggerApplication) getApplication();
        mActivityGraph = daggerApplication.getObjectGraph().plus(getActivityModules());

        mActivityGraph.inject(this);
    }

    @Override
    protected void onDestroy() {
        mActivityGraph = null;
        super.onDestroy();
    }

    protected <T> T getView(int id) {
        return (T) findViewById(id);
    }

    @Override
    public void inject(Object object) {
        mActivityGraph.inject(object);
    }

    public ObjectGraph getObjectGraph() {
        return mActivityGraph;
    }

    protected abstract Object[] getActivityModules();
}
