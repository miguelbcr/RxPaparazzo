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

import android.Manifest;

import com.refineriaweb.rx_paparazzo.library.entities.Config;
import com.refineriaweb.rx_paparazzo.library.entities.Folder;
import com.refineriaweb.rx_paparazzo.library.entities.TargetUi;
import com.refineriaweb.rx_paparazzo.library.entities.UserCanceledException;
import com.tbruyelle.rxpermissions.RxPermissions;

import javax.inject.Inject;

import rx.Observable;

public final class GrantPermissions extends UseCase<Void> {
    private final TargetUi targetUi;
    private final Config config;

    @Inject public GrantPermissions(TargetUi targetUi, Config config) {
        this.targetUi = targetUi;
        this.config = config;
    }

    @Override public Observable<Void> react() {
        RxPermissions permissions = RxPermissions.getInstance(targetUi.activity());

        return Observable.just(config.getFolder() == Folder.Private)
                .flatMap(privateFolder -> {
                    if (privateFolder) return Observable.just(true);
                    if (permissions.isGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE) && permissions.isGranted(Manifest.permission.READ_EXTERNAL_STORAGE))
                        return Observable.just(true);
                    return permissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE);
                })
                 .flatMap(granted -> {
                     if (granted) return Observable.<Void>just(null);
                     throw new UserCanceledException();
                 });
    }
}
