package com.thunsaker.soup.ui.nav;

public interface NavigationDrawerItem {
    public int getId();
    public String getLabel();
    public int getType();
    public boolean isEnabled();
    public boolean updateActionBarTitle();
}
