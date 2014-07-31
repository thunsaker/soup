package com.thunsaker.soup.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.thunsaker.android.common.annotations.ForApplication;
import com.thunsaker.soup.PreferencesHelper;
import com.thunsaker.soup.R;
import com.thunsaker.soup.app.BaseSoupFragment;
import com.thunsaker.soup.app.SoupApp;
import com.thunsaker.soup.data.api.model.Category;
import com.thunsaker.soup.data.api.model.CompactVenue;
import com.thunsaker.soup.data.api.model.FoursquareImage;
import com.thunsaker.soup.data.api.model.Venue;
import com.thunsaker.soup.data.events.GetVenueEvent;
import com.thunsaker.soup.services.foursquare.FoursquarePrefs;
import com.thunsaker.soup.services.foursquare.FoursquareTasks;
import com.thunsaker.soup.services.foursquare.endpoints.VenueEndpoint;

import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.event.EventBus;

/*
 * Created by @thunsaker
 */
/**
 * A fragment representing a single Venue detail screen. This fragment is either
 * contained in a {@link VenueListFragment} in two-pane mode (on tablets) or a
 * {@link com.thunsaker.soup.ui.VenueDetailActivity} on handsets.
 */
public class VenueDetailFragment extends BaseSoupFragment {
    @Inject @ForApplication
    Context mContext;

    @Inject
    EventBus mBus;

    @InjectView(R.id.textViewVenueName) TextView mName;
    @InjectView(R.id.textViewVenueAddress) TextView mAddress;
    @InjectView(R.id.textViewVenueAddressLine2) TextView mAddress2;
    @InjectView(R.id.textViewVenueCrossStreet) TextView mCrossStreet;
    @InjectView(R.id.imageViewVenueCategoryPrimary) ImageView mCategoryPrimary;
    @InjectView(R.id.linearLayoutVenueDescription) LinearLayout mDescriptionWrapper;
    @InjectView(R.id.progressBarVenueDescription) ProgressBar mDescriptionProgress;
    @InjectView(R.id.textViewVenueDescription) TextView mDescription;
    @InjectView(R.id.relativeLayoutVenueDetailError) RelativeLayout mDetailError;

    private static final String ARG_VENUE_ARG_TYPE = "arg_type";
    private static final String ARG_VENUE_TO_LOAD = "venue_arg";

    public static final int VENUE_TYPE_ID = 0;
    public static final int VENUE_TYPE_URL = 1;
    public static final int VENUE_TYPE_JSON = 2;

    public static final String ARG_ITEM_JSON_STRING = "item_json_string";
	public static final String VENUE_EDIT_EXTRA = "compact_venue_original";
	protected static final String FLAG_VENUE_DIALOG = "FLAG_VENUE_DIALOG";
	protected static final String FLAG_DUPLICATE_VENUE_DIALOG = "FLAG_DUPLICATE_VENUE_DIALOG";

	public static CompactVenue currentCompactVenue;
	public static Venue currentVenue;
	public static Integer currentFlagItem = 0;

