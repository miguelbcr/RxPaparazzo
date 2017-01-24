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
import android.support.annotation.Nullable;

import com.miguelbcr.ui.rx_paparazzo2.entities.FileData;
import com.miguelbcr.ui.rx_paparazzo2.entities.TargetUi;

import java.io.File;

import io.reactivex.Observable;

public final class GetPath extends UseCase<FileData> {

  private static class Document {
    String type;
    String id;
  }

  private final TargetUi targetUi;
  private final DownloadFile downloadFile;
  private Uri uri;

  public GetPath(TargetUi targetUi, DownloadFile downloadFile) {
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
    Context context = targetUi.activity();

    if (uri == null) {
      return null;
    }

    FileData fileData = getFileData(context);

    if (fileData != null) {
      return Observable.just(fileData);
    }

    return downloadFile.with(uri).react();
  }

  @Nullable
  private FileData getFileData(Context context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(context, uri)) {
      if (isExternalStorageDocument(uri)) {
        Document document = getDocument(uri);

        if ("primary".equalsIgnoreCase(document.type)) {
          return getPrimaryExternalDocument(document);
        }
      } else if (isDownloadsDocument(uri)) {
        return getDownloadsDocument(context);
      } else if (isMediaDocument(uri)) {
        return getMediaDocument(context);
      }
    } else if ("content".equalsIgnoreCase(uri.getScheme())) {
      return getDataColumn(context, uri, null, null);
    } else if ("file".equalsIgnoreCase(uri.getScheme())) {
      File file = new File(uri.getPath());
      String fileName = ImageUtils.getFileName(uri.getPath());
      String mimeType = ImageUtils.getMimeType(targetUi.getContext(), uri);

      return new FileData(file, fileName, mimeType);
    }

    return null;
  }

  private FileData getPrimaryExternalDocument(Document document) {
    String filePath = Environment.getExternalStorageDirectory() + "/" + document.id;
    String mimeType = ImageUtils.getMimeType(document.id);
    String fileName = ImageUtils.stripPathFromFilename(document.id);

    return new FileData(new File(filePath), fileName, mimeType);
  }

  private FileData getDownloadsDocument(Context context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      String id = DocumentsContract.getDocumentId(uri);
      Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

      return getDataColumn(context, contentUri, null, null);
    }

    throw new IllegalStateException("Must be KitKat");
  }

  private FileData getMediaDocument(Context context) {
    Document document = getDocument(uri);
    Uri contentUri = null;
    if ("image".equals(document.type)) {
      contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    } else if ("video".equals(document.type)) {
      contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
    } else if ("audio".equals(document.type)) {
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
      cursor.moveToFirst();
      String fileData = cursor.getString(cursor.getColumnIndexOrThrow(dataColumn));
      String fileName = cursor.getString(cursor.getColumnIndexOrThrow(nameColumn));
      String mimeType = cursor.getString(cursor.getColumnIndexOrThrow(mimeTypeColumn));
      String title = cursor.getString(cursor.getColumnIndexOrThrow(titleColumn));

      return new FileData(new File(fileData), fileName, mimeType, title);
    } catch (Exception e) {
      return null;
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }
  }

  private boolean isExternalStorageDocument(Uri uri) {
    return "com.android.externalstorage.documents".equals(uri.getAuthority());
  }

  private boolean isDownloadsDocument(Uri uri) {
    return "com.android.providers.downloads.documents".equals(uri.getAuthority());
  }

  private boolean isMediaDocument(Uri uri) {
    return "com.android.providers.media.documents".equals(uri.getAuthority());
  }
}
