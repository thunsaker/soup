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

import com.mikepenz.aboutlibraries.LibsBuilder;
import com.thunsaker.soup.R;

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
                new LibsBuilder()
                        .withLibraries("Eventbus", "Dagger", "Retrofit", "Butterknife",
                                "gson", "picasso", "androidtimessquare", "joda", "betterpickers",
                                "urlimageviewhelper")
                        .withAutoDetect(true)
                        .withLicenseShown(true)
                        .withVersionShown(true)
                        .withActivityTitle(getString(R.string.action_about))
                        .withActivityTheme(R.style.Theme_Soup)
                        .start(getActivity());
//                        new String[]{"gson", "eventbus", "joda", "butterknife",
//                                "dagger", "picasso", "retrofit", "androidtimessquare",
//                                "betterpickers", "urlimageviewhelper"});
            }
        });

		return rootView;
	}
}
