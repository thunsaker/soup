package com.thunsaker.soup.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;
import com.thunsaker.android.common.annotations.ForApplication;
import com.thunsaker.soup.PreferencesHelper;
import com.thunsaker.soup.R;
import com.thunsaker.soup.app.BaseSoupFragment;
import com.thunsaker.soup.app.SoupApp;
import com.thunsaker.soup.data.api.model.Category;
import com.thunsaker.soup.data.api.model.CompactVenue;
import com.thunsaker.soup.data.api.model.FoursquareImage;
import com.thunsaker.soup.data.api.model.TimeFrame;
import com.thunsaker.soup.data.api.model.Venue;
import com.thunsaker.soup.data.events.FlagVenueEvent;
import com.thunsaker.soup.data.events.GetVenueEvent;
import com.thunsaker.soup.data.events.GetVenueHoursEvent;
import com.thunsaker.soup.services.foursquare.FoursquarePrefs;
import com.thunsaker.soup.services.foursquare.FoursquareTasks;
import com.thunsaker.soup.util.Util;

import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

/*
 * Created by @thunsaker
 */
/**
 * A fragment representing a single Venue detail screen. This fragment is either
 * contained in a {@link VenueListFragment} in two-pane mode (on tablets) or a
 * {@link com.thunsaker.soup.ui.VenueDetailActivity} on handsets.
 */
public class VenueDetailFragment extends BaseSoupFragment implements SwipeRefreshLayout.OnRefreshListener {
    @Inject @ForApplication
    Context mContext;

    @Inject
    FoursquareTasks mFoursquareTasks;

    @Inject
    EventBus mBus;

    @InjectView(R.id.swipeLayoutVenueDetailsContainer) SwipeRefreshLayout mSwipeViewVenueDetailsContainer;
    @InjectView(R.id.relativeLayoutVenueDetailsWrapper) RelativeLayout mContentWrapper;
    @InjectView(R.id.relativeLayoutVenueWrapperInner) RelativeLayout mContentWrapperInner;

    @InjectView(R.id.imageViewVenueIcon) ImageView mVenueIcon;
    @InjectView(R.id.textViewVenueDetailsName) TextView mName;

    @InjectView(R.id.textViewVenueAddress) TextView mAddress;
    @InjectView(R.id.textViewVenueAddressLine2) TextView mAddress2;

    @InjectView(R.id.relativeLayoutVenuePhoneWrapper) RelativeLayout mPhoneWrapper;
    @InjectView(R.id.textViewVenuePhone) TextView mPhone;
    @InjectView(R.id.relativeLayoutVenueTwitterWrapper) RelativeLayout mTwitterWrapper;
    @InjectView(R.id.textViewVenueTwitter) TextView mTwitter;
    @InjectView(R.id.relativeLayoutVenueUrlWrapper) RelativeLayout mUrlWrapper;
    @InjectView(R.id.textViewVenueUrl) TextView mUrl;
    @InjectView(R.id.textViewVenueCrossStreet) TextView mCrossStreet;

    @InjectView(R.id.relativeLayoutVenueDescription) RelativeLayout mDescriptionWrapper;
    @InjectView(R.id.progressBarVenueDescription) ProgressBar mDescriptionProgress;
    @InjectView(R.id.textViewVenueDescription) TextView mDescription;
    @InjectView(R.id.relativeLayoutVenueDetailError) RelativeLayout mDetailError;

    @InjectView(R.id.relativeLayoutVenueHoursWrapper) RelativeLayout mHoursWrapper;
    @InjectView(R.id.linearLayoutHoursListContainer) LinearLayout mHoursList;

    @InjectView(R.id.relativeLayoutVenueCategoriesWrapper) RelativeLayout mCategoryWrapper;
    @InjectView(R.id.imageViewVenueCategoryPrimary) ImageView mCategoryPrimaryIcon;
    @InjectView(R.id.textViewVenueCategoryPrimary) TextView mCategoryPrimaryText;
    @InjectView(R.id.linearLayoutVenueCategoriesList) LinearLayout mCategoryList;

