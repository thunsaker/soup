package com.thunsaker.soup.adapters.history;

import android.view.LayoutInflater;
import android.widget.RelativeLayout;

public interface HistoryListItemBase {
	public int getViewType();
	public RelativeLayout getView(LayoutInflater inflater, RelativeLayout convertView);
}