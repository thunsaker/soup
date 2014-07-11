package com.thunsaker.soup.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.thunsaker.soup.R;
import com.thunsaker.soup.adapters.history.HistoryListItemArrayAdapter;
import com.thunsaker.soup.adapters.history.HistoryListItemBase;
import com.thunsaker.soup.adapters.history.HistoryListItemHeader;
import com.thunsaker.soup.app.BaseSoupActivity;
import com.thunsaker.soup.data.api.model.Checkin;
import com.thunsaker.soup.services.foursquare.FoursquarePrefs;
import com.thunsaker.soup.services.foursquare.endpoints.UserEndpoint;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/*
 * Created by @thunsaker
 */
public class HistoryActivity extends BaseSoupActivity implements
		ActionBar.OnNavigationListener {

	private boolean useLogo = true;
	private boolean showHomeUp = true;

	public static List<Checkin> historyListToday;
	public static List<Checkin> historyListLastWeek;
	public static List<Checkin> historyListLastMonth;

	private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";

	static SimpleDateFormat dateFormat;
	static SimpleDateFormat dateFormatWithYear;
	static SimpleDateFormat dateFormatTime;
	static SimpleDateFormat dateFormatDay;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_PROGRESS);

		setContentView(R.layout.activity_history);

		ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(showHomeUp);
		ab.setDisplayUseLogoEnabled(useLogo);

		setProgressBarVisibility(true);
		setProgressBarIndeterminate(true);

		// Set up the action bar to show a dropdown list.
		final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

		// Set up the dropdown list navigation in the action bar.
		actionBar.setListNavigationCallbacks(
				// Specify a SpinnerAdapter to populate the dropdown list.
				new ArrayAdapter<String>(this.getApplicationContext(),
						android.R.layout.simple_list_item_1,
						android.R.id.text1, new String[] {
								getString(R.string.history_title_today),
								getString(R.string.history_title_last_week),
								getString(R.string.history_title_last_month), }), this);

		setProgressBarVisibility(false);

        // Date with Short Month
		dateFormat = new SimpleDateFormat("dd-MMM", Locale.getDefault());
        // Date with Short Month an Year
		dateFormatWithYear = new SimpleDateFormat("dd-MMM-y", Locale.getDefault());
        // Time (with AM/PM)
		dateFormatTime = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        // Day of Week
		dateFormatDay = new SimpleDateFormat("EEEE", Locale.getDefault());
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		// Restore the previously serialized current dropdown position.
		if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
			getSupportActionBar().setSelectedNavigationItem(
					savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// Serialize the current dropdown position.
		outState.putInt(STATE_SELECTED_NAVIGATION_ITEM, getSupportActionBar()
				.getSelectedNavigationIndex());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_history, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		case R.id.action_history_today:
			switch (getSupportActionBar().getSelectedNavigationIndex()) {
			case 1: // FoursquarePrefs.History.View.LAST_WEEK
				((ListView) findViewById(R.id.listViewHistoryLastWeek))
                        .setSelectionAfterHeaderView();
				return true;
			case 2: // FoursquarePrefs.History.View.LAST_MONTH
				((ListView) findViewById(R.id.listViewHistoryLastMonth))
                        .setSelectionAfterHeaderView();
				return true;
			default: // FoursquarePrefs.History.View.TODAY
				((ListView) findViewById(R.id.listViewHistoryToday))
                        .setSelectionAfterHeaderView();
				return true;
			}
		case R.id.action_history_refresh:
			RefreshData(getSupportActionBar().getSelectedNavigationIndex(),
                    getLayoutInflater(), (ViewGroup)findViewById(R.id.frameLayoutHistoryContainer),
                    HistoryActivity.this);
			return true;
		default:
			return false;
		}
	}

	@Override
	public boolean onNavigationItemSelected(int position, long id) {
		// When the given dropdown item is selected, show its contents in the
		// container view.
		Fragment fragment = new DummySectionFragment();
		Bundle args = new Bundle();
		args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, position);
		fragment.setArguments(args);
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.frameLayoutHistoryContainer, fragment).commit();
		return true;
	}

    /**
	 * A dummy fragment representing a section of the app, but that simply
	 * displays dummy text.
	 */
	public static class DummySectionFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		public static final String ARG_SECTION_NUMBER = "section_number";

		public DummySectionFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			return RefreshData(
                    getArguments().getInt(ARG_SECTION_NUMBER),
                    inflater, container, (BaseSoupActivity) getActivity());
		}
	}

	// TODO: When/if I feel like getting fancy with the dropdown
	// Refer to this: http://stackoverflow.com/questions/15193598/actionbar-spinner-customisation

    @SuppressWarnings("all")
	public static View RefreshData(int section, LayoutInflater inflater, ViewGroup container,
                                   BaseSoupActivity theActivity) {
		try {
			View rootView;

//			SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM", Locale.getDefault());
			Calendar cal_today = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
			long todayActualUnixTime = cal_today.getTimeInMillis() / 1000;
//			String todayFormatted = dateFormat.format(cal_today.getTime());

			Calendar cal_yesterday = Calendar.getInstance();
			cal_yesterday.add(Calendar.DATE, -1);
//			String yesterdayFormatted = dateFormat.format(cal_yesterday.getTime());

			cal_yesterday.set(Calendar.HOUR_OF_DAY, 0);
			cal_yesterday.set(Calendar.MINUTE, 0);
			cal_yesterday.set(Calendar.SECOND, 0);
			cal_yesterday.set(Calendar.MILLISECOND, 0);
			long yesterdayUnixTime = cal_yesterday.getTimeInMillis() / 1000;
			cal_yesterday.add(Calendar.DATE, -6);
			long lastWeekUnixTime = cal_yesterday.getTimeInMillis() / 1000;
			cal_yesterday.add(Calendar.DATE, -24);
			long lastMonthUnixTime = cal_yesterday.getTimeInMillis() / 1000;

			switch (section) {
			case 1: // HISTORY_VIEW_LAST_WEEK
				rootView = inflater.inflate(R.layout.fragment_history_last_week, container, false);
				new UserEndpoint.GetCheckins(theActivity.getApplicationContext(),
					theActivity, lastWeekUnixTime, todayActualUnixTime,
					0, 0, FoursquarePrefs.History.Sort.NEWEST,
                        FoursquarePrefs.History.View.LAST_WEEK).execute();
				break;
			case 2: // HISTORY_VIEW_LAST_MONTH
				rootView = inflater.inflate(R.layout.fragment_history_last_month, container, false);
				new UserEndpoint.GetCheckins(theActivity.getApplicationContext(),
					theActivity, lastMonthUnixTime, todayActualUnixTime,
					0, 0, FoursquarePrefs.History.Sort.NEWEST,
                        FoursquarePrefs.History.View.LAST_MONTH).execute();
				break;
			default: // HISTORY_VIEW_TODAY
				rootView = inflater.inflate(R.layout.fragment_history_today, container, false);

				new UserEndpoint.GetCheckins(
                        theActivity.getApplicationContext(),
                        theActivity, yesterdayUnixTime, todayActualUnixTime,
                        0, 0, FoursquarePrefs.History.Sort.NEWEST,
                        FoursquarePrefs.History.View.TODAY).execute();
				break;
			}

			return rootView;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	// Reference: http://stackoverflow.com/questions/13590627/android-listview-headers
    @SuppressWarnings("all")
	public static void SetupHistoryView(BaseSoupActivity theActivity) {
		LayoutInflater inflater = theActivity.getLayoutInflater();

		LinearLayout rootView;
		List<Checkin> listToLoad;
		boolean isTodayView = false;

		Calendar myCal = Calendar.getInstance();
		boolean hasHeader = false;
		Calendar myCalLast = Calendar.getInstance();
		myCalLast.add(Calendar.DATE, 1);
		Calendar todayCal = Calendar.getInstance();
        ActionBar myActionBar = theActivity.getSupportActionBar();
		switch (myActionBar.getSelectedNavigationIndex()) {
		case 1: // FoursquarePrefs.History.View.LAST_WEEK
			rootView = (LinearLayout) theActivity
					.findViewById(R.id.linearLayoutHistoryLastWeekWrapper);
			listToLoad = new ArrayList<Checkin>(historyListLastWeek);
			break;
		case 2: // FoursquarePrefs.History.View.LAST_MONTH
			rootView = (LinearLayout) theActivity
					.findViewById(R.id.linearLayoutHistoryLastMonthWrapper);
			listToLoad = new ArrayList<Checkin>(historyListLastMonth);
			break;
		default: // FoursquarePrefs.History.View.TODAY
			rootView = (LinearLayout) theActivity
					.findViewById(R.id.linearLayoutHistoryTodayWrapper);
			listToLoad = new ArrayList<Checkin>(historyListToday);
			isTodayView = true;
			break;
		}

		ListView myListView = (ListView) rootView.getChildAt(0);
		List<HistoryListItemBase> myList = new ArrayList<HistoryListItemBase>();
		boolean hasToday = false;
		boolean hasYesterday = false;

		for (Checkin c : listToLoad) {
			long timeInMillis = Long.parseLong(c.createdDate) * 1000;
			myCal.setTimeInMillis(timeInMillis);

			if (myCal.get(Calendar.DAY_OF_MONTH) != myCalLast
					.get(Calendar.DAY_OF_MONTH))
				hasHeader = false;

			if (!hasHeader) {
				if (theActivity.getSupportActionBar()
						.getSelectedNavigationIndex() == FoursquarePrefs.History.View.TODAY) {
					if ((myCal.get(Calendar.DATE) == todayCal
							.get(Calendar.DATE))
							&& (myCal.get(Calendar.MONTH) == todayCal
									.get(Calendar.MONTH))) {
						hasToday = true;
						myList.add(new HistoryListItemHeader(
								inflater,
								theActivity
										.getString(R.string.history_section_title_today),
								dateFormat.format(myCal.getTime())));
					} else {
						hasYesterday = true;
						myList.add(new HistoryListItemHeader(
								inflater,
								theActivity
										.getString(R.string.history_section_title_yesterday),
								dateFormat.format(myCal.getTime())));
					}
				} else {
					myList.add(new HistoryListItemHeader(inflater,
							dateFormatDay.format(myCal.getTime()), dateFormat
									.format(myCal.getTime())));
				}
				hasHeader = true;
				myCalLast.setTimeInMillis(timeInMillis);
			}

			myList.add(new com.thunsaker.soup.adapters.history.HistoryListItem(
					inflater, c, theActivity));
		}

		if(isTodayView) {
			if(!hasToday) {
				myList.add(0, new com.thunsaker.soup.adapters.history.HistoryListItem(inflater, null, theActivity));
				myList.add(0, new HistoryListItemHeader(inflater, theActivity.getString(R.string.history_section_title_today), dateFormat.format(todayCal.getTime())));

			}

			if(!hasYesterday) {
				todayCal.add(Calendar.DATE, -1);
				myList.add(new HistoryListItemHeader(inflater, theActivity.getString(R.string.history_section_title_yesterday), dateFormat.format(todayCal.getTime())));
				myList.add(new com.thunsaker.soup.adapters.history.HistoryListItem(inflater, null, theActivity));
			}
		}

		HistoryListItemArrayAdapter adapter =
				new HistoryListItemArrayAdapter(theActivity.getApplicationContext(), myList);
		myListView.setAdapter(adapter);
//        mPullToRefreshHelper.setRefreshableView(myListView, theActivity);
	}
}