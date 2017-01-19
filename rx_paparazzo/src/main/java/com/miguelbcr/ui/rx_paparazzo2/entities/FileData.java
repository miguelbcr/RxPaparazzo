package com.miguelbcr.ui.rx_paparazzo2.entities;

import java.io.File;

public class FileData {

    public static final String FILENAME_MIMETYPE = "%s (%s)";
    public static final String FILENAME_MIMETYPE_TITLE = "%s (%s) - %s";

    private File file;
    private String filename;
    private String mimeType;
    private String title;

    public FileData(FileData source, File file, String mimeType) {
        this(file, source.getFilename(), mimeType, source.getTitle());
    }

    public FileData(File file, String filename, String mimeType) {
        this(file, filename, mimeType, null);
    }

    public FileData(File file, String filename, String mimeType, String title) {
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

    @Override
    public String toString() {
        if (title == null) {
            return String.format(FILENAME_MIMETYPE, filename, mimeType);
        }

        return String.format(FILENAME_MIMETYPE_TITLE, filename, mimeType, title);
    }
}
