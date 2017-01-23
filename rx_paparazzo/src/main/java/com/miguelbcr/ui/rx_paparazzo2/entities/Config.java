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

package com.miguelbcr.ui.rx_paparazzo2.entities;

import com.miguelbcr.ui.rx_paparazzo2.entities.size.ScreenSize;
import com.miguelbcr.ui.rx_paparazzo2.entities.size.Size;
import com.yalantis.ucrop.UCrop;

public class Config {
  private UCrop.Options options;

  private Size size;
  private boolean doCrop;
  private boolean failCropIfNotImage;
  private boolean useInternalStorage;

  private String pickMimeType;
  private boolean pickOpenableOnly;
  private boolean sendToMediaScanner;

  public Config() {
    this.size = new ScreenSize();
    this.doCrop = false;
    this.useInternalStorage = false;
    this.pickOpenableOnly = false;
    this.pickMimeType = null;
    this.sendToMediaScanner = false;
    this.failCropIfNotImage = false;
  }

  public Size getSize() {
    return size;
  }

  public void setCrop(UCrop.Options options) {
    this.options = options;
    this.doCrop = true;
  }

  public void setCrop() {
    this.options = new UCrop.Options();
    this.doCrop = true;
  }

  public UCrop.Options getOptions() {
    return options;
  }

  public void setSize(Size size) {
    this.size = size;
  }

  public boolean useInternalStorage() {
    return useInternalStorage;
  }

  public void setUseInternalStorage() {
    this.useInternalStorage = true;
  }

  public void setPickMimeType(String pickMimeType) {
    this.pickMimeType = pickMimeType;
  }

  public String getMimeType(String defaultMimeType) {
    if (this.pickMimeType == null) {
      return defaultMimeType;
    }

    return pickMimeType;
  }

  public void setPickOpenableOnly(boolean pickOpenableOnly) {
    this.pickOpenableOnly = pickOpenableOnly;
  }

  public boolean isPickOpenableOnly() {
    return pickOpenableOnly;
  }

  public void setSendToMediaScanner(boolean sendToMediaScanner) {
    this.sendToMediaScanner = sendToMediaScanner;
  }

  public boolean isSendToMediaScanner() {
    return sendToMediaScanner;
  }

  public void failCropIfNotImage() {
    this.failCropIfNotImage = true;
  }

  public void doNotFailCropIfNotImage() {
    this.failCropIfNotImage = false;
  }

  public boolean isFailCropIfNotImage() {
    return failCropIfNotImage;
  }

  public boolean isDoCrop() {
    return doCrop;
  }

}
