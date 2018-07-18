package org.fossasia.pslab.activity;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.sdsmdg.harjot.crollerTest.Croller;
import com.sdsmdg.harjot.crollerTest.OnCrollerChangeListener;

import org.fossasia.pslab.R;
import org.fossasia.pslab.communication.ScienceLab;
import org.fossasia.pslab.items.SquareImageButton;
import org.fossasia.pslab.others.ScienceLabCommon;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PowerSourceActivity extends AppCompatActivity {

    public static final String POWER_PREFERENCES = "Power_Preferences";

    private final int CONTROLLER_MIN = 1;
    private final int PV1_CONTROLLER_MAX = 1001;
    private final int PV2_CONTROLLER_MAX = 661;
    private final int PV3_CONTROLLER_MAX = 331;
    private final int PCS_CONTROLLER_MAX = 331;
    
    private final long LONG_CLICK_DELAY = 100;

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

    private SharedPreferences powerPreferences;
    private boolean isRunning = false;
    private boolean incrementPower = false, decrementPower = false;

    private ScienceLab scienceLab = ScienceLabCommon.scienceLab;

    private Timer powerCounter;
    private Handler powerHandler = new Handler();

    private float voltagePV1 = 0.00f, voltagePV2 = 0.00f, voltagePV3 = 0.00f, currentPCS = 0.00f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_power_source);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        powerPreferences = getSharedPreferences(POWER_PREFERENCES, MODE_PRIVATE);

        // Autosize the voltage display in textView to utilize empty space
        TextViewCompat.setAutoSizeTextTypeWithDefaults(displayPV1, TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM);
        TextViewCompat.setAutoSizeTextTypeWithDefaults(displayPV2, TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM);
        TextViewCompat.setAutoSizeTextTypeWithDefaults(displayPV3, TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM);
        TextViewCompat.setAutoSizeTextTypeWithDefaults(displayPCS, TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM);

        monitorControllers(controllerPV1, Pin.PV1, PV1_CONTROLLER_MAX);
        monitorControllers(controllerPV2, Pin.PV2, PV2_CONTROLLER_MAX);
        monitorControllers(controllerPV3, Pin.PV3, PV3_CONTROLLER_MAX);
        monitorControllers(controllerPCS, Pin.PCS, PCS_CONTROLLER_MAX);

        monitorVariations(upPV1, downPV1, Pin.PV1);
        monitorVariations(upPV2, downPV2, Pin.PV2);
        monitorVariations(upPV3, downPV3, Pin.PV3);
        monitorVariations(upPCS, downPCS, Pin.PCS);

        monitorLongClicks(upPV1, downPV1);
        monitorLongClicks(upPV2, downPV2);
        monitorLongClicks(upPV3, downPV3);
        monitorLongClicks(upPCS, downPCS);

        updateDisplay(displayPV1, limitDigits(mapProgressToPower(retrievePowerValues(Pin.PV1),
                PV1_CONTROLLER_MAX, 5.00f, -5.00f)), Pin.PV1);
        updateDisplay(displayPV2, limitDigits(mapProgressToPower(retrievePowerValues(Pin.PV2),
                PV2_CONTROLLER_MAX, 3.30f, -3.30f)), Pin.PV2);
        updateDisplay(displayPV3, limitDigits(mapProgressToPower(retrievePowerValues(Pin.PV3),
                PV3_CONTROLLER_MAX, 3.30f, 0.00f)), Pin.PV3);
        updateDisplay(displayPCS, limitDigits(mapProgressToPower(retrievePowerValues(Pin.PCS),
                PCS_CONTROLLER_MAX, 3.30f, 0.00f)), Pin.PCS);
    }

    /**
     * Initiates and sets up power knob controller
     *
     * @param controller      assigned knob
     * @param pin             assigned power pin
     * @param controllerLimit maximum value the knob can handle
     */
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

    /**
     * Click listeners to increment and decrement buttons
     *
     * @param up   increment button
     * @param down decrement button
     * @param pin  assigned power pin
     */
    private void monitorVariations(SquareImageButton up, SquareImageButton down, final Pin pin) {
        up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                incrementValue(pin);
            }
        });
        up.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(final View view) {
                if (!isRunning) {
                    isRunning = true;
                    incrementPower = true;
                    fastCounter(pin);
                }
                return true;
            }
        });
        down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                decrementValue(pin);
            }
        });
        down.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(final View view) {
                if (!isRunning) {
                    isRunning = true;
                    decrementPower = true;
                    fastCounter(pin);
                }
                return true;
            }
        });
    }

    /**
     * Handles action when user releases long click on an increment or a decrement button
     *
     * @param up   increment button
     * @param down decrement button
     */
    private void monitorLongClicks(SquareImageButton up, SquareImageButton down) {
        up.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                view.onTouchEvent(motionEvent);
                if (motionEvent.getAction() == MotionEvent.ACTION_UP && incrementPower) {
                    if (isRunning) {
                        isRunning = false;
                        stopCounter();
                        incrementPower = false;
                    }
                }
                return true;
            }
        });
        down.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                view.onTouchEvent(motionEvent);
                if (motionEvent.getAction() == MotionEvent.ACTION_UP && decrementPower) {
                    if (isRunning) {
                        isRunning = false;
                        stopCounter();
                        decrementPower = false;
                    }
                }
                return true;
            }
        });
    }

    /**
     * Increase power value by a fraction of hundreds
     *
     * @param pin assigned power pin
     */
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

    /**
     * Decrease power value by a fraction of hundreds
     *
     * @param pin assigned power pin
     */
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

    /**
     * Rotate power knob to the correct position determined by the numerical power value
     *
     * @param controller assigned knob
     * @param pin        assigned power pin
     */
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

    /**
     * Updates display with user set values and issue commands to PSLab device to output power
     *
     * @param display text view corresponding to power values
     * @param value   signed power value
     * @param pin     assigned power pin
     */
    private void updateDisplay(TextView display, float value, Pin pin) {
        String displayText = (value >= 0 ? "+" : "-").concat(String.format(Locale.getDefault(),
                "%.2f", Math.abs(value))).concat(pin.equals(Pin.PCS) ? " mA" : " V");
        display.setText(displayText);
        setPower(pin);
    }

    /**
     * Updates display and calculate power value determined by knob position
     *
     * @param pin      assigned power pin
     * @param progress corresponding progress value
     */
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

    /**
     * Output the power values set by user when the PSLab device is connected
     *
     * @param pin assigned power pin
     */
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

    /**
     * Saves power values set by user if the PSLab device is plugged in
     *
     * @param pin   assigned power pin
     * @param power corresponding progress value
     */
    private void savePowerValues(Pin pin, int power) {
        if (scienceLab.isConnected()) {
            SharedPreferences.Editor modifier = powerPreferences.edit();
            modifier.putInt(String.valueOf(pin), power);
            modifier.apply();
        }
    }

    /**
     * Retrieves saved power values corresponding to power pin. If user has already unplugged the
     * PSLab device, this method will clean up records as the device has reset already
     *
     * @param pin assigned power pin
     * @return corresponding progress value
     */
    private int retrievePowerValues(Pin pin) {
        if (scienceLab.isConnected()) {
            return powerPreferences.getInt(String.valueOf(pin), 1);
        } else {
            powerPreferences.edit().clear().apply();
            return 1;
        }
    }

    /**
     * Maps progress value to power values in between the range supported by power pin
     *
     * @param progress       value captured from knob position
     * @param CONTROLLER_MAX maximum value supported by knob
     * @param max            maximum power output
     * @param min            minimum power output
     * @return float value corresponding to the progress value in between min and max
     */
    private float mapProgressToPower(int progress, int CONTROLLER_MAX, float max, float min) {
        return (progress - CONTROLLER_MIN) * (max - min) /
                (CONTROLLER_MAX - CONTROLLER_MIN) + min;
    }

    /**
     * Maps power value to progress values in between the range supported by knob
     *
     * @param power          signed voltage or current value
     * @param CONTROLLER_MAX maximum value supported by knob
     * @param max            maximum power output
     * @param min            minimum power output
     * @return integer value representing progress level at the respective power level in between
     * CONTROLLER_MIN and CONTROLLER_MAX
     */
    private int mapPowerToProgress(float power, int CONTROLLER_MAX, float max, float min) {
        return (int) (limitDigits((power - min) * (CONTROLLER_MAX - CONTROLLER_MIN) /
                (max - min)) + CONTROLLER_MIN);
    }

    /**
     * Chops off excess and rounds off a float number to two decimal places
     *
     * @param number float value with inconsistent decimal places
     * @return truncated float value
     */
    private float limitDigits(float number) {
        return Float.valueOf(String.format(Locale.getDefault(), "%.2f", number));
    }

    /**
     * Stops the Timer that is changing power pin values
     */
    private void stopCounter() {
        if (powerCounter != null) {
            powerCounter.cancel();
            powerCounter.purge();
        }
    }

    /**
     * TimerTask implementation to increment or decrement assigned power pin values at a constant
     * rate provided by LONG_CLICK_DELAY
     *
     * @param pin assigned power pin
     */
    private void fastCounter(final Pin pin) {
        powerCounter = new Timer();
        TimerTask task = new TimerTask() {
            public void run() {
                powerHandler.post(new Runnable() {
                    public void run() {
                        if (incrementPower) {
                            incrementValue(pin);
                        } else if (decrementPower) {
                            decrementValue(pin);
                        }
                    }
                });
            }
        };
        powerCounter.schedule(task, 1, LONG_CLICK_DELAY);
    }

    private enum Pin {
        PV1, PV2, PV3, PCS
    }
}
