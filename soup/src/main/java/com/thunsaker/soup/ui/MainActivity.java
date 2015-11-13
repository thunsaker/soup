package com.thunsaker.soup.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.thunsaker.android.common.annotations.ForApplication;
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
import com.thunsaker.soup.util.Util;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.event.EventBus;

public class MainActivity extends BaseSoupActivity implements
        NavigationView.OnNavigationItemSelectedListener,
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

    @InjectView(R.id.drawer_layout) DrawerLayout mDrawerLayout;

    @InjectView(R.id.toolbar) Toolbar mToolbar;

    @InjectView(R.id.nav_view) NavigationView mNavView;

	private final static Uri soupProUri = Uri.parse("market://details?id=com.thunsaker.soup.pro");

	public static boolean isFoursquareConnected = false;
	static FoursquareClient mFoursquareClient;

	public static LatLng currentLocation;

//	private AdView adView;

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

//        if(!BuildConfig.DEBUG)
//            Crashlytics.start(this);

        mBus.register(this);

        handleIntent(getIntent());

        setContentView(R.layout.activity_nav);
        ButterKnife.inject(this);

        setSupportActionBar(mToolbar);
//        setupDrawerContent(mNavView);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Search!", null).show();
            }
        });

        DrawerLayout drawer =
                (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle =
                new ActionBarDrawerToggle(this, drawer, mToolbar,
                        R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mFoursquareClient = new FoursquareClient(
                AuthHelper.FOURSQUARE_CLIENT_ID,
                AuthHelper.FOURSQUARE_CLIENT_SECRET,
                AuthHelper.FOURSQUARE_CALLBACK_URL);
        isFoursquareConnected = PreferencesHelper
                .getFoursquareConnected(getApplicationContext());

        if (isFoursquareConnected) {
            if (Util.IsProInstalled(getApplicationContext())) {
                hideAds();
                setTitle(R.string.app_name_pro);
            } else {
                showAds();
            }

            selectDrawerItem(null);

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

    private void selectDrawerItem(MenuItem menuItem) {
        Fragment fragment = null;
        Class fragmentClass = VenueListFragment.class;

        if (menuItem != null) {
            switch (menuItem.getItemId()) {
                case R.id.nav_venues:
                    fragmentClass = VenueListFragment.class;
                    break;
                case R.id.nav_history:
                    if (Util.IsProInstalled(mContext)) {
                        startActivity(new Intent(mContext, CheckinHistoryActivity.class));
                        menuItem.setChecked(true);
                    } else {
                        DialogFragment historyPreviewDialog = new HistoryUpsellDialogFragment();
                        historyPreviewDialog.show(getSupportFragmentManager(),
                                HISTORY_PREVIEW_DIALOG);
                    }
                    return;
                case R.id.nav_lists:
                    if (Util.IsProInstalled(getApplicationContext())) {
                        fragmentClass = ListsFragment.class;
                    } else {
                        DialogFragment listsPreviewDialog = new ListsUpsellDialogFragment();
                        listsPreviewDialog.show(getSupportFragmentManager(),
                                LISTS_PREVIEW_DIALOG);
                        return;
                    }
                    break;
                case R.id.nav_about:
                    fragmentClass = AboutFragment.class;
                    break;
                case R.id.nav_logout:
                    DialogFragment confirmationDialog = new LogOutDialogFragment();
                    confirmationDialog.show(getSupportFragmentManager(),
                            LOGOUT_CONFIRMATION_DIALOG);
                    break;
            }

            menuItem.setChecked(true);
            setTitle(menuItem.getTitle());
        }

        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();

        mDrawerLayout.closeDrawers();
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

    private void showAds() {
//        adView = new AdView(this);
//        adView.setAdSize(AdSize.BANNER);
//        adView.setAdUnitId(getString(R.string.admob_id));
//        LinearLayout adLayout = (LinearLayout)findViewById(R.id.adViewLayoutWrapper);
//        adLayout.addView(adView);
//
//        AdRequest adRequest = new AdRequest.Builder()
//                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
//                .addTestDevice("2B0F45ECB7E319BC2500CD6AFF1353CC") // N7 - 4.4.4
//                .addTestDevice("D50AF454D0BF794B6A38811EEA1F21EE") // GS3 - 4.3
//                .addTestDevice("6287716BED76BCE3BB981DD19AA858E1") // GS3 - 4.1
//                .addTestDevice("FDC26B2E6C049E2E9ECE7C97D42A4726") // G2 - 2.3.4
//                .addTestDevice("1BF36BBC3C197AFF96AF3F9F305CAD48") // N5 - L
//                .addTestDevice("D8FE76757F1CA9B485916499EC8C13DB") // MAXX - 4.4.2
//                .addTestDevice("B8588B68EF8E1193C85527B332B413D9") // S6 - 5.1.1
//                .build();
//
//        if(adView != null)
//            adView.loadAd(adRequest);
    }

    private void hideAds() {
//        LinearLayout adLayout = (LinearLayout) findViewById(R.id.adViewLayoutWrapper);
//        adLayout.setVisibility(View.GONE);
//
//        if (adView != null)
//            adView.destroy();
    }

    private void ShowNavDrawer(boolean show) {
		if(!show) {
			mDrawerLayout.openDrawer(GravityCompat.START);
			PreferencesHelper.setShownNavDrawer(getApplicationContext(), true);
		}
	}

    @Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.action_search:
                Intent searchActivity = new Intent(getApplicationContext(), VenueSearchActivity.class);
                startActivity(searchActivity);
                return true;
            default:
                return false;
		}
	}

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
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

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        selectDrawerItem(item);
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
									FoursquarePrefs
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

//        if (adView != null) {
//            adView.pause();
//        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
//        if (adView != null) {
//            hideAds();
//        }
        super.onDestroy();
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