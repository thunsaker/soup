package com.thunsaker.soup.services.foursquare;

import com.thunsaker.soup.data.api.model.GetUserCheckinHistoryResponse;
import com.thunsaker.soup.data.api.model.PostUserCheckinResponse;

import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;

public interface SwarmService {
    /**
     *
     * Check a user into a location.
     * More: https://developer.foursquare.com/docs/checkins/add
     *
     * @param oauth_token   Auth Token
     * @param venueId       Venue Id {@link java.util.UUID} for which to fetch hours
     * @param latLong     Current GPS Coords from Device
     * @return A checkin object with details about the checkin
     */
    @POST("/checkins/add")
    PostUserCheckinResponse postUserCheckin(
            @Query("oauth_token") String oauth_token,
            @Query("venueId") String venueId,
            @Query("ll") String latLong
    );

    /**
     * Check a user into a location.
     * More: https://developer.foursquare.com/docs/checkins/add
     *
     * @param oauth_token   Auth Token
     * @param venueId       Venue Id {@link java.util.UUID} for which to fetch hours
     * @param shout         Message to accompany checkin
     * @param mentions      Users that are mentioned within the shout
     * @param latLong       Current GPS Coords from Device
     * @return A checkin object with details about the checkin
     */
    @POST("/checkins/add")
    PostUserCheckinResponse postUserCheckinWithShout(
            @Query("oauth_token") String oauth_token,
            @Query("venueId") String venueId,
            @Query("shout") String shout,
            @Query("mentions") String mentions,
            @Query("ll") String latLong
    );

    /**
     * Fetch user checkins between a set of dates.
     *
     * @param userId          User Id {@link String} of user ("self" is the only support value at this time)
     * @param oauth_token     Auth Token
     * @param afterTimestamp  Start date timestamp in UNIX time.
     * @param beforeTimestamp End date timestamp in UNIX time.
     * @param sortOrder       Either {@link com.thunsaker.soup.services.foursquare.FoursquarePrefs.History.Sort.NEWEST} or
     *                        {@link com.thunsaker.soup.services.foursquare.FoursquarePrefs.History.Sort.OLDEST}
     * @return List of checkins within {@link com.thunsaker.soup.data.api.model.GetUserCheckinHistoryResponse}
     */
    @GET("/users/{userId}/checkins")
    GetUserCheckinHistoryResponse getUserCheckins(
            @Path("USER_ID") String userId,
            @Query("oauth_token") String oauth_token,
            @Query("beforeTimestamp") long beforeTimestamp,
            @Query("afterTimestamp") long afterTimestamp,
            @Query("sort") String sortOrder);

    /**
     * Fetch user checkins between a set of dates.
     *
     * @param userId            User Id {@link String} of user ("self" is the only support value at this time)
     * @param oauth_token       Auth Token
     * @param beforeTimestamp   End date timestamp in UNIX time.
     * @param afterTimestamp    Start date timestamp in UNIX time.
     * @param limit             Limit the amount of checkins to return
     * @param offset            The amount to offset the returned checkins, use this in conjunction with @param limit
     * @param sortOrder         Either {@link com.thunsaker.soup.services.foursquare.FoursquarePrefs.History.Sort.NEWEST} or
     *                          {@link com.thunsaker.soup.services.foursquare.FoursquarePrefs.History.Sort.OLDEST}
     * @return List of checkins within {@link com.thunsaker.soup.data.api.model.GetUserCheckinHistoryResponse}
     */
    @GET("/users/{USER_ID}/checkins")
    GetUserCheckinHistoryResponse getUserCheckins(
            @Path("USER_ID") String userId,
            @Query("oauth_token") String oauth_token,
            @Query("beforeTimestamp") long beforeTimestamp,
            @Query("afterTimestamp") long afterTimestamp,
            @Query("limit") String limit,
            @Query("offset") int offset,
            @Query("sort") String sortOrder);
}