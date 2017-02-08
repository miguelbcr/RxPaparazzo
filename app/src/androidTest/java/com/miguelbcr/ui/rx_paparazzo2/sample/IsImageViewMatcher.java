package com.miguelbcr.ui.rx_paparazzo2.sample;

import android.support.test.espresso.matcher.BoundedMatcher;
import android.view.View;
import android.widget.ImageView;

import org.hamcrest.Description;

class IsImageViewMatcher extends BoundedMatcher<View, ImageView> {

    public IsImageViewMatcher() {
        super(ImageView.class);
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("has drawable");
    }

    @Override
    public boolean matchesSafely(ImageView imageView) {
        return imageView.getDrawable() != null;
    }
}