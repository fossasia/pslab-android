package org.fossasia.pslab.others;

/**
 * Created by akarshan on 6/10/17.
 */

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.SeekBar;

import org.fossasia.pslab.R;

import java.text.DecimalFormat;


/**
 * Created by akarshan on 4/10/17.
 */

public class FloatSeekBar extends android.support.v7.widget.AppCompatSeekBar {
    private double max = 3.0;
    private double min = 0.0;

    public FloatSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        applyAttrs(attrs);
    }

    public FloatSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        applyAttrs(attrs);
    }

    public FloatSeekBar(Context context) {
        super(context);
    }

    public double getValue() {
        DecimalFormat df = new DecimalFormat("#.##");
        Double value = (max - min) * ((float) getProgress() / (float) getMax()) + min;
        value = Double.valueOf(df.format(value));
        return value;
    }

    public void setValue(double value) {
        setProgress((int) ((value - min) / (max - min) * getMax()));
    }

    private void applyAttrs(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.FloatSeekBar);
        final int N = a.getIndexCount();
        for (int i = 0; i < N; ++i) {
            int attr = a.getIndex(i);
            switch (attr) {
                case R.styleable.FloatSeekBar_floatMax:
                    this.max = a.getFloat(attr, 1.0f);
                    break;
                case R.styleable.FloatSeekBar_floatMin:
                    this.min = a.getFloat(attr, 0.0f);
                    break;
            }
        }
        a.recycle();
    }
    public void setters(double a, double b)
    {
        min = a;
        max = b;
    }
}
