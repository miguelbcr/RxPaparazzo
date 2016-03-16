/*
 * Copyright 2016 Refinería Web
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

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

import rx.Observable;

public class GetPath extends UseCase<String> {
    private Uri uri;
    private Context context;

    public GetPath with(Context context, Uri uri) {
        this.context = context;
        this.uri = uri;
        return this;
    }

    @Override public Observable<String> react() {
        return Observable.just(getPath(context, uri));
    }

    @SuppressLint("NewApi")
    private String getPath(final Context context, final Uri uri) {
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            if (isExternalStorageDocument(uri)) {
                final Document document = getDocument(uri);
                if ("primary".equalsIgnoreCase(document.type)) return Environment.getExternalStorageDirectory() + "/" + document.id;
                // TODO handle non-primary volumes
                return null;
            } else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId( Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                return getDataColumn(context, contentUri, null, null);
            } else if (isMediaDocument(uri)) {
                final Document document = getDocument(uri);
                Uri contentUri = null;
                if ("image".equals(document.type)) contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                else if ("video".equals(document.type)) contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                else if ("audio".equals(document.type)) contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

                return getDataColumn(context, contentUri, "_id=?", new String[] {document.id});
            }
        }
        else if ("content".equalsIgnoreCase(uri.getScheme())) return getDataColumn(context, uri, null, null);
        else if ("file".equalsIgnoreCase(uri.getScheme())) return uri.getPath();

        return null;
    }

    private class Document {
        String type;
        String id;
    }

    @SuppressLint("NewApi")
    private Document getDocument(Uri uri) {
        Document document = new Document();
        final String docId = DocumentsContract.getDocumentId(uri);
        final String[] docArray = docId.split(":");
        document.type = docArray[0];
        document.id = docArray[1];
        return document;
    }

    private String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = MediaStore.Images.ImageColumns.DATA;
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int columnIndex = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(columnIndex);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
        }

        return null;
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
