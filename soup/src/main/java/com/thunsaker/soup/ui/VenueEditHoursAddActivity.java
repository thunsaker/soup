package com.thunsaker.soup.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.doomonafireball.betterpickers.timepicker.TimePickerBuilder;
import com.doomonafireball.betterpickers.timepicker.TimePickerDialogFragment.TimePickerDialogHandler;
import com.thunsaker.soup.R;
import com.thunsaker.soup.app.BaseSoupActivity;
import com.thunsaker.soup.data.api.model.TimeFrame;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Created by thunsaker on 7/19/13.
 */
public class VenueEditHoursAddActivity extends BaseSoupActivity
        implements TimePickerDialogHandler {

    public static final Integer TIME_PICKER_OPEN = 0;
    public static final Integer TIME_PICKER_CLOSE = 1;

    public static final int EDIT_HOURS = 0;
    public static final String ORIGINAL_HOURS_EXTRA = "ORIGINAL_HOURS_EXTRA";
    public static Integer itemToUpdate = -1;

    public TextView mTextViewTimeOpen;
    public TextView mTextViewTimeOpenLabel;
    public TextView mTextViewTimeClose;
    public TextView mTextViewTimeCloseLabel;

    public static TimeFrame originalTimeFrame;
    public TimeFrame updatedTimeFrame;

    public ToggleButton mToggleButtonSun;
    public ToggleButton mToggleButtonMon;
    public ToggleButton mToggleButtonTue;
    public ToggleButton mToggleButtonWed;
    public ToggleButton mToggleButtonThu;
    public ToggleButton mToggleButtonFri;
    public ToggleButton mToggleButtonSat;
    public ToggleButton mToggleButton24;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_venue_edit_hours_item);

        SetupActionBar();

        mTextViewTimeOpen = (TextView) findViewById(R.id.textViewVenueEditTimeStart);
        mTextViewTimeOpen.setOnClickListener(openTimeListener);
        mTextViewTimeOpenLabel = (TextView) findViewById(R.id.textViewVenueEditTimeStartLabel);
        mTextViewTimeOpenLabel.setOnClickListener(openTimeListener);

        mTextViewTimeClose = (TextView) findViewById(R.id.textViewVenueEditTimeEnd);
        mTextViewTimeClose.setOnClickListener(closeTimeListener);
        mTextViewTimeCloseLabel = (TextView) findViewById(R.id.textViewVenueEditTimeEndLabel);
        mTextViewTimeCloseLabel.setOnClickListener(closeTimeListener);

        mToggleButtonSun = (ToggleButton)findViewById(R.id.toggleButtonSun);
        mToggleButtonMon = (ToggleButton)findViewById(R.id.toggleButtonMon);
        mToggleButtonTue = (ToggleButton)findViewById(R.id.toggleButtonTue);
        mToggleButtonWed = (ToggleButton)findViewById(R.id.toggleButtonWed);
        mToggleButtonThu = (ToggleButton)findViewById(R.id.toggleButtonThu);
        mToggleButtonFri = (ToggleButton)findViewById(R.id.toggleButtonFri);
        mToggleButtonSat = (ToggleButton)findViewById(R.id.toggleButtonSat);
        mToggleButton24 = (ToggleButton)findViewById(R.id.toggleButton24Hours);

        mToggleButton24.setOnCheckedChangeListener(twentyFourHourToggleChangeListener);

        if(originalTimeFrame != null) {
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

            // Set the times
            String openTime = originalTimeFrame.openTime;
            if(openTime.length() < 4)
                openTime = "0" + openTime;
            // TODO: Fix error editing existing times
            mTextViewTimeOpen.setText(
                    String.format("%s:%s",
                            openTime.substring(0,2), openTime.substring(2)));

            String closeTime = originalTimeFrame.closeTime;
            if(closeTime.length() < 4)
                closeTime = "0" + closeTime;
            if(closeTime.length() == 5)
                closeTime = closeTime.substring(1);
            mTextViewTimeClose.setText(
                    String.format("%s:%s",
                            closeTime.substring(0,2),closeTime.substring(2)));

            if(openTime.equals("0000") && closeTime.equals("+0000")) {
            	mToggleButton24.setChecked(true);
            } else {
            	mToggleButton24.setChecked(false);
            }
        }

        if(getIntent().hasExtra(VenueEditHoursAddActivity.ORIGINAL_HOURS_EXTRA)) {
            itemToUpdate = Integer.parseInt(
                    getIntent().getExtras()
                            .get(VenueEditHoursAddActivity.ORIGINAL_HOURS_EXTRA).toString());
        }
    }

    private void SetupActionBar() {
        ActionBar ab = getSupportActionBar();
        ab.setDisplayShowHomeEnabled(true);
        ab.setDisplayUseLogoEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);
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
        // Check to see if we can save
        if(!mTextViewTimeOpen.getText().toString().equals("")
                && !mTextViewTimeClose.getText().toString().equals("")) {
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

                if(mToggleButton24.isChecked()) {
                	updatedTimeFrame.is24Hours = true;
                	updatedTimeFrame.openTimesString = getString(R.string.edit_venue_hours_24_hours);
                } else {
	                String rawTimeOpen = mTextViewTimeOpen.getText().toString();
	                String rawTimeClose = mTextViewTimeClose.getText().toString();
	                updatedTimeFrame.openTime = rawTimeOpen.replace(":", "");
	                updatedTimeFrame.closeTime = rawTimeClose.replace(":", "");

	                updatedTimeFrame.openTimesString =
	                        String.format("%s-%s", rawTimeOpen, rawTimeClose);
                }

                if(selectedDays.size() == 1) {
                    updatedTimeFrame.daysString =
                            TimeFrame.ConvertIntegerDayToLocalizedDayString(
                                    getApplicationContext(), selectedDays.get(0));
                } else if(selectedDays.size() == 7) {
                    updatedTimeFrame.daysString =
                            String.format("%s-%s",
                                    TimeFrame.ConvertIntegerDayToLocalizedDayString(
                                            getApplicationContext(), Collections.min(selectedDays)),
                                    TimeFrame.ConvertIntegerDayToLocalizedDayString(
                                            getApplicationContext(), Collections.max(selectedDays)));
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
                // STOP We don't have anything checked.
                Toast.makeText(getApplicationContext(),
                        "Please select a day before continuing.",
                        Toast.LENGTH_SHORT).show();
                updatedTimeFrame = null;
                return false;
            }
        } else {
            updatedTimeFrame = null;
            return false;
        }
    }

    View.OnClickListener openTimeListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            TimePickerBuilder openBuilder = new TimePickerBuilder()
                    .setReference(TIME_PICKER_OPEN)
                    .setFragmentManager(getSupportFragmentManager())
                    .setStyleResId(com.doomonafireball.betterpickers.R.style.BetterPickersDialogFragment);
            openBuilder.show();
        }
    };

    View.OnClickListener closeTimeListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            TimePickerBuilder closeBuilder = new TimePickerBuilder()
                    .setReference(TIME_PICKER_CLOSE)
                    .setFragmentManager(getSupportFragmentManager())
                    .setStyleResId(com.doomonafireball.betterpickers.R.style.BetterPickersDialogFragment);
            closeBuilder.show();
        }
    };

    CompoundButton.OnCheckedChangeListener twentyFourHourToggleChangeListener = new CompoundButton.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			if(isChecked) {
				mTextViewTimeOpen.setText("0000");
				mTextViewTimeClose.setText("0000");
			}
		}
	};

    @Override
    public void onDialogTimeSet(int reference, int hourOfDay, int minute) {
        String text = String.format(Locale.getDefault(),
                "%1$02d:%2$02d",
                hourOfDay,
                minute);
        switch (reference) {
            case 0:
                mTextViewTimeOpen.setText(text);
                break;
            case 1:
                mTextViewTimeClose.setText(text);
                break;
        }
    }
}