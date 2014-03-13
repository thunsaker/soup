package com.thunsaker.soup.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.thunsaker.soup.R;
import com.thunsaker.soup.FoursquareHelper;
import com.thunsaker.soup.util.foursquare.VenueEndpoint;

/**
 * Created by thunsaker on 7/19/13.
 */
public class VenueEditLocationFragment extends Fragment {
    public static final String ARG_OBJECT = "object";

    public static LinearLayout mMainLocationLinearLayout;
    public static EditText mAddressEditText;
    public static EditText mCrossStreetEditText;
    public static EditText mCityEditText;
    public static EditText mStateEditText;
    public static EditText mZipEditText;
    public static EditText mLatLngEditText;
    public static ImageButton mLatLngImageButton;
    public static LinearLayout mLinearLayoutLocationSuperuserSection;
    public static LinearLayout mLinearLayoutEditVenueLocationMapContainer;
    public static ScrollView mScrollViewEditVenueLocationWrapper;

    LatLng selectedLatLng = null;
    boolean showMyLocation = false;

    private GoogleMap mMap;
    public int MAP_PAN_DELAY = 2000; // in miliseconds
    public int MAP_BOUNDS_PADDING = 150;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(
                R.layout.fragment_venue_edit_location, container, false);
//        Bundle args = getArguments();

        mMainLocationLinearLayout =
                (LinearLayout) rootView.findViewById(R.id.linearLayoutEditVenueLocationWrapper);
        mLinearLayoutEditVenueLocationMapContainer =
                (LinearLayout)
                        rootView.findViewById(R.id.linearLayoutEditVenueLocationMapContainer);
        mAddressEditText = (EditText) rootView.findViewById(R.id.editTextEditVenueLocationAddress);
        mCrossStreetEditText =
                (EditText) rootView.findViewById(R.id.editTextEditVenueLocationCrossStreet);
        mCityEditText = (EditText) rootView.findViewById(R.id.editTextEditVenueLocationCity);
        mStateEditText = (EditText) rootView.findViewById(R.id.editTextEditVenueLocationState);
        mZipEditText = (EditText) rootView.findViewById(R.id.editTextEditVenueLocationZip);
        mLatLngEditText = (EditText) rootView.findViewById(R.id.editTextEditVenueLocationLatLng);
        mLatLngImageButton =
                (ImageButton) rootView.findViewById(R.id.imageButtonEditVenueLocationPickLocation);
        mScrollViewEditVenueLocationWrapper =
                (ScrollView) rootView.findViewById(
                        R.id.scrollViewEditVenueLocationWrapper);
        mLinearLayoutLocationSuperuserSection =
                (LinearLayout) rootView.findViewById(
                        R.id.linearLayoutEditVenueLocationSuperuserSection);

        if(VenueEditTabsActivity.venueToEdit != null)
            LoadForm();

