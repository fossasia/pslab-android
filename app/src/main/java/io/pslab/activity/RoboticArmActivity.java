package io.pslab.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.triggertrap.seekarc.SeekArc;

import io.pslab.R;

public class RoboticArmActivity extends AppCompatActivity {

    TextView degreeText1, degreeText2, degreeText3, degreeText4;
    SeekArc seekArc1, seekArc2, seekArc3, seekArc4;
    LinearLayout servo1TimeLine, servo2TimeLine, servo3TimeLine, servo4TimeLine;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_robotic_arm);

        View servo1Layout = findViewById(R.id.servo_1);
        View servo2Layout = findViewById(R.id.servo_2);
        View servo3Layout = findViewById(R.id.servo_3);
        View servo4Layout = findViewById(R.id.servo_4);

        servo1TimeLine = findViewById(R.id.servo1_timeline);
        servo2TimeLine = findViewById(R.id.servo2_timeline);
        servo3TimeLine = findViewById(R.id.servo3_timeline);
        servo4TimeLine = findViewById(R.id.servo4_timeline);
        TextView servo1Title = servo1Layout.findViewById(R.id.servo_title);
        servo1Title.setText(getResources().getString(R.string.servo1_title));

        TextView servo2Title = servo2Layout.findViewById(R.id.servo_title);
        servo2Title.setText(getResources().getString(R.string.servo2_title));

        TextView servo3Title = servo3Layout.findViewById(R.id.servo_title);
        servo3Title.setText(getResources().getString(R.string.servo3_title));

        TextView servo4Title = servo4Layout.findViewById(R.id.servo_title);
        servo4Title.setText(getResources().getString(R.string.servo4_title));

        degreeText1 = servo1Layout.findViewById(R.id.degreeText);
        degreeText2 = servo2Layout.findViewById(R.id.degreeText);
        degreeText3 = servo3Layout.findViewById(R.id.degreeText);
        degreeText4 = servo4Layout.findViewById(R.id.degreeText);
        seekArc1 = servo1Layout.findViewById(R.id.seek_arc);
        seekArc2 = servo2Layout.findViewById(R.id.seek_arc);
        seekArc3 = servo3Layout.findViewById(R.id.seek_arc);
        seekArc4 = servo4Layout.findViewById(R.id.seek_arc);

        seekArc1.setOnSeekArcChangeListener(new SeekArc.OnSeekArcChangeListener() {
            @Override
            public void onProgressChanged(SeekArc seekArc, int i, boolean b) {
                degreeText1.setText(String.valueOf(Math.round(i*3.6)));
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
                degreeText2.setText(String.valueOf(Math.round(i*3.6)));
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
                degreeText3.setText(String.valueOf(Math.round(i*3.6)));
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
                degreeText4.setText(String.valueOf(Math.round(i*3.6)));
            }

            @Override
            public void onStartTrackingTouch(SeekArc seekArc) {

            }

            @Override
            public void onStopTrackingTouch(SeekArc seekArc) {

            }
        });

        servo1Layout.findViewById(R.id.drag_text).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                View.DragShadowBuilder myShadow = new View.DragShadowBuilder(servo1Layout);
                v.startDrag(null,myShadow,servo1Layout,0);
                return true;
            }
        });

        servo1TimeLine.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                if (event.getAction() == DragEvent.ACTION_DRAG_ENTERED) {
                        View view = (View)event.getLocalState();
                        TextView text = view.findViewById(R.id.degreeText);
                        TextView new_text = (TextView) LayoutInflater.from(RoboticArmActivity.this).inflate(R.layout.robotic_arm_timeline_textview, null);
                        new_text.setText(text.getText());
                        if (view.getId() == R.id.servo_1) {
                            servo1TimeLine.addView(new_text, servo1TimeLine.getChildCount());
                        }
                }
                return true;
            }
        });

        servo2Layout.findViewById(R.id.drag_text).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                View.DragShadowBuilder myShadow = new View.DragShadowBuilder(servo2Layout);
                v.startDrag(null,myShadow,servo2Layout,0);
                return true;
            }
        });

        servo2TimeLine.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                if (event.getAction() == DragEvent.ACTION_DRAG_ENTERED) {
                    View view = (View)event.getLocalState();
                    TextView text = view.findViewById(R.id.degreeText);
                    TextView new_text = (TextView) LayoutInflater.from(RoboticArmActivity.this).inflate(R.layout.robotic_arm_timeline_textview, null);
                    new_text.setText(text.getText());
                    if (view.getId() == R.id.servo_2) {
                        servo2TimeLine.addView(new_text, servo2TimeLine.getChildCount());
                    }
                }
                return true;
            }
        });

        servo3Layout.findViewById(R.id.drag_text).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                View.DragShadowBuilder myShadow = new View.DragShadowBuilder(servo3Layout);
                v.startDrag(null,myShadow,servo3Layout,0);
                return true;
            }
        });

        servo3TimeLine.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                if (event.getAction() == DragEvent.ACTION_DRAG_ENTERED) {
                    View view = (View)event.getLocalState();
                    TextView text = view.findViewById(R.id.degreeText);
                    TextView new_text = (TextView) LayoutInflater.from(RoboticArmActivity.this).inflate(R.layout.robotic_arm_timeline_textview, null);
                    new_text.setText(text.getText());
                    if (view.getId() == R.id.servo_3) {
                        servo3TimeLine.addView(new_text, servo3TimeLine.getChildCount());
                    }
                }
                return true;
            }
        });

        servo4Layout.findViewById(R.id.drag_text).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                View.DragShadowBuilder myShadow = new View.DragShadowBuilder(servo4Layout);
                v.startDrag(null,myShadow,servo4Layout,0);
                return true;
            }
        });

        servo4TimeLine.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                if (event.getAction() == DragEvent.ACTION_DRAG_ENTERED) {
                    View view = (View)event.getLocalState();
                    TextView text = view.findViewById(R.id.degreeText);
                    TextView new_text = (TextView) LayoutInflater.from(RoboticArmActivity.this).inflate(R.layout.robotic_arm_timeline_textview, null);
                    new_text.setText(text.getText());
                    if (view.getId() == R.id.servo_4) {
                        servo4TimeLine.addView(new_text, servo4TimeLine.getChildCount());
                    }
                }
                return true;
            }
        });
    }
}
