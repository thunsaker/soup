package com.thunsaker.soup.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import com.thunsaker.soup.R;
import com.thunsaker.soup.FoursquareHelper;
import com.thunsaker.soup.PreferencesHelper;
import com.thunsaker.soup.classes.foursquare.Category;
import com.thunsaker.soup.classes.foursquare.CompactVenue;
import com.thunsaker.soup.classes.foursquare.FoursquareImage;
import com.thunsaker.soup.classes.foursquare.Venue;
import com.thunsaker.soup.util.foursquare.VenueEndpoint;

import java.util.List;

/*
 * Created by @thunsaker
 */
/**
 * A fragment representing a single Venue detail screen. This fragment is either
 * contained in a {@link VenueListActivity} in two-pane mode (on tablets) or a
 * {@link com.thunsaker.soup.ui.VenueDetailActivity} on handsets.
 */
public class VenueDetailFragment extends Fragment {
	public static final String ARG_ITEM_JSON_STRING = "item_json_string";
	public static final String VENUE_EDIT_EXTRA = "compact_venue_original";
	protected static final String FLAG_VENUE_DIALOG = "FLAG_VENUE_DIALOG";
	protected static final String FLAG_DUPLICATE_VENUE_DIALOG = "FLAG_DUPLICATE_VENUE_DIALOG";
	final static String ORIGINAL_LOCATION_SELECTED_EXTRA = "ORIGINAL_LOCATION_SELECTED_EXTRA";

	public static CompactVenue currentCompactVenue;
	public static Venue currentVenue;
	public static Integer currentFlagItem = 0;

	private static GoogleMap mMap;
	public final static double mapMarkerAdjustment = 0.0015;
	protected static final int EDIT_VENUE = 0;
	protected static final int FLAG_DUPLICATE = 1;
	protected static final int REQUEST_SIGNIN_TO_EDIT = 3;

	protected static final float MAP_ZOOMED_OUT_LEVEL = 12;
	protected static final float MAP_DEFAULT_ZOOM_LEVEL = 16;

	static CompactVenue duplicateResultVenue;
	static int duplicateResultType = 0;
	static String originalId;
	static String duplicateId;
	static boolean showConfirmDialog = false;
	static boolean zoomedOut = false;

	public VenueDetailFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setHasOptionsMenu(true);
		getActivity().setProgressBarVisibility(false);

		if (VenueDetailActivity.venueIdToLoad.length() > 0) {
			currentCompactVenue = null;
			currentVenue = null;
			getActivity().setProgressBarVisibility(true);
			new VenueEndpoint.GetVenue(getActivity().getApplicationContext(),
					VenueDetailActivity.venueIdToLoad, getActivity(),
					FoursquareHelper.CALLER_SOURCE_DETAILS).execute();
		} else if (getActivity().getIntent().hasExtra(
				VenueDetailActivity.VENUE_URL_TO_LOAD_EXTRA)) {
			currentCompactVenue = null;
			currentVenue = null;
			getActivity().setProgressBarVisibility(true);
			String venueUrlLoad = getActivity().getIntent().getStringExtra(
					VenueDetailActivity.VENUE_URL_TO_LOAD_EXTRA);
			new VenueEndpoint.GetVenue(getActivity().getApplicationContext(),
					venueUrlLoad, getActivity(),
					FoursquareHelper.CALLER_SOURCE_DETAILS_INTENT).execute();
		} else if (getArguments().containsKey(ARG_ITEM_JSON_STRING)) {
			currentCompactVenue = CompactVenue
					.GetCompactVenueFromJson(getArguments().getString(
							ARG_ITEM_JSON_STRING));
			getActivity().setProgressBarVisibility(true);
			new VenueEndpoint.GetVenue(getActivity().getApplicationContext(),
					currentCompactVenue.getId(), getActivity(),
					FoursquareHelper.CALLER_SOURCE_DETAILS).execute();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_venue_detail,
				container, false);

		if (currentCompactVenue != null || currentVenue != null) {
			LoadDetails(rootView, this.getActivity());
		}

