package com.thunsaker.soup.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.thunsaker.soup.R;
import com.tundem.aboutlibraries.Libs;
import com.tundem.aboutlibraries.ui.LibsActivity;

/*
 * Created by @thunsaker
 */
public class AboutFragment extends Fragment {
	public AboutFragment() {}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_about,
				container, false);

		Button mButtonGooglePlus = (Button)rootView.findViewById(R.id.about_follow);
		mButtonGooglePlus.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				getActivity().startActivity(new Intent(
						Intent.ACTION_VIEW,
						Uri.parse("https://plus.google.com/u/0/114888349644757313309/")));
			}
		});

		Button mButtonOtherApps = (Button)rootView.findViewById(R.id.about_other_apps);
		mButtonOtherApps.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				getActivity().startActivity(new Intent(
						Intent.ACTION_VIEW,
						Uri.parse("market://search?q=pub:Thomas+Hunsaker")));
			}
		});

        Button mButtonOpenSource = (Button)rootView.findViewById(R.id.about_open_source);
        mButtonOpenSource.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent aboutIntent = new Intent(getActivity().getApplicationContext(), LibsActivity.class);
                aboutIntent.putExtra(Libs.BUNDLE_FIELDS, Libs.toStringArray(R.string.class.getFields()));
                aboutIntent.putExtra(Libs.BUNDLE_LIBS,
                        new String[]{"gson", "eventbus", "joda", "butterknife",
                                "dagger", "picasso", "retrofit", "androidtimessquare",
                                "betterpickers", "urlimageviewhelper"});
                aboutIntent.putExtra(Libs.BUNDLE_VERSION, true);
                aboutIntent.putExtra(Libs.BUNDLE_LICENSE, true);
                aboutIntent.putExtra(Libs.BUNDLE_TITLE, "Open Source");
                aboutIntent.putExtra(Libs.BUNDLE_THEME, R.style.Theme_Soup);
                aboutIntent.putExtra(Libs.BUNDLE_ACCENT_COLOR, "#00d04e" /*R.color.soup_green*/);
                startActivity(aboutIntent);
            }
        });

		return rootView;
	}
}
