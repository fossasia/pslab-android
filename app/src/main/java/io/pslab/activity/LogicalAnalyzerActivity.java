package io.pslab.activity;

import android.content.Intent;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import io.pslab.R;
import io.pslab.fragment.LALogicLinesFragment;
import io.pslab.models.LogicAnalyzerData;
import io.pslab.others.LocalDataLog;
import io.pslab.others.MathUtils;

import io.pslab.others.SwipeGestureDetector;
import io.realm.RealmResults;

/**
 * Created by viveksb007 on 10/5/17.
 */

public class LogicalAnalyzerActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "LogicAnalyzerPreference";

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
    private final String KEY_LOG = "has_log";
    private final String DATA_BLOCK = "data_block";
    public boolean isPlayback = false;
    public RealmResults<LogicAnalyzerData> recordedLAData;
    private LALogicLinesFragment laLogicLinesFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_logic_analyzer);

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

        laLogicLinesFragment = LALogicLinesFragment.newInstance(this);
        getSupportFragmentManager().beginTransaction().add(R.id.la_frame_layout, laLogicLinesFragment).commit();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.logical_analyzer);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        if (getIntent().getExtras() != null && getIntent().getExtras().getBoolean(KEY_LOG)) {
            recordedLAData = LocalDataLog.with()
                    .getBlockOfLARecords(getIntent().getExtras().getLong(DATA_BLOCK));
            isPlayback = true;
        }
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.logical_analyzer_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.show_logged_data:
                Intent intent = new Intent(LogicalAnalyzerActivity.this, DataLoggerActivity.class);
                intent.putExtra(DataLoggerActivity.CALLER_ACTIVITY, getResources().getString(R.string.logical_analyzer));
                startActivity(intent);
                break;
            case R.id.save_graph:
                laLogicLinesFragment.logData();
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
