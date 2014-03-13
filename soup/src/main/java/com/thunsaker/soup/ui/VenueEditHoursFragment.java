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
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.thunsaker.soup.R;
import com.thunsaker.soup.classes.foursquare.TimeFrame;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by thunsaker on 7/19/13.
 */
public class VenueEditHoursFragment extends Fragment {
    public static final String ARG_OBJECT = "object";

    public static String DIALOG_CONFIRM_DELETE = "DIALOG_CONFIRM_DELETE";
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
                && VenueEditTabsActivity.originalVenue.getVenueHours() != null
                && VenueEditTabsActivity.originalVenue.getVenueHours().getTimeFrames() != null
                && VenueEditTabsActivity.originalVenue.getVenueHours().getTimeFrames().size() > 0
                && currentVenueListHours.size() == 0) {
            currentVenueListHours.addAll(
                    VenueEditTabsActivity.originalVenue.getVenueHours().getTimeFrames());
        }

        currentVenueHoursListAdapter = new VenueHoursListAdapter(
                getActivity().getApplicationContext(),
                R.layout.list_hours_item,
                currentVenueListHours);

        LinearLayout mLinearLayoutAdd =
                (LinearLayout) inflater.inflate(R.layout.list_hours_item_add, null);
        mLinearLayoutAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShowAddHours();
            }
        });

        mListViewHours.addFooterView(mLinearLayoutAdd);
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
                final LinearLayout layoutButtonWrapper =
                        (LinearLayout) v.findViewById(R.id.linearLayoutVenueEditButtonsWrapper);

                final Button buttonAddEditSave =
                        (Button) v.findViewById(R.id.buttonVenueEditTimeFrameAddEditSave);
                final Button buttonDeleteCancel =
                        (Button) v.findViewById(R.id.buttonVenueEditTimeFrameDeleteCancel);

                final TimeFrame time = items.get(pos);
                
                final TextView daysTextView =
                        (TextView)v.findViewById(R.id.textViewVenueEditHoursDays);
                final TextView timeTextView =
                        (TextView)v.findViewById(R.id.textViewVenueEditHoursTime);
        		
                if (time != null && !time.getOpenTimesString().equals("")) {
                    final String myTimeDays = time.getDaysString() != null
                            ? time.getDaysString()
                            : "";
                    final String myTimeHours = time.getOpenTimesString() != null
                            ? time.getOpenTimesString()
                            : "";
                            
                    daysTextView.setText(myTimeDays);
                    timeTextView.setText(myTimeHours);

                    buttonDeleteCancel.setVisibility(View.VISIBLE);
                    buttonDeleteCancel.setText(getString(R.string.edit_venue_hours_delete));

                    buttonAddEditSave.setVisibility(View.VISIBLE);
                    buttonAddEditSave.setText(getString(R.string.edit_venue_hours_edit));
                }

                layoutTimeWrapper.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(layoutButtonWrapper.getVisibility() == View.GONE)
                            layoutButtonWrapper.setVisibility(View.VISIBLE);
                        else
                            layoutButtonWrapper.setVisibility(View.GONE);
                    }
                });

                View.OnClickListener venueHoursButtonsListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Button myView = (Button) view;
                        if(myView.getText() == getString(R.string.edit_venue_hours_edit)) {
                            ShowAddEditHours(pos, time);
                        } else if (myView.getText() ==
                                getString(R.string.edit_venue_hours_delete)) {
                            currentVenueListHours.remove(pos);
                            currentVenueHoursListAdapter.notifyDataSetChanged();
                            VenueEditTabsActivity.ShowRevertOption(getActivity());
                        }
                    }
                };
                buttonAddEditSave.setOnClickListener(venueHoursButtonsListener);
                buttonDeleteCancel.setOnClickListener(venueHoursButtonsListener);
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

//    public class DeleteTimeFrameDialogFragment extends DialogFragment {
//        @Override
//        public Dialog onCreateDialog(Bundle savedInstanceState) {
//            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//            builder.setMessage(R.string.edit_venue_delete_hours_dialog)
//                    .setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int id) {
//                            if(currentVenueListHours.size() > 0 &&
//                                confirmDeletePosition < currentVenueListHours.size()) {
//                                currentVenueListHours.remove(confirmDeletePosition);
//                                currentVenueHoursListAdapter.notifyDataSetChanged();
//                                VenueEditTabsActivity.ShowRevertOption(getSherlockActivity());
//                            }
//                            confirmDeletePosition = -1;
//                        }
//                    })
//                    .setNegativeButton(R.string.dialog_no, new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int id) {
//                            confirmDeletePosition = -1;
//                        }
//                    });
//            return builder.create();
//        }
//    }
}