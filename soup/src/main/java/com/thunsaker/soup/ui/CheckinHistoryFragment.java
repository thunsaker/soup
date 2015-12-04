package com.thunsaker.soup.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.timessquare.CalendarPickerView;
import com.thunsaker.android.common.annotations.ForApplication;
import com.thunsaker.soup.R;
import com.thunsaker.soup.adapters.history.HistoryListItem;
import com.thunsaker.soup.adapters.history.HistoryListItemArrayAdapter;
import com.thunsaker.soup.adapters.history.HistoryListItemBase;
import com.thunsaker.soup.adapters.history.HistoryListItemHeader;
import com.thunsaker.soup.app.BaseSoupFragment;
import com.thunsaker.soup.app.SoupApp;
import com.thunsaker.soup.data.api.model.Checkin;
import com.thunsaker.soup.data.events.CheckinHistoryEvent;
import com.thunsaker.soup.services.foursquare.FoursquarePrefs;
import com.thunsaker.soup.services.foursquare.FoursquareTasks;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

public class CheckinHistoryFragment extends BaseSoupFragment
        implements SwipeRefreshLayout.OnRefreshListener,
        AbsListView.OnItemClickListener,
        AbsListView.OnItemLongClickListener {

    @Inject
    @ForApplication
    Context mContext;

    @Inject
    EventBus mBus;

    public static final String ARG_SECTION_NUMBER = "section_number";
    private static final String ARG_CHECKIN_START = "start_date";
    private static final String ARG_CHECKIN_END = "end_date";

    private int mSection;
    private long mStartDate;
    private long mEndDate;

    static SimpleDateFormat dateFormat;
    static SimpleDateFormat dateFormatWithYear;
    static SimpleDateFormat dateFormatTime;
    static SimpleDateFormat dateFormatDay;

    @InjectView(R.id.swipeLayoutCheckinHistoryContainer) SwipeRefreshLayout mSwipeViewCheckinContainer;
    @InjectView(R.id.relativeLayoutHistoryDateRangeWrapper) RelativeLayout mWrapperOuter;
    @InjectView(R.id.linearLayoutHistoryDateRangeBarWrapper) LinearLayout mBarWrapper;
    @InjectView(R.id.textViewHistoryDateRange) TextView mTextDateRange;
    @InjectView(R.id.imageViewHistoryDateRangeExpand) ImageView mImageViewToggle;

    @InjectView(R.id.linearLayoutHistoryDateRangeCalendarWrapper) LinearLayout mCalendarWrapper;
    @InjectView(R.id.calendarHistoryDateRange) CalendarPickerView mCalendarPicker;
    @InjectView(R.id.buttonHistoryGetCheckins) Button mButtonGetHistory;

    private OnFragmentInteractionListener mListener;
    private boolean mIsTodayView;

    public ListView mListView;
    private boolean calendarOpen;
    private String mSortOrder;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param sectionNumber Which pre-selected view should be displayed.
     * @param startDate Custom start date timestamp.
     * @param endDate Custom end date timestamp.
     * @return A new instance of fragment CheckinHistoryFragment.
     */
    public static CheckinHistoryFragment newInstance(int sectionNumber, long startDate, long endDate) {
        CheckinHistoryFragment fragment = new CheckinHistoryFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        args.putLong(ARG_CHECKIN_START, startDate);
        args.putLong(ARG_CHECKIN_END, endDate);
        fragment.setArguments(args);
        return fragment;
    }
    public CheckinHistoryFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(mBus != null && !mBus.isRegistered(this))
            mBus.register(this);

        setHasOptionsMenu(true);

        if (getArguments() != null) {
            mSection = getArguments().getInt(ARG_SECTION_NUMBER);
            mStartDate = getArguments().getLong(ARG_CHECKIN_START);
            mEndDate = getArguments().getLong(ARG_CHECKIN_END);

            if(mStartDate > 0 || mEndDate > 0)
                RefreshData();
        }

        // Date with Short Month
        dateFormat = new SimpleDateFormat("dd-MMM", Locale.getDefault());
        // Date with Short Month an Year
        dateFormatWithYear = new SimpleDateFormat("dd-MMM-y", Locale.getDefault());
        // Time (with AM/PM)
        dateFormatTime = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        // Day of Week
        dateFormatDay = new SimpleDateFormat("EEEE", Locale.getDefault());
    }

    private void RefreshData() {
        if(mSwipeViewCheckinContainer != null)
            mSwipeViewCheckinContainer.setRefreshing(true);

        if(mSection == CheckinHistoryActivity.CHECKIN_SECTION_CUSTOM)
            mSortOrder = FoursquarePrefs.History.Sort.OLDEST;
        else
            mSortOrder = FoursquarePrefs.History.Sort.NEWEST;

        FoursquareTasks mFoursquareTasks = new FoursquareTasks((SoupApp) mContext);
        mFoursquareTasks.new GetUserHistory(mStartDate, mEndDate, FoursquarePrefs.History.Limit.MAX, 0, mSortOrder).execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_checkin_history, container, false);

        mListView = (ListView) rootView.findViewById(android.R.id.list);
        mListView.setSelector(R.drawable.layout_selector_green);
        mListView.setEmptyView(rootView.findViewById(android.R.id.empty));
        mListView.setOnItemClickListener(this);
        mListView.setOnItemLongClickListener(this);

        ButterKnife.inject(this, rootView);

        Calendar todayDate = Calendar.getInstance();
        Calendar maxDate = Calendar.getInstance();
        maxDate.set(Calendar.HOUR, 23);
        maxDate.set(Calendar.MINUTE, 59);
        maxDate.set(Calendar.SECOND, 59);
        maxDate.add(Calendar.HOUR, 24);
        Calendar minDate = Calendar.getInstance();
        minDate.add(Calendar.YEAR, -1);
