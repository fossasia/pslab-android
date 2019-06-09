package io.pslab.adapters;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Date;

import io.pslab.R;
import io.pslab.activity.AccelerometerActivity;
import io.pslab.activity.BarometerActivity;
import io.pslab.activity.GyroscopeActivity;
import io.pslab.activity.LuxMeterActivity;
import io.pslab.activity.MapsActivity;
import io.pslab.activity.CompassActivity;
import io.pslab.activity.RoboticArmActivity;
import io.pslab.activity.ThermometerActivity;
import io.pslab.models.AccelerometerData;
import io.pslab.models.BaroData;
import io.pslab.models.GyroData;
import io.pslab.models.CompassData;
import io.pslab.models.LuxData;
import io.pslab.models.PSLabSensor;
import io.pslab.models.SensorDataBlock;
import io.pslab.models.ServoData;
import io.pslab.models.ThermometerData;
import io.pslab.others.CSVLogger;
import io.pslab.others.LocalDataLog;
import io.realm.RealmRecyclerViewAdapter;
import io.realm.RealmResults;

/**
 * Created by Avjeet on 03-08-2018.
 */
public class SensorLoggerListAdapter extends RealmRecyclerViewAdapter<SensorDataBlock, SensorLoggerListAdapter.ViewHolder> {


    private Activity context;
    private final String KEY_LOG = "has_log";
    private final String DATA_BLOCK = "data_block";

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
                holder.tileIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.tile_icon_lux_meter_log));
                break;
            case PSLabSensor.BAROMETER:
                holder.sensor.setText(context.getResources().getString(R.string.baro_meter));
                holder.tileIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.tile_icon_barometer_log));
                break;
            case PSLabSensor.GYROSCOPE:
                holder.sensor.setText(context.getResources().getString(R.string.gyroscope));
                holder.tileIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.gyroscope_logo));
                break;
            case PSLabSensor.COMPASS:
                holder.sensor.setText(context.getResources().getString(R.string.compass));
                holder.tileIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.tile_icon_compass_log));
                break;
            case PSLabSensor.ACCELEROMETER:
                holder.sensor.setText(context.getResources().getString(R.string.accelerometer));
                holder.tileIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.tile_icon_accelerometer));
                break;
            case PSLabSensor.THERMOMETER:
                holder.sensor.setText(R.string.thermometer);
                holder.tileIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.thermometer_logo));
                break;
            case PSLabSensor.ROBOTIC_ARM:
                holder.sensor.setText(R.string.robotic_arm);
                holder.tileIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.gyroscope_logo));
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
                        Toast.makeText(context, logDirectory.delete()
                                        ? context.getString(R.string.log_deleted)
                                        : context.getString(R.string.nothing_to_delete),
                                Toast.LENGTH_LONG).show();
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
        } else if (block.getSensorType().equalsIgnoreCase(PSLabSensor.ACCELEROMETER_CONFIGURATIONS)) {
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
            Toast.makeText(context, context.getResources().getString(R.string.no_location_data), Toast.LENGTH_LONG).show();
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
