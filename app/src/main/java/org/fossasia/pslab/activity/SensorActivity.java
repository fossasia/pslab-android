package org.fossasia.pslab.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import org.fossasia.pslab.R;
import org.fossasia.pslab.communication.ScienceLab;
import org.fossasia.pslab.communication.peripherals.I2C;
import org.fossasia.pslab.communication.peripherals.SPI;
import org.fossasia.pslab.communication.sensors.ADS1115;
import org.fossasia.pslab.communication.sensors.BH1750;
import org.fossasia.pslab.communication.sensors.BMP180;
import org.fossasia.pslab.communication.sensors.HMC5883L;
import org.fossasia.pslab.communication.sensors.MLX90614;
import org.fossasia.pslab.communication.sensors.MPU6050;
import org.fossasia.pslab.communication.sensors.MPU925x;
import org.fossasia.pslab.communication.sensors.SHT21;
import org.fossasia.pslab.communication.sensors.Sx1276;
import org.fossasia.pslab.communication.sensors.TSL2561;
import org.fossasia.pslab.others.ScienceLabCommon;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by asitava on 18/6/17.
 */

public class SensorActivity extends AppCompatActivity {

    private I2C i2c;
    private SPI spi;
    private ScienceLab scienceLab;
    private HashMap<Integer, String> sensorAddr = new HashMap<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);

        scienceLab = ScienceLabCommon.scienceLab;
        i2c = scienceLab.i2c;
        spi = scienceLab.spi;

        sensorAddr.put(0x60, "MCP4728");
        sensorAddr.put(0x48, "ADS1115");
        sensorAddr.put(0x23, "BH1750");
        sensorAddr.put(0x77, "BMP180");
        sensorAddr.put(0x5A, "MLX90614");
        sensorAddr.put(0x1E, "HMC5883L");
        sensorAddr.put(0x68, "MPU6050");
        sensorAddr.put(0x40, "SHT21");
        sensorAddr.put(0x39, "TSL2561");

        Button buttonSensorAutoscan = (Button) findViewById(R.id.button_sensor_autoscan);
        Button buttonSensorGetRaw = (Button) findViewById(R.id.button_sensor_getraw);
        final TextView tvSensorScan = (TextView) findViewById(R.id.tv_sensor_scan);
        final TextView tvSensorGetRaw = (TextView) findViewById(R.id.tv_sensor_getraw);
        final Spinner spinnerSensorSelect = (Spinner) findViewById(R.id.spinner_sensor_select);

        buttonSensorAutoscan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (scienceLab.isConnected()) {
                    ArrayList<Integer> data = new ArrayList<Integer>();
                    ArrayList<String> datastr = new ArrayList<String>();
                    String datadisp = "";
                    try {
                        data = i2c.scan(null);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (data != null) {
                        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, android.R.id.text1);
                        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerSensorSelect.setAdapter(spinnerAdapter);
                        for (Integer myInt : data) {
                            if (sensorAddr.get(myInt) != null) {
                                datastr.add(String.valueOf(myInt));
                                spinnerAdapter.add(sensorAddr.get(myInt));
                                spinnerAdapter.notifyDataSetChanged();
                            }
                        }

                        for (String s : datastr) {
                            datadisp += s + "\n";
                        }
                        tvSensorScan.setText(datadisp);

                    }
                }
            }
        });

        buttonSensorGetRaw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (scienceLab.isConnected()) {
                    String sensor = spinnerSensorSelect.getSelectedItem().toString();
                    switch (sensor) {
                        case "ADS1115":
                            ADS1115 ADS1115 = null;
                            try {
                                ADS1115 = new ADS1115(i2c);
                            } catch (IOException | InterruptedException e) {
                                e.printStackTrace();
                            }

                            int[] dataADS1115 = null;
                            String datadispADS1115 = null;
                            try {
                                if (ADS1115 != null) {
                                    dataADS1115 = ADS1115.getRaw();
                                }
                            } catch (IOException | InterruptedException e) {
                                e.printStackTrace();
                            }

                            if (dataADS1115 != null) {
                                for (int i = 0; i < dataADS1115.length; i++)
                                    datadispADS1115 += String.valueOf(dataADS1115[i]);
                            }

                            tvSensorGetRaw.setText(datadispADS1115);
                            break;

                        case "BH1750":
                            BH1750 BH1750 = null;
                            try {
                                BH1750 = new BH1750(i2c);
                            } catch (IOException | InterruptedException e) {
                                e.printStackTrace();
                            }
                            Boolean dataBH1750 = null;
                            String datadispBH1750 = null;
                            try {
                                if (BH1750 != null) {
                                    dataBH1750 = BH1750.getRaw();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            datadispBH1750 = String.valueOf(dataBH1750);
                            tvSensorGetRaw.setText(datadispBH1750);
                            break;
                        case "BMP180":
                            BMP180 BMP180 = null;
                            try {
                                BMP180 = new BMP180(i2c);
                            } catch (IOException | InterruptedException e) {
                                e.printStackTrace();
                            }

                            double[] dataBMP180 = null;
                            String datadispBMP180 = null;
                            try {
                                if (BMP180 != null) {
                                    dataBMP180 = BMP180.getRaw();
                                }
                            } catch (IOException | InterruptedException e) {
                                e.printStackTrace();
                            }

                            if (dataBMP180 != null) {
                                for (int i = 0; i < dataBMP180.length; i++)
                                    datadispBMP180 += String.valueOf(dataBMP180[i]);
                            }

                            tvSensorGetRaw.setText(datadispBMP180);
                            break;
                        case "MLX90614":
                            MLX90614 MLX90614 = null;
                            try {
                                MLX90614 = new MLX90614(i2c);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            Double[] dataMLX90614 = null;
                            String datadispMLX90614 = null;
                            try {
                                if (MLX90614 != null) {
                                    dataMLX90614 = MLX90614.getRaw();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            if (dataMLX90614 != null) {
                                for (int i = 0; i < dataMLX90614.length; i++)
                                    datadispMLX90614 += String.valueOf(dataMLX90614[i]);
                            }

                            tvSensorGetRaw.setText(datadispMLX90614);
                            break;
                        case "HMC5883L":
                            HMC5883L HMC5883L = null;
                            try {
                                HMC5883L = new HMC5883L(i2c);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            ArrayList<Double> dataHMC5883L = new ArrayList<Double>();
                            String datadispHMC5883L = null;
                            try {
                                if (HMC5883L != null) {
                                    dataHMC5883L = HMC5883L.getRaw();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            for (int i = 0; i < dataHMC5883L.size(); i++)
                                datadispHMC5883L += String.valueOf(dataHMC5883L.get(i));

                            tvSensorGetRaw.setText(datadispHMC5883L);
                            break;
                        case "MPU6050":
                            MPU6050 MPU6050 = null;
                            try {
                                MPU6050 = new MPU6050(i2c);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            ArrayList<Double> dataMPU6050 = new ArrayList<Double>();
                            String datadispMPU6050 = null;
                            try {
                                if (MPU6050 != null) {
                                    dataMPU6050 = MPU6050.getRaw();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            for (int i = 0; i < dataMPU6050.size(); i++)
                                datadispMPU6050 += String.valueOf(dataMPU6050.get(i));

                            tvSensorGetRaw.setText(datadispMPU6050);
                            break;
                        case "MPU925x":
                            MPU925x MPU925x = null;
                            try {
                                MPU925x = new MPU925x(i2c);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            ArrayList<Double> dataMPU925x = new ArrayList<Double>();
                            String datadispMPU925x = null;
                            try {
                                if (MPU925x != null) {
                                    dataMPU925x = MPU925x.getRaw();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            for (int i = 0; i < dataMPU925x.size(); i++)
                                datadispMPU925x += String.valueOf(dataMPU925x.get(i));

                            tvSensorGetRaw.setText(datadispMPU925x);
                            break;

                        case "SHT21":
                            SHT21 SHT21 = null;
                            try {
                                SHT21 = new SHT21(i2c);
                            } catch (IOException | InterruptedException e) {
                                e.printStackTrace();
                            }

                            ArrayList<Double> dataSHT21 = new ArrayList<Double>();
                            String datadispSHT21 = null;
                            try {
                                if (SHT21 != null) {
                                    dataSHT21 = SHT21.getRaw();
                                }
                            } catch (IOException | InterruptedException e) {
                                e.printStackTrace();
                            }
                            for (int i = 0; i < dataSHT21.size(); i++)
                                datadispSHT21 += String.valueOf(dataSHT21.get(i));

                            tvSensorGetRaw.setText(datadispSHT21);
                            break;

                        case "Sx1276":
                            Sx1276 Sx1276 = null;
                            try {
                                Sx1276 = new Sx1276(spi, 10);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            ArrayList<Byte> dataSx1276 = null;
                            String datadispSx1276 = null;
                            try {
                                if (Sx1276 != null) {
                                    dataSx1276 = Sx1276.getRaw();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            for (int i = 0; i < dataSx1276.size(); i++)
                                datadispSx1276 += String.valueOf(dataSx1276.get(i));
                            break;
                        case "TSL2561":
                            TSL2561 TSL2561 = null;
                            try {
                                TSL2561 = new TSL2561(i2c);
                            } catch (IOException | InterruptedException e) {
                                e.printStackTrace();
                            }

                            int[] dataTSL2561 = null;
                            String datadispTSL2561 = null;
                            try {
                                if (TSL2561 != null) {
                                    dataTSL2561 = TSL2561.getRaw();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            if (dataTSL2561 != null) {
                                for (int i = 0; i < dataTSL2561.length; i++)
                                    datadispTSL2561 += String.valueOf(dataTSL2561[i]);
                            }

                            tvSensorGetRaw.setText(datadispTSL2561);
                            break;

                        default:
                            break;
                    }
                }
            }

        });
    }
}
