package org.fossasia.pslab.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;

import org.fossasia.pslab.R;
import org.fossasia.pslab.communication.ScienceLab;
import org.fossasia.pslab.communication.peripherals.I2C;
import org.fossasia.pslab.communication.sensors.BH1750;
import org.fossasia.pslab.others.ScienceLabCommon;

import java.io.IOException;

public class LuxMeterFragmentConfig extends Fragment {
    private static BH1750 bh1750;
    private final int highLimitMax = 10000;
    private final int updatePeriodMax = 900;
    private final int updatePeriodMin = 100;
    private static int selectedSensor = 0; //0 for built in and 1 for BH1750
    private static int highValue = 0;
    private static int updatePeriodValue = 100;

    private EditText highLimit;
    private EditText updatePeriod;
    private CardView gainRangeCard;
    private Spinner gainValue;

    private static ScienceLab scienceLab;

    private OnDataPass dataParser;

    public static LuxMeterFragmentConfig newInstance() {
        return new LuxMeterFragmentConfig();
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lux_meter_config, container, false);

        highLimit = (EditText) view.findViewById(R.id.lux_hight_limit_text);
        final SeekBar highLimitSeek = (SeekBar) view.findViewById(R.id.lux_hight_limit_seekbar);
        updatePeriod = (EditText) view.findViewById(R.id.lux_update_period_text);
        final SeekBar updatePeriodSeek = (SeekBar) view.findViewById(R.id.lux_update_period_seekbar);
        gainValue = (Spinner) view.findViewById(R.id.spinner_bh1750_gain);
        final Spinner selectSensor = (Spinner) view.findViewById(R.id.spinner_select_light);
        gainRangeCard = (CardView) view.findViewById(R.id.cardview_gain_range);

        highLimitSeek.setMax(highLimitMax);
        updatePeriodSeek.setMax(updatePeriodMax);

        highLimit.setText(String.format("%d", highValue));
        highLimitSeek.setProgress(highValue);
        updatePeriod.setText(String.format("%d", updatePeriodValue));
        updatePeriodSeek.setProgress(Integer.valueOf(updatePeriod.getText().toString()) - updatePeriodMin);

        highLimitSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                highValue = progress;
                highLimit.setText(String.format("%d", progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //do nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //do nothing
            }
        });

        highLimit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    String stringValue = highLimit.getText().toString();
                    int value = 0;
                    try {
                        value = Integer.parseInt(stringValue);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    if (value > highLimitMax)
                        highLimitSeek.setProgress(highLimitMax);
                    else if (value < 0)
                        highLimitSeek.setProgress(0);
                    else
                        highLimitSeek.setProgress(value);
                }
            }
        });

        updatePeriodSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updatePeriodValue = progress + updatePeriodMin;
                updatePeriod.setText(String.format("%d", updatePeriodValue));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //do nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //do nothing
            }
        });

        updatePeriod.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    String stringValue = updatePeriod.getText().toString();
                    int value = 100;
                    try {
                        value = Integer.parseInt(stringValue);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    if (value > updatePeriodMax)
                        updatePeriodSeek.setProgress(updatePeriodMax + 100);
                    else if (value < updatePeriodMin)
                        updatePeriodSeek.setProgress(updatePeriodMin - 100);
                    else
                        updatePeriodSeek.setProgress(value - updatePeriodMin);
                }
            }
        });

        gainValue.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    if (bh1750 != null) {
                        bh1750.setRange(gainValue.getSelectedItem().toString());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //do nothing
            }
        });

        selectSensor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        gainRangeCard.setEnabled(false);
                        LuxMeterFragmentConfig.selectedSensor = 0;
                        break;

                    case 1:
                        scienceLab = ScienceLabCommon.scienceLab;
                        if (scienceLab.isConnected()) {
                            I2C i2c = scienceLab.i2c;

                            try {
                                bh1750 = new BH1750(i2c);
                                gainRangeCard.setEnabled(true);
                                LuxMeterFragmentConfig.selectedSensor = 1;
                            } catch (InterruptedException | IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            Toast.makeText(getContext(), getResources().getText(R.string.device_not_found), Toast.LENGTH_SHORT).show();
                            selectSensor.setSelection(0);
                        }
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //do nothing
            }
        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        dataParser = (OnDataPass) context;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        highValue = getValueFromText(highLimit, 0, highLimitMax);
        updatePeriodValue = getValueFromText(updatePeriod, updatePeriodMin, updatePeriodMax + 100);

        dataParser.onDataPass(selectedSensor, updatePeriodValue, highValue);
    }

    public interface OnDataPass {
        void onDataPass(int sensor, int updatePeriod, int highValue);
    }

    public int getValueFromText(EditText editText, int lowerBound, int upperBound) {
        String strValue = editText.getText().toString();
        if ("".equals(strValue)) {
            return lowerBound;
        }
        int value = Integer.parseInt(strValue);
        if (value > upperBound) return upperBound;
        else if (value < lowerBound) return lowerBound;
        else return value;
    }
}
