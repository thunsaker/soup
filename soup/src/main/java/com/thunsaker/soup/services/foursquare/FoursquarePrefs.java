package com.thunsaker.soup.services.foursquare;

/*
 * Created by @thunsaker
 */
public class FoursquarePrefs {
    public static final int DEFAULT_SEARCH_LIMIT = 30;
    public static final int DEFAULT_SEARCH_RADIUS = 10000;

    public static String FOURSQUARE_BASE_URL = "https://api.foursquare.com/v2";
    public static String FOURSQUARE_BASE_URL_MULTI =
            "https://api.foursquare.com/v2/multi?requests=";

	public static String FOURSQUARE_VENUE_SEARCH_SUFFIX = "/venues/search";
	public static String FOURSQUARE_VENUE_ENDPOINT = "/venues/%s";
	public static String FOURSQUARE_VENUE_EDIT_SUFFIX = "/edit";
	public static String FOURSQUARE_VENUE_PROPOSED_EDIT_SUFFIX = "/proposeedit";
	public static String FOURSQUARE_VENUE_FLAG_SUFFIX = "/flag";
	public static String FOURSQUARE_VENUE_CATEGORIES = "/venues/categories";
    public static String FOURSQUARE_VENUE_HOURS = "/venues/%s/hours";

	public static String FOURSQUARE_USER_ENDPOINT = "/users";
	public static String FOURSQUARE_USER_SELF_SUFFIX = "self";
	public static String FOURSQUARE_USER_CHECKINS_SUFFIX = "/checkins";
	public static String FOURSQUARE_USER_CHECKINS_ADD_SUFFIX = "/add";
	public static String FOURSQUARE_USER_LISTS_SUFFIX = "/lists";

	public static String FOURSQUARE_LISTS_ENDPOINT = "/lists";
	public static String FOURSQUARE_LISTS_GROUP_CREATED = "created";
	public static String FOURSQUARE_LISTS_GROUP_EDITED = "edited"; // NOTE: The API doesn't like this for some reason, though it is in the documentation
	public static String FOURSQUARE_LISTS_GROUP_FOLLOWED = "followed";
	public static String FOURSQUARE_LISTS_GROUP_FRIENDS = "friends";
	public static String FOURSQUARE_LISTS_GROUP_SUGGESTED = "suggested";

	public static String FOURSQUARE_CHECKIN_SUFFIX = "/checkins/add";

	public static String FOURSQURE_INTENT_VENUE_URL = "https://foursquare.com/v/%s";

	public static String SUCCESS = "success";
	public static String FAIL = "fail";
	public static String FAIL_UNAUTHORIZED = "fail_unauthorized";

	public final static Integer CALLER_SOURCE_EDIT_VENUE = 0;
	public final static Integer CALLER_SOURCE_EDIT_CATEGORIES = 1;
	public final static Integer CALLER_SOURCE_DETAILS = 2;
	public final static Integer CALLER_SOURCE_DETAILS_INTENT = 3;

	public static class FlagType {
		public final static Integer MISLOCATED = 0;
		public final static Integer DOESNT_EXIST = 1;
		public final static Integer CLOSED = 2;
		public final static Integer INAPPROPRIATE = 3;
		public final static Integer EVENT_OVER = 4;
		public final static Integer DUPLICATE = 5;
	}

	public final static int SUPERUSER_STATUS_UNKNOWN = -1;
	public static final String SEARCH_LOCATION = "SEARCH_QUERY_LOCATION";
	public static final String SEARCH_DUPLICATE = "SEARCH_DUPLICATE";
    public static final String SEARCH_DUPLICATE_VENUE_ID = "SEARCH_DUPLICATE_VENUE_ID";

	public static class SUPERUSER {
		public final static Integer UNKNOWN = -1;
		public final static Integer NOPE = 0;
		public final static Integer SU1 = 1;
		public final static Integer SU2 = 2;
		public final static Integer SU3 = 3;
	}

	final public static class History {
		final public static class Sort {
			final public static String NEWEST = "newestfirst";
			final public static String OLDEST = "oldestfirst";
		}

		final public static class View {
			final public static Integer TODAY = 0;
			final public static Integer LAST_WEEK = 1;
			final public static Integer LAST_MONTH = 2;
		}

		final public static class Limit {
			final public static Integer MAX = 250;
			final public static Integer DEFAULT = 30;
            final public static Integer NO_LIMIT = -1;
		}
	}

	public static String CURRENT_API_DATE = "20140901";
    public static final String API_MODE_FOURSQUARE = "foursquare";
    public static final String API_MODE_SWARM = "swarm";

    /**
     *   Find venues within a given area. Unlike the checkin intent, browse searches an entire region instead of only finding Venues closest to a point. You must define a region to search be including either the ll and radius parameters, or the sw and ne. The region will be a spherical cap if you include the ll and radius parameters, or it will be a bounding quadrangle if you include the sw and ne parameters.
     */
    public static final String FOURSQUARE_SEARCH_INTENT_BROWSE = "browse";
    /**
     *   Finds results that the current user (or, for userless requests, a typical user) is likely to check in to at the provided ll at the current moment in time. This is the intent we recommend most apps use.
     */
    public static final String FOURSQUARE_SEARCH_INTENT_CHECKIN = "checkin";
    /**
     *   Finds the most globally relevant venues for the search, independent of location. Ignores all other parameters other than query and limit.
     */
    public static final String FOURSQUARE_SEARCH_INTENT_GLOBAL = "global";
    /**
     *   Finds venues that are are nearly-exact matches for the given parameters. This intent is highly sensitive to the provided location. We recommend using this intent only when trying to correlate an existing place database with Foursquare's. The results will be sorted best match first, taking distance and spelling mistakes/variations into account. query and ll are the only required parameters for this intent, but matching also supports phone, address, city, state, zip, and twitter. There's no specified format for these parameters—we do our best to normalize them and drop them from the search if unsuccessful.
     */
    public static final String FOURSQUARE_SEARCH_INTENT_MATCH = "match";

    public static final String EDIT_VENUE_NAME = "name";
    public static final String EDIT_VENUE_ADDRESS = "address";
    public static final String EDIT_VENUE_CROSS_STREET = "crossStreet";
    public static final String EDIT_VENUE_CITY = "city";
    public static final String EDIT_VENUE_STATE = "state";
    public static final String EDIT_VENUE_ZIP = "zip";
    public static final String EDIT_VENUE_PHONE = "phone";
    public static final String EDIT_VENUE_LATLONG = "venuell";
    public static final String EDIT_VENUE_CATEGORY_PRIMARY = "primaryCategoryId";
    public static final String EDIT_VENUE_CATEGORY_ADD = "addCategoryIds";
    public static final String EDIT_VENUE_CATEGORY_REMOVE = "removeCategoryIds";
    public static final String EDIT_VENUE_TWITTER = "twitter";
    public static final String EDIT_VENUE_DESCRIPTION = "description";
    public static final String EDIT_VENUE_URL = "url";
    public static final String EDIT_VENUE_URL_FACEBOOK = "facebookUrl";
    public static final String EDIT_VENUE_URL_MENU = "menuUrl";
    public static final String EDIT_VENUE_STORE_ID = "storeId";
    public static final String EDIT_VENUE_HOURS = "hours";

    public static final String EDIT_VENUE_USER_LATLONG = "ll";
}