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

import android.net.Uri;
import com.miguelbcr.ui.rx_paparazzo2.entities.TargetUi;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.schedulers.Schedulers;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public final class DownloadImage extends UseCase<String> {
  private final TargetUi targetUi;
  private final ImageUtils imageUtils;
  private Uri uri;

  public DownloadImage(TargetUi targetUi, ImageUtils imageUtils) {
    this.targetUi = targetUi;
    this.imageUtils = imageUtils;
  }

  @Override Observable<String> react() {
    return getObservableDownloadFile();
  }

  public DownloadImage with(Uri uri) {
    this.uri = uri;
    return this;
  }

  private Observable<String> getObservableDownloadFile() {
    return Observable.create(new ObservableOnSubscribe<String>() {
      @Override public void subscribe(ObservableEmitter<String> subscriber) throws Exception {
        try {
          if ("content".equalsIgnoreCase(uri.getScheme())) {
            subscriber.onNext(getContent());
          } else {
            subscriber.onNext(downloadFile());
          }

          subscriber.onComplete();
        } catch (Exception e) {
          subscriber.onError(e);
        }
      }
    }).subscribeOn(Schedulers.io());
  }

  private String downloadFile() throws Exception {
    URL url = new URL(uri.toString());
    URLConnection connection = url.openConnection();
    connection.connect();
    InputStream inputStream = new BufferedInputStream(url.openStream(), 1024);
    String filename = getFilename(uri);
    filename += imageUtils.getFileExtension(uri);
    File file = imageUtils.getPrivateFile(filename);
    imageUtils.copy(inputStream, file);
    return file.getAbsolutePath();
  }

  private String getContent() throws Exception {
    InputStream inputStream = targetUi.getContext().getContentResolver().openInputStream(uri);
    String filename = getFilename(uri);
    filename += imageUtils.getFileExtension(uri);
    File file = imageUtils.getPrivateFile(filename);
    imageUtils.copy(inputStream, file);
    return file.getAbsolutePath();
  }

  private String getFilename(Uri uri) {
    // Remove non alphanumeric characters
    return uri.getLastPathSegment().replaceAll("[^A-Za-z0-9 ]", "");
  }
}
