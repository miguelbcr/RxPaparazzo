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

import com.fuck_boilerplate.rx_paparazzo.entities.Config;
import com.fuck_boilerplate.rx_paparazzo.entities.TargetUi;

import java.io.File;

import rx.Observable;
import rx.functions.Func1;

public final class PickImage extends UseCase<Uri> {
    private final StartIntent startIntent;
    private final TargetUi targetUi;
    private Config config;

    public PickImage(StartIntent startIntent, Config config, TargetUi targetUi) {
        this.startIntent = startIntent;
        this.targetUi = targetUi;
        this.config = config;
    }

    @Override
    public Observable<Uri> react() {
        return startIntent.with(getFileChooserIntent()).react()
                .map(new Func1<Intent, Uri>() {
                    @Override
                    public Uri call(Intent intent) {
                        if (intent.getData() != null && config.getDirPath() != null) {
                            File file = new File(config.getDirPath());
                            if (file != null && !file.exists()) {
                                file.mkdirs();
                            }
                        }
                        return intent.getData();
                    }
                });
    }

    private Intent getFileChooserIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            intent.setAction(Intent.ACTION_GET_CONTENT);
        } else {
            intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }

        return intent;
    }
}
