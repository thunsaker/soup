package com.thunsaker.soup.app;

import com.thunsaker.android.common.dagger.BaseFragment;

public class BaseSoupFragment extends BaseFragment {
    @Override
    protected Object[] getActivityModules() {
        return new Object[] {
                new SoupActivityModule(this.getActivity())
        };
    }
}
