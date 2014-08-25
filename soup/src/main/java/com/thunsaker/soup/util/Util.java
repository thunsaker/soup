package com.thunsaker.soup.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.thunsaker.soup.BuildConfig;
import com.thunsaker.soup.R;
import com.thunsaker.soup.data.api.model.CompactVenue;
import com.thunsaker.soup.data.api.model.Location;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.TimeZone;

/*
 * Created by @thunsaker
 */
public class Util {
	private static final String LOG_TAG = "Util";

	public static final String ENCODER_CHARSET = "UTF-8";
	public static final String SOUP_PACKAGE_NAME = "com.thunsaker.soup";
	public static final String SOUP_PRO_PACKAGE_NAME = "com.thunsaker.soup.pro";

	public static final String REGEX_GPS = "^([-+]?\\d{1,2}([.]\\d+)?),\\s*([-+]?\\d{1,3}([.]\\d+)?)$";

	public static String contentType = "json/application";

	public static String getHttpResponse(String url, String contentType, String accepts) {
		return getHttpResponse(url, false, contentType, accepts);
	}

	public static String getHttpResponse(String url, Boolean isHttpPost, String contentType, String accepts) {
		String result = "";
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(url);
		HttpPost httpPost = new HttpPost(url);
		HttpResponse response;

		try {
			if(isHttpPost)
				response = httpclient.execute(httpPost);
			else
				response = httpclient.execute(httpGet);

			HttpEntity entity = response.getEntity();
			if (entity != null) {
				InputStream instream = entity.getContent();
				result = convertStreamToString(instream);
				instream.close();
			}
		} catch (ClientProtocolException e) {
			Log.e(LOG_TAG, "There was a protocol based error", e);
		} catch (IOException e) {
			Log.e(LOG_TAG, "There was an IO Stream related error", e);
		}
		return result;
	}

