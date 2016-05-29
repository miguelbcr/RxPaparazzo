package com.fuck_boilerplate.rx_paparazzo.entities;

import android.support.annotation.IntRange;

/**
 * Created by farhad on 16.29.5.
 */
public class UCropFeature {

    private float aspectRatioX;
    private float aspectRatioY;
    private int maxWidth;
    private int maxHeight;

    public void setAspectRatio(float x, float y) {
        this.aspectRatioX = x;
        this.aspectRatioY = y;
    }

    public void setMaxResultSize(@IntRange(from = 100) int width, @IntRange(from = 100) int height) {
        this.maxWidth = width;
        this.maxHeight = height;
    }

    public boolean hasAspectRatio() {
        return (aspectRatioX != 0.0f & aspectRatioY != 0.0f);
    }

    public float getAspectRatioX() {
        return aspectRatioX;
    }

    public float getAspectRatioY() {
        return aspectRatioY;
    }

    public boolean hasMaxResultSize() {
        return (maxWidth != 0 && maxHeight != 0);
    }

    public int getMaxWidth() {
        return maxWidth;
    }

    public int getMaxHeight() {
        return maxHeight;
    }
}
