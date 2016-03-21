package rx_activity_result;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.functions.Func1;

class ActivitiesLifecycleCallbacks {
    private final Application application;
    private Activity liveActivityOrNull;
    private Application.ActivityLifecycleCallbacks activityLifecycleCallbacks;

    public ActivitiesLifecycleCallbacks(Application application) {
        this.application = application;
        registerActivityLifeCycle();
    }

    private void registerActivityLifeCycle() {
        if (activityLifecycleCallbacks != null) application.unregisterActivityLifecycleCallbacks(activityLifecycleCallbacks);

        activityLifecycleCallbacks = new Application.ActivityLifecycleCallbacks() {
            @Override public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                liveActivityOrNull = activity;
            }

            @Override public void onActivityStarted(Activity activity) {}

            @Override public void onActivityResumed(Activity activity) {
                liveActivityOrNull = activity;
            }

            @Override public void onActivityPaused(Activity activity) {
                liveActivityOrNull = null;
            }

            @Override public void onActivityStopped(Activity activity) {}

            @Override public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}

            @Override public void onActivityDestroyed(Activity activity) {}
        };

        application.registerActivityLifecycleCallbacks(activityLifecycleCallbacks);
    }

    Activity getLiveActivityOrNull() {
        return liveActivityOrNull;
    }

    /**
     * Emits just one time a valid reference to the current activity
     * @return the current activity
     */
    private boolean emitted = false;
    Observable<Activity> getOLiveActivity() {
        emitted = false;
        return Observable.interval(50, 50, TimeUnit.MILLISECONDS)
                .map(new Func1<Long, Activity>() {
                    @Override public Activity call(Long aLong) {
                        return liveActivityOrNull;
                    }
                })
                .takeWhile(new Func1<Activity, Boolean>() {
                    @Override public Boolean call(Activity activity) {
                        boolean continueEmitting = true;
                        if (emitted) continueEmitting = false;
                        if (activity != null) emitted = true;
                        return continueEmitting;
                    }
                })
                .filter(new Func1<Activity, Boolean>() {
                    @Override public Boolean call(Activity activity) {
                        return activity != null;
                    }
                });
    }

}
