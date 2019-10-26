package io.pslab.fragment;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import io.pslab.R;
import io.pslab.items.PinDetails;

/**
 * Created by Padmal on 5/22/18.
 */

public class PSLabPinLayoutFragment extends Fragment implements View.OnTouchListener {

    private ArrayList<PinDetails> pinDetails = new ArrayList<>();

    private Matrix matrix = new Matrix();
    private Matrix savedMatrix = new Matrix();

    public static boolean frontSide = true;

    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private int mode = NONE;

    private PointF start = new PointF();
    private PointF mid = new PointF();
    private float oldDist = 1f;

    private ImageView colorMap;

    public static PSLabPinLayoutFragment newInstance() {
        return new PSLabPinLayoutFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pin_layout, container, false);
        ImageView imgLayout = view.findViewById(R.id.img_pslab_pin_layout);
        colorMap = view.findViewById(R.id.img_pslab_color_map);
        imgLayout.setImageDrawable(ResourcesCompat.getDrawable(getResources(),
                frontSide ? R.drawable.pslab_v5_front_layout : R.drawable.pslab_v5_back_layout, null));
        colorMap.setImageDrawable(ResourcesCompat.getDrawable(getResources(),
                frontSide ? R.drawable.pslab_v5_front_colormap : R.drawable.pslab_v5_back_colormap, null));
        imgLayout.setOnTouchListener(this);
        populatePinDetails();

