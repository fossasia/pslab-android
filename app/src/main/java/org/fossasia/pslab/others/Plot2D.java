package org.fossasia.pslab.others;

/**
 * Created by akarshan on 7/15/17.
 */


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

public class Plot2D extends View {

    private Paint paint;
    private float[] xValues = {0.0f};
    private float[] yValues = {0.0f};
    private float maxX = 0.0f, maxY = 0.0f, minX = 0.0f, minY = 0.0f,
            locxAxis = 0.0f, locyAxis = 0.0f;
    private int vectorLength;
    private int axis = 1;
    private static float MIN_ZOOM = 1f;
    private static float MAX_ZOOM = 5f;

    private float scaleFactor = 1.f;
    private ScaleGestureDetector detector;

    public Plot2D(Context context, AttributeSet attrs) {
        super(context, attrs);
        detector = new ScaleGestureDetector(getContext(), new ScaleListener());
        paint = new Paint();
    }

    public Plot2D(Context context, AttributeSet attrs, int id) {
        super(context, attrs);
        detector = new ScaleGestureDetector(getContext(), new ScaleListener());
        paint = new Paint();
    }

    public void plotData(float[] xValues, float[] yValues, int axis) {
        this.xValues = xValues;
        this.yValues = yValues;
        this.axis = axis;
        vectorLength = xValues.length;
        // paint = new Paint();

        getAxis(xValues, yValues);
        invalidate();
    }

    public void plotData(float[] yValues, int axis) {
        if (yValues != null) {
            vectorLength = yValues.length;
            this.xValues = new float[vectorLength];
            for (int i = 0; i < vectorLength; i++) {
                xValues[i] = i;
            }
            this.yValues = yValues;
            this.axis = axis;
            // paint = new Paint();

            getAxis(xValues, yValues);
            invalidate();
        }
    }


    public Plot2D(Context context, float[] xValues, float[] yValues, int axis) {
        super(context);
        this.xValues = xValues;
        this.yValues = yValues;
        this.axis = axis;
        vectorLength = xValues.length;
        paint = new Paint();
        detector = new ScaleGestureDetector(getContext(), new ScaleListener());
        getAxis(xValues, yValues);

    }

    @Override
    protected void onDraw(Canvas canvas) {

        canvas.save();
        canvas.scale(scaleFactor, scaleFactor);

        float canvasHeight = getHeight();
        float canvasWidth = getWidth();
        int[] xValuesInPixels = toPixel(canvasWidth, minX, maxX, xValues);
        int[] yValuesInPixels = toPixel(canvasHeight, minY, maxY, yValues);
        int locxAxisInPixels = toPixelInt(canvasHeight, minY, maxY, locxAxis);
        int locyAxisInPixels = toPixelInt(canvasWidth, minX, maxX, locyAxis);

        paint.setStrokeWidth(2);
        canvas.drawARGB(255, 0, 0, 0);
        for (int i = 0; i < vectorLength - 1; i++) {
            paint.setColor(Color.RED);
            canvas.drawLine(xValuesInPixels[i], canvasHeight
                    - yValuesInPixels[i], xValuesInPixels[i + 1], canvasHeight
                    - yValuesInPixels[i + 1], paint);
        }

        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(5f);
        canvas.drawLine(0, canvasHeight - locxAxisInPixels, canvasWidth,
                canvasHeight - locxAxisInPixels, paint);
        canvas.drawLine(locyAxisInPixels, 0, locyAxisInPixels, canvasHeight,
                paint);

        // Automatic axis markings, modify n to control the number of axis labels
        if (axis != 0) {
            float temp = 0.0f;
            int n = 8;
            //paint.setTextAlign(Paint.Align.LEFT);
            paint.setTextSize(30.0f);
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            for (int i = 1; i <= n; i++) {
                if (i <= n / 2) {
                    temp = Math.round(10 * (minX + (i - 1) * (maxX - minX) / n)) / 10;
                    canvas.drawText("" + temp,
                            (float) toPixelInt(canvasWidth, minX, maxX, temp),
                            canvasHeight - locxAxisInPixels - 10, paint);
                    temp = Math.round(10 * (minY + (i - 1) * (maxY - minY) / n)) / 10;
                    canvas.drawText("" + temp, locyAxisInPixels + 10, canvasHeight
                                    - (float) toPixelInt(canvasHeight, minY, maxY, temp),
                            paint);
                } else {
                    temp = Math.round(10 * (minX + (i - 1) * (maxX - minX) / n)) / 10;
                    canvas.drawText("" + temp,
                            (float) toPixelInt(canvasWidth, minX, maxX, temp),
                            canvasHeight - locxAxisInPixels + 30, paint);
                    temp = Math.round(10 * (minY + (i - 1) * (maxY - minY) / n)) / 10;
                    canvas.drawText("" + temp, locyAxisInPixels - 65, canvasHeight
                                    - (float) toPixelInt(canvasHeight, minY, maxY, temp),
                            paint);
                }
            }
            canvas.drawText("" + maxX,
                    (float) toPixelInt(canvasWidth, minX, maxX, maxX),
                    canvasHeight - locxAxisInPixels + 30, paint);
            canvas.drawText("" + maxY, locyAxisInPixels - 65, canvasHeight
                    - (float) toPixelInt(canvasHeight, minY, maxY, maxY), paint);

        }
        canvas.restore();
    }

    private int[] toPixel(float pixels, float min, float max, float[] value) {
        double[] p = new double[value.length];
        int[] pInt = new int[value.length];

        for (int i = 0; i < value.length; i++) {
            p[i] = .1 * pixels + ((value[i] - min) / (max - min)) * .8 * pixels;
            pInt[i] = (int) p[i];
        }
        return (pInt);
    }

    private void getAxis(float[] xValues, float[] yValues) {

        minX = -16f;
        minY = -16f;
        maxX = 16f;
        maxY = 16f;

        if (minX >= 0)
            locyAxis = minX;
        else if (minX < 0 && maxX >= 0)
            locyAxis = 0;
        else
            locyAxis = maxX;

        if (minY >= 0)
            locxAxis = minY;
        else if (minY < 0 && maxY >= 0)
            locxAxis = 0;
        else
            locxAxis = maxY;
    }

    private int toPixelInt(float pixels, float min, float max, float value) {

        double p;
        int pInt;
        p = .1 * pixels + ((value - min) / (max - min)) * .8 * pixels;
        pInt = (int) p;
        return (pInt);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        detector.onTouchEvent(event);
        final int action = event.getAction();
        return true;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scaleFactor *= detector.getScaleFactor();
            scaleFactor = Math.max(MIN_ZOOM, Math.min(scaleFactor, MAX_ZOOM));
            invalidate();
            return true;
        }
    }
}

