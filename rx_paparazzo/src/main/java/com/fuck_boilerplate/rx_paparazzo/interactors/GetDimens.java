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

import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.DisplayMetrics;

import com.fuck_boilerplate.rx_paparazzo.entities.Config;
import com.fuck_boilerplate.rx_paparazzo.entities.Size;
import com.fuck_boilerplate.rx_paparazzo.entities.TargetUi;

import javax.inject.Inject;

import rx.Observable;

public final class GetDimens extends UseCase<int[]> {
    private final TargetUi targetUi;
    private final Config config;
    private final GetPath getPath;
    private Uri uri;

    @Inject public GetDimens(TargetUi targetUi, Config config, GetPath getPath) {
        this.targetUi = targetUi;
        this.config = config;
        this.getPath = getPath;
    }

    public GetDimens with(Uri uri) {
        this.uri = uri;
        return this;
    }

    @Override public Observable<int[]> react() {
        return getPath.with(uri).react()
                .map(filePath -> {
                    if (config.getSize() == Size.Original)
                        return getFileDimens(filePath);
                    else if (config.getSize() == Size.Screen)
                        return getScreenDimens();
                    else {
                        int[] dimens = getScreenDimens();
                        return new int[]{dimens[0] / 8, dimens[1] / 8};
                    }
                });
    }

    private int[] getScreenDimens() {
        DisplayMetrics metrics = targetUi.getContext().getResources().getDisplayMetrics();
        return new int[] {metrics.widthPixels, metrics.heightPixels};
    }

    private int[] getFileDimens(String filePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        return new int[] {options.outWidth, options.outHeight};
    }
}
