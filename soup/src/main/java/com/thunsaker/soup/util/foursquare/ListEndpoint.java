package com.thunsaker.soup.util.foursquare;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Debug;
import android.util.Log;
import android.view.View;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.thunsaker.soup.PreferencesHelper;
import com.thunsaker.soup.R;
import com.thunsaker.soup.data.api.model.FoursquareImage;
import com.thunsaker.soup.data.api.model.FoursquareList;
import com.thunsaker.soup.services.AuthHelper;
import com.thunsaker.soup.services.foursquare.FoursquarePrefs;
import com.thunsaker.soup.ui.ListFragment;
import com.thunsaker.soup.util.Util;

/*
 * Created by @thunsaker
 */
public class ListEndpoint {
	public static class GetList extends
			AsyncTask<Void, Integer, FoursquareList> {
		Context myContext;
		ListFragment myCaller;
		String myListId;

		String myAccessToken;
		String myClientId;
		String myClientSecret;

		public GetList(Context theContext, ListFragment theCaller,
				String theListId) {
			myContext = theContext;
			myCaller = theCaller;
			myListId = theListId;
		}

		@Override
		protected void onPreExecute() {
			myCaller.getActivity().setProgressBarVisibility(true);
			super.onPreExecute();
		}

		@Override
		protected FoursquareList doInBackground(Void... params) {
			try {
				myAccessToken = PreferencesHelper.getFoursquareToken(myContext) != "" ? PreferencesHelper
						.getFoursquareToken(myContext) : "";
				myClientId = AuthHelper.FOURSQUARE_CLIENT_ID;
				myClientSecret = AuthHelper.FOURSQUARE_CLIENT_SECRET;

				FoursquareList myListResult = ListEndpoint.GetList(
						myAccessToken, myClientId, myClientSecret, myListId);
				return myListResult != null ? myListResult : null;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		@Override
		protected void onPostExecute(FoursquareList result) {
			super.onPostExecute(result);
			if (myCaller.isVisible()) {
				myCaller.getActivity().setProgressBarVisibility(false);
			} else
				return;

			try {
				if (result != null) {
					ListFragment.currentList = result;
					ListFragment.currentListItems = result.getItems();
					myCaller.setListAdapter(myCaller.new FoursquareListItemsAdapter(
							myContext, R.layout.list_lists_item,
							ListFragment.currentListItems));
					ListFragment.currentListItemsAdapter.notifyDataSetChanged();

					if (result.getUrl().contains("/todos")) {
						ListFragment.mImageViewHeaderPhoto
								.setImageDrawable(myContext
										.getResources()
										.getDrawable(
												R.drawable.list_placeholder_todo_header));
						ListFragment.mImageViewHeaderProfile
								.setVisibility(View.GONE);
						ListFragment.mTextViewHeaderCreator
								.setVisibility(View.GONE);
					} else {
						if (result.getPhoto() != null
								&& result.getPhoto().getFoursquareImageUrl() != null) {
							UrlImageViewHelper
									.setUrlDrawable(
											ListFragment.mImageViewHeaderPhoto,
											result.getPhoto()
													.getFoursquareImageUrl(),
											R.drawable.list_placeholder_gray_dark_small);
						} else {
							ListFragment.mImageViewHeaderPhoto
									.setImageDrawable(myContext
											.getResources()
											.getDrawable(
													R.drawable.list_placeholder_orange));
						}
						if (!result.getType().equals(FoursquarePrefs.FOURSQUARE_LISTS_GROUP_CREATED)) {
							if (result.getUser() != null) {
								if (result.getUser().photo != null
										&& result.getUser().photo.getFoursquareImageUrl(FoursquareImage.SIZE_EXTRA_GRANDE) != null) {
									UrlImageViewHelper
											.setUrlDrawable(
													ListFragment.mImageViewHeaderProfile,
													result.getUser().photo
															.getFoursquareImageUrl(
																	FoursquareImage.SIZE_EXTRA_GRANDE),
													R.drawable.list_placeholder_gray_dark_small);
								} else {
									if (result.getUser().type.equals("page")) {
										ListFragment.mImageViewHeaderProfile
												.setImageDrawable(myContext
														.getResources()
														.getDrawable(
																R.drawable.list_placeholder_gray_dark_small));
									} else {
										ListFragment.mImageViewHeaderProfile
												.setImageDrawable(myContext
														.getResources()
														.getDrawable(
																result.getUser()
																		.gender != null
																		&& result
																				.getUser()
																				.gender
																				.equals("male") ? R.drawable.profile_boy
																		: R.drawable.profile_girl));
									}
								}

								if (result.getUser().firstName != null) {
									Boolean isPerson = result.getUser().type != null &&
											(result.getUser().type.equals("page")
											|| result.getUser().type.equals("chain")
											|| result.getUser().type.equals("celebrity")
											|| result.getUser().type.equals("venuePage"))
											? false
											: true;
									String creator = !isPerson ? result.getUser().firstName
											: result.getUser().firstName + " " + result.getUser().lastName;
									ListFragment.mTextViewHeaderCreator
											.setText(String.format(
													myContext.getString(R.string.lists_title_header_creator), creator));
								}
							} else {

								ListFragment.mImageViewHeaderProfile
										.setVisibility(View.GONE);
								ListFragment.mTextViewHeaderCreator
										.setVisibility(View.GONE);
							}
						} else {
							ListFragment.mImageViewHeaderProfile
									.setVisibility(View.GONE);
							ListFragment.mTextViewHeaderCreator
									.setVisibility(View.GONE);
						}
					}

					if (result.getName() != null) {
						myCaller.getActivity().setTitle(result.getName());
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static FoursquareList GetList(String accessToken, String clientId,
			String clientSecret, String listId) {
		FoursquareList myList = new FoursquareList();
		try {
			String listRequestUrl;
			if (accessToken != null && accessToken.length() > 0) {
				listRequestUrl = String.format("%s%s/%s?oauth_token=%s&v=%s",
						FoursquarePrefs.FOURSQUARE_BASE_URL,
						FoursquarePrefs.FOURSQUARE_LISTS_ENDPOINT, listId,
						accessToken, FoursquarePrefs.CURRENT_API_DATE);
			} else
				return null;

			if (Debug.isDebuggerConnected())
				Log.i("UserEndpoint - GetLists", "Url: " + listRequestUrl);

			String jsonListRequestResponse = Util.getHttpResponse(
					listRequestUrl, "", "");

			try {
				if (jsonListRequestResponse != null) {
					JsonParser jParser = new JsonParser();
					JsonObject jObject = (JsonObject) jParser
							.parse(jsonListRequestResponse);

					if (jObject != null) {
						JsonObject jObjectMeta = jObject
								.getAsJsonObject("meta");
						if (jObjectMeta != null) {
							if (Integer.parseInt(jObjectMeta.get("code")
									.toString()) == 200) {
								JsonObject jObjectResponses = jObject
										.getAsJsonObject("response");
								if (jObjectResponses != null) {
									JsonObject jObjectList = jObjectResponses
											.getAsJsonObject("list");
									if (jObjectList != null) {
										myList = FoursquareList
												.GetListFromJson(jObjectList,
														null);
										if (myList != null) {
											return myList;
										} else {
											Log.e("UserEndpoint - GetList",
													"Failed to parse the lists json");
										}
									} else {
										Log.e("UserEndpoint - GetList",
												"Failed to parse the lists json");
									}
								} else {
									Log.e("UserEndpoint - GetList",
											"Failed to parse the response json");
								}
							} else {
								Log.e("UserEndpoint - GetList",
										"Failed to return a 200, meaning there was an error with the call");
							}
						} else {
							Log.e("UserEndpoint - GetList",
									"Failed to parse the meta section");
						}
					} else {
						Log.e("UserEndpoint - GetList",
								"Failed to parse main response");
					}
				} else {
					Log.e("UserEndpoint - GetList", "Problem fetching the data");
				}

				Log.e("UserEndpoint - GetLists",
						"Failed for some other reason...");
				return null;
			} catch (Exception e) {
				Log.e("UserEndpoint - GetLists", e.getMessage());
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}