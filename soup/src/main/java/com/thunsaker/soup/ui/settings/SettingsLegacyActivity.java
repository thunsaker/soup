package com.thunsaker.soup.ui.settings;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.thunsaker.soup.R;

public class SettingsLegacyActivity extends PreferenceActivity {
	final static String ACTION_PREFS_ONE = "com.thunsaker.soup.ui.SettingsLegacyActivity";
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        // Load the legacy preferences headers
        addPreferencesFromResource(R.xml.preference_headers_legacy);
	}
}