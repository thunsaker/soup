package com.thunsaker.soup.services.foursquare;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.thunsaker.android.common.annotations.ForApplication;
import com.thunsaker.soup.PreferencesHelper;
import com.thunsaker.soup.R;
import com.thunsaker.soup.app.SoupApp;
import com.thunsaker.soup.data.api.model.Category;
import com.thunsaker.soup.data.api.model.CompactVenue;
import com.thunsaker.soup.data.api.model.FoursquareCompactVenueResponse;
import com.thunsaker.soup.data.api.model.Venue;
import com.thunsaker.soup.data.api.model.VenueSearchResponse;
import com.thunsaker.soup.data.events.VenueListEvent;
import com.thunsaker.soup.services.AuthHelper;
import com.thunsaker.soup.services.FoursquareService;
import com.thunsaker.soup.services.foursquare.endpoints.VenueEndpoint;
import com.thunsaker.soup.ui.MainActivity;
import com.thunsaker.soup.ui.VenueAddCategoryActivity;
import com.thunsaker.soup.ui.VenueDetailActivity;
import com.thunsaker.soup.ui.VenueDetailFragment;
import com.thunsaker.soup.ui.VenueEditCategoriesActivity;
import com.thunsaker.soup.ui.VenueEditTabsActivity;
import com.thunsaker.soup.ui.VenueListFragment;
import com.thunsaker.soup.util.Util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;

public class FoursquareTasks {
    @Inject
    EventBus mBus;

    @Inject @ForApplication
    Context mContext;

    @Inject
    FoursquareService mFoursquareService;

    public FoursquareTasks(SoupApp app) {
        app.inject(this);
    }

    // DEBUG EMAIL
    public static boolean SEND_DEBUG_EMAIL = false;

    @Deprecated
    public class GetClosestVenues extends AsyncTask<Void, Integer, List<CompactVenue>> {
        Context myContext;
        LatLng myLatLng;
        String mySearchQuery;
        String mySearchQueryLocation;
        String myAccessToken;
        String myClientId;
        String myClientSecret;
        VenueListFragment myCaller;
        String myDuplicateSearchId;

