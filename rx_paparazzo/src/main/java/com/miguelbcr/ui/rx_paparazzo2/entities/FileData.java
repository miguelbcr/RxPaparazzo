package com.miguelbcr.ui.rx_paparazzo2.entities;

import android.util.Log;

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

    public FileData(FileData source, File file, boolean transientFile, String mimeType) {
        this(file, transientFile, source.getFilename(), mimeType, source.getTitle());
    }

    public FileData(File file, boolean transientFile, String filename, String mimeType) {
        this(file, transientFile, filename, mimeType, null);
    }

    public FileData(File file, boolean transientFile, String filename, String mimeType, String title) {
        this.filename = filename;
        this.transientFile = transientFile;
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

    public boolean isTransientFile() {
        return transientFile;
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
