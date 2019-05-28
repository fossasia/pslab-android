package io.pslab.fragment;

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

import io.pslab.R;
import io.pslab.activity.AccelerometerActivity;
import io.pslab.activity.BarometerActivity;
import io.pslab.activity.CompassActivity;
import io.pslab.activity.GyroscopeActivity;
import io.pslab.activity.LogicalAnalyzerActivity;
import io.pslab.activity.LuxMeterActivity;
import io.pslab.activity.MultimeterActivity;
import io.pslab.activity.OscilloscopeActivity;
import io.pslab.activity.PowerSourceActivity;
import io.pslab.activity.RoboticArmActivity;
import io.pslab.activity.SensorActivity;
import io.pslab.activity.ThermometerActivity;
import io.pslab.activity.WaveGeneratorActivity;
import io.pslab.adapters.ApplicationAdapter;
import io.pslab.items.ApplicationItem;
import io.pslab.models.PSLabSensor;

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
                            case "Logic Analyzer":
                                intent = new Intent(context, LogicalAnalyzerActivity.class);
                                startActivity(intent);
                                break;
                            case "Sensors":
                                intent = new Intent(context, SensorActivity.class);
                                startActivity(intent);
                                break;
                            case "Wave Generator":
                                intent = new Intent(context, WaveGeneratorActivity.class);
                                startActivity(intent);
                                break;
                            case "Power Source":
                                intent = new Intent(context, PowerSourceActivity.class);
                                startActivity(intent);
                                break;
                            case PSLabSensor.LUXMETER:
                                intent = new Intent(context, LuxMeterActivity.class);
                                startActivity(intent);
                                break;
                            case "Accelerometer":
                                intent = new Intent(context, AccelerometerActivity.class);
                                startActivity(intent);
                                break;
                            case PSLabSensor.BAROMETER:
                                intent = new Intent(context, BarometerActivity.class);
                                startActivity(intent);
                                break;
                            case "Compass":
                                intent = new Intent(context, CompassActivity.class);
                                startActivity(intent);
                                break;
                            case "Gyroscope":
                                intent = new Intent(context, GyroscopeActivity.class);
                                startActivity(intent);
                                break;
                            case "Thermometer":
                                intent = new Intent(context, ThermometerActivity.class);
                                startActivity(intent);
                                break;
                            case "Robotic Arm":
                                intent = new Intent(context, RoboticArmActivity.class);
                                startActivity(intent);
                                break;
                            default:
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

            int[] descriptions = new int[]{
                    R.string.oscilloscope_description,
                    R.string.multimeter_description,
                    R.string.logic_analyzer_description,
                    R.string.sensors_description,
                    R.string.wave_generator_description,
                    R.string.power_source_description,
                    R.string.lux_meter_description,
                    R.string.accelerometer_description,
                    R.string.baro_meter_description,
                    R.string.compass_description,
                    R.string.gyroscope_description,
                    R.string.thermometer_desc,
                    R.string.robotic_arm_descriptoin

            };

            applicationItemList.add(new ApplicationItem(
                    getResources().getString(R.string.oscilloscope), R.drawable.tile_icon_oscilloscope, getResources().getString(descriptions[0]))
            );
            applicationItemList.add(new ApplicationItem(
                    getResources().getString(R.string.multimeter), R.drawable.tile_icon_multimeter, getResources().getString(descriptions[1]))
            );
            applicationItemList.add(new ApplicationItem(
                    getResources().getString(R.string.logical_analyzer), R.drawable.tile_icon_logic_analyzer, getResources().getString(descriptions[2]))
            );
            applicationItemList.add(new ApplicationItem(
                    getResources().getString(R.string.sensors), R.drawable.tile_icon_sensors, getResources().getString(descriptions[3]))
            );
            applicationItemList.add(new ApplicationItem(
                    getResources().getString(R.string.wave_generator), R.drawable.tile_icon_wave_generator, getResources().getString(descriptions[4]))
            );
            applicationItemList.add(new ApplicationItem(
                    getResources().getString(R.string.power_source), R.drawable.tile_icon_power_source, getResources().getString(descriptions[5]))
            );
            applicationItemList.add(new ApplicationItem(
                    getResources().getString(R.string.lux_meter), R.drawable.tile_icon_lux_meter, getResources().getString(descriptions[6]))
            );
            applicationItemList.add(new ApplicationItem(
                    getResources().getString(R.string.accelerometer), R.drawable.tile_icon_accelerometer, getResources().getString(descriptions[7]))
            );
            applicationItemList.add(new ApplicationItem(
                    getResources().getString(R.string.baro_meter), R.drawable.tile_icon_barometer, getResources().getString(descriptions[8])
            ));
            applicationItemList.add(new ApplicationItem(
                    getResources().getString(R.string.compass), R.drawable.tile_icon_compass, getResources().getString(descriptions[9])
            ));
            applicationItemList.add(new ApplicationItem(
                    getResources().getString(R.string.gyroscope), R.drawable.gyroscope_logo, getResources().getString(descriptions[10])
            ));
            applicationItemList.add(new ApplicationItem(
                    getResources().getString(R.string.thermometer), R.drawable.thermometer_logo, getResources().getString(descriptions[11])
                    ));
            applicationItemList.add(new ApplicationItem(
                    getResources().getString(R.string.robotic_arm), R.drawable.gyroscope_logo, getResources().getString(descriptions[11])
            ));
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
}
