package com.miguelbcr.ui.rx_paparazzo.entities.size;

/**
 * Scale source image proportionally. Max size of new image will be
 * approximately equal to maxImageSize.
 */
public class CustomMaxSize implements Size {

    private int maxImageSize = 1024;

    public CustomMaxSize() {
    }

    public CustomMaxSize(int maxSize) {
        this.maxImageSize = maxSize;
    }

    public int getMaxImageSize() {
        return maxImageSize;
    }
}
