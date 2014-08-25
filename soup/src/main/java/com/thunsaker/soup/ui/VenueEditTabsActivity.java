package com.thunsaker.soup.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.thunsaker.soup.PreferencesHelper;
import com.thunsaker.soup.R;
import com.thunsaker.soup.VenueEditPagerAdapter;
import com.thunsaker.soup.app.BaseSoupActivity;
import com.thunsaker.soup.data.api.model.CompactVenue;
import com.thunsaker.soup.data.api.model.Contact;
import com.thunsaker.soup.data.api.model.Hours;
import com.thunsaker.soup.data.api.model.Location;
import com.thunsaker.soup.data.api.model.Venue;
import com.thunsaker.soup.services.foursquare.FoursquareTasks;
import com.viewpagerindicator.TitlePageIndicator;

/*
 * Created by @thunsaker
 */
public class VenueEditTabsActivity extends BaseSoupActivity {
    VenueEditPagerAdapter mVenueEditPagerAdapter;
    ViewPager mViewPager;

    public static String EDIT_VENUE_RESULT = "EDIT_VENUE_RESULT";
    public static String DIALOG_CONFIRM_REVERT = "DIALOG_CONFIRM_REVERT";

    public static CompactVenue venueToEdit;
    public static Venue originalVenue = null;
    public static Venue modifiedVenue = null;
    public static Boolean hasModified = false;
    public static Boolean passedValidation = true;

    public static ProgressDialog myLoadingDialog;
    public static String mDebugString;

