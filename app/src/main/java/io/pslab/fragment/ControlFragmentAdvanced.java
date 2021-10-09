package io.pslab.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;

import io.pslab.DataFormatter;
import io.pslab.R;
import io.pslab.communication.ScienceLab;
import io.pslab.others.CustomSnackBar;
import io.pslab.others.EditTextWidget;
import io.pslab.others.ScienceLabCommon;

import java.util.HashMap;
import java.util.Map;


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

        Button buttonControlAdvanced1 = view.findViewById(R.id.button_control_advanced1);
        Button buttonControlAdvanced2 = view.findViewById(R.id.button_control_advanced2);

        final EditText etWidgetControlAdvanced1 = view.findViewById(R.id.etwidget_control_advanced1);
        final EditText etWidgetControlAdvanced2 = view.findViewById(R.id.etwidget_control_advanced2);
        final EditText etWidgetControlAdvanced3 = view.findViewById(R.id.etwidget_control_advanced3);
        final EditText etWidgetControlAdvanced4 = view.findViewById(R.id.etwidget_control_advanced4);
        final EditText etWidgetControlAdvanced5 = view.findViewById(R.id.etwidget_control_advanced5);
        final EditText etWidgetControlAdvanced6 = view.findViewById(R.id.etwidget_control_advanced6);
        final EditText etWidgetControlAdvanced7 = view.findViewById(R.id.etwidget_control_advanced7);
        final EditText etWidgetControlAdvanced8 = view.findViewById(R.id.etwidget_control_advanced8);
        final EditText etWidgetControlAdvanced9 = view.findViewById(R.id.etwidget_control_advanced9);
        final EditText etWidgetControlAdvanced10 = view.findViewById(R.id.etwidget_control_advanced10);
        final EditText etWidgetControlAdvanced11 = view.findViewById(R.id.etwidget_control_advanced11);
        final Spinner spinnerControlAdvanced1 = view.findViewById(R.id.spinner_control_advanced1);
        final Spinner spinnerControlAdvanced2 = view.findViewById(R.id.spinner_control_advanced2);

        CheckBox checkBoxControlAdvanced1 = view.findViewById(R.id.checkbox_control_advanced1);
        CheckBox checkBoxControlAdvanced2 = view.findViewById(R.id.checkbox_control_advanced2);
        CheckBox checkBoxControlAdvanced3 = view.findViewById(R.id.checkbox_control_advanced3);
        CheckBox checkBoxControlAdvanced4 = view.findViewById(R.id.checkbox_control_advanced4);

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

        etWidgetControlAdvanced1.setText(DataFormatter.formatDouble(10, DataFormatter.MINIMAL_PRECISION_FORMAT));
        etWidgetControlAdvanced2.setText(DataFormatter.formatDouble(10, DataFormatter.MINIMAL_PRECISION_FORMAT));
        etWidgetControlAdvanced3.setText(DataFormatter.formatDouble(10, DataFormatter.MINIMAL_PRECISION_FORMAT));
        etWidgetControlAdvanced4.setText(DataFormatter.formatDouble(0, DataFormatter.MINIMAL_PRECISION_FORMAT));
        etWidgetControlAdvanced5.setText(DataFormatter.formatDouble(0, DataFormatter.MINIMAL_PRECISION_FORMAT));
        etWidgetControlAdvanced6.setText(DataFormatter.formatDouble(0, DataFormatter.MINIMAL_PRECISION_FORMAT));
        etWidgetControlAdvanced7.setText(DataFormatter.formatDouble(0, DataFormatter.MINIMAL_PRECISION_FORMAT));
        etWidgetControlAdvanced8.setText(DataFormatter.formatDouble(0, DataFormatter.MINIMAL_PRECISION_FORMAT));
        etWidgetControlAdvanced9.setText(DataFormatter.formatDouble(0, DataFormatter.MINIMAL_PRECISION_FORMAT));
        etWidgetControlAdvanced10.setText(DataFormatter.formatDouble(0, DataFormatter.MINIMAL_PRECISION_FORMAT));
        etWidgetControlAdvanced11.setText(DataFormatter.formatDouble(0, DataFormatter.MINIMAL_PRECISION_FORMAT));
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
                    Double frequencySI1 = Double.parseDouble(etWidgetControlAdvanced1.getText().toString());
                    Double frequencySI2 = Double.parseDouble(etWidgetControlAdvanced2.getText().toString());
                    float phase = Float.parseFloat(etWidgetControlAdvanced3.getText().toString());

                    String wavetypeSI1 = spinnerControlAdvanced1.getSelectedItem().toString();
                    String wavetypeSI2 = spinnerControlAdvanced2.getSelectedItem().toString();

                    if ("SINE".equals(wavetypeSI1) && scienceLab.isConnected())
                        scienceLab.setSine1(frequencySI1);
                    else if ("SQUARE".equals(wavetypeSI1) && scienceLab.isConnected())
                        scienceLab.setSqr1(frequencySI1, -1, false);

                    if ("SINE".equals(wavetypeSI2) && scienceLab.isConnected())
                        scienceLab.setSine2(frequencySI2);
                    else if ("SQUARE".equals(wavetypeSI2) && scienceLab.isConnected())
                        scienceLab.setSqr2(frequencySI2, -1);
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
                    double phase2 = Double.parseDouble(etWidgetControlAdvanced5.getText().toString());
                    double phase3 = Double.parseDouble(etWidgetControlAdvanced7.getText().toString());
                    double phase4 = Double.parseDouble(etWidgetControlAdvanced9.getText().toString());

                    double dutyCycle1 = Double.parseDouble(etWidgetControlAdvanced4.getText().toString());
                    double dutyCycle2 = Double.parseDouble(etWidgetControlAdvanced6.getText().toString());
                    double dutyCycle3 = Double.parseDouble(etWidgetControlAdvanced8.getText().toString());
                    double dutyCycle4 = Double.parseDouble(etWidgetControlAdvanced10.getText().toString());

                    double frequency = Double.parseDouble(etWidgetControlAdvanced11.getText().toString());

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
                promptsView.findViewById(R.id.editTextDialogUserInput);

        userInput.init(getContext(), leastCount, minima, maxima);
        userInput.setText(et.getText().toString());

        alertDialogBuilder
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // get user input and set it to result
                                // edit text
                                String input = userInput.getText();

                                if (Double.parseDouble(input) > maxima) {
                                    input = DataFormatter.formatDouble(maxima, DataFormatter.LOW_PRECISION_FORMAT);
                                    CustomSnackBar.showSnackBar(getActivity().findViewById(android.R.id.content),
                                            "The Maximum value for this field is " + maxima,null,null, Snackbar.LENGTH_SHORT);
                                }
                                if (Double.parseDouble(input) < minima) {
                                    input = DataFormatter.formatDouble(minima, DataFormatter.MEDIUM_PRECISION_FORMAT);
                                    CustomSnackBar.showSnackBar(getActivity().findViewById(android.R.id.content),
                                            "The Minimum value for this field is " + minima,null,null, Snackbar.LENGTH_SHORT);
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
}
