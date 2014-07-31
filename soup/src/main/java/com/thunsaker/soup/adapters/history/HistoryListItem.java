package com.thunsaker.soup.adapters.history;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.thunsaker.soup.R;
import com.thunsaker.soup.adapters.history.HistoryListItemArrayAdapter.RowType;
import com.thunsaker.soup.data.api.model.Checkin;
import com.thunsaker.soup.util.Util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class HistoryListItem implements HistoryListItemBase {
	public final Checkin checkin;

	public HistoryListItem(LayoutInflater inflater, Checkin checkin) {
		this.checkin = checkin;
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
			((TextView) linearLayoutWrapperName.getChildAt(1))
                    .setText(checkin.venue != null && checkin.venue.name != null ? checkin.venue.name : "");
			ImageView privateImageView = (ImageView) linearLayoutWrapperName.getChildAt(0);
			((TextView) linearLayoutWrapper.getChildAt(1))
                    .setText(checkin.venue != null && checkin.venue.location != null && checkin.venue.location.address != null
                            ? checkin.venue.location.address : "");
			ImageView alertImageView = (ImageView) relativeLayoutWrapper.getChildAt(1);

			Calendar myDate = Calendar.getInstance();
			long timeInMilis = Long.parseLong(checkin.createdAt) * 1000;
			myDate.setTimeInMillis(timeInMilis);

			SimpleDateFormat dateFormatTime = new SimpleDateFormat("hh:mm a", Locale.getDefault()); // Time (with AM/PM)

			((TextView) relativeLayoutWrapper.getChildAt(2)).setText(dateFormatTime.format(myDate.getTime()));

			if(checkin.venue == null || Util.VenueHasProblems(checkin.venue))
				alertImageView.setVisibility(View.VISIBLE);
			else
				alertImageView.setVisibility(View.GONE);

			if(checkin.isPrivate) {
				privateImageView.setVisibility(View.VISIBLE);
			} else {
				privateImageView.setVisibility(View.GONE);
			}
		} else {
			view = (RelativeLayout) inflater.inflate(R.layout.list_history_item_empty, null);
		}

		return view;
	}
}