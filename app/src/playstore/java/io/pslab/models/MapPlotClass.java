package io.pslab.models;

import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.widget.Toast;
import org.json.JSONArray;

import io.pslab.activity.GoogleMapsActivity;
import io.pslab.R;
import io.pslab.activity.MapsActivity;
import io.pslab.fragment.SettingsFragment;

public class MapPlotClass{

    public static void setMapData(JSONArray array, Context context){

        if (PreferenceManager.getDefaultSharedPreferences(context).getString(SettingsFragment.KEY_MAP_SERVICES,"OpenStreetMap").equals("0")) {
             Intent map = new Intent(context, MapsActivity.class);
            if (array.length() > 0) {
                map.putExtra("hasMarkers", true);
                map.putExtra("markers", array.toString());
                context.startActivity(map);
            } else {
                map.putExtra("hasMarkers", false);
                Toast.makeText(context, context.getResources().getString(R.string.no_location_data), Toast.LENGTH_LONG).show();
            }
        }else{
            Intent googleMap = new Intent(context, GoogleMapsActivity.class);
            if (array.length() > 0) {
                googleMap.putExtra("hasMarkers", true);
                googleMap.putExtra("markers", array.toString());
                context.startActivity(googleMap);
            } else {
                googleMap.putExtra("hasMarkers", false);
                Toast.makeText(context, context.getResources().getString(R.string.no_location_data), Toast.LENGTH_LONG).show();
            }
        }
    }
}
