package org.fossasia.pslab.activity;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


import org.fossasia.pslab.communication.CommunicationHandler;
import org.fossasia.pslab.fragment.ApplicationsFragment;
import org.fossasia.pslab.fragment.DesignExperiments;
import org.fossasia.pslab.fragment.HomeFragment;
import org.fossasia.pslab.fragment.SavedExperiments;
import org.fossasia.pslab.fragment.SettingsFragment;

import java.io.IOException;

import org.fossasia.pslab.R;
import org.fossasia.pslab.others.ScienceLabCommon;
import org.fossasia.pslab.receivers.USBDetachReceiver;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @BindView(R.id.nav_view)
    NavigationView navigationView;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    View navHeader;
    private ImageView imgProfile;
    private TextView txtName;

    public static int navItemIndex = 0;

    private static final String TAG_HOME = "home";

    private static final String TAG_APPLICATIONS = "applications";
    private static final String TAG_SAVED_EXPERIMENTS = "savedExperiments";
    private static final String TAG_DESIGN_EXPERIMENTS = "designExperiments";
    private static final String TAG_SETTINGS = "settings";
    public static String CURRENT_TAG = TAG_HOME;
    private String[] activityTitles;

    private boolean shouldLoadHomeFragOnBackPress = true;
    private Handler mHandler;
    private ScienceLabCommon mScienceLabCommon;

    public static boolean hasPermission = false;
    private boolean receiverRegister = false;
    private UsbManager usbManager;
    private PendingIntent mPermissionIntent;
    private CommunicationHandler communicationHandler;
    private USBDetachReceiver usbDetachReceiver;
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        usbManager = (UsbManager) getSystemService(USB_SERVICE);
        mScienceLabCommon = ScienceLabCommon.getInstance();
        communicationHandler = new CommunicationHandler(usbManager);
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
                    hasPermission = true;
            }
        } else if (usbManager.hasPermission(communicationHandler.mUsbDevice)) {
            hasPermission = true;
        }

        mScienceLabCommon.openDevice(communicationHandler);

        IntentFilter usbDetachFilter = new IntentFilter();
        usbDetachFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        usbDetachReceiver = new USBDetachReceiver(this);
        registerReceiver(usbDetachReceiver, usbDetachFilter);

        setSupportActionBar(toolbar);
        mHandler = new Handler();

        navHeader = navigationView.getHeaderView(0);
        txtName = (TextView) navHeader.findViewById(org.fossasia.pslab.R.id.name);
        imgProfile = (ImageView) navHeader.findViewById(org.fossasia.pslab.R.id.img_profile);
        activityTitles = getResources().getStringArray(org.fossasia.pslab.R.array.nav_item_activity_titles);

        loadNavHeader();

        setUpNavigationView();

        if (savedInstanceState == null) {
            navItemIndex = 0;
            CURRENT_TAG = TAG_HOME;
            loadHomeFragment();
        }
    }

    private void loadHomeFragment() {
        selectNavMenu();
        setToolbarTitle();
        if (getSupportFragmentManager().findFragmentByTag(CURRENT_TAG) != null) {
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
                fragmentTransaction.setCustomAnimations(android.R.anim.fade_in,
                        android.R.anim.fade_out);
                fragmentTransaction.replace(R.id.frame, fragment, CURRENT_TAG);
                fragmentTransaction.commitAllowingStateLoss();
            }
        };
        if (mPendingRunnable != null) {
            mHandler.post(mPendingRunnable);
        }
        drawer.closeDrawers();
        invalidateOptionsMenu();
    }

    private Fragment getHomeFragment() throws IOException {
        switch (navItemIndex) {
            case 1:
                return ApplicationsFragment.newInstance();
            case 2:
                return SavedExperiments.newInstance(this);
            case 3:
                return DesignExperiments.newInstance();
            case 4:
                return SettingsFragment.newInstance();
            default:
                return HomeFragment.newInstance(ScienceLabCommon.scienceLab.isConnected(), ScienceLabCommon.scienceLab.isDeviceFound());
        }
    }

    private void setToolbarTitle() {
        getSupportActionBar().setTitle(activityTitles[navItemIndex]);
    }

    private void selectNavMenu() {
        navigationView.getMenu().getItem(navItemIndex).setChecked(true);
    }

    private void setUpNavigationView() {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_home:
                        navItemIndex = 0;
                        CURRENT_TAG = TAG_HOME;
                        break;
                    case R.id.nav_applications:
                        navItemIndex = 1;
                        CURRENT_TAG = TAG_APPLICATIONS;
                        break;
                    case R.id.nav_saved_experiments:
                        navItemIndex = 2;
                        CURRENT_TAG = TAG_SAVED_EXPERIMENTS;
                        break;
                    case R.id.nav_design_experiments:
                        navItemIndex = 3;
                        CURRENT_TAG = TAG_DESIGN_EXPERIMENTS;
                        break;
                    case R.id.nav_settings:
                        navItemIndex = 4;
                        CURRENT_TAG = TAG_SETTINGS;
                        break;
                    case R.id.nav_about_us:
                        startActivity(new Intent(MainActivity.this, AboutUs.class));
                        drawer.closeDrawers();
                        break;
                    case R.id.nav_help_feedback:
                        startActivity(new Intent(MainActivity.this, HelpAndFeedback.class));
                        drawer.closeDrawers();
                        break;
                    case R.id.nav_report_us:
                        startActivity(new Intent(MainActivity.this, ReportUs.class));
                        drawer.closeDrawers();
                        break;
                    default:
                        navItemIndex = 0;
                }

                loadHomeFragment();
                return true;
            }
        });

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawer, toolbar, org.fossasia.pslab.R.string.openDrawer, org.fossasia.pslab.R.string.closeDrawer) {
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

    private void loadNavHeader() {
        txtName.setText("PSLab Testing");
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawers();
            return;
        }
        if (shouldLoadHomeFragOnBackPress) {
            if (navItemIndex != 0) {
                navItemIndex = 0;
                CURRENT_TAG = TAG_HOME;
                loadHomeFragment();
                return;
            }
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "MainActivityDestroyed");
        try {
            ScienceLabCommon.scienceLab.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        unregisterReceiver(usbDetachReceiver);
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
                            mScienceLabCommon.openDevice(communicationHandler);
                            getSupportFragmentManager().beginTransaction().replace(R.id.frame, HomeFragment.newInstance(ScienceLabCommon.scienceLab.isConnected(), ScienceLabCommon.scienceLab.isDeviceFound())).commit();
                        }
                    } else {
                        Log.d(TAG, "permission denied for device " + device);
                    }
                }
            }
        }
    };

}
