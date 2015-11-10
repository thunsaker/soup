package com.thunsaker.soup.util;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.thunsaker.soup.data.LocationServiceError;
import com.thunsaker.soup.data.events.LocationChangedEvent;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;

public class LocationService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    @Inject
    Context mContext;

    @Inject
    LocationManager mLocationManager;

    @Inject
    EventBus bus;

    private Binder mBinder = new LocalBinder();
    public static GoogleApiClient mGoogleClient;
    private static float lastLatitude = 33.1264583f;
    private static float lastLongitude = -117.3106229f;

    @Override
    public void onCreate() {
    }

    @Override
    public void onConnected(Bundle bundle) {
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleClient);
        if (location != null) {
            bus.post(
                    new LocationChangedEvent(
                            (float) location.getLatitude(), (float) location.getLongitude()));
        }
        stopSelf();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleClient.disconnect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        UseBackupProviders();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private class LocalBinder extends Binder {
        LocationService getService() {
            return LocationService.this;
        }
    }

    public final LocationListener mLocationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location loc) {
            bus.post(new LocationChangedEvent((float) loc.getLatitude(), (float) loc.getLongitude()));
        }

        @Override
        public void onProviderDisabled(String arg0) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // TODO Auto-generated method stub
        }
    };

    public void UseBackupProviders() {
        // Get the current gps position
        LocationProvider provider = null;
        Boolean hasLocationProvider = false;

        mLocationManager =
                (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

        // Use network provider first
        LocationProvider networkProvider =
                mLocationManager.getProvider(
                        LocationManager.NETWORK_PROVIDER);

        // TODO: Address MM Permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    public void requestPermissions(@NonNull String[] permissions, int requestCode)
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.
                return;
            }
        }
        Location lastKnown =
                mLocationManager.getLastKnownLocation(
                        LocationManager.PASSIVE_PROVIDER);
        double lat = lastKnown.getLatitude();
        double lon = lastKnown.getLongitude();
//        MainActivity.currentLocation = new LatLng(lat, lon);
        bus.post(new LocationChangedEvent((float)lat, (float)lon));

        final boolean networkEnabled =
                mLocationManager.isProviderEnabled(networkProvider.getName());
        LocationProvider gpsProvider =
                mLocationManager.getProvider(
                        LocationManager.NETWORK_PROVIDER);
        final boolean gpsEnabled =
                mLocationManager.isProviderEnabled(gpsProvider.getName());
        if (gpsEnabled) {
            provider = gpsProvider;
            hasLocationProvider = true;
        } else if(networkEnabled) {
            provider = networkProvider;
            hasLocationProvider = true;
        } else {
            hasLocationProvider = false;
        }

        if(hasLocationProvider) {
            // Update every 5 minutes or ~1 mile
            mLocationManager.requestLocationUpdates(
                    provider.getName(), 300000, 1709, mLocationListener);
            // TODO: When is the location unknown?
//            else {
//                Toast.makeText(getActivity(), R.string.alert_current_location_unknown,
//                        Toast.LENGTH_SHORT).show();
//            }
        } else {
            bus.post(new LocationServiceError(LocationServiceError.LOCATION_GENERAL_ERROR, "Problem getting a location provider."));
            stopSelf();
        }
    }
}