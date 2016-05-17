/*
 * Copyright 2016 FuckBoilerplate
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

package com.fuck_boilerplate.rx_paparazzo;

import android.app.Activity;
import android.app.Application;
import android.support.v4.app.Fragment;

import com.fuck_boilerplate.rx_paparazzo.entities.Config;
import com.fuck_boilerplate.rx_paparazzo.entities.Response;
import com.fuck_boilerplate.rx_paparazzo.entities.Size;
import com.fuck_boilerplate.rx_paparazzo.internal.di.ApplicationComponent;
import com.fuck_boilerplate.rx_paparazzo.internal.di.ApplicationModule;
import com.fuck_boilerplate.rx_paparazzo.internal.di.DaggerApplicationComponent;
import com.yalantis.ucrop.UCrop;

import java.util.List;

import rx.Observable;
import rx_activity_result.RxActivityResult;

public final class RxPaparazzo {
    public static final int RESULT_DENIED_PERMISSION = 2;

    public static void register(Application application) {
        RxActivityResult.register(application);
    }

    public static <T extends Activity> BuilderImage<T> takeImage(T activity) {
        return new BuilderImage<T>(activity);
    }

    /**
     * Prior to API 18, only one image will be retrieved.
     */
    public static <T extends Activity> BuilderImages<T> takeImages(T activity) {
        return new BuilderImages<T>(activity);
    }

    public static <T extends Fragment> BuilderImage<T> takeImage(T fragment) {
        return new BuilderImage<T>(fragment);
    }

    /**
     * Prior to API 18, only one image will be retrieved.
     */
    public static <T extends Fragment> BuilderImages<T> takeImages(T fragment) {
        return new BuilderImages<T>(fragment);
    }

    private abstract static class Builder<T> {
        protected final Config config;
        protected final ApplicationComponent applicationComponent;

        public Builder(T ui) {
            this.config = new Config();
            this.applicationComponent = DaggerApplicationComponent.builder()
                    .applicationModule(new ApplicationModule(config, ui))
                    .build();
        }
    }

    /**
     * Call it when just one image is required to retrieve.
     */
    public static class BuilderImage<T> extends Builder<T> {

        public BuilderImage(T ui) {
            super(ui);
        }

        /**
         * Sets the size for the retrieved image.
         * @param size
         * @see Size
         */
        public BuilderImage<T> size(Size size) {
            this.config.setSize(size);
            return this;
        }

        /**
         * Call it when crop option is required.
         */
        public BuilderImage<T> crop() {
            this.config.setCrop();
            return this;
        }

        /**
         * Call it when crop option is required as such as configuring the options of the cropping action.
         */
        public BuilderImage<T> crop(UCrop.Options options) {
            this.config.setCrop(options);
            return this;
        }

        /**
         * Use gallery to retrieve the image.
         */
        public Observable<Response<T, String>> usingGallery() {
            return applicationComponent.gallery().pickImage();
        }

        /**
         * Use camera to retrieve the image.
         */
        public Observable<Response<T, String>> usingCamera() {
            return applicationComponent.camera().takePhoto();
        }
    }

    /**
     * Call it when multiple images are required to retrieve from gallery.
     */
    public static class BuilderImages<T> extends Builder<T> {

        public BuilderImages(T ui) {
            super(ui);
        }

        /**
         * Sets the size for the retrieved image.
         * @param size
         * @see Size
         */
        public BuilderImages<T> size(Size size) {
            this.config.setSize(size);
            return this;
        }

        /**
         * Call it when crop option is required.
         */
        public BuilderImages<T> crop() {
            this.config.setCrop();
            return this;
        }

        /**
         * Call it when crop option is required as such as configuring the options of the cropping action.
         */
        public BuilderImages<T> crop(UCrop.Options options) {
            this.config.setCrop(options);
            return this;
        }

        /**
         * Call it when crop option is required.
         */
        public Observable<Response<T, List<String>>> usingGallery() {
            return applicationComponent.gallery().pickImages();
        }
    }
}
