package io.pslab;

import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class DataFormatter {
    public static final String HIGH_PRECISION_FORMAT = "%.5f";
    public static final String MEDIUM_PRECISION_FORMAT = "%.4f";
    public static final String LOW_PRECISION_FORMAT = "%.2f";
    public static final String MINIMAL_PRECISION_FORMAT = "%.1f";

    public static final char decSeparator = DecimalFormatSymbols.getInstance().getDecimalSeparator();

    public static String formatDouble(double value, String format) {
        return String.format(Locale.getDefault(), format, value);
    }
}