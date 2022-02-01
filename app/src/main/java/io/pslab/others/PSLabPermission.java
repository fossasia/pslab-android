package io.pslab.others;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Padmal on 11/3/18.
 */

public class PSLabPermission {

    private String[] allPermissions = new String[] {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    private String[] csvPermissions = new String[] {
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    private String[] logPermissions = new String[] {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private String[] mapPermissions = new String[] {
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    public static final int ALL_PERMISSION = 100;
    public static final int LOG_PERMISSION = 101;
    public static final int MAP_PERMISSION = 102;
    public static final int GPS_PERMISSION = 103;
    public static final int CSV_PERMISSION = 104;

    private static final PSLabPermission pslabPermission = new PSLabPermission();

    public static PSLabPermission getInstance() {
        return pslabPermission;
    }

    private PSLabPermission() {/**/}

    public boolean checkPermissions(Activity activity, int mode) {
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (mode == ALL_PERMISSION) {
            for (String permission : allPermissions) {
                if (ContextCompat.checkSelfPermission(activity, permission)
                        != PackageManager.PERMISSION_GRANTED) {
                    listPermissionsNeeded.add(permission);
                }
            }
        } else if (mode == LOG_PERMISSION) {
            for (String permission : logPermissions) {
                if (ContextCompat.checkSelfPermission(activity, permission)
                        != PackageManager.PERMISSION_GRANTED) {
                    listPermissionsNeeded.add(permission);
                }
            }
        } else if (mode == MAP_PERMISSION) {
            for (String permission : mapPermissions) {
                if (ContextCompat.checkSelfPermission(activity, permission)
                        != PackageManager.PERMISSION_GRANTED) {
                    listPermissionsNeeded.add(permission);
                }
            }
        } else if (mode == GPS_PERMISSION) {
            for (String permission : mapPermissions) {
                if (ContextCompat.checkSelfPermission(activity, permission)
                        != PackageManager.PERMISSION_GRANTED) {
                    listPermissionsNeeded.add(permission);
                }
            }
        } else if (mode == CSV_PERMISSION) {
            for (String permission : csvPermissions) {
                if (ContextCompat.checkSelfPermission(activity, permission)
                        != PackageManager.PERMISSION_GRANTED) {
                    listPermissionsNeeded.add(permission);
                }
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(activity, listPermissionsNeeded.toArray(
                    new String[listPermissionsNeeded.size()]), mode);
            return false;
        }
        return true;
    }
}
