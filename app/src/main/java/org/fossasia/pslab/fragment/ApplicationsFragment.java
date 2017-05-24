package org.fossasia.pslab.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.fossasia.pslab.activity.AdvancedControlActivity;
import org.fossasia.pslab.activity.ControlActivity;
import org.fossasia.pslab.activity.LogicalAnalyzerActivity;
import org.fossasia.pslab.activity.OscilloscopeActivity;
import org.fossasia.pslab.items.ApplicationItem;
import org.fossasia.pslab.R;
import org.fossasia.pslab.adapters.ApplicationAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;


/**
 * Created by viveksb007 on 29/3/17.
 */

public class ApplicationsFragment extends Fragment {

    private ApplicationAdapter applicationAdapter;
    private List<ApplicationItem> applicationItemList;
    private Context context;

    public static ApplicationsFragment newInstance() {
        return new ApplicationsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.applications_fragment, container, false);
        context = getActivity().getApplicationContext();
        applicationItemList = new ArrayList<>();
        applicationAdapter = new ApplicationAdapter(context, applicationItemList,
                new ApplicationAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(ApplicationItem item) {
                        Toast.makeText(context, item.getApplicationName(),
                                Toast.LENGTH_LONG).show();
                        Intent intent;
                        switch (item.getApplicationName()) {
                            case "Oscilloscope":
                                intent = new Intent(context, OscilloscopeActivity.class);
                                startActivity(intent);
                                break;
                            case "Control":
                                intent = new Intent(context, ControlActivity.class);
                                startActivity(intent);
                                break;
                            case "Advance Control":
                                intent = new Intent(context, AdvancedControlActivity.class);
                                startActivity(intent);
                                break;
                            case "Logical Analyzer":
                                intent = new Intent(context, LogicalAnalyzerActivity.class);
                                startActivity(intent);
                                break;
                        }

                    }
                });
        // Initiate Views
        int rows = context.getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_PORTRAIT ? 2 : 3;
        initiateViews(view, rows);
        // Generate Applications
        new loadList().execute();
        // Return created view
        return view;
    }

    /**
     * Initiate Recycler view
     */
    private void initiateViews(View view, int rows) {
        // Initiate Recycler View with a Grid
        RecyclerView listView = (RecyclerView) view.findViewById(R.id.applications_recycler_view);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(context, rows);
        listView.setLayoutManager(mLayoutManager);
        listView.setItemAnimator(new DefaultItemAnimator());
        // Set adapter
        listView.setAdapter(applicationAdapter);
    }

    /**
     * Generate an array of Application Items and add them to the adapter in background
     */
    private class loadList extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            int[] applications = new int[]{
                    R.drawable.osciloscope_icon,
                    R.drawable.control_icon,
                    R.drawable.advance_icon,
                    R.drawable.logic_analzers_icon,
                    R.drawable.sensor_icon,
                    R.drawable.wireless_sensor_icon,
                    R.drawable.sensor_qv_icon,
                    R.drawable.wireless_sensor_qv_icon};

            applicationItemList.add(new ApplicationItem(
                    getResources().getString(R.string.oscilloscope), applications[0])
            );
            applicationItemList.add(new ApplicationItem(
                    getResources().getString(R.string.control), applications[1])
            );
            applicationItemList.add(new ApplicationItem(
                    getResources().getString(R.string.advance_control), applications[2])
            );
            applicationItemList.add(new ApplicationItem(
                    getResources().getString(R.string.logical_analyzer), applications[3])
            );
            applicationItemList.add(new ApplicationItem(
                    getResources().getString(R.string.data_sensor_logger), applications[4])
            );
            applicationItemList.add(new ApplicationItem(
                    getResources().getString(R.string.w_sensor_logger), applications[5])
            );
            applicationItemList.add(new ApplicationItem(
                    getResources().getString(R.string.sensor_quick_view), applications[6])
            );
            applicationItemList.add(new ApplicationItem(
                    getResources().getString(R.string.w_sensor_quick_view), applications[7])
            );
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            // Notify the adapter that data has been fetched
            applicationAdapter.notifyDataSetChanged();
        }
    }

}
