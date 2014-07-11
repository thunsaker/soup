package com.thunsaker.soup.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.thunsaker.android.common.annotations.ForApplication;
import com.thunsaker.soup.R;
import com.thunsaker.soup.app.BaseSoupFragment;
import com.thunsaker.soup.data.api.model.FoursquareList;
import com.thunsaker.soup.services.foursquare.FoursquarePrefs;
import com.thunsaker.soup.services.foursquare.endpoints.UserEndpoint;

import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;

/*
 * Created by @thunsaker
 */
public class ListsFragment extends BaseSoupFragment implements
        SwipeRefreshLayout.OnRefreshListener {

    @Inject
    @ForApplication
    Context mContext;

    @Inject
    Picasso mPicasso;

	public static FoursquareListAdapter currentListsListAdapter;

	public static List<FoursquareList> currentListsList;

	private Callbacks mCallbacks = sDummyCallbacks;
	public static int mActivatedPosition = ListView.INVALID_POSITION;

    @InjectView(R.id.swipeLayoutListsContainer) SwipeRefreshLayout mSwipeViewListsContainer;

	private static final String STATE_ACTIVATED_POSITION = "activated_position";

	public static GridView mGridViewLists;

    @Override
    public void onRefresh() {
        RefreshLists(this);
    }

    public interface Callbacks {
		public void onItemSelected(String listJson);
	}

	private static Callbacks sDummyCallbacks = new Callbacks() {
		@Override
		public void onItemSelected(String id) {
		}
	};

	public ListsFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_lists, container, false);

		mGridViewLists = (GridView)rootView.findViewById(R.id.gridViewLists);
		mGridViewLists.setSelector(R.drawable.layout_selector_green);

		currentListsListAdapter = new FoursquareListAdapter(getActivity().getApplicationContext(),
				R.layout.list_lists_item, currentListsList);

		if(currentListsList != null && currentListsList.size() > 0) {
			mGridViewLists.setAdapter(currentListsListAdapter);
			currentListsListAdapter.notifyDataSetChanged();
		}

		RefreshLists(this);

		return rootView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

        ButterKnife.inject(this, view);
        if(mSwipeViewListsContainer != null) {
            mSwipeViewListsContainer.setOnRefreshListener(this);
            mSwipeViewListsContainer.setColorScheme(R.color.foursquare_green, R.color.foursquare_orange, R.color.foursquare_green, R.color.foursquare_blue);
        }
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		if (mActivatedPosition != ListView.INVALID_POSITION) {
			outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
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

	public void RefreshLists(ListsFragment theCaller) {
        if(mSwipeViewListsContainer != null)
            mSwipeViewListsContainer.setRefreshing(true);
		currentListsList = null;

		if (currentListsListAdapter == null)
			currentListsListAdapter = theCaller.new FoursquareListAdapter(
					theCaller.getActivity().getApplicationContext(),
					R.layout.list_lists_item, null);

		currentListsListAdapter.notifyDataSetChanged();

		ConnectivityManager connectivityManager = (ConnectivityManager) theCaller
				.getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager
				.getActiveNetworkInfo();
		if (activeNetworkInfo != null) {
			theCaller.getActivity().setProgressBarVisibility(true);

			new UserEndpoint.GetLists(theCaller.getActivity()
					.getApplicationContext(), theCaller,
					theCaller.mGridViewLists,
					FoursquarePrefs.FOURSQUARE_LISTS_GROUP_CREATED).execute();
		}
	}

	public class FoursquareListAdapter extends ArrayAdapter<FoursquareList> {
		public List<FoursquareList> items;

		public FoursquareListAdapter(Context context,
				int textViewResourceId, List<FoursquareList> items) {
			super(context, textViewResourceId, items);
			this.items = items;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater viewInflater = (LayoutInflater) getActivity()
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = viewInflater.inflate(R.layout.list_lists_item, null);
			}
			try {
				final FoursquareList list = items.get(position);
				if (list != null) {
					final String myListId = list.getId() != null
							? list.getId() : "";
					final String myListName = list.getName() != null
							? list.getName() : "";
					final TextView nameTextView = (TextView) v.findViewById(R.id.textViewListName);
					if (nameTextView != null)
						nameTextView.setText(myListName);

					final Integer myVenueCount = list.getVenueCount();
					final TextView countTextView = (TextView) v.findViewById(R.id.textViewListCount);
					countTextView.setText(String.format(getResources().getQuantityString(R.plurals.venue_count, myVenueCount), myVenueCount));
                    final LinearLayout textWrapper = (LinearLayout) v.findViewById(R.id.linearLayoutTextWrapper);

					final ImageView myImageViewPhoto = (ImageView) v.findViewById(R.id.imageViewListPhoto);
					if(position == 0) {
						myImageViewPhoto
						.setImageDrawable(getResources()
								.getDrawable(
										R.drawable.list_placeholder_todo));
					} else {
						final String myPhotoUrl = list.getPhoto() != null
								? list.getPhoto().getFoursquareImageUrl() : "";

						if (myPhotoUrl != "") {
//                            UrlImageViewHelper.setUrlDrawable(myImageViewPhoto,
//                                    myPhotoUrl,
//                                    R.drawable.list_placeholder_gray_dark_small);

                            mPicasso.load(myPhotoUrl)
                                    .placeholder(mContext.getResources().getDrawable(R.drawable.list_placeholder_orange_small))
                                    .error(mContext.getResources().getDrawable(R.drawable.list_placeholder_gray_dark_small))
                                    .into(myImageViewPhoto);

// TODO: Replace this with palette when the time comes.
//                            mPicasso.load(myPhotoUrl).into(new Target() {
//                                @Override
//                                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
//                                    Drawable faviconDrawable = new BitmapDrawable(mContext.getResources(), bitmap);
//                                    myImageViewPhoto.setImageDrawable(faviconDrawable);
//
//                                    textWrapper.setBackgroundColor(Crayon.getAverageColor(bitmap, 170));
//                                }
//
//                                @Override
//                                public void onBitmapFailed(Drawable errorDrawable) {
//                                    Drawable defaultFaviconDrawable = mContext.getResources().getDrawable(R.drawable.list_placeholder_gray_dark_small);
//                                    myImageViewPhoto.setImageDrawable(defaultFaviconDrawable);
//                                }
//
//                                @Override
//                                public void onPrepareLoad(Drawable placeHolderDrawable) {
//                                    Drawable placeholderFaviconDrawable = mContext.getResources().getDrawable(R.drawable.list_placeholder_green_small);
//                                    myImageViewPhoto.setImageDrawable(placeholderFaviconDrawable);
//                                }
//                            });
                        } else {
							myImageViewPhoto
									.setImageDrawable(getResources()
                                            .getDrawable(
                                                    R.drawable.list_placeholder_gray_dark_small));
						}
					}

					final ImageView myImageViewIsPublic = (ImageView)v.findViewById(R.id.imageViewListIsPublic);
					if(!list.getIsPublic()) {
						myImageViewIsPublic.setVisibility(View.VISIBLE);
					} else {
						myImageViewIsPublic.setVisibility(View.GONE);
					}

					final ImageView myImageViewListIsFollowed = (ImageView)v.findViewById(R.id.imageViewListIsFollowed);
					if(list.getType().equals(FoursquarePrefs.FOURSQUARE_LISTS_GROUP_FOLLOWED)) {
						myImageViewListIsFollowed.setVisibility(View.VISIBLE);
					} else {
						myImageViewListIsFollowed.setVisibility(View.GONE);
					}

					final Button myViewOverlay = (Button)v.findViewById(R.id.buttonListActivityOverlay);
					myViewOverlay.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
								Intent myListIntent = new Intent(getActivity()
										.getApplicationContext(),
										ListActivity.class);
								myListIntent.putExtra(
										ListActivity.LIST_TO_LOAD_EXTRA,
										myListId);
								getActivity().startActivity(myListIntent);
//							} else {
//								Toast.makeText(getActivity(),
//										R.string.alert_still_loading,
//										Toast.LENGTH_SHORT).show();
//							}
						}
					});
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return v;
		}
	}
}