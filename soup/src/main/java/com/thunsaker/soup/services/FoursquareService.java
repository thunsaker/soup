package com.thunsaker.soup.services;

import com.thunsaker.soup.data.api.GetUserInfoResponse;
import com.thunsaker.soup.data.api.model.FlagVenueResponse;
import com.thunsaker.soup.data.api.model.GetCategoriesResponse;
import com.thunsaker.soup.data.api.model.GetListResponse;
import com.thunsaker.soup.data.api.model.GetUserListsResponse;
import com.thunsaker.soup.data.api.model.GetVenueHoursResponse;
import com.thunsaker.soup.data.api.model.GetVenueResponse;
import com.thunsaker.soup.data.api.model.PostVenueEditResponse;
import com.thunsaker.soup.data.api.model.VenueSearchResponse;

import java.util.Map;
import java.util.UUID;

import retrofit.http.EncodedQueryMap;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;

public interface FoursquareService {

    /**
     * Get User information
     * https://developer.foursquare.com/docs/users/users
     * @param userId        User id {@link String} to fetch or self
     * @param oauth_token   Auth Token {@link String}
     * @return
     */
    @GET("/users/{USER_ID}")
    GetUserInfoResponse getUserInfo(
            @Path("USER_ID") String userId,
            @Query("oauth_token") String oauth_token
    );

    /**
     *  Search for Venues!
     *  https://developer.foursquare.com/docs/venues/search/v2/venues/search
     *
     * @param oauth_token Auth Token {@link String}
     * @param latLong     Current GPS Coords from Device
     * @param query       Search query {@link String}
     * @param limit       Max number of search results to return {@link int}
     * @param intent      Either {@link com.thunsaker.soup.services.foursquare.FoursquarePrefs.FOURSQUARE_SEARCH_INTENT_BROWSE} or
     *                    {@link com.thunsaker.soup.services.foursquare.FoursquarePrefs.FOURSQUARE_SEARCH_INTENT_GLOBAL} or
     *                    {@link com.thunsaker.soup.services.foursquare.FoursquarePrefs.FOURSQUARE_SEARCH_INTENT_CHECKIN} or
     *                    {@link com.thunsaker.soup.services.foursquare.FoursquarePrefs.FOURSQUARE_SEARCH_INTENT_MATCH}
     * @param radius      Area to search within {@link int}
     * @return List of venues contained within {@link VenueSearchResponse}
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
     *  Search for Venues!
     *  https://developer.foursquare.com/docs/venues/search/v2/venues/search
     *
     * @param oauth_token Auth Token {@link String}
     * @param query       Search query
     * @param near        Text search location instead of using device GPS
     * @param limit       Max number of search results to return {@link int}
     * @param intent      Either {@link com.thunsaker.soup.services.foursquare.FoursquarePrefs.FOURSQUARE_SEARCH_INTENT_BROWSE} or
     *                    {@link com.thunsaker.soup.services.foursquare.FoursquarePrefs.FOURSQUARE_SEARCH_INTENT_GLOBAL} or
     *                    {@link com.thunsaker.soup.services.foursquare.FoursquarePrefs.FOURSQUARE_SEARCH_INTENT_CHECKIN} or
     *                    {@link com.thunsaker.soup.services.foursquare.FoursquarePrefs.FOURSQUARE_SEARCH_INTENT_MATCH}
     * @param radius      Area to search within {@link int}
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
     * Fetch the complete venue
     *
     * @param venueId     Venue Id {@link UUID} to fetch
     * @param oauth_token Auth Token {@link String}
     * @return Single venue contained within {@link com.thunsaker.soup.data.api.model.GetVenueResponse}
     */
    @GET("/venues/{VENUE_ID}")
    GetVenueResponse getVenue(
            @Path("VENUE_ID") String venueId,
            @Query("oauth_token") String oauth_token);

    /**
     * Fetch Venue Hours
     *
     * @param venueId     Venue Id {@link UUID} for which to fetch hours
     * @param oauth_token Auth Token {@link String}
     * @return Venue Hours within {@link com.thunsaker.soup.data.api.model.GetVenueHoursResponse}
     */
    @GET("/venues/{VENUE_ID}/hours")
    GetVenueHoursResponse getVenueHours(
            @Path("VENUE_ID") String venueId,
            @Query("oauth_token") String oauth_token);

