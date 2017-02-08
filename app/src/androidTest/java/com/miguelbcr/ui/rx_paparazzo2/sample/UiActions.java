package com.miguelbcr.ui.rx_paparazzo2.sample;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.os.RemoteException;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.uiautomator.UiDevice;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import com.miguelbcr.ui.rx_paparazzo2.entities.FileData;
import com.miguelbcr.ui.rx_paparazzo2.entities.size.Size;
import com.miguelbcr.ui.rx_paparazzo2.interactors.Dimensions;
import com.miguelbcr.ui.rx_paparazzo2.sample.activities.Testable;

import org.hamcrest.Matcher;

import java.io.File;
import java.util.List;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.miguelbcr.ui.rx_paparazzo2.sample.recyclerview.RecyclerViewUtils.withRecyclerView;
import static org.junit.Assert.assertNotNull;

class UiActions {

    private UiDevice uiDevice;

    void setUiDevice(UiDevice uiDevice) {
        this.uiDevice = uiDevice;
    }

    void takePhoto(boolean crop) {
        if (crop) {
            onView(withId(R.id.fab_camera_crop)).perform(click());
        } else {
            onView(withId(R.id.fab_camera)).perform(click());
        }

        waitTime();

        clickBottomMiddleScreen();
        rotateDevice();
        clickBottomMiddleScreen();

        if (crop) {
            rotateDevice();
            clickTopRightScreen();
        }

        checkImageDimensions(0);
    }

    void pickUpPhoto(boolean onlyOne) {
        int imagesToPick = onlyOne ? 1 : 2;

        if (onlyOne) {
            onView(withId(R.id.fab_pickup_image)).perform(click());
        } else {
            onView(withId(R.id.fab_pickup_images)).perform(click());
        }

        waitTime();

        clickImagesOnScreen(imagesToPick);

        // Open selected images
        if (!onlyOne) {
            clickTopRightScreen(250);
        }

        waitTime();

        // Close crop screen/s
        for (int i = 0; i < imagesToPick; i++) {
            clickTopRightScreen();
        }

        waitTime();

        for (int i = 0; i < imagesToPick; i++) {
            checkImageDimensions(i);
        }
    }


    private void checkImageDimensions(int index) {
        onView(withId(R.id.rv_images)).perform(RecyclerViewActions.scrollToPosition(index));

        waitTime();

        Matcher<View> itemAtPosition = withRecyclerView(R.id.rv_images).atPositionOnView(index, R.id.iv_image);

        IsImageViewMatcher isImageViewMatcher = new IsImageViewMatcher();
        onView(itemAtPosition).check(matches(isImageViewMatcher));

        checkDimensions();
    }

    private void checkDimensions() {
        Testable testable = getTestable();
        List<FileData> fileDatas = testable.getFileDatas();

        assertNotNull(fileDatas);
        for (FileData fileData : fileDatas) {
            assertNotNull(fileData);

            File file = fileData.getFile();
            assertNotNull(file);

            Size expectedSize = testable.getSize();
            Dimensions originalDimensions = fileData.getOriginalDimensions();
            DimensionMatcher.fromSize(getActivity(), expectedSize, originalDimensions).matches(fileData);
        }
    }

    private Testable getTestable() {
        Activity activity = getActivity();
        if (activity instanceof Testable) {
            return ((Testable) activity);
        }

        throw new IllegalStateException("Expected Activity to implement Testable");
    }

    private Activity getActivity() {
        return ((SampleApplication) InstrumentationRegistry.getTargetContext().getApplicationContext()).getLiveActivity();
    }

    void cancelUserAction() {
        onView(withId(R.id.fab_camera)).perform(click());
        waitTime();

        clickBottomMiddleScreen();
        rotateDevice();
        uiDevice.pressBack();

        withRecyclerView(R.id.rv_images).isEmpty();

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
        int screenDimens[] = getScreenDimensions();
        int width = screenDimens[0];
        int height = screenDimens[1];

        uiDevice.click(width / 2, height - 100);
        waitTime();
    }

    private void clickTopRightScreen() {
        clickTopRightScreen(100);
    }

    private void clickTopRightScreen(int offset) {
        int width = getScreenDimensions()[0];

        uiDevice.click(width - offset, 150);
        waitTime();
    }

//    private void closeDrawer() {
//        int screenDimens[] = getScreenDimensions();
//        int width = screenDimens[0];
//        int height = screenDimens[1];
//
//        uiDevice.click(width - 50, height / 2);
//        waitTime();
//    }

    private void clickImagesOnScreen(int imagesToPick) {
        int screenDimens[] = getScreenDimensions();
        int width = screenDimens[0];
        int height = screenDimens[1];
        int y = 0;

        //closeDrawer();

        for (int i = 0; i < imagesToPick; i++) {
            int widthQuarter = width / 4;
            int x = (i % 2 == 0) ? widthQuarter : widthQuarter * 3;
            y += (i % 2 == 0) ? height / 4 : 0;

            if (imagesToPick == 1) {
                uiDevice.click(x, y);
            } else {
                uiDevice.swipe(x, y, x, y, 500);
            }

            waitTime();
        }
    }

    private int[] getScreenDimensions() {
        WindowManager wm = (WindowManager) InstrumentationRegistry.getTargetContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        return new int[]{size.x, size.y};
    }

    private void waitTime() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
