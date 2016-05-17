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

import android.Manifest;

import com.fuck_boilerplate.rx_paparazzo.entities.TargetUi;
import com.fuck_boilerplate.rx_paparazzo.entities.UserCanceledException;
import com.tbruyelle.rxpermissions.RxPermissions;

import javax.inject.Inject;

import rx.Observable;

public final class GrantPermissions extends UseCase<Void> {
    private final TargetUi targetUi;

    @Inject public GrantPermissions(TargetUi targetUi) {
        this.targetUi = targetUi;
    }

    @Override public Observable<Void> react() {
        return requestPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    private Observable<Void> requestPermissions(String... permissionsString) {
        RxPermissions permissions = RxPermissions.getInstance(targetUi.activity());
        return permissions.request(permissionsString)
                 .flatMap(granted -> {
                     if (granted) return Observable.<Void>just(null);
                     throw new UserCanceledException();
                 });
    }

    public Observable<Void> reactAlsoWithCameraPermission() {
        return requestPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA);
    }
}
