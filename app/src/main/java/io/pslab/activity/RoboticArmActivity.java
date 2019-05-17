package io.pslab.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.goodiebag.protractorview.ProtractorView;

import io.pslab.R;

public class RoboticArmActivity extends AppCompatActivity {

    ProtractorView protractorView1, protractorView2, protractorView3, protractorView4;
    TextView degreeText1, degreeText2, degreeText3, degreeText4;
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

        protractorView1 = servo1Layout.findViewById(R.id.protractorView);
        protractorView2 = servo2Layout.findViewById(R.id.protractorView);
        protractorView3 = servo3Layout.findViewById(R.id.protractorView);
        protractorView4 = servo4Layout.findViewById(R.id.protractorView);
        degreeText1 = servo1Layout.findViewById(R.id.degreeText);
        degreeText2 = servo2Layout.findViewById(R.id.degreeText);
        degreeText3 = servo3Layout.findViewById(R.id.degreeText);
        degreeText4 = servo4Layout.findViewById(R.id.degreeText);
        protractorView1.setTickIntervals(15);
        protractorView2.setTickIntervals(15);
        protractorView3.setTickIntervals(15);
        protractorView4.setTickIntervals(15);

        protractorView1.setOnProtractorViewChangeListener(new ProtractorView.OnProtractorViewChangeListener() {
            @Override
            public void onProgressChanged(ProtractorView protractorView, int i, boolean b) {
                degreeText1.setText(String.valueOf(i));
            }

            @Override
            public void onStartTrackingTouch(ProtractorView protractorView) {

            }

            @Override
            public void onStopTrackingTouch(ProtractorView protractorView) {

            }
        });
        protractorView2.setOnProtractorViewChangeListener(new ProtractorView.OnProtractorViewChangeListener() {
            @Override
            public void onProgressChanged(ProtractorView protractorView, int i, boolean b) {
                degreeText2.setText(String.valueOf(i));
            }

            @Override
            public void onStartTrackingTouch(ProtractorView protractorView) {

            }

            @Override
            public void onStopTrackingTouch(ProtractorView protractorView) {

            }
        });
        protractorView3.setOnProtractorViewChangeListener(new ProtractorView.OnProtractorViewChangeListener() {
            @Override
            public void onProgressChanged(ProtractorView protractorView, int i, boolean b) {
                degreeText3.setText(String.valueOf(i));
            }

            @Override
            public void onStartTrackingTouch(ProtractorView protractorView) {

            }

            @Override
            public void onStopTrackingTouch(ProtractorView protractorView) {

            }
        });
        protractorView4.setOnProtractorViewChangeListener(new ProtractorView.OnProtractorViewChangeListener() {
            @Override
            public void onProgressChanged(ProtractorView protractorView, int i, boolean b) {
                degreeText4.setText(String.valueOf(i));
            }

            @Override
            public void onStartTrackingTouch(ProtractorView protractorView) {

            }

            @Override
            public void onStopTrackingTouch(ProtractorView protractorView) {

            }
        });
    }
}
