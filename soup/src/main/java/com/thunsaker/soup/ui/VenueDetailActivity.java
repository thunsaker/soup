package com.thunsaker.soup.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.view.Window;

import com.thunsaker.android.common.annotations.ForApplication;
import com.thunsaker.soup.R;
import com.thunsaker.soup.app.BaseSoupActivity;
import com.thunsaker.soup.services.foursquare.FoursquareTasks;

import javax.inject.Inject;

/*
 * Created by @thunsaker
 */
public class VenueDetailActivity extends BaseSoupActivity {
    @Inject @ForApplication
    Context mContext;

//    @Inject
//    EventBus mBus;

    @Inject
    FoursquareTasks mFoursquareTasks;

    public static boolean wasEdited;

    private boolean showHomeUp = true;

    public static final String VENUE_TO_LOAD_EXTRA = "VENUE_TO_LOAD_EXTRA";
    public static final String VENUE_URL_TO_LOAD_EXTRA = "VENUE_URL_TO_LOAD_EXTRA";
    public static final String VENUE_CALLBACK_EXTRA = "VENUE_CALLBACK_EXTRA";
    public static final String VENUE_DETAILS_SOURCE = "VENUE_DETAILS_SOURCE";

    public static final int VENUE_DETAIL_SOURCE_MAIN = 0;
    public static final int VENUE_DETAIL_SOURCE_SEARCH = 1;
    public static final int VENUE_DETAIL_SOURCE_LIST = 2;
    public static final int VENUE_DETAIL_SOURCE_WELCOME = 3;
    public static final int VENUE_DETAIL_SOURCE_HISTORY = 4;

    public static String venueIdToLoad = "";
    public static int venueDetailsSource = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            supportRequestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
		}

        setContentView(R.layout.activity_venue_detail);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayUseLogoEnabled(true);
        ab.setDisplayShowHomeEnabled(true);
        ab.setTitle(null);
        ab.setIcon(R.drawable.transparent_square);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            ab.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.black_super_transparent)));
            ab.setSplitBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.black_super_transparent)));
            ab.setStackedBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.black_super_transparent)));
        }

        setSupportProgressBarVisibility(true);
        setSupportProgressBarIndeterminate(true);

        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    public void handleIntent(Intent intent) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        VenueDetailFragment venueDetailFragment = VenueDetailFragment.newInstance(null, -1);

        if (intent.hasExtra(VENUE_URL_TO_LOAD_EXTRA)) { // Venue Url - from VenueDetailActivity Receiver
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            venueDetailFragment = VenueDetailFragment.newInstance(intent.getStringExtra(VENUE_URL_TO_LOAD_EXTRA), VenueDetailFragment.VENUE_TYPE_URL);
        } else if (intent.hasExtra(VENUE_TO_LOAD_EXTRA)) { // Venue Id - from other screens (History, List, etc)
            venueDetailFragment = VenueDetailFragment.newInstance(intent.getStringExtra(VENUE_TO_LOAD_EXTRA), VenueDetailFragment.VENUE_TYPE_ID);
        } else if (intent.hasExtra(VenueDetailFragment.ARG_ITEM_JSON_STRING)) { // Venue JSON - From VenueListFragment
            venueDetailFragment = VenueDetailFragment.newInstance(intent.getStringExtra(VenueDetailFragment.ARG_ITEM_JSON_STRING), VenueDetailFragment.VENUE_TYPE_JSON);
        }
//        venueDetailsSource = intent.getIntExtra(VENUE_DETAILS_SOURCE, 0);
        if (venueDetailFragment != null)
            fragmentManager
                    .beginTransaction()
                    .add(R.id.venue_detail_container, venueDetailFragment)
                    .commit();
    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case android.R.id.home:
//                switch (venueDetailsSource) {
//                    case 1: // Venue Search
//                        NavUtils.navigateUpTo(this, new Intent(this, VenueSearchActivity.class));
//                        break;
//                    case 2: // List
//                        finish();
//                        break;
//                    case 3:
//                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
//                        finish();
//                        break;
//                    default:
//
//                        break;
//                }
//
//                venueIdToLoad = "";
//                VenueDetailFragment.currentCompactVenue = null;
//                VenueDetailFragment.currentVenue = null;
//                return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        venueIdToLoad = "";
        VenueDetailFragment.currentVenue = null;
        VenueEditCategoriesActivity.currentVenue = null;
    }

    public void FlagVenue(String id, int type) {
        mFoursquareTasks.new FlagVenue(id, type, "").execute();
    }

    public void FlagVenueDuplicate(String id, int type, String dupeId) {
        mFoursquareTasks.new FlagVenue(id, type, dupeId).execute();
    }
}