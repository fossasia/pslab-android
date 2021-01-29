package io.pslab.widgets.loggedData;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import io.pslab.R;
import io.pslab.adapters.SensorLoggerListAdapter;
import io.pslab.models.PSLabSensor;
import io.pslab.models.SensorDataBlock;
import io.pslab.others.CSVLogger;
import io.pslab.others.LocalDataLog;
import io.realm.Realm;
import io.realm.RealmResults;

public class logDataWidgetList implements RemoteViewsService.RemoteViewsFactory {

    private List<SensorDataBlock> results = new ArrayList<>();
    private Context context;
    private RealmResults<SensorDataBlock> entities;
    private Realm realm;
//  private int appWidgetId;

    public logDataWidgetList(Context context, Intent intent) {

        this.context = context;
//      appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    @Override
    public void onCreate() {
        refreshEntities();
    }

    private void refreshEntities() {
        realm = LocalDataLog.with().getRealm();
        entities = LocalDataLog.with().getAllSensorBlocks();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        final RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.logger_data_item);
        final SensorDataBlock block = entities.get(position);
        assert block!=null;
        switch (block.getSensorType()) {
            case PSLabSensor.LUXMETER:
                remoteViews.setTextViewText(R.id.sensor_name,context.getResources().getString(R.string.lux_meter));
                remoteViews.setImageViewResource(R.id.sensor_tile_icon,R.drawable.tile_icon_lux_meter_log);
                break;
            case PSLabSensor.BAROMETER:
                remoteViews.setTextViewText(R.id.sensor_name,context.getResources().getString(R.string.baro_meter));
                remoteViews.setImageViewResource(R.id.sensor_tile_icon,R.drawable.tile_icon_barometer_log);
                break;
            case PSLabSensor.GYROSCOPE:
                remoteViews.setTextViewText(R.id.sensor_name,context.getResources().getString(R.string.gyroscope));
                remoteViews.setImageViewResource(R.id.sensor_tile_icon,R.drawable.gyroscope_logo);
                break;
            case PSLabSensor.COMPASS:
                remoteViews.setTextViewText(R.id.sensor_name,context.getResources().getString(R.string.gyroscope));
                remoteViews.setImageViewResource(R.id.sensor_tile_icon,R.drawable.gyroscope_logo);
                break;
            case PSLabSensor.ACCELEROMETER:
                remoteViews.setTextViewText(R.id.sensor_name,context.getResources().getString(R.string.accelerometer));
                remoteViews.setImageViewResource(R.id.sensor_tile_icon,R.drawable.tile_icon_accelerometer);
                break;
            case PSLabSensor.THERMOMETER:
                remoteViews.setTextViewText(R.id.sensor_name,context.getResources().getString(R.string.thermometer));
                remoteViews.setImageViewResource(R.id.sensor_tile_icon,R.drawable.thermometer_logo);
                break;
            case PSLabSensor.ROBOTIC_ARM:
                remoteViews.setTextViewText(R.id.sensor_name,context.getResources().getString(R.string.robotic_arm));
                remoteViews.setImageViewResource(R.id.sensor_tile_icon,R.drawable.robotic_arm);
                break;
            case PSLabSensor.WAVE_GENERATOR:
                remoteViews.setTextViewText(R.id.sensor_name,context.getResources().getString(R.string.wave_generator));
                remoteViews.setImageViewResource(R.id.sensor_tile_icon,R.drawable.tile_icon_wave_generator);
                break;
            case PSLabSensor.OSCILLOSCOPE:
                remoteViews.setTextViewText(R.id.sensor_name,context.getResources().getString(R.string.oscilloscope));
                remoteViews.setImageViewResource(R.id.sensor_tile_icon,R.drawable.tile_icon_oscilloscope);
                break;
            case PSLabSensor.POWER_SOURCE:
                remoteViews.setTextViewText(R.id.sensor_name,context.getResources().getString(R.string.power_source));
                remoteViews.setImageViewResource(R.id.sensor_tile_icon,R.drawable.tile_icon_power_source);
                break;
            case PSLabSensor.MULTIMETER:
                remoteViews.setTextViewText(R.id.sensor_name,context.getResources().getString(R.string.multimeter));
                remoteViews.setImageViewResource(R.id.sensor_tile_icon,R.drawable.tile_icon_multimeter);
                break;
            case PSLabSensor.LOGIC_ANALYZER:
                remoteViews.setTextViewText(R.id.sensor_name,context.getResources().getString(R.string.logical_analyzer));
                remoteViews.setImageViewResource(R.id.sensor_tile_icon,R.drawable.tile_icon_logic_analyzer);
                break;
            case PSLabSensor.GAS_SENSOR:
                remoteViews.setTextViewText(R.id.sensor_name,context.getResources().getString(R.string.gas_sensor));
                remoteViews.setImageViewResource(R.id.sensor_tile_icon,R.drawable.tile_icon_gas);
                break;
            case PSLabSensor.SOUND_METER:
                remoteViews.setTextViewText(R.id.sensor_name,context.getResources().getString(R.string.sound_meter));
                remoteViews.setImageViewResource(R.id.sensor_tile_icon,R.drawable.tile_icon_gas);
                break;
            default:
                break;
        }
        remoteViews.setTextViewText(R.id.date_time,String.valueOf(CSVLogger.FILE_NAME_FORMAT.format(new Date(block.getBlock()))));
        return remoteViews;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getCount() {

        return entities.size();
    }

    @Override
    public long getItemId(int position) {

        return position;
    }

    @Override
    public void onDataSetChanged() {

        refreshEntities();
    }

    @Override
    public void onDestroy() { }


    @Override
    public int getViewTypeCount() {

        return 1;
    }

    @Override
    public boolean hasStableIds() {

        return false;
    }
}
