package com.thunsaker.soup.ui;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.thunsaker.android.common.annotations.ForApplication;
import com.thunsaker.soup.R;
import com.thunsaker.soup.services.foursquare.FoursquarePrefs;
import com.thunsaker.soup.services.foursquare.FoursquareTasks;

import javax.inject.Inject;

public class VenueEditInfoFragment extends Fragment {
    @Inject
    @ForApplication
    Context mContext;

    @Inject
    FoursquareTasks mFoursquareTasks;

    public static final String ARG_OBJECT = "object";

    private LinearLayout mMainInfoLinearLayout;
    public static EditText mNameEditText;
    public static EditText mPhoneEditText;
    public static EditText mTwitterEditText;
    public static EditText mUrlEditText;
    public static EditText mDescriptionEditText;
    public static TextView mDescriptionTextViewCount;
    public static LinearLayout mDescriptionProgressBar;
    public static LinearLayout mLinearLayoutInfoSuperuserSection;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(
                R.layout.fragment_venue_edit_info, container, false);
//        Bundle args = getArguments();

        mMainInfoLinearLayout =
                (LinearLayout) rootView.findViewById(R.id.linearLayoutEditVenueInfoWrapper);
        mNameEditText = (EditText) rootView.findViewById(R.id.editTextEditVenueInfoName);
        mPhoneEditText = (EditText) rootView.findViewById(R.id.editTextEditVenueInfoPhone);
        mTwitterEditText = (EditText) rootView.findViewById(R.id.editTextEditVenueInfoTwitter);
        mUrlEditText = (EditText) rootView.findViewById(R.id.editTextEditVenueInfoUrl);
        mDescriptionEditText = (EditText) rootView.findViewById(R.id.editTextEditVenueInfoDescription);
        mDescriptionTextViewCount =
                (TextView) rootView.findViewById(R.id.textViewEditVenueInfoDescriptionCount);
        mDescriptionProgressBar =
                (LinearLayout) rootView.findViewById(
                        R.id.linearLayoutProgressBarLoadingVenueInfoDescriptionWrapper);
        mLinearLayoutInfoSuperuserSection =
                  (LinearLayout) rootView.findViewById(
                          R.id.linearLayoutEditVenueInfoSuperuserSection);

        if(VenueEditTabsActivity.originalVenue == null) {
            VenueEditTabsActivity.originalVenue = VenueDetailActivity.currentVenue;
        }

        LoadForm();
        return rootView;
    }

    private void LoadForm() {
        try {
            VenueEditTabsActivity.RevertChanges(false, getActivity());

            String name = "";
            String phone = "";
            String twitter = "";
            String url = "";
            String description = "";

            if(VenueEditTabsActivity.originalVenue != null) {
                name = VenueEditTabsActivity.originalVenue.name;
                phone = VenueEditTabsActivity.originalVenue.contact.phone != null
                        ? VenueEditTabsActivity.originalVenue.contact.phone
                        : "";
                twitter = VenueEditTabsActivity.originalVenue.contact.twitter;
                url = VenueEditTabsActivity.originalVenue.url;
                description = VenueEditTabsActivity.originalVenue.description;
                mDescriptionEditText.setVisibility(View.VISIBLE);
                mDescriptionProgressBar.setVisibility(View.GONE);
            } else {
                mDescriptionEditText.setVisibility(View.GONE);
                mDescriptionProgressBar.setVisibility(View.VISIBLE);

                getActivity().setProgressBarVisibility(true);
                mFoursquareTasks.new GetVenue(VenueEditTabsActivity.originalVenue.id, FoursquarePrefs.CALLER_SOURCE_EDIT_VENUE).execute();

                name = VenueEditTabsActivity.originalVenue.name;
                phone = VenueEditTabsActivity.originalVenue.contact.phone != null
                        ? VenueEditTabsActivity.originalVenue.contact.phone
                        : "";
                twitter = VenueEditTabsActivity.originalVenue.contact.twitter;
                url = VenueEditTabsActivity.originalVenue.url;
            }

            mNameEditText.setText(name);
            mNameEditText.addTextChangedListener(mEditTextWatcher);

            mPhoneEditText.setText(phone);
            mPhoneEditText.addTextChangedListener(mEditTextWatcher);

            mTwitterEditText.setText(twitter);
            mTwitterEditText.addTextChangedListener(mEditTextWatcher);

            mUrlEditText.setText(url);
            mUrlEditText.addTextChangedListener(mEditTextWatcher);

            mDescriptionEditText.setText(description);
            mDescriptionEditText.addTextChangedListener(mDescriptionEditTextWatcher);

            if(VenueEditTabsActivity.level <= 1) {
                mLinearLayoutInfoSuperuserSection.setVisibility(View.GONE);

                mUrlEditText.setEnabled(false);
                mDescriptionEditText.setEnabled(false);
            } else {
                mLinearLayoutInfoSuperuserSection.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Displays revert/undo option once an edit text has been modified.
     * This was added to each EditText except descriptionEditText.
     *
     */
    private final TextWatcher mEditTextWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            VenueEditTabsActivity.ShowRevertOption(getActivity());
        }

        public void afterTextChanged(Editable s) { }
    };


    /**
     * Show character count for description field.
     *
     */
    private final TextWatcher mDescriptionEditTextWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            try {
                mDescriptionTextViewCount.setVisibility(View.VISIBLE);

                if(mDescriptionProgressBar.getVisibility() == View.VISIBLE)
                    mDescriptionProgressBar.setVisibility(View.GONE);
            } catch (Exception e) {
                e.printStackTrace();
            }

            VenueEditTabsActivity.ShowRevertOption(getActivity());
            updateCharacterCount();
        }

        public void afterTextChanged(Editable s) { }
    };

    private void updateCharacterCount() {

        try {
            String text = mDescriptionEditText.getText().toString();
            mDescriptionTextViewCount.setText(R.string.count_zero);

            int myLimit = 300;
            int mylength = text.length();

            if (mylength > myLimit) {
                mDescriptionTextViewCount.setTextColor(
                        getResources().getColor(R.color.error_text));
            } else {
                mDescriptionTextViewCount.setTextColor(
                        getResources().getColor(R.color.body_text));
            }
            mDescriptionTextViewCount.setText(String.valueOf(mylength - myLimit));
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
    }
}