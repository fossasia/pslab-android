package org.fossasia.pslab.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.sdsmdg.harjot.crollerTest.Croller;
import com.sdsmdg.harjot.crollerTest.OnCrollerChangeListener;

import org.fossasia.pslab.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Abhinav Raj on 1/6/18.
 */

public class PowerSourceActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.power_card_pv1_controller)
    Croller controllerPV1;
    @BindView(R.id.power_card_pv2_controller)
    Croller controllerPV2;
    @BindView(R.id.power_card_pv3_controller)
    Croller controllerPV3;
    @BindView(R.id.power_card_pcs_controller)
    Croller controllerPCS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_power_source);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        monitorControllers(controllerPV1);
        monitorControllers(controllerPV2);
        monitorControllers(controllerPV3);
        monitorControllers(controllerPCS);
    }

    private void monitorControllers(Croller controller) {
        controller.setOnCrollerChangeListener(new OnCrollerChangeListener() {
            @Override
            public void onProgressChanged(Croller croller, int progress) {
                /* TODO: Use progress to output desired voltage */
            }

            @Override
            public void onStartTrackingTouch(Croller croller) {/**/}

            @Override
            public void onStopTrackingTouch(Croller croller) {
                /* TODO: Output desired voltage when this method is called */
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
