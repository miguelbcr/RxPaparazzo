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
import com.miguelbcr.ui.rx_paparazzo2.entities.Ignore;
import com.miguelbcr.ui.rx_paparazzo2.entities.Response;
import com.miguelbcr.ui.rx_paparazzo2.entities.TargetUi;
import com.miguelbcr.ui.rx_paparazzo2.interactors.CropImage;
import com.miguelbcr.ui.rx_paparazzo2.interactors.GrantPermissions;
import com.miguelbcr.ui.rx_paparazzo2.interactors.PickImage;
import com.miguelbcr.ui.rx_paparazzo2.interactors.PickImages;
import com.miguelbcr.ui.rx_paparazzo2.interactors.SaveImage;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;
import java.util.List;

public final class Gallery extends Worker {
  private final GrantPermissions grantPermissions;
  private final PickImages pickImages;
  private final PickImage pickImage;
  private final CropImage cropImage;
  private final SaveImage saveImage;
  private final TargetUi targetUi;
  private final Config config;

  public Gallery(GrantPermissions grantPermissions, PickImages pickImages, PickImage pickImage,
      CropImage cropImage, SaveImage saveImage, TargetUi targetUi, Config config) {
    super(targetUi);
    this.grantPermissions = grantPermissions;
    this.pickImages = pickImages;
    this.pickImage = pickImage;
    this.cropImage = cropImage;
    this.saveImage = saveImage;
    this.targetUi = targetUi;
    this.config = config;
  }

  public <T> Observable<Response<T, String>> pickImage() {
    return grantPermissions.with(permissions())
        .react()
        .flatMap(new Function<Ignore, ObservableSource<Uri>>() {
          @Override public ObservableSource<Uri> apply(Ignore ignore) throws Exception {
            return pickImage.react();
          }
        })
        .flatMap(new Function<Uri, ObservableSource<Uri>>() {
          @Override public ObservableSource<Uri> apply(Uri uri) throws Exception {
            return cropImage.with(uri).react();
          }
        })
        .flatMap(new Function<Uri, ObservableSource<String>>() {
          @Override public ObservableSource<String> apply(Uri uri) throws Exception {
            return saveImage.with(uri).react();
          }
        })
        .map(new Function<String, Response<T, String>>() {
          @Override public Response<T, String> apply(String path) throws Exception {
            return new Response<>((T) targetUi.ui(), path, Activity.RESULT_OK);
          }
        })
        .compose(this.<Response<T, String>>applyOnError());
  }

  public <T> Observable<Response<T, List<String>>> pickImages() {
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
        .concatMap(new Function<Uri, ObservableSource<Uri>>() {
          @Override public ObservableSource<Uri> apply(Uri uri) throws Exception {
            return cropImage.with(uri).react();
          }
        })
        .concatMap(new Function<Uri, ObservableSource<String>>() {
          @Override public ObservableSource<String> apply(Uri uri) throws Exception {
            return saveImage.with(uri).react();
          }
        })
        .toList()
        .toObservable()
        .map(new Function<List<String>, Response<T, List<String>>>() {
          @Override public Response<T, List<String>> apply(List<String> paths) throws Exception {
            return new Response<>((T) targetUi.ui(), paths, Activity.RESULT_OK);
          }
        })
        .compose(this.<Response<T, List<String>>>applyOnError());
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
