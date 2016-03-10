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

package rx_paparazzo;

import android.app.Activity;

import java.util.List;

import rx.Observable;
import rx_paparazzo.entities.Config;
import rx_paparazzo.entities.Folder;
import rx_paparazzo.entities.Size;
import rx_paparazzo.entities.Style;
import rx_paparazzo.workers.Camera;
import rx_paparazzo.workers.Gallery;

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
            return gallery.pickImage();
        }

        public Observable<String> usingCamera() {
            return camera.takePhoto();
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
