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

package com.miguelbcr.ui.rx_paparazzo2.workers;

import android.app.Activity;

import com.miguelbcr.ui.rx_paparazzo2.RxPaparazzo;
import com.miguelbcr.ui.rx_paparazzo2.entities.PermissionDeniedException;
import com.miguelbcr.ui.rx_paparazzo2.entities.Response;
import com.miguelbcr.ui.rx_paparazzo2.entities.TargetUi;
import com.miguelbcr.ui.rx_paparazzo2.entities.UserCanceledException;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.functions.Function;

abstract class Worker {

    private final TargetUi targetUi;

    public Worker(TargetUi targetUi) {
        this.targetUi = targetUi;
    }

    @SuppressWarnings("unchecked")
    protected <T> ObservableTransformer<T, T> applyOnError() {
        return new ObservableTransformer<T, T>() {
            @Override public ObservableSource<T> apply(Observable<T> observable) {
                return observable.onErrorResumeNext(new Function<Throwable, ObservableSource<? extends T>>() {
                    @Override public ObservableSource<? extends T> apply(Throwable throwable)
                        throws Exception {
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
