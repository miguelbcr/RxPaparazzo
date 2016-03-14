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

package rx_paparazzo.interactors;

import android.content.Context;
import android.net.Uri;

import rx.Observable;

public class SaveImage extends UseCase<String> {
    private Uri uri;
    private Context context;

    public SaveImage with(Context context, Uri uri) {
        this.context = context;
        this.uri = uri;
        return this;
    }

    @Override public Observable<String> react() {

        return Observable.just(null);
    }
}
