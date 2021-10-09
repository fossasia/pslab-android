package io.pslab.receivers;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import com.google.android.material.snackbar.Snackbar;
import androidx.fragment.app.Fragment;

import android.util.Log;

import io.pslab.R;
import io.pslab.activity.MainActivity;
import io.pslab.activity.PowerSourceActivity;
import io.pslab.communication.PacketHandler;
import io.pslab.fragment.HomeFragment;
import io.pslab.others.CustomSnackBar;
import io.pslab.others.ScienceLabCommon;

/**
 * Created by viveksb007 on 21/6/17.
 */

public class USBDetachReceiver extends BroadcastReceiver {

    private final String TAG = this.getClass().getSimpleName();
    private Context activityContext;

    public USBDetachReceiver(){}
    public USBDetachReceiver(Context context) {
        this.activityContext = context;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(intent.getAction())) {
                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (device != null) {
                    ScienceLabCommon.scienceLab.close();
                    // Clear saved values in Power Source Instrument
                    context.getSharedPreferences(PowerSourceActivity.POWER_PREFERENCES, Context.MODE_PRIVATE).edit().clear().apply();
                    CustomSnackBar.showSnackBar(((Activity)context).findViewById(android.R.id.content),
                            "USB Device Disconnected",null,null, Snackbar.LENGTH_SHORT);

                    PacketHandler.version = "";

                    if (activityContext != null) {
                        MainActivity mainActivity = (MainActivity) activityContext;
                        Fragment currentFragment = mainActivity.getSupportFragmentManager().findFragmentById(R.id.frame);
                        if (currentFragment instanceof HomeFragment) {
                            mainActivity.getSupportFragmentManager().beginTransaction().replace(R.id.frame, HomeFragment.newInstance(false, false)).commitAllowingStateLoss();
                        }
                        mainActivity.PSLabisConnected = false;
                        mainActivity.invalidateOptionsMenu();
                    }
                } else {
                    Log.v(TAG, "USB Device is null");
                }
            }
        } catch (IllegalStateException ignored){

        }

    }
}