    public static int level = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_PROGRESS);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        setContentView(R.layout.fragment_venue_edit_collection);
        setupActionBar();

        mVenueEditPagerAdapter = new VenueEditPagerAdapter(
                getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setOffscreenPageLimit(3);
        mViewPager.setBackgroundColor(getResources().getColor(R.color.soup_green));
        mViewPager.setAdapter(mVenueEditPagerAdapter);
        TitlePageIndicator indicator = (TitlePageIndicator)findViewById(R.id.titles);
        indicator.setViewPager(mViewPager);
//        indicator.setTextColor(getResources().getColor(R.color.white));
//        indicator.setSelectedBold(true);
//        indicator.setFooterIndicatorStyle(TitlePageIndicator.IndicatorStyle.Underline);
//        indicator.setFooterColor(getResources().getColor(R.color.white));

        level = PreferencesHelper.getFoursquareSuperuserLevel(getApplicationContext());

        if(getIntent().hasExtra(VenueDetailFragment.VENUE_EDIT_EXTRA)) {
            venueToEdit = CompactVenue.GetCompactVenueFromJson(
                    getIntent().getExtras().get(VenueDetailFragment.VENUE_EDIT_EXTRA).toString());
        }
    }

    /**
     * Set up the {@link android.app.ActionBar}.
     */
    private void setupActionBar() {
        ActionBar ab = getSupportActionBar();
        boolean showHomeUp = true;
        ab.setDisplayHomeAsUpEnabled(showHomeUp);
        boolean useLogo = true;
        ab.setDisplayUseLogoEnabled(useLogo);
        ab.setIcon(getResources().getDrawable(R.drawable.ic_launcher_white));

        setProgressBarVisibility(false);
        setProgressBarIndeterminate(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_venue_edit, menu);

        MenuItem action_revert = menu.findItem(R.id.action_revert);

        if(hasModified) {
            if (action_revert != null) {
                action_revert.setVisible(true);
            }
        } else {
            if (action_revert != null) {
                action_revert.setVisible(false);
            }
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                ClearVenue();
                finish();
                return true;
            case R.id.action_revert:
                DialogFragment confirmFragment = new RevertChangesDialogFragment();
                confirmFragment.show(getSupportFragmentManager(), DIALOG_CONFIRM_REVERT);
                return true;
            case R.id.action_save:
                SaveForm();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void SaveForm() {
        LoadModifiedVenue();

        if(ValidateModifiedVenue()) {
            setSupportProgressBarVisibility(true);
            Boolean modifiedDescription = false;
            if(level >= 2) {
                if(originalVenue == null || originalVenue.description == null)
                    modifiedDescription = true;
                if(!modifiedDescription && !originalVenue.description.equals(modifiedVenue.description))
                    modifiedDescription = true;
            }

            setSupportProgressBarVisibility(true);
            new FoursquareTasks.EditVenue(
                    getApplicationContext(), venueToEdit.id, modifiedVenue,
                    this,modifiedDescription,false)
                    .execute();
        }
    }

    private void LoadModifiedVenue() {
        modifiedVenue = new Venue(originalVenue);
        if(VenueEditHoursFragment.currentVenueListHours != null) {
            Hours myVenueHours = new Hours();
            myVenueHours.timeFrames = VenueEditHoursFragment.currentVenueListHours;
            modifiedVenue.venueHours = myVenueHours;
        }

        if(VenueEditLocationFragment.mAddressEditText != null) {
            Location modifiedLocation = new Location();
            modifiedLocation.address = VenueEditLocationFragment.mAddressEditText.getText().toString();
            modifiedLocation.crossStreet = VenueEditLocationFragment.mCrossStreetEditText.getText().toString();
            modifiedLocation.city = VenueEditLocationFragment.mCityEditText.getText().toString();
            modifiedLocation.state = VenueEditLocationFragment.mStateEditText.getText().toString();
            modifiedLocation.postalCode = VenueEditLocationFragment.mZipEditText.getText().toString();

            try {
                // TODO: Implement my own try parse functions
                String[] myLatLngArray =
                        VenueEditLocationFragment.mLatLngEditText.getText()
                                .toString().replace(" ", "").split(",");
                Double myLat;
                if(myLatLngArray[0] != null) {
                    myLat = Double.parseDouble(myLatLngArray[0]);
                    modifiedLocation.latitude = myLat;
                }

                Double myLng;
                if(myLatLngArray[1] != null) {
                    myLng = Double.parseDouble(myLatLngArray[1]);
                    modifiedLocation.longitude = myLng;
                }
            } catch (Exception e) {
                modifiedLocation.latitude = originalVenue.location.latitude;
                modifiedLocation.longitude = originalVenue.location.longitude;
            }
            modifiedVenue.location = modifiedLocation;
        }

        if(VenueEditInfoFragment.mNameEditText != null) {
            modifiedVenue.name = VenueEditInfoFragment.mNameEditText.getText().toString();

            Contact modifiedContact = new Contact();
            modifiedContact.phone = VenueEditInfoFragment.mPhoneEditText.getText().toString();
            modifiedContact.twitter = VenueEditInfoFragment.mTwitterEditText.getText().toString();
            modifiedVenue.contact = modifiedContact;

            modifiedVenue.url = VenueEditInfoFragment.mUrlEditText.getText().toString();
            modifiedVenue.description = VenueEditInfoFragment.mDescriptionEditText.getText().toString();
        }
    }

    /**
     * Validate the edits made by the user before submitting them.
     * Presently, description has validation, the others are a free-for-all, for now.
     *
     * @return True = Validation passed, False = Validation failed for some reason
     */
    public Boolean ValidateModifiedVenue() {
        try {
            if(level >= 2) {
                if(VenueEditInfoFragment.mDescriptionEditText.getText().length() > 0) {
                    if(VenueEditInfoFragment.mDescriptionTextViewCount != null
                            && VenueEditInfoFragment.mDescriptionTextViewCount.getText() != null
                            && Integer.parseInt(
                            VenueEditInfoFragment.mDescriptionTextViewCount
                                    .getText().toString()) > 300) {
                        passedValidation = false;
                        VenueEditInfoFragment.mDescriptionEditText.setTextColor(
                                getResources().getColor(R.color.error_text));
//                        mMainLinearLayout.scrollTo(0, mMainLinearLayout.getHeight());
                    }
                } else {
                    // Removing the description
                    // TODO: Prompt the user?
                    passedValidation = true;
                }
            }

            if(passedValidation)
                return true;
            else {
//                Crouton.makeText(this, getString(R.string.validation_failed), Style.ALERT).show();
                Toast.makeText(this, getString(R.string.validation_failed), Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ClearVenue();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ClearVenue();
    }

    public static void ShowRevertOption(FragmentActivity activity) {
        hasModified = true;
        activity.supportInvalidateOptionsMenu();
    }

    public static void RevertChanges(Boolean notify, FragmentActivity activity) {
        if(notify)
            Toast.makeText(activity, activity.getString(R.string.edit_venue_reverted_changes), Toast.LENGTH_SHORT).show();

        hasModified = false;
        activity.supportInvalidateOptionsMenu();
    }

    public void ClearVenue() {
        modifiedVenue = null;
        originalVenue = null;
        venueToEdit = null;
        VenueEditHoursFragment.currentVenueListHours = null;
        VenueEditHoursFragment.updatedTimes = null;
    }

    public static class RevertChangesDialogFragment extends DialogFragment {
    	static RevertChangesDialogFragment newInstance(){
    		RevertChangesDialogFragment fragment = new RevertChangesDialogFragment();

    		Bundle args = new Bundle();
    		fragment.setArguments(args);

    		return fragment;
    	}

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.edit_venue_revert_changes_dialog)
                    .setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent editVenueIntent =
                                    new Intent(
                                    		getActivity().getApplicationContext(),
                                            VenueEditTabsActivity.class);
                            editVenueIntent.putExtra(
                                    VenueDetailFragment.VENUE_EDIT_EXTRA,originalVenue.toString());
                            editVenueIntent.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
                            VenueEditTabsActivity.originalVenue = new Venue(originalVenue);
                            startActivity(editVenueIntent);
                            getActivity().finish();
                        }
                    })
                    .setNegativeButton(R.string.dialog_no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    });
            return builder.create();
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
                                        ((VenueEditTabsActivity) getActivity()).doRemoveSegment(mItem);
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
        VenueEditHoursFragment.currentVenueListHours.remove(item);
        VenueEditHoursFragment.currentVenueHoursListAdapter.notifyDataSetChanged();
        VenueEditTabsActivity.ShowRevertOption(this);
    }
}