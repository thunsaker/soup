package com.thunsaker.soup.ui.settings;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class SettingsActivity extends ActionBarActivity {
	
	public static String EXTRA_IS_PRO = "EXTRA_IS_PRO";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Display the fragment as the main content.
		Bundle args = new Bundle();
		SettingsFragment myFragment = new SettingsFragment();
		args.putBoolean(SettingsActivity.EXTRA_IS_PRO, getIntent()
				.getBooleanExtra(SettingsActivity.EXTRA_IS_PRO, false));
		myFragment.setArguments(args);
		getFragmentManager().beginTransaction()
				.replace(android.R.id.content, myFragment).commit();
	}
}