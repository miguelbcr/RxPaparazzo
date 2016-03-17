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

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.refineriaweb.rx_paparazzo.library.entities.Config;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropActivity;

import java.io.File;

import javax.inject.Inject;

import rx.Observable;

public final class CropImage extends UseCase<Uri> {
    private final Config config;
    private final StartIntent startIntent;
    private final GetPath getPath;
    private Context context;
    private Uri uri;

    @Inject public CropImage(Context context, Config config, StartIntent startIntent, GetPath getPath) {
        this.context = context;
        this.config = config;
        this.startIntent = startIntent;
        this.getPath = getPath;
    }

    public CropImage with(Uri uri) {
        this.uri = uri;
        return this;
    }

    @Override public Observable<Uri> react() {
        if (config.doCrop()) {
            return getOutputUri().flatMap(outputUri ->
                startIntent.with(getIntent(outputUri)).react()
                        .map(UCrop::getOutput)
            );
        }

        return Observable.just(uri);
    }

    public Intent getIntent(Uri outputUri) {
        Intent intent = new Intent(context, UCropActivity.class);
        Bundle cropOptionsBundle = new Bundle();
        cropOptionsBundle.putParcelable(UCrop.EXTRA_INPUT_URI, uri);
        cropOptionsBundle.putParcelable(UCrop.EXTRA_OUTPUT_URI, outputUri);
        intent.putExtras(cropOptionsBundle);
        return intent;
    }

    private Observable<Uri> getOutputUri() {
        return getPath.with(uri).react()
                .map(filePath -> {
                    File file = new File(filePath);
                    return Uri.fromFile(new File(context.getExternalCacheDir(), "cropped-" + file.getName()));
                });
    }
}
