package com.thunsaker.soup.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.thunsaker.soup.R;
import com.thunsaker.soup.app.BaseSoupActivity;

/*
 * Created by @thunsaker
 */
public class VenueDetailActivityReceiver extends BaseSoupActivity {
	@Override
	protected void onCreate(Bundle arg0) {
		handleIntent(getIntent());

		super.onCreate(arg0);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		handleIntent(intent);
	}

	public void handleIntent(Intent intent) {
		try {
			String rawVenueUrl;
			if(Intent.ACTION_SEND.equals(intent.getAction()) || Intent.ACTION_VIEW.equals(intent.getAction())) {
				String passedString;
                if(Intent.ACTION_SEND.equals(intent.getAction()))
				    passedString = intent.getStringExtra(Intent.EXTRA_TEXT);
                else
                    passedString = intent.getDataString();

                if(passedString.contains("https"))
				    rawVenueUrl = passedString.substring(passedString.indexOf("https"), passedString.length());
                else
                    rawVenueUrl = passedString.substring(passedString.indexOf("http"), passedString.length());

                Intent detailIntent = new Intent(this, VenueDetailActivity.class);

            	if(rawVenueUrl.contains("4sq.com") || rawVenueUrl.contains("foursquare.com"))
            		detailIntent.putExtra(VenueDetailActivity.VENUE_URL_TO_LOAD_EXTRA, rawVenueUrl);

                if(detailIntent.hasExtra(VenueDetailActivity.VENUE_URL_TO_LOAD_EXTRA))
                    startActivity(detailIntent);
                else
                    Toast.makeText(getApplicationContext(), getString(R.string.alert_receiver_not_valid), Toast.LENGTH_SHORT).show();

			} else {
				Toast.makeText(getApplicationContext(), getString(R.string.alert_receiver_not_valid), Toast.LENGTH_SHORT).show();
			}
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(getApplicationContext(), getString(R.string.alert_receiver_not_valid), Toast.LENGTH_SHORT).show();
		} finally {
			finish();
		}
	}
}
