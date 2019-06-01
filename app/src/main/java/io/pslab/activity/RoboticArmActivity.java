package io.pslab.activity;

import android.graphics.Point;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.triggertrap.seekarc.SeekArc;

import io.pslab.R;

public class RoboticArmActivity extends AppCompatActivity {

    private EditText degreeText1, degreeText2, degreeText3, degreeText4;
    private SeekArc seekArc1, seekArc2, seekArc3, seekArc4;
    private LinearLayout servo1TimeLine, servo2TimeLine, servo3TimeLine, servo4TimeLine;
    private int degree;
    private boolean editEnter = false;
    private Button playButton, pauseButton, stopButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_robotic_arm);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int screen_width = size.x;
        int screen_height = size.y;

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
        playButton = findViewById(R.id.timeline_play_button);
        pauseButton = findViewById(R.id.timeline_pause_button);
        stopButton = findViewById(R.id.timeline_stop_button);
        LinearLayout timeLineControlsLayout = findViewById(R.id.servo_timeline_controls);

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

        LinearLayout.LayoutParams timeLineControlsParams = new LinearLayout.LayoutParams(screen_width / 15, screen_height / 2);
        timeLineControlsLayout.setLayoutParams(timeLineControlsParams);

        LinearLayout.LayoutParams servoTimeLineBoxParams = new LinearLayout.LayoutParams(screen_width / 6 - 2, screen_height / 8 - 2);
        servoTimeLineBoxParams.setMargins(2, 0, 0, 0);

        for (int i = 0; i < 60; i++) {
            RelativeLayout timeLineBox = (RelativeLayout) LayoutInflater.from(RoboticArmActivity.this).inflate(R.layout.robotic_arm_timeline_textview, null);
            timeLineBox.setLayoutParams(servoTimeLineBoxParams);
            timeLineBox.setPadding(5, 5, 5, 5);
            TextView timeText = timeLineBox.findViewById(R.id.timeline_box_time_text);
            timeText.setText(String.valueOf(i + 1));
            timeLineBox.setOnDragListener(servo1DragListener);
            servo1TimeLine.addView(timeLineBox, i);
        }

        for (int i = 0; i < 60; i++) {
            RelativeLayout timeLineBox = (RelativeLayout) LayoutInflater.from(RoboticArmActivity.this).inflate(R.layout.robotic_arm_timeline_textview, null);
            timeLineBox.setLayoutParams(servoTimeLineBoxParams);
            timeLineBox.setPadding(5, 5, 5, 5);
            TextView timeText = timeLineBox.findViewById(R.id.timeline_box_time_text);
            timeText.setText(String.valueOf(i + 1));
            timeLineBox.setOnDragListener(servo2DragListener);
            servo2TimeLine.addView(timeLineBox, i);
        }

        for (int i = 0; i < 60; i++) {
            RelativeLayout timeLineBox = (RelativeLayout) LayoutInflater.from(RoboticArmActivity.this).inflate(R.layout.robotic_arm_timeline_textview, null);
            timeLineBox.setLayoutParams(servoTimeLineBoxParams);
            timeLineBox.setPadding(5, 5, 5, 5);
            TextView timeText = timeLineBox.findViewById(R.id.timeline_box_time_text);
            timeText.setText(String.valueOf(i + 1));
            timeLineBox.setOnDragListener(servo3DragListener);
            servo3TimeLine.addView(timeLineBox, i);
        }

        for (int i = 0; i < 60; i++) {
            RelativeLayout timeLineBox = (RelativeLayout) LayoutInflater.from(RoboticArmActivity.this).inflate(R.layout.robotic_arm_timeline_textview, null);
            timeLineBox.setLayoutParams(servoTimeLineBoxParams);
            timeLineBox.setPadding(5, 5, 5, 5);
            TextView timeText = timeLineBox.findViewById(R.id.timeline_box_time_text);
            timeText.setText(String.valueOf(i + 1));
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
                    seekArc1.setProgress((int) (degree / 3.6));
                    editEnter = true;
                }
                return false;
            }
        });

        degreeText2.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                removeStatusBar();
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    degree = Integer.valueOf(degreeText2.getText().toString());
                    seekArc2.setProgress((int) (degree / 3.6));
                    editEnter = true;
                }
                return false;
            }
        });

        degreeText3.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                removeStatusBar();
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    degree = Integer.valueOf(degreeText3.getText().toString());
                    seekArc3.setProgress((int) (degree / 3.6));
                    editEnter = true;
                }
                return false;
            }
        });

        degreeText4.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                removeStatusBar();
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    degree = Integer.valueOf(degreeText4.getText().toString());
                    seekArc4.setProgress((int) (degree / 3.6));
                    editEnter = true;
                }
                return false;
            }
        });

        LinearLayout timeIndicatorLayout = findViewById(R.id.time_indicator);
        LinearLayout.LayoutParams timeIndicatorParams = new LinearLayout.LayoutParams(screen_width / 6 - 2, 12);
        timeIndicatorParams.setMargins(3, 0, 0, 0);
        timeIndicatorLayout.setLayoutParams(timeIndicatorParams);
    }

    private View.OnDragListener servo1DragListener = new View.OnDragListener() {
        @Override
        public boolean onDrag(View v, DragEvent event) {
            if (event.getAction() == DragEvent.ACTION_DRAG_ENTERED) {
                View view = (View) event.getLocalState();
                TextView text = view.findViewById(R.id.degreeText);
                if (view.getId() == R.id.servo_1) {
                    ((TextView) v.findViewById(R.id.timeline_box_degree_text)).setText(text.getText());
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
                    ((TextView) v.findViewById(R.id.timeline_box_degree_text)).setText(text.getText());
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
                    ((TextView) v.findViewById(R.id.timeline_box_degree_text)).setText(text.getText());
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
                    ((TextView) v.findViewById(R.id.timeline_box_degree_text)).setText(text.getText());
                }
            }
            return true;
        }
    };

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
}
