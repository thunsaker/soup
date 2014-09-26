package com.thunsaker.soup.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.doomonafireball.betterpickers.timepicker.TimePickerBuilder;
import com.doomonafireball.betterpickers.timepicker.TimePickerDialogFragment.TimePickerDialogHandler;
import com.thunsaker.android.common.annotations.ForApplication;
import com.thunsaker.soup.R;
import com.thunsaker.soup.app.BaseSoupActivity;
import com.thunsaker.soup.data.api.model.TimeFrame;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class VenueEditHoursAddActivity extends BaseSoupActivity
        implements TimePickerDialogHandler {

    @Inject
    @ForApplication
    Context mContext;

    protected static final String TIME_SEGMENT_DELETE_CONFIRMATION_DIALOG = "TIME_SEGMENT_DELETE_CONFIRMATION_DIALOG";

    public static final Integer TIME_PICKER_OPEN = 100;
    public static final Integer TIME_PICKER_CLOSE = 200;

    public static final int EDIT_HOURS = 0;
    public static final String ORIGINAL_HOURS_EXTRA = "ORIGINAL_HOURS_EXTRA";
    public static Integer itemToUpdate = -1;

    public static final DateTimeFormatter SIMPLE_DATE_FORMAT_HOURS = DateTimeFormat.forPattern("H:mma");

    public static TimeFrame originalTimeFrame;
    public TimeFrame updatedTimeFrame;

    @InjectView(R.id.toggleButtonSun) ToggleButton mToggleButtonSun;
    @InjectView(R.id.toggleButtonMon) ToggleButton mToggleButtonMon;
    @InjectView(R.id.toggleButtonTue) ToggleButton mToggleButtonTue;
    @InjectView(R.id.toggleButtonWed) ToggleButton mToggleButtonWed;
    @InjectView(R.id.toggleButtonThu) ToggleButton mToggleButtonThu;
    @InjectView(R.id.toggleButtonFri) ToggleButton mToggleButtonFri;
    @InjectView(R.id.toggleButtonSat) ToggleButton mToggleButtonSat;

    // TODO: Swap this out for a checkbox/switch...
    @InjectView(R.id.linearLayout24HoursWrapper) LinearLayout m24HourWrapper;
    @InjectView(R.id.checkBox24Hours) CheckBox mCheckBox24Hours;

    @InjectView(R.id.linearLayoutTimeSegmentContainer) LinearLayout mTimeSegmentContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_venue_edit_hours_item);

        SetupActionBar();

        ButterKnife.inject(this);

        mCheckBox24Hours.setOnCheckedChangeListener(twentyFourHourToggleChangeListener);

        LayoutInflater inflater = LayoutInflater.from(mContext);

        if(originalTimeFrame != null && originalTimeFrame.openTime != null && originalTimeFrame.closeTime != null) {
            // Set the days
            if(originalTimeFrame.daysList != null) {
                mToggleButtonMon.setChecked(
                        originalTimeFrame.daysList.contains(1));
                mToggleButtonTue.setChecked(
                        originalTimeFrame.daysList.contains(2));
                mToggleButtonWed.setChecked(
                        originalTimeFrame.daysList.contains(3));
                mToggleButtonThu.setChecked(
                        originalTimeFrame.daysList.contains(4));
                mToggleButtonFri.setChecked(
                        originalTimeFrame.daysList.contains(5));
                mToggleButtonSat.setChecked(
                        originalTimeFrame.daysList.contains(6));
                mToggleButtonSun.setChecked(
                        originalTimeFrame.daysList.contains(7));
            }

            for (int i = 0; i < originalTimeFrame.openTime.size(); i++) {
                final int segment = i;
                mTimeSegmentContainer.setVisibility(View.VISIBLE);
                LinearLayout newSegment = (LinearLayout) inflater.inflate(R.layout.item_time_segment, null);
                // Set the times
                String openTime = originalTimeFrame.openTime.get(i);
                String openTimeHour = openTime.substring(0, 2);
                String openTimeMinute = openTime.substring(2, 4);
                TextView textViewOpenTime = (TextView)newSegment.findViewById(R.id.textViewOpenTime);
                textViewOpenTime.setText(GetClockStyleTimeString(openTimeHour, openTimeMinute));
                newSegment.findViewById(R.id.linearLayoutOpenTimeWrapper).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openStartTimePicker(segment);
                    }
                });

                String closeTime = originalTimeFrame.closeTime.get(i);
                String closeTimeHour = closeTime.substring(0, 2);
                String closeTimeMinute = closeTime.substring(2, 4);
                TextView textViewCloseTime = (TextView)newSegment.findViewById(R.id.textViewCloseTime);
                textViewCloseTime.setText(GetClockStyleTimeString(closeTimeHour, closeTimeMinute));
                newSegment.findViewById(R.id.linearLayoutCloseTimeWrapper).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openEndTimePicker(segment);
                    }
                });

                newSegment.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        ConfirmDeleteDialogFragment confirmDeleteDialogFragment =
                                ConfirmDeleteDialogFragment.newInstance(segment + 1);
                        confirmDeleteDialogFragment.show(getSupportFragmentManager(), TIME_SEGMENT_DELETE_CONFIRMATION_DIALOG);
                        return true;
                    }
                });

                mTimeSegmentContainer.addView(newSegment, i);

                if (openTime.equals("0000") && closeTime.equals("+0000")) {
                    mCheckBox24Hours.setChecked(true);
                    mTimeSegmentContainer.setVisibility(View.GONE);
                    return;
                } else {
                    mCheckBox24Hours.setChecked(false);
                    mTimeSegmentContainer.setVisibility(View.VISIBLE);
                }
            }
        } else {
            LinearLayout newSegment = (LinearLayout) inflater.inflate(R.layout.item_time_segment, null);

            newSegment.findViewById(R.id.linearLayoutOpenTimeWrapper).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openStartTimePicker(0);
                }
            });

            newSegment.findViewById(R.id.linearLayoutCloseTimeWrapper).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openEndTimePicker(0);
                }
            });

            newSegment.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    ConfirmDeleteDialogFragment confirmDeleteDialogFragment =
                            ConfirmDeleteDialogFragment.newInstance(0);
                    confirmDeleteDialogFragment.show(getSupportFragmentManager(), TIME_SEGMENT_DELETE_CONFIRMATION_DIALOG);
                    return true;
                }
            });

            mTimeSegmentContainer.addView(newSegment);
        }

        if(getIntent().hasExtra(VenueEditHoursAddActivity.ORIGINAL_HOURS_EXTRA)) {
            itemToUpdate = Integer.parseInt(
                    getIntent().getExtras()
                            .get(VenueEditHoursAddActivity.ORIGINAL_HOURS_EXTRA).toString());
        }
    }

    private String GetClockStyleTimeString(int hour, int minute) {
        String timeFormat = "%1$02d:%2$02d";
//        if(TimePicker.get24HourMode(mContext) && hour > 12) {
//            hour -= 12;
//            timeFormat = "%1$02d:%2$02d";
//        }

        return String.format(timeFormat, hour, minute);
    }

    private String GetClockStyleTimeString(String hour, String minute) {
        return GetClockStyleTimeString(Integer.parseInt(hour), Integer.parseInt(minute));
    }

    private void SetupActionBar() {
        ActionBar ab = getSupportActionBar();
        ab.setDisplayShowHomeEnabled(true);
        ab.setDisplayUseLogoEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setIcon(getResources().getDrawable(R.drawable.ic_launcher_white));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_venue_edit_hours, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(Activity.RESULT_CANCELED);
                finish();
                return true;
            case R.id.action_cancel:
                setResult(Activity.RESULT_CANCELED);
                finish();
                return true;
            case R.id.action_save:
                if(ValidateFormAndSave()) {
                    if(VenueEditHoursFragment.currentVenueListHours == null)
                        VenueEditHoursFragment.currentVenueListHours = new ArrayList<TimeFrame>();

                    if(updatedTimeFrame != null) {
                        if(itemToUpdate == -1) {
                            VenueEditHoursFragment.currentVenueListHours.add(updatedTimeFrame);
                        } else {
                            VenueEditHoursFragment.currentVenueListHours.set(itemToUpdate, updatedTimeFrame);
                        }

                        setResult(Activity.RESULT_OK);
                        finish();
                        return true;
                    }  else {
                        Toast.makeText(getApplicationContext(),
                                "Nothing to Save",
                                Toast.LENGTH_SHORT).show();
                        return false;
                    }
                } else {
                    return false;
                }
        }
        return false;
    }

    private boolean ValidateFormAndSave() {
        List<Integer> selectedDays = new ArrayList<Integer>();

        if(mToggleButtonMon.isChecked())
            selectedDays.add(1);
        if(mToggleButtonTue.isChecked())
            selectedDays.add(2);
        if(mToggleButtonWed.isChecked())
            selectedDays.add(3);
        if(mToggleButtonThu.isChecked())
            selectedDays.add(4);
        if(mToggleButtonFri.isChecked())
            selectedDays.add(5);
        if(mToggleButtonSat.isChecked())
            selectedDays.add(6);
        if(mToggleButtonSun.isChecked())
            selectedDays.add(7);

        if(selectedDays.size() > 0){
            updatedTimeFrame = new TimeFrame();
            updatedTimeFrame.daysList = selectedDays;

            if(mCheckBox24Hours.isChecked()) {
                updatedTimeFrame.is24Hours = true;
                updatedTimeFrame.openTimesString = getString(R.string.edit_venue_hours_24_hours);
            } else {
                // TODO: For each time segment
                if(mTimeSegmentContainer.getChildCount() > 0)
                    for (int i = 0; i < mTimeSegmentContainer.getChildCount(); i++) {
                        String rawTimeOpen = ((TextView)mTimeSegmentContainer.findViewById(R.id.textViewOpenTime)).getText().toString();
                        String rawTimeClose = ((TextView)mTimeSegmentContainer.findViewById(R.id.textViewCloseTime)).getText().toString();

                        if(updatedTimeFrame.openTime == null)
                            updatedTimeFrame.openTime = new ArrayList<String>();

                        if(updatedTimeFrame.closeTime == null)
                            updatedTimeFrame.closeTime = new ArrayList<String>();

                        if(updatedTimeFrame.openTime.size() == 0)
                            updatedTimeFrame.openTime.add(rawTimeOpen.replace(":", ""));
                        else if(updatedTimeFrame.openTime.get(i) != null)
                            updatedTimeFrame.openTime.set(i, rawTimeOpen.replace(":", ""));

                        if(updatedTimeFrame.closeTime.size() == 0)
                            updatedTimeFrame.closeTime.add(rawTimeClose.replace(":", ""));
                        else if(updatedTimeFrame.closeTime.get(i) != null)
                            updatedTimeFrame.closeTime.set(i, rawTimeClose.replace(":", ""));

                        String openString = updatedTimeFrame.openTime.get(i);
                        String closeString = updatedTimeFrame.closeTime.get(i);

                        String newTimeString = TimeFrame.GetStringSegmentFormat(openString, closeString);

                        updatedTimeFrame.openTimesString +=
                                updatedTimeFrame.openTimesString.length() > 0
                                        ? ", " + newTimeString
                                        : newTimeString;
                    }
            }

            if(selectedDays.size() == 1) {
                updatedTimeFrame.daysString =
                        TimeFrame.ConvertIntegerDayToLocalizedDayString(
                                mContext, selectedDays.get(0));
            } else if(selectedDays.size() == 7) {
                updatedTimeFrame.daysString =
                        TimeFrame.GetStringSegmentFormat(
                                TimeFrame.ConvertIntegerDayToLocalizedDayString(mContext, Collections.min(selectedDays)),
                                TimeFrame.ConvertIntegerDayToLocalizedDayString(mContext, Collections.max(selectedDays)));
            } else {
                StringBuilder daysString = new StringBuilder();
                for (Integer selectedDay : selectedDays) {
                    String dayName = TimeFrame.ConvertIntegerDayToLocalizedDayString(
                            getApplicationContext(), selectedDay);

                    if (daysString.length() == 0) {
                        daysString.append(dayName);
                    } else {
                        daysString.append(",").append(dayName);
                    }
                }
                updatedTimeFrame.daysString = daysString.toString();
            }
            return true;
        } else {
            updatedTimeFrame = null;
            return false;
        }
    }

    public void openStartTimePicker(int segment) {
        TimePickerBuilder openBuilder = new TimePickerBuilder()
                .setReference(TIME_PICKER_OPEN + segment)
                .setFragmentManager(getSupportFragmentManager())
                .setStyleResId(R.style.BetterPickerTimeTheme);
//                .setStyleResId(com.doomonafireball.betterpickers.R.style.BetterPickersDialogFragment);
        openBuilder.show();
    }

    public void openEndTimePicker(int segment) {
        TimePickerBuilder closeBuilder = new TimePickerBuilder()
                .setReference(TIME_PICKER_CLOSE + segment)
                .setFragmentManager(getSupportFragmentManager())
                .setStyleResId(R.style.BetterPickerTimeTheme);
//                .setStyleResId(com.doomonafireball.betterpickers.R.style.BetterPickersDialogFragment);
        closeBuilder.show();
    }

    @OnClick(R.id.linearLayout24HoursWrapper)
    public void Click24HoursWrapper() {
        mCheckBox24Hours.performClick();
    }

    CompoundButton.OnCheckedChangeListener twentyFourHourToggleChangeListener = new CompoundButton.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			if(isChecked) {
                mTimeSegmentContainer.setVisibility(View.GONE);
			} else {
                mTimeSegmentContainer.setVisibility(View.VISIBLE);
            }
		}
	};

    @OnClick(R.id.relativeLayoutHoursAddWrapper)
    public void ClickAddHours() {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        LinearLayout newSegment = (LinearLayout)inflater.inflate(R.layout.item_time_segment, null);

        final int childCount = mTimeSegmentContainer.getChildCount();

        newSegment.findViewById(R.id.linearLayoutOpenTimeWrapper).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openStartTimePicker(childCount);
            }
        });

        newSegment.findViewById(R.id.linearLayoutCloseTimeWrapper).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openEndTimePicker(childCount);
            }
        });

        newSegment.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ConfirmDeleteDialogFragment confirmDeleteDialogFragment =
                        ConfirmDeleteDialogFragment.newInstance(childCount);
                confirmDeleteDialogFragment.show(getSupportFragmentManager(), TIME_SEGMENT_DELETE_CONFIRMATION_DIALOG);
                return true;
            }
        });

        mTimeSegmentContainer.addView(newSegment);
    }

    @Override
    public void onDialogTimeSet(int reference, int hourOfDay, int minute) {
        if(reference >= TIME_PICKER_CLOSE) { // Close
            LinearLayout timeSegment = (LinearLayout) mTimeSegmentContainer.getChildAt(reference - 200);
            TextView textViewClose = (TextView)timeSegment.findViewById(R.id.textViewCloseTime);
            textViewClose.setText(GetClockStyleTimeString(hourOfDay, minute));
        } else if (reference >= TIME_PICKER_OPEN) {  // Open
            LinearLayout timeSegment = (LinearLayout) mTimeSegmentContainer.getChildAt(reference - 100);
            TextView textViewOpen = (TextView)timeSegment.findViewById(R.id.textViewOpenTime);
            String textString = GetClockStyleTimeString(hourOfDay, minute);
            textViewOpen.setText(textString);
        }
    }

    public static class ConfirmDeleteDialogFragment extends DialogFragment {
        private static String ITEM_TO_DELETE = "ITEM_TO_DELETE";

        public static ConfirmDeleteDialogFragment newInstance(int item) {
            ConfirmDeleteDialogFragment fragment = new ConfirmDeleteDialogFragment();
            Bundle args = new Bundle();
            args.putInt(ITEM_TO_DELETE, item);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            if (getArguments() != null) {
                final int mItem = getArguments().getInt(ITEM_TO_DELETE);

                return new AlertDialog.Builder(getActivity())
                        .setMessage(String.format(getString(R.string.dialog_hour_segment_delete_confirm)))
                        .setPositiveButton(
                                getString(R.string.dialog_yes),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        ((VenueEditHoursAddActivity) getActivity()).doRemoveSegment(mItem);
                                    }
                                }
                        )
                        .setNegativeButton(getString(R.string.dialog_no), null)
                        .create();
            } else
                return null;
        }
    }

    public void doRemoveSegment(int item) {
        RemoveSegment(item);
    }

    private void RemoveSegment(int item) {
        if(mTimeSegmentContainer.getChildAt(item) != null)
            mTimeSegmentContainer.removeViewAt(item);
    }
}