        return view;
    }

    private void populatePinDetails() {
        pinDetails.add(new PinDetails("ESP", "ESP Programmer pin", Color.parseColor("#6b40a9"), Color.parseColor("#dc1616")));
        pinDetails.add(new PinDetails("RXP", "Receiver pin for UART communication", Color.parseColor("#4372a2"), Color.parseColor("#1616dc")));
        pinDetails.add(new PinDetails("TXP", "Transmitter pin for UART communication", Color.parseColor("#4372a2"), Color.parseColor("#37dc16")));
        pinDetails.add(new PinDetails("GND", "Ground pin (0 V)", Color.parseColor("#ff4040"), Color.parseColor("#16dcda")));
        pinDetails.add(new PinDetails("SI1", "Wave generator pin 1", Color.parseColor("#406743"), Color.parseColor("#226a0c")));
        pinDetails.add(new PinDetails("SI2", "Wave generator pin 2", Color.parseColor("#406743"), Color.parseColor("#226aba")));
        pinDetails.add(new PinDetails("SQ1", "PWM generator pin 1", Color.parseColor("#406743"), Color.parseColor("#baaa22")));
        pinDetails.add(new PinDetails("SQ2", "PWM generator pin 2", Color.parseColor("#406743"), Color.parseColor("#22ba6d")));
        pinDetails.add(new PinDetails("SQ3", "PWM generator pin 3", Color.parseColor("#406743"), Color.parseColor("#aa44aa")));
        pinDetails.add(new PinDetails("SQ4", "PWM generator pin 4", Color.parseColor("#406743"), Color.parseColor("#d28080")));
        pinDetails.add(new PinDetails("LA1", "Logic analyzer pin 1", Color.parseColor("#406743"), Color.parseColor("#0053ad")));
        pinDetails.add(new PinDetails("LA2", "Logic analyzer pin 2", Color.parseColor("#406743"), Color.parseColor("#ad2d00")));
        pinDetails.add(new PinDetails("LA3", "Logic analyzer pin 3", Color.parseColor("#406743"), Color.parseColor("#e7a4ff")));
        pinDetails.add(new PinDetails("LA4", "Logic analyzer pin 4", Color.parseColor("#406743"), Color.parseColor("#e7a41a")));
        pinDetails.add(new PinDetails("AC1", "Alternative channel input", Color.parseColor("#ffe040"), Color.parseColor("#b01498")));
        pinDetails.add(new PinDetails("CH1", "Oscilloscope channel input 1", Color.parseColor("#ffe040"), Color.parseColor("#0b4189")));
        pinDetails.add(new PinDetails("CH2", "Oscilloscope channel input 2", Color.parseColor("#ffe040"), Color.parseColor("#410b89")));
        pinDetails.add(new PinDetails("CH3", "Oscilloscope channel input 3", Color.parseColor("#ffe040"), Color.parseColor("#41890b")));
        pinDetails.add(new PinDetails("CHG", "Oscilloscope channel 3 gain set", Color.parseColor("#ffe040"), Color.parseColor("#89410b")));
        pinDetails.add(new PinDetails("MIC", "External microphone input", Color.parseColor("#7d5840"), Color.parseColor("#890b41")));
        pinDetails.add(new PinDetails("FRQ", "Frequency counter pin", Color.parseColor("#7d5840"), Color.parseColor("#ff0b41")));
        pinDetails.add(new PinDetails("CAP", "Capacitance measurement pin", Color.parseColor("#7d5840"), Color.parseColor("#ff410b")));
        pinDetails.add(new PinDetails("RES", "Resistance measurement pin", Color.parseColor("#7d5840"), Color.parseColor("#41ff0b")));
        pinDetails.add(new PinDetails("VOL", "Voltage measurement pin", Color.parseColor("#7d5840"), Color.parseColor("#410bff")));
        pinDetails.add(new PinDetails("PCS", "Programmable current source (3.3 mA)", Color.parseColor("#c3007c"), Color.parseColor("#3fa96f")));
        pinDetails.add(new PinDetails("PV3", "Programmable voltage source 3 (0-3.3 V)", Color.parseColor("#c3007c"), Color.parseColor("#a93f6f")));
        pinDetails.add(new PinDetails("PV2", "Programmable voltage source 2 (-+3.3 V)", Color.parseColor("#c3007c"), Color.parseColor("#a96f3f")));
        pinDetails.add(new PinDetails("PV1", "Programmable voltage source 1 (-+5.0 V)", Color.parseColor("#c3007c"), Color.parseColor("#446e72")));
        pinDetails.add(new PinDetails("SCL", "Serial clock pin for I2C", Color.parseColor("#4372a2"), Color.parseColor("#6e4472")));
        pinDetails.add(new PinDetails("SDA", "Serial data pin for I2C", Color.parseColor("#4372a2"), Color.parseColor("#6e7244")));
        pinDetails.add(new PinDetails("VDD", "Voltage supply pin (3.3 V)", Color.parseColor("#ff4040"), Color.parseColor("#d25c3c")));
        pinDetails.add(new PinDetails("STA", "Bluetooth device state output pin", Color.parseColor("#4372a2"), Color.parseColor("#5c5c3c")));
        pinDetails.add(new PinDetails("V+", "Voltage test pin (+8.0 V)", Color.parseColor("#ff4040"), Color.parseColor("#5cd23c")));
        pinDetails.add(new PinDetails("ENA", "Bluetooth device enable/disable pin", Color.parseColor("#4372a2"), Color.parseColor("#5c143c")));
        pinDetails.add(new PinDetails("V-", "Voltage test pin (-8.0 V)", Color.parseColor("#ff4040"), Color.parseColor("#5c3c14")));
        pinDetails.add(new PinDetails("MCL", "Master clear pin for programmer", Color.parseColor("#4372a2"), Color.parseColor("#143c14")));
        pinDetails.add(new PinDetails("PGM", "Mode pin for programmer", Color.parseColor("#4372a2"), Color.parseColor("#b73cf1")));
        pinDetails.add(new PinDetails("PGC", "Clock pin for programmer", Color.parseColor("#4372a2"), Color.parseColor("#fff724")));
        pinDetails.add(new PinDetails("PGD", "Data pin for programmer", Color.parseColor("#4372a2"), Color.parseColor("#724fff")));
        pinDetails.add(new PinDetails("NRF", "Radio communication module using nRF24", Color.parseColor("#6b40a9"), Color.parseColor("#ffa64f")));
        pinDetails.add(new PinDetails("USB", "Micro B type USB socket", Color.parseColor("#6b40a9"), Color.parseColor("#ff2b4f")));
        pinDetails.add(new PinDetails("VCC", "Voltage supply pin (+5.0 V)", Color.parseColor("#ff4040"), Color.parseColor("#6b6500")));
        pinDetails.add(new PinDetails("+5V", "Test pin +5.0 V", Color.parseColor("#ff4040"), Color.parseColor("#765f40")));
        pinDetails.add(new PinDetails("D+", "Test pin for USB Data +", Color.parseColor("#6b40a9"), Color.parseColor("#681654")));
        pinDetails.add(new PinDetails("D-", "Test pin for USB Data -", Color.parseColor("#6b40a9"), Color.parseColor("#a68b47")));
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        ImageView view = (ImageView) v;
        view.setScaleType(ImageView.ScaleType.MATRIX);
        colorMap.setScaleType(ImageView.ScaleType.MATRIX);
        float scale;

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                matrix.set(view.getImageMatrix());
                savedMatrix.set(matrix);
                start.set(event.getX(), event.getY());
                mode = DRAG;
                break;

            case MotionEvent.ACTION_UP:
                colorMap.setDrawingCacheEnabled(true);
                Bitmap clickSpot = Bitmap.createBitmap(colorMap.getDrawingCache());
                colorMap.setDrawingCacheEnabled(false);
                try {
                    int pixel = clickSpot.getPixel((int) event.getX(), (int) event.getY());
                    for (PinDetails pin : pinDetails) {
                        if (pin.getColorID() == Color.rgb(Color.red(pixel), Color.green(pixel), Color.blue(pixel))) {
                            displayPinDescription(pin);
                        }
                    }
                } catch (IllegalArgumentException e) {/**/}
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = spacing(event);
                if (oldDist > 5f) {
                    savedMatrix.set(matrix);
                    midPoint(mid, event);
                    mode = ZOOM;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (mode == DRAG) {
                    matrix.set(savedMatrix);
                    matrix.postTranslate(event.getX() - start.x, event.getY() - start.y);
                } else if (mode == ZOOM) {
                    float newDist = spacing(event);
                    if (newDist > 5f) {
                        matrix.set(savedMatrix);
                        scale = newDist / oldDist;
                        matrix.postScale(scale, scale, mid.x, mid.y);
                    }
                }
                break;

            default:
                break;
        }

        view.setImageMatrix(matrix);
        colorMap.setImageMatrix(matrix);

        return true;
    }

    private void displayPinDescription(PinDetails pin) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.pin_description_dialog, null);
        builder.setView(view);

        ImageView pinColor = view.findViewById(R.id.pin_category_color);
        pinColor.setBackgroundColor(pin.getCategoryColor());
        TextView pinTitle = view.findViewById(R.id.pin_description_title);
        pinTitle.setText(pin.getName());
        TextView pinDescription = view.findViewById(R.id.pin_description);
        pinDescription.setText(pin.getDescription());
        Button dialogButton = view.findViewById(R.id.pin_description_dismiss);

        builder.create();
        final AlertDialog dialog = builder.show();

        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    private float spacing(MotionEvent event) {
        float x = 0;
        float y = 0;
        try {
            x = event.getX(0) - event.getX(1);
            y = event.getY(0) - event.getY(1);
        } catch (Exception e) {/**/}
        return (float) Math.sqrt(x * x + y * y);
    }

    private void midPoint(PointF point, MotionEvent event) {
        float x = 0;
        float y = 0;
        try {
            x = event.getX(0) + event.getX(1);
            y = event.getY(0) + event.getY(1);
        } catch (Exception e) {/**/}
        point.set(x / 2, y / 2);
    }
}
