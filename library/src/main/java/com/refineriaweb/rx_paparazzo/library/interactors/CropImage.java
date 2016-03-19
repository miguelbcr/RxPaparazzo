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

import android.content.Intent;
import android.net.Uri;

import com.refineriaweb.rx_paparazzo.library.entities.Config;
import com.refineriaweb.rx_paparazzo.library.entities.TargetUi;
import com.yalantis.ucrop.UCrop;

import java.io.File;

import javax.inject.Inject;

import rx.Observable;

public final class CropImage extends UseCase<Uri> {
    private final Config config;
    private final StartIntent startIntent;
    private final GetPath getPath;
    private final TargetUi targetUi;
    private final GetDimens getDimens;
    private Uri uri;

    @Inject public CropImage(TargetUi targetUi, Config config, StartIntent startIntent, GetPath getPath, GetDimens getDimens) {
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
            return getIntent().flatMap(intent ->
                    startIntent.with(intent).react()
                        .map(intent1 -> UCrop.getOutput(intent))
            );
        }

        return Observable.just(uri);
    }

    public Observable<Intent> getIntent() {
        return Observable.zip(getOutputUri(), getDimens.with(uri).react(), (outputUri, dimens) ->
                UCrop.of(uri, outputUri)
                    .withMaxResultSize(dimens[0], dimens[1])
                    .withOptions(config.getOptions())
                    .getIntent(targetUi.getContext())
        );
    }

    private Observable<Uri> getOutputUri() {
        return getPath.with(uri).react()
                .map(filePath -> {
                    File file = new File(filePath);
                    return Uri.fromFile(new File(targetUi.getContext().getExternalCacheDir(), "cropped-" + file.getName()));
                });
    }
}