    @InjectView(R.id.fragmentMapWrapper) FrameLayout mMapFragmentWrapper;

//    @InjectView(R.id.linearLayoutVenueCategorySecondary) LinearLayout mCategorySecondary;
//    @InjectView(R.id.imageViewVenueCategorySecondary) ImageView mCategorySecondaryIcon;
//    @InjectView(R.id.textViewVenueCategorySecondary) TextView mCategorySecondaryText;
//
//    @InjectView(R.id.linearLayoutVenueCategoryTertiary) LinearLayout mCategoryTertiary;
//    @InjectView(R.id.imageViewVenueCategoryTertiary) ImageView mCategoryTertiaryIcon;
//    @InjectView(R.id.textViewVenueCategoryTertiary) TextView mCategoryTertiaryText;

    @InjectView(R.id.fabEditWrapper) RelativeLayout mFabEdit;

    private static final String ARG_VENUE_ARG_TYPE = "arg_type";
    private static final String ARG_VENUE_TO_LOAD = "venue_arg";

    public static final int VENUE_TYPE_ID = 0;
    public static final int VENUE_TYPE_URL = 1;
    public static final int VENUE_TYPE_JSON = 2;

    public static final String ARG_ITEM_JSON_STRING = "item_json_string";
	public static final String VENUE_EDIT_EXTRA = "compact_venue_original";
	protected static final String FLAG_VENUE_DIALOG = "FLAG_VENUE_DIALOG";
	protected static final String FLAG_DUPLICATE_VENUE_DIALOG = "FLAG_DUPLICATE_VENUE_DIALOG";

	public static Venue currentVenue;
	public static Integer currentFlagItem = 0;

	private static GoogleMap mMap;
	public final static double mapMarkerAdjustment = 0.0010;
	protected static final int EDIT_VENUE = 0;
	protected static final int FLAG_DUPLICATE = 1;

	protected static final float MAP_ZOOMED_OUT_LEVEL = 12;
	protected static final float MAP_DEFAULT_ZOOM_LEVEL = 16;

	static CompactVenue duplicateResultVenue;
	static int duplicateResultType = 0;
	static String originalId;
	static String duplicateId;
	static boolean showConfirmDialog = false;
	static boolean zoomedOut = false;
    private boolean isExpanded = true;
    private float originalPosition;

    public SupportMapFragment mMapFragment;

    public LayoutInflater mInflater;

    public static VenueDetailFragment newInstance(String venueArg, int argType) {
        VenueDetailFragment fragment = new VenueDetailFragment();
        Bundle args = new Bundle();
        if(venueArg != null && argType > -1) {
            args.putInt(ARG_VENUE_ARG_TYPE, argType);
            args.putString(ARG_VENUE_TO_LOAD, venueArg);
        } else
            args = null;
        fragment.setArguments(args);
        return fragment;
    }

	public VenueDetailFragment() { }

