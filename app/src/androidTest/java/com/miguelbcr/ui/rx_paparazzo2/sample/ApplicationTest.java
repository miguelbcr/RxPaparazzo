package com.miguelbcr.ui.rx_paparazzo2.sample;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.RemoteException;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.miguelbcr.ui.rx_paparazzo2.entities.Config;
import com.miguelbcr.ui.rx_paparazzo2.entities.FileData;
import com.miguelbcr.ui.rx_paparazzo2.entities.TargetUi;
import com.miguelbcr.ui.rx_paparazzo2.entities.size.Size;
import com.miguelbcr.ui.rx_paparazzo2.interactors.GetDimens;
import com.miguelbcr.ui.rx_paparazzo2.interactors.ImageUtils;
import com.miguelbcr.ui.rx_paparazzo2.sample.activities.StartActivity;
import com.miguelbcr.ui.rx_paparazzo2.sample.activities.Testable;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.io.File;
import java.util.List;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static com.miguelbcr.ui.rx_paparazzo2.sample.recyclerview.RecyclerViewUtils.withRecyclerView;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.core.CombinableMatcher.both;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * TESTED ON:
 * - Google Nexus 5 5.0.0 API 21 1080x1920 480dpi
 * - Google Nexus 7 5.1.0 API 22 800x1280 213dpi
 */
@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ApplicationTest {

    static class IsImageViewMatcher extends BoundedMatcher<View, ImageView> {

        private int[] imageDimens = {0, 0};

        public IsImageViewMatcher() {
            super(ImageView.class);
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("has drawable");
        }

        @Override
        public boolean matchesSafely(ImageView imageView) {
            Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
            imageDimens = new int[]{bitmap.getWidth(), bitmap.getHeight()};

            return imageView.getDrawable() != null;
        }

        public int[] getMatchedImageDimensions() {
            return imageDimens;
        }
    }

    @Rule
    public ActivityTestRule<StartActivity> activityRule = new ActivityTestRule<>(StartActivity.class);

    private UiDevice uiDevice;

    @Before
    public void init() {
        uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
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

    private void takePhoto(boolean crop) {
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

    private void pickUpPhoto(boolean onlyOne) {
        // With 4 items the recycler view do not scroll properly and do not find the item view and test crash in pos=2
        int imagesToPick = onlyOne ? 1 : 2;

        if (onlyOne) {
            onView(withId(R.id.fab_pickup_image)).perform(click());
        } else {
            onView(withId(R.id.fab_pickup_images)).perform(click());
        }

        waitTime();

        clickImagesOnScreen(imagesToPick);

        // Open selected images
        if (!onlyOne) clickTopRightScreen();

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

        checkDimensions(isImageViewMatcher.getMatchedImageDimensions());
    }

    private void checkDimensions(int[] observedDimensions) {
        Activity activity = ((SampleApplication) InstrumentationRegistry.getTargetContext().getApplicationContext()).getLiveActivity();

        if (activity instanceof Testable) {
            List<String> filePaths = ((Testable) activity).getFilePaths();
            Size size = ((Testable) activity).getSize();

            for (String filePath : filePaths) {
                assertNotNull(filePath);
                assertNotEquals(filePath, "");

                File file = new File(filePath);
                String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
                String mimeType = ImageUtils.getMimeType(filePath);

                FileData fileData = new FileData(file, true, fileName, mimeType);

                getDimens(size).with(fileData).react()
                        .subscribe(dimens -> {
                            int[] dimensPortrait = getDimensionsPortrait(dimens[0], dimens[1]);
                            int[] imageDimensPortrait = getDimensionsPortrait(observedDimensions[0], observedDimensions[1]);
                            int marginOfError = 200;
                            assertThat(dimensPortrait[0], is(both(greaterThan(imageDimensPortrait[0] - marginOfError)).and(lessThan(imageDimensPortrait[0] + marginOfError))));
                            assertThat(dimensPortrait[1], is(both(greaterThan(imageDimensPortrait[1] - marginOfError)).and(lessThan(imageDimensPortrait[1] + marginOfError))));
                        });
            }
        }
    }

    private int[] getDimensionsPortrait(int width, int height) {
        if (width < height) return new int[]{width, height};
        else return new int[]{height, width};
    }

    private GetDimens getDimens(Size size) {
        Config config = new Config();
        config.setSize(size);
        TargetUi targetUi = new TargetUi(activityRule.getActivity());

        return new GetDimens(targetUi, config);
    }

    private void cancelUserAction() {
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
        int width = getScreenDimensions()[0];

        uiDevice.click(width - 100, 150);
        waitTime();
    }

    private void closeDrawer() {
        int screenDimens[] = getScreenDimensions();
        int width = screenDimens[0];
        int height = screenDimens[1];

        uiDevice.click(width - 50, height / 2);
        waitTime();
    }

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

            if (imagesToPick == 1) uiDevice.click(x, y);
            else uiDevice.swipe(x, y, x, y, 500);

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