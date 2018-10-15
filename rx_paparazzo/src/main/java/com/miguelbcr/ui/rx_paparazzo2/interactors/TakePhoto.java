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
import android.hardware.Camera;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;

import com.miguelbcr.ui.rx_paparazzo2.entities.CameraMode;
import com.miguelbcr.ui.rx_paparazzo2.entities.Config;
import com.miguelbcr.ui.rx_paparazzo2.entities.FileData;
import com.miguelbcr.ui.rx_paparazzo2.entities.TargetUi;

import java.io.File;
import java.io.FileNotFoundException;

import io.reactivex.Observable;
import io.reactivex.functions.Function;

public final class TakePhoto extends UseCase<FileData> {
  private static final String PHOTO_FILE_PREFIX = "PHOTO-";

  private final Config config;
  private final StartIntent startIntent;
  private final TargetUi targetUi;
  private final ImageUtils imageUtils;

  public TakePhoto(Config config, StartIntent startIntent, TargetUi targetUi, ImageUtils imageUtils) {
    this.config = config;
    this.startIntent = startIntent;
    this.targetUi = targetUi;
    this.imageUtils = imageUtils;
  }

  @Override
  public Observable<FileData> react() {
    final File file = getOutputFile();
    Uri uri = getUri(file);

    return startIntent.with(getIntentCamera(uri))
            .react()
            .map(revokeFileReadWritePermissions(targetUi, uri))
            .map(new Function<Uri, FileData>() {
              @Override
              public FileData apply(Uri uri) throws Exception {
                if (!file.exists()) {
                  throw new FileNotFoundException(String.format("Camera file not saved", file.getAbsolutePath()));
                }

                return new FileData(file, true, file.getName(), ImageUtils.MIME_TYPE_JPEG);
              }
            });
  }

  private Function<Intent, Uri> revokeFileReadWritePermissions(final TargetUi targetUi, final Uri uri) {
    return new Function<Intent, Uri>() {
      @Override public Uri apply(Intent data) {
        PermissionUtil.revokeFileReadWritePermissions(targetUi, uri);

        return uri;
      }
    };
  }

  private Uri getUri(File file) {
    Context context = targetUi.getContext();
    String authority = config.getFileProviderAuthority(context);

    return FileProvider.getUriForFile(context, authority, file);
  }

  private File getOutputFile() {
    String filename = imageUtils.createTimestampedFilename(PHOTO_FILE_PREFIX, ImageUtils.JPG_FILE_EXTENSION);
    String directory = config.getFileProviderDirectory();
    return imageUtils.getPrivateFile(directory, filename);
  }

  private Intent getIntentCamera(Uri uri) {
    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);

    if (CameraMode.FRONT.equals(config.getCameraMode())) {
      intent.putExtra("android.intent.extras.CAMERA_FACING", android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT);
      intent.putExtra("android.intent.extras.LENS_FACING_FRONT", 1);
      intent.putExtra("android.intent.extra.USE_FRONT_CAMERA", true);
    } else {
      intent.putExtra("android.intent.extras.CAMERA_FACING", Camera.CameraInfo.CAMERA_FACING_BACK);
      intent.putExtra("android.intent.extras.LENS_FACING_FRONT", 0);
      intent.putExtra("android.intent.extra.USE_FRONT_CAMERA", false);
    }

    return PermissionUtil.requestReadWritePermission(targetUi, intent, uri);
  }

}
