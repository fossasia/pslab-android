package org.fossasia.pslab.activity;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import org.fossasia.pslab.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Harsh on 7/17/18.
 */

public class CompassActivity extends AppCompatActivity implements SensorEventListener {
    @BindView(R.id.compass)
    ImageView compass;
    @BindView(R.id.degree_indicator)
    TextView degreeIndicator;

    @BindView(R.id.compass_radio_button_x_axis)
    RadioButton xAxisRadioButton;
    @BindView(R.id.compass_radio_button_y_axis)
    RadioButton yAxisRadioButton;
    @BindView(R.id.compass_radio_button_z_axis)
    RadioButton zAxisRadioButton;

    @BindView(R.id.tv_sensor_hmc5883l_bx)
    TextView xAxisMagneticField;
    @BindView(R.id.tv_sensor_hmc5883l_by)
    TextView yAxisMagneticField;
    @BindView(R.id.tv_sensor_hmc5883l_bz)
    TextView zAxismagneticField;

    private float currentDegree = 0f;
    private int direction; // 0 for X-axis, 1 for Y-axis and 2 for Z-axis
    private SensorManager mSensorManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compass);
        ButterKnife.bind(this);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        xAxisRadioButton.setChecked(true);
        direction = 0;

        xAxisRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                xAxisRadioButton.setChecked(true);
                yAxisRadioButton.setChecked(false);
                zAxisRadioButton.setChecked(false);
                direction = 0;
            }
        });

        yAxisRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                xAxisRadioButton.setChecked(false);
                yAxisRadioButton.setChecked(true);
                zAxisRadioButton.setChecked(false);
                direction = 1;
            }
        });

        zAxisRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                xAxisRadioButton.setChecked(false);
                yAxisRadioButton.setChecked(false);
                zAxisRadioButton.setChecked(true);
                direction = 2;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();

        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        float degree;
        switch (direction) {
            case 0:
                degree = Math.round(event.values[1]);
                break;
            case 1:
                degree = Math.round(event.values[2]);
                break;
            case 2:
                degree = Math.round(event.values[0]);
                break;
            default:
                degree = Math.round(event.values[1]);
                break;
        }

        setCompassAnimation(degree);

        degreeIndicator.setText(String.valueOf(degree));
        currentDegree = -degree;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // No use
    }

    /**
     * Sets rotational animation of compass image to provided degree angle
     *
     * @param degree Angle to which N-pole of compass should point
     */

    private void setCompassAnimation(float degree) {

        RotateAnimation ra = new RotateAnimation(
                currentDegree,
                -degree,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f);

        ra.setDuration(210);
        ra.setFillAfter(true);

        compass.startAnimation(ra);
    }

}
