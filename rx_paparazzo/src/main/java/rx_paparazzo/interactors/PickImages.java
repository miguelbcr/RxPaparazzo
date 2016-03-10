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

import android.app.Activity;
import android.net.Uri;

import java.util.List;

import rx.Observable;

public class PickImages extends UseCase<List<Uri>>{
    private Activity activity;

    public PickImages with(Activity activity) {
        this.activity = activity;
        return this;
    }

    @Override public Observable<List<Uri>> react() {
        //if back return userBack()
        return Observable.just(null);
    }
}
