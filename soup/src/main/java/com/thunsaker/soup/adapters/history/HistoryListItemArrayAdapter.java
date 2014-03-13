package com.thunsaker.soup.adapters.history;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;

import java.util.List;

public class HistoryListItemArrayAdapter extends ArrayAdapter<HistoryListItemBase> {

	public List<HistoryListItemBase> items;
	private LayoutInflater inflater;
	
	public enum RowType {
		LIST_ITEM, HEADER_ITEM, LIST_ITEM_EMPTY
	}
	
	public HistoryListItemArrayAdapter(Context context, List<HistoryListItemBase> items) {
		super(context, 0, items);
		inflater = LayoutInflater.from(context);
		this.items = items;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return items.get(position).getView(inflater, (RelativeLayout)convertView);
	}

	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	@Override
	public int getViewTypeCount() {
		return RowType.values().length;
	}
	
	@Override
	public int getItemViewType(int position) {
		return items.get(position).getViewType();
	}
}
