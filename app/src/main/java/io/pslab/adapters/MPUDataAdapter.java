package io.pslab.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import io.pslab.R;
import io.pslab.models.DataMPU6050;

import io.realm.RealmResults;

/**
 * Created by viveksb007 on 13/8/17.
 */

public class MPUDataAdapter extends RecyclerView.Adapter<MPUDataAdapter.ViewHolder> {

    private RealmResults<DataMPU6050> results;

    public MPUDataAdapter(RealmResults<DataMPU6050> results) {
        this.results = results;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.sensor_mpu6050_data_card, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        DataMPU6050 temp = results.get(position);
        holder.tvAx.setText(String.valueOf(temp.getAx()));
        holder.tvAy.setText(String.valueOf(temp.getAy()));
        holder.tvAz.setText(String.valueOf(temp.getAz()));
        holder.tvGx.setText(String.valueOf(temp.getGx()));
        holder.tvGy.setText(String.valueOf(temp.getGy()));
        holder.tvGz.setText(String.valueOf(temp.getGz()));
        holder.tvTemperature.setText(String.valueOf(temp.getTemperature()));
        holder.tvTitle.setText("Raw Data #" + (position + 1));

    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tvAx, tvAy, tvAz, tvGx, tvGy, tvGz, tvTemperature, tvTitle;

        ViewHolder(View itemView) {
            super(itemView);
            tvAx = itemView.findViewById(R.id.tv_sensor_mpu6050_ax);
            tvAy = itemView.findViewById(R.id.tv_sensor_mpu6050_ay);
            tvAz = itemView.findViewById(R.id.tv_sensor_mpu6050_az);
            tvTemperature = itemView.findViewById(R.id.tv_sensor_mpu6050_temp);
            tvGx = itemView.findViewById(R.id.tv_sensor_mpu6050_gx);
            tvGy = itemView.findViewById(R.id.tv_sensor_mpu6050_gy);
            tvGz = itemView.findViewById(R.id.tv_sensor_mpu6050_gz);
            tvTitle = itemView.findViewById(R.id.card_title);
        }
    }

}
