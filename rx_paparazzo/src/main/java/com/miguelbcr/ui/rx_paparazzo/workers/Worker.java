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

package com.miguelbcr.ui.rx_paparazzo.workers;

import android.app.Activity;

import com.miguelbcr.ui.rx_paparazzo.RxPaparazzo;
import com.miguelbcr.ui.rx_paparazzo.entities.PermissionDeniedException;
import com.miguelbcr.ui.rx_paparazzo.entities.Response;
import com.miguelbcr.ui.rx_paparazzo.entities.TargetUi;
import com.miguelbcr.ui.rx_paparazzo.entities.UserCanceledException;

import rx.Observable;
import rx.exceptions.Exceptions;
import rx.functions.Func1;

abstract class Worker {

    private final TargetUi targetUi;

    public Worker(TargetUi targetUi) {
        this.targetUi = targetUi;
    }

    @SuppressWarnings("unchecked")
    protected <T> Observable.Transformer<T, T> applyOnError() {
        return new Observable.Transformer<T, T>() {
            @Override
            public Observable<T> call(Observable<T> observable) {
                return observable.onErrorResumeNext(new Func1<Throwable, Observable<? extends T>>() {
                    @Override
                    public Observable<? extends T> call(Throwable throwable) {
                        if (throwable instanceof UserCanceledException) {
                            return Observable.just((T) new Response(targetUi.ui(), null, Activity.RESULT_CANCELED));
                        } else if (throwable instanceof PermissionDeniedException) {
                            return Observable.just((T) new Response(targetUi.ui(), null, RxPaparazzo.RESULT_DENIED_PERMISSION));
                        }
                        throw Exceptions.propagate(throwable);
                    }
                });
            }
        };
    }
}
