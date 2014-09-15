package com.thunsaker.soup.util.foursquare;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Debug;
import android.util.Log;
import android.widget.GridView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.thunsaker.soup.PreferencesHelper;
import com.thunsaker.soup.R;
import com.thunsaker.soup.app.BaseSoupActivity;
import com.thunsaker.soup.data.api.model.Checkin;
import com.thunsaker.soup.data.api.model.FoursquareList;
import com.thunsaker.soup.services.AuthHelper;
import com.thunsaker.soup.services.foursquare.FoursquarePrefs;
import com.thunsaker.soup.ui.ListsFragment;
import com.thunsaker.soup.util.Util;

import java.util.ArrayList;
import java.util.List;

/*
 * Created by @thunsaker
 */
public class UserEndpoint {
	public static class GetUserInfo extends AsyncTask<Void, Integer, Boolean> {
		Context myContext;
		String myUrl;

		public GetUserInfo(Context theContext, String theUrl) {
			myContext = theContext;
			myUrl = theUrl;
		}

		@Override
		protected Boolean doInBackground(Void... params) {
            String log_tag = "UserEndpoint - GetUserInfo";
			String response = Util.getHttpResponse(myUrl, false, Util.contentType, Util.contentType);
			try {
				if (response != null) {
					JsonParser jParser = new JsonParser();
					JsonObject jObject = (JsonObject) jParser.parse(response);

					if(jObject != null) {
						JsonObject jObjectMeta = jObject.getAsJsonObject("meta");
						if(jObjectMeta != null) {
							if(Integer.parseInt(jObjectMeta.get("code").toString()) == 200) {
								JsonObject jObjectResponses = jObject.getAsJsonObject("response");
								if(jObjectResponses != null) {
									JsonObject jObjectUser = jObjectResponses.getAsJsonObject("user");
									if(jObjectUser != null) {
										String userId = jObjectUser.get("id").getAsString();
										if(!userId.equals(""))
											PreferencesHelper.setFoursquareUserId(myContext, userId);

										Integer superuserLevel = jObjectUser.get("superuser") != null ? jObjectUser.get("superuser").getAsInt() : 0;
										PreferencesHelper.setFoursquareSuperuserLevel(myContext, superuserLevel);
										return true;
									} else {
										Log.e(log_tag, "Failed to parse the user json");
									}
								} else {
									Log.e(log_tag, "Failed to parse the response json");
								}
							} else {
								Log.e(log_tag, "Failed to return a 200, meaning there was an error with the call");
							}
						} else {
							Log.e(log_tag, "Failed to parse the meta section");
						}
					} else {
						Log.e(log_tag, "Failed to parse main response");
					}
				} else {
					Log.e(log_tag, "Problem fetching the data");
				}

				Log.e(log_tag, "Failed for some other reason...");
				return null;
			} catch(Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
		}
	}

    @Deprecated
	public static class GetCheckins extends AsyncTask<Void, Integer, List<Checkin>> {
		Context myContext;
        BaseSoupActivity myCaller;

		long myStartTimestamp;
		long myEndTimestamp;
		Integer myLimit;
		Integer myOffset;
		String mySortOrder;
		Integer myHistoryView;

		String myAccessToken;
		String myClientId;
		String myClientSecret;

		public GetCheckins(Context theContext, BaseSoupActivity theCaller, long theStartTimestamp, long theEndTimestamp, Integer theLimit, Integer theOffset, String theSortOrder, Integer theHistoryView) {
			myContext = theContext;
			myCaller = theCaller;

			myStartTimestamp = theStartTimestamp;
			myEndTimestamp = theEndTimestamp;
			myLimit = theLimit;
			myOffset = theOffset;
			mySortOrder = theSortOrder;
			myHistoryView = theHistoryView;
		}

		@Override
		protected void onPreExecute() {
			myCaller.setProgressBarVisibility(true);
			super.onPreExecute();
		}

