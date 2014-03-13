package com.thunsaker.soup.adapters.history;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.thunsaker.soup.adapters.history.HistoryListItemArrayAdapter.RowType;
import com.thunsaker.soup.R;

import java.util.Locale;

public class HistoryListItemHeader implements HistoryListItemBase {
	private final String weekday;
	private final String date;

	public HistoryListItemHeader(LayoutInflater inflater, String weekday, String date) {
		this.weekday = weekday;
		this.date = date;
	}

	@Override
	public int getViewType() {
		return RowType.HEADER_ITEM.ordinal();
	}

	@Override
	public RelativeLayout getView(LayoutInflater inflater, RelativeLayout convertView) {
		RelativeLayout view;
		if(convertView == null) {
			view = (RelativeLayout) inflater.inflate(R.layout.list_history_header, null);
		} else {
			view = convertView;
		}

		((TextView) view.getChildAt(0)).setText(weekday.toUpperCase(Locale.US));
		((TextView) view.getChildAt(1)).setText(date.toUpperCase(Locale.US));

		view.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				v.setSelected(false);
			}
		});

		return view;
	}
}