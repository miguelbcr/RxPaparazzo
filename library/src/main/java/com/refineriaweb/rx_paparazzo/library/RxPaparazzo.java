/*
 * Copyright 2016 Refinería Web
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

package com.refineriaweb.rx_paparazzo.library;

import android.app.Activity;
import android.support.v4.app.Fragment;

import com.refineriaweb.rx_paparazzo.library.entities.Config;
import com.refineriaweb.rx_paparazzo.library.entities.Response;
import com.refineriaweb.rx_paparazzo.library.entities.Size;
import com.refineriaweb.rx_paparazzo.library.internal.di.ApplicationComponent;
import com.refineriaweb.rx_paparazzo.library.internal.di.ApplicationModule;
import com.refineriaweb.rx_paparazzo.library.internal.di.DaggerApplicationComponent;
import com.yalantis.ucrop.UCrop;

import java.util.List;

import rx.Observable;

public final class RxPaparazzo {

    public static <T extends Activity> BuilderImage<T> takeImage(T activity) {
        return new BuilderImage<T>(activity);
    }

    /**
     * From API >= 18
     */
    public static <T extends Activity> BuilderImages<T> takeImages(T activity) {
        return new BuilderImages<T>(activity);
    }

    public static <T extends Fragment> BuilderImage<T> takeImage(T fragment) {
        return new BuilderImage<T>(fragment);
    }

    /**
     * From API >= 18
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

    public static class BuilderImage<T> extends Builder<T> {

        public BuilderImage(T ui) {
            super(ui);
        }

        public BuilderImage<T> size(Size size) {
            this.config.setSize(size);
            return this;
        }

        public BuilderImage<T> crop() {
            this.config.setCrop();
            return this;
        }

        public BuilderImage<T> crop(UCrop.Options options) {
            this.config.setCrop(options);
            return this;
        }

        public Observable<Response<T, String>> usingGallery() {
            return applicationComponent.gallery().pickImage();
        }

        public Observable<Response<T, String>> usingCamera() {
            return applicationComponent.camera().takePhoto();
        }
    }

    public static class BuilderImages<T> extends Builder<T> {

        public BuilderImages(T ui) {
            super(ui);
        }

        public BuilderImages<T> size(Size size) {
            this.config.setSize(size);
            return this;
        }

        public BuilderImages<T> crop() {
            this.config.setCrop();
            return this;
        }

        public BuilderImages<T> crop(UCrop.Options options) {
            this.config.setCrop(options);
            return this;
        }

        public Observable<Response<T, List<String>>> usingGallery() {
            return applicationComponent.gallery().pickImages();
        }
    }
}
