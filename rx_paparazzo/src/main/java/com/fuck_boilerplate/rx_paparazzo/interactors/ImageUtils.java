/*
 * Copyright 2016 FuckBoilerplate
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.fuck_boilerplate.rx_paparazzo.interactors;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Environment;
import android.text.TextUtils;

import com.fuck_boilerplate.rx_paparazzo.entities.Config;
import com.fuck_boilerplate.rx_paparazzo.entities.TargetUi;
import com.fuck_boilerplate.rx_paparazzo.entities.size.OriginalSize;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import rx.exceptions.Exceptions;

public final class ImageUtils {
    private static final String DATE_FORMAT = "ddMMyyyy_HHmmss_SSS";
    private static final String LOCALE_EN = "en";
    private final TargetUi targetUi;
    private final Config config;

    public ImageUtils(TargetUi targetUi, Config config) {
        this.targetUi = targetUi;
        this.config = config;
    }

    public File getOutputFile() {
        String filename = new SimpleDateFormat(DATE_FORMAT, new Locale(LOCALE_EN)).format(new Date());
        filename = "IMG-" + filename + ".jpg";
        String dirname = getApplicationName(targetUi.getContext());
        File dir = getDir(null, dirname);
        return new File(dir.getAbsolutePath(), filename);
    }

    private String getApplicationName(Context context) {
        int stringId = context.getApplicationInfo().labelRes;
        return context.getString(stringId);
    }

    private File getDir(String dirRoot, String dirname) {
        File storageDir = null;

        if (!config.useInternalStorage()) {
            storageDir = getPublicDir(dirRoot, dirname);
        }

        if (storageDir == null) {
            storageDir = getPrivateDir(dirname);
        }

        return storageDir;
    }

    private File getPublicDir(String dirRoot, String dirname) {
        File storageDir = null;

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File dir = (dirRoot != null) ? Environment.getExternalStoragePublicDirectory(dirRoot) : Environment.getExternalStorageDirectory();
            storageDir = new File(dir, dirname);

            if (!storageDir.exists() && !storageDir.mkdirs()) {
                storageDir = null;
            }
        }

        return storageDir;
    }

    private File getPrivateDir(String dirname) {
        File dir = targetUi.getContext().getFilesDir();
        File storageDir = TextUtils.isEmpty(dirname) ? dir : new File(dir, dirname);

        // Create the storage directory if it does not exist
        if (!storageDir.exists() && !storageDir.mkdirs()) {
            storageDir = null;
        }
        return storageDir;
    }

    public String scaleImage(String filePath, String filePathOutput, int[] dimens) {
        if (config.getSize() instanceof OriginalSize) {
            return copyFile(filePath, filePathOutput);
        }

        boolean rotateIfNeeded = false;
        Bitmap bitmap = handleSamplingAndRotationBitmap(filePath, dimens[0], dimens[1], rotateIfNeeded);

        if (bitmap == null) {
            return copyFile(filePath, filePathOutput);
        }

        File file = new File(filePath);
        File fileScaled = new File(filePathOutput);
        bitmap2file(bitmap, fileScaled, Bitmap.CompressFormat.JPEG);
        copyExifRotation(file, fileScaled, rotateIfNeeded);

        return filePathOutput;
    }

    private String copyFile(String filePath, String newfilePath) {
        File file = new File(filePath);
        File fileOutput = new File(newfilePath);

        copy(file, fileOutput);
        return newfilePath;
    }

    public void copy(InputStream in, File dst) {
        OutputStream out = null;

        try {
            out = new FileOutputStream(dst);
            byte[] buffer = new byte[1024];
            int length;

            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw Exceptions.propagate(e);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.flush();
                    out.close();
                }
            } catch (IOException e) {
                // nothing
            }
        }
    }

    private void copy(File src, File dst) {
        try {
            InputStream in = new FileInputStream(src);
            copy(in, dst);
        } catch (IOException e) {
            e.printStackTrace();
            throw Exceptions.propagate(e);
        }
    }

    private boolean bitmap2file(Bitmap bitmap, File file, Bitmap.CompressFormat compressFormat) {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(file);
            bitmap.compress(compressFormat, 90, fileOutputStream);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            throw Exceptions.propagate(e);
        } finally {
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.flush();
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                // nothing
            }
        }
    }

    private boolean copyExifRotation(File sourceFile, File destFile, boolean rotateIfNeeded) {
        if (sourceFile == null || destFile == null) {
            return false;
        }
        try {
            ExifInterface exifSource = new ExifInterface(sourceFile.getAbsolutePath());
            ExifInterface exifDest = new ExifInterface(destFile.getAbsolutePath());
            String value = rotateIfNeeded ? String.valueOf(ExifInterface.ORIENTATION_NORMAL) : exifSource.getAttribute(ExifInterface.TAG_ORIENTATION);
            exifDest.setAttribute(ExifInterface.TAG_ORIENTATION, value);
            exifDest.saveAttributes();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            throw Exceptions.propagate(e);
        }
    }

    private Bitmap handleSamplingAndRotationBitmap(String filePath, int maxWidth, int maxHeight, boolean rotateIfNeeded) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        options.inSampleSize = (maxWidth <= 0 || maxHeight <= 0) ? 1 : calculateInSampleSize(options, maxWidth, maxHeight);

        if (options.inSampleSize == 1)
            return null;

        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);

        if (rotateIfNeeded)
            bitmap = rotateImageIfRequired(bitmap, filePath);

        return bitmap;
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int maxWidth, int maxHeight) {
        int inSampleSize = 1;
        int[] dimensPortrait = getDimensionsPortrait(options.outWidth, options.outHeight);
        int[] maxDimensPortrait = getDimensionsPortrait(maxWidth, maxHeight);
        float width = dimensPortrait[0];
        float height = dimensPortrait[1];
        float newMaxWidth = maxDimensPortrait[0];
        float newMaxHeight = maxDimensPortrait[1];

        if (height > newMaxHeight || width > newMaxWidth) {
            int heightRatio = Math.round(height / newMaxHeight);
            int widthRatio = Math.round(width / newMaxWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
            float totalPixels = width * height;
            float totalReqPixels = newMaxWidth * newMaxHeight * 2;

            while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixels) {
                inSampleSize++;
            }
        }

        return inSampleSize;
    }

    private int[] getDimensionsPortrait(int width, int height) {
        if (width < height) {
            return new int[]{width, height};
        } else {
            return new int[]{height, width};
        }
    }

    private Bitmap rotateImageIfRequired(Bitmap img, String filePath) {
        try {
            ExifInterface ei = new ExifInterface(filePath);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            Bitmap processedImage = img;
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    processedImage = rotateImage(img, 90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    processedImage = rotateImage(img, 180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    processedImage = rotateImage(img, 270);
                    break;
                default:
                    break;
            }
            return processedImage;
        } catch (IOException e) {
            throw Exceptions.propagate(e);
        }
    }

    private Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }
}
