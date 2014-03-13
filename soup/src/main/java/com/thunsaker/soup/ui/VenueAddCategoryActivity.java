package com.thunsaker.soup.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
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
import com.thunsaker.soup.R;
import com.thunsaker.soup.classes.foursquare.Category;
import com.thunsaker.soup.classes.foursquare.FoursquareImage;
import com.thunsaker.soup.util.foursquare.VenueEndpoint;

import java.util.Calendar;
import java.util.List;

/*
 * Created by @thunsaker
 */
public class VenueAddCategoryActivity extends ActionBarActivity {
	private boolean useLogo = true;
	private boolean showHomeUp = true;

	public static List<Category> FoursquareCategoriesMaster = null;
	public static Integer FoursquareCategoriesMasterDate = 0;
	public static CategoryListAdapter myPrimaryCategoryListAdapter = null;
	public static CategoryListAdapter mySecondaryCategoryListAdapter = null;
	public static CategoryListAdapter myTertiaryCategoryListAdapter = null;

	public static ListView mPrimaryListView = null;
	public static ListView mSecondaryListView = null;
	public static ListView mTertiaryListView = null;

	FrameLayout mPrimaryListViewOverlay = null;
	FrameLayout mSecondaryListViewOverlay = null;

//    SearchView mSearchViewCategoryFilter = null;

	public static String SELECTED_CATEGORY = "SELECTED_CATEGORY";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ActionBar ab = getSupportActionBar();
		ab.setDisplayHomeAsUpEnabled(showHomeUp);
		ab.setDisplayUseLogoEnabled(useLogo);

		setContentView(R.layout.activity_venue_add_categories);

		mPrimaryListView = (ListView)findViewById(R.id.listViewPrimaryCategories);
		mSecondaryListView = (ListView)findViewById(R.id.listViewSecondaryCategories);
		mTertiaryListView = (ListView)findViewById(R.id.listViewTertiaryCategories);

		mPrimaryListViewOverlay = (FrameLayout) findViewById(R.id.frameLayoutPrimaryOverlay);
		mSecondaryListViewOverlay = (FrameLayout) findViewById(R.id.frameLayoutSecondaryOverlay);

		mPrimaryListView.setEmptyView((LinearLayout) findViewById(R.id.progressForAddCategory));

		Calendar cal = Calendar.getInstance();
		if(FoursquareCategoriesMaster == null || (FoursquareCategoriesMaster != null && Math.abs(FoursquareCategoriesMasterDate - cal.get(Calendar.SECOND)) >= 3600))
			new VenueEndpoint.GetCategories(getApplicationContext(), this).execute();

		myPrimaryCategoryListAdapter = new CategoryListAdapter(getApplicationContext(), R.layout.list_category_item, FoursquareCategoriesMaster, 1);

		if(FoursquareCategoriesMaster != null && FoursquareCategoriesMaster.size() > 0) {
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

	public static void LoadCategories() {
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
				UrlImageViewHelper.setUrlDrawable(myImageView, myCategory.getIcon().getFoursquareLegacyImageUrl(FoursquareImage.SIZE_MEDIANO), R.drawable.foursquare_generic_category_icon);
				((TextView) myLinearLayout.getChildAt(1)).setText(myCategory.getName().toString());

				final LinearLayout myOptionalLinearLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.list_category_item_optional, null);

				myLinearLayout.setOnClickListener(new OnClickListener() {
					@SuppressLint("NewApi")
					@Override
					public void onClick(View v) {
						if(tier < 3 && myCategory.getSubcategories() != null && myCategory.getSubcategories().size() > 0) {
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
								mySecondaryCategoryListAdapter = new CategoryListAdapter(getContext(), R.layout.list_category_item, myCategory.getSubcategories(), 2);
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
								myTertiaryCategoryListAdapter = new CategoryListAdapter(getContext(), R.layout.list_category_item, myCategory.getSubcategories(), 3);

								ImageView myImageView = (ImageView) myOptionalLinearLayout.getChildAt(0);
								UrlImageViewHelper.setUrlDrawable(myImageView, myCategory.getIcon().getFoursquareLegacyImageUrl(FoursquareImage.SIZE_MEDIANO), R.drawable.foursquare_generic_category_icon);
								((TextView) myOptionalLinearLayout.getChildAt(1)).setText(myCategory.getName().toString());
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
