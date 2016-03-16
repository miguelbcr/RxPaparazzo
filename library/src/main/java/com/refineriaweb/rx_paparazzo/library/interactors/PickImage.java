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

import com.refineriaweb.rx_paparazzo.library.entities.TargetUi;

import javax.inject.Inject;

import rx.Observable;

public final class PickImage extends UseCase<Uri> {
    private final StartIntent startIntent;
    private final TargetUi targetUi;

    @Inject public PickImage(StartIntent startIntent, TargetUi targetUi) {
        this.startIntent = startIntent;
        this.targetUi = targetUi;
    }

    @Override public Observable<Uri> react() {
        return startIntent.with(getFileChooserIntent()).react()
                .map(intent -> intent.getData());
    }

    private Intent getFileChooserIntent() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        return intent;
    }
}
