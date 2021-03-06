package com.thunsaker.soup.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.thunsaker.android.common.annotations.ForApplication;
import com.thunsaker.soup.R;
import com.thunsaker.soup.app.BaseSoupActivity;
import com.thunsaker.soup.data.api.model.Category;
import com.thunsaker.soup.data.api.model.FoursquareImage;
import com.thunsaker.soup.data.events.GetCategoriesEvent;
import com.thunsaker.soup.services.foursquare.FoursquarePrefs;
import com.thunsaker.soup.services.foursquare.FoursquareTasks;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;

/*
 * Created by @thunsaker
 */
public class VenueAddCategoryActivity extends BaseSoupActivity {
    @Inject @ForApplication
    Context mContext;

    @Inject
    FoursquareTasks mFoursquareTaaks;

    @Inject
    EventBus mBus;

    public static List<Category> FoursquareCategoriesMaster = null;
	public static Integer FoursquareCategoriesMasterDate = 0;
	public CategoryListAdapter myPrimaryCategoryListAdapter = null;
	public CategoryListAdapter mySecondaryCategoryListAdapter = null;
	public CategoryListAdapter myTertiaryCategoryListAdapter = null;

	public ListView mPrimaryListView = null;
	public ListView mSecondaryListView = null;
	public ListView mTertiaryListView = null;

	FrameLayout mPrimaryListViewOverlay = null;
	FrameLayout mSecondaryListViewOverlay = null;

//    SearchView mSearchViewCategoryFilter = null;

	public static String SELECTED_CATEGORY = "SELECTED_CATEGORY";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayUseLogoEnabled(true);
        ab.setIcon(getResources().getDrawable(R.drawable.ic_launcher_white));

		setContentView(R.layout.activity_venue_add_categories);

        if(mBus != null && !mBus.isRegistered(this))
            mBus.register(this);

		mPrimaryListView = (ListView)findViewById(R.id.listViewPrimaryCategories);
		mSecondaryListView = (ListView)findViewById(R.id.listViewSecondaryCategories);
		mTertiaryListView = (ListView)findViewById(R.id.listViewTertiaryCategories);

		mPrimaryListViewOverlay = (FrameLayout) findViewById(R.id.frameLayoutPrimaryOverlay);
		mSecondaryListViewOverlay = (FrameLayout) findViewById(R.id.frameLayoutSecondaryOverlay);

		mPrimaryListView.setEmptyView(findViewById(R.id.progressForAddCategory));

		Calendar cal = Calendar.getInstance();
		if(VenueAddCategoryActivity.FoursquareCategoriesMaster == null
                || Math.abs(VenueAddCategoryActivity.FoursquareCategoriesMasterDate - cal.get(Calendar.SECOND)) >= 86400) // 24 hours
            mFoursquareTaaks.new GetCategories(FoursquarePrefs.CALLER_SOURCE_ADD_CATEGORIES).execute();

		myPrimaryCategoryListAdapter = new CategoryListAdapter(getApplicationContext(), R.layout.list_category_item, FoursquareCategoriesMaster, 1);

		if(VenueAddCategoryActivity.FoursquareCategoriesMaster != null
                && VenueAddCategoryActivity.FoursquareCategoriesMaster.size() > 0) {
			mPrimaryListView.setAdapter(myPrimaryCategoryListAdapter);
			myPrimaryCategoryListAdapter.notifyDataSetChanged();
		}

