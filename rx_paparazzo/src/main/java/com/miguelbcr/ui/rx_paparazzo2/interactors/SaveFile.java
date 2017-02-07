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

import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.util.Log;

import com.miguelbcr.ui.rx_paparazzo2.entities.Config;
import com.miguelbcr.ui.rx_paparazzo2.entities.FileData;
import com.miguelbcr.ui.rx_paparazzo2.entities.TargetUi;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;

public final class SaveFile extends UseCase<FileData> {

  private static final String TAG = SaveFile.class.getSimpleName();
  private static final String SAVED_FILE_PREFIX = "SAVED-";

  private final TargetUi targetUi;
  private final Config config;
  private final GetDimens getDimens;
  private final ImageUtils imageUtils;

  private FileData fileData;

  public SaveFile(TargetUi targetUi, Config config, GetDimens getDimens, ImageUtils imageUtils) {
    this.targetUi = targetUi;
    this.config = config;
    this.getDimens = getDimens;
    this.imageUtils = imageUtils;
  }

  public SaveFile with(FileData fileData) {
    this.fileData = fileData;

    return this;
  }

  @Override
  public Observable<FileData> react() {
    return getDimens.with(fileData).react().flatMap(new Function<int[], ObservableSource<FileData>>() {
      @Override
      public ObservableSource<FileData> apply(int[] dimens) throws Exception {
        return saveAndSendToMediaScanner(dimens);
      }
    });
  }

  private ObservableSource<FileData> saveAndSendToMediaScanner(int[] dimens) throws Exception {
    FileData saved = save(dimens);

    if (config.isSendToMediaScanner()) {
      if (config.isUseInternalStorage()) {
        File file = fileData.getFile();
        String message = String.format("Media scanner will not be able to access internal storage '%s'", file.getAbsolutePath());
        Log.w(TAG, message);
      }

//      sendToMediaScanner(saved);
      sendToMediaScannerIntent(saved);
    }

    return Observable.just(saved);
  }

  private FileData save(int[] dimens) throws Exception {
    if (imageUtils.isImage(fileData.getFile())) {
      return saveImageAndDeleteSourceFile(fileData, dimens);
    } else {
      return saveToDestinationAndDeleteSourceFile(fileData);
    }
  }

  private FileData saveToDestinationAndDeleteSourceFile(FileData fileData) throws Exception {
    File source = fileData.getFile();

    InputStream inputStream = new BufferedInputStream(new FileInputStream(source));
    File destination = getOutputFile();
    imageUtils.copy(inputStream, destination);

    return FileData.toFileDataDeleteSourceFileIfTransient(fileData, destination, true, fileData.getMimeType());
  }

  private FileData saveImageAndDeleteSourceFile(FileData fileData, int[] dimens) {
    FileData scaled = imageUtils.scaleImage(fileData, getOutputFile(), dimens);

    FileData.deleteSourceFile(fileData);

    return scaled;
  }

  // unused because MediaScannerConnection.scanFile causes memory leak
  private void sendToMediaScanner(FileData fileDataToScan) {
    File file = fileDataToScan.getFile();
    if (file.exists()) {
      String[] mimeTypes = {fileDataToScan.getMimeType()};
      String[] files = {fileDataToScan.getFile().getAbsolutePath()};

      MediaScannerConnection.scanFile(targetUi.getContext(), files, mimeTypes, null);
    }
  }

  private void sendToMediaScannerIntent(FileData fileDataToScan) {
    File file = fileDataToScan.getFile();
    if (file.exists()) {
      Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
      Context context = targetUi.getContext();
      Uri contentUri = Uri.fromFile(file);
      mediaScanIntent.setData(contentUri);

      context.sendBroadcast(mediaScanIntent);
    }
  }

  private File getOutputFile() {
    String fileName = fileData.getFilename();
    String extension = imageUtils.getFileExtension(fileName);

    return imageUtils.getOutputFile(SAVED_FILE_PREFIX, extension);
  }

}
