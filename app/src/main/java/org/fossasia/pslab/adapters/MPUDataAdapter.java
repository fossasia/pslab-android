package org.fossasia.pslab.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.fossasia.pslab.R;
import org.fossasia.pslab.models.DataMPU6050;

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

        public ViewHolder(View itemView) {
            super(itemView);
            tvAx = (TextView) itemView.findViewById(R.id.tv_sensor_mpu6050_ax);
            tvAy = (TextView) itemView.findViewById(R.id.tv_sensor_mpu6050_ay);
            tvAz = (TextView) itemView.findViewById(R.id.tv_sensor_mpu6050_az);
            tvTemperature = (TextView) itemView.findViewById(R.id.tv_sensor_mpu6050_temp);
            tvGx = (TextView) itemView.findViewById(R.id.tv_sensor_mpu6050_gx);
            tvGy = (TextView) itemView.findViewById(R.id.tv_sensor_mpu6050_gy);
            tvGz = (TextView) itemView.findViewById(R.id.tv_sensor_mpu6050_gz);
            tvTitle = (TextView) itemView.findViewById(R.id.card_title);
        }
    }

}
