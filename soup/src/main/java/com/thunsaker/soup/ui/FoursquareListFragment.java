package com.thunsaker.soup.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.thunsaker.soup.R;
import com.thunsaker.soup.app.BaseSoupFragment;
import com.thunsaker.soup.data.api.model.CompactVenue;
import com.thunsaker.soup.data.api.model.FoursquareList;
import com.thunsaker.soup.data.api.model.FoursquareListItem;
import com.thunsaker.soup.services.foursquare.endpoints.ListEndpoint;
import com.thunsaker.soup.util.Util;

import java.util.List;

import butterknife.ButterKnife;

/*
 * Created by @thunsaker
 */
public class FoursquareListFragment extends BaseSoupFragment implements
        SwipeRefreshLayout.OnRefreshListener {

	public static final String ARG_ITEM_JSON_STRING = "item_json_string";

	public static FoursquareListItemsAdapter currentListItemsAdapter;
	public static FoursquareList currentList;
	public static List<FoursquareListItem> currentListItems;

	private Callbacks mCallbacks = sDummyCallbacks;
	public static int mActivatedPosition = ListView.INVALID_POSITION;

	private static final String STATE_ACTIVATED_POSITION = "activated_position";

	public static ImageView mImageViewHeaderPhoto;
	public static ImageView mImageViewTemp;

	public static ImageView mImageViewHeaderProfile;
	public static TextView mTextViewHeaderCreator;

    public static ListView mListView;

//    @InjectView(R.id.swipeLayoutFoursquareListItems) SwipeRefreshLayout mSwipeViewFoursquareListItems;

    @Override
    public void onRefresh() {
        RefreshList(this);
    }

    public interface Callbacks {
		public void onItemSelected(String listJson);
	}

	private static Callbacks sDummyCallbacks = new Callbacks() {
		@Override
		public void onItemSelected(String id) {
		}
	};

	public static boolean isRefreshing = false;

	public FoursquareListFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setHasOptionsMenu(true);

		currentListItemsAdapter = new FoursquareListItemsAdapter(getActivity()
				.getApplicationContext(), R.layout.list_lists_item,
				currentListItems);

		if (currentListItems != null && currentListItems.size() > 0) {
            if(mListView != null) {
                mListView.setAdapter(currentListItemsAdapter);
                currentListItemsAdapter.notifyDataSetChanged();
            }
		}
	}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_foursquare_list, container, false);

        mListView = (ListView) rootView.findViewById(R.id.listViewFoursquareListItems);
        mListView.setSelector(R.drawable.layout_selector_blue);

        return rootView;
    }

    @Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		LayoutInflater inflater = getLayoutInflater(savedInstanceState);
		View headerViewPhoto = inflater.inflate(
				R.layout.list_list_item_header_photo, null);
		mImageViewHeaderPhoto = (ImageView) headerViewPhoto
				.findViewById(R.id.imageViewListHeader);
		mImageViewHeaderPhoto
				.setLayoutParams((ViewGroup.LayoutParams) new AbsListView.LayoutParams(
						AbsListView.LayoutParams.MATCH_PARENT,
						(int) getResources().getDimension(
								R.dimen.list_header_size)));
        if(mListView != null) {
            mListView.addHeaderView(mImageViewHeaderPhoto, null, false);

            View headerViewCreator = inflater.inflate(
                    R.layout.list_list_item_header_creator, null);

            if (headerViewCreator != null) {
                mImageViewHeaderProfile = (ImageView) headerViewCreator
                        .findViewById(R.id.imageViewListItemHeaderProfile);
                mTextViewHeaderCreator = (TextView) headerViewCreator
                        .findViewById(R.id.textViewListItemHeaderName);

                mListView.addHeaderView(headerViewCreator);
            }

            View emptyView = inflater.inflate(R.layout.fragment_list_empty, null);
            mListView.setEmptyView(emptyView);
            mListView.setSelector(R.drawable.layout_selector_green);
            mListView.setDivider(null);
            mListView.setFastScrollEnabled(true);
        }

		if (savedInstanceState != null
				&& savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
			setActivatedPosition(savedInstanceState
					.getInt(STATE_ACTIVATED_POSITION));
		}

		RefreshList(this);

        ButterKnife.inject(this, view);

//        if(mSwipeViewFoursquareListItems != null) {
//            mSwipeViewFoursquareListItems.setOnRefreshListener(this);
//            mSwipeViewFoursquareListItems.setColorScheme(R.color.foursquare_green, R.color.foursquare_orange, R.color.foursquare_green, R.color.foursquare_blue);
//        }

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    int offsetPosition = position - mListView.getHeaderViewsCount();
                    if (!FoursquareListFragment.isRefreshing) {
                        if(mListView.getHeaderViewsCount() == 2 && position == 1) {
                            String userUrl = "";
                            if(currentList != null && currentList.getUser() != null && currentList.getUser().id.length() > 0)
                                userUrl = String.format("https://foursquare.com/user/%s", currentList.getUser().id);

                            if(userUrl != null && userUrl.length() > 0)
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(userUrl)));
                            else
