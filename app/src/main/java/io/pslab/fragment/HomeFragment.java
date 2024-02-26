package io.pslab.fragment;

import static io.pslab.others.ScienceLabCommon.scienceLab;

import android.graphics.Bitmap;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.pslab.R;
import io.pslab.others.InitializationVariable;
import io.pslab.others.ScienceLabCommon;

public class HomeFragment extends Fragment {

    public static InitializationVariable booleanVariable;
    public static boolean isWebViewShowing = false;
    @BindView(R.id.tv_device_status)
    TextView tvDeviceStatus;
    @BindView(R.id.tv_device_version)
    TextView tvVersion;
    @BindView(R.id.img_device_status)
    ImageView imgViewDeviceStatus;
    @BindView(R.id.tv_device_description)
    TextView deviceDescription;
    @BindView(R.id.tv_connect_msg)
    LinearLayout tvConnectMsg;
    @BindView(R.id.pslab_web_view)
    WebView webView;
    @BindView(R.id.home_content_scroll_view)
    ScrollView svHomeContent;
    @BindView(R.id.web_view_progress)
    ProgressBar wvProgressBar;
    @BindView(R.id.steps_header_text)
    TextView stepsHeader;
    @BindView(R.id.bluetooth_btn)
    Button bluetoothButton;
    @BindView(R.id.wifi_btn)
    Button wifiButton;
    @BindView(R.id.bluetooth_wifi_option_tv)
    TextView bluetoothWifiOption;
    private boolean deviceFound = false, deviceConnected = false;
    private Unbinder unbinder;

    public static HomeFragment newInstance(boolean deviceConnected, boolean deviceFound) {
        HomeFragment homeFragment = new HomeFragment();
        homeFragment.deviceConnected = deviceConnected;
        homeFragment.deviceFound = deviceFound;
        return homeFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (booleanVariable == null) {
            booleanVariable = new InitializationVariable();
        }
        if (scienceLab.calibrated)
            booleanVariable.setVariable(true);
        else
            booleanVariable.setVariable(false);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_fragment, container, false);
        unbinder = ButterKnife.bind(this, view);
        stepsHeader.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
        deviceDescription.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
        wvProgressBar = (ProgressBar) view.findViewById(R.id.web_view_progress);

        if (deviceFound & deviceConnected) {
            tvConnectMsg.setVisibility(View.GONE);
            try {
                tvVersion.setText(scienceLab.getVersion());
                tvVersion.setVisibility(View.VISIBLE);
            } catch (IOException e) {
                e.printStackTrace();
            }
            imgViewDeviceStatus.setImageResource(R.drawable.icons8_usb_connected_100);
            tvDeviceStatus.setText(getString(R.string.device_connected_successfully));
        } else {
            imgViewDeviceStatus.setImageResource(R.drawable.icons_usb_disconnected_100);
            tvDeviceStatus.setText(getString(R.string.device_not_found));
        }

        /*
         * The null-checks in the OnClickListener may seem unnecessary, but even though the
         * respective variables are initialized before the setter is called, they may contain null
         * in later phases of the lifecycle of this Fragment and cause NullPointerExceptions if not
         * checked before access.
         *
         * See: https://github.com/fossasia/pslab-android/issues/2211
         */
        deviceDescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (webView == null) {
                    return;
                }

                webView.loadUrl("https://pslab.io");
                webView.getSettings().setDomStorageEnabled(true);
                webView.getSettings().setJavaScriptEnabled(true);
                svHomeContent.setVisibility(View.GONE);
                webView.setWebViewClient(new WebViewClient() {
                    @Override
                    public void onPageStarted(WebView view, String url, Bitmap favicon) {
                        if (wvProgressBar != null) {
                            wvProgressBar.setIndeterminate(true);
                            wvProgressBar.setVisibility(View.VISIBLE);
                        }
                    }

                    public void onPageFinished(WebView view, String url) {
                        if (wvProgressBar != null) {
                            wvProgressBar.setVisibility(View.GONE);
                        }
                        if (webView != null) {
                            webView.setVisibility(View.VISIBLE);
                        }
                    }
                });
                isWebViewShowing = true;
            }
        });

        webView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    WebView webView = (WebView) v;
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_BACK:
                            if (webView.canGoBack()) {
                                webView.goBack();
                                return true;
                            }
                            break;
                        default:
                            return false;
                    }
                }
                return false;
            }
        });
        if (ScienceLabCommon.scienceLab.isConnected()) {
            bluetoothButton.setVisibility(View.GONE);
            wifiButton.setVisibility(View.GONE);
            bluetoothWifiOption.setVisibility(View.GONE);
        } else {
            bluetoothButton.setVisibility(View.VISIBLE);
            wifiButton.setVisibility(View.VISIBLE);
            bluetoothWifiOption.setVisibility(View.VISIBLE);
        }
        bluetoothButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BluetoothScanFragment bluetoothScanFragment = new BluetoothScanFragment();
                bluetoothScanFragment.show(getActivity().getSupportFragmentManager(), "bluetooth");
                bluetoothScanFragment.setCancelable(true);
            }
        });

        wifiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ESPFragment espFragment = new ESPFragment();
                espFragment.show(getActivity().getSupportFragmentManager(), "wifi");
                espFragment.setCancelable(true);
            }
        });
        return view;
    }

    public void hideWebView() {
        webView.setVisibility(View.GONE);
        svHomeContent.setVisibility(View.VISIBLE);
        isWebViewShowing = false;
        webView.loadUrl("");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
