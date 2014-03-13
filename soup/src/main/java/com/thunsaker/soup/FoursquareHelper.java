package com.thunsaker.soup;

/*
 * Created by @thunsaker
 */
public class FoursquareHelper {
	public static String FOURSQUARE_BASE_URL = "https://api.foursquare.com/v2/";
    public static String FOURSQUARE_BASE_URL_MULTI =
            "https://api.foursquare.com/v2/multi?requests=";

	public static String FOURSQUARE_VENUE_SEARCH_SUFFIX = "venues/search";
	public static String FOURSQUARE_VENUE_ENDPOINT = "venues/%s";
	public static String FOURSQUARE_VENUE_EDIT_SUFFIX = "/edit";
	public static String FOURSQUARE_VENUE_PROPOSED_EDIT_SUFFIX = "/proposeedit";
	public static String FOURSQUARE_VENUE_FLAG_SUFFIX = "/flag";
	public static String FOURSQUARE_VENUE_CATEGORIES = "venues/categories";
    public static String FOURSQUARE_VENUE_HOURS = "venues/%s/hours";

	public static String FOURSQUARE_USER_ENDPOINT = "users";
	public static String FOURSQUARE_USER_SELF_SUFFIX = "/self";
	public static String FOURSQUARE_USER_CHECKINS_SUFFIX = "/checkins";
	public static String FOURSQUARE_USER_CHECKINS_ADD_SUFFIX = "/add";
	public static String FOURSQUARE_USER_LISTS_SUFFIX = "/lists";

	public static String FOURSQUARE_LISTS_ENDPOINT = "lists";
	public static String FOURSQUARE_LISTS_GROUP_CREATED = "created";
	public static String FOURSQUARE_LISTS_GROUP_EDITED = "edited"; // NOTE: The API doesn't like this for some reason, though it is in the documentation
	public static String FOURSQUARE_LISTS_GROUP_FOLLOWED = "followed";
	public static String FOURSQUARE_LISTS_GROUP_FRIENDS = "friends";
	public static String FOURSQUARE_LISTS_GROUP_SUGGESTED = "suggested";

	public static String FOURSQUARE_CHECKIN_SUFFIX = "checkins/add";

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

	public final static String SUPERUSER_STATUS_UNKNOWN = "-1";
	public static final String SEARCH_LOCATION = "SEARCH_QUERY_LOCATION";
	public static final String SEARCH_DUPLICATE = "SEARCH_DUPLICATE";

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
			final public static Integer DEFAULT = 20;
		}
	}

	public static String CURRENT_API_DATE = "20140301";
}