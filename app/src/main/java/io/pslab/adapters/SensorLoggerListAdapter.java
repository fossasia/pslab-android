package io.pslab.adapters;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import androidx.annotation.NonNull;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Date;

import io.pslab.R;
import io.pslab.activity.AccelerometerActivity;
import io.pslab.activity.BarometerActivity;
import io.pslab.activity.CompassActivity;
import io.pslab.activity.GasSensorActivity;
import io.pslab.activity.GyroscopeActivity;
import io.pslab.activity.LogicalAnalyzerActivity;
import io.pslab.activity.LuxMeterActivity;
import io.pslab.activity.MapsActivity;
import io.pslab.activity.MultimeterActivity;
import io.pslab.activity.OscilloscopeActivity;
import io.pslab.activity.PowerSourceActivity;
import io.pslab.activity.RoboticArmActivity;
import io.pslab.activity.SoundMeterActivity;
import io.pslab.activity.ThermometerActivity;
import io.pslab.activity.WaveGeneratorActivity;
import io.pslab.models.AccelerometerData;
import io.pslab.models.BaroData;
import io.pslab.models.CompassData;
import io.pslab.models.GasSensorData;
import io.pslab.models.GyroData;
import io.pslab.models.LogicAnalyzerData;
import io.pslab.models.LuxData;
import io.pslab.models.MultimeterData;
import io.pslab.models.OscilloscopeData;
import io.pslab.models.PSLabSensor;
import io.pslab.models.PowerSourceData;
import io.pslab.models.SensorDataBlock;
import io.pslab.models.ServoData;
import io.pslab.models.SoundData;
import io.pslab.models.ThermometerData;
import io.pslab.models.WaveGeneratorData;
import io.pslab.others.CSVLogger;
import io.pslab.others.CustomSnackBar;
import io.pslab.others.LocalDataLog;
import io.realm.RealmRecyclerViewAdapter;
import io.realm.RealmResults;

/**
 * Created by Avjeet on 03-08-2018.
 */
public class SensorLoggerListAdapter extends RealmRecyclerViewAdapter<SensorDataBlock, SensorLoggerListAdapter.ViewHolder> {


    private final String KEY_LOG = "has_log";
    private final String DATA_BLOCK = "data_block";
    private Activity context;

