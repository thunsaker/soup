package com.thunsaker.soup;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import com.thunsaker.soup.ui.VenueEditHoursFragment;
import com.thunsaker.soup.ui.VenueEditInfoFragment;
import com.thunsaker.soup.ui.VenueEditLocationFragment;

/*
 * Created by @thunsaker
 */
public class VenueEditPagerAdapter extends FragmentPagerAdapter {
    protected static final String[] CONTENT = new String[] { "Info","Location","Hours" };
    private FragmentManager mFragmentManager;
    public VenueEditPagerAdapter(FragmentManager fm) {
        super(fm);
        mFragmentManager = fm;
    }

    @Override
    public Fragment getItem(int i) {
        Bundle args = new Bundle();
        String name = makeFragmentName(R.id.pager, i);
        Fragment fragment = mFragmentManager.findFragmentByTag(name);

        switch (i) {
            case 0:
                fragment = new VenueEditInfoFragment();
                args.putInt(VenueEditInfoFragment.ARG_OBJECT, i + 1);
                break;
            case 1:
                fragment = new VenueEditLocationFragment();
                args.putInt(VenueEditLocationFragment.ARG_OBJECT, i + 1);
                break;
            case 2:
                fragment = new VenueEditHoursFragment();
                args.putInt(VenueEditHoursFragment.ARG_OBJECT, i + 1);
                break;
        }

        fragment.setArguments(args);
        return fragment;
    }

    private static String makeFragmentName(int viewId, int index) {
        return "android:switcher" + viewId + ":" + index;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        return super.instantiateItem(container, position);
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return VenueEditPagerAdapter.CONTENT[position];
    }
}