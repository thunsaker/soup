package com.thunsaker.soup.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.maps.model.LatLng;
import com.thunsaker.soup.AuthHelper;
import com.thunsaker.soup.FoursquareHelper;
import com.thunsaker.soup.PreferencesHelper;
import com.thunsaker.soup.R;
import com.thunsaker.soup.classes.FoursquareClient;
import com.thunsaker.soup.ui.settings.SettingsActivity;
import com.thunsaker.soup.ui.settings.SettingsLegacyActivity;
import com.thunsaker.soup.util.Util;
import com.thunsaker.soup.util.foursquare.CheckinEndpoint;
import com.thunsaker.soup.util.foursquare.UserEndpoint;

/*
 * Created by @thunsaker
 */
public class MainActivity extends ActionBarActivity implements
		VenueListFragment.Callbacks, ListsFragment.Callbacks {
	private String[] mDrawerItems;
	private DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle mDrawerToggle;
	private String mTitle;
	private String mDrawerTitle;
	private ListView mDrawerList;
	final String[] fragments = { "com.thunsaker.soup.ui.VenueListFragment", // Home
			"com.thunsaker.soup.ui.HistoryActivity", // History
			"com.thunsaker.soup.ui.ListsFragment", // Lists
			"com.thunsaker.soup.ui.AboutFragment", // About
//			"com.thunsaker.soup.ui.SettingsActivity", // Settings
			"com.thunsaker.soup.ui.WelcomeActivity" }; // Log Out

	private final static Uri soupProUri = Uri
			.parse("market://details?id=com.thunsaker.soup.pro");

	public static boolean isFoursquareConnected = false;
	static FoursquareClient mFoursquareClient;

	public static LocationManager mLocationManager;
	public static LatLng currentLocation;

	private AdView adView;

	protected static final String HISTORY_PREVIEW_DIALOG = "HISTORY_PREVIEW_DIALOG";
	protected static final String LOGOUT_CONFIRMATION_DIALOG = "LOGOUT_CONFIRMATION_DIALOG";
	protected static final String LISTS_PREVIEW_DIALOG = "LISTS_PREVIEW_DIALOG";
	protected static final String CHECKIN_CONFIRMATION_DIALOG = "CHECKIN_CONFIRMATION_DIALOG";

	static String longPressedVenueId = "";
	static String longPressedVenueName = "";

	public static Intent genericIntent;
	public static PendingIntent genericPendingIntent;

	public static final int NOTIFICATION_CHECKIN = 0;

	public static final String VENUE_ID_CHECKIN_EXTRA = "VENUE_ID_CHECKIN_EXTRA";
	public static final String VENUE_NAME_CHECKIN_EXTRA = "VENUE_NAME_CHECKIN_EXTRA";

	@SuppressLint("InlinedApi")
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Crashlytics.start(this);

		handleIntent(getIntent());

//        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
//        }

		setContentView(R.layout.activity_main);

		mDrawerTitle = getString(R.string.app_name);
		mTitle = getString(R.string.app_name);

		mDrawerItems = Util.IsProInstalled(getApplicationContext()) ? getResources()
				.getStringArray(R.array.navigation_array_items_pro)
				: getResources().getStringArray(R.array.navigation_array_items);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				R.drawable.ic_navigation_drawer, R.string.drawer_open,
				R.string.drawer_close) {
			@Override
			public void onDrawerClosed(View drawerView) {
				getSupportActionBar().setTitle(mTitle);
			}

			@Override
			public void onDrawerOpened(View drawerView) {
				getSupportActionBar().setTitle(mDrawerTitle);
			}
		};
		mDrawerList = (ListView) findViewById(R.id.left_drawer);

		mDrawerList.setAdapter(new ArrayAdapter<String>(this,
				R.layout.drawer_list_item, mDrawerItems));
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

		// Set the drawer toggle as the DrawerListener
		mDrawerLayout.setDrawerListener(mDrawerToggle);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeButtonEnabled(true);
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
//            ab.setBackgroundDrawable(new ColorDrawable(getResources().getColor(android.R.color.transparent)));
//            ab.setSplitBackgroundDrawable(new ColorDrawable(getResources().getColor(android.R.color.transparent)));
//            ab.setStackedBackgroundDrawable(new ColorDrawable(getResources().getColor(android.R.color.transparent)));
//        }

