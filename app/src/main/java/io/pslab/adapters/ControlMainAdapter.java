package io.pslab.adapters;

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

import io.pslab.R;
import io.pslab.communication.ScienceLab;
import io.pslab.others.ScienceLabCommon;
import static io.pslab.others.MathUtils.map;

import java.text.DecimalFormat;
import io.pslab.DataFormatter;
import static io.pslab.others.MathUtils.map;

/**
 * Created by asitava on 6/6/17.
 */

public class ControlMainAdapter extends RecyclerView.Adapter<ControlMainAdapter.ViewHolder> {

    private String[] mDataset;
    private ScienceLab scienceLab = ScienceLabCommon.scienceLab;
    private boolean manualSeekBarChange = false;

    private boolean isSet1 = false;
    private boolean isSet2 = false;
    private boolean isSet3 = false;
    private boolean isSet4 = false;
    private boolean isSet5 = false;
    private boolean isSet6 = false;
    private boolean isSet7 = false;

    static class ViewHolder extends RecyclerView.ViewHolder {

        CardView mCardView;
        TextView tvControlMain1;
        TextView tvControlMain2;
        EditText editTextControlMain;
        Button buttonControlMain1;
        Button buttonControlMain2;
        Button buttonControlMain3;
        SeekBar seekBarControlMain;

