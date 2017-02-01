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

import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import com.miguelbcr.ui.rx_paparazzo2.entities.Config;
import com.miguelbcr.ui.rx_paparazzo2.entities.TargetUi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.functions.Function;

public class PickFiles extends UseCase<List<Uri>> {

  public static final String DEFAULT_MIME_TYPE = "*/*";

  private final TargetUi targetUi;
  private final Config config;
  private final StartIntent startIntent;

  public PickFiles(TargetUi targetUi, Config config, StartIntent startIntent) {
    this.targetUi = targetUi;
    this.config = config;
    this.startIntent = startIntent;
  }

  public String getDefaultMimeType() {
    return DEFAULT_MIME_TYPE;
  }

  @Override public Observable<List<Uri>> react() {
    return startIntent.with(getFileChooserIntent()).react().map(new Function<Intent, List<Uri>>() {
      @Override public List<Uri> apply(Intent intent) throws Exception {
        if (intent == null) {
          return new ArrayList<>();
        }

        intent.addFlags(PermissionUtil.READ_WRITE_PERMISSIONS);

        Uri pickedUri = intent.getData();
        if (pickedUri != null) {
          PermissionUtil.grantReadPermissionToUri(targetUi, pickedUri);

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

        PermissionUtil.grantReadPermissionToUri(targetUi, uri);

        uris.add(uri);
      }
    }

    return uris;
  }

  private Intent getFileChooserIntent() {
    String mimeType = config.getMimeType(getDefaultMimeType());
    Intent intent = new Intent();
    intent.setType(mimeType);

    if (config.isUseDocumentPicker() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
      intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
    } else {
      intent.setAction(Intent.ACTION_GET_CONTENT);
    }

    if (config.isPickOpenableOnly()) {
      intent.addCategory(Intent.CATEGORY_OPENABLE);
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
      intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
    }

    return intent;
  }
}
