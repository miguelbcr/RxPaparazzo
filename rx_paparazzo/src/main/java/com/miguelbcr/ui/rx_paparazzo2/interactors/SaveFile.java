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

import android.media.MediaScannerConnection;

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
    ObservableSource<FileData> saved = save(dimens);

    if (config.isSendToMediaScanner()) {
      sendToMediaScanner();
    }

    return saved;
  }

  private ObservableSource<FileData> save(int[] dimens) throws Exception {
    if (imageUtils.isImage(fileData.getFile())) {
      return saveImage(fileData, dimens);
    } else {
      return saveFile(fileData);
    }
  }

  private ObservableSource<FileData> saveFile(FileData fileData) throws Exception {
    File source = fileData.getFile();

    InputStream inputStream = new BufferedInputStream(new FileInputStream(source));
    File destination = getOutputFile();
    imageUtils.copy(inputStream, destination);

    deleteTemporaryFile(fileData);

    FileData copied = new FileData(fileData, destination, fileData.getMimeType());

    return Observable.just(copied);
  }

  private ObservableSource<FileData> saveImage(FileData fileData, int[] dimens) {
    FileData scaled = imageUtils.scaleImage(fileData, getOutputFile(), dimens);

    deleteTemporaryFile(fileData);

    return Observable.just(scaled);
  }

  private void deleteTemporaryFile(FileData fileData) {
    // remove source file - assumes it is a temporary file which is no longer needed
    File source = fileData.getFile();

    // TODO: catch exception?
    source.delete();
  }

  private void sendToMediaScanner() {
    String[] mimeTypes = { fileData.getMimeType() };
    String[] files = { fileData.getFile().getAbsolutePath() };

    MediaScannerConnection.scanFile(targetUi.getContext(), files, mimeTypes, null);
  }

  private File getOutputFile() {
    String fileName = fileData.getFilename();
    String extension = imageUtils.getFileExtension(fileName);

    return imageUtils.getOutputFile(SAVED_FILE_PREFIX, extension);
  }

}
