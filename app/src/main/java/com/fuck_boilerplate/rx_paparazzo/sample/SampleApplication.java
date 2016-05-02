package com.fuck_boilerplate.rx_paparazzo.sample;

import android.app.Activity;
import android.app.Application;
import android.support.annotation.Nullable;

import com.fuck_boilerplate.rx_paparazzo.RxPaparazzo;

/**
 * Created by miguel on 16/03/2016.
 */
public class SampleApplication extends Application {

    @Override public void onCreate() {
        super.onCreate();
        RxPaparazzo.register(this);
        AppCare.YesSir.takeCareOn(this);
    }

    @Nullable
    public Activity getLiveActivity(){
        return AppCare.YesSir.getLiveActivityOrNull();
    }
}
