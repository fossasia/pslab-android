package org.fossasia.pslab.activity;
/**
 * Created by sagarbaba on 08/03/18.
 */
import android.hardware.usb.UsbManager;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.fossasia.pslab.R;
import org.fossasia.pslab.communication.CommunicationHandler;
import org.fossasia.pslab.communication.ScienceLab;

import java.text.DecimalFormat;

public class ResistanceMeasurement extends AppCompatActivity {
    EditText resistance;
    Button clickme;
    ScienceLab scienceLab;
    CommunicationHandler communicationHandler;
    UsbManager usbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resistance_measurement);
        resistance = (EditText) findViewById(R.id.resistance);
        clickme = (Button) findViewById(R.id.clickme);
        usbManager = (UsbManager) getSystemService(USB_SERVICE);
        communicationHandler = new CommunicationHandler(usbManager);
        scienceLab = new ScienceLab(communicationHandler);
        clickme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (scienceLab.isConnected()) {
                    Double resist = scienceLab.getResistance();
                    resistance.setText(String.valueOf(resist));
                } else {
                    Toast.makeText(ResistanceMeasurement.this, "Device not Connected", Toast.LENGTH_SHORT).show();
                }


            }
        });

    }
}