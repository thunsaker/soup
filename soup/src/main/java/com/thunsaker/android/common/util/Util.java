package com.thunsaker.android.common.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Util {
	private static final String LOG_TAG = "Util";
	public static final int FACEBOOK_UPDATE = 0;
	public static final int TWITTER_UPDATE = 1;
	public static final int FOURSQUARE_UPDATE = 2;
	public static final int GOOGLEPLUS_UPDATE = 3;
	public static final int APPDOTNET_POST = 4;

	public static final String ENCODER_CHARSET = "UTF-8";

	public static final int CHAR_LIMIT_FACEBOOK = 1000;
	public static final int CHAR_LIMIT_TWITTER = 140;
	public static final int CHAR_LIMIT_APP_DOT_NET = 256;

	public static String contentType = "json/application";

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

    /*
        // TODO: Decide if I want to try to use this.
        From: http://stackoverflow.com/questions/16007401/android-use-external-profile-image-in-notification-bar-like-facebook
    */
    public Bitmap getBitmapFromURL(String strURL) {
        try {
            URL url = new URL(strURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}