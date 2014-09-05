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
import com.thunsaker.soup.data.api.model.*;
import com.thunsaker.soup.data.events.*;
import com.thunsaker.soup.services.AuthHelper;
import com.thunsaker.soup.services.FoursquareService;
import com.thunsaker.soup.services.foursquare.endpoints.VenueEndpoint;
import com.thunsaker.soup.ui.MainActivity;
import com.thunsaker.soup.ui.VenueDetailActivity;
import com.thunsaker.soup.ui.VenueEditTabsActivity;
import com.thunsaker.soup.util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;

import static com.thunsaker.soup.services.foursquare.FoursquarePrefs.CALLER_SOURCE_EDIT_CATEGORIES;

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

    public class EditVenue extends AsyncTask<Void, Integer, PostVenueEditResponse> {
        String mVenueId;
        Venue mModifiedVenue;

        String mAccessToken;
        String mClientId;
        String mClientSecret;
        boolean canEdit;
        boolean modifiedDescription;
        int mSource;

        public EditVenue(String theVenueToModifyId,
                         Venue theModifiedVenue,
                         Boolean modifiedDescription,
                         int theSource) {
            mVenueId = theVenueToModifyId;
            mModifiedVenue = theModifiedVenue;
            this.modifiedDescription = modifiedDescription;
            this.mSource = theSource;
        }

        @Override
        protected PostVenueEditResponse doInBackground(Void... params) {
            try {
                mAccessToken = PreferencesHelper.getFoursquareToken(mContext);

                mClientId = AuthHelper.FOURSQUARE_CLIENT_ID;
                mClientSecret = AuthHelper.FOURSQUARE_CLIENT_SECRET;

                int mSuperuserLevel = PreferencesHelper.getFoursquareSuperuserLevel(mContext);

                canEdit = mSuperuserLevel > 0;

                Map<String, String> queryParams = new HashMap<String, String>();

                if(mSource == CALLER_SOURCE_EDIT_CATEGORIES) {
                    if (mModifiedVenue.categories != null)
                        queryParams.putAll(getCategoryQueryParams(mSuperuserLevel, mModifiedVenue.categories));
                } else {
                    if (mModifiedVenue.name != null)
                        queryParams.put(FoursquarePrefs.EDIT_VENUE_NAME, Util.Encode(mModifiedVenue.name));

                    if (mModifiedVenue.location != null) {
                        queryParams.putAll(getLocationQueryParams(mSuperuserLevel, mModifiedVenue.location));
                    }

                    if (mModifiedVenue.contact != null) {
                        if (mModifiedVenue.contact.phone != null)
                            queryParams.put(FoursquarePrefs.EDIT_VENUE_PHONE, Util.Encode(mModifiedVenue.contact.phone));
                        if (mSuperuserLevel >= 2 && mModifiedVenue.contact.twitter != null)
                            queryParams.put(FoursquarePrefs.EDIT_VENUE_TWITTER, Util.Encode(mModifiedVenue.contact.twitter));
                    }

                    if (mSuperuserLevel >= 2) {
                        if (modifiedDescription && mModifiedVenue.description != null)
                            queryParams.put(FoursquarePrefs.EDIT_VENUE_DESCRIPTION, Util.Encode(mModifiedVenue.description));

                        if (mModifiedVenue.url != null)
                            queryParams.put(FoursquarePrefs.EDIT_VENUE_URL, mModifiedVenue.url);
                    }

                    if (mModifiedVenue.venueHours != null && mModifiedVenue.venueHours.timeFrames != null) {
                        StringBuilder venueHours = new StringBuilder();
                        for (TimeFrame t : mModifiedVenue.venueHours.timeFrames)
                            venueHours.append(TimeFrame.createFoursquareApiString(t));

                        if (venueHours.length() > 0)
                            queryParams.put(FoursquarePrefs.EDIT_VENUE_HOURS, venueHours.toString());
                    }

                    if(MainActivity.currentLocation != null)
                        queryParams.put(FoursquarePrefs.EDIT_VENUE_LATLONG, String.format("%s,%s", MainActivity.currentLocation.latitude, MainActivity.currentLocation.longitude));
                }

                return mFoursquareService.postVenueEdit(mVenueId, mAccessToken, queryParams);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(PostVenueEditResponse result) {
            super.onPostExecute(result);

            try {
                if(result != null) {
                    if(result.meta.code == 200) {
                        mBus.post(new EditVenueEvent(true, "", mSource));
                    } else {
                        String resultMessage;
                        switch (result.meta.code) {
                            case 403:
                                resultMessage = mContext.getString(
                                        modifiedDescription
                                                ? R.string.edit_venue_fail_unauthorized_description
                                                : R.string.edit_venue_fail_unauthorized);
                                break;
                            default:
                                resultMessage = "Failure! Code: " + result.meta.code + " Error: " + result.meta.errorType + " - " + result.meta.errorDetail;
                                break;
                        }

                        mBus.post(new EditVenueEvent(false, resultMessage, mSource));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Deprecated
    public static class EditVenueOld extends AsyncTask<Void, Integer, String> {
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

        public EditVenueOld(Context theContext, String theVenueToModifyId,
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
        Boolean withHours;

        /**
         *
         * @param theVenueId
         * @param theSource     Either {@link com.thunsaker.soup.services.foursquare.FoursquarePrefs.CALLER_SOURCE_EDIT_VENUE} or
                                {@link CALLER_SOURCE_EDIT_CATEGORIES) or
                                {@link com.thunsaker.soup.services.foursquare.FoursquarePrefs.CALLER_SOURCE_DETAILS} or
                                {@link com.thunsaker.soup.services.foursquare.FoursquarePrefs.CALLER_SOURCE_DETAILS_INTENT}
         */
        public GetVenue(String theVenueId, Integer theSource) {
            this(theVenueId, theSource, false);
        }

        public GetVenue(String theVenueId, Integer theSource, Boolean withHours) {
            myVenueId = theVenueId;
            mySource = theSource;
            this.withHours = withHours;
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

                if(myVenueId != null && myVenueId.length() > 0) {
                    GetVenueResponse response = mFoursquareService.getVenue(myVenueId, myAccessToken);

                    if(withHours)
                        new GetVenueHours(myVenueId, mySource).execute();

                    if (response != null) {
                        if (response.meta.code == 200 && response.response.venue != null) {
                            resultVenue = FoursquareVenueResponse.ConvertFoursquareVenueResponseToVenue(response.response.venue);
                        } else if (response.meta.code == 503) {
                            ShowServerErrorToast(response.meta);
                        } else {
                            Log.i("FoursquareTasks", "Failure! Code: " + response.meta.code + " Error: " + response.meta.errorType + " - " + response.meta.errorDetail);
                        }
                    } else {
                        Log.i("FoursquareTasks", "Failure :( GetVenue call");
                    }
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
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public class GetVenueHours extends AsyncTask<Void, Integer, List<TimeFrame>> {
        String myVenueId;
        Integer mySource;
        String myAccessToken;

        /**
         *
         * @param theVenueId
         * @param theSource     Either {@link com.thunsaker.soup.services.foursquare.FoursquarePrefs.CALLER_SOURCE_EDIT_VENUE} or
        {@link CALLER_SOURCE_EDIT_CATEGORIES) or
        {@link com.thunsaker.soup.services.foursquare.FoursquarePrefs.CALLER_SOURCE_DETAILS} or
        {@link com.thunsaker.soup.services.foursquare.FoursquarePrefs.CALLER_SOURCE_DETAILS_INTENT}
         */
        public GetVenueHours(String theVenueId, Integer theSource) {
            myVenueId = theVenueId;
            mySource = theSource;
        }

        @Override
        protected List<TimeFrame> doInBackground(Void... params) {
            try {
                List<TimeFrame> resultHours = new ArrayList<TimeFrame>();

                myAccessToken =
                        !PreferencesHelper.getFoursquareToken(mContext).equals("")
                                ? PreferencesHelper.getFoursquareToken(mContext) : "";

                if(myVenueId != null && myVenueId.length() > 0) {
                    GetVenueHoursResponse response = mFoursquareService.getVenueHours(myVenueId, myAccessToken);

                    if (response != null) {
                        if (response.meta.code == 200 && response.response.hours != null && response.response.hours.timeframes != null) {
                            resultHours = TimeFrame.ConvertVenueHoursTimeFrameResponseDayListToTimeFrameList(response.response.hours.timeframes);
                        } else if (response.meta.code == 503) {
                            ShowServerErrorToast(response.meta);
                        } else {
                            Log.i("FoursquareTasks", "Failure! Code: " + response.meta.code + " Error: " + response.meta.errorType + " - " + response.meta.errorDetail);
                        }
                    } else {
                        Log.i("FoursquareTasks", "Failure :( GetVenueHours call");
                    }
                }

                return resultHours;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<TimeFrame> result) {
            super.onPostExecute(result);

            try {
                if (mySource == 3)
                    mySource = 2;

                if(result != null) {
                    mBus.post(new GetVenueHoursEvent(true, "", result, 2));
                } else {
                    Toast.makeText(mContext, R.string.alert_error_loading_details, Toast.LENGTH_SHORT).show();
                    mBus.post(new GetVenueHoursEvent(false, "", null, mySource));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public class GetCategories extends AsyncTask<Void, Integer, List<Category>> {
        String myAccessToken;
        int mSource;

        public GetCategories(int theSource) {
            mSource = theSource;
        }

        @Override
        protected List<Category> doInBackground(Void... params) {
            List<Category> resultCategories = new ArrayList<Category>();

            try {
                myAccessToken =
                        !PreferencesHelper.getFoursquareToken(mContext).equals("")
                                ? PreferencesHelper.getFoursquareToken(mContext) : "";

                GetCategoriesResponse response = mFoursquareService.getCategories(myAccessToken);

                if(response != null) {
                    if(response.meta.code == 200 && response.response.categories != null) {
                        resultCategories = response.response.categories;
                    } else if (response.meta.code == 503) {
                        ShowServerErrorToast(response.meta);
                    } else {
                        Log.i("FoursquareTasks", "Failure! Code: " + response.meta.code + " Error: " + response.meta.errorType + " - " + response.meta.errorDetail);
                    }
                } else {
                    Log.i("FoursquareTasks", "Failure :( GetCategories call");
                }

                return resultCategories;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Category> result) {
            super.onPostExecute(result);
            try {
                if(result != null)
                    mBus.post(new GetCategoriesEvent(true, "", result, mSource));
                else
                    mBus.post(new GetCategoriesEvent(false, "", null, mSource));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public class FlagVenue extends AsyncTask<Void, Integer, FlagVenueResponse> {
        Integer mFlagType;
        String mVenueId;
        String mDuplicateId = "";

        String myAccessToken;

        public FlagVenue(String theVenueToModifyId, Integer theFlagType, String theDuplicateId) {
            mVenueId = theVenueToModifyId;
            mFlagType = theFlagType;
            if (mFlagType.equals(FoursquarePrefs.FlagType.DUPLICATE)) {
                mDuplicateId = theDuplicateId;
            }
        }

        @Override
        protected FlagVenueResponse doInBackground(Void... params) {
            try {
                myAccessToken =
                        !PreferencesHelper.getFoursquareToken(mContext).equals("")
                        ? PreferencesHelper.getFoursquareToken(mContext) : "";

                FlagVenueResponse response;

                if(mFlagType == FoursquarePrefs.FlagType.DUPLICATE)
                    response = mFoursquareService.flagDuplicateVenue(mVenueId, myAccessToken, Util.Encode(Util.GetFlagTypeStringFromInt(mFlagType, false)), mDuplicateId);
                else
                    response = mFoursquareService.flagVenue(mVenueId, myAccessToken, Util.Encode(Util.GetFlagTypeStringFromInt(mFlagType, false)));

                return response != null ? response : null;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(FlagVenueResponse result) {
            super.onPostExecute(result);
            mBus.post(new FlagVenueEvent(true, ""));
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

    // Helper Classes
    private Map<String, String> getLocationQueryParams(int mSuperuserLevel, Location location) {
        Map<String, String> locationParams = new HashMap<String, String>();
        if (location.address != null)
            locationParams.put(FoursquarePrefs.EDIT_VENUE_ADDRESS, Util.Encode(location.address));

        if (location.crossStreet != null)
            locationParams.put(FoursquarePrefs.EDIT_VENUE_CROSS_STREET, Util.Encode(location.crossStreet));

        if (location.city != null)
            locationParams.put(FoursquarePrefs.EDIT_VENUE_CITY, Util.Encode(location.city));

        if (location.state != null)
            locationParams.put(FoursquarePrefs.EDIT_VENUE_STATE, Util.Encode(location.state));

        if (location.postalCode != null)
            locationParams.put(FoursquarePrefs.EDIT_VENUE_ZIP, Util.Encode(location.postalCode));

        if (mSuperuserLevel >= 2 && location.latitude != 0 && location.longitude != 0)
            locationParams.put(FoursquarePrefs.EDIT_VENUE_LATLONG, location.getLatLngString());

        return locationParams;
    }

    private Map<String, String> getCategoryQueryParams(int mSuperuserLevel, List<Category> categories) {
        Map<String, String> categoryParams = new HashMap<String, String>();
        if (mSuperuserLevel > 0) {
            String modifiedCategories = "";
            for (Category c : categories)
                modifiedCategories += c.id + ",";

            modifiedCategories = modifiedCategories.substring(0, modifiedCategories.length() - 1);

            if (!modifiedCategories.equals(""))
                categoryParams.put(FoursquarePrefs.EDIT_VENUE_CATEGORY_ADD, Util.Encode(modifiedCategories));
        }

        categoryParams.put(FoursquarePrefs.EDIT_VENUE_CATEGORY_PRIMARY, categories.get(0).id);
        return categoryParams;
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
}