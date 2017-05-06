package org.fossasia.pslab.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.fossasia.pslab.R;
import org.fossasia.pslab.adapters.ApplicationsAdapter;


/**
 * Created by viveksb007 on 29/3/17.
 */

public class ApplicationsFragment extends Fragment {

    private String[] applicationsList = {"Oscilloscope", "Control", "Advance Control", "Logical Analyzer", "Data Sensor Logger", "Wireless Sensor", "Sensor QuickView", "Wireless Sensor QuickView"};
    private int[] imageList = {org.fossasia.pslab.R.drawable.osciloscope, org.fossasia.pslab.R.drawable.control, org.fossasia.pslab.R.drawable.advance, org.fossasia.pslab.R.drawable.la, org.fossasia.pslab.R.drawable.sensor, org.fossasia.pslab.R.drawable.wirelesssensor, org.fossasia.pslab.R.drawable.sensor_quickview, org.fossasia.pslab.R.drawable.wireless_sensor_quickview};

    public static ApplicationsFragment newInstance() {
        ApplicationsFragment applicationsFragment = new ApplicationsFragment();
        return applicationsFragment;
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.applications_fragment, container, false);
        ListView applicationsListView = (ListView) view.findViewById(org.fossasia.pslab.R.id.applicationsListView);
        applicationsListView.setAdapter(new ApplicationsAdapter(getContext(), applicationsList, imageList));
        return view;
    }
}
