package org.fossasia.pslab.activity;

import android.content.SharedPreferences;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.fossasia.pslab.R;
import org.fossasia.pslab.fragment.LuxMeterFragmentConfig;
import org.fossasia.pslab.fragment.LuxMeterFragmentData;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lux_main);
        ButterKnife.bind(this);
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
}