    public SensorLoggerListAdapter(RealmResults<SensorDataBlock> results, Activity context) {
        super(results, true, true);
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.logger_data_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final SensorDataBlock block = getItem(position);
        assert block != null;
        switch (block.getSensorType()) {
            case PSLabSensor.LUXMETER:
                holder.sensor.setText(context.getResources().getString(R.string.lux_meter));
                holder.tileIcon.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), R.drawable.tile_icon_lux_meter_log, null));
                break;
            case PSLabSensor.BAROMETER:
                holder.sensor.setText(context.getResources().getString(R.string.baro_meter));
                holder.tileIcon.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), R.drawable.tile_icon_barometer_log, null));
                break;
            case PSLabSensor.GYROSCOPE:
                holder.sensor.setText(context.getResources().getString(R.string.gyroscope));
                holder.tileIcon.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), R.drawable.gyroscope_logo, null));
                break;
            case PSLabSensor.COMPASS:
                holder.sensor.setText(context.getResources().getString(R.string.compass));
                holder.tileIcon.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), R.drawable.tile_icon_compass_log, null));
                break;
            case PSLabSensor.ACCELEROMETER:
                holder.sensor.setText(context.getResources().getString(R.string.accelerometer));
                holder.tileIcon.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), R.drawable.tile_icon_accelerometer, null));
                break;
            case PSLabSensor.THERMOMETER:
                holder.sensor.setText(R.string.thermometer);
                holder.tileIcon.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), R.drawable.thermometer_logo, null));
                break;
            case PSLabSensor.ROBOTIC_ARM:
                holder.sensor.setText(R.string.robotic_arm);
                holder.tileIcon.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), R.drawable.robotic_arm, null));
                break;
            case PSLabSensor.WAVE_GENERATOR:
                holder.sensor.setText(R.string.wave_generator);
                holder.tileIcon.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), R.drawable.tile_icon_wave_generator, null));
                break;
            case PSLabSensor.OSCILLOSCOPE:
                holder.sensor.setText(R.string.oscilloscope);
                holder.tileIcon.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), R.drawable.tile_icon_oscilloscope, null));
                break;
            case PSLabSensor.POWER_SOURCE:
                holder.sensor.setText(R.string.power_source);
                holder.tileIcon.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), R.drawable.tile_icon_power_source, null));
                break;
            case PSLabSensor.MULTIMETER:
                holder.sensor.setText(R.string.multimeter);
                holder.tileIcon.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), R.drawable.tile_icon_multimeter, null));
                break;
            case PSLabSensor.LOGIC_ANALYZER:
                holder.sensor.setText(R.string.logical_analyzer);
                holder.tileIcon.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), R.drawable.tile_icon_logic_analyzer, null));
                break;
            case PSLabSensor.GAS_SENSOR:
                holder.sensor.setText(R.string.gas_sensor);
                holder.tileIcon.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), R.drawable.tile_icon_gas, null));
                break;
            case PSLabSensor.SOUND_METER:
                holder.sensor.setText(R.string.sound_meter);
                holder.tileIcon.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), R.drawable.tile_icon_gas, null));
                break;
            default:
                break;
        }
        holder.dateTime.setText(String.valueOf(CSVLogger.FILE_NAME_FORMAT.format(new Date(block.getBlock()))));
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleCardViewClick(block);
            }
        });
        holder.deleteIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleDeleteItem(block);
            }
        });
        holder.mapIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                populateMapData(block);
            }
        });
    }

    private void handleCardViewClick(SensorDataBlock block) {
        if (block.getSensorType().equalsIgnoreCase(context.getResources().getString(R.string.lux_meter))) {
            Intent LuxMeter = new Intent(context, LuxMeterActivity.class);
            LuxMeter.putExtra(KEY_LOG, true);
            LuxMeter.putExtra(DATA_BLOCK, block.getBlock());
            context.startActivity(LuxMeter);
        } else if (block.getSensorType().equalsIgnoreCase(context.getResources().getString(R.string.baro_meter))) {
            Intent BaroMeter = new Intent(context, BarometerActivity.class);
            BaroMeter.putExtra(KEY_LOG, true);
            BaroMeter.putExtra(DATA_BLOCK, block.getBlock());
            context.startActivity(BaroMeter);
        } else if (block.getSensorType().equalsIgnoreCase(context.getResources().getString(R.string.gyroscope))) {
            Intent Gyroscope = new Intent(context, GyroscopeActivity.class);
            Gyroscope.putExtra(KEY_LOG, true);
            Gyroscope.putExtra(DATA_BLOCK, block.getBlock());
            context.startActivity(Gyroscope);
        } else if (block.getSensorType().equalsIgnoreCase(context.getResources().getString(R.string.compass))) {
            Intent Compass = new Intent(context, CompassActivity.class);
            Compass.putExtra(KEY_LOG, true);
            Compass.putExtra(DATA_BLOCK, block.getBlock());
            context.startActivity(Compass);
        } else if (block.getSensorType().equalsIgnoreCase(context.getResources().getString(R.string.accelerometer))) {
            Intent Accelerometer = new Intent(context, AccelerometerActivity.class);
            Accelerometer.putExtra(KEY_LOG, true);
            Accelerometer.putExtra(DATA_BLOCK, block.getBlock());
            context.startActivity(Accelerometer);
        } else if (block.getSensorType().equalsIgnoreCase(context.getResources().getString(R.string.thermometer))) {
            Intent Thermometer = new Intent(context, ThermometerActivity.class);
            Thermometer.putExtra(KEY_LOG, true);
            Thermometer.putExtra(DATA_BLOCK, block.getBlock());
            context.startActivity(Thermometer);
        } else if (block.getSensorType().equalsIgnoreCase(context.getResources().getString(R.string.robotic_arm))) {
            Intent RoboticArm = new Intent(context, RoboticArmActivity.class);
            RoboticArm.putExtra(KEY_LOG, true);
            RoboticArm.putExtra(DATA_BLOCK, block.getBlock());
            context.startActivity(RoboticArm);
        } else if (block.getSensorType().equalsIgnoreCase(context.getResources().getString(R.string.wave_generator))) {
            Intent waveGenerator = new Intent(context, WaveGeneratorActivity.class);
            waveGenerator.putExtra(KEY_LOG, true);
            waveGenerator.putExtra(DATA_BLOCK, block.getBlock());
            context.startActivity(waveGenerator);
        } else if (block.getSensorType().equalsIgnoreCase(context.getResources().getString(R.string.oscilloscope))) {
            Intent oscilloscope = new Intent(context, OscilloscopeActivity.class);
            oscilloscope.putExtra(KEY_LOG, true);
            oscilloscope.putExtra(DATA_BLOCK, block.getBlock());
            context.startActivity(oscilloscope);
        } else if (block.getSensorType().equalsIgnoreCase(context.getResources().getString(R.string.power_source))) {
            Intent powerSource = new Intent(context, PowerSourceActivity.class);
            powerSource.putExtra(KEY_LOG, true);
            powerSource.putExtra(DATA_BLOCK, block.getBlock());
            context.startActivity(powerSource);
        } else if (block.getSensorType().equalsIgnoreCase(context.getResources().getString(R.string.multimeter))) {
            Intent multimeter = new Intent(context, MultimeterActivity.class);
            multimeter.putExtra(KEY_LOG, true);
            multimeter.putExtra(DATA_BLOCK, block.getBlock());
            context.startActivity(multimeter);
        } else if (block.getSensorType().equalsIgnoreCase(context.getResources().getString(R.string.logical_analyzer))) {
            Intent laIntent = new Intent(context, LogicalAnalyzerActivity.class);
            laIntent.putExtra(KEY_LOG, true);
            laIntent.putExtra(DATA_BLOCK, block.getBlock());
            context.startActivity(laIntent);
        } else if (block.getSensorType().equalsIgnoreCase(context.getResources().getString(R.string.gas_sensor))) {
            Intent gasSensorIntent = new Intent(context, GasSensorActivity.class);
            gasSensorIntent.putExtra(KEY_LOG, true);
            gasSensorIntent.putExtra(DATA_BLOCK, block.getBlock());
            context.startActivity(gasSensorIntent);
        } else if (block.getSensorType().equalsIgnoreCase(context.getString(R.string.sound_meter))) {
            Intent soundMeterIntent = new Intent(context, SoundMeterActivity.class);
            soundMeterIntent.putExtra(KEY_LOG, true);
            soundMeterIntent.putExtra(DATA_BLOCK, block.getBlock());
            context.startActivity(soundMeterIntent);
        }
    }

    private void handleDeleteItem(final SensorDataBlock block) {
        new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.delete))
                .setMessage(context.getString(R.string.delete_confirmation) + " " +
                        CSVLogger.FILE_NAME_FORMAT.format(block.getBlock()) + "?")
                .setPositiveButton(context.getString(R.string.delete), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        File logDirectory = new File(
                                Environment.getExternalStorageDirectory().getAbsolutePath() +
                                        File.separator + CSVLogger.CSV_DIRECTORY +
                                        File.separator + block.getSensorType() +
                                        File.separator + CSVLogger.FILE_NAME_FORMAT.format(block.getBlock()) + ".csv");
                        CustomSnackBar.showSnackBar(context.findViewById(android.R.id.content), context.getString(R.string.log_deleted), null, null, Snackbar.LENGTH_LONG);
                        if (block.getSensorType().equalsIgnoreCase(PSLabSensor.LUXMETER)) {
                            LocalDataLog.with().clearBlockOfLuxRecords(block.getBlock());
                        } else if (block.getSensorType().equalsIgnoreCase(PSLabSensor.BAROMETER)) {
                            LocalDataLog.with().clearBlockOfBaroRecords(block.getBlock());
                        } else if (block.getSensorType().equalsIgnoreCase(PSLabSensor.GYROSCOPE)) {
                            LocalDataLog.with().clearBlockOfBaroRecords(block.getBlock());
                        } else if (block.getSensorType().equalsIgnoreCase(PSLabSensor.COMPASS)) {
                            LocalDataLog.with().clearBlockOfCompassRecords(block.getBlock());
                        } else if (block.getSensorType().equalsIgnoreCase(PSLabSensor.ACCELEROMETER_CONFIGURATIONS)) {
                            LocalDataLog.with().clearBlockOfAccelerometerRecords(block.getBlock());
                        } else if (block.getSensorType().equalsIgnoreCase(PSLabSensor.ROBOTIC_ARM)) {
                            LocalDataLog.with().clearBlockOfServoRecords(block.getBlock());
                        } else if (block.getSensorType().equalsIgnoreCase(PSLabSensor.WAVE_GENERATOR)) {
                            LocalDataLog.with().clearBlockOfWaveRecords(block.getBlock());
                        } else if (block.getSensorType().equalsIgnoreCase(PSLabSensor.OSCILLOSCOPE)) {
                            LocalDataLog.with().clearBlockOfOscilloscopeRecords(block.getBlock());
                        } else if (block.getSensorType().equalsIgnoreCase(PSLabSensor.POWER_SOURCE)) {
                            LocalDataLog.with().clearBlockOfPowerRecords(block.getBlock());
                        } else if (block.getSensorType().equalsIgnoreCase(PSLabSensor.MULTIMETER)) {
                            LocalDataLog.with().clearBlockOfMultimeterRecords(block.getBlock());
                        } else if (block.getSensorType().equalsIgnoreCase(PSLabSensor.LOGIC_ANALYZER)) {
                            LocalDataLog.with().clearBlockOfLARecords(block.getBlock());
                        } else if (block.getSensorType().equalsIgnoreCase(PSLabSensor.GAS_SENSOR)) {
                            LocalDataLog.with().clearBlockOfGasSensorRecords(block.getBlock());
                        } else if (block.getSensorType().equalsIgnoreCase(PSLabSensor.SOUND_METER)) {
                            LocalDataLog.with().clearBlockOfSoundRecords(block.getBlock());
                        }
                        LocalDataLog.with().clearSensorBlock(block.getBlock());
                        dialog.dismiss();
                        if (LocalDataLog.with().getAllSensorBlocks().size() <= 0) {
                            context.findViewById(R.id.data_logger_blank_view).setVisibility(View.VISIBLE);
                        } else {
                            context.findViewById(R.id.data_logger_blank_view).setVisibility(View.GONE);
                        }
                    }
                })
                .setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create().show();
    }

    private void populateMapData(SensorDataBlock block) {

        if (block.getSensorType().equalsIgnoreCase(PSLabSensor.LUXMETER)) {
            RealmResults<LuxData> data = LocalDataLog.with().getBlockOfLuxRecords(block.getBlock());
            JSONArray array = new JSONArray();
            for (LuxData d : data) {
                try {
                    JSONObject i = new JSONObject();
                    i.put("date", CSVLogger.FILE_NAME_FORMAT.format(d.getTime()));
                    i.put("data", d.getLux());
                    i.put("lon", d.getLon());
                    i.put("lat", d.getLat());
                    if (d.getLat() != 0.0 && d.getLon() != 0.0) array.put(i);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            setMapDataToIntent(array);
        } else if (block.getSensorType().equalsIgnoreCase(PSLabSensor.BAROMETER)) {
            RealmResults<BaroData> data = LocalDataLog.with().getBlockOfBaroRecords(block.getBlock());
            JSONArray array = new JSONArray();
            for (BaroData d : data) {
                try {
                    JSONObject i = new JSONObject();
                    i.put("date", CSVLogger.FILE_NAME_FORMAT.format(d.getTime()));
                    i.put("data", d.getBaro());
                    i.put("altitude", d.getAltitude());
                    i.put("lon", d.getLon());
                    i.put("lat", d.getLat());
                    if (d.getLat() != 0.0 && d.getLon() != 0.0) array.put(i);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            setMapDataToIntent(array);
        } else if (block.getSensorType().equalsIgnoreCase(PSLabSensor.GYROSCOPE)) {
            RealmResults<GyroData> data = LocalDataLog.with().getBlockOfGyroRecords(block.getBlock());
            JSONArray array = new JSONArray();
            for (GyroData d : data) {
                try {
                    JSONObject i = new JSONObject();
                    i.put("date", CSVLogger.FILE_NAME_FORMAT.format(d.getTime()));
                    i.put("dataX", d.getGyroX());
                    i.put("dataY", d.getGyroY());
                    i.put("dataZ", d.getGyroZ());
                    i.put("lon", d.getLon());
                    i.put("lat", d.getLat());
                    if (d.getLat() != 0.0 && d.getLon() != 0.0) array.put(i);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            setMapDataToIntent(array);
        } else if (block.getSensorType().equalsIgnoreCase(PSLabSensor.COMPASS)) {
            RealmResults<CompassData> data = LocalDataLog.with().getBlockOfCompassRecords(block.getBlock());
            JSONArray array = new JSONArray();
            for (CompassData d : data) {
                try {
                    JSONObject i = new JSONObject();
                    i.put("date", CSVLogger.FILE_NAME_FORMAT.format(d.getTime()));
                    i.put("dataX", d.getBx());
                    i.put("dataY", d.getBy());
                    i.put("dataZ", d.getBz());
                    i.put("Axis", d.getAxis());
                    i.put("lon", d.getLon());
                    i.put("lat", d.getLat());
                    if (d.getLat() != 0.0 && d.getLon() != 0.0) array.put(i);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            setMapDataToIntent(array);
        } else if (block.getSensorType().equalsIgnoreCase(PSLabSensor.ACCELEROMETER)) {
            RealmResults<AccelerometerData> data = LocalDataLog.with().getBlockOfAccelerometerRecords(block.getBlock());
            JSONArray array = new JSONArray();
            for (AccelerometerData d : data) {
                try {
                    JSONObject i = new JSONObject();
                    i.put("date", CSVLogger.FILE_NAME_FORMAT.format(d.getTime()));
                    i.put("dataX", d.getAccelerometerX());
                    i.put("dataY", d.getAccelerometerY());
                    i.put("dataZ", d.getAccelerometerZ());
                    i.put("lon", d.getLon());
                    i.put("lat", d.getLat());
                    if (d.getLat() != 0.0 && d.getLon() != 0.0) array.put(i);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            setMapDataToIntent(array);
        } else if (block.getSensorType().equalsIgnoreCase(PSLabSensor.THERMOMETER)) {
            RealmResults<ThermometerData> data = LocalDataLog.with().getBlockOfThermometerRecords(block.getBlock());
            JSONArray array = new JSONArray();
            for (ThermometerData d : data) {
                try {
                    JSONObject i = new JSONObject();
                    i.put("date", CSVLogger.FILE_NAME_FORMAT.format(d.getTime()));
                    i.put("Axis", d.getTemp());
                    i.put("lon", d.getLon());
                    i.put("lat", d.getLat());
                    if (d.getLat() != 0.0 && d.getLon() != 0.0) array.put(i);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            setMapDataToIntent(array);
        } else if (block.getSensorType().equalsIgnoreCase(PSLabSensor.ROBOTIC_ARM)) {
            RealmResults<ServoData> data = LocalDataLog.with().getBlockOfServoRecords(block.getBlock());
            JSONArray array = new JSONArray();
            for (ServoData d : data) {
                try {
                    JSONObject i = new JSONObject();
                    i.put("date", CSVLogger.FILE_NAME_FORMAT.format(d.getTime()));
                    i.put("Servo1", d.getDegree1());
                    i.put("Servo2", d.getDegree2());
                    i.put("Servo3", d.getDegree3());
                    i.put("Servo4", d.getDegree4());
                    i.put("lon", d.getLon());
                    i.put("lat", d.getLat());
                    if (d.getLat() != 0.0 && d.getLon() != 0.0) array.put(i);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            setMapDataToIntent(array);
        } else if (block.getSensorType().equalsIgnoreCase(PSLabSensor.WAVE_GENERATOR)) {
            RealmResults<WaveGeneratorData> data = LocalDataLog.with().getBlockOfWaveRecords(block.getBlock());
            JSONArray array = new JSONArray();
            for (WaveGeneratorData d : data) {
                try {
                    JSONObject i = new JSONObject();
                    i.put("date", CSVLogger.FILE_NAME_FORMAT.format(d.getTime()));
                    i.put("Mode", d.getMode());
                    i.put("Wave", d.getWave());
                    i.put("Shape", d.getShape());
                    i.put("Freq", d.getFreq());
                    i.put("Phase", d.getPhase());
                    i.put("Duty", d.getDuty());
                    i.put("lon", d.getLon());
                    i.put("lat", d.getLat());
                    if (d.getLat() != 0.0 && d.getLon() != 0.0) array.put(i);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            setMapDataToIntent(array);
        } else if (block.getSensorType().equalsIgnoreCase(PSLabSensor.OSCILLOSCOPE)) {
            RealmResults<OscilloscopeData> data = LocalDataLog.with().getBlockOfOscilloscopeRecords(block.getBlock());
            JSONArray array = new JSONArray();
            for (OscilloscopeData d : data) {
                try {
                    JSONObject i = new JSONObject();
                    i.put("date", CSVLogger.FILE_NAME_FORMAT.format(d.getTime()));
                    i.put("channel", d.getChannel());
                    i.put("xData", d.getDataX());
                    i.put("yData", d.getDataY());
                    i.put("timebase", d.getTimebase());
                    i.put("lat", d.getLat());
                    i.put("lon", d.getLon());
                    if (d.getLat() != 0.0 && d.getLon() != 0.0) array.put(i);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            setMapDataToIntent(array);
        } else if (block.getSensorType().equalsIgnoreCase(PSLabSensor.POWER_SOURCE)) {
            RealmResults<PowerSourceData> data = LocalDataLog.with().getBlockOfPowerRecords(block.getBlock());
            JSONArray array = new JSONArray();
            for (PowerSourceData d : data) {
                try {
                    JSONObject i = new JSONObject();
                    i.put("date", CSVLogger.FILE_NAME_FORMAT.format(d.getTime()));
                    i.put("PV1", d.getPv1());
                    i.put("PV2", d.getPv2());
                    i.put("PV3", d.getPv3());
                    i.put("PCS", d.getPcs());
                    i.put("lat", d.getLat());
                    i.put("lon", d.getLon());
                    if (d.getLat() != 0.0 && d.getLon() != 0.0) array.put(i);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            setMapDataToIntent(array);
        } else if (block.getSensorType().equalsIgnoreCase(PSLabSensor.MULTIMETER)) {
            RealmResults<MultimeterData> data = LocalDataLog.with().getBlockOfMultimeterRecords(block.getBlock());
            JSONArray array = new JSONArray();
            for (MultimeterData d : data) {
                try {
                    JSONObject i = new JSONObject();
                    i.put("date", CSVLogger.FILE_NAME_FORMAT.format(d.getTime()));
                    i.put("data", d.getData());
                    i.put("value", d.getValue());
                    i.put("lat", d.getLat());
                    i.put("lon", d.getLon());
                    if (d.getLat() != 0.0 && d.getLon() != 0.0) array.put(i);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            setMapDataToIntent(array);
        } else if (block.getSensorType().equalsIgnoreCase(PSLabSensor.LOGIC_ANALYZER)) {
            RealmResults<LogicAnalyzerData> data = LocalDataLog.with().getBlockOfLARecords(block.getBlock());
            JSONArray array = new JSONArray();
            for (LogicAnalyzerData d : data) {
                try {
                    JSONObject i = new JSONObject();
                    i.put("date", CSVLogger.FILE_NAME_FORMAT.format(d.getTime()));
                    i.put("channel", d.getChannel());
                    i.put("channel_mode", d.getChannelMode());
                    i.put("xaxis", d.getDataX());
                    i.put("yaxis", d.getDataY());
                    i.put("lat", d.getLat());
                    i.put("lon", d.getLon());
                    if (d.getLat() != 0.0 && d.getLon() != 0.0) array.put(i);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            setMapDataToIntent(array);
        } else if (block.getSensorType().equalsIgnoreCase(PSLabSensor.GAS_SENSOR)) {
            RealmResults<GasSensorData> data = LocalDataLog.with().getBlockOfGasSensorRecords(block.getBlock());
            JSONArray array = new JSONArray();
            for (GasSensorData d : data) {
                try {
                    JSONObject i = new JSONObject();
                    i.put("date", CSVLogger.FILE_NAME_FORMAT.format(d.getTime()));
                    i.put("ppmValue", d.getPpmValue());
                    i.put("lon", d.getLon());
                    i.put("lat", d.getLat());
                    if (d.getLat() != 0.0 && d.getLon() != 0.0) array.put(i);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            setMapDataToIntent(array);
        } else if (block.getSensorType().equalsIgnoreCase(PSLabSensor.SOUND_METER)) {
            RealmResults<SoundData> data = LocalDataLog.with().getBlockOfSoundRecords(block.getBlock());
            JSONArray array = new JSONArray();
            for (SoundData d : data) {
                try {
                    JSONObject i = new JSONObject();
                    i.put("date", CSVLogger.FILE_NAME_FORMAT.format(d.getTime()));
                    i.put("dB", d.getdB());
                    i.put("lon", d.getLon());
                    i.put("lat", d.getLat());
                    if (d.getLat() != 0.0 && d.getLon() != 0.0) array.put(i);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            setMapDataToIntent(array);
        }
    }

    private void setMapDataToIntent(JSONArray array) {
        Intent map = new Intent(context, MapsActivity.class);
        if (array.length() > 0) {
            map.putExtra("hasMarkers", true);
            map.putExtra("markers", array.toString());
            context.startActivity(map);
        } else {
            map.putExtra("hasMarkers", false);
            CustomSnackBar.showSnackBar(context.findViewById(android.R.id.content),
                    context.getString(R.string.no_location_data), null, null, Snackbar.LENGTH_LONG);
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView sensor, dateTime;
        private ImageView deleteIcon, mapIcon, tileIcon;
        private CardView cardView;

        public ViewHolder(View itemView) {
            super(itemView);
            dateTime = itemView.findViewById(R.id.date_time);
            sensor = itemView.findViewById(R.id.sensor_name);
            deleteIcon = itemView.findViewById(R.id.delete_item);
            mapIcon = itemView.findViewById(R.id.map_item);
            tileIcon = itemView.findViewById(R.id.sensor_tile_icon);
            cardView = itemView.findViewById(R.id.data_item_card);
        }
    }
}
