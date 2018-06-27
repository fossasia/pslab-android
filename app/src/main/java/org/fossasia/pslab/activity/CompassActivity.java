package org.fossasia.pslab.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.github.anastr.speedviewlib.ImageSpeedometer;
import com.github.anastr.speedviewlib.components.Indicators.ImageIndicator;
import com.github.anastr.speedviewlib.util.OnPrintTickLabel;

import org.fossasia.pslab.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Vikum on 6/27/18.
 */

public class CompassActivity extends AppCompatActivity {
    @BindView(R.id.compass)
    ImageSpeedometer compass;
    Float speed = 0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compass);
        ButterKnife.bind(this);
        ImageIndicator indicator = new ImageIndicator(getApplicationContext(), R.drawable.compass_needle);
        compass.setIndicator(indicator);
        compass.speedTo(360.0f);
        compass.setWithTremble(false);

        compass.setOnPrintTickLabel(new OnPrintTickLabel() {
            @Override
            public CharSequence getTickLabel(int tickPosition, float tick) {
                switch (tickPosition) {
                    case 0:
                    case 4:
                        return "N";
                    case 1:
                        return "E";
                    case 2:
                        return "S";
                    case 3:
                        return "W";
                    default:
                        return null;
                }
            }
        });
    }

}
