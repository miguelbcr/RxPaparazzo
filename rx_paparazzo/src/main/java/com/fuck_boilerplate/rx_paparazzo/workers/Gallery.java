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

import android.Manifest;
import android.app.Activity;

import com.fuck_boilerplate.rx_paparazzo.entities.Response;
import com.fuck_boilerplate.rx_paparazzo.entities.TargetUi;
import com.fuck_boilerplate.rx_paparazzo.interactors.CropImage;
import com.fuck_boilerplate.rx_paparazzo.interactors.GrantPermissions;
import com.fuck_boilerplate.rx_paparazzo.interactors.PickImage;
import com.fuck_boilerplate.rx_paparazzo.interactors.PickImages;
import com.fuck_boilerplate.rx_paparazzo.interactors.SaveImage;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;

public final class Gallery extends Worker {
    private final GrantPermissions grantPermissions;
    private final PickImages pickImages;
    private final PickImage pickImage;
    private final CropImage cropImage;
    private final SaveImage saveImage;
    private final TargetUi targetUi;

    @Inject public Gallery(GrantPermissions grantPermissions, PickImages pickImages, PickImage pickImage, CropImage cropImage, SaveImage saveImage, TargetUi targetUi) {
        super(targetUi);
        this.grantPermissions = grantPermissions;
        this.pickImages = pickImages;
        this.pickImage = pickImage;
        this.cropImage = cropImage;
        this.saveImage = saveImage;
        this.targetUi = targetUi;
    }

    public <T> Observable<Response<T, String>> pickImage() {
        return grantPermissions.with(permissions()).react()
                .flatMap(granted -> pickImage.react())
                .flatMap(uri -> cropImage.with(uri).react())
                .flatMap(uri -> saveImage.with(uri).react())
                .map(path -> new Response<>((T) targetUi.ui(), path, Activity.RESULT_OK))
                .compose(applyOnError());
    }

    public <T> Observable<Response<T, List<String>>> pickImages() {
        return grantPermissions.with(permissions()).react()
                .flatMap(granted -> pickImages.react())
                .flatMapIterable(uris -> uris)
                .concatMap(uri -> cropImage.with(uri).react())
                .concatMap(uri -> saveImage.with(uri).react())
                .toList()
                .map(paths -> new Response<>((T) targetUi.ui(), paths, Activity.RESULT_OK))
                .compose(applyOnError());
    }

    private String[] permissions() {
        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        return permissions;
    }

}
