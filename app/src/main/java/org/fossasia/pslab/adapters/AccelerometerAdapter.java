package org.fossasia.pslab.adapters;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;

import org.fossasia.pslab.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import java.text.DecimalFormat;

import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;

import static android.content.Context.SENSOR_SERVICE;

/**
 * Created by Vikum on 6/10/18.
 */

public class AccelerometerAdapter extends RecyclerView.Adapter<AccelerometerAdapter.ViewHolder> {

    private String[] dataset;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private DecimalFormat df = new DecimalFormat("+#0.0;-#0.0");
    private Context context;
    private long startTime;
    private int[] colors = {Color.YELLOW, Color.BLUE, Color.GREEN};

    static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.axis_image)
        ImageView axisImage;
        @BindView(R.id.accel_value)
        TextView value;
        @BindView(R.id.accel_max_text)
        TextView maxValue;
        @BindView(R.id.accel_min_text)
        TextView minValue;
        @BindView(R.id.chart_accelerometer)
        LineChart chart;

        private float currentMax = Integer.MIN_VALUE;
        private float currentMin = Integer.MAX_VALUE;
        private YAxis y;
        private LineData data = new LineData();
        private ArrayList<Entry> entries;
        private long timeElapsed;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public AccelerometerAdapter(String[] dataset, Context context) {
        this.dataset = dataset;
        this.context = context;
    }

    @NonNull
    @Override
    public AccelerometerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.accelerometer_list_item, parent, false);
        sensorManager = (SensorManager) parent.getContext().getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager != null ? sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) : null;
        startTime = System.currentTimeMillis();
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        holder.setIsRecyclable(true);
        holder.entries = new ArrayList<>();
        XAxis x = holder.chart.getXAxis();
        holder.y = holder.chart.getAxisLeft();
        YAxis y2 = holder.chart.getAxisRight();

        holder.chart.setTouchEnabled(true);
        holder.chart.setHighlightPerDragEnabled(true);
        holder.chart.setDragEnabled(true);
        holder.chart.setScaleEnabled(true);
        holder.chart.setDrawGridBackground(false);
        holder.chart.setPinchZoom(true);
        holder.chart.setScaleYEnabled(false);
        holder.chart.setBackgroundColor(Color.BLACK);
        holder.chart.getDescription().setEnabled(false);
        Legend l = holder.chart.getLegend();
        l.setForm(Legend.LegendForm.LINE);
        l.setTextColor(Color.WHITE);

        holder.chart.setData(holder.data);

        x.setTextColor(Color.WHITE);
        x.setDrawGridLines(true);
        x.setAvoidFirstLastClipping(true);
        x.setDrawLabels(false);

        holder.y.setTextColor(Color.WHITE);
        holder.y.setAxisMaximum(20);
        holder.y.setAxisMinimum(-20);
        holder.y.setDrawGridLines(true);
        holder.y.setLabelCount(6);

        y2.setDrawGridLines(false);
        sensorManager.registerListener(new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                float currentAcc = event.values[holder.getAdapterPosition()];
                StringBuilder builder = new StringBuilder();
                builder.append(df.format(currentAcc));
                builder.append(" ");
                builder.append(context.getResources().getString(R.string.meters_per_sec_text));
                holder.value.setText(Html.fromHtml(builder.toString()));

                if (currentAcc > holder.currentMax) {
                    builder.insert(0, context.getResources().getString(R.string.text_max));
                    builder.insert(3, " ");
                    holder.maxValue.setText(Html.fromHtml(builder.toString()));
                    holder.currentMax = currentAcc;
                } else if (currentAcc < holder.currentMin) {
                    builder.insert(0, context.getResources().getString(R.string.text_min));
                    builder.insert(3, " ");
                    holder.minValue.setText(Html.fromHtml(builder.toString()));
                    holder.currentMin = currentAcc;
                }
                holder.timeElapsed = (System.currentTimeMillis() - startTime) / 1000;
                holder.entries.add(new Entry((float) holder.timeElapsed, currentAcc));
                LineDataSet dataSet = new LineDataSet(holder.entries, dataset[holder.getAdapterPosition()]);
                LineData data = new LineData(dataSet);
                dataSet.setDrawCircles(false);
                dataSet.setColor(colors[holder.getAdapterPosition()]);

                holder.chart.setData(data);
                holder.chart.notifyDataSetChanged();
                holder.chart.setVisibleXRangeMaximum(3);
                holder.chart.moveViewToX(data.getEntryCount());
                holder.chart.invalidate();
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                //do nothing
            }
        }, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        switch (position) {
            case 0:
                holder.axisImage.setImageResource(R.drawable.phone_x_axis);
                break;
            case 1:
                holder.axisImage.setImageResource(R.drawable.phone_y_axis);
                break;
            case 2:
                holder.axisImage.setImageResource(R.drawable.phone_z_axis);
                break;
            default:
                break;
        }
    }

    @Override
    public int getItemCount() {
        return dataset.length;
    }
}
