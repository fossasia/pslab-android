package io.pslab.activity;


import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.IdlingResource;
import androidx.test.espresso.PerformException;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.util.HumanReadables;
import androidx.test.espresso.util.TreeIterables;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import tools.fastlane.screengrab.FalconScreenshotStrategy;
import tools.fastlane.screengrab.Screengrab;
import tools.fastlane.screengrab.UiAutomatorScreenshotStrategy;
import tools.fastlane.screengrab.locale.LocaleTestRule;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiScrollable;
import androidx.test.uiautomator.UiSelector;
import androidx.test.uiautomator.Until;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.concurrent.TimeoutException;

import io.pslab.R;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class ScreenshotsTest {
    private static final int LAUNCH_TIMEOUT = 10000;
    private static final String APP_PACKAGE_NAME = "io.pslab";
    UiDevice mDevice;

    @ClassRule
    public static final LocaleTestRule localeTestRule = new LocaleTestRule();

    @Rule
    public ActivityScenarioRule<MainActivity> mActivityScenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Rule
    public GrantPermissionRule mGrantPermissionRule =
            GrantPermissionRule.grant(
                    "android.permission.ACCESS_FINE_LOCATION",
                    "android.permission.RECORD_AUDIO",
                    "android.permission.WRITE_EXTERNAL_STORAGE");

    @Before
    public void setUp() {
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        mDevice = UiDevice.getInstance(instrumentation);
        mDevice.pressHome();

        final String launcherPackage = getLauncherPackageName();
        assertThat(launcherPackage, notNullValue());

        Context context = getApplicationContext();
        final Intent intent = context.getPackageManager().getLaunchIntentForPackage(APP_PACKAGE_NAME);
        assert intent != null;
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
        Screengrab.setDefaultScreenshotStrategy(new UiAutomatorScreenshotStrategy());

        mDevice.wait(Until.hasObject(By.pkg(APP_PACKAGE_NAME).depth(0)), LAUNCH_TIMEOUT);
    }

    private String getLauncherPackageName() {
        final Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);

        PackageManager pm = getApplicationContext().getPackageManager();
        ResolveInfo resolveInfo = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        assert resolveInfo != null;
        return resolveInfo.activityInfo.packageName;
    }

    @Test
    public void testTakeScreenshot() throws UiObjectNotFoundException {
        Screengrab.screenshot("dashboard");

        UiObject openDrawer = mDevice.findObject(new UiSelector().description("open_drawer"));
        openDrawer.click();
        Screengrab.screenshot("drawer");

        UiScrollable navRecyclerView = new UiScrollable(new UiSelector().resourceId(APP_PACKAGE_NAME + ":id/nav_instruments"));
        UiObject item = navRecyclerView.getChild(new UiSelector().text("Instruments"));
        item.click();

        UiScrollable applicationsRecyclerView = new UiScrollable(new UiSelector().resourceId(APP_PACKAGE_NAME + ":id/applications_recycler_view"));
        applicationsRecyclerView.scrollTextIntoView("ACCELEROMETER");
        item = applicationsRecyclerView.getChild(new UiSelector().text("ACCELEROMETER"));
        item.clickAndWaitForNewWindow();
        Screengrab.screenshot("instrument_accelerometer_view");

        mDevice.pressBack();

        applicationsRecyclerView.scrollTextIntoView("BAROMETER");
        item = applicationsRecyclerView.getChild(new UiSelector().text("BAROMETER"));
        item.clickAndWaitForNewWindow();
        Screengrab.screenshot("instrument_barometer_view");

        mDevice.pressBack();

        applicationsRecyclerView.scrollTextIntoView("MULTIMETER");
        item = applicationsRecyclerView.getChild(new UiSelector().text("MULTIMETER"));
        item.clickAndWaitForNewWindow();
        Screengrab.screenshot("instrument_multimeter_view");

        mDevice.pressBack();

        applicationsRecyclerView.scrollTextIntoView("LOGIC ANALYZER");
        item = applicationsRecyclerView.getChild(new UiSelector().text("LOGIC ANALYZER"));
        item.clickAndWaitForNewWindow();
        Screengrab.screenshot("logic_analyzer_view");

        mDevice.pressBack();

        UiObject moreOptions = mDevice.findObject(new UiSelector().description("More options"));
        moreOptions.click();

        UiObject pinLayoutFront = mDevice.findObject(new UiSelector().text("Pin Layout Front"));
        pinLayoutFront.clickAndWaitForNewWindow();
        Screengrab.screenshot("layout_pin_front");

        openDrawer.click();

        item = navRecyclerView.getChild(new UiSelector().text("Instruments"));
        item.clickAndWaitForNewWindow();

        applicationsRecyclerView.scrollTextIntoView("OSCILLOSCOPE");
        item = applicationsRecyclerView.getChild(new UiSelector().text("OSCILLOSCOPE"));
        item.clickAndWaitForNewWindow();
        Screengrab.screenshot("oscilloscope_channel_view");

        mDevice.pressBack();
    }
}
