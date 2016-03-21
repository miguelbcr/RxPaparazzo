/*
 * Copyright 2016 Víctor Albertos
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

package rx_activity_result;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;

public class RxActivityResult {
    private static ActivitiesLifecycleCallbacks activitiesLifecycle;

    public static void register(final Application application) {
        activitiesLifecycle = new ActivitiesLifecycleCallbacks(application);
    }

    public static <T extends Activity> Builder<T> on(T activity) {
        return new Builder<T>(activity);
    }

    public static <T extends Fragment> Builder<T> on(T fragment) {
        return new Builder<T>(fragment);
    }

    public static class Builder<T> {
        private final Class clazz;
        private Subscriber<? super Result<T>> subscriber;
        private final boolean uiTargetActivity;

        public Builder(T t) {
            if (activitiesLifecycle == null) {
                throw new IllegalStateException(Locale.RX_ACTIVITY_RESULT_NOT_REGISTER);
            }

            this.clazz = t.getClass();
            this.uiTargetActivity = t instanceof Activity;
        }

        public Observable<Result<T>> startIntent(final Intent intent) {
            Observable<Result<T>> observable = Observable.create(new Observable.OnSubscribe<Result<T>>() {
                @Override public void call(Subscriber<? super Result<T>> aSubscriber) {
                    subscriber = aSubscriber;
                }
            });

            OnResult onResult = uiTargetActivity ? onResultActivity() : onResultFragment();
            HolderActivity.setRequest(new Request(intent, onResult));

            activitiesLifecycle.getOLiveActivity().subscribe(new Action1<Activity>() {
                @Override public void call(Activity activity) {
                    activity.startActivity(new Intent(activity, HolderActivity.class));
                }
            });

            return observable;
        }

        private OnResult onResultActivity() {
            return new OnResult() {
                @Override public void response(int resultCode, Intent data) {
                    if (activitiesLifecycle.getLiveActivityOrNull() == null) return;

                    //If true it means some other activity has been stacked as a secondary process.
                    //Wait until the current activity be the target activity
                    if (activitiesLifecycle.getLiveActivityOrNull().getClass() != clazz) {
                        return;
                    }

                    T activity = (T) activitiesLifecycle.getLiveActivityOrNull();
                    subscriber.onNext(new Result<T>((T) activity, resultCode, data));
                    subscriber.onCompleted();
                }
            };
        }

        private OnResult onResultFragment() {
            return new OnResult() {
                @Override public void response(int resultCode, Intent data) {
                    if (activitiesLifecycle.getLiveActivityOrNull() == null) return;

                    Activity activity = activitiesLifecycle.getLiveActivityOrNull();

                    FragmentActivity fragmentActivity = (FragmentActivity) activity;
                    FragmentManager fragmentManager = fragmentActivity.getSupportFragmentManager();

                    List<Fragment> fragments = fragmentManager.getFragments();

                    if(fragments != null) {
                        for(Fragment fragment : fragments){
                            if(fragment != null && fragment.isVisible() && fragment.getClass() == clazz) {
                                subscriber.onNext(new Result<T>((T) fragment, resultCode, data));
                                subscriber.onCompleted();
                                return;
                            }
                        }
                    }

                    //If code reaches this point it means some other activity has been stacked as a secondary process.
                    //Wait until the current activity be the target activity to get the associated fragment
                }
            };
        }
    }
}
