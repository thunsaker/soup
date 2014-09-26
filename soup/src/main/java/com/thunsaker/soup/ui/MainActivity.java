package com.thunsaker.soup.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.maps.model.LatLng;
import com.thunsaker.android.common.annotations.ForApplication;
import com.thunsaker.soup.BuildConfig;
import com.thunsaker.soup.PreferencesHelper;
import com.thunsaker.soup.R;
import com.thunsaker.soup.app.BaseSoupActivity;
import com.thunsaker.soup.data.FoursquareClient;
import com.thunsaker.soup.data.api.model.CompactFoursquareUser;
import com.thunsaker.soup.data.events.GetUserInfoEvent;
import com.thunsaker.soup.data.events.VenueSearchEvent;
import com.thunsaker.soup.services.AuthHelper;
import com.thunsaker.soup.services.foursquare.FoursquarePrefs;
import com.thunsaker.soup.services.foursquare.FoursquareTasks;
import com.thunsaker.soup.ui.settings.SettingsActivity;
import com.thunsaker.soup.ui.settings.SettingsLegacyActivity;
import com.thunsaker.soup.util.Util;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;

public class MainActivity extends BaseSoupActivity implements
		VenueListFragment.OnFragmentInteractionListener,
        ListsFragment.OnFragmentInteractionListener {

    @Inject @ForApplication
    Context mContext;

    @Inject
    FoursquareTasks mFoursquareTasks;

    @Inject
    EventBus mBus;

    @Inject
    LocationManager mLocationManager;

	private String[] mDrawerItems;
	private DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle mDrawerToggle;
	private String mTitle;
	private String mDrawerTitle;
	private ListView mDrawerList;
	final String[] fragments = { "com.thunsaker.soup.ui.VenueListFragment", // Home
			"com.thunsaker.soup.ui.CheckinHistoryActivity", // History
			"com.thunsaker.soup.ui.ListsFragment", // Lists
			"com.thunsaker.soup.ui.AboutFragment", // About
			"com.thunsaker.soup.ui.WelcomeActivity" }; // Log Out
    //			"com.thunsaker.soup.ui.SettingsActivity" }; // Settings

	private final static Uri soupProUri = Uri.parse("market://details?id=com.thunsaker.soup.pro");

	public static boolean isFoursquareConnected = false;
	static FoursquareClient mFoursquareClient;

	public static LatLng currentLocation;

	private AdView adView;

	protected static final String HISTORY_PREVIEW_DIALOG = "HISTORY_PREVIEW_DIALOG";
	protected static final String LOGOUT_CONFIRMATION_DIALOG = "LOGOUT_CONFIRMATION_DIALOG";
	protected static final String LISTS_PREVIEW_DIALOG = "LISTS_PREVIEW_DIALOG";
	protected static final String CHECKIN_CONFIRMATION_DIALOG = "CHECKIN_CONFIRMATION_DIALOG";

	public static Intent genericIntent;
	public static PendingIntent genericPendingIntent;

	public static final int NOTIFICATION_CHECKIN = 0;

	public static final String VENUE_ID_CHECKIN_EXTRA = "VENUE_ID_CHECKIN_EXTRA";
	public static final String VENUE_NAME_CHECKIN_EXTRA = "VENUE_NAME_CHECKIN_EXTRA";
	public static final String IS_SEARCH_CHECKIN_EXTRA = "IS_SEARCH_CHECKIN_EXTRA";

//    public static double markerActionBarAdjustment = 0.00;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(!BuildConfig.DEBUG)
            Crashlytics.start(this);

        mBus.register(this);

        handleIntent(getIntent());

        setContentView(R.layout.activity_main);

        mDrawerTitle = getString(R.string.app_name);
        mTitle = getString(R.string.app_name);

        mDrawerItems = Util.IsProInstalled(mContext.getApplicationContext()) ? getResources()
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

        ActionBar ab = SetupActionBar();

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
            } else {
                showAds();
            }

            selectItem(0);

            checkSuperuserLevel();

            genericIntent = new Intent(getApplicationContext(),
                    MainActivity.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder
                    .create(getApplicationContext());
            stackBuilder.addParentStack(MainActivity.class);
            stackBuilder.addNextIntent(genericIntent);
            genericPendingIntent = stackBuilder.getPendingIntent(0,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            ShowNavDrawer(PreferencesHelper.getShownNavDrawer(getApplicationContext()));
        } else {
            ShowWelcomeActivity();
        }
    }

    public void checkSuperuserLevel() {
        int superuserLevel = FoursquarePrefs.SUPERUSER.UNKNOWN;
        try {
            PreferencesHelper.getFoursquareSuperuserLevel(mContext);
        } catch (ClassCastException exception) {
            PreferencesHelper.migrateSuperUserPref(mContext);
            superuserLevel = PreferencesHelper.getFoursquareSuperuserLevel(mContext);
        }

        if (superuserLevel == FoursquarePrefs.SUPERUSER.UNKNOWN && Util.HasInternet(mContext))
            mFoursquareTasks.new GetUserInfo(FoursquarePrefs.FOURSQUARE_USER_SELF_SUFFIX).execute();
    }

    private ActionBar SetupActionBar() {
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeButtonEnabled(true);
        ab.setIcon(getResources().getDrawable(R.drawable.ic_launcher_white));
        return ab;
    }

    private void showAds() {
        adView = new AdView(this);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId(getString(R.string.admob_id));
        LinearLayout adLayout = (LinearLayout)findViewById(R.id.adViewLayoutWrapper);
        adLayout.addView(adView);

        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("2B0F45ECB7E319BC2500CD6AFF1353CC") // N7 - 4.4.4
                .addTestDevice("D50AF454D0BF794B6A38811EEA1F21EE") // GS3 - 4.3
                .addTestDevice("6287716BED76BCE3BB981DD19AA858E1") // GS3 - 4.1
                .addTestDevice("FDC26B2E6C049E2E9ECE7C97D42A4726") // G2 - 2.3.4
                .addTestDevice("1BF36BBC3C197AFF96AF3F9F305CAD48") // N5 - L
                .addTestDevice("D8FE76757F1CA9B485916499EC8C13DB") // MAXX - 4.4.2
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
			Intent searchActivity = new Intent(getApplicationContext(), VenueSearchActivity.class);
			startActivity(searchActivity);
			return true;
			// case R.id.action_go_pro:
			// startActivity(new Intent(
			// Intent.ACTION_VIEW,
			// MainActivity.soupProUri));
			// return true;
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
        String checkinVenueId = "";
		if (intent.hasExtra(VENUE_ID_CHECKIN_EXTRA))
            checkinVenueId = intent.getStringExtra(VENUE_ID_CHECKIN_EXTRA);

        String checkinVenueName = "";
        if (intent.hasExtra(VENUE_NAME_CHECKIN_EXTRA))
            checkinVenueId = intent.getStringExtra(VENUE_NAME_CHECKIN_EXTRA);

        if (!checkinVenueId.equals("") && !checkinVenueName.equals(""))
            CheckinUser(checkinVenueId, checkinVenueName);
	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title.toString();
		getSupportActionBar().setTitle(mTitle);
	}

	public void ShowWelcomeActivity() {
		MainActivity.isFoursquareConnected = false;
		finish();
		Intent welcomeActivity =
                new Intent(getApplicationContext(),WelcomeActivity.class);
		startActivity(welcomeActivity);
	}

    @Override
    public void onFoursquareListClick(String foursquareListId) {
        Intent myListIntent = new Intent(mContext, ListActivity.class);
        myListIntent.putExtra(ListActivity.LIST_TO_LOAD_EXTRA, foursquareListId);
        startActivity(myListIntent);
    }

	@Override
	public void onVenueListClick(String compactVenueJson) {
		Intent detailIntent = new Intent(this, VenueDetailActivity.class);
		detailIntent.putExtra(VenueDetailFragment.ARG_ITEM_JSON_STRING, compactVenueJson);
		detailIntent.putExtra(VenueDetailActivity.VENUE_DETAILS_SOURCE, VenueDetailActivity.VENUE_DETAIL_SOURCE_MAIN);
		startActivity(detailIntent);
	}

	@Override
    public boolean onVenueListLongClick(String venueId, String venueName) {
		CheckinDialogFragment checkinDialog = CheckinDialogFragment.newInstance(venueId, venueName, false);
		checkinDialog.show(getSupportFragmentManager(), CHECKIN_CONFIRMATION_DIALOG);
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
        public static CheckinDialogFragment newInstance(String id, String name, boolean isSearch) {
            CheckinDialogFragment fragment = new CheckinDialogFragment();
            Bundle args = new Bundle();
            args.putString(VENUE_ID_CHECKIN_EXTRA, id);
            args.putString(VENUE_NAME_CHECKIN_EXTRA, name);
            args.putBoolean(IS_SEARCH_CHECKIN_EXTRA, isSearch);
            fragment.setArguments(args);
            return fragment;
        }

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			if (getArguments() != null) {
                final String venueId = getArguments().getString(VENUE_ID_CHECKIN_EXTRA);
                final String venueName = getArguments().getString(VENUE_NAME_CHECKIN_EXTRA);
                final boolean isSearch = getArguments().getBoolean(IS_SEARCH_CHECKIN_EXTRA);
				return new AlertDialog.Builder(getActivity())
                        .setMessage(
                                String.format(getString(R.string.dialog_checkin_confirmation), venueName))
						.setPositiveButton(
                                getString(R.string.dialog_yes),
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
                                        if(isSearch)
                                            ((VenueSearchActivity)getActivity()).CheckinUser(venueId, venueName);
                                        else {
                                            ((MainActivity)getActivity()).CheckinUser(venueId, venueName);
                                        }
									}
								})
						.setNegativeButton(getString(R.string.dialog_no), null)
                        .create();
			} else {
				return null;
			}
		}
	}

	@Override
	protected void onResume() {
        super.onResume();
        if (Util.IsProInstalled(getApplicationContext()))
            hideAds();
	}

    @Override
    public void onPause() {
        mLocationManager = null;

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
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectItem(position);
        }
    }

	private void selectItem(int position) {
		FragmentManager fragmentManager = getSupportFragmentManager();

		switch (position) {
            case 0:
                // Venue List (VenueListFragment)
                fragmentManager
                        .beginTransaction()
                        .replace(R.id.content_frame, VenueListFragment.newInstance(VenueListFragment.VENUE_LIST_TYPE_DEFAULT))
                        .commit();
                mDrawerList.setItemChecked(position, true);
                setTitle(getString(R.string.app_name));
                break;
            case 1: // History
                if (Util.IsProInstalled(mContext)) {
                    startActivity(new Intent(mContext, CheckinHistoryActivity.class));
                    mDrawerList.setItemChecked(0, true);
                } else {
                    DialogFragment historyPreviewDialog = new HistoryUpsellDialogFragment();
                    historyPreviewDialog.show(getSupportFragmentManager(),
                            HISTORY_PREVIEW_DIALOG);
                }
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
                                            fragments[position])
                            ).commit();

                    mDrawerList.setItemChecked(position, true);
                    setTitle(mDrawerItems[position]);
                } else {
                    DialogFragment listsPreviewDialog = new ListsUpsellDialogFragment();
                    listsPreviewDialog.show(getSupportFragmentManager(),
                            LISTS_PREVIEW_DIALOG);
                }
                break;
            case 4: // Log out
                DialogFragment confirmationDialog = new LogOutDialogFragment();
                confirmationDialog.show(getSupportFragmentManager(),
                        LOGOUT_CONFIRMATION_DIALOG);
                break;
            case 5: // Settings
                Intent settingsIntent;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD)
                    settingsIntent = new Intent(getApplicationContext(), SettingsActivity.class);
                else
                    settingsIntent = new Intent(getApplicationContext(), SettingsLegacyActivity.class);

                if (Util.IsProInstalled(getApplicationContext())) {
                    settingsIntent.putExtra(SettingsActivity.EXTRA_IS_PRO, Util.IsProInstalled(getApplicationContext()));
                }
                startActivity(settingsIntent);

                mDrawerList.setItemChecked(0, true);
                break;
            default: // About and others
                fragmentManager
                        .beginTransaction()
                        .replace(
                                R.id.content_frame,
                                Fragment.instantiate(MainActivity.this,
                                        fragments[position])
                        ).commit();

                mDrawerList.setItemChecked(position, true);
                setTitle(mDrawerItems[position]);
                break;
        }

		mDrawerLayout.closeDrawer(mDrawerList);
	}

	public void CheckinUser(String id, String name) {
        if(Util.HasInternet(mContext)) {
            mFoursquareTasks.new PostUserCheckin(id, name, "", currentLocation).execute();
            finish();
        } else
            NoInternet();
	}

    private void NoInternet() {
        Toast.makeText(mContext, R.string.alert_no_internet, Toast.LENGTH_SHORT).show();
    }

    public void onEvent(VenueSearchEvent event) {
        if(Util.HasInternet(mContext))
            mFoursquareTasks.new GetClosestVenuesNew(event.searchQuery, event.searchLocation, event.duplicateVenueId, event.listType).execute();
        else
            NoInternet();
    }

    public void onEvent(GetUserInfoEvent event) {
        if(event != null) {
            if(event.user != null) {
                CompactFoursquareUser user = event.user;

                if(user.id != null)
                    PreferencesHelper.setFoursquareUserId(mContext, user.id);

                PreferencesHelper.setFoursquareSuperuserLevel(mContext, user.superuser);

                if(user.homeCity != null)
                    PreferencesHelper.setFoursquareHomeCity(mContext, user.homeCity);
            }
        }
    }
}