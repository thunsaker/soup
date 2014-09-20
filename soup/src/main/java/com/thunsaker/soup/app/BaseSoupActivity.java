package com.thunsaker.soup.app;

import android.os.Bundle;
import android.view.Window;

import com.thunsaker.android.common.dagger.BaseActivity;

public class BaseSoupActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_PROGRESS);
    }

    @Override
    protected Object[] getActivityModules() {
        return new Object[] {
                new SoupActivityModule(this)
        };
    }
}
