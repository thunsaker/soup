package com.thunsaker.soup.services.foursquare;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.thunsaker.android.common.annotations.ForApplication;
import com.thunsaker.soup.PreferencesHelper;
import com.thunsaker.soup.R;
import com.thunsaker.soup.app.SoupApp;
import com.thunsaker.soup.data.api.model.Category;
import com.thunsaker.soup.data.api.model.Checkin;
import com.thunsaker.soup.data.api.model.CompactVenue;
import com.thunsaker.soup.data.api.model.FoursquareCompactVenueResponse;
import com.thunsaker.soup.data.api.model.FoursquareResponse;
import com.thunsaker.soup.data.api.model.FoursquareVenueResponse;
import com.thunsaker.soup.data.api.model.GetUserCheckinHistoryResponse;
import com.thunsaker.soup.data.api.model.GetVenueResponse;
import com.thunsaker.soup.data.api.model.Venue;
import com.thunsaker.soup.data.api.model.VenueSearchResponse;
import com.thunsaker.soup.data.events.CheckinHistoryEvent;
import com.thunsaker.soup.data.events.GetVenueEvent;
import com.thunsaker.soup.data.events.VenueListEvent;
import com.thunsaker.soup.services.AuthHelper;
import com.thunsaker.soup.services.FoursquareService;
import com.thunsaker.soup.services.foursquare.endpoints.VenueEndpoint;
import com.thunsaker.soup.ui.MainActivity;
import com.thunsaker.soup.ui.VenueAddCategoryActivity;
import com.thunsaker.soup.ui.VenueDetailActivity;
import com.thunsaker.soup.ui.VenueEditTabsActivity;
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

    public class GetClosestVenuesNew extends AsyncTask<Void, Integer, List<CompactVenue>> {
        LatLng myLatLng;
        String mySearchQuery;
        String mySearchQueryLocation;
        String myDuplicateSearchId;
        int myListType;

        public GetClosestVenuesNew(String theSearchQuery, String theSearchQueryLocation, String theDuplicateSearchId, int theListType) {
            myLatLng = MainActivity.currentLocation;
            mySearchQuery = theSearchQuery;
            mySearchQueryLocation = theSearchQueryLocation;
            myDuplicateSearchId = theDuplicateSearchId;
            myListType = theListType;
        }

        @Override
        protected List<CompactVenue> doInBackground(Void... params) {
            try {
                // TODO: Why are we returning null here? is it to prevent a list from being displayed?
//                if (VenueListFragment.isSearching && mySearchQuery.equals("")) {
//                    VenueListFragment.searchResultsVenueList = new ArrayList<CompactVenue>();
//                    return null;
//                }

                List<CompactVenue> nearbyVenues = new ArrayList<CompactVenue>();

                String mAccessToken = PreferencesHelper.getFoursquareToken(mContext);

                VenueSearchResponse response;
                if(mySearchQueryLocation != null && mySearchQueryLocation.length() > 0) {
                    response =
                            mFoursquareService.searchVenues(
                                    mAccessToken,
                                    mySearchQuery, mySearchQueryLocation,
                                    FoursquarePrefs.DEFAULT_SEARCH_LIMIT,
                                    FoursquarePrefs.FOURSQUARE_SEARCH_INTENT_CHECKIN,
                                    FoursquarePrefs.DEFAULT_SEARCH_RADIUS);
                } else {
                    response =
                            mFoursquareService.searchVenuesNearby(
                                    mAccessToken, String.format("%s,%s", myLatLng.latitude, myLatLng.longitude),
                                    mySearchQuery,
                                    FoursquarePrefs.DEFAULT_SEARCH_LIMIT,
                                    FoursquarePrefs.FOURSQUARE_SEARCH_INTENT_CHECKIN,
                                    FoursquarePrefs.DEFAULT_SEARCH_RADIUS);
                }

                if(response != null) {
                    if(response.meta.code == 200 && response.response.venues != null) {
                        for (FoursquareCompactVenueResponse compact : response.response.venues) {
                            nearbyVenues.add(FoursquareCompactVenueResponse.ConvertFoursquareCompactVenueResponseToCompactVenue(compact));
                        }
                    } else if (response.meta.code == 503) {
                        ShowServerErrorToast(response.meta);
                    } else {
                        Log.i("FoursquareTasks", "Failure! Code: " + response.meta.code + " Error: " + response.meta.errorType + " - " + response.meta.errorDetail);
                        nearbyVenues = null;
                    }
                }

                return nearbyVenues;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<CompactVenue> result) {
            super.onPostExecute(result);

            if(result != null) {
                mBus.post(new VenueListEvent(true, "", result, mySearchQuery, mySearchQueryLocation, myDuplicateSearchId, myListType));
            } else {
                mBus.post(new VenueListEvent(false, "", null, mySearchQuery, mySearchQueryLocation, myDuplicateSearchId, myListType));
            }
        }
    }

    private void ShowServerErrorToast(FoursquareResponse.FoursquareResponseMeta responseMeta) {
        Toast.makeText(mContext,
                String.format(
                        mContext.getResources().getString(R.string.error_server),
                        responseMeta.errorDetail != null
                                ? responseMeta.errorDetail
                                : mContext.getResources().getString(R.string.foursquare_server_status)
                ),
                Toast.LENGTH_SHORT
        ).show();
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

    public class GetVenue extends AsyncTask<Void, Integer, Venue> {
        String myVenueId;
        Integer mySource;
        String myAccessToken;

        /**
         *
         * @param theVenueId
         * @param theSource     Either {@link com.thunsaker.soup.services.foursquare.FoursquarePrefs.CALLER_SOURCE_EDIT_VENUE} or
                                {@link com.thunsaker.soup.services.foursquare.FoursquarePrefs.CALLER_SOURCE_EDIT_CATEGORIES) or
                                {@link com.thunsaker.soup.services.foursquare.FoursquarePrefs.CALLER_SOURCE_DETAILS} or
                                {@link com.thunsaker.soup.services.foursquare.FoursquarePrefs.CALLER_SOURCE_DETAILS_INTENT}
         */
        public GetVenue(String theVenueId, Integer theSource) {
            myVenueId = theVenueId;
            mySource = theSource;
        }

        @Override
        protected Venue doInBackground(Void... params) {
            try {
                Venue resultVenue = null;

                myAccessToken =
                        !PreferencesHelper.getFoursquareToken(mContext).equals("")
                        ? PreferencesHelper.getFoursquareToken(mContext) : "";

                // Extract the venueId from a url
                if (mySource.equals(FoursquarePrefs.CALLER_SOURCE_DETAILS_INTENT) && myVenueId.contains("http")) {
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
                            else // We don't have a foursquare URL and can't do anything...
                                return null;
                            myVenueId = venueId;
                        }
                    }
                }

                GetVenueResponse response = mFoursquareService.getVenue(myVenueId, myAccessToken);

                if(response != null) {
                    if(response.meta.code == 200 && response.response.venue != null) {
                        resultVenue = FoursquareVenueResponse.ConvertFoursquareVenueResponseToVenue(response.response.venue);
                    } else if (response.meta.code == 503) {
                        ShowServerErrorToast(response.meta);
                    } else {
                        Log.i("FoursquareTasks", "Failure! Code: " + response.meta.code + " Error: " + response.meta.errorType + " - " + response.meta.errorDetail);
                    }
                } else {
                    Log.i("FoursquareTasks", "Failure :( GetVenue call");
                }

                return resultVenue;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Venue result) {
            super.onPostExecute(result);

            try {
                if (mySource == 3)
                    mySource = 2;

                if(result != null) {
                    mBus.post(new GetVenueEvent(true, "", result, 2));
                } else {
                    Toast.makeText(mContext, R.string.alert_error_loading_details, Toast.LENGTH_SHORT).show();
                    mBus.post(new GetVenueEvent(false, "", null, mySource));
                }

//                    VenueEditTabsActivity.originalVenue = result;
//                    VenueDetailFragment.currentVenue = result;
//                    if (mySource == 3)
//                        mySource = 2;
//                    switch (mySource) {
//                        case 0: // CALLER_SOURCE_EDIT_VENUE
//                            LinearLayout myLinearLayout = (LinearLayout) myCaller
//                                    .findViewById(R.id.linearLayoutProgressBarLoadingVenueInfoDescriptionWrapper);
//                            EditText myEditText = (EditText) myCaller
//                                    .findViewById(R.id.editTextEditVenueInfoDescription);
//                            myEditText.setVisibility(View.VISIBLE);
//                            myEditText.setText(result.description);
//                            myLinearLayout.setVisibility(View.GONE);
//                            myCaller.setProgressBarVisibility(false);
//                            myCaller.supportInvalidateOptionsMenu();
//                            break;
//                        case 1: // CALLER_SOURCE_EDIT_CATEGORIES
//                            VenueEditCategoriesActivity.currentVenue = result;
//                            VenueEditCategoriesActivity myCategoryCaller = (VenueEditCategoriesActivity) myCaller;
//                            VenueEditCategoriesActivity.originalCategories = new ArrayList<Category>();
//                            VenueEditCategoriesActivity.originalCategories.addAll(result.categories);
//                            myCategoryCaller.LoadCurrentCategories(null);
//                            myCategoryCaller.setProgressBarVisibility(false);
//                            break;
//                        case 2: // CALLER_SOURCE_DETAILS
//                            myVenueDetailCaller = (VenueDetailActivity) myCaller;
//                            myVenueDetailCaller.setProgressBarVisibility(false);
//
//                            if (VenueDetailFragment.currentCompactVenue == null) {
//                                VenueDetailFragment.LoadDetails(
//                                        myVenueDetailCaller.findViewById(R.id.relativeLayoutVenueWrapperOuter),
//                                        myVenueDetailCaller
//                                );
//                            }
//
//                            if (myVenueDetailCaller.findViewById(
//                                    R.id.relativeLayoutVenueWrapperOuter).isShown()) {
//                                if (result.description != null
//                                        && result.description.length() > 0) {
//                                    LinearLayout myDescriptionLinearLayout = (LinearLayout) myVenueDetailCaller
//                                            .findViewById(R.id.linearLayoutVenueDescriptionBackground);
//
//                                    myDescriptionLinearLayout
//                                            .setVisibility(View.VISIBLE);
//                                    TextView myDescriptionTextView = (TextView) myVenueDetailCaller
//                                            .findViewById(R.id.textViewVenueDescription);
//                                    myDescriptionTextView.setText(result
//                                            .description);
//                                    myDescriptionTextView
//                                            .setOnClickListener(new View.OnClickListener() {
//                                                @Override
//                                                public void onClick(View v) {
//                                                    TextView text = (TextView) v;
//                                                    text.setMaxLines(Integer.MAX_VALUE);
//                                                    text.setBackgroundDrawable(null);
//                                                    text.setOnClickListener(null);
//                                                }
//                                            });
//                                }
//                                myVenueDetailCaller.findViewById(
//                                        R.id.progressBarVenueDescription)
//                                        .setVisibility(View.GONE);
//                            }
//
//                            if (VenueDetailActivity.wasEdited)
//                                VenueDetailFragment.LoadDetails(myVenueDetailCaller
//                                                .findViewById(R.id.venue_detail_container),
//                                        myVenueDetailCaller
//                                );
//                            break;
//                    }
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
                myAccessToken = !PreferencesHelper.getFoursquareToken(myContext).equals("") ? PreferencesHelper
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
                myAccessToken = !PreferencesHelper.getFoursquareToken(myContext).equals("") ? PreferencesHelper
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

    public class GetUserHistory extends AsyncTask<Void, Integer, List<Checkin>> {
        long myStartTimestamp;
        long myEndTimestamp;
        Integer myLimit;
        Integer myOffset;
        String mySortOrder;

        public GetUserHistory(long theStartTimestamp, long theEndTimestamp, Integer theLimit, Integer theOffset, String theSortOrder) {
            myStartTimestamp = theStartTimestamp;
            myEndTimestamp = theEndTimestamp;
            myLimit = theLimit;
            myOffset = theOffset;
            mySortOrder = theSortOrder;
        }

        @Override
        protected List<Checkin> doInBackground(Void... params) {
            try {
                List<Checkin> myCheckins = new ArrayList<Checkin>();

                String mAccessToken = PreferencesHelper.getFoursquareToken(mContext);

                GetUserCheckinHistoryResponse response;
                if(myOffset == -1)
                    response =
                            mFoursquareService.getUserCheckins(
                                    FoursquarePrefs.FOURSQUARE_USER_SELF_SUFFIX,
                                    mAccessToken,
                                    myStartTimestamp,
                                    myEndTimestamp,
                                    mySortOrder);
                else
                    response =
                            mFoursquareService.getUserCheckins(
                                    FoursquarePrefs.FOURSQUARE_USER_SELF_SUFFIX,
                                    mAccessToken,
                                    myStartTimestamp,
                                    myEndTimestamp,
                                    myLimit.toString(),
                                    myOffset,
                                    mySortOrder);

                if(response != null) {
                    if(response.meta.code == 200 && response.response != null) {
                        myCheckins.addAll(response.response.checkins.items);
                    } else if (response.meta.code == 503) {
                        ShowServerErrorToast(response.meta);
                    } else {
                        Log.i("FoursquareTasks", "Failure! Code: " + response.meta.code + " Error: " + response.meta.errorType + " - " + response.meta.errorDetail);
                        myCheckins = null;
                    }
                }

                return myCheckins;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Checkin> result) {
            super.onPostExecute(result);

            if(result != null) {
                mBus.post(new CheckinHistoryEvent(true, "", result));
            } else {
                mBus.post(new CheckinHistoryEvent(false, "", null));
            }
        }
    }
}