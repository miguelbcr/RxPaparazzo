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

package com.miguelbcr.ui.rx_paparazzo2.interactors;

import android.app.Activity;
import com.miguelbcr.ui.rx_paparazzo2.RxPaparazzo;
import com.miguelbcr.ui.rx_paparazzo2.entities.Ignore;
import com.miguelbcr.ui.rx_paparazzo2.entities.PermissionDeniedException;
import com.miguelbcr.ui.rx_paparazzo2.entities.TargetUi;
import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;
import java.util.List;

public final class GrantPermissions extends UseCase<Ignore> {
  private final TargetUi targetUi;
  private String[] permissions;

  public GrantPermissions(TargetUi targetUi) {
    this.targetUi = targetUi;
  }

  public GrantPermissions with(String... permissions) {
    this.permissions = permissions;
    return this;
  }

  @Override public Observable<Ignore> react() {
    if (permissions.length == 0) {
      return Observable.just(Ignore.Get);
    }

    return new RxPermissions(targetUi.activity())
        .requestEach(permissions)
        .buffer(permissions.length)
        .flatMapIterable(new Function<List<Permission>, Iterable<Permission>>() {
          @Override public Iterable<Permission> apply(List<Permission> permissions)
              throws Exception {
            return permissions;
          }
        })
        .concatMap(new Function<Permission, ObservableSource<Integer>>() {
          @Override public ObservableSource<Integer> apply(Permission permission) throws Exception {
            if (permission.granted) {
              return Observable.just(Activity.RESULT_OK);
            } else if (permission.shouldShowRequestPermissionRationale) {
              return Observable.just(RxPaparazzo.RESULT_DENIED_PERMISSION);
            } else {
              return Observable.just(RxPaparazzo.RESULT_DENIED_PERMISSION_NEVER_ASK);
            }
          }
        })
        .toList()
        .flatMap(new Function<List<Integer>, SingleSource<Ignore>>() {
          @Override public SingleSource<Ignore> apply(List<Integer> resultCodes) throws Exception {
            int maxResultCode = Activity.RESULT_OK;
            for (int resultCode : resultCodes) {
              if (resultCode > maxResultCode) {
                maxResultCode = resultCode;
              }
            }

            if (maxResultCode == Activity.RESULT_OK) {
              return Single.just(Ignore.Get);
            } else {
              return Single.error(new PermissionDeniedException(maxResultCode));
            }
          }
        })
        .toObservable();
  }
}