//						Crouton.makeText(getActivity(),
//	                            R.string.alert_error_loading_details,
//	                            Style.INFO).show();
                                Toast.makeText(getActivity(),
                                        R.string.alert_error_loading_details,
                                        Toast.LENGTH_SHORT).show();
                        } else {
                            CompactVenue clickedVenue = currentListItems.get(offsetPosition).getVenue();
                            mCallbacks.onItemSelected(clickedVenue.toString());
                        }
                    } else {
                        // Crouton.makeText(getActivity(), R.string.alert_still_loading,
                        // Style.INFO).show();
                        Toast.makeText(getActivity(), R.string.alert_still_loading,
                                Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception ex) {
                    // Crouton.makeText(getActivity(),
                    // R.string.alert_error_loading_venues, Style.INFO).show();
                    Toast.makeText(getActivity(), getString(R.string.alert_error_loading_venues) + " - Error 6", Toast.LENGTH_SHORT).show();
                    ex.printStackTrace();
                }
            }
        });
	}

	public static void RefreshList(FoursquareListFragment theCaller) {
//		theCaller.getActivity().setProgressBarVisibility(true);
		currentList = null;
		if (currentListItemsAdapter == null)
			currentListItemsAdapter = theCaller.new FoursquareListItemsAdapter(
					theCaller.getActivity().getApplicationContext(),
					R.layout.list_lists_item, null);

		currentListItemsAdapter.notifyDataSetChanged();

		ConnectivityManager connectivityManager = (ConnectivityManager) theCaller
				.getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager
				.getActiveNetworkInfo();
		if (activeNetworkInfo != null) {
			FoursquareListFragment.isRefreshing = true;
//			theCaller.getActivity().setProgressBarVisibility(true);
			new ListEndpoint.GetList(theCaller.getActivity()
					.getApplicationContext(), theCaller,
					ListActivity.listIdToLoad).execute();
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (!(activity instanceof Callbacks)) {
			throw new IllegalStateException(
					"Activity must implement fragment's callbacks.");
		}
		mCallbacks = (Callbacks) activity;
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mCallbacks = sDummyCallbacks;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		getActivity().getMenuInflater().inflate(R.menu.activity_list, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_foursquare:
			String canonicalUrl = "";
			if(currentList != null && currentList.getCanonicalUrl() != null && currentList.getCanonicalUrl().length() > 0)
				canonicalUrl = currentList.getCanonicalUrl();
			else if(currentList != null && !currentList.getCanonicalUrl().equals("") && currentList.getCanonicalUrl().length() > 0)
				canonicalUrl = currentList.getCanonicalUrl();

			if(canonicalUrl != null && canonicalUrl.length() > 0)
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(canonicalUrl)));
			else
//				Crouton.makeText(getActivity(),
//                        R.string.alert_error_loading_details,
//                        Style.INFO).show();
                Toast.makeText(getActivity(),
                        R.string.alert_error_loading_details,
                        Toast.LENGTH_SHORT).show();
			break;
		default:
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	public void setActivateOnItemClick(boolean activateOnItemClick) {
		// When setting CHOICE_MODE_SINGLE, ListView will automatically
		// give items the 'activated' state when touched.
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

	public class FoursquareListItemsAdapter extends
            ArrayAdapter<FoursquareListItem> {
		public List<FoursquareListItem> items;

		public FoursquareListItemsAdapter(Context context,
				int textViewResourceId, List<FoursquareListItem> items) {
			super(context, textViewResourceId, items);
			this.items = items;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater viewInflater = (LayoutInflater) getActivity()
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				// TODO: Replace this with another layout specific for lists?
				// Needs to be selectable, batch delete? move? Not sure yet.
				v = viewInflater.inflate(R.layout.list_list_item, null);
			}
			try {
				final FoursquareListItem listItem = items.get(position);
				final CompactVenue venue = listItem.getVenue();
				if (venue != null) {
					final String myVenueName = venue.name != null ? venue.name : "";
					final String myVenueAddress = venue.location.address != null ? venue.location.address : "";

					final TextView nameTextView = (TextView) v
							.findViewById(R.id.textViewListItemName);
					if (nameTextView != null)
						nameTextView.setText(myVenueName);

					final TextView addressTextView = (TextView) v
							.findViewById(R.id.textViewListItemAddress);
					if (addressTextView != null)
						addressTextView.setText(myVenueAddress);

					ImageView doneImageView = (ImageView) v.findViewById(R.id.imageViewListItemDone);
					if (venue.beenHere.marked)
						doneImageView.setVisibility(View.VISIBLE);
					else
						doneImageView.setVisibility(View.GONE);

					/*
					 * final ImageView primaryCategoryImageView =
					 * (ImageView)v.findViewById(R.id.imageViewVenueCategory);
					 * List<Category> myCategories = venuecategories;
					 * if(myCategories != null) { Category primaryCategory =
					 * myCategories.get(0) != null ? myCategories.get(0) : null;
					 * if(primaryCategoryImageView != null && primaryCategory !=
					 * null) { String imageUrl =
					 * primaryCategory.getIcon().toString();
					 * UrlImageViewHelper.setUrlDrawable(
					 * primaryCategoryImageView, imageUrl,
					 * R.drawable.foursquare_generic_category_icon); } } else {
					 * primaryCategoryImageView.setImageResource(
					 * R.drawable.foursquare_generic_category_icon); }
					 */

					ImageView alertImageView = (ImageView) v
							.findViewById(R.id.imageViewListItemAlert);
					if (Util.VenueHasProblems(venue))
						alertImageView.setVisibility(View.VISIBLE);
					else
						alertImageView.setVisibility(View.GONE);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return v;
		}
	}
}