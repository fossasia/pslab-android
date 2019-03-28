package io.pslab.activity;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsServiceConnection;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.pslab.BuildConfig;
import io.pslab.R;
import io.pslab.communication.CommunicationHandler;
import io.pslab.fragment.AboutUsFragment;
import io.pslab.fragment.FAQFragment;
import io.pslab.fragment.HomeFragment;
import io.pslab.fragment.InstrumentsFragment;
import io.pslab.fragment.PSLabPinLayoutFragment;
import io.pslab.others.CustomTabService;
import io.pslab.others.InitializationVariable;
import io.pslab.others.ScienceLabCommon;
import io.pslab.receivers.USBDetachReceiver;

import static io.pslab.others.ScienceLabCommon.scienceLab;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @BindView(R.id.nav_view)
    NavigationView navigationView;
    @Nullable
    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    View navHeader;
    private ImageView imgProfile;
    private TextView txtName;

    private ProgressDialog initialisationDialog;

    private CustomTabService customTabService;
    private CustomTabsServiceConnection customTabsServiceConnection;

    public static int navItemIndex = 0;

    private static final String TAG_DEVICE = "device";
    private static final String TAG_INSTRUMENTS = "instruments";
    private static final String TAG_SETTINGS = "settings";
    private static final String TAG_ABOUTUS = "aboutUs";
    private static final String TAG_PINLAYOUT = "pinLayout";
    private static final String TAG_FAQ = "faq";
    private static String CURRENT_TAG = TAG_INSTRUMENTS;
    private String[] activityTitles;

    private boolean shouldLoadHomeFragOnBackPress = true;
    private Handler mHandler;
    private ScienceLabCommon mScienceLabCommon;

    public boolean PSLabisConnected = false;

    InitializationVariable initialisationStatus;

    public static boolean hasPermission = false;
    private boolean receiverRegister = false;
    private UsbManager usbManager;
    private PendingIntent mPermissionIntent;
    private CommunicationHandler communicationHandler;
    private USBDetachReceiver usbDetachReceiver;
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private static final int TIME_INTERVAL = 2000;
    private long mBackPressed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        initialisationStatus = new InitializationVariable();
        initialisationDialog = new ProgressDialog(this);
        initialisationDialog.setMessage(getString(R.string.initialising_wait));
        initialisationDialog.setIndeterminate(true);
        initialisationDialog.setCancelable(false);

        usbManager = (UsbManager) getSystemService(USB_SERVICE);

        customTabService = new CustomTabService(MainActivity.this, customTabsServiceConnection);

        mScienceLabCommon = ScienceLabCommon.getInstance();

        initialisationDialog.show();

        communicationHandler = new CommunicationHandler(usbManager);
        attemptToGetUSBPermission();

        PSLabisConnected = mScienceLabCommon.openDevice(communicationHandler);

        IntentFilter usbDetachFilter = new IntentFilter();
        usbDetachFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        usbDetachReceiver = new USBDetachReceiver(this);
        registerReceiver(usbDetachReceiver, usbDetachFilter);

        setSupportActionBar(toolbar);
        mHandler = new Handler();

        navHeader = navigationView.getHeaderView(0);
        txtName = navHeader.findViewById(io.pslab.R.id.name);
        imgProfile = navHeader.findViewById(io.pslab.R.id.img_profile);
        activityTitles = getResources().getStringArray(io.pslab.R.array.nav_item_activity_titles);

        setPSLabVersionIDs();

        setUpNavigationView();
        initialisationDialog.dismiss();

        if (savedInstanceState == null) {
            navItemIndex = 0;
            CURRENT_TAG = TAG_INSTRUMENTS;
            loadHomeFragment();
        }
    }

    private void loadHomeFragment() {
        selectNavMenu();
        setToolbarTitle();
        if (drawer != null && getSupportFragmentManager().findFragmentByTag(CURRENT_TAG) != null) {
            drawer.closeDrawers();
            return;
        }
        Runnable mPendingRunnable = new Runnable() {
            @Override
            public void run() {
                Fragment fragment = null;
                try {
                    fragment = getHomeFragment();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.fade_in,
                        R.anim.fade_out);
                fragmentTransaction.replace(R.id.frame, fragment, CURRENT_TAG);
                fragmentTransaction.commitAllowingStateLoss();
            }
        };
        if (mPendingRunnable != null) {
            mHandler.post(mPendingRunnable);
        }
        if (drawer != null) {
            drawer.closeDrawers();
            invalidateOptionsMenu();
        }
    }

    private Fragment getHomeFragment() throws IOException {
        switch (navItemIndex) {
            case 1:
                return HomeFragment.newInstance(ScienceLabCommon.scienceLab.isConnected(), ScienceLabCommon.scienceLab.isDeviceFound());
            case 2:
                return null;
            case 3:
                return AboutUsFragment.newInstance();
            case 4:
                return FAQFragment.newInstance();
            default:
                return InstrumentsFragment.newInstance();
        }
    }

    private void setToolbarTitle() {
        getSupportActionBar().setTitle(activityTitles[navItemIndex]);
    }

    private void unCheckAllMenuItems(Menu menu) {
        int size = menu.size();
        for (int i = 0; i < size; i++) {
            final MenuItem item = menu.getItem(i);
            item.setChecked(false);
        }
    }

    private void selectNavMenu() {
        int size_menu = navigationView.getMenu().size();
        for (int i = 0; i < size_menu; i++) {
            final MenuItem item = navigationView.getMenu().getItem(i);
            if (item.hasSubMenu()) {
                unCheckAllMenuItems(item.getSubMenu());
            } else {
                item.setChecked(false);
            }
        }
        switch (navItemIndex) {
            case 0:
            case 1:
            case 2:
                navigationView.getMenu().getItem(navItemIndex).setChecked(true);
                break;
            case 3:
                navigationView.getMenu().getItem(4).getSubMenu().getItem(1).setChecked(true);
                break;
            case 4:
                navigationView.getMenu().getItem(4).getSubMenu().getItem(0).setChecked(true);
                break;
            default:
                navigationView.getMenu().getItem(0).setChecked(true);
                break;
        }
    }

    private void setUpNavigationView() {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_instruments:
                        navItemIndex = 0;
                        CURRENT_TAG = TAG_INSTRUMENTS;
                        break;
                    case R.id.nav_device:
                        navItemIndex = 1;
                        CURRENT_TAG = TAG_DEVICE;
                        break;
                    case R.id.nav_settings:
                        if (drawer != null) {
                            drawer.closeDrawers();
                        }
                        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                        intent.putExtra("title", "Settings");
                        startActivity(intent);
                        return true;
                    case R.id.nav_about_us:
                        navItemIndex = 3;
                        CURRENT_TAG = TAG_ABOUTUS;
                        break;
                    case R.id.nav_help_feedback:
                        navItemIndex = 4;
                        CURRENT_TAG = TAG_FAQ;
                        break;
                    case R.id.nav_report_us:
                        customTabService.launchUrl("https://goo.gl/forms/sHlmRAPFmzcGQ27u2");
                        if (drawer != null) {
                            drawer.closeDrawers();
                        }
                        break;
                    case R.id.nav_app_version:
                        setTitleColor(R.color.gray);
                        break;
                    case R.id.sensor_data_logger:
                        if (drawer != null) {
                            drawer.closeDrawers();
                        }
                        startActivity(new Intent(MainActivity.this, DataLoggerActivity.class));
                        break;
                    case R.id.nav_share_app:
                        try {
                            Intent shareIntent = new Intent(Intent.ACTION_SEND);
                            shareIntent.setType("text/plain");
                            shareIntent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.app_name));
                            String shareMessage = "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID;
                            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                            startActivity(shareIntent);
                        } catch(Exception e) {
                            //e.toString();
                        }
                    default:
                        navItemIndex = 0;
                }
                loadHomeFragment();
                return true;
            }
        });

        if (drawer != null) {
            ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawer, toolbar, io.pslab.R.string.openDrawer, io.pslab.R.string.closeDrawer) {
                @Override
                public void onDrawerClosed(View drawerView) {
                    super.onDrawerClosed(drawerView);
                }

                @Override
                public void onDrawerOpened(View drawerView) {
                    super.onDrawerOpened(drawerView);
                }
            };
            drawer.setDrawerListener(actionBarDrawerToggle);
            actionBarDrawerToggle.syncState();
        }
    }

    private void setPSLabVersionIDs() {
        try {
            txtName.setText(scienceLab.getVersion());
        } catch (IOException e) {
            txtName.setText(getString(R.string.device_not_found));
        }
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frame);
        if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawers();
            return;
        }
        if (fragment instanceof HomeFragment && HomeFragment.isWebViewShowing) {
            ((HomeFragment) fragment).hideWebView();
            return;
        }
        if (shouldLoadHomeFragOnBackPress) {
            if (navItemIndex != 0 || CURRENT_TAG.equals(TAG_PINLAYOUT)) {
                navItemIndex = 0;
                CURRENT_TAG = TAG_INSTRUMENTS;
                loadHomeFragment();
                return;
            }
        }
        if (fragment instanceof InstrumentsFragment) {
            if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis()) {
                super.onBackPressed();
                return;
            } else {
                Toast.makeText(getBaseContext(), getString(R.string.Toast_double_tap), Toast.LENGTH_SHORT).show();
            }
            mBackPressed = System.currentTimeMillis();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.pslab_connectivity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_pslab_connected:
                Toast.makeText(getApplicationContext(), getString(R.string.device_connected_successfully), Toast.LENGTH_SHORT).show();
                break;
            case R.id.menu_pslab_disconnected:
                attemptToConnectPSLab();
                break;
            case R.id.menu_pslab_layout_front:
                PSLabPinLayoutFragment.frontSide = true;
                displayPSLabPinLayout();
                break;
            case R.id.menu_pslab_layout_back:
                PSLabPinLayoutFragment.frontSide = false;
                displayPSLabPinLayout();
                break;
            default:
                break;
        }
        return true;
    }

    private void attemptToConnectPSLab() {
        initialisationDialog.show();
        mScienceLabCommon = ScienceLabCommon.getInstance();
        if (communicationHandler.isConnected()) {
            initialisationDialog.dismiss();
            Toast.makeText(this, getString(R.string.device_connected_successfully), Toast.LENGTH_SHORT).show();
        } else {
            communicationHandler = new CommunicationHandler(usbManager);
            if (communicationHandler.isDeviceFound()) {
                attemptToGetUSBPermission();
            } else {
                initialisationDialog.dismiss();
                Toast.makeText(this, getString(R.string.device_not_found), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void displayPSLabPinLayout() {
        CURRENT_TAG = TAG_PINLAYOUT;
        navigationView.getMenu().getItem(navItemIndex).setChecked(false);
        getSupportActionBar().setTitle(getResources().getString(R.string.pslab_pinlayout));
        Runnable mPendingRunnable = new Runnable() {
            @Override
            public void run() {
                Fragment fragment = PSLabPinLayoutFragment.newInstance();
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                        .replace(R.id.frame, fragment, TAG_PINLAYOUT)
                        .commitAllowingStateLoss();
            }
        };
        mHandler.post(mPendingRunnable);
    }

    private void attemptToGetUSBPermission() {
        if (!("android.hardware.usb.action.USB_DEVICE_ATTACHED".equals(getIntent().getAction()))) {
            if (communicationHandler.isDeviceFound() && !usbManager.hasPermission(communicationHandler.mUsbDevice)) {
                mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
                IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
                registerReceiver(mUsbReceiver, filter);
                receiverRegister = true;
                usbManager.requestPermission(communicationHandler.mUsbDevice, mPermissionIntent);
            }
            if (communicationHandler.mUsbDevice != null) {
                if (usbManager.hasPermission(communicationHandler.mUsbDevice))
                    initialisationDialog.dismiss();
                hasPermission = true;
            }
        } else if (usbManager.hasPermission(communicationHandler.mUsbDevice)) {
            hasPermission = true;
            initialisationDialog.dismiss();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.getItem(0).setVisible(PSLabisConnected);
        menu.getItem(1).setVisible(!PSLabisConnected);
        setPSLabVersionIDs();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "MainActivityDestroyed");
        try {
            scienceLab.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        unregisterReceiver(usbDetachReceiver);
        if (customTabsServiceConnection != null) {
            this.unbindService(customTabsServiceConnection);
        }
        if (receiverRegister)
            unregisterReceiver(mUsbReceiver);
    }

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            hasPermission = true;
                            PSLabisConnected = mScienceLabCommon.openDevice(communicationHandler);
                            initialisationDialog.dismiss();
                            invalidateOptionsMenu();
                            Toast.makeText(getApplicationContext(), getString(R.string.device_connected_successfully), Toast.LENGTH_SHORT).show();
                            if (navItemIndex == 0) {
                                getSupportFragmentManager().beginTransaction().replace(R.id.frame, InstrumentsFragment.newInstance()).commit();
                            } else if (navItemIndex == 1) {
                                getSupportFragmentManager().beginTransaction().replace(R.id.frame, HomeFragment.newInstance(true, true)).commitAllowingStateLoss();
                            } else {
                                Toast.makeText(getApplicationContext(), getString(R.string.device_connected_successfully), Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        initialisationDialog.dismiss();
                        Log.d(TAG, "permission denied for device " + device);
                        Toast.makeText(getApplicationContext(), getString(R.string.device_not_found), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    };

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        attemptToConnectPSLab();
        synchronized (this) {
            UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            if (device != null && hasPermission) {
                PSLabisConnected = mScienceLabCommon.openDevice(communicationHandler);
                initialisationDialog.dismiss();
                invalidateOptionsMenu();
                if (navItemIndex == 0) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.frame, InstrumentsFragment.newInstance()).commit();
                } else if (navItemIndex == 1) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.frame, HomeFragment.newInstance(true, true)).commitAllowingStateLoss();
                }
                Toast.makeText(getApplicationContext(), getString(R.string.device_connected_successfully), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        selectNavMenu();
    }
}
