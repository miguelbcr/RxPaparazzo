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

package com.refineriaweb.rx_paparazzo.library.workers;

import com.refineriaweb.rx_paparazzo.library.entities.Response;
import com.refineriaweb.rx_paparazzo.library.entities.TargetUi;
import com.refineriaweb.rx_paparazzo.library.interactors.CropImage;
import com.refineriaweb.rx_paparazzo.library.interactors.GrantPermissions;
import com.refineriaweb.rx_paparazzo.library.interactors.PickImage;
import com.refineriaweb.rx_paparazzo.library.interactors.PickImages;
import com.refineriaweb.rx_paparazzo.library.interactors.SaveImage;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;

public final class Gallery {
    private final GrantPermissions grantPermissions;
    private final PickImages pickImages;
    private final PickImage pickImage;
    private final CropImage cropImage;
    private final SaveImage saveImage;
    private final TargetUi targetUi;

    @Inject public Gallery(GrantPermissions grantPermissions, PickImages pickImages, PickImage pickImage, CropImage cropImage, SaveImage saveImage, TargetUi targetUi) {
        this.grantPermissions = grantPermissions;
        this.pickImages = pickImages;
        this.pickImage = pickImage;
        this.cropImage = cropImage;
        this.saveImage = saveImage;
        this.targetUi = targetUi;
    }

    public <T> Observable<Response<T, String>> pickImage() {
        return grantPermissions.react()
                .flatMap(granted -> pickImage.react())
                .flatMap(uri -> cropImage.with(uri).react())
                .flatMap(uri -> saveImage.with(uri).react())
                .map(path -> new Response((T) targetUi.ui(), path));
    }

    public <T> Observable<Response<T, List<String>>> pickImages() {
        return grantPermissions.react()
                .flatMap(granted -> pickImages.react())
                .flatMapIterable(uris -> uris)
                .concatMap(uri -> cropImage.with(uri).react())
                .concatMap(uri -> saveImage.with(uri).react())
                .toList()
                .map(paths -> new Response((T) targetUi.ui(), paths));
    }
}
