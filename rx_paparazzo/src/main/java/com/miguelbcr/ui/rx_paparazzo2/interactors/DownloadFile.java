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
import android.os.Build;
import android.support.v4.provider.DocumentFile;

import com.miguelbcr.ui.rx_paparazzo2.entities.Config;
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

  private static final String DOWNLOADED_FILENAME_PREFIX = "DOWNLOAD-";
  private static final String MATCH_ANYTHING_NOT_A_LETTER_OR_NUMBER = "[^A-Za-z0-9 ]";

  private final TargetUi targetUi;
  private final Config config;
  private final ImageUtils imageUtils;
  private Uri uri;
  private FileData fileData;

  public DownloadFile(TargetUi targetUi, Config config, ImageUtils imageUtils) {
    this.targetUi = targetUi;
    this.config = config;
    this.imageUtils = imageUtils;
  }

  @Override
  Observable<FileData> react() {
    return getObservableDownloadFile();
  }

  public DownloadFile with(Uri uri, FileData fileData) {
    this.uri = uri;
    this.fileData = fileData;

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
    String directory = config.getFileProviderDirectory();
    File file = imageUtils.getPrivateFile(directory, filename);

    URL url = new URL(uri.toString());
    URLConnection connection = url.openConnection();
    connection.connect();
    InputStream inputStream = new BufferedInputStream(url.openStream());
    imageUtils.copy(inputStream, file);

    return toFileData(mimeType, filename, file);
  }

  private FileData toFileData(String mimeType, String filename, File destination) {
    if (fileData == null || fileData.getFilename() == null) {
      return new FileData(destination, filename, mimeType);
    } else {
      // maintain existing filename and mime-type unless missing
      String fileMimeType = fileData.getMimeType();
      if (fileMimeType == null) {
        fileMimeType = mimeType;
      }

      return new FileData(fileData, destination, fileMimeType);
    }
  }

  private FileData getUsingContentResolver() throws FileNotFoundException {
    String mimeType = imageUtils.getMimeType(targetUi.getContext(), uri);
    String uriFilename = getFilename(uri);
    String downloadFilename = DOWNLOADED_FILENAME_PREFIX + uriFilename;
    String directory = config.getFileProviderDirectory();
    File file = imageUtils.getPrivateFile(directory, downloadFilename);

    InputStream inputStream = targetUi.getContext().getContentResolver().openInputStream(uri);
    imageUtils.copy(inputStream, file);

    return toFileData(mimeType, uriFilename, file);
  }

  private String getFilename(Uri uri) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      DocumentFile file = DocumentFile.fromSingleUri(targetUi.getContext(), uri);
      if (file != null) {
        String fileName = file.getName();
        if (fileName != null) {
          return ImageUtils.stripPathFromFilename(fileName);
        }
      }
    }

    String fileName = uri.getLastPathSegment();
    String safeFilename = fileName.replaceAll(MATCH_ANYTHING_NOT_A_LETTER_OR_NUMBER, "");

    return safeFilename + "." + imageUtils.getFileExtension(uri);
  }

}
