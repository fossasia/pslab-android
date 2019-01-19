package io.pslab.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import io.pslab.R;

/**
 * Created by Padmal on 11/7/18.
 */

public class MapsActivity extends AppCompatActivity {

    MapView map = null;
    private Marker m;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        map = findViewById(R.id.osmmap);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);

        m = new Marker(map);

        IMapController mapController = map.getController();
        mapController.setZoom((double) 9);

        if (getIntent().getExtras() != null && getIntent().getExtras().getBoolean("hasMarkers")) {
            try {
                JSONArray markers = new JSONArray(getIntent().getExtras().getString("markers"));
                addMarkers(markers);
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                map.invalidate();
                mapController.setCenter(m.getPosition());
            }
        } else {
            GeoPoint startPoint = new GeoPoint(-33.8688, 151.2093);
            mapController.setCenter(startPoint);
        }
    }

    private void addMarkers(JSONArray markers) throws JSONException {
        for (int i = 0; i < markers.length(); i++) {
            JSONObject marker = markers.getJSONObject(i);
            m.setPosition(new GeoPoint(marker.getDouble("lat"), marker.getDouble("lon")));
            m.setTitle(marker.getString("data_x") + " @ " + marker.getString("date"));
            m.setTitle(marker.getString("data_y") + " @ " + marker.getString("date"));
            m.setTitle(marker.getString("data_z") + " @ " + marker.getString("date"));
            m.setIcon(getResources().getDrawable(R.drawable.action_item_read));
            m.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_TOP);
            map.getOverlays().add(m);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        map.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        map.onPause();
    }
}
