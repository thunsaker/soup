package com.thunsaker.soup.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.thunsaker.soup.R;
import com.thunsaker.soup.data.api.model.TimeFrame;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by thunsaker on 7/19/13.
 */
public class VenueEditHoursFragment extends Fragment {
    public static final String ARG_OBJECT = "object";

    protected static final String TIME_SEGMENT_DELETE_CONFIRMATION_DIALOG = "TIME_SEGMENT_DELETE_CONFIRMATION_DIALOG";
    public Integer confirmDeletePosition = -1;

    public static VenueHoursListAdapter currentVenueHoursListAdapter;
    public static List<TimeFrame> updatedTimes;
    public static List<TimeFrame> currentVenueListHours;

    public ListView mListViewHours;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(
                R.layout.fragment_venue_edit_hours, container, false);
//        Bundle args = getArguments();
        mListViewHours = (ListView) rootView.findViewById(R.id.listViewEditVenueHours);
        if(currentVenueListHours == null)
            currentVenueListHours = new ArrayList<TimeFrame>();

        if(VenueEditTabsActivity.originalVenue != null
                && VenueEditTabsActivity.originalVenue.venueHours != null
                && VenueEditTabsActivity.originalVenue.venueHours.timeFrames != null
                && VenueEditTabsActivity.originalVenue.venueHours.timeFrames.size() > 0
                && currentVenueListHours.size() == 0) {
            currentVenueListHours.addAll(
                    VenueEditTabsActivity.originalVenue.venueHours.timeFrames);
        }

        currentVenueHoursListAdapter = new VenueHoursListAdapter(
                getActivity().getApplicationContext(),
                R.layout.list_hours_item,
                currentVenueListHours);

        RelativeLayout mRelativeLayoutAdd =
                (RelativeLayout) inflater.inflate(R.layout.list_hours_item_add, null);
        mRelativeLayoutAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShowAddHours();
            }
        });

        mListViewHours.addFooterView(mRelativeLayoutAdd);
        mListViewHours.setAdapter(currentVenueHoursListAdapter);

        currentVenueHoursListAdapter.notifyDataSetChanged();

        return rootView;
    }

    public class VenueHoursListAdapter extends ArrayAdapter<TimeFrame> {
        public List<TimeFrame> items;

        public VenueHoursListAdapter(Context context,
                                     int textViewResourceId,
                                     List<TimeFrame> items) {
            super(context, textViewResourceId, items);
            this.items = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            final int pos = position;

            if(v == null) {
                LayoutInflater viewInflater =
                        (LayoutInflater.from(getActivity().getApplicationContext()));
                v = viewInflater.inflate(R.layout.list_hours_item, null);
            }

            try {
                final LinearLayout layoutTimeWrapper =
                        (LinearLayout) v.findViewById(R.id.linearLayoutVenueEditHoursItemWrapper);

                final TimeFrame time = items.get(pos);
                final TextView daysTextView =
                        (TextView)v.findViewById(R.id.textViewVenueEditHoursDays);
                final TextView timeTextView =
                        (TextView)v.findViewById(R.id.textViewVenueEditHoursTime);

                if (time != null && !time.openTimesString.equals("")) {
                    final String myTimeDays = time.daysString != null
                            ? time.daysString
                            : "";
                    final String myTimeHours = time.openTimesString != null
                            ? time.openTimesString
                            : "";

                    daysTextView.setText(myTimeDays);
                    timeTextView.setText(myTimeHours);
                }

                layoutTimeWrapper.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ShowAddEditHours(pos, time);
                    }
                });

                layoutTimeWrapper.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        VenueEditTabsActivity.ConfirmDeleteDialogFragment confirmDeleteDialogFragment =
                                VenueEditTabsActivity.ConfirmDeleteDialogFragment.newInstance(pos);
                        confirmDeleteDialogFragment.show(getFragmentManager(), TIME_SEGMENT_DELETE_CONFIRMATION_DIALOG);
                        return true;
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
            return v;
        }
    }

    public void ShowAddHours() {
        ShowAddEditHours(-1, null);
    }

    public void ShowAddEditHours(int item, TimeFrame timeToEdit) {
        VenueEditTabsActivity.ShowRevertOption(getActivity());

        Intent editHoursIntent =
                new Intent(
                        getActivity().getApplicationContext(),
                        VenueEditHoursAddActivity.class);
        VenueEditHoursAddActivity.originalTimeFrame = timeToEdit;
        editHoursIntent.putExtra(
                VenueEditHoursAddActivity.ORIGINAL_HOURS_EXTRA,
                item);
        startActivityForResult(editHoursIntent,
                VenueEditHoursAddActivity.EDIT_HOURS);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == VenueEditHoursAddActivity.EDIT_HOURS) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    VenueEditHoursFragment.currentVenueHoursListAdapter.notifyDataSetChanged();
                    VenueEditTabsActivity.ShowRevertOption(getActivity());
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}