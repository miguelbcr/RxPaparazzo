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
import android.os.Build;

import com.fuck_boilerplate.rx_paparazzo.entities.TargetUi;

import rx.Observable;
import rx.functions.Func1;

public final class PickImage extends UseCase<Uri> {
    private final StartIntent startIntent;

     public PickImage(StartIntent startIntent) {
        this.startIntent = startIntent;
    }

    @Override public Observable<Uri> react() {
        return startIntent.with(getFileChooserIntent()).react()
                .map(new Func1<Intent, Uri>() {
                    @Override
                    public Uri call(Intent intent) {
                        return intent.getData();
                    }
                });
    }

    private Intent getFileChooserIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        return intent;
    }
}
