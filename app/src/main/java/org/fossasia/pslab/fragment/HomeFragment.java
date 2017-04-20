package org.fossasia.pslab.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.fossasia.pslab.R;

/**
 * Created by viveksb007 on 15/3/17.
 */

public class HomeFragment extends Fragment {

    private boolean deviceFound = false, deviceConnected = false;
    private String version;

    public static HomeFragment newInstance(boolean deviceConnected, boolean deviceFound, String version) {
        HomeFragment homeFragment = new HomeFragment();
        homeFragment.deviceConnected = deviceConnected;
        homeFragment.deviceFound = deviceFound;
        homeFragment.version = version;
        return homeFragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_fragment, container, false);
        TextView tvDeviceStatus = (TextView) view.findViewById(org.fossasia.pslab.R.id.tv_device_status);
        TextView tvVersion = (TextView) view.findViewById(org.fossasia.pslab.R.id.tv_device_version);
        ImageView imgViewDeviceStatus = (ImageView) view.findViewById(org.fossasia.pslab.R.id.img_device_status);
        if (deviceFound & deviceConnected) {
            imgViewDeviceStatus.setImageResource(org.fossasia.pslab.R.drawable.usb_connected);
            tvDeviceStatus.setText("Device Connected Successfully");
            tvVersion.setText(version);
        } else {
            imgViewDeviceStatus.setImageResource(org.fossasia.pslab.R.drawable.usb_disconnected);
            tvDeviceStatus.setText("PSLab Device not found");
        }
        return view;
    }
}
