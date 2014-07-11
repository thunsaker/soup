package com.thunsaker.android.common.dagger;

import android.os.Bundle;
import android.support.v4.app.Fragment;

public class BaseFragment extends Fragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((BaseActivity)getActivity()).inject(this);
    }

    protected <T> T getView(int id) {
        return (T) getView().findViewById(id);
    }
}