package io.pslab;

import android.text.InputFilter;
import android.text.Spanned;

public class InputMinMaxFilter implements InputFilter {

    private int min;
    private int max;

    public InputMinMaxFilter(int min, int max) {
        this.min = min;
        this.max = max;
    }
    public InputMinMaxFilter(String min, String max) {
        this.min = Integer.parseInt(min);
        this.max = Integer.parseInt(max);
    }
    private boolean isInRange(int a, int b, int c) {
        return b > a ? c >= a && c <= b : c >= b && c <= a;
    }
    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        try {
            String newVal = dest.toString().substring(0, dstart) + dest.toString().substring(dend, dest.toString().length());
            newVal = newVal.substring(0, dstart) + source.toString() + newVal.substring(dstart, newVal.length());
            int input = Integer.parseInt(newVal);
            if (newVal.length() >= 4)
                return "";
            if (isInRange(min, max, input))
                return null;
        } catch (NumberFormatException nfe) {
            }
        return "";
    }
}
