package org.fossasia.pslab;

import android.support.test.espresso.contrib.DrawerActions;
import android.support.test.espresso.contrib.NavigationViewActions;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.fossasia.pslab.activity.MainActivity;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Created by viveksb007 on 18/7/17.
 */

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainActivityTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<MainActivity>(MainActivity.class);

    @Test
    public void checkingAllFragments() throws Throwable {
        Thread.sleep(2000);

        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.nav_device));

        // checking home fragment views
        onView(withId(R.id.img_device_status)).check(matches(isDisplayed()));
        onView(withId(R.id.tv_device_status)).check(matches(isDisplayed()));

        // Shifting to Instruments Fragment
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.nav_instruments));
        Thread.sleep(1000);

    /*    // checking all is visible tiles inside Application Fragment
        onView(withText("Oscilloscope")).check(matches(isDisplayed()));
        onView(withText("Multimeter")).check(matches(isDisplayed()));
        onView(withText("Logical Analyzer")).check(matches(isDisplayed()));
        onView(withText("Sensors")).check(matches(isDisplayed()));
        onView(withText("Wave Generator")).check(matches(isDisplayed()));
        onView(withText("Power Source")).check(matches(isDisplayed()));  */

        // Shifting to Settings Fragment
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.nav_settings));
        Thread.sleep(1000);
        onView(withText("Auto Start")).check(matches(isDisplayed()));
    }

}
