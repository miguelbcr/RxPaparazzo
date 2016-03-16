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

import com.refineriaweb.rx_paparazzo.library.entities.Config;
import com.refineriaweb.rx_paparazzo.library.entities.Folder;
import com.refineriaweb.rx_paparazzo.library.entities.Size;
import com.refineriaweb.rx_paparazzo.library.entities.Style;
import com.refineriaweb.rx_paparazzo.library.workers.Camera;
import com.refineriaweb.rx_paparazzo.library.workers.Gallery;

import java.util.List;

import rx.Observable;

public final class RxPaparazzo {

    public static BuilderImage takeImage(Activity activity) {
        return new BuilderImage(activity);
    }

    public static BuilderImages takeImages(Activity activity) {
        return new BuilderImages(activity);
    }

    private abstract static class Builder {
        protected final Config config;
        protected final Camera camera;
        protected final Gallery gallery;

        public Builder(Activity activity) {
            this.camera = new Camera(activity);
            this.gallery = new Gallery(activity);
            this.config = new Config();
        }
    }

    public static class BuilderImage extends Builder {

        public BuilderImage(Activity activity) {
            super(activity);
        }

        public BuilderImage output(Folder folder) {
            this.config.setFolder(folder);
            return this;
        }

        public BuilderImage size(Size size) {
            this.config.setSize(size);
            return this;
        }

        public BuilderImage crop(Style style) {
            this.config.setCrop(style);
            return this;
        }

        public Observable<String> usingGallery() {
            return gallery.with(config).pickImage();
        }

        public Observable<String> usingCamera() {
            return camera.with(config).takePhoto();
        }
    }

    public static class BuilderImages extends Builder {

        public BuilderImages(Activity activity) {
            super(activity);
        }

        public BuilderImages output(Folder folder) {
            this.config.setFolder(folder);
            return this;
        }

        public BuilderImages size(Size size) {
            this.config.setSize(size);
            return this;
        }

        public BuilderImages crop(Style style) {
            this.config.setCrop(style);
            return this;
        }

        public Observable<List<String>> usingGallery() {
            return gallery.pickImages();
        }
    }
}
