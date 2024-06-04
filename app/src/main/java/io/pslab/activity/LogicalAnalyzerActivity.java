package io.pslab.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import io.pslab.R;
import io.pslab.activity.guide.GuideActivity;
import io.pslab.fragment.LALogicLinesFragment;
import io.pslab.models.LogicAnalyzerData;
import io.pslab.others.LocalDataLog;
import io.realm.RealmResults;

/**
 * Created by viveksb007 on 10/5/17.
 */

public class LogicalAnalyzerActivity extends GuideActivity {

    public static final String PREFS_NAME = "LogicAnalyzerPreference";

    private boolean isRunning = false;

    private final String KEY_LOG = "has_log";
    private final String DATA_BLOCK = "data_block";
    public boolean isPlayback = false;
    public RealmResults<LogicAnalyzerData> recordedLAData;
    private LALogicLinesFragment laLogicLinesFragment;

    public LogicalAnalyzerActivity() {
        super(R.layout.activity_logic_analyzer);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        removeStatusBar();

        laLogicLinesFragment = LALogicLinesFragment.newInstance(this);
        getSupportFragmentManager().beginTransaction().add(R.id.la_frame_layout, laLogicLinesFragment).commit();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.logical_analyzer);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
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
        final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        getWindow().getDecorView().setSystemUiVisibility(flags);
        final View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int i) {
                if ((i & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                    decorView.setSystemUiVisibility(flags);
                }
            }
        });
    }

    @SuppressLint("NewApi")
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
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
                toggleGuide();
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


}