	@SuppressLint("AppCompatMethod")
    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        if(getArguments() != null) {
            setHasOptionsMenu(true);
            if(getActivity() != null)
                getActivity().setProgressBarVisibility(true);

            if(mBus != null && !mBus.isRegistered(this))
                mBus.register(this);

            int mType = getArguments().getInt(ARG_VENUE_ARG_TYPE);
            String mVenueToLoad = getArguments().getString(ARG_VENUE_TO_LOAD);
            switch (mType) {
                case 1: // We have a url - from the VenueDetailReceiver (When someone selects "Edit in Soup" from share menu)
                    currentVenue = null;
                    mFoursquareTasks.new GetVenue(mVenueToLoad, FoursquarePrefs.CALLER_SOURCE_DETAILS_INTENT, true).execute();
                    break;
                case 2: // We have a Compact Venue to parse
                    if(getActivity() != null)
                        getActivity().setProgressBarVisibility(false);
                    Venue mVenue = currentVenue = Venue.ConvertCompactVenueToVenue(CompactVenue.GetCompactVenueFromJson(mVenueToLoad));
                    mFoursquareTasks.new GetVenue(mVenue.id, FoursquarePrefs.CALLER_SOURCE_DETAILS, true).execute();
                    break;
                default: // We have a venue id
                    currentVenue = null;
                    mFoursquareTasks.new GetVenue(mVenueToLoad, FoursquarePrefs.CALLER_SOURCE_DETAILS, true).execute();
                    break;
            }
        }
	}

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
        View rootView;
        rootView = inflater.inflate(R.layout.fragment_venue_detail_material_scroll, container, false);
        mInflater = inflater;
        ButterKnife.inject(this, rootView);

        if(mSwipeViewVenueDetailsContainer != null) {
            mSwipeViewVenueDetailsContainer.setOnRefreshListener(this);
            mSwipeViewVenueDetailsContainer.setColorScheme(
                    R.color.soup_green,
                    R.color.soup_blue,
                    R.color.soup_green,
                    R.color.soup_red);
            mSwipeViewVenueDetailsContainer.setRefreshing(true);
        }

        GoogleMapOptions options = new GoogleMapOptions()
                .mapType(GoogleMap.MAP_TYPE_NORMAL)
                .zoomControlsEnabled(false)
                .zoomGesturesEnabled(true)
                .compassEnabled(false)
                .rotateGesturesEnabled(false)
                .scrollGesturesEnabled(false)
                .tiltGesturesEnabled(false);

        mMapFragment = SupportMapFragment.newInstance(options);
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentMapWrapper, mMapFragment)
                .commit();
        mMapFragmentWrapper.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.fade_in));

        if (currentVenue != null)
			ReloadDetails();
		return rootView;
	}

    public void ReloadDetails() {
		try {
//            switch (type) {
//                case 1: // Secondary
//                    LoadHours();
//                    LoadDescription();
//                    break;
//                case 2: // All the things!
                    LoadMap();
                    LoadDetails();
                    LoadCategories();
                    LoadHours();
                    LoadDescription();
//                    break;
//                default: // Compact Venue
//                    LoadMap();
//                    LoadDetails();
//                    LoadCategories();
//                    break;
//            }
//
//            LoadHours();
//            LoadDescription();

            if(mContentWrapper.getVisibility() == View.GONE || mContentWrapper.getVisibility() == View.INVISIBLE) {
                mContentWrapper.setVisibility(View.VISIBLE);
                mContentWrapper.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.fade_in));
            }

            VenueDetailActivity.wasEdited = false;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    public void LoadMap() {
        if(mMapFragment != null)
            mMap = mMapFragment.getMap();

        if (mMap != null) {
            LatLng currentLocation = new LatLng(33.44866, -112.06627);
            mMap.clear();
            mMap.addMarker(new MarkerOptions()
                    .position(currentLocation)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_orange_refined_outline)));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, MAP_DEFAULT_ZOOM_LEVEL));
        }

        if (currentVenue != null && currentVenue.location != null
                && currentVenue.location.getLatLng() != null)
            setUpMap(currentVenue.location.getLatLng());
    }

    public void LoadDetails() {
        mName.setText(currentVenue.name);

        mAddress.setText(currentVenue.location.address);
        mAddress2.setText(currentVenue.location.getCityStatePostalCode());
        mCrossStreet.setText(currentVenue.location.crossStreet);

        if(mAddress.getText().length() > 0)
            mAddress.setVisibility(View.VISIBLE);
        else
            mAddress.setVisibility(View.GONE);

        if(mAddress2.getText().length() > 0)
            mAddress2.setVisibility(View.VISIBLE);
        else
            mAddress2.setVisibility(View.GONE);

        if(mCrossStreet.getText().length() > 0)
            mCrossStreet.setVisibility(View.VISIBLE);
        else
            mCrossStreet.setVisibility(View.GONE);

        if(currentVenue.contact != null) {
            mPhone.setText(
                    currentVenue.contact.formattedPhone != null
                            ? currentVenue.contact.formattedPhone
                            : currentVenue.contact.phone);
            mTwitter.setText(currentVenue.contact.twitter);
        }

        mUrl.setText(currentVenue.url != null ? currentVenue.url.replace("http://","").replace("https://","") : "");


        if(mPhone.getText().length() > 0)
            mPhoneWrapper.setVisibility(View.VISIBLE);
        else
            mPhoneWrapper.setVisibility(View.GONE);


        if(mTwitter.getText().length() > 0)
            mTwitterWrapper.setVisibility(View.VISIBLE);
        else
            mTwitterWrapper.setVisibility(View.GONE);

        if(mUrl.getText().length() > 0)
            mUrlWrapper.setVisibility(View.VISIBLE);
        else
            mUrlWrapper.setVisibility(View.GONE);
    }

    public void LoadCategories() {
        boolean isPrimarySet = false;

        List<Category> myCategories;
        myCategories = currentVenue.categories != null ? currentVenue.categories : null;

        if (myCategories != null) {
            mCategoryWrapper.setVisibility(View.VISIBLE);
            Picasso mPicasso = Picasso.with(mContext);

            for (Category cat : myCategories) {
                String imageUrl = cat.icon.getFoursquareLegacyImageUrl(FoursquareImage.SIZE_MEDIANO, false);
                if(!isPrimarySet) {
                    mCategoryList.setVisibility(View.GONE);
                    if (!imageUrl.equals("")) {
                        mPicasso.load(imageUrl).placeholder(R.drawable.foursquare_generic_category_icon).into(mVenueIcon);
                        mPicasso.load(imageUrl).placeholder(R.drawable.foursquare_generic_category_icon).into(mCategoryPrimaryIcon);

                        String name = cat.name;
                        int color = Util.GetCategoryColor(name.charAt(0), mContext);
                        mVenueIcon.setBackgroundColor(color);
                        mCategoryPrimaryText.setText(name);
                    }
                    isPrimarySet = true;
                    return;
                }
//                } else {
//                    mCategoryList.setVisibility(View.VISIBLE);
//                    if (!imageUrl.equals("")) {
//                        LinearLayout listItem = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.list_category_item_material, mCategoryList);
//                        mPicasso.load(imageUrl).placeholder(R.drawable.foursquare_generic_category_icon).into((ImageView) listItem.findViewById(R.id.category_icon));
//                        ((TextView) listItem.findViewById(R.id.category_title)).setText(cat.name);
//                    }
//                }
            }
        } else {
            mCategoryList.setVisibility(View.GONE);
            mVenueIcon.setImageResource(R.drawable.foursquare_generic_category_icon);
            mVenueIcon.setBackgroundColor(mContext.getResources().getColor(R.color.gray_light_super));
            mCategoryPrimaryIcon.setImageResource(R.drawable.foursquare_generic_category_icon);
            mCategoryPrimaryText.setText("No categories");
        }
    }

    @SuppressLint("NewApi")
    public void LoadDescription() {
        if(currentVenue.description != null) {
            if (mDescription != null) {
                mDescription.setText(currentVenue.description );

                mDescriptionWrapper.setVisibility(View.VISIBLE);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    mDescription.setAlpha(0f);
                    mDescription.setVisibility(View.VISIBLE);

                    mDescription.animate()
                            .alpha(1f)
                            .setDuration(1000)
                            .setListener(null);

                    mDescriptionProgress.animate()
                            .alpha(0f)
                            .setDuration(1000)
                            .setListener(new android.animation.AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(android.animation.Animator animation) {
                                    mDescriptionProgress.setVisibility(View.GONE);
                                }
                            });
                } else {
                    mDescriptionProgress.setVisibility(View.GONE);
                    mDescriptionProgress.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.fade_out));

                    mDescription.setVisibility(View.VISIBLE);
                    mDescription.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.fade_in));
                }
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                mDescriptionProgress.animate()
                        .alpha(0f)
                        .setDuration(1000)
                        .setListener(new android.animation.AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(android.animation.Animator animation) {
                                mDescriptionProgress.setVisibility(View.GONE);
                                mDescriptionWrapper.setVisibility(View.GONE);
                            }
                        });
            } else {
                mDescriptionProgress.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.fade_out));
                mDescriptionProgress.setVisibility(View.GONE);
                mDescriptionWrapper.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.fade_out));
                mDescriptionWrapper.setVisibility(View.GONE);
            }

        }
    }

    public void LoadHours() {
        if(currentVenue.venueHours != null && currentVenue.venueHours.timeFrames != null && currentVenue.venueHours.timeFrames.size() > 0) {
            mHoursWrapper.setVisibility(View.VISIBLE);
            if(mHoursList.getChildCount() == currentVenue.venueHours.timeFrames.size())
                return;

            int i = 0;
            for (TimeFrame time : currentVenue.venueHours.timeFrames) {
                assert mInflater != null;
                LinearLayout listItem = (LinearLayout)mInflater.inflate(R.layout.list_hours_item_single_line, null);
                ((TextView)listItem.findViewById(R.id.textViewVenueDetailsHoursDay)).setText(time.daysString);
                ((TextView)listItem.findViewById(R.id.textViewVenueDetailsHoursTime)).setText(time.openTimesString);
                mHoursList.addView(listItem, i);
                i++;
            }
        } else {
            mHoursWrapper.setVisibility(View.GONE);
        }
    }

    @Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        if(getActivity() != null)
		    getActivity().getMenuInflater().inflate(R.menu.activity_venue_detail, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().finish();
                break;
            case R.id.action_duplicate:
                OpenDuplicateSearch();
                break;
            case R.id.action_flag:
                ShowFlagDialog();
                break;
            case R.id.action_categories:
                OpenEditCategories();
                break;
            case R.id.action_foursquare:
                OpenFoursquareApp();
                break;
		}

		return true;
	}

    public void OpenFoursquareApp() {
        String canonicalUrl = "";
        if (currentVenue != null
                && currentVenue.canonicalUrl != null
                && !currentVenue.canonicalUrl.equals("")
                && currentVenue.canonicalUrl.length() > 0)
            canonicalUrl = currentVenue.canonicalUrl;

        if (canonicalUrl != null && canonicalUrl.length() > 0)
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(canonicalUrl)));
        else
            Toast.makeText(mContext, R.string.alert_error_loading_details, Toast.LENGTH_SHORT).show();
    }

    public void OpenDuplicateSearch() {
        if (!PreferencesHelper.getFoursquareConnected(mContext)) {
            showWelcomeActivity();
        } else {
            if (currentVenue != null) {
                OpenSearchDuplicateDialog();
            } else {
                Toast.makeText(mContext, R.string.alert_still_loading,
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void ShowFlagDialog() {
        if (!PreferencesHelper.getFoursquareConnected(mContext)) {
            showWelcomeActivity();
        } else {
            if (currentVenue != null) {
                DialogFragment flagDialog = new FlagVenueDialogFragment();
                flagDialog.show(getActivity().getSupportFragmentManager(),
                        FLAG_VENUE_DIALOG);
            } else {
                Toast.makeText(mContext, R.string.alert_still_loading,
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void OpenEditCategories() {
        if (!PreferencesHelper.getFoursquareConnected(mContext)) {
            showWelcomeActivity();
        } else {
            if (currentVenue != null) {
                Intent editVenueCategoriesIntent = new Intent(mContext, VenueEditCategoriesActivity.class);
                editVenueCategoriesIntent.putExtra(VENUE_EDIT_EXTRA, currentVenue.toString());
                getActivity().startActivity(editVenueCategoriesIntent);
            } else {
                Toast.makeText(mContext, R.string.alert_still_loading, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void OpenEditVenue() {
        if (!PreferencesHelper.getFoursquareConnected(mContext)) {
            showWelcomeActivity();
        } else {
            if (currentVenue != null) {
                Intent editVenueIntent = new Intent(mContext, VenueEditTabsActivity.class);
                editVenueIntent.putExtra(VENUE_EDIT_EXTRA, currentVenue.toString());
                getActivity().startActivityForResult(editVenueIntent, EDIT_VENUE);
            } else {
                Toast.makeText(mContext, R.string.alert_still_loading, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showWelcomeActivity() {
		Intent welcomeActivity = new Intent(getActivity()
				.getApplicationContext(), WelcomeActivity.class);
		// welcomeActivity.putExtra(WelcomeActivity.EXTRA_VENUE_BEFORE_AUTH,
		// currentCompactVenue.toString());
		welcomeActivity.putExtra(
				WelcomeActivity.EXTRA_VENUE_ID_BEFORE_AUTH, currentVenue != null ? currentVenue.id : "");
		startActivity(welcomeActivity);
		getActivity().finish();
	}

	private void OpenSearchDuplicateDialog() {
		try {
			Intent mySearchForDuplicateIntent = new Intent(mContext, VenueSearchActivity.class)
                    .setAction(Intent.ACTION_SEARCH);

			String myVenueName = currentVenue != null ? currentVenue.name : "";
			mySearchForDuplicateIntent.putExtra(SearchManager.QUERY, myVenueName);

			String myVenueLocation =
                    currentVenue != null
                            ? currentVenue.location.getLatLngString()
                            : "";
			mySearchForDuplicateIntent.putExtra(FoursquarePrefs.SEARCH_LOCATION, myVenueLocation);
			mySearchForDuplicateIntent.putExtra(FoursquarePrefs.SEARCH_DUPLICATE, true);
            mySearchForDuplicateIntent.putExtra(
                    FoursquarePrefs.SEARCH_DUPLICATE_VENUE_ID, currentVenue != null
                            ? currentVenue.id : 0);

			startActivityForResult(mySearchForDuplicateIntent,VenueDetailFragment.FLAG_DUPLICATE);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void setUpMap(LatLng myLatLng) {
		try {
			if (mMap == null) {
				mMap = mMapFragment.getMap();
			}

			if (mMap != null) {
				mMap.clear();
				mMap.addMarker(new MarkerOptions().position(myLatLng).icon(
						BitmapDescriptorFactory
								.fromResource(R.drawable.map_marker_orange_outline)));

				final LatLng adjustedCurrentLocation = new LatLng(
						myLatLng.latitude + mapMarkerAdjustment,
						myLatLng.longitude);
				mMap.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(adjustedCurrentLocation, MAP_DEFAULT_ZOOM_LEVEL));

				mMap.setOnMapClickListener(new OnMapClickListener() {
					@Override
					public void onMapClick(LatLng newPoint) {
						if (zoomedOut) {
							mMap.animateCamera(CameraUpdateFactory
									.newLatLngZoom(adjustedCurrentLocation,
											MAP_DEFAULT_ZOOM_LEVEL));
							zoomedOut = false;
						} else {
							mMap.animateCamera(CameraUpdateFactory
									.newLatLngZoom(adjustedCurrentLocation,
											MAP_ZOOMED_OUT_LEVEL));
							zoomedOut = true;
						}
					}
				});
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    @Override
    public void onRefresh() {
        RefreshVenueData();
    }

    public void RefreshVenueData() {
        if(currentVenue != null) {
            mSwipeViewVenueDetailsContainer.setRefreshing(true);
            FoursquareTasks mFoursquareTasks = new FoursquareTasks((SoupApp) mContext);
            mFoursquareTasks.new GetVenue(currentVenue.id, FoursquarePrefs.CALLER_SOURCE_DETAILS, true).execute();
        }
    }

    public static class FlagVenueDialogFragment extends DialogFragment {

        // TODO: Implement newInstance fragment pattern
		public FlagVenueDialogFragment() {
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(R.string.venue_details_dialog_flag_message)
					.setPositiveButton(
							R.string.venue_details_dialog_flag_venue,
							new DialogInterface.OnClickListener() {
								@SuppressLint("AppCompatMethod")
                                @Override
								public void onClick(DialogInterface dialog, int which) {
									if(getActivity() != null)
                                        getActivity().setProgressBarVisibility(true);

                                    String venueId = "";
                                    if (currentVenue != null)
                                        venueId = currentVenue.id;
                                    else if (VenueDetailActivity.venueIdToLoad.length() > 0)
                                        venueId = VenueDetailActivity.venueIdToLoad;

                                    ((VenueDetailActivity)getActivity()).FlagVenue(venueId, currentFlagItem);
								}
							})
					.setNegativeButton(R.string.dialog_cancel,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
								}
							})
					.setSingleChoiceItems(
							R.array.venue_details_flag_venue_categories, 0,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									currentFlagItem = which;
								}
							});
			return builder.create();
		}
	}

    public static class FlagDuplicateVenueDialogFragment extends DialogFragment {

		public FlagDuplicateVenueDialogFragment() {
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			String flagDuplicateMessage = "";
			switch (duplicateResultType) {
			case 0: // VenueSearchActivity.DUPLICATE_VENUE
				flagDuplicateMessage = String
						.format(getString(R.string.venue_details_dialog_flag_duplicate_message),
								duplicateResultVenue.name,
								currentVenue.name);
				break;
			case 1: // VenueSearchActivity.ORIGINAL_VENUE
				if (currentVenue != null)
					flagDuplicateMessage = String
							.format(getString(R.string.venue_details_dialog_flag_duplicate_message),
									currentVenue.name,
									duplicateResultVenue.name);
				break;
			}

			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(R.string.venue_details_dialog_flag_duplicate_title)
					.setPositiveButton(R.string.dialog_yes,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
                                    if(getActivity() != null)
									    getActivity().setProgressBarVisibility(true);

                                    ((VenueDetailActivity)getActivity()).FlagVenueDuplicate(originalId, currentFlagItem, duplicateId);
									ClearFlagDuplicateValues();
								}
							})
					.setNegativeButton(R.string.dialog_no,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    ClearFlagDuplicateValues();
                                    dialog.dismiss();
                                }
                            }).setMessage(Html.fromHtml(flagDuplicateMessage));
			return builder.create();
		}

		@Override
		public void show(FragmentManager manager, String tag) {
			// TODO Auto-generated method stub
			super.show(manager, tag);
		}
	}

	@Override
	public void onDestroy() {
		currentVenue = null;
		super.onDestroy();
	}

	@Override
	public void onResume() {
        super.onResume();

        if(mBus != null && !mBus.isRegistered(this))
            mBus.register(this);

		if (showConfirmDialog) {
			if (duplicateResultVenue != null && originalId != null
					&& duplicateId != null) {
				DialogFragment flagDuplicateDialog = new FlagDuplicateVenueDialogFragment();
				flagDuplicateDialog.show(getActivity()
						.getSupportFragmentManager(),
						FLAG_DUPLICATE_VENUE_DIALOG);
			}
			showConfirmDialog = false;
		} else {
            FoursquareTasks mFoursquareTasks = new FoursquareTasks((SoupApp) mContext);
			if (currentVenue != null) {
				mFoursquareTasks.new GetVenue(currentVenue.id, FoursquarePrefs.CALLER_SOURCE_DETAILS, true).execute();
			} else if (VenueDetailActivity.venueIdToLoad.length() > 0) {
				mFoursquareTasks.new GetVenue(VenueDetailActivity.venueIdToLoad, FoursquarePrefs.CALLER_SOURCE_DETAILS, true).execute();
			}
		}

		// DEBUG Email
//		if (VenueEndpoint.SEND_DEBUG_EMAIL
//				&& VenueEditTabsActivity.mDebugString != null) {
//			Intent debugIntent = new Intent(Intent.ACTION_SEND);
//			debugIntent.setType("message/rfc822");
//			debugIntent.putExtra(Intent.EXTRA_SUBJECT, "Soup Debug Data");
//			debugIntent.putExtra(Intent.EXTRA_TEXT,
//					VenueEditTabsActivity.mDebugString);
//			debugIntent.putExtra(Intent.EXTRA_EMAIL,
//					new String[] { "android+soup+debug@thomashunsaker.com" });
//			Intent mailerIntent = Intent.createChooser(debugIntent, null);
//			startActivity(mailerIntent);
//			VenueEditTabsActivity.mDebugString = null;
//		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case EDIT_VENUE:
			switch (resultCode) {
			case Activity.RESULT_OK:
				String resultData = null;
				if (data.hasExtra(VenueEditTabsActivity.EDIT_VENUE_RESULT))
					resultData = data.getStringExtra(VenueEditTabsActivity.EDIT_VENUE_RESULT);

                assert resultData != null;
                if (resultData.equals(FoursquarePrefs.SUCCESS)) {
                    FoursquareTasks mFoursquareTasks = new FoursquareTasks((SoupApp) mContext);
					mFoursquareTasks.new GetVenue(currentVenue.id, FoursquarePrefs.CALLER_SOURCE_DETAILS, true).execute();
                    mSwipeViewVenueDetailsContainer.setRefreshing(true);
				}
				break;
			default:
				break;
			}
			break;
		case FLAG_DUPLICATE:
			switch (resultCode) {
			case Activity.RESULT_OK:
				if (currentVenue != null) {
					duplicateResultVenue = null;
					duplicateResultType = 0;
					originalId = null;
					duplicateId = null;

					if (data.hasExtra(VenueSearchActivity.SELECTED_DUPLICATE_VENUE))
						duplicateResultVenue = CompactVenue
								.GetCompactVenueFromJson(data
										.getStringExtra(VenueSearchActivity.SELECTED_DUPLICATE_VENUE));

					if (data.hasExtra(VenueSearchActivity.SELECTED_DUPLICATE_VENUE_TYPE))
						duplicateResultType = data
								.getIntExtra(
										VenueSearchActivity.SELECTED_DUPLICATE_VENUE_TYPE,
										0);

					switch (duplicateResultType) {
					case 0: // VenueSearchActivity.DUPLICATE_VENUE
						originalId = currentVenue.id;
						duplicateId = duplicateResultVenue.id;
						break;

					case 1: // VenueSearchActivity.ORIGINAL_VENUE
						originalId = duplicateResultVenue.id;
						duplicateId = currentVenue.id;
						break;
					}

					if (duplicateResultVenue != null && originalId != null
							&& duplicateId != null) {
						showConfirmDialog = true;
					}
				}
				break;
			default:
				ClearFlagDuplicateValues();
				break;
			}
			break;
		}
	}

	protected static void ClearFlagDuplicateValues() {
		originalId = "";
		duplicateId = "";
		duplicateResultType = 0;
		duplicateResultVenue = null;
	}

    public void onEvent(GetVenueEvent event) {
        mSwipeViewVenueDetailsContainer.setRefreshing(false);

        boolean error = false;
        if(event != null) {
            if(event.resultVenue != null) {
                currentVenue = event.resultVenue;
                ReloadDetails();
            } else {
                error = true;
            }
        } else {
            error = true;
        }

        if(error) {
            mFabEdit.setVisibility(View.GONE);
            setHasOptionsMenu(false);
            mContentWrapperInner.setVisibility(View.GONE);
            mDetailError.setVisibility(View.VISIBLE);
        } else {
            mFabEdit.setVisibility(View.VISIBLE);
            setHasOptionsMenu(true);
            mContentWrapperInner.setVisibility(View.VISIBLE);
            mDetailError.setVisibility(View.GONE);
        }
    }

    public void onEvent(GetVenueHoursEvent event) {
        if(getActivity() != null)
            getActivity().setProgressBarVisibility(false);

        mSwipeViewVenueDetailsContainer.setRefreshing(false);

        if(event != null) {
            if(event.resultVenueHours != null) {
                if(event.resultVenueHours.size() > 0) {
                    currentVenue.venueHours.timeFrames = TimeFrame.MergeVenueHoursTimeFrames(currentVenue.venueHours.timeFrames, event.resultVenueHours, mContext);
                    LoadHours();
                } else {
                    mHoursWrapper.setVisibility(View.GONE);
                }
            }
        }
    }

    public void onEvent(FlagVenueEvent event) {
        mSwipeViewVenueDetailsContainer.setRefreshing(false);
        String message;
        if (event != null) {
            if (event.result != null) {
                Toast.makeText(mContext, mContext.getString(R.string.flag_venue_success), Toast.LENGTH_SHORT).show();
            } else {
                message = event.resultMessage;
                Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
            }
        } else {
            message = mContext.getString(R.string.flag_venue_fail);
            Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.relativeLayoutVenuePhoneWrapper)
    public void CallVenue() {
        Intent openDialerIntent =
                new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + currentVenue.contact.phone))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(openDialerIntent);
    }

    @OnClick(R.id.relativeLayoutVenueUrlWrapper)
    public void OpenUrl() {
        Intent browserIntent =
                new Intent(Intent.ACTION_VIEW, Uri.parse(currentVenue.url))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(browserIntent);
    }

    @OnClick(R.id.relativeLayoutVenueTwitterWrapper)
    public void OpenTwitter() {
        Intent twitterIntent =
                new Intent(Intent.ACTION_VIEW,
                    Uri.parse(String.format(
                            getString(R.string.twitter_url_base),
                            currentVenue.contact.twitter)))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(twitterIntent);
    }

    @OnClick(R.id.linearLayoutAddressWrapper)
    public void OpenMap() {
        Intent mapIntent = new Intent(Intent.ACTION_VIEW)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        //geo:lat,long?z=zoom or geo:0,0?q=lat,lng(label)
                .setData(Uri.parse(String.format(
                        "geo:0,0?q=%s,%s(%s)&z=%s",
                        currentVenue.location.latitude,
                        currentVenue.location.longitude,
                        Util.Encode(currentVenue.name),
                        MAP_DEFAULT_ZOOM_LEVEL)));
        startActivity(mapIntent);
    }

    @OnClick(R.id.fabEdit)
    public void FabEditClick() {
        OpenEditVenue();
    }
}