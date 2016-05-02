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

package com.fuck_boilerplate.rx_paparazzo.workers;

import android.app.Activity;

import com.fuck_boilerplate.rx_paparazzo.entities.Response;
import com.fuck_boilerplate.rx_paparazzo.entities.TargetUi;
import com.fuck_boilerplate.rx_paparazzo.interactors.CropImage;
import com.fuck_boilerplate.rx_paparazzo.interactors.GrantPermissions;
import com.fuck_boilerplate.rx_paparazzo.interactors.SaveImage;
import com.fuck_boilerplate.rx_paparazzo.interactors.TakePhoto;

import javax.inject.Inject;

import rx.Observable;

public final class Camera extends Worker {
    private final TakePhoto takePhoto;
    private final CropImage cropImage;
    private final SaveImage saveImage;
    private final GrantPermissions grantPermissions;
    private final TargetUi targetUi;

    @Inject public Camera(TakePhoto takePhoto, CropImage cropImage, SaveImage saveImage, GrantPermissions grantPermissions, TargetUi targetUi) {
        super(targetUi);
        this.takePhoto = takePhoto;
        this.cropImage = cropImage;
        this.saveImage = saveImage;
        this.grantPermissions = grantPermissions;
        this.targetUi = targetUi;
    }

    public <T> Observable<Response<T, String>> takePhoto() {
        return grantPermissions.react()
                .flatMap(granted -> takePhoto.react())
                .flatMap(uri -> cropImage.with(uri).react())
                .flatMap(uri -> saveImage.with(uri).react())
                .map(path -> new Response<>((T) targetUi.ui(), path, Activity.RESULT_OK))
                .compose(applyOnError());
    }
}
