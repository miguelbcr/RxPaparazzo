package com.miguelbcr.ui.rx_paparazzo2.entities;

import android.util.Log;

import com.miguelbcr.ui.rx_paparazzo2.interactors.Dimensions;

import java.io.File;
import java.io.Serializable;

public class FileData implements Serializable {

    private static final String FILENAME_MIMETYPE = "%s (%s)";
    private static final String FILENAME_MIMETYPE_TITLE = "%s (%s) - %s";
    private static final String FILENAME_TRANSIENT_MIMETYPE_TITLE = "%s %s (%s) - %s";

    private File file;
    private String filename;
    private String mimeType;
    private String title;
    private boolean transientFile;
    private boolean exceededMaximumFileSize;
    private Dimensions originalDimensions;

    public static FileData toFileDataDeleteSourceFileIfTransient(FileData source, File file, boolean transientFile, String mimeType) {
        deleteSourceFile(source);

        return new FileData(source, file, transientFile, mimeType);
    }

    public static void deleteSourceFile(FileData fileData) {
        if (fileData.isTransientFile()) {
            File file = fileData.getFile();
            if (file != null && file.exists()) {
                try {
                    Log.i(FileData.class.getSimpleName(), String.format("Removing source file '%s'", file.getAbsolutePath()));
                    if (!file.delete()) {
                        // silently fail delete
                    }
                } catch (Exception e) {
                    Log.i(FileData.class.getSimpleName(), String.format("Could not remove source file '%s'", file.getAbsolutePath()), e);
                }
            }
        }
    }

    public static FileData exceededMaximumFileSize(FileData source) {
        return new FileData(null, true, source.getFilename(), source.getMimeType(), source.getTitle(), source.getOriginalDimensions(), true);
    }

    public FileData(FileData source, Dimensions dimensions) {
        this(source.getFile(), source.isTransientFile(), source.getFilename(), source.getMimeType(), source.getTitle(), dimensions, source.isExceededMaximumFileSize());
    }

    public FileData(FileData source, File file, boolean transientFile, String mimeType) {
        this(file, transientFile, source.getFilename(), mimeType, source.getTitle(), source.getOriginalDimensions(), source.isExceededMaximumFileSize());
    }

    public FileData(File file, boolean transientFile, String filename, String mimeType) {
        this(file, transientFile, filename, mimeType, null, null, false);
    }

    public FileData(File file, boolean transientFile, String filename, String mimeType, String title) {
        this(file, transientFile, filename, mimeType, title, null, false);
    }

//    public FileData(File file, boolean transientFile, String filename, String mimeType, String title, Dimensions dimensions) {
//        this(file, transientFile, filename, mimeType, title, dimensions, false);
//    }

    public FileData(File file, boolean transientFile, String filename, String mimeType, String title, Dimensions originalDimensions, boolean exceededMaximumFileSize) {
        this.filename = filename;
        this.transientFile = transientFile;
        this.mimeType = mimeType;
        this.file = file;
        this.title = title;
        this.exceededMaximumFileSize = exceededMaximumFileSize;
        this.originalDimensions = originalDimensions;
    }

    public Dimensions getOriginalDimensions() {
        return originalDimensions;
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

    public boolean isTransientFile() {
        return transientFile;
    }

    public boolean isExceededMaximumFileSize() {
        return exceededMaximumFileSize;
    }

    public String describe() {
        if (title == null) {
            return String.format(FILENAME_MIMETYPE, filename, mimeType);
        }

        return String.format(FILENAME_MIMETYPE_TITLE, filename, mimeType, title);
    }

    @Override
    public String toString() {
        String transientDescription = transientFile ? "Transient" : "Not transient";
        return String.format(FILENAME_TRANSIENT_MIMETYPE_TITLE, transientDescription, filename, mimeType, title);
    }
}
