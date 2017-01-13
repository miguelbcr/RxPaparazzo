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

import com.miguelbcr.ui.rx_paparazzo2.entities.FileData;

import java.io.File;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;

public final class SaveFile extends UseCase<FileData> {
  private final GetDimens getDimens;
  private final ImageUtils imageUtils;

  private FileData fileData;

  public SaveFile(GetDimens getDimens, ImageUtils imageUtils) {
    this.getDimens = getDimens;
    this.imageUtils = imageUtils;
  }

  public SaveFile with(FileData fileData) {
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

//                TODO: detecting if it is an image could be return from scaleImage
//                ScalingResult(File, boolean)
//                String[] mimeTypes = {"image/*"};

//                TODO: why even scan this?
//                String[] mimeTypes = null;
//                MediaScannerConnection.scanFile(targetUi.getContext(),
//                    new String[] { filePathOutput }, mimeTypes, null);

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
