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

import rx.Observable;
import rx_paparazzo.entities.Config;
import rx_paparazzo.interactors.CropImage;
import rx_paparazzo.interactors.GrantPermissions;
import rx_paparazzo.interactors.SaveImage;
import rx_paparazzo.interactors.TakePhoto;

public final class Camera {
    private final Activity activity;
    private final TakePhoto takePhoto;
    private final CropImage cropImage;
    private final SaveImage saveImage;
    private final GrantPermissions grantPermissions;
    private Config config;

    public Camera(Activity activity) {
        this.activity = activity;
        this.grantPermissions = new GrantPermissions();
        this.takePhoto = new TakePhoto();
        this.cropImage = new CropImage();
        this.saveImage = new SaveImage();
    }

    //Testing purposes
    public Camera(Activity activity, GrantPermissions grantPermissions, TakePhoto takePhoto, CropImage cropImage, SaveImage saveImage) {
        this.activity = activity;
        this.grantPermissions = grantPermissions;
        this.takePhoto = takePhoto;
        this.cropImage = cropImage;
        this.saveImage = saveImage;
    }


    public Camera with(Config config) {
        this.config = config;
        return this;
    }

    public Observable<String> takePhoto() {
        return grantPermissions.with(activity, config.getFolder()).react()
                .flatMap(granted -> takePhoto.with(activity).react())
                .flatMap(uri -> cropImage.with(activity, config, uri).react())
                .flatMap(uri -> saveImage.with(activity, uri).react());
    }
}
