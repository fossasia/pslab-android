package io.pslab.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Display;
import android.view.DragEvent;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.triggertrap.seekarc.SeekArc;

import java.util.ArrayList;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.pslab.R;
import io.pslab.communication.ScienceLab;
import io.pslab.models.SensorDataBlock;
import io.pslab.models.ServoData;
import io.pslab.others.CSVLogger;
import io.pslab.others.CustomSnackBar;
import io.pslab.others.GPSLogger;
import io.pslab.others.LocalDataLog;
import io.pslab.others.MathUtils;
import io.pslab.others.ScienceLabCommon;
import io.pslab.others.SwipeGestureDetector;
import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;

public class RoboticArmActivity extends AppCompatActivity {

    private static final String PREF_NAME = "RoboticArmActivity";
    private EditText degreeText1, degreeText2, degreeText3, degreeText4;
    private SeekArc seekArc1, seekArc2, seekArc3, seekArc4;
    private LinearLayout servo1TimeLine, servo2TimeLine, servo3TimeLine, servo4TimeLine;
    private int degree;
    private boolean editEnter = false;
    private HorizontalScrollView scrollView;
    private CountDownTimer timeLine;
    private boolean isPlaying = false;
    private CSVLogger servoCSVLogger;
    private Realm realm;
    private GPSLogger gpsLogger;
    private RealmResults<ServoData> recordedServoData;
    private final String KEY_LOG = "has_log";
    private final String DATA_BLOCK = "data_block";
    private int timelinePosition = 0;
    private ScienceLab scienceLab;
    private BottomSheetBehavior bottomSheetBehavior;
    private GestureDetector gestureDetector;
    private LinearLayout timeIndicatorLayout;
    private LinearLayout.LayoutParams timeIndicatorParams;
    private MenuItem playMenu;
    @BindView(R.id.sheet_slide_text_robotic_arm)
    TextView bottomSheetSlideText;
    @BindView(R.id.parent_layout_robotic)
    View parentLayout;
    @BindView(R.id.bottom_sheet_robotic_arm)
    LinearLayout bottomSheet;
    @BindView(R.id.img_arrow_robotic_arm)
    ImageView arrowUpDown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_robotic_arm);
        ButterKnife.bind(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.robotic_arm);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        setUpBottomSheet();
        parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED)
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                parentLayout.setVisibility(View.GONE);
            }
        });

        scienceLab = ScienceLabCommon.scienceLab;
        if (!scienceLab.isConnected()) {
            Toast.makeText(this, getResources().getString(R.string.device_not_connected), Toast.LENGTH_SHORT).show();
        }
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        TypedValue tv = new TypedValue();
        int actionBarHeight = 0;
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
        }
        int screen_width = size.x;
        int screen_height = size.y - actionBarHeight;
        realm = LocalDataLog.with().getRealm();
        gpsLogger = new GPSLogger(this,
                (LocationManager) getSystemService(Context.LOCATION_SERVICE));

        View servo1Layout = findViewById(R.id.servo_1);
        View servo2Layout = findViewById(R.id.servo_2);
        View servo3Layout = findViewById(R.id.servo_3);
        View servo4Layout = findViewById(R.id.servo_4);
        degreeText1 = servo1Layout.findViewById(R.id.degreeText);
        degreeText2 = servo2Layout.findViewById(R.id.degreeText);
        degreeText3 = servo3Layout.findViewById(R.id.degreeText);
        degreeText4 = servo4Layout.findViewById(R.id.degreeText);
        seekArc1 = servo1Layout.findViewById(R.id.seek_arc);
        seekArc2 = servo2Layout.findViewById(R.id.seek_arc);
        seekArc3 = servo3Layout.findViewById(R.id.seek_arc);
        seekArc4 = servo4Layout.findViewById(R.id.seek_arc);
        servo1TimeLine = findViewById(R.id.servo1_timeline);
        servo2TimeLine = findViewById(R.id.servo2_timeline);
        servo3TimeLine = findViewById(R.id.servo3_timeline);
        servo4TimeLine = findViewById(R.id.servo4_timeline);
        scrollView = findViewById(R.id.horizontal_scroll_view);
        servoCSVLogger = new CSVLogger(getResources().getString(R.string.robotic_arm));

        degreeText1.setText(getResources().getString(R.string.zero));
        degreeText2.setText(getResources().getString(R.string.zero));
        degreeText3.setText(getResources().getString(R.string.zero));
        degreeText4.setText(getResources().getString(R.string.zero));

        LinearLayout.LayoutParams servoControllerParams = new LinearLayout.LayoutParams(screen_width / 4 - 4, screen_height / 2 - 4);
        servoControllerParams.setMargins(2, 5, 2, 0);
        servo1Layout.setLayoutParams(servoControllerParams);
        servo2Layout.setLayoutParams(servoControllerParams);
        servo3Layout.setLayoutParams(servoControllerParams);
        servo4Layout.setLayoutParams(servoControllerParams);

        LinearLayout.LayoutParams servoTimeLineParams = new LinearLayout.LayoutParams(screen_width * 10, screen_height / 8 - 3);
        servoTimeLineParams.setMargins(2, 0, 2, 4);

        servo1TimeLine.setLayoutParams(servoTimeLineParams);
        servo2TimeLine.setLayoutParams(servoTimeLineParams);
        servo3TimeLine.setLayoutParams(servoTimeLineParams);
        servo4TimeLine.setLayoutParams(servoTimeLineParams);

        LinearLayout.LayoutParams servoTimeLineBoxParams = new LinearLayout.LayoutParams(screen_width / 6 - 2, screen_height / 8 - 2);
        servoTimeLineBoxParams.setMargins(2, 0, 0, 0);

        for (int i = 0; i < 60; i++) {
            RelativeLayout timeLineBox = (RelativeLayout) LayoutInflater.from(RoboticArmActivity.this).inflate(R.layout.robotic_arm_timeline_textview, null);
            timeLineBox.setLayoutParams(servoTimeLineBoxParams);
            timeLineBox.setPadding(5, 5, 5, 5);
            TextView timeText = timeLineBox.findViewById(R.id.timeline_box_time_text);
            timeText.setText(String.valueOf(i + 1) + getResources().getString(R.string.robotic_arm_second_unit));
            timeLineBox.setOnDragListener(servo1DragListener);
            servo1TimeLine.addView(timeLineBox, i);
        }

        for (int i = 0; i < 60; i++) {
            RelativeLayout timeLineBox = (RelativeLayout) LayoutInflater.from(RoboticArmActivity.this).inflate(R.layout.robotic_arm_timeline_textview, null);
            timeLineBox.setLayoutParams(servoTimeLineBoxParams);
            timeLineBox.setPadding(5, 5, 5, 5);
            TextView timeText = timeLineBox.findViewById(R.id.timeline_box_time_text);
            timeText.setText(String.valueOf(i + 1) + getResources().getString(R.string.robotic_arm_second_unit));
            timeLineBox.setOnDragListener(servo2DragListener);
            servo2TimeLine.addView(timeLineBox, i);
        }

        for (int i = 0; i < 60; i++) {
            RelativeLayout timeLineBox = (RelativeLayout) LayoutInflater.from(RoboticArmActivity.this).inflate(R.layout.robotic_arm_timeline_textview, null);
            timeLineBox.setLayoutParams(servoTimeLineBoxParams);
            timeLineBox.setPadding(5, 5, 5, 5);
            TextView timeText = timeLineBox.findViewById(R.id.timeline_box_time_text);
            timeText.setText(String.valueOf(i + 1) + getResources().getString(R.string.robotic_arm_second_unit));
            timeLineBox.setOnDragListener(servo3DragListener);
            servo3TimeLine.addView(timeLineBox, i);
        }

        for (int i = 0; i < 60; i++) {
            RelativeLayout timeLineBox = (RelativeLayout) LayoutInflater.from(RoboticArmActivity.this).inflate(R.layout.robotic_arm_timeline_textview, null);
            timeLineBox.setLayoutParams(servoTimeLineBoxParams);
            timeLineBox.setPadding(5, 5, 5, 5);
            TextView timeText = timeLineBox.findViewById(R.id.timeline_box_time_text);
            timeText.setText(String.valueOf(i + 1) + getResources().getString(R.string.robotic_arm_second_unit));
            timeLineBox.setOnDragListener(servo4DragListener);
            servo4TimeLine.addView(timeLineBox, i);
        }

        TextView servo1Title = servo1Layout.findViewById(R.id.servo_title);
        servo1Title.setText(getResources().getString(R.string.servo1_title));

        TextView servo2Title = servo2Layout.findViewById(R.id.servo_title);
        servo2Title.setText(getResources().getString(R.string.servo2_title));

        TextView servo3Title = servo3Layout.findViewById(R.id.servo_title);
        servo3Title.setText(getResources().getString(R.string.servo3_title));

        TextView servo4Title = servo4Layout.findViewById(R.id.servo_title);
        servo4Title.setText(getResources().getString(R.string.servo4_title));

        removeStatusBar();

        seekArc1.setOnSeekArcChangeListener(new SeekArc.OnSeekArcChangeListener() {
            @Override
            public void onProgressChanged(SeekArc seekArc, int i, boolean b) {
                if (editEnter) {
                    degreeText1.setText(String.valueOf(degree));
                    editEnter = false;
                } else {
                    degreeText1.setText(String.valueOf((int) (i * 3.6)));
                }
                degreeText1.setCursorVisible(false);
            }

            @Override
            public void onStartTrackingTouch(SeekArc seekArc) {

            }

            @Override
            public void onStopTrackingTouch(SeekArc seekArc) {

            }
        });

        seekArc2.setOnSeekArcChangeListener(new SeekArc.OnSeekArcChangeListener() {
            @Override
            public void onProgressChanged(SeekArc seekArc, int i, boolean b) {
                if (editEnter) {
                    degreeText2.setText(String.valueOf(degree));
                    editEnter = false;
                } else {
                    degreeText2.setText(String.valueOf((int) (i * 3.6)));
                }
                degreeText2.setCursorVisible(false);
            }

            @Override
            public void onStartTrackingTouch(SeekArc seekArc) {

            }

            @Override
            public void onStopTrackingTouch(SeekArc seekArc) {

            }
        });

        seekArc3.setOnSeekArcChangeListener(new SeekArc.OnSeekArcChangeListener() {
            @Override
            public void onProgressChanged(SeekArc seekArc, int i, boolean b) {
                if (editEnter) {
                    degreeText3.setText(String.valueOf(degree));
                    editEnter = false;
                } else {
                    degreeText3.setText(String.valueOf((int) (i * 3.6)));
                }
                degreeText3.setCursorVisible(false);
            }

            @Override
            public void onStartTrackingTouch(SeekArc seekArc) {

            }

            @Override
            public void onStopTrackingTouch(SeekArc seekArc) {

            }
        });

        seekArc4.setOnSeekArcChangeListener(new SeekArc.OnSeekArcChangeListener() {
            @Override
            public void onProgressChanged(SeekArc seekArc, int i, boolean b) {
                if (editEnter) {
                    degreeText4.setText(String.valueOf(degree));
                    editEnter = false;
                } else {
                    degreeText4.setText(String.valueOf((int) (i * 3.6)));
                }
                degreeText4.setCursorVisible(false);
            }

            @Override
            public void onStartTrackingTouch(SeekArc seekArc) {

            }

            @Override
            public void onStopTrackingTouch(SeekArc seekArc) {

            }
        });

        servo1Layout.findViewById(R.id.drag_handle).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                View.DragShadowBuilder myShadow = new View.DragShadowBuilder(servo1Layout);
                v.startDrag(null, myShadow, servo1Layout, 0);
                return true;
            }
        });

        servo2Layout.findViewById(R.id.drag_handle).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                View.DragShadowBuilder myShadow = new View.DragShadowBuilder(servo2Layout);
                v.startDrag(null, myShadow, servo2Layout, 0);
                return true;
            }
        });

        servo3Layout.findViewById(R.id.drag_handle).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                View.DragShadowBuilder myShadow = new View.DragShadowBuilder(servo3Layout);
                v.startDrag(null, myShadow, servo3Layout, 0);
                return true;
            }
        });

        servo4Layout.findViewById(R.id.drag_handle).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                View.DragShadowBuilder myShadow = new View.DragShadowBuilder(servo4Layout);
                v.startDrag(null, myShadow, servo4Layout, 0);
                return true;
            }
        });

        degreeText1.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                removeStatusBar();
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    degree = Integer.valueOf(degreeText1.getText().toString());
                    if (degree > 360 || degree < 0) {
                        degreeText1.setText(getResources().getString(R.string.zero));
                        seekArc1.setProgress(0);
                        toastInvalidValueMessage();
                    } else {
                        seekArc1.setProgress((int) (degree / 3.6));
                        editEnter = true;
                    }
                }
                return false;
            }
        });

        degreeText1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                degreeText1.setCursorVisible(true);
            }
        });

        degreeText2.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                removeStatusBar();
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    degree = Integer.valueOf(degreeText2.getText().toString());
                    if (degree > 360 || degree < 0) {
                        degreeText2.setText(getResources().getString(R.string.zero));
                        seekArc2.setProgress(0);
                        toastInvalidValueMessage();
                    } else {
                        seekArc2.setProgress((int) (degree / 3.6));
                        editEnter = true;
                    }
                }
                return false;
            }
        });

        degreeText2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                degreeText2.setCursorVisible(true);
            }
        });

        degreeText3.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                removeStatusBar();
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    degree = Integer.valueOf(degreeText3.getText().toString());
                    if (degree > 360 || degree < 0) {
                        degreeText3.setText(getResources().getString(R.string.zero));
                        seekArc3.setProgress(0);
                        toastInvalidValueMessage();
                    } else {
                        seekArc3.setProgress((int) (degree / 3.6));
                        editEnter = true;
                    }
                }
                return false;
            }
        });

        degreeText3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                degreeText3.setCursorVisible(true);
            }
        });

        degreeText4.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                removeStatusBar();
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    degree = Integer.valueOf(degreeText4.getText().toString());
                    if (degree > 360 || degree < 0) {
                        degreeText4.setText(getResources().getString(R.string.zero));
                        seekArc4.setProgress(0);
                        toastInvalidValueMessage();
                    } else {
                        seekArc4.setProgress((int) (degree / 3.6));
                        editEnter = true;
                    }
                }
                return false;
            }
        });

        degreeText4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                degreeText4.setCursorVisible(true);
            }
        });

        timeIndicatorLayout = findViewById(R.id.time_indicator);
        timeIndicatorParams = new LinearLayout.LayoutParams(screen_width / 6 - 2, 12);
        timeIndicatorParams.setMarginStart(3);
        timeIndicatorLayout.setLayoutParams(timeIndicatorParams);

        timeLine = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeIndicatorParams.setMarginStart(timeIndicatorParams
                        .getMarginStart() + screen_width / 6);
                timeIndicatorLayout.setLayoutParams(timeIndicatorParams);
                scrollView.smoothScrollBy(screen_width / 6, 0);
                String deg1 = ((TextView) servo1TimeLine.getChildAt(timelinePosition).findViewById(R.id.timeline_box_degree_text)).getText().toString();
                deg1 = (deg1.length() > 0) ? deg1.substring(0, deg1.length() - 1) : getResources().getString(R.string.zero);
                String deg2 = ((TextView) servo2TimeLine.getChildAt(timelinePosition).findViewById(R.id.timeline_box_degree_text)).getText().toString();
                deg2 = (deg2.length() > 0) ? deg2.substring(0, deg2.length() - 1) : getResources().getString(R.string.zero);
                String deg3 = ((TextView) servo3TimeLine.getChildAt(timelinePosition).findViewById(R.id.timeline_box_degree_text)).getText().toString();
                deg3 = (deg3.length() > 0) ? deg3.substring(0, deg3.length() - 1) : getResources().getString(R.string.zero);
                String deg4 = ((TextView) servo4TimeLine.getChildAt(timelinePosition).findViewById(R.id.timeline_box_degree_text)).getText().toString();
                deg4 = (deg4.length() > 0) ? deg4.substring(0, deg4.length() - 1) : getResources().getString(R.string.zero);
                if (scienceLab.isConnected()) {
                    scienceLab.servo4(Double.valueOf(deg1), Double.valueOf(deg2), Double.valueOf(deg3), Double.valueOf(deg4));
                }
                timelinePosition++;
            }

            @Override
            public void onFinish() {
                timeIndicatorLayout.setLayoutParams(timeIndicatorParams);
                cancel();
            }
        };

        if (getIntent().getExtras() != null && getIntent().getExtras().getBoolean(KEY_LOG)) {
            recordedServoData = LocalDataLog.with()
                    .getBlockOfServoRecords(getIntent().getExtras().getLong(DATA_BLOCK));
            setReceivedData();
        }

    }

    private void setUpBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        final SharedPreferences settings = this.getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        Boolean isFirstTime = settings.getBoolean("RoboticArmFirstTime", true);

        if (isFirstTime) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            parentLayout.setVisibility(View.VISIBLE);
            parentLayout.setAlpha(0.8f);
            arrowUpDown.setRotation(180);
            bottomSheetSlideText.setText(R.string.hide_guide_text);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("RoboticArmFirstTime", false);
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
                parentLayout.setVisibility(View.VISIBLE);
                parentLayout.setAlpha(value);
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

    private void toastInvalidValueMessage() {
        Toast.makeText(RoboticArmActivity.this, getResources().getString(R.string.invalid_servo_value), Toast.LENGTH_SHORT).show();
    }

    private void setReceivedData() {
        ArrayList servoDataList = new ArrayList(recordedServoData);
        for (int i = 0; i < servoDataList.size(); i++) {
            ServoData servoData = (ServoData) servoDataList.get(i);
            ((TextView) servo1TimeLine.getChildAt(i).findViewById(R.id.timeline_box_degree_text)).setText(servoData.getDegree1() + getResources().getString(R.string.robotic_arm_degree_symbol));
            ((TextView) servo2TimeLine.getChildAt(i).findViewById(R.id.timeline_box_degree_text)).setText(servoData.getDegree2() + getResources().getString(R.string.robotic_arm_degree_symbol));
            ((TextView) servo3TimeLine.getChildAt(i).findViewById(R.id.timeline_box_degree_text)).setText(servoData.getDegree3() + getResources().getString(R.string.robotic_arm_degree_symbol));
            ((TextView) servo4TimeLine.getChildAt(i).findViewById(R.id.timeline_box_degree_text)).setText(servoData.getDegree4() + getResources().getString(R.string.robotic_arm_degree_symbol));
        }
    }

    private void saveTimeline() {
        long block = System.currentTimeMillis();
        servoCSVLogger.prepareLogFile();
        servoCSVLogger.writeMetaData(getResources().getString(R.string.robotic_arm));
        String data = "Timestamp,DateTime,Servo1,Servo2,Servo3,Servo4,Latitude,Longitude\n";
        long timestamp;
        recordSensorDataBlockID(new SensorDataBlock(block, getString(R.string.robotic_arm)));
        String degree1, degree2, degree3, degree4;
        double lat, lon;
        for (int i = 0; i < 60; i++) {
            timestamp = System.currentTimeMillis();
            degree1 = degree2 = degree3 = degree4 = getResources().getString(R.string.zero);
            if (((TextView) servo1TimeLine.getChildAt(i).findViewById(R.id.timeline_box_degree_text)).getText().length() > 0) {
                degree1 = ((TextView) servo1TimeLine.getChildAt(i).findViewById(R.id.timeline_box_degree_text)).getText().toString();
                degree1 = degree1.substring(0, degree1.length() - 1);
            }
            if (((TextView) servo2TimeLine.getChildAt(i).findViewById(R.id.timeline_box_degree_text)).getText().length() > 0) {
                degree2 = ((TextView) servo2TimeLine.getChildAt(i).findViewById(R.id.timeline_box_degree_text)).getText().toString();
                degree2 = degree2.substring(0, degree2.length() - 1);
            }
            if (((TextView) servo3TimeLine.getChildAt(i).findViewById(R.id.timeline_box_degree_text)).getText().length() > 0) {
                degree3 = ((TextView) servo3TimeLine.getChildAt(i).findViewById(R.id.timeline_box_degree_text)).getText().toString();
                degree3 = degree3.substring(0, degree3.length() - 1);
            }
            if (((TextView) servo4TimeLine.getChildAt(i).findViewById(R.id.timeline_box_degree_text)).getText().length() > 0) {
                degree4 = ((TextView) servo4TimeLine.getChildAt(i).findViewById(R.id.timeline_box_degree_text)).getText().toString();
                degree4 = degree4.substring(0, degree4.length() - 1);
            }
            if (gpsLogger.isGPSEnabled()) {
                Location location = gpsLogger.getDeviceLocation();
                if (location != null) {
                    lat = location.getLatitude();
                    lon = location.getLongitude();
                } else {
                    lat = 0.0;
                    lon = 0.0;
                }
            } else {
                lat = 0.0;
                lon = 0.0;
            }
            recordSensorData(new ServoData(timestamp, block, degree1, degree2, degree3, degree4, lat, lon));
            if (i == 59) {
                data += timestamp + "," + CSVLogger.FILE_NAME_FORMAT.format(new Date(timestamp)) + "," + degree1 + "," + degree2 + "," + degree3 + "," + degree4 + "," + lat + "," + lon;
            } else {
                data += timestamp + "," + CSVLogger.FILE_NAME_FORMAT.format(new Date(timestamp)) + "," + degree1 + "," + degree2 + "," + degree3 + "," + degree4 + "," + lat + "," + lon + "\n";
            }
        }
        servoCSVLogger.writeCSVFile(data);
        CustomSnackBar.showSnackBar(findViewById(R.id.robotic_arm_coordinator),
                getString(R.string.csv_store_text) + " " + servoCSVLogger.getCurrentFilePath()
                , getString(R.string.open), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(RoboticArmActivity.this, DataLoggerActivity.class);
                        intent.putExtra(DataLoggerActivity.CALLER_ACTIVITY, getResources().getString(R.string.robotic_arm));
                        startActivity(intent);
                    }
                }, Snackbar.LENGTH_SHORT);
    }

    private View.OnDragListener servo1DragListener = new View.OnDragListener() {
        @Override
        public boolean onDrag(View v, DragEvent event) {
            if (event.getAction() == DragEvent.ACTION_DRAG_ENTERED) {
                View view = (View) event.getLocalState();
                TextView text = view.findViewById(R.id.degreeText);
                if (view.getId() == R.id.servo_1) {
                    ((TextView) v.findViewById(R.id.timeline_box_degree_text)).setText(text.getText().toString());
                }
            }
            return true;
        }
    };
    private View.OnDragListener servo2DragListener = new View.OnDragListener() {
        @Override
        public boolean onDrag(View v, DragEvent event) {
            if (event.getAction() == DragEvent.ACTION_DRAG_ENTERED) {
                View view = (View) event.getLocalState();
                TextView text = view.findViewById(R.id.degreeText);
                if (view.getId() == R.id.servo_2) {
                    ((TextView) v.findViewById(R.id.timeline_box_degree_text)).setText(text.getText().toString());
                }
            }
            return true;
        }
    };
    private View.OnDragListener servo3DragListener = new View.OnDragListener() {
        @Override
        public boolean onDrag(View v, DragEvent event) {
            if (event.getAction() == DragEvent.ACTION_DRAG_ENTERED) {
                View view = (View) event.getLocalState();
                TextView text = view.findViewById(R.id.degreeText);
                if (view.getId() == R.id.servo_3) {
                    ((TextView) v.findViewById(R.id.timeline_box_degree_text)).setText(text.getText().toString());
                }
            }
            return true;
        }
    };
    private View.OnDragListener servo4DragListener = new View.OnDragListener() {
        @Override
        public boolean onDrag(View v, DragEvent event) {
            if (event.getAction() == DragEvent.ACTION_DRAG_ENTERED) {
                View view = (View) event.getLocalState();
                TextView text = view.findViewById(R.id.degreeText);
                if (view.getId() == R.id.servo_4) {
                    ((TextView) v.findViewById(R.id.timeline_box_degree_text)).setText(text.getText().toString());
                }
            }
            return true;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.robotic_arm_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        playMenu = menu.findItem(R.id.play_data);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.play_data:
                if (isPlaying) {
                    isPlaying = false;
                    item.setIcon(getResources().getDrawable(R.drawable.ic_play_arrow_white_24dp));
                    timeLine.onFinish();
                } else {
                    isPlaying = true;
                    item.setIcon(getResources().getDrawable(R.drawable.ic_pause_white_24dp));
                    timeLine.start();
                }
                break;
            case R.id.stop_data:
                timeLine.cancel();
                timeIndicatorParams.setMarginStart(3);
                timeIndicatorLayout.setLayoutParams(timeIndicatorParams);
                scrollView.fullScroll(HorizontalScrollView.FOCUS_LEFT);
                isPlaying = false;
                playMenu.setIcon(getResources().getDrawable(R.drawable.ic_play_arrow_white_24dp));
                timelinePosition = 0;
                break;
            case R.id.show_guide:
                bottomSheetBehavior.setState(bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN ?
                        BottomSheetBehavior.STATE_EXPANDED : BottomSheetBehavior.STATE_HIDDEN);
                break;
            case R.id.show_logged_data:
                Intent intent = new Intent(RoboticArmActivity.this, DataLoggerActivity.class);
                intent.putExtra(DataLoggerActivity.CALLER_ACTIVITY, getResources().getString(R.string.robotic_arm));
                startActivity(intent);
                break;
            case R.id.save_data:
                saveTimeline();
                break;
            default:
                break;
        }
        return true;
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
        } else {
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

    public void recordSensorDataBlockID(SensorDataBlock block) {
        realm.beginTransaction();
        realm.copyToRealm(block);
        realm.commitTransaction();
    }

    public void recordSensorData(RealmObject sensorData) {
        realm.beginTransaction();
        realm.copyToRealm((ServoData) sensorData);
        realm.commitTransaction();
    }
}
