package com.miguelbcr.ui.rx_paparazzo2.interactors;

public final class Dimensions {

    private int width;
    private int height;

    public Dimensions(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public boolean hasSize() {
        return width > 0 && height > 0;
    }
}