        public ViewHolder(View view) {
            super(view);
            mCardView = view.findViewById(R.id.cardview_control_main);
            tvControlMain1 = view.findViewById(R.id.tv_control_main1);
            tvControlMain2 = view.findViewById(R.id.tv_control_main2);
            editTextControlMain = view.findViewById(R.id.edittext_control_main);
            buttonControlMain1 = view.findViewById(R.id.button_control_main1);
            buttonControlMain2 = view.findViewById(R.id.button_control_main2);
            buttonControlMain3 = view.findViewById(R.id.button_control_main3);
            seekBarControlMain = view.findViewById(R.id.seekbar_control_main);
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

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.setIsRecyclable(true);
        holder.tvControlMain1.setText(mDataset[position]);

        final Button buttonControlMain1 = holder.buttonControlMain1;
        final Button buttonControlMain2 = holder.buttonControlMain2;
        final Button buttonControlMain3 = holder.buttonControlMain3;

        final SeekBar seekBarControlMain = holder.seekBarControlMain;
        final EditText editTextControlMain = holder.editTextControlMain;

        switch (position) {
            case 0:
                buttonControlMain1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            if (isSet1) {
                                buttonControlMain3.setBackgroundColor(Color.parseColor("#c72c2c"));
                                isSet1 = false;
                            }
                            Double data = Double.parseDouble(editTextControlMain.getText().toString());
                            Double dataDecrement = data - 0.0025;
                            if (dataDecrement < -5.0)
                                dataDecrement = -5.0;
                            else if (dataDecrement > 5.0)
                                dataDecrement = 5.0;
                            int setProgress = (int) ((dataDecrement + 5) * 100);
                            manualSeekBarChange = true;
                            seekBarControlMain.setProgress(setProgress);
                            manualSeekBarChange = false;
                            editTextControlMain.setText(DataFormatter.formatDouble(dataDecrement, DataFormatter.MEDIUM_PRECISION_FORMAT));
                        } catch (NumberFormatException e) {
                            editTextControlMain.setText(DataFormatter.formatDouble(0, DataFormatter.MEDIUM_PRECISION_FORMAT));
                        }
                    }
                });

                buttonControlMain2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            if (isSet1) {
                                buttonControlMain3.setBackgroundColor(Color.parseColor("#c72c2c"));
                                isSet1 = false;
                            }
                            Double data1 = Double.parseDouble(editTextControlMain.getText().toString());
                            Double dataIncrement = data1 + 0.0025;
                            if (dataIncrement < -5.0)
                                dataIncrement = -5.0;
                            else if (dataIncrement > 5.0)
                                dataIncrement = 5.0;
                            int setProgress = (int) ((dataIncrement + 5) * 100);
                            manualSeekBarChange = true;
                            seekBarControlMain.setProgress(setProgress);
                            manualSeekBarChange = false;
                            editTextControlMain.setText(DataFormatter.formatDouble(dataIncrement, DataFormatter.MEDIUM_PRECISION_FORMAT));
                        } catch (NumberFormatException e) {
                            DataFormatter.formatDouble(0, DataFormatter.MEDIUM_PRECISION_FORMAT);
                        }
                    }
                });

                buttonControlMain3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            Float value = Float.parseFloat(editTextControlMain.getText().toString());
                            if (value > 5)
                                value = 5f;
                            else if (value < -5)
                                value = -5f;
                            editTextControlMain.setText(DataFormatter.formatDouble(value, DataFormatter.MEDIUM_PRECISION_FORMAT));
                            manualSeekBarChange = true;
                            seekBarControlMain.setProgress((int) map(value, -5, 5, 0, 1000));
                            manualSeekBarChange = false;
                            if (scienceLab.isConnected()) {
                                scienceLab.setPV1(value);
                                if (!isSet1) {
                                    buttonControlMain3.setBackgroundColor(Color.GREEN);
                                    isSet1 = true;
                                }
                            }
                        } catch (NumberFormatException e) {
                            editTextControlMain.setText(DataFormatter.formatDouble(0, DataFormatter.MEDIUM_PRECISION_FORMAT));
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
                        if (isSet1) {
                            buttonControlMain3.setBackgroundColor(Color.parseColor("#c72c2c"));
                            isSet1 = false;
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {

                    }
                });

                seekBarControlMain.setMax(1000);
                editTextControlMain.setText(DataFormatter.formatDouble(-5f, DataFormatter.MEDIUM_PRECISION_FORMAT));


                seekBarControlMain.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (!manualSeekBarChange) {
                            double text = map(progress, 0, 1000, -5.0, 5.0);
                            DecimalFormat df = new DecimalFormat("0.0000");
                            editTextControlMain.setText(
                                    DataFormatter.formatDouble(text, DataFormatter.MEDIUM_PRECISION_FORMAT));
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        if (isSet1) {
                            buttonControlMain3.setBackgroundColor(Color.parseColor("#c72c2c"));
                            isSet1 = false;
                        }
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });

                break;

            case 1:
                buttonControlMain1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            if (isSet2) {
                                buttonControlMain3.setBackgroundColor(Color.parseColor("#c72c2c"));
                                isSet2 = false;
                            }
                            Double data = Double.parseDouble(editTextControlMain.getText().toString());
                            Double dataDecrement = data - 0.0025;
                            if (dataDecrement < -3.3)
                                dataDecrement = -3.3;
                            else if (dataDecrement > 3.3)
                                dataDecrement = 3.3;
                            int setProgress = (int) ((dataDecrement + 3.3) * 100);
                            manualSeekBarChange = true;
                            seekBarControlMain.setProgress(setProgress);
                            manualSeekBarChange = false;
                            editTextControlMain.setText(DataFormatter.formatDouble(dataDecrement, DataFormatter.MEDIUM_PRECISION_FORMAT));
                        } catch (NumberFormatException e) {
                            editTextControlMain.setText("0");
                        }
                    }
                });

                buttonControlMain2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            if (isSet2) {
                                buttonControlMain3.setBackgroundColor(Color.parseColor("#c72c2c"));
                                isSet2 = false;
                            }
                            Double data1 = Double.parseDouble(editTextControlMain.getText().toString());
                            Double dataIncrement = data1 + 0.0025;
                            if (dataIncrement < -3.3)
                                dataIncrement = -3.3;
                            else if (dataIncrement > 3.3)
                                dataIncrement = 3.3;
                            int setProgress = (int) ((dataIncrement + 3.3) * 100);
                            manualSeekBarChange = true;
                            seekBarControlMain.setProgress(setProgress);
                            manualSeekBarChange = false;
                            editTextControlMain.setText(DataFormatter.formatDouble(dataIncrement, DataFormatter.MEDIUM_PRECISION_FORMAT));
                        } catch (NumberFormatException e) {
                            editTextControlMain.setText(DataFormatter.formatDouble(0, DataFormatter.MEDIUM_PRECISION_FORMAT));
                        }
                    }
                });

                buttonControlMain3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            Float value = Float.parseFloat(editTextControlMain.getText().toString());
                            if (value > 3.3)
                                value = 3.3f;
                            else if (value < -3.3)
                                value = -3.3f;
                            editTextControlMain.setText(DataFormatter.formatDouble(value, DataFormatter.MEDIUM_PRECISION_FORMAT));
                            manualSeekBarChange = true;
                            seekBarControlMain.setProgress((int) ((value + 3.3) * 100));
                            manualSeekBarChange = false;
                            if (scienceLab.isConnected()) {
                                scienceLab.setPV2(value);
                                if (!isSet2) {
                                    buttonControlMain3.setBackgroundColor(Color.GREEN);
                                    isSet2 = true;
                                }
                            }
                        } catch (NumberFormatException e) {
                            editTextControlMain.setText(DataFormatter.formatDouble(0, DataFormatter.MEDIUM_PRECISION_FORMAT));
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
                        if (isSet2) {
                            buttonControlMain3.setBackgroundColor(Color.parseColor("#c72c2c"));
                            isSet2 = false;
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {

                    }
                });

                seekBarControlMain.setMax(660);
                editTextControlMain.setText(DataFormatter.formatDouble(-3.3, DataFormatter.MEDIUM_PRECISION_FORMAT));

                seekBarControlMain.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (!manualSeekBarChange) {
                            double text = map(progress, 0, 660, -3.3, 3.3);
                            editTextControlMain.setText(DataFormatter.formatDouble(text, DataFormatter.MEDIUM_PRECISION_FORMAT));
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        if (isSet2) {
                            buttonControlMain3.setBackgroundColor(Color.parseColor("#c72c2c"));
                            isSet2 = false;
                        }
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });
                break;

            case 2:
                buttonControlMain1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            if (isSet3) {
                                buttonControlMain3.setBackgroundColor(Color.parseColor("#c72c2c"));
                                isSet3 = false;
                            }
                            Double data = Double.parseDouble(editTextControlMain.getText().toString());
                            Double dataDecrement = data - 0.0025;
                            if (dataDecrement < 0.0)
                                dataDecrement = 0.0;
                            else if (dataDecrement > 3.3)
                                dataDecrement = 3.3;
                            manualSeekBarChange = true;
                            seekBarControlMain.setProgress((int) (dataDecrement * 100));
                            manualSeekBarChange = false;
                            editTextControlMain.setText(DataFormatter.formatDouble(dataDecrement, DataFormatter.MEDIUM_PRECISION_FORMAT));
                        } catch (NumberFormatException e) {
                            editTextControlMain.setText(DataFormatter.formatDouble(0, DataFormatter.MEDIUM_PRECISION_FORMAT));
                        }
                    }
                });

                buttonControlMain2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            if (isSet3) {
                                buttonControlMain3.setBackgroundColor(Color.parseColor("#c72c2c"));
                                isSet3 = false;
                            }
                            Double data1 = Double.parseDouble(editTextControlMain.getText().toString());
                            Double dataIncrement = data1 + 0.0025;
                            if (dataIncrement < 0.0)
                                dataIncrement = 0.0;
                            else if (dataIncrement > 3.3)
                                dataIncrement = 3.3;
                            manualSeekBarChange = true;
                            seekBarControlMain.setProgress((int) (dataIncrement * 100));
                            manualSeekBarChange = false;
                            editTextControlMain.setText(DataFormatter.formatDouble(dataIncrement, DataFormatter.MEDIUM_PRECISION_FORMAT));
                        } catch (NumberFormatException e) {
                            editTextControlMain.setText(DataFormatter.formatDouble(0f, DataFormatter.MEDIUM_PRECISION_FORMAT));
                        }
                    }
                });

                buttonControlMain3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            Float value = Float.parseFloat(editTextControlMain.getText().toString());
                            if (value > 3.3)
                                value = 3.3f;
                            else if (value < 0)
                                value = 0f;
                            editTextControlMain.setText(DataFormatter.formatDouble(value, DataFormatter.MEDIUM_PRECISION_FORMAT));
                            manualSeekBarChange = true;
                            seekBarControlMain.setProgress((int) map(value, 0, 3.3, 0, 330));
                            manualSeekBarChange = false;
                            if (scienceLab.isConnected()) {
                                scienceLab.setPV3(value);
                                if (!isSet3) {
                                    buttonControlMain3.setBackgroundColor(Color.GREEN);
                                    isSet3 = true;
                                }
                            }
                        } catch (NumberFormatException e) {
                            editTextControlMain.setText(DataFormatter.formatDouble(0, DataFormatter.MEDIUM_PRECISION_FORMAT));
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
                        if (isSet3) {
                            buttonControlMain3.setBackgroundColor(Color.parseColor("#c72c2c"));
                            isSet3 = false;
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {

                    }
                });

                seekBarControlMain.setMax(330);
                editTextControlMain.setText(DataFormatter.formatDouble(0, DataFormatter.MEDIUM_PRECISION_FORMAT));


                seekBarControlMain.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (!manualSeekBarChange) {
                            double text = map(progress, 0, 330, 0, 3.3);
                            editTextControlMain.setText(DataFormatter.formatDouble(text, DataFormatter.MEDIUM_PRECISION_FORMAT));
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        if (isSet3) {
                            buttonControlMain3.setBackgroundColor(Color.parseColor("#c72c2c"));
                            isSet3 = false;
                        }
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });

                break;

            case 3:
                buttonControlMain1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            if (isSet4) {
                                buttonControlMain3.setBackgroundColor(Color.parseColor("#c72c2c"));
                                isSet4 = false;
                            }
                            Double data = Double.parseDouble(editTextControlMain.getText().toString());
                            Double dataDecrement = data - 0.0025;
                            if (dataDecrement < 0.0)
                                dataDecrement = 0.0;
                            else if (dataDecrement > 3.3)
                                dataDecrement = 3.3;
                            manualSeekBarChange = true;
                            seekBarControlMain.setProgress((int) (dataDecrement * 100));
                            manualSeekBarChange = false;
                            editTextControlMain.setText(DataFormatter.formatDouble(dataDecrement, DataFormatter.MEDIUM_PRECISION_FORMAT));
                        } catch (NumberFormatException e) {
                            editTextControlMain.setText(DataFormatter.formatDouble(0, DataFormatter.MEDIUM_PRECISION_FORMAT));
                        }
                    }
                });

                buttonControlMain2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            if (isSet4) {
                                buttonControlMain3.setBackgroundColor(Color.parseColor("#c72c2c"));
                                isSet4 = false;
                            }
                            Double data1 = Double.parseDouble(editTextControlMain.getText().toString());
                            Double dataIncrement = data1 + 0.0025;
                            if (dataIncrement < 0.0)
                                dataIncrement = 0.0;
                            else if (dataIncrement > 3.3)
                                dataIncrement = 3.3;
                            manualSeekBarChange = true;
                            seekBarControlMain.setProgress((int) (dataIncrement * 100));
                            manualSeekBarChange = false;
                            editTextControlMain.setText(DataFormatter.formatDouble(dataIncrement, DataFormatter.MEDIUM_PRECISION_FORMAT));
                        } catch (NumberFormatException e) {
                            editTextControlMain.setText(DataFormatter.formatDouble(0, DataFormatter.MEDIUM_PRECISION_FORMAT));
                        }
                    }
                });

                buttonControlMain3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            Float value = Float.parseFloat(editTextControlMain.getText().toString());
                            if (value > 3.3)
                                value = 3.3f;
                            else if (value < 0)
                                value = 0f;
                            editTextControlMain.setText(DataFormatter.formatDouble(value, DataFormatter.MEDIUM_PRECISION_FORMAT));
                            manualSeekBarChange = true;
                            seekBarControlMain.setProgress((int) (value * 100));
                            manualSeekBarChange = false;
                            if (scienceLab.isConnected()) {
                                scienceLab.setPCS(value);
                                if (!isSet4) {
                                    buttonControlMain3.setBackgroundColor(Color.GREEN);
                                    isSet4 = true;
                                }
                            }
                        } catch (NumberFormatException e) {
                            editTextControlMain.setText(DataFormatter.formatDouble(0, DataFormatter.MEDIUM_PRECISION_FORMAT));
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
                        if (isSet4) {
                            buttonControlMain3.setBackgroundColor(Color.parseColor("#c72c2c"));
                            isSet4 = false;
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {

                    }
                });

                seekBarControlMain.setMax(330);
                DataFormatter.formatDouble(0, DataFormatter.MEDIUM_PRECISION_FORMAT);

                seekBarControlMain.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (!manualSeekBarChange) {
                            double text = map(progress, 0, 330, 0.0, 3.3);
                            editTextControlMain.setText(DataFormatter.formatDouble(text, DataFormatter.MEDIUM_PRECISION_FORMAT));
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        if (isSet4) {
                            buttonControlMain3.setBackgroundColor(Color.parseColor("#c72c2c"));
                            isSet4 = false;
                        }
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });
                break;

            case 4:
                buttonControlMain1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            if (isSet5) {
                                buttonControlMain3.setBackgroundColor(Color.parseColor("#c72c2c"));
                                isSet5 = false;
                            }
                            int data = Integer.parseInt(editTextControlMain.getText().toString());
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

                buttonControlMain2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            if (isSet5) {
                                buttonControlMain3.setBackgroundColor(Color.parseColor("#c72c2c"));
                                isSet5 = false;
                            }
                            int data1 = Integer.parseInt(editTextControlMain.getText().toString());
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

                buttonControlMain3.setOnClickListener(new View.OnClickListener() {
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
                            if (scienceLab.isConnected()) {
                                scienceLab.setSine1(value);
                                if (!isSet5) {
                                    buttonControlMain3.setBackgroundColor(Color.GREEN);
                                    isSet5 = true;
                                }
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
                        if (isSet5) {
                            buttonControlMain3.setBackgroundColor(Color.parseColor("#c72c2c"));
                            isSet5 = false;
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {

                    }
                });

                seekBarControlMain.setMax(4990);
                editTextControlMain.setText("10");

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
                        if (isSet5) {
                            buttonControlMain3.setBackgroundColor(Color.parseColor("#c72c2c"));
                            isSet5 = false;
                        }
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });
                break;

            case 5:
                buttonControlMain1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            if (isSet6) {
                                buttonControlMain3.setBackgroundColor(Color.parseColor("#c72c2c"));
                                isSet6 = false;
                            }
                            int data = Integer.parseInt(editTextControlMain.getText().toString());
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

                buttonControlMain2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            if (isSet6) {
                                buttonControlMain3.setBackgroundColor(Color.parseColor("#c72c2c"));
                                isSet6 = false;
                            }
                            int data1 = Integer.parseInt(editTextControlMain.getText().toString());
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

                buttonControlMain3.setOnClickListener(new View.OnClickListener() {
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
                            if (scienceLab.isConnected()) {
                                scienceLab.setSine2(value);
                                if (!isSet6) {
                                    buttonControlMain3.setBackgroundColor(Color.GREEN);
                                    isSet6 = true;
                                }
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
                        if (isSet6) {
                            buttonControlMain3.setBackgroundColor(Color.parseColor("#c72c2c"));
                            isSet6 = false;
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {

                    }
                });

                seekBarControlMain.setMax(4990);
                editTextControlMain.setText("10");
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
                        if (isSet6) {
                            buttonControlMain3.setBackgroundColor(Color.parseColor("#c72c2c"));
                            isSet6 = false;
                        }
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });
                break;

            case 6:
                buttonControlMain1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            if (isSet7) {
                                buttonControlMain3.setBackgroundColor(Color.parseColor("#c72c2c"));
                                isSet7 = false;
                            }
                            int data = Integer.parseInt(editTextControlMain.getText().toString());
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

                buttonControlMain2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            if (isSet7) {
                                buttonControlMain3.setBackgroundColor(Color.parseColor("#c72c2c"));
                                isSet7 = false;
                            }
                            int data1 = Integer.parseInt(editTextControlMain.getText().toString());
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

                buttonControlMain3.setOnClickListener(new View.OnClickListener() {
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
                            // Setting a SQUARE Wave in SQR1 by default
                            if (scienceLab.isConnected()) {
                                scienceLab.setSqr1(value, -1, false);
                                if (!isSet7) {
                                    buttonControlMain3.setBackgroundColor(Color.GREEN);
                                    isSet7 = true;
                                }
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
                        if (isSet7) {
                            buttonControlMain3.setBackgroundColor(Color.parseColor("#c72c2c"));
                            isSet7 = false;
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {

                    }
                });

                seekBarControlMain.setMax(5000);
                editTextControlMain.setText("10");
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
                        if (isSet7) {
                            buttonControlMain3.setBackgroundColor(Color.parseColor("#c72c2c"));
                            isSet7 = false;
                        }
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });
                break;
        }

    }


    @Override
    public int getItemCount() {
        return mDataset.length;
    }

}
