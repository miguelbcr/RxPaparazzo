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

import com.fuck_boilerplate.rx_paparazzo.entities.PermissionDeniedException;
import com.fuck_boilerplate.rx_paparazzo.entities.TargetUi;
import com.tbruyelle.rxpermissions.RxPermissions;

import javax.inject.Inject;

import rx.Observable;

public final class GrantPermissions extends UseCase<Void> {
    private final TargetUi targetUi;
    private String[] permissions;

    @Inject public GrantPermissions(TargetUi targetUi) {
        this.targetUi = targetUi;
    }

    public GrantPermissions with(String... permissions) {
        this.permissions = permissions;
        return this;
    }

    @Override public Observable<Void> react() {
        return RxPermissions.getInstance(targetUi.activity())
                .request(permissions)
                .flatMap(granted -> {
                    if (granted) return Observable.<Void>just(null);
                    throw new PermissionDeniedException();
                });
    }
}
