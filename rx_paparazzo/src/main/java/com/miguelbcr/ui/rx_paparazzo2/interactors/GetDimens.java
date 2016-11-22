/*
 * Copyright 2016 Miguel Garcia
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

package com.miguelbcr.ui.rx_paparazzo2.interactors;

import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.DisplayMetrics;
import com.miguelbcr.ui.rx_paparazzo2.entities.Config;
import com.miguelbcr.ui.rx_paparazzo2.entities.TargetUi;
import com.miguelbcr.ui.rx_paparazzo2.entities.size.CustomMaxSize;
import com.miguelbcr.ui.rx_paparazzo2.entities.size.OriginalSize;
import com.miguelbcr.ui.rx_paparazzo2.entities.size.ScreenSize;
import io.reactivex.Observable;
import io.reactivex.functions.Function;

public final class GetDimens extends UseCase<int[]> {
    private final TargetUi targetUi;
    private final Config config;
    private final GetPath getPath;
    private Uri uri;

    public GetDimens(TargetUi targetUi, Config config, GetPath getPath) {
        this.targetUi = targetUi;
        this.config = config;
        this.getPath = getPath;
    }

    public GetDimens with(Uri uri) {
        this.uri = uri;
        return this;
    }

    @Override
    public Observable<int[]> react() {
        return getPath.with(uri).react()
            .map(new Function<String, int[]>() {
                @Override public int[] apply(String filePath) throws Exception {
                    if (config.getSize() instanceof OriginalSize) {
                        return GetDimens.this.getFileDimens(filePath);
                    } else if (config.getSize() instanceof CustomMaxSize) {
                        CustomMaxSize customMaxSize = (CustomMaxSize) config.getSize();
                        return getCustomDimens(customMaxSize, filePath);
                    } else if (config.getSize() instanceof ScreenSize) {
                        return GetDimens.this.getScreenDimens();
                    } else {
                        int[] dimens = GetDimens.this.getScreenDimens();
                        return new int[]{dimens[0] / 8, dimens[1] / 8};
                    }
                }
            });
    }

    private int[] getScreenDimens() {
        DisplayMetrics metrics = targetUi.getContext().getResources().getDisplayMetrics();
        return new int[]{metrics.widthPixels, metrics.heightPixels};
    }

    private int[] getFileDimens(String filePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        return new int[]{options.outWidth, options.outHeight};
    }

    private int[] getCustomDimens(CustomMaxSize customMaxSize, String filePath) {
        int maxSize = customMaxSize.getMaxImageSize();
        int[] dimens = GetDimens.this.getFileDimens(filePath);
        int maxFileSize = Math.max(dimens[0], dimens[1]);
        if (maxFileSize < maxSize) {
            return dimens;
        }
        float scaleFactor = (float) maxSize / maxFileSize;
        dimens[0] *= scaleFactor;
        dimens[1] *= scaleFactor;
        return dimens;
    }
}
