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

package rx_paparazzo.workers;

import android.app.Activity;

import java.util.List;

import rx.Observable;
import rx_paparazzo.entities.Config;
import rx_paparazzo.interactors.CropImage;
import rx_paparazzo.interactors.GrantPermissions;
import rx_paparazzo.interactors.PickImage;
import rx_paparazzo.interactors.PickImages;
import rx_paparazzo.interactors.SaveImage;

public final class Gallery {
    private final Activity activity;
    private final GrantPermissions grantPermissions;
    private final PickImages pickImages;
    private final PickImage pickImage;
    private final CropImage cropImage;
    private final SaveImage saveImage;
    private Config config;

    public Gallery(Activity activity) {
        this.activity = activity;
        this.grantPermissions = new GrantPermissions();
        this.pickImage = new PickImage();
        this.pickImages = new PickImages();
        this.cropImage = new CropImage();
        this.saveImage = new SaveImage();
    }

    //Testing purposes
    public Gallery(Activity activity, GrantPermissions grantPermissions, PickImage pickImage, PickImages pickImages, CropImage cropImage, SaveImage saveImage) {
        this.activity = activity;
        this.grantPermissions = grantPermissions;
        this.pickImage = pickImage;
        this.pickImages = pickImages;
        this.cropImage = cropImage;
        this.saveImage = saveImage;
    }

    public Gallery with(Config config) {
        this.config = config;
        return this;
    }

    public Observable<String> pickImage() {
        return grantPermissions.with(activity, config.getFolder()).react()
                .flatMap(granted -> pickImage.with(activity).react())
                .flatMap(uri -> cropImage.with(activity, config, uri).react())
                .flatMap(uri -> saveImage.with(activity, uri).react());
    }

    public Observable<List<String>> pickImages() {
        return grantPermissions.with(activity, config.getFolder()).react()
                .flatMap(granted -> pickImages.with(activity).react())
                .flatMapIterable(uris -> uris)
                .flatMap(uri -> cropImage.with(activity, config, uri).react())
                .flatMap(uri -> saveImage.with(activity, uri).react())
                .toList();
    }
}
