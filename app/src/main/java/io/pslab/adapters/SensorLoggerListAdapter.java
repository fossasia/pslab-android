package io.pslab.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

import io.pslab.R;
import io.pslab.models.LuxData;
import io.pslab.models.SensorLogged;
import io.realm.Realm;
import io.realm.RealmRecyclerViewAdapter;
import io.realm.RealmResults;

/**
 * Created by Avjeet on 03-08-2018.
 */
public class SensorLoggerListAdapter extends RealmRecyclerViewAdapter<SensorLogged, SensorLoggerListAdapter.ViewHolder> {


    private Context context;
    private SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy  HH:mm:ss");
    private Realm realm;

    public SensorLoggerListAdapter(RealmResults<SensorLogged> results, Context context) {
        super(results, true, true);
        this.context = context;
        realm = Realm.getDefaultInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.logger_data_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final SensorLogged temp = getItem(position);
        holder.sensor.setText(temp.getSensor());
        Date date = new Date(temp.getDateTimeStamp());
        holder.dateTime.setText(String.valueOf(sdf.format(date)));
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RealmResults<LuxData> results = realm.where(LuxData.class).equalTo("foreignKey", getItem(holder.getAdapterPosition()).getUniqueRef()).findAll();
                Toast.makeText(context, "Results size :" + String.valueOf(results.size()), Toast.LENGTH_SHORT).show();
            }
        });
        holder.deleteIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RealmResults<SensorLogged> sensorItem = realm.where(SensorLogged.class).equalTo("uniqueRef", getItem(holder.getAdapterPosition()).getUniqueRef()).findAll();
                RealmResults<LuxData> results = realm.where(LuxData.class).equalTo("foreignKey", getItem(holder.getAdapterPosition()).getUniqueRef()).findAll();
                realm.beginTransaction();
                results.deleteAllFromRealm();
                sensorItem.deleteAllFromRealm();
                realm.commitTransaction();
            }
        });
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView sensor, dateTime;
        ImageView deleteIcon;
        private CardView cardView;

        public ViewHolder(View itemView) {
            super(itemView);
            dateTime = itemView.findViewById(R.id.date_time);
            sensor = itemView.findViewById(R.id.sensor_name);
            deleteIcon = itemView.findViewById(R.id.delete_item);
            cardView = itemView.findViewById(R.id.data_item_card);
        }
    }
}
