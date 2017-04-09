package com.viveksb007.pslab.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.viveksb007.pslab.R;


/**
 * Created by viveksb007 on 29/3/17.
 */

public class ApplicationsFragment extends Fragment {
    String [] applicationsList = {"Oscilloscope","Control","Advance Control","Logical Analyzer","Data Sensor Logger","Wireless Sensor","Sensor QuickView","Wireless Sensor QuickView"};
    int [] imageList = {R.drawable.osciloscope,R.drawable.control,R.drawable.advance,R.drawable.la,R.drawable.sensor,R.drawable.wirelesssensor,R.drawable.sensor_quickview,R.drawable.wireless_sensor_quickview};
    public static ApplicationsFragment newInstance() {
        ApplicationsFragment applicationsFragment = new ApplicationsFragment();
        return applicationsFragment;
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.applications_fragment, container, false);
        ListView applicationsListView = (ListView) view.findViewById(R.id.applicationsListView);
        applicationsListView.setAdapter(new CustomAdapter(getContext(),applicationsList,imageList));
        return view;

    }
    
}
