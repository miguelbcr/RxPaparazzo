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
import android.net.Uri;
import android.os.Build;
import android.support.v4.provider.DocumentFile;

import com.miguelbcr.ui.rx_paparazzo2.entities.FileData;
import com.miguelbcr.ui.rx_paparazzo2.entities.TargetUi;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.schedulers.Schedulers;

public final class DownloadFile extends UseCase<FileData> {

  private final TargetUi targetUi;
  private final ImageUtils imageUtils;
  private Uri uri;

  public DownloadFile(TargetUi targetUi, ImageUtils imageUtils) {
    this.targetUi = targetUi;
    this.imageUtils = imageUtils;
  }

  @Override Observable<FileData> react() {
    return getObservableDownloadFile();
  }

  public DownloadFile with(Uri uri) {
    this.uri = uri;
    return this;
  }

  private Observable<FileData> getObservableDownloadFile() {
    return Observable.create(new ObservableOnSubscribe<FileData>() {
      @Override public void subscribe(ObservableEmitter<FileData> subscriber) throws Exception {
        if (!subscriber.isDisposed()) {
          try {
            if ("content".equalsIgnoreCase(uri.getScheme())) {
              subscriber.onNext(getUsingContentResolver());
            } else {
              subscriber.onNext(downloadFile());
            }

            subscriber.onComplete();
          } catch (FileNotFoundException e) {
            subscriber.onError(e);
          }
        }
      }
    }).subscribeOn(Schedulers.io());
  }

  private FileData downloadFile() throws Exception {
    String mimeType = imageUtils.getMimeType(targetUi.getContext(), uri);
    String filename = getFilename(uri);
    File destination = imageUtils.getPrivateFile(filename);

    URL url = new URL(uri.toString());
    URLConnection connection = url.openConnection();
    connection.connect();
    InputStream inputStream = new BufferedInputStream(url.openStream(), 1024);
    imageUtils.copy(inputStream, destination);

    return new FileData(destination, filename, mimeType);
  }

  private FileData getUsingContentResolver() throws FileNotFoundException {
    String mimeType = imageUtils.getMimeType(targetUi.getContext(), uri);
    String filename = getFilename(uri);
    File file = imageUtils.getPrivateFile(filename);

    InputStream inputStream = targetUi.getContext().getContentResolver().openInputStream(uri);
    imageUtils.copy(inputStream, file);

    return new FileData(file, filename, mimeType);
  }

  private String getFilename(Uri uri) {
    if (Build.VERSION.SDK_INT >= 19) {
      DocumentFile file = DocumentFile.fromSingleUri(targetUi.getContext(), uri);
      if (file != null) {
        String fileName = file.getName();
        if (fileName != null) {
          return ImageUtils.stripPathFromFilename(fileName);
        }
      }
    }

    // Remove non alphanumeric characters
    return uri.getLastPathSegment().replaceAll("[^A-Za-z0-9 ]", "") + "." + imageUtils.getFileExtension(uri);
  }

}
