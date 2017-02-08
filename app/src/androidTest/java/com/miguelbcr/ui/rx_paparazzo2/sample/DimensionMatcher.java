package com.miguelbcr.ui.rx_paparazzo2.sample;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;

import com.miguelbcr.ui.rx_paparazzo2.entities.FileData;
import com.miguelbcr.ui.rx_paparazzo2.entities.size.CustomMaxSize;
import com.miguelbcr.ui.rx_paparazzo2.entities.size.OriginalSize;
import com.miguelbcr.ui.rx_paparazzo2.entities.size.ScreenSize;
import com.miguelbcr.ui.rx_paparazzo2.entities.size.Size;
import com.miguelbcr.ui.rx_paparazzo2.interactors.Dimensions;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import static org.hamcrest.Matchers.allOf;

class DimensionMatcher extends BaseMatcher<FileData> {

    private enum Dimension {
        WIDTH, HEIGHT;
    }

    private enum Match {
        EQUALS, LESS_THAN_EQUAL, GREATER_THAN;
    }

    private Dimension dimension;
    private Match match;
    private int expected;

    public DimensionMatcher(Dimension dimension, Match match, int expected) {
        this.dimension = dimension;
        this.match = match;
        this.expected = expected;
    }

    private int getDimension(FileData fileData) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(fileData.getFile().getAbsolutePath(), options);

        switch (dimension) {
            case WIDTH:
                return options.outWidth;

            case HEIGHT:
                return options.outHeight;

            default:
                throw new IllegalStateException("Unknown dimension " + dimension);
        }
    }

    @Override
    public boolean matches(Object item) {
        FileData fileData = (FileData) item;

        int dimension = getDimension(fileData);
        switch (match) {
            case EQUALS:
                return dimension == expected;

            case LESS_THAN_EQUAL:
                return dimension <= expected;

            case GREATER_THAN:
                return dimension > expected;

        }
        return false;
    }

    @Override
    public void describeTo(Description description) {
        // TODO
    }

    public static Matcher<FileData> fromSize(Activity activity, Size size, Dimensions originalSize) {
        if (size instanceof OriginalSize) {
            DimensionMatcher widthMatcher = new DimensionMatcher(Dimension.WIDTH, Match.EQUALS, originalSize.getWidth());
            DimensionMatcher heightMatcher = new DimensionMatcher(Dimension.HEIGHT, Match.EQUALS, originalSize.getHeight());

            return allOf(widthMatcher, heightMatcher);
        } else if (size instanceof CustomMaxSize) {
            CustomMaxSize maxSize = (CustomMaxSize) size;
            int maxDimension = maxSize.getMaxImageSize();
            DimensionMatcher widthMatcher = new DimensionMatcher(Dimension.WIDTH, Match.LESS_THAN_EQUAL, maxDimension);
            DimensionMatcher heightMatcher = new DimensionMatcher(Dimension.HEIGHT, Match.LESS_THAN_EQUAL, maxDimension);

            return allOf(widthMatcher, heightMatcher);
        } else if (size instanceof ScreenSize) {
            DisplayMetrics metrics = activity.getResources().getDisplayMetrics();
            DimensionMatcher widthMatcher = new DimensionMatcher(Dimension.WIDTH, Match.LESS_THAN_EQUAL, metrics.widthPixels);
            DimensionMatcher heightMatcher = new DimensionMatcher(Dimension.HEIGHT, Match.LESS_THAN_EQUAL, metrics.heightPixels);

            return allOf(widthMatcher, heightMatcher);
        } else {
            DisplayMetrics metrics = activity.getResources().getDisplayMetrics();
            DimensionMatcher widthMatcher = new DimensionMatcher(Dimension.WIDTH, Match.LESS_THAN_EQUAL, metrics.widthPixels / 8);
            DimensionMatcher heightMatcher = new DimensionMatcher(Dimension.HEIGHT, Match.LESS_THAN_EQUAL, metrics.heightPixels / 8);

            return allOf(widthMatcher, heightMatcher);
        }
    }
}