	private static String convertStreamToString(InputStream is) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	@SuppressWarnings("static-access")
	public static Boolean HasInternet(Context myContext) {
		Boolean HasConnection = false;
		ConnectivityManager connectivityManager = (ConnectivityManager) myContext
				.getSystemService(myContext.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager
				.getActiveNetworkInfo();
		if (activeNetworkInfo != null) {
			State myState = activeNetworkInfo.getState();
			if (myState == State.CONNECTED || myState == State.CONNECTING) {
				HasConnection = true;
			}
		}
		return HasConnection;
	}

	public static String Encode(String s) {
		try {
			return URLEncoder.encode(s, Util.ENCODER_CHARSET);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String GetFlagTypeStringFromInt(Integer flagType, Boolean pretty) {
		String myFlagTypeString = "";
		switch (flagType) {
		case 0:
			myFlagTypeString = pretty ? "Mislocated" : "mislocated";
			break;
		case 1:
			myFlagTypeString = pretty ? "Doesn't Exist" : "doesnt_exist";
			break;
		case 2:
			myFlagTypeString = pretty ? "Closed" : "closed";
			break;
		case 3:
			myFlagTypeString = pretty ? "Inappropriate" : "inappropriate";
			break;
		case 4:
			myFlagTypeString = pretty ? "Event Over" : "event_over";
			break;
		case 5:
			myFlagTypeString = pretty ? "Duplicate" : "duplicate";
			break;
		}

		return myFlagTypeString;
	}

	public static LatLngBounds GetLatLngBounds(LatLng PointA, LatLng PointB, LatLng PointC) {
		double maxLat;
		double minLat;
		double maxLng;
		double minLng;

		if(PointA.latitude > PointB.latitude) {
			maxLat = PointA.latitude;
			minLat = PointB.latitude;
		} else {
			maxLat = PointB.latitude;
			minLat = PointA.latitude;
		}

		if(PointA.longitude > PointB.longitude) {
			maxLng = PointA.longitude;
			minLng = PointB.longitude;
		} else {
			maxLng = PointB.longitude;
			minLng = PointA.longitude;
		}

		if(PointC != null) {
			if(PointC.latitude > maxLat) {
				maxLat = PointC.latitude;
			} else if(PointC.latitude < minLat) {
				minLat = PointC.latitude;
			}

			if(PointC.longitude > maxLng) {
				maxLng = PointC.longitude;
			} else if(PointC.longitude < minLng) {
				minLat = PointC.longitude;
			}
		}

		LatLng northeast = new LatLng(maxLat, maxLng);
//		Log.i("Util", "Northeast: " + northeast.toString());
		LatLng southwest = new LatLng(minLat, minLng);
//		Log.i("Util", "Southwest: " + southwest.toString());
		return new LatLngBounds(southwest, northeast);
	}

	/*
	public static LatLngBounds GetLatLngBounds(LatLng[] Points) {
		double maxLat;
		return null;
	}
	*/

	public static boolean IsProInstalled(Context myContext){
		PackageManager manager = myContext.getPackageManager();
        if(BuildConfig.DEBUG)
		    return manager.checkSignatures(SOUP_PACKAGE_NAME + ".debug", SOUP_PRO_PACKAGE_NAME + ".debug") == PackageManager.SIGNATURE_MATCH;
        else
		    return manager.checkSignatures(SOUP_PACKAGE_NAME, SOUP_PRO_PACKAGE_NAME) == PackageManager.SIGNATURE_MATCH;
	}

	/**
	 * This method convets dp unit to equivalent device specific value in pixels.
	 *
	 * @param dp A value in dp(Device independent pixels) unit. Which we need to convert into pixels
	 * @param context Context to get resources and device specific display metrics
	 * @return A float value to represent Pixels equivalent to dp according to device
	 *
	 * from: http://stackoverflow.com/a/9563438/339820
	 */
	public static float convertDpToPixel(float dp,Context context){
//      Resources resources = context.getResources();
//	    DisplayMetrics metrics = resources.getDisplayMetrics();
//	    float px = dp * (metrics.densityDpi/160f);
//	    return px;

        Resources r = context.getResources();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
	}

	/**
	 * This method converts device specific pixels to device independent pixels.
	 *
	 * @param px A value in px (pixels) unit. Which we need to convert into db
	 * @param context Context to get resources and device specific display metrics
	 * @return A float value to represent db equivalent to px value
	 *
	 * from: http://stackoverflow.com/a/9563438/339820
	 */
	public static float convertPixelsToDp(float px,Context context){
	    Resources resources = context.getResources();
	    DisplayMetrics metrics = resources.getDisplayMetrics();
	    float dp = px / (metrics.densityDpi / 160f);
	    return dp;
	}

	public static long getCurrentTimeInSeconds() {
    	return Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTimeInMillis() / 1000;
    }

	public static boolean VenueHasProblems(CompactVenue venue) {
		boolean hasProblems = false;
		try {
			if(venue != null) {
				if(venue.location != null) {
					Location myLocation = venue.location;
                    if (myLocation.address == null || myLocation.city == null ||
                            myLocation.state == null || myLocation.postalCode == null ||
                            myLocation.crossStreet == null || myLocation.address.length() == 0 ||
                            myLocation.city.length() == 0 || myLocation.state.length() == 0 ||
                            myLocation.postalCode.length() == 0 || myLocation.crossStreet.length() == 0)
                        hasProblems = true;
				} else {
					hasProblems = true;
				}

				if(venue.contact == null || venue.contact.phone == null || venue.contact.phone.length() == 0)
				    hasProblems = true;

				if(venue.categories == null || venue.categories.size() == 0)
					hasProblems = true;

			} else {
				hasProblems = true;
			}
			return hasProblems;
		} catch (Exception e) {
			e.printStackTrace();
			return true;
		}
	}

	public static String ResolveShortUrl(String shortUrl) {
		try {
			if(shortUrl.contains("4sq.com")) {
				URL myShortUrl = new URL(shortUrl);
				HttpURLConnection urlConnection = (HttpURLConnection) myShortUrl.openConnection();
				urlConnection.setInstanceFollowRedirects(true);
				URL myResolvedUrl = new URL(urlConnection.getHeaderField("Location"));
				return myResolvedUrl.toString();
			} else {
				return shortUrl;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

    public static int GetCategoryColor(Character mChar, Context mContext) {
        int color;
        switch (Character.getNumericValue(mChar) % 5) {
            case 0:
                color = mContext.getResources().getColor(R.color.category_travel);
                break;
            case 1:
                color = mContext.getResources().getColor(R.color.category_art);
                break;
            case 2:
                color = mContext.getResources().getColor(R.color.category_night);
                break;
            case 3:
                color = mContext.getResources().getColor(R.color.category_school);
                break;
            case 4:
                color = mContext.getResources().getColor(R.color.category_food);
                break;
            default:
                color = mContext.getResources().getColor(R.color.category_event);
                break;
        }
        return color;
    }
}