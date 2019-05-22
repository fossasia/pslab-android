package io.pslab.activity;

import android.graphics.Point;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.triggertrap.seekarc.SeekArc;

import io.pslab.R;

public class RoboticArmActivity extends AppCompatActivity {

    private TextView degreeText1, degreeText2, degreeText3, degreeText4;
    private SeekArc seekArc1, seekArc2, seekArc3, seekArc4;
    private LinearLayout servo1TimeLine, servo2TimeLine, servo3TimeLine, servo4TimeLine;

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
        LinearLayout timeLineControlsLayout = findViewById(R.id.servo_timeline_controls);

        LinearLayout.LayoutParams servoControllerParams = new LinearLayout.LayoutParams(screen_width / 4 - 4, screen_height / 2 - 4);
        servoControllerParams.setMargins(2, 5, 2, 0);
        servo1Layout.setLayoutParams(servoControllerParams);
        servo2Layout.setLayoutParams(servoControllerParams);
        servo3Layout.setLayoutParams(servoControllerParams);
        servo4Layout.setLayoutParams(servoControllerParams);

        LinearLayout.LayoutParams servoTimeLineParams = new LinearLayout.LayoutParams(screen_width - 4 - screen_width / 15, screen_height / 8 - 2);
        servoTimeLineParams.setMargins(2, 2, 2, 0);

        servo1TimeLine.setLayoutParams(servoTimeLineParams);
        servo2TimeLine.setLayoutParams(servoTimeLineParams);
        servo3TimeLine.setLayoutParams(servoTimeLineParams);
        servo4TimeLine.setLayoutParams(servoTimeLineParams);

        LinearLayout.LayoutParams timeLineControlsParams = new LinearLayout.LayoutParams(screen_width / 15, screen_height / 2);
        timeLineControlsLayout.setLayoutParams(timeLineControlsParams);

        TextView servo1Title = servo1Layout.findViewById(R.id.servo_title);
        servo1Title.setText(getResources().getString(R.string.servo1_title));

        TextView servo2Title = servo2Layout.findViewById(R.id.servo_title);
        servo2Title.setText(getResources().getString(R.string.servo2_title));

        TextView servo3Title = servo3Layout.findViewById(R.id.servo_title);
        servo3Title.setText(getResources().getString(R.string.servo3_title));

        TextView servo4Title = servo4Layout.findViewById(R.id.servo_title);
        servo4Title.setText(getResources().getString(R.string.servo4_title));


        seekArc1.setOnSeekArcChangeListener(new SeekArc.OnSeekArcChangeListener() {
            @Override
            public void onProgressChanged(SeekArc seekArc, int i, boolean b) {
                degreeText1.setText(String.valueOf(Math.round(i * 3.6)));
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
                degreeText2.setText(String.valueOf(Math.round(i * 3.6)));
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
                degreeText3.setText(String.valueOf(Math.round(i * 3.6)));
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
                degreeText4.setText(String.valueOf(Math.round(i * 3.6)));
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

        servo1TimeLine.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                if (event.getAction() == DragEvent.ACTION_DRAG_ENTERED) {
                    View view = (View) event.getLocalState();
                    TextView text = view.findViewById(R.id.degreeText);
                    TextView new_text = (TextView) LayoutInflater.from(RoboticArmActivity.this).inflate(R.layout.robotic_arm_timeline_textview, null);
                    LinearLayout.LayoutParams timeLineTextParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    timeLineTextParams.setMargins(5, 1, 1, 1);
                    new_text.setLayoutParams(timeLineTextParams);
                    new_text.setText(text.getText());
                    if (view.getId() == R.id.servo_1) {
                        servo1TimeLine.addView(new_text, servo1TimeLine.getChildCount());
                    }
                }
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

        servo2TimeLine.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                if (event.getAction() == DragEvent.ACTION_DRAG_ENTERED) {
                    View view = (View) event.getLocalState();
                    TextView text = view.findViewById(R.id.degreeText);
                    TextView new_text = (TextView) LayoutInflater.from(RoboticArmActivity.this).inflate(R.layout.robotic_arm_timeline_textview, null);
                    LinearLayout.LayoutParams timeLineTextParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    timeLineTextParams.setMargins(5, 1, 1, 1);
                    new_text.setLayoutParams(timeLineTextParams);
                    new_text.setText(text.getText());
                    if (view.getId() == R.id.servo_2) {
                        servo2TimeLine.addView(new_text, servo2TimeLine.getChildCount());
                    }
                }
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

        servo3TimeLine.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                if (event.getAction() == DragEvent.ACTION_DRAG_ENTERED) {
                    View view = (View) event.getLocalState();
                    TextView text = view.findViewById(R.id.degreeText);
                    TextView new_text = (TextView) LayoutInflater.from(RoboticArmActivity.this).inflate(R.layout.robotic_arm_timeline_textview, null);
                    LinearLayout.LayoutParams timeLineTextParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    timeLineTextParams.setMargins(5, 1, 1, 1);
                    new_text.setLayoutParams(timeLineTextParams);
                    new_text.setText(text.getText());
                    if (view.getId() == R.id.servo_3) {
                        servo3TimeLine.addView(new_text, servo3TimeLine.getChildCount());
                    }
                }
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

        servo4TimeLine.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                if (event.getAction() == DragEvent.ACTION_DRAG_ENTERED) {
                    View view = (View) event.getLocalState();
                    TextView text = view.findViewById(R.id.degreeText);
                    TextView new_text = (TextView) LayoutInflater.from(RoboticArmActivity.this).inflate(R.layout.robotic_arm_timeline_textview, null);
                    LinearLayout.LayoutParams timeLineTextParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    timeLineTextParams.setMargins(5, 1, 1, 1);
                    new_text.setLayoutParams(timeLineTextParams);
                    new_text.setText(text.getText());
                    if (view.getId() == R.id.servo_4) {
                        servo4TimeLine.addView(new_text, servo4TimeLine.getChildCount());
                    }
                }
                return true;
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
