package com.miguelbcr.ui.rx_paparazzo2.entities;

import java.io.File;

public class FileData {

    private File file;
    private String filename;
    private String mimeType;
    private String title;

    public FileData(FileData source, File file, String mimeType) {
        this(file, mimeType, source.getFilename(), source.getTitle());
    }

    public FileData(File file, String mimeType, String filename) {
        this(file, mimeType, filename, null);
    }

    public FileData(File file, String mimeType, String filename, String title) {
        this.filename = filename;
        this.mimeType = mimeType;
        this.file = file;
        this.title = title;
    }

    public File getFile() {
        return file;
    }

    public String getFilename() {
        return filename;
    }

    public String getMimeType() {
        return mimeType;
    }

    public String getTitle() {
        return title;
    }
}
