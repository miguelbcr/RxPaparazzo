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
import android.provider.MediaStore;

import com.refineriaweb.rx_paparazzo.library.entities.TargetUi;

import java.io.File;

import javax.inject.Inject;

import rx.Observable;

public final class TakePhoto extends UseCase<Uri> {
    private final StartIntent startIntent;
    private final TargetUi targetUi;

    @Inject  public TakePhoto(StartIntent startIntent, TargetUi targetUi) {
        this.startIntent = startIntent;
        this.targetUi = targetUi;
    }

    @Override public Observable<Uri> react() {
        Uri uri = getUri();
        return startIntent.with(getIntentCamera(uri)).react()
                .map(data -> uri);
    }

    private Uri getUri() {
        File file = targetUi.activity().getExternalCacheDir();

        return Uri.fromFile(file)
                .buildUpon()
                .appendPath("shoot.jpg")
                .build();
    }

    private Intent getIntentCamera(Uri uri) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        return intent;
    }
}
