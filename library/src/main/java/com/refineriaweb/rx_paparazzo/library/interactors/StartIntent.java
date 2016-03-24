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

import android.app.Activity;
import android.content.Intent;

import com.refineriaweb.rx_paparazzo.library.entities.TargetUi;
import com.refineriaweb.rx_paparazzo.library.entities.UserCanceledException;

import javax.inject.Inject;

import rx.Observable;
import rx_activity_result.Result;
import rx_activity_result.RxActivityResult;

final class StartIntent extends UseCase<Intent> {
    private final TargetUi targetUi;
    private Intent intent;

    @Inject public StartIntent(TargetUi targetUi) {
        this.targetUi = targetUi;
    }

    StartIntent with(Intent intent) {
        this.intent = intent;
        return this;
    }

    @Override public Observable<Intent> react() {
        if (targetUi.fragment() != null) {
            return RxActivityResult.on(targetUi.fragment())
                    .startIntent(intent)
                    .map(this::getResponse);
        } else {
            return RxActivityResult.on(targetUi.activity())
                    .startIntent(intent)
                    .map(this::getResponse);
        }
    }

    private Intent getResponse(Result result) {
        targetUi.setUi(result.targetUI());
        if (result.resultCode() != Activity.RESULT_OK) throw new UserCanceledException();
        return result.data();
    }
}
