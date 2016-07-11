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

import android.net.Uri;

import com.fuck_boilerplate.rx_paparazzo.entities.TargetUi;
import com.fuck_boilerplate.rx_paparazzo.entities.UserCanceledException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.exceptions.Exceptions;
import rx.functions.Func0;
import rx.schedulers.Schedulers;

public final class DownloadImage extends UseCase<String> {
    private final TargetUi targetUi;
    private final ImageUtils imageUtils;
    private Uri uri;

    public DownloadImage(TargetUi targetUi, ImageUtils imageUtils) {
        this.targetUi = targetUi;
        this.imageUtils = imageUtils;
    }

    @Override Observable<String> react() {
        return getObservableDownloadFile();
    }

    public DownloadImage with(Uri uri) {
        this.uri = uri;
        return this;
    }

    private Observable<String> getObservableDownloadFile() {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                try {
                    if ("content".equalsIgnoreCase(uri.getScheme())){
                        subscriber.onNext(downloadContent());
                    }
                    else {
                        subscriber.onNext(downloadFile());
                    }

                    subscriber.onCompleted();
                } catch (Exception e) {
                    subscriber.onNext(null);
                    subscriber.onError(e);
                }
            }
        })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread());
    }

    private String downloadFile() throws Exception {
        URL url = new URL(uri.toString());
        URLConnection connection = url.openConnection();
        connection.connect();
        InputStream inputStream = new BufferedInputStream(url.openStream(), 1024);
        File file = imageUtils.getOutputFile();

        imageUtils.copy(inputStream, file);
        return file.getAbsolutePath();
    }

    private String downloadContent() throws Exception {
        InputStream inputStream = targetUi.getContext().getContentResolver().openInputStream(uri);
        File file = imageUtils.getOutputFile();
        imageUtils.copy(inputStream, file);
        return file.getAbsolutePath();
    }
}
