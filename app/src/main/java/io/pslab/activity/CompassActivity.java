package io.pslab.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.Date;

import io.pslab.R;
import io.pslab.models.CompassData;
import io.pslab.others.CSVLogger;
import io.pslab.others.CustomSnackBar;
import io.pslab.others.MathUtils;
import io.pslab.others.SwipeGestureDetector;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Harsh on 7/17/18.
 */

public class CompassActivity extends AppCompatActivity implements SensorEventListener {

    private static final String PREFS_NAME = "CompassPreference";
    public CSVLogger compassLogger = null;

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
    TextView zAxisMagneticField;

    @BindView(R.id.compass_toolbar)
    Toolbar mToolbar;
    @BindView(R.id.compass_coordinator_layout)
    CoordinatorLayout coordinatorLayout;

    @BindView(R.id.bottom_sheet)
    LinearLayout bottomSheet;
    @BindView(R.id.shadow)
    View tvShadow;
    @BindView(R.id.img_arrow)
    ImageView arrowUpDown;
    @BindView(R.id.sheet_slide_text)
    TextView bottomSheetSlideText;
    @BindView(R.id.guide_title)
    TextView bottomSheetGuideTitle;
    @BindView(R.id.custom_dialog_text)
    TextView bottomSheetText;
    @BindView(R.id.custom_dialog_schematic)
    ImageView bottomSheetSchematic;
    @BindView(R.id.custom_dialog_desc)
    TextView bottomSheetDesc;

    BottomSheetBehavior bottomSheetBehavior;
    GestureDetector gestureDetector;
    CompassData compassData = new CompassData();
    private SharedPreferences compassPreference;
    private float currentDegree = 0f;
    public Boolean writeHeaderToFile = true;
    private int direction; // 0 for X-axis, 1 for Y-axis and 2 for Z-axis
    private SensorManager mSensorManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compass_main);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        compassPreference = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        setUpBottomSheet();

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

        tvShadow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED)
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                tvShadow.setVisibility(View.GONE);
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
                degree = Math.round(event.values[0]);
                if (degree < 0)
                    degree += 360;
                break;
            case 1:
                degree = Math.round(event.values[1]);
                if (degree < 0)
                    degree += 360;
                break;
            case 2:
                degree = Math.round(event.values[2]);
                if (degree < 0)
                    degree += 360;
                break;
            default:
                degree = Math.round(event.values[0]);
                break;
        }

        setCompassAnimation(degree);

        degreeIndicator.setText(String.valueOf(degree));
        currentDegree = -degree;

        degree = Math.round(event.values[0]);
        if (degree < 0)
            degree += 360;
        compassData.setBx(String.valueOf(degree));
        xAxisMagneticField.setText(String.valueOf(degree));

        degree = Math.round(event.values[1]);
        if (degree < 0)
            degree += 360;
        compassData.setBy(String.valueOf(degree));
        yAxisMagneticField.setText(String.valueOf(degree));

        degree = Math.round(event.values[2]);
        if (degree < 0)
            degree += 360;
        compassData.setBz(String.valueOf(degree));
        zAxisMagneticField.setText(String.valueOf(degree));
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

    /**
     * Initiates bottom sheet to display guide on how to use Compass instrument
     */
    private void setUpBottomSheet() {

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        boolean isFirstTime = compassPreference.getBoolean("CompassFirstTime", true);

        if (isFirstTime) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            tvShadow.setVisibility(View.VISIBLE);
            tvShadow.setAlpha(0.8f);
            arrowUpDown.setRotation(180);
            bottomSheetSlideText.setText(R.string.hide_guide_text);
            SharedPreferences.Editor editor = compassPreference.edit();
            editor.putBoolean("CompassFirstTime", false);
            editor.apply();
        } else {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }

        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            private Handler handler = new Handler();
            private Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                    } catch (IllegalArgumentException e) {
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    }
                }
            };

            @Override
            public void onStateChanged(@NonNull final View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_EXPANDED:
                        handler.removeCallbacks(runnable);
                        bottomSheetSlideText.setText(R.string.hide_guide_text);
                        break;

                    case BottomSheetBehavior.STATE_COLLAPSED:
                        handler.postDelayed(runnable, 2000);
                        break;

                    default:
                        handler.removeCallbacks(runnable);
                        bottomSheetSlideText.setText(R.string.show_guide_text);
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                Float value = (float) MathUtils.map((double) slideOffset, 0.0, 1.0,
                        0.0, 0.8);
                tvShadow.setVisibility(View.VISIBLE);
                tvShadow.setAlpha(value);
                arrowUpDown.setRotation(slideOffset * 180);
            }
        });
        gestureDetector = new GestureDetector(this,
                new SwipeGestureDetector(bottomSheetBehavior));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_compass_help_menu, menu);
        return true;
    }

    private void recordData() {
        String dateTime = CSVLogger.FILE_NAME_FORMAT.format(new Date(System.currentTimeMillis()));
        compassLogger.writeCSVFile(System.currentTimeMillis() + "," + dateTime + "," + compassData.getBx()
                + "," + compassData.getBy() + "," + compassData.getBz());
        CustomSnackBar.showSnackBar(coordinatorLayout,
                getString(R.string.csv_store_text) + " " + compassLogger.getCurrentFilePath()
                , getString(R.string.delete_capital), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new AlertDialog.Builder(CompassActivity.this, R.style.AlertDialogStyle)
                                .setTitle(R.string.delete_file)
                                .setMessage(R.string.delete_warning)
                                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        compassLogger.deleteFile();
                                    }
                                })
                                .setNegativeButton(R.string.cancel, null)
                                .create()
                                .show();
                    }
                }, Snackbar.LENGTH_LONG);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.compass_help_icon:
                bottomSheetBehavior.setState(bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN ?
                        BottomSheetBehavior.STATE_EXPANDED : BottomSheetBehavior.STATE_HIDDEN);
                break;
            case R.id.compass_record_data:
                if (writeHeaderToFile) {
                    compassLogger = new CSVLogger(getString(R.string.compass));
                    compassLogger.prepareLogFile();
                    compassLogger.writeCSVFile("Timestamp,DateTime,Bx,By,Bz");
                    recordData();
                    writeHeaderToFile = !writeHeaderToFile;
                } else {
                    recordData();
                }
                break;
            case android.R.id.home:
                this.finish();
                break;
            default:
                break;
        }
        return true;
    }
}
