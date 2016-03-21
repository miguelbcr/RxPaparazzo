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

import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.DisplayMetrics;

import com.refineriaweb.rx_paparazzo.library.entities.Config;
import com.refineriaweb.rx_paparazzo.library.entities.Size;
import com.refineriaweb.rx_paparazzo.library.entities.TargetUi;

import javax.inject.Inject;

import rx.Observable;

public class GetDimens extends UseCase<int[]> {
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

    @Override Observable<int[]> react() {
        return getPath.with(uri).react()
                .map(filePath -> {
                    if (config.getSize() == Size.Original)
                        return getFileDimens(filePath);
                    else if (config.getSize() == Size.Normal)
                        return getScreenDimens();
                    else {
                        int[] dimens = getScreenDimens();
                        return new int[]{dimens[0] / 4, dimens[1] / 4};
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

    public void printDimens(String log, String filePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        System.out.println(log + options.outWidth + "x" + options.outHeight);
    }
}
