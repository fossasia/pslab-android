package io.pslab.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Supplier;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.pslab.R;
import io.pslab.activity.AccelerometerActivity;
import io.pslab.activity.BarometerActivity;
import io.pslab.activity.CompassActivity;
import io.pslab.activity.DustSensorActivity;
import io.pslab.activity.GasSensorActivity;
import io.pslab.activity.GyroscopeActivity;
import io.pslab.activity.LogicalAnalyzerActivity;
import io.pslab.activity.LuxMeterActivity;
import io.pslab.activity.MultimeterActivity;
import io.pslab.activity.OscilloscopeActivity;
import io.pslab.activity.PowerSourceActivity;
import io.pslab.activity.RoboticArmActivity;
import io.pslab.activity.SensorActivity;
import io.pslab.activity.SoundMeterActivity;
import io.pslab.activity.ThermometerActivity;
import io.pslab.activity.WaveGeneratorActivity;
import io.pslab.adapters.ApplicationAdapter;
import io.pslab.items.ApplicationItem;


/**
 * Created by viveksb007 on 29/3/17.
 */

public final class InstrumentsFragment extends Fragment {

    private Map<String, Supplier<Intent>> intentSupplierMap;

    private ApplicationAdapter applicationAdapter;
    private List<ApplicationItem> applicationItemList;
    private Context context;

    public static InstrumentsFragment newInstance() {
        return new InstrumentsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        intentSupplierMap = generateIntentMap();

        final View view = inflater.inflate(R.layout.applications_fragment, container, false);
        context = getContext();
        applicationItemList = new ArrayList<>();
        applicationAdapter = new ApplicationAdapter(applicationItemList,
                item -> {
                    final String applicationName = item.getApplicationName();
                    final Intent intent = createIntent(applicationName);
                    if (intent != null) {
                        startActivity(intent);
                    }
                });
        final int rows = context.getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_PORTRAIT ? 1 : 2;

        initiateViews(view, rows);

        return view;
    }

    private Intent createIntent(@NonNull final String applicationName) {
        final Supplier<Intent> callable = intentSupplierMap.get(applicationName);
        return callable == null ? null : callable.get();
    }

    /**
     * Initiate Recycler view
     */
    private void initiateViews(@NonNull final View view, final int rows) {
        final RecyclerView listView = view.findViewById(R.id.applications_recycler_view);
        final RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(context, rows);
        listView.setLayoutManager(mLayoutManager);
        new LoadList().doTask(listView);

    }

    /**
     * Generate an array of Application Items and add them to the adapter
     */
    private class LoadList {

        private void doTask(@NonNull final RecyclerView listView) {

            final int[] descriptions = {
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
                    R.string.robotic_arm_description,
                    R.string.gas_sensor_description,
                    R.string.dust_sensor_description,
                    R.string.sound_meter_desc
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
                    getResources().getString(R.string.robotic_arm), R.drawable.robotic_arm, getResources().getString(descriptions[12])
            ));
            applicationItemList.add(new ApplicationItem(
                    getResources().getString(R.string.gas_sensor), R.drawable.tile_icon_gas, getResources().getString(descriptions[13])
            ));
            applicationItemList.add(new ApplicationItem(
                    getResources().getString(R.string.dust_sensor), R.drawable.tile_icon_gas, getResources().getString(descriptions[14])
            ));
            applicationItemList.add(new ApplicationItem(
                    getString(R.string.sound_meter), R.drawable.tile_icon_gas, getString(descriptions[15])
            ));
            listView.setItemAnimator(new DefaultItemAnimator());
            listView.setAdapter(applicationAdapter);

        }
    }

    @Override
    public void onConfigurationChanged(@NonNull final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        final FragmentActivity activity = getActivity();
        if (activity != null) {
            final FragmentManager fragmentManager = activity.getSupportFragmentManager();
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                fragmentManager.beginTransaction().detach(this).attach(this).commitAllowingStateLoss();
            } else {
                fragmentManager.beginTransaction().detach(this).commitNowAllowingStateLoss();
                fragmentManager.beginTransaction().attach(this).commitNowAllowingStateLoss();
            }
        }
    }

    private Map<String, Supplier<Intent>> generateIntentMap() {
        final Map<String, Supplier<Intent>> map = new HashMap<>();

        map.put(getString(R.string.oscilloscope), () -> {
            final Intent intent = new Intent(context, OscilloscopeActivity.class);
            intent.putExtra("who", "Instruments");
            return intent;
        });
        map.put(getString(R.string.multimeter), () ->
                new Intent(context, MultimeterActivity.class));
        map.put(getString(R.string.logical_analyzer), () ->
                new Intent(context, LogicalAnalyzerActivity.class));
        map.put(getString(R.string.sensors), () ->
                new Intent(context, SensorActivity.class));
        map.put(getString(R.string.wave_generator), () ->
                new Intent(context, WaveGeneratorActivity.class));
        map.put(getString(R.string.power_source), () ->
                new Intent(context, PowerSourceActivity.class));
        map.put(getString(R.string.lux_meter), () ->
                new Intent(context, LuxMeterActivity.class));
        map.put(getString(R.string.accelerometer), () ->
                new Intent(context, AccelerometerActivity.class));
        map.put(getString(R.string.baro_meter), () ->
                new Intent(context, BarometerActivity.class));
        map.put(getString(R.string.compass), () ->
                new Intent(context, CompassActivity.class));
        map.put(getString(R.string.gyroscope), () ->
                new Intent(context, GyroscopeActivity.class));
        map.put(getString(R.string.thermometer), () ->
                new Intent(context, ThermometerActivity.class));
        map.put(getString(R.string.robotic_arm), () ->
                new Intent(context, RoboticArmActivity.class));
        map.put(getString(R.string.gas_sensor), () ->
                new Intent(context, GasSensorActivity.class));
        map.put(getString(R.string.dust_sensor), () ->
                new Intent(context, DustSensorActivity.class));
        map.put(getString(R.string.sound_meter), () ->
                new Intent(context, SoundMeterActivity.class));

        return map;
    }
}
