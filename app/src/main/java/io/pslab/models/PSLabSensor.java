package io.pslab.models;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.pslab.R;
import io.pslab.activity.DataLoggerActivity;
import io.pslab.activity.MapsActivity;
import io.pslab.activity.SettingsActivity;
import io.pslab.others.MathUtils;
import io.pslab.others.SwipeGestureDetector;

/**
 * Created by Padmal on 10/20/18.
 */

public abstract class PSLabSensor extends AppCompatActivity {

    public boolean recordData = false;

    public final int MY_PERMISSIONS_REQUEST_STORAGE_FOR_MAPS = 102;

    public Toolbar sensorToolBar;

    public BottomSheetBehavior bottomSheetBehavior;
    public GestureDetector gestureDetector;

    @BindView(R.id.cl)
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
    @BindView(R.id.custom_dialog_additional_content)
    LinearLayout bottomSheetAdditionalContent;

    /**
     * Getting layout file distinct to each sensor
     *
     * @return Layout resource file in 'R.layout.id' format
     */
    public abstract int getLayout();

    /**
     * Getting menu layout distinct to each sensor
     *
     * @return Menu resource file in 'R.menu.id' format
     */
    public abstract int getMenu();

    /**
     * Getting toolbar layout distinct to each sensor
     *
     * @return Toolbar resource in 'R.id.id' format
     */
    public abstract int getSensorToolBar();

    /**
     * Getting saved setting configurations for dialogs
     *
     * @return SharedPreferences in Private mode
     */
    public abstract SharedPreferences getStateSettings();

    /**
     * Getting ID to fetch first time usage of each sensor
     *
     * @return String ID of the first time usage ID of sensor
     */
    public abstract String getFirstTimeSettingID();

    /**
     * Sensor ID
     *
     * @return String ID of the sensor
     */
    public abstract String getSensorName();

    /**
     * Title of the sensor guide
     *
     * @return Sensor name as a String resource
     */
    public abstract int getGuideTitle();

    /**
     * Abstract of the sensor guide
     *
     * @return Sensor abstract as a String resource
     */
    public abstract int getGuideAbstract();

    /**
     * Circuit diagrams and pin settings for the sensor
     *
     * @return Schematics as a drawable resource
     */
    public abstract int getGuideSchematics();

    /**
     * Description of the sensor guide
     *
     * @return Sensor guide description as a String resource
     */
    public abstract int getGuideDescription();

    /**
     * Extra content for a specific sensor if it is not a generic one
     *
     * @return Layout id of the content file
     */
    public abstract int getGuideExtraContent();

    /**
     * This method will be called upon when menu button for recording data has been clicked
     */
    public abstract void startRecordSensorData();

    /**
     * This method will be called upon when menu button for stop recording data has been clicked
     */
    public abstract void stopRecordSensorData();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayout());
        ButterKnife.bind(this);
        sensorToolBar = findViewById(getSensorToolBar());
        setSupportActionBar(sensorToolBar);
        setUpBottomSheet();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(getMenu(), menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.record_data);
        item.setIcon(recordData ? R.drawable.ic_record_stop_white : R.drawable.ic_record_white);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.record_data:
                if (recordData) {
                    startRecordSensorData();
                } else {
                    stopRecordSensorData();
                }
                recordData = !recordData;
                invalidateOptionsMenu();
                break;
            case R.id.show_map:
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_REQUEST_STORAGE_FOR_MAPS);
                    return true;
                }
                Intent MAP = new Intent(getApplicationContext(), MapsActivity.class);
                startActivity(MAP);
                break;
            case R.id.settings:
                Intent settingIntent = new Intent(this, SettingsActivity.class);
                settingIntent.putExtra("title", getSensorName());
                startActivity(settingIntent);
                break;
            case R.id.show_logged_data:
                Intent intent = new Intent(this, DataLoggerActivity.class);
                intent.putExtra(DataLoggerActivity.CALLER_ACTIVITY, getSensorName());
                startActivity(intent);
                break;
            case R.id.show_guide:
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_STORAGE_FOR_MAPS
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Intent MAP = new Intent(getApplicationContext(), MapsActivity.class);
            startActivity(MAP);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    /**
     * Configure the sensor guide with content and settings
     */
    private void setUpBottomSheet() {
        setupGuideLayout();
        handleFirstTimeUsage();
        handleBottomSheetBehavior();
        gestureDetector = new GestureDetector(this,
                new SwipeGestureDetector(bottomSheetBehavior));
    }

    /**
     * Handle sliding up and down behaviors and proper handling in closure.
     */
    private void handleBottomSheetBehavior() {
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            private Handler handler = new Handler();
            private Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
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
                Float value = (float) MathUtils.map((double) slideOffset, 0.0, 1.0, 0.0, 0.8);
                tvShadow.setVisibility(View.VISIBLE);
                tvShadow.setAlpha(value);
                arrowUpDown.setRotation(slideOffset * 180);
            }
        });
    }

    /**
     * Inflate each individual view with content to fill up the sensor guide
     */
    private void setupGuideLayout() {
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        bottomSheetGuideTitle.setText(getGuideTitle());
        bottomSheetText.setText(getGuideAbstract());
        bottomSheetSchematic.setImageResource(getGuideSchematics());
        bottomSheetDesc.setText(getGuideDescription());
        // If a sensor has extra content than provided in the standard layout, create a new layout
        // and attach the layout id with getGuideExtraContent()
        if (getGuideExtraContent() != 0) {
            LayoutInflater I = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
            assert I != null;
            View childLayout = I.inflate(getGuideExtraContent(), null);
            bottomSheetAdditionalContent.addView(childLayout);
        }
    }

    /**
     * Handle first time usage of sensor to show the guide at startup
     */
    private void handleFirstTimeUsage() {
        if (getStateSettings().getBoolean(getFirstTimeSettingID(), true)) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            tvShadow.setVisibility(View.VISIBLE);
            tvShadow.setAlpha(0.8f);
            arrowUpDown.setRotation(180);
            bottomSheetSlideText.setText(R.string.hide_guide_text);
            SharedPreferences.Editor editor = getStateSettings().edit();
            editor.putBoolean(getFirstTimeSettingID(), false);
            editor.apply();
        } else {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }
    }

}
