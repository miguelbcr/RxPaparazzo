package com.miguelbcr.ui.rx_paparazzo2.sample;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import androidx.annotation.Nullable;
import com.miguelbcr.ui.rx_paparazzo2.RxPaparazzo;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

/**
 * Created by miguel on 16/03/2016.
 */
public class SampleApplication extends Application {

    private RefWatcher refWatcher;

    @Override public void onCreate() {
        super.onCreate();
        RxPaparazzo.register(this)
            .withFileProviderAuthority("")
            .withFileProviderPath("");

        AppCare.YesSir.takeCareOn(this);

        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        refWatcher = LeakCanary.install(this);
    }

    @Nullable
    public Activity getLiveActivity(){
        return AppCare.YesSir.getLiveActivityOrNull();
    }

    public static RefWatcher getRefWatcher(Context context) {
        SampleApplication application = (SampleApplication) context.getApplicationContext();

        return application.refWatcher;
    }

}
