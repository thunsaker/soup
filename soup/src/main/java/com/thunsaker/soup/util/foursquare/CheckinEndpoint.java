package com.thunsaker.soup.util.foursquare;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.thunsaker.soup.FoursquareHelper;
import com.thunsaker.soup.PreferencesHelper;
import com.thunsaker.soup.R;
import com.thunsaker.soup.ui.MainActivity;
import com.thunsaker.soup.util.Util;

import java.net.URLEncoder;

/*
 * Created by @thunsaker
 */
public class CheckinEndpoint {
	public static class PostUserCheckin extends AsyncTask<Void, Integer, Boolean> {
		Context myContext;
		LatLng myCurrentLatLng;
		String myVenueId;
		String myVenueName;
		String myMessage;

		public PostUserCheckin(Context theContext, LatLng theCurrentLatLng, String theFoursquareVenueId, String theVenueName, String theMsg) {
			myContext = theContext;
			myCurrentLatLng = theCurrentLatLng;
			myVenueId = theFoursquareVenueId;
			myVenueName = theVenueName;
			myMessage = theMsg;
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			NotificationManager mNotificationManager =
					(NotificationManager) myContext.getSystemService(Context.NOTIFICATION_SERVICE);

			NotificationCompat.Builder mNotificationFoursquare =
					new NotificationCompat.Builder(myContext)
						.setSmallIcon(R.drawable.ic_stat_soup)
						.setLargeIcon(BitmapFactory.decodeResource(myContext.getResources(), R.drawable.ic_stat_check_white))
						.setProgress(0, 0, true)
						.setContentText(myContext.getString(R.string.notification_checkin_title))
						.setContentTitle(myContext.getString(R.string.notification_checkin_pending))
						.setContentIntent(MainActivity.genericPendingIntent);

			mNotificationManager.notify(MainActivity.NOTIFICATION_CHECKIN, mNotificationFoursquare.build());

			Boolean myResult = false;
			myResult = postUserCheckin(myContext, myCurrentLatLng, myVenueId, myMessage);
			return myResult;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			NotificationManager mNotificationManager =
					(NotificationManager) myContext.getSystemService(Context.NOTIFICATION_SERVICE);

			if(result) {
				NotificationCompat.Builder mNotificationFoursquarePosted =
					new NotificationCompat.Builder(myContext)
						.setSmallIcon(R.drawable.ic_stat_soup)
						.setLargeIcon(BitmapFactory.decodeResource(myContext.getResources(), R.drawable.ic_stat_check_white))
						.setContentText(myContext.getString(R.string.notification_checkin_title))
						.setContentTitle(String.format(myContext.getString(R.string.notification_checkin_complete), myVenueName))
						.setContentIntent(MainActivity.genericPendingIntent);
				mNotificationManager.notify(MainActivity.NOTIFICATION_CHECKIN, mNotificationFoursquarePosted.build());
				mNotificationManager.cancel(MainActivity.NOTIFICATION_CHECKIN);
			} else {
				Intent foursquareVenueIntent = new Intent(Intent.ACTION_VIEW,
						Uri.parse(String.format(FoursquareHelper.FOURSQURE_INTENT_VENUE_URL, myVenueId)));
				TaskStackBuilder foursquareVenueStackBuilder = TaskStackBuilder.create(myContext);
				foursquareVenueStackBuilder.addParentStack(MainActivity.class);
				foursquareVenueStackBuilder.addNextIntent(foursquareVenueIntent);
				PendingIntent foursquareVenuePendingIntent =
						foursquareVenueStackBuilder.getPendingIntent(
				            0,
				            PendingIntent.FLAG_UPDATE_CURRENT
				        );

				Intent retryCheckinIntent = new Intent(myContext, MainActivity.class);
				retryCheckinIntent.putExtra(MainActivity.VENUE_ID_CHECKIN_EXTRA, myVenueId);
				retryCheckinIntent.putExtra(MainActivity.VENUE_NAME_CHECKIN_EXTRA, myVenueName);
				TaskStackBuilder retryCheckinStackBuilder = TaskStackBuilder.create(myContext);
				retryCheckinStackBuilder.addParentStack(MainActivity.class);
				retryCheckinStackBuilder.addNextIntent(retryCheckinIntent);
				PendingIntent retryCheckinPendingIntent =
						retryCheckinStackBuilder.getPendingIntent(
				            0,
				            PendingIntent.FLAG_UPDATE_CURRENT
				        );

				NotificationCompat.Builder mNotificationFoursquareFail =
						new NotificationCompat.Builder(myContext)
							.setSmallIcon(R.drawable.ic_stat_soup)
							.setLargeIcon(BitmapFactory.decodeResource(myContext.getResources(), R.drawable.ic_stat_check_white))
							.setAutoCancel(true)
							.addAction(R.drawable.ic_action_foursquare_holo_dark, myContext.getString(R.string.notification_checkin_foursquare), foursquareVenuePendingIntent)
							.addAction(R.drawable.ic_action_refresh_holo_dark, myContext.getString(R.string.notification_checkin_retry), retryCheckinPendingIntent)
							.setContentIntent(foursquareVenuePendingIntent)
							.setContentText(myContext.getString(R.string.notification_checkin_title))
							.setContentTitle(myContext.getString(R.string.notification_checkin_fail));

				mNotificationManager.notify(MainActivity.NOTIFICATION_CHECKIN, mNotificationFoursquareFail.build());
			}
		}

	}

	public static Boolean postUserCheckin(Context myContext, LatLng myCurrentLatLng, String myVenueId, String myShout) {
		try {
			if(PreferencesHelper.getFoursquareConnected(myContext)) {
				String token = PreferencesHelper.getFoursquareToken(myContext);
				String myUrlEncodedShout = myShout != null && myShout.length() > 0 ? URLEncoder.encode(myShout.trim(), Util.ENCODER_CHARSET) : null;

				String checkinRequestUrl =
						String.format("%s%s?ll=%s,%s&oauth_token=%s&venueId=%s&v=%s",
								FoursquareHelper.FOURSQUARE_BASE_URL,
								FoursquareHelper.FOURSQUARE_CHECKIN_SUFFIX,
								myCurrentLatLng.latitude,
								myCurrentLatLng.longitude,
								token,
								myVenueId,
								FoursquareHelper.CURRENT_API_DATE);
				if(myUrlEncodedShout != null && myUrlEncodedShout.length() > 0) {
					checkinRequestUrl += String.format("&shout=%s", myUrlEncodedShout);
				}

				String jsonCheckinResponse =
						Util.getHttpResponse(checkinRequestUrl, true, "", "");
				Log.e("CheckinEndpoint", "Checkin Url - " + checkinRequestUrl);

				try {
					if(jsonCheckinResponse != null) {
						JsonParser jParser = new JsonParser();
						JsonObject jObject = (JsonObject) jParser.parse(jsonCheckinResponse);

						if(jObject != null) {
							JsonObject jObjectMeta = jObject.getAsJsonObject("meta");
							if(jObjectMeta != null) {
								if(Integer.parseInt(jObjectMeta.get("code").toString()) == 200) {
									return true;
								}
							}
						}
					}
				} catch (Exception e) {
					return false;
				}
			}
			return false;
		} catch (Exception e) {
			return false;
		}
	}
}
