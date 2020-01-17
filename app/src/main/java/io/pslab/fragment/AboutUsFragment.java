package io.pslab.fragment;


import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatDelegate;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import io.pslab.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;

/**
 * Created by Abhinav on 12-05-2018.
 */

public class AboutUsFragment extends Fragment {

    @BindView(R.id.appBarAnim)
    AppBarLayout appBarLayout;

    public static AboutUsFragment newInstance() {
        return new AboutUsFragment();
    }

    public AboutUsFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about_us, container, false);
        simulateDayNight(0);
        ButterKnife.bind(this, view);
        View aboutPage = new AboutPage(getActivity())
                .isRTL(false)
                .setImage(R.drawable.logo200x200)
                .addWebsite("https://goo.gl/forms/sHlmRAPFmzcGQ27u2", getString(R.string.nav_report))
                .addItem(new Element(getString(R.string.version), R.drawable.ic_widgets_black_24dp))
                .addItem(new Element(getString(R.string.flavor), R.drawable.ic_android_black_24dp))
                .setDescription(getString(R.string.about_us_description))
                .addGroup("Connect with us")
                .addEmail("pslab-fossasia@googlegroups.com")
                .addWebsite("https://pslab.io/")
                .addGitHub("fossasia?utf8=✓&q=pslab")
                .addFacebook("pslabio")
                .addTwitter("pslabio")
                .addYoutube("UCQprMsG-raCIMlBudm20iLQ")
                .create();

        appBarLayout.addView(aboutPage, -1);
        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
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
