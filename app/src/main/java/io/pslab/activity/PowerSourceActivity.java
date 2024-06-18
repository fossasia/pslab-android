package io.pslab.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.util.Log;
import android.util.Range;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.widget.TextViewCompat;

import com.google.android.material.snackbar.Snackbar;
import com.sdsmdg.harjot.crollerTest.Croller;
import com.sdsmdg.harjot.crollerTest.OnCrollerChangeListener;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.pslab.R;
import io.pslab.activity.guide.GuideActivity;
import io.pslab.communication.ScienceLab;
import io.pslab.items.SquareImageButton;
import io.pslab.models.PowerSourceData;
import io.pslab.models.SensorDataBlock;
import io.pslab.others.CSVDataLine;
import io.pslab.others.CSVLogger;
import io.pslab.others.CustomSnackBar;
import io.pslab.others.GPSLogger;
import io.pslab.others.LocalDataLog;
import io.pslab.others.ScienceLabCommon;
import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;

public class PowerSourceActivity extends GuideActivity {

    private final static String TAG = PowerSourceActivity.class.getSimpleName();

    public static final String POWER_PREFERENCES = "Power_Preferences";
    private static final CSVDataLine CSV_HEADER = new CSVDataLine()
            .add("Timestamp")
            .add("DateTime")
            .add("PV1")
            .add("PV2")
            .add("PV3")
            .add("PCS")
            .add("Latitude")
            .add("Longitude");

    private static final String VOLTAGE_FORMAT = "%f V";
    private static final String CURRENT_FORMAT = "%f mA";

    private static final int CONTROLLER_MIN = 1;
    private static final int PV1_CONTROLLER_MAX = 1001;
    private static final int PV2_CONTROLLER_MAX = 661;
    private static final int PV3_CONTROLLER_MAX = 331;
    private static final int PCS_CONTROLLER_MAX = 331;

    private static final Range<Float> PV1_VOLTAGE_RANGE = Range.create(-5.0f, 5.0f);
    private static final Range<Float> PV2_VOLTAGE_RANGE = Range.create(-3.30f, 3.30f);
    private static final Range<Float> PV3_VOLTAGE_RANGE = Range.create(0.0f, 3.30f);
    private static final Range<Float> PCS_CURRENT_RANGE = Range.create(0.0f, 3.30f);

