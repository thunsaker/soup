package com.thunsaker.soup.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.thunsaker.soup.PreferencesHelper;
import com.thunsaker.soup.R;
import com.thunsaker.soup.classes.foursquare.Category;
import com.thunsaker.soup.classes.foursquare.CompactVenue;
import com.thunsaker.soup.classes.foursquare.FoursquareImage;
import com.thunsaker.soup.util.foursquare.VenueEndpoint;

import java.util.List;

import uk.co.senab.actionbarpulltorefresh.extras.actionbarcompat.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.DefaultHeaderTransformer;
import uk.co.senab.actionbarpulltorefresh.library.Options;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

/*
 * Created by @thunsaker
 */
public class VenueListFragment extends ListFragment
        implements OnRefreshListener {

    public static VenueListAdapter currentVenueListAdapter;
    public static List<CompactVenue> currentVenueList;

    public static VenueListAdapter searchResultsVenueListAdapter;
    public static List<CompactVenue> searchResultsVenueList;

    public static VenueListAdapter searchDuplicateResultsVenueListAdapter;
    public static List<CompactVenue> searchDuplicateResultsVenueList;

    public static boolean isRefreshing = false;

    public static String searchQuery = "";
	public static String searchQueryLocation = "";
	public static boolean isSearching;
	public static boolean isDuplicateSearching;

    public static PullToRefreshLayout mPullToRefreshLayout;

	public RelativeLayout mRelativeLayoutEmptyListView;

	private static final String STATE_ACTIVATED_POSITION = "activated_position";

	private Callbacks mCallbacks = sDummyCallbacks;

	public static int mActivatedPosition = ListView.INVALID_POSITION;

    public interface Callbacks {
		public void onItemSelected(String compactVenueJson);
		public boolean onListItemLongClick(String id, String name);
	}

	private static Callbacks sDummyCallbacks = new Callbacks() {
		@Override
		public void onItemSelected(String id) { }

		@Override
		public boolean onListItemLongClick(String id, String name) {
			return true;
		}
	};

	public VenueListFragment() { }

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if(!searchQuery.equals("")) {
			if(isDuplicateSearching) {
				searchDuplicateResultsVenueListAdapter =
                        new VenueListAdapter(getActivity().getApplicationContext(),
                                R.layout.list_venue_item, searchDuplicateResultsVenueList);

	    		searchDuplicateResultsVenueList = null;
	    		searchDuplicateResultsVenueListAdapter.notifyDataSetChanged();
			} else {
				searchResultsVenueListAdapter =
                        new VenueListAdapter(getActivity().getApplicationContext(),
                                R.layout.list_venue_item, searchResultsVenueList);

	    		searchResultsVenueList = null;
	    		searchResultsVenueListAdapter.notifyDataSetChanged();
			}
    	} else {
    		currentVenueListAdapter =
                    new VenueListAdapter(getActivity().getApplicationContext(),
                            R.layout.list_venue_item, currentVenueList);

	    	if(currentVenueList != null && currentVenueList.size() > 0) {
	    		setListAdapter(currentVenueListAdapter);
	    		currentVenueListAdapter.notifyDataSetChanged();
	    	}
    	}

		if(PreferencesHelper.getFoursquareConnected(getActivity().getApplicationContext()))
			BeginUpdates();
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		if (savedInstanceState != null
				&& savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
			setActivatedPosition(savedInstanceState
					.getInt(STATE_ACTIVATED_POSITION));
		}

        super.onViewCreated(view, savedInstanceState);

		if(getListView() != null) {
			getListView().setSelector(R.drawable.layout_selector_green);

			LayoutInflater inflater = getActivity().getLayoutInflater();
			View myFooter = inflater.inflate(R.layout.list_venue_footer, null);
			getListView().addFooterView(myFooter);
			getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			    @Override
			    public boolean onItemLongClick(AdapterView<?> parent,
                                               View view,
                                               int position,
                                               long id) {
			    	CompactVenue clickedVenue =
                            (CompactVenue) getListView().getItemAtPosition(position);
			    	return mCallbacks.onListItemLongClick(
                            clickedVenue != null ? clickedVenue.getId() : null,
                            clickedVenue != null ? clickedVenue.getName() : null);
			    }
			});

            ViewGroup viewGroup = (ViewGroup) view;

            mPullToRefreshLayout = new PullToRefreshLayout(viewGroup.getContext());
            ActionBarPullToRefresh.from(getActivity())
                    .insertLayoutInto(viewGroup)
                    .theseChildrenArePullable(getListView(), getListView().getEmptyView())
                    .listener(this)
                    .options(Options.create()
                            .scrollDistance(0.40f)
                            .build())
                    .setup(mPullToRefreshLayout);

            DefaultHeaderTransformer transformer = (DefaultHeaderTransformer) mPullToRefreshLayout.getHeaderTransformer();
            transformer.setProgressBarColor(getResources().getColor(R.color.foursquare_green));

            if(!isSearching) {
                mPullToRefreshLayout.setRefreshing(true);
            }
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (!(activity instanceof Callbacks)) {
			throw new IllegalStateException(
					"Activity must implement fragment's callbacks.");
		}

		if(!isSearching) {
			setListAdapter(currentVenueListAdapter);
		}

		mCallbacks = (Callbacks) activity;
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mCallbacks = sDummyCallbacks;
	}

	@Override
	public void onListItemClick(ListView listView, View view, int position, long id) {
        try {
        	if(getListView().getFooterViewsCount() == 1
                    && position == getListView().getCount() - 1) {
        		Intent searchActivity =
                        new Intent(
                                getActivity().getApplicationContext(),
                                VenueSearchActivity.class);
    			VenueListFragment.isSearching = true;
    			VenueListFragment.searchQuery = "";
    			VenueListFragment.searchQueryLocation = "";
    			startActivity(searchActivity);
    			return;
        	}

            if(isDuplicateSearching)
                setActivateOnItemClick(true);

            if(!VenueListFragment.isRefreshing) {
                CompactVenue clickedVenue =
                        (CompactVenue) getListView().getItemAtPosition(position);
                mCallbacks.onItemSelected(clickedVenue != null ? clickedVenue.toString() : null);
            } else {
                Toast.makeText(getActivity(), R.string.alert_still_loading,
                        Toast.LENGTH_SHORT).show();
            }
        } catch (Exception ex) {
            Toast.makeText(getActivity(), R.string.alert_error_loading_venues,
                    Toast.LENGTH_SHORT).show();
            ex.printStackTrace();
        }

        super.onListItemClick(listView, view, position, id);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		if (mActivatedPosition != ListView.INVALID_POSITION) {
			outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
		}
	}

	public void setActivateOnItemClick(boolean activateOnItemClick) {
		getListView().setChoiceMode(
				activateOnItemClick ? ListView.CHOICE_MODE_SINGLE
						: ListView.CHOICE_MODE_NONE);
	}

	private void setActivatedPosition(int position) {
		if (position == ListView.INVALID_POSITION) {
			getListView().setItemChecked(mActivatedPosition, false);
		} else {
			getListView().setItemChecked(position, true);
		}

		mActivatedPosition = position;
	}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_venue_list, container, false);
    }

	public class VenueListAdapter extends ArrayAdapter<CompactVenue> {
		public List<CompactVenue> items;

		public VenueListAdapter(Context context, int textViewResourceId, List<CompactVenue> items) {
			super(context, textViewResourceId, items);
			this.items = items;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if(v == null) {
				LayoutInflater viewInflater =
                        (LayoutInflater)getActivity()
                                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = viewInflater.inflate(R.layout.list_venue_item, null);
			}
			try {
				final CompactVenue venue = items.get(position);
				if(venue != null) {
					final String myVenueName = venue.getName() != null ? venue.getName() : "";
					final String myVenueAddress = venue.getLocation().getAddress() != null
							? venue.getLocation().getAddress() : "";

					final TextView nameTextView = (TextView)v.findViewById(R.id.textViewVenueName);
					if(nameTextView != null)
						nameTextView.setText(myVenueName);

					final TextView addressTextView =
                            (TextView)v.findViewById(R.id.textViewVenueAddress);
					if(addressTextView != null)
						addressTextView.setText(myVenueAddress);

					final ImageView primaryCategoryImageView =
                            (ImageView)v.findViewById(R.id.imageViewVenueCategory);
					List<Category> myCategories = venue.getCategories();
					if(myCategories != null) {
						Category primaryCategory = myCategories.get(0) != null
                                ? myCategories.get(0)
                                : null;
						if(primaryCategoryImageView != null && primaryCategory != null) {
							String imageUrl =
                                    primaryCategory.getIcon()
                                                    .getFoursquareLegacyImageUrl(
                                                            FoursquareImage.SIZE_MEDIANO);
							UrlImageViewHelper.setUrlDrawable(
                                    primaryCategoryImageView,
                                    imageUrl,
                                    R.drawable.foursquare_generic_category_icon);
						}
					} else {
						primaryCategoryImageView.setImageResource(
                                R.drawable.foursquare_generic_category_icon);
					}

					final TextView distanceTextView =
                            (TextView) v.findViewById(R.id.textViewDistance);
					distanceTextView.setText(venue.getLocation().getDistance() + " m");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return v;
		}
	}

	private void enableLocationSettings() {
        getActivity().setProgressBarVisibility(false);

		if(MainActivity.mLocationManager != null)
			MainActivity.mLocationManager.removeUpdates(mLocationListener);

        Toast.makeText(getActivity(), R.string.alert_gps_disabled, Toast.LENGTH_SHORT).show();

		currentVenueList = null;
		currentVenueListAdapter.notifyDataSetChanged();
	}

	public final LocationListener mLocationListener = new LocationListener() {

		@Override
	    public void onLocationChanged(Location location) {
	    	LatLng newLocation = new LatLng(location.getLatitude(), location.getLongitude());
	    	MainActivity.currentLocation = newLocation;
    		if(!isSearching)
    			RefreshVenueList(VenueListFragment.this);
            else if(isSearching && (searchQueryLocation != null || searchQueryLocation != "")) {
            	MainActivity.mLocationManager = null;
            }
        }

		@Override
		public void onProviderDisabled(String arg0) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
		}
	};

	public static void RefreshVenueList(VenueListFragment theCaller) {
		try {
			if(currentVenueListAdapter == null)
				currentVenueListAdapter =
                        theCaller.new VenueListAdapter(theCaller.getActivity()
                                .getApplicationContext(), R.layout.list_venue_item, null);

			currentVenueListAdapter.notifyDataSetChanged();

			ConnectivityManager connectivityManager =
                    (ConnectivityManager) theCaller.getActivity()
                            .getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
			if (activeNetworkInfo != null) {
				theCaller.getActivity().setProgressBarVisibility(true);
				if(searchQuery != null && searchQuery.length() > 0) {
					if(searchQueryLocation != null && searchQueryLocation.length() > 0) {
						if(isDuplicateSearching) {
                            String tempId = "venue";
							new VenueEndpoint.GetClosestVenues(
                                    theCaller.getActivity().getApplicationContext(),
                                    theCaller, searchQuery, searchQueryLocation, tempId).execute();
						} else
							new VenueEndpoint.GetClosestVenues(
                                    theCaller.getActivity().getApplicationContext(), theCaller,
                                    searchQuery, searchQueryLocation, "").execute();
					} else {
						new VenueEndpoint.GetClosestVenues(
                                theCaller.getActivity().getApplicationContext(), theCaller,
                                searchQuery, searchQueryLocation, "").execute();
					}
				} else {
					if(!isSearching)
						new VenueEndpoint.GetClosestVenues(
								theCaller.getActivity().getApplicationContext(), theCaller,
								"", "", "").execute();
				}
			} else {
				mPullToRefreshLayout.setRefreshComplete();
                Toast.makeText(theCaller.getActivity(),
                        theCaller.getActivity().getString(R.string.alert_no_internet),
                        Toast.LENGTH_SHORT).show();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onStop() {
	    super.onStop();
	    if(MainActivity.mLocationManager != null)
	    	MainActivity.mLocationManager.removeUpdates(mLocationListener);
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	private void BeginUpdates() {
		try {
			if(searchQueryLocation.equals("") && !isSearching) {
				// Get the current gps position
				LocationProvider provider = null;
				Boolean hasLocationProvider = false;

				MainActivity.mLocationManager =
				        (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
				// Use network provider first
				LocationProvider networkProvider =
						MainActivity.mLocationManager.getProvider(
                                LocationManager.NETWORK_PROVIDER);

				Location lastKnown =
						MainActivity.mLocationManager.getLastKnownLocation(
                                LocationManager.PASSIVE_PROVIDER);
				double lat = lastKnown.getLatitude();
				double lon = lastKnown.getLongitude();
				MainActivity.currentLocation = new LatLng(lat, lon);

				final boolean networkEnabled =
                        MainActivity.mLocationManager.isProviderEnabled(networkProvider.getName());
				LocationProvider gpsProvider =
						MainActivity.mLocationManager.getProvider(
                                LocationManager.NETWORK_PROVIDER);
				final boolean gpsEnabled =
                        MainActivity.mLocationManager.isProviderEnabled(gpsProvider.getName());
				if (gpsEnabled) {
					provider = gpsProvider;
					hasLocationProvider = true;
				} else if(networkEnabled) {
					provider = networkProvider;
					hasLocationProvider = true;
				} else {
					hasLocationProvider = false;
				}

				if(hasLocationProvider) {
                    // Update every 5 minutes or ~1 mile
					MainActivity.mLocationManager.requestLocationUpdates(
                            provider.getName(), 300000, 1709, mLocationListener);
					if(MainActivity.currentLocation != null)
						RefreshVenueList(VenueListFragment.this);
					else {
						Toast.makeText(getActivity(), R.string.alert_current_location_unknown,
                                Toast.LENGTH_SHORT).show();
					}
				} else {
					enableLocationSettings();
				}
			} else {
				RefreshVenueList(VenueListFragment.this);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void ClearSearchValues() {
		searchQuery = "";
		searchQueryLocation = "";
		isSearching = false;
		isDuplicateSearching = false;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
    public void onRefreshStarted(View view) {
		VenueListFragment.isRefreshing = true;
        RefreshVenueList(VenueListFragment.this);
    }
}