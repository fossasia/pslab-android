package org.fossasia.pslab.adapters;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
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
            mCardView = (CardView) view.findViewById(R.id.cardview_control_main);
            tvControlMain1 = (TextView) view.findViewById(R.id.tv_control_main1);
            tvControlMain2 = (TextView) view.findViewById(R.id.tv_control_main2);
            editTextControlMain = (EditText) view.findViewById(R.id.edittext_control_main);
            buttonControlMain1 = (Button) view.findViewById(R.id.button_control_main1);
            buttonControlMain2 = (Button) view.findViewById(R.id.button_control_main2);
            buttonControlMain3 = (Button) view.findViewById(R.id.button_control_main3);
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

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.setIsRecyclable(true);
        holder.tvControlMain1.setText(mDataset[position]);

        final Button buttonControlMain1 = holder.buttonControlMain1;
        final Button buttonControlMain2 = holder.buttonControlMain2;
        Button buttonControlMain3 = holder.buttonControlMain3;

        final SeekBar seekBarControlMain = holder.seekBarControlMain;
        final EditText editTextControlMain = holder.editTextControlMain;

        switch (position) {
            case 0:
                buttonControlMain1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            Double data = Double.valueOf(editTextControlMain.getText().toString());
                            Double dataDecrement = data - 0.0025;
                            if (dataDecrement < -5.0)
                                dataDecrement = -5.0;
                            else if (dataDecrement > 5.0)
                                dataDecrement = 5.0;
                            int setProgress = (int) ((dataDecrement + 5) * 100);
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

                buttonControlMain2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            Double data1 = Double.valueOf(editTextControlMain.getText().toString());
                            Double dataIncrement = data1 + 0.0025;
                            if (dataIncrement < -5.0)
                                dataIncrement = -5.0;
                            else if (dataIncrement > 5.0)
                                dataIncrement = 5.0;
                            int setProgress = (int) ((dataIncrement + 5) * 100);
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

                buttonControlMain3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            Float value = Float.parseFloat(editTextControlMain.getText().toString());
                            if (value > 5)
                                value = 5f;
                            else if (value < -5)
                                value = -5f;
                            editTextControlMain.setText(String.valueOf(value));
                            manualSeekBarChange = true;
                            seekBarControlMain.setProgress((int) map(value, -5, 5, 0, 1000));
                            manualSeekBarChange = false;
                            ControlActivityCommon.editTextValues.put(mDataset[position], value);
                            if (scienceLab.isConnected())
                                scienceLab.setPV1(value);
                        } catch (NumberFormatException e) {
                            editTextControlMain.setText("0.0000");
                        }

                    }
                });
                seekBarControlMain.setMax(1000);
                if (svalue.get(mDataset[position]) != null) {
                    editTextControlMain.setText(svalue.get(mDataset[position]).toString());
                    seekBarControlMain.setProgress((int) map(svalue.get(mDataset[position]), -5, 5, 0, 1000));
                } else {
                    editTextControlMain.setText("-5.0000");
                }


                seekBarControlMain.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (!manualSeekBarChange) {
                            double text = map(progress, 0, 1000, -5.0, 5.0);
                            DecimalFormat df = new DecimalFormat("0.0000");
                            editTextControlMain.setText(df.format(text));
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

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
                            Double data = Double.valueOf(editTextControlMain.getText().toString());
                            Double dataDecrement = data - 0.0025;
                            if (dataDecrement < -3.3)
                                dataDecrement = -3.3;
                            else if (dataDecrement > 3.3)
                                dataDecrement = 3.3;
                            int setProgress = (int) ((dataDecrement + 3.3) * 100);
                            manualSeekBarChange = true;
                            seekBarControlMain.setProgress(setProgress);
                            manualSeekBarChange = false;
                            DecimalFormat df = new DecimalFormat("0.0000");
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
                            Double data1 = Double.valueOf(editTextControlMain.getText().toString());
                            Double dataIncrement = data1 + 0.0025;
                            if (dataIncrement < -3.3)
                                dataIncrement = -3.3;
                            else if (dataIncrement > 3.3)
                                dataIncrement = 3.3;
                            int setProgress = (int) ((dataIncrement + 3.3) * 100);
                            manualSeekBarChange = true;
                            seekBarControlMain.setProgress(setProgress);
                            manualSeekBarChange = false;
                            DecimalFormat df = new DecimalFormat("0.0000");
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
                            Float value = Float.parseFloat(editTextControlMain.getText().toString());
                            if (value > 3.3)
                                value = 3.3f;
                            else if (value < -3.3)
                                value = -3.3f;
                            editTextControlMain.setText(String.valueOf(value));
                            manualSeekBarChange = true;
                            seekBarControlMain.setProgress((int) ((value + 3.3) * 100));
                            manualSeekBarChange = false;
                            ControlActivityCommon.editTextValues.put(mDataset[position], value);
                            if (scienceLab.isConnected())
                                scienceLab.setPV2(value);
                        } catch (NumberFormatException e) {
                            editTextControlMain.setText("0");
                        }
                    }
                });


                seekBarControlMain.setMax(660);
                if (svalue.get(mDataset[position]) != null) {
                    editTextControlMain.setText(svalue.get(mDataset[position]).toString());
                    seekBarControlMain.setProgress((int) ((svalue.get(mDataset[position]) + 3.3) * 100));

                } else {
                    editTextControlMain.setText("-3.3000");
                }

                seekBarControlMain.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (!manualSeekBarChange) {
                            double text = map(progress, 0, 660, -3.3, 3.3);
                            DecimalFormat df = new DecimalFormat("0.0000");
                            editTextControlMain.setText(df.format(text));
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

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
                            Double data = Double.valueOf(editTextControlMain.getText().toString());
                            Double dataDecrement = data - 0.0025;
                            if (dataDecrement < 0.0)
                                dataDecrement = 0.0;
                            else if (dataDecrement > 3.3)
                                dataDecrement = 3.3;
                            manualSeekBarChange = true;
                            seekBarControlMain.setProgress((int) (dataDecrement * 100));
                            manualSeekBarChange = false;
                            DecimalFormat df = new DecimalFormat("0.0000");
                            editTextControlMain.setText(df.format(dataDecrement));
                        } catch (NumberFormatException e) {
                            editTextControlMain.setText("0.0000");
                        }
                    }
                });

                buttonControlMain2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            Double data1 = Double.valueOf(editTextControlMain.getText().toString());
                            Double dataIncrement = data1 + 0.0025;
                            if (dataIncrement < 0.0)
                                dataIncrement = 0.0;
                            else if (dataIncrement > 3.3)
                                dataIncrement = 3.3;
                            manualSeekBarChange = true;
                            seekBarControlMain.setProgress((int) (dataIncrement * 100));
                            manualSeekBarChange = false;
                            DecimalFormat df = new DecimalFormat("0.0000");
                            editTextControlMain.setText(df.format(dataIncrement));
                        } catch (NumberFormatException e) {
                            editTextControlMain.setText("0.0000");
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
                            editTextControlMain.setText(String.valueOf(value));
                            manualSeekBarChange = true;
                            seekBarControlMain.setProgress((int) map(value, 0, 3.3, 0, 330));
                            manualSeekBarChange = false;
                            ControlActivityCommon.editTextValues.put(mDataset[position], value);
                            if (scienceLab.isConnected())
                                scienceLab.setPV3(value);
                        } catch (NumberFormatException e) {
                            editTextControlMain.setText("0.0000");
                        }

                    }
                });


                seekBarControlMain.setMax(330);
                if (svalue.get(mDataset[position]) != null) {
                    editTextControlMain.setText(svalue.get(mDataset[position]).toString());
                    seekBarControlMain.setProgress((int) map(svalue.get(mDataset[position]), 0, 3.3, 0, 330));
                } else {
                    editTextControlMain.setText("0.0000");
                }


                seekBarControlMain.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (!manualSeekBarChange) {
                            double text = map(progress, 0, 330, 0, 3.3);
                            DecimalFormat df = new DecimalFormat("0.0000");
                            editTextControlMain.setText(df.format(text));
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

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
                            Double data = Double.valueOf(editTextControlMain.getText().toString());
                            Double dataDecrement = data - 0.0025;
                            if (dataDecrement < 0.0)
                                dataDecrement = 0.0;
                            else if (dataDecrement > 3.3)
                                dataDecrement = 3.3;
                            manualSeekBarChange = true;
                            seekBarControlMain.setProgress((int) (dataDecrement * 100));
                            manualSeekBarChange = false;
                            DecimalFormat df = new DecimalFormat("0.0000");
                            editTextControlMain.setText(df.format(dataDecrement));
                        } catch (NumberFormatException e) {
                            editTextControlMain.setText("0.0000");
                        }
                    }
                });

                buttonControlMain2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            Double data1 = Double.valueOf(editTextControlMain.getText().toString());
                            Double dataIncrement = data1 + 0.0025;
                            if (dataIncrement < 0.0)
                                dataIncrement = 0.0;
                            else if (dataIncrement > 3.3)
                                dataIncrement = 3.3;
                            manualSeekBarChange = true;
                            seekBarControlMain.setProgress((int) (dataIncrement * 100));
                            manualSeekBarChange = false;
                            DecimalFormat df = new DecimalFormat("0.0000");
                            editTextControlMain.setText(df.format(dataIncrement));
                        } catch (NumberFormatException e) {
                            editTextControlMain.setText("0.0000");
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
                            editTextControlMain.setText(String.valueOf(value));
                            manualSeekBarChange = true;
                            seekBarControlMain.setProgress((int) (value * 100));
                            manualSeekBarChange = false;
                            ControlActivityCommon.editTextValues.put(mDataset[position], value);
                            if (scienceLab.isConnected())
                                scienceLab.setPCS(value);
                        } catch (NumberFormatException e) {
                            editTextControlMain.setText("0.0000");
                        }
                    }
                });


                seekBarControlMain.setMax(330);
                if (svalue.get(mDataset[position]) != null) {
                    editTextControlMain.setText(svalue.get(mDataset[position]).toString());
                    seekBarControlMain.setProgress((int) (svalue.get(mDataset[position]) * 100));
                } else {
                    editTextControlMain.setText("0.0000");
                }

                seekBarControlMain.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (!manualSeekBarChange) {
                            double text = map(progress, 0, 330, 0.0, 3.3);
                            DecimalFormat df = new DecimalFormat("0.0000");
                            editTextControlMain.setText(df.format(text));
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

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

                buttonControlMain2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
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
                            ControlActivityCommon.editTextValues.put(mDataset[position], (float) value);
                            if (scienceLab.isConnected())
                                scienceLab.setSine1(value);
                        } catch (NumberFormatException e) {
                            editTextControlMain.setText("0");
                        }
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

                buttonControlMain2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
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
                            ControlActivityCommon.editTextValues.put(mDataset[position], (float) value);
                            if (scienceLab.isConnected())
                                scienceLab.setSine2(value);
                        } catch (NumberFormatException e) {
                            editTextControlMain.setText("0");
                        }
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

                buttonControlMain2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
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
                            ControlActivityCommon.editTextValues.put(mDataset[position], (float) value);
                            // Setting a SQUARE Wave in SQR1 by default
                            if (scienceLab.isConnected())
                                scienceLab.setSqr1(value, -1, false);

                        } catch (NumberFormatException e) {
                            editTextControlMain.setText("0");
                        }
                    }
                });
                seekBarControlMain.setMax(5000);
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
