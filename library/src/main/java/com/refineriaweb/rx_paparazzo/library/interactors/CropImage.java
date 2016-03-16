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

package com.refineriaweb.rx_paparazzo.library.interactors;

import android.net.Uri;

import com.refineriaweb.rx_paparazzo.library.entities.Config;

import javax.inject.Inject;

import rx.Observable;

public final class CropImage extends UseCase<Uri> {
    private final Config config;
    private Uri uri;

    @Inject public CropImage(Config config) {
        this.config = config;
    }

    public CropImage with(Uri uri) {
        this.uri = uri;
        return this;
    }

    @Override public Observable<Uri> react() {
        if (config.doCrop()) return Observable.just(null);
        else return Observable.just(uri);
    }
}
