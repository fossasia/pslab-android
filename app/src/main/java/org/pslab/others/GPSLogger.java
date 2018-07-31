package org.pslab.others;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import org.pslab.R;
import org.pslab.activity.LuxMeterActivity;

/**
 * Created by Padmal on 6/29/18.
 */

public class GPSLogger {

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private static final int UPDATE_INTERVAL_IN_MILLISECONDS = 1000;
    private static final int MIN_DISTANCE_CHANGE_FOR_UPDATES = 1;
    private LocationManager locationManager;
    private Context context;
    private Location bestLocation;
    private LuxMeterActivity callerActivity;

    public GPSLogger(Context context) {
        this.context = context;
    }

    public GPSLogger(Context context, LocationManager locationManager) {
        this.context = context;
        this.locationManager = locationManager;
        if (context instanceof LuxMeterActivity) {
            callerActivity = (LuxMeterActivity) context;
        }
    }

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            bestLocation = location;
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {/**/}

        @Override
        public void onProviderEnabled(String s) { /**/}

        @Override
        public void onProviderDisabled(String s) {
            callerActivity.saveData = false;
            new AlertDialog.Builder(callerActivity, R.style.AlertDialogStyle)
                    .setTitle(R.string.allow_gps)
                    .setMessage(R.string.allow_gps_info)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            context.startActivity(intent);
                        }
                    })
                    .create()
                    .show();
        }
    };

    public boolean isGPSEnabled() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    /**
     * Requests constant updates of location
     */
    @SuppressLint("MissingPermission")
    private void getUpdate() {
        String provider = LocationManager.GPS_PROVIDER;
        locationManager.requestLocationUpdates(provider, UPDATE_INTERVAL_IN_MILLISECONDS, MIN_DISTANCE_CHANGE_FOR_UPDATES,
                locationListener);
    }

    /**
     * Stop requesting updates
     */
    public void removeUpdate() {
        locationManager.removeUpdates(locationListener);
    }

    @SuppressLint("MissingPermission")
    public Location whatsLocation() {
        return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    }

    /**
     * @return the best location fetched
     */
    public Location getBestLocation() {
        return bestLocation;
    }

    /**
     * Request for allow location permission
     * if the permission is not given initially
     */
    public void requestPermissionIfNotGiven() {
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);
        }
    }

    /**
     * First fetch last known location for faster results,
     * then start listening for location updates
     */
    public void startFetchingLocation() {
        bestLocation = whatsLocation();
        getUpdate();
    }
}
