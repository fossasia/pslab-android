package org.fossasia.pslab.activity;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.view.View;
import android.widget.LinearLayout;

import org.fossasia.pslab.R;

import mehdi.sakout.aboutpage.AboutPage;

/**
 * Created by viveksb007 on 15/3/17.
 */

public class AboutUs extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        simulateDayNight(0);
        setContentView(R.layout.about_toolbar);
        AppBarLayout appBarLayout=(AppBarLayout)findViewById(R.id.appBarAnim);
        View aboutPage = new AboutPage(this)
                .isRTL(false)
                .setImage(R.drawable.logo200x200)
                .setDescription(getString(R.string.about_us_description))
                .addGroup("Connect with us")
                .addEmail("pslab-fossasia@googlegroups.com")
                .addWebsite("http://pslab.fossasia.org/")
                .addGitHub("fossasia")
                .addFacebook("fossasia")
                .addTwitter("fossasia")
                .addYoutube("UCQprMsG-raCIMlBudm20iLQ")
                .create();

        appBarLayout.addView(aboutPage,-1);
    }

    private void simulateDayNight(int currentSetting) {
        final int DAY = 0;
        final int NIGHT = 1;
        final int FOLLOW_SYSTEM = 3;

        int currentNightMode = getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;
        if (currentSetting == DAY && currentNightMode != Configuration.UI_MODE_NIGHT_NO) {
            AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_NO);
        } else if (currentSetting == NIGHT && currentNightMode != Configuration.UI_MODE_NIGHT_YES) {
            AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_YES);
        } else if (currentSetting == FOLLOW_SYSTEM) {
            AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }
    }

}
