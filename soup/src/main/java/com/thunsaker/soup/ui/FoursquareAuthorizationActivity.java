package com.thunsaker.soup.ui;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.thunsaker.soup.R;
import com.thunsaker.soup.PreferencesHelper;
import com.thunsaker.soup.app.BaseSoupActivity;
import com.thunsaker.soup.data.FoursquareClient;
import com.thunsaker.soup.util.QueryStringParser;
import com.thunsaker.soup.util.Util;

/*
 * Created by @thunsaker
 */
public class FoursquareAuthorizationActivity extends BaseSoupActivity {
	final String TAG = "FoursquareAuthorizationActivity";

    public static final String ACCESS_URL = "https://foursquare.com/oauth2/access_token";
	public static final String AUTHORIZE_URL = "https://foursquare.com/oauth2/authorize";

	public static FoursquareClient mFoursquareClient;

	public ProgressDialog loadingDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if(mFoursquareClient != null) {
			ActionBar ab = getSupportActionBar();
            boolean showHomeUp = true;
            ab.setDisplayHomeAsUpEnabled(showHomeUp);
            boolean useLogo = true;
            ab.setDisplayUseLogoEnabled(useLogo);

			loadingDialog = ProgressDialog.show(
				FoursquareAuthorizationActivity.this,
				getString(R.string.dialog_please_wait), String.format(
						getString(R.string.dialog_loading),
						getString(R.string.foursquare)), true, // Undefined progress
				true, // Allow canceling of operation
				new OnCancelListener() {
					public void onCancel(DialogInterface dialog) {
//						Crouton.makeText(FoursquareAuthorizationActivity.this, getString(R.string.auth_cancelled), Style.ALERT).show();
                        Toast.makeText(FoursquareAuthorizationActivity.this, getString(R.string.auth_cancelled), Toast.LENGTH_SHORT).show();
					}
				});
		} else {
//			Crouton.makeText(this, getString(R.string.auth_failed), Style.ALERT).show();
            Toast.makeText(this, getString(R.string.auth_failed), Toast.LENGTH_SHORT).show();
			Log.i(TAG, "FoursquareClient is null. Be sure to set the foursquare client before attempting to login.");
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onResume() {
		super.onResume();

		WebView webView = new WebView(this);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.setVisibility(View.VISIBLE);
		setContentView(webView);

		String authUrl = String.format("%s?client_id=%s&response_type=code&redirect_uri=%s",
				AUTHORIZE_URL, mFoursquareClient.getClientId(),
				mFoursquareClient.getCallbackUrl());

		try {
			webView.setWebViewClient(new WebViewClient() {
				@Override
				public void onPageStarted(WebView view, String url, Bitmap favicon) {
				}

				@Override
				public void onPageFinished(WebView view, String url) {
					if (url.startsWith(mFoursquareClient.getCallbackUrl())) {
						try {
							if (url.indexOf("code=") != -1) {
								String requestToken = extractParamFromUrl(url,
										"code");

								// Do http post here...
								String accessUrl = String
										.format("%s?code=%s&client_id=%s&client_secret=%s&grant_type=authorization_code&redirect_uri=%s",
												ACCESS_URL, requestToken,
												mFoursquareClient.getClientId(),
												mFoursquareClient.getClientSecret(),
												mFoursquareClient.getCallbackUrl());

								new TokenFetcher(
										FoursquareAuthorizationActivity.this.getApplicationContext(),
										accessUrl, FoursquareAuthorizationActivity.this).execute();

								view.setVisibility(View.INVISIBLE);
							} else if (url.indexOf("error=") != -1) {
								view.setVisibility(View.INVISIBLE);
							}
						} catch (Exception e) {
							Log.i(TAG, "IOException: " + e.getMessage());
							e.printStackTrace();
						}
					}
				}
			});

			webView.loadUrl(authUrl);
			webView.requestFocus();
			loadingDialog.dismiss();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		}
	}

	private String extractParamFromUrl(String url,String paramName) {
		String queryString = url.substring(url.indexOf("?", 0)+1,url.length());
		QueryStringParser queryStringParser = new QueryStringParser(queryString);
		return queryStringParser.getQueryParamValue(paramName);
	}

	public class TokenFetcher extends AsyncTask<Void, Integer, Boolean> {
		Context myContext;
		String myUrl;
		String myAccessToken = "";
        ActionBarActivity myCaller;

		public TokenFetcher(Context theContext, String theUrl, ActionBarActivity theCaller) {
			myContext = theContext;
			myUrl = theUrl;
			myCaller = theCaller;
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			Boolean result = false;
			String response = Util.getHttpResponse(myUrl, true, Util.contentType, Util.contentType);
			if (response != null) {
				JsonParser jParser = new JsonParser();
				JsonObject jObject = (JsonObject) jParser.parse(response);
				if(jObject != null) {
					String accessToken = jObject.get("access_token") != null ? jObject.get("access_token").getAsString() : "";

					if (accessToken.length() > 0) {
						result = true;
						myAccessToken = accessToken.trim();

						PreferencesHelper.setFoursquareToken(myContext, myAccessToken);
						PreferencesHelper.setFoursquareConnected(myContext, result);
					} else {
//						Crouton.makeText(myCaller, "There was a problem authenticating, please try again.", Style.ALERT).show();
                        Toast.makeText(myCaller, "There was a problem authenticating, please try again.", Toast.LENGTH_SHORT).show();
					}
				} else {
//					Crouton.makeText(myCaller, "There was a problem authenticating, please try again.", Style.ALERT).show();
                    Toast.makeText(myCaller, "There was a problem authenticating, please try again.", Toast.LENGTH_SHORT).show();
				}
			}

			return result;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if(result)
				Toast.makeText(myContext, "Foursquare Account Authorized", Toast.LENGTH_SHORT).show();

			finish();
			startActivity(new Intent(getApplicationContext(), MainActivity.class));
		}
	}

	public static void clearFoursquareUser(Context myContext) {
		PreferencesHelper.setFoursquareToken(myContext, "");
		PreferencesHelper.setFoursquareConnected(myContext, false);
		PreferencesHelper.setFoursquareUserId(myContext, "");
		PreferencesHelper.setFoursquareSuperuserLevel(myContext, 0);
	}
}
