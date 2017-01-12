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

import com.miguelbcr.ui.rx_paparazzo2.entities.FileData;
import com.miguelbcr.ui.rx_paparazzo2.entities.TargetUi;

import java.io.File;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;

public final class SaveImage extends UseCase<FileData> {
  private final TargetUi targetUi;
  private final GetDimens getDimens;
  private final ImageUtils imageUtils;
  private FileData fileData;

  public SaveImage(TargetUi targetUi, GetDimens getDimens, ImageUtils imageUtils) {
    this.targetUi = targetUi;
    this.getDimens = getDimens;
    this.imageUtils = imageUtils;
  }

  public SaveImage with(FileData fileData) {
    this.fileData = fileData;
    return this;
  }

  @Override public Observable<FileData> react() {
    return getDimens.with(fileData).react().flatMap(new Function<int[], ObservableSource<FileData>>() {
      @Override
      public ObservableSource<FileData> apply(int[] dimens) throws Exception {
        File output = getOutputFile();
        File scaled = imageUtils.scaleImage(fileData.getFile(), output, dimens);

//      TODO: why deleting source file?
        fileData.getFile().delete();

        MediaScannerConnection.scanFile(targetUi.getContext(),
                new String[] { output.getAbsolutePath() }, new String[] { "image/*" }, null);

        return Observable.just(new FileData(scaled, fileData.getFilename()));
      }
    });
  }

  private File getOutputFile() {
    String fileName = fileData.getFilename();
    String extension = imageUtils.getFileExtension(fileName);

    return imageUtils.getOutputFile(extension);
  }
}
