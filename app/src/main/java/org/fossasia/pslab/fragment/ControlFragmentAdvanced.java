package org.fossasia.pslab.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.fossasia.pslab.PSLabApplication;
import org.fossasia.pslab.R;
import org.fossasia.pslab.communication.ScienceLab;
import org.fossasia.pslab.others.EditTextWidget;
import org.fossasia.pslab.others.ScienceLabCommon;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by asitava on 6/6/17.
 */

public class ControlFragmentAdvanced extends Fragment {

    private ScienceLab scienceLab;
    private Map<String, Integer> state = new HashMap<>();

    public static ControlFragmentAdvanced newInstance() {
        return new ControlFragmentAdvanced();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scienceLab = ScienceLabCommon.scienceLab;
        state.put("SQR1", 0);
        state.put("SQR2", 0);
        state.put("SQR3", 0);
        state.put("SQR4", 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_control_advanced, container, false);

        Button buttonControlAdvanced1 = (Button) view.findViewById(R.id.button_control_advanced1);
        Button buttonControlAdvanced2 = (Button) view.findViewById(R.id.button_control_advanced2);

        final EditText etWidgetControlAdvanced1 = (EditText) view.findViewById(R.id.etwidget_control_advanced1);
        final EditText etWidgetControlAdvanced2 = (EditText) view.findViewById(R.id.etwidget_control_advanced2);
        final EditText etWidgetControlAdvanced3 = (EditText) view.findViewById(R.id.etwidget_control_advanced3);
        final EditText etWidgetControlAdvanced4 = (EditText) view.findViewById(R.id.etwidget_control_advanced4);
        final EditText etWidgetControlAdvanced5 = (EditText) view.findViewById(R.id.etwidget_control_advanced5);
        final EditText etWidgetControlAdvanced6 = (EditText) view.findViewById(R.id.etwidget_control_advanced6);
        final EditText etWidgetControlAdvanced7 = (EditText) view.findViewById(R.id.etwidget_control_advanced7);
        final EditText etWidgetControlAdvanced8 = (EditText) view.findViewById(R.id.etwidget_control_advanced8);
        final EditText etWidgetControlAdvanced9 = (EditText) view.findViewById(R.id.etwidget_control_advanced9);
        final EditText etWidgetControlAdvanced10 = (EditText) view.findViewById(R.id.etwidget_control_advanced10);
        final EditText etWidgetControlAdvanced11 = (EditText) view.findViewById(R.id.etwidget_control_advanced11);
        final Spinner spinnerControlAdvanced1 = (Spinner) view.findViewById(R.id.spinner_control_advanced1);
        final Spinner spinnerControlAdvanced2 = (Spinner) view.findViewById(R.id.spinner_control_advanced2);

        CheckBox checkBoxControlAdvanced1 = (CheckBox) view.findViewById(R.id.checkbox_control_advanced1);
        CheckBox checkBoxControlAdvanced2 = (CheckBox) view.findViewById(R.id.checkbox_control_advanced2);
        CheckBox checkBoxControlAdvanced3 = (CheckBox) view.findViewById(R.id.checkbox_control_advanced3);
        CheckBox checkBoxControlAdvanced4 = (CheckBox) view.findViewById(R.id.checkbox_control_advanced4);

        etWidgetControlAdvanced1.setInputType(InputType.TYPE_NULL);
        etWidgetControlAdvanced2.setInputType(InputType.TYPE_NULL);
        etWidgetControlAdvanced3.setInputType(InputType.TYPE_NULL);
        etWidgetControlAdvanced4.setInputType(InputType.TYPE_NULL);
        etWidgetControlAdvanced5.setInputType(InputType.TYPE_NULL);
        etWidgetControlAdvanced6.setInputType(InputType.TYPE_NULL);
        etWidgetControlAdvanced7.setInputType(InputType.TYPE_NULL);
        etWidgetControlAdvanced8.setInputType(InputType.TYPE_NULL);
        etWidgetControlAdvanced9.setInputType(InputType.TYPE_NULL);
        etWidgetControlAdvanced10.setInputType(InputType.TYPE_NULL);
        etWidgetControlAdvanced11.setInputType(InputType.TYPE_NULL);

        etWidgetControlAdvanced1.setText("10.0");
        etWidgetControlAdvanced2.setText("10.0");
        etWidgetControlAdvanced3.setText("10.0");
        etWidgetControlAdvanced4.setText("0.0");
        etWidgetControlAdvanced5.setText("0.0");
        etWidgetControlAdvanced6.setText("0.0");
        etWidgetControlAdvanced7.setText("0.0");
        etWidgetControlAdvanced8.setText("0.0");
        etWidgetControlAdvanced9.setText("0.0");
        etWidgetControlAdvanced10.setText("0.0");
        etWidgetControlAdvanced11.setText("0.0");

        etWidgetControlAdvanced1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                showInputDialog(etWidgetControlAdvanced1, 1.0, 10.0, 5000.0);
            }
        });

        etWidgetControlAdvanced1.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showInputDialog(etWidgetControlAdvanced1, 1.0, 10.0, 5000.0);
                }
            }
        });

        etWidgetControlAdvanced2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                showInputDialog(etWidgetControlAdvanced2, 1.0, 10.0, 5000.0);

            }
        });

        etWidgetControlAdvanced2.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showInputDialog(etWidgetControlAdvanced2, 1.0, 10.0, 5000.0);
                }
            }
        });

        etWidgetControlAdvanced3.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                showInputDialog(etWidgetControlAdvanced3, 1.0, 0.0, 360.0);

            }
        });

        etWidgetControlAdvanced3.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showInputDialog(etWidgetControlAdvanced3, 1.0, 0.0, 360.0);
                }
            }
        });

        etWidgetControlAdvanced4.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                showInputDialog(etWidgetControlAdvanced4, 0.1, 0.0, 1.0);
            }
        });

        etWidgetControlAdvanced4.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showInputDialog(etWidgetControlAdvanced4, 0.1, 0.0, 1.0);
                }
            }
        });

        etWidgetControlAdvanced5.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                showInputDialog(etWidgetControlAdvanced5, 1.0, 0.0, 360.0);
            }
        });

        etWidgetControlAdvanced5.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showInputDialog(etWidgetControlAdvanced5, 1.0, 0.0, 360.0);
                }
            }
        });

        etWidgetControlAdvanced6.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                showInputDialog(etWidgetControlAdvanced6, 0.1, 0.0, 1.0);

            }
        });

        etWidgetControlAdvanced6.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showInputDialog(etWidgetControlAdvanced6, 0.1, 0.0, 1.0);
                }
            }
        });

        etWidgetControlAdvanced7.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                showInputDialog(etWidgetControlAdvanced7, 1.0, 0.0, 360.0);

            }
        });

        etWidgetControlAdvanced7.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showInputDialog(etWidgetControlAdvanced7, 1.0, 0.0, 360.0);
                }
            }
        });

        etWidgetControlAdvanced8.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                showInputDialog(etWidgetControlAdvanced8, 0.1, 0.0, 1.0);

            }
        });

        etWidgetControlAdvanced8.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showInputDialog(etWidgetControlAdvanced8, 0.1, 0.0, 1.0);
                }
            }
        });

        etWidgetControlAdvanced9.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                showInputDialog(etWidgetControlAdvanced9, 1.0, 0.0, 360.0);

            }
        });

        etWidgetControlAdvanced9.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showInputDialog(etWidgetControlAdvanced9, 1.0, 0.0, 360.0);
                }
            }
        });

        etWidgetControlAdvanced10.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                showInputDialog(etWidgetControlAdvanced10, 0.1, 0.0, 1.0);

            }
        });

        etWidgetControlAdvanced10.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showInputDialog(etWidgetControlAdvanced10, 0.1, 0.0, 1.0);
                }
            }
        });

        etWidgetControlAdvanced11.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                showInputDialog(etWidgetControlAdvanced11, 1.0, 10.0, 5000.0);

            }
        });

        etWidgetControlAdvanced11.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showInputDialog(etWidgetControlAdvanced11, 1.0, 10.0, 5000.0);
                }
            }
        });

        buttonControlAdvanced1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Double frequencyW1 = Double.valueOf(etWidgetControlAdvanced1.getText().toString());
                    Double frequencyW2 = Double.valueOf(etWidgetControlAdvanced2.getText().toString());
                    float phase = Float.valueOf(etWidgetControlAdvanced3.getText().toString());

                    String wavetypeW1 = spinnerControlAdvanced1.getSelectedItem().toString();
                    String wavetypeW2 = spinnerControlAdvanced2.getSelectedItem().toString();

                    if ("SINE".equals(wavetypeW1) && scienceLab.isConnected())
                        scienceLab.setSine1(frequencyW1);
                    else if ("SQUARE".equals(wavetypeW1) && scienceLab.isConnected())
                        scienceLab.setSqr1(frequencyW1, -1, false);

                    if ("SINE".equals(wavetypeW2) && scienceLab.isConnected())
                        scienceLab.setSine2(frequencyW2);
                    else if ("SQUARE".equals(wavetypeW2) && scienceLab.isConnected())
                        scienceLab.setSqr2(frequencyW2, -1);
                } catch (NumberFormatException e) {
                    etWidgetControlAdvanced1.setText("0");
                    etWidgetControlAdvanced2.setText("0");
                    etWidgetControlAdvanced3.setText("0");
                }
            }
        });

        buttonControlAdvanced2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    double phase2 = Double.valueOf(etWidgetControlAdvanced5.getText().toString());
                    double phase3 = Double.valueOf(etWidgetControlAdvanced7.getText().toString());
                    double phase4 = Double.valueOf(etWidgetControlAdvanced9.getText().toString());

                    double dutyCycle1 = Double.valueOf(etWidgetControlAdvanced4.getText().toString());
                    double dutyCycle2 = Double.valueOf(etWidgetControlAdvanced6.getText().toString());
                    double dutyCycle3 = Double.valueOf(etWidgetControlAdvanced8.getText().toString());
                    double dutyCycle4 = Double.valueOf(etWidgetControlAdvanced10.getText().toString());

                    double frequency = Double.valueOf(etWidgetControlAdvanced11.getText().toString());

                    if (scienceLab.isConnected())
                        scienceLab.sqrPWM(frequency, dutyCycle1, phase2, dutyCycle2, phase3, dutyCycle3,
                                phase4, dutyCycle4, true);
                } catch (NumberFormatException e) {
                    etWidgetControlAdvanced4.setText("0");
                    etWidgetControlAdvanced5.setText("0");
                    etWidgetControlAdvanced6.setText("0");
                    etWidgetControlAdvanced7.setText("0");
                    etWidgetControlAdvanced8.setText("0");
                    etWidgetControlAdvanced9.setText("0");
                    etWidgetControlAdvanced10.setText("0");
                    etWidgetControlAdvanced11.setText("0");
                }
            }
        });

        checkBoxControlAdvanced1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) state.put("SQR1", 1);
                else state.put("SQR1", 0);
                if (scienceLab.isConnected())
                    scienceLab.setState(state);
            }
        });

        checkBoxControlAdvanced2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) state.put("SQR2", 1);
                else state.put("SQR2", 0);
                if (scienceLab.isConnected())
                    scienceLab.setState(state);
            }
        });

        checkBoxControlAdvanced3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) state.put("SQR3", 1);
                else state.put("SQR3", 0);
                if (scienceLab.isConnected())
                    scienceLab.setState(state);
            }
        });

        checkBoxControlAdvanced4.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) state.put("SQR4", 1);
                else state.put("SQR4", 0);
                if (scienceLab.isConnected())
                    scienceLab.setState(state);
            }
        });

        return view;
    }

    private void showInputDialog(final EditText et, final double leastCount, final double minima, final double maxima) {
        LayoutInflater li = LayoutInflater.from(getContext());
        View promptsView = li.inflate(R.layout.dialog_input_edit_text_widget, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());

        alertDialogBuilder.setView(promptsView);
        final EditTextWidget userInput =
                (EditTextWidget) promptsView.findViewById(R.id.editTextDialogUserInput);

        userInput.init(getContext(), leastCount, minima, maxima);
        userInput.setText(et.getText().toString());

        alertDialogBuilder
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // get user input and set it to result
                                // edit text
                                String input = userInput.getText();

                                if (Double.valueOf(input) > maxima) {
                                    input = String.valueOf(maxima);
                                    Toast.makeText(getContext(), "The Maximum value for this field is " + maxima, Toast.LENGTH_SHORT).show();
                                }
                                if (Double.valueOf(input) < minima) {
                                    input = String.valueOf(minima);
                                    Toast.makeText(getContext(), "The Minimum value for this field is " + minima, Toast.LENGTH_SHORT).show();
                                }


                                et.setText(input);
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        ((PSLabApplication)getActivity().getApplication()).refWatcher.watch(this, ControlFragmentAdvanced.class.getSimpleName());
    }

}
