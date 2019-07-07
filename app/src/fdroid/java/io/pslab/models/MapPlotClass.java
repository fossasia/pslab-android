package io.pslab.models;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import org.json.JSONArray;
import io.pslab.R;
import io.pslab.activity.MapsActivity;

public class MapPlotClass{

    public static void setMapData(JSONArray array, Context context){
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
