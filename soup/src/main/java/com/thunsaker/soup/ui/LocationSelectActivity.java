package com.thunsaker.soup.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.thunsaker.soup.R;
import com.thunsaker.soup.util.Util;

/*
 * Created by @thunsaker
 */
public class LocationSelectActivity extends ActionBarActivity {
	private boolean useLogo = true;
	private boolean showHomeUp = true;

	public static final int PICK_LOCATION = 0;
	public static final String PICKED_LOCATION_EXTRA = "PICKED_LOCATION_EXTRA";

	public boolean hasOriginalLocation = false;
	public LatLng originalLatLng;
	public static final String ORIGINAL_LOCATION_EXTRA = "ORIGINAL_LOCATION_EXTRA";
	LatLng selectedLatLng = null;
	boolean showMyLocation = false;

	private GoogleMap mMap;
	public int MAP_PAN_DELAY = 2000; // in miliseconds
	public int MAP_BOUNDS_PADDING = 150;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

		setContentView(R.layout.activity_location_select);

		SetupActionBar();

		if(getIntent().hasExtra(ORIGINAL_LOCATION_EXTRA)) {
			double[] latLngArray = getIntent().getDoubleArrayExtra(ORIGINAL_LOCATION_EXTRA);
			originalLatLng = new LatLng(latLngArray[0], latLngArray[1]);
			if(originalLatLng != null)
				hasOriginalLocation = true;
		}

		SetupMap();
	}

	private void SetupMap() {
		if (mMap == null) {
			mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(
                    R.id.map_fragment_location_picker)).getMap();
	        if (mMap != null) {
	        	LatLng currentLocation = null;
	        	if(MainActivity.currentLocation != null)
    				currentLocation = MainActivity.currentLocation;
	        	else {
	        		// TODO: Make this work better...zoom way out, use some other method of figuring out who they are and where they are...
	        		currentLocation = new LatLng(33.44866, -112.06627);
	        	}
	        	if(hasOriginalLocation) {
	        		RestoreOriginalMarker(true);
	        	} else {
	        		mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
	        	}

	        	mMap.setOnMapClickListener(new OnMapClickListener() {
					@Override
					public void onMapClick(LatLng newPoint) {
						mMap.clear();
						RestoreOriginalMarker(false);
			        	mMap.addMarker(new MarkerOptions()
				    		.position(newPoint)
				    		.icon(BitmapDescriptorFactory.fromResource(
                                    R.drawable.map_marker_orange_outline)));
			        	selectedLatLng = new LatLng(newPoint.latitude, newPoint.longitude);
			        	supportInvalidateOptionsMenu();
			        	mMap.animateCamera(
                                CameraUpdateFactory.newLatLng(newPoint), MAP_PAN_DELAY, null);
					}
				});
	        }
	    }
	}

	protected void RestoreOriginalMarker(boolean panToMarker) {
		if(hasOriginalLocation) {
        	mMap.addMarker(new MarkerOptions()
	    		.position(originalLatLng)
	    		.icon(BitmapDescriptorFactory.fromResource(
                        R.drawable.map_marker_red)));
        	if(panToMarker) {
        		if(selectedLatLng != null)
        			mMap.animateCamera(
                            CameraUpdateFactory.newLatLngBounds(
                                    Util.GetLatLngBounds(originalLatLng, selectedLatLng, null),
                                    MAP_BOUNDS_PADDING), MAP_PAN_DELAY, null);
        		else
        			mMap.moveCamera(CameraUpdateFactory.newLatLng(originalLatLng));
        	}
		}

		if(showMyLocation)
			RestoreMyLocationMarker(true, false);
	}

	protected void RestoreMyLocationMarker(boolean dropMarker, boolean panToMarker) {
		if(mMap != null && MainActivity.currentLocation != null) {
			if(dropMarker) {
				mMap.addMarker(new MarkerOptions()
					.position(MainActivity.currentLocation)
					.icon(BitmapDescriptorFactory.fromResource(
                            R.drawable.map_marker_blue)));
				showMyLocation = true;
			}

			if(panToMarker) {
				if(hasOriginalLocation) {
					mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(
                            Util.GetLatLngBounds(
                            		MainActivity.currentLocation,
                                    originalLatLng,
                                    selectedLatLng != null ? selectedLatLng : null),
                            MAP_BOUNDS_PADDING),
                            MAP_PAN_DELAY, null);
				} else {
					mMap.animateCamera(
                            CameraUpdateFactory.newLatLng(
                            		MainActivity.currentLocation), MAP_PAN_DELAY, null);
				}
			}
		} else {
//			Crouton.makeText(this, R.string.alert_current_location_unknown, Style.INFO).show();
            Toast.makeText(this, R.string.alert_current_location_unknown, Toast.LENGTH_SHORT).show();
		}
	}

	private void SetupActionBar() {
		ActionBar ab = getSupportActionBar();
		ab.setDisplayShowHomeEnabled(showHomeUp);
		ab.setDisplayUseLogoEnabled(useLogo);
		ab.setDisplayHomeAsUpEnabled(showHomeUp);
		ab.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#aa000000")));
        ab.setSplitBackgroundDrawable(new ColorDrawable(Color.parseColor("#aa000000")));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_location_select, menu);

		MenuItem selectLocation = (MenuItem) menu.findItem(R.id.action_select_location);

		if(selectedLatLng != null) {
			selectLocation.setEnabled(true);
		} else {
			selectLocation.setEnabled(false);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			setResult(Activity.RESULT_CANCELED);
			finish();
			return true;
		case R.id.action_cancel:
			setResult(Activity.RESULT_CANCELED);
			finish();
			return true;
		case R.id.action_select_location:
			Intent extra = new Intent();
			extra.putExtra(LocationSelectActivity.PICKED_LOCATION_EXTRA,
				String.format("%s,%s", selectedLatLng.latitude, selectedLatLng.longitude));
			setResult(Activity.RESULT_OK, extra);
			finish();
			return true;
		case R.id.action_my_location:
			if(!showMyLocation)
				RestoreMyLocationMarker(true, true);
			else
				RestoreMyLocationMarker(false, true);
		}

		return false;
	}
}