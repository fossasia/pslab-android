package com.viveksb007.pslab.activity;

import android.content.Intent;
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

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.viveksb007.pslab.R;
import com.viveksb007.pslab.communication.CommunicationHandler;
import com.viveksb007.pslab.communication.ScienceLab;
import com.viveksb007.pslab.fragment.ApplicationsFragment;
import com.viveksb007.pslab.fragment.DesignExperiments;
import com.viveksb007.pslab.fragment.HomeFragment;
import com.viveksb007.pslab.fragment.SavedExperiments;
import com.viveksb007.pslab.fragment.SettingsFragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        usbManager = (UsbManager) getSystemService(USB_SERVICE);
        mScienceLab = new ScienceLab(usbManager);
        if (mScienceLab.isDeviceFound()) {
            Log.d(TAG, "PSLab device found");
        } else {
            Log.d(TAG, "PSLab device not found");
        }

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mHandler = new Handler();

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);

        navHeader = navigationView.getHeaderView(0);
        txtName = (TextView) navHeader.findViewById(R.id.name);
        imgProfile = (ImageView) navHeader.findViewById(R.id.img_profile);
        activityTitles = getResources().getStringArray(R.array.nav_item_activity_titles);

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

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.openDrawer, R.string.closeDrawer) {
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
}
