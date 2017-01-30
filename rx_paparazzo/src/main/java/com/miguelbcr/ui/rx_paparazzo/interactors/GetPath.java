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

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import com.miguelbcr.ui.rx_paparazzo.entities.TargetUi;
import rx.Observable;

public final class GetPath extends UseCase<String> {
  private final TargetUi targetUi;
  private final DownloadImage downloadImage;
  private Uri uri;

  public GetPath(TargetUi targetUi, DownloadImage downloadImage) {
    this.targetUi = targetUi;
    this.downloadImage = downloadImage;
  }

  public GetPath with(Uri uri) {
    this.uri = uri;
    return this;
  }

  @Override public Observable<String> react() {
    return getPath();
  }

  @SuppressLint("NewApi") private Observable<String> getPath() {
    boolean isFromKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    Context context = targetUi.activity();
    String filePath = null;

    if (context == null) {
      return Observable.just("");
    }

    if (uri == null) {
      return null;
    }

    if (isFromKitKat && DocumentsContract.isDocumentUri(context, uri)) {
      if (isExternalStorageDocument(uri)) {
        Document document = getDocument(uri);
        if ("primary".equalsIgnoreCase(document.type)) {
          filePath = Environment.getExternalStorageDirectory() + "/" + document.id;
        }
      } else if (isDownloadsDocument(uri)) {
        String id = DocumentsContract.getDocumentId(uri);
        Uri contentUri =
            ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),
                Long.valueOf(id));
        filePath = getDataColumn(context, contentUri, null, null);
      } else if (isMediaDocument(uri)) {
        Document document = getDocument(uri);
        Uri contentUri = null;
        if ("image".equals(document.type)) {
          contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        } else if ("video".equals(document.type)) {
          contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        } else if ("audio".equals(document.type)) {
          contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        }

        filePath = getDataColumn(context, contentUri, MediaStore.Images.Media._ID + "=?",
            new String[] { document.id });
      }
    } else if ("content".equalsIgnoreCase(uri.getScheme())) {
      filePath = getDataColumn(context, uri, null, null);
    } else if ("file".equalsIgnoreCase(uri.getScheme())) {
      filePath = uri.getPath();
    }

    if (filePath == null) {
      return downloadImage.with(uri).react();
    }

    return Observable.just(filePath);
  }

  private class Document {
    String type;
    String id;
  }

  @SuppressLint("NewApi") private Document getDocument(Uri uri) {
    Document document = new Document();
    String docId = DocumentsContract.getDocumentId(uri);
    String[] docArray = docId.split(":");
    document.type = docArray[0];
    document.id = docArray[1];
    return document;
  }

  private String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
    Cursor cursor = null;
    String column = MediaStore.Images.Media.DATA;
    String[] projection = { column };

    try {
      cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
      cursor.moveToFirst();
      return cursor.getString(cursor.getColumnIndexOrThrow(column));
    } catch (Exception e) {
      //            throw Exceptions.propagate(e);
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
