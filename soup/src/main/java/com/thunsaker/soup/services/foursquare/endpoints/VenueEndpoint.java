package com.thunsaker.soup.services.foursquare.endpoints;

import android.content.Context;
import android.os.Debug;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.thunsaker.android.common.annotations.ForApplication;
import com.thunsaker.soup.PreferencesHelper;
import com.thunsaker.soup.app.SoupApp;
import com.thunsaker.soup.data.api.model.Category;
import com.thunsaker.soup.data.api.model.CompactVenue;
import com.thunsaker.soup.data.api.model.TimeFrame;
import com.thunsaker.soup.data.api.model.Venue;
import com.thunsaker.soup.services.AuthHelper;
import com.thunsaker.soup.services.FoursquareService;
import com.thunsaker.soup.services.foursquare.FoursquarePrefs;
import com.thunsaker.soup.util.Util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

/*
 * Created by @thunsaker
 */
public class VenueEndpoint {
    // DEBUG EMAIL
    public static boolean SEND_DEBUG_EMAIL = false;

    @Inject
    @ForApplication
    Context mContext;

    @Inject
    FoursquareService mFoursquareService;

    public VenueEndpoint(SoupApp app) {
        app.inject(this);
    }

    public List<CompactVenue> GetClosestVenuesWithLatLng(LatLng currentLatLng, String searchQuery, String searchQueryLocation) {
        String mAccessToken = PreferencesHelper.getFoursquareToken(mContext);
        String mClientId = AuthHelper.FOURSQUARE_CLIENT_ID;
        String mClientSecret = AuthHelper.FOURSQUARE_CLIENT_SECRET;

        try {
            String venueRequestUrl = ConstructFoursquareServiceCall(currentLatLng, searchQuery, mAccessToken, mClientId, mClientSecret, searchQueryLocation);
            Log.d("FoursquarePrefs", "Url: " + venueRequestUrl);

            String jsonVenueRequestResponse = Util.getHttpResponse(venueRequestUrl, "", "");
            return ConvertJsonResponseToVenueList(searchQuery, jsonVenueRequestResponse);

//            mFoursquareService.searchVenues(mAccessToken, currentLatLng, resultSearchQuery,)
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

	public static List<CompactVenue> GetClosestVenuesWithLatLng(
			LatLng currentLatLng, String searchQuery, String accessToken,
			String clientId, String clientSecret, String searchQueryLocation) {

        try {
            String venueRequestUrl = ConstructFoursquareServiceCall(currentLatLng, searchQuery, accessToken, clientId, clientSecret, searchQueryLocation);
            Log.d("FoursquarePrefs", "Url: " + venueRequestUrl);

            String jsonVenueRequestResponse = Util.getHttpResponse(venueRequestUrl, "", "");

            return ConvertJsonResponseToVenueList(searchQuery, jsonVenueRequestResponse);
        } catch (Exception e) {
            Log.e("FoursquarePrefs",
                    "GetClosestVenuesWithLatLng: " + e.getMessage());
            return null;
        }
    }

    private static List<CompactVenue> ConvertJsonResponseToVenueList(String searchQuery, String jsonVenueRequestResponse) {
        List<CompactVenue> myVenues = new ArrayList<CompactVenue>();
        try {
            if (jsonVenueRequestResponse != null) {
//                Log.d("FoursquarePrefs", "Response: " + jsonVenueRequestResponse);

                JsonParser jParser = new JsonParser();
                JsonObject jObject = (JsonObject) jParser.parse(jsonVenueRequestResponse);

                if (jObject != null) {
                    JsonObject jObjectMeta = jObject.getAsJsonObject("meta");
                    if (jObjectMeta != null) {
                        if (Integer.parseInt(jObjectMeta.get("code").toString()) == 200) {
                            JsonObject jObjectResponses = jObject.getAsJsonObject("response");
                            if (jObjectResponses != null) {
                                JsonArray jArrayVenues = jObjectResponses.getAsJsonArray("venues");
                                if (jArrayVenues != null) {
                                    for (JsonElement jsonElement : jArrayVenues) {
                                        CompactVenue myParsedVenue = new CompactVenue();
                                        myParsedVenue = CompactVenue.ParseCompactVenueFromJson(jsonElement.getAsJsonObject());
                                        myVenues.add(myParsedVenue);
                                    }

                                    if (!searchQuery.equals("")) {
                                        Collections
                                                .sort(myVenues,
                                                        new Comparator<CompactVenue>() {
                                                            public int compare(CompactVenue c1, CompactVenue c2) {
                                                                return c1.location.distance.compareTo(c2.location.distance);
                                                            }
                                                        }
                                                );
                                    }
                                    return myVenues;
                                } else {
                                    Log.e("FoursquarePrefs",
                                            "Failed to parse the venues json");
                                }
                            } else {
                                Log.e("FoursquarePrefs",
                                        "Failed to parse the response json");
                            }
                        } else {
                            Log.e("FoursquarePrefs", "Failed to return a 200, meaning there was an error with the call");
                        }
                    } else {
                        Log.e("FoursquarePrefs",
                                "Failed to parse the meta section");
                    }
                } else {
                    Log.e("FoursquarePrefs",
                            "Failed to parse main response");
                }
            } else {
                Log.e("FoursquarePrefs", "Problem fetching the data");
            }

            Log.e("FoursquarePrefs", "Failed for some other reason...");
            return null;
        } catch (Exception e) {
            Log.e("FoursquarePrefs",
                    "GetClosestVenuesWithLatLng: " + e.getMessage());
            return null;
        }
    }

    private static String ConstructFoursquareServiceCall(LatLng currentLatLng, String searchQuery, String accessToken, String clientId, String clientSecret, String searchQueryLocation) throws UnsupportedEncodingException {
        String venueRequestUrl;
        if (accessToken != null && accessToken.length() > 0) {
            venueRequestUrl = String.format("%s%s?oauth_token=%s&v=%s",
                    FoursquarePrefs.FOURSQUARE_BASE_URL,
                    FoursquarePrefs.FOURSQUARE_VENUE_SEARCH_SUFFIX,
                    accessToken, FoursquarePrefs.CURRENT_API_DATE);
        } else {
            venueRequestUrl = String.format(
                    "%s%s?client_id=%s&client_secret=%s&v=%s",
                    FoursquarePrefs.FOURSQUARE_BASE_URL,
                    FoursquarePrefs.FOURSQUARE_VENUE_SEARCH_SUFFIX,
                    clientId, clientSecret,
                    FoursquarePrefs.CURRENT_API_DATE);
        }

        Boolean isGPSSearch = searchQueryLocation.matches(Util.REGEX_GPS);

        if (searchQueryLocation != null && searchQueryLocation.length() > 0) {
            // TODO: If the location is a GPS coordinate, don't encode it.
            venueRequestUrl +=
                    String.format("&near=%s", isGPSSearch
                            ? searchQueryLocation
                            : URLEncoder.encode(searchQueryLocation, Util.ENCODER_CHARSET));
        }

        if (currentLatLng != null) {
            venueRequestUrl +=
                    isGPSSearch && searchQueryLocation != null && searchQueryLocation.length() > 0
                    ? String.format("&ll=%s", searchQueryLocation)
                    : String.format("&ll=%s,%s", currentLatLng.latitude, currentLatLng.longitude);
        }

        if (searchQuery != null && searchQuery.length() > 0) {
            venueRequestUrl +=
                    "&query=" + URLEncoder.encode(searchQuery, Util.ENCODER_CHARSET);
        }
        return venueRequestUrl;
    }

    public static Venue GetVenue(String Id, String accessToken,
			String clientId, String clientSecret) {
		Venue myVenue;
		try {
			String venueRequestUrl;
			if (accessToken != null && accessToken.length() > 0) {
				venueRequestUrl = String
						.format("%s%s,%s&oauth_token=%s&v=%s",
								FoursquarePrefs.FOURSQUARE_BASE_URL_MULTI,
								String.format(
										FoursquarePrefs.FOURSQUARE_VENUE_ENDPOINT,
										Id),
								String.format(
										FoursquarePrefs.FOURSQUARE_VENUE_HOURS,
										Id), accessToken,
								FoursquarePrefs.CURRENT_API_DATE);
			} else {
				venueRequestUrl = String
						.format("%s%s,%s&client_id=%s&client_secret=%s&v=%s",
								FoursquarePrefs.FOURSQUARE_BASE_URL_MULTI,
								String.format(
										FoursquarePrefs.FOURSQUARE_VENUE_ENDPOINT,
										Id),
								String.format(
										FoursquarePrefs.FOURSQUARE_VENUE_HOURS,
										Id), clientId, clientSecret,
								FoursquarePrefs.CURRENT_API_DATE);
			}

			if (Debug.isDebuggerConnected())
				Log.i("FoursquarePrefs", "Url: " + venueRequestUrl);

			String jsonVenueRequestResponse = Util.getHttpResponse(
					venueRequestUrl, "", "");

			try {
				if (jsonVenueRequestResponse != null) {
					JsonParser jParser = new JsonParser();
					JsonObject jObject = (JsonObject) jParser
							.parse(jsonVenueRequestResponse);

					if (jObject != null) {
						myVenue = new Venue();
						JsonObject jObjectMeta = jObject
								.getAsJsonObject("meta");
						if (jObjectMeta != null) {
							if (Integer.parseInt(jObjectMeta.get("code")
									.toString()) == 200) {
								JsonObject jObjectResponse = jObject
										.getAsJsonObject("response");
								JsonArray jObjectResponseArray = jObjectResponse
										.getAsJsonArray("responses");
								if (jObjectResponseArray != null) {
									JsonObject jObjectVenueResponseArray = jObjectResponseArray
											.get(0).getAsJsonObject();
									JsonObject jObjectResponseArrayItem = jObjectVenueResponseArray
											.getAsJsonObject("response");
									JsonObject jObjectVenue = jObjectResponseArrayItem
											.getAsJsonObject("venue");
									if (jObjectVenue != null) {
										myVenue = Venue
												.ParseVenueFromJson(jObjectVenue);
									}

									JsonObject jObjectHoursResponseArray = jObjectResponseArray
											.get(1).getAsJsonObject();
									JsonObject jsonObjectHoursResponse = jObjectHoursResponseArray
											.getAsJsonObject("response");
									JsonObject jObjectHours = jsonObjectHoursResponse
											.getAsJsonObject("hours");

									if (jObjectHours != null
											&& jObjectHours.has("timeframes")) {
										myVenue.venueHours = Venue.ParseVenueHoursFromJson(
												jObjectHours, myVenue.venueHours);
									}

									return myVenue;
									// } else {
									// Log.e("FoursquarePrefs",
									// "Failed to parse the venue json");
									// }
								} else {
									Log.e("FoursquarePrefs",
											"Failed to parse the response json");
								}
							} else {
								Log.e("FoursquarePrefs",
										"Failed to return a 200, "
												+ "meaning there was an error with the call");
							}
						} else {
							Log.e("FoursquarePrefs",
									"Failed to parse the meta section");
						}
					} else {
						Log.e("FoursquarePrefs",
								"Failed to parse main response");
					}
				} else {
					Log.e("FoursquarePrefs", "Problem fetching the data");
				}

				Log.e("FoursquarePrefs", "Failed for some other reason...");
				return null;
			} catch (Exception e) {
				Log.e("FoursquarePrefs",
						"GetClosestVenuesWithLatLng: " + e.getMessage());
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Edit Venue allows submission of edits to a single venue. Edit Refrence:
	 * https://developer.foursquare.com/docs/venues/edit Proposed Edit
	 * Reference: https://developer.foursquare.com/docs/venues/proposeedit
	 */
	public static String EditVenue(String myVenueId, String accessToken,
			String clientId, String clientSecret, Venue myModifiedVenue,
			int level, Boolean modifiedDescription, Boolean fromAddCategory) {
		// DEBUG EMAIL
		String tempDebugString = "DEBUG: ";
		try {
			String venueRequestUrl;
			boolean canEdit = level > 0;

			if (accessToken != null && accessToken.length() > 0) {
				venueRequestUrl = String
						.format("%s%s?oauth_token=%s&v=%s",
								FoursquarePrefs.FOURSQUARE_BASE_URL,
								String.format(
										FoursquarePrefs.FOURSQUARE_VENUE_ENDPOINT,
										myVenueId)
										+ (canEdit ? FoursquarePrefs.FOURSQUARE_VENUE_EDIT_SUFFIX
												: FoursquarePrefs.FOURSQUARE_VENUE_PROPOSED_EDIT_SUFFIX),
								accessToken, FoursquarePrefs.CURRENT_API_DATE);
			} else {
				venueRequestUrl = String
						.format("%s%s?client_id=%s&client_secret=%s&v=%s",
								FoursquarePrefs.FOURSQUARE_BASE_URL,
								String.format(
										FoursquarePrefs.FOURSQUARE_VENUE_ENDPOINT,
										myVenueId)
										+ (canEdit ? FoursquarePrefs.FOURSQUARE_VENUE_EDIT_SUFFIX
												: FoursquarePrefs.FOURSQUARE_VENUE_PROPOSED_EDIT_SUFFIX),
								clientId, clientSecret,
								FoursquarePrefs.CURRENT_API_DATE);
			}

			if (fromAddCategory && myModifiedVenue.categories != null) {
				if (canEdit) {
					String myModifiedCategories = "";
					for (Category c : myModifiedVenue.categories) {
						myModifiedCategories += c.id + ",";
					}

					myModifiedCategories = myModifiedCategories.substring(0,
							myModifiedCategories.length() - 1);

					if (!myModifiedCategories.equals("")) {
						venueRequestUrl += String.format("&categoryId=%s",
								Util.Encode(myModifiedCategories));
					}
				} else {
					venueRequestUrl += String.format(
							"&primaryCategoryId=%s",
							Util.Encode(myModifiedVenue.categories.get(0).id));
				}
			} else {
				// Add the editable values
				venueRequestUrl += String
						.format("&name=%s&address=%s&crossStreet=%s&city=%s&state=%s&zip=%s&phone=%s",
								Util.Encode(myModifiedVenue.name),
                                Util.Encode(myModifiedVenue.location.address),
                                Util.Encode(myModifiedVenue.location.crossStreet),
                                Util.Encode(myModifiedVenue.location.city),
                                Util.Encode(myModifiedVenue.location.state),
                                Util.Encode(myModifiedVenue.location.postalCode),
                                Util.Encode(myModifiedVenue.contact.phone));

				if (myModifiedVenue.venueHours != null
						&& myModifiedVenue.venueHours.timeFrames != null) {
					StringBuilder venueHours = new StringBuilder();
					for (TimeFrame t : myModifiedVenue.venueHours
							.timeFrames) {
						venueHours.append(TimeFrame.createFoursquareApiString(t));
					}
					if (venueHours.length() > 0) {
						venueRequestUrl += String.format("&hours=%s",
								venueHours.toString());
					}
				}

				if (level >= 2) {
					venueRequestUrl += String.format("&twitter=%s&url=%s", Util
							.Encode(myModifiedVenue.contact.twitter),
							Util.Encode(myModifiedVenue.url));

					if (modifiedDescription)
						venueRequestUrl += String.format("&description=%s",
								Util.Encode(myModifiedVenue.description));

					venueRequestUrl += String.format("&ll=%s,%s",
							myModifiedVenue.location.latitude,
							myModifiedVenue.location.longitude);
				}
			}

			if (SEND_DEBUG_EMAIL)
				tempDebugString += String.format("Url: %s", venueRequestUrl);

			if (Debug.isDebuggerConnected())
				Log.i("FoursquarePrefs", "Edit Request Url: "
						+ venueRequestUrl);

			String jsonEditVenueRequestResponse = Util.getHttpResponse(
					venueRequestUrl, true, "", "");

			if (SEND_DEBUG_EMAIL)
				tempDebugString += String.format(" Response: %s",
						jsonEditVenueRequestResponse);

			try {
				if (jsonEditVenueRequestResponse != null) {
					if (Debug.isDebuggerConnected())
						Log.i("FoursquarePrefs",
								"Edit Venue Response for VenueId " + myVenueId
										+ ": " + jsonEditVenueRequestResponse);

					JsonParser jParser = new JsonParser();
					JsonObject jObject = (JsonObject) jParser
							.parse(jsonEditVenueRequestResponse);

					if (jObject != null) {
						JsonObject jObjectMeta = jObject
								.getAsJsonObject("meta");
						if (jObjectMeta != null) {
							Integer responseCode = jObjectMeta.get("code")
									.getAsInt();
							if (responseCode == 200) {
								if (SEND_DEBUG_EMAIL) {
									return tempDebugString;
								} else {
									return FoursquarePrefs.SUCCESS;
								}
							} else if (responseCode == 403) {
								if (SEND_DEBUG_EMAIL) {
									return tempDebugString;
								} else {
									return FoursquarePrefs.FAIL_UNAUTHORIZED;
								}
							} else {
								Log.e("FoursquarePrefs",
										"Failed to return a 200, "
												+ "meaning there was an error with the call");

								if (SEND_DEBUG_EMAIL) {
									return tempDebugString;
								} else {
									return FoursquarePrefs.FAIL;
								}
							}
						} else {
							Log.e("FoursquarePrefs",
									"Failed to parse the meta section");
						}
					} else {
						Log.e("FoursquarePrefs",
								"Failed to parse main response");
					}
				} else {
					Log.e("FoursquarePrefs", "Problem fetching the data");
				}

				Log.e("FoursquarePrefs", "Failed for some other reason...");
				if (SEND_DEBUG_EMAIL) {
					return tempDebugString;
				} else {
					return FoursquarePrefs.FAIL;
				}
			} catch (Exception e) {
				Log.e("FoursquarePrefs", "EditVenue: " + e.getMessage());
				if (SEND_DEBUG_EMAIL) {
					return tempDebugString;
				} else {
					return FoursquarePrefs.FAIL;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (SEND_DEBUG_EMAIL) {
				return tempDebugString;
			} else {
				return FoursquarePrefs.FAIL;
			}
		}
	}

	public static String EditVenueCategories(String myVenueId,
			String accessToken, String clientId, String clientSecret,
			List<Category> myCategories, Boolean canEdit) {
		try {
			String venueRequestUrl;
			// Create the base url
			if (accessToken != null && accessToken.length() > 0) {
				venueRequestUrl = String
						.format("%s%s?oauth_token=%s&v=%s",
								FoursquarePrefs.FOURSQUARE_BASE_URL,
								String.format(
										FoursquarePrefs.FOURSQUARE_VENUE_ENDPOINT,
										myVenueId)
										+ (canEdit ? FoursquarePrefs.FOURSQUARE_VENUE_EDIT_SUFFIX
												: FoursquarePrefs.FOURSQUARE_VENUE_PROPOSED_EDIT_SUFFIX),
								accessToken, FoursquarePrefs.CURRENT_API_DATE);
			} else {
				venueRequestUrl = String
						.format("%s%s?client_id=%s&client_secret=%s&v=%s",
								FoursquarePrefs.FOURSQUARE_BASE_URL,
								String.format(
										FoursquarePrefs.FOURSQUARE_VENUE_ENDPOINT,
										myVenueId)
										+ (canEdit ? FoursquarePrefs.FOURSQUARE_VENUE_EDIT_SUFFIX
												: FoursquarePrefs.FOURSQUARE_VENUE_PROPOSED_EDIT_SUFFIX),
								clientId, clientSecret,
								FoursquarePrefs.CURRENT_API_DATE);
			}
			if (Debug.isDebuggerConnected())
				Log.i("FoursquarePrefs", "Base Url: " + venueRequestUrl);

			String listOfCategoryIds = null;

			for (Category c : myCategories) {
				if (c.primary)
					listOfCategoryIds = c.id + listOfCategoryIds + ",";
				else
					listOfCategoryIds += c.id + ",";
			}

			listOfCategoryIds.subSequence(0, listOfCategoryIds.length() - 1);

			// Add the editable values
			venueRequestUrl += String.format("&categoryId=%s",
					listOfCategoryIds);

			if (Debug.isDebuggerConnected())
				Log.i("FoursquarePrefs", "Edit Venue Categories Request Url: "
						+ venueRequestUrl);

			String jsonEditVenueRequestResponse = Util.getHttpResponse(
					venueRequestUrl, true, "", "");

			try {
				if (jsonEditVenueRequestResponse != null) {
					if (Debug.isDebuggerConnected())
						Log.i("FoursquarePrefs",
								"Edit Venue Response for VenueId " + myVenueId
										+ ": " + jsonEditVenueRequestResponse);
					JsonParser jParser = new JsonParser();
					JsonObject jObject = (JsonObject) jParser
							.parse(jsonEditVenueRequestResponse);

					if (jObject != null) {
						JsonObject jObjectMeta = jObject
								.getAsJsonObject("meta");
						if (jObjectMeta != null) {
							Integer responseCode = Integer.parseInt(jObjectMeta
									.get("code").toString());
							if (responseCode == 200) {
								JsonObject jObjectResponses = jObject
										.getAsJsonObject("response");
								if (jObjectResponses != null) {
									return FoursquarePrefs.SUCCESS;
								} else {
									Log.e("FoursquarePrefs",
											"Failed to parse the response json");
								}
							} else if (responseCode == 403) {
								return FoursquarePrefs.FAIL_UNAUTHORIZED;
							} else {
								Log.e("FoursquarePrefs",
										"Failed to return a 200, "
												+ "meaning there was an error with the call");
							}
						} else {
							Log.e("FoursquarePrefs",
									"Failed to parse the meta section");
						}
					} else {
						Log.e("FoursquarePrefs",
								"Failed to parse main response");
					}
				} else {
					Log.e("FoursquarePrefs", "Problem fetching the data");
				}

				Log.e("FoursquarePrefs", "Failed for some other reason...");
				return FoursquarePrefs.FAIL;
			} catch (Exception e) {
				Log.e("FoursquarePrefs",
						"GetClosestVenuesWithLatLng: " + e.getMessage());
				return FoursquarePrefs.FAIL;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return FoursquarePrefs.FAIL;
		}
	}

	public static List<Category> GetCategories(String accessToken,
			String clientId, String clientSecret) {
		List<Category> myFoursquareCategories;

		try {
			String venueRequestUrl;
			if (accessToken != null && accessToken.length() > 0) {
				venueRequestUrl = String.format("%s%s?oauth_token=%s&v=%s",
						FoursquarePrefs.FOURSQUARE_BASE_URL,
						FoursquarePrefs.FOURSQUARE_VENUE_CATEGORIES,
						accessToken, FoursquarePrefs.CURRENT_API_DATE);
			} else {
				venueRequestUrl = String.format(
						"%s%s?client_id=%s&client_secret=%s&v=%s",
						FoursquarePrefs.FOURSQUARE_BASE_URL,
						FoursquarePrefs.FOURSQUARE_VENUE_CATEGORIES, clientId,
						clientSecret, FoursquarePrefs.CURRENT_API_DATE);
			}

			if (Debug.isDebuggerConnected())
				Log.i("FoursquarePrefs", "Get Categories Url: "
						+ venueRequestUrl);

			String jsonVenueRequestResponse = Util.getHttpResponse(
					venueRequestUrl, "", "");

			try {
				if (jsonVenueRequestResponse != null) {
					JsonParser jParser = new JsonParser();
					JsonObject jObject = (JsonObject) jParser
							.parse(jsonVenueRequestResponse);

					if (jObject != null) {
						JsonObject jObjectMeta = jObject
								.getAsJsonObject("meta");
						if (jObjectMeta != null) {
							if (Integer.parseInt(jObjectMeta.get("code")
									.toString()) == 200) {
								JsonObject jObjectResponses = jObject
										.getAsJsonObject("response");
								if (jObjectResponses != null) {
									JsonArray jArrayVenues = jObjectResponses
											.getAsJsonArray("categories");
									if (jArrayVenues != null) {
										myFoursquareCategories = Category
												.GetCategoriesFromJson(
														jArrayVenues, true);
										return myFoursquareCategories;
									} else {
										Log.e("FoursquarePrefs",
												"Failed to parse the venues json");
									}
								} else {
									Log.e("FoursquarePrefs",
											"Failed to parse the response json");
								}
							} else {
								Log.e("FoursquarePrefs",
										"Failed to return a 200, "
												+ "meaning there was an error with the call");
							}
						} else {
							Log.e("FoursquarePrefs",
									"Failed to parse the meta section");
						}
					} else {
						Log.e("FoursquarePrefs",
								"Failed to parse main response");
					}
				} else {
					Log.e("FoursquarePrefs", "Problem fetching the data");
				}

				Log.e("FoursquarePrefs", "Failed for some other reason...");
				return null;
			} catch (Exception e) {
				Log.e("FoursquarePrefs",
						"GetClosestVenuesWithLatLng: " + e.getMessage());
				return null;
			}

		} catch (Exception e) {
			Log.e("FoursquarePrefs",
					"GetClosestVenuesWithLatLng: " + e.getMessage());
			return null;
		}
	}

	/**
	 * Flag Venue allows user to flag an incorrect/closed/etc venue. Edit
	 * Reference: https://developer.foursquare.com/docs/venues/flag
	 */
	public static String FlagVenue(String myVenueId, String accessToken,
			String clientId, String clientSecret, Integer myFlagType,
			String myDuplicateId) {
		try {
			String venueRequestUrl;
			// Create the base url
			if (accessToken != null && accessToken.length() > 0) {
				venueRequestUrl = String
						.format("%s%s?oauth_token=%s&v=%s",
								FoursquarePrefs.FOURSQUARE_BASE_URL,
								String.format(
										FoursquarePrefs.FOURSQUARE_VENUE_ENDPOINT,
										myVenueId)
										+ FoursquarePrefs.FOURSQUARE_VENUE_FLAG_SUFFIX,
								accessToken, FoursquarePrefs.CURRENT_API_DATE);
			} else {
				venueRequestUrl = String
						.format("%s%s?client_id=%s&client_secret=%s&v=%s",
								FoursquarePrefs.FOURSQUARE_BASE_URL,
								String.format(
										FoursquarePrefs.FOURSQUARE_VENUE_ENDPOINT,
										myVenueId)
										+ FoursquarePrefs.FOURSQUARE_VENUE_FLAG_SUFFIX,
								clientId, clientSecret,
								FoursquarePrefs.CURRENT_API_DATE);
			}

			venueRequestUrl += String.format("&problem=%s", Util.Encode(Util
					.GetFlagTypeStringFromInt(myFlagType, false)));

			if (myFlagType.equals(FoursquarePrefs.FlagType.DUPLICATE)
					&& !myDuplicateId.equals(""))
				venueRequestUrl += String.format("&venueId=%s", myDuplicateId);

			if (Debug.isDebuggerConnected())
				Log.i("FoursquarePrefs", "Flag Venue Request Url: "
						+ venueRequestUrl);

			String jsonEditVenueRequestResponse = Util.getHttpResponse(
					venueRequestUrl, true, "", "");

			try {
				if (jsonEditVenueRequestResponse != null) {
					if (Debug.isDebuggerConnected())
						Log.i("FoursquarePrefs",
								"Flag Venue Response for VenueId " + myVenueId
										+ ": " + jsonEditVenueRequestResponse);
					JsonParser jParser = new JsonParser();
					JsonObject jObject = (JsonObject) jParser
							.parse(jsonEditVenueRequestResponse);

					if (jObject != null) {
						JsonObject jObjectMeta = jObject
								.getAsJsonObject("meta");
						if (jObjectMeta != null) {
							Integer responseCode = jObjectMeta.get("code")
									.getAsInt();
							if (responseCode == 200) {
								return FoursquarePrefs.SUCCESS;
							} else if (responseCode == 403) {
								return FoursquarePrefs.FAIL_UNAUTHORIZED;
							} else {
								Log.e("FoursquarePrefs",
										"Failed to return a 200, "
												+ "meaning there was an error with the call");
								return FoursquarePrefs.FAIL;
							}
						} else {
							Log.e("FoursquarePrefs",
									"Failed to parse the meta section");
						}
					} else {
						Log.e("FoursquarePrefs",
								"Failed to parse main response");
					}
				} else {
					Log.e("FoursquarePrefs", "Problem fetching the data");
				}

				Log.e("FoursquarePrefs", "Failed for some other reason...");
				return FoursquarePrefs.FAIL;
			} catch (Exception e) {
				Log.e("FoursquarePrefs", "EditVenue: " + e.getMessage());
				return FoursquarePrefs.FAIL;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return FoursquarePrefs.FAIL;
		}
	}
}