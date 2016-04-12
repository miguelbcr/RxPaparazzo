package com.refineriaweb.rx_paparazzo.sample;

import android.app.Application;

import rx_activity_result.RxActivityResult;

/**
 * Created by miguel on 16/03/2016.
 */
public class SampleApplication extends Application {

    @Override public void onCreate() {
        super.onCreate();
        RxActivityResult.register(this);
    }

}
