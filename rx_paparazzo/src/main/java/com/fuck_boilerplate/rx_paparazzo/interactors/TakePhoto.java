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

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;

import com.fuck_boilerplate.rx_paparazzo.entities.TargetUi;

import java.io.File;
import java.util.List;

import rx.Observable;
import rx.functions.Func1;

public final class TakePhoto extends UseCase<Uri> {
    private static final int READ_WRITE_PERMISSIONS = Intent.FLAG_GRANT_READ_URI_PERMISSION
            | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
    private static final String SHOOT_APPEND = "shoot.jpg";
    private final StartIntent startIntent;
    private final TargetUi targetUi;

      public TakePhoto(StartIntent startIntent, TargetUi targetUi) {
        this.startIntent = startIntent;
        this.targetUi = targetUi;
    }

    @Override public Observable<Uri> react() {
        final Uri uri = getUri();
        return startIntent.with(getIntentCamera(uri)).react()
                .map(new Func1<Intent, Uri>() {
                    @Override
                    public Uri call(Intent data) {
                        revokeFileReadWritePermissions(uri);
                        return uri;
                    }
                });
    }

    private Uri getUri() {
        Context context = targetUi.getContext();
        File dir = new File(context.getFilesDir(), getApplicationName(context));
        File file = new File(dir, SHOOT_APPEND);

        return FileProvider.getUriForFile(context, context.getPackageName() + ".file_provider", file);
    }

    private String getApplicationName(Context context) {
        int stringId = context.getApplicationInfo().labelRes;
        return context.getString(stringId);
    }

    private Intent getIntentCamera(Uri uri) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.addFlags(READ_WRITE_PERMISSIONS);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        grantFileReadWritePermissions(intent, uri);
        return intent;
    }

    /**
     * Workaround for Android bug.<br/>
     * See https://code.google.com/p/android/issues/detail?id=76683 <br/>
     * See http://stackoverflow.com/questions/18249007/how-to-use-support-fileprovider-for-sharing-content-to-other-apps
     * @param intent
     * @param uri
     */
    private void grantFileReadWritePermissions(Intent intent, Uri uri) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            List<ResolveInfo> resInfoList = targetUi.getContext().getPackageManager()
                    .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo resolveInfo : resInfoList) {
                String packageName = resolveInfo.activityInfo.packageName;
                targetUi.getContext().grantUriPermission(packageName, uri, READ_WRITE_PERMISSIONS);
            }
        }
    }

    public void revokeFileReadWritePermissions(Uri uri) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            targetUi.getContext().revokeUriPermission(uri, READ_WRITE_PERMISSIONS);
        }
    }
}