//		setProgressBarIndeterminate(true);

		mFoursquareClient = new FoursquareClient(
				AuthHelper.FOURSQUARE_CLIENT_ID,
				AuthHelper.FOURSQUARE_CLIENT_SECRET,
				AuthHelper.FOURSQUARE_CALLBACK_URL);
		FoursquareAuthorizationActivity.mFoursquareClient = mFoursquareClient;
		isFoursquareConnected = PreferencesHelper
				.getFoursquareConnected(getApplicationContext());

        if (isFoursquareConnected) {
			if (Util.IsProInstalled(getApplicationContext())) {
                hideAds();

				ab.setTitle(R.string.app_name_pro);
				ab.setLogo(R.drawable.ic_launcher_pro);
            } else {
                showAds();
            }

			selectItem(0);

			String superuserLevel = PreferencesHelper
					.getFoursquareSuperuserLevel(getApplicationContext()) != null ? PreferencesHelper
					.getFoursquareSuperuserLevel(getApplicationContext())
					: FoursquareHelper.SUPERUSER_STATUS_UNKNOWN;
			if (superuserLevel.equals(FoursquareHelper.SUPERUSER_STATUS_UNKNOWN)
					|| superuserLevel.equals("")) {
				String userRequestUrl;
				String accessToken = PreferencesHelper
						.getFoursquareToken(getApplicationContext());
				if (accessToken != null && accessToken.length() > 0) {
					userRequestUrl = String
							.format("%s%s?&oauth_token=%s&v=%s",
									FoursquareHelper.FOURSQUARE_BASE_URL,
									FoursquareHelper.FOURSQUARE_USER_ENDPOINT
											+ FoursquareHelper.FOURSQUARE_USER_SELF_SUFFIX,
									accessToken,
									FoursquareHelper.CURRENT_API_DATE);
				} else {
					userRequestUrl = String
							.format("%s%s?client_id=%s&client_secret=%s&v=%s",
									FoursquareHelper.FOURSQUARE_BASE_URL,
									FoursquareHelper.FOURSQUARE_USER_ENDPOINT
											+ FoursquareHelper.FOURSQUARE_USER_SELF_SUFFIX,
									MainActivity.mFoursquareClient
											.getClientId(),
									MainActivity.mFoursquareClient
											.getClientSecret(),
									FoursquareHelper.CURRENT_API_DATE);
				}

				new UserEndpoint.GetUserInfo(getApplicationContext(),
						userRequestUrl).execute();

				genericIntent = new Intent(getApplicationContext(),
						MainActivity.class);
				TaskStackBuilder stackBuilder = TaskStackBuilder
						.create(getApplicationContext());
				stackBuilder.addParentStack(MainActivity.class);
				stackBuilder.addNextIntent(genericIntent);
				genericPendingIntent = stackBuilder.getPendingIntent(0,
						PendingIntent.FLAG_UPDATE_CURRENT);
			}

			VenueListFragment.searchQuery = "";
			VenueListFragment.searchQueryLocation = "";

			ShowNavDrawer(PreferencesHelper.getShownNavDrawer(getApplicationContext()));
		} else {
			ShowWelcomeActivity();
		}
	}
    private void showAds() {
        adView = new AdView(this);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId(getString(R.string.admob_id));
        LinearLayout adLayout = (LinearLayout)findViewById(R.id.adViewLayoutWrapper);
        adLayout.addView(adView);

        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("D17425E1A2C3EB1DE2D8E56DEE780F50")
                .build();

        if(adView != null)
            adView.loadAd(adRequest);
    }

    private void hideAds() {
        LinearLayout adLayout = (LinearLayout) findViewById(R.id.adViewLayoutWrapper);
        adLayout.setVisibility(View.GONE);

        if (adView != null)
            adView.destroy();
    }

    private void ShowNavDrawer(boolean show) {
		if(!show) {
			mDrawerLayout.openDrawer(mDrawerList);
			PreferencesHelper.setShownNavDrawer(getApplicationContext(), true);
		}
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}

		switch (item.getItemId()) {
		case R.id.action_search:
			Intent searchActivity = new Intent(getApplicationContext(),
					VenueSearchActivity.class);
			VenueListFragment.isSearching = true;
			VenueListFragment.searchQuery = "";
			VenueListFragment.searchQueryLocation = "";
			startActivity(searchActivity);
			return true;
			// case R.id.action_go_pro:
			// startActivity(new Intent(
			// Intent.ACTION_VIEW,
			// MainActivity.soupProUri));
			// return true;
			/*
			 * case R.id.action_licences: final LicensesDialogFragment
			 * licencesFragment =
			 * LicensesDialogFragment.newInstace(R.raw.licences);
			 * licencesFragment.show(getSupportFragmentManager(), null);
			 */
		default:
			return false;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		handleIntent(intent);
	}

	public void handleIntent(Intent intent) {
		if (intent.hasExtra(VENUE_ID_CHECKIN_EXTRA)) {
			longPressedVenueId = intent.getStringExtra(VENUE_ID_CHECKIN_EXTRA);

			if (intent.hasExtra(VENUE_NAME_CHECKIN_EXTRA)) {
				longPressedVenueName = intent
						.getStringExtra(VENUE_NAME_CHECKIN_EXTRA);
			}

			if (longPressedVenueId != null && longPressedVenueId.length() > 0
					&& longPressedVenueName != null
					&& longPressedVenueName.length() > 0)
				CheckinUser(longPressedVenueId, longPressedVenueName,
						MainActivity.this);
		}
	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title.toString();
		getSupportActionBar().setTitle(mTitle);
	}

	public void ShowWelcomeActivity() {
		MainActivity.isFoursquareConnected = false;
		finish();
		Intent welcomeActivity = new Intent(getApplicationContext(),
				WelcomeActivity.class);
		startActivity(welcomeActivity);
	}

	@Override
	public void onItemSelected(String compactVenueJson) {
		Intent detailIntent = new Intent(this, VenueDetailActivity.class);
		detailIntent.putExtra(VenueDetailFragment.ARG_ITEM_JSON_STRING,
				compactVenueJson);
		detailIntent.putExtra(VenueDetailActivity.VENUE_DETAILS_SOURCE,
				VenueDetailActivity.VENUE_DETAIL_SOURCE_MAIN);
		startActivity(detailIntent);
	}

	@Override
	public boolean onListItemLongClick(String id, String name) {
		longPressedVenueId = id;
		longPressedVenueName = name;

		DialogFragment checkinDialog = new CheckinDialogFragment();
		checkinDialog.show(getSupportFragmentManager(),
				CHECKIN_CONFIRMATION_DIALOG);

		return true;
	}

	public static class HistoryUpsellDialogFragment extends DialogFragment {
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			LayoutInflater inflater = getActivity().getLayoutInflater();
			builder.setView(inflater.inflate(R.layout.history_preview, null))
					.setPositiveButton(R.string.pro_upsell_button,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									startActivity(new Intent(
											Intent.ACTION_VIEW,
											MainActivity.soupProUri));
								}
							}).setNegativeButton(android.R.string.cancel, null);
			return builder.create();
		}
	}

	public static class ListsUpsellDialogFragment extends DialogFragment {
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			LayoutInflater inflater = getActivity().getLayoutInflater();
			builder.setView(inflater.inflate(R.layout.lists_preview, null))
					.setPositiveButton(R.string.pro_upsell_button,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									startActivity(new Intent(
											Intent.ACTION_VIEW,
											MainActivity.soupProUri));
								}
							}).setNegativeButton(android.R.string.cancel, null);
			return builder.create();
		}
	}

	public static class LogOutDialogFragment extends DialogFragment {
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setMessage(R.string.dialog_logout_confirmation)
					.setPositiveButton(R.string.dialog_yes,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									FoursquareAuthorizationActivity
											.clearFoursquareUser(getActivity()
													.getApplicationContext());
									startActivity(new Intent(getActivity()
											.getApplicationContext(),
											WelcomeActivity.class));
									getActivity().finish();
								}
							}).setNegativeButton(R.string.dialog_no, null);
			return builder.create();
		}
	}

	public static class CheckinDialogFragment extends DialogFragment {
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			if (longPressedVenueId != null && longPressedVenueId.length() > 0
					&& longPressedVenueName != null
					&& longPressedVenueName.length() > 0) {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						getActivity());
				builder.setMessage(
						String.format(
								getString(R.string.dialog_checkin_confirmation),
								longPressedVenueName))
						.setPositiveButton(getString(R.string.dialog_yes),
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										MainActivity.CheckinUser(
												longPressedVenueId,
												longPressedVenueName,
												getActivity());
									}
								})
						.setNegativeButton(getString(R.string.dialog_no), null);
				return builder.create();
			} else {
				return null;
			}
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onResume() {
        super.onResume();
        if (Util.IsProInstalled(getApplicationContext()))
            hideAds();
	}

    @Override
    public void onPause() {
        MainActivity.mLocationManager = null;

        if (adView != null) {
            adView.pause();
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (adView != null) {
            hideAds();
        }
        super.onDestroy();
    }

	private class DrawerItemClickListener implements
			ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView parent, View view, int position,
				long id) {
			selectItem(position);
		}
	}

	private void selectItem(int position) {
		FragmentManager fragmentManager = getSupportFragmentManager();

		switch (position) {
		case 5: // Log out
			DialogFragment confirmationDialog = new LogOutDialogFragment();
			confirmationDialog.show(getSupportFragmentManager(),
					LOGOUT_CONFIRMATION_DIALOG);
			break;
		case 4: // Settings
			Intent settingsIntent;
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD)
				settingsIntent = new Intent(getApplicationContext(), SettingsActivity.class);
			else
				settingsIntent = new Intent(getApplicationContext(), SettingsLegacyActivity.class);

			if(Util.IsProInstalled(getApplicationContext())) {
				settingsIntent.putExtra(SettingsActivity.EXTRA_IS_PRO, Util.IsProInstalled(getApplicationContext()));
			}
			startActivity(settingsIntent);

			mDrawerList.setItemChecked(0, true);
			break;
		case 2: // Lists
			// TODO: Change my upsell dialog. Thinking, show the history page,
			// but don't let them interact with anything, or show the old dialog
			// as a fragment instead of a dialog
			if (Util.IsProInstalled(getApplicationContext())) {
				fragmentManager
						.beginTransaction()
						.replace(
								R.id.content_frame,
								Fragment.instantiate(MainActivity.this,
										fragments[position])).commit();

				mDrawerList.setItemChecked(position, true);
                setTitle(mDrawerItems[position]);
			} else {
				DialogFragment listsPreviewDialog = new ListsUpsellDialogFragment();
				listsPreviewDialog.show(getSupportFragmentManager(),
						LISTS_PREVIEW_DIALOG);
			}
			break;
		case 1: // History
			if (Util.IsProInstalled(getApplicationContext())) {
				startActivity(new Intent(getApplicationContext(),
						HistoryActivity.class));
				mDrawerList.setItemChecked(0, true);
			} else {
				DialogFragment historyPreviewDialog = new HistoryUpsellDialogFragment();
				historyPreviewDialog.show(getSupportFragmentManager(),
						HISTORY_PREVIEW_DIALOG);
			}
			break;
		default: // Venue List (VenueListFragment)
			fragmentManager
					.beginTransaction()
					.replace(
							R.id.content_frame,
							Fragment.instantiate(MainActivity.this,
									fragments[position])).commit();

			mDrawerList.setItemChecked(position, true);
			setTitle(position == 0 ? getString(R.string.app_name)
					: mDrawerItems[position]);
            VenueListFragment.ClearSearchValues();
			break;
		}

		mDrawerLayout.closeDrawer(mDrawerList);
	}

	public static void CheckinUser(String id, String name, Activity activity) {
		new CheckinEndpoint.PostUserCheckin(activity.getApplicationContext(),
				currentLocation, id, name, "").execute();
		longPressedVenueId = "";
		longPressedVenueName = "";
		activity.finish();
	}
}