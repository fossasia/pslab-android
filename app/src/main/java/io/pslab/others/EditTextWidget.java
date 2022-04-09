package io.pslab.others;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import io.pslab.DataFormatter;
import io.pslab.R;


/**
 * Created by asitava on 22/6/17.
 */

public class EditTextWidget extends LinearLayout{

    private EditText editText;
    private Button button1;
    private Button button2;
    private double leastCount;
    private double maxima;
    private double minima;

    public EditTextWidget(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        applyAttrs(attrs);
    }

    public EditTextWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        applyAttrs(attrs);
    }

    public EditTextWidget(Context context) {
        super(context);
    }

    public void init(Context context, final double leastCount, final double minima, final double maxima) {
        View.inflate(context, R.layout.edittext_control, this);
        editText = findViewById(R.id.edittext_control);
        editText.setText("0");
        button1 = findViewById(R.id.button_control_plus);
        button2 = findViewById(R.id.button_control_minus);

        button1.setEnabled(false);
        button2.setEnabled(false);

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().equals("")){
                    button1.setEnabled(true);
                    button2.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        button1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    double data = Double.parseDouble(editText.getText().toString());
                    data = data - leastCount;
                    data = Math.round(data*100.0)/100.0;
                    data = data > maxima ? maxima : data;
                    data = data < minima ? minima : data;
                    String editTextValue = DataFormatter.formatDouble(data, DataFormatter.MEDIUM_PRECISION_FORMAT);
                    editText.setText(editTextValue);
                    editText.setSelection(editTextValue.length());
                } catch (Exception e) {
                    editText.setText("0");
                    editText.setSelection(1);
                }
            }
        });

        button2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    double data = Double.valueOf(editText.getText().toString());
                    data = data + leastCount;
                    data = Math.round(data*100.0)/100.0;
                    data = data > maxima ? maxima : data;
                    data = data < minima ? minima : data;
                    String editTextValue = DataFormatter.formatDouble(data, DataFormatter.MEDIUM_PRECISION_FORMAT);
                    editText.setText(editTextValue);
                    editText.setSelection(editTextValue.length());
                } catch (Exception e) {
                    editText.setText("0");
                    editText.setSelection(1);
                }
            }
        });
    }

    private void applyAttrs(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.EditTextWidget);
        final int N = a.getIndexCount();
        for (int i = 0; i < N; ++i) {
            int attr = a.getIndex(i);
            switch (attr) {
                case R.styleable.EditTextWidget_leastcount:
                    this.leastCount = a.getFloat(attr, 1.0f);
                    break;
                case R.styleable.EditTextWidget_maxima:
                    this.maxima = a.getFloat(attr, 1.0f);
                    break;
                case R.styleable.EditTextWidget_minima:
                    this.minima = a.getFloat(attr, 1.0f);
            }
        }
        a.recycle();
    }

    public String getText() {
        return  editText.getText().toString();
    }

    public void setText(String text) {
        editText.setText(text);
        editText.setSelection(text.length());
    }
}
