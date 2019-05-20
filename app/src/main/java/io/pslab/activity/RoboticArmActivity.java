package io.pslab.activity;

import android.content.ClipData;
import android.content.ClipDescription;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.DragEvent;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.triggertrap.seekarc.SeekArc;

import io.pslab.R;

public class RoboticArmActivity extends AppCompatActivity {

    TextView degreeText1, degreeText2, degreeText3, degreeText4;
    SeekArc seekArc1, seekArc2, seekArc3, seekArc4;
    private int _xDelta;
    private int _yDelta;
    private android.widget.RelativeLayout.LayoutParams layoutParams;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_robotic_arm);

        View servo1Layout = findViewById(R.id.servo_1);
        View servo2Layout = findViewById(R.id.servo_2);
        View servo3Layout = findViewById(R.id.servo_3);
        View servo4Layout = findViewById(R.id.servo_4);

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
//        seekArc1 = servo1Layout.findViewById(R.id.seek_arc);
//        seekArc2 = servo2Layout.findViewById(R.id.seek_arc);
//        seekArc3 = servo3Layout.findViewById(R.id.seek_arc);
//        seekArc4 = servo4Layout.findViewById(R.id.seek_arc);
//
//        seekArc1.setOnSeekArcChangeListener(new SeekArc.OnSeekArcChangeListener() {
//            @Override
//            public void onProgressChanged(SeekArc seekArc, int i, boolean b) {
//                degreeText1.setText(String.valueOf(Math.round(i*3.6)));
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekArc seekArc) {
//
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekArc seekArc) {
//
//            }
//        });
//
//        seekArc2.setOnSeekArcChangeListener(new SeekArc.OnSeekArcChangeListener() {
//            @Override
//            public void onProgressChanged(SeekArc seekArc, int i, boolean b) {
//                degreeText2.setText(String.valueOf(Math.round(i*3.6)));
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekArc seekArc) {
//
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekArc seekArc) {
//
//            }
//        });
//
//        seekArc3.setOnSeekArcChangeListener(new SeekArc.OnSeekArcChangeListener() {
//            @Override
//            public void onProgressChanged(SeekArc seekArc, int i, boolean b) {
//                degreeText3.setText(String.valueOf(Math.round(i*3.6)));
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekArc seekArc) {
//
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekArc seekArc) {
//
//            }
//        });
//
//        seekArc4.setOnSeekArcChangeListener(new SeekArc.OnSeekArcChangeListener() {
//            @Override
//            public void onProgressChanged(SeekArc seekArc, int i, boolean b) {
//                degreeText4.setText(String.valueOf(Math.round(i*3.6)));
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekArc seekArc) {
//
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekArc seekArc) {
//
//            }
//        });
        servo1Layout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    ClipData data = ClipData.newPlainText("", "");
                    View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
                    view.startDrag(data, shadowBuilder, view, 0);
                    view.setVisibility(View.INVISIBLE);
                    return true;
                }else {
                    return false;
                }
            }
        });
        servo1Layout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ClipData.Item item = new ClipData.Item((CharSequence)v.getTag());
                String[] mimeTypes = {ClipDescription.MIMETYPE_TEXT_PLAIN};

                ClipData dragData = new ClipData(v.getTag().toString(),mimeTypes, item);
                View.DragShadowBuilder myShadow = new View.DragShadowBuilder(servo1Layout);

                v.startDrag(dragData,myShadow,null,0);
                return true;
            }
        });
        servo1Layout.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                int action = event.getAction();
                switch (action) {
                    case DragEvent.ACTION_DRAG_STARTED:
                        layoutParams = (RelativeLayout.LayoutParams)v.getLayoutParams();
                        Log.d("drag", "Action is DragEvent.ACTION_DRAG_STARTED");

                        // Do nothing
                        break;

                    case DragEvent.ACTION_DRAG_ENTERED:
                        Log.d("drag", "Action is DragEvent.ACTION_DRAG_ENTERED");
                        int x_cord = (int) event.getX();
                        int y_cord = (int) event.getY();
                        break;

                    case DragEvent.ACTION_DRAG_EXITED :
                        Log.d("drag", "Action is DragEvent.ACTION_DRAG_EXITED");
                        x_cord = (int) event.getX();
                        y_cord = (int) event.getY();
                        layoutParams.leftMargin = x_cord;
                        layoutParams.topMargin = y_cord;
                        v.setLayoutParams(layoutParams);
                        break;

                    case DragEvent.ACTION_DRAG_LOCATION  :
                        Log.d("drag", "Action is DragEvent.ACTION_DRAG_LOCATION");
                        x_cord = (int) event.getX();
                        y_cord = (int) event.getY();
                        break;

                    case DragEvent.ACTION_DRAG_ENDED   :
                        Log.d("drag", "Action is DragEvent.ACTION_DRAG_ENDED");

                        // Do nothing
                        break;

                    case DragEvent.ACTION_DROP:
                        Log.d("drag", "ACTION_DROP event");
                        v.setVisibility(View.VISIBLE);
                        ((View) event.getLocalState()).setVisibility(View.VISIBLE);
                        // Do nothing
                        break;
                    default: break;
                }
                ((View) event.getLocalState()).setVisibility(View.VISIBLE);
                return true;
            }
        });
    }
}
