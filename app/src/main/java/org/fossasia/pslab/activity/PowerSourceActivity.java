package org.fossasia.pslab.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.sdsmdg.harjot.crollerTest.Croller;
import com.sdsmdg.harjot.crollerTest.OnCrollerChangeListener;

import org.fossasia.pslab.R;
import org.fossasia.pslab.communication.ScienceLab;
import org.fossasia.pslab.items.SquareImageButton;
import org.fossasia.pslab.others.ScienceLabCommon;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Abhinav Raj on 1/6/18.
 */

public class PowerSourceActivity extends AppCompatActivity {

    public static final String POWER_PREFERENCES = "Power_Preferences";
    private SharedPreferences powerPreferences;

    private enum Pin {
        PV1, PV2, PV3, PCS;
    }

    private ScienceLab scienceLab = ScienceLabCommon.scienceLab;

    private final int CONTROLLER_MIN = 1;
    private final int PV1_CONTROLLER_MAX = 1001;
    private final int PV2_CONTROLLER_MAX = 661;
    private final int PV3_CONTROLLER_MAX = 331;
    private final int PCS_CONTROLLER_MAX = 331;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.power_card_pv1_controller)
    Croller controllerPV1;
    @BindView(R.id.power_card_pv1_display)
    TextView displayPV1;
    @BindView(R.id.power_card_pv1_up)
    SquareImageButton upPV1;
    @BindView(R.id.power_card_pv1_down)
    SquareImageButton downPV1;

    @BindView(R.id.power_card_pv2_controller)
    Croller controllerPV2;
    @BindView(R.id.power_card_pv2_display)
    TextView displayPV2;
    @BindView(R.id.power_card_pv2_up)
    SquareImageButton upPV2;
    @BindView(R.id.power_card_pv2_down)
    SquareImageButton downPV2;

    @BindView(R.id.power_card_pv3_controller)
    Croller controllerPV3;
    @BindView(R.id.power_card_pv3_display)
    TextView displayPV3;
    @BindView(R.id.power_card_pv3_up)
    SquareImageButton upPV3;
    @BindView(R.id.power_card_pv3_down)
    SquareImageButton downPV3;

    @BindView(R.id.power_card_pcs_controller)
    Croller controllerPCS;
    @BindView(R.id.power_card_pcs_display)
    TextView displayPCS;
    @BindView(R.id.power_card_pcs_up)
    SquareImageButton upPCS;
    @BindView(R.id.power_card_pcs_down)
    SquareImageButton downPCS;

    private float voltagePV1 = 0.00f, voltagePV2 = 0.00f, voltagePV3 = 0.00f, currentPCS = 0.00f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_power_source);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        powerPreferences = getSharedPreferences(POWER_PREFERENCES, MODE_PRIVATE);

        monitorControllers(controllerPV1, Pin.PV1, PV1_CONTROLLER_MAX);
        monitorControllers(controllerPV2, Pin.PV2, PV2_CONTROLLER_MAX);
        monitorControllers(controllerPV3, Pin.PV3, PV3_CONTROLLER_MAX);
        monitorControllers(controllerPCS, Pin.PCS, PCS_CONTROLLER_MAX);

        monitorVariations(upPV1, downPV1, Pin.PV1);
        monitorVariations(upPV2, downPV2, Pin.PV2);
        monitorVariations(upPV3, downPV3, Pin.PV3);
        monitorVariations(upPCS, downPCS, Pin.PCS);

        updateDisplay(displayPV1, limitDigits(mapProgressToPower(retrievePowerValues(Pin.PV1),
                PV1_CONTROLLER_MAX, 5.00f, -5.00f)), Pin.PV1);
        updateDisplay(displayPV2, limitDigits(mapProgressToPower(retrievePowerValues(Pin.PV2),
                PV2_CONTROLLER_MAX, 3.30f, -3.30f)), Pin.PV2);
        updateDisplay(displayPV3, limitDigits(mapProgressToPower(retrievePowerValues(Pin.PV3),
                PV3_CONTROLLER_MAX, 3.30f, 0.00f)), Pin.PV3);
        updateDisplay(displayPCS, limitDigits(mapProgressToPower(retrievePowerValues(Pin.PCS),
                PCS_CONTROLLER_MAX, 3.30f, 0.00f)), Pin.PCS);
    }

    private void monitorControllers(Croller controller, final Pin pin, int controllerLimit) {
        controller.setMax(controllerLimit);
        controller.setProgress(retrievePowerValues(pin));
        controller.setOnCrollerChangeListener(new OnCrollerChangeListener() {
            @Override
            public void onProgressChanged(Croller croller, int progress) {
                setMappedPower(pin, progress);
            }

            @Override
            public void onStartTrackingTouch(Croller croller) {/**/}

            @Override
            public void onStopTrackingTouch(Croller croller) {
                setPower(pin);
            }
        });

    }

    private void monitorVariations(SquareImageButton up, SquareImageButton down, final Pin pin) {
        up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                incrementValue(pin);
            }
        });
        down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                decrementValue(pin);
            }
        });
    }

    private void incrementValue(Pin pin) {
        switch (pin) {
            case PV1:
                if (voltagePV1 < 5.00f) {
                    voltagePV1 += 0.01f;
                    updateDisplay(displayPV1, voltagePV1, Pin.PV1);
                    updateController(controllerPV1, Pin.PV1);
                }
                break;
            case PV2:
                if (voltagePV2 < 3.30f) {
                    voltagePV2 += 0.01f;
                    updateDisplay(displayPV2, voltagePV2, Pin.PV2);
                    updateController(controllerPV2, Pin.PV2);
                }
                break;
            case PV3:
                if (voltagePV3 < 3.30f) {
                    voltagePV3 += 0.01f;
                    updateDisplay(displayPV3, voltagePV3, Pin.PV3);
                    updateController(controllerPV3, Pin.PV3);
                }
                break;
            case PCS:
                if (currentPCS < 3.30f) {
                    currentPCS += 0.01f;
                    updateDisplay(displayPCS, currentPCS, Pin.PCS);
                    updateController(controllerPCS, Pin.PCS);
                }
                break;
            default:
                break;
        }
    }

    private void decrementValue(Pin pin) {
        switch (pin) {
            case PV1:
                if (voltagePV1 > -5.00f) {
                    voltagePV1 -= 0.01f;
                    updateDisplay(displayPV1, voltagePV1, Pin.PV1);
                    updateController(controllerPV1, Pin.PV1);
                }
                break;
            case PV2:
                if (voltagePV2 > -3.30f) {
                    voltagePV2 -= 0.01f;
                    updateDisplay(displayPV2, voltagePV2, Pin.PV2);
                    updateController(controllerPV2, Pin.PV2);
                }
                break;
            case PV3:
                if (voltagePV3 > 0.00f) {
                    voltagePV3 -= 0.01f;
                    updateDisplay(displayPV3, voltagePV3, Pin.PV3);
                    updateController(controllerPV3, Pin.PV3);
                }
                break;
            case PCS:
                if (currentPCS > 0.00f) {
                    currentPCS -= 0.01f;
                    updateDisplay(displayPCS, currentPCS, Pin.PCS);
                    updateController(controllerPCS, Pin.PCS);
                }
                break;
            default:
                break;
        }
    }

    private void updateController(Croller controller, Pin pin) {
        switch (pin) {
            case PV1:
                controller.setProgress(mapPowerToProgress(voltagePV1, PV1_CONTROLLER_MAX,
                        5.00f, -5.00f));
                break;
            case PV2:
                controller.setProgress(mapPowerToProgress(voltagePV2, PV2_CONTROLLER_MAX,
                        3.30f, -3.30f));
                break;
            case PV3:
                controller.setProgress(mapPowerToProgress(voltagePV3, PV3_CONTROLLER_MAX,
                        3.30f, 0.00f));
                break;
            case PCS:
                controller.setProgress(mapPowerToProgress(currentPCS, PCS_CONTROLLER_MAX,
                        3.30f, 0.00f));
                break;
            default:
                break;
        }

    }

    private void updateDisplay(TextView display, float value, Pin pin) {
        String displayText = (value >= 0 ? "+" : "-").concat(String.format(Locale.getDefault(),
                "%.2f", Math.abs(value))).concat(pin.equals(Pin.PCS) ? " mA" : " V");
        display.setText(displayText);
        setPower(pin);
    }

    private void setMappedPower(Pin pin, int progress) {
        savePowerValues(pin, progress);
        switch (pin) {
            case PV1:
                voltagePV1 = limitDigits(mapProgressToPower(progress, PV1_CONTROLLER_MAX,
                        5.00f, -5.00f));
                updateDisplay(displayPV1, voltagePV1, pin);
                break;
            case PV2:
                voltagePV2 = limitDigits(mapProgressToPower(progress, PV2_CONTROLLER_MAX,
                        3.30f, -3.30f));
                updateDisplay(displayPV2, voltagePV2, pin);
                break;
            case PV3:
                voltagePV3 = limitDigits(mapProgressToPower(progress, PV3_CONTROLLER_MAX,
                        3.30f, 0.00f));
                updateDisplay(displayPV3, voltagePV3, pin);
                break;
            case PCS:
                currentPCS = limitDigits(mapProgressToPower(progress, PCS_CONTROLLER_MAX,
                        3.30f, 0.00f));
                updateDisplay(displayPCS, currentPCS, pin);
                break;
            default:
                break;
        }
    }

    private void setPower(Pin pin) {
        if (scienceLab.isConnected()) {
            switch (pin) {
                case PV1:
                    scienceLab.setPV1(voltagePV1);
                    break;
                case PV2:
                    scienceLab.setPV2(voltagePV2);
                    break;
                case PV3:
                    scienceLab.setPV3(voltagePV3);
                    break;
                case PCS:
                    scienceLab.setPCS(currentPCS);
                    break;
                default:
                    break;
            }
        }
    }

    private void savePowerValues(Pin pin, int power) {
        if (scienceLab.isConnected()) {
            SharedPreferences.Editor modifier = powerPreferences.edit();
            modifier.putInt(String.valueOf(pin), power);
            modifier.apply();
        }
    }

    private int retrievePowerValues(Pin pin) {
        // Detects and responds if user has unplugged the device and closed the app from system
        if (scienceLab.isConnected()) {
            return powerPreferences.getInt(String.valueOf(pin), 1);
        } else {
            powerPreferences.edit().clear().apply();
            return 1;
        }
    }

    private float mapProgressToPower(int progress, int CONTROLLER_MAX, float max, float min) {
        return (progress - CONTROLLER_MIN) * (max - min) /
                (CONTROLLER_MAX - CONTROLLER_MIN) + min;
    }

    private int mapPowerToProgress(float progress, int MAX, float max, float min) {
        return (int) (limitDigits((progress - min) * (MAX - CONTROLLER_MIN) /
                (max - min)) + CONTROLLER_MIN);
    }

    private float limitDigits(float number) {
        return Float.valueOf(String.format(Locale.getDefault(), "%.2f", number));
    }
}
