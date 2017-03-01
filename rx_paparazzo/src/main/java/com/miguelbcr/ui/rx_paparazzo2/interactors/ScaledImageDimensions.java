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

import android.util.DisplayMetrics;

import com.miguelbcr.ui.rx_paparazzo2.entities.Config;
import com.miguelbcr.ui.rx_paparazzo2.entities.FileData;
import com.miguelbcr.ui.rx_paparazzo2.entities.TargetUi;
import com.miguelbcr.ui.rx_paparazzo2.entities.size.CustomMaxSize;
import com.miguelbcr.ui.rx_paparazzo2.entities.size.OriginalSize;
import com.miguelbcr.ui.rx_paparazzo2.entities.size.ScreenSize;

import java.io.File;

import io.reactivex.Observable;

public final class ScaledImageDimensions extends UseCase<Dimensions> {
  private final TargetUi targetUi;
  private final Config config;
  private FileData fileData;

  public ScaledImageDimensions(TargetUi targetUi, Config config) {
    this.targetUi = targetUi;
    this.config = config;
  }

  public ScaledImageDimensions with(FileData fileData) {
    this.fileData = fileData;
    return this;
  }

  @Override public Observable<Dimensions> react() {
    return Observable.just(getDimensions());
  }

  private Dimensions getDimensions() {
    File file = fileData.getFile();
    if (config.getSize() instanceof OriginalSize) {
      return ImageUtils.getImageDimensions(file);
    } else if (config.getSize() instanceof CustomMaxSize) {
      return getCustomDimens((CustomMaxSize) config.getSize(), file);
    } else if (config.getSize() instanceof ScreenSize) {
      return ScaledImageDimensions.this.getScreenDimens();
    } else {
      Dimensions dimens = ScaledImageDimensions.this.getScreenDimens();

      return new Dimensions(dimens.getWidth() / 8, dimens.getHeight() / 8);
    }
  }

  private Dimensions getScreenDimens() {
    DisplayMetrics metrics = targetUi.getContext().getResources().getDisplayMetrics();

    return new Dimensions(metrics.widthPixels, metrics.heightPixels);
  }

  private Dimensions getCustomDimens(CustomMaxSize customMaxSize, File file) {
    int maxSize = customMaxSize.getMaxImageSize();
    Dimensions dimensions = ImageUtils.getImageDimensions(file);

    int maxFileSize = Math.max(dimensions.getWidth(), dimensions.getHeight());
    if (maxFileSize < maxSize) {
      return dimensions;
    }

    float scaleFactor = (float) maxSize / maxFileSize;
    float scaledWidth = dimensions.getWidth() * scaleFactor;
    float scaledHeight = dimensions.getHeight() * scaleFactor;

    return new Dimensions((int)scaledWidth, (int)scaledHeight);
  }
}
