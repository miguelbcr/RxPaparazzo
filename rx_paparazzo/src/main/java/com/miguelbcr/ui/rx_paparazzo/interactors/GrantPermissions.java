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

import com.miguelbcr.ui.rx_paparazzo.entities.PermissionDeniedException;
import com.miguelbcr.ui.rx_paparazzo.entities.TargetUi;
import com.tbruyelle.rxpermissions.RxPermissions;

import rx.Observable;
import rx.functions.Func1;

public final class GrantPermissions extends UseCase<Void> {
    private final TargetUi targetUi;
    private String[] permissions;

    public GrantPermissions(TargetUi targetUi) {
        this.targetUi = targetUi;
    }

    public GrantPermissions with(String... permissions) {
        this.permissions = permissions;
        return this;
    }

    @Override
    public Observable<Void> react() {
        if (permissions.length == 0) {
            return Observable.just(null);
        }

        return RxPermissions.getInstance(targetUi.activity())
                .request(permissions)
                .flatMap(new Func1<Boolean, Observable<Void>>() {
                    @Override
                    public Observable<Void> call(Boolean granted) {
                        if (granted) {
                            return Observable.just(null);
                        }
                        return Observable.error(new PermissionDeniedException());
                    }
                });
    }
}
