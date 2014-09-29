package com.thunsaker.soup.app;

public class BaseSoupListFragment extends BaseListFragment {
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onActivityCreated(savedInstanceState);
//        ((SoupApp) getActivity().getApplication()).inject(this);
//    }

    @Override
    protected Object[] getActivityModules() {
        return new Object[]{
                new SoupActivityModule(this.getActivity())
        };
    }
}
