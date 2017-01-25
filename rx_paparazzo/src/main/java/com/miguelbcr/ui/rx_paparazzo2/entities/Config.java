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

import android.content.Context;

import com.miguelbcr.ui.rx_paparazzo2.entities.size.ScreenSize;
import com.miguelbcr.ui.rx_paparazzo2.entities.size.Size;
import com.yalantis.ucrop.UCrop;

public class Config {

  private static final String DEFAULT_FILE_PROVIDER_DIRECTORY = "RxPaparazzo";
  private static final String DEFAULT_FILE_PROVIDER = "file_provider";

  private UCrop.Options options;

  private Size size;
  private boolean doCrop;
  private boolean failCropIfNotImage;
  private boolean useInternalStorage;

  private String pickMimeType;
  private boolean pickOpenableOnly;
  private boolean sendToMediaScanner;

  private String fileProviderAuthority;
  private String fileProviderDirectory;

  public Config() {
    this.size = new ScreenSize();
    this.doCrop = false;
    this.useInternalStorage = false;
    this.pickOpenableOnly = false;
    this.pickMimeType = null;
    this.sendToMediaScanner = false;
    this.failCropIfNotImage = false;
    this.fileProviderAuthority = null;
    this.fileProviderDirectory = null;
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

  public boolean isUseInternalStorage() {
    return useInternalStorage;
  }

  public void setUseInternalStorage(boolean useInternalStorage) {
    this.useInternalStorage = useInternalStorage;
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

  public void setFailCropIfNotImage(boolean failCropIfNotImage) {
    this.failCropIfNotImage = failCropIfNotImage;
  }

  public boolean isFailCropIfNotImage() {
    return failCropIfNotImage;
  }

  public boolean isDoCrop() {
    return doCrop;
  }

  public void setFileProviderAuthority(String fileProviderAuthority) {
    this.fileProviderAuthority = fileProviderAuthority;
  }

  public String getFileProviderAuthority(Context context) {
    if (fileProviderAuthority == null) {
      return context.getPackageName() + "." + DEFAULT_FILE_PROVIDER;
    }

    return fileProviderAuthority;
  }

  public void setFileProviderDirectory(String fileProviderDirectory) {
    this.fileProviderDirectory = fileProviderDirectory;
  }

  public String getFileProviderDirectory() {
    if (fileProviderDirectory == null) {
      return DEFAULT_FILE_PROVIDER_DIRECTORY;
    }

    return fileProviderDirectory;
  }

}
