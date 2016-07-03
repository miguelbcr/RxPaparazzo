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
import android.net.Uri;

import com.fuck_boilerplate.rx_paparazzo.entities.Config;
import com.fuck_boilerplate.rx_paparazzo.entities.Response;
import com.fuck_boilerplate.rx_paparazzo.entities.TargetUi;
import com.fuck_boilerplate.rx_paparazzo.interactors.CropImage;
import com.fuck_boilerplate.rx_paparazzo.interactors.GrantPermissions;
import com.fuck_boilerplate.rx_paparazzo.interactors.SaveImage;
import com.fuck_boilerplate.rx_paparazzo.interactors.TakePhoto;

import rx.Observable;
import rx.functions.Func1;

public final class Camera extends Worker {
    private final TakePhoto takePhoto;
    private final CropImage cropImage;
    private final SaveImage saveImage;
    private final GrantPermissions grantPermissions;
    private final TargetUi targetUi;
    private final Config config;

    public Camera(TakePhoto takePhoto, CropImage cropImage, SaveImage saveImage, GrantPermissions grantPermissions, TargetUi targetUi, Config config) {
        super(targetUi);
        this.takePhoto = takePhoto;
        this.cropImage = cropImage;
        this.saveImage = saveImage;
        this.grantPermissions = grantPermissions;
        this.targetUi = targetUi;
        this.config = config;
    }

    public <T> Observable<Response<T, String>> takePhoto() {
        return grantPermissions.with(permissions()).react()
                .flatMap(new Func1<Void, Observable<Uri>>() {
                    @Override
                    public Observable<Uri> call(Void granted) {
                        return takePhoto.react();
                    }
                })
                .flatMap(new Func1<Uri, Observable<Uri>>() {
                    @Override
                    public Observable<Uri> call(Uri uri) {
                        return cropImage.with(uri).react();
                    }
                })
                .flatMap(new Func1<Uri, Observable<String>>() {
                    @Override
                    public Observable<String> call(Uri uri) {
                        return saveImage.with(uri).react();
                    }
                })
                .map(new Func1<String, Response<T, String>>() {
                    @Override
                    public Response<T, String> call(String path) {
                        return new Response<>((T) targetUi.ui(), path, Activity.RESULT_OK);
                    }
                })
                .compose(this.<Response<T, String>>applyOnError());
    }

    private String[] permissions() {
        if (config.useInternalStorage()) {
            return new String[]{Manifest.permission.CAMERA};
        } else {
            return new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA};
        }
    }
}
