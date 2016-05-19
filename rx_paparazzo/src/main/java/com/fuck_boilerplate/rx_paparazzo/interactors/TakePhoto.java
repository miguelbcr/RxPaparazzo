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
import android.provider.MediaStore;

import com.fuck_boilerplate.rx_paparazzo.entities.TargetUi;

import java.io.File;

import rx.Observable;
import rx.functions.Func1;

public final class TakePhoto extends UseCase<Uri> {
    private static final String SHOOT_APPEND = "shoot.jpg";
    private final StartIntent startIntent;
    private final TargetUi targetUi;

      public TakePhoto(StartIntent startIntent, TargetUi targetUi) {
        this.startIntent = startIntent;
        this.targetUi = targetUi;
    }

    @Override public Observable<Uri> react() {
        final Uri uri = getUri();
        return startIntent.with(getIntentCamera(uri)).react()
                .map(new Func1<Intent, Uri>() {
                    @Override
                    public Uri call(Intent data) {
                        return uri;
                    }
                });
    }

    private Uri getUri() {
        File file = targetUi.activity().getExternalCacheDir();

        return Uri.fromFile(file)
                .buildUpon()
                .appendPath(SHOOT_APPEND)
                .build();
    }

    private Intent getIntentCamera(Uri uri) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        return intent;
    }
}
