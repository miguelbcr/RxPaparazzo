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

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import java.io.File;
import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx_activity_result.OnPreResult;

public final class PickImage extends UseCase<Uri> {
  private final StartIntent startIntent;
  private final GetPath getPath;

  public PickImage(StartIntent startIntent, GetPath getPath) {
    this.startIntent = startIntent;
    this.getPath = getPath;
  }

  @Override public Observable<Uri> react() {
    return startIntent.with(getFileChooserIntent(), getOnPreResultProcessing())
        .react()
        .map(new Func1<Intent, Uri>() {
          @Override public Uri call(Intent intent) {
            return intent.getData();
          }
        });
  }

  private Intent getFileChooserIntent() {
    Intent intent = new Intent();
    intent.setType("image/*");
    intent.setAction(Intent.ACTION_GET_CONTENT);

    return intent;
  }

  private OnPreResult getOnPreResultProcessing() {
    return new OnPreResult() {
      @Override
      public Observable<String> response(int responseCode, @Nullable final Intent intent) {
        if (responseCode == Activity.RESULT_OK) {
          return getPath.with(intent.getData())
              .react()
              .subscribeOn(Schedulers.io())
              .map(new Func1<String, String>() {
                @Override public String call(String filePath) {
                  intent.setData(Uri.fromFile(new File(filePath)));
                  return filePath;
                }
              });
        } else {
          return Observable.just("");
        }
      }
    };
  }
}
