package com.refineriaweb.rx_paparazzo.sample;

import android.app.Application;
import android.support.annotation.VisibleForTesting;

import com.refineriaweb.rx_paparazzo.library.internal.di.ApplicationComponent;

import rx_activity_result.RxActivityResult;

/**
 * Created by miguel on 16/03/2016.
 */
public class MyApplication extends Application {
    private ApplicationComponent applicationComponent;

    @Override public void onCreate() {
        super.onCreate();
        RxActivityResult.register(this);
    }

    /**
     * Visible only for testing purposes.
     */
    @VisibleForTesting
    public void setTestComponent(ApplicationComponent appComponent) {
        applicationComponent = appComponent;
    }
}
