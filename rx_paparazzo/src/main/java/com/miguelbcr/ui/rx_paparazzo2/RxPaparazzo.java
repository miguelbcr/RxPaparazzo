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

package com.miguelbcr.ui.rx_paparazzo2;

import android.app.Activity;
import android.app.Application;
import android.support.v4.app.Fragment;

import com.miguelbcr.ui.rx_paparazzo2.entities.Config;
import com.miguelbcr.ui.rx_paparazzo2.entities.FileData;
import com.miguelbcr.ui.rx_paparazzo2.entities.Response;
import com.miguelbcr.ui.rx_paparazzo2.entities.size.Size;
import com.miguelbcr.ui.rx_paparazzo2.interactors.ImageUtils;
import com.miguelbcr.ui.rx_paparazzo2.internal.di.ApplicationComponent;
import com.miguelbcr.ui.rx_paparazzo2.internal.di.ApplicationModule;
import com.yalantis.ucrop.UCrop;

import java.util.List;

import io.reactivex.Observable;
import rx_activity_result2.RxActivityResult;

public final class RxPaparazzo {
  public static final int RESULT_DENIED_PERMISSION = 2;
  public static final int RESULT_DENIED_PERMISSION_NEVER_ASK = 3;

  public static void register(Application application) {
    RxActivityResult.register(application);
  }

  public static <T extends Activity> SingleSelectionBuilder<T> single(T activity) {
    return new SingleSelectionBuilder<T>(activity);
  }

  public static <T extends Fragment> SingleSelectionBuilder<T> single(T fragment) {
    return new SingleSelectionBuilder<T>(fragment);
  }

  /**
   * Prior to API 18, only one image will be retrieved.
   */
  public static <T extends Activity> MultipleSelectionBuilder<T> multiple(T activity) {
    return new MultipleSelectionBuilder<T>(activity);
  }

  /**
   * Prior to API 18, only one image will be retrieved.
   */
  public static <T extends Fragment> MultipleSelectionBuilder<T> multiple(T fragment) {
    return new MultipleSelectionBuilder<T>(fragment);
  }

  private abstract static class Builder<T, B extends Builder<T, B>> {
    private final Config config;
    private final ApplicationComponent applicationComponent;
    private final B self;

    Builder(T ui) {
      this.self = (B)this;
      this.config = new Config();
      this.applicationComponent = ApplicationComponent.create(new ApplicationModule(config, ui));
    }

    ApplicationComponent getApplicationComponent() {
      return applicationComponent;
    }

    Config getConfig() {
      return config;
    }

    /**
     * Sets this to the value of name attribute of {@link android.support.v4.content.FileProvider} in AndroidManifest.xml
     */
    public B setFileProviderAuthority(String authority) {
      this.config.setFileProviderAuthority(authority);
      return self;
    }

    /**
     * Sets this to the directory which is use in the {@link android.support.v4.content.FileProvider} xml file
     */
    public B setFileProviderDirectory(String authority) {
      this.config.setFileProviderDirectory(authority);
      return self;
    }

    /**
     * Limits the file which can be selected to those obey {@link android.content.Intent}.CATEGORY_OPENABLE.
     */
    public B limitPickerToOpenableFilesOnly() {
      this.config.setPickOpenableOnly(true);
      return self;
    }

    /**
     * Calling it the images will be saved in internal storage, otherwise in public storage
     */
    public B useInternalStorage() {
      this.config.setUseInternalStorage(true);
      return self;
    }

    /**
     * Sets the image dimensions for the retrieved image.
     *
     * @see Size
     */
    public B size(Size size) {
      this.config.setSize(size);
      return self;
    }

    /**
     * Sets the mime type of the picker.
     */
    public B setMimeType(String mimeType) {
      this.config.setPickMimeType(mimeType);
      return self;
    }

    /**
     * Enables cropping of images.
     */
    public B crop() {
      this.config.setCrop();
      return self;
    }

    /**
     * Sets crop option is required as such as configuring the options of the cropping
     * action.
     */
    public <O extends UCrop.Options> B crop(O options) {
      this.config.setCrop(options);
      return self;
    }

    /**
     * Send result to media scanner
     */
    public B sendToMediaScanner() {
      this.config.setSendToMediaScanner(true);
      return self;
    }

    /**
     * Do not send result to media scanner
     */
    public B doNotSendToMediaScanner() {
      this.config.setSendToMediaScanner(false);
      return self;
    }

    /**
     * Use Android Storage Access Framework document picker
     */
    public B useDocumentPicker() {
      this.config.setSendToMediaScanner(false);
      return self;
    }

  }

  /**
   * Use when just one image is required.
   */
  public static class SingleSelectionBuilder<T> extends Builder<T, SingleSelectionBuilder<T>> {

    SingleSelectionBuilder(T ui) {
      super(ui);
    }

    /*
     * Use file picker to retrieve only images.
     */
    public Observable<Response<T, FileData>> usingGallery() {
      Config config = getConfig();
      config.setPickMimeType(ImageUtils.MIME_TYPE_IMAGE_WILDCARD);
      config.setFailCropIfNotImage(true);

      return usingFiles();
    }

    /**
     * Use camera to retrieve the image.
     */
    public Observable<Response<T, FileData>> usingCamera() {
      getConfig().setFailCropIfNotImage(true);

      return getApplicationComponent().camera().takePhoto();
    }

    /**
     * Use file picker to retrieve the files.
     */
    public Observable<Response<T, FileData>> usingFiles() {
      return getApplicationComponent().files().pickFile();
    }
  }

  /**
   * Use when multiple images are required.
   */
  public static class MultipleSelectionBuilder<T> extends Builder<T, MultipleSelectionBuilder<T>> {

    MultipleSelectionBuilder(T ui) {
      super(ui);
    }

    /**
     * Use file picker to retrieve only images.
     */
    public Observable<Response<T, List<FileData>>> usingGallery() {
      getConfig().setPickMimeType(ImageUtils.MIME_TYPE_IMAGE_WILDCARD);

      return usingFiles();
    }

    /**
     * Use file picker to retrieve the files.
     */
    public Observable<Response<T, List<FileData>>> usingFiles() {
      return getApplicationComponent().files().pickFiles();
    }

  }
}
