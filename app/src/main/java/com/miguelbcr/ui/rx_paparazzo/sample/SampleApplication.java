package com.miguelbcr.ui.rx_paparazzo.sample;

import android.app.Activity;
import android.app.Application;
import android.support.annotation.Nullable;

import com.miguelbcr.ui.rx_paparazzo.RxPaparazzo;
import com.squareup.leakcanary.LeakCanary;

/**
 * Created by miguel on 16/03/2016.
 */
public class SampleApplication extends Application {

    @Override public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);

        RxPaparazzo.register(this);
        AppCare.YesSir.takeCareOn(this);
    }

    @Nullable
    public Activity getLiveActivity(){
        return AppCare.YesSir.getLiveActivityOrNull();
    }
}