		@Override
		protected List<Checkin> doInBackground(Void... params) {
			try {
				myAccessToken = PreferencesHelper.getFoursquareToken(myContext) != "" ? PreferencesHelper.getFoursquareToken(myContext) : "";
				myClientId = AuthHelper.FOURSQUARE_CLIENT_ID;
				myClientSecret = AuthHelper.FOURSQUARE_CLIENT_SECRET;

				List<Checkin> myCheckinsResult = UserEndpoint.GetCheckins(myAccessToken, myClientId, myClientSecret, myStartTimestamp, myEndTimestamp, myLimit, myOffset, mySortOrder);
				return myCheckinsResult != null ? myCheckinsResult : null;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

//		@Override
//		protected void onPostExecute(List<Checkin> result) {
//			super.onPostExecute(result);
//			myCaller.setProgressBarVisibility(false);
//
//			try {
//				if(result != null) {
//					switch (myHistoryView) {
//					case 1: // FoursquarePrefs.History.View.LAST_WEEK
//						HistoryActivity.historyListLastWeek = result;
//						HistoryActivity.SetupHistoryView(myCaller);
//						break;
//					case 2: // FoursquarePrefs.History.View.LAST_MONTH
//						HistoryActivity.historyListLastMonth = result;
//						// TODO: Add the adapter
//						HistoryActivity.SetupHistoryView(myCaller);
//						break;
//					default: // FoursquarePrefs.History.View.TODAY
//						HistoryActivity.historyListToday = result;
//						HistoryActivity.SetupHistoryView(myCaller);
//						break;
//					}
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
	}

    @Deprecated
	public static List<Checkin> GetCheckins(String accessToken, String clientId, String clientSecret, long startTimestamp, long endTimestamp, Integer limit, Integer offset, String sortOrder) {
		List<Checkin> myCheckins = new ArrayList<Checkin>();
		try {
			String checkinsRequestUrl;
			if(accessToken != null && accessToken.length() > 0) {
				checkinsRequestUrl = String.format("%s%s?oauth_token=%s&beforeTimestamp=%s&afterTimestamp=%s&sort=%s&v=%s",
					FoursquarePrefs.FOURSQUARE_BASE_URL,
					FoursquarePrefs.FOURSQUARE_USER_ENDPOINT + FoursquarePrefs.FOURSQUARE_USER_SELF_SUFFIX + FoursquarePrefs.FOURSQUARE_USER_CHECKINS_SUFFIX,
					accessToken,
					endTimestamp,
					startTimestamp,
					sortOrder,
					FoursquarePrefs.CURRENT_API_DATE);
			} else {
				checkinsRequestUrl = String.format("%s%s?client_id=%s&client_secret=%s&beforeTimestamp=%s&afterTimestamp=%s&sort=%s&v=%s",
					FoursquarePrefs.FOURSQUARE_BASE_URL,
					FoursquarePrefs.FOURSQUARE_USER_ENDPOINT + FoursquarePrefs.FOURSQUARE_USER_SELF_SUFFIX + FoursquarePrefs.FOURSQUARE_USER_CHECKINS_SUFFIX,
					clientId,
					clientSecret,
					endTimestamp,
					startTimestamp,
					sortOrder,
					FoursquarePrefs.CURRENT_API_DATE);
			}

			checkinsRequestUrl += String.format("&limit=%s", limit > 0 ? limit : FoursquarePrefs.History.Limit.MAX);

			if(offset > 0)
				checkinsRequestUrl += String.format("&offset=%s", offset);

			if(Debug.isDebuggerConnected())
				Log.i("UserEndpoint", "Url: " + checkinsRequestUrl);

			String jsonCheckinsRequestResponse = Util.getHttpResponse(
                    checkinsRequestUrl,
                    "", "");

//			Log.i("UserEndpoint", "Response: " + jsonCheckinsRequestResponse);

            String log_tag = "UserEndpoint - GetCheckins";

			try {
				if(jsonCheckinsRequestResponse != null) {
					JsonParser jParser = new JsonParser();
					JsonObject jObject = (JsonObject) jParser.parse(jsonCheckinsRequestResponse);

					if(jObject != null) {
						JsonObject jObjectMeta = jObject.getAsJsonObject("meta");
						if(jObjectMeta != null) {
							if(Integer.parseInt(jObjectMeta.get("code").toString()) == 200) {
								JsonObject jObjectResponses = jObject.getAsJsonObject("response");
								if(jObjectResponses != null) {
									JsonObject jObjectCheckins = jObjectResponses.getAsJsonObject("checkins");
									if(jObjectCheckins != null) {
										JsonArray jArrayCheckinItems = jObjectCheckins.getAsJsonArray("items");
										if(jArrayCheckinItems != null) {
											for (JsonElement checkinJsonElement : jArrayCheckinItems) {
												Checkin myParsedCheckin = Checkin.GetCheckinFromJson(checkinJsonElement.getAsJsonObject());
												if(myParsedCheckin != null)
													myCheckins.add(myParsedCheckin);
											}
											return myCheckins;
										} else {
											Log.e(log_tag, "Failed to parse the items json");
										}
									} else {
										Log.e(log_tag, "Failed to parse the venue json");
									}
								} else {
									Log.e(log_tag, "Failed to parse the response json");
								}
							} else {
								Log.e(log_tag, "Failed to return a 200, meaning there was an error with the call");
							}
						} else {
							Log.e(log_tag, "Failed to parse the meta section");
						}
					} else {
						Log.e(log_tag, "Failed to parse main response");
					}
				} else {
					Log.e(log_tag, "Problem fetching the data");
				}

				Log.e(log_tag, "Failed for some other reason...");
				return null;
			} catch (Exception e) {
				Log.e(log_tag, "GetCheckins: " + e.getMessage());
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static class GetLists extends AsyncTask<Void, Integer, List<FoursquareList>> {
		Context myContext;
        ListsFragment myCaller;
        GridView myGridView;
        String myListType;

		String myAccessToken;
		String myClientId;
		String myClientSecret;

		public GetLists(Context theContext, ListsFragment theCaller, GridView theGridView, String theListType) {
			myContext = theContext;
			myCaller = theCaller;
			myGridView = theGridView;
			myListType = theListType;
		}

		@Override
		protected void onPreExecute() {
			myCaller.getActivity().setProgressBarVisibility(true);
			super.onPreExecute();
		}

		@Override
		protected List<FoursquareList> doInBackground(Void... params) {
			try {
				myAccessToken = !PreferencesHelper.getFoursquareToken(myContext).equals("") ? PreferencesHelper.getFoursquareToken(myContext) : "";
				myClientId = AuthHelper.FOURSQUARE_CLIENT_ID;
				myClientSecret = AuthHelper.FOURSQUARE_CLIENT_SECRET;

				List<FoursquareList> myListsResult = UserEndpoint.GetLists(myAccessToken, myClientId, myClientSecret);
				return myListsResult != null ? myListsResult : null;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		@Override
		protected void onPostExecute(List<FoursquareList> result) {
			if(myCaller.isVisible()) {
				myCaller.getActivity().setProgressBarVisibility(false);

				try {
					if(result != null) {
						ListsFragment.currentListsList = result;
						ListsFragment.mGridViewLists.setAdapter(myCaller.new FoursquareListAdapter(myContext, R.layout.list_lists_item, ListsFragment.currentListsList));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			super.onPostExecute(result);
		}
	}

	public static List<FoursquareList> GetListsByGroup(String accessToken, String clientId, String clientSecret, String listType) {
		try {
			String listsRequestUrl;
			if (accessToken != null && accessToken.length() > 0) {
				listsRequestUrl = String
						.format("%s%s?oauth_token=%s&group=%s&v=%s",
								FoursquarePrefs.FOURSQUARE_BASE_URL,
								FoursquarePrefs.FOURSQUARE_USER_ENDPOINT
										+ FoursquarePrefs.FOURSQUARE_USER_SELF_SUFFIX
										+ FoursquarePrefs.FOURSQUARE_USER_LISTS_SUFFIX,
								accessToken,
								listType,
								FoursquarePrefs.CURRENT_API_DATE);
			} else
				return null;

			if(Debug.isDebuggerConnected())
				Log.i("UserEndpoint - GetLists", "Url: " + listsRequestUrl);

			String jsonListsRequestResponse = Util.getHttpResponse(listsRequestUrl,"", "");

			try {
				if(jsonListsRequestResponse != null) {
					JsonParser jParser = new JsonParser();
					JsonObject jObject = (JsonObject) jParser.parse(jsonListsRequestResponse);

					return ParseListFromJson(jObject, listType);
				} else {
					Log.e("UserEndpoint - GetLists", "Problem fetching the data");
				}

				Log.e("UserEndpoint - GetLists", "Failed for some other reason...");
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

	public static List<FoursquareList> GetLists(String accessToken, String clientId, String clientSecret) {
		List<FoursquareList> myLists = new ArrayList<FoursquareList>();
		try {
			String listsRequestUrl;
			if (accessToken != null && accessToken.length() > 0) {
				listsRequestUrl = String
						.format("https://api.foursquare.com/v2/multi?oauth_token=%s&v=%s&requests=/users/self/lists?group=created,/users/self/lists?group=followed",
								accessToken,
								FoursquarePrefs.CURRENT_API_DATE);
			} else
				return null;

			if(Debug.isDebuggerConnected())
				Log.i("UserEndpoint - GetLists", "Url: " + listsRequestUrl);

			String jsonListsRequestResponse = Util.getHttpResponse(listsRequestUrl,"", "");

			try {
				if(jsonListsRequestResponse != null) {
					JsonParser jParser = new JsonParser();
					JsonObject jObject = (JsonObject) jParser.parse(jsonListsRequestResponse);

					if (jObject != null) {
						JsonObject jObjectMeta = jObject
								.getAsJsonObject("meta");
						if (jObjectMeta != null) {
							if (Integer.parseInt(jObjectMeta.get("code")
									.toString()) == 200) {
								JsonObject jObjectResponses = jObject
										.getAsJsonObject("response");
								if (jObjectResponses != null) {
									JsonArray jArrayResponses = jObjectResponses.getAsJsonArray("responses");

									if(jArrayResponses != null) {
										if(jArrayResponses.get(0) != null)
											myLists.addAll(
                                                    ParseListFromJson(
                                                            jArrayResponses.get(0).getAsJsonObject().getAsJsonObject(),
                                                            FoursquarePrefs.FOURSQUARE_LISTS_GROUP_CREATED));
										if(jArrayResponses.get(1) != null)
											myLists.addAll(
                                                    ParseListFromJson(
                                                            jArrayResponses.get(1).getAsJsonObject().getAsJsonObject(),
                                                            FoursquarePrefs.FOURSQUARE_LISTS_GROUP_FOLLOWED));
										return myLists;
									} else {
										Log.e("UserEndpoint - GetLists",
												"Failed to parse the responses json array");
									}
								} else {
									Log.e("UserEndpoint - GetLists",
											"Failed to parse the response json");
								}
							} else {
								Log.e("UserEndpoint - GetLists",
										"Failed to return a 200, meaning there was an error with the call");
							}
						} else {
							Log.e("UserEndpoint - GetLists",
									"Failed to parse the meta section");
						}
					} else {
						Log.e("UserEndpoint - GetLists",
								"Failed to parse main response");
					}
				} else {
					Log.e("UserEndpoint - GetLists", "Problem fetching the data");
				}

				Log.e("UserEndpoint - GetLists", "Failed for some other reason...");
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

	private static List<FoursquareList> ParseListFromJson(JsonObject jObject, String listType) {
		List<FoursquareList> myLists = new ArrayList<FoursquareList>();
        String log_tag = "UserEndpoint - ParseListFromJson";
		if (jObject != null) {
			JsonObject jObjectMeta = jObject
					.getAsJsonObject("meta");
			if (jObjectMeta != null) {
				if (Integer.parseInt(jObjectMeta.get("code")
						.toString()) == 200) {
					JsonObject jObjectResponses = jObject
							.getAsJsonObject("response");
					if (jObjectResponses != null) {
						JsonObject jObjectLists = jObjectResponses
								.getAsJsonObject("lists");
						if (jObjectLists != null) {
                            if (jObjectLists.get("count")
                                    .getAsInt() > 0) {
                                JsonArray jArrayListsItem = jObjectLists.getAsJsonArray("items");
                                for (JsonElement jsonListElement : jArrayListsItem) {
                                    FoursquareList myParsedList = FoursquareList
                                            .GetListFromJson(jsonListElement.getAsJsonObject());
                                    if (myParsedList != null)
                                        myLists.add(myParsedList);
                                }

                                return myLists;
                            } else {
                                Log.e(log_tag, "No lists...");
                                return null;
                            }
                        } else {
							Log.e("UserEndpoint - ParseListFromJson",
									"Failed to parse the lists json");
						}
					} else {
						Log.e("UserEndpoint - ParseListFromJson",
								"Failed to parse the response json");
					}
				} else {
					Log.e("UserEndpoint - ParseListFromJson",
							"Failed to return a 200, meaning there was an error with the call");
				}
			} else {
				Log.e("UserEndpoint - ParseListFromJson",
						"Failed to parse the meta section");
			}
		} else {
			Log.e("UserEndpoint - ParseListFromJson",
					"Failed to parse main response");
		}
		return null;
	}
}