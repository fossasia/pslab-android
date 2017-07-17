package org.fossasia.pslab.fragment;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.fossasia.pslab.R;
import org.fossasia.pslab.communication.PacketHandler;
import org.fossasia.pslab.communication.ScienceLab;
import org.fossasia.pslab.others.InitializationVariable;
import org.fossasia.pslab.others.ScienceLabCommon;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by viveksb007 on 15/3/17.
 */

public class HomeFragment extends Fragment {

    private boolean deviceFound = false, deviceConnected = false;
    @BindView(R.id.tv_device_status)
    TextView tvDeviceStatus;
    @BindView(R.id.tv_device_version)
    TextView tvVersion;
    @BindView(R.id.img_device_status)
    ImageView imgViewDeviceStatus;
    private Unbinder unbinder;

    @BindView(R.id.tv_initialisation_status)
    TextView tvInitializationStatus;
    public static InitializationVariable booleanVariable;

    private ProgressDialog progressDialog;

    private ScienceLab scienceLab;

    public static HomeFragment newInstance(boolean deviceConnected, boolean deviceFound) {
        HomeFragment homeFragment = new HomeFragment();
        homeFragment.deviceConnected = deviceConnected;
        homeFragment.deviceFound = deviceFound;
        return homeFragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scienceLab = ScienceLabCommon.scienceLab;
        booleanVariable = new InitializationVariable();
        progressDialog = new ProgressDialog(getActivity());
        if (scienceLab.calibrated)
            booleanVariable.setVariable(true);
        else
            booleanVariable.setVariable(false);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_fragment, container, false);
        unbinder = ButterKnife.bind(this, view);
        progressDialog.setMessage(getString(R.string.initialising_wait));
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        if (deviceFound & deviceConnected) {
            imgViewDeviceStatus.setImageResource(org.fossasia.pslab.R.drawable.usb_connected);
            tvDeviceStatus.setText(getString(R.string.device_connected_successfully));
        } else {
            imgViewDeviceStatus.setImageResource(org.fossasia.pslab.R.drawable.usb_disconnected);
            tvDeviceStatus.setText(getString(R.string.device_not_found));
        }
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (deviceConnected) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {

                        String version = PacketHandler.version;
                        if ("".equals(version)) {
                            progressDialog.show();
                            version = scienceLab.getVersion();
                            PacketHandler.version = version;
                        }
                        tvVersion.setText(version);
                        if (booleanVariable.isInitialised()) {
                            progressDialog.dismiss();
                            tvInitializationStatus.setText(getString(R.string.initialisation_completed));
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }, 100);

            booleanVariable.setValueChangeListener(new InitializationVariable.onValueChangeListener() {
                @Override
                public void onChange() {
                    if (tvInitializationStatus != null)
                        if (booleanVariable.isInitialised()) {
                            progressDialog.dismiss();
                            tvInitializationStatus.setText(getString(R.string.initialisation_completed));
                        }
                }
            });

        } else {
            tvVersion.setText(getString(R.string.not_connected));
        }



        /*  DEBUGGING CODE FOR REFERENCE

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    tvVersion.setText(scienceLab.getVersion());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 300);

        final Handler handler = new Handler();
        final int CAP_AND_PCS = 0;
        final int ADC_SHIFTS_LOCATION1 = 1;
        final int ADC_SHIFTS_LOCATION2 = 2;
        final int ADC_POLYNOMIALS_LOCATION = 3;


        final Runnable r4 = new Runnable() {
            @Override
            public void run() {
                ArrayList<Byte> polynomialsByteData = new ArrayList<>();
                for (int i = 0; i < 2048 / 16; i++) {
                    byte[] temp = scienceLab.readFlash(ADC_SHIFTS_LOCATION2, i * 2);
                    for (byte a : temp) {
                        polynomialsByteData.add(a);
                    }
                }
                Log.v("size ", "" + polynomialsByteData.size());
                byte[] data = new byte[2048];
                for (int i = 0; i < 2048; i++) {
                    data[i] = polynomialsByteData.get(i);
                }
                Log.v("String ", new String(data));
            }
        };

        final Runnable r3 = new Runnable() {
            @Override
            public void run() {
                ArrayList<Byte> polynomialsByteData = new ArrayList<>();
                for (int i = 0; i < 2048 / 16; i++) {
                    byte[] temp = scienceLab.readFlash(ADC_SHIFTS_LOCATION1, i);
                    for (byte a : temp) {
                        polynomialsByteData.add(a);
                    }
                }
                Log.v("size ", "" + polynomialsByteData.size());
                byte[] data = new byte[2048];
                for (int i = 0; i < 2048; i++) {
                    data[i] = polynomialsByteData.get(i);
                }
                Log.v("String ", new String(data));
                handler.postDelayed(r4, 1000);
            }
        };

        final Runnable r2 = new Runnable() {
            @Override
            public void run() {
                ArrayList<Byte> polynomialsByteData = new ArrayList<>();
                for (int i = 0; i < 2048 / 16; i++) {
                    byte[] temp = scienceLab.readFlash(ADC_POLYNOMIALS_LOCATION, i);
                    for (byte a : temp) {
                        polynomialsByteData.add(a);
                    }
                }
                Log.v("size ", "" + polynomialsByteData.size());
                byte[] data = new byte[2048];
                for (int i = 0; i < 2048; i++) {
                    data[i] = polynomialsByteData.get(i);
                }
                Log.v("String ", new String(data));
                handler.postDelayed(r3, 1000);
            }
        };

        Runnable r1 = new Runnable() {
            @Override
            public void run() {
                ArrayList<Byte> capsAndPcs = new ArrayList<Byte>();
                byte[] cps = scienceLab.readBulkFlash(CAP_AND_PCS, 8 * 4 + 5);
                for (int i = 0; i < 37; i++) {
                    capsAndPcs.add(cps[i]);
                }
                Log.v("cps size : ", capsAndPcs.size() + "");
                Log.v("String ", new String(cps));
                handler.postDelayed(r2, 1000);
            }
        };

        handler.postDelayed(r1, 1000);


        /*
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                ArrayList<Byte> polynomialsByteData = new ArrayList<>();
                for (int i = 0; i < 2048 / 16; i++) {
                    byte[] temp = scienceLab.readFlash(3, i * 16);
                    for (byte a : temp) {
                        polynomialsByteData.add(a);
                    }
                }
                Log.v("size ", "" + polynomialsByteData.size());
                byte[] data = new byte[2048];
                for (int i = 0; i < 2048; i++) {
                    data[i] = polynomialsByteData.get(i);
                }
                try {
                    String strData = new String(data, "US-ASCII");
                    Log.v("String Data", strData);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }, 2000);
        */

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