	private static GoogleMap mMap;
	public final static double mapMarkerAdjustment = 0.0015;
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

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        if(getArguments() != null) {
            setHasOptionsMenu(true);
            if(getActivity() != null)
                getActivity().setProgressBarVisibility(true);
            FoursquareTasks mFoursquareTasks = new FoursquareTasks((SoupApp) mContext);

            if(mBus != null && !mBus.isRegistered(this))
                mBus.register(this);

            int mType = getArguments().getInt(ARG_VENUE_ARG_TYPE);
            String mVenueToLoad = getArguments().getString(ARG_VENUE_TO_LOAD);
            switch (mType) {
                case 1: // We have a url - from the VenueDetailReceiver (When someone selects "Edit in Soup" from share menu)
                    currentCompactVenue = null;
                    currentVenue = null;
                    mFoursquareTasks.new GetVenue(mVenueToLoad, FoursquarePrefs.CALLER_SOURCE_DETAILS_INTENT).execute();
                    break;
                case 2: // We have a Compact Venue to parse
                    if(getActivity() != null)
                        getActivity().setProgressBarVisibility(false);
                    currentCompactVenue = CompactVenue.GetCompactVenueFromJson(mVenueToLoad);
                    Venue mVenue = currentVenue = Venue.ConvertCompactVenueToVenue(currentCompactVenue);
                    mFoursquareTasks.new GetVenue(mVenue.id, FoursquarePrefs.CALLER_SOURCE_DETAILS).execute();
                    break;
                default: // We have a venue id
                    currentCompactVenue = null;
                    currentVenue = null;
                    mFoursquareTasks.new GetVenue(mVenueToLoad, FoursquarePrefs.CALLER_SOURCE_DETAILS).execute();
                    break;
            }
        }
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
        View rootView;
        rootView = inflater.inflate(R.layout.fragment_venue_detail, container, false);
        ButterKnife.inject(this, rootView);
        if (currentCompactVenue != null || currentVenue != null)
			loadDetails();
		return rootView;
	}

	public void loadDetails() {
		try {
			List<Category> myCategories;

			if (currentVenue != null) {
				mName.setText(currentVenue.name);
				mAddress.setText(currentVenue.location.address);
                mAddress2.setText(currentVenue.location.getCityStatePostalCode());
				mCrossStreet.setText(currentVenue.location.crossStreet);
				myCategories = currentVenue.categories != null ? currentVenue.categories : null;
			} else {
                mName.setText(currentCompactVenue.name);
				mAddress.setText(currentCompactVenue.location.address);
				mAddress2.setText(currentCompactVenue.location.getCityStatePostalCode());
				mCrossStreet.setText(currentCompactVenue.location.crossStreet);
				myCategories = currentCompactVenue.categories != null ? currentCompactVenue.categories : null;
			}

            mName.setVisibility(View.GONE);

			String primaryImageUrl;

			if (myCategories != null) {
				primaryImageUrl = myCategories.get(0) != null ? myCategories
						.get(0)
						.icon
						.getFoursquareLegacyImageUrl(
								FoursquareImage.SIZE_MEDIANO) : "";

				if (!primaryImageUrl.equals(""))
					UrlImageViewHelper.setUrlDrawable(mCategoryPrimary,
							primaryImageUrl,
							R.drawable.foursquare_generic_category_icon);
				else
					mCategoryPrimary.setImageResource(R.drawable.foursquare_generic_category_icon);
			} else
				mCategoryPrimary.setImageResource(R.drawable.foursquare_generic_category_icon);

			String venueName = "";
			if (currentVenue != null && !currentVenue.name.equals(""))
				venueName = currentVenue.name;
			else if (currentCompactVenue != null
					&& !currentCompactVenue.name.equals(""))
				venueName = currentCompactVenue.name;
			if (!venueName.equals("")) {
                if(getActivity() != null)
                    getActivity().setTitle(venueName);
            }

//			mMap = null;
//			if (mMap == null) {
                if(getActivity() != null)
				    mMap = ((SupportMapFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.map_fragment)).getMap();
				if (mMap != null) {
					LatLng currentLocation = new LatLng(33.44866, -112.06627);
					mMap.clear();
					mMap.addMarker(new MarkerOptions()
							.position(currentLocation)
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker_orange_outline)));
					mMap.moveCamera(CameraUpdateFactory
							.newLatLng(currentLocation));
				}
