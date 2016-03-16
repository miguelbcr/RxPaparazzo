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
import android.content.Context;

import com.refineriaweb.rx_paparazzo.library.entities.Folder;
import com.tbruyelle.rxpermissions.RxPermissions;

import rx.Observable;

public class GrantPermissions extends UseCase<Void> {
    private Context context;
    private Folder folder;

    public GrantPermissions with(Context context, Folder folder) {
        this.context = context;
        this.folder = folder;
        return this;
    }

    @Override public Observable<Void> react() {
        RxPermissions permissions = RxPermissions.getInstance(context);

        return Observable.just(folder == Folder.Private)
                .flatMap(privateFolder -> {
                    if (privateFolder) return Observable.just(true);
                    if (permissions.isGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)) return Observable.just(true);
                    return permissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                })
                .flatMap(granted -> granted ? Observable.<Void>just(null) : oBreakChain());
    }
}
