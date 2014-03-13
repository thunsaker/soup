package com.thunsaker.soup.ui.settings;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.thunsaker.soup.R;

@SuppressLint("NewApi")
public class SettingsFragment extends PreferenceFragment {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Boolean isPro = getArguments().getBoolean(SettingsActivity.EXTRA_IS_PRO);
		if(isPro)
			addPreferencesFromResource(R.xml.settings_pro);
		else
			addPreferencesFromResource(R.xml.settings);
	}
}