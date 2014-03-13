package com.thunsaker.soup.adapters.lists;

import android.view.LayoutInflater;
import android.widget.RelativeLayout;

public interface ListsListItemBase {
	public int getViewType();
	public RelativeLayout getView(LayoutInflater inflater, RelativeLayout convertView);
}