package com.thunsaker.soup.services;

import com.thunsaker.soup.data.api.model.GetUserCheckinHistoryResponse;
import com.thunsaker.soup.data.api.model.GetVenueHoursResponse;
import com.thunsaker.soup.data.api.model.GetVenueResponse;
import com.thunsaker.soup.data.api.model.PostVenueEditResponse;
import com.thunsaker.soup.data.api.model.VenueSearchResponse;

import java.util.Map;
import java.util.UUID;

import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;
import retrofit.http.QueryMap;

public interface FoursquareService {

    /*
    Search for Venues!
    https://developer.foursquare.com/docs/venues/search
    /v2/venues/search
    */

    /**
     * @param oauth_token Auth Token
     * @param latLong     Current GPS Coords from Device
     * @param query       Search query
     * @param limit       Max number of search results to return
     * @param intent      Either {@link com.thunsaker.soup.services.foursquare.FoursquarePrefs.FOURSQUARE_SEARCH_INTENT_BROWSE} or
     *                    {@link com.thunsaker.soup.services.foursquare.FoursquarePrefs.FOURSQUARE_SEARCH_INTENT_GLOBAL} or
     *                    {@link com.thunsaker.soup.services.foursquare.FoursquarePrefs.FOURSQUARE_SEARCH_INTENT_CHECKIN} or
     *                    {@link com.thunsaker.soup.services.foursquare.FoursquarePrefs.FOURSQUARE_SEARCH_INTENT_MATCH}
     * @param radius      Area to search within
     * @return List of venues contained within {@link com.thunsaker.soup.data.api.model.VenueSearchResponse}
     */
    @GET("/venues/search")
    VenueSearchResponse searchVenuesNearby(
            @Query("oauth_token") String oauth_token,
            @Query("ll") String latLong,
            @Query("query") String query,
            @Query("limit") int limit,
            @Query("intent") String intent,
            @Query("radius") int radius);

    /**
     * @param oauth_token Auth Token
     * @param query       Search query
     * @param near        Text search location instead of using device GPS
     * @param limit       Max number of search results to return
     * @param intent      Either {@link com.thunsaker.soup.services.foursquare.FoursquarePrefs.FOURSQUARE_SEARCH_INTENT_BROWSE} or
     *                    {@link com.thunsaker.soup.services.foursquare.FoursquarePrefs.FOURSQUARE_SEARCH_INTENT_GLOBAL} or
     *                    {@link com.thunsaker.soup.services.foursquare.FoursquarePrefs.FOURSQUARE_SEARCH_INTENT_CHECKIN} or
     *                    {@link com.thunsaker.soup.services.foursquare.FoursquarePrefs.FOURSQUARE_SEARCH_INTENT_MATCH}
     * @param radius      Area to search within
     * @return List of venues contained within {@link com.thunsaker.soup.data.api.model.VenueSearchResponse}
     */
    @GET("/venues/search")
    VenueSearchResponse searchVenues(
            @Query("oauth_token") String oauth_token,
            @Query("query") String query,
            @Query("near") String near,
            @Query("limit") int limit,
            @Query("intent") String intent,
            @Query("radius") int radius);

    /**
     * @param venueId     Venue Id {@link UUID} to fetch
     * @param oauth_token Auth Token
     * @return Single venue contained within {@link com.thunsaker.soup.data.api.model.GetVenueResponse}
     */
    @GET("/venues/{venueId}")
    GetVenueResponse getVenue(
            @Path("venueId") String venueId,
            @Query("oauth_token") String oauth_token);

    /**
     * @param venueId     Venue Id {@link UUID} for which to fetch hours
     * @param oauth_token Auth Token
     * @return Venue Hours within {@link com.thunsaker.soup.data.api.model.GetVenueHoursResponse}
     */
    @GET("/venues/{venueId}/hours")
    GetVenueHoursResponse getVenueHours(
            @Path("venueId") String venueId,
            @Query("oauth_token") String oauth_token);

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
            @Path("userId") String userId,
            @Query("oauth_token") String oauth_token,
            @Query("beforeTimestamp") long beforeTimestamp,
            @Query("afterTimestamp") long afterTimestamp,
            @Query("sort") String sortOrder);

    @GET("/users/{userId}/checkins")
    GetUserCheckinHistoryResponse getUserCheckins(
            @Path("userId") String userId,
            @Query("oauth_token") String oauth_token,
            @Query("beforeTimestamp") long beforeTimestamp,
            @Query("afterTimestamp") long afterTimestamp,
            @Query("limit") String limit,
            @Query("offset") int offset,
            @Query("sort") String sortOrder);

    @POST("/venue/{venueId}/edit")
    PostVenueEditResponse postVenueEdit(
            @Path("venueId") String venueId,
            @Query("oauth_token") String oauth_token,
            @QueryMap Map<String, String> edits);
}