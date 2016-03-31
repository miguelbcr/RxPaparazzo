package com.refineriaweb.rx_paparazzo.sample;

import android.app.Application;
import android.content.Context;
import android.graphics.Point;
import android.net.Uri;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.refineriaweb.rx_paparazzo.library.interactors.GetDimens;
import com.refineriaweb.rx_paparazzo.library.interactors.GetPath;
import com.refineriaweb.rx_paparazzo.sample.activities.StartActivity;
import com.refineriaweb.rx_paparazzo.sample.util.DaggerActivityTestRule;

import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import javax.inject.Inject;

import rx.Observable;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
public class ApplicationTest {
//    @Rule public ActivityTestRule<StartActivity> activityRule = new ActivityTestRule<>(StartActivity.class);
    private UiDevice uiDevice;
    private Context context;
    @Inject GetDimens getDimens;
    @Inject GetPath getPath;
    private ApplicationTestComponent applicationTestComponent;
    @Rule public ActivityTestRule<StartActivity> activityRule =
            new DaggerActivityTestRule<>(StartActivity.class, new DaggerActivityTestRule.OnBeforeActivityLaunchedListener<StartActivity>() {
                @Override
                public void beforeActivityLaunched(@NonNull Application application, @NonNull StartActivity activity) {
                    applicationTestComponent = DaggerApplicationTestComponent.create();
                    ((MyApplication) application).setTestComponent(applicationTestComponent);
                    applicationTestComponent.inject(ApplicationTest.this);
                }
            });

    @Before public void init() {
        uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        context = InstrumentationRegistry.getContext();
    }

    @Test public void showScreenDimens() {
        getPath.with(getUri()).react()
                .subscribe(filepath -> getDimens.printDimens("screen dimens: ", filepath));
    }

//    @Test public void CancelUserActionActivity() {
//        onView(withId(R.id.bt_activity)).perform(click());
//        cancelUserAction();
//    }
//
//    @Test public void CancelUserActionFragment() {
//        onView(withId(R.id.bt_fragment)).perform(click());
//        cancelUserAction();
//    }
//
//    @Test public void TakePhotoActivity() {
//        onView(withId(R.id.bt_activity)).perform(click());
//        takePhoto(false);
//    }
//
//    @Test public void TakePhotoFragment() {
//        onView(withId(R.id.bt_fragment)).perform(click());
//        takePhoto(false);
//    }
//
//    @Test public void TakePhotoActivityCrop() {
//        onView(withId(R.id.bt_activity)).perform(click());
//        takePhoto(true);
//    }
//
//    @Test public void TakePhotoFragmentCrop() {
//        onView(withId(R.id.bt_fragment)).perform(click());
//        takePhoto(true);
//    }

    private void takePhoto(boolean crop) {
        if (crop) onView(withId(R.id.fab_camera_crop)).perform(click());
        else onView(withId(R.id.fab_camera)).perform(click());
        waitTime();

        clickBottomMiddleScreen();
        rotateDevice();
        clickBottomMiddleScreen();

        if (crop) {
            rotateDevice();
            clickTopRightScreen();
        }

        onView(withId(R.id.iv_image)).check(matches(new BoundedMatcher<View, ImageView>(ImageView.class) {
            @Override public void describeTo(Description description) {
                description.appendText("has drawable");
            }

            @Override public boolean matchesSafely(ImageView imageView) {
                return imageView.getDrawable() != null;
            }
        }));
    }

    private void cancelUserAction() {
        onView(withId(R.id.fab_camera)).perform(click());
        waitTime();

        clickBottomMiddleScreen();
        rotateDevice();
        uiDevice.pressBack();

        onView(withId(R.id.iv_image)).check(matches(new BoundedMatcher<View, ImageView>(ImageView.class) {
            @Override public void describeTo(Description description) {
                description.appendText("has not drawable");
            }

            @Override public boolean matchesSafely(ImageView imageView) {
                return imageView.getDrawable() == null;
            }
        }));
    }

    private void rotateDevice() {
        try {
            uiDevice.setOrientationLeft();
            waitTime();
            uiDevice.setOrientationNatural();
            waitTime();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void clickBottomMiddleScreen() {
        WindowManager wm = (WindowManager) InstrumentationRegistry.getTargetContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        int width = size.x;
        int height = size.y;

        uiDevice.click(width/2, height - 100);
        waitTime();
    }

    private void clickTopRightScreen() {
        WindowManager wm = (WindowManager) InstrumentationRegistry.getTargetContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        int width = size.x;

        uiDevice.click(width-50, 100);
        waitTime();
    }

    private void waitTime() {
        try {Thread.sleep(3000);}
        catch (InterruptedException e) { e.printStackTrace();}
    }

    private Observable<int[]> getDimensScreen() {
        return getDimens.with(getUri()).react();
    }

    private Uri getUri() {
        File file = context.getExternalCacheDir();

        return Uri.fromFile(file)
                .buildUpon()
                .appendPath("shoot.jpg")
                .build();
    }
}