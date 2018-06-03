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

import org.fossasia.pslab.PSLabApplication;
import org.fossasia.pslab.activity.ControlActivity;
import org.fossasia.pslab.activity.LogicalAnalyzerActivity;
import org.fossasia.pslab.activity.MultimeterActivity;
import org.fossasia.pslab.activity.OscilloscopeActivity;
import org.fossasia.pslab.activity.PowerSourceActivity;
import org.fossasia.pslab.activity.SensorActivity;
import org.fossasia.pslab.items.ApplicationItem;
import org.fossasia.pslab.R;
import org.fossasia.pslab.adapters.ApplicationAdapter;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by viveksb007 on 29/3/17.
 */

public class InstrumentsFragment extends Fragment {

    private ApplicationAdapter applicationAdapter;
    private List<ApplicationItem> applicationItemList;
    private Context context;

    public static InstrumentsFragment newInstance() {
        return new InstrumentsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.applications_fragment, container, false);
        context = getActivity().getApplicationContext();
        applicationItemList = new ArrayList<>();
        applicationAdapter = new ApplicationAdapter(context, applicationItemList,
                new ApplicationAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(ApplicationItem item) {
                        Intent intent;
                        switch (item.getApplicationName()) {
                            case "Oscilloscope":
                                intent = new Intent(context, OscilloscopeActivity.class);
                                intent.putExtra("who", "Instruments");
                                startActivity(intent);
                                break;
                            case "Multimeter":
                                intent = new Intent(context, MultimeterActivity.class);
                                startActivity(intent);
                                break;
                            case "Logical Analyzer":
                                intent = new Intent(context, LogicalAnalyzerActivity.class);
                                startActivity(intent);
                                break;
                            case "Sensors":
                                intent = new Intent(context, SensorActivity.class);
                                startActivity(intent);
                                break;
                            case "Wave Generator":
                                Toast.makeText(getContext(), getResources().getString(R.string.coming_soon), Toast.LENGTH_SHORT).show();
                                break;
                            case "Power Source":
                                intent = new Intent(context, PowerSourceActivity.class);
                                startActivity(intent);
                                break;
                        }

                    }
                });
        int rows = context.getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_PORTRAIT ? 1 : 2;
        initiateViews(view, rows);
        new loadList().execute();
        return view;
    }

    /**
     * Initiate Recycler view
     */
    private void initiateViews(View view, int rows) {
        RecyclerView listView = view.findViewById(R.id.applications_recycler_view);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(context, rows);
        listView.setLayoutManager(mLayoutManager);
        listView.setItemAnimator(new DefaultItemAnimator());
        listView.setAdapter(applicationAdapter);
    }

    /**
     * Generate an array of Application Items and add them to the adapter in background
     */
    private class loadList extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            int[] applications = new int[]{
                    R.drawable.oscilloscope_card,
                    R.drawable.multimeter_card,
                    R.drawable.logic_analyzer_card,
                    R.drawable.sensors_card,
                    R.drawable.wave_generator_card,
                    R.drawable.power_source_card};

            int[] descriptions = new int[]{
                    R.string.oscilloscope_description,
                    R.string.multimeter_description,
                    R.string.logic_analyzer_description,
                    R.string.sensors_description,
                    R.string.wave_generator_description,
                    R.string.power_source_description
            };

            applicationItemList.add(new ApplicationItem(
                    getResources().getString(R.string.oscilloscope), applications[0], getResources().getString(descriptions[0]))
            );
            applicationItemList.add(new ApplicationItem(
                    getResources().getString(R.string.multimeter), applications[1], getResources().getString(descriptions[1]))
            );
            applicationItemList.add(new ApplicationItem(
                    getResources().getString(R.string.logical_analyzer), applications[2], getResources().getString(descriptions[2]))
            );
            applicationItemList.add(new ApplicationItem(
                    getResources().getString(R.string.sensors), applications[3], getResources().getString(descriptions[3]))
            );
            applicationItemList.add(new ApplicationItem(
                    getResources().getString(R.string.wavegen), applications[4], getResources().getString(descriptions[4]))
            );
            applicationItemList.add(new ApplicationItem(
                    getResources().getString(R.string.power_source), applications[5], getResources().getString(descriptions[5]))
            );
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            applicationAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        getActivity().getSupportFragmentManager().beginTransaction().detach(this).attach(this).commitAllowingStateLoss();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ((PSLabApplication) getActivity().getApplication()).refWatcher.watch(this, InstrumentsFragment.class.getSimpleName());
    }
}
