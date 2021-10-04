package io.pslab.activity;

import androidx.fragment.app.FragmentActivity;
import android.os.Bundle;
import com.google.android.material.snackbar.Snackbar;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.pslab.R;
import io.pslab.others.CustomSnackBar;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (getIntent().getExtras() != null && getIntent().getExtras().getBoolean("hasMarkers")) {
            try {
                JSONArray markers = new JSONArray(getIntent().getExtras().getString("markers"));
                addMarkers(markers);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            CustomSnackBar.showSnackBar(findViewById(android.R.id.content),
                    getString(R.string.no_location_specified),null,null, Snackbar.LENGTH_SHORT);
        }
    }

    private void addMarkers(JSONArray markers) throws JSONException {
        for (int i = 0; i < markers.length(); i++) {
            JSONObject marker = markers.getJSONObject(i);
            LatLng location = new LatLng(marker.getDouble("lat"),marker.getDouble("lon"));
            mMap.addMarker(new MarkerOptions().position(location).title("Location"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(location));
        }
    }
}
