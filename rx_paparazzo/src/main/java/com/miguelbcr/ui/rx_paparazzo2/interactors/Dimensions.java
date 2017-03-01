package com.miguelbcr.ui.rx_paparazzo2.interactors;

import java.io.Serializable;

public final class Dimensions implements Serializable {

    private static final long serialVersionUID = 1L;

    private int width;
    private int height;

    public Dimensions() {
    }

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
