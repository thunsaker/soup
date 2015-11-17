package com.thunsaker.soup.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.foursquare.android.nativeoauth.FoursquareOAuth;
import com.foursquare.android.nativeoauth.model.AccessTokenResponse;
import com.foursquare.android.nativeoauth.model.AuthCodeResponse;
import com.thunsaker.soup.PreferencesHelper;
import com.thunsaker.soup.R;
import com.thunsaker.soup.services.AuthHelper;
import com.thunsaker.soup.services.foursquare.FoursquarePrefs;

import butterknife.ButterKnife;
import butterknife.InjectView;

/*
 * Created by @thunsaker
 */
public class WelcomeActivity extends ActionBarActivity {
    private static final int REQUEST_FOURSQUARE_AUTH = 0;
    private static final int REQUEST_FOURSQUARE_AUTH_TOKEN = 1;

    public static final String EXTRA_VENUE_ID_BEFORE_AUTH = "EXTRA_VENUE_ID_BEFORE_AUTH";

    private boolean useLogo = true;
    private boolean showHomeUp = false;

    @InjectView(R.id.toolbarWelcome) Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        ButterKnife.inject(this);
        setSupportActionBar(mToolbar);

        setTitle("");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_welcome, menu);

        final MenuItem action_learn = (MenuItem) menu.findItem(R.id.action_learn_more);
        if (action_learn != null) {
            action_learn.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.welcome_learn_more_url))));
                    return false;
                }
            });
        }

        final MenuItem action_login = (MenuItem) menu.findItem(R.id.action_login);
        if (action_login != null) {
            action_login.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    LaunchFoursquareAuthActivity();
                    return true;
                }
            });
        }

        return super.onCreateOptionsMenu(menu);
    }

    // Pop foursquare native auth
    private void LaunchFoursquareAuthActivity() {
        Intent foursquareAuth = FoursquareOAuth.getConnectIntent(getApplicationContext(),
                AuthHelper.FOURSQUARE_CLIENT_ID);
        startActivityForResult(foursquareAuth, REQUEST_FOURSQUARE_AUTH);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_FOURSQUARE_AUTH:
                AuthCodeResponse codeResponse =
                        FoursquareOAuth.getAuthCodeFromResult(resultCode, data);

                if(codeResponse != null) {
                    if(codeResponse.getCode() != null) {
                        String authCode = codeResponse.getCode();
                        Intent tokenIntent = FoursquareOAuth.getTokenExchangeIntent(
                                getApplicationContext(),
                                AuthHelper.FOURSQUARE_CLIENT_ID,
                                AuthHelper.FOURSQUARE_CLIENT_SECRET, authCode);
                        startActivityForResult(tokenIntent, REQUEST_FOURSQUARE_AUTH_TOKEN);
                    }
                }
                break;
            case REQUEST_FOURSQUARE_AUTH_TOKEN:
                AccessTokenResponse tokenResponse =
                        FoursquareOAuth.getTokenFromResult(resultCode, data);

                if(tokenResponse != null && tokenResponse.getAccessToken() != null) {
                    PreferencesHelper.setFoursquareToken(getApplicationContext(), tokenResponse.getAccessToken());
                    PreferencesHelper.setFoursquareConnected(getApplicationContext(), true);
                    if(getIntent().hasExtra(WelcomeActivity.EXTRA_VENUE_ID_BEFORE_AUTH)) {
                    	Intent detailIntent = new Intent(this, VenueDetailActivity.class);
//                		detailIntent.putExtra(VenueDetailFragment.ARG_ITEM_JSON_STRING, );
                		detailIntent.putExtra(VenueDetailActivity.VENUE_TO_LOAD_EXTRA, getIntent().getStringExtra(WelcomeActivity.EXTRA_VENUE_ID_BEFORE_AUTH));
                		detailIntent.putExtra(VenueDetailActivity.VENUE_DETAILS_SOURCE, VenueDetailActivity.VENUE_DETAIL_SOURCE_WELCOME);
                		startActivity(detailIntent);
                    } else {
                    	startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    }
                    finish();
                } else {
                    if(tokenResponse != null && tokenResponse.getException() != null) {
                        Toast.makeText(getApplicationContext(),
                                "Problem Authenticating: " + tokenResponse.getException().toString(),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(),
                                "Problem Authenticating: An unknown error occurred",
                                Toast.LENGTH_SHORT).show();
                    }
                    FoursquarePrefs.clearFoursquareUser(getApplicationContext());
                }

        }
    }
}