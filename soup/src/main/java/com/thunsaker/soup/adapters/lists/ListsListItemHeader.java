package com.thunsaker.soup.adapters.lists;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.thunsaker.soup.adapters.history.HistoryListItemArrayAdapter.RowType;
import com.thunsaker.soup.R;

import java.util.Locale;

public class ListsListItemHeader implements ListsListItemBase {
	private final String listType;

	public ListsListItemHeader(LayoutInflater inflater, String listType) {
		this.listType = listType;
	}

	@Override
	public int getViewType() {
		return RowType.HEADER_ITEM.ordinal();
	}

	@Override
	public RelativeLayout getView(LayoutInflater inflater, RelativeLayout convertView) {
		RelativeLayout view;
		if(convertView == null) {
			view = (RelativeLayout) inflater.inflate(R.layout.list_lists_header, null);
		} else {
			view = convertView;
		}

		((TextView) view.getChildAt(1)).setText(listType.toUpperCase(Locale.US));

		view.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				v.setSelected(false);
			}
		});

		return view;
	}
}