//        minDate.set(Calendar.MONTH, 1);
//        minDate.set(Calendar.DAY_OF_MONTH, 1);
//        minDate.set(Calendar.HOUR, 0);
//        minDate.set(Calendar.MINUTE, 1);
        ArrayList<Date> dates = new ArrayList<Date>();

        if(mStartDate > 0 && mEndDate > 0) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(mStartDate * 1000);
            Date startDate = calendar.getTime();
            calendar.setTimeInMillis(mEndDate * 1000);
            Date endDate = calendar.getTime();

            dates.add(startDate);
            dates.add(endDate);
            mCalendarPicker
                    .init(minDate.getTime(), maxDate.getTime())
                    .inMode(CalendarPickerView.SelectionMode.RANGE)
                    .withSelectedDates(dates);

            mTextDateRange.setText(String.format("%s - %s", dateFormat.format(endDate), dateFormat.format(startDate)));

            CloseCalendarPicker();
        } else {

            mCalendarPicker
                    .init(minDate.getTime(), maxDate.getTime())
                    .inMode(CalendarPickerView.SelectionMode.RANGE)
                    .withSelectedDate(todayDate.getTime());
            OpenCalendarPicker();
        }

        if(mSwipeViewCheckinContainer != null) {
            mSwipeViewCheckinContainer.setOnRefreshListener(this);
            mSwipeViewCheckinContainer.setColorSchemeColors(
                    getResources().getColor(R.color.soup_green),
                    getResources().getColor(R.color.soup_blue),
                    getResources().getColor(R.color.soup_green),
                    getResources().getColor(R.color.soup_red));
            mSwipeViewCheckinContainer.setRefreshing(true);
        }

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.checkin_history, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_history_top) {
            mListView.setSelectionAfterHeaderView();
            return true;
        } else
            return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        try {
            HistoryListItem clickedItem = (HistoryListItem) mListView.getItemAtPosition(position);
            mListener.onClick(clickedItem.checkin.venue.toString());
        } catch (Exception ignored) { }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        try {
            HistoryListItem clickedItem = (HistoryListItem) mListView.getItemAtPosition(position);
            mListener.onLongClick(clickedItem.checkin.id, clickedItem.checkin.venue.name);

            // TODO: Show popup list delete/share/comment/etc?
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    public void onEvent(CheckinHistoryEvent event) {
        if (mSwipeViewCheckinContainer != null)
            mSwipeViewCheckinContainer.setRefreshing(false);

        mIsTodayView = mSection == CheckinHistoryActivity.CHECKIN_SECTION_TODAY;

        if (event.result && event.resultHistoryList != null && event.resultHistoryList.size() > 0) {
            List<Checkin> listToLoad = event.resultHistoryList;
            LayoutInflater inflater = LayoutInflater.from(mContext);

            Calendar myCal = Calendar.getInstance();
            boolean hasHeader = false;
            Calendar myCalLast = Calendar.getInstance();
            myCalLast.add(Calendar.DATE, 1);
            Calendar todayCal = Calendar.getInstance();

            List<HistoryListItemBase> myList = new ArrayList<HistoryListItemBase>();
            boolean hasToday = false;
            boolean hasYesterday = false;

            for (Checkin c : listToLoad) {
                long timeInMillis = Long.parseLong(c.createdAt) * 1000;
                myCal.setTimeInMillis(timeInMillis);

                if (myCal.get(Calendar.DAY_OF_MONTH) !=
                        myCalLast.get(Calendar.DAY_OF_MONTH))
                    hasHeader = false;

                if (!hasHeader) {
                    if (mSection == FoursquarePrefs.History.View.TODAY) {
                        if ((myCal.get(Calendar.DATE) == todayCal
                                .get(Calendar.DATE))
                                && (myCal.get(Calendar.MONTH) == todayCal
                                .get(Calendar.MONTH))) {
                            hasToday = true;
                            myList.add(new HistoryListItemHeader(
                                    inflater,
                                    mContext.getString(R.string.history_section_title_today),
                                    dateFormat.format(myCal.getTime())
                            ));
                        } else {
                            hasYesterday = true;
                            myList.add(new HistoryListItemHeader(
                                    inflater,
                                    mContext.getString(R.string.history_section_title_yesterday),
                                    dateFormat.format(myCal.getTime())
                            ));
                        }
                    } else {
                        myList.add(new HistoryListItemHeader(inflater,
                                dateFormatDay.format(myCal.getTime()), dateFormat
                                .format(myCal.getTime())
                        ));
                    }
                    hasHeader = true;
                    myCalLast.setTimeInMillis(timeInMillis);
                }

                myList.add(new HistoryListItem(inflater, c));
            }

            if (mIsTodayView) {
                if (!hasToday) {
                    myList.add(0, new HistoryListItem(inflater, null));
                    myList.add(0, new HistoryListItemHeader(inflater, mContext.getString(R.string.history_section_title_today), dateFormat.format(todayCal.getTime())));

                }

                if (!hasYesterday) {
                    todayCal.add(Calendar.DATE, -1);
                    myList.add(new HistoryListItem(inflater, null));
                    myList.add(
                            new HistoryListItemHeader(
                                    inflater,
                                    mContext.getString(R.string.history_section_title_yesterday),
                                    dateFormat.format(todayCal.getTime()))
                    );
                }
            }

            HistoryListItemArrayAdapter adapter =
                    new HistoryListItemArrayAdapter(mContext, myList);
            mListView.setAdapter(adapter);
        }
    }

    @Override
    public void onRefresh() {
        RefreshData();
    }

    public interface OnFragmentInteractionListener {
        public void onClick(String checkinVenueId);
        public boolean onLongClick(String checkinId, String checkinVenueName);
    }

    @OnClick(R.id.relativeLayoutHistoryDateRangeWrapper)
    public void CalendarPanelClicked() {
        if(calendarOpen) {
            CloseCalendarPicker();
        } else {
            OpenCalendarPicker();
        }
    }

    private void CloseCalendarPicker() {
        calendarOpen = false;
        mCalendarWrapper.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.abc_slide_out_top));
        mCalendarWrapper.setVisibility(View.GONE);
        mImageViewToggle.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_unfold_more));
    }

    private void OpenCalendarPicker() {
        calendarOpen = true;
        mCalendarWrapper.setVisibility(View.VISIBLE);
        mCalendarWrapper.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.abc_slide_in_top));
        mImageViewToggle.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_unfold_less));
    }


    @SuppressWarnings("NewApi")
    @SuppressLint("NewApi")
    @OnClick(R.id.buttonHistoryGetCheckins)
    public void GetCustomDateRange() {
        boolean isSingleDay = false;

        if(mCalendarPicker != null) {
            List<Date> dates = mCalendarPicker.getSelectedDates();
            if(dates != null && dates.size() > 0) {
                mEndDate = dates.get(0).getTime() / 1000; // Convert to seconds for Foursquare's API
                if(dates.get(dates.size() - 1) != null) {
                    mStartDate = dates.get(dates.size() - 1).getTime() / 1000; // Convert to seconds for Foursquare's API
                } else {
                    isSingleDay = true;
                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(mEndDate * 1000);
                    cal.set(Calendar.HOUR, 0);
                    cal.set(Calendar.MINUTE, 1);
                    cal.set(Calendar.SECOND, 1);
                    Date endDate = cal.getTime();
                    mEndDate = endDate.getTime() / 1000;
                    cal.set(Calendar.HOUR, 23);
                    cal.set(Calendar.MINUTE, 59);
                    cal.set(Calendar.SECOND, 59);
                    Date startDate = cal.getTime();
                    mStartDate = startDate.getTime() / 1000;
                }
            }
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(mStartDate * 1000);
        Date startDate = calendar.getTime();
        calendar.setTimeInMillis(mEndDate * 1000);
        Date endDate = calendar.getTime();
        mTextDateRange.setText(isSingleDay ? dateFormat.format(startDate) : String.format("%s - %s", dateFormat.format(endDate), dateFormat.format(startDate)));

        CheckinHistoryActivity.customStartDate = mStartDate;
        CheckinHistoryActivity.customEndDate = mEndDate;

        // FIXME: 11/10/2015 - Determine if I need to call this on the custom date range
//        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//            assert getActivity() != null;
//            assert getActivity().getActionBar() != null;
//            getActivity().getActionBar().setSelectedNavigationItem(3);
//        }

        CloseCalendarPicker();
        RefreshData();
    }
}