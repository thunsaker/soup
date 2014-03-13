package com.thunsaker.soup.ui.nav;

import com.thunsaker.soup.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class NavigationDrawerAdapter extends ArrayAdapter<NavigationDrawerItem> {

	private LayoutInflater inflater;

	public NavigationDrawerAdapter(Context context, int textViewResourceId,
			NavigationDrawerItem[] drawerMenu) {
		super(context, textViewResourceId, drawerMenu);
		this.inflater = LayoutInflater.from(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = null;
		NavigationDrawerItem menuItem = this.getItem(position);
		if (menuItem.getType() == NavigationMenuItem.ITEM_TYPE) {
			view = getItemView(convertView, parent, menuItem);
		} else {
			view = getSectionView(convertView, parent, menuItem);
		}
		return view;
	}

	public View getSectionView(View convertView, ViewGroup parentView,
			NavigationDrawerItem navigationDrawerItem) {

		NavigationMenuSection menuSection = (NavigationMenuSection) navigationDrawerItem;
		NavigationMenuSectionHolder navMenuItemHolder = null;

		if (convertView == null) {
			convertView = inflater.inflate(R.layout.drawer_section, parentView,
					false);
			TextView labelView = (TextView) convertView
					.findViewById(R.id.drawer_section_title);

			navMenuItemHolder = new NavigationMenuSectionHolder();
			navMenuItemHolder.labelView = labelView;
			convertView.setTag(navMenuItemHolder);
		}

		if (navMenuItemHolder == null) {
			navMenuItemHolder = (NavigationMenuSectionHolder) convertView
					.getTag();
		}

		navMenuItemHolder.labelView.setText(menuSection.getLabel());

		return convertView;
	}

	public View getItemView(View convertView, ViewGroup parentView,
			NavigationDrawerItem navigationDrawerItem) {

		NavigationMenuItem menuItem = (NavigationMenuItem) navigationDrawerItem;
		NavigationMenuItemHolder navMenuItemHolder = null;

		if (convertView == null) {
			convertView = inflater.inflate(R.layout.drawer_item, parentView,
					false);
			TextView labelView = (TextView) convertView
					.findViewById(R.id.drawer_item_title);
			ImageView iconView = (ImageView) convertView
					.findViewById(R.id.drawer_item_icon);

			navMenuItemHolder = new NavigationMenuItemHolder();
			navMenuItemHolder.labelView = labelView;
			navMenuItemHolder.iconView = iconView;

			convertView.setTag(navMenuItemHolder);
		}

		if (navMenuItemHolder == null) {
			navMenuItemHolder = (NavigationMenuItemHolder) convertView.getTag();
		}

		navMenuItemHolder.labelView.setText(menuItem.getLabel());
		navMenuItemHolder.iconView.setImageResource(menuItem.getIcon());

		return convertView;
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public int getItemViewType(int position) {
		return this.getItem(position).getType();
	}

	@Override
	public boolean isEnabled(int position) {
		return getItem(position).isEnabled();
	}

	private static class NavigationMenuItemHolder {
		private TextView labelView;
		private ImageView iconView;
	}

	private class NavigationMenuSectionHolder {
		private TextView labelView;
	}
}