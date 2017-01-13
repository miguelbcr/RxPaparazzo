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

package com.miguelbcr.ui.rx_paparazzo2.workers;

import android.Manifest;
import android.app.Activity;
import android.net.Uri;

import com.miguelbcr.ui.rx_paparazzo2.entities.Config;
import com.miguelbcr.ui.rx_paparazzo2.entities.FileData;
import com.miguelbcr.ui.rx_paparazzo2.entities.Ignore;
import com.miguelbcr.ui.rx_paparazzo2.entities.Response;
import com.miguelbcr.ui.rx_paparazzo2.entities.TargetUi;
import com.miguelbcr.ui.rx_paparazzo2.interactors.CropImage;
import com.miguelbcr.ui.rx_paparazzo2.interactors.GetPath;
import com.miguelbcr.ui.rx_paparazzo2.interactors.GrantPermissions;
import com.miguelbcr.ui.rx_paparazzo2.interactors.PickImage;
import com.miguelbcr.ui.rx_paparazzo2.interactors.PickImages;
import com.miguelbcr.ui.rx_paparazzo2.interactors.SaveFile;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;

public final class Gallery extends Worker {
  private final GrantPermissions grantPermissions;
  private final PickImages pickImages;
  private final PickImage pickImage;
  private final GetPath getPath;
  private final CropImage cropImage;
  private final SaveFile saveFile;
  private final TargetUi targetUi;
  private final Config config;

  public Gallery(GrantPermissions grantPermissions, PickImages pickImages, PickImage pickImage,
                 GetPath getPath, CropImage cropImage, SaveFile saveFile, TargetUi targetUi, Config config) {
    super(targetUi);
    this.grantPermissions = grantPermissions;
    this.pickImages = pickImages;
    this.pickImage = pickImage;
    this.getPath = getPath;
    this.cropImage = cropImage;
    this.saveFile = saveFile;
    this.targetUi = targetUi;
    this.config = config;
  }

  public <T> Observable<Response<T, FileData>> pickImage() {
    return grantPermissions.with(permissions())
        .react()
        .flatMap(new Function<Ignore, ObservableSource<Uri>>() {
          @Override public ObservableSource<Uri> apply(Ignore ignore) throws Exception {
            return pickImage.react();
          }
        })
            .flatMap(new Function<Uri, ObservableSource<FileData>>() {
              @Override
              public ObservableSource<FileData> apply(final Uri uri) throws Exception {
                return getPath.with(uri).react();
              }
            })
            .flatMap(new Function<FileData, ObservableSource<FileData>>() {
              @Override public ObservableSource<FileData> apply(FileData fileData) throws Exception {
                return handleSavingFile(fileData);
              }
            })
        .map(new Function<FileData, Response<T, FileData>>() {
          @Override public Response<T, FileData> apply(FileData path) throws Exception {
            return new Response<>((T) targetUi.ui(), path, Activity.RESULT_OK);
          }
        })
        .compose(this.<Response<T, FileData>>applyOnError());
  }

  private Observable<FileData> handleSavingFile(final FileData sourceFileData) {
    return cropImage.with(sourceFileData).react()
            .flatMap(new Function<Uri, ObservableSource<FileData>>() {
              @Override
              public ObservableSource<FileData> apply(Uri uri) throws Exception {
                return getPath.with(uri).react().flatMap(new Function<FileData, ObservableSource<FileData>>() {
                  @Override
                  public ObservableSource<FileData> apply(FileData cropped) throws Exception {
                    FileData destination = new FileData(cropped.getFile(), sourceFileData.getFilename());

                    return saveFile.with(destination).react();
                  }
                });
              }
            });
  }

  public <T> Observable<Response<T, List<FileData>>> pickImages() {
    return grantPermissions.with(permissions())
        .react()
        .flatMap(new Function<Ignore, ObservableSource<List<Uri>>>() {
          @Override public ObservableSource<List<Uri>> apply(Ignore ignore) throws Exception {
            return pickImages.react();
          }
        })
        .flatMapIterable(new Function<List<Uri>, Iterable<Uri>>() {
          @Override public Iterable<Uri> apply(List<Uri> uris) throws Exception {
            return uris;
          }
        })
        .concatMap(new Function<Uri, ObservableSource<FileData>>() {
          @Override
          public ObservableSource<FileData> apply(final Uri uri) throws Exception {
            return getPath.with(uri).react();
          }
        })
        .concatMap(new Function<FileData, ObservableSource<FileData>>() {
          @Override
          public ObservableSource<FileData> apply(FileData fileData) throws Exception {
            return handleSavingFile(fileData);
          }
        })
        .toList()
        .toObservable()
        .map(new Function<List<FileData>, Response<T, List<FileData>>>() {
          @Override public Response<T, List<FileData>> apply(List<FileData> paths) throws Exception {
            return new Response<>((T) targetUi.ui(), paths, Activity.RESULT_OK);
          }
        })
        .compose(this.<Response<T, List<FileData>>>applyOnError());
  }

  private String[] permissions() {
    if (config.useInternalStorage()) {
      return new String[] { Manifest.permission.READ_EXTERNAL_STORAGE };
    } else {
      return new String[] {
          Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE
      };
    }
  }
}
