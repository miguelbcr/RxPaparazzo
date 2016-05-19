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

package com.fuck_boilerplate.rx_paparazzo.interactors;

import android.content.Intent;
import android.net.Uri;

import com.fuck_boilerplate.rx_paparazzo.entities.Config;
import com.fuck_boilerplate.rx_paparazzo.entities.TargetUi;
import com.yalantis.ucrop.UCrop;

import java.io.File;

import rx.Observable;
import rx.functions.Func1;

public final class CropImage extends UseCase<Uri> {
    private final Config config;
    private final StartIntent startIntent;
    private final GetPath getPath;
    private final TargetUi targetUi;
    private final GetDimens getDimens;
    private Uri uri;

    public CropImage(TargetUi targetUi, Config config, StartIntent startIntent, GetPath getPath, GetDimens getDimens) {
        this.targetUi = targetUi;
        this.config = config;
        this.startIntent = startIntent;
        this.getPath = getPath;
        this.getDimens = getDimens;
    }

    public CropImage with(Uri uri) {
        this.uri = uri;
        return this;
    }

    @Override public Observable<Uri> react() {
        if (config.doCrop()) {
            return getIntent().flatMap(new Func1<Intent, Observable<Uri>>() {
                @Override
                public Observable<Uri> call(Intent intent) {
                    return startIntent.with(intent).react().map(new Func1<Intent, Uri>() {
                        @Override
                        public Uri call(Intent intentResult) {
                            return UCrop.getOutput(intentResult);
                        }
                    });
                }
            });
        }

        return Observable.just(uri);
    }

    public Observable<Intent> getIntent() {
        return getOutputUri().map(new Func1<Uri, Intent>() {
            @Override
            public Intent call(Uri outputUri) {
                return UCrop.of(uri, outputUri).withOptions(config.getOptions())
                        .getIntent(targetUi.getContext());
            }
        });
    }

    private Observable<Uri> getOutputUri() {
        return getPath.with(uri).react().map(new Func1<String, Uri>() {
            @Override
            public Uri call(String filePath) {
                File file = targetUi.activity().getExternalCacheDir();
                return Uri.fromFile(file).buildUpon().appendPath("cropped.jpg").build();
            }
                });
    }
}