    private final NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());

    /**
     * Step of one tap on an up or down button.
     */
    private static final float STEP = 0.01f;

    private static final long LONG_CLICK_DELAY = 100;
    private static final String KEY_LOG = "has_log";
    private static final String DATA_BLOCK = "data_block";

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.power_card_pv1_controller)
    Croller controllerPV1;
    @BindView(R.id.power_card_pv1_display)
    EditText displayPV1;
    @BindView(R.id.power_card_pv1_up)
    SquareImageButton upPV1;
    @BindView(R.id.power_card_pv1_down)
    SquareImageButton downPV1;
    @BindView(R.id.power_source_coordinatorLayout)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.power_card_pv2_controller)
    Croller controllerPV2;
    @BindView(R.id.power_card_pv2_display)
    EditText displayPV2;
    @BindView(R.id.power_card_pv2_up)
    SquareImageButton upPV2;
    @BindView(R.id.power_card_pv2_down)
    SquareImageButton downPV2;
    @BindView(R.id.power_card_pv3_controller)
    Croller controllerPV3;
    @BindView(R.id.power_card_pv3_display)
    EditText displayPV3;
    @BindView(R.id.power_card_pv3_up)
    SquareImageButton upPV3;
    @BindView(R.id.power_card_pv3_down)
    SquareImageButton downPV3;
    @BindView(R.id.power_card_pcs_controller)
    Croller controllerPCS;
    @BindView(R.id.power_card_pcs_display)
    EditText displayPCS;
    @BindView(R.id.power_card_pcs_up)
    SquareImageButton upPCS;
    @BindView(R.id.power_card_pcs_down)
    SquareImageButton downPCS;
    private CSVLogger powerSourceLogger = null;
    private GPSLogger gpsLogger = null;
    private Realm realm;
    private Timer recordTimer = null;
    private Timer playbackTimer = null;
    private int currentPosition = 0;
    private boolean playClicked = false;
    private final long recordPeriod = 1000;
    private boolean isRecording = false;
    private Boolean writeHeaderToFile = true;
    private SharedPreferences powerPreferences;
    private boolean isRunning = false;
    private boolean incrementPower = false, decrementPower = false;
    private final ScienceLab scienceLab = ScienceLabCommon.scienceLab;
    private RealmResults<PowerSourceData> recordedPowerData;
    private Timer powerCounter;
    private final Handler powerHandler = new Handler();
    private long block;
    private boolean isPlayingBack = false;
    private MenuItem stopMenu;
    private MenuItem playMenu;

    private float voltagePV1 = 0.00f, voltagePV2 = 0.00f, voltagePV3 = 0.00f, currentPCS = 0.00f;

    public PowerSourceActivity() {
        super(R.layout.activity_power_source);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        powerPreferences = getSharedPreferences(POWER_PREFERENCES, MODE_PRIVATE);

        gpsLogger = new GPSLogger(this,
                (LocationManager) getSystemService(Context.LOCATION_SERVICE));
        realm = LocalDataLog.with().getRealm();

        autoSize(displayPV1);
        autoSize(displayPV2);
        autoSize(displayPV3);
        autoSize(displayPCS);

        displayPV1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayPV1.setCursorVisible(true);
            }
        });
        displayPV2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayPV2.setCursorVisible(true);
            }
        });
        displayPV3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayPV3.setCursorVisible(true);
            }
        });
        displayPCS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayPCS.setCursorVisible(true);
            }
        });
        displayPV1.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    final String voltageValue = remove(displayPV1.getText(), "V", "\\+").trim();
                    final float voltage = PV1_VOLTAGE_RANGE.clamp(parseFloat(voltageValue, PV1_VOLTAGE_RANGE.getLower()));
                    setText(displayPV1, VOLTAGE_FORMAT, voltage);
                    controllerPV1.setProgress(mapPowerToProgress(voltage, PV1_CONTROLLER_MAX,
                            PV1_VOLTAGE_RANGE.getUpper(), PV1_VOLTAGE_RANGE.getLower()));
                }
                return false;
            }
        });

        displayPV2.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    final String voltageValue = remove(displayPV2.getText(), "V", "\\+").trim();
                    final float voltage = PV2_VOLTAGE_RANGE.clamp(parseFloat(voltageValue, PV2_VOLTAGE_RANGE.getLower()));
                    setText(displayPV2, VOLTAGE_FORMAT, voltage);
                    controllerPV2.setProgress(mapPowerToProgress(voltage, PV2_CONTROLLER_MAX,
                            PV2_VOLTAGE_RANGE.getUpper(), PV2_VOLTAGE_RANGE.getLower()));
                }
                return false;
            }
        });

        displayPV3.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    final String voltageValue = remove(displayPV3.getText(), "V", "\\+").trim();
                    final float voltage = PV3_VOLTAGE_RANGE.clamp(parseFloat(voltageValue, PV3_VOLTAGE_RANGE.getLower()));
                    setText(displayPV3, VOLTAGE_FORMAT, voltage);
                    controllerPV3.setProgress(mapPowerToProgress(voltage, PV3_CONTROLLER_MAX,
                            PV3_VOLTAGE_RANGE.getUpper(), PV3_VOLTAGE_RANGE.getLower()));
                }
                return false;
            }
        });

        displayPCS.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    final String currentValue = remove(displayPCS.getText(), "mA", "\\+").trim();
                    final float current = PCS_CURRENT_RANGE.clamp(parseFloat(currentValue, PCS_CURRENT_RANGE.getLower()));
                    setText(displayPV3, CURRENT_FORMAT, current);
                    controllerPCS.setProgress(mapPowerToProgress(current, PCS_CONTROLLER_MAX,
                            PCS_CURRENT_RANGE.getUpper(), PCS_CURRENT_RANGE.getLower()));
                }
                return false;
            }
        });

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
                PV1_CONTROLLER_MAX, PV1_VOLTAGE_RANGE.getUpper(), PV1_VOLTAGE_RANGE.getLower())), Pin.PV1);
        updateDisplay(displayPV2, limitDigits(mapProgressToPower(retrievePowerValues(Pin.PV2),
                PV2_CONTROLLER_MAX, PV2_VOLTAGE_RANGE.getUpper(), PV2_VOLTAGE_RANGE.getLower())), Pin.PV2);
        updateDisplay(displayPV3, limitDigits(mapProgressToPower(retrievePowerValues(Pin.PV3),
                PV3_CONTROLLER_MAX, PV3_VOLTAGE_RANGE.getUpper(), PV3_VOLTAGE_RANGE.getLower())), Pin.PV3);
        updateDisplay(displayPCS, limitDigits(mapProgressToPower(retrievePowerValues(Pin.PCS),
                PCS_CONTROLLER_MAX, PCS_CURRENT_RANGE.getUpper(), PCS_CURRENT_RANGE.getLower())), Pin.PCS);

        if (getIntent().getExtras() != null && getIntent().getExtras().getBoolean(KEY_LOG)) {
            recordedPowerData = LocalDataLog.with()
                    .getBlockOfPowerRecords(getIntent().getExtras().getLong(DATA_BLOCK));
            isPlayingBack = true;
            disableButtons();
        }

        if (getResources().getBoolean(R.bool.isTablet)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
        }

        if (!scienceLab.isConnected() && savedInstanceState != null) {
            displayPV1.setText(savedInstanceState.getString("displayPV1"));
            displayPV1.onEditorAction(EditorInfo.IME_ACTION_DONE);
            displayPV2.setText(savedInstanceState.getString("displayPV2"));
            displayPV2.onEditorAction(EditorInfo.IME_ACTION_DONE);
            displayPV3.setText(savedInstanceState.getString("displayPV3"));
            displayPV3.onEditorAction(EditorInfo.IME_ACTION_DONE);
            displayPCS.setText(savedInstanceState.getString("displayPCS"));
            displayPCS.onEditorAction(EditorInfo.IME_ACTION_DONE);
        }
    }

    /**
     * Parses text to produce a number respecting the current locale of the system.
     *
     * @param toBeParsed   text to be parsed
     * @param defaultValue fallback value to be used if text cannot be parsed
     * @return number parsed from text
     */
    private float parseFloat(@NonNull String toBeParsed, float defaultValue) {
        float parsedValue = defaultValue;
        try {
            parsedValue = numberFormat.parse(toBeParsed).floatValue();
        } catch (ParseException e) {
            Log.e(TAG, "Unable to parse " + toBeParsed, e);
        }

        return parsedValue;
    }

    /**
     * Turns a value into a String representation that respects the current locale of the device
     * and sets it to a TextVew.
     *
     * @param textView UI element which will display the text
     * @param format   formatted String which value will be inserted to
     * @param value    the value to display
     */
    private static void setText(@NonNull TextView textView, @NonNull String format, float value) {
        textView.setText(String.format(Locale.getDefault(), format, value));
    }

    private String remove(@NonNull Editable input, @NonNull String... toBeRemoved) {
        return remove(input.toString(), toBeRemoved);
    }

    private String remove(@NonNull String input, @NonNull String... toBeRemoved) {
        String output = input;
        for (String s : toBeRemoved) {
            output = output.replaceAll(s, "");
        }
        return output;
    }

    /**
     * Autosize the voltage display in textView to utilize empty space
     *
     * @param view Display text view
     */
    private void autoSize(TextView view) {
        TextViewCompat.setAutoSizeTextTypeWithDefaults(view,
                TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.power_source_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (isPlayingBack) {
            menu.findItem(R.id.play_data).setVisible(true);
            menu.findItem(R.id.stop_data).setVisible(false);
            menu.findItem(R.id.power_source_record_data).setVisible(false);
        } else {
            menu.findItem(R.id.play_data).setVisible(false);
            menu.findItem(R.id.stop_data).setVisible(false);
            menu.findItem(R.id.power_source_record_data).setVisible(true);
        }
        stopMenu = menu.findItem(R.id.stop_data);
        playMenu = menu.findItem(R.id.play_data);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.show_guide:
                toggleGuide();
                break;
            case R.id.power_source_record_data:
                if (!isRecording) {
                    item.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_record_stop_white, null));
                    isRecording = true;
                    if (recordTimer == null) {
                        recordTimer = new Timer();
                    } else {
                        recordTimer.cancel();
                        recordTimer = new Timer();
                    }
                    CustomSnackBar.showSnackBar(coordinatorLayout, getString(R.string.data_recording_start), null, null, Snackbar.LENGTH_SHORT);
                    final Handler handler = new Handler();
                    block = System.currentTimeMillis();
                    recordTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    recordData();
                                }
                            });
                        }
                    }, 0, recordPeriod);
                } else {
                    item.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_record_white, null));
                    recordTimer.cancel();
                    recordTimer = null;
                    isRecording = false;
                    writeHeaderToFile = true;
                    CustomSnackBar.showSnackBar(coordinatorLayout,
                            getString(R.string.csv_store_text) + " " + powerSourceLogger.getCurrentFilePath()
                            , getString(R.string.open), new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent intent = new Intent(PowerSourceActivity.this, DataLoggerActivity.class);
                                    intent.putExtra(DataLoggerActivity.CALLER_ACTIVITY, getResources().getString(R.string.power_source));
                                    startActivity(intent);
                                }
                            }, Snackbar.LENGTH_LONG);

                }
                break;
            case R.id.play_data:
                if (!playClicked) {
                    playClicked = true;
                    stopMenu.setVisible(true);
                    item.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_pause_white_24dp, null));
                    if (playbackTimer == null) {
                        playbackTimer = new Timer();
                    } else {
                        playbackTimer.cancel();
                        playbackTimer = new Timer();
                    }
                    final Handler handler = new Handler();
                    playbackTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (recordedPowerData != null && currentPosition < recordedPowerData.size()) {
                                        final PowerSourceData data = recordedPowerData.get(currentPosition);
                                        if (data != null) {
                                            setSavedValue(data);
                                        }
                                    } else {
                                        playbackTimer.cancel();
                                        currentPosition = 0;
                                        playClicked = false;
                                        stopMenu.setVisible(false);
                                        item.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_play_arrow_white_24dp, null));
                                    }
                                }
                            });
                        }
                    }, 0, recordPeriod);

                } else {
                    playClicked = false;
                    stopMenu.setVisible(false);
                    item.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_play_arrow_white_24dp, null));
                    if (playbackTimer != null) {
                        playbackTimer.cancel();
                        playbackTimer = null;
                    }
                }
                break;
            case R.id.stop_data:
                if (playbackTimer != null) {
                    playbackTimer.cancel();
                    currentPosition = 0;
                    playClicked = false;
                    playMenu.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_play_arrow_white_24dp, null));
                    stopMenu.setVisible(false);
                }
                break;
            case android.R.id.home:
                this.finish();
                break;
            default:
                break;
        }

        return true;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString("displayPV1", displayPV1.getText().toString());
        outState.putString("displayPV2", displayPV2.getText().toString());
        outState.putString("displayPV3", displayPV3.getText().toString());
        outState.putString("displayPCS", displayPCS.getText().toString());
        super.onSaveInstanceState(outState);
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
                removeCursor();
            }

            @Override
            public void onStartTrackingTouch(Croller croller) {/**/}

            @Override
            public void onStopTrackingTouch(Croller croller) {
                setPower(pin);
            }
        });
    }

    private void removeCursor() {
        displayPV1.setCursorVisible(false);
        displayPV2.setCursorVisible(false);
        displayPV3.setCursorVisible(false);
        displayPCS.setCursorVisible(false);
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
                if (voltagePV1 < PV1_VOLTAGE_RANGE.getUpper()) {
                    voltagePV1 += STEP;
                    updateDisplay(displayPV1, voltagePV1, Pin.PV1);
                    updateController(controllerPV1, Pin.PV1);
                }
                break;
            case PV2:
                if (voltagePV2 < PV2_VOLTAGE_RANGE.getUpper()) {
                    voltagePV2 += STEP;
                    updateDisplay(displayPV2, voltagePV2, Pin.PV2);
                    updateController(controllerPV2, Pin.PV2);
                }
                break;
            case PV3:
                if (voltagePV3 < PV3_VOLTAGE_RANGE.getUpper()) {
                    voltagePV3 += STEP;
                    updateDisplay(displayPV3, voltagePV3, Pin.PV3);
                    updateController(controllerPV3, Pin.PV3);
                }
                break;
            case PCS:
                if (currentPCS < PCS_CURRENT_RANGE.getUpper()) {
                    currentPCS += STEP;
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
                if (voltagePV1 > PV1_VOLTAGE_RANGE.getLower()) {
                    voltagePV1 -= STEP;
                    updateDisplay(displayPV1, voltagePV1, Pin.PV1);
                    updateController(controllerPV1, Pin.PV1);
                }
                break;
            case PV2:
                if (voltagePV2 > PV2_VOLTAGE_RANGE.getLower()) {
                    voltagePV2 -= STEP;
                    updateDisplay(displayPV2, voltagePV2, Pin.PV2);
                    updateController(controllerPV2, Pin.PV2);
                }
                break;
            case PV3:
                if (voltagePV3 > PV3_VOLTAGE_RANGE.getLower()) {
                    voltagePV3 -= STEP;
                    updateDisplay(displayPV3, voltagePV3, Pin.PV3);
                    updateController(controllerPV3, Pin.PV3);
                }
                break;
            case PCS:
                if (currentPCS > PCS_CURRENT_RANGE.getLower()) {
                    currentPCS -= STEP;
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
                        PV1_VOLTAGE_RANGE.getUpper(), PV1_VOLTAGE_RANGE.getLower()));
                break;
            case PV2:
                controller.setProgress(mapPowerToProgress(voltagePV2, PV2_CONTROLLER_MAX,
                        PV2_VOLTAGE_RANGE.getUpper(), PV2_VOLTAGE_RANGE.getLower()));
                break;
            case PV3:
                controller.setProgress(mapPowerToProgress(voltagePV3, PV3_CONTROLLER_MAX,
                        PV3_VOLTAGE_RANGE.getUpper(), PV3_VOLTAGE_RANGE.getLower()));
                break;
            case PCS:
                controller.setProgress(mapPowerToProgress(currentPCS, PCS_CONTROLLER_MAX,
                        PCS_CURRENT_RANGE.getUpper(), PCS_CURRENT_RANGE.getLower()));
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
                        PV1_VOLTAGE_RANGE.getUpper(), PV1_VOLTAGE_RANGE.getLower()));
                updateDisplay(displayPV1, voltagePV1, pin);
                break;
            case PV2:
                voltagePV2 = limitDigits(mapProgressToPower(progress, PV2_CONTROLLER_MAX,
                        PV2_VOLTAGE_RANGE.getUpper(), PV2_VOLTAGE_RANGE.getLower()));
                updateDisplay(displayPV2, voltagePV2, pin);
                break;
            case PV3:
                voltagePV3 = limitDigits(mapProgressToPower(progress, PV3_CONTROLLER_MAX,
                        PV3_VOLTAGE_RANGE.getUpper(), PV3_VOLTAGE_RANGE.getLower()));
                updateDisplay(displayPV3, voltagePV3, pin);
                break;
            case PCS:
                currentPCS = limitDigits(mapProgressToPower(progress, PCS_CONTROLLER_MAX,
                        PCS_CURRENT_RANGE.getUpper(), PCS_CURRENT_RANGE.getLower()));
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
            boolean guideState = powerPreferences.getBoolean("PowerSourceFirstTime", true);
            powerPreferences.edit().clear().apply();
            SharedPreferences.Editor editor = powerPreferences.edit();
            editor.putBoolean("PowerSourceFirstTime", guideState);
            editor.apply();
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
        try {
            return Float.parseFloat(String.format(Locale.ROOT, "%.2f", number));
        } catch (NumberFormatException e) {
            return 0.00f;
        }
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

    private void recordData() {
        long timestamp;
        double lat;
        double lon;
        if (writeHeaderToFile) {
            powerSourceLogger = new CSVLogger(getString(R.string.power_source));
            powerSourceLogger.prepareLogFile();
            powerSourceLogger.writeMetaData(getString(R.string.power_source));
            powerSourceLogger.writeCSVFile(CSV_HEADER);
            writeHeaderToFile = !writeHeaderToFile;
            recordSensorDataBlockID(new SensorDataBlock(block, getResources().getString(R.string.power_source)));
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
        timestamp = System.currentTimeMillis();
        powerSourceLogger.writeCSVFile(
                new CSVDataLine()
                        .add(System.currentTimeMillis())
                        .add(CSVLogger.FILE_NAME_FORMAT.format(new Date(System.currentTimeMillis())))
                        .add(voltagePV1)
                        .add(voltagePV2)
                        .add(voltagePV3)
                        .add(currentPCS)
                        .add(lat)
                        .add(lon)
        );
        recordSensorData(new PowerSourceData(timestamp, block, voltagePV1, voltagePV2, voltagePV3, currentPCS, lat, lon));
    }

    public void recordSensorDataBlockID(SensorDataBlock block) {
        realm.beginTransaction();
        realm.copyToRealm(block);
        realm.commitTransaction();
    }

    public void recordSensorData(RealmObject sensorData) {
        realm.beginTransaction();
        realm.copyToRealm((PowerSourceData) sensorData);
        realm.commitTransaction();
    }

    private void setSavedValue(PowerSourceData data) {
        voltagePV1 = data.getPv1();
        voltagePV2 = data.getPv2();
        voltagePV3 = data.getPv3();
        currentPCS = data.getPcs();
        controllerPV1.setProgress(mapPowerToProgress(voltagePV1, PV1_CONTROLLER_MAX,
                PV1_VOLTAGE_RANGE.getUpper(), PV1_VOLTAGE_RANGE.getLower()));
        setMappedPower(Pin.PV1, mapPowerToProgress(voltagePV1, PV1_CONTROLLER_MAX,
                PV1_VOLTAGE_RANGE.getUpper(), PV1_VOLTAGE_RANGE.getLower()));
        controllerPV2.setProgress(mapPowerToProgress(voltagePV2, PV2_CONTROLLER_MAX,
                PV2_VOLTAGE_RANGE.getUpper(), PV2_VOLTAGE_RANGE.getLower()));
        setMappedPower(Pin.PV2, mapPowerToProgress(voltagePV2, PV2_CONTROLLER_MAX,
                PV2_VOLTAGE_RANGE.getUpper(), PV2_VOLTAGE_RANGE.getLower()));
        controllerPV3.setProgress(mapPowerToProgress(voltagePV3, PV3_CONTROLLER_MAX,
                PV3_VOLTAGE_RANGE.getUpper(), PV3_VOLTAGE_RANGE.getLower()));
        setMappedPower(Pin.PV3, mapPowerToProgress(voltagePV3, PV3_CONTROLLER_MAX,
                PV3_VOLTAGE_RANGE.getUpper(), PV3_VOLTAGE_RANGE.getLower()));
        controllerPCS.setProgress(mapPowerToProgress(currentPCS, PCS_CONTROLLER_MAX,
                PCS_CURRENT_RANGE.getUpper(), PCS_CURRENT_RANGE.getLower()));
        setMappedPower(Pin.PCS, mapPowerToProgress(currentPCS, PCS_CONTROLLER_MAX,
                PCS_CURRENT_RANGE.getUpper(), PCS_CURRENT_RANGE.getLower()));
        currentPosition++;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (recordTimer != null) {
            recordTimer.cancel();
            recordTimer = null;
        }
    }

    private void disableButtons() {
        upPV1.setEnabled(false);
        upPV2.setEnabled(false);
        upPV3.setEnabled(false);
        upPCS.setEnabled(false);
        downPV1.setEnabled(false);
        downPV2.setEnabled(false);
        downPV3.setEnabled(false);
        downPCS.setEnabled(false);
    }

    private enum Pin {
        PV1, PV2, PV3, PCS
    }
}
