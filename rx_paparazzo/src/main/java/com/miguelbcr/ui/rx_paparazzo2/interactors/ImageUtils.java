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

package com.miguelbcr.ui.rx_paparazzo2.interactors;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import com.miguelbcr.ui.rx_paparazzo2.entities.Config;
import com.miguelbcr.ui.rx_paparazzo2.entities.FileData;
import com.miguelbcr.ui.rx_paparazzo2.entities.TargetUi;
import com.miguelbcr.ui.rx_paparazzo2.entities.size.OriginalSize;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.reactivex.exceptions.Exceptions;

public final class ImageUtils {

  private static final String DEFAULT_EXTENSION = "";
  public static final String JPG_FILE_EXTENSION = "jpg";
  private static final String PNG_FILE_EXTENSION = "png";

  public static final String MIME_TYPE_IMAGE_WILDCARD = "image/*";
  public static final String MIME_TYPE_JPEG = "image/jpeg";
  private static final String MIME_TYPE_PNG = "image/png";

  private static final String DATE_FORMAT = "ddMMyyyy_HHmmss_SSS";
  private static final String LOCALE_EN = "en";

  private final TargetUi targetUi;
  private final Config config;

  public ImageUtils(TargetUi targetUi, Config config) {
    this.targetUi = targetUi;
    this.config = config;
  }

  public File getOutputFile(String prefix, String extension) {
    String fileProviderDirectory = config.getFileProviderDirectory();
    File dir = getDir(null, fileProviderDirectory);

    return createTimestampedFile(dir, prefix, extension);
  }

  private File createTimestampedFile(File dir, String prefix, String extension) {
    File file = new File(dir.getAbsolutePath(), createTimestampedFilename(prefix, extension));

    while (file.exists()) {
      String filename = createTimestampedFilename(prefix, extension);
      file = new File(dir.getAbsolutePath(), filename);
    }

    return file;
  }

  public String createTimestampedFilename(String prefix, String extension) {
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT, new Locale(LOCALE_EN));
    String datetime = simpleDateFormat.format(new Date());

    if (!TextUtils.isEmpty(extension) && !extension.startsWith(".")) {
      extension = "." + extension;
    }

    return prefix + datetime + extension;
  }

  public File getPrivateFile(String directory, String filename) {
    File dir = new File(targetUi.getContext().getFilesDir(), directory);
    dir.mkdirs();

    return new File(dir, filename);
  }

  private String getApplicationName(Context context) {
    int stringId = context.getApplicationInfo().labelRes;

    return context.getString(stringId);
  }

  private File getDir(String dirRoot, String dirname) {
    File storageDir = null;

    if (!config.isUseInternalStorage()) {
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
      File dir;
      if (dirRoot == null) {
        dir = Environment.getExternalStorageDirectory();
      } else {
        dir = Environment.getExternalStoragePublicDirectory(dirRoot);
      }

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

  public static String getFileName(String filepath) {
    File file = new File(filepath);

    return file.getName();
  }

  public static String stripPathFromFilename(String fileName) {
    int lastSlash = fileName.lastIndexOf("/");
    if (lastSlash == -1) {
      return fileName;
    } else {
      return fileName.substring(lastSlash + 1);
    }
  }

  public String getFileExtension(String filepath) {
    return getFileExtension(filepath, DEFAULT_EXTENSION);
  }

  public String getFileExtension(String filepath, String defaultExtension) {
    String extension = "";

    if (filepath != null) {
      int i = filepath.lastIndexOf('.');
      if (i > 0) {
        extension = filepath.substring(i + 1);
      } else {
        extension = "";
      }
    }

    if (TextUtils.isEmpty(extension) && !TextUtils.isEmpty(defaultExtension)) {
      extension = defaultExtension;
    }

    return extension;
  }

  String getFileExtension(Uri uri) {
    String mimeType = getMimeType(targetUi.getContext(), uri);

    if (TextUtils.isEmpty(mimeType)) {
      return getFileExtension(uri.getLastPathSegment());
    } else {
      return mimeType.split("/")[1];
    }
  }

  public static String getMimeType(Context context, Uri uri) {
    String mimeType;

    if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
      mimeType = context.getContentResolver().getType(uri);
    } else {
      String path = uri.toString();
      mimeType = getMimeType(path);
    }
    return mimeType;
  }

  public static String getMimeType(String path) {
    String fileExtension = MimeTypeMap.getFileExtensionFromUrl(path);

    return MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.toLowerCase());
  }

  FileData scaleImage(FileData inputData, File destination, int[] dimens) {
    File input = inputData.getFile();
    String mimeType;

    if (config.getSize() instanceof OriginalSize) {
      copyFileAndExifTags(input, destination, dimens);
      mimeType = inputData.getMimeType();
    } else {
      Bitmap bitmap = sampleBitmap(input, dimens[0], dimens[1]);
      if (bitmap == null) {
        copyFileAndExifTags(input, destination, dimens);
        mimeType = inputData.getMimeType();
      } else {
        Bitmap.CompressFormat compressFormat = getCompressFormat(destination.getName());
        if (Bitmap.CompressFormat.JPEG == compressFormat) {
          mimeType = MIME_TYPE_JPEG;
        } else if (Bitmap.CompressFormat.PNG == compressFormat) {
          mimeType = MIME_TYPE_PNG;
        } else {
          throw new IllegalStateException(String.format("Received unexpected compression format '%s'", compressFormat));
        }

        bitmap2file(bitmap, destination, compressFormat);
        copyExifTags(input, destination, dimens);
      }
    }

    return new FileData(inputData, destination, true, mimeType);
  }

  private Bitmap.CompressFormat getCompressFormat(String filePath) {
    Bitmap.CompressFormat compressFormat = Bitmap.CompressFormat.JPEG;
    String extension = getFileExtension(filePath);

    if (extension.toLowerCase().contains(PNG_FILE_EXTENSION)) {
      compressFormat = Bitmap.CompressFormat.PNG;
    }

    return compressFormat;
  }

  private void copyFileAndExifTags(File input, File fileOutput, int[] dimens) {
    copy(input, fileOutput);
    copyExifTags(input, fileOutput, dimens);
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

  private void copyExifTags(File srcFile, File dstFile, int[] dimens) {
    try {
      String dstFilePath = dstFile.getAbsolutePath();
      String srcFilePath = srcFile.getAbsolutePath();
      if (getCompressFormat(dstFilePath) == Bitmap.CompressFormat.JPEG) {
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
      }
    } catch (IOException e) {
      e.printStackTrace();
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

  private Bitmap sampleBitmap(File input, int maxWidth, int maxHeight) {
    BitmapFactory.Options options = sampleSize(input, maxWidth, maxHeight);
    if (options.inSampleSize == 1) {
      return null;
    }

    options.inJustDecodeBounds = false;

    String filePath = input.getAbsolutePath();

    return BitmapFactory.decodeFile(filePath, options);
  }

  public boolean isImage(File input) {
    BitmapFactory.Options options = sampleSize(input, 0, 0);

    return options.outWidth > 0 && options.outHeight > 0;
  }

  private BitmapFactory.Options sampleSize(File input, int maxWidth, int maxHeight) {
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inJustDecodeBounds = true;

    // load dimensions
    String filePath = input.getAbsolutePath();
    BitmapFactory.decodeFile(filePath, options);

    if (maxWidth <= 0 || maxHeight <= 0) {
      options.inSampleSize = 1;
    } else {
      options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight);
    }

    return options;
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
