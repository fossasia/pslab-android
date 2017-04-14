package org.fossasia.pslab.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
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
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.fossasia.pslab.communication.ScienceLab;
import org.fossasia.pslab.fragment.ApplicationsFragment;
import org.fossasia.pslab.fragment.DesignExperiments;
import org.fossasia.pslab.fragment.HomeFragment;
import org.fossasia.pslab.fragment.SavedExperiments;
import org.fossasia.pslab.fragment.SettingsFragment;

import java.util.ArrayList;
import java.util.List;

import org.fossasia.pslab.R;

import static android.R.attr.filter;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private UsbManager usbManager;
    private static final String DEVICE_NAME = "PSLAB";
    private LineChart lineChart;

    private NavigationView navigationView;
    private DrawerLayout drawer;
    private View navHeader;
    private ImageView imgProfile;
    private TextView txtName;
    private Toolbar toolbar;
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
    private ScienceLab mScienceLab;

    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        usbManager = (UsbManager) getSystemService(USB_SERVICE);
        mScienceLab = new ScienceLab(usbManager);
        IntentFilter filter;
        toolbar = (Toolbar) findViewById(org.fossasia.pslab.R.id.toolbar);
        setSupportActionBar(toolbar);

        mHandler = new Handler();

        drawer = (DrawerLayout) findViewById(org.fossasia.pslab.R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(org.fossasia.pslab.R.id.nav_view);

        navHeader = navigationView.getHeaderView(0);
        txtName = (TextView) navHeader.findViewById(org.fossasia.pslab.R.id.name);
        imgProfile = (ImageView) navHeader.findViewById(org.fossasia.pslab.R.id.img_profile);
        activityTitles = getResources().getStringArray(org.fossasia.pslab.R.array.nav_item_activity_titles);

        loadNavHeader();

        filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(mUsbReceiver, filter);

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
                Fragment fragment = getHomeFragment();
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

    private Fragment getHomeFragment() {
        switch (navItemIndex) {
            case 0:
                return HomeFragment.newInstance(mScienceLab.isConnected(), mScienceLab.isDeviceFound());
            case 1:
                return ApplicationsFragment.newInstance();
            case 2:
                return SavedExperiments.newInstance();
            case 3:
                return DesignExperiments.newInstance();
            case 4:
                return SettingsFragment.newInstance();
            default:
                return HomeFragment.newInstance(mScienceLab.isConnected(), mScienceLab.isDeviceFound());
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


    private void plotMap() {
        List<Entry> sinEntries = new ArrayList<>();
        List<Entry> cosEntries = new ArrayList<>();
        for (float i = 0; i < 7f; i += 0.02f) {
            sinEntries.add(new Entry(i, (float) Math.sin(i)));
            cosEntries.add(new Entry(i, (float) Math.cos(i)));
        }
        List<ILineDataSet> dataSets = new ArrayList<>();
        LineDataSet sinSet = new LineDataSet(sinEntries, "sin curve");
        LineDataSet cosSet = new LineDataSet(cosEntries, "cos curve");
        cosSet.setColor(Color.GREEN);
        cosSet.setCircleColor(Color.GREEN);
        sinSet.setColor(Color.BLUE);
        sinSet.setCircleColor(Color.BLUE);
        dataSets.add(sinSet);
        dataSets.add(cosSet);
        lineChart.setData(new LineData(dataSets));
        lineChart.invalidate();
    }
    BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (mScienceLab.isDeviceFound()) {
                            Toast.makeText(getBaseContext(), "Device found!!", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "PSLab device found");
                        }
                        else {
                            Toast.makeText(getBaseContext(), "Problem!, Reconnect device", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "Problem !");
                        }
                    }
                    else {
                        Toast.makeText(getBaseContext(), "No device connected. check connections.", Toast.LENGTH_LONG).show();
                        Log.d(TAG, "PSLab device not found");
                        }
                    }
                }
            else {
                    Toast.makeText(getBaseContext(), "Please grant permissions to access the device", Toast.LENGTH_LONG).show();
            }
        }
    };
}
