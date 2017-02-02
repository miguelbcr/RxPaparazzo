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

package com.miguelbcr.ui.rx_paparazzo.interactors;

import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import com.miguelbcr.ui.rx_paparazzo.entities.TargetUi;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import rx.Observable;
import rx.functions.Func1;

public final class PickImages extends UseCase<List<Uri>> {
  private static final int READ_WRITE_PERMISSIONS =
      Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
  private final StartIntent startIntent;
  private final GetPath getPath;
  private TargetUi targetUi;

  public PickImages(StartIntent startIntent, GetPath getPath, TargetUi targetUi) {
    this.startIntent = startIntent;
    this.getPath = getPath;
    this.targetUi = targetUi;
  }

  @Override public Observable<List<Uri>> react() {
    return startIntent.with(getFileChooserIntent()).react().map(new Func1<Intent, List<Uri>>() {
      @Override public List<Uri> call(Intent intent) {
        if (intent == null) {
          return new ArrayList<>();
        }

        intent.addFlags(READ_WRITE_PERMISSIONS);

        Uri pickedUri = intent.getData();
        if (pickedUri != null) {
          grantReadPermissionToUri(targetUi, pickedUri);

          return Arrays.asList(pickedUri);
        } else {
          return getUris(intent);
        }
      }
    });
  }

  private List<Uri> getUris(Intent intent) {
    List<Uri> uris = new ArrayList<>();
    ClipData clipData = intent.getClipData();

    if (clipData != null) {
      for (int i = 0; i < clipData.getItemCount(); i++) {
        ClipData.Item item = clipData.getItemAt(i);
        Uri uri = item.getUri();

        grantReadPermissionToUri(targetUi, uri);

        uris.add(uri);
      }
    }

    return uris;
  }

  private Intent getFileChooserIntent() {
    Intent intent = new Intent();
    intent.setType("image/*");
    intent.setAction(Intent.ACTION_GET_CONTENT);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
      intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
    }

    return intent;
  }

  private void grantReadPermissionToUri(TargetUi targetUi, Uri uri) {
    String uiPackageName = targetUi.getContext().getPackageName();

    targetUi.getContext()
        .grantUriPermission(uiPackageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
  }
}
