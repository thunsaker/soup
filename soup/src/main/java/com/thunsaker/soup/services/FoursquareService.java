package com.thunsaker.soup.services;

import com.google.android.gms.maps.model.LatLng;
import com.thunsaker.soup.data.api.model.VenueSearchResponse;

import retrofit.http.GET;
import retrofit.http.Query;

public interface FoursquareService {

    /*
    Search for Venues!
    https://developer.foursquare.com/docs/venues/search
    /v2/venues/search
    */

    /**
     *
     * @param oauth_token
     * @param latLong
     * @param query
     * @param near
     * @param limit
     * @param intent        Either {@link com.thunsaker.soup.services.foursquare.FoursquarePrefs.FOURSQUARE_SEARCH_INTENT_BROWSE} or
     *                      {@link com.thunsaker.soup.services.foursquare.FoursquarePrefs.FOURSQUARE_SEARCH_INTENT_GLOBAL} or
     *                      {@link com.thunsaker.soup.services.foursquare.FoursquarePrefs.FOURSQUARE_SEARCH_INTENT_CHECKIN} or
     *                      {@link com.thunsaker.soup.services.foursquare.FoursquarePrefs.FOURSQUARE_SEARCH_INTENT_MATCH}
     * @param radius
     * @return
     */
    @GET("/venues/search")
    VenueSearchResponse searchVenues(@Query("oauth_token") String oauth_token,
                                     @Query("ll") String latLong,
                                     @Query("query") String query,
                                     @Query("near") String near,
                                     @Query("limit") int limit,
                                     @Query("intent") String intent,
                                     @Query("radius") int radius);
//    ShortenSaveResponse saveLink(@Query("access_token") String access_token, @Query("longUrl") String longUrl, @Query("private") Boolean isPrivate);
}
