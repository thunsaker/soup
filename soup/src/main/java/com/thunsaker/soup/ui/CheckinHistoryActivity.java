package com.thunsaker.soup.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import com.thunsaker.android.common.annotations.ForApplication;
import com.thunsaker.soup.R;
import com.thunsaker.soup.app.BaseSoupActivity;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class CheckinHistoryActivity extends BaseSoupActivity
        implements
        CheckinHistoryFragment.OnFragmentInteractionListener,
        AdapterView.OnItemSelectedListener {

    @Inject
    @ForApplication
    Context mContext;

    @InjectView(R.id.checkinToolbar) Toolbar mToolbar;

    @InjectView(R.id.checkinSpinner) Spinner mSpinner;

    private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";

    public static final int CHECKIN_SECTION_TODAY = 0;
    public static final int CHECKIN_SECTION_LAST_WEEK = 1;
    public static final int CHECKIN_SECTION_LAST_MONTH = 2;
    public static final int CHECKIN_SECTION_CUSTOM = 3;
    public static long customStartDate;
    public static long customEndDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkin_history);

        ButterKnife.inject(this);

        setSupportActionBar(mToolbar);
        setTitle("");

        SpinnerAdapter mSpinnerAdapter = ArrayAdapter.createFromResource(getSupportActionBar().getThemedContext(),
                R.array.checkin_ranges, android.R.layout.simple_spinner_dropdown_item);

        mSpinner.setAdapter(mSpinnerAdapter);
        mSpinner.setOnItemSelectedListener(this);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
            getSupportActionBar().setSelectedNavigationItem(
                    savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(STATE_SELECTED_NAVIGATION_ITEM,
                getSupportActionBar().getSelectedNavigationIndex());
    }

    public long getEndDateTimestamp(int type) {
        Calendar endTimestampCalendar = Calendar.getInstance();
        endTimestampCalendar.add(Calendar.DATE, -1);

        endTimestampCalendar.set(Calendar.HOUR_OF_DAY, 0);
        endTimestampCalendar.set(Calendar.MINUTE, 0);
        endTimestampCalendar.set(Calendar.SECOND, 0);
        endTimestampCalendar.set(Calendar.MILLISECOND, 0);

        switch (type) {
            case 1: // HISTORY_VIEW_LAST_WEEK
                endTimestampCalendar.add(Calendar.DATE, -6);
                break;
            case 2: // HISTORY_VIEW_LAST_MONTH
                endTimestampCalendar.add(Calendar.DATE, -29);
                break;
            default: // HISTORY_VIEW_TODAY
                break;
        }

        return endTimestampCalendar.getTimeInMillis() / 1000;
    }

    @Override
    public void onClick(String compactVenueJson) {
        Intent venueDetailsIntent =
                new Intent(mContext, VenueDetailActivity.class);
        venueDetailsIntent.putExtra(
                VenueDetailFragment.ARG_ITEM_JSON_STRING, compactVenueJson);
        venueDetailsIntent.putExtra(
                VenueDetailActivity.VENUE_DETAILS_SOURCE,
                VenueDetailActivity.VENUE_DETAIL_SOURCE_HISTORY);
        startActivity(venueDetailsIntent);
    }

    @Override
    public boolean onLongClick(String checkinId, String checkinVenueName) {
        return false;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Calendar cal_today =
                Calendar.getInstance(TimeZone.getDefault(),
                        Locale.getDefault());
        long startDate = cal_today.getTimeInMillis() / 1000;
        long endDate = getEndDateTimestamp(CHECKIN_SECTION_TODAY);
        switch (position) {
            case 1: // Last Week
                endDate = getEndDateTimestamp(CHECKIN_SECTION_LAST_WEEK);
                customStartDate = 0;
                customEndDate = 0;
                break;
            case 2: // Last Month
                endDate = getEndDateTimestamp(CHECKIN_SECTION_LAST_MONTH);
                customStartDate = 0;
                customEndDate = 0;
                break;
            case 3: // Custom date range, get it from the date picker
                startDate = customStartDate != 0 ? customStartDate : -1;
                endDate = customEndDate != 0 ? customEndDate : -1;
                break;
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(
                        R.id.checkinHistoryContent,
                        CheckinHistoryFragment.newInstance(
                                position, startDate, endDate))
                .commit();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        Calendar cal_today =
                Calendar.getInstance(TimeZone.getDefault(),
                        Locale.getDefault());
        long startDate = cal_today.getTimeInMillis() / 1000;
        long endDate = getEndDateTimestamp(CHECKIN_SECTION_TODAY);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(
                        R.id.checkinHistoryContent,
                        CheckinHistoryFragment.newInstance(
                                0, startDate, endDate))
                .commit();
    }
}