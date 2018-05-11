package org.fossasia.pslab.adapters;

import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import org.fossasia.pslab.R;
import org.fossasia.pslab.communication.ScienceLab;
import org.fossasia.pslab.others.ControlActivityCommon;
import org.fossasia.pslab.others.ScienceLabCommon;

import java.text.DecimalFormat;
import java.util.HashMap;

import static org.fossasia.pslab.others.MathUtils.map;

/**
 * Created by asitava on 6/6/17.
 */

public class ControlMainAdapter extends RecyclerView.Adapter<ControlMainAdapter.ViewHolder> {

    private String[] mDataset;
    private ScienceLab scienceLab = ScienceLabCommon.scienceLab;
    private boolean manualSeekBarChange = false;
    private HashMap<String, Float> svalue = ControlActivityCommon.editTextValues;

    private boolean setButtonStates[] = {false,false,false,false,false,false,false};

    public boolean getSetButtonStates(int position) {
        return setButtonStates[position];
    }
    public void changeSetButtonStates(int position, boolean value) {
        this.setButtonStates[position] = value;
    }
    public void changeGreenToRed(int position, Button btn) {
        if(getSetButtonStates(position)){
            btn.setBackgroundColor(Color.parseColor("#c72c2c"));
            changeSetButtonStates(position, false);
        }
    }

