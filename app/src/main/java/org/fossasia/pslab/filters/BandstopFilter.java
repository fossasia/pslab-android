package org.fossasia.pslab.filters;

import android.util.Log;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.complex.Complex;


import java.util.ArrayList;
import java.util.Arrays;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.tan;

/**
 * Created by akarshan on 5/29/17.
 */

public class BandstopFilter {
    String TAG = "BandStopFilter";
    double[] a;
    double[] b;

    public  BandstopFilter(int order, double[] wn){
        int k = 1;
        double z[] = new double[]{};
        Complex p[] = buttap(order);
        double[] warped = new double[wn.length];
        double fs = 2.0;
        for(int i = 0; i < wn.length; i++)
            warped[i] = 2 * fs * tan(Math.PI * wn[i] / fs);
        double bw = warped[1] - warped[0];
        double wo = sqrt(warped[0] * warped[1]);

        ArrayList<Object> zpklp2bsObjectArray = zpklp2bs(z, p, k, wo, bw);
        Complex[] z2 = (Complex[]) zpklp2bsObjectArray.get(0);
        p = (Complex[]) zpklp2bsObjectArray.get(1);
        k = (int) zpklp2bsObjectArray.get(2);

        ArrayList<Object> zpkBilinearObjectArray = zpkBilinear(z2, p, k, fs);
        z2 = (Complex[])zpkBilinearObjectArray.get(0);
        p = (Complex[])zpkBilinearObjectArray.get(1);
        double k2 = (double)zpkBilinearObjectArray.get(2);

        ArrayList<double[]> zpk2tfArray =  zpk2tf(z2, p, k2);
        a =  zpk2tfArray.get(0);
        b =  zpk2tfArray.get(1);
    }

    private Complex[] buttap(int order){
        /*
        Return (z,p,k) for analog prototype of Nth-order Butterworth filter.
        The filter will have an angular (e.g. rad/s) cutoff frequency of 1.
         */
        ArrayList<Integer> m =  new ArrayList<Integer>();
        Complex p[];
        if (Math.abs(order) != order)
            Log.v(TAG, "Filter order must be a nonnegative integer");
        for(int i = -order + 1; i < order; i = i + 2)
            m.add(i);
        p = new Complex[m.size()];
        for(int i = 0; i < m.size(); i++)
            p[i] = new Complex(-cos(m.get(i) * Math.PI / (2 * order)), -sin(m.get(i) * Math.PI / (2 * order)));

        return p;
    }

    private ArrayList<Object> zpklp2bs(double[] z, Complex[] p, int k, double wo, double bw){
        int degree = relativeDegree(z, p);
        double[] zhp = new double[z.length];
        Complex[] php = new Complex[p.length];
        Complex[] zbs = new Complex[degree * 2];
        Complex[] pbs = new Complex[p.length * 2];
        double zProd = 1;
        double pProd = 1;
        int kbs;

        // Invert to a highpass filter with desired bandwidth
        for(int i = 0; i < z.length; i++)
            zhp[i] = (bw / 2) / z[i];

        Complex numerator = new Complex((bw/2));
        for(int i = 0; i < p.length; i++)
            php[i] = numerator.divide(p[i]);

        // Duplicate poles and zeros and shift from baseband to +wo and -wo
        int l = 0;
        for(int i = 0; i < degree; i++)
            zbs[i] = new Complex(0, wo);
        for(int i = 0; i < degree; i++)
            zbs[degree + i] = new Complex(0, -wo);
        for(int i = 0; i < p.length; i++)
            pbs[i] = php[i].add((php[i].pow(2).subtract(Math.pow(wo, 2))).sqrt());
        for(int i = 0; i < p.length; i++)
            pbs[i + p.length] = php[i].subtract((php[i].pow(2).subtract(Math.pow(wo, 2))).sqrt());

        // Cancel out gain change caused by inversion
        Complex temp = new Complex(1,0);
        for(int i = 0; i < p.length; i++)
            temp = temp.multiply(p[i].negate());

        pProd = temp.getReal();
        kbs = (int) (k / pProd);
        return new ArrayList<Object>(Arrays.asList(zbs, pbs, kbs));
    }