        // TODO: Allow searching/filtering of categories
//        if(findViewById(R.id.searchViewCategoryFilter) != null) {
//            mSearchViewCategoryFilter = (SearchView) findViewById(R.id.searchViewCategoryFilter);
//        }
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			setResult(RESULT_CANCELED);
			finish();
			break;
		}

		return super.onOptionsItemSelected(item);
	}

    public void onEvent(GetCategoriesEvent event) {
        if(event != null) {
            List<Category> result = VenueAddCategoryActivity.FoursquareCategoriesMaster = event.resultCategories;
            Calendar cal = Calendar.getInstance();
            FoursquareCategoriesMasterDate = cal.get(Calendar.SECOND);

            if (myPrimaryCategoryListAdapter.items == null)
                myPrimaryCategoryListAdapter.items = new ArrayList<Category>();

            myPrimaryCategoryListAdapter.items.addAll(result);
            myPrimaryCategoryListAdapter.notifyDataSetChanged();

            myPrimaryCategoryListAdapter = new CategoryListAdapter(mContext, R.layout.list_category_item, result, 1);
            mPrimaryListView.setAdapter(myPrimaryCategoryListAdapter);
            myPrimaryCategoryListAdapter.notifyDataSetChanged();
        }
    }

	public class CategoryListAdapter extends ArrayAdapter<Category> {
		public List<Category> items;
		int tier;

		public CategoryListAdapter(Context context, int textViewResourceId, List<Category> items, int tier) {
			super(context, textViewResourceId, items);
			this.items = items;
			this.tier = tier;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			try {
				if(v == null) {
					v = LayoutInflater.from(getApplicationContext()).inflate(R.layout.list_category_item, null);
				}

				final Category myCategory = items.get(position);

				final LinearLayout myLinearLayout = (LinearLayout) v.findViewById(R.id.linearLayoutCategoryItemWrapper);
				ImageView myImageView = (ImageView) myLinearLayout.getChildAt(0);
				UrlImageViewHelper.setUrlDrawable(myImageView, myCategory.icon.getFoursquareLegacyImageUrl(FoursquareImage.SIZE_MEDIANO, true), R.drawable.foursquare_generic_category_icon);
				((TextView) myLinearLayout.getChildAt(1)).setText(myCategory.name);

				final LinearLayout myOptionalLinearLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.list_category_item_optional, null);

				myLinearLayout.setOnClickListener(new OnClickListener() {
					@SuppressLint("NewApi")
					@Override
					public void onClick(View v) {
						if(tier < 3 && myCategory.categories != null && myCategory.categories.size() > 0) {
							if(tier == 1) {
								mSecondaryListView.setVisibility(View.VISIBLE);
								if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
									mSecondaryListView.animate().alpha(100);
								}
//								mSecondaryListView.animate().alpha(100).translationX(-120);
//								LayoutParams myLayoutParams = new LayoutParams(mSecondaryListView.getLayoutParams());
//								myLayoutParams.width = mPrimaryListView.getWidth() - 120;
//								mSecondaryListView.setLayoutParams(myLayoutParams);
								mPrimaryListViewOverlay.setVisibility(View.VISIBLE);
								mPrimaryListViewOverlay.setOnClickListener(new OnClickListener() {
									@Override
									public void onClick(View v) {
										mySecondaryCategoryListAdapter = null;
										mSecondaryListView.setAdapter(null);
										mSecondaryListView.setVisibility(View.GONE);

										myTertiaryCategoryListAdapter = null;
										mTertiaryListView.setAdapter(null);
										mTertiaryListView.setVisibility(View.GONE);

										mPrimaryListViewOverlay.setVisibility(View.GONE);
										mPrimaryListViewOverlay.setOnClickListener(null);

										mSecondaryListViewOverlay.setVisibility(View.GONE);
										mSecondaryListViewOverlay.setOnClickListener(null);
									}
								});
								mySecondaryCategoryListAdapter = new CategoryListAdapter(getContext(), R.layout.list_category_item, myCategory.categories, 2);
								mSecondaryListView.setAdapter(mySecondaryCategoryListAdapter);
								mySecondaryCategoryListAdapter.notifyDataSetChanged();
							} else {
								mTertiaryListView.setVisibility(View.VISIBLE);
								if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
									mTertiaryListView.animate().alpha(100);
								}
								mSecondaryListViewOverlay.setVisibility(View.VISIBLE);
								mSecondaryListViewOverlay.setOnClickListener(new OnClickListener() {
									@Override
									public void onClick(View v) {
										myTertiaryCategoryListAdapter = null;

										mTertiaryListView.setAdapter(null);
										mTertiaryListView.setVisibility(View.GONE);

										mSecondaryListViewOverlay.setVisibility(View.GONE);
										mSecondaryListViewOverlay.setOnClickListener(null);
									}
								});
								myTertiaryCategoryListAdapter = new CategoryListAdapter(getContext(), R.layout.list_category_item, myCategory.categories, 3);

								ImageView myImageView = (ImageView) myOptionalLinearLayout.getChildAt(0);
								UrlImageViewHelper.setUrlDrawable(myImageView, myCategory.icon.getFoursquareLegacyImageUrl(FoursquareImage.SIZE_MEDIANO, true), R.drawable.foursquare_generic_category_icon);
								((TextView) myOptionalLinearLayout.getChildAt(1)).setText(myCategory.name.toString());
								myOptionalLinearLayout.setOnClickListener(new OnClickListener() {
									@Override
									public void onClick(View v) {
										Intent myResultData = new Intent();
										myResultData.putExtra(SELECTED_CATEGORY, myCategory.toString());
										setResult(VenueAddCategoryActivity.RESULT_OK, myResultData);
										finish();
									}
								});

								if(mTertiaryListView.getHeaderViewsCount() >= 1) {
									mTertiaryListView.removeHeaderView(mTertiaryListView.findViewById(R.id.linearLayoutCategoryHeaderItemWrapper));
								}

								mTertiaryListView.addHeaderView(myOptionalLinearLayout, null, true);

								mTertiaryListView.setAdapter(myTertiaryCategoryListAdapter);
								myTertiaryCategoryListAdapter.notifyDataSetChanged();
							}
						} else {
							Intent myResultData = new Intent();
							myResultData.putExtra(SELECTED_CATEGORY, myCategory.toString());
							setResult(VenueAddCategoryActivity.RESULT_OK, myResultData);
							finish();
						}
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
			}

			return v;
		}
	}
}
