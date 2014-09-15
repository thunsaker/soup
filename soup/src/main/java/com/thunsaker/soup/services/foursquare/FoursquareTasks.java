package com.thunsaker.soup.services.foursquare;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.thunsaker.android.common.annotations.ForApplication;
import com.thunsaker.soup.PreferencesHelper;
import com.thunsaker.soup.R;
import com.thunsaker.soup.app.SoupApp;
import com.thunsaker.soup.data.api.GetUserInfoResponse;
import com.thunsaker.soup.data.api.model.Category;
import com.thunsaker.soup.data.api.model.Checkin;
import com.thunsaker.soup.data.api.model.CompactVenue;
import com.thunsaker.soup.data.api.model.FlagVenueResponse;
import com.thunsaker.soup.data.api.model.FoursquareCompactVenueResponse;
import com.thunsaker.soup.data.api.model.FoursquareList;
import com.thunsaker.soup.data.api.model.FoursquareListGroup;
import com.thunsaker.soup.data.api.model.FoursquareResponse;
import com.thunsaker.soup.data.api.model.FoursquareVenueResponse;
import com.thunsaker.soup.data.api.model.GetCategoriesResponse;
import com.thunsaker.soup.data.api.model.GetListResponse;
import com.thunsaker.soup.data.api.model.GetUserCheckinHistoryResponse;
import com.thunsaker.soup.data.api.model.GetUserListsResponse;
import com.thunsaker.soup.data.api.model.GetVenueHoursResponse;
import com.thunsaker.soup.data.api.model.GetVenueResponse;
import com.thunsaker.soup.data.api.model.Location;
import com.thunsaker.soup.data.api.model.PostUserCheckinResponse;
import com.thunsaker.soup.data.api.model.PostVenueEditResponse;
import com.thunsaker.soup.data.api.model.TimeFrame;
import com.thunsaker.soup.data.api.model.Venue;
import com.thunsaker.soup.data.api.model.VenueSearchResponse;
import com.thunsaker.soup.data.events.CheckinHistoryEvent;
import com.thunsaker.soup.data.events.EditVenueEvent;
import com.thunsaker.soup.data.events.FlagVenueEvent;
import com.thunsaker.soup.data.events.GetCategoriesEvent;
import com.thunsaker.soup.data.events.GetListEvent;
import com.thunsaker.soup.data.events.GetListsEvent;
import com.thunsaker.soup.data.events.GetUserInfoEvent;
import com.thunsaker.soup.data.events.GetVenueEvent;
import com.thunsaker.soup.data.events.GetVenueHoursEvent;
import com.thunsaker.soup.data.events.VenueListEvent;
import com.thunsaker.soup.services.AuthHelper;
import com.thunsaker.soup.services.FoursquareService;
import com.thunsaker.soup.ui.MainActivity;
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

    @Inject
    SwarmService mSwarmService;

    public FoursquareTasks(SoupApp app) {
        app.inject(this);
    }

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
                            mSwarmService.getUserCheckins(
                                    FoursquarePrefs.FOURSQUARE_USER_SELF_SUFFIX,
                                    mAccessToken,
                                    myStartTimestamp,
                                    myEndTimestamp,
                                    mySortOrder);
                else
                    response =
                            mSwarmService.getUserCheckins(
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

    public class PostUserCheckin extends AsyncTask<Void, Integer, PostUserCheckinResponse> {
        LatLng mCurrentLatLng;
        String mVenueId;
        String mVenueName;
        String mMessage;

        String myAccessToken;

        public PostUserCheckin(String theFoursquareVenueId, String theVenueName, String theMsg, LatLng theCurrentLatLng ) {
            mVenueId = theFoursquareVenueId;
            mVenueName = theVenueName;
            mMessage = theMsg;
            mCurrentLatLng = theCurrentLatLng;
        }

        @Override
        protected PostUserCheckinResponse doInBackground(Void... params) {
            NotificationManager mNotificationManager =
                    (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

            NotificationCompat.Builder mNotificationFoursquare =
                    new NotificationCompat.Builder(mContext)
                            .setSmallIcon(R.drawable.ic_stat_soup)
                            .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_stat_check_white))
                            .setProgress(0, 0, true)
                            .setContentText(mContext.getString(R.string.notification_checkin_title))
                            .setContentTitle(mContext.getString(R.string.notification_checkin_pending))
                            .setContentIntent(MainActivity.genericPendingIntent);

            mNotificationManager.notify(MainActivity.NOTIFICATION_CHECKIN, mNotificationFoursquare.build());

            myAccessToken =
                    !PreferencesHelper.getFoursquareToken(mContext).equals("")
                            ? PreferencesHelper.getFoursquareToken(mContext) : "";

            String ll = String.format("%s,%s", mCurrentLatLng.latitude, mCurrentLatLng.longitude);

            if(mMessage.length() > 0)
                return mSwarmService.postUserCheckinWithShout(myAccessToken, mVenueId, mMessage, "", ll);
            else
                return mSwarmService.postUserCheckin(myAccessToken, mVenueId, ll);
        }

        @Override
        protected void onPostExecute(PostUserCheckinResponse result) {
            super.onPostExecute(result);
            NotificationManager mNotificationManager =
                    (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

            if(result != null && result.meta.code == 200) {
                NotificationCompat.Builder mNotificationFoursquarePosted =
                        new NotificationCompat.Builder(mContext)
                                .setSmallIcon(R.drawable.ic_stat_soup)
                                .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_stat_check_white))
                                .setContentText(mContext.getString(R.string.notification_checkin_title))
                                .setContentTitle(String.format(mContext.getString(R.string.notification_checkin_complete), mVenueName))
                                .setContentIntent(MainActivity.genericPendingIntent);
                mNotificationManager.notify(MainActivity.NOTIFICATION_CHECKIN, mNotificationFoursquarePosted.build());
                mNotificationManager.cancel(MainActivity.NOTIFICATION_CHECKIN);
            } else {
                Intent foursquareVenueIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse(String.format(FoursquarePrefs.FOURSQURE_INTENT_VENUE_URL, mVenueId)));
                TaskStackBuilder foursquareVenueStackBuilder = TaskStackBuilder.create(mContext);
                foursquareVenueStackBuilder.addParentStack(MainActivity.class);
                foursquareVenueStackBuilder.addNextIntent(foursquareVenueIntent);
                PendingIntent foursquareVenuePendingIntent =
                        foursquareVenueStackBuilder.getPendingIntent(
                                0,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );

                Intent retryCheckinIntent = new Intent(mContext, MainActivity.class);
                retryCheckinIntent.putExtra(MainActivity.VENUE_ID_CHECKIN_EXTRA, mVenueId);
                retryCheckinIntent.putExtra(MainActivity.VENUE_NAME_CHECKIN_EXTRA, mVenueName);
                TaskStackBuilder retryCheckinStackBuilder = TaskStackBuilder.create(mContext);
                retryCheckinStackBuilder.addParentStack(MainActivity.class);
                retryCheckinStackBuilder.addNextIntent(retryCheckinIntent);
                PendingIntent retryCheckinPendingIntent =
                        retryCheckinStackBuilder.getPendingIntent(
                                0,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );

                NotificationCompat.Builder mNotificationFoursquareFail =
                        new NotificationCompat.Builder(mContext)
                                .setSmallIcon(R.drawable.ic_stat_soup)
                                .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_stat_check_white))
                                .setAutoCancel(true)
                                .addAction(R.drawable.ic_action_foursquare_holo_dark, mContext.getString(R.string.notification_checkin_foursquare), foursquareVenuePendingIntent)
                                .addAction(R.drawable.ic_action_refresh_holo_dark, mContext.getString(R.string.notification_checkin_retry), retryCheckinPendingIntent)
                                .setContentIntent(foursquareVenuePendingIntent)
                                .setContentText(mContext.getString(R.string.notification_checkin_title))
                                .setContentTitle(mContext.getString(R.string.notification_checkin_fail));

                mNotificationManager.notify(MainActivity.NOTIFICATION_CHECKIN, mNotificationFoursquareFail.build());
            }
        }

    }

    public class GetUserInfo extends AsyncTask<Void, Integer, GetUserInfoResponse> {
        String mUserId;
        String myAccessToken;

        public GetUserInfo(String theUserId) {
            mUserId = theUserId;
        }

        @Override
        protected GetUserInfoResponse doInBackground(Void... params) {
            myAccessToken =
                    !PreferencesHelper.getFoursquareToken(mContext).equals("")
                            ? PreferencesHelper.getFoursquareToken(mContext) : "";

            return mFoursquareService.getUserInfo(mUserId, myAccessToken);
        }

        @Override
        protected void onPostExecute(GetUserInfoResponse result) {
            super.onPostExecute(result);

            String resultMessage = null;
            if (result != null) {
                if (result.meta.code == 200) {
                    if (result.response != null && result.response.user != null) {
                        mBus.post(new GetUserInfoEvent(true, "", result.response.user));
                        return;
                    }
                } else {
                    resultMessage = "Failure! Code: " + result.meta.code + " Error: " + result.meta.errorType + " - " + result.meta.errorDetail;
                }
            } else {
                resultMessage = "Failure fetching user details";
            }

            mBus.post(new GetUserInfoEvent(false, resultMessage, null));
        }
    }

    public class GetFoursquareList extends AsyncTask<Void, Integer, FoursquareList> {
        String myListId;
        String myAccessToken;

        /**
         * Get Foursquare list.
         *
         * @param theListId     List Id to fetch
         */
        public GetFoursquareList(String theListId) {
            myListId = theListId;
        }

        @Override
        protected FoursquareList doInBackground(Void... params) {
            try {
                FoursquareList resultList = null;

                myAccessToken =
                        !PreferencesHelper.getFoursquareToken(mContext).equals("")
                                ? PreferencesHelper.getFoursquareToken(mContext) : "";

                if(myListId != null && myListId.length() > 0) {
                    GetListResponse response = mFoursquareService.getList(myListId, myAccessToken);

                    if (response != null)
                        if (response.meta.code == 200 && response.response.list != null)
                            resultList = response.response.list;
                        else if (response.meta.code == 503)
                            ShowServerErrorToast(response.meta);
                        else
                            Log.i("FoursquareTasks", "Failure! Code: " + response.meta.code + " Error: " + response.meta.errorType + " - " + response.meta.errorDetail);
                    else
                        Log.i("FoursquareTasks", "Failure :( GetLists call");
                }

                return resultList;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(FoursquareList result) {
            super.onPostExecute(result);

            try {
                if(result != null) {
                    mBus.post(new GetListEvent(true, "", result));
                } else {
                    Toast.makeText(mContext, R.string.alert_error_loading_list, Toast.LENGTH_SHORT).show();
                    mBus.post(new GetListsEvent(false, "", null));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public class GetFoursquareLists extends AsyncTask<Void, Integer, List<FoursquareList>> {
        String myUserId;
        String myAccessToken;
        String myListGroup;

        /**
         * Get default Foursquare lists for the current user.
         */
        public GetFoursquareLists() {
            this(FoursquarePrefs.FOURSQUARE_USER_SELF_SUFFIX);
        }

        /**
         * Get default Foursquare lists for the given user.
         *
         * @param theUserId     User id or "self"
         */
        public GetFoursquareLists(String theUserId) {
            this(theUserId, "");
        }

        /**
         * Get default Foursquare lists for the given user and list group
         *
         * @param theUserId     User id or "self"
         * @param theListGroup  Either {@link com.thunsaker.soup.services.foursquare.FoursquarePrefs.FOURSQUARE_LISTS_GROUP_CREATED} or
         *                      {@link com.thunsaker.soup.services.foursquare.FoursquarePrefs.FOURSQUARE_LISTS_GROUP_EDITED} or
         *                      {@link com.thunsaker.soup.services.foursquare.FoursquarePrefs.FOURSQUARE_LISTS_GROUP_FOLLOWED} or
         *                      {@link com.thunsaker.soup.services.foursquare.FoursquarePrefs.FOURSQUARE_LISTS_GROUP_FRIENDS} or
         *                      {@link com.thunsaker.soup.services.foursquare.FoursquarePrefs.FOURSQUARE_LISTS_GROUP_SUGGESTED}
         */
        public GetFoursquareLists(String theUserId, String theListGroup) {
            myUserId = theUserId;
            myListGroup = theListGroup;
        }

        @Override
        protected List<FoursquareList> doInBackground(Void... params) {
            try {
                List<FoursquareList> resultLists = new ArrayList<FoursquareList>();

                myAccessToken =
                        !PreferencesHelper.getFoursquareToken(mContext).equals("")
                                ? PreferencesHelper.getFoursquareToken(mContext) : "";

                if(myUserId != null && myUserId.length() > 0) {
                    GetUserListsResponse response;
                    if(myListGroup.length() > 0)
                        response = mFoursquareService.getUserListsByGroup(myUserId, myAccessToken, myListGroup);
                    else
                        response = mFoursquareService.getUserLists(myUserId, myAccessToken);

                    if (response != null)
                        if (response.meta.code == 200 && response.response.lists != null)
                            for (FoursquareListGroup listGroup : response.response.lists.groups)
                                resultLists.addAll(listGroup.items);
                        else if (response.meta.code == 503)
                            ShowServerErrorToast(response.meta);
                        else
                            Log.i("FoursquareTasks", "Failure! Code: " + response.meta.code + " Error: " + response.meta.errorType + " - " + response.meta.errorDetail);
                    else
                        Log.i("FoursquareTasks", "Failure :( GetLists call");
                }

                return resultLists;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<FoursquareList> result) {
            super.onPostExecute(result);

            try {
                if(result != null) {
                    mBus.post(new GetListsEvent(true, "", result));
                } else {
                    Toast.makeText(mContext, R.string.alert_error_loading_details, Toast.LENGTH_SHORT).show();
                    mBus.post(new GetListsEvent(false, "", null));
                }
            } catch (Exception e) {
                e.printStackTrace();
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