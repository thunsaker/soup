package com.thunsaker.soup.ui.nav;

import android.widget.BaseAdapter;

public class NavigationDrawerActivityConfiguration {
	private int mainLayout;
	private int drawerShadow;
	private int drawerLayoutId;
	private int leftDrawerId;
	private int[] actionMenuItemsToHideWhenDrawerOpen;
	private NavigationDrawerItem[] navigationItems;
	private int drawerOpenDescription;
	private int drawerCloseDescription;
	private BaseAdapter baseAdapter;
	private boolean requestWindowFeatureProgress;
	
	public int getMainLayout() {
		return mainLayout;
	}
	public void setMainLayout(int mainLayout) {
		this.mainLayout = mainLayout;
	}
	
	public int getDrawerShadow() {
		return drawerShadow;
	}
	public void setDrawerShadow(int drawerShadow) {
		this.drawerShadow = drawerShadow;
	}
	
	public int getDrawerLayoutId() {
		return drawerLayoutId;
	}
	public void setDrawerLayoutId(int drawerLayoutId) {
		this.drawerLayoutId = drawerLayoutId;
	}
	
	public int getLeftDrawerId() {
		return leftDrawerId;
	}
	public void setLeftDrawerId(int leftDrawerId) {
		this.leftDrawerId = leftDrawerId;
	}
	
	public int[] getActionMenuItemsToHideWhenDrawerOpen() {
		return actionMenuItemsToHideWhenDrawerOpen;
	}
	public void setActionMenuItemsToHideWhenDrawerOpen(
			int[] actionMenuItemsToHideWhenDrawerOpen) {
		this.actionMenuItemsToHideWhenDrawerOpen = actionMenuItemsToHideWhenDrawerOpen;
	}
	
	public NavigationDrawerItem[] getNavigationItems() {
		return navigationItems;
	}
	public void setNavigationItems(NavigationDrawerItem[] navigationItems) {
		this.navigationItems = navigationItems;
	}
	
	public int getDrawerOpenDescription() {
		return drawerOpenDescription;
	}
	public void setDrawerOpenDescription(int drawerOpenDescription) {
		this.drawerOpenDescription = drawerOpenDescription;
	}
	
	public int getDrawerCloseDescription() {
		return drawerCloseDescription;
	}
	public void setDrawerCloseDescription(int drawerCloseDescription) {
		this.drawerCloseDescription = drawerCloseDescription;
	}
	
	public BaseAdapter getBaseAdapter() {
		return baseAdapter;
	}
	public void setBaseAdapter(BaseAdapter baseAdapter) {
		this.baseAdapter = baseAdapter;
	}
	
	public boolean getRequestWindowFeatureProgress() {
		return requestWindowFeatureProgress;
	}
	public void setRequestWindowFeatureProgress(boolean requestWindowFeatureProgress) {
		this.requestWindowFeatureProgress = requestWindowFeatureProgress;
	}
}