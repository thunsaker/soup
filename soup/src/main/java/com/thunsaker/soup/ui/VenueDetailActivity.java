package com.thunsaker.soup.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.Window;

import com.thunsaker.soup.R;
import com.thunsaker.soup.app.BaseSoupActivity;

/*
 * Created by @thunsaker
 */
public class VenueDetailActivity extends BaseSoupActivity {
	public static boolean wasEdited;

    private boolean useLogo = false;
	private boolean showHomeUp = true;

	public static final String VENUE_TO_LOAD_EXTRA = "VENUE_TO_LOAD_EXTRA";
	public static final String VENUE_URL_TO_LOAD_EXTRA = "VENUE_URL_TO_LOAD_EXTRA";
    public static final String VENUE_CALLBACK_EXTRA = "VENUE_CALLBACK_EXTRA";
    public static final String VENUE_DETAILS_SOURCE = "VENUE_DETAILS_SOURCE";

    public static final int VENUE_DETAIL_SOURCE_MAIN = 0;
    public static final int VENUE_DETAIL_SOURCE_SEARCH = 1;
    public static final int VENUE_DETAIL_SOURCE_LIST = 2;
    public static final int VENUE_DETAIL_SOURCE_WELCOME = 3;

	public static String venueIdToLoad = "";
	public static int venueDetailsSource = 0;


	@SuppressLint("InlinedApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
			requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
		}

		handleIntent(getIntent());

		setContentView(R.layout.activity_venue_detail);

		ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(showHomeUp);
		ab.setDisplayUseLogoEnabled(useLogo);
		ab.setDisplayShowHomeEnabled(false);
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
			ab.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.black_super_transparent)));
			ab.setSplitBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.black_super_transparent)));
			ab.setStackedBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.black_super_transparent)));
		}

		setProgressBarVisibility(true);
		setProgressBarIndeterminate(true);

		if (savedInstanceState == null) {
			// Create the detail fragment and add it to the activity
			// using a fragment transaction.
			Bundle arguments = new Bundle();
			if(getIntent().hasExtra(VenueDetailFragment.ARG_ITEM_JSON_STRING))
				arguments.putString(VenueDetailFragment.ARG_ITEM_JSON_STRING,
						getIntent().getStringExtra(VenueDetailFragment.ARG_ITEM_JSON_STRING));
			VenueDetailFragment fragment = new VenueDetailFragment();
			fragment.setArguments(arguments);
			getSupportFragmentManager().beginTransaction()
					.add(R.id.venue_detail_container, fragment).commit();
		}
	}


	@Override
	protected void onNewIntent(Intent intent) {
		handleIntent(intent);
	}

	public void handleIntent(Intent intent) {
		if(intent.hasExtra(VENUE_URL_TO_LOAD_EXTRA)) {
			showHomeUp = false;
		} else if(intent.hasExtra(VENUE_TO_LOAD_EXTRA)) {
			venueIdToLoad = intent.getStringExtra(VENUE_TO_LOAD_EXTRA);
		}

		venueDetailsSource = intent.getIntExtra(VENUE_DETAILS_SOURCE, 0);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			switch (venueDetailsSource) {
			case 1: // Venue Search
				NavUtils.navigateUpTo(this, new Intent(this, VenueSearchActivity.class));
				break;
			case 2: // List
				finish();
				break;
			case 3:
				startActivity(new Intent(getApplicationContext(), MainActivity.class));
				finish();
				break;
			default:

				break;
			}

			venueIdToLoad = "";
			VenueDetailFragment.currentCompactVenue = null;
			VenueDetailFragment.currentVenue = null;
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		venueIdToLoad = "";
		VenueDetailFragment.currentVenue = null;
		VenueEditCategoriesActivity.currentVenue = null;
	}
}