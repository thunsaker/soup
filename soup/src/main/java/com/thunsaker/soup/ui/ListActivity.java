package com.thunsaker.soup.ui;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

import com.thunsaker.soup.R;
import com.thunsaker.soup.app.BaseSoupActivity;
import com.thunsaker.soup.data.api.model.FoursquareList;

/*
 * Created by @thunsaker
 */
public class ListActivity extends BaseSoupActivity
	implements FoursquareListFragment.Callbacks {

	public static final String LIST_TO_LOAD_EXTRA = "LIST_TO_LOAD_EXTRA";
	public static String listIdToLoad = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
			supportRequestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
		}

		handleIntent(getIntent());

		ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
		ab.setDisplayUseLogoEnabled(false);
		ab.setDisplayShowHomeEnabled(false);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            ab.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.black_super_transparent)));
            ab.setSplitBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.black_super_transparent)));
            ab.setStackedBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.black_super_transparent)));
        }

		setSupportProgressBarIndeterminate(true);

		if(savedInstanceState == null) {
			setContentView(R.layout.activity_list);

			if(getIntent().hasExtra(FoursquareListFragment.ARG_ITEM_JSON_STRING)) {
				Bundle arguments = new Bundle();
				arguments.putString(FoursquareListFragment.ARG_ITEM_JSON_STRING, getIntent().getStringExtra(FoursquareListFragment.ARG_ITEM_JSON_STRING));

				FoursquareListFragment fragment = new FoursquareListFragment();
				fragment.setArguments(arguments);
				getSupportFragmentManager().beginTransaction().add(R.id.fragmentList, fragment).commit();
			}
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		handleIntent(intent);
	}

	public void handleIntent(Intent intent) {
		if(intent.hasExtra(LIST_TO_LOAD_EXTRA)) {
			listIdToLoad = intent.getStringExtra(LIST_TO_LOAD_EXTRA);

			if(ListsFragment.currentListsList != null) {
				for (FoursquareList list : ListsFragment.currentListsList) {
					if(list.getId() == listIdToLoad) {
						FoursquareListFragment.currentList = list;
						break;
					}
				}
			}
		} else {
			// TODO: Handle this better
			Toast.makeText(getApplicationContext(), R.string.alert_no_list_items, Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpTo(this, new Intent(this, MainActivity.class));
			listIdToLoad = "";
			FoursquareListFragment.currentList = null;
			FoursquareListFragment.currentListItems = null;
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onItemSelected(String compactVenueJson) {
		Intent detailIntent = new Intent(this, VenueDetailActivity.class);
		detailIntent.putExtra(VenueDetailFragment.ARG_ITEM_JSON_STRING,compactVenueJson);
		detailIntent.putExtra(VenueDetailActivity.VENUE_DETAILS_SOURCE, VenueDetailActivity.VENUE_DETAIL_SOURCE_LIST);
		startActivity(detailIntent);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		listIdToLoad = "";
		FoursquareListFragment.currentList = null;
		FoursquareListFragment.currentListItems = null;
	}
}