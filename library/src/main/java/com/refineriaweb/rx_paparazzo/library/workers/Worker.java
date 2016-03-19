package com.refineriaweb.rx_paparazzo.library.workers;

import android.app.Activity;

import com.refineriaweb.rx_paparazzo.library.entities.Response;
import com.refineriaweb.rx_paparazzo.library.entities.TargetUi;
import com.refineriaweb.rx_paparazzo.library.entities.UserCanceledException;

import rx.Observable;
import rx.exceptions.Exceptions;


abstract class Worker {
    private final TargetUi targetUi;

    public Worker(TargetUi targetUi) {
        this.targetUi = targetUi;
    }

    protected <T> Observable.Transformer<T, T> applyOnError() {
        return (observable) -> {
            return observable.onErrorResumeNext(throwable -> {
                if (throwable instanceof UserCanceledException) {
                    return Observable.just((T) new Response(targetUi.ui(), null, Activity.RESULT_CANCELED));
                }
                throw Exceptions.propagate(throwable);
            });
        };
    }
}