    /**
     * Edit venue passing a collection of fields to be edited
     *
     * @param venueId       Venue Id {@link UUID} for which to fetch hours
     * @param oauth_token   Auth Token {@link String}
     * @param edits         Collection of fields to be edited see: https://developer.foursquare.com/docs/venues/proposeedit
     * @return Returns success/error message
     */
    @POST("/venues/{VENUE_ID}/proposeedit")
    PostVenueEditResponse postVenueEdit(
            @Path("VENUE_ID") String venueId,
            @Query("oauth_token") String oauth_token,
            @EncodedQueryMap Map<String, String> edits);

    /**
     * Flag a venue with a certain problem
     *
     * @param venueId       Venue Id {@link UUID} for which to fetch hours
     * @param oauth_token   Auth Token {@link String}
     * @param problem       Problem to be reported. {@link String}
     * @return Returns success/error message
     */
    @POST("/venues/{VENUE_ID}/flag")
    FlagVenueResponse flagVenue(
            @Path("VENUE_ID") String venueId,
            @Query("oauth_token") String oauth_token,
            @Query("problem") String problem);

    /**
     * Flag a venue as duplicate
     *
     * @param venueId           Venue Id {@link UUID} for which to fetch hours
     * @param oauth_token       Auth Token {@link String}
     * @param problem           Problem to be reported. {@link String}
     * @param duplicateVenueId  Venue Id {@link UUID} of the duplicate venue
     * @return Returns success/error message
     */
    @POST("/venues/{VENUE_ID}/flag")
    FlagVenueResponse flagDuplicateVenue(
            @Path("VENUE_ID") String venueId,
            @Query("oauth_token") String oauth_token,
            @Query("problem") String problem,
            @Query("venueId") String duplicateVenueId);

    /**
     * Fetch the complete venue
     *
     * @return All venue categories {@link com.thunsaker.soup.data.api.model.Category}
     */
    @GET("/venues/categories")
    GetCategoriesResponse getCategories(
            @Query("oauth_token") String oauth_token);

    /**
     * Fetch default lists from user (created/followed)
     *
     * @param userId        User id {@link String} to fetch or self
     * @param oauth_token   Auth Token {@link String}
     * @return  List of {@link com.thunsaker.soup.data.api.model.FoursquareListGroup}
     */
    @GET("/users/{USER_ID}/lists")
    GetUserListsResponse getUserLists(
            @Path("USER_ID") String userId,
            @Query("oauth_token") String oauth_token);

    /**
     * Fetch specific list(s) from user by group name
     * @param userId        User id {@link String} to fetch or self
     * @param oauth_token   Auth Token {@link String}
     * @param group         Foursquare List group either {@link com.thunsaker.soup.services.foursquare.FoursquarePrefs.FOURSQUARE_LISTS_GROUP_CREATED} or
     *                      {@link com.thunsaker.soup.services.foursquare.FoursquarePrefs.FOURSQUARE_LISTS_GROUP_EDITED} or
     *                      {@link com.thunsaker.soup.services.foursquare.FoursquarePrefs.FOURSQUARE_LISTS_GROUP_FOLLOWED} or
     *                      {@link com.thunsaker.soup.services.foursquare.FoursquarePrefs.FOURSQUARE_LISTS_GROUP_FRIENDS} or
     *                      {@link com.thunsaker.soup.services.foursquare.FoursquarePrefs.FOURSQUARE_LISTS_GROUP_SUGGESTED}
     *                      or a comma-delimited string of the list types
     * @return  List of {@link com.thunsaker.soup.data.api.model.FoursquareListGroup}
     */
    @GET("/users/{USER_ID}/lists")
    GetUserListsResponse getUserListsByGroup(
            @Path("USER_ID") String userId,
            @Query("oauth_token") String oauth_token,
            @Query("group") String group);

    /**
     * Fetch specific list from Foursquare
     * @param listId        List id {@link UUID} to fetch
     * @param oauth_token   Auth Token {@link String}
     * @return  FoursquareList with list items and details.
     */
    @GET("/lists/{LIST_ID}")
    GetListResponse getList(
            @Path("LIST_ID") String listId,
            @Query("oauth_token") String oauth_token);
}