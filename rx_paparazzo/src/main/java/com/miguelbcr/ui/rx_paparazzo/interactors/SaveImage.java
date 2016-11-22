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

package com.miguelbcr.ui.rx_paparazzo.interactors;

import android.media.MediaScannerConnection;
import android.net.Uri;

import com.miguelbcr.ui.rx_paparazzo.entities.Config;
import com.miguelbcr.ui.rx_paparazzo.entities.TargetUi;

import java.io.File;

import rx.Observable;
import rx.functions.Func1;
import rx.functions.Func3;

public final class SaveImage extends UseCase<String> {
    private final TargetUi targetUi;
    private final GetPath getPath;
    private final GetDimens getDimens;
    private final ImageUtils imageUtils;
    private Uri uri;

    public SaveImage(TargetUi targetUi, GetPath getPath, GetDimens getDimens, ImageUtils imageUtils) {
        this.targetUi = targetUi;
        this.getPath = getPath;
        this.getDimens = getDimens;
        this.imageUtils = imageUtils;
    }

    public SaveImage with(Uri uri) {
        this.uri = uri;
        return this;
    }

    @Override
    public Observable<String> react() {
        return getOutputUri()
                .flatMap(new Func1<Uri, Observable<String>>() {
                    @Override
                    public Observable<String> call(Uri outputUri) {
                        return Observable.zip(
                                getPath.with(uri).react(),
                                getPath.with(outputUri).react(),
                                getDimens.with(uri).react(),
                                new Func3<String, String, int[], String>() {
                                    @Override
                                    public String call(String filePath, String filePathOutput, int[] dimens) {
                                        String filePathScaled = imageUtils.scaleImage(filePath, filePathOutput, dimens);
                                        new File(filePath).delete();

                                        MediaScannerConnection.scanFile(
                                                targetUi.getContext(),
                                                new String[]{filePathScaled},
                                                new String[]{"image/*"},
                                                null);

                                        return filePathScaled;
                                    }
                                });
                    }
                });
    }

    private Observable<Uri> getOutputUri() {
        return getPath.with(uri).react()
                .flatMap(new Func1<String, Observable<Uri>>() {
                    @Override
                    public Observable<Uri> call(String filepath) {
                        String extension = imageUtils.getFileExtension(filepath);
                        return Observable.just(Uri.fromFile(imageUtils.getOutputFile(extension)));
                    }
                });
    }
}
