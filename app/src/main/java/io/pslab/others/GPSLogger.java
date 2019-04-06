package io.pslab.others;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.widget.Toast;

import io.pslab.R;
import io.pslab.models.PSLabSensor;

/**
 * Created by Padmal on 6/29/18.
 */

public class GPSLogger {

    public static final int PSLAB_PERMISSION_FOR_MAPS = 102;
    private static final int UPDATE_INTERVAL_IN_MILLISECONDS = 400;
    private static final int MIN_DISTANCE_CHANGE_FOR_UPDATES = 1;
    private LocationManager locationManager;
    private Context context;
    private Location bestLocation;
    private PSLabSensor sensorActivity;
    private PSLabPermission psLabPermission;
    private String provider = LocationManager.GPS_PROVIDER;
    public AlertDialog gpsAlert;

    public GPSLogger(Context context, LocationManager locationManager) {
        this.context = context;
        this.locationManager = locationManager;
        psLabPermission = PSLabPermission.getInstance();
        if (context instanceof PSLabSensor) {
            sensorActivity = (PSLabSensor) context;
            buildUpGPSAlert();
        }
    }

    private void buildUpGPSAlert() {
        gpsAlert = new AlertDialog.Builder(sensorActivity, R.style.AlertDialogStyle)
                .setTitle(R.string.allow_gps)
                .setMessage(R.string.allow_gps_info)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        context.startActivity(intent);
                        sensorActivity.checkGPSOnResume = true;
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (sensorActivity.isRecording) {
                            CustomSnackBar.showSnackBar(sensorActivity.sensorParentView,
                                    context.getResources().getString(R.string.data_recording_with_gps_off),
                                    null, null, Snackbar.LENGTH_LONG);
                        } else {
                            sensorActivity.isRecording = true;
                            sensorActivity.invalidateOptionsMenu();
                            CustomSnackBar.showSnackBar(sensorActivity.sensorParentView,
                                    context.getResources().getString(R.string.data_recording_with_nogps),
                                    null, null, Snackbar.LENGTH_LONG);
                        }
                    }
                })
                .create();
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
            if (sensorActivity.isRecording && !gpsAlert.isShowing()) {
                gpsAlert.show();
            }
        }
    };

    public boolean isGPSEnabled() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    /**
     * Stop requesting updates
     */
    public void removeUpdate() {
        locationManager.removeUpdates(locationListener);
    }

    /**
     * @return the best location fetched
     */
    @SuppressLint("MissingPermission")
    public Location getDeviceLocation() {
        if (bestLocation == null) {
            if (psLabPermission.checkPermissions((Activity) context, PSLabPermission.MAP_PERMISSION)) {
                locationManager.requestLocationUpdates(provider,
                        UPDATE_INTERVAL_IN_MILLISECONDS, MIN_DISTANCE_CHANGE_FOR_UPDATES,
                        locationListener);
                return locationManager.getLastKnownLocation(provider);
            } else {
                return dummyLocation();
            }
        } else {
            return bestLocation;
        }
    }

    private Location dummyLocation() {
        Location l = new Location("");
        l.setLatitude(0.0);
        l.setLongitude(0.0);
        return l;
    }

    /**
     * Set location updates
     */
    @SuppressLint("MissingPermission")
    public void startCaptureLocation() {
        if (psLabPermission.checkPermissions((Activity) context, PSLabPermission.MAP_PERMISSION)) {
            locationManager.requestLocationUpdates(provider, UPDATE_INTERVAL_IN_MILLISECONDS, MIN_DISTANCE_CHANGE_FOR_UPDATES,
                    locationListener);
        } else {
            Toast.makeText(context.getApplicationContext(),
                    context.getResources().getString(R.string.no_permission_for_maps),
                    Toast.LENGTH_LONG).show();
        }
    }
}