    private ArrayList<Object> zpkBilinear(Complex[] z, Complex[] p, int k, double fs){
        //Return a digital filter from an analog one using a bilinear transform.
        Complex[] zz = new Complex[z.length];
        Complex[] pz = new Complex[p.length];
        double kz;
        relativeDegree(z, p);
        double fs2 = 2 * fs;
        Complex complexFs2 = new Complex(fs2);
        for(int i = 0; i < z.length; i++){
            zz[i] = (complexFs2.add(z[i])).divide(complexFs2.subtract(z[i]));
        }
        for(int i = 0; i < p.length; i++){
            pz[i] = (complexFs2.add(p[i])).divide(complexFs2.subtract(p[i]));
        }

        //rearranging pz
        ArrayList<Complex> pz2 = new ArrayList<Complex>();
        for(int i = 0; i < pz.length; i++){
            if(!pz2.contains(pz[i]) && !pz2.contains(pz[i].conjugate())){
                pz2.add(pz[i]);
            }
        }

        for(int i = 0; i < pz.length / 2; i++)
            pz2.add(pz2.get(i).conjugate());

        Complex [] pzRearranged = pz2.toArray(new Complex[pz.length]);

        Complex temp = new Complex(1,0);
        for(int i = 0; i < z.length; i++)
            temp = temp.multiply(complexFs2.subtract(z[i]));


        Complex temp2 = new Complex(1,0);
        for(int i = 0; i < p.length; i++)
            temp2 = temp2.multiply(complexFs2.subtract(p[i]));

        kz =  (k * (temp.divide(temp2)).getReal());
        return new ArrayList<Object>(Arrays.asList(zz, pzRearranged, kz));
    }

    private ArrayList<double[]> zpk2tf(Complex[] z, Complex[] p, double k){
        //Return polynomial transfer function representation from zeros and poles
        double[] zCoefficients = rootsToPolynomial(z);
        double[] bCoefficients = new double[zCoefficients.length];

        for(int i = 0; i < zCoefficients.length; i++)
            bCoefficients[i] = zCoefficients[i] * k;

        double[] aCoefficients = rootsToPolynomial(p);
        ArrayUtils.reverse(aCoefficients);
        return new ArrayList<double[]>(Arrays.asList(aCoefficients,bCoefficients));
    }

    private int relativeDegree(double[] z,Complex[] p){
        int degree = p.length - z.length;
        if(degree < 0) {
            Log.v(TAG, "Improper transfer function.");
            return -1;
        }
        else
            return degree;
    }

    private int relativeDegree(Complex[] z,Complex[] p){
        int degree = p.length - z.length;
        if(degree < 0) {
            Log.v(TAG, "Improper transfer function.");
            return -1;
        }
        else
            return degree;
    }

    private double[] rootsToPolynomial(Complex[] x){
        /*
        Returns a double array of coefficients of the polynomial,
        assuming the each complex root has it's conjugate in the same array.
        */
        PolynomialFunction[] polynomialFunctionArray = new PolynomialFunction[x.length/2];
        PolynomialFunction product = new PolynomialFunction(new double[]{1});
        for(int i = 0; i < x.length / 2; i++){
            PolynomialFunction complexRoot = new PolynomialFunction(new double[]{-x[i].getReal(), 1});
            complexRoot = complexRoot.multiply(complexRoot);
            complexRoot = complexRoot.subtract(new PolynomialFunction(new double[]{-1 * x[i].getImaginary() * x[i].getImaginary()}));
            System.out.println((complexRoot));
            polynomialFunctionArray[i] = complexRoot;
        }

        for(int i = 0; i < polynomialFunctionArray.length; i ++)
            product = polynomialFunctionArray[i].multiply(product);

        return product.getCoefficients();
    }

    public ArrayList<double[]> abGetter(){
        return new ArrayList<double[]>(Arrays.asList(b, a));
    }
}
