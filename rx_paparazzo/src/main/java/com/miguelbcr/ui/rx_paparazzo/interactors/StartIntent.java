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

package com.miguelbcr.ui.rx_paparazzo.interactors;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;

import com.miguelbcr.ui.rx_paparazzo.entities.TargetUi;
import com.miguelbcr.ui.rx_paparazzo.entities.UserCanceledException;

import rx.Observable;
import rx.functions.Func1;
import rx_activity_result.OnPreResult;
import rx_activity_result.Result;
import rx_activity_result.RxActivityResult;

public final class StartIntent extends UseCase<Intent> {
    private final TargetUi targetUi;
    private Intent intent;
    private OnPreResult onPreResult;

    public StartIntent(TargetUi targetUi) {
        this.targetUi = targetUi;
    }

    StartIntent with(Intent intent) {
        this.intent = intent;
        this.onPreResult = null;
        return this;
    }

    StartIntent with(Intent intent, OnPreResult onPreResult) {
        this.intent = intent;
        this.onPreResult = onPreResult;
        return this;
    }

    @Override public Observable<Intent> react() {
        final Fragment fragment = targetUi.fragment();
        if (fragment != null) {
            return RxActivityResult.on(fragment)
                    .startIntent(intent, onPreResult)
                    .map(new Func1<Result<Fragment>, Intent>() {
                        @Override
                        public Intent call(Result<Fragment> result) {
                            return StartIntent.this.getResponse(result);
                        }
                    });
        } else {
            return RxActivityResult.on(targetUi.activity())
                    .startIntent(intent, onPreResult)
                    .map(new Func1<Result<Activity>, Intent>() {
                        @Override
                        public Intent call(Result<Activity> result) {
                            return StartIntent.this.getResponse(result);
                        }
                    });
        }
    }

    private Intent getResponse(Result result) {
        targetUi.setUi(result.targetUI());
        if (result.resultCode() != Activity.RESULT_OK) {
            throw new UserCanceledException();
        }
        return result.data();
    }
}