//			}

			if (currentVenue != null && currentVenue.location != null
					&& currentVenue.location.getLatLng() != null)
				setUpMap(currentVenue.location.getLatLng());
			else
				setUpMap(currentCompactVenue.location.getLatLng());

			VenueDetailActivity.wasEdited = false;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if(getActivity() != null)
		    getActivity().getMenuInflater().inflate(R.menu.activity_venue_detail, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		String myItemToSend;
		if (currentVenue != null) {
			myItemToSend = currentVenue.toString();
		} else if(currentCompactVenue != null) {
			myItemToSend = currentCompactVenue.toString();
		} else {
            return false;
        }

		switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().finish();
//                switch (VenueDetailActivity.venueDetailsSource) {
//                    case 1: // Venue Search
//                        NavUtils.navigateUpTo(getActivity(), new Intent(mContext, VenueSearchActivity.class));
//                        break;
//                    case 2: // List
//                        getActivity().finish();
//                        break;
//                    case 3:
//                        startActivity(new Intent(mContext, MainActivity.class));
//                        getActivity().finish();
//                        break;
//                    default:
//                        break;
//                }
                break;
		case R.id.action_foursquare:
			String canonicalUrl = "";
			if (currentCompactVenue != null
					&& currentCompactVenue.canonicalUrl != null
					&& currentCompactVenue.canonicalUrl.length() > 0)
				canonicalUrl = currentCompactVenue.canonicalUrl;
			else if (currentVenue != null
					&& !currentVenue.canonicalUrl.equals("")
					&& currentVenue.canonicalUrl.length() > 0)
				canonicalUrl = currentVenue.canonicalUrl;

			if (canonicalUrl != null && canonicalUrl.length() > 0)
				startActivity(new Intent(Intent.ACTION_VIEW,
						Uri.parse(canonicalUrl)));
			else
				Toast.makeText(mContext, R.string.alert_error_loading_details, Toast.LENGTH_SHORT).show();
			break;
		case R.id.action_edit:
			if (!PreferencesHelper.getFoursquareConnected(mContext)) {
				showWelcomeActivity();
			} else {
				if (currentVenue != null) {
					Intent editVenueIntent = new Intent(mContext,
							VenueEditTabsActivity.class);
					editVenueIntent.putExtra(VENUE_EDIT_EXTRA, myItemToSend);
					VenueEditTabsActivity.originalVenue = new Venue(
							currentVenue);
					getActivity().startActivityForResult(editVenueIntent,
							EDIT_VENUE);
				} else {
					Toast.makeText(mContext, R.string.alert_still_loading,
							Toast.LENGTH_SHORT).show();
				}
			}
			break;
		case R.id.action_categories:
			if (!PreferencesHelper.getFoursquareConnected(mContext)) {
				showWelcomeActivity();
			} else {
				if (currentVenue != null) {
					Intent editVenueCategoriesIntent = new Intent(mContext,
							VenueEditCategoriesActivity.class);
					editVenueCategoriesIntent.putExtra(VENUE_EDIT_EXTRA, myItemToSend);
					getActivity().startActivity(editVenueCategoriesIntent);
				} else {
					Toast.makeText(mContext, R.string.alert_still_loading, Toast.LENGTH_SHORT).show();
				}
			}
			break;
		case R.id.action_flag:
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
			break;
		case R.id.action_duplicate:
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
			break;
		}

		return true;
	}

	private void showWelcomeActivity() {
		Intent welcomeActivity = new Intent(getActivity()
				.getApplicationContext(), WelcomeActivity.class);
		// welcomeActivity.putExtra(WelcomeActivity.EXTRA_VENUE_BEFORE_AUTH,
		// currentCompactVenue.toString());
		welcomeActivity.putExtra(
				WelcomeActivity.EXTRA_VENUE_ID_BEFORE_AUTH,
				currentVenue != null ? currentVenue.id
						: currentCompactVenue != null ? currentCompactVenue
								.id : 0);
		getActivity().startActivity(welcomeActivity);
		getActivity().finish();
	}

	private void OpenSearchDuplicateDialog() {
		try {
			Intent mySearchForDuplicateIntent = new Intent(mContext, VenueSearchActivity.class);
			mySearchForDuplicateIntent.setAction(Intent.ACTION_SEARCH);

			String myVenueName = currentVenue != null ? currentVenue.name : currentCompactVenue.name;
			mySearchForDuplicateIntent.putExtra(SearchManager.QUERY, myVenueName);

			String myVenueLocation =
                    currentVenue != null
                            ? currentVenue.location.getLatLngString()
                            : currentCompactVenue.location.getLatLngString();
			mySearchForDuplicateIntent.putExtra(FoursquarePrefs.SEARCH_LOCATION, myVenueLocation);
			mySearchForDuplicateIntent.putExtra(FoursquarePrefs.SEARCH_DUPLICATE, true);
            mySearchForDuplicateIntent.putExtra(
                    FoursquarePrefs.SEARCH_DUPLICATE_VENUE_ID, currentVenue != null
                            ? currentVenue.id
                            : currentCompactVenue.id);

			startActivityForResult(mySearchForDuplicateIntent,VenueDetailFragment.FLAG_DUPLICATE);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void setUpMap(LatLng myLatLng) {
		try {
			if (mMap == null) {
				mMap = ((SupportMapFragment) getActivity()
						.getSupportFragmentManager().findFragmentById(R.id.map))
						.getMap();
			}

			if (mMap != null) {
				mMap.clear();
				mMap.addMarker(new MarkerOptions().position(myLatLng).icon(
						BitmapDescriptorFactory
								.fromResource(R.drawable.map_marker_orange_outline)));

				final LatLng adjustedCurrentLocation = new LatLng(
						myLatLng.latitude + mapMarkerAdjustment,
						myLatLng.longitude);
				mMap.moveCamera(CameraUpdateFactory
						.newLatLng(adjustedCurrentLocation));

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

    public static class FlagVenueDialogFragment extends DialogFragment {

		public FlagVenueDialogFragment() {
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(R.string.venue_details_dialog_flag_message)
					.setPositiveButton(
							R.string.venue_details_dialog_flag_venue,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									if(getActivity() != null)
                                        getActivity().setProgressBarVisibility(true);

									if (currentVenue != null) {
										new FoursquareTasks.FlagVenue(getActivity(),
												currentVenue.id,
												currentFlagItem, "", getActivity())
												.execute();
									} else if (VenueDetailActivity.venueIdToLoad.length() > 0) {
										new FoursquareTasks.FlagVenue(getActivity(),
												VenueDetailActivity.venueIdToLoad,
												currentFlagItem, "", getActivity())
												.execute();
									}
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
				else
					flagDuplicateMessage = String
							.format(getString(R.string.venue_details_dialog_flag_duplicate_message),
									currentCompactVenue.name,
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
									new FoursquareTasks.FlagVenue(
											getActivity()
													.getApplicationContext(),
											originalId,
											FoursquarePrefs.FlagType.DUPLICATE,
											duplicateId, getActivity())
											.execute();
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
		currentCompactVenue = null;
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
			if (currentCompactVenue != null) {
				mFoursquareTasks.new GetVenue(currentCompactVenue.id, FoursquarePrefs.CALLER_SOURCE_DETAILS)
						.execute();
			} else if (VenueDetailActivity.venueIdToLoad.length() > 0) {
				mFoursquareTasks.new GetVenue(VenueDetailActivity.venueIdToLoad, FoursquarePrefs.CALLER_SOURCE_DETAILS).execute();
			}
		}

		// DEBUG Email
		if (VenueEndpoint.SEND_DEBUG_EMAIL
				&& VenueEditTabsActivity.mDebugString != null) {
			Intent debugIntent = new Intent(Intent.ACTION_SEND);
			debugIntent.setType("message/rfc822");
			debugIntent.putExtra(Intent.EXTRA_SUBJECT, "Soup Debug Data");
			debugIntent.putExtra(Intent.EXTRA_TEXT,
					VenueEditTabsActivity.mDebugString);
			debugIntent.putExtra(Intent.EXTRA_EMAIL,
					new String[] { "android+soup+debug@thomashunsaker.com" });
			Intent mailerIntent = Intent.createChooser(debugIntent, null);
			startActivity(mailerIntent);
			VenueEditTabsActivity.mDebugString = null;
		}
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
					mFoursquareTasks.new GetVenue(
							currentCompactVenue.id,
							FoursquarePrefs.CALLER_SOURCE_DETAILS).execute();
				}
				break;
			default:
				break;
			}
			break;
		case FLAG_DUPLICATE:
			switch (resultCode) {
			case Activity.RESULT_OK:
				if (currentCompactVenue != null) {
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
						originalId = currentCompactVenue.id;
						duplicateId = duplicateResultVenue.id;
						break;

					case 1: // VenueSearchActivity.ORIGINAL_VENUE
						originalId = duplicateResultVenue.id;
						duplicateId = currentCompactVenue.id;
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
        if(getActivity() != null)
            getActivity().setProgressBarVisibility(false);

        boolean error = false;
        if(event != null) {
            if(event.resultVenue != null) {
                currentVenue = event.resultVenue;
                loadDetails();
                if(event.resultVenue.description != null) {
                    if (mDescription != null) {
                        mDescriptionProgress.setVisibility(View.GONE);
                        mDescription.setText(event.resultVenue.description);
                        mDescription.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                TextView text = (TextView) v;
                                text.setMaxLines(Integer.MAX_VALUE);
                                text.setBackgroundDrawable(null);
                                text.setOnClickListener(null);
                            }
                        });

                        mDescriptionWrapper.setVisibility(View.VISIBLE);
                        mDescriptionWrapper.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.abc_fade_in));
                    }
                } else {
                    if (mDescription != null) {
                        mDescriptionProgress.setVisibility(View.GONE);
                        mDescriptionWrapper.setVisibility(View.GONE);
                    }
                }
            } else {
                error = true;
            }
        } else {
            error = true;
        }

        if(error)
            mDetailError.setVisibility(View.VISIBLE);
        else
            mDetailError.setVisibility(View.GONE);
    }
}