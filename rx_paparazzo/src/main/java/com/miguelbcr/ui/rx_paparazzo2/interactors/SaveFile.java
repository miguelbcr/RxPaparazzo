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

import java.io.File;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;
import io.reactivex.functions.Function3;

public final class SaveFile extends UseCase<File> {
  private final GetPath getPath;
  private final GetDimens getDimens;
  private final ImageUtils imageUtils;

  private File destination;
  private Uri uri;
  private boolean maintainFilename;

  public SaveFile(GetPath getPath, GetDimens getDimens, ImageUtils imageUtils) {
    this.getPath = getPath;
    this.getDimens = getDimens;
    this.imageUtils = imageUtils;
  }

  public SaveFile with(Uri uri) {
    this.uri = uri;
    this.destination = null;
    this.maintainFilename = false;

    return this;
  }

  // save as existing file name
  public SaveFile with(Uri uri, boolean maintainFilename) {
    this.uri = uri;
    this.destination = null;
    this.maintainFilename = maintainFilename;

    return this;
  }

  // specify destination filename
  public SaveFile with(Uri uri, File destination) {
    this.uri = uri;
    this.destination = destination;
    this.maintainFilename = false;

    return this;
  }

  @Override public Observable<File> react() {
    return getOutputUri().flatMap(new Function<Uri, ObservableSource<File>>() {
      @Override public ObservableSource<File> apply(Uri outputUri) throws Exception {
        return Observable.zip(getPath.with(uri).react(), getPath.with(outputUri).react(),
            getDimens.with(uri).react(), new Function3<String, String, int[], File>() {
              @Override public File apply(String filePath, String filePathOutput, int[] dimens)
                  throws Exception {
                File output = imageUtils.scaleImage(filePath, filePathOutput, dimens);

//                 TODO: why deleting source file?
                File sourceFile = new File(filePath);
                sourceFile.delete();

//                TODO: detecting if it is an image could be return from scaleImage
//                ScalingResult(File, boolean)
//                String[] mimeTypes = {"image/*"};

//                TODO: why even scan this?
//                String[] mimeTypes = null;
//                MediaScannerConnection.scanFile(targetUi.getContext(),
//                    new String[] { filePathOutput }, mimeTypes, null);

                return output;
              }
            });
      }
    });
  }

  private Observable<Uri> getOutputUri() {
    return getPath.with(uri).react().flatMap(new Function<String, ObservableSource<Uri>>() {
      @Override public ObservableSource<Uri> apply(String filepath) throws Exception {
        File finalDestination;
        if (maintainFilename) {
          String fileName = imageUtils.getFileNameWithoutExtension(filepath);
          File outputDirectory = imageUtils.getOutputDirectory();

          if (fileName != null) {
            finalDestination = new File(filepath, fileName);
          } else {
            String fileExtension = imageUtils.getFileExtension(filepath);
            finalDestination = imageUtils.createNewFile(outputDirectory, fileExtension);
          }
        } else {
          finalDestination = destination;
        }

        return Observable.just(Uri.fromFile(finalDestination));
      }
    });
  }
}
