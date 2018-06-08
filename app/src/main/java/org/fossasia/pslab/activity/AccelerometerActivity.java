package org.fossasia.pslab.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import org.fossasia.pslab.R;
import org.fossasia.pslab.adapters.AccelerometerAdapter;

public class AccelerometerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accelerometer);
        AccelerometerAdapter adapter = new AccelerometerAdapter(new String[]{"X axis", "Y axis", "Z axis"}, getApplicationContext());

        RecyclerView recyclerView = this.findViewById(R.id.accelerometer_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }
}
