package io.pslab.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.pslab.R;
import io.pslab.DataFormatter;
import io.pslab.others.CSVLogger;
import io.pslab.others.CustomSnackBar;

public class SensorGraphViewActivity extends AppCompatActivity {
    public static final String TYPE_SENSOR = "sensor";
    public static final String DATA_FOREIGN_KEY = "foreignKey";
    public static final String DATE_TIME_START = "dateTimeStart";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    private static final int MY_PERMISSIONS_REQUEST_STORAGE_FOR_DATA = 101;
    private static final int MY_PERMISSIONS_REQUEST_STORAGE_FOR_MAPS = 102;
    public static final String DATE_TIME_END = "dateTimeEnd";
    public static final String TIME_ZONE = "timezone";


    @BindView(R.id.chart_lux_meter)
    LineChart mChart;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.date_rec)
    TextView tv_date;

    @BindView(R.id.time_rec)
    TextView tv_time;

    @BindView(R.id.data_view_cl)
    CoordinatorLayout cl;

    @BindView(R.id.osmmap)
    MapView map;

    @BindView(R.id.loc_lat)
    TextView tv_lat;

    @BindView(R.id.loc_long)
    TextView tv_long;

    private Resources resources;
    private ArrayList<Entry> entries;
    private SimpleDateFormat sdd = new SimpleDateFormat("dd MMM yyyy");
    private SimpleDateFormat sdt = new SimpleDateFormat("HH:mm:ss");
    private Date startDate;
    private double latitude;
    private double longitude;
    private Date endDate;
    private String timeZone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_graph_view);
        ButterKnife.bind(this);
        resources = getResources();
        Intent intent = getIntent();
        String sensorType = intent.getStringExtra(TYPE_SENSOR);
        setSupportActionBar(toolbar);

        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(sensorType + " " + getResources().getString(R.string.data));
        }

        timeZone = intent.getStringExtra(TIME_ZONE);
        startDate = new Date(intent.getLongExtra(DATE_TIME_START, 0));
        endDate = new Date(intent.getLongExtra(DATE_TIME_END, 0));
        tv_date.setText(sdd.format(startDate));
        tv_time.setText(sdt.format(startDate));

        latitude = intent.getDoubleExtra(LATITUDE, 0.0);
        longitude = intent.getDoubleExtra(LONGITUDE, 0.0);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                IMapController mapController = map.getController();
                mapController.setZoom((double) 9);
                GeoPoint startPoint = new GeoPoint(latitude, latitude);
                mapController.setCenter(startPoint);
            }
        });

        if (latitude != 0.0 && longitude != 0.0) {
            tv_lat.setText(DataFormatter.formatDouble(latitude, DataFormatter.MEDIUM_PRECISION_FORMAT));
            tv_long.setText(DataFormatter.formatDouble(longitude, DataFormatter.MEDIUM_PRECISION_FORMAT));
            thread.start();
        } else {
            tv_lat.setText("NA");
            tv_long.setText("NA");
        }

        entries = new ArrayList<>();

        XAxis x = mChart.getXAxis();
        YAxis y = mChart.getAxisLeft();
        YAxis y2 = mChart.getAxisRight();

        mChart.setTouchEnabled(true);
        mChart.setHighlightPerDragEnabled(true);
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setDrawGridBackground(false);
        mChart.setPinchZoom(true);
        mChart.setScaleYEnabled(false);
        mChart.setBackgroundColor(Color.BLACK);
        mChart.getDescription().setEnabled(false);

        LineData data = new LineData();
        mChart.setData(data);

        Legend l = mChart.getLegend();
        l.setForm(Legend.LegendForm.LINE);
        l.setTextColor(Color.WHITE);

        x.setTextColor(Color.WHITE);
        x.setDrawGridLines(true);
        x.setAvoidFirstLastClipping(true);

        y.setTextColor(Color.WHITE);
        y.setDrawGridLines(true);
        y.setLabelCount(10);

        y2.setDrawGridLines(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LineDataSet dataSet = new LineDataSet(entries, getString(R.string.lux));
        LineData data = new LineData(dataSet);
        dataSet.setDrawCircles(false);
        dataSet.setDrawValues(false);
        dataSet.setLineWidth(2);
        mChart.setData(data);
        mChart.invalidate();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_show_item_logged_data, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.export_logged_data:
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_STORAGE_FOR_DATA);
                } else {
                    exportData();
                }
                break;
            case R.id.simulate:
                simulateData();
        }
        return false;
    }

    private void exportData() {
        boolean firstDataRow = true;

        CSVLogger luxLogger = new CSVLogger(getResources().getString(R.string.lux_meter));
        luxLogger.writeCSVFile(resources.getString(R.string.time_elapsed)
                + "," + resources.getString(R.string.lux)
                + "," + resources.getString(R.string.date_of_rec)
                + "," + getString(R.string.start_time)
                + "," + getString(R.string.end_time)
                + "," + resources.getString(R.string.latitude)
                + "," + resources.getString(R.string.longitude)
                + "," + getString(R.string.timezone)
                + "," + getString(R.string.dev_model)
                + "," + getString(R.string.dev_brand)
                + "," + getString(R.string.dev_android_sdk) + "\n");

        String data;
        for (Entry item : entries) {
            if (firstDataRow) {
                data = String.valueOf(item.getX())
                        + "," + String.valueOf(item.getY())
                        + "," + sdd.format(startDate)
                        + "," + sdt.format(startDate)
                        + "," + sdt.format(endDate);

                if (latitude != 0.0 && longitude != 0.0) {
                    data += "," + String.valueOf(latitude) + "," + String.valueOf(longitude);
                } else {
                    data += ",NA,NA";
                }

                data += "," + timeZone
                        + "," + Build.MODEL
                        + "," + Build.MANUFACTURER
                        + "," + String.valueOf(Build.VERSION.SDK_INT) + "\n";

                firstDataRow = false;
            } else {
                data = String.valueOf(item.getX()) + "," + String.valueOf(item.getY()) + "\n";
            }
            luxLogger.writeCSVFile(data);
        }
        CustomSnackBar.showSnackBar(cl, getResources().getString(R.string.csv_store_text) + luxLogger.getCurrentFilePath(), null, null, Snackbar.LENGTH_LONG);
    }

    private void simulateData() {
        mChart.clear();
        mChart.invalidate();
        final ArrayList<Entry> simulateData = new ArrayList<>();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                for (Entry item : entries) {
                    simulateData.add(item);
                    LineDataSet dataSet = new LineDataSet(simulateData, getString(R.string.lux));
                    LineData data = new LineData(dataSet);
                    dataSet.setDrawCircles(false);
                    dataSet.setDrawValues(false);
                    dataSet.setLineWidth(2);

                    mChart.setData(data);
                    mChart.invalidate();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == MY_PERMISSIONS_REQUEST_STORAGE_FOR_DATA) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                exportData();
            } else {
                Toast.makeText(this, R.string.prmsn_denied_storage, Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == MY_PERMISSIONS_REQUEST_STORAGE_FOR_MAPS
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Intent MAP = new Intent(getApplicationContext(), MapsActivity.class);
            startActivity(MAP);
        }
    }
}