        public GetClosestVenues(Context theContext, VenueListFragment theCaller, String theSearchQuery,
                                String theSearchQueryLocation, String theDuplicateSearchId) {
            myContext = theContext;
            myLatLng = MainActivity.currentLocation;
            mySearchQuery = theSearchQuery;
            mySearchQueryLocation = theSearchQueryLocation;
            myCaller = theCaller;
            myDuplicateSearchId = theDuplicateSearchId;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected List<CompactVenue> doInBackground(Void... params) {
            try {
                if (VenueListFragment.isSearching && mySearchQuery.equals("")) {
                    VenueListFragment.searchResultsVenueList = new ArrayList<CompactVenue>();
                    return null;
                }

                myAccessToken = !PreferencesHelper.getFoursquareToken(myContext).equals("")
                        ? PreferencesHelper.getFoursquareToken(myContext) : "";
//                myClientId = AuthHelper.FOURSQUARE_CLIENT_ID;
//                myClientSecret = AuthHelper.FOURSQUARE_CLIENT_SECRET;

                List<CompactVenue> nearbyVenues = new ArrayList<CompactVenue>();
//                if (mySearchQueryLocation != null
//                        && mySearchQueryLocation.length() > 0) {
//                    nearbyVenues = VenueEndpoint.GetClosestVenuesWithLatLng(
//                            myLatLng, mySearchQuery, myAccessToken, myClientId,
//                            myClientSecret, mySearchQueryLocation);
                    VenueSearchResponse response =
                            mFoursquareService.searchVenues(
                                    myAccessToken, myLatLng.toString(),
                                    mySearchQuery, "",
                                    FoursquarePrefs.DEFAULT_SEARCH_LIMIT,
                                    FoursquarePrefs.FOURSQUARE_SEARCH_INTENT_CHECKIN,
                                    FoursquarePrefs.DEFAULT_SEARCH_RADIUS);

                    for(FoursquareCompactVenueResponse compact : response.response.venues) {
                        nearbyVenues.add(FoursquareCompactVenueResponse.ConvertFoursquareCompactVenueResponseToCompactVenue(compact));
                    }
//                } else {
//                    nearbyVenues = VenueEndpoint.GetClosestVenuesWithLatLng(
//                            myLatLng, mySearchQuery, myAccessToken, myClientId,
//                            myClientSecret, "");
//                }
                return nearbyVenues != null ? nearbyVenues : null;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<CompactVenue> result) {
            super.onPostExecute(result);

            List<CompactVenue> updatedList;

            try {
                if (result != null && myCaller.isVisible()) {
                    if (!mySearchQuery.equals("")) {
                        if (VenueListFragment.isDuplicateSearching) {
                            List<CompactVenue> modifiedList = new ArrayList<CompactVenue>();
                            if (VenueListFragment.searchDuplicateResultsVenueList == null)
                                VenueListFragment.searchDuplicateResultsVenueList = new ArrayList<CompactVenue>();

                            for (CompactVenue c : result) {
                                String tempId = c.id.trim();
                                if (!tempId.equals(myDuplicateSearchId.trim()))
                                    modifiedList.add(c);
                            }

                            VenueListFragment.searchDuplicateResultsVenueList = modifiedList;

                            if (VenueListFragment.searchDuplicateResultsVenueListAdapter == null)
                                VenueListFragment.searchDuplicateResultsVenueListAdapter = myCaller.new VenueListAdapter(
                                        myContext, R.layout.list_venue_item,
                                        null);

                            if (VenueListFragment.searchDuplicateResultsVenueListAdapter.items == null)
                                VenueListFragment.searchDuplicateResultsVenueListAdapter.items = new ArrayList<CompactVenue>();

                            VenueListFragment.searchDuplicateResultsVenueListAdapter.items.addAll(modifiedList);
                            VenueListFragment.searchDuplicateResultsVenueListAdapter.notifyDataSetChanged();

                            updatedList = VenueListFragment.searchDuplicateResultsVenueList;
                        } else {
                            if (VenueListFragment.searchResultsVenueList == null)
                                VenueListFragment.searchResultsVenueList = new ArrayList<CompactVenue>();

                            VenueListFragment.searchResultsVenueList = result;

                            if (VenueListFragment.searchResultsVenueListAdapter == null)
                                VenueListFragment.searchResultsVenueListAdapter = myCaller.new VenueListAdapter(
                                        myContext, R.layout.list_venue_item,
                                        null);

                            if (VenueListFragment.searchResultsVenueListAdapter.items == null)
                                VenueListFragment.searchResultsVenueListAdapter.items = new ArrayList<CompactVenue>();

                            VenueListFragment.searchResultsVenueListAdapter.items
                                    .addAll(result);
                            VenueListFragment.searchResultsVenueListAdapter
                                    .notifyDataSetChanged();

                            updatedList = VenueListFragment.searchResultsVenueList;
                        }
                    } else {
                        if (VenueListFragment.currentVenueList == null)
                            VenueListFragment.currentVenueList = new ArrayList<CompactVenue>();

                        VenueListFragment.currentVenueList = result;

                        if (VenueListFragment.currentVenueListAdapter.items == null)
                            VenueListFragment.currentVenueListAdapter.items = new ArrayList<CompactVenue>();

                        VenueListFragment.currentVenueListAdapter.items
                                .addAll(result);
                        VenueListFragment.currentVenueListAdapter
                                .notifyDataSetChanged();

                        updatedList = VenueListFragment.currentVenueList;
                    }

                    if (updatedList != null) {
                        VenueListFragment.VenueListAdapter myAdapter = myCaller.new VenueListAdapter(
                                myContext, R.layout.list_venue_item,
                                updatedList);
                        myCaller.setListAdapter(myAdapter);
                    }
                } else if (VenueListFragment.isSearching && mySearchQuery.equals("")) {
                    VenueListFragment.searchResultsVenueListAdapter = myCaller.new VenueListAdapter(
                            myContext, R.layout.list_venue_item,
                            VenueListFragment.searchResultsVenueList);
                    myCaller.setListAdapter(VenueListFragment.searchResultsVenueListAdapter);

                    Toast.makeText(myContext, myContext.getString(R.string.alert_error_loading_venues), Toast.LENGTH_SHORT).show();
                }

                VenueListFragment.isRefreshing = false;
            } catch (Exception e) {
                e.printStackTrace();
                if (myCaller != null && myCaller.isVisible()) {
                    myCaller.getActivity().setProgressBarVisibility(false);
//                    VenueListFragment.mPullToRefreshLayout.setRefreshComplete();
                }
                VenueListFragment.isRefreshing = false;
            } finally {
                if (myCaller != null && myCaller.isVisible()) {
                    myCaller.getActivity().setProgressBarVisibility(false);
//                    VenueListFragment.mPullToRefreshLayout.setRefreshComplete();
                }
                VenueListFragment.isRefreshing = false;
            }
        }
    }

    public class GetClosestVenuesNew extends AsyncTask<Void, Integer, List<CompactVenue>> {
        LatLng myLatLng;
        String mySearchQuery;
        String mySearchQueryLocation;
        String myDuplicateSearchId;

        public GetClosestVenuesNew(String theSearchQuery, String theSearchQueryLocation, String theDuplicateSearchId) {
            myLatLng = MainActivity.currentLocation;
            mySearchQuery = theSearchQuery;
            mySearchQueryLocation = theSearchQueryLocation;
            myDuplicateSearchId = theDuplicateSearchId;
        }

        @Override
        protected List<CompactVenue> doInBackground(Void... params) {
            try {
                if (VenueListFragment.isSearching && mySearchQuery.equals("")) {
                    VenueListFragment.searchResultsVenueList = new ArrayList<CompactVenue>();
                    return null;
                }

                List<CompactVenue> nearbyVenues = new ArrayList<CompactVenue>();
//                VenueEndpoint venueEndpoint = new VenueEndpoint((SoupApp) mContext);
//                if (mySearchQueryLocation != null
//                        && mySearchQueryLocation.length() > 0) {
//                    nearbyVenues = venueEndpoint.GetClosestVenuesWithLatLng(myLatLng, mySearchQuery, mySearchQueryLocation);
//                } else {
//                    nearbyVenues = venueEndpoint.GetClosestVenuesWithLatLng(myLatLng, mySearchQuery, "");
//                }

                String mAccessToken = PreferencesHelper.getFoursquareToken(mContext);

                VenueSearchResponse response =
                        mFoursquareService.searchVenues(
                                mAccessToken, String.format("%s,%s", myLatLng.latitude, myLatLng.longitude),
                                mySearchQuery, "",
                                FoursquarePrefs.DEFAULT_SEARCH_LIMIT,
                                FoursquarePrefs.FOURSQUARE_SEARCH_INTENT_CHECKIN,
                                FoursquarePrefs.DEFAULT_SEARCH_RADIUS);
                if(response != null) {
                    if(response.meta.code == 200 && response.response.venues != null) {
                        for (FoursquareCompactVenueResponse compact : response.response.venues) {
                            nearbyVenues.add(FoursquareCompactVenueResponse.ConvertFoursquareCompactVenueResponseToCompactVenue(compact));
                        }
                    } else {
                        Log.i("FoursquareTasks", "Failure! Code: " + response.meta.code + " Error: " + response.meta.errorType + " - " + response.meta.errorMessage);
                        return null;
                    }
                }

                return nearbyVenues != null ? nearbyVenues : null;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<CompactVenue> result) {
            super.onPostExecute(result);

            List<CompactVenue> updatedList;

            if(result != null) {
                mBus.post(new VenueListEvent(true, "", result, mySearchQuery, mySearchQueryLocation, myDuplicateSearchId));
            } else {
                mBus.post(new VenueListEvent(false, "", null, mySearchQuery, mySearchQueryLocation, myDuplicateSearchId));
            }
        }
    }

    public static class EditVenue extends AsyncTask<Void, Integer, String> {
        Context myContext;
        String myVenueId;
        Venue myModifiedVenue;
        FragmentActivity myCaller;

        String myAccessToken;
        String myClientId;
        String myClientSecret;
        boolean canEdit;
        boolean modifiedDescription;
        boolean fromAddCategory;

        public EditVenue(Context theContext, String theVenueToModifyId,
                         Venue theModifiedVenue, FragmentActivity theCaller,
                         Boolean modifiedDescription, Boolean fromAddCategory) {
            myContext = theContext;
            myVenueId = theVenueToModifyId;
            myModifiedVenue = theModifiedVenue;
            myCaller = theCaller;
            this.modifiedDescription = modifiedDescription;
            this.fromAddCategory = fromAddCategory;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                myAccessToken = !PreferencesHelper.getFoursquareToken(myContext).equals("") ? PreferencesHelper
                        .getFoursquareToken(myContext) : "";
                myClientId = AuthHelper.FOURSQUARE_CLIENT_ID;
                myClientSecret = AuthHelper.FOURSQUARE_CLIENT_SECRET;

                int mySuperuserLevel = PreferencesHelper
                        .getFoursquareSuperuserLevel(myContext);

                canEdit = mySuperuserLevel > 0;

                String venueResult = VenueEndpoint.EditVenue(myVenueId,
                        myAccessToken, myClientId, myClientSecret,
                        myModifiedVenue, mySuperuserLevel, modifiedDescription,
                        fromAddCategory);
                return venueResult != null ? venueResult : null;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            try {
                myCaller.setProgressBarVisibility(false);
                VenueDetailActivity.wasEdited = true;

                // DEBUG EMAIL
                if (SEND_DEBUG_EMAIL && result.startsWith("DEBUG: ")) {
                    Toast.makeText(
                            myCaller.getApplicationContext(),
                            "The editing failed. "
                                    + "Trying to send an email to the developer to help debug.",
                            Toast.LENGTH_SHORT).show();
                    VenueEditTabsActivity.mDebugString = result;
                } else {
                    if (result.equals(FoursquarePrefs.SUCCESS)) {
                        Toast.makeText(
                                myCaller.getApplicationContext(),
                                canEdit ? myContext
                                        .getString(R.string.edit_venue_success)
                                        : myContext
                                        .getString(R.string.edit_venue_success_propose),
                                Toast.LENGTH_SHORT).show();
                        myCaller.setResult(Activity.RESULT_OK);
                    } else if (result
                            .equals(FoursquarePrefs.FAIL_UNAUTHORIZED)) {
                        Toast.makeText(
                                myCaller.getApplicationContext(),
                                myContext
                                        .getString(modifiedDescription ? R.string.edit_venue_fail_unauthorized_description
                                                : R.string.edit_venue_fail_unauthorized),
                                Toast.LENGTH_SHORT).show();
                        myCaller.setResult(Activity.RESULT_CANCELED);
                    } else {
                        Toast.makeText(myCaller.getApplicationContext(),
                                myContext.getString(R.string.edit_venue_fail),
                                Toast.LENGTH_SHORT).show();
                        myCaller.setResult(Activity.RESULT_CANCELED);
                    }
                }

                myCaller.finish();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            if (myCaller != null)
                myCaller.setProgressBarVisibility(true);
        }
    }

    public static class GetVenue extends AsyncTask<Void, Integer, Venue> {
        Context myContext;
        String myVenueId;
        FragmentActivity myCaller;
        VenueDetailActivity myVenueDetailCaller;
        Integer mySource;

        String myAccessToken;
        String myClientId;
        String myClientSecret;

        public GetVenue(Context theContext, String theVenueId,
                        FragmentActivity theCaller, Integer theSource) {
            myContext = theContext;
            myVenueId = theVenueId;
            mySource = theSource;
            myCaller = theCaller;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Venue doInBackground(Void... params) {
            try {
                myAccessToken = !PreferencesHelper.getFoursquareToken(myContext).equals("") ? PreferencesHelper
                        .getFoursquareToken(myContext) : "";
                myClientId = AuthHelper.FOURSQUARE_CLIENT_ID;
                myClientSecret = AuthHelper.FOURSQUARE_CLIENT_SECRET;

                if (mySource.equals(FoursquarePrefs.CALLER_SOURCE_DETAILS_INTENT)
                        && myVenueId.contains("http")) {
                    String myLongUrl = Util.ResolveShortUrl(myVenueId);
                    if (myLongUrl.length() > 0) {
                        if (!myLongUrl.equals("") && myLongUrl.length() > 24) {
                            String venueId;
                            if (myLongUrl.contains("/v/"))
                                venueId = myLongUrl.substring(
                                        myLongUrl.length() - 24,
                                        myLongUrl.length());
                            else if (myLongUrl.contains("/venue/"))
                                venueId = myLongUrl.substring(
                                        myLongUrl.indexOf("/venue/") + 7,
                                        myLongUrl.length());
                            else
                                return null;
                            // We don't have a foursquare URL and can't do
                            // anything...

                            myVenueId = venueId;
                        }
                    }
                }

                Venue venueResult = VenueEndpoint.GetVenue(myVenueId,
                        myAccessToken, myClientId, myClientSecret);
                return venueResult != null ? venueResult : null;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Venue result) {
            super.onPostExecute(result);

            try {
                if (result != null) {
                    VenueEditTabsActivity.originalVenue = result;
                    VenueDetailFragment.currentVenue = result;
                    if (mySource == 3)
                        mySource = 2;
                    switch (mySource) {
                        case 0: // CALLER_SOURCE_EDIT_VENUE
                            LinearLayout myLinearLayout = (LinearLayout) myCaller
                                    .findViewById(R.id.linearLayoutProgressBarLoadingVenueInfoDescriptionWrapper);
                            EditText myEditText = (EditText) myCaller
                                    .findViewById(R.id.editTextEditVenueInfoDescription);
                            myEditText.setVisibility(View.VISIBLE);
                            myEditText.setText(result.description);
                            myLinearLayout.setVisibility(View.GONE);
                            myCaller.setProgressBarVisibility(false);
                            myCaller.supportInvalidateOptionsMenu();
                            break;
                        case 1: // CALLER_SOURCE_EDIT_CATEGORIES
                            VenueEditCategoriesActivity.currentVenue = result;
                            VenueEditCategoriesActivity myCategoryCaller = (VenueEditCategoriesActivity) myCaller;
                            VenueEditCategoriesActivity.originalCategories = new ArrayList<Category>();
                            VenueEditCategoriesActivity.originalCategories
                                    .addAll(result.categories);
                            myCategoryCaller.LoadCurrentCategories(null);
                            myCategoryCaller.setProgressBarVisibility(false);
                            break;
                        case 2: // CALLER_SOURCE_DETAILS
                            myVenueDetailCaller = (VenueDetailActivity) myCaller;
                            myVenueDetailCaller.setProgressBarVisibility(false);

                            if (VenueDetailFragment.currentCompactVenue == null) {
                                VenueDetailFragment
                                        .LoadDetails(
                                                myVenueDetailCaller
                                                        .findViewById(R.id.relativeLayoutVenueWrapperOuter),
                                                myVenueDetailCaller
                                        );
                            }

                            if (myVenueDetailCaller.findViewById(
                                    R.id.relativeLayoutVenueWrapperOuter).isShown()) {
                                if (result.description != null
                                        && result.description.length() > 0) {
                                    LinearLayout myDescriptionLinearLayout = (LinearLayout) myVenueDetailCaller
                                            .findViewById(R.id.linearLayoutVenueDescriptionBackground);

                                    myDescriptionLinearLayout
                                            .setVisibility(View.VISIBLE);
                                    TextView myDescriptionTextView = (TextView) myVenueDetailCaller
                                            .findViewById(R.id.textViewVenueDescription);
                                    myDescriptionTextView.setText(result
                                            .description);
                                    myDescriptionTextView
                                            .setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    TextView text = (TextView) v;
                                                    text.setMaxLines(Integer.MAX_VALUE);
                                                    text.setBackgroundDrawable(null);
                                                    text.setOnClickListener(null);
                                                }
                                            });
                                }
                                myVenueDetailCaller.findViewById(
                                        R.id.progressBarVenueDescription)
                                        .setVisibility(View.GONE);
                            }

                            if (VenueDetailActivity.wasEdited)
                                VenueDetailFragment.LoadDetails(myVenueDetailCaller
                                                .findViewById(R.id.venue_detail_container),
                                        myVenueDetailCaller
                                );
                            break;
                    }

                } else {
                    Toast.makeText(myContext,
                            R.string.alert_error_loading_details,
                            Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static class GetCategories extends AsyncTask<Void, Integer, List<Category>> {
        Context myContext;
        VenueAddCategoryActivity myCaller;

        String myAccessToken;
        String myClientId;
        String myClientSecret;

        public GetCategories(Context theContext,
                             VenueAddCategoryActivity theCaller) {
            myContext = theContext;
            myCaller = theCaller;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected List<Category> doInBackground(Void... params) {
            try {
                myAccessToken = PreferencesHelper.getFoursquareToken(myContext) != "" ? PreferencesHelper
                        .getFoursquareToken(myContext) : "";
                myClientId = AuthHelper.FOURSQUARE_CLIENT_ID;
                myClientSecret = AuthHelper.FOURSQUARE_CLIENT_SECRET;

                List<Category> foursquareCategories = VenueEndpoint
                        .GetCategories(myAccessToken, myClientId,
                                myClientSecret);
                return foursquareCategories != null ? foursquareCategories
                        : null;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @SuppressWarnings("all") // There is a bug with inspection of calendar resources something: https://code.google.com/p/android/issues/detail?id=68894
        @Override
        protected void onPostExecute(List<Category> result) {
            super.onPostExecute(result);
            try {
                VenueAddCategoryActivity.FoursquareCategoriesMaster = result;
                Calendar cal = Calendar.getInstance();
                VenueAddCategoryActivity.FoursquareCategoriesMasterDate =
                        cal.get(Calendar.SECOND);

                if (VenueAddCategoryActivity.myPrimaryCategoryListAdapter.items == null)
                    VenueAddCategoryActivity.myPrimaryCategoryListAdapter.items = new ArrayList<Category>();

                VenueAddCategoryActivity.myPrimaryCategoryListAdapter.items
                        .addAll(result);
                VenueAddCategoryActivity.myPrimaryCategoryListAdapter
                        .notifyDataSetChanged();

                VenueAddCategoryActivity.myPrimaryCategoryListAdapter = myCaller.new CategoryListAdapter(
                        myContext, R.layout.list_category_item, result, 1);
                VenueAddCategoryActivity.mPrimaryListView
                        .setAdapter(VenueAddCategoryActivity.myPrimaryCategoryListAdapter);
                VenueAddCategoryActivity.myPrimaryCategoryListAdapter
                        .notifyDataSetChanged();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static class FlagVenue extends AsyncTask<Void, Integer, String> {
        Context myContext;
        Integer myFlagType;
        String myVenueId;
        String myDuplicateId = "";
        FragmentActivity myCaller;

        String myAccessToken;
        String myClientId;
        String myClientSecret;

        public FlagVenue(Context theContext, String theVenueToModifyId,
                         Integer theFlagType, String theDuplicateId,
                         FragmentActivity FragmentActivity) {
            myContext = theContext;
            myVenueId = theVenueToModifyId;
            myFlagType = theFlagType;
            if (myFlagType.equals(FoursquarePrefs.FlagType.DUPLICATE)) {
                myDuplicateId = theDuplicateId;
            }
            myCaller = FragmentActivity;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                myAccessToken = PreferencesHelper.getFoursquareToken(myContext) != "" ? PreferencesHelper
                        .getFoursquareToken(myContext) : "";
                myClientId = AuthHelper.FOURSQUARE_CLIENT_ID;
                myClientSecret = AuthHelper.FOURSQUARE_CLIENT_SECRET;

                String venueResult = VenueEndpoint.FlagVenue(myVenueId,
                        myAccessToken, myClientId, myClientSecret, myFlagType,
                        myDuplicateId);
                return venueResult != null ? venueResult : null;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            try {
                myCaller.setProgressBarVisibility(false);
                myCaller.supportInvalidateOptionsMenu();
                // myCaller.setProgressBarVisibility(false);
                if (result.equals(FoursquarePrefs.SUCCESS)) {
                    // Crouton.makeText(myCaller,
                    // String.format(myContext.getString(R.string.flag_venue_success),
                    // Util.GetFlagTypeStringFromInt(myFlagType, true)),
                    // Style.CONFIRM).show();

                    Toast.makeText(
                            myCaller,
                            String.format(myContext
                                            .getString(R.string.flag_venue_success),
                                    Util.GetFlagTypeStringFromInt(myFlagType,
                                            true)), Toast.LENGTH_SHORT).show();
                    // } else if(result == FAIL_UNAUTHORIZED) {
                    // Toast.makeText(myContext,
                    // myContext.getString(R.string.edit_venue_fail_unauthorized),
                    // Toast.LENGTH_LONG).show();
                } else {
                    // Crouton.makeText(myCaller,
                    // myContext.getString(R.string.flag_venue_fail),
                    // Style.ALERT).show();

                    Toast.makeText(myCaller,
                            myContext.getString(R.string.flag_venue_fail),
                            Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            if (myCaller != null) {
                myCaller.setProgressBarVisibility(true);
                myCaller.supportInvalidateOptionsMenu();
            }
        }
    }
}