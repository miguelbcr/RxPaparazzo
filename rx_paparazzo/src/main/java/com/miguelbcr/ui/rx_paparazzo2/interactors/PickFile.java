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

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.Nullable;

import com.miguelbcr.ui.rx_paparazzo2.entities.Config;
import com.miguelbcr.ui.rx_paparazzo2.entities.TargetUi;

import io.reactivex.Observable;
import io.reactivex.functions.Function;
import rx_activity_result2.OnPreResult;

public class PickFile extends UseCase<Uri> {

  public static final String DEFAULT_MIME_TYPE = "*/*";

  private final Config config;
  private final StartIntent startIntent;
  private final TargetUi targetUi;

  public PickFile(TargetUi targetUi, Config config, StartIntent startIntent) {
    this.targetUi = targetUi;
    this.config = config;
    this.startIntent = startIntent;
  }

  public String getDefaultMimeType() {
    return DEFAULT_MIME_TYPE;
  }

  @Override
  public Observable<Uri> react() {
    return startIntent.with(getFileChooserIntent(), getOnPreResultProcessing())
            .react()
            .map(new Function<Intent, Uri>() {
              @Override public Uri apply(Intent intent) throws Exception {
                return intent.getData();
              }
            });
  }

  private Intent getFileChooserIntent() {
    Intent intent = new Intent();
    if (config.getMultipleMimeTypes() == null) {
      intent.setType(config.getMimeType(getDefaultMimeType()));
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      intent.setType(DEFAULT_MIME_TYPE);
      intent.putExtra(Intent.EXTRA_MIME_TYPES, config.getMultipleMimeTypes());
    }

    if (config.isUseDocumentPicker() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
      intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
    } else {
      intent.setAction(Intent.ACTION_GET_CONTENT);
    }

    if (config.isPickOpenableOnly()) {
      intent.addCategory(Intent.CATEGORY_OPENABLE);
    }

    return intent;
  }

  private OnPreResult getOnPreResultProcessing() {
    return new OnPreResult() {
      @Override
      public Observable<Uri> response(int requestCode, int resultCode, @Nullable final Intent intent) {
        if (resultCode == Activity.RESULT_OK && intent != null && intent.getData() != null) {

          Uri pickedUri = intent.getData();
          PermissionUtil.grantReadPermissionToUri(targetUi, pickedUri);

          return Observable.just(intent.getData());
        } else {
          return Observable.empty();
        }
      }
    };
  }

}
