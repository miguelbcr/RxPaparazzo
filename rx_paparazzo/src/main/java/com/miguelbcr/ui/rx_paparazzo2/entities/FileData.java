package com.miguelbcr.ui.rx_paparazzo2.entities;

import java.io.File;

public class FileData {

    private File file;
    private String filename;

    public FileData(File file, String filename) {
        this.filename = filename;
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    public String getFilename() {
        return filename;
    }

}
