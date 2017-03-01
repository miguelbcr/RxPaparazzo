package com.miguelbcr.ui.rx_paparazzo2.sample;

import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;

import com.miguelbcr.ui.rx_paparazzo2.sample.activities.StartActivity;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * TESTED ON:
 * - Google Nexus 5 5.0.0 API 21 1080x1920 480dpi
 * - Google Nexus 7 5.1.0 API 22 800x1280 213dpi
 * - Samsung Galaxy S6 - 6.0.0 API 23 1440x2560 640dpi
 */
@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ApplicationTest extends UiActions {

    @Rule
    public ActivityTestRule<StartActivity> activityRule = new ActivityTestRule<>(StartActivity.class);

    @Before
    public void init() {
        // You may need to change the profile if tests are failing because they can't close the gallery picker
        DeviceConfig.CURRENT = DeviceConfig.GOOGLE_NEXUS;
        DeviceConfig.WAIT_TIME_FUDGE_FACTOR = 1.0;

        setUiDevice(UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()));
    }

    @Test
    public void _01_CancelUserActionActivity() {
        onView(withId(R.id.bt_activity)).perform(click());
        cancelUserAction();
    }

    @Test
    public void _02_CancelUserActionFragment() {
        onView(withId(R.id.bt_fragment)).perform(click());
        cancelUserAction();
    }

    @Test
    public void _03_TakePhotoActivity() {
        onView(withId(R.id.bt_activity)).perform(click());
        takePhoto(false);
    }

    @Test
    public void _04_TakePhotoFragment() {
        onView(withId(R.id.bt_fragment)).perform(click());
        takePhoto(false);
    }

    @Test
    public void _05_TakePhotoActivityCrop() {
        onView(withId(R.id.bt_activity)).perform(click());
        takePhoto(true);
    }

    @Test
    public void _06_TakePhotoFragmentCrop() {
        onView(withId(R.id.bt_fragment)).perform(click());
        takePhoto(true);
    }

    @Test
    public void _07_PickUpPhotoActivityCrop() {
        onView(withId(R.id.bt_activity)).perform(click());
        pickUpPhoto(true);
    }

    @Test
    public void _08_PickUpPhotoFragmentCrop() {
        onView(withId(R.id.bt_fragment)).perform(click());
        pickUpPhoto(true);
    }

    @Test
    public void _09_PickUpPhotosActivityCrop() {
        onView(withId(R.id.bt_activity)).perform(click());
        pickUpPhoto(false);
    }

    @Test
    public void _10_PickUpPhotosFragmentCrop() {
        onView(withId(R.id.bt_fragment)).perform(click());
        pickUpPhoto(false);
    }

}