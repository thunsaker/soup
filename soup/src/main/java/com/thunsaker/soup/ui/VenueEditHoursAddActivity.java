package com.thunsaker.soup.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
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
import com.thunsaker.soup.classes.foursquare.TimeFrame;

/**
 * Created by thunsaker on 7/19/13.
 */
public class VenueEditHoursAddActivity extends ActionBarActivity
        implements TimePickerDialogHandler {
    private boolean useLogo = true;
    private boolean showHomeUp = true;

    public static final Integer TIME_PICKER_OPEN = 0;
    public static final Integer TIME_PICKER_CLOSE = 1;

    public static final int EDIT_HOURS = 0;
    public static final String UPDATED_HOURS_EXTRA = "UPDATED_HOURS_EXTRA";
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
            if(originalTimeFrame.getDaysList() != null) {
                mToggleButtonMon.setChecked(
                        originalTimeFrame.getDaysList().contains(1) ? true : false);
                mToggleButtonTue.setChecked(
                        originalTimeFrame.getDaysList().contains(2) ? true : false);
                mToggleButtonWed.setChecked(
                        originalTimeFrame.getDaysList().contains(3) ? true : false);
                mToggleButtonThu.setChecked(
                        originalTimeFrame.getDaysList().contains(4) ? true : false);
                mToggleButtonFri.setChecked(
                        originalTimeFrame.getDaysList().contains(5) ? true : false);
                mToggleButtonSat.setChecked(
                        originalTimeFrame.getDaysList().contains(6) ? true : false);
                mToggleButtonSun.setChecked(
                        originalTimeFrame.getDaysList().contains(7) ? true : false);
            }

            // Set the times
            String openTime = originalTimeFrame.getOpenTime();
            if(openTime.length() < 4)
                openTime = "0" + openTime;
            mTextViewTimeOpen.setText(
                    String.format("%s:%s",
                            openTime.substring(0,2), openTime.substring(2)));

            String closeTime = originalTimeFrame.getCloseTime();
            if(closeTime.length() < 4)
                closeTime = "0" + closeTime;
            if(closeTime.length() == 5)
                closeTime = closeTime.substring(1);
            mTextViewTimeClose.setText(
                    String.format("%s:%s",
                            closeTime.substring(0,2),closeTime.substring(2)));
            
            if(openTime == "0000" && closeTime == "+0000") {
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
        ab.setDisplayShowHomeEnabled(showHomeUp);
        ab.setDisplayUseLogoEnabled(useLogo);
        ab.setDisplayHomeAsUpEnabled(showHomeUp);
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
                updatedTimeFrame.setDaysList(selectedDays);
                
                if(mToggleButton24.isChecked()) {
                	updatedTimeFrame.setIs24Hours(true);
                	updatedTimeFrame.setOpenTimesString(getString(R.string.edit_venue_hours_24_hours));
                } else {
	                String rawTimeOpen = mTextViewTimeOpen.getText().toString();
	                String rawTimeClose = mTextViewTimeClose.getText().toString();
	                updatedTimeFrame.setOpenTime(rawTimeOpen.replace(":", ""));
	                updatedTimeFrame.setCloseTime(rawTimeClose.toString().replace(":",""));
	
	                updatedTimeFrame.setOpenTimesString(
	                        String.format("%s-%s", rawTimeOpen, rawTimeClose));
                }

                if(selectedDays.size() == 1) {
                    updatedTimeFrame.setDaysString(
                            TimeFrame.ConvertIntegerDayToLocalizedDayString(
                                    getApplicationContext(), selectedDays.get(0)));
                } else if(selectedDays.size() == 7) {
                    updatedTimeFrame.setDaysString(
                            String.format("%s-%s",
                                    TimeFrame.ConvertIntegerDayToLocalizedDayString(
                                            getApplicationContext(), Collections.min(selectedDays)),
                                    TimeFrame.ConvertIntegerDayToLocalizedDayString(
                                            getApplicationContext(), Collections.max(selectedDays))));
                } else {
                    StringBuilder daysString = new StringBuilder();
                    Integer lastDay = 0;
                    for (int i = 0; i < selectedDays.size(); i++) {
                        String dayName = TimeFrame.ConvertIntegerDayToLocalizedDayString(
                                getApplicationContext(), selectedDays.get(i));
                        lastDay = selectedDays.get(i);

                        if(daysString.length() == 0) {
                            daysString.append(dayName);
                        } else {
                            daysString.append("," + dayName);
                        }
                    }
                    updatedTimeFrame.setDaysString(daysString.toString());
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