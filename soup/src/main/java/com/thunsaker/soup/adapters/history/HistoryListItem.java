package com.thunsaker.soup.adapters.history;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.thunsaker.soup.R;
import com.thunsaker.soup.adapters.history.HistoryListItemArrayAdapter.RowType;
import com.thunsaker.soup.classes.foursquare.Checkin;
import com.thunsaker.soup.ui.VenueDetailActivity;
import com.thunsaker.soup.util.Util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class HistoryListItem implements HistoryListItemBase {
	private final Checkin checkin;
	private final ActionBarActivity activity;

	public HistoryListItem(LayoutInflater inflater, Checkin checkin, ActionBarActivity activity) {
		this.checkin = checkin;
		this.activity = activity;
	}

	@Override
	public int getViewType() {
		if(checkin == null)
			return RowType.LIST_ITEM_EMPTY.ordinal();
		else
			return RowType.LIST_ITEM.ordinal();
	}

	@Override
	public RelativeLayout getView(LayoutInflater inflater, RelativeLayout convertView) {
		RelativeLayout view;

		if(checkin != null) {
			if(convertView == null) {
				view = (RelativeLayout) inflater.inflate(R.layout.list_history_item, null);
			} else {
				view = convertView;
			}

			RelativeLayout relativeLayoutWrapper = (RelativeLayout) view.getChildAt(0);
			LinearLayout linearLayoutWrapper = (LinearLayout) relativeLayoutWrapper.getChildAt(0);
			LinearLayout linearLayoutWrapperName = (LinearLayout) linearLayoutWrapper.getChildAt(0);
			((TextView) linearLayoutWrapperName.getChildAt(1)).setText(checkin.getVenue().getName());
			ImageView privateImageView = (ImageView) linearLayoutWrapperName.getChildAt(0);
			((TextView) linearLayoutWrapper.getChildAt(1)).setText(checkin.getVenue().getLocation().getAddress());
			ImageView alertImageView = (ImageView) relativeLayoutWrapper.getChildAt(1);

			Calendar myDate = Calendar.getInstance();
			long timeInMilis = Long.parseLong(checkin.getCreatedDate()) * 1000;
			myDate.setTimeInMillis(timeInMilis);

			SimpleDateFormat dateFormatTime = new SimpleDateFormat("hh:mm a", Locale.getDefault()); // Time (with AM/PM)

			((TextView) relativeLayoutWrapper.getChildAt(2)).setText(dateFormatTime.format(myDate.getTime()));

			if(Util.VenueHasProblems(checkin.getVenue()))
				alertImageView.setVisibility(View.VISIBLE);
			else
				alertImageView.setVisibility(View.GONE);

			final String venueId = checkin.getVenue().getId();
			final ActionBarActivity myActivity = activity;

			if(checkin.isIsPrivate()) {
				privateImageView.setVisibility(View.VISIBLE);
			} else {
				privateImageView.setVisibility(View.GONE);
			}

			relativeLayoutWrapper.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent venueDetailsIntent = new Intent(myActivity.getApplicationContext(), VenueDetailActivity.class);
					venueDetailsIntent.putExtra(VenueDetailActivity.VENUE_TO_LOAD_EXTRA, venueId);
					myActivity.startActivity(venueDetailsIntent);
				}
			});
		} else {
			view = (RelativeLayout) inflater.inflate(R.layout.list_history_item_empty, null);
		}

		return view;
	}
}