package org.fossasia.pslab.receivers;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

import org.fossasia.pslab.R;
import org.fossasia.pslab.activity.MainActivity;
import org.fossasia.pslab.activity.OscilloscopeActivity;
import org.fossasia.pslab.fragment.HomeFragment;
import org.fossasia.pslab.others.PreferenceManager;
import org.fossasia.pslab.others.ScienceLabCommon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by viveksb007 on 21/6/17.
 */

public class USBDetachReceiver extends BroadcastReceiver {

    private final String TAG = this.getClass().getSimpleName();
    private Context activityContext;

    public USBDetachReceiver(Context context) {
        this.activityContext = context;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(intent.getAction())) {
                UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (device != null) {
                    ScienceLabCommon.scienceLab.close();
                    Toast.makeText(context, "USB Device Disconnected", Toast.LENGTH_SHORT).show();

                    new PreferenceManager(context).setVersion("none"); // writing version as "none" so that its read againg when PSLab is connected

                    ArrayList<String> runningactivities = new ArrayList<String>();
                    ActivityManager activityManager = (ActivityManager)context.getSystemService (Context.ACTIVITY_SERVICE);
                    List<ActivityManager.RunningTaskInfo> services = activityManager.getRunningTasks(Integer.MAX_VALUE);

                    for (int i = 0; i < services.size(); i++) {
                        runningactivities.add(0,services.get(i).topActivity.toString());
                        Log.v(TAG, runningactivities.get(i));
                    }
                    if(runningactivities.contains("ComponentInfo{org.fossasia.pslab/org.fossasia.pslab.activity.OscilloscopeActivity}")) {
                        Context oscilloscopeContext = OscilloscopeActivity.getContext();
                        ((OscilloscopeActivity) oscilloscopeContext).finish();
                    }
                    MainActivity mainActivity = (MainActivity) activityContext;
                    mainActivity.getSupportFragmentManager().beginTransaction().replace(R.id.frame, HomeFragment.newInstance(false, false)).commitAllowingStateLoss();
                    }
                } else {
                    Log.v(TAG, "USB Device is null");
                }

        } catch (IllegalStateException ignored) {

        }
    }
}
