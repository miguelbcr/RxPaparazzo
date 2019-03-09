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

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.Nullable;
import com.miguelbcr.ui.rx_paparazzo2.entities.Config;
import com.miguelbcr.ui.rx_paparazzo2.entities.FileData;
import com.miguelbcr.ui.rx_paparazzo2.entities.TargetUi;

import java.io.File;

import io.reactivex.Observable;

public final class GetPath extends UseCase<FileData> {

  private static final String TAG = GetPath.class.getSimpleName();
  private static final String URI_SCHEME_CONTENT = "content";
  private static final String URI_SCHEME_FILE = "file";
  private static final String PUBLIC_DOWNLOADS_URI = "content://downloads/public_downloads";
  private static final String DOCUMENT_AUTHORITY_EXTERNAL_STORAGE = "com.android.externalstorage.documents";
  private static final String DOCUMENT_AUTHORITY_DOWNLOADS = "com.android.providers.downloads.documents";
  private static final String DOCUMENT_AUTHORITY_MEDIA = "com.android.providers.media.documents";
  private static final String DOCUMENT_TYPE_PRIMARY = "primary";
  private static final String DOCUMENT_TYPE_IMAGE = "image";
  private static final String DOCUMENT_TYPE_VIDEO = "video";
  private static final String DOCUMENT_TYPE_AUDIO = "audio";

  private static class Document {
    String type;
    String id;
  }

  private final Config config;
  private final TargetUi targetUi;
  private final DownloadFile downloadFile;
  private Uri uri;

  public GetPath(Config config, TargetUi targetUi, DownloadFile downloadFile) {
    this.config = config;
    this.targetUi = targetUi;
    this.downloadFile = downloadFile;
  }

  public GetPath with(Uri uri) {
    this.uri = uri;
    return this;
  }

  @Override
  public Observable<FileData> react() {
    return getFileData();
  }

  @SuppressLint("NewApi")
  private Observable<FileData> getFileData() {
    Context context = targetUi.getContext();

    if (uri == null || context == null) {
      return null;
    }

    FileData fileData = getFileData(context);

    if (fileData != null && fileData.getFile() != null) {
      return Observable.just(fileData);
    }

    return downloadFile.with(uri, fileData).react();
  }

  @Nullable
  private FileData getFileData(Context context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(context, uri)) {
      if (isExternalStorageDocument(uri)) {
        Document document = getDocument(uri);

        if (DOCUMENT_TYPE_PRIMARY.equalsIgnoreCase(document.type)) {
          return getPrimaryExternalDocument(document);
        }
      } else if (isDownloadsDocument(uri)) {
        return getDownloadsDocument(context);
      } else if (isMediaDocument(uri)) {
        return getMediaDocument(context);
      }
    } else if (URI_SCHEME_CONTENT.equalsIgnoreCase(uri.getScheme())) {
      if (!isFileProvider(context)) {
        return getDataColumn(context, uri, null, null);
      }
    } else if (URI_SCHEME_FILE.equalsIgnoreCase(uri.getScheme())) {
      return getFile(context);
    }

    return null;
  }

  private FileData getFile(Context context) {
    File file = new File(uri.getPath());
    String fileName = ImageUtils.getFileName(uri.getPath());
    String mimeType = ImageUtils.getMimeType(context, uri);

    return new FileData(file, false, fileName, mimeType);
  }

  private boolean isFileProvider(Context context) {
    String authority = config.getFileProviderAuthority(context);

    return uri.getPath().startsWith(authority);
  }

  private FileData getPrimaryExternalDocument(Document document) {
    String mimeType = ImageUtils.getMimeType(document.id);
    String fileName = ImageUtils.stripPathFromFilename(document.id);
    String filePath = Environment.getExternalStorageDirectory() + "/" + document.id;
    File file = new File(filePath);

    return new FileData(file, false, fileName, mimeType);
  }

  private FileData getDownloadsDocument(Context context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      String id = DocumentsContract.getDocumentId(uri);
      Uri contentUri;

      try {
        contentUri = ContentUris.withAppendedId(Uri.parse(PUBLIC_DOWNLOADS_URI), Long.valueOf(id));
      } catch (NumberFormatException e) {
        Log.e(TAG, e.getMessage());
        e.printStackTrace();
        contentUri = uri;
      }

      return getDataColumn(context, contentUri, null, null);
    }

    throw new IllegalStateException("Minimum Android API version must be be KitKat to use DocumentsContract API");
  }

  private FileData getMediaDocument(Context context) {
    Document document = getDocument(uri);
    Uri contentUri = null;
    if (DOCUMENT_TYPE_IMAGE.equals(document.type)) {
      contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    } else if (DOCUMENT_TYPE_VIDEO.equals(document.type)) {
      contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
    } else if (DOCUMENT_TYPE_AUDIO.equals(document.type)) {
      contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    }

    return getDataColumn(context, contentUri, MediaStore.Images.Media._ID + "=?", new String[] { document.id });
  }

  @SuppressLint("NewApi")
  private Document getDocument(Uri uri) {
    Document document = new Document();
    String docId = DocumentsContract.getDocumentId(uri);
    String[] docArray = docId.split(":");
    document.type = docArray[0];
    document.id = docArray[1];

    return document;
  }

  private FileData getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
    Cursor cursor = null;
    String dataColumn = MediaStore.Images.Media.DATA;
    String nameColumn = MediaStore.Images.Media.DISPLAY_NAME;
    String mimeTypeColumn = MediaStore.Images.Media.MIME_TYPE;
    String titleColumn = MediaStore.Images.Media.TITLE;

    String[] projection = { dataColumn, nameColumn, mimeTypeColumn, titleColumn };

    try {
      cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
      if (cursor != null && cursor.moveToFirst()) {
        String filePath = cursor.getString(cursor.getColumnIndexOrThrow(dataColumn));
        String fileName = cursor.getString(cursor.getColumnIndexOrThrow(nameColumn));
        String mimeType = cursor.getString(cursor.getColumnIndexOrThrow(mimeTypeColumn));
        String title = cursor.getString(cursor.getColumnIndexOrThrow(titleColumn));

        File file;
        if (filePath != null) {
          file = new File(filePath);
        } else {
          file = null;
        }

        return new FileData(file, false, fileName, mimeType, title);
      } else {
        return null;
      }
    } catch (Exception e) {
      return null;
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }
  }

  private boolean isExternalStorageDocument(Uri uri) {
    return DOCUMENT_AUTHORITY_EXTERNAL_STORAGE.equals(uri.getAuthority());
  }

  private boolean isDownloadsDocument(Uri uri) {
    return DOCUMENT_AUTHORITY_DOWNLOADS.equals(uri.getAuthority());
  }

  private boolean isMediaDocument(Uri uri) {
    return DOCUMENT_AUTHORITY_MEDIA.equals(uri.getAuthority());
  }
}