		return rootView;
	}

	public static void LoadDetails(View rootView, FragmentActivity theActivity) {
		try {
			List<Category> myCategories;
			TextView nameTextView = (TextView) rootView.findViewById(R.id.textViewVenueName);

			if (currentVenue != null) {
				nameTextView.setText(currentVenue.getName());
				((TextView) rootView.findViewById(R.id.textViewVenueAddress))
						.setText(currentVenue.getLocation().getAddress());
				((TextView) rootView
						.findViewById(R.id.textViewVenueAddressLine2))
						.setText(currentVenue.getLocation()
								.getCityStatePostalCode());
				((TextView) rootView
						.findViewById(R.id.textViewVenueCrossStreet))
						.setText(currentVenue.getLocation().getCrossStreet());
				myCategories = currentVenue.getCategories() != null ? currentVenue
						.getCategories() : null;
			} else {
				nameTextView.setText(currentCompactVenue.getName());
				((TextView) rootView.findViewById(R.id.textViewVenueAddress))
						.setText(currentCompactVenue.getLocation().getAddress());
				((TextView) rootView
						.findViewById(R.id.textViewVenueAddressLine2))
						.setText(currentCompactVenue.getLocation()
								.getCityStatePostalCode());
				((TextView) rootView
						.findViewById(R.id.textViewVenueCrossStreet))
						.setText(currentCompactVenue.getLocation()
								.getCrossStreet());
				myCategories = currentCompactVenue.getCategories() != null ? currentCompactVenue
						.getCategories() : null;
			}

			nameTextView.setVisibility(View.GONE);

			final ImageView primaryCategoryImageView = (ImageView) rootView
					.findViewById(R.id.imageViewVenueCategoryPrimary);
			final ImageView secondaryCategoryImageView = (ImageView) rootView
					.findViewById(R.id.imageViewVenueCategorySecondary);
			final ImageView tertiaryCategoryImageView = (ImageView) rootView
					.findViewById(R.id.imageViewVenueCategoryTertiary);

			String primaryImageUrl = "";

			if (myCategories != null) {
				primaryImageUrl = myCategories.get(0) != null ? myCategories
						.get(0)
						.getIcon()
						.getFoursquareLegacyImageUrl(
								FoursquareImage.SIZE_MEDIANO) : "";

				if (primaryImageUrl != "")
					UrlImageViewHelper.setUrlDrawable(primaryCategoryImageView,
							primaryImageUrl,
							R.drawable.foursquare_generic_category_icon);
				else
					primaryCategoryImageView
							.setImageResource(R.drawable.foursquare_generic_category_icon);
			} else {
				primaryCategoryImageView
						.setImageResource(R.drawable.foursquare_generic_category_icon);
				secondaryCategoryImageView.setVisibility(View.GONE);
				tertiaryCategoryImageView.setVisibility(View.GONE);
			}
			String venueName = "";
			if (currentVenue != null && currentVenue.getName() != "")
				venueName = currentVenue.getName();
			else if (currentCompactVenue != null
					&& currentCompactVenue.getName() != "")
				venueName = currentCompactVenue.getName();
			if (venueName != "")
				theActivity.setTitle(venueName);

			// SetupButtons(rootView, theActivity);

			mMap = null;
			if (mMap == null) {
				mMap = ((SupportMapFragment) theActivity
						.getSupportFragmentManager().findFragmentById(
								R.id.map_fragment)).getMap();
				if (mMap != null) {
					LatLng currentLocation = new LatLng(33.44866, -112.06627);
					mMap.clear();
					mMap.addMarker(new MarkerOptions()
							.position(currentLocation)
							.icon(BitmapDescriptorFactory
									.fromResource(R.drawable.map_marker_orange_outline)));
					mMap.moveCamera(CameraUpdateFactory
							.newLatLng(currentLocation));
				}
			}

			if (currentVenue != null && currentVenue.getLocation() != null
					&& currentVenue.getLocation().getLatLng() != null)
				setUpMap(currentVenue.getLocation().getLatLng(), theActivity);
			else
				setUpMap(currentCompactVenue.getLocation().getLatLng(),
						theActivity);

			VenueDetailActivity.wasEdited = false;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		getActivity().getMenuInflater().inflate(R.menu.activity_venue_detail,
				menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		String myItemToSend = "";
		if (currentVenue != null) {
			myItemToSend = currentVenue.toString();
		} else {
			myItemToSend = currentCompactVenue.toString();
		}

		switch (item.getItemId()) {
		case R.id.action_foursquare:
			String canonicalUrl = "";
			if (currentCompactVenue != null
					&& currentCompactVenue.getCanonicalUrl() != null
					&& currentCompactVenue.getCanonicalUrl().length() > 0)
				canonicalUrl = currentCompactVenue.getCanonicalUrl();
			else if (currentVenue != null
					&& currentVenue.getCanonicalUrl() != ""
					&& currentVenue.getCanonicalUrl().length() > 0)
				canonicalUrl = currentVenue.getCanonicalUrl();

			if (canonicalUrl != null && canonicalUrl.length() > 0)
				startActivity(new Intent(Intent.ACTION_VIEW,
						Uri.parse(canonicalUrl)));
			else
				// Crouton.makeText(getActivity(),
				// R.string.alert_error_loading_details,
				// Style.INFO).show();

				Toast.makeText(getActivity(),
						R.string.alert_error_loading_details,
						Toast.LENGTH_SHORT).show();
			break;
		case R.id.action_edit:
			if (!PreferencesHelper.getFoursquareConnected(getActivity()
					.getApplicationContext())) {
				showWelcomeActivity();
			} else {
				if (currentVenue != null) {
					Intent editVenueIntent = new Intent(getActivity()
							.getApplicationContext(),
							VenueEditTabsActivity.class);
					editVenueIntent.putExtra(VENUE_EDIT_EXTRA, myItemToSend);
					VenueEditTabsActivity.originalVenue = new Venue(
							currentVenue);
					getActivity().startActivityForResult(editVenueIntent,
							EDIT_VENUE);

					// Intent editVenueIntent =
					// new Intent(getActivity().getApplicationContext(),
					// VenueEditActivity.class);
					// editVenueIntent.putExtra(VENUE_EDIT_EXTRA, myItemToSend);
					// VenueEditActivity.originalVenue = new
					// Venue(currentVenue);
					// getActivity().startActivityForResult(editVenueIntent,
					// EDIT_VENUE);
				} else {
					// Crouton.makeText(getActivity(),
					// R.string.alert_still_loading, Style.INFO).show();
					Toast.makeText(getActivity(), R.string.alert_still_loading,
							Toast.LENGTH_SHORT).show();
				}
			}
			break;
		case R.id.action_categories:
			if (!PreferencesHelper.getFoursquareConnected(getActivity()
					.getApplicationContext())) {
				showWelcomeActivity();
			} else {
				if (currentVenue != null) {
					Intent editVenueCategoriesIntent = new Intent(getActivity()
							.getApplicationContext(),
							VenueEditCategoriesActivity.class);
					editVenueCategoriesIntent.putExtra(VENUE_EDIT_EXTRA,
							myItemToSend);
					getActivity().startActivity(editVenueCategoriesIntent);
				} else {
					// Crouton.makeText(getActivity(),
					// R.string.alert_still_loading,
					// Style.INFO).show();
					Toast.makeText(getActivity(), R.string.alert_still_loading,
							Toast.LENGTH_SHORT).show();
				}
			}
			break;
		case R.id.action_flag:
			if (!PreferencesHelper.getFoursquareConnected(getActivity()
					.getApplicationContext())) {
				showWelcomeActivity();
			} else {
				if (currentVenue != null) {
					DialogFragment flagDialog = new FlagVenueDialogFragment();
					flagDialog.show(getActivity().getSupportFragmentManager(),
							FLAG_VENUE_DIALOG);
				} else {
					// Crouton.makeText(getActivity(),
					// R.string.alert_still_loading,
					// Style.INFO).show();

					Toast.makeText(getActivity(), R.string.alert_still_loading,
							Toast.LENGTH_SHORT).show();
				}
			}
			break;
		case R.id.action_duplicate:
			if (!PreferencesHelper.getFoursquareConnected(getActivity()
					.getApplicationContext())) {
				showWelcomeActivity();
			} else {
				if (currentVenue != null) {
					OpenSearchDuplicateDialog();
				} else {
					// Crouton.makeText(getActivity(),
					// R.string.alert_still_loading,
					// Style.INFO).show();
					Toast.makeText(getActivity(), R.string.alert_still_loading,
							Toast.LENGTH_SHORT).show();
				}
			}
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	private void showWelcomeActivity() {
		Intent welcomeActivity = new Intent(getActivity()
				.getApplicationContext(), WelcomeActivity.class);
		// welcomeActivity.putExtra(WelcomeActivity.EXTRA_VENUE_BEFORE_AUTH,
		// currentCompactVenue.toString());
		welcomeActivity.putExtra(
				WelcomeActivity.EXTRA_VENUE_ID_BEFORE_AUTH,
				currentVenue != null ? currentVenue.getId()
						: currentCompactVenue != null ? currentCompactVenue
								.getId() : 0);
		getActivity().startActivity(welcomeActivity);
		getActivity().finish();
	}

	private void OpenSearchDuplicateDialog() {
		try {
			Intent mySearchForDuplicateIntent = new Intent(getActivity()
					.getApplicationContext(), VenueSearchActivity.class);
			mySearchForDuplicateIntent.setAction(Intent.ACTION_SEARCH);
			String myVenueName = currentVenue != null ? currentVenue.getName()
					: currentCompactVenue.getName();
			VenueListFragment.searchQuery = myVenueName;
			mySearchForDuplicateIntent.putExtra(SearchManager.QUERY,
					myVenueName);

			String myVenueLocation = currentVenue != null ? currentVenue
					.getLocation().getLatLngString() : currentCompactVenue
					.getLocation().getLatLngString();
			VenueListFragment.searchQueryLocation = myVenueLocation;
			mySearchForDuplicateIntent.putExtra(
					FoursquareHelper.SEARCH_LOCATION, myVenueLocation);

			VenueListFragment.isDuplicateSearching = true;
			mySearchForDuplicateIntent.putExtra(
					FoursquareHelper.SEARCH_DUPLICATE, true);

			startActivityForResult(mySearchForDuplicateIntent,
					VenueDetailFragment.FLAG_DUPLICATE);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void setUpMap(LatLng myLatLng, FragmentActivity myActivity) {
		try {
			// final SherlockFragmentActivity theActivity = myActivity;
			// final RelativeLayout detailLayout =
			// (RelativeLayout)
			// theActivity.findViewById(R.id.relativeLayoutVenueDetailsWrapper);

			if (mMap == null) {
				mMap = ((SupportMapFragment) myActivity
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
								public void onClick(DialogInterface dialog,
										int which) {
									getActivity()
											.setProgressBarVisibility(true);

									if (currentVenue != null) {
										new VenueEndpoint.FlagVenue(getActivity(),
												currentVenue.getId(),
												currentFlagItem, "", getActivity())
												.execute();
									} else if (VenueDetailActivity.venueIdToLoad.length() > 0) {
										new VenueEndpoint.FlagVenue(getActivity(),
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
								duplicateResultVenue.getName(),
								currentVenue.getName());
				break;
			case 1: // VenueSearchActivity.ORIGINAL_VENUE
				if (currentVenue != null)
					flagDuplicateMessage = String
							.format(getString(R.string.venue_details_dialog_flag_duplicate_message),
									currentVenue.getName(),
									duplicateResultVenue.getName());
				else
					flagDuplicateMessage = String
							.format(getString(R.string.venue_details_dialog_flag_duplicate_message),
									currentCompactVenue.getName(),
									duplicateResultVenue.getName());
				break;
			}

			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(R.string.venue_details_dialog_flag_duplicate_title)
					.setPositiveButton(R.string.dialog_yes,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									getActivity()
											.setProgressBarVisibility(true);
									new VenueEndpoint.FlagVenue(
											getActivity()
													.getApplicationContext(),
											originalId,
											FoursquareHelper.FlagType.DUPLICATE,
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
			if (currentCompactVenue != null) {
				new VenueEndpoint.GetVenue(getActivity()
						.getApplicationContext(), currentCompactVenue.getId(),
						getActivity(), FoursquareHelper.CALLER_SOURCE_DETAILS)
						.execute();
			} else if (VenueDetailActivity.venueIdToLoad.length() > 0) {
				new VenueEndpoint.GetVenue(getActivity()
						.getApplicationContext(),
						VenueDetailActivity.venueIdToLoad, getActivity(),
						FoursquareHelper.CALLER_SOURCE_DETAILS).execute();
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

		super.onResume();
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
					resultData = data
							.getStringExtra(VenueEditTabsActivity.EDIT_VENUE_RESULT);

				if (resultData == FoursquareHelper.SUCCESS) {
					new VenueEndpoint.GetVenue(getActivity()
							.getApplicationContext(),
							currentCompactVenue.getId(), getActivity(),
							FoursquareHelper.CALLER_SOURCE_DETAILS).execute();
				}
				/*
				 * Crouton.makeText(getActivity(), canEdit ?
				 * myContext.getString(R.string.edit_venue_success) :
				 * getString(R.string.edit_venue_success_propose),
				 * Style.ALERT).show(); Crouton.makeText(getActivity(),
				 * getString(R.string.edit_venue_success), Style.INFO).show();
				 * Toast.makeText(getActivity().getApplicationContext(),
				 * getString(R.string.edit_venue_success),
				 * Toast.LENGTH_SHORT).show(); } else if(resultData ==
				 * FoursquareHelper.FAIL_UNAUTHORIZED) {
				 * Crouton.makeText(getActivity(),
				 * getString(R.string.edit_venue_fail_unauthorized),
				 * Style.ALERT).show();
				 * Toast.makeText(getActivity().getApplicationContext(),
				 * getString(R.string.edit_venue_fail_unauthorized),
				 * Toast.LENGTH_SHORT).show(); } else {
				 * Crouton.makeText(getActivity(),
				 * getString(R.string.edit_venue_fail), Style.ALERT).show();
				 * Toast.makeText(getActivity().getApplicationContext(),
				 * getString(R.string.edit_venue_fail),
				 * Toast.LENGTH_SHORT).show(); }
				 */
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
						originalId = currentCompactVenue.getId();
						duplicateId = duplicateResultVenue.getId();
						break;

					case 1: // VenueSearchActivity.ORIGINAL_VENUE
						originalId = duplicateResultVenue.getId();
						duplicateId = currentCompactVenue.getId();
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
		VenueListFragment.isDuplicateSearching = false;
	}
}
