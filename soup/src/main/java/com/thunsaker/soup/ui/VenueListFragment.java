package com.thunsaker.soup.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.thunsaker.android.common.annotations.ForApplication;
import com.thunsaker.soup.PreferencesHelper;
import com.thunsaker.soup.R;
import com.thunsaker.soup.adapters.VenueListAdapter;
import com.thunsaker.soup.app.BaseSoupFragment;
import com.thunsaker.soup.data.api.model.CompactVenue;
import com.thunsaker.soup.data.events.VenueListEvent;
import com.thunsaker.soup.data.events.VenueSearchEvent;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.event.EventBus;

/*
 * Created by @thunsaker
 */
public class VenueListFragment extends BaseSoupFragment
        implements SwipeRefreshLayout.OnRefreshListener,
        AbsListView.OnItemClickListener,
        AbsListView.OnItemLongClickListener {
    @Inject
    @ForApplication
    Context mContext;

    @Inject
    EventBus mBus;

    @Inject
    LocationManager mLocationManager;

    private VenueListAdapter currentVenueListAdapter;
    private List<CompactVenue> currentVenueList;

    private VenueListAdapter searchResultsVenueListAdapter;
    private List<CompactVenue> searchResultsVenueList;

    private VenueListAdapter searchDuplicateResultsVenueListAdapter;
    private List<CompactVenue> searchDuplicateResultsVenueList;

    public boolean isRefreshing = false;

    public String searchQuery = "";
    public String searchQueryLocation = "";
    public String duplicateVenueId = "";
    public boolean isSearching;
    public boolean isDuplicateSearching;

    private static final String STATE_ACTIVATED_POSITION = "activated_position";

    private static String ARG_SEARCH_QUERY = "query";
    private static String ARG_SEARCH_QUERY_LOCATION = "queryLocation";
    private static String ARG_SEARCH_DUPLICATE_VENUE_ID = "queryDuplicateVenueId";

    private static String ARG_LIST_TYPE = "mListType";

    public static int VENUE_LIST_TYPE_DEFAULT = 0;
    public static int VENUE_LIST_TYPE_SEARCH = 1;
    public static int VENUE_LIST_TYPE_DUPLICATE = 2;

    public static int mActivatedPosition = ListView.INVALID_POSITION;

    private ListView mListView;

    @InjectView(R.id.swipeLayoutVenueListContainer) SwipeRefreshLayout mSwipeViewVenueListContainer;

    private int mListType;

    private OnFragmentInteractionListener mClickListener;

    public interface OnFragmentInteractionListener {
        public void onVenueListClick(String compactVenueJson);
        public boolean onVenueListLongClick(String venueId, String venueName);
    }

    /**
     *
     * @param venueListType     Either {@link com.thunsaker.soup.ui.VenueListFragment.VENUE_LIST_TYPE_DEFAULT} or
     *                          {@link com.thunsaker.soup.ui.VenueListFragment.VENUE_LIST_TYPE_SEARCH} or
     *                          {@link com.thunsaker.soup.ui.VenueListFragment.VENUE_LIST_TYPE_DUPLICATE}
     * @return
     */
    public static VenueListFragment newInstance(int venueListType) {
        return VenueListFragment.newInstance(null, null, null, venueListType);
    }

    public static VenueListFragment newInstance(String query, String queryLocation, String queryDuplicateVenueId, int venueListType) {
        VenueListFragment fragment = new VenueListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_LIST_TYPE, venueListType);
        if(query != null)
            args.putString(ARG_SEARCH_QUERY, query);
        if(queryLocation != null)
            args.putString(ARG_SEARCH_QUERY_LOCATION, queryLocation);
        if(queryDuplicateVenueId != null)
            args.putString(ARG_SEARCH_DUPLICATE_VENUE_ID, queryDuplicateVenueId);

        fragment.setArguments(args);
        return fragment;
    }

    public VenueListFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ClearSearchValues();

        if(mBus != null && !mBus.isRegistered(this))
            mBus.register(this);

        if(getArguments() != null) {
            mListType = getArguments().getInt(ARG_LIST_TYPE);

            searchQuery =
                    getArguments().getString(ARG_SEARCH_QUERY) != null
                            ? getArguments().getString(ARG_SEARCH_QUERY)
                            : "";
            searchQueryLocation =
                    getArguments().getString(ARG_SEARCH_QUERY_LOCATION) != null
                            ? getArguments().getString(ARG_SEARCH_QUERY_LOCATION)
                            : "";
            duplicateVenueId =
                    getArguments().getString(ARG_SEARCH_DUPLICATE_VENUE_ID) != null
                            ? getArguments().getString(ARG_SEARCH_DUPLICATE_VENUE_ID)
                            : "";
            isDuplicateSearching = duplicateVenueId != null && duplicateVenueId.length() > 0;
        }

        if (PreferencesHelper.getFoursquareConnected(mContext)) {
            if (mListType == VENUE_LIST_TYPE_DEFAULT)
                BeginUpdates();
            else
                RefreshVenuesList(searchQuery, searchQueryLocation, duplicateVenueId);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState != null && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION))
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mClickListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mClickListener = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mActivatedPosition != ListView.INVALID_POSITION) {
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
    }

    public void setActivateOnItemClick(boolean activateOnItemClick) {
        mListView.setChoiceMode(
                activateOnItemClick ? ListView.CHOICE_MODE_SINGLE
                        : ListView.CHOICE_MODE_NONE
        );
    }

    private void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
            mListView.setItemChecked(mActivatedPosition, false);
        } else {
            mListView.setItemChecked(position, true);
        }

        mActivatedPosition = position;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_venue_list, container, false);

        mListView = (ListView) view.findViewById(android.R.id.list);
        mListView.setSelector(R.drawable.layout_selector_green);
        mListView.setEmptyView(view.findViewById(android.R.id.empty));

        View myFooter = inflater.inflate(R.layout.list_venue_footer, null);
        mListView.addFooterView(myFooter);
        mListView.setOnItemClickListener(this);
        mListView.setOnItemLongClickListener(this);

        ButterKnife.inject(this, view);

        if(mSwipeViewVenueListContainer != null) {
            mSwipeViewVenueListContainer.setOnRefreshListener(this);
            mSwipeViewVenueListContainer.setColorScheme(
                    R.color.foursquare_green,
                    R.color.foursquare_orange,
                    R.color.foursquare_green,
                    R.color.foursquare_blue);
            mSwipeViewVenueListContainer.setRefreshing(true);
        }

        switch (mListType) {
            case 1:
                searchResultsVenueList = new ArrayList<CompactVenue>();
                searchResultsVenueListAdapter =
                        new VenueListAdapter(mContext, searchResultsVenueList);
                searchResultsVenueListAdapter.notifyDataSetChanged();
                mListView.setAdapter(searchResultsVenueListAdapter);
                break;
            case 2:
                searchDuplicateResultsVenueList = new ArrayList<CompactVenue>();
                searchDuplicateResultsVenueListAdapter =
                        new VenueListAdapter(mContext, searchDuplicateResultsVenueList);
                searchDuplicateResultsVenueListAdapter.notifyDataSetChanged();
                mListView.setAdapter(searchDuplicateResultsVenueListAdapter);
                break;
            default:
                currentVenueList = new ArrayList<CompactVenue>();
                currentVenueListAdapter =
                        new VenueListAdapter(mContext, currentVenueList);
                currentVenueListAdapter.notifyDataSetChanged();
                mListView.setAdapter(currentVenueListAdapter);
                break;
        }

        return view;
    }

    private void enableLocationSettings() {
        if(mSwipeViewVenueListContainer != null)
            mSwipeViewVenueListContainer.setRefreshing(false);

        if (mLocationManager != null)
            mLocationManager.removeUpdates(mLocationListener);

        Toast.makeText(mContext, R.string.alert_gps_disabled, Toast.LENGTH_SHORT).show();

        currentVenueList = null;
        currentVenueListAdapter.notifyDataSetChanged();
    }

    public final LocationListener mLocationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            LatLng newLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            MainActivity.currentLocation = newLatLng;
            if (!isSearching) {
                RefreshVenuesList("", "", "");
            } else if (isSearching && (searchQueryLocation != null || !searchQueryLocation.equals(""))) {
                mLocationManager = null;
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

    public void RefreshVenuesList(String query, String location, String duplicateId) {
        if(mSwipeViewVenueListContainer != null)
            mSwipeViewVenueListContainer.setRefreshing(true);
        mBus.post(new VenueSearchEvent(query, location, duplicateId, mListType));
    }

    @Override
    public void onStop() {
        super.onStop();

        if (mLocationManager != null)
            mLocationManager.removeUpdates(mLocationListener);
    }

    private void BeginUpdates() {
        try {
            if (searchQueryLocation != null && searchQueryLocation.equals("") && !isSearching) {
                // Get the current gps position
                LocationProvider provider = null;
                Boolean hasLocationProvider;

                mLocationManager =
                        (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
                // Use network provider first
                LocationProvider networkProvider =
                        mLocationManager.getProvider(
                                LocationManager.NETWORK_PROVIDER);

                Location lastKnown =
                        mLocationManager.getLastKnownLocation(
                                LocationManager.PASSIVE_PROVIDER);
                double lat = lastKnown.getLatitude();
                double lon = lastKnown.getLongitude();
                MainActivity.currentLocation = new LatLng(lat, lon);

                final boolean networkEnabled =
                        mLocationManager.isProviderEnabled(networkProvider.getName());
                LocationProvider gpsProvider =
                        mLocationManager.getProvider(
                                LocationManager.NETWORK_PROVIDER);
                final boolean gpsEnabled =
                        mLocationManager.isProviderEnabled(gpsProvider.getName());
                if (gpsEnabled) {
                    provider = gpsProvider;
                    hasLocationProvider = true;
                } else if (networkEnabled) {
                    provider = networkProvider;
                    hasLocationProvider = true;
                } else {
                    hasLocationProvider = false;
                }

                if (hasLocationProvider) {
                    // Update every 5 minutes or ~1 mile
                    mLocationManager.requestLocationUpdates(
                            provider.getName(), 300000, 1709, mLocationListener);
                    if (MainActivity.currentLocation != null)
                        RefreshVenuesList(searchQuery, searchQueryLocation, duplicateVenueId);
                    else {
                        Toast.makeText(getActivity(), R.string.alert_current_location_unknown,
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    enableLocationSettings();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void ClearSearchValues() {
        searchQuery = "";
        searchQueryLocation = "";
        duplicateVenueId = "";
        isSearching = false;
        isDuplicateSearching = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void onEvent(VenueListEvent event) {
        if(mSwipeViewVenueListContainer != null)
            mSwipeViewVenueListContainer.setRefreshing(false);

        if (event.result && event.resultList != null && event.resultList.size() > 0) {
            switch (event.resultListType) {
                case 1: // {@link com.thunsaker.soup.ui.VenueListFragment.VENUE_LIST_TYPE_SEARCH}
                    searchResultsVenueList = new ArrayList<CompactVenue>(event.resultList);
                    searchResultsVenueListAdapter = new VenueListAdapter(mContext, searchResultsVenueList);
                    searchResultsVenueListAdapter.notifyDataSetChanged();
                    mListView.setAdapter(searchResultsVenueListAdapter);
                    break;
                case 2: // {@link com.thunsaker.soup.ui.VenueListFragment.VENUE_LIST_TYPE_DUPLICATE}
                    searchDuplicateResultsVenueList = new ArrayList<CompactVenue>();

                    for (CompactVenue c : event.resultList) {
                        String tempId = c.id.trim();
                        if (!tempId.equals(event.resultDuplicateVenueId.trim()))
                            searchDuplicateResultsVenueList.add(c);
                    }

                    searchDuplicateResultsVenueListAdapter = new VenueListAdapter(mContext, searchDuplicateResultsVenueList);
                    searchDuplicateResultsVenueListAdapter.notifyDataSetChanged();
                    mListView.setAdapter(searchDuplicateResultsVenueListAdapter);
                    break;
                case 0: // {@link com.thunsaker.soup.ui.VenueListFragment.VENUE_LIST_TYPE_DEFAULT}
                    currentVenueList = new ArrayList<CompactVenue>(event.resultList);
                    currentVenueListAdapter = new VenueListAdapter(mContext, currentVenueList);
                    currentVenueListAdapter.notifyDataSetChanged();
                    mListView.setAdapter(currentVenueListAdapter);
                    break;
            }
        } else {
            Toast.makeText(mContext, mContext.getString(R.string.alert_error_loading_venues) + " - Location 4", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRefresh() {
        RefreshVenuesList(searchQuery, searchQueryLocation, duplicateVenueId);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        try {
            if (mListView.getFooterViewsCount() == 1 && position == mListView.getCount() - 1) {
                Intent searchActivity =
                        new Intent(getActivity().getApplicationContext(), VenueSearchActivity.class);
                startActivity(searchActivity);
                return;
            }

            if (isDuplicateSearching)
                setActivateOnItemClick(true);

            if (!isRefreshing) {
                CompactVenue clickedVenue =
                        (CompactVenue) mListView.getItemAtPosition(position);
                mClickListener.onVenueListClick(clickedVenue.toString());
            } else {
                Toast.makeText(getActivity(), R.string.alert_still_loading, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(getActivity(), getString(R.string.alert_error_loading_venues) + " - Location 3", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        CompactVenue clickedVenue =
                (CompactVenue) mListView.getItemAtPosition(position);
        return mClickListener.onVenueListLongClick(clickedVenue.id, clickedVenue.name);
    }
}