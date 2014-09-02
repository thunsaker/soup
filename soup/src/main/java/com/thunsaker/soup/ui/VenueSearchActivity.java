package com.thunsaker.soup.ui;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources.NotFoundException;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.thunsaker.android.common.annotations.ForApplication;
import com.thunsaker.soup.PreferencesHelper;
import com.thunsaker.soup.R;
import com.thunsaker.soup.app.BaseSoupActivity;
import com.thunsaker.soup.data.api.model.CompactVenue;
import com.thunsaker.soup.services.foursquare.FoursquarePrefs;
import com.thunsaker.soup.services.foursquare.endpoints.CheckinEndpoint;
import com.thunsaker.soup.ui.MainActivity.CheckinDialogFragment;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

/*
 * Created by @thunsaker
 */
public class VenueSearchActivity extends BaseSoupActivity implements
	VenueListFragment.OnFragmentInteractionListener {

    @Inject @ForApplication
    Context mContext;

    @Inject
    LocationManager mLocationManager;

    @Inject
    EventBus mBus;

	private boolean enableDuplicateMenu;

	public static String SELECTED_DUPLICATE_VENUE = "SELECTED_DUPLICATE_VENUE";
	public static String SELECTED_DUPLICATE_VENUE_TYPE = "SELECTED_DUPLICATE_VENUE_TYPE";

	public static int DUPLICATE_VENUE = 0;
	public static int ORIGINAL_VENUE = 1;

    @InjectView(R.id.editTextSearchVenueLocation) EditText mLocation;
    @InjectView(R.id.relativeLayoutVenueSearchContainer) RelativeLayout mSearchEmptyWrapper;

    // Overlay Views
    @InjectView(R.id.frameLayoutSearchInstructionalOverlay) FrameLayout mOverlayWrapper;
    @InjectView(R.id.imageViewSearchOverlayPickLocation) ImageView mOverlayPickLocationImage;
    @InjectView(R.id.textViewSearchOverlayPickLocation) TextView mOverlayPickLocationText;
    @InjectView(R.id.imageViewSearchOverlayTypeLocation) ImageView mOverlayLocationImage;
    @InjectView(R.id.textViewSearchOverlayTypeLocation) TextView mOverlayLocationText;

	SearchView mSearchView;
	ImageButton mSearchImageButton;

	CompactVenue currentSelectedCompactVenue = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		enableDuplicateMenu = false;

        setContentView(R.layout.activity_venue_search);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        ab.setCustomView(R.layout.search_layout_actionbar);
        ab.setDisplayShowCustomEnabled(true);
        ab.setDisplayShowHomeEnabled(true);
//        ab.setDisplayUseLogoEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setIcon(getResources().getDrawable(R.drawable.ic_launcher_white));

		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		LinearLayout searchViewWrapper = (LinearLayout) ab.getCustomView();
		mSearchView = (SearchView) searchViewWrapper.getChildAt(0);
		mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
		mSearchView.setQueryHint(getString(R.string.search_hint));
        mSearchImageButton = (ImageButton) searchViewWrapper.getChildAt(1);
        mSearchImageButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                StartSearch(null);
            }
        });

        ButterKnife.inject(this);
        handleIntent(getIntent());

		SetupSearchViews();
		ShowInstructionalOverlay(PreferencesHelper.getShownSearchOverlay(mContext));
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		handleIntent(intent);
	}

	public void handleIntent(Intent intent) {
        FragmentManager mFragmentManager = getSupportFragmentManager();

		if(Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            mSearchView.setQuery(query, false);

            String location =
                    intent.hasExtra(FoursquarePrefs.SEARCH_LOCATION)
                            ? intent.getStringExtra(FoursquarePrefs.SEARCH_LOCATION)
                            : null;
            if (location != null && location.length() > 0)
                mLocation.setText(location);

            String duplicateVenueId =
                    intent.hasExtra(FoursquarePrefs.SEARCH_DUPLICATE_VENUE_ID)
                            ? intent.getStringExtra(FoursquarePrefs.SEARCH_DUPLICATE_VENUE_ID)
                            : null;

            if (duplicateVenueId != null && duplicateVenueId.length() > 0) {
                enableDuplicateMenu = true;
                supportInvalidateOptionsMenu();
            }

            mFragmentManager
                    .beginTransaction()
                    .replace(
                            R.id.frameLayoutVenueSearchContainer,
                            VenueListFragment.newInstance(query, location, duplicateVenueId,
                                    enableDuplicateMenu
                                            ? VenueListFragment.VENUE_LIST_TYPE_DUPLICATE
                                            : VenueListFragment.VENUE_LIST_TYPE_SEARCH
                            )
                    )
                    .commit();

            mSearchEmptyWrapper.setVisibility(View.GONE);
        } else {
            mSearchEmptyWrapper.setVisibility(View.VISIBLE);
        }
	}

	private void ShowInstructionalOverlay(boolean show) {
		if(!show) {
            mOverlayWrapper.setVisibility(View.VISIBLE);
            mOverlayWrapper.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					PreferencesHelper.setShownSearchOverlay(getApplicationContext(), true);
					ShowInstructionalOverlay(true);
				}
			});

            mOverlayPickLocationImage.setVisibility(View.VISIBLE);
            mOverlayPickLocationText.setVisibility(View.VISIBLE);
			mOverlayLocationImage.setVisibility(View.VISIBLE);
			mOverlayLocationText.setVisibility(View.VISIBLE);
		} else {
            mOverlayWrapper.setVisibility(View.GONE);
			mOverlayPickLocationImage.setVisibility(View.GONE);
			mOverlayPickLocationText.setVisibility(View.GONE);
            mOverlayLocationImage.setVisibility(View.GONE);
            mOverlayLocationText.setVisibility(View.GONE);
		}
	}

	private void SetupSearchViews() {
		if(getSupportActionBar() != null) {
		    mSearchView.setIconified(false);
		    mSearchView.setFocusable(true);
		    mSearchView.requestFocusFromTouch();

		    mSearchView.setOnCloseListener(new SearchView.OnCloseListener() {
				@Override
				public boolean onClose() {
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

	        mLocation.setOnEditorActionListener(new OnEditorActionListener() {
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
            String searchQuery = query;
			Intent mySearchIntent = new Intent(getApplicationContext(), VenueSearchActivity.class);
			mySearchIntent.setAction(Intent.ACTION_SEARCH);
			if(searchQuery != null && searchQuery.length() > 0) {
				mySearchIntent.putExtra(SearchManager.QUERY, searchQuery);
			} else {
                searchQuery = mSearchView.getQuery() != null
                                ? mSearchView.getQuery().toString() : "";
				if(searchQuery.length() > 0) {
					mySearchIntent.putExtra(SearchManager.QUERY, searchQuery);
				} else {
                    Toast.makeText(this, R.string.search_error_no_query, Toast.LENGTH_SHORT).show();
				    mSearchView.requestFocusFromTouch();
				}
			}

			String searchLocation = mLocation.getText() != null
                    ? mLocation.getText().toString() : "";
			if(searchLocation.length() > 0) {
				// We have a search location, lets see if it is a gps
				// TODO: Add GPS Validation here, if they paste that
				mySearchIntent.putExtra(FoursquarePrefs.SEARCH_LOCATION, searchLocation);
				mLocationManager = null;
            }

			if(searchQuery.length() > 0) {
				startActivity(mySearchIntent);
				finish();
			}
		} catch (NotFoundException e) {
			e.printStackTrace();
		}
	}

    @OnClick(R.id.imageButtonSearchVenueLocationPick)
    public void OpenLocationPicker() {
        Intent pickLocationIntent =
            new Intent(getApplicationContext(), LocationSelectActivity.class);
            startActivityForResult(pickLocationIntent, LocationSelectActivity.PICK_LOCATION);
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if(enableDuplicateMenu) {
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
			finish();
			return true;
		case android.R.id.home:
			finish();
			return true;
		case R.id.action_duplicate_cancel:
			finish();
			return true;
		}
		return false;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == LocationSelectActivity.PICK_LOCATION) {
			switch (resultCode) {
			case Activity.RESULT_OK:
                String myPickedLocation =
                        data.hasExtra(LocationSelectActivity.PICKED_LOCATION_EXTRA)
                                ? data.getStringExtra(LocationSelectActivity.PICKED_LOCATION_EXTRA).trim()
                                : "";
                mLocation.setText(myPickedLocation);
                break;

			default:
				break;
			}
		}
	}

	@Override
	public void onBackPressed() {
        super.onBackPressed();
		finish();
	}

    @Override
    public void onVenueListClick(String compactVenueJson) {
        if(enableDuplicateMenu) {
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
    public boolean onVenueListLongClick(String venueId, String venueName) {
        CheckinDialogFragment checkinDialog = CheckinDialogFragment.newInstance(venueId, venueName, true);
		checkinDialog.show(getSupportFragmentManager(), MainActivity.CHECKIN_CONFIRMATION_DIALOG);
		return true;
	}

    public void CheckinUser(String id, String name) {
        new CheckinEndpoint.PostUserCheckin(mContext, MainActivity.currentLocation, id, name, "").execute();
        finish();
    }
}