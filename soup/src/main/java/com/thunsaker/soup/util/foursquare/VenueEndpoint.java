package com.thunsaker.soup.util.foursquare;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Debug;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.thunsaker.soup.AuthHelper;
import com.thunsaker.soup.FoursquareHelper;
import com.thunsaker.soup.PreferencesHelper;
import com.thunsaker.soup.R;
import com.thunsaker.soup.classes.foursquare.Category;
import com.thunsaker.soup.classes.foursquare.CompactVenue;
import com.thunsaker.soup.classes.foursquare.TimeFrame;
import com.thunsaker.soup.classes.foursquare.Venue;
import com.thunsaker.soup.ui.MainActivity;
import com.thunsaker.soup.ui.VenueAddCategoryActivity;
import com.thunsaker.soup.ui.VenueDetailActivity;
import com.thunsaker.soup.ui.VenueDetailFragment;
import com.thunsaker.soup.ui.VenueEditCategoriesActivity;
import com.thunsaker.soup.ui.VenueEditTabsActivity;
import com.thunsaker.soup.ui.VenueListFragment;
import com.thunsaker.soup.ui.VenueListFragment.VenueListAdapter;
import com.thunsaker.soup.util.Util;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/*
 * Created by @thunsaker
 */
public class VenueEndpoint {
	// DEBUG EMAIL
	public static boolean SEND_DEBUG_EMAIL = false;

