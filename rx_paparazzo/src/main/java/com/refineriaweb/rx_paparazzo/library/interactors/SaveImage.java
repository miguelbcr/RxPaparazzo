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

package com.refineriaweb.rx_paparazzo.library.interactors;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.refineriaweb.rx_paparazzo.library.entities.Config;
import com.refineriaweb.rx_paparazzo.library.entities.Size;
import com.refineriaweb.rx_paparazzo.library.entities.TargetUi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;

import rx.Observable;
import rx.exceptions.Exceptions;

public final class SaveImage extends UseCase<String> {
    private static final String TAG = "RxPaparazzo";
    private final TargetUi targetUi;
    private final Config config;
    private final GetPath getPath;
    private final GetDimens getDimens;
    private Uri uri;

    @Inject public SaveImage(TargetUi targetUi, Config config, GetPath getPath, GetDimens getDimens) {
        this.targetUi = targetUi;
        this.config = config;
        this.getPath = getPath;
        this.getDimens = getDimens;
    }

    public SaveImage with(Uri uri) {
        this.uri = uri;
        return this;
    }

    @Override public Observable<String> react() {
        return getOutputUri()
                .flatMap(outputUri -> Observable.zip(getPath.with(uri).react(), getPath.with(outputUri).react(), getDimens.with(uri).react(),
                        (filePath, filePathOutput, dimens) -> {
                            String filepath = scaleImage(filePath, filePathOutput, dimens);
                            MediaScannerConnection.scanFile(targetUi.getContext(), new String[] {filepath}, new String[] { "image/jpeg" }, null);
                            return filepath;
                        }));
    }

    private Observable<Uri> getOutputUri() {
        return Observable.just(Uri.fromFile(getOutputFile()));
    }

    private File getOutputFile() {
        String filename = new SimpleDateFormat("ddMMyyyy_HHmmss", new Locale("en")).format(new Date());
        filename = "IMG-" + filename + ".jpg";
        String dirname = getApplicationName(targetUi.getContext());
        File dir = getPublicDir(null, dirname);
        return new File(dir.getAbsolutePath(), filename);
    }

    private String getApplicationName(Context context) {
        int stringId = context.getApplicationInfo().labelRes;
        return context.getString(stringId);
    }

    private File getPublicDir(String dirRoot, String dirname) {
        File storageDir = null;

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File dir = (dirRoot != null) ? Environment.getExternalStoragePublicDirectory(dirRoot) : Environment.getExternalStorageDirectory();
            storageDir = new File(dir, dirname);

            // Create the storage directory if it does not exist
            if (!storageDir.exists()) {
                if (!storageDir.mkdirs()) {
                    Log.e(TAG, "Failed to create directory " + dirname);
                    storageDir = null;
                }
            }
        }

        if (storageDir == null) {
            Log.e(TAG, "Failed to create directory " + dirRoot + "/" + dirname + ".External storage not mounted");
            storageDir = getPrivateDir(dirname);
        }

        return storageDir;
    }

    private File getPrivateDir(String dirname) {
        File dir = targetUi.getContext().getFilesDir();
        File storageDir = TextUtils.isEmpty(dirname) ? dir : new File(dir, dirname);

        // Create the storage directory if it does not exist
        if (!storageDir.exists()) {
            if (!storageDir.mkdirs()) {
                Log.e(TAG, "Failed to create directory " + dirname);
                storageDir = null;
            }
        }

        return storageDir;
    }

    //TODO return a file name based on date (month-day-hour-seconds) instead of 'shoot'
    private String scaleImage(String filePath, String filePathOutput, int[] dimens) {
        getDimens.printDimens("input size : ", filePath);
        if (config.getSize() == Size.Original) return filePath;
        // TODO check behaviour with last param, rotateIfNeeded
        Bitmap bitmap = handleSamplingAndRotationBitmap(filePath, dimens[0], dimens[1], true);

        if (bitmap != null) {
            File file = new File(filePath);
            File fileScaled = new File(filePathOutput);
            bitmap2file(bitmap, fileScaled, Bitmap.CompressFormat.JPEG);
            copyExifRotation(file, fileScaled);
            file.delete();
            getDimens.printDimens("output size : ", fileScaled.getAbsolutePath());
            return fileScaled.getAbsolutePath();
        }

        return filePath;
    }

    private boolean bitmap2file(Bitmap bitmap, File file, Bitmap.CompressFormat compressFormat) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            bitmap.compress(compressFormat, 90, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            throw Exceptions.propagate(e);
        }
    }

    private boolean copyExifRotation(File sourceFile, File destFile) {
        if (sourceFile == null || destFile == null) return false;
        try {
            ExifInterface exifSource = new ExifInterface(sourceFile.getAbsolutePath());
            ExifInterface exifDest = new ExifInterface(destFile.getAbsolutePath());
            exifDest.setAttribute(ExifInterface.TAG_ORIENTATION, exifSource.getAttribute(ExifInterface.TAG_ORIENTATION));
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
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);

        if (rotateIfNeeded)
            bitmap = rotateImageIfRequired(bitmap, filePath);

        return bitmap;
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            int heightRatio = Math.round((float) height / (float) reqHeight);
            int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
            float totalPixels = width * height;
            float totalReqPixels = reqWidth * reqHeight * 2;

            while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixels) {
                inSampleSize++;
            }
        }

        return inSampleSize;
    }

    private Bitmap rotateImageIfRequired(Bitmap img, String filePath) {
        try {
            ExifInterface ei = new ExifInterface(filePath);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    img = rotateImage(img, 90);
                case ExifInterface.ORIENTATION_ROTATE_180:
                    img = rotateImage(img, 180);
                case ExifInterface.ORIENTATION_ROTATE_270:
                    img = rotateImage(img, 270);
            }

            return img;
        } catch (IOException e) {
            e.printStackTrace();
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
