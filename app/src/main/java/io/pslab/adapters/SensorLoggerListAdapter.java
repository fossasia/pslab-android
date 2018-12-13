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
import io.pslab.activity.LuxMeterActivity;
import io.pslab.activity.MapsActivity;
import io.pslab.models.LuxData;
import io.pslab.models.SensorDataBlock;
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
        holder.sensor.setText(block.getSensorType());
        holder.dateTime.setText(String.valueOf(CSVLogger.FILE_NAME_FORMAT.format(new Date(block.getBlock()))));
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (block.getSensorType().equalsIgnoreCase(context.getResources().getString(R.string.lux_meter))) {
                    Intent LuxMeter = new Intent(context, LuxMeterActivity.class);
                    LuxMeter.putExtra(KEY_LOG, true);
                    LuxMeter.putExtra(DATA_BLOCK, block.getBlock());
                    context.startActivity(LuxMeter);
                }
            }
        });
        holder.deleteIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(context)
                        .setTitle(context.getString(R.string.delete))
                        .setMessage(context.getString(R.string.delete_confirmation) + " " +
                                CSVLogger.FILE_NAME_FORMAT.format(block.getBlock()) + "?")
                        .setPositiveButton(context.getString(R.string.delete), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                if (block.getSensorType().equalsIgnoreCase("Lux Meter")) {
                                    File logDirectory = new File(
                                            Environment.getExternalStorageDirectory().getAbsolutePath() +
                                                    File.separator + CSVLogger.CSV_DIRECTORY +
                                                    File.separator + block.getSensorType() +
                                                    File.separator + CSVLogger.FILE_NAME_FORMAT.format(block.getBlock()) + ".csv");
                                    Toast.makeText(context, logDirectory.delete()
                                                    ? context.getString(R.string.log_deleted)
                                                    : context.getString(R.string.nothing_to_delete),
                                            Toast.LENGTH_LONG).show();
                                    LocalDataLog.with().clearBlockOfLuxRecords(block.getBlock());
                                    LocalDataLog.with().clearSensorBlock(block.getBlock());
                                }
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create().show();
            }
        });
        holder.mapIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (block.getSensorType().equalsIgnoreCase("Lux Meter")) {
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
            }
        });
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView sensor, dateTime;
        private ImageView deleteIcon, mapIcon;
        private CardView cardView;

        public ViewHolder(View itemView) {
            super(itemView);
            dateTime = itemView.findViewById(R.id.date_time);
            sensor = itemView.findViewById(R.id.sensor_name);
            deleteIcon = itemView.findViewById(R.id.delete_item);
            mapIcon = itemView.findViewById(R.id.map_item);
            cardView = itemView.findViewById(R.id.data_item_card);
        }
    }
}
