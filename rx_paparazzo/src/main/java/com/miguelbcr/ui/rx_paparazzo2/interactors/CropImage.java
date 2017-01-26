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

import android.content.Intent;
import android.net.Uri;

import com.miguelbcr.ui.rx_paparazzo2.entities.Config;
import com.miguelbcr.ui.rx_paparazzo2.entities.FileData;
import com.miguelbcr.ui.rx_paparazzo2.entities.Options;
import com.miguelbcr.ui.rx_paparazzo2.entities.TargetUi;
import com.yalantis.ucrop.UCrop;

import java.io.File;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;

public final class CropImage extends UseCase<FileData> {
  private static final String CROP_APPEND = "CROPPED-";

  private final Config config;
  private final GetPath getPath;
  private final StartIntent startIntent;
  private final TargetUi targetUi;
  private final ImageUtils imageUtils;
  private FileData fileData;

  public CropImage(TargetUi targetUi, Config config, GetPath getPath, StartIntent startIntent, ImageUtils imageUtils) {
    this.targetUi = targetUi;
    this.config = config;
    this.getPath = getPath;
    this.startIntent = startIntent;
    this.imageUtils = imageUtils;
  }

  public CropImage with(FileData fileData) {
    this.fileData = fileData;
    return this;
  }

  @Override
  public Observable<FileData> react() {
    if (config.isDoCrop()) {

      // bypass cropping if not image
      if (!isImage()) {
        if (config.isFailCropIfNotImage()) {
          throw new IllegalArgumentException("Expected an image file, cannot perform image crop");
        } else {
          return Observable.just(fileData);
        }
      }

      return cropImage();
    }

    return Observable.just(fileData);
  }

  private Observable<FileData> cropImage() {
    Observable<Intent> intent = Observable.just(getIntent());

    return intent.flatMap(new Function<Intent, ObservableSource<FileData>>() {
      @Override
      public ObservableSource<FileData> apply(Intent intent) throws Exception {
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        return startIntent.with(intent).react()
          .map(new Function<Intent, Uri>() {
            @Override
            public Uri apply(Intent intentResult) throws Exception {
              return UCrop.getOutput(intentResult);
            }
          })
          .flatMap(new Function<Uri, ObservableSource<FileData>>() {
            @Override
            public ObservableSource<FileData> apply(Uri uri) throws Exception {
              return getPath.with(uri).react();
            }
          })
          .flatMap(new Function<FileData, ObservableSource<FileData>>() {
            @Override
            public ObservableSource<FileData> apply(FileData cropped) throws Exception {
              FileData result = FileData.toFileDataDeleteSourceFileIfTransient(fileData, cropped.getFile(), true, cropped.getMimeType());

              return Observable.just(result);
            }
          });
      }
    });
  }

  private boolean isImage() {
    File file = fileData.getFile();

    return imageUtils.isImage(file);
  }

  private Intent getIntent() {
      Uri inputUri = getInputUri();
      Uri outputUri = getOutputUriCrop();

      UCrop.Options options = config.getOptions();
      if (options == null) {
        return UCrop.of(inputUri, outputUri).getIntent(targetUi.getContext());
      }

      if (options instanceof Options) {
        return getIntentWithOptions((Options) options, outputUri);
      } else {
        return UCrop.of(inputUri, outputUri)
            .withOptions(config.getOptions())
            .getIntent(targetUi.getContext());
      }
  }

  private Intent getIntentWithOptions(Options options, Uri outputUri) {
    Uri uri = Uri.fromFile(fileData.getFile());

    UCrop uCrop = UCrop.of(uri, outputUri).withOptions(options);
    if (options.getX() != 0) {
      uCrop.withAspectRatio(options.getX(), options.getY());
    }
    if (options.getWidth() != 0) {
      uCrop.withMaxResultSize(options.getWidth(), options.getHeight());
    }

    return uCrop.getIntent(targetUi.getContext());
  }

  private Uri getInputUri() {
    return Uri.fromFile(fileData.getFile());
  }

  private Uri getOutputUriCrop() {
    String destination = fileData.getFile().getAbsolutePath();
    String extension = imageUtils.getFileExtension(destination, ImageUtils.JPG_FILE_EXTENSION);
    String directory = config.getFileProviderDirectory();
    String outputFilename = imageUtils.createDefaultFilename(CROP_APPEND, extension);
    File outputFile = imageUtils.getPrivateFile(directory, outputFilename);

    return Uri.fromFile(outputFile);
  }

}