        return rootView;
    }

    private void LoadForm() {
        try {
            VenueEditTabsActivity.RevertChanges(false, this.getActivity());

            String address = "";
            String crossStreet = "";
            String city = "";
            String state = "";
            String zip = "";
            String latLng = "";

            if(VenueEditTabsActivity.originalVenue != null) {
                address = VenueEditTabsActivity.originalVenue.getLocation().getAddress();
                crossStreet = VenueEditTabsActivity.originalVenue.getLocation().getCrossStreet();
                city = VenueEditTabsActivity.originalVenue.getLocation().getCity();
                state = VenueEditTabsActivity.originalVenue.getLocation().getState();
                zip = VenueEditTabsActivity.originalVenue.getLocation().getPostalCode();
                latLng = VenueEditTabsActivity.originalVenue.getLocation().getLatLngString();
            } else {
                getActivity().setProgressBarVisibility(true);
                new VenueEndpoint.GetVenue(getActivity().getApplicationContext(),
                        VenueEditTabsActivity.venueToEdit.getId(), this.getActivity(),
                        FoursquareHelper.CALLER_SOURCE_EDIT_VENUE).execute();

                address = VenueEditTabsActivity.venueToEdit.getLocation().getAddress();
                crossStreet = VenueEditTabsActivity.venueToEdit.getLocation().getCrossStreet();
                city = VenueEditTabsActivity.venueToEdit.getLocation().getCity();
                state = VenueEditTabsActivity.venueToEdit.getLocation().getState();
                zip = VenueEditTabsActivity.venueToEdit.getLocation().getPostalCode();
                latLng = VenueEditTabsActivity.venueToEdit.getLocation().getLatLngString();
            }

            mAddressEditText.setText(address);
            mAddressEditText.addTextChangedListener(mEditTextWatcher);

            mCrossStreetEditText.setText(crossStreet);
            mCrossStreetEditText.addTextChangedListener(mEditTextWatcher);

            mCityEditText.setText(city);
            mCityEditText.addTextChangedListener(mEditTextWatcher);

            mStateEditText.setText(state);
            mStateEditText.addTextChangedListener(mEditTextWatcher);

            mZipEditText.setText(zip);
            mZipEditText.addTextChangedListener(mEditTextWatcher);

            mLatLngEditText.setText(latLng);
            mLatLngEditText.addTextChangedListener(mEditTextWatcher);

            mLatLngImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent pickLocationIntent =
                            new Intent(
                                    getActivity().getApplicationContext(),
                                    LocationSelectActivity.class);
                    double myLat = VenueEditTabsActivity.originalVenue.getLocation().getLatitude();
                    double myLng = VenueEditTabsActivity.originalVenue.getLocation().getLongitude();
                    pickLocationIntent.putExtra(
                            LocationSelectActivity.ORIGINAL_LOCATION_EXTRA,
                            new double[]{myLat,myLng});
                    startActivityForResult(pickLocationIntent,
                            LocationSelectActivity.PICK_LOCATION);
                }
            });

            if(VenueEditTabsActivity.level <= 1) {
                mLinearLayoutLocationSuperuserSection.setVisibility(View.GONE);

                mLatLngEditText.setEnabled(false);
                mLatLngImageButton.setEnabled(false);
            } else {
                mLinearLayoutLocationSuperuserSection.setVisibility(View.VISIBLE);
            }

            SetupMap(VenueEditTabsActivity.venueToEdit.getLocation().getLatLng());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void SetupMap(LatLng currentLocation) {
        if (mMap == null) {
            mMap = ((SupportMapFragment) getActivity().getSupportFragmentManager()
                            .findFragmentById(R.id.map_fragment_venue_edit_location)).getMap();
            if (mMap != null) {
                if(currentLocation == null) {
//                    currentLocation = VenueListActivity.currentLocation;
//                else {
                    // TODO: Make this work better...zoom way out, use some other method of figuring out who they are and where they are...
                    currentLocation = new LatLng(33.44866, -112.06627);
                }

                // Handle Interaction with map
                if(mMap != null) {
                    mMap.addMarker(new MarkerOptions()
                            .position(currentLocation)
                            .icon(BitmapDescriptorFactory.fromResource(
                                    R.drawable.map_marker_orange_outline)));
                    showMyLocation = true;
                    LatLng myLatLngOffset =
                            new LatLng(currentLocation.latitude + 0.0015, currentLocation.longitude);
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(myLatLngOffset));
//                    mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
                }

//                mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
//                    @Override
//                    public void onMapClick(LatLng newPoint) {
//                        if(expanded == true) {
//                            mLinearLayoutEditVenueLocationMapContainer
//                        }
//                        if(mScrollViewEditVenueLocationWrapper.getVisibility()
//                                == View.VISIBLE){
//                            mLinearLayoutEditVenueLocationMapContainer
//                                    .setLayoutParams(
//                                            new LinearLayout.LayoutParams(
//                                                    ViewGroup.LayoutParams.MATCH_PARENT,
//                                                    ViewGroup.LayoutParams.MATCH_PARENT));
//                            mScrollViewEditVenueLocationWrapper.setVisibility(View.GONE);
//                        } else {
//                            mMap.clear();
//                            // TODO: Implement Restore marker
////                            VenueEditTabsActivity.RestoreOriginalMarker(false);
//                            mMap.addMarker(new MarkerOptions()
//                                    .position(newPoint)
//                                    .icon(BitmapDescriptorFactory.fromResource(
//                                            R.drawable.map_marker_orange_gray_outline)));
//                            selectedLatLng = new LatLng(newPoint.latitude, newPoint.longitude);
//                            getSherlockActivity().supportInvalidateOptionsMenu();
//                            mMap.animateCamera(
//                                    CameraUpdateFactory.newLatLng(newPoint),
//                                    MAP_PAN_DELAY,
//                                    null);
//                        }
//                    }
//                });
            }
        }
    }

    /**
     * Displays revert/undo option once an edit text has been modified.
     * This was added to each EditText except descriptionEditText.
     *
     */
    private final TextWatcher mEditTextWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            VenueEditTabsActivity.ShowRevertOption(getActivity());
        }

        public void afterTextChanged(Editable s) {
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == LocationSelectActivity.PICK_LOCATION) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    String myPickedLocation =
                            data.hasExtra(LocationSelectActivity.PICKED_LOCATION_EXTRA)
                                    ? data.getStringExtra(
                                    LocationSelectActivity.PICKED_LOCATION_EXTRA).trim()
                                    : "";
                    String[] latLngSplit = myPickedLocation.split(",");
                    LatLng myLatLng =
                            new LatLng(Double.parseDouble(latLngSplit[0]),
                                    Double.parseDouble(latLngSplit[1]));
                    mLatLngEditText.setText(myPickedLocation);

                    LatLng originalLatLng =
                            VenueEditTabsActivity.originalVenue.getLocation().getLatLng();
                    if(mMap != null && myLatLng != null) {
                        mMap.clear();
                        mMap.addMarker(new MarkerOptions()
                                .position(originalLatLng)
                                .icon(BitmapDescriptorFactory.fromResource(
                                        R.drawable.map_marker_gray)));
                        mMap.addMarker(new MarkerOptions()
                                .position(myLatLng)
                                .icon(BitmapDescriptorFactory.fromResource(
                                        R.drawable.map_marker_orange_outline)));
                        showMyLocation = true;
                        // TODO: Implement bounds...
                        LatLng myLatLngOffset =
                                new LatLng(myLatLng.latitude + 0.0015, myLatLng.longitude);
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(myLatLngOffset));
                    }
                    break;

                default:
                    break;
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}
