package org.fossasia.pslab.others;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;

/**
 * Created by Padmal on 6/29/18.
 */

public class GPSLogger {

    private LocationManager locationManager;
    private Context context;
    private boolean locationAvailable = false;

    public GPSLogger(Context context, LocationManager locationManager) {
        this.context = context;
        this.locationManager = locationManager;
        getUpdate();
    }

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            locationAvailable = true;
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {/**/}

        @Override
        public void onProviderEnabled(String s) {/**/}

        @Override
        public void onProviderDisabled(String s) {
            // TODO: Handle GPS turned on/off situations
        }
    };

    public boolean isGPSEnabled() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private void getUpdate() {
        String provider = LocationManager.GPS_PROVIDER;
        if (ActivityCompat.checkSelfPermission(context.getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context.getApplicationContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Handle location permissions
            return;
        }
        locationManager.requestLocationUpdates(provider, 1000, 1,
                locationListener);
    }

    public void removeUpdate() {
        locationManager.removeUpdates(locationListener);
    }

    public void whatsLocation() {
        if (ActivityCompat.checkSelfPermission(context.getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context.getApplicationContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Handle location permissions
            return;
        }
        if (locationAvailable) {
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
    }
}
