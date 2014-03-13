package com.thunsaker.soup.ui;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources.NotFoundException;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.thunsaker.soup.FoursquareHelper;
import com.thunsaker.soup.PreferencesHelper;
import com.thunsaker.soup.R;
import com.thunsaker.soup.classes.foursquare.CompactVenue;
import com.thunsaker.soup.ui.MainActivity.CheckinDialogFragment;

/*
 * Created by @thunsaker
 */
public class VenueSearchActivity extends ActionBarActivity implements
	VenueListFragment.Callbacks {

	private boolean useLogo = true;
	private boolean showHomeUp = true;
//	private boolean mTwoPane;
	private boolean enableDuplicateMenu;

	public static String SELECTED_DUPLICATE_VENUE = "SELECTED_DUPLICATE_VENUE";
	public static String SELECTED_DUPLICATE_VENUE_TYPE = "SELECTED_DUPLICATE_VENUE_TYPE";

	public static int DUPLICATE_VENUE = 0;
	public static int ORIGINAL_VENUE = 1;

	SearchView mSearchView;
	ImageButton mSearchImageButton;
	EditText mEditTextLocation;
	ActionBar ab;

	CompactVenue currentSelectedCompactVenue = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		enableDuplicateMenu = false;

		VenueListFragment myFragment =
				(VenueListFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.venue_search_results_list);

		handleIntent(getIntent());

		setContentView(R.layout.activity_venue_search);

		ab = getSupportActionBar();

		SetupActionBar();

		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		mEditTextLocation = (EditText) findViewById(R.id.editTextSearchVenueLocation);
		LinearLayout searchViewWrapper = (LinearLayout) ab.getCustomView();
		mSearchView = (SearchView) searchViewWrapper.getChildAt(0);
		mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
		mSearchView.setQueryHint(getString(R.string.search_hint));

		if(VenueListFragment.searchQuery != null && VenueListFragment.searchQuery.length() > 0)
			mSearchView.setQuery(VenueListFragment.searchQuery, false);
		if(VenueListFragment.searchQueryLocation.length() > 0)
			((EditText) findViewById(R.id.editTextSearchVenueLocation))
                    .setText(VenueListFragment.searchQueryLocation);

		mSearchImageButton = (ImageButton) searchViewWrapper.getChildAt(1);
		mSearchImageButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				StartSearch(null);
			}
		});

		SetupSearchViews();
		SetupLocationSearchButtons();
		ShowInstructionalOverlay(PreferencesHelper.getShownSearchOverlay(getApplicationContext()));
		setProgressBarVisibility(false);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		handleIntent(intent);
	}

	public void handleIntent(Intent intent) {
		VenueListFragment.searchResultsVenueList = null;
		VenueListFragment.searchResultsVenueListAdapter = null;
		VenueListFragment.isSearching = true;

		if(Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY);
			String location =
                    intent.hasExtra(FoursquareHelper.SEARCH_LOCATION)
                            ? intent.getStringExtra(FoursquareHelper.SEARCH_LOCATION)
                            : "";

            VenueListFragment.isDuplicateSearching = intent.hasExtra(FoursquareHelper.SEARCH_DUPLICATE) && intent.getBooleanExtra(FoursquareHelper.SEARCH_DUPLICATE, false);
			VenueListFragment.searchQuery = query;
			VenueListFragment.searchQueryLocation = location.length() > 0 ? location : "";
		}
	}

	private void ShowInstructionalOverlay(boolean show) {
		if(!show) {
			FrameLayout myFrameOverlay =
                    (FrameLayout) findViewById(R.id.frameLayoutSearchInstructionalOverlay);
			myFrameOverlay.setVisibility(View.VISIBLE);
			myFrameOverlay.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					PreferencesHelper.setShownSearchOverlay(getApplicationContext(), true);
					ShowInstructionalOverlay(true);
				}
			});
			findViewById(R.id.imageViewSearchOverlayTypeLocation)
                    .setVisibility(View.VISIBLE);
			findViewById(R.id.textViewSearchOverlayTypeLocation)
                    .setVisibility(View.VISIBLE);
			findViewById(R.id.imageViewSearchOverlayPickLocation)
                    .setVisibility(View.VISIBLE);
			findViewById(R.id.textViewSearchOverlayPickLocation)
                    .setVisibility(View.VISIBLE);
		} else {
			findViewById(R.id.frameLayoutSearchInstructionalOverlay)
                    .setVisibility(View.GONE);
			findViewById(R.id.imageViewSearchOverlayTypeLocation)
                    .setVisibility(View.GONE);
			findViewById(R.id.textViewSearchOverlayTypeLocation)
                    .setVisibility(View.GONE);
			findViewById(R.id.imageViewSearchOverlayPickLocation)
                    .setVisibility(View.GONE);
			findViewById(R.id.textViewSearchOverlayPickLocation)
                    .setVisibility(View.GONE);
		}
	}

	private void SetupActionBar() {
		ab.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		ab.setCustomView(R.layout.search_layout_actionbar);
		ab.setDisplayShowCustomEnabled(true);
		ab.setDisplayShowHomeEnabled(showHomeUp);
		ab.setDisplayUseLogoEnabled(useLogo);
		ab.setDisplayHomeAsUpEnabled(showHomeUp);
		setProgressBarVisibility(true);
		setProgressBarIndeterminate(true);
	}

	private void SetupSearchViews() {
		if(ab != null) {
		    mSearchView.setIconified(false);
		    mSearchView.setFocusable(true);
		    mSearchView.requestFocusFromTouch();

		    mSearchView.setOnCloseListener(new SearchView.OnCloseListener() {
				@Override
				public boolean onClose() {
					VenueListFragment.ClearSearchValues();
					finish();
					return false;
				}
			});

	        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
				@Override
				public boolean onQueryTextSubmit(String query) {
					StartSearch(query);
					return true;
				}

				@Override
				public boolean onQueryTextChange(String newText) {
					return true;
				}
			});

	        mEditTextLocation.setOnEditorActionListener(new OnEditorActionListener() {
				@Override
				public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
					if(actionId == EditorInfo.IME_ACTION_SEARCH) {
						StartSearch(null);
						return true;
					}
					return false;
				}
			});
		}
	}

	protected void StartSearch(String query) {
		try {
			Intent mySearchIntent = new Intent(getApplicationContext(), VenueSearchActivity.class);
			mySearchIntent.setAction(Intent.ACTION_SEARCH);
			if(query != null && query.length() > 0) {
				VenueListFragment.searchQuery = query;
				mySearchIntent.putExtra(SearchManager.QUERY, query);
			} else {
				String searchText = mSearchView.getQuery() != null
                        ? mSearchView.getQuery().toString() : "";
				if(searchText.length() > 0) {
					VenueListFragment.searchQuery = searchText;
					mySearchIntent.putExtra(SearchManager.QUERY, searchText);
				} else {
                    Toast.makeText(this, R.string.search_error_no_query, Toast.LENGTH_SHORT).show();
				    mSearchView.requestFocusFromTouch();
				}
			}

			String searchLocation = mEditTextLocation.getText() != null
                    ? mEditTextLocation.getText().toString() : "";
			if(searchLocation.length() > 0) {
				// We have a search location, lets see if it is a gps
				// TODO: Add GPS Validation here, if they paste that
				VenueListFragment.searchQueryLocation = searchLocation;
				mySearchIntent.putExtra(FoursquareHelper.SEARCH_LOCATION, searchLocation);
				MainActivity.mLocationManager = null;
			} else {

            }

			if(VenueListFragment.searchQuery != null
                    && VenueListFragment.searchQuery.length() > 0) {
				if(VenueListFragment.isDuplicateSearching) {
					mySearchIntent.putExtra(FoursquareHelper.SEARCH_DUPLICATE, true);
					mySearchIntent.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
				}

				startActivity(mySearchIntent);
				finish();
			}
		} catch (NotFoundException e) {
			e.printStackTrace();
		}
	}

	private void SetupLocationSearchButtons() {
		ImageButton pickLocationImageButton =
                (ImageButton) findViewById(R.id.imageButtonSearchVenueLocationPick);
		pickLocationImageButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent pickLocationIntent =
                        new Intent(getApplicationContext(), LocationSelectActivity.class);
				startActivityForResult(pickLocationIntent, LocationSelectActivity.PICK_LOCATION);
			}
		});
	}

	@Override
	public void onItemSelected(String compactVenueJson) {
		if(VenueListFragment.isDuplicateSearching) {
			enableDuplicateMenu = true;
			supportInvalidateOptionsMenu();
			currentSelectedCompactVenue = CompactVenue.GetCompactVenueFromJson(compactVenueJson);
		} else {
            Intent detailIntent = new Intent(this, VenueDetailActivity.class);
            detailIntent.putExtra(VenueDetailFragment.ARG_ITEM_JSON_STRING, compactVenueJson);
            detailIntent.putExtra(VenueDetailActivity.VENUE_DETAILS_SOURCE, VenueDetailActivity.VENUE_DETAIL_SOURCE_SEARCH);
            startActivity(detailIntent);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if(VenueListFragment.isDuplicateSearching) {
			getMenuInflater().inflate(R.menu.activity_venue_search_duplicate, menu);

			MenuItem duplicateMenuItem =
                    menu.findItem(R.id.action_duplicate_mark_duplicate);
			MenuItem originalMenuItem =
                    menu.findItem(R.id.action_duplicate_mark_original);

			if(enableDuplicateMenu) {
				duplicateMenuItem.setEnabled(true);
				originalMenuItem.setEnabled(true);
			} else {
				duplicateMenuItem.setEnabled(false);
				originalMenuItem.setEnabled(false);
			}
		}

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_duplicate_mark_duplicate:
			if(currentSelectedCompactVenue != null) {
				Intent myDuplicateIntentData = new Intent();
				myDuplicateIntentData.putExtra(
                        VenueSearchActivity.SELECTED_DUPLICATE_VENUE,
                        currentSelectedCompactVenue.toString());
				myDuplicateIntentData.putExtra(
                        VenueSearchActivity.SELECTED_DUPLICATE_VENUE_TYPE,
                        VenueSearchActivity.DUPLICATE_VENUE);
				setResult(Activity.RESULT_OK, myDuplicateIntentData);
			}

			VenueListFragment.ClearSearchValues();
			finish();
			return true;
		case R.id.action_duplicate_mark_original:
			if(currentSelectedCompactVenue != null) {
				Intent myDuplicateIntentData = new Intent();
				myDuplicateIntentData.putExtra(
                        VenueSearchActivity.SELECTED_DUPLICATE_VENUE,
                        currentSelectedCompactVenue.toString());
				myDuplicateIntentData.putExtra(
                        VenueSearchActivity.SELECTED_DUPLICATE_VENUE_TYPE,
                        VenueSearchActivity.ORIGINAL_VENUE);
				setResult(Activity.RESULT_OK, myDuplicateIntentData);
			}

			VenueListFragment.ClearSearchValues();
			finish();
			return true;
		case android.R.id.home:
			VenueListFragment.isRefreshing = false;
            VenueListFragment.ClearSearchValues();
			finish();
			return true;
		case R.id.action_duplicate_cancel:
			VenueListFragment.isRefreshing = false;
			VenueListFragment.isDuplicateSearching = false;
			VenueListFragment.isSearching = false;
			finish();
			return true;
		}
		return false;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == LocationSelectActivity.PICK_LOCATION) {
			switch (resultCode) {
			case Activity.RESULT_OK:
                String myPickedLocation =
                        data.hasExtra(LocationSelectActivity.PICKED_LOCATION_EXTRA)
                                ? data.getStringExtra(
                                LocationSelectActivity.PICKED_LOCATION_EXTRA).trim()
                                : "";
                mEditTextLocation.setText(myPickedLocation);
                break;

			default:
				break;
			}
		}
	}

	@Override
	public void onBackPressed() {
		VenueListFragment.ClearSearchValues();
		finish();
		super.onBackPressed();
	}

	@Override
	public boolean onListItemLongClick(String id, String name) {
		MainActivity.longPressedVenueId = id;
		MainActivity.longPressedVenueName = name;
		DialogFragment checkinDialog = new CheckinDialogFragment();
		checkinDialog.show(getSupportFragmentManager(), MainActivity.CHECKIN_CONFIRMATION_DIALOG);
		return true;
	}
}