    public void changeRedToGreen(int position, Button btn) {
        if(!getSetButtonStates(position)){
            btn.setBackgroundColor(Color.GREEN);
            changeSetButtonStates(position, true);
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        CardView mCardView;
        TextView tvControlMain1;
        TextView tvControlMain2;
        EditText editTextControlMain;
        Button buttonDecreaseValue;
        Button buttonIncreaseValue;
        Button buttonSetValue;
        SeekBar seekBarControlMain;

        public ViewHolder(View view) {
            super(view);
            mCardView = (CardView) view.findViewById(R.id.cardview_control_main);
            tvControlMain1 = (TextView) view.findViewById(R.id.tv_control_main1);
            tvControlMain2 = (TextView) view.findViewById(R.id.tv_control_main2);
            editTextControlMain = (EditText) view.findViewById(R.id.edittext_control_main);
            buttonDecreaseValue = (Button) view.findViewById(R.id.button_decrease_value);
            buttonIncreaseValue = (Button) view.findViewById(R.id.button_increase_value);
            buttonSetValue = (Button) view.findViewById(R.id.button_set_value);
            seekBarControlMain = (SeekBar) view.findViewById(R.id.seekbar_control_main);
            editTextControlMain.setText("0");
        }

    }

    public ControlMainAdapter(String[] myDataset) {
        mDataset = myDataset;
    }

    @Override
    public ControlMainAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.control_main_list_item, parent, false);
        return new ViewHolder(v);
    }

    public void detectValueChanges(ViewHolder holder, final int position, final double upperBound, final double lowerBound, final int max){
        final Button buttonDecreaseValue = holder.buttonDecreaseValue;
        final Button buttonIncreaseValue = holder.buttonIncreaseValue;
        final Button buttonSetValue = holder.buttonSetValue;

        final SeekBar seekBarControlMain = holder.seekBarControlMain;
        final EditText editTextControlMain = holder.editTextControlMain;

        buttonDecreaseValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    changeGreenToRed(position, buttonSetValue);
                    Double data = Double.valueOf(editTextControlMain.getText().toString());
                    Double dataDecrement = data - 0.0025;
                    if (dataDecrement < lowerBound)
                        dataDecrement = lowerBound;
                    else if (dataDecrement > upperBound)
                        dataDecrement = upperBound;
                    int setProgress = (int) ((dataDecrement + upperBound) * 100);
                    manualSeekBarChange = true;
                    seekBarControlMain.setProgress(setProgress);
                    manualSeekBarChange = false;
                    DecimalFormat df = new DecimalFormat("0.0000");
                    editTextControlMain.setText(df.format(dataDecrement));
                } catch (NumberFormatException e) {
                    editTextControlMain.setText("0.0000");
                }
            }
        });

        buttonIncreaseValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    changeGreenToRed(position, buttonSetValue);
                    Double data1 = Double.valueOf(editTextControlMain.getText().toString());
                    Double dataIncrement = data1 + 0.0025;
                    if (dataIncrement < lowerBound)
                        dataIncrement = lowerBound;
                    else if (dataIncrement > upperBound)
                        dataIncrement = upperBound;
                    int setProgress = (int) ((dataIncrement + upperBound) * 100);
                    manualSeekBarChange = true;
                    seekBarControlMain.setProgress(setProgress);
                    manualSeekBarChange = false;
                    DecimalFormat df = new DecimalFormat("0.0000");
                    editTextControlMain.setText(df.format(dataIncrement));
                } catch (NumberFormatException e) {
                    editTextControlMain.setText("0.0000");
                }
            }
        });

        buttonSetValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Float value = Float.parseFloat(editTextControlMain.getText().toString());
                    if (value > upperBound)
                        value = (float) upperBound;
                    else if (value < lowerBound)
                        value = (float) lowerBound;
                    editTextControlMain.setText(String.valueOf(value));
                    manualSeekBarChange = true;
                    seekBarControlMain.setProgress((int) map(value, lowerBound, upperBound, 0, max));
                    manualSeekBarChange = false;
                    ControlActivityCommon.editTextValues.put(mDataset[position], value);
                    if (scienceLab.isConnected()) {
                        setScienceLabValue(position, value);
                        changeRedToGreen(position, buttonSetValue);
                    }
                } catch (NumberFormatException e) {
                    editTextControlMain.setText("0.0000");
                }

            }
        });
        //Text focus listener to figure out if the value has changed. Change color of SET button
        editTextControlMain.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                changeGreenToRed(position, buttonSetValue);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        seekBarControlMain.setMax(max);
        if (svalue.get(mDataset[position]) != null) {
            editTextControlMain.setText(svalue.get(mDataset[position]).toString());
            seekBarControlMain.setProgress((int) map(svalue.get(mDataset[position]), lowerBound, upperBound, 0, max));
        } else {
            editTextControlMain.setText(String.format("%.4f", lowerBound));
        }


        seekBarControlMain.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!manualSeekBarChange) {
                    double text = map(progress, 0, max, lowerBound, upperBound);
                    DecimalFormat df = new DecimalFormat("0.0000");
                    editTextControlMain.setText(df.format(text));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                changeGreenToRed(position, buttonSetValue);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    public void detectValueChanges(ViewHolder holder, final int position){
        final Button buttonDecreaseValue = holder.buttonDecreaseValue;
        final Button buttonIncreaseValue = holder.buttonIncreaseValue;
        final Button buttonSetValue = holder.buttonSetValue;

        final SeekBar seekBarControlMain = holder.seekBarControlMain;
        final EditText editTextControlMain = holder.editTextControlMain;


        buttonDecreaseValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    changeGreenToRed(position, buttonSetValue);
                    int data = Integer.valueOf(editTextControlMain.getText().toString());
                    int dataDecrement = data - 1;
                    if (dataDecrement < 10)
                        dataDecrement = 10;
                    else if (dataDecrement > 5000)
                        dataDecrement = 5000;
                    manualSeekBarChange = true;
                    seekBarControlMain.setProgress(dataDecrement - 10);
                    manualSeekBarChange = false;
                    DecimalFormat df = new DecimalFormat("####");
                    editTextControlMain.setText(df.format(dataDecrement));
                } catch (NumberFormatException e) {
                    editTextControlMain.setText("0");
                }
            }
        });

        buttonIncreaseValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    changeGreenToRed(position, buttonSetValue);
                    int data1 = Integer.valueOf(editTextControlMain.getText().toString());
                    int dataIncrement = data1 + 1;
                    if (dataIncrement < 10)
                        dataIncrement = 10;
                    else if (dataIncrement > 5000)
                        dataIncrement = 5000;
                    manualSeekBarChange = true;
                    seekBarControlMain.setProgress(dataIncrement - 10);
                    manualSeekBarChange = false;
                    DecimalFormat df = new DecimalFormat("####");
                    editTextControlMain.setText(df.format(dataIncrement));
                } catch (NumberFormatException e) {
                    editTextControlMain.setText("0");
                }
            }
        });

        buttonSetValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    int value = Integer.parseInt(editTextControlMain.getText().toString());
                    if (value < 10)
                        value = 10;
                    else if (value > 5000)
                        value = 5000;
                    editTextControlMain.setText(String.valueOf(value));
                    manualSeekBarChange = true;
                    seekBarControlMain.setProgress(value - 10);
                    manualSeekBarChange = false;
                    ControlActivityCommon.editTextValues.put(mDataset[position], (float) value);
                    if (scienceLab.isConnected()) {
                        setScienceLabValue(position, value);
                        changeRedToGreen(position, buttonSetValue);
                    }
                } catch (NumberFormatException e) {
                    editTextControlMain.setText("0");
                }
            }
        });

        //Text focus listener to figure out if the value has changed. Change color of SET button
        editTextControlMain.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                changeGreenToRed(position, buttonSetValue);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        seekBarControlMain.setMax(4990);
        if (svalue.get(mDataset[position]) != null) {
            editTextControlMain.setText(String.valueOf(svalue.get(mDataset[position]).intValue()));
            seekBarControlMain.setProgress(svalue.get(mDataset[position]).intValue() - 10);
        } else {
            editTextControlMain.setText("10");
        }
        seekBarControlMain.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!manualSeekBarChange) {
                    int frequency = (int) map(progress, 0, 4990, 10, 5000);
                    DecimalFormat df = new DecimalFormat("####");
                    editTextControlMain.setText(df.format(frequency));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                changeGreenToRed(position, buttonSetValue);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void setScienceLabValue(int position, float value) {
        switch (position){
            case 0:
                scienceLab.setPV1(value);
                break;
            case 1:
                scienceLab.setPV2(value);
                break;
            case 2:
                scienceLab.setPV3(value);
                break;
            case 3:
                scienceLab.setPCS(value);
                break;
            case 4:
                scienceLab.setSine1(value);
                break;
            case 5:
                scienceLab.setSine2(value);
                break;
            case 6:
                scienceLab.setSqr1(value, -1, false);
                break;
            default:
                break;
        }
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.setIsRecyclable(true);
        holder.tvControlMain1.setText(mDataset[position]);

        switch (position) {
            case 0:
                detectValueChanges(holder, position, 5.0, -5.0, 1000);
                break;

            case 1:
                detectValueChanges(holder, position, 3.0, -3.0, 660);
                break;

            case 2:
                detectValueChanges(holder, position, 3.3, 0.0, 330);
                break;

            case 3:
                detectValueChanges(holder, position, 3.3, 0.0, 330);
                break;

            case 4:
                detectValueChanges(holder, position);
                break;

            case 5:
                detectValueChanges(holder, position);
                break;

            case 6:
                detectValueChanges(holder, position);
                break;
        }

    }


    @Override
    public int getItemCount() {
        return mDataset.length;
    }

}
