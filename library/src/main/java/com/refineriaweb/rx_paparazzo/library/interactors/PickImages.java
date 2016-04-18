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

package com.refineriaweb.rx_paparazzo.library.interactors;

import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;

public final class PickImages extends UseCase<List<Uri>>{
    private final StartIntent startIntent;

    @Inject public PickImages(StartIntent startIntent) {
        this.startIntent = startIntent;
    }

    @Override public Observable<List<Uri>> react() {
        return startIntent.with(getFileChooserIntent()).react()
                .map(intent -> {
                    if (intent.getData() != null) return Arrays.asList(intent.getData());
                    else return getUris(intent);
                });
    }

    private List<Uri> getUris(Intent intent) {
        List<Uri> uris = new ArrayList<>();
        ClipData clipData = intent.getClipData();

        if (clipData != null) {
            for (int i = 0; i < clipData.getItemCount(); i++) {
                ClipData.Item item = clipData.getItemAt(i);
                Uri uri = item.getUri();
                uris.add(uri);
            }
        }

        return uris;
    }

    private Intent getFileChooserIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            intent.setAction(Intent.ACTION_GET_CONTENT);
        } else {
            intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);

        return intent;
    }
}
