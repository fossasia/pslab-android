package org.fossasia.pslab.others;

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

import org.fossasia.pslab.R;
import org.fossasia.pslab.activity.LuxMeterActivity;

/**
 * Created by Padmal on 6/29/18.
 */

public class GPSLogger {

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private LocationManager locationManager;
    private Context context;
    public static boolean permissionAvailable = false;
    private Location bestLocation;

    public GPSLogger(Context context, LocationManager locationManager) {
        this.context = context;
        this.locationManager = locationManager;
        callerActivity = (LuxMeterActivity) context;
    }

    private LuxMeterActivity callerActivity;
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
            new AlertDialog.Builder(callerActivity)
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

    @SuppressLint("MissingPermission")
    private void getUpdate() {
        if (permissionAvailable) {
            String provider = LocationManager.GPS_PROVIDER;
            locationManager.requestLocationUpdates(provider, 1000, 1,
                    locationListener);
        }
    }

    public void removeUpdate() {
        locationManager.removeUpdates(locationListener);
    }

    @SuppressLint("MissingPermission")
    public Location whatsLocation() {
        if (permissionAvailable) {
            return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
        return null;
    }

    public Location getBestLocation() {
        return bestLocation;
    }

    public boolean checkPermission() {
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);
            return false;
        } else {
            permissionAvailable = true;
            return true;
        }
    }

    public void startFetchingLocation() {
        bestLocation = whatsLocation();
        getUpdate();
    }
}
