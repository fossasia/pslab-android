package io.pslab.activity;


import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.test.espresso.Espresso;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import tools.fastlane.screengrab.FalconScreenshotStrategy;
import tools.fastlane.screengrab.Screengrab;
import tools.fastlane.screengrab.locale.LocaleTestRule;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.GrantPermissionRule;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.pslab.R;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class ScreenshotsTest {

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

    @Test
    public void testTakeScreenshot() {
        mActivityScenarioRule.getScenario().onActivity(activity -> Screengrab.setDefaultScreenshotStrategy(new FalconScreenshotStrategy(activity)));

        Screengrab.screenshot("dashboard");

        onView(
                allOf(withContentDescription("open_drawer"),
                        childAtPosition(
                                allOf(withId(R.id.toolbar),
                                        childAtPosition(
                                                withClassName(is("com.google.android.material.appbar.AppBarLayout")),
                                                0)),
                                1),
                        isDisplayed())).perform(click());

        Screengrab.screenshot("drawer");

        Espresso.pressBack();

        onView(
                allOf(withId(R.id.applications_recycler_view),
                        childAtPosition(
                                withClassName(is("android.widget.RelativeLayout")),
                                0))).perform(actionOnItemAtPosition(7, click()));

        Screengrab.screenshot("instrument_accelerometer_view");

        Espresso.pressBack();

        onView(
                allOf(withId(R.id.applications_recycler_view),
                        childAtPosition(
                                withClassName(is("android.widget.RelativeLayout")),
                                0))).perform(actionOnItemAtPosition(8, click()));

        Screengrab.screenshot("instrument_barometer_view");

        Espresso.pressBack();

        onView(
                allOf(withId(R.id.applications_recycler_view),
                        childAtPosition(
                                withClassName(is("android.widget.RelativeLayout")),
                                0))).perform(actionOnItemAtPosition(1, click()));

        Screengrab.screenshot("instrument_multimeter_view");

        Espresso.pressBack();

        onView(
                allOf(withId(R.id.applications_recycler_view),
                        childAtPosition(
                                withClassName(is("android.widget.RelativeLayout")),
                                0))).perform(actionOnItemAtPosition(2, click()));

        Screengrab.screenshot("logic_analyzer_view");

        Espresso.pressBack();

        onView(
                allOf(withContentDescription("More options"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.toolbar),
                                        2),
                                1),
                        isDisplayed())).perform(click());

        onView(
                allOf(withId(me.zhanghai.android.materialprogressbar.R.id.title), withText("Pin Layout Front"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.content),
                                        0),
                                0),
                        isDisplayed())).perform(click());

        Screengrab.screenshot("layout_pin_front");

        onView(
                allOf(withContentDescription("open_drawer"),
                        childAtPosition(
                                allOf(withId(R.id.toolbar),
                                        childAtPosition(
                                                withClassName(is("com.google.android.material.appbar.AppBarLayout")),
                                                0)),
                                1),
                        isDisplayed())).perform(click());

        onView(
                allOf(withId(R.id.nav_instruments),
                        childAtPosition(
                                allOf(withId(com.google.android.material.R.id.design_navigation_view),
                                        childAtPosition(
                                                withId(R.id.nav_view),
                                                0)),
                                1),
                        isDisplayed())).perform(click());

        onView(
                allOf(withId(R.id.applications_recycler_view),
                        childAtPosition(
                                withClassName(is("android.widget.RelativeLayout")),
                                0))).perform(actionOnItemAtPosition(0, click()));

        Screengrab.screenshot("oscilloscope_channel_view");

        Espresso.pressBack();
    }

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }
}
