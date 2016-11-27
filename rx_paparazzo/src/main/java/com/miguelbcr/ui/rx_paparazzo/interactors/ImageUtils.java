/*
 * Copyright 2016 Miguel Garcia
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

package com.miguelbcr.ui.rx_paparazzo.interactors;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;
import com.miguelbcr.ui.rx_paparazzo.entities.Config;
import com.miguelbcr.ui.rx_paparazzo.entities.TargetUi;
import com.miguelbcr.ui.rx_paparazzo.entities.size.OriginalSize;
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
  private final TargetUi targetUi;
  private final Config config;

  public ImageUtils(TargetUi targetUi, Config config) {
    this.targetUi = targetUi;
    this.config = config;
  }

  File getOutputFile(String extension) {
    String dirname = getApplicationName(targetUi.getContext());
    File dir = getDir(null, dirname);
    return getFile(dir, extension);
  }

  private File getFile(File dir, String extension) {
    SimpleDateFormat simpleDateFormat =
        new SimpleDateFormat(Constants.DATE_FORMAT, new Locale(Constants.LOCALE_EN));
    String datetime = simpleDateFormat.format(new Date());
    File file = new File(dir.getAbsolutePath(), "IMG-" + datetime + extension);

    while (file.exists()) {
      datetime = simpleDateFormat.format(new Date());
      file = new File(dir.getAbsolutePath(), "IMG-" + datetime + extension);
    }

    return file;
  }

  File getPrivateFile(String filename) {
    File dir = new File(targetUi.getContext().getFilesDir(), Constants.SUBDIR);
    dir.mkdirs();
    return new File(dir, filename);
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
      File dir = (dirRoot != null) ? Environment.getExternalStoragePublicDirectory(dirRoot)
          : Environment.getExternalStorageDirectory();
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

  String getFileExtension(String filepath) {
    String extension = "";

    if (filepath != null) {
      int i = filepath.lastIndexOf('.');
      extension = i > 0 ? filepath.substring(i) : "";
    }

    return (TextUtils.isEmpty(extension)) ? ".jpg" : extension;
  }

  String getFileExtension(Uri uri) {
    String mimeType;

    if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
      mimeType = targetUi.getContext().getContentResolver().getType(uri);
    } else {
      String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
      mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.toLowerCase());
    }

    return (TextUtils.isEmpty(mimeType)) ? getFileExtension(uri.getLastPathSegment())
        : "." + mimeType.split("/")[1];
  }

  String scaleImage(String filePath, String filePathOutput, int[] dimens) {
    if (config.getSize() instanceof OriginalSize) {
      copyFileAndExifTags(filePath, filePathOutput, dimens);
      return filePathOutput;
    }

    Bitmap bitmap = handleBitmapSampling(filePath, dimens[0], dimens[1]);
    if (bitmap == null) {
      copyFileAndExifTags(filePath, filePathOutput, dimens);
      return filePathOutput;
    }

    bitmap2file(bitmap, new File(filePathOutput), getCompressFormat(filePathOutput));
    copyExifTags(filePath, filePathOutput, dimens);

    return filePathOutput;
  }

  private Bitmap.CompressFormat getCompressFormat(String filePath) {
    Bitmap.CompressFormat compressFormat = Bitmap.CompressFormat.JPEG;
    final String extension = getFileExtension(filePath);

    if (extension.toLowerCase().contains(Constants.EXT_PNG)) {
      compressFormat = Bitmap.CompressFormat.PNG;
    }

    return compressFormat;
  }

  private void copyFileAndExifTags(String filePath, String filePathOutput, int[] dimens) {
    copy(new File(filePath), new File(filePathOutput));
    copyExifTags(filePath, filePathOutput, dimens);
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

  public void copy(File src, File dst) {
    try {
      InputStream in = new FileInputStream(src);
      copy(in, dst);
    } catch (IOException e) {
      e.printStackTrace();
      throw Exceptions.propagate(e);
    }
  }

  private void bitmap2file(Bitmap bitmap, File file, Bitmap.CompressFormat compressFormat) {
    FileOutputStream fileOutputStream = null;
    try {
      fileOutputStream = new FileOutputStream(file);
      bitmap.compress(compressFormat, 90, fileOutputStream);
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

  private void copyExifTags(String srcFilePath, String dstFilePath, int[] dimens) {
    if (getCompressFormat(dstFilePath) == Bitmap.CompressFormat.JPEG) {
      try {
        ExifInterface exifSource = new ExifInterface(srcFilePath);
        ExifInterface exifDest = new ExifInterface(dstFilePath);

        for (String attribute : getExifTags()) {
          String tagValue = exifSource.getAttribute(attribute);

          if (!TextUtils.isEmpty(tagValue)) {
            exifDest.setAttribute(attribute, tagValue);
          }
        }

        exifDest.setAttribute(ExifInterface.TAG_IMAGE_WIDTH, String.valueOf(dimens[0]));
        exifDest.setAttribute(ExifInterface.TAG_IMAGE_LENGTH, String.valueOf(dimens[1]));
        exifDest.saveAttributes();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private String[] getExifTags() {
    return new String[] {
        ExifInterface.TAG_DATETIME, ExifInterface.TAG_EXPOSURE_TIME, ExifInterface.TAG_FLASH,
        ExifInterface.TAG_FOCAL_LENGTH, ExifInterface.TAG_GPS_ALTITUDE,
        ExifInterface.TAG_GPS_ALTITUDE_REF, ExifInterface.TAG_GPS_DATESTAMP,
        ExifInterface.TAG_GPS_LATITUDE, ExifInterface.TAG_GPS_LATITUDE_REF,
        ExifInterface.TAG_GPS_LONGITUDE, ExifInterface.TAG_GPS_LONGITUDE_REF,
        ExifInterface.TAG_GPS_PROCESSING_METHOD, ExifInterface.TAG_WHITE_BALANCE,
        ExifInterface.TAG_ORIENTATION, ExifInterface.TAG_MAKE, ExifInterface.TAG_GPS_TIMESTAMP,
        ExifInterface.TAG_MODEL, ExifInterface.TAG_ISO_SPEED_RATINGS, ExifInterface.TAG_SUBSEC_TIME,
        ExifInterface.TAG_DATETIME_DIGITIZED, ExifInterface.TAG_SUBSEC_TIME_DIGITIZED,
        ExifInterface.TAG_SUBSEC_TIME_ORIGINAL, ExifInterface.TAG_METERING_MODE,
        ExifInterface.TAG_F_NUMBER
    };
  }

  private Bitmap handleBitmapSampling(String filePath, int maxWidth, int maxHeight) {
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inJustDecodeBounds = true;
    BitmapFactory.decodeFile(filePath, options);
    options.inSampleSize =
        (maxWidth <= 0 || maxHeight <= 0) ? 1 : calculateInSampleSize(options, maxWidth, maxHeight);

    if (options.inSampleSize == 1) {
      return null;
    }

    options.inJustDecodeBounds = false;
    return BitmapFactory.decodeFile(filePath, options);
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
      return new int[] { width, height };
    } else {
      return new int[] { height, width };
    }
  }
}
