package org.fossasia.pslab.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.fossasia.pslab.R;
import org.fossasia.pslab.fragment.LuxMeterFragmentConfig;
import org.fossasia.pslab.fragment.LuxMeterFragmentData;
import org.fossasia.pslab.others.CustomSnackBar;
import org.fossasia.pslab.others.GPSLogger;
import org.fossasia.pslab.others.MathUtils;
import org.fossasia.pslab.others.SwipeGestureDetector;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LuxMeterActivity extends AppCompatActivity {
    BottomSheetBehavior bottomSheetBehavior;
    GestureDetector gestureDetector;
    private static final String PREF_NAME = "customDialogPreference";

    @BindView(R.id.navigation_lux_meter)
    BottomNavigationView bottomNavigationView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.cl)
    CoordinatorLayout coordinatorLayout;

    //bottomSheet
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

    public boolean saveData = false;
    public GPSLogger gpsLogger;
    private boolean checkGpsOnResume = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lux_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        setUpBottomSheet();
        bottomNavigationView.setOnNavigationItemSelectedListener
                (new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        Fragment selectedFragment = null;
                        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frame_layout_lux_meter);

                        switch (item.getItemId()) {
                            case R.id.action_data:
                                if (!(fragment instanceof LuxMeterFragmentData))
                                    selectedFragment = LuxMeterFragmentData.newInstance();
                                break;
                            case R.id.action_config:
                                if (!(fragment instanceof LuxMeterFragmentConfig))
                                    selectedFragment = LuxMeterFragmentConfig.newInstance();
                                break;
                            default:
                                break;
                        }
                        if (selectedFragment != null) {
                            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                            transaction.replace(R.id.frame_layout_lux_meter, selectedFragment, selectedFragment.getTag());
                            transaction.commit();
                        }
                        return true;
                    }
                });
        try {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            Fragment selectedFragment = LuxMeterFragmentData.newInstance();
            transaction.replace(R.id.frame_layout_lux_meter, selectedFragment, selectedFragment.getTag());
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setUpBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        final SharedPreferences settings = this.getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        Boolean isFirstTime = settings.getBoolean("LuxMeterFirstTime", true);

        bottomSheetGuideTitle.setText(R.string.lux_meter);
        bottomSheetText.setText(R.string.lux_meter_intro);
        bottomSheetSchematic.setImageResource(R.drawable.bh1750_schematic);
        bottomSheetDesc.setText(R.string.lux_meter_desc);

        if (isFirstTime) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            tvShadow.setAlpha(0.8f);
            arrowUpDown.setRotation(180);
            bottomSheetSlideText.setText(R.string.hide_guide_text);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("LuxMeterFirstTime", false);
            editor.apply();
        } else {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }

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
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                Float value = (float) MathUtils.map((double) slideOffset, 0.0, 1.0, 0.0, 0.8);
                tvShadow.setAlpha(value);
                arrowUpDown.setRotation(slideOffset * 180);
            }
        });
        gestureDetector = new GestureDetector(this, new SwipeGestureDetector(bottomSheetBehavior));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);                 //Gesture detector need this to transfer touch event to the gesture detector.
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.data_log_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        gpsLogger = new GPSLogger(this, (LocationManager) getSystemService(Context.LOCATION_SERVICE));
        switch (item.getItemId()) {
            case R.id.save_csv_data:
                if (saveData) {
                    saveData = false;
                } else {
                    if (gpsLogger.checkPermission()) {
                        if (gpsLogger.isGPSEnabled()) {
                            saveData = true;
                            CustomSnackBar.showSnackBar(coordinatorLayout, getString(R.string.data_recording_start), null, null);
                        } else {
                            checkGpsOnResume = true;
                        }
                        gpsLogger.startFetchingLocation();
                    }
                }
                invalidateOptionsMenu();
                break;
            case R.id.show_map:
                Intent MAP = new Intent(getApplicationContext(), MapsActivity.class);
                startActivity(MAP);
                break;
            case R.id.settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case GPSLogger.MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    GPSLogger.permissionAvailable = true;
                    if (gpsLogger.isGPSEnabled()) {
                        saveData = true;
                        CustomSnackBar.showSnackBar(coordinatorLayout, getString(R.string.data_recording_start), null, null);
                    } else {
                        checkGpsOnResume = true;
                    }
                    gpsLogger.startFetchingLocation();
                } else {
                    GPSLogger.permissionAvailable = false;
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkGpsOnResume) {
            if (gpsLogger.isGPSEnabled()) {
                saveData = true;
                CustomSnackBar.showSnackBar(coordinatorLayout, getString(R.string.data_recording_start), null, null);
            } else {
                saveData = false;
                Toast.makeText(getApplicationContext(), getString(R.string.gps_not_enabled),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}