	public static class GetClosestVenues extends
			AsyncTask<Void, Integer, List<CompactVenue>> {
		Context myContext;
		LatLng myLatLng;
		String mySearchQuery;
		String mySearchQueryLocation;
		String myAccessToken;
		String myClientId;
		String myClientSecret;
		VenueListFragment myCaller;
		String myDuplicateSearchId;

		public GetClosestVenues(Context theContext, VenueListFragment theCaller, String theSearchQuery,
				String theSearchQueryLocation, String theDuplicateSearchId) {
			myContext = theContext;
			myLatLng = MainActivity.currentLocation;
			mySearchQuery = theSearchQuery;
			mySearchQueryLocation = theSearchQueryLocation;
			myCaller = theCaller;
			myDuplicateSearchId = theDuplicateSearchId;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			VenueListFragment.isRefreshing = true;
			myCaller.getActivity().setProgressBarVisibility(true);
		}

		@Override
		protected List<CompactVenue> doInBackground(Void... params) {
			try {
				if (VenueListFragment.isSearching && mySearchQuery.equals("")) {
					VenueListFragment.searchResultsVenueList = new ArrayList<CompactVenue>();
					return null;
				}

				myAccessToken = !PreferencesHelper.getFoursquareToken(myContext).equals("")
						? PreferencesHelper.getFoursquareToken(myContext) : "";
				myClientId = AuthHelper.FOURSQUARE_CLIENT_ID;
				myClientSecret = AuthHelper.FOURSQUARE_CLIENT_SECRET;

				List<CompactVenue> nearbyVenues;
				if (mySearchQueryLocation != null
						&& mySearchQueryLocation.length() > 0) {
					nearbyVenues = VenueEndpoint.GetClosestVenuesWithLatLng(
							myLatLng, mySearchQuery, myAccessToken, myClientId,
							myClientSecret, myCaller, mySearchQueryLocation);
				} else {
					nearbyVenues = VenueEndpoint.GetClosestVenuesWithLatLng(
							myLatLng, mySearchQuery, myAccessToken, myClientId,
							myClientSecret, myCaller, "");
				}
				return nearbyVenues != null ? nearbyVenues : null;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		@Override
		protected void onPostExecute(List<CompactVenue> result) {
			super.onPostExecute(result);

			List<CompactVenue> updatedList;

			try {
				if (result != null && myCaller.isVisible()) {
					if (!mySearchQuery.equals("")) {
						if (VenueListFragment.isDuplicateSearching) {
							List<CompactVenue> modifiedList = new ArrayList<CompactVenue>();
							if (VenueListFragment.searchDuplicateResultsVenueList == null)
								VenueListFragment.searchDuplicateResultsVenueList = new ArrayList<CompactVenue>();

							for (CompactVenue c : result) {
								String tempId = c.getId().trim();
								if (!tempId.equals(myDuplicateSearchId.trim()))
									modifiedList.add(c);
							}

							VenueListFragment.searchDuplicateResultsVenueList = modifiedList;

							if (VenueListFragment.searchDuplicateResultsVenueListAdapter == null)
								VenueListFragment.searchDuplicateResultsVenueListAdapter = myCaller.new VenueListAdapter(
										myContext, R.layout.list_venue_item,
										null);

							if (VenueListFragment.searchDuplicateResultsVenueListAdapter.items == null)
								VenueListFragment.searchDuplicateResultsVenueListAdapter.items = new ArrayList<CompactVenue>();

							VenueListFragment.searchDuplicateResultsVenueListAdapter.items
									.addAll(modifiedList);
							VenueListFragment.searchDuplicateResultsVenueListAdapter
									.notifyDataSetChanged();

							updatedList = VenueListFragment.searchDuplicateResultsVenueList;
						} else {
							if (VenueListFragment.searchResultsVenueList == null)
								VenueListFragment.searchResultsVenueList = new ArrayList<CompactVenue>();

							VenueListFragment.searchResultsVenueList = result;

							if (VenueListFragment.searchResultsVenueListAdapter == null)
								VenueListFragment.searchResultsVenueListAdapter = myCaller.new VenueListAdapter(
										myContext, R.layout.list_venue_item,
										null);

							if (VenueListFragment.searchResultsVenueListAdapter.items == null)
								VenueListFragment.searchResultsVenueListAdapter.items = new ArrayList<CompactVenue>();

							VenueListFragment.searchResultsVenueListAdapter.items
									.addAll(result);
							VenueListFragment.searchResultsVenueListAdapter
									.notifyDataSetChanged();

							updatedList = VenueListFragment.searchResultsVenueList;
						}
					} else {
						if (VenueListFragment.currentVenueList == null)
							VenueListFragment.currentVenueList = new ArrayList<CompactVenue>();

						VenueListFragment.currentVenueList = result;

						if (VenueListFragment.currentVenueListAdapter.items == null)
							VenueListFragment.currentVenueListAdapter.items = new ArrayList<CompactVenue>();

						VenueListFragment.currentVenueListAdapter.items
								.addAll(result);
						VenueListFragment.currentVenueListAdapter
								.notifyDataSetChanged();

						updatedList = VenueListFragment.currentVenueList;
					}

					if (updatedList != null) {
						VenueListAdapter myAdapter = myCaller.new VenueListAdapter(
								myContext, R.layout.list_venue_item,
								updatedList);
						myCaller.setListAdapter(myAdapter);
					}
				} else if (VenueListFragment.isSearching && mySearchQuery.equals("")) {
					VenueListFragment.searchResultsVenueListAdapter = myCaller.new VenueListAdapter(
							myContext, R.layout.list_venue_item,
							VenueListFragment.searchResultsVenueList);
					myCaller.setListAdapter(VenueListFragment.searchResultsVenueListAdapter);

					Toast.makeText(myContext, myContext.getString(R.string.alert_error_loading_venues), Toast.LENGTH_SHORT).show();
				}

				VenueListFragment.isRefreshing = false;
			} catch (Exception e) {
				e.printStackTrace();
				if (myCaller != null && myCaller.isVisible()) {
					myCaller.getActivity().setProgressBarVisibility(false);
					VenueListFragment.mPullToRefreshLayout.setRefreshComplete();
				}
				VenueListFragment.isRefreshing = false;
			} finally {
				if (myCaller != null && myCaller.isVisible()) {
					myCaller.getActivity().setProgressBarVisibility(false);
					VenueListFragment.mPullToRefreshLayout.setRefreshComplete();
				}
				VenueListFragment.isRefreshing = false;
			}
		}
	}

	public static List<CompactVenue> GetClosestVenuesWithLatLng(
			LatLng currentLatLng, String searchQuery, String accessToken,
			String clientId, String clientSecret, VenueListFragment caller,
			String searchQueryLocation) {
		List<CompactVenue> myVenues = new ArrayList<CompactVenue>();

		try {
			String venueRequestUrl;
			if (accessToken != null && accessToken.length() > 0) {
				venueRequestUrl = String.format("%s%s?oauth_token=%s&v=%s",
						FoursquareHelper.FOURSQUARE_BASE_URL,
						FoursquareHelper.FOURSQUARE_VENUE_SEARCH_SUFFIX,
						accessToken, FoursquareHelper.CURRENT_API_DATE);
			} else {
				venueRequestUrl = String.format(
						"%s%s?client_id=%s&client_secret=%s&v=%s",
						FoursquareHelper.FOURSQUARE_BASE_URL,
						FoursquareHelper.FOURSQUARE_VENUE_SEARCH_SUFFIX,
						clientId, clientSecret,
						FoursquareHelper.CURRENT_API_DATE);
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

//			Log.e("FoursquareHelper",
//			"Failed because we have no location...");
//			return null;

			if (Debug.isDebuggerConnected())
				Log.i("FoursquareHelper", "Url: " + venueRequestUrl);
			String jsonVenueRequestResponse = Util.getHttpResponse(
					venueRequestUrl, "", "");

			try {
				if (jsonVenueRequestResponse != null) {
					/*
					 * if(Debug.isDebuggerConnected()) Log.i("FoursquareHelper",
					 * "Response: " + jsonVenueRequestResponse);
					 */
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
											.getAsJsonArray("venues");
									if (jArrayVenues != null) {
										for (JsonElement jsonElement : jArrayVenues) {
											CompactVenue myParsedVenue = new CompactVenue();
											myParsedVenue = CompactVenue
													.ParseCompactVenueFromJson(jsonElement
															.getAsJsonObject());
											myVenues.add(myParsedVenue);
										}
										if (!searchQuery.equals("")) {
											Collections
													.sort(myVenues,
															new Comparator<CompactVenue>() {
																public int compare(
																		CompactVenue c1,
																		CompactVenue c2) {
																	return c1
																			.getLocation()
																			.getDistance()
																			.compareTo(
																					c2.getLocation()
																							.getDistance());
																}
															});
										}
										return myVenues;
									} else {
										Log.e("FoursquareHelper",
												"Failed to parse the venues json");
									}
								} else {
									Log.e("FoursquareHelper",
											"Failed to parse the response json");
								}
							} else {
								Log.e("FoursquareHelper", "Failed to return a 200, meaning there was an error with the call");
							}
						} else {
							Log.e("FoursquareHelper",
									"Failed to parse the meta section");
						}
					} else {
						Log.e("FoursquareHelper",
								"Failed to parse main response");
					}
				} else {
					Log.e("FoursquareHelper", "Problem fetching the data");
				}

				Log.e("FoursquareHelper", "Failed for some other reason...");
				return null;
			} catch (Exception e) {
				Log.e("FoursquareHelper",
						"GetClosestVenuesWithLatLng: " + e.getMessage());
				return null;
			}

		} catch (Exception e) {
			Log.e("FoursquareHelper",
					"GetClosestVenuesWithLatLng: " + e.getMessage());
			return null;
		}
	}

	public static class GetVenue extends AsyncTask<Void, Integer, Venue> {
		Context myContext;
		String myVenueId;
		FragmentActivity myCaller;
		VenueDetailActivity myVenueDetailCaller;
		Integer mySource;

		String myAccessToken;
		String myClientId;
		String myClientSecret;

		public GetVenue(Context theContext, String theVenueId,
				FragmentActivity theCaller, Integer theSource) {
			myContext = theContext;
			myVenueId = theVenueId;
			mySource = theSource;
			myCaller = theCaller;
		}

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
		protected Venue doInBackground(Void... params) {
			try {
				myAccessToken = !PreferencesHelper.getFoursquareToken(myContext).equals("") ? PreferencesHelper
						.getFoursquareToken(myContext) : "";
				myClientId = AuthHelper.FOURSQUARE_CLIENT_ID;
				myClientSecret = AuthHelper.FOURSQUARE_CLIENT_SECRET;

				if (mySource == FoursquareHelper.CALLER_SOURCE_DETAILS_INTENT
						&& myVenueId.contains("http")) {
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
							else
								return null;
							// We don't have a foursquare URL and can't do
							// anything...

							myVenueId = venueId;
						}
					}
				}

				Venue venueResult = VenueEndpoint.GetVenue(myVenueId,
						myAccessToken, myClientId, myClientSecret);
				return venueResult != null ? venueResult : null;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		@Override
		protected void onPostExecute(Venue result) {
			super.onPostExecute(result);

			try {
				if (result != null) {
					VenueEditTabsActivity.originalVenue = result;
					VenueDetailFragment.currentVenue = result;
					if (mySource == 3)
						mySource = 2;
					switch (mySource) {
					case 0: // CALLER_SOURCE_EDIT_VENUE
						LinearLayout myLinearLayout = (LinearLayout) myCaller
								.findViewById(R.id.linearLayoutProgressBarLoadingVenueInfoDescriptionWrapper);
						EditText myEditText = (EditText) myCaller
								.findViewById(R.id.editTextEditVenueInfoDescription);
						myEditText.setVisibility(View.VISIBLE);
						myEditText.setText(result.getDescription());
						myLinearLayout.setVisibility(View.GONE);
						myCaller.setProgressBarVisibility(false);
						myCaller.supportInvalidateOptionsMenu();
						break;
					case 1: // CALLER_SOURCE_EDIT_CATEGORIES
						VenueEditCategoriesActivity.currentVenue = result;
						VenueEditCategoriesActivity myCategoryCaller = (VenueEditCategoriesActivity) myCaller;
						VenueEditCategoriesActivity.originalCategories = new ArrayList<Category>();
						VenueEditCategoriesActivity.originalCategories
								.addAll(result.getCategories());
						myCategoryCaller.LoadCurrentCategories(null);
						myCategoryCaller.setProgressBarVisibility(false);
						break;
					case 2: // CALLER_SOURCE_DETAILS
						myVenueDetailCaller = (VenueDetailActivity) myCaller;
						myVenueDetailCaller.setProgressBarVisibility(false);

						if (VenueDetailFragment.currentCompactVenue == null) {
							VenueDetailFragment
									.LoadDetails(
											myVenueDetailCaller
													.findViewById(R.id.relativeLayoutVenueWrapperOuter),
											myVenueDetailCaller);
						}

						if (myVenueDetailCaller.findViewById(
								R.id.relativeLayoutVenueWrapperOuter).isShown()) {
							if (result.getDescription() != null
									&& result.getDescription().length() > 0) {
								LinearLayout myDescriptionLinearLayout = (LinearLayout) myVenueDetailCaller
										.findViewById(R.id.linearLayoutVenueDescriptionBackground);

								myDescriptionLinearLayout
										.setVisibility(View.VISIBLE);
								TextView myDescriptionTextView = (TextView) myVenueDetailCaller
										.findViewById(R.id.textViewVenueDescription);
								myDescriptionTextView.setText(result
										.getDescription());
								myDescriptionTextView
										.setOnClickListener(new View.OnClickListener() {
											@Override
											public void onClick(View v) {
												TextView text = (TextView) v;
												text.setMaxLines(Integer.MAX_VALUE);
												text.setBackgroundDrawable(null);
												text.setOnClickListener(null);
											}
										});
							}
							myVenueDetailCaller.findViewById(
									R.id.progressBarVenueDescription)
									.setVisibility(View.GONE);
						}

						if (VenueDetailActivity.wasEdited)
							VenueDetailFragment.LoadDetails(myVenueDetailCaller
									.findViewById(R.id.venue_detail_container),
									myVenueDetailCaller);
						break;
					}

				} else {
					Toast.makeText(myContext,
							R.string.alert_error_loading_details,
							Toast.LENGTH_SHORT).show();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static Venue GetVenue(String Id, String accessToken,
			String clientId, String clientSecret) {
		Venue myVenue;
		try {
			String venueRequestUrl;
			if (accessToken != null && accessToken.length() > 0) {
				venueRequestUrl = String
						.format("%s/%s,/%s&oauth_token=%s&v=%s",
								FoursquareHelper.FOURSQUARE_BASE_URL_MULTI,
								String.format(
										FoursquareHelper.FOURSQUARE_VENUE_ENDPOINT,
										Id),
								String.format(
										FoursquareHelper.FOURSQUARE_VENUE_HOURS,
										Id), accessToken,
								FoursquareHelper.CURRENT_API_DATE);
			} else {
				venueRequestUrl = String
						.format("%s/%s,/%s&client_id=%s&client_secret=%s&v=%s",
								FoursquareHelper.FOURSQUARE_BASE_URL_MULTI,
								String.format(
										FoursquareHelper.FOURSQUARE_VENUE_ENDPOINT,
										Id),
								String.format(
										FoursquareHelper.FOURSQUARE_VENUE_HOURS,
										Id), clientId, clientSecret,
								FoursquareHelper.CURRENT_API_DATE);
			}

			if (Debug.isDebuggerConnected())
				Log.i("FoursquareHelper", "Url: " + venueRequestUrl);

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
										myVenue.setVenueHours(Venue.ParseVenueHoursFromJson(
												jObjectHours,
												myVenue.getVenueHours()));
									}

									return myVenue;
									// } else {
									// Log.e("FoursquareHelper",
									// "Failed to parse the venue json");
									// }
								} else {
									Log.e("FoursquareHelper",
											"Failed to parse the response json");
								}
							} else {
								Log.e("FoursquareHelper",
										"Failed to return a 200, "
												+ "meaning there was an error with the call");
							}
						} else {
							Log.e("FoursquareHelper",
									"Failed to parse the meta section");
						}
					} else {
						Log.e("FoursquareHelper",
								"Failed to parse main response");
					}
				} else {
					Log.e("FoursquareHelper", "Problem fetching the data");
				}

				Log.e("FoursquareHelper", "Failed for some other reason...");
				return null;
			} catch (Exception e) {
				Log.e("FoursquareHelper",
						"GetClosestVenuesWithLatLng: " + e.getMessage());
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

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

				String mySuperuserLevel = PreferencesHelper
						.getFoursquareSuperuserLevel(myContext);
				Integer superuserLevel;

				try {
					superuserLevel = Integer.parseInt(mySuperuserLevel);
				} catch (NumberFormatException e) {
					superuserLevel = 0;
				}

				canEdit = superuserLevel > 0;

				String venueResult = VenueEndpoint.EditVenue(myVenueId,
						myAccessToken, myClientId, myClientSecret,
						myModifiedVenue, superuserLevel, modifiedDescription,
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
					if (result.equals(FoursquareHelper.SUCCESS)) {
						Toast.makeText(
								myCaller.getApplicationContext(),
								canEdit ? myContext
										.getString(R.string.edit_venue_success)
										: myContext
												.getString(R.string.edit_venue_success_propose),
								Toast.LENGTH_SHORT).show();
						myCaller.setResult(Activity.RESULT_OK);
					} else if (result
							.equals(FoursquareHelper.FAIL_UNAUTHORIZED)) {
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
								FoursquareHelper.FOURSQUARE_BASE_URL,
								String.format(
										FoursquareHelper.FOURSQUARE_VENUE_ENDPOINT,
										myVenueId)
										+ (canEdit ? FoursquareHelper.FOURSQUARE_VENUE_EDIT_SUFFIX
												: FoursquareHelper.FOURSQUARE_VENUE_PROPOSED_EDIT_SUFFIX),
								accessToken, FoursquareHelper.CURRENT_API_DATE);
			} else {
				venueRequestUrl = String
						.format("%s%s?client_id=%s&client_secret=%s&v=%s",
								FoursquareHelper.FOURSQUARE_BASE_URL,
								String.format(
										FoursquareHelper.FOURSQUARE_VENUE_ENDPOINT,
										myVenueId)
										+ (canEdit ? FoursquareHelper.FOURSQUARE_VENUE_EDIT_SUFFIX
												: FoursquareHelper.FOURSQUARE_VENUE_PROPOSED_EDIT_SUFFIX),
								clientId, clientSecret,
								FoursquareHelper.CURRENT_API_DATE);
			}

			if (fromAddCategory && myModifiedVenue.getCategories() != null) {
				if (canEdit) {
					String myModifiedCategories = "";
					for (Category c : myModifiedVenue.getCategories()) {
						myModifiedCategories += c.getId() + ",";
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
							Util.Encode(myModifiedVenue.getCategories().get(0)
									.getId()));
				}
			} else {
				// Add the editable values
				venueRequestUrl += String
						.format("&name=%s&address=%s&crossStreet=%s&city=%s&state=%s&zip=%s&phone=%s",
								Util.Encode(myModifiedVenue.getName()), Util
										.Encode(myModifiedVenue.getLocation()
												.getAddress()), Util
										.Encode(myModifiedVenue.getLocation()
												.getCrossStreet()), Util
										.Encode(myModifiedVenue.getLocation()
												.getCity()), Util
										.Encode(myModifiedVenue.getLocation()
												.getState()), Util
										.Encode(myModifiedVenue.getLocation()
												.getPostalCode()), Util
										.Encode(myModifiedVenue.getContact()
												.getPhone()));

				if (myModifiedVenue.getVenueHours() != null
						&& myModifiedVenue.getVenueHours().getTimeFrames() != null) {
					StringBuilder venueHours = new StringBuilder();
					for (TimeFrame t : myModifiedVenue.getVenueHours()
							.getTimeFrames()) {
						venueHours.append(t.getFoursquareApiString());
					}
					if (venueHours.length() > 0) {
						venueRequestUrl += String.format("&hours=%s",
								venueHours.toString());
					}
				}

				if (level >= 2) {
					venueRequestUrl += String.format("&twitter=%s&url=%s", Util
							.Encode(myModifiedVenue.getContact().getTwitter()),
							Util.Encode(myModifiedVenue.getUrl()));

					if (modifiedDescription)
						venueRequestUrl += String.format("&description=%s",
								Util.Encode(myModifiedVenue.getDescription()));

					venueRequestUrl += String.format("&ll=%s,%s",
							myModifiedVenue.getLocation().getLatitude(),
							myModifiedVenue.getLocation().getLongitude());
				}
			}

			if (SEND_DEBUG_EMAIL)
				tempDebugString += String.format("Url: %s", venueRequestUrl);

			if (Debug.isDebuggerConnected())
				Log.i("FoursquareHelper", "Edit Request Url: "
						+ venueRequestUrl);

			String jsonEditVenueRequestResponse = Util.getHttpResponse(
					venueRequestUrl, true, "", "");

			if (SEND_DEBUG_EMAIL)
				tempDebugString += String.format(" Response: %s",
						jsonEditVenueRequestResponse);

			try {
				if (jsonEditVenueRequestResponse != null) {
					if (Debug.isDebuggerConnected())
						Log.i("FoursquareHelper",
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
									return FoursquareHelper.SUCCESS;
								}
							} else if (responseCode == 403) {
								if (SEND_DEBUG_EMAIL) {
									return tempDebugString;
								} else {
									return FoursquareHelper.FAIL_UNAUTHORIZED;
								}
							} else {
								Log.e("FoursquareHelper",
										"Failed to return a 200, "
												+ "meaning there was an error with the call");

								if (SEND_DEBUG_EMAIL) {
									return tempDebugString;
								} else {
									return FoursquareHelper.FAIL;
								}
							}
						} else {
							Log.e("FoursquareHelper",
									"Failed to parse the meta section");
						}
					} else {
						Log.e("FoursquareHelper",
								"Failed to parse main response");
					}
				} else {
					Log.e("FoursquareHelper", "Problem fetching the data");
				}

				Log.e("FoursquareHelper", "Failed for some other reason...");
				if (SEND_DEBUG_EMAIL) {
					return tempDebugString;
				} else {
					return FoursquareHelper.FAIL;
				}
			} catch (Exception e) {
				Log.e("FoursquareHelper", "EditVenue: " + e.getMessage());
				if (SEND_DEBUG_EMAIL) {
					return tempDebugString;
				} else {
					return FoursquareHelper.FAIL;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (SEND_DEBUG_EMAIL) {
				return tempDebugString;
			} else {
				return FoursquareHelper.FAIL;
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
								FoursquareHelper.FOURSQUARE_BASE_URL,
								String.format(
										FoursquareHelper.FOURSQUARE_VENUE_ENDPOINT,
										myVenueId)
										+ (canEdit ? FoursquareHelper.FOURSQUARE_VENUE_EDIT_SUFFIX
												: FoursquareHelper.FOURSQUARE_VENUE_PROPOSED_EDIT_SUFFIX),
								accessToken, FoursquareHelper.CURRENT_API_DATE);
			} else {
				venueRequestUrl = String
						.format("%s%s?client_id=%s&client_secret=%s&v=%s",
								FoursquareHelper.FOURSQUARE_BASE_URL,
								String.format(
										FoursquareHelper.FOURSQUARE_VENUE_ENDPOINT,
										myVenueId)
										+ (canEdit ? FoursquareHelper.FOURSQUARE_VENUE_EDIT_SUFFIX
												: FoursquareHelper.FOURSQUARE_VENUE_PROPOSED_EDIT_SUFFIX),
								clientId, clientSecret,
								FoursquareHelper.CURRENT_API_DATE);
			}
			if (Debug.isDebuggerConnected())
				Log.i("FoursquareHelper", "Base Url: " + venueRequestUrl);

			String listOfCategoryIds = null;

			for (Category c : myCategories) {
				if (c.getPrimary())
					listOfCategoryIds = c.getId() + listOfCategoryIds + ",";
				else
					listOfCategoryIds += c.getId() + ",";
			}

			listOfCategoryIds.subSequence(0, listOfCategoryIds.length() - 1);

			// Add the editable values
			venueRequestUrl += String.format("&categoryId=%s",
					listOfCategoryIds);

			if (Debug.isDebuggerConnected())
				Log.i("FoursquareHelper", "Edit Venue Categories Request Url: "
						+ venueRequestUrl);

			String jsonEditVenueRequestResponse = Util.getHttpResponse(
					venueRequestUrl, true, "", "");

			try {
				if (jsonEditVenueRequestResponse != null) {
					if (Debug.isDebuggerConnected())
						Log.i("FoursquareHelper",
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
									return FoursquareHelper.SUCCESS;
								} else {
									Log.e("FoursquareHelper",
											"Failed to parse the response json");
								}
							} else if (responseCode == 403) {
								return FoursquareHelper.FAIL_UNAUTHORIZED;
							} else {
								Log.e("FoursquareHelper",
										"Failed to return a 200, "
												+ "meaning there was an error with the call");
							}
						} else {
							Log.e("FoursquareHelper",
									"Failed to parse the meta section");
						}
					} else {
						Log.e("FoursquareHelper",
								"Failed to parse main response");
					}
				} else {
					Log.e("FoursquareHelper", "Problem fetching the data");
				}

				Log.e("FoursquareHelper", "Failed for some other reason...");
				return FoursquareHelper.FAIL;
			} catch (Exception e) {
				Log.e("FoursquareHelper",
						"GetClosestVenuesWithLatLng: " + e.getMessage());
				return FoursquareHelper.FAIL;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return FoursquareHelper.FAIL;
		}
	}

	public static List<Category> GetCategories(String accessToken,
			String clientId, String clientSecret) {
		List<Category> myFoursquareCategories;

		try {
			String venueRequestUrl;
			if (accessToken != null && accessToken.length() > 0) {
				venueRequestUrl = String.format("%s%s?oauth_token=%s&v=%s",
						FoursquareHelper.FOURSQUARE_BASE_URL,
						FoursquareHelper.FOURSQUARE_VENUE_CATEGORIES,
						accessToken, FoursquareHelper.CURRENT_API_DATE);
			} else {
				venueRequestUrl = String.format(
						"%s%s?client_id=%s&client_secret=%s&v=%s",
						FoursquareHelper.FOURSQUARE_BASE_URL,
						FoursquareHelper.FOURSQUARE_VENUE_CATEGORIES, clientId,
						clientSecret, FoursquareHelper.CURRENT_API_DATE);
			}

			if (Debug.isDebuggerConnected())
				Log.i("FoursquareHelper", "Get Categories Url: "
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
										Log.e("FoursquareHelper",
												"Failed to parse the venues json");
									}
								} else {
									Log.e("FoursquareHelper",
											"Failed to parse the response json");
								}
							} else {
								Log.e("FoursquareHelper",
										"Failed to return a 200, "
												+ "meaning there was an error with the call");
							}
						} else {
							Log.e("FoursquareHelper",
									"Failed to parse the meta section");
						}
					} else {
						Log.e("FoursquareHelper",
								"Failed to parse main response");
					}
				} else {
					Log.e("FoursquareHelper", "Problem fetching the data");
				}

				Log.e("FoursquareHelper", "Failed for some other reason...");
				return null;
			} catch (Exception e) {
				Log.e("FoursquareHelper",
						"GetClosestVenuesWithLatLng: " + e.getMessage());
				return null;
			}

		} catch (Exception e) {
			Log.e("FoursquareHelper",
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
			if (myFlagType.equals(FoursquareHelper.FlagType.DUPLICATE)) {
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
				if (result.equals(FoursquareHelper.SUCCESS)) {
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
								FoursquareHelper.FOURSQUARE_BASE_URL,
								String.format(
										FoursquareHelper.FOURSQUARE_VENUE_ENDPOINT,
										myVenueId)
										+ FoursquareHelper.FOURSQUARE_VENUE_FLAG_SUFFIX,
								accessToken, FoursquareHelper.CURRENT_API_DATE);
			} else {
				venueRequestUrl = String
						.format("%s%s?client_id=%s&client_secret=%s&v=%s",
								FoursquareHelper.FOURSQUARE_BASE_URL,
								String.format(
										FoursquareHelper.FOURSQUARE_VENUE_ENDPOINT,
										myVenueId)
										+ FoursquareHelper.FOURSQUARE_VENUE_FLAG_SUFFIX,
								clientId, clientSecret,
								FoursquareHelper.CURRENT_API_DATE);
			}

			venueRequestUrl += String.format("&problem=%s", Util.Encode(Util
					.GetFlagTypeStringFromInt(myFlagType, false)));

			if (myFlagType.equals(FoursquareHelper.FlagType.DUPLICATE)
					&& !myDuplicateId.equals(""))
				venueRequestUrl += String.format("&venueId=%s", myDuplicateId);

			if (Debug.isDebuggerConnected())
				Log.i("FoursquareHelper", "Flag Venue Request Url: "
						+ venueRequestUrl);

			String jsonEditVenueRequestResponse = Util.getHttpResponse(
					venueRequestUrl, true, "", "");

			try {
				if (jsonEditVenueRequestResponse != null) {
					if (Debug.isDebuggerConnected())
						Log.i("FoursquareHelper",
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
								return FoursquareHelper.SUCCESS;
							} else if (responseCode == 403) {
								return FoursquareHelper.FAIL_UNAUTHORIZED;
							} else {
								Log.e("FoursquareHelper",
										"Failed to return a 200, "
												+ "meaning there was an error with the call");
								return FoursquareHelper.FAIL;
							}
						} else {
							Log.e("FoursquareHelper",
									"Failed to parse the meta section");
						}
					} else {
						Log.e("FoursquareHelper",
								"Failed to parse main response");
					}
				} else {
					Log.e("FoursquareHelper", "Problem fetching the data");
				}

				Log.e("FoursquareHelper", "Failed for some other reason...");
				return FoursquareHelper.FAIL;
			} catch (Exception e) {
				Log.e("FoursquareHelper", "EditVenue: " + e.getMessage());
				return FoursquareHelper.FAIL;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return FoursquareHelper.FAIL;
		}
	}
}