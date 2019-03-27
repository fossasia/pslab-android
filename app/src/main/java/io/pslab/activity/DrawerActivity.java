package io.pslab.activity;

import android.content.Intent;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.io.IOException;

import io.pslab.R;
import io.pslab.fragment.AboutUsFragment;
import io.pslab.fragment.FAQFragment;
import io.pslab.fragment.HomeFragment;
import io.pslab.others.CustomTabService;
import io.pslab.others.ScienceLabCommon;

import static io.pslab.others.ScienceLabCommon.scienceLab;

public abstract class DrawerActivity extends AppCompatActivity {

    public NavigationView navigationView;
    DrawerLayout drawer;
    public Toolbar toolbar;
    View navHeader;
    private TextView txtName;
    public static int navItemIndex = 0;
    private CustomTabService customTabService;
    private static final String TAG_DEVICE = "device";
    private static final String TAG_ABOUTUS = "aboutUs";
    private static final String TAG_FAQ = "faq";
    private static String CURRENT_TAG = "back";
    private String[] activityTitles;

    public abstract String getSensorName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setContentView(int layoutResID) {
        drawer = (DrawerLayout) getLayoutInflater().inflate(R.layout.activity_drawer, null);
        FrameLayout activityContainer = drawer.findViewById(R.id.frame_base);
        getLayoutInflater().inflate(layoutResID, activityContainer, true);
        super.setContentView(drawer);
        toolbar = findViewById(R.id.toolbar_base);
        setSupportActionBar(toolbar);
        navigationView = findViewById(R.id.nav_view_base);
        setUpNavigationView();
        navHeader = navigationView.getHeaderView(0);
        customTabService = new CustomTabService(DrawerActivity.this);
        txtName = navHeader.findViewById(io.pslab.R.id.name);
        activityTitles = getResources().getStringArray(io.pslab.R.array.nav_item_activity_titles);
        setPSLabVersionIDs();
    }

    private void loadHomeFragment() {
        setToolbarTitle();
        if (drawer != null && getSupportFragmentManager().findFragmentByTag(CURRENT_TAG) != null) {
            drawer.closeDrawers();
            return;
        }
        Fragment fragment = getHomeFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.fade_in,
                R.anim.fade_out);
        fragmentTransaction.replace(R.id.sensor_frame, fragment, CURRENT_TAG);
        fragmentTransaction.commit();
        fragmentTransaction.addToBackStack(null);
        if (drawer != null) {
            drawer.closeDrawers();
            invalidateOptionsMenu();
        }
    }

    private Fragment getHomeFragment() {
        switch (navItemIndex) {
            case 1:
                return HomeFragment.newInstance(ScienceLabCommon.scienceLab.isConnected(), ScienceLabCommon.scienceLab.isDeviceFound());
            case 2:
                return null;
            case 3:
                return AboutUsFragment.newInstance();
            case 4:
                return FAQFragment.newInstance();
        }
        return null;
    }

    private void setToolbarTitle() {
        getSupportActionBar().setTitle(activityTitles[navItemIndex]);
    }

    public void unCheckAllMenuItems(Menu menu) {
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
        }
    }

    private void setUpNavigationView() {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_instruments:
                        if (drawer != null) {
                            drawer.closeDrawers();
                        }
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                        return true;
                    case R.id.nav_device:
                        navItemIndex = 1;
                        CURRENT_TAG = TAG_DEVICE;
                        break;
                    case R.id.nav_settings:
                        if (drawer != null) {
                            drawer.closeDrawers();
                        }
                        Intent intent2 = new Intent(getApplicationContext(), SettingsActivity.class);
                        intent2.putExtra("title", "Settings");
                        startActivity(intent2);
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
                        return true;
                    case R.id.nav_app_version:
                        setTitleColor(R.color.gray);
                        return true;
                    case R.id.sensor_data_logger:
                        if (drawer != null) {
                            drawer.closeDrawers();
                        }
                        startActivity(new Intent(getApplicationContext(), DataLoggerActivity.class));
                        return true;
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
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.sensor_frame);
        if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawers();
            return;
        }
        if (fragment instanceof HomeFragment && HomeFragment.isWebViewShowing) {
            ((HomeFragment) fragment).hideWebView();
            return;
        }
        if (navItemIndex != 0) {
            navItemIndex = 0;
            getSupportFragmentManager().popBackStack();
            toolbar.setTitle(getSensorName());
            int size_menu = navigationView.getMenu().size();
            for (int i = 0; i < size_menu; i++) {
                final MenuItem item = navigationView.getMenu().getItem(i);
                if (item.hasSubMenu()) {
                    unCheckAllMenuItems(item.getSubMenu());
                } else {
                    item.setChecked(false);
                }
            }
        } else {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        navItemIndex = 0;
        selectNavMenu();
        int size_menu = navigationView.getMenu().size();
        for (int i = 0; i < size_menu; i++) {
            final MenuItem item = navigationView.getMenu().getItem(i);
            if (item.hasSubMenu()) {
                unCheckAllMenuItems(item.getSubMenu());
            } else {
                item.setChecked(false);
            }
        }
    }
}
