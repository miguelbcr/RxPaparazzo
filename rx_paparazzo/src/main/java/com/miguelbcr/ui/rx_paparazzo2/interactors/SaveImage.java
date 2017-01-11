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
import android.net.Uri;
import com.miguelbcr.ui.rx_paparazzo2.entities.TargetUi;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;
import io.reactivex.functions.Function3;
import java.io.File;

public final class SaveImage extends UseCase<String> {
  private final TargetUi targetUi;
  private final GetPath getPath;
  private final GetDimens getDimens;
  private final ImageUtils imageUtils;
  private Uri uri;

  public SaveImage(TargetUi targetUi, GetPath getPath, GetDimens getDimens, ImageUtils imageUtils) {
    this.targetUi = targetUi;
    this.getPath = getPath;
    this.getDimens = getDimens;
    this.imageUtils = imageUtils;
  }

  public SaveImage with(Uri uri) {
    this.uri = uri;
    return this;
  }

  @Override public Observable<String> react() {
    return getOutputUri().flatMap(new Function<Uri, ObservableSource<String>>() {
      @Override public ObservableSource<String> apply(Uri outputUri) throws Exception {
        return Observable.zip(getPath.with(uri).react(), getPath.with(outputUri).react(),
            getDimens.with(uri).react(), new Function3<String, String, int[], String>() {
              @Override public String apply(String filePath, String filePathOutput, int[] dimens)
                  throws Exception {
                File filePathScaled = imageUtils.scaleImage(filePath, filePathOutput, dimens);

                new File(filePath).delete();

                MediaScannerConnection.scanFile(targetUi.getContext(),
                    new String[] { filePathOutput }, new String[] { "image/*" }, null);

                return filePathOutput;
              }
            });
      }
    });
  }

  private Observable<Uri> getOutputUri() {
    return getPath.with(uri).react().flatMap(new Function<String, ObservableSource<Uri>>() {
      @Override public ObservableSource<Uri> apply(String filepath) throws Exception {
        String extension = imageUtils.getFileExtension(filepath);
        return Observable.just(Uri.fromFile(imageUtils.getOutputFile(extension)));
      }
    });
  }
}
