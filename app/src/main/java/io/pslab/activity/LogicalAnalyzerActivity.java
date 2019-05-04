package io.pslab.activity;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import io.pslab.R;
import io.pslab.communication.ScienceLab;
import io.pslab.fragment.LALogicLinesFragment;
import io.pslab.others.MathUtils;
import io.pslab.others.ScienceLabCommon;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.pslab.others.SwipeGestureDetector;

/**
 * Created by viveksb007 on 10/5/17.
 */

public class LogicalAnalyzerActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "LogicAnalyzerPreference";

    @BindView(R.id.logical_analyzer_toolbar)
    Toolbar toolbar;
    private ScienceLab scienceLab;
    private boolean isRunning = false;

    //Bottom Sheet
    private LinearLayout bottomSheet;
    private View tvShadow;
    private ImageView arrowUpDown;
    private TextView bottomSheetSlideText;
    private TextView bottomSheetGuideTitle;
    private TextView bottomSheetText;
    private ImageView bottomSheetSchematic;
    private TextView bottomSheetDesc;
    private BottomSheetBehavior bottomSheetBehavior;
    private GestureDetector gestureDetector;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_logic_analyzer);
        scienceLab = ScienceLabCommon.scienceLab;
        ButterKnife.bind(this);

        // Bottom Sheet guide
        bottomSheet = findViewById(R.id.bottom_sheet);
        tvShadow = findViewById(R.id.shadow);
        arrowUpDown = findViewById(R.id.img_arrow);
        bottomSheetSlideText = findViewById(R.id.sheet_slide_text);
        bottomSheetGuideTitle = findViewById(R.id.guide_title);
        bottomSheetText = findViewById(R.id.custom_dialog_text);
        bottomSheetSchematic = findViewById(R.id.custom_dialog_schematic);
        bottomSheetDesc = findViewById(R.id.custom_dialog_desc);

        // Inflating bottom sheet dialog on how to use Logic Analyzer
        setUpBottomSheet();
        tvShadow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bottomSheetBehavior.getState()==BottomSheetBehavior.STATE_EXPANDED)
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                tvShadow.setVisibility(View.GONE);
            }
        });

        removeStatusBar();

        getSupportFragmentManager().beginTransaction().add(R.id.la_frame_layout, LALogicLinesFragment.newInstance(this)).commit();
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        ImageView guideImageView = findViewById(R.id.logic_analyzer_guide_button);
        guideImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetBehavior.setState(bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN ?
                        BottomSheetBehavior.STATE_EXPANDED : BottomSheetBehavior.STATE_HIDDEN);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        removeStatusBar();
    }
    private void removeStatusBar() {
        if (Build.VERSION.SDK_INT < 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        else {
            View decorView = getWindow().getDecorView();

            decorView.setSystemUiVisibility((View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY));
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!isRunning)
            finish();
    }

    public void setStatus(boolean status) {
        isRunning = status;
    }


    /**
     * Sets the bottom sheet for Logic Analyzer on how to use the instrument
     */

    private void setUpBottomSheet() {

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        final SharedPreferences settings = getApplicationContext().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        Boolean isFirstTime = settings.getBoolean("LogicAnalyzerFirstTime", true);

        bottomSheetGuideTitle.setText(R.string.logical_analyzer);
        bottomSheetText.setText(R.string.logic_analyzer_dialog_text);
        bottomSheetSchematic.setImageResource(R.drawable.logic_analyzer_circuit);
        bottomSheetDesc.setText(R.string.logic_analyzer_dialog_description);

        if (isFirstTime) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            tvShadow.setVisibility(View.VISIBLE);
            tvShadow.setAlpha(0.8f);
            arrowUpDown.setRotation(180);
            bottomSheetSlideText.setText(R.string.hide_guide_text);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("LogicAnalyzerFirstTime", false);
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

        gestureDetector = new GestureDetector(this, new SwipeGestureDetector(bottomSheetBehavior));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);  //Gesture detector need this to transfer touch event to the gesture detector.
        return super.onTouchEvent(event);
    }

}
