package com.thunsaker.soup.app;

import android.os.Bundle;
import android.support.v4.app.ListFragment;

public class BaseSoupListFragment extends ListFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((SoupApp) getActivity().getApplication()).inject(this);
    }
}
