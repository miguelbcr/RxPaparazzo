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
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import com.miguelbcr.ui.rx_paparazzo2.entities.TargetUi;
import io.reactivex.Observable;
import io.reactivex.functions.Function;
import java.io.File;
import java.util.List;

public final class TakePhoto extends UseCase<Uri> {
  private static final int READ_WRITE_PERMISSIONS =
      Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
  private final StartIntent startIntent;
  private final TargetUi targetUi;
  private final ImageUtils imageUtils;

  public TakePhoto(StartIntent startIntent, TargetUi targetUi, ImageUtils imageUtils) {
    this.startIntent = startIntent;
    this.targetUi = targetUi;
    this.imageUtils = imageUtils;
  }

  @Override public Observable<Uri> react() {
    final Uri uri = getUri();
    return startIntent.with(getIntentCamera(uri)).react().map(new Function<Intent, Uri>() {
      @Override public Uri apply(Intent data) throws Exception {
        revokeFileReadWritePermissions(uri);
        return uri;
      }
    });
  }

  private Uri getUri() {
    Context context = targetUi.getContext();
    File file = imageUtils.getPrivateFile(Constants.SHOOT_APPEND);
    String authority = context.getPackageName() + "." + Constants.FILE_PROVIDER;
    return FileProvider.getUriForFile(context, authority, file);
  }

  private Intent getIntentCamera(Uri uri) {
    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    intent.addFlags(READ_WRITE_PERMISSIONS);
    intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
    grantFileReadWritePermissions(intent, uri);
    return intent;
  }

  /**
   * Workaround for Android bug.<br/>
   * See https://code.google.com/p/android/issues/detail?id=76683 <br/>
   * See http://stackoverflow.com/questions/18249007/how-to-use-support-fileprovider-for-sharing-content-to-other-apps
   */
  private void grantFileReadWritePermissions(Intent intent, Uri uri) {
    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
      List<ResolveInfo> resInfoList = targetUi.getContext()
          .getPackageManager()
          .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
      for (ResolveInfo resolveInfo : resInfoList) {
        String packageName = resolveInfo.activityInfo.packageName;
        targetUi.getContext().grantUriPermission(packageName, uri, READ_WRITE_PERMISSIONS);
      }
    }
  }

  private void revokeFileReadWritePermissions(Uri uri) {
    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
      targetUi.getContext().revokeUriPermission(uri, READ_WRITE_PERMISSIONS);
    }
  }
}
