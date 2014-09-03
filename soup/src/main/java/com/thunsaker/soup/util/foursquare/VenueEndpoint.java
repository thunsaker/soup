package com.thunsaker.soup.util.foursquare;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Debug;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.thunsaker.soup.PreferencesHelper;
import com.thunsaker.soup.R;
import com.thunsaker.soup.data.api.model.Category;
import com.thunsaker.soup.data.api.model.TimeFrame;
import com.thunsaker.soup.data.api.model.Venue;
import com.thunsaker.soup.services.AuthHelper;
import com.thunsaker.soup.services.foursquare.FoursquarePrefs;
import com.thunsaker.soup.ui.VenueAddCategoryActivity;
import com.thunsaker.soup.ui.VenueDetailActivity;
import com.thunsaker.soup.ui.VenueEditTabsActivity;
import com.thunsaker.soup.util.Util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;

/*
 * Created by @thunsaker
 */

@Deprecated
public class VenueEndpoint {
    @Inject
    EventBus mBus;

    public VenueEndpoint() {}

	// DEBUG EMAIL
	public static boolean SEND_DEBUG_EMAIL = false;

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
								canEdit ? myContext.getString(R.string.edit_venue_success)
										: myContext.getString(R.string.edit_venue_success_propose),
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
					for (TimeFrame t : myModifiedVenue.venueHours.timeFrames) {
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

	public static class GetCategories extends
			AsyncTask<Void, Integer, List<Category>> {
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
				myAccessToken = PreferencesHelper.getFoursquareToken(myContext) != "" ? PreferencesHelper
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

		@Override
		protected void onPostExecute(List<Category> result) {
			super.onPostExecute(result);
			try {
				VenueAddCategoryActivity.FoursquareCategoriesMaster = result;
				Calendar cal = Calendar.getInstance();
				VenueAddCategoryActivity.FoursquareCategoriesMasterDate = cal
						.get(Calendar.SECOND);

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
				myAccessToken = PreferencesHelper.getFoursquareToken(myContext) != "" ? PreferencesHelper
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