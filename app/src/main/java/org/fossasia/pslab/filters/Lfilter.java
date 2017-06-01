package org.fossasia.pslab.filters;

import android.util.Log;

/**
 * Created by akarshan on 6/1/17.
 */

public class Lfilter {
    String TAG = "Lfilter";
    public double[] filter(double[] b, double[] a, double[] x) {
        double[] filter = null;
        double[] a1 = getRealArrayScalarDiv(a,a[0]);
        double[] b1 = getRealArrayScalarDiv(b,a[0]);
        int sx = x.length;
        filter = new double[sx];
        filter[0] = b1[0]*x[0];
        for (int i = 1; i < sx; i++) {
            filter[i] = 0.0;
            for (int j = 0; j <= i; j++) {
                int k = i-j;
                if (j > 0) {
                    if ((k < b1.length) && (j < x.length)) {
                        filter[i] += b1[k]*x[j];
                    }
                    if ((k < filter.length) && (j < a1.length)) {
                        filter[i] -= a1[j]*filter[k];
                    }
                } else {
                    if ((k < b1.length) && (j < x.length)) {
                        filter[i] += (b1[k]*x[j]);
                    }
                }
            }
        }
        return filter;
    }

    private double[] getRealArrayScalarDiv(double[] dDividend, double dDivisor) {
        if (dDividend == null)
            Log.v(TAG, "The array must be defined or different to null");
        if (dDividend.length == 0) {
            Log.v(TAG, "The size array must be greater than Zero");
        }
        double[] dQuotient = new double[dDividend.length];

        for (int i = 0; i < dDividend.length; i++) {
            if (!(dDivisor == 0.0)) {
                dQuotient[i] = dDividend[i]/dDivisor;
            } else {
                if (dDividend[i] > 0.0) {
                    dQuotient[i] = Double.POSITIVE_INFINITY;
                }
                if (dDividend[i] == 0.0) {
                    dQuotient[i] = Double.NaN;
                }
                if (dDividend[i] < 0.0) {
                    dQuotient[i] = Double.NEGATIVE_INFINITY;
                }
            }
        }
        return dQuotient;
    }
}
