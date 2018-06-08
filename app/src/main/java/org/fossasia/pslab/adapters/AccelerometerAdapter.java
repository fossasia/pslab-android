package org.fossasia.pslab.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;

import org.fossasia.pslab.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Vikum on 6/10/18.
 */

public class AccelerometerAdapter extends RecyclerView.Adapter<AccelerometerAdapter.ViewHolder> {

    private String[] dataset;

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

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public AccelerometerAdapter(String[] dataset) {
        this.dataset = dataset;
    }

    @NonNull
    @Override
    public AccelerometerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.accelerometer_list_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setIsRecyclable(true);